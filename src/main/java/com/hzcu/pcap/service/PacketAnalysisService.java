package com.hzcu.pcap.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hzcu.pcap.dto.AnalysisProgressEvent;
import com.hzcu.pcap.entity.AnalysisSummary;
import com.hzcu.pcap.entity.CaptureFile;
import com.hzcu.pcap.entity.PacketRecord;
import com.hzcu.pcap.repository.AnalysisSummaryRepository;
import com.hzcu.pcap.repository.CaptureFileRepository;
import com.hzcu.pcap.repository.PacketRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class PacketAnalysisService {

    private static final int DETAIL_PACKET_LIMIT = 200;

    private final FileStorageService fileStorageService;
    private final CaptureFileRepository captureFileRepository;
    private final PacketRecordRepository packetRecordRepository;
    private final AnalysisSummaryRepository analysisSummaryRepository;
    private final TsharkCommandRunner tsharkCommandRunner;
    private final ObjectMapper objectMapper;
    private final PacketRecordBatchWriter packetRecordBatchWriter;

    public PacketAnalysisService(FileStorageService fileStorageService,
                                 CaptureFileRepository captureFileRepository,
                                 PacketRecordRepository packetRecordRepository,
                                 AnalysisSummaryRepository analysisSummaryRepository,
                                 TsharkCommandRunner tsharkCommandRunner,
                                 ObjectMapper objectMapper,
                                 PacketRecordBatchWriter packetRecordBatchWriter) {
        this.fileStorageService = fileStorageService;
        this.captureFileRepository = captureFileRepository;
        this.packetRecordRepository = packetRecordRepository;
        this.analysisSummaryRepository = analysisSummaryRepository;
        this.tsharkCommandRunner = tsharkCommandRunner;
        this.objectMapper = objectMapper;
        this.packetRecordBatchWriter = packetRecordBatchWriter;
    }

    @Transactional
    public Map<String, Object> analyze(Long fileId) {
        return analyzeInternal(fileId, AnalysisProgressReporter.noop());
    }

    @Transactional
    public Map<String, Object> analyzeWithProgress(Long fileId, AnalysisProgressReporter reporter) {
        return analyzeInternal(fileId, reporter);
    }

    private Map<String, Object> analyzeInternal(Long fileId, AnalysisProgressReporter reporter) {
        CaptureFile captureFile = fileStorageService.getFile(fileId);
        try {
            reporter.report(AnalysisProgressEvent.of("starting", "prepare", "正在准备解析文件", 0, 0, 0));
            Path capturePath = fileStorageService.getDownloadPath(fileId);
            captureFile.setStatus("analyzing");
            captureFileRepository.save(captureFile);

            reporter.report(AnalysisProgressEvent.of("counting", "capinfos-count", "正在读取数据包总数", 0, 0, 0));
            long totalPackets = tsharkCommandRunner.countPackets(capturePath);
            reporter.report(AnalysisProgressEvent.of("parsing", "tshark-fields", "正在读取数据包字段", totalPackets, 0, 8));

            packetRecordRepository.deleteByFileId(fileId);
            analysisSummaryRepository.deleteByFileId(fileId);

            AtomicLong processedPackets = new AtomicLong(0);
            List<String> lines = tsharkCommandRunner.readPacketFieldLines(capturePath, line -> {
                long processed = processedPackets.incrementAndGet();
                reporter.report(AnalysisProgressEvent.of(
                        "parsing",
                        "tshark-fields",
                        "正在读取数据包字段",
                        totalPackets,
                        processed,
                        fieldPercent(processed, totalPackets)
                ));
            });

            reporter.report(AnalysisProgressEvent.of("saving", "packet-detail", "正在读取数据包详情", totalPackets, processedPackets.get(), 90));
            List<String> detailItems = tsharkCommandRunner.readPacketDetailJsonItems(capturePath, DETAIL_PACKET_LIMIT);
            List<String> featureLines = tsharkCommandRunner.readProtocolFeatureLines(capturePath);

            List<PacketRecord> records = new ArrayList<>();
            for (int i = 0; i < lines.size(); i++) {
                records.add(toPacketRecord(fileId, lines.get(i), detailItems.size() > i ? detailItems.get(i) : "{}"));
            }

            reporter.report(AnalysisProgressEvent.of("saving", "database-save", "正在批量保存解析结果", totalPackets, records.size(), 95));
            packetRecordBatchWriter.saveAll(records);

            reporter.report(AnalysisProgressEvent.of("summary", "summary-build", "正在生成统计结果", totalPackets, records.size(), 98));
            analysisSummaryRepository.save(buildSummary(fileId, records, featureLines));

            captureFile.setPacketCount((long) records.size());
            captureFile.setStatus("analyzed");
            captureFileRepository.save(captureFile);
            return Map.of("status", "ok", "packetCount", records.size());
        } catch (RuntimeException e) {
            captureFile.setStatus("failed");
            captureFileRepository.save(captureFile);
            reporter.report(AnalysisProgressEvent.error("解析失败：" + rootMessage(e)));
            throw new IllegalStateException("Failed to analyze capture file " + fileId, e);
        }
    }

    private int percent(long processedPackets, long totalPackets) {
        if (totalPackets <= 0) {
            return 0;
        }
        long value = Math.round((processedPackets * 100.0) / totalPackets);
        return (int) Math.max(0, Math.min(100, value));
    }

    private int fieldPercent(long processedPackets, long totalPackets) {
        if (totalPackets <= 0) {
            return 8;
        }
        long value = 8 + Math.round((processedPackets * 80.0) / totalPackets);
        return (int) Math.max(8, Math.min(88, value));
    }

    private String rootMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        String message = current.getMessage();
        return message == null || message.isBlank() ? current.getClass().getSimpleName() : message;
    }

    private PacketRecord toPacketRecord(Long fileId, String line, String detailJson) {
        String[] columns = splitColumns(line);
        PacketRecord record = new PacketRecord();
        record.setFileId(fileId);
        record.setPacketNo(parseLong(value(columns, 0)));
        record.setTimestampText(value(columns, 1));
        record.setSourceIp(firstNonBlank(value(columns, 2), value(columns, 3)));
        record.setDestinationIp(firstNonBlank(value(columns, 4), value(columns, 5)));
        record.setSourcePort(parseInteger(firstNonBlank(value(columns, 6), value(columns, 7))));
        record.setDestinationPort(parseInteger(firstNonBlank(value(columns, 8), value(columns, 9))));
        record.setProtocol(value(columns, 10));
        record.setLength(parseInteger(value(columns, 11)));
        record.setInfo(value(columns, 12));
        record.setDetailJson(detailJson == null || detailJson.isBlank() ? "{}" : detailJson);
        return record;
    }

    private String[] splitColumns(String line) {
        String[] raw = line.split("\t", -1);
        if (raw.length <= 13) {
            return raw;
        }
        String[] columns = new String[13];
        System.arraycopy(raw, 0, columns, 0, 12);
        columns[12] = String.join("\t", java.util.Arrays.copyOfRange(raw, 12, raw.length));
        return columns;
    }

    private AnalysisSummary buildSummary(Long fileId, List<PacketRecord> records, List<String> featureLines) {
        Map<String, Long> protocols = new LinkedHashMap<>();
        Map<String, Long> trafficTrend = new LinkedHashMap<>();
        Map<String, Long> ips = new LinkedHashMap<>();
        Map<String, Long> ports = new LinkedHashMap<>();
        Map<String, Long> sourceIps = new LinkedHashMap<>();
        Map<String, Long> destinationIps = new LinkedHashMap<>();
        Map<String, Long> sourcePorts = new LinkedHashMap<>();
        Map<String, Long> destinationPorts = new LinkedHashMap<>();

        for (PacketRecord record : records) {
            increment(protocols, record.getProtocol());
            increment(trafficTrend, epochSecond(record.getTimestampText()));
            increment(ips, record.getSourceIp());
            increment(ips, record.getDestinationIp());
            String sourcePort = record.getSourcePort() == null ? "" : record.getSourcePort().toString();
            String destinationPort = record.getDestinationPort() == null ? "" : record.getDestinationPort().toString();
            increment(ports, sourcePort);
            increment(ports, destinationPort);
            increment(sourceIps, record.getSourceIp());
            increment(destinationIps, record.getDestinationIp());
            increment(sourcePorts, sourcePort);
            increment(destinationPorts, destinationPort);
        }

        List<Map<String, Object>> trendItems = trafficTrend.entrySet().stream()
                .map(entry -> Map.<String, Object>of("time", entry.getKey(), "value", entry.getValue()))
                .toList();

        AnalysisSummary summary = new AnalysisSummary();
        summary.setFileId(fileId);
        summary.setProtocolJson(writeJson(protocols));
        summary.setTrafficTrendJson(writeJson(trendItems));
        summary.setIpJson(writeJson(ips));
        summary.setPortJson(writeJson(ports));
        summary.setLengthDistributionJson(writeJson(lengthDistribution(records)));
        summary.setSourceIpTopJson(writeJson(topItems(sourceIps)));
        summary.setDestinationIpTopJson(writeJson(topItems(destinationIps)));
        summary.setSourcePortTopJson(writeJson(topItems(sourcePorts)));
        summary.setDestinationPortTopJson(writeJson(topItems(destinationPorts)));
        summary.setDnsRecordsJson(writeJson(dnsRecords(featureLines)));
        summary.setHttpRecordsJson(writeJson(httpRecords(featureLines)));
        summary.setStartTimeText(records.isEmpty() ? "" : records.get(0).getTimestampText());
        summary.setEndTimeText(records.isEmpty() ? "" : records.get(records.size() - 1).getTimestampText());
        return summary;
    }

    private List<Map<String, Object>> lengthDistribution(List<PacketRecord> records) {
        String[] ranges = {"0-63", "64-127", "128-255", "256-511", "512-1023", "1024-1518", "1519+"};
        long[] counts = new long[ranges.length];
        for (PacketRecord record : records) {
            Integer length = record.getLength();
            if (length == null) {
                continue;
            }
            counts[lengthBucket(length)]++;
        }
        List<Map<String, Object>> items = new ArrayList<>();
        for (int i = 0; i < ranges.length; i++) {
            items.add(Map.of("range", ranges[i], "value", counts[i]));
        }
        return items;
    }

    private int lengthBucket(int length) {
        if (length < 64) {
            return 0;
        }
        if (length <= 127) {
            return 1;
        }
        if (length <= 255) {
            return 2;
        }
        if (length <= 511) {
            return 3;
        }
        if (length <= 1023) {
            return 4;
        }
        if (length <= 1518) {
            return 5;
        }
        return 6;
    }

    private List<Map<String, Object>> topItems(Map<String, Long> counts) {
        return counts.entrySet().stream()
                .sorted(Comparator.<Map.Entry<String, Long>>comparingLong(Map.Entry::getValue).reversed()
                        .thenComparing(Map.Entry::getKey))
                .limit(10)
                .map(entry -> Map.<String, Object>of("name", entry.getKey(), "value", entry.getValue()))
                .toList();
    }

    private List<Map<String, Object>> dnsRecords(List<String> featureLines) {
        List<Map<String, Object>> records = new ArrayList<>();
        for (String line : featureLines) {
            String[] columns = splitFeatureColumns(line);
            String queryName = value(columns, 1);
            String answerAddress = value(columns, 2);
            if (queryName.isBlank() && answerAddress.isBlank()) {
                continue;
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("packetNo", parseLongOrNull(value(columns, 0)));
            item.put("queryName", queryName);
            item.put("answerAddress", answerAddress);
            records.add(item);
            if (records.size() >= 100) {
                break;
            }
        }
        return records;
    }

    private List<Map<String, Object>> httpRecords(List<String> featureLines) {
        List<Map<String, Object>> records = new ArrayList<>();
        for (String line : featureLines) {
            String[] columns = splitFeatureColumns(line);
            String method = value(columns, 3);
            String host = value(columns, 4);
            String uri = value(columns, 5);
            String responseCode = value(columns, 6);
            if (method.isBlank() && host.isBlank() && uri.isBlank() && responseCode.isBlank()) {
                continue;
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("packetNo", parseLongOrNull(value(columns, 0)));
            item.put("method", method);
            item.put("host", host);
            item.put("uri", uri);
            item.put("responseCode", responseCode);
            records.add(item);
            if (records.size() >= 100) {
                break;
            }
        }
        return records;
    }

    private String[] splitFeatureColumns(String line) {
        String[] raw = line.split("\t", -1);
        if (raw.length <= 7) {
            return raw;
        }
        String[] columns = new String[7];
        System.arraycopy(raw, 0, columns, 0, 6);
        columns[6] = String.join("\t", java.util.Arrays.copyOfRange(raw, 6, raw.length));
        return columns;
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to write analysis summary JSON", e);
        }
    }

    private void increment(Map<String, Long> counts, String key) {
        if (key == null || key.isBlank()) {
            return;
        }
        counts.merge(key, 1L, Long::sum);
    }

    private String epochSecond(String timestampText) {
        if (timestampText == null || timestampText.isBlank()) {
            return "";
        }
        int dot = timestampText.indexOf('.');
        if (dot < 0) {
            return timestampText;
        }
        return timestampText.substring(0, dot);
    }

    private String value(String[] columns, int index) {
        if (index >= columns.length) {
            return "";
        }
        return columns[index] == null ? "" : columns[index];
    }

    private String firstNonBlank(String first, String second) {
        return first == null || first.isBlank() ? second : first;
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Long.parseLong(value);
    }

    private Long parseLongOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer parseInteger(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Integer.parseInt(value);
    }
}
