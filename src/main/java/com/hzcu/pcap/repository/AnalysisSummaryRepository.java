package com.hzcu.pcap.repository;

import com.hzcu.pcap.entity.AnalysisSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AnalysisSummaryRepository extends JpaRepository<AnalysisSummary, Long> {

    Optional<AnalysisSummary> findByFileId(Long fileId);

    void deleteByFileId(Long fileId);
}
