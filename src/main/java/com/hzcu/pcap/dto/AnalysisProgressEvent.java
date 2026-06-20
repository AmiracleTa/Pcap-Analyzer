package com.hzcu.pcap.dto;

/**
 * 抓包解析进度事件。
 *
 * @param status 进度状态
 * @param phase 当前阶段
 * @param message 展示给用户的进度消息
 * @param totalPackets 数据包总数
 * @param processedPackets 已处理数据包数
 * @param percent 进度百分比
 * @param packetCount 完成后的数据包数量
 * @param aiReportAvailable AI 安全报告是否可用
 */
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
    /**
     * 创建普通进度事件。
     *
     * @param status 进度状态
     * @param phase 当前阶段
     * @param message 进度消息
     * @param totalPackets 数据包总数
     * @param processedPackets 已处理数据包数
     * @param percent 进度百分比
     * @return 进度事件
     */
    public static AnalysisProgressEvent of(String status,
                                           String phase,
                                           String message,
                                           long totalPackets,
                                           long processedPackets,
                                           int percent) {
        return new AnalysisProgressEvent(status, phase, message, totalPackets, processedPackets, percent, null, null);
    }

    /**
     * 创建解析完成且 AI 报告可用的事件。
     *
     * @param packetCount 数据包数量
     * @return 完成事件
     */
    public static AnalysisProgressEvent done(long packetCount) {
        return new AnalysisProgressEvent("done", "complete", "解析完成，AI 安全报告已生成", packetCount, packetCount, 100, packetCount, true);
    }

    /**
     * 创建解析完成事件。
     *
     * @param packetCount 数据包数量
     * @param aiReportAvailable AI 安全报告是否可用
     * @param message 完成消息
     * @return 完成事件
     */
    public static AnalysisProgressEvent done(long packetCount, boolean aiReportAvailable, String message) {
        return new AnalysisProgressEvent("done", "complete", message, packetCount, packetCount, 100, packetCount, aiReportAvailable);
    }

    /**
     * 创建 AI 报告生成阶段事件。
     *
     * @param packetCount 数据包数量
     * @return AI 报告进度事件
     */
    public static AnalysisProgressEvent aiReport(long packetCount) {
        return new AnalysisProgressEvent("running", "ai-report", "正在生成 AI 安全报告", packetCount, packetCount, 99, packetCount, null);
    }

    /**
     * 创建解析失败事件。
     *
     * @param message 错误消息
     * @return 失败事件
     */
    public static AnalysisProgressEvent error(String message) {
        return new AnalysisProgressEvent("error", "failed", message, 0, 0, 100, null, false);
    }
}
