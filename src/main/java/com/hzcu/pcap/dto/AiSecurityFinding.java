package com.hzcu.pcap.dto;

import java.util.List;

public record AiSecurityFinding(
        String category,
        String severity,
        String title,
        String explanation,
        List<SecurityEvidence> evidence,
        String recommendation
) {
}
