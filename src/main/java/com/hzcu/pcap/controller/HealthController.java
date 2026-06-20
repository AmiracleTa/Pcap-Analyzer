package com.hzcu.pcap.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 提供应用健康检查接口。
 */
@RestController
@RequestMapping("/api")
public class HealthController {

    /**
     * 返回后端服务的健康状态。
     *
     * @return 包含状态字段的响应对象
     */
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }
}
