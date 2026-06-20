package com.hzcu.pcap.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;

import java.time.LocalDateTime;

/**
 * AI 安全报告实体。
 */
@Entity
public class SecurityReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long fileId;
    private Boolean available;
    private String provider;
    private String model;
    private String riskLevel;
    private Integer riskScore;
    @Lob
    private String summaryText;
    @Lob
    private String findingsJson;
    @Lob
    private String normalObservationsJson;
    @Lob
    private String recommendationsJson;
    @Lob
    private String message;
    private LocalDateTime createdAt;

    /**
     * 获取安全报告主键。
     *
     * @return 安全报告主键
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置安全报告主键。
     *
     * @param id 安全报告主键
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
     * 获取报告是否可用。
     *
     * @return 报告是否可用
     */
    public Boolean getAvailable() {
        return available;
    }

    /**
     * 设置报告是否可用。
     *
     * @param available 报告是否可用
     */
    public void setAvailable(Boolean available) {
        this.available = available;
    }

    /**
     * 获取 AI 提供商名称。
     *
     * @return AI 提供商名称
     */
    public String getProvider() {
        return provider;
    }

    /**
     * 设置 AI 提供商名称。
     *
     * @param provider AI 提供商名称
     */
    public void setProvider(String provider) {
        this.provider = provider;
    }

    /**
     * 获取模型名称。
     *
     * @return 模型名称
     */
    public String getModel() {
        return model;
    }

    /**
     * 设置模型名称。
     *
     * @param model 模型名称
     */
    public void setModel(String model) {
        this.model = model;
    }

    /**
     * 获取风险等级。
     *
     * @return 风险等级
     */
    public String getRiskLevel() {
        return riskLevel;
    }

    /**
     * 设置风险等级。
     *
     * @param riskLevel 风险等级
     */
    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    /**
     * 获取风险分数。
     *
     * @return 风险分数
     */
    public Integer getRiskScore() {
        return riskScore;
    }

    /**
     * 设置风险分数。
     *
     * @param riskScore 风险分数
     */
    public void setRiskScore(Integer riskScore) {
        this.riskScore = riskScore;
    }

    /**
     * 获取安全摘要文本。
     *
     * @return 安全摘要文本
     */
    public String getSummaryText() {
        return summaryText;
    }

    /**
     * 设置安全摘要文本。
     *
     * @param summaryText 安全摘要文本
     */
    public void setSummaryText(String summaryText) {
        this.summaryText = summaryText;
    }

    /**
     * 获取风险发现 JSON。
     *
     * @return 风险发现 JSON
     */
    public String getFindingsJson() {
        return findingsJson;
    }

    /**
     * 设置风险发现 JSON。
     *
     * @param findingsJson 风险发现 JSON
     */
    public void setFindingsJson(String findingsJson) {
        this.findingsJson = findingsJson;
    }

    /**
     * 获取正常观察项 JSON。
     *
     * @return 正常观察项 JSON
     */
    public String getNormalObservationsJson() {
        return normalObservationsJson;
    }

    /**
     * 设置正常观察项 JSON。
     *
     * @param normalObservationsJson 正常观察项 JSON
     */
    public void setNormalObservationsJson(String normalObservationsJson) {
        this.normalObservationsJson = normalObservationsJson;
    }

    /**
     * 获取处置建议 JSON。
     *
     * @return 处置建议 JSON
     */
    public String getRecommendationsJson() {
        return recommendationsJson;
    }

    /**
     * 设置处置建议 JSON。
     *
     * @param recommendationsJson 处置建议 JSON
     */
    public void setRecommendationsJson(String recommendationsJson) {
        this.recommendationsJson = recommendationsJson;
    }

    /**
     * 获取报告状态消息。
     *
     * @return 报告状态消息
     */
    public String getMessage() {
        return message;
    }

    /**
     * 设置报告状态消息。
     *
     * @param message 报告状态消息
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * 获取报告创建时间。
     *
     * @return 报告创建时间
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * 设置报告创建时间。
     *
     * @param createdAt 报告创建时间
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
