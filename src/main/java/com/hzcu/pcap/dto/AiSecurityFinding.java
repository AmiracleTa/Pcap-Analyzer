package com.hzcu.pcap.dto;

import java.util.List;

/**
 * AI 安全报告中的单个风险发现。
 *
 * @param category 风险分类
 * @param severity 严重程度
 * @param title 风险标题
 * @param explanation 风险说明
 * @param evidence 证据列表
 * @param recommendation 单项处置建议
 */
public record AiSecurityFinding(
        String category,
        String severity,
        String title,
        String explanation,
        List<SecurityEvidence> evidence,
        String recommendation
) {
}
