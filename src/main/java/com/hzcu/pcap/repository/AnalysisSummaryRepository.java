package com.hzcu.pcap.repository;

import com.hzcu.pcap.entity.AnalysisSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 分析统计结果仓储。
 */
public interface AnalysisSummaryRepository extends JpaRepository<AnalysisSummary, Long> {

    /**
     * 根据文件 ID 查询分析统计结果。
     *
     * @param fileId 文件 ID
     * @return 分析统计结果
     */
    Optional<AnalysisSummary> findByFileId(Long fileId);

    /**
     * 删除指定文件关联的分析统计结果。
     *
     * @param fileId 文件 ID
     */
    void deleteByFileId(Long fileId);
}
