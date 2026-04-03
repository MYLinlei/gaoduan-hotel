package com.sky.ai.service;

import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sky.ai.dto.AiChatResponse;
import com.sky.ai.tools.AdminAssistantTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class AdminAiAssistantService {

    private static final Pattern NUMBER_PATTERN = Pattern.compile("(\\d+)");

    private static final String[] DISH_FIELDS = {"name", "categoryName", "price", "status"};
    private static final String[] SETMEAL_FIELDS = {"name", "categoryName", "price", "status"};
    private static final String[] CATEGORY_FIELDS = {"name", "type", "sort"};
    private static final String[] EMPLOYEE_FIELDS = {"name", "username", "status"};

    private static final String[] QUERY_NOISE_WORDS = {
            "帮我", "帮忙", "请帮我", "查询", "查一下", "查一查", "查看", "看一下", "看一看",
            "搜索", "找一下", "列出", "列一下", "显示", "告诉我", "当前", "店铺", "门店",
            "后台", "系统", "里面", "中的", "的", "一下", "是否", "是不是", "还有", "现在",
            "分页", "列表", "清单", "详情", "信息", "数据", "有哪些", "有什么", "哪些", "所有",
            "在售", "停售", "启用", "禁用", "上架", "下架"
    };

    private static final String[] DISH_NOISE_WORDS = {
            "菜品", "菜", "商品", "店铺菜品", "菜品列表", "菜品分页", "菜品详情"
    };

    private static final String[] SETMEAL_NOISE_WORDS = {
            "套餐", "套饭", "套餐列表", "套餐分页", "套餐详情"
    };

    private final ReactAgent adminAssistantAgent;
    private final AdminAssistantTools adminAssistantTools;

    public AdminAiAssistantService(ReactAgent adminAssistantAgent, AdminAssistantTools adminAssistantTools) {
        this.adminAssistantAgent = adminAssistantAgent;
        this.adminAssistantTools = adminAssistantTools;
    }

    public AiChatResponse chat(String message, String threadId) {
        String normalizedThreadId = StringUtils.hasText(threadId) ? threadId.trim() : UUID.randomUUID().toString();
        String answer = tryRemoteReply(message, normalizedThreadId);
        if (!StringUtils.hasText(answer)) {
            answer = fallbackReply(message);
        }
        return new AiChatResponse(normalizedThreadId, answer);
    }

    private String tryRemoteReply(String message, String threadId) {
        RunnableConfig config = RunnableConfig.builder()
                .threadId(threadId)
                .build();
        try {
            return CompletableFuture.supplyAsync(() -> {
                        try {
                            AssistantMessage response = adminAssistantAgent.call(message, config);
                            return response != null ? response.getText() : null;
                        } catch (Throwable ex) {
                            log.warn("Remote DashScope agent call failed, fallback to local assistant", ex);
                            return null;
                        }
                    })
                    .orTimeout(20, TimeUnit.SECONDS)
                    .exceptionally(ex -> {
                        log.warn("Remote DashScope agent call timed out or failed, fallback to local assistant", ex);
                        return null;
                    })
                    .join();
        } catch (Throwable ex) {
            log.warn("Remote DashScope agent call could not be completed, fallback to local assistant", ex);
            return null;
        }
    }

    private String fallbackReply(String message) {
        String text = message == null ? "" : message.trim();
        if (!StringUtils.hasText(text)) {
            return "请输入要查询的内容。";
        }

        if (containsAnyIgnoreCase(text, "你好", "您好", "hello", "hi")) {
            return "你好，我是苍穹外卖后台 AI 助手。你可以问我菜品、套餐、分类、员工信息，或者查看和切换店铺营业状态。";
        }

        if (containsAny(text, "营业状态", "店铺状态", "营业中", "打烊")) {
            return replyForShopStatus(text);
        }

        if (containsAny(text, "菜品", "店铺菜品", "商品")) {
            return replyForDishQuery(text);
        }

        if (containsAny(text, "套餐", "套饭")) {
            return replyForSetmealQuery(text);
        }

        if (containsAny(text, "分类", "类目")) {
            Integer type = null;
            if (containsAny(text, "菜品分类")) {
                type = 1;
            } else if (containsAny(text, "套餐分类")) {
                type = 2;
            }
            return summarizeListResult("分类", adminAssistantTools.listCategories(type, 1, 10, null), CATEGORY_FIELDS);
        }

        if (containsAny(text, "员工", "店员")) {
            String keyword = extractKeyword(text, null, EMPLOYEE_FIELDS, new String[]{"员工", "店员"});
            return summarizePagedResult("员工", adminAssistantTools.queryEmployees(1, 10, keyword), EMPLOYEE_FIELDS);
        }

        return "AI 模型当前不可用，我已经切换到本地兜底模式。你可以尝试这样问我：查询在售菜品、查看套餐列表、查询员工分页、查看店铺营业状态、把店铺切换为打烊。";
    }

    private String replyForShopStatus(String text) {
        if (containsAny(text, "切换", "设置", "改成", "改为")) {
            if (containsAny(text, "营业中", "营业")) {
                return summarizeShopStatus(adminAssistantTools.setShopStatus(1), true);
            }
            if (containsAny(text, "打烊", "停业")) {
                return summarizeShopStatus(adminAssistantTools.setShopStatus(0), true);
            }
        }
        return summarizeShopStatus(adminAssistantTools.getShopStatus(), false);
    }

    private String replyForDishQuery(String text) {
        Long id = extractFirstLong(text);
        if (id != null && containsAny(text, "详情", "详细", "查看", "查询")) {
            return summarizeEntityDetail("菜品", adminAssistantTools.getDishById(id));
        }

        String categoryName = adminAssistantTools.findMentionedDishCategoryName(text);
        String keyword = extractKeyword(text, categoryName, DISH_FIELDS, DISH_NOISE_WORDS);
        Integer status = extractStatus(text);
        return summarizePagedResult("菜品", adminAssistantTools.searchDishes(1, 10, keyword, categoryName, status), DISH_FIELDS);
    }

    private String replyForSetmealQuery(String text) {
        Long id = extractFirstLong(text);
        if (id != null && containsAny(text, "详情", "详细", "查看", "查询")) {
            return summarizeEntityDetail("套餐", adminAssistantTools.getSetmealById(id));
        }

        String categoryName = adminAssistantTools.findMentionedSetmealCategoryName(text);
        String keyword = extractKeyword(text, categoryName, SETMEAL_FIELDS, SETMEAL_NOISE_WORDS);
        Integer status = extractStatus(text);
        return summarizePagedResult("套餐", adminAssistantTools.querySetmeals(1, 10, keyword, null, status), SETMEAL_FIELDS);
    }

    private String summarizeShopStatus(String json, boolean changed) {
        JSONObject payload = parseObject(json);
        String errorMessage = extractErrorMessage(payload);
        if (errorMessage != null) {
            return errorMessage;
        }

        JSONObject data = payload.getJSONObject("data");
        if (data == null) {
            return changed ? "店铺状态已切换完成。" : "暂时无法获取店铺状态。";
        }

        String statusText = data.getString("statusText");
        if (!StringUtils.hasText(statusText)) {
            Integer status = data.getInteger("status");
            statusText = status != null && status == 1 ? "营业中" : "打烊中";
        }
        return changed ? "已将店铺状态切换为" + statusText + "。" : "当前店铺状态为" + statusText + "。";
    }

    private String summarizePagedResult(String label, String json, String[] fields) {
        JSONObject payload = parseObject(json);
        String errorMessage = extractErrorMessage(payload);
        if (errorMessage != null) {
            return errorMessage;
        }

        JSONObject data = payload.getJSONObject("data");
        if (data == null) {
            return "暂时无法获取" + label + "数据。";
        }

        JSONArray records = data.getJSONArray("records");
        Long total = data.getLong("total");
        if (records == null || records.isEmpty()) {
            return "当前没有查询到符合条件的" + label + "数据。";
        }

        List<String> lines = new ArrayList<>();
        lines.add(label + "查询成功，共 " + (total == null ? records.size() : total) + " 条，当前展示前 " + records.size() + " 条：");
        for (int i = 0; i < records.size(); i++) {
            JSONObject item = records.getJSONObject(i);
            lines.add((i + 1) + ". " + formatFields(item, fields));
        }
        return String.join("\n", lines);
    }

    private String summarizeListResult(String label, String json, String[] fields) {
        JSONObject payload = parseObject(json);
        String errorMessage = extractErrorMessage(payload);
        if (errorMessage != null) {
            return errorMessage;
        }

        Object data = payload.get("data");
        JSONArray items;
        if (data instanceof JSONArray jsonArray) {
            items = jsonArray;
        } else if (data instanceof JSONObject jsonObject) {
            items = jsonObject.getJSONArray("records");
        } else {
            items = null;
        }

        if (items == null || items.isEmpty()) {
            return "当前没有查询到符合条件的" + label + "数据。";
        }

        List<String> lines = new ArrayList<>();
        lines.add(label + "查询成功，当前展示前 " + items.size() + " 条：");
        for (int i = 0; i < items.size(); i++) {
            JSONObject item = items.getJSONObject(i);
            lines.add((i + 1) + ". " + formatFields(item, fields));
        }
        return String.join("\n", lines);
    }

    private String summarizeEntityDetail(String label, String json) {
        JSONObject payload = parseObject(json);
        String errorMessage = extractErrorMessage(payload);
        if (errorMessage != null) {
            return errorMessage;
        }

        JSONObject data = payload.getJSONObject("data");
        if (data == null || data.isEmpty()) {
            return "没有查询到对应的" + label + "详情。";
        }
        return label + "详情：\n" + JSON.toJSONString(data, true);
    }

    private JSONObject parseObject(String json) {
        if (!StringUtils.hasText(json)) {
            return new JSONObject();
        }
        try {
            return JSON.parseObject(json);
        } catch (Exception ex) {
            log.warn("Failed to parse tool result: {}", json, ex);
            JSONObject fallback = new JSONObject();
            fallback.put("code", 0);
            fallback.put("msg", "工具返回结果无法解析");
            fallback.put("raw", json);
            return fallback;
        }
    }

    private String extractErrorMessage(JSONObject payload) {
        Integer code = payload.getInteger("code");
        if (code != null && code == 0) {
            String message = payload.getString("msg");
            return StringUtils.hasText(message) ? message : "请求执行失败。";
        }
        return null;
    }

    private String formatFields(JSONObject item, String[] fields) {
        List<String> parts = new ArrayList<>();
        for (String field : fields) {
            Object value = item.get(field);
            if (value == null || !StringUtils.hasText(String.valueOf(value))) {
                continue;
            }
            parts.add(displayFieldName(field) + "=" + formatFieldValue(field, value));
        }
        return parts.isEmpty() ? item.toJSONString() : String.join(", ", parts);
    }

    private String formatFieldValue(String field, Object value) {
        if ("status".equals(field)) {
            return "1".equals(String.valueOf(value)) ? "启用/在售" : "禁用/停售";
        }
        if ("type".equals(field)) {
            return "1".equals(String.valueOf(value)) ? "菜品分类" : "套餐分类";
        }
        return String.valueOf(value);
    }

    private String displayFieldName(String field) {
        return switch (field) {
            case "name" -> "名称";
            case "categoryName" -> "分类";
            case "price" -> "价格";
            case "status" -> "状态";
            case "type" -> "类型";
            case "sort" -> "排序";
            case "username" -> "账号";
            default -> field;
        };
    }

    private Integer extractStatus(String text) {
        if (containsAny(text, "在售", "启售", "上架", "营业")) {
            return 1;
        }
        if (containsAny(text, "停售", "下架", "禁用")) {
            return 0;
        }
        return null;
    }

    private String extractKeyword(String text, String matchedCategoryName, String[] fields, String[] entityNoiseWords) {
        String candidate = text;
        if (StringUtils.hasText(matchedCategoryName)) {
            candidate = candidate.replace(matchedCategoryName, " ");
        }

        for (String word : QUERY_NOISE_WORDS) {
            candidate = candidate.replace(word, " ");
        }
        for (String word : entityNoiseWords) {
            candidate = candidate.replace(word, " ");
        }
        for (String field : fields) {
            String displayName = displayFieldName(field);
            candidate = candidate.replace(field, " ").replace(displayName, " ");
        }

        candidate = candidate.replaceAll("\\d+", " ");
        candidate = candidate.replaceAll("[，。！？、,.?;；:：()（）\\[\\]【】/\\\\-]", " ");
        candidate = candidate.replaceAll("\\s+", " ").trim();

        if (!StringUtils.hasText(candidate) || candidate.length() < 2) {
            return null;
        }
        return candidate;
    }

    private Long extractFirstLong(String text) {
        Matcher matcher = NUMBER_PATTERN.matcher(text);
        if (matcher.find()) {
            return Long.valueOf(matcher.group(1));
        }
        return null;
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsAnyIgnoreCase(String text, String... keywords) {
        String normalized = text.toLowerCase();
        for (String keyword : keywords) {
            if (normalized.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}
