package com.hzcu.pcap.repository;

import com.hzcu.pcap.entity.PacketRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PacketRecordRepository extends JpaRepository<PacketRecord, Long> {

    List<PacketRecord> findByFileId(Long fileId);

    void deleteByFileId(Long fileId);
}
