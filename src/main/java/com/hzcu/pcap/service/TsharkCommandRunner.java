package com.hzcu.pcap.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 封装 {@code capinfos} 和 {@code tshark} 外部命令调用。
 */
@Component
public class TsharkCommandRunner {

    private static final Duration TIMEOUT = Duration.ofMinutes(10);
    private static final Pattern PACKET_COUNT_PATTERN = Pattern.compile("(?m)^\\s*Number of packets:\\s*(\\d+)\\s*$");
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 使用 {@code capinfos} 读取抓包文件中的数据包总数。
     *
     * @param capturePath 抓包文件路径
     * @return 数据包数量
     */
    public long countPackets(Path capturePath) {
        String output = runCommand(packetCountCommand(capturePath));
        return parsePacketCount(output);
    }

    List<String> packetCountCommand(Path capturePath) {
        return List.of("capinfos", "-M", "-c", capturePath.toString());
    }

    /**
     * 读取抓包文件的核心字段行。
     *
     * @param capturePath 抓包文件路径
     * @return tshark 字段输出行
     */
    public List<String> readPacketFieldLines(Path capturePath) {
        return readPacketFieldLines(capturePath, line -> {
        });
    }

    /**
     * 读取抓包文件的核心字段行，并在每行读取时调用回调。
     *
     * @param capturePath 抓包文件路径
     * @param lineConsumer 单行输出回调
     * @return tshark 字段输出行
     */
    public List<String> readPacketFieldLines(Path capturePath, Consumer<String> lineConsumer) {
        return runLineCommand(packetFieldCommand(capturePath), lineConsumer);
    }

    private List<String> packetFieldCommand(Path capturePath) {
        return List.of(
                "tshark",
                "-n",
                "-r",
                capturePath.toString(),
                "-T",
                "fields",
                "-E",
                "header=n",
                "-E",
                "separator=/t",
                "-E",
                "occurrence=f",
                "-e",
                "frame.number",
                "-e",
                "frame.time_epoch",
                "-e",
                "ip.src",
                "-e",
                "ipv6.src",
                "-e",
                "ip.dst",
                "-e",
                "ipv6.dst",
                "-e",
                "tcp.srcport",
                "-e",
                "udp.srcport",
                "-e",
                "tcp.dstport",
                "-e",
                "udp.dstport",
                "-e",
                "_ws.col.Protocol",
                "-e",
                "frame.len",
                "-e",
                "_ws.col.Info"
        );
    }

    /**
     * 读取所有数据包的 JSON 详情。
     *
     * @param capturePath 抓包文件路径
     * @return 每个数据包对应的 JSON 字符串
     */
    public List<String> readPacketDetailJsonItems(Path capturePath) {
        return readPacketDetailJsonItems(capturePath, 0);
    }

    /**
     * 读取数据包 JSON 详情，可限制最大数据包数量。
     *
     * @param capturePath 抓包文件路径
     * @param packetLimit 读取上限，非正数表示不限制
     * @return 每个数据包对应的 JSON 字符串
     */
    public List<String> readPacketDetailJsonItems(Path capturePath, int packetLimit) {
        List<String> command = new ArrayList<>(List.of(
                "tshark",
                "-n",
                "-r",
                capturePath.toString(),
                "-T",
                "json"
        ));
        if (packetLimit > 0) {
            command.add("-c");
            command.add(String.valueOf(packetLimit));
        }
        String output = runCommand(command);
        try {
            JsonNode root = objectMapper.readTree(output);
            List<String> items = new ArrayList<>();
            if (root != null && root.isArray()) {
                for (JsonNode item : root) {
                    items.add(objectMapper.writeValueAsString(item));
                }
            }
            return items;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to parse tshark JSON output", e);
        }
    }

    /**
     * 读取 DNS 和 HTTP 相关的协议特征字段。
     *
     * @param capturePath 抓包文件路径
     * @return 协议特征字段输出行
     */
    public List<String> readProtocolFeatureLines(Path capturePath) {
        String output = runCommand(List.of(
                "tshark",
                "-n",
                "-r",
                capturePath.toString(),
                "-T",
                "fields",
                "-E",
                "header=n",
                "-E",
                "separator=/t",
                "-E",
                "occurrence=f",
                "-e",
                "frame.number",
                "-e",
                "dns.qry.name",
                "-e",
                "dns.a",
                "-e",
                "http.request.method",
                "-e",
                "http.host",
                "-e",
                "http.request.uri",
                "-e",
                "http.response.code"
        ));
        return output.lines().filter(line -> !line.isBlank()).toList();
    }

    /**
     * 查询当前环境中的 tshark 版本信息。
     *
     * @return tshark 版本文本
     */
    public String versionText() {
        return runCommand(List.of("tshark", "-v"));
    }

    long parsePacketCount(String output) {
        Matcher matcher = PACKET_COUNT_PATTERN.matcher(output == null ? "" : output);
        if (matcher.find()) {
            return Long.parseLong(matcher.group(1));
        }
        throw new IllegalStateException("capinfos did not return a valid packet count: " + output);
    }

    private List<String> runLineCommand(List<String> command, Consumer<String> lineConsumer) {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        try {
            Process process = processBuilder.start();
            // 字段解析需要边读边上报进度，stderr 仍异步读取以避免进程阻塞。
            CompletableFuture<String> stderrFuture = readStreamAsync(process.getErrorStream());
            List<String> lines = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.isBlank()) {
                        lines.add(line);
                        lineConsumer.accept(line);
                    }
                }
            }
            boolean completed = process.waitFor(TIMEOUT.toSeconds(), TimeUnit.SECONDS);
            if (!completed) {
                process.destroyForcibly();
                stderrFuture.cancel(true);
                throw new IllegalStateException(command.get(0) + " command timed out after " + TIMEOUT.toSeconds() + " seconds: " + String.join(" ", command));
            }
            String stderr = stderrFuture.get();
            if (process.exitValue() != 0) {
                throw new IllegalStateException(command.get(0) + " command failed: " + stderr);
            }
            return lines;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to execute " + command.get(0) + " command", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Failed to read " + command.get(0) + " command output", e.getCause());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while executing " + command.get(0) + " command", e);
        }
    }

    private String runCommand(List<String> command) {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        try {
            Process process = processBuilder.start();
            // stdout/stderr 同时读取，避免任一管道填满导致外部命令卡住。
            CompletableFuture<String> stdoutFuture = readStreamAsync(process.getInputStream());
            CompletableFuture<String> stderrFuture = readStreamAsync(process.getErrorStream());
            boolean completed = process.waitFor(TIMEOUT.toSeconds(), TimeUnit.SECONDS);
            if (!completed) {
                process.destroyForcibly();
                stdoutFuture.cancel(true);
                stderrFuture.cancel(true);
                throw new IllegalStateException(command.get(0) + " command timed out after " + TIMEOUT.toSeconds() + " seconds: " + String.join(" ", command));
            }
            String stdout = stdoutFuture.get();
            String stderr = stderrFuture.get();
            if (process.exitValue() != 0) {
                throw new IllegalStateException(command.get(0) + " command failed: " + stderr);
            }
            return stdout;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to execute " + command.get(0) + " command", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Failed to read " + command.get(0) + " command output", e.getCause());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while executing " + command.get(0) + " command", e);
        }
    }

    private CompletableFuture<String> readStreamAsync(InputStream stream) {
        return CompletableFuture.supplyAsync(() -> {
            try (stream) {
                return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }
}
