package com.hzcu.pcap.repository;

import com.hzcu.pcap.entity.SecurityReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * AI 安全报告仓储。
 */
public interface SecurityReportRepository extends JpaRepository<SecurityReport, Long> {

    /**
     * 查询指定文件最近创建的一条安全报告。
     *
     * @param fileId 文件 ID
     * @return 最近安全报告
     */
    Optional<SecurityReport> findTopByFileIdOrderByCreatedAtDesc(Long fileId);

    /**
     * 按创建时间倒序查询指定文件的全部安全报告。
     *
     * @param fileId 文件 ID
     * @return 安全报告列表
     */
    List<SecurityReport> findByFileIdOrderByCreatedAtDesc(Long fileId);

    /**
     * 删除指定文件关联的全部安全报告。
     *
     * @param fileId 文件 ID
     */
    void deleteByFileId(Long fileId);
}
