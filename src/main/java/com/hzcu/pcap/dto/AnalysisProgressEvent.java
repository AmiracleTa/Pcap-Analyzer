package com.hzcu.pcap.dto;

public record AnalysisProgressEvent(
        String status,
        String phase,
        String message,
        long totalPackets,
        long processedPackets,
        int percent,
        Long packetCount
) {
    public static AnalysisProgressEvent of(String status,
                                           String phase,
                                           String message,
                                           long totalPackets,
                                           long processedPackets,
                                           int percent) {
        return new AnalysisProgressEvent(status, phase, message, totalPackets, processedPackets, percent, null);
    }

    public static AnalysisProgressEvent done(long packetCount) {
        return new AnalysisProgressEvent("done", "complete", "解析完成", packetCount, packetCount, 100, packetCount);
    }

    public static AnalysisProgressEvent error(String message) {
        return new AnalysisProgressEvent("error", "failed", message, 0, 0, 100, null);
    }
}
