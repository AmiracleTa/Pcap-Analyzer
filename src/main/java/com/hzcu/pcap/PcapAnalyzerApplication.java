package com.hzcu.pcap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * PCAP 分析平台的 Spring Boot 启动类。
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class PcapAnalyzerApplication {

    /**
     * 启动 Spring Boot 应用。
     *
     * @param args 命令行启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(PcapAnalyzerApplication.class, args);
    }
}
