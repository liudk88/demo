package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 创建Redis会话管理服务
 *
 * @author liudk
 * create on 3/7/25
 */
@Service
public class WebSocketSessionService {
    
    private static final String USER_SERVER_KEY = "websocket:user:server:";
    private static final int EXPIRE_TIME = 30; // 30秒过期

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 保存用户会话信息，使用心跳机制
     */
    public void saveSession(String userId, String serverId) {
        String key = USER_SERVER_KEY + userId;
        redisTemplate.opsForValue().set(key, serverId, EXPIRE_TIME, TimeUnit.SECONDS);
    }

    /**
     * 更新用户会话心跳
     */
    public void updateHeartbeat(String userId) {
        String key = USER_SERVER_KEY + userId;
        redisTemplate.expire(key, EXPIRE_TIME, TimeUnit.SECONDS);
    }

    /**
     * 获取指定用户所在的服务器ID
     */
    public Set<String> getServerIdsByUserId(String userId) {
        Set<String> serverIds = new HashSet<>();
        String key = USER_SERVER_KEY + userId;
        String serverId = (String) redisTemplate.opsForValue().get(key);
        if (serverId != null) {
            serverIds.add(serverId);
        }
        return serverIds;
    }

    /**
     * 移除用户会话
     */
    public void removeSession(String userId) {
        redisTemplate.delete(USER_SERVER_KEY + userId);
    }
} 