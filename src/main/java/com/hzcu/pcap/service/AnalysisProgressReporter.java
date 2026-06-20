package com.hzcu.pcap.service;

import com.hzcu.pcap.dto.AnalysisProgressEvent;

/**
 * 抓包解析进度事件上报器。
 */
@FunctionalInterface
public interface AnalysisProgressReporter {

    /**
     * 上报一次解析进度事件。
     *
     * @param event 解析进度事件
     */
    void report(AnalysisProgressEvent event);

    /**
     * 创建不执行任何操作的进度上报器。
     *
     * @return 空操作上报器
     */
    static AnalysisProgressReporter noop() {
        return event -> {
        };
    }
}
