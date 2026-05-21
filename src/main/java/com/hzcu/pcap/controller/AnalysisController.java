package com.hzcu.pcap.controller;

import com.hzcu.pcap.entity.PacketRecord;
import com.hzcu.pcap.repository.PacketRecordRepository;
import com.hzcu.pcap.service.PacketAnalysisService;
import com.hzcu.pcap.service.SummaryService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/files/{id}")
public class AnalysisController {

    private final PacketAnalysisService packetAnalysisService;
    private final SummaryService summaryService;
    private final PacketRecordRepository packetRecordRepository;

    public AnalysisController(PacketAnalysisService packetAnalysisService,
                              SummaryService summaryService,
                              PacketRecordRepository packetRecordRepository) {
        this.packetAnalysisService = packetAnalysisService;
        this.summaryService = summaryService;
        this.packetRecordRepository = packetRecordRepository;
    }

    @PostMapping("/analyze")
    public Map<String, Object> analyze(@PathVariable Long id) {
        return packetAnalysisService.analyze(id);
    }

    @GetMapping("/packets")
    public List<PacketRecord> packets(@PathVariable Long id) {
        return packetRecordRepository.findByFileId(id);
    }

    @GetMapping("/summary")
    public Map<String, Object> summary(@PathVariable Long id) {
        return summaryService.getSummary(id);
    }

    @GetMapping(value = "/export/csv", produces = "text/csv")
    public String exportCsv(@PathVariable Long id) {
        List<PacketRecord> packets = packetRecordRepository.findByFileId(id);
        StringBuilder csv = new StringBuilder();
        csv.append("packetNo,timestamp,sourceIp,destinationIp,sourcePort,destinationPort,protocol,length,info\n");
        for (PacketRecord packet : packets) {
            csv.append(csvValue(packet.getPacketNo()))
                    .append(',')
                    .append(csvValue(packet.getTimestampText()))
                    .append(',')
                    .append(csvValue(packet.getSourceIp()))
                    .append(',')
                    .append(csvValue(packet.getDestinationIp()))
                    .append(',')
                    .append(csvValue(packet.getSourcePort()))
                    .append(',')
                    .append(csvValue(packet.getDestinationPort()))
                    .append(',')
                    .append(csvValue(packet.getProtocol()))
                    .append(',')
                    .append(csvValue(packet.getLength()))
                    .append(',')
                    .append(csvValue(packet.getInfo()))
                    .append('\n');
        }
        return csv.toString();
    }

    @GetMapping(value = "/export/json", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> exportJson(@PathVariable Long id) {
        return Map.of("packets", packetRecordRepository.findByFileId(id));
    }

    private String csvValue(Object value) {
        String text = value == null ? "" : value.toString();
        return "\"" + text.replace("\"", "\"\"") + "\"";
    }
}
