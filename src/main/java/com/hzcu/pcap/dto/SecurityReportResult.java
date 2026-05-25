package com.hzcu.pcap.dto;

import java.util.List;

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

    public static SecurityReportResult unavailable(String provider, String model, String message) {
        return new SecurityReportResult(false, provider, model, "unknown", 0, "", List.of(), List.of(), List.of(), message);
    }

    public static SecurityReportResult unavailable(String provider, String model, String message, List<String> recommendations) {
        return new SecurityReportResult(false, provider, model, "unknown", 0, "", List.of(), List.of(), recommendations == null ? List.of() : recommendations, message);
    }

    public static SecurityReportResult none(String provider, String model) {
        return new SecurityReportResult(false, provider, model, "unknown", 0, "", List.of(), List.of(), List.of(), "暂无安全报告，请先分析该文件。");
    }
}
