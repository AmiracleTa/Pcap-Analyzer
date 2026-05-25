package com.hzcu.pcap.dto;

public record AnalysisProgressEvent(
        String status,
        String phase,
        String message,
        long totalPackets,
        long processedPackets,
        int percent,
        Long packetCount,
        Boolean aiReportAvailable
) {
    public static AnalysisProgressEvent of(String status,
                                           String phase,
                                           String message,
                                           long totalPackets,
                                           long processedPackets,
                                           int percent) {
        return new AnalysisProgressEvent(status, phase, message, totalPackets, processedPackets, percent, null, null);
    }

    public static AnalysisProgressEvent done(long packetCount) {
        return new AnalysisProgressEvent("done", "complete", "解析完成，AI 安全报告已生成", packetCount, packetCount, 100, packetCount, true);
    }

    public static AnalysisProgressEvent done(long packetCount, boolean aiReportAvailable, String message) {
        return new AnalysisProgressEvent("done", "complete", message, packetCount, packetCount, 100, packetCount, aiReportAvailable);
    }

    public static AnalysisProgressEvent aiReport(long packetCount) {
        return new AnalysisProgressEvent("running", "ai-report", "正在生成 AI 安全报告", packetCount, packetCount, 99, packetCount, null);
    }

    public static AnalysisProgressEvent error(String message) {
        return new AnalysisProgressEvent("error", "failed", message, 0, 0, 100, null, false);
    }
}
