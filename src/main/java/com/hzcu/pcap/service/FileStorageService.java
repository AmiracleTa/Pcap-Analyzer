package com.hzcu.pcap.service;

import com.hzcu.pcap.entity.CaptureFile;
import com.hzcu.pcap.repository.AnalysisSummaryRepository;
import com.hzcu.pcap.repository.CaptureFileRepository;
import com.hzcu.pcap.repository.PacketRecordRepository;
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

@Service
public class FileStorageService {

    private final CaptureFileRepository captureFileRepository;
    private final PacketRecordRepository packetRecordRepository;
    private final AnalysisSummaryRepository analysisSummaryRepository;
    private final Path uploadPath;

    public FileStorageService(CaptureFileRepository captureFileRepository,
                              PacketRecordRepository packetRecordRepository,
                              AnalysisSummaryRepository analysisSummaryRepository,
                              @Value("${app.upload-dir:uploads}") String uploadDir) {
        this.captureFileRepository = captureFileRepository;
        this.packetRecordRepository = packetRecordRepository;
        this.analysisSummaryRepository = analysisSummaryRepository;
        this.uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

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

    public List<CaptureFile> listFiles() {
        return captureFileRepository.findAll();
    }

    public CaptureFile getFile(Long id) {
        return captureFileRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "file not found"));
    }

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
        captureFileRepository.delete(captureFile);
    }

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
