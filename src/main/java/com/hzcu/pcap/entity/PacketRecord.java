package com.hzcu.pcap.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;

@Entity
public class PacketRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long fileId;
    private Long packetNo;
    private String timestampText;
    private String sourceIp;
    private String destinationIp;
    private Integer sourcePort;
    private Integer destinationPort;
    private String protocol;
    private Integer length;
    private String info;
    @Lob
    private String detailJson;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFileId() {
        return fileId;
    }

    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }

    public Long getPacketNo() {
        return packetNo;
    }

    public void setPacketNo(Long packetNo) {
        this.packetNo = packetNo;
    }

    public String getTimestampText() {
        return timestampText;
    }

    public void setTimestampText(String timestampText) {
        this.timestampText = timestampText;
    }

    public String getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }

    public String getDestinationIp() {
        return destinationIp;
    }

    public void setDestinationIp(String destinationIp) {
        this.destinationIp = destinationIp;
    }

    public Integer getSourcePort() {
        return sourcePort;
    }

    public void setSourcePort(Integer sourcePort) {
        this.sourcePort = sourcePort;
    }

    public Integer getDestinationPort() {
        return destinationPort;
    }

    public void setDestinationPort(Integer destinationPort) {
        this.destinationPort = destinationPort;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getDetailJson() {
        return detailJson;
    }

    public void setDetailJson(String detailJson) {
        this.detailJson = detailJson;
    }
}
