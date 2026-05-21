package com.hzcu.pcap.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;

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

    public String getProtocolJson() {
        return protocolJson;
    }

    public void setProtocolJson(String protocolJson) {
        this.protocolJson = protocolJson;
    }

    public String getTrafficTrendJson() {
        return trafficTrendJson;
    }

    public void setTrafficTrendJson(String trafficTrendJson) {
        this.trafficTrendJson = trafficTrendJson;
    }

    public String getIpJson() {
        return ipJson;
    }

    public void setIpJson(String ipJson) {
        this.ipJson = ipJson;
    }

    public String getPortJson() {
        return portJson;
    }

    public void setPortJson(String portJson) {
        this.portJson = portJson;
    }

    public String getLengthDistributionJson() {
        return lengthDistributionJson;
    }

    public void setLengthDistributionJson(String lengthDistributionJson) {
        this.lengthDistributionJson = lengthDistributionJson;
    }

    public String getSourceIpTopJson() {
        return sourceIpTopJson;
    }

    public void setSourceIpTopJson(String sourceIpTopJson) {
        this.sourceIpTopJson = sourceIpTopJson;
    }

    public String getDestinationIpTopJson() {
        return destinationIpTopJson;
    }

    public void setDestinationIpTopJson(String destinationIpTopJson) {
        this.destinationIpTopJson = destinationIpTopJson;
    }

    public String getSourcePortTopJson() {
        return sourcePortTopJson;
    }

    public void setSourcePortTopJson(String sourcePortTopJson) {
        this.sourcePortTopJson = sourcePortTopJson;
    }

    public String getDestinationPortTopJson() {
        return destinationPortTopJson;
    }

    public void setDestinationPortTopJson(String destinationPortTopJson) {
        this.destinationPortTopJson = destinationPortTopJson;
    }

    public String getDnsRecordsJson() {
        return dnsRecordsJson;
    }

    public void setDnsRecordsJson(String dnsRecordsJson) {
        this.dnsRecordsJson = dnsRecordsJson;
    }

    public String getHttpRecordsJson() {
        return httpRecordsJson;
    }

    public void setHttpRecordsJson(String httpRecordsJson) {
        this.httpRecordsJson = httpRecordsJson;
    }

    public String getStartTimeText() {
        return startTimeText;
    }

    public void setStartTimeText(String startTimeText) {
        this.startTimeText = startTimeText;
    }

    public String getEndTimeText() {
        return endTimeText;
    }

    public void setEndTimeText(String endTimeText) {
        this.endTimeText = endTimeText;
    }
}
