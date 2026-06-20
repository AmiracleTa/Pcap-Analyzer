package com.hzcu.pcap.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

/**
 * 上传抓包文件的元数据实体。
 */
@Entity
public class CaptureFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String storedName;
    private String originalName;
    private Long fileSize;
    private String fileType;
    private LocalDateTime uploadTime;
    private Long packetCount;
    private String status;

    /**
     * 获取文件记录主键。
     *
     * @return 文件记录主键
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置文件记录主键。
     *
     * @param id 文件记录主键
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 获取服务端保存的文件名。
     *
     * @return 服务端保存的文件名
     */
    public String getStoredName() {
        return storedName;
    }

    /**
     * 设置服务端保存的文件名。
     *
     * @param storedName 服务端保存的文件名
     */
    public void setStoredName(String storedName) {
        this.storedName = storedName;
    }

    /**
     * 获取用户上传时的原始文件名。
     *
     * @return 原始文件名
     */
    public String getOriginalName() {
        return originalName;
    }

    /**
     * 设置用户上传时的原始文件名。
     *
     * @param originalName 原始文件名
     */
    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    /**
     * 获取文件大小，单位字节。
     *
     * @return 文件大小
     */
    public Long getFileSize() {
        return fileSize;
    }

    /**
     * 设置文件大小，单位字节。
     *
     * @param fileSize 文件大小
     */
    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    /**
     * 获取文件扩展名。
     *
     * @return 文件扩展名
     */
    public String getFileType() {
        return fileType;
    }

    /**
     * 设置文件扩展名。
     *
     * @param fileType 文件扩展名
     */
    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    /**
     * 获取上传时间。
     *
     * @return 上传时间
     */
    public LocalDateTime getUploadTime() {
        return uploadTime;
    }

    /**
     * 设置上传时间。
     *
     * @param uploadTime 上传时间
     */
    public void setUploadTime(LocalDateTime uploadTime) {
        this.uploadTime = uploadTime;
    }

    /**
     * 获取已解析的数据包数量。
     *
     * @return 数据包数量
     */
    public Long getPacketCount() {
        return packetCount;
    }

    /**
     * 设置已解析的数据包数量。
     *
     * @param packetCount 数据包数量
     */
    public void setPacketCount(Long packetCount) {
        this.packetCount = packetCount;
    }

    /**
     * 获取文件分析状态。
     *
     * @return 文件分析状态
     */
    public String getStatus() {
        return status;
    }

    /**
     * 设置文件分析状态。
     *
     * @param status 文件分析状态
     */
    public void setStatus(String status) {
        this.status = status;
    }
}
