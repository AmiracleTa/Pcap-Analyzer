package com.hzcu.pcap.dto;

public record SecurityEvidence(
        Long packetNo,
        String time,
        String sourceIp,
        String destinationIp,
        Integer sourcePort,
        Integer destinationPort,
        String protocol,
        Integer length
) {
}
