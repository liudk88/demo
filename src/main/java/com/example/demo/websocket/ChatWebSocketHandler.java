package com.example.demo.websocket;

import com.example.demo.entity.ChatMessage;
import com.example.demo.service.ChatMessageService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/websocket")
@Component
public class ChatWebSocketHandler {
    // 用来记录sessionId和该session进行绑定
    private static Map<String, Session> clients = new ConcurrentHashMap<>();
    
    // 注意：由于WebSocket的特殊性，不能直接使用@Autowired，需要手动注入
    private static ChatMessageService chatMessageService;
    
    @Autowired
    public void setChatMessageService(ChatMessageService service) {
        ChatWebSocketHandler.chatMessageService = service;
    }

    @OnOpen
    public void onOpen(Session session) {
        clients.put(session.getId(), session);
        sendMessage(session, "欢迎连接到WebSocket服务器（springboot版本）！");
        System.out.println("新的客户端已连接，ID: " + session.getId());
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        System.out.println("收到客户端消息: " + message);
        
        // 保存消息到数据库
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setContent(message);
        chatMessage.setSender(session.getId());
        chatMessageService.saveMessage(chatMessage);

        // 广播消息给其他客户端
        clients.forEach((id, client) -> {
            if (!id.equals(session.getId()) && client.isOpen()) {
                try {
                    client.getBasicRemote().sendText("其他用户说: " + message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        // 发送确认消息给发送者
        sendMessage(session, "服务器已收到你的消息: " + message);
    }

    @OnClose
    public void onClose(Session session) {
        clients.remove(session.getId());
        System.out.println("客户端已断开连接，ID: " + session.getId());
    }

    @OnError
    public void onError(Session session, Throwable error) {
        System.out.println("发生错误");
        error.printStackTrace();
    }

    private void sendMessage(Session session, String message) {
        try {
            session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
} 