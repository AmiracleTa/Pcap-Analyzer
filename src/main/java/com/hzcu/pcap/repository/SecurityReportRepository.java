package com.hzcu.pcap.repository;

import com.hzcu.pcap.entity.SecurityReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SecurityReportRepository extends JpaRepository<SecurityReport, Long> {

    Optional<SecurityReport> findTopByFileIdOrderByCreatedAtDesc(Long fileId);

    List<SecurityReport> findByFileIdOrderByCreatedAtDesc(Long fileId);

    void deleteByFileId(Long fileId);
}
