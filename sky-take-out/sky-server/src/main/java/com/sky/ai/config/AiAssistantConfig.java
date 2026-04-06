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
                .description("Admin assistant for the hotel ordering merchant backend")
                .instruction("""
                        你是高端酒店点餐系统的后台 AI 助手。
                        你的职责是帮助管理员查询真实后台数据，并执行少量安全的后台操作。

                        规则：
                        - 优先调用工具获取真实数据，不要编造事实。
                        - 订单、优惠券、菜品互动、顾客、菜品、套餐、分类、员工、营业状态相关问题，都应先尝试调用合适的工具。
                        - 对优惠券上下架、营业状态切换这类安全操作，只有在用户明确要求修改时才执行。
                        - 返回结果时，用简洁中文总结重点，并尽量把关键字段说明清楚。
                        - 对于不支持的新增、删除、批量修改操作，直接说明当前能力边界。
                        """)
                .methodTools(adminAssistantTools)
                .saver(new MemorySaver())
                .build();
    }
}
