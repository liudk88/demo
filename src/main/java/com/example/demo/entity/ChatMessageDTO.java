package com.example.demo.entity;

import lombok.Data;
import java.io.Serializable;

@Data
public class ChatMessageDTO implements Serializable {
    private String fromUser;
    private String toUser;
    private String content;
    private Long timestamp;
    private String messageType; // PRIVATE, GROUP
} 