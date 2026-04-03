package com.sky.ai.dto;

import lombok.Data;

@Data
public class AiChatRequest {

    private String message;

    private String threadId;
}
