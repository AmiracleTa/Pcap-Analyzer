package com.hzcu.pcap.service;

import com.hzcu.pcap.dto.AnalysisProgressEvent;

@FunctionalInterface
public interface AnalysisProgressReporter {

    void report(AnalysisProgressEvent event);

    static AnalysisProgressReporter noop() {
        return event -> {
        };
    }
}
