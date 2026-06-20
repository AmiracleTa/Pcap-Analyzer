package com.hzcu.pcap.repository;

import com.hzcu.pcap.entity.PacketRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 数据包记录仓储。
 */
public interface PacketRecordRepository extends JpaRepository<PacketRecord, Long> {

    /**
     * 查询指定文件的全部数据包记录。
     *
     * @param fileId 文件 ID
     * @return 数据包记录列表
     */
    List<PacketRecord> findByFileId(Long fileId);

    /**
     * 删除指定文件关联的全部数据包记录。
     *
     * @param fileId 文件 ID
     */
    void deleteByFileId(Long fileId);
}
