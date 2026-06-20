package com.hzcu.pcap.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hzcu.pcap.config.AiProviderProperties;
import com.hzcu.pcap.dto.AiSecurityFinding;
import com.hzcu.pcap.dto.AiSecurityModelOutput;
import com.hzcu.pcap.dto.SecurityEvidence;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 调用兼容 Chat Completions 的 AI 服务生成安全报告。
 */
@Service
public class AiChatCompletionClient {

    private final AiProviderProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    /**
     * 创建 AI 聊天补全客户端。
     *
     * @param properties AI 提供商配置
     * @param objectMapper JSON 解析组件
     */
    public AiChatCompletionClient(AiProviderProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newHttpClient();
    }

    /**
     * 调用 AI 服务并规范化模型返回的安全报告。
     *
     * @param contextJson AI 安全分析上下文
     * @return 规范化后的安全报告模型输出
     */
    public AiSecurityModelOutput createReport(String contextJson) {
        try {
            String requestBody = objectMapper.writeValueAsString(requestBody(contextJson));
            HttpRequest request = HttpRequest.newBuilder(URI.create(properties.endpoint()))
                    .timeout(Duration.ofSeconds(properties.timeoutSeconds()))
                    .header("Authorization", "Bearer " + properties.apiKey())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("AI request failed: " + response.body());
            }
            String content = messageContent(response.body());
            return normalizeModelOutput(content);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to parse AI security report", e);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to call AI security report service", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("AI security report request was interrupted", e);
        }
    }

    private Map<String, Object> requestBody(String contextJson) {
        // 提示词要求模型只基于输入证据输出 JSON，避免前端再处理 Markdown 或自由文本。
        return Map.of(
                "model", properties.model(),
                "messages", List.of(
                        Map.of(
                                "role", "system",
                                "content", "你是网络安全助手。根据用户提供的数据包表格、整体流量趋势和协议占比生成安全报告。数据包表格每行只包含 packetNo、time、sourceIp、destinationIp、sourcePort、destinationPort、protocol、length。不要编造数据；不要把可疑现象直接断言为攻击或木马；输出紧凑中文 JSON，不要 Markdown。JSON 字段必须包含 riskLevel、riskScore、summary、findings、normalObservations、recommendations。findings 是主要风险点，最多 6 项；每项 explanation 写成类似报告段落，必须引用输入中的时间、IP、端口、协议、长度、代表包序号、整体流量趋势或协议占比。findings[].evidence 必须来自输入证据，每项只包含 packetNo、time、sourceIp、destinationIp、sourcePort、destinationPort、protocol、length。normalObservations 写相对正常的现象。recommendations 写处理建议。"
                        ),
                        Map.of(
                                "role", "user",
                                "content", contextJson
                        )
                ),
                "response_format", Map.of("type", "json_object"),
                "thinking", Map.of("type", "disabled"),
                "temperature", 0.1,
                "max_tokens", 2200,
                "stream", false
        );
    }

    private String messageContent(String body) throws JsonProcessingException {
        JsonNode content = objectMapper.readTree(body)
                .path("choices")
                .path(0)
                .path("message")
                .path("content");
        if (content.isMissingNode() || content.isNull() || content.asText().isBlank()) {
            throw new IllegalStateException("AI response does not contain message content");
        }
        return content.asText();
    }

    private AiSecurityModelOutput normalizeModelOutput(String content) throws JsonProcessingException {
        JsonNode root = objectMapper.readTree(content);
        return new AiSecurityModelOutput(
                textValue(root.get("riskLevel"), "unknown"),
                intValue(root.get("riskScore")),
                textValue(root.get("summary"), ""),
                findings(root.get("findings")),
                stringList(root.get("normalObservations")),
                stringList(root.get("recommendations"))
        );
    }

    private List<AiSecurityFinding> findings(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return List.of();
        }
        List<AiSecurityFinding> findings = new ArrayList<>();
        if (node.isArray()) {
            for (JsonNode item : node) {
                AiSecurityFinding finding = finding(item);
                if (finding != null) {
                    findings.add(finding);
                }
            }
            return findings;
        }
        AiSecurityFinding finding = finding(node);
        return finding == null ? List.of() : List.of(finding);
    }

    private AiSecurityFinding finding(JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }
        return new AiSecurityFinding(
                textValue(node.get("category"), "other"),
                textValue(node.get("severity"), "unknown"),
                textValue(node.get("title"), "未命名风险"),
                textValue(node.get("explanation"), ""),
                evidenceList(node.get("evidence")),
                textValue(node.get("recommendation"), "")
        );
    }

    private List<SecurityEvidence> evidenceList(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return List.of();
        }
        List<SecurityEvidence> evidence = new ArrayList<>();
        if (node.isArray()) {
            for (JsonNode item : node) {
                evidence.add(evidence(item));
            }
            return evidence;
        }
        return List.of(evidence(node));
    }

    private SecurityEvidence evidence(JsonNode node) {
        if (node != null && node.isObject()) {
            return new SecurityEvidence(
                    longValue(node.get("packetNo")),
                    textValue(node.get("time"), ""),
                    textValue(node.get("sourceIp"), ""),
                    textValue(node.get("destinationIp"), ""),
                    integerValue(node.get("sourcePort")),
                    integerValue(node.get("destinationPort")),
                    textValue(node.get("protocol"), ""),
                    integerValue(node.get("length"))
            );
        }
        return new SecurityEvidence(null, textValue(node, ""), "", "", null, null, "", null);
    }

    private List<String> stringList(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return List.of();
        }
        if (node.isArray()) {
            List<String> values = new ArrayList<>();
            for (JsonNode item : node) {
                String value = textValue(item, "");
                if (!value.isBlank()) {
                    values.add(value);
                }
            }
            return values;
        }
        String value = textValue(node, "");
        return value.isBlank() ? List.of() : List.of(value);
    }

    private String textValue(JsonNode node, String fallback) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return fallback;
        }
        return node.isTextual() ? node.asText() : node.toString();
    }

    private int intValue(JsonNode node) {
        Integer value = integerValue(node);
        return value == null ? 0 : Math.max(0, Math.min(100, value));
    }

    private Integer integerValue(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        if (node.isNumber()) {
            return node.asInt();
        }
        if (node.isTextual()) {
            try {
                return Integer.parseInt(node.asText().trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private Long longValue(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        if (node.isNumber()) {
            return node.asLong();
        }
        if (node.isTextual()) {
            try {
                return Long.parseLong(node.asText().trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }
}
