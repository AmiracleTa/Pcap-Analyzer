package com.hzcu.pcap.service;

import com.hzcu.pcap.entity.CaptureFile;
import com.hzcu.pcap.repository.AnalysisSummaryRepository;
import com.hzcu.pcap.repository.CaptureFileRepository;
import com.hzcu.pcap.repository.PacketRecordRepository;
import com.hzcu.pcap.repository.SecurityReportRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * 管理抓包文件在本地磁盘和数据库中的元数据。
 */
@Service
public class FileStorageService {

    private final CaptureFileRepository captureFileRepository;
    private final PacketRecordRepository packetRecordRepository;
    private final AnalysisSummaryRepository analysisSummaryRepository;
    private final SecurityReportRepository securityReportRepository;
    private final Path uploadPath;

    /**
     * 创建文件存储服务。
     *
     * @param captureFileRepository 抓包文件仓储
     * @param packetRecordRepository 数据包记录仓储
     * @param analysisSummaryRepository 分析统计仓储
     * @param securityReportRepository 安全报告仓储
     * @param uploadDir 文件上传目录
     */
    public FileStorageService(CaptureFileRepository captureFileRepository,
                              PacketRecordRepository packetRecordRepository,
                              AnalysisSummaryRepository analysisSummaryRepository,
                              SecurityReportRepository securityReportRepository,
                              @Value("${app.upload-dir:uploads}") String uploadDir) {
        this.captureFileRepository = captureFileRepository;
        this.packetRecordRepository = packetRecordRepository;
        this.analysisSummaryRepository = analysisSummaryRepository;
        this.securityReportRepository = securityReportRepository;
        this.uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    /**
     * 保存上传文件，并创建初始状态为 {@code uploaded} 的文件记录。
     *
     * @param file 用户上传的抓包文件
     * @return 保存后的文件元数据
     */
    public CaptureFile store(MultipartFile file) {
        try {
            Files.createDirectories(uploadPath);
            String originalName = file.getOriginalFilename() == null ? "capture.pcap" : file.getOriginalFilename();
            String fileType = extensionOf(originalName);
            String storedName = UUID.randomUUID() + fileType;
            Path targetPath = uploadPath.resolve(storedName).normalize();
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            CaptureFile captureFile = new CaptureFile();
            captureFile.setStoredName(storedName);
            captureFile.setOriginalName(originalName);
            captureFile.setFileSize(file.getSize());
            captureFile.setFileType(fileType);
            captureFile.setUploadTime(LocalDateTime.now());
            captureFile.setPacketCount(0L);
            captureFile.setStatus("uploaded");
            return captureFileRepository.save(captureFile);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to store uploaded file", e);
        }
    }

    /**
     * 查询所有抓包文件记录。
     *
     * @return 文件元数据列表
     */
    public List<CaptureFile> listFiles() {
        return captureFileRepository.findAll();
    }

    /**
     * 根据 ID 查询抓包文件记录。
     *
     * @param id 文件 ID
     * @return 文件元数据
     */
    public CaptureFile getFile(Long id) {
        return captureFileRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "file not found"));
    }

    /**
     * 删除抓包文件及其关联的数据包、统计和安全报告。
     *
     * @param id 文件 ID
     */
    @Transactional
    public void delete(Long id) {
        CaptureFile captureFile = getFile(id);
        try {
            Files.deleteIfExists(getDownloadPath(id));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to delete stored file", e);
        }
        packetRecordRepository.deleteByFileId(id);
        analysisSummaryRepository.deleteByFileId(id);
        securityReportRepository.deleteByFileId(id);
        captureFileRepository.delete(captureFile);
    }

    /**
     * 获取指定抓包文件在本地磁盘上的路径。
     *
     * @param id 文件 ID
     * @return 本地文件路径
     */
    public Path getDownloadPath(Long id) {
        CaptureFile captureFile = getFile(id);
        return uploadPath.resolve(captureFile.getStoredName()).normalize();
    }

    private String extensionOf(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if (dot < 0) {
            return "";
        }
        return fileName.substring(dot).toLowerCase(Locale.ROOT);
    }
}
