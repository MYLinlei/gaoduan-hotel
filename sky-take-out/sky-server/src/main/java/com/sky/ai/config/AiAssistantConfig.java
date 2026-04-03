package com.sky.ai.config;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.sky.ai.tools.AdminAssistantTools;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiAssistantConfig {

    @Bean
    public ReactAgent adminAssistantAgent(@Qualifier("dashScopeChatModel") ChatModel dashScopeChatModel,
                                          AdminAssistantTools adminAssistantTools) {
        return ReactAgent.builder()
                .name("sky_take_out_admin_assistant")
                .model(dashScopeChatModel)
                .description("Admin assistant for the Sky Take-Out merchant backend")
                .instruction("""
                        You are the merchant backend assistant for the Sky Take-Out system.
                        Help the merchant query backend data and perform a small set of safe operations.

                        Rules:
                        - Prefer using tools to get real backend data. Do not invent facts.
                        - For dish, setmeal, category, employee, or shop status questions, call the appropriate tool first.
                        - For shop status changes, check the current status first and only change it when the user clearly asks for it.
                        - Tool results are structured data. Summarize them into concise Chinese answers for the user.
                        - If the user asks for unsupported create, delete, or update operations, clearly explain the limitation.
                        """)
                .methodTools(adminAssistantTools)
                .saver(new MemorySaver())
                .build();
    }
}
