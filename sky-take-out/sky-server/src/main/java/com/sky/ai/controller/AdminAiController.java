package com.sky.ai.controller;

import com.sky.ai.dto.AiChatRequest;
import com.sky.ai.dto.AiChatResponse;
import com.sky.ai.service.AdminAiAssistantService;
import com.sky.result.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/ai")
public class AdminAiController {

    private final AdminAiAssistantService adminAiAssistantService;

    public AdminAiController(AdminAiAssistantService adminAiAssistantService) {
        this.adminAiAssistantService = adminAiAssistantService;
    }

    @PostMapping("/chat")
    public Result<AiChatResponse> chat(@RequestBody AiChatRequest request) {
        if (request == null || request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            return Result.error("message 不能为空");
        }
        return Result.success(adminAiAssistantService.chat(request.getMessage(), request.getThreadId()));
    }
}
