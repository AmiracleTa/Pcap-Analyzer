package com.hzcu.pcap.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Component
public class TsharkCommandRunner {

    private static final Duration TIMEOUT = Duration.ofSeconds(60);
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<String> readPacketFieldLines(Path capturePath) {
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
        ));
        return output.lines().filter(line -> !line.isBlank()).toList();
    }

    public List<String> readPacketDetailJsonItems(Path capturePath) {
        String output = runCommand(List.of(
                "tshark",
                "-n",
                "-r",
                capturePath.toString(),
                "-T",
                "json",
                "-x"
        ));
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

    public String versionText() {
        return runCommand(List.of("tshark", "-v"));
    }

    private String runCommand(List<String> command) {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        try {
            Process process = processBuilder.start();
            CompletableFuture<String> stdoutFuture = readStreamAsync(process.getInputStream());
            CompletableFuture<String> stderrFuture = readStreamAsync(process.getErrorStream());
            boolean completed = process.waitFor(TIMEOUT.toSeconds(), TimeUnit.SECONDS);
            if (!completed) {
                process.destroyForcibly();
                stdoutFuture.cancel(true);
                stderrFuture.cancel(true);
                throw new IllegalStateException("tshark command timed out after 60 seconds: " + String.join(" ", command));
            }
            String stdout = stdoutFuture.get();
            String stderr = stderrFuture.get();
            if (process.exitValue() != 0) {
                throw new IllegalStateException("tshark command failed: " + stderr);
            }
            return stdout;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to execute tshark command", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Failed to read tshark command output", e.getCause());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while executing tshark command", e);
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
