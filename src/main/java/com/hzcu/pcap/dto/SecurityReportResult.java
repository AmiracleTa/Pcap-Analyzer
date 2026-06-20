package com.hzcu.pcap.dto;

import java.util.List;

/**
 * 面向前端返回的安全报告结果。
 *
 * @param available 报告是否可用
 * @param provider AI 提供商名称
 * @param model 模型名称
 * @param riskLevel 风险等级
 * @param riskScore 风险分数
 * @param summary 安全摘要
 * @param findings 风险发现列表
 * @param normalObservations 相对正常的观察项
 * @param recommendations 处置建议
 * @param message 报告状态消息
 */
public record SecurityReportResult(
        boolean available,
        String provider,
        String model,
        String riskLevel,
        int riskScore,
        String summary,
        List<AiSecurityFinding> findings,
        List<String> normalObservations,
        List<String> recommendations,
        String message
) {
    /**
     * 将 AI 模型输出转换为可用报告结果。
     *
     * @param provider AI 提供商名称
     * @param model 模型名称
     * @param output AI 模型输出
     * @return 可用报告结果
     */
    public static SecurityReportResult fromAi(String provider, String model, AiSecurityModelOutput output) {
        return new SecurityReportResult(
                true,
                provider,
                model,
                output.riskLevel(),
                Math.max(0, Math.min(100, output.riskScore())),
                output.summary(),
                output.findings() == null ? List.of() : output.findings(),
                output.normalObservations() == null ? List.of() : output.normalObservations(),
                output.recommendations() == null ? List.of() : output.recommendations(),
                ""
        );
    }

    /**
     * 创建不可用报告结果。
     *
     * @param provider AI 提供商名称
     * @param model 模型名称
     * @param message 不可用原因
     * @return 不可用报告结果
     */
    public static SecurityReportResult unavailable(String provider, String model, String message) {
        return new SecurityReportResult(false, provider, model, "unknown", 0, "", List.of(), List.of(), List.of(), message);
    }

    /**
     * 创建带处置建议的不可用报告结果。
     *
     * @param provider AI 提供商名称
     * @param model 模型名称
     * @param message 不可用原因
     * @param recommendations 处置建议
     * @return 不可用报告结果
     */
    public static SecurityReportResult unavailable(String provider, String model, String message, List<String> recommendations) {
        return new SecurityReportResult(false, provider, model, "unknown", 0, "", List.of(), List.of(), recommendations == null ? List.of() : recommendations, message);
    }

    /**
     * 创建尚未生成报告时的空结果。
     *
     * @param provider AI 提供商名称
     * @param model 模型名称
     * @return 空报告结果
     */
    public static SecurityReportResult none(String provider, String model) {
        return new SecurityReportResult(false, provider, model, "unknown", 0, "", List.of(), List.of(), List.of(), "暂无安全报告，请先分析该文件。");
    }
}
