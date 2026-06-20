package com.hzcu.pcap.dto;

import java.util.List;

/**
 * AI 模型返回的安全报告结构。
 *
 * @param riskLevel 风险等级
 * @param riskScore 风险分数
 * @param summary 安全摘要
 * @param findings 风险发现列表
 * @param normalObservations 相对正常的观察项
 * @param recommendations 处置建议
 */
public record AiSecurityModelOutput(
        String riskLevel,
        int riskScore,
        String summary,
        List<AiSecurityFinding> findings,
        List<String> normalObservations,
        List<String> recommendations
) {
}
