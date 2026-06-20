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

/**
 * 处理抓包文件的上传、查询、下载和删除请求。
 */
@RestController
@RequestMapping("/api/files")
public class FileController {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".pcap", ".pcapng", ".cap");

    private final FileStorageService fileStorageService;

    /**
     * 创建文件控制器。
     *
     * @param fileStorageService 文件存储服务
     */
    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    /**
     * 上传抓包文件，并保存文件元数据。
     *
     * @param file 表单字段名为 {@code file} 的上传文件
     * @return 保存后的文件元数据
     */
    @PostMapping
    public CaptureFile upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty() || !ALLOWED_EXTENSIONS.contains(extensionOf(file.getOriginalFilename()))) {
            throw new ResponseStatusException(BAD_REQUEST, "only .pcap, .pcapng and .cap files are allowed");
        }
        return fileStorageService.store(file);
    }

    /**
     * 查询所有已上传的抓包文件。
     *
     * @return 文件元数据列表
     */
    @GetMapping
    public List<CaptureFile> list() {
        return fileStorageService.listFiles();
    }

    /**
     * 根据文件 ID 查询抓包文件元数据。
     *
     * @param id 文件 ID
     * @return 文件元数据
     */
    @GetMapping("/{id}")
    public CaptureFile get(@PathVariable Long id) {
        return fileStorageService.getFile(id);
    }

    /**
     * 下载指定抓包文件的原始内容。
     *
     * @param id 文件 ID
     * @return 文件下载响应
     * @throws MalformedURLException 当本地文件路径无法转换为 URL 时抛出
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<UrlResource> download(@PathVariable Long id) throws MalformedURLException {
        CaptureFile captureFile = fileStorageService.getFile(id);
        Path downloadPath = fileStorageService.getDownloadPath(id);
        UrlResource resource = new UrlResource(downloadPath.toUri());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + captureFile.getOriginalName() + "\"")
                .body(resource);
    }

    /**
     * 删除指定抓包文件及其关联分析结果。
     *
     * @param id 文件 ID
     * @return 删除结果标记
     */
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
