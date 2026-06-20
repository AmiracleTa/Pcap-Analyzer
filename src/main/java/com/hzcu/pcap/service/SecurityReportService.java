package com.hzcu.pcap.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hzcu.pcap.config.AiProviderProperties;
import com.hzcu.pcap.dto.AiSecurityFinding;
import com.hzcu.pcap.dto.AiSecurityModelOutput;
import com.hzcu.pcap.dto.SecurityReportResult;
import com.hzcu.pcap.entity.SecurityReport;
import com.hzcu.pcap.repository.SecurityReportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 生成、保存并查询 AI 安全报告。
 */
@Service
public class SecurityReportService {

    private final AiProviderProperties properties;
    private final AiSecurityContextService contextService;
    private final AiChatCompletionClient aiClient;
    private final SecurityReportRepository securityReportRepository;
    private final ObjectMapper objectMapper;

    /**
     * 创建安全报告服务。
     *
     * @param properties AI 提供商配置
     * @param contextService AI 上下文构造服务
     * @param aiClient AI 调用客户端
     * @param securityReportRepository 安全报告仓储
     * @param objectMapper JSON 处理组件
     */
    public SecurityReportService(AiProviderProperties properties,
                                 AiSecurityContextService contextService,
                                 AiChatCompletionClient aiClient,
                                 SecurityReportRepository securityReportRepository,
                                 ObjectMapper objectMapper) {
        this.properties = properties;
        this.contextService = contextService;
        this.aiClient = aiClient;
        this.securityReportRepository = securityReportRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * 生成并保存指定文件的安全报告。
     *
     * @param fileId 文件 ID
     * @return 保存后的安全报告结果
     */
    @Transactional
    public SecurityReportResult generateAndStore(Long fileId) {
        if (!properties.configured()) {
            return saveReport(fileId, SecurityReportResult.unavailable(
                    properties.provider(),
                    properties.model(),
                    "未配置 AI_API_KEY，无法调用 AI。",
                    List.of("配置 AI_API_KEY 后重新分析该文件以生成 AI 安全报告。")
            ));
        }

        try {
            String contextJson = contextService.buildContext(fileId);
            AiSecurityModelOutput output = aiClient.createReport(contextJson);
            return saveReport(fileId, SecurityReportResult.fromAi(properties.provider(), properties.model(), output));
        } catch (RuntimeException e) {
            return saveReport(fileId, SecurityReportResult.unavailable(
                    properties.provider(),
                    properties.model(),
                    "AI 安全报告生成失败：" + rootMessage(e)
            ));
        }
    }

    /**
     * 查询指定文件最近一次生成的安全报告。
     *
     * @param fileId 文件 ID
     * @return 最近一次安全报告；不存在时返回空报告
     */
    public SecurityReportResult latest(Long fileId) {
        return securityReportRepository.findTopByFileIdOrderByCreatedAtDesc(fileId)
                .map(this::toResult)
                .orElseGet(() -> SecurityReportResult.none(properties.provider(), properties.model()));
    }

    private SecurityReportResult saveReport(Long fileId, SecurityReportResult result) {
        // 即使 AI 不可用也保存一条报告记录，前端可以展示明确原因和后续建议。
        try {
            SecurityReport report = new SecurityReport();
            report.setFileId(fileId);
            report.setAvailable(result.available());
            report.setProvider(result.provider());
            report.setModel(result.model());
            report.setRiskLevel(result.riskLevel());
            report.setRiskScore(result.riskScore());
            report.setSummaryText(result.summary());
            report.setFindingsJson(objectMapper.writeValueAsString(result.findings()));
            report.setNormalObservationsJson(objectMapper.writeValueAsString(result.normalObservations()));
            report.setRecommendationsJson(objectMapper.writeValueAsString(result.recommendations()));
            report.setMessage(result.message());
            report.setCreatedAt(LocalDateTime.now());
            SecurityReport saved = securityReportRepository.save(report);
            return toResult(saved);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to save security report", e);
        }
    }

    private SecurityReportResult toResult(SecurityReport report) {
        return new SecurityReportResult(
                Boolean.TRUE.equals(report.getAvailable()),
                report.getProvider(),
                report.getModel(),
                report.getRiskLevel(),
                report.getRiskScore() == null ? 0 : report.getRiskScore(),
                report.getSummaryText() == null ? "" : report.getSummaryText(),
                readFindings(report.getFindingsJson()),
                readStringList(report.getNormalObservationsJson(), "normal observations"),
                readRecommendations(report.getRecommendationsJson()),
                report.getMessage() == null ? "" : report.getMessage()
        );
    }

    private List<AiSecurityFinding> readFindings(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to read security findings JSON", e);
        }
    }

    private List<String> readRecommendations(String json) {
        return readStringList(json, "security recommendations");
    }

    private List<String> readStringList(String json, String name) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to read " + name + " JSON", e);
        }
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
