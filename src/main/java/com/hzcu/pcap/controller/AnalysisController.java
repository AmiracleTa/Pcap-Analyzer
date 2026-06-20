package com.hzcu.pcap.controller;

import com.hzcu.pcap.dto.AnalysisProgressEvent;
import com.hzcu.pcap.dto.SecurityReportResult;
import com.hzcu.pcap.entity.PacketRecord;
import com.hzcu.pcap.repository.PacketRecordRepository;
import com.hzcu.pcap.service.PacketAnalysisService;
import com.hzcu.pcap.service.SecurityReportService;
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

/**
 * 提供抓包解析、统计查询、安全报告和结果导出接口。
 */
@RestController
@RequestMapping("/api/files/{id}")
public class AnalysisController {

    private final PacketAnalysisService packetAnalysisService;
    private final SummaryService summaryService;
    private final PacketRecordRepository packetRecordRepository;
    private final SecurityReportService securityReportService;

    /**
     * 创建分析控制器。
     *
     * @param packetAnalysisService 抓包解析服务
     * @param summaryService 统计结果服务
     * @param packetRecordRepository 数据包记录仓储
     * @param securityReportService AI 安全报告服务
     */
    public AnalysisController(PacketAnalysisService packetAnalysisService,
                              SummaryService summaryService,
                              PacketRecordRepository packetRecordRepository,
                              SecurityReportService securityReportService) {
        this.packetAnalysisService = packetAnalysisService;
        this.summaryService = summaryService;
        this.packetRecordRepository = packetRecordRepository;
        this.securityReportService = securityReportService;
    }

    /**
     * 同步触发抓包解析，并在解析完成后尝试生成 AI 安全报告。
     *
     * @param id 文件 ID
     * @return 解析状态和数据包数量
     */
    @PostMapping("/analyze")
    public Map<String, Object> analyze(@PathVariable Long id) {
        Map<String, Object> result = packetAnalysisService.analyze(id);
        securityReportService.generateAndStore(id);
        return result;
    }

    /**
     * 通过 SSE 推送抓包解析进度，并在完成时推送 AI 报告生成状态。
     *
     * @param id 文件 ID
     * @return SSE 事件发射器
     */
    @GetMapping(value = "/analyze/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter analyzeEvents(@PathVariable Long id) {
        SseEmitter emitter = new SseEmitter(0L);
        CompletableFuture.runAsync(() -> {
            try {
                Map<String, Object> result = packetAnalysisService.analyzeWithProgress(id, event -> sendProgress(emitter, event));
                long packetCount = packetCount(result);
                sendProgress(emitter, AnalysisProgressEvent.aiReport(packetCount));
                SecurityReportResult report;
                try {
                    report = securityReportService.generateAndStore(id);
                } catch (RuntimeException ignored) {
                    report = SecurityReportResult.unavailable("", "", "AI 安全报告保存失败。");
                }
                sendProgress(emitter, doneProgress(packetCount, report));
                emitter.complete();
            } catch (RuntimeException e) {
                sendProgress(emitter, AnalysisProgressEvent.error("解析失败：" + rootMessage(e)));
                emitter.completeWithError(e);
            }
        });
        return emitter;
    }

    /**
     * 查询指定文件的全部数据包记录。
     *
     * @param id 文件 ID
     * @return 数据包记录列表
     */
    @GetMapping("/packets")
    public List<PacketRecord> packets(@PathVariable Long id) {
        return packetRecordRepository.findByFileId(id);
    }

    /**
     * 查询指定文件的统计结果。
     *
     * @param id 文件 ID
     * @return 面向前端图表的统计数据
     */
    @GetMapping("/summary")
    public Map<String, Object> summary(@PathVariable Long id) {
        return summaryService.getSummary(id);
    }

    /**
     * 查询指定文件最近一次保存的 AI 安全报告。
     *
     * @param id 文件 ID
     * @return AI 安全报告结果
     */
    @GetMapping("/security-report")
    public SecurityReportResult latestSecurityReport(@PathVariable Long id) {
        return securityReportService.latest(id);
    }

    /**
     * 将指定文件的数据包记录导出为 CSV。
     *
     * @param id 文件 ID
     * @return CSV 下载响应
     */
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

    /**
     * 将指定文件的数据包记录导出为 JSON。
     *
     * @param id 文件 ID
     * @return 包含数据包列表的 JSON 对象
     */
    @GetMapping(value = "/export/json", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> exportJson(@PathVariable Long id) {
        return Map.of("packets", packetRecordRepository.findByFileId(id));
    }

    // 同时处理换行、引号、长数字误转科学计数法和 Excel 公式注入风险。
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

    private AnalysisProgressEvent doneProgress(long packetCount, SecurityReportResult report) {
        if (report.available()) {
            return AnalysisProgressEvent.done(packetCount);
        }
        String message = report.message();
        if (message == null || message.isBlank()) {
            message = "解析完成，但 AI 安全报告未生成。";
        } else if (message.startsWith("未配置")) {
            message = "解析完成，但 AI 安全报告未生成：" + message;
        } else if (message.length() > 80) {
            message = message.substring(0, 80) + "...";
        }
        if (!message.startsWith("解析完成")) {
            message = "解析完成，但 " + message;
        }
        return AnalysisProgressEvent.done(packetCount, false, message);
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
