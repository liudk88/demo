package com.example.demo.listener;

import com.example.demo.entity.ChatMessageDTO;
import com.example.demo.websocket.DistributedChatWebSocketHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.example.demo.config.RabbitConfig.CHAT_QUEUE;

@Component
public class ChatMessageListener {

    @Autowired
    private DistributedChatWebSocketHandler webSocketHandler;

    @RabbitListener(queues = CHAT_QUEUE)
    public void receiveMessage(ChatMessageDTO message) {
        webSocketHandler.handleQueueMessage(message);
    }
} 