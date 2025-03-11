package com.example.demo.websocket;

import com.example.demo.entity.ChatMessageDTO;
import com.example.demo.service.ChatMessageService;
import com.example.demo.service.WebSocketSessionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/chat/{userId}")
@Component
public class DistributedChatWebSocketHandler {

    private static final Map<String, Session> localSessions = new ConcurrentHashMap<>();
    private static final Map<String, String> sessionUserMap = new ConcurrentHashMap<>();
    private static ChatMessageService chatMessageService;
    private static WebSocketSessionService sessionService;
    private static ObjectMapper objectMapper;
    
    @Value("${server.id:server1}")
    private static String serverId;

    @Autowired
    public void setChatMessageService(ChatMessageService service) {
        DistributedChatWebSocketHandler.chatMessageService = service;
    }

    @Autowired
    public void setSessionService(WebSocketSessionService service) {
        DistributedChatWebSocketHandler.sessionService = service;
    }

    @Autowired
    public void setObjectMapper(ObjectMapper mapper) {
        DistributedChatWebSocketHandler.objectMapper = mapper;
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
        localSessions.put(userId, session);
        sessionUserMap.put(session.getId(), userId);
        sessionService.saveSession(userId, serverId);
        sendMessage(session, "连接成功，你的用户ID是: " + userId);
    }

    @OnMessage
    public void onMessage(String message, Session session, @PathParam("userId") String userId) {
        try {
            // 更新心跳
            sessionService.updateHeartbeat(userId);
            
            if ("PING".equals(message)) {
                // 处理心跳消息
                sendMessage(session, "PONG");
                return;
            }

            ChatMessageDTO chatMessage = objectMapper.readValue(message, ChatMessageDTO.class);
            chatMessage.setFromUser(userId);
            chatMessage.setTimestamp(System.currentTimeMillis());
            
            // 发送消息到消息队列
            chatMessageService.sendMessage(chatMessage);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnClose
    public void onClose(Session session, @PathParam("userId") String userId) {
        localSessions.remove(userId);
        sessionUserMap.remove(session.getId());
        sessionService.removeSession(userId);
    }

    @OnError
    public void onError(Session session, Throwable error) {
        error.printStackTrace();
    }

    // 处理从消息队列接收到的消息
    public void handleQueueMessage(ChatMessageDTO message) {
        Session session = localSessions.get(message.getToUser());
        if (session != null && session.isOpen()) {
            try {
                // 只发送给指定的接收者
                String formattedMessage = String.format("来自 %s 的消息: %s", 
                    message.getFromUser(), message.getContent());
                sendMessage(session, formattedMessage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void sendMessage(Session session, String message) {
        try {
            session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 定时清理过期的会话
     */
    @Scheduled(fixedRate = 30000) // 每30秒执行一次
    public void cleanExpiredSessions() {
        localSessions.forEach((userId, session) -> {
            try {
                // 发送心跳检测
                sendMessage(session, "PING");
            } catch (Exception e) {
                // 如果发送失败，说明连接已断开
                localSessions.remove(userId);
                sessionUserMap.remove(session.getId());
                sessionService.removeSession(userId);
            }
        });
    }
} 