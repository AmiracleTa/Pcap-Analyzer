package com.hzcu.pcap.controller;

import com.hzcu.pcap.entity.CaptureFile;
import com.hzcu.pcap.service.FileStorageService;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".pcap", ".pcapng", ".cap");

    private final FileStorageService fileStorageService;

    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping
    public CaptureFile upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty() || !ALLOWED_EXTENSIONS.contains(extensionOf(file.getOriginalFilename()))) {
            throw new ResponseStatusException(BAD_REQUEST, "only .pcap, .pcapng and .cap files are allowed");
        }
        return fileStorageService.store(file);
    }

    @GetMapping
    public List<CaptureFile> list() {
        return fileStorageService.listFiles();
    }

    @GetMapping("/{id}")
    public CaptureFile get(@PathVariable Long id) {
        return fileStorageService.getFile(id);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<UrlResource> download(@PathVariable Long id) throws MalformedURLException {
        CaptureFile captureFile = fileStorageService.getFile(id);
        Path downloadPath = fileStorageService.getDownloadPath(id);
        UrlResource resource = new UrlResource(downloadPath.toUri());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + captureFile.getOriginalName() + "\"")
                .body(resource);
    }

    @DeleteMapping("/{id}")
    public Map<String, Boolean> delete(@PathVariable Long id) {
        fileStorageService.delete(id);
        return Map.of("deleted", true);
    }

    private String extensionOf(String fileName) {
        if (fileName == null) {
            return "";
        }
        int dot = fileName.lastIndexOf('.');
        if (dot < 0) {
            return "";
        }
        return fileName.substring(dot).toLowerCase(Locale.ROOT);
    }
}
