package com.hzcu.pcap.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hzcu.pcap.entity.AnalysisSummary;
import com.hzcu.pcap.repository.AnalysisSummaryRepository;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 将数据库中的分析统计实体转换为前端可直接消费的响应结构。
 */
@Service
public class SummaryService {

    private final AnalysisSummaryRepository analysisSummaryRepository;
    private final ObjectMapper objectMapper;

    /**
     * 创建统计结果服务。
     *
     * @param analysisSummaryRepository 分析统计仓储
     * @param objectMapper JSON 解析组件
     */
    public SummaryService(AnalysisSummaryRepository analysisSummaryRepository, ObjectMapper objectMapper) {
        this.analysisSummaryRepository = analysisSummaryRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * 查询指定文件的统计结果，未分析时返回空统计结构。
     *
     * @param fileId 文件 ID
     * @return 前端统计响应
     */
    public Map<String, Object> getSummary(Long fileId) {
        return analysisSummaryRepository.findByFileId(fileId)
                .map(this::toResponse)
                .orElseGet(this::emptySummary);
    }

    private Map<String, Object> toResponse(AnalysisSummary summary) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("protocols", readMap(summary.getProtocolJson()));
        response.put("trafficTrend", readList(summary.getTrafficTrendJson()));
        response.put("ips", readMap(summary.getIpJson()));
        response.put("ports", readMap(summary.getPortJson()));
        response.put("lengthDistribution", readList(summary.getLengthDistributionJson()));
        response.put("sourceIpTop", readList(summary.getSourceIpTopJson()));
        response.put("destinationIpTop", readList(summary.getDestinationIpTopJson()));
        response.put("sourcePortTop", readList(summary.getSourcePortTopJson()));
        response.put("destinationPortTop", readList(summary.getDestinationPortTopJson()));
        response.put("dnsRecords", readList(summary.getDnsRecordsJson()));
        response.put("httpRecords", readList(summary.getHttpRecordsJson()));
        response.put("startTimeText", safe(summary.getStartTimeText()));
        response.put("endTimeText", safe(summary.getEndTimeText()));
        return response;
    }

    private Map<String, Object> emptySummary() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("protocols", Map.of());
        response.put("trafficTrend", List.of());
        response.put("ips", Map.of());
        response.put("ports", Map.of());
        response.put("lengthDistribution", List.of());
        response.put("sourceIpTop", List.of());
        response.put("destinationIpTop", List.of());
        response.put("sourcePortTop", List.of());
        response.put("destinationPortTop", List.of());
        response.put("dnsRecords", List.of());
        response.put("httpRecords", List.of());
        response.put("startTimeText", "");
        response.put("endTimeText", "");
        return response;
    }

    private Map<String, Object> readMap(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (Exception e) {
            throw new IllegalStateException("Failed to read summary JSON object", e);
        }
    }

    private List<Object> readList(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (Exception e) {
            throw new IllegalStateException("Failed to read summary JSON array", e);
        }
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
