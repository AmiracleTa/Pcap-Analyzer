package com.hzcu.pcap.repository;

import com.hzcu.pcap.entity.CaptureFile;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 抓包文件元数据仓储。
 */
public interface CaptureFileRepository extends JpaRepository<CaptureFile, Long> {
}
