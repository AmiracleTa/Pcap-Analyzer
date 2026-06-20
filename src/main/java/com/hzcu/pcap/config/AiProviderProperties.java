package com.hzcu.pcap.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

/**
 * AI 提供商配置项。
 *
 * @param enabled 是否启用 AI 报告
 * @param provider AI 提供商名称
 * @param apiKey API Key
 * @param model 模型名称
 * @param baseUrl API 基础地址
 * @param chatCompletionsPath Chat Completions 路径
 * @param timeoutSeconds 请求超时时间，单位秒
 * @param maxOutputTokens 最大输出 token 数
 */
@ConfigurationProperties(prefix = "ai.provider")
public record AiProviderProperties(
        boolean enabled,
        String provider,
        String apiKey,
        String model,
        String baseUrl,
        String chatCompletionsPath,
        int timeoutSeconds,
        int maxOutputTokens
) {
    /**
     * 判断当前配置是否足以发起 AI 请求。
     *
     * @return 启用且存在 API Key 时返回 {@code true}
     */
    public boolean configured() {
        return enabled && apiKey() != null && !apiKey().isBlank();
    }

    /**
     * 获取 API Key，Windows 环境下会额外从系统环境注册表读取。
     *
     * @return API Key；未配置时返回空字符串
     */
    public String apiKey() {
        if (apiKey != null && !apiKey.isBlank()) {
            return apiKey;
        }
        return readWindowsEnvironmentValue("AI_API_KEY");
    }

    /**
     * 拼接完整的 Chat Completions 端点地址。
     *
     * @return 完整 API 端点
     */
    public String endpoint() {
        String normalizedBaseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        String normalizedPath = chatCompletionsPath.startsWith("/") ? chatCompletionsPath : "/" + chatCompletionsPath;
        return normalizedBaseUrl + normalizedPath;
    }

    private static String readWindowsEnvironmentValue(String name) {
        // Windows 图形界面设置的用户环境变量可能不在当前 Java 进程环境中，兜底查注册表。
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
