package com.hzcu.pcap.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStoredName() {
        return storedName;
    }

    public void setStoredName(String storedName) {
        this.storedName = storedName;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public LocalDateTime getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(LocalDateTime uploadTime) {
        this.uploadTime = uploadTime;
    }

    public Long getPacketCount() {
        return packetCount;
    }

    public void setPacketCount(Long packetCount) {
        this.packetCount = packetCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
