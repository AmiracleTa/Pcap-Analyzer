package com.hzcu.pcap.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;

/**
 * 抓包文件的统计分析结果实体。
 */
@Entity
public class AnalysisSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long fileId;
    @Lob
    private String protocolJson;
    @Lob
    private String trafficTrendJson;
    @Lob
    private String ipJson;
    @Lob
    private String portJson;
    @Lob
    private String lengthDistributionJson;
    @Lob
    private String sourceIpTopJson;
    @Lob
    private String destinationIpTopJson;
    @Lob
    private String sourcePortTopJson;
    @Lob
    private String destinationPortTopJson;
    @Lob
    private String dnsRecordsJson;
    @Lob
    private String httpRecordsJson;
    private String startTimeText;
    private String endTimeText;

    /**
     * 获取统计记录主键。
     *
     * @return 统计记录主键
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置统计记录主键。
     *
     * @param id 统计记录主键
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
     * 获取协议分布 JSON。
     *
     * @return 协议分布 JSON
     */
    public String getProtocolJson() {
        return protocolJson;
    }

    /**
     * 设置协议分布 JSON。
     *
     * @param protocolJson 协议分布 JSON
     */
    public void setProtocolJson(String protocolJson) {
        this.protocolJson = protocolJson;
    }

    /**
     * 获取流量趋势 JSON。
     *
     * @return 流量趋势 JSON
     */
    public String getTrafficTrendJson() {
        return trafficTrendJson;
    }

    /**
     * 设置流量趋势 JSON。
     *
     * @param trafficTrendJson 流量趋势 JSON
     */
    public void setTrafficTrendJson(String trafficTrendJson) {
        this.trafficTrendJson = trafficTrendJson;
    }

    /**
     * 获取 IP 统计 JSON。
     *
     * @return IP 统计 JSON
     */
    public String getIpJson() {
        return ipJson;
    }

    /**
     * 设置 IP 统计 JSON。
     *
     * @param ipJson IP 统计 JSON
     */
    public void setIpJson(String ipJson) {
        this.ipJson = ipJson;
    }

    /**
     * 获取端口统计 JSON。
     *
     * @return 端口统计 JSON
     */
    public String getPortJson() {
        return portJson;
    }

    /**
     * 设置端口统计 JSON。
     *
     * @param portJson 端口统计 JSON
     */
    public void setPortJson(String portJson) {
        this.portJson = portJson;
    }

    /**
     * 获取包长度分布 JSON。
     *
     * @return 包长度分布 JSON
     */
    public String getLengthDistributionJson() {
        return lengthDistributionJson;
    }

    /**
     * 设置包长度分布 JSON。
     *
     * @param lengthDistributionJson 包长度分布 JSON
     */
    public void setLengthDistributionJson(String lengthDistributionJson) {
        this.lengthDistributionJson = lengthDistributionJson;
    }

    /**
     * 获取源 IP Top 统计 JSON。
     *
     * @return 源 IP Top 统计 JSON
     */
    public String getSourceIpTopJson() {
        return sourceIpTopJson;
    }

    /**
     * 设置源 IP Top 统计 JSON。
     *
     * @param sourceIpTopJson 源 IP Top 统计 JSON
     */
    public void setSourceIpTopJson(String sourceIpTopJson) {
        this.sourceIpTopJson = sourceIpTopJson;
    }

    /**
     * 获取目的 IP Top 统计 JSON。
     *
     * @return 目的 IP Top 统计 JSON
     */
    public String getDestinationIpTopJson() {
        return destinationIpTopJson;
    }

    /**
     * 设置目的 IP Top 统计 JSON。
     *
     * @param destinationIpTopJson 目的 IP Top 统计 JSON
     */
    public void setDestinationIpTopJson(String destinationIpTopJson) {
        this.destinationIpTopJson = destinationIpTopJson;
    }

    /**
     * 获取源端口 Top 统计 JSON。
     *
     * @return 源端口 Top 统计 JSON
     */
    public String getSourcePortTopJson() {
        return sourcePortTopJson;
    }

    /**
     * 设置源端口 Top 统计 JSON。
     *
     * @param sourcePortTopJson 源端口 Top 统计 JSON
     */
    public void setSourcePortTopJson(String sourcePortTopJson) {
        this.sourcePortTopJson = sourcePortTopJson;
    }

    /**
     * 获取目的端口 Top 统计 JSON。
     *
     * @return 目的端口 Top 统计 JSON
     */
    public String getDestinationPortTopJson() {
        return destinationPortTopJson;
    }

    /**
     * 设置目的端口 Top 统计 JSON。
     *
     * @param destinationPortTopJson 目的端口 Top 统计 JSON
     */
    public void setDestinationPortTopJson(String destinationPortTopJson) {
        this.destinationPortTopJson = destinationPortTopJson;
    }

    /**
     * 获取 DNS 记录 JSON。
     *
     * @return DNS 记录 JSON
     */
    public String getDnsRecordsJson() {
        return dnsRecordsJson;
    }

    /**
     * 设置 DNS 记录 JSON。
     *
     * @param dnsRecordsJson DNS 记录 JSON
     */
    public void setDnsRecordsJson(String dnsRecordsJson) {
        this.dnsRecordsJson = dnsRecordsJson;
    }

    /**
     * 获取 HTTP 记录 JSON。
     *
     * @return HTTP 记录 JSON
     */
    public String getHttpRecordsJson() {
        return httpRecordsJson;
    }

    /**
     * 设置 HTTP 记录 JSON。
     *
     * @param httpRecordsJson HTTP 记录 JSON
     */
    public void setHttpRecordsJson(String httpRecordsJson) {
        this.httpRecordsJson = httpRecordsJson;
    }

    /**
     * 获取首包时间戳文本。
     *
     * @return 首包时间戳文本
     */
    public String getStartTimeText() {
        return startTimeText;
    }

    /**
     * 设置首包时间戳文本。
     *
     * @param startTimeText 首包时间戳文本
     */
    public void setStartTimeText(String startTimeText) {
        this.startTimeText = startTimeText;
    }

    /**
     * 获取尾包时间戳文本。
     *
     * @return 尾包时间戳文本
     */
    public String getEndTimeText() {
        return endTimeText;
    }

    /**
     * 设置尾包时间戳文本。
     *
     * @param endTimeText 尾包时间戳文本
     */
    public void setEndTimeText(String endTimeText) {
        this.endTimeText = endTimeText;
    }
}
