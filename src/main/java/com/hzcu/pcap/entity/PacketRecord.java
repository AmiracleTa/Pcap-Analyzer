package com.hzcu.pcap.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;

/**
 * 单条数据包解析结果实体。
 */
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
    @Lob
    private String info;
    @Lob
    private String detailJson;

    /**
     * 获取数据包记录主键。
     *
     * @return 数据包记录主键
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置数据包记录主键。
     *
     * @param id 数据包记录主键
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 获取所属抓包文件 ID。
     *
     * @return 文件 ID
     */
    public Long getFileId() {
        return fileId;
    }

    /**
     * 设置所属抓包文件 ID。
     *
     * @param fileId 文件 ID
     */
    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }

    /**
     * 获取数据包序号。
     *
     * @return 数据包序号
     */
    public Long getPacketNo() {
        return packetNo;
    }

    /**
     * 设置数据包序号。
     *
     * @param packetNo 数据包序号
     */
    public void setPacketNo(Long packetNo) {
        this.packetNo = packetNo;
    }

    /**
     * 获取时间戳文本。
     *
     * @return 时间戳文本
     */
    public String getTimestampText() {
        return timestampText;
    }

    /**
     * 设置时间戳文本。
     *
     * @param timestampText 时间戳文本
     */
    public void setTimestampText(String timestampText) {
        this.timestampText = timestampText;
    }

    /**
     * 获取源 IP 地址。
     *
     * @return 源 IP 地址
     */
    public String getSourceIp() {
        return sourceIp;
    }

    /**
     * 设置源 IP 地址。
     *
     * @param sourceIp 源 IP 地址
     */
    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }

    /**
     * 获取目的 IP 地址。
     *
     * @return 目的 IP 地址
     */
    public String getDestinationIp() {
        return destinationIp;
    }

    /**
     * 设置目的 IP 地址。
     *
     * @param destinationIp 目的 IP 地址
     */
    public void setDestinationIp(String destinationIp) {
        this.destinationIp = destinationIp;
    }

    /**
     * 获取源端口。
     *
     * @return 源端口
     */
    public Integer getSourcePort() {
        return sourcePort;
    }

    /**
     * 设置源端口。
     *
     * @param sourcePort 源端口
     */
    public void setSourcePort(Integer sourcePort) {
        this.sourcePort = sourcePort;
    }

    /**
     * 获取目的端口。
     *
     * @return 目的端口
     */
    public Integer getDestinationPort() {
        return destinationPort;
    }

    /**
     * 设置目的端口。
     *
     * @param destinationPort 目的端口
     */
    public void setDestinationPort(Integer destinationPort) {
        this.destinationPort = destinationPort;
    }

    /**
     * 获取协议名称。
     *
     * @return 协议名称
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * 设置协议名称。
     *
     * @param protocol 协议名称
     */
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    /**
     * 获取数据包长度。
     *
     * @return 数据包长度
     */
    public Integer getLength() {
        return length;
    }

    /**
     * 设置数据包长度。
     *
     * @param length 数据包长度
     */
    public void setLength(Integer length) {
        this.length = length;
    }

    /**
     * 获取数据包摘要信息。
     *
     * @return 摘要信息
     */
    public String getInfo() {
        return info;
    }

    /**
     * 设置数据包摘要信息。
     *
     * @param info 摘要信息
     */
    public void setInfo(String info) {
        this.info = info;
    }

    /**
     * 获取数据包详情 JSON。
     *
     * @return 详情 JSON
     */
    public String getDetailJson() {
        return detailJson;
    }

    /**
     * 设置数据包详情 JSON。
     *
     * @param detailJson 详情 JSON
     */
    public void setDetailJson(String detailJson) {
        this.detailJson = detailJson;
    }
}
