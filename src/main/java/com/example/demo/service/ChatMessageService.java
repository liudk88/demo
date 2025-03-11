package com.example.demo.service;

import com.example.demo.entity.ChatMessage;
import com.example.demo.entity.ChatMessageDTO;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.example.demo.config.RabbitConfig.CHAT_EXCHANGE;

import java.util.Set;

/**
 * 消息发送服务
 *
 * @author liudk
 * create on 3/7/25
 */
@Service
public class ChatMessageService {
    
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    @Autowired
    private WebSocketSessionService sessionService;

    public void saveMessage(ChatMessage message) {
        // 模拟数据库存储
        System.out.println("保存消息到数据库: " + message.getContent());
    }

    public void getHistoryMessages() {
        // 模拟查询历史消息
        System.out.println("查询历史消息");
    }

    /**
     * 发送消息到指定用户
     */
    public void sendMessage(ChatMessageDTO message) {
        // 获取接收者所在的所有服务器ID
        Set<String> targetServerIds = sessionService.getServerIdsByUserId(message.getToUser());
        
        if (targetServerIds.isEmpty()) {
            System.out.println("接收用户不在线: " + message.getToUser());
            return;
        }
        
        // 只发送到接收者所在的服务器
        for (String serverId : targetServerIds) {
            rabbitTemplate.convertAndSend(CHAT_EXCHANGE, "chat." + serverId, message);
            System.out.println("消息已发送到服务器" + serverId + ": " + message);
        }
    }
} 