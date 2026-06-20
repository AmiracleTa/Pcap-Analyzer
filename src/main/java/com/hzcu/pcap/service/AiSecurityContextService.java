package com.hzcu.pcap.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hzcu.pcap.entity.CaptureFile;
import com.hzcu.pcap.entity.PacketRecord;
import com.hzcu.pcap.repository.CaptureFileRepository;
import com.hzcu.pcap.repository.PacketRecordRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 构造发送给 AI 模型的安全分析上下文。
 */
@Service
public class AiSecurityContextService {

    private static final int PACKET_SAMPLE_LIMIT = 200;
    private static final int MAX_TEXT_LENGTH = 96;

    private final CaptureFileRepository captureFileRepository;
    private final PacketRecordRepository packetRecordRepository;
    private final SummaryService summaryService;
    private final ObjectMapper objectMapper;

    /**
     * 创建 AI 安全上下文服务。
     *
     * @param captureFileRepository 抓包文件仓储
     * @param packetRecordRepository 数据包记录仓储
     * @param summaryService 统计结果服务
     * @param objectMapper JSON 序列化组件
     */
    public AiSecurityContextService(CaptureFileRepository captureFileRepository,
                                    PacketRecordRepository packetRecordRepository,
                                    SummaryService summaryService,
                                    ObjectMapper objectMapper) {
        this.captureFileRepository = captureFileRepository;
        this.packetRecordRepository = packetRecordRepository;
        this.summaryService = summaryService;
        this.objectMapper = objectMapper;
    }

    /**
     * 构造指定文件的 AI 安全报告输入上下文。
     *
     * @param fileId 文件 ID
     * @return JSON 格式的模型上下文
     */
    public String buildContext(Long fileId) {
        CaptureFile captureFile = captureFileRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("文件不存在：" + fileId));
        if (!"analyzed".equals(captureFile.getStatus())) {
            throw new IllegalStateException("请先完成抓包解析后再生成 AI 安全报告。");
        }

        List<PacketRecord> packets = sortedPackets(fileId);
        Map<String, Object> summary = trimmedSummary(summaryService.getSummary(fileId));
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("summary", summary);
        context.put("packetSamples", packetSamples(packets));
        context.put("outputContract", outputContract());
        try {
            return objectMapper.writeValueAsString(context);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to build AI security context", e);
        }
    }

    private Map<String, Object> trimmedSummary(Map<String, Object> source) {
        // 只发送协议分布和流量趋势，减少敏感明细外传并控制 token 成本。
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("protocols", source.getOrDefault("protocols", Map.of()));
        summary.put("trafficTrend", compactValue(source.getOrDefault("trafficTrend", List.of())));
        return summary;
    }

    private List<PacketRecord> sortedPackets(Long fileId) {
        return packetRecordRepository.findByFileId(fileId).stream()
                .sorted(Comparator.comparing(PacketRecord::getPacketNo, Comparator.nullsLast(Long::compareTo)))
                .toList();
    }

    private List<Map<String, Object>> packetSamples(List<PacketRecord> packets) {
        return packets.stream()
                .limit(PACKET_SAMPLE_LIMIT)
                .map(this::packetSample)
                .toList();
    }

    private Map<String, Object> packetSample(PacketRecord packet) {
        Map<String, Object> sample = new LinkedHashMap<>();
        sample.put("packetNo", packet.getPacketNo());
        sample.put("time", truncate(packet.getTimestampText()));
        sample.put("sourceIp", truncate(packet.getSourceIp()));
        sample.put("destinationIp", truncate(packet.getDestinationIp()));
        sample.put("sourcePort", packet.getSourcePort());
        sample.put("destinationPort", packet.getDestinationPort());
        sample.put("protocol", truncate(packet.getProtocol()));
        sample.put("length", packet.getLength());
        return sample;
    }

    private Map<String, Object> outputContract() {
        Map<String, Object> contract = new LinkedHashMap<>();
        contract.put("riskLevel", "low|medium|high|critical|unknown");
        contract.put("riskScore", "0-100 integer");
        contract.put("summary", "中文，总体结论，一到三句话");
        contract.put("findings", "主要风险点数组，每项包含 category、severity、title、explanation、evidence、recommendation；evidence 必须来自 packetSamples、summary.protocols 或 summary.trafficTrend");
        contract.put("normalObservations", "相对正常的现象数组，中文");
        contract.put("recommendations", "处置建议数组，中文");
        return contract;
    }

    private Object compactValue(Object value) {
        if (value instanceof List<?> list) {
            return list.stream().map(this::compactValue).toList();
        }
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> compacted = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                compacted.put(String.valueOf(entry.getKey()), compactValue(entry.getValue()));
            }
            return compacted;
        }
        if (value instanceof String text) {
            return truncate(text);
        }
        return value;
    }

    private String truncate(String value) {
        if (value == null) {
            return "";
        }
        String normalized = value.replace('\r', ' ').replace('\n', ' ').trim();
        if (normalized.length() <= MAX_TEXT_LENGTH) {
            return normalized;
        }
        return normalized.substring(0, MAX_TEXT_LENGTH) + "...";
    }
}
