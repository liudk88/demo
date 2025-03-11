package com.example.demo.entity;

import lombok.Data;

import java.time.LocalDateTime;
/**
 * 消息实体类
 *
 * @author liudk
 * create on 3/7/25
 */
@Data
public class ChatMessage {
    private Long id;
    private String content;
    private String sender;
    private LocalDateTime createTime;

    // getter和setter方法省略
} 