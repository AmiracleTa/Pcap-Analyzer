package com.hzcu.pcap.repository;

import com.hzcu.pcap.entity.CaptureFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CaptureFileRepository extends JpaRepository<CaptureFile, Long> {
}
