package com.hzcu.pcap.dto;

import java.util.List;

public record AiSecurityModelOutput(
        String riskLevel,
        int riskScore,
        String summary,
        List<AiSecurityFinding> findings,
        List<String> normalObservations,
        List<String> recommendations
) {
}
