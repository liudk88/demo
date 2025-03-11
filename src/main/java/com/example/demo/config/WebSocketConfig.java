package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * WebSocket的配置类
 *
 * @author liudk
 * create on 3/7/25
 */
@Configuration
public class WebSocketConfig {
    /** ServerEndpointExporter是servlet 3.0 + 环境下用于发布 WebSocket 端点的类，它会自动注册带有@ServerEndpoint注解的 WebSocket 端点 */
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
} 