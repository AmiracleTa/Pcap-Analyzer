package com.hzcu.pcap.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hzcu.pcap.entity.AnalysisSummary;
import com.hzcu.pcap.entity.CaptureFile;
import com.hzcu.pcap.entity.PacketRecord;
import com.hzcu.pcap.repository.AnalysisSummaryRepository;
import com.hzcu.pcap.repository.CaptureFileRepository;
import com.hzcu.pcap.repository.PacketRecordRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PacketAnalysisServiceTests {

    @Test
    void analyzeStoresPacketsAndBuildsSummaryFromTsharkFields() throws Exception {
        CaptureFile captureFile = new CaptureFile();
        captureFile.setId(9L);
        captureFile.setStoredName("sample.pcapng");
        captureFile.setPacketCount(0L);
        captureFile.setStatus("uploaded");

        FileStorageService fileStorageService = mock(FileStorageService.class);
        CaptureFileRepository captureFileRepository = mock(CaptureFileRepository.class);
        PacketRecordRepository packetRecordRepository = mock(PacketRecordRepository.class);
        AnalysisSummaryRepository analysisSummaryRepository = mock(AnalysisSummaryRepository.class);
        ObjectMapper objectMapper = new ObjectMapper();

        when(fileStorageService.getFile(9L)).thenReturn(captureFile);
        when(fileStorageService.getDownloadPath(9L)).thenReturn(Path.of("sample.pcapng"));
        when(packetRecordRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(captureFileRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(analysisSummaryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        PacketAnalysisService service = new PacketAnalysisService(
                fileStorageService,
                captureFileRepository,
                packetRecordRepository,
                analysisSummaryRepository,
                new FakeTsharkCommandRunner(),
                objectMapper
        );

        Map<String, Object> result = service.analyze(9L);

        assertThat(result).containsEntry("status", "ok").containsEntry("packetCount", 3);
        assertThat(captureFile.getPacketCount()).isEqualTo(3L);
        assertThat(captureFile.getStatus()).isEqualTo("analyzed");
        verify(packetRecordRepository).deleteByFileId(9L);
        verify(analysisSummaryRepository).deleteByFileId(9L);

        ArgumentCaptor<Iterable<PacketRecord>> recordsCaptor = ArgumentCaptor.forClass(Iterable.class);
        verify(packetRecordRepository).saveAll(recordsCaptor.capture());
        List<PacketRecord> records = stream(recordsCaptor.getValue());
        assertThat(records).hasSize(3);
        assertThat(records.get(0).getPacketNo()).isEqualTo(1L);
        assertThat(records.get(0).getSourceIp()).isEqualTo("192.168.1.2");
        assertThat(records.get(0).getDestinationIp()).isEqualTo("93.184.216.34");
        assertThat(records.get(0).getSourcePort()).isEqualTo(51514);
        assertThat(records.get(0).getDestinationPort()).isEqualTo(443);
        assertThat(records.get(2).getProtocol()).isEqualTo("DNS");
        assertThat(records.get(2).getDetailJson()).isEqualTo("{}");

        ArgumentCaptor<AnalysisSummary> summaryCaptor = ArgumentCaptor.forClass(AnalysisSummary.class);
        verify(analysisSummaryRepository).save(summaryCaptor.capture());
        AnalysisSummary summary = summaryCaptor.getValue();
        JsonNode protocolJson = objectMapper.readTree(summary.getProtocolJson());
        JsonNode trendJson = objectMapper.readTree(summary.getTrafficTrendJson());
        JsonNode lengthDistributionJson = objectMapper.readTree(summary.getLengthDistributionJson());
        JsonNode sourceIpTopJson = objectMapper.readTree(summary.getSourceIpTopJson());
        JsonNode destinationIpTopJson = objectMapper.readTree(summary.getDestinationIpTopJson());
        JsonNode sourcePortTopJson = objectMapper.readTree(summary.getSourcePortTopJson());
        JsonNode destinationPortTopJson = objectMapper.readTree(summary.getDestinationPortTopJson());
        JsonNode dnsRecordsJson = objectMapper.readTree(summary.getDnsRecordsJson());
        JsonNode httpRecordsJson = objectMapper.readTree(summary.getHttpRecordsJson());
        assertThat(protocolJson.get("TLS").asInt()).isEqualTo(2);
        assertThat(protocolJson.get("DNS").asInt()).isEqualTo(1);
        assertThat(trendJson).hasSize(2);
        assertThat(lengthDistributionJson).isNotEmpty();
        assertThat(sourceIpTopJson.get(0).get("name").asText()).isEqualTo("192.168.1.2");
        assertThat(destinationIpTopJson.get(0).get("name").asText()).isEqualTo("192.168.1.2");
        assertThat(sourcePortTopJson.get(0).has("name")).isTrue();
        assertThat(sourcePortTopJson.get(0).has("value")).isTrue();
        assertThat(destinationPortTopJson.get(0).has("name")).isTrue();
        assertThat(destinationPortTopJson.get(0).has("value")).isTrue();
        assertThat(dnsRecordsJson.get(0).get("queryName").asText()).isEqualTo("example.com");
        assertThat(httpRecordsJson.get(0).get("method").asText()).isEqualTo("GET");
        assertThat(httpRecordsJson.get(1).get("responseCode").asText()).isEqualTo("200");
        assertThat(summary.getStartTimeText()).isEqualTo("1716260000.100000");
        assertThat(summary.getEndTimeText()).isEqualTo("1716260001.300000");
    }

    private List<PacketRecord> stream(Iterable<PacketRecord> records) {
        return java.util.stream.StreamSupport.stream(records.spliterator(), false).toList();
    }

    static class FakeTsharkCommandRunner extends TsharkCommandRunner {
        @Override
        public List<String> readPacketFieldLines(Path capturePath) {
            return List.of(
                    "1\t1716260000.100000\t192.168.1.2\t\t93.184.216.34\t\t51514\t\t443\t\tTLS\t128\tClient Hello",
                    "2\t1716260001.200000\t93.184.216.34\t\t192.168.1.2\t\t443\t\t51514\t\tTLS\t256\tServer Hello",
                    "3\t1716260001.300000\t192.168.1.2\t\t8.8.8.8\t\t\t5353\t\t53\tDNS\t80\tStandard query"
            );
        }

        @Override
        public List<String> readPacketDetailJsonItems(Path capturePath) {
            return List.of("{\"layers\":{\"frame\":{\"frame.number\":\"1\"}}}", "{\"layers\":{}}");
        }

        @Override
        public List<String> readProtocolFeatureLines(Path capturePath) {
            return List.of(
                    "1\texample.com\t93.184.216.34\t\t\t\t",
                    "2\t\t\tGET\texample.com\t/index.html\t",
                    "3\t\t\t\t\t\t200"
            );
        }
    }
}
