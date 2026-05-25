package com.hzcu.pcap.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

@ConfigurationProperties(prefix = "ai.provider")
public record AiProviderProperties(
        boolean enabled,
        String provider,
        String apiKey,
        String model,
        String baseUrl,
        String chatCompletionsPath,
        int timeoutSeconds
) {
    public boolean configured() {
        return enabled && apiKey() != null && !apiKey().isBlank();
    }

    public String apiKey() {
        if (apiKey != null && !apiKey.isBlank()) {
            return apiKey;
        }
        return readWindowsEnvironmentValue("AI_API_KEY");
    }

    public String endpoint() {
        String normalizedBaseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        String normalizedPath = chatCompletionsPath.startsWith("/") ? chatCompletionsPath : "/" + chatCompletionsPath;
        return normalizedBaseUrl + normalizedPath;
    }

    private static String readWindowsEnvironmentValue(String name) {
        String osName = System.getProperty("os.name", "");
        if (!osName.toLowerCase().contains("windows")) {
            return "";
        }
        String userValue = queryRegistryEnvironment("HKCU\\Environment", name);
        if (!userValue.isBlank()) {
            return userValue;
        }
        return queryRegistryEnvironment("HKLM\\SYSTEM\\CurrentControlSet\\Control\\Session Manager\\Environment", name);
    }

    private static String queryRegistryEnvironment(String keyPath, String name) {
        try {
            Process process = new ProcessBuilder("reg", "query", keyPath, "/v", name)
                    .redirectErrorStream(true)
                    .start();
            String value = readRegistryValue(process, name);
            if (!process.waitFor(2, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                return "";
            }
            return process.exitValue() == 0 ? value : "";
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return "";
        }
    }

    private static String readRegistryValue(Process process, String name) throws IOException {
        try (BufferedReader reader = process.inputReader(Charset.defaultCharset())) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.startsWith(name + " ")) {
                    String[] parts = trimmed.split("\\s+", 3);
                    return parts.length == 3 ? parts[2].trim() : "";
                }
            }
        }
        return "";
    }
}
