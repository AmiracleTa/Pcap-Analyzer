package com.hzcu.pcap.controller;

import com.hzcu.pcap.entity.PacketRecord;
import com.hzcu.pcap.repository.PacketRecordRepository;
import com.hzcu.pcap.service.FileStorageService;
import com.hzcu.pcap.service.PacketAnalysisService;
import com.hzcu.pcap.service.SummaryService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class ApiControllerTests {

    @Test
    void healthEndpointReturnsOk() throws Exception {
        MockMvc mockMvc = standaloneSetup(new HealthController()).build();

        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"status\":\"ok\"}"));
    }

    @Test
    void fileUploadRejectsUnsupportedExtensions() throws Exception {
        FileStorageService fileStorageService = mock(FileStorageService.class);
        MockMvc mockMvc = standaloneSetup(new FileController(fileStorageService)).build();
        MockMultipartFile file = new MockMultipartFile("file", "readme.txt", "text/plain", "hello".getBytes());

        mockMvc.perform(multipart("/api/files").file(file))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(fileStorageService);
    }

    @Test
    void fileDeleteReturnsDeletedFlag() throws Exception {
        FileStorageService fileStorageService = mock(FileStorageService.class);
        MockMvc mockMvc = standaloneSetup(new FileController(fileStorageService)).build();

        mockMvc.perform(delete("/api/files/7"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"deleted\":true}"));
    }

    @Test
    void analysisEndpointsReturnPacketsAndExports() throws Exception {
        PacketAnalysisService packetAnalysisService = mock(PacketAnalysisService.class);
        SummaryService summaryService = mock(SummaryService.class);
        PacketRecordRepository packetRecordRepository = mock(PacketRecordRepository.class);
        when(packetAnalysisService.analyze(3L)).thenReturn(Map.of(
                "status", "ok",
                "packetCount", 0
        ));
        when(summaryService.getSummary(3L)).thenReturn(Map.ofEntries(
                Map.entry("protocols", Map.of()),
                Map.entry("trafficTrend", java.util.List.of()),
                Map.entry("ips", Map.of()),
                Map.entry("ports", Map.of()),
                Map.entry("lengthDistribution", java.util.List.of()),
                Map.entry("sourceIpTop", java.util.List.of()),
                Map.entry("destinationIpTop", java.util.List.of()),
                Map.entry("sourcePortTop", java.util.List.of()),
                Map.entry("destinationPortTop", java.util.List.of()),
                Map.entry("dnsRecords", java.util.List.of()),
                Map.entry("httpRecords", java.util.List.of()),
                Map.entry("startTimeText", ""),
                Map.entry("endTimeText", "")
        ));
        when(packetRecordRepository.findByFileId(3L)).thenReturn(java.util.List.of());
        MockMvc mockMvc = standaloneSetup(
                new AnalysisController(packetAnalysisService, summaryService, packetRecordRepository)
        ).build();

        mockMvc.perform(post("/api/files/3/analyze"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"))
                .andExpect(jsonPath("$.packetCount").value(0));
        mockMvc.perform(get("/api/files/3/packets"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
        mockMvc.perform(get("/api/files/3/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.protocols").isMap())
                .andExpect(jsonPath("$.trafficTrend").isArray())
                .andExpect(jsonPath("$.lengthDistribution").exists())
                .andExpect(jsonPath("$.sourceIpTop").exists())
                .andExpect(jsonPath("$.destinationIpTop").exists())
                .andExpect(jsonPath("$.sourcePortTop").exists())
                .andExpect(jsonPath("$.destinationPortTop").exists())
                .andExpect(jsonPath("$.dnsRecords").exists())
                .andExpect(jsonPath("$.httpRecords").exists());
        mockMvc.perform(get("/api/files/3/export/csv"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"packets-3.csv\""))
                .andExpect(content().string(containsString("序号,时间戳,源地址,目的地址,源端口,目的端口,协议,长度,摘要")));
        mockMvc.perform(get("/api/files/3/export/json"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"packets\":[]}"));
    }

    @Test
    void csvExportEscapesExcelSensitiveValuesAndNewlines() throws Exception {
        PacketAnalysisService packetAnalysisService = mock(PacketAnalysisService.class);
        SummaryService summaryService = mock(SummaryService.class);
        PacketRecordRepository packetRecordRepository = mock(PacketRecordRepository.class);
        PacketRecord packet = new PacketRecord();
        packet.setPacketNo(1L);
        packet.setTimestampText("1779350202.670520900");
        packet.setSourceIp("192.168.222.190");
        packet.setDestinationIp("104.16.7.34");
        packet.setSourcePort(43489);
        packet.setDestinationPort(443);
        packet.setProtocol("TCP");
        packet.setLength(54);
        packet.setInfo("43489 → 443 [ACK]\nSeq=1");
        when(packetRecordRepository.findByFileId(3L)).thenReturn(List.of(packet));
        MockMvc mockMvc = standaloneSetup(
                new AnalysisController(packetAnalysisService, summaryService, packetRecordRepository)
        ).build();

        String csv = mockMvc.perform(get("/api/files/3/export/csv"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"packets-3.csv\""))
                .andExpect(content().string(containsString("=\"1779350202.670520900\"")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertFalse(csv.contains("[ACK]\nSeq=1"));
        assertTrue(csv.contains("43489 → 443 [ACK] Seq=1"));
    }

    @Test
    void apiExceptionHandlerReturnsJsonErrorResponse() throws Exception {
        MockMvc mockMvc = standaloneSetup(new FailingController())
                .setControllerAdvice(new ApiExceptionHandler())
                .build();

        mockMvc.perform(get("/boom"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("boom")))
                .andExpect(jsonPath("$.path").isNotEmpty())
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @RestController
    static class FailingController {
        @GetMapping("/boom")
        Map<String, Object> boom() {
            throw new IllegalStateException("boom");
        }
    }
}
