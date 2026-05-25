package com.hzcu.pcap.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hzcu.pcap.entity.CaptureFile;
import com.hzcu.pcap.entity.PacketRecord;
import com.hzcu.pcap.repository.CaptureFileRepository;
import com.hzcu.pcap.repository.PacketRecordRepository;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AiSecurityContextServiceTests {

    @Test
    void buildContextOnlySendsPacketTableTrendAndProtocolDistribution() throws Exception {
        CaptureFile captureFile = new CaptureFile();
        captureFile.setId(12L);
        captureFile.setOriginalName("capture-with-sensitive-name.pcapng");
        captureFile.setFileSize(4096L);
        captureFile.setPacketCount(220L);
        captureFile.setStatus("analyzed");

        CaptureFileRepository captureFileRepository = mock(CaptureFileRepository.class);
        PacketRecordRepository packetRecordRepository = mock(PacketRecordRepository.class);
        SummaryService summaryService = mock(SummaryService.class);
        ObjectMapper objectMapper = new ObjectMapper();

        when(captureFileRepository.findById(12L)).thenReturn(Optional.of(captureFile));
        when(summaryService.getSummary(12L)).thenReturn(Map.ofEntries(
                Map.entry("protocols", Map.of("TCP", 80, "DNS", 40)),
                Map.entry("trafficTrend", List.of(
                        Map.of("time", "2026-05-21 15:56:00", "count", 60),
                        Map.of("time", "2026-05-21 15:57:00", "count", 60)
                )),
                Map.entry("sourceIpTop", List.of(Map.of("name", "10.0.0.1", "value", 80))),
                Map.entry("destinationPortTop", List.of(Map.of("name", "443", "value", 80))),
                Map.entry("dnsRecords", List.of(Map.of("queryName", "example.com"))),
                Map.entry("httpRecords", List.of(Map.of("uri", "/admin"))),
                Map.entry("lengthDistribution", List.of(Map.of("name", "0-100", "value", 1)))
        ));
        when(packetRecordRepository.findByFileId(12L)).thenReturn(reversedPackets(220));

        AiSecurityContextService service = new AiSecurityContextService(
                captureFileRepository,
                packetRecordRepository,
                summaryService,
                objectMapper
        );

        JsonNode root = objectMapper.readTree(service.buildContext(12L));

        assertThat(root.has("file")).isFalse();
        assertThat(root.size()).isEqualTo(3);
        assertThat(root.has("trafficOverview")).isFalse();
        assertThat(root.has("ipPairStats")).isFalse();
        assertThat(root.has("conversationStats")).isFalse();
        assertThat(root.has("destinationPortStats")).isFalse();
        assertThat(root.has("protocolSamples")).isFalse();
        assertThat(root.path("summary").size()).isEqualTo(2);
        assertThat(root.path("summary").has("protocols")).isTrue();
        assertThat(root.path("summary").has("trafficTrend")).isTrue();
        assertThat(root.path("summary").has("dnsRecords")).isFalse();
        assertThat(root.path("summary").has("httpRecords")).isFalse();
        assertThat(root.path("summary").has("sourceIpTop")).isFalse();
        assertThat(root.path("summary").has("destinationPortTop")).isFalse();
        assertThat(root.path("summary").has("lengthDistribution")).isFalse();

        JsonNode samples = root.path("packetSamples");
        assertThat(samples).hasSize(200);
        assertThat(samples.get(0).path("packetNo").asLong()).isEqualTo(1);
        assertThat(samples.get(0).path("time").asText()).isEqualTo("2026-05-21 15:56:01");
        assertThat(samples.get(199).path("time").asText()).isEqualTo("2026-05-21 15:56:200");
        assertThat(samples.get(0).size()).isEqualTo(8);
        assertThat(samples.get(0).has("sourceIp")).isTrue();
        assertThat(samples.get(0).has("destinationIp")).isTrue();
        assertThat(samples.get(0).has("protocol")).isTrue();
        assertThat(samples.get(0).has("sourcePort")).isTrue();
        assertThat(samples.get(0).has("destinationPort")).isTrue();
        assertThat(samples.get(0).has("length")).isTrue();
        assertThat(samples.get(0).has("info")).isFalse();
        assertThat(samples.get(0).has("detailJson")).isFalse();
    }

    private List<PacketRecord> reversedPackets(int count) {
        List<PacketRecord> packets = new ArrayList<>();
        for (int i = count; i >= 1; i--) {
            PacketRecord packet = new PacketRecord();
            packet.setPacketNo((long) i);
            packet.setTimestampText("2026-05-21 15:56:" + String.format("%02d", i));
            packet.setSourceIp("10.0.0." + (i % 5));
            packet.setDestinationIp("172.16.0." + (i % 7));
            packet.setSourcePort(40000 + i);
            packet.setDestinationPort(443);
            packet.setProtocol(i % 2 == 0 ? "TCP" : "DNS");
            packet.setLength(128 + i);
            packet.setInfo("packet " + i);
            packet.setDetailJson("{\"frame\":\"" + i + "\"}");
            packets.add(packet);
        }
        return packets;
    }
}
