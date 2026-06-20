package com.hzcu.pcap.service;

import com.hzcu.pcap.entity.PacketRecord;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.List;

/**
 * 使用 JDBC 批量写入数据包记录。
 */
@Service
public class PacketRecordBatchWriter {

    private static final int BATCH_SIZE = 1000;
    private static final String INSERT_SQL = """
            insert into packet_record
            (file_id, packet_no, timestamp_text, source_ip, destination_ip, source_port, destination_port, protocol, length, info, detail_json)
            values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private final JdbcTemplate jdbcTemplate;

    /**
     * 创建数据包批量写入器。
     *
     * @param jdbcTemplate Spring JDBC 模板
     */
    public PacketRecordBatchWriter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 批量保存数据包记录。
     *
     * @param records 待保存的数据包记录
     */
    public void saveAll(List<PacketRecord> records) {
        if (records.isEmpty()) {
            return;
        }
        jdbcTemplate.batchUpdate(INSERT_SQL, records, BATCH_SIZE, this::setValues);
    }

    private void setValues(PreparedStatement statement, PacketRecord record) throws java.sql.SQLException {
        setLong(statement, 1, record.getFileId());
        setLong(statement, 2, record.getPacketNo());
        setString(statement, 3, record.getTimestampText());
        setString(statement, 4, record.getSourceIp());
        setString(statement, 5, record.getDestinationIp());
        setInteger(statement, 6, record.getSourcePort());
        setInteger(statement, 7, record.getDestinationPort());
        setString(statement, 8, record.getProtocol());
        setInteger(statement, 9, record.getLength());
        setString(statement, 10, record.getInfo());
        setString(statement, 11, record.getDetailJson());
    }

    private void setLong(PreparedStatement statement, int index, Long value) throws java.sql.SQLException {
        if (value == null) {
            statement.setNull(index, Types.BIGINT);
        } else {
            statement.setLong(index, value);
        }
    }

    private void setInteger(PreparedStatement statement, int index, Integer value) throws java.sql.SQLException {
        if (value == null) {
            statement.setNull(index, Types.INTEGER);
        } else {
            statement.setInt(index, value);
        }
    }

    private void setString(PreparedStatement statement, int index, String value) throws java.sql.SQLException {
        if (value == null) {
            statement.setNull(index, Types.LONGVARCHAR);
        } else {
            statement.setString(index, value);
        }
    }
}
