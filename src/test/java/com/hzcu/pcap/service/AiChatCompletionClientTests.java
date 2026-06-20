package com.hzcu.pcap.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hzcu.pcap.config.AiProviderProperties;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class AiChatCompletionClientTests {

    @Test
    void normalizeModelOutputReportsTruncatedJsonClearly() throws Exception {
        AiChatCompletionClient client = new AiChatCompletionClient(
                new AiProviderProperties(true, "deepseek", "test-key", "deepseek-v4-flash", "https://api.deepseek.com", "/chat/completions", 60, 8192),
                new ObjectMapper()
        );
        Method normalize = AiChatCompletionClient.class.getDeclaredMethod("normalizeModelOutput", String.class, String.class);
        normalize.setAccessible(true);

        Throwable thrown = catchThrowable(() -> normalize.invoke(client, "{\"riskLevel\":\"low\",\"summary\":\"未结束", "length"));

        assertThat(thrown)
                .isInstanceOf(java.lang.reflect.InvocationTargetException.class)
                .cause()
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("AI 返回内容超出长度限制，JSON 被截断。请提高 AI_MAX_OUTPUT_TOKENS 或重新生成报告。");
    }
}
