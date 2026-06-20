package com.hzcu.pcap.dto;

/**
 * 安全报告中的数据包证据。
 *
 * @param packetNo 数据包序号
 * @param time 时间文本
 * @param sourceIp 源 IP
 * @param destinationIp 目的 IP
 * @param sourcePort 源端口
 * @param destinationPort 目的端口
 * @param protocol 协议
 * @param length 数据包长度
 */
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
