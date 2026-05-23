package com.hzcu.pcap.controller;

import com.hzcu.pcap.dto.AnalysisProgressEvent;
import com.hzcu.pcap.entity.PacketRecord;
import com.hzcu.pcap.repository.PacketRecordRepository;
import com.hzcu.pcap.service.PacketAnalysisService;
import com.hzcu.pcap.service.SummaryService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/files/{id}")
public class AnalysisController {

    private final PacketAnalysisService packetAnalysisService;
    private final SummaryService summaryService;
    private final PacketRecordRepository packetRecordRepository;

    public AnalysisController(PacketAnalysisService packetAnalysisService,
                              SummaryService summaryService,
                              PacketRecordRepository packetRecordRepository) {
        this.packetAnalysisService = packetAnalysisService;
        this.summaryService = summaryService;
        this.packetRecordRepository = packetRecordRepository;
    }

    @PostMapping("/analyze")
    public Map<String, Object> analyze(@PathVariable Long id) {
        return packetAnalysisService.analyze(id);
    }

    @GetMapping(value = "/analyze/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter analyzeEvents(@PathVariable Long id) {
        SseEmitter emitter = new SseEmitter(0L);
        CompletableFuture.runAsync(() -> {
            try {
                Map<String, Object> result = packetAnalysisService.analyzeWithProgress(id, event -> sendProgress(emitter, event));
                sendProgress(emitter, AnalysisProgressEvent.done(packetCount(result)));
                emitter.complete();
            } catch (RuntimeException e) {
                sendProgress(emitter, AnalysisProgressEvent.error("解析失败：" + rootMessage(e)));
                emitter.completeWithError(e);
            }
        });
        return emitter;
    }

    @GetMapping("/packets")
    public List<PacketRecord> packets(@PathVariable Long id) {
        return packetRecordRepository.findByFileId(id);
    }

    @GetMapping("/summary")
    public Map<String, Object> summary(@PathVariable Long id) {
        return summaryService.getSummary(id);
    }

    @GetMapping(value = "/export/csv", produces = "text/csv;charset=UTF-8")
    public ResponseEntity<String> exportCsv(@PathVariable Long id) {
        List<PacketRecord> packets = packetRecordRepository.findByFileId(id);
        StringBuilder csv = new StringBuilder("\uFEFF");
        csv.append("序号,时间戳,源地址,目的地址,源端口,目的端口,协议,长度,摘要\n");
        for (PacketRecord packet : packets) {
            csv.append(csvValue(packet.getPacketNo()))
                    .append(',')
                    .append(csvTextValue(packet.getTimestampText()))
                    .append(',')
                    .append(csvValue(packet.getSourceIp()))
                    .append(',')
                    .append(csvValue(packet.getDestinationIp()))
                    .append(',')
                    .append(csvValue(packet.getSourcePort()))
                    .append(',')
                    .append(csvValue(packet.getDestinationPort()))
                    .append(',')
                    .append(csvValue(packet.getProtocol()))
                    .append(',')
                    .append(csvValue(packet.getLength()))
                    .append(',')
                    .append(csvValue(packet.getInfo()))
                    .append('\n');
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"packets-" + id + ".csv\"")
                .body(csv.toString());
    }

    @GetMapping(value = "/export/json", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> exportJson(@PathVariable Long id) {
        return Map.of("packets", packetRecordRepository.findByFileId(id));
    }

    private String csvValue(Object value) {
        if (value == null) {
            return "\"\"";
        }
        String text = sanitizeCsvText(value.toString());
        if (text.matches("\\d{13,}")) {
            return csvTextValue(text);
        }
        if (!text.isEmpty() && "=+-@".indexOf(text.charAt(0)) >= 0) {
            text = "'" + text;
        }
        return "\"" + text + "\"";
    }

    private String csvTextValue(Object value) {
        if (value == null) {
            return "\"\"";
        }
        return "=\"" + sanitizeCsvText(value.toString()) + "\"";
    }

    private String sanitizeCsvText(String text) {
        return text.replace("\"", "\"\"")
                .replace('\r', ' ')
                .replace('\n', ' ');
    }

    private void sendProgress(SseEmitter emitter, AnalysisProgressEvent event) {
        try {
            emitter.send(SseEmitter.event().name("progress").data(event));
        } catch (IOException e) {
            throw new IllegalStateException("SSE client disconnected", e);
        }
    }

    private long packetCount(Map<String, Object> result) {
        Object value = result.get("packetCount");
        return value instanceof Number ? ((Number) value).longValue() : 0L;
    }

    private String rootMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        String message = current.getMessage();
        return message == null || message.isBlank() ? current.getClass().getSimpleName() : message;
    }
}
