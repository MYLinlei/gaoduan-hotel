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
    private static final Pattern PHONE_PATTERN = Pattern.compile("(1\\d{10})");

    private static final String[] DISH_FIELDS = {"name", "categoryName", "price", "status"};
    private static final String[] SETMEAL_FIELDS = {"name", "categoryName", "price", "status"};
    private static final String[] CATEGORY_FIELDS = {"name", "type", "sort"};
    private static final String[] EMPLOYEE_FIELDS = {"name", "username", "status"};
    private static final String[] ORDER_FIELDS = {"number", "userName", "phone", "status", "orderType", "amount", "orderTime"};
    private static final String[] VOUCHER_FIELDS = {"name", "scopeType", "channelType", "actualValue", "payValue", "availableStock", "status"};
    private static final String[] USER_FIELDS = {"id", "name", "phone", "createTime"};
    private static final String[] SALES_FIELDS = {"name", "number"};

    private final ReactAgent adminAssistantAgent;
    private final AdminAssistantTools adminAssistantTools;

    public AdminAiAssistantService(ReactAgent adminAssistantAgent, AdminAssistantTools adminAssistantTools) {
        this.adminAssistantAgent = adminAssistantAgent;
        this.adminAssistantTools = adminAssistantTools;
    }

    public AiChatResponse chat(String message, String threadId) {
        String normalizedThreadId = StringUtils.hasText(threadId) ? threadId.trim() : UUID.randomUUID().toString();
        String strictAnswer = tryStrictReply(message);
        if (StringUtils.hasText(strictAnswer)) {
            return new AiChatResponse(normalizedThreadId, strictAnswer);
        }
        String answer = tryRemoteReply(message, normalizedThreadId);
        if (!StringUtils.hasText(answer)) {
            answer = fallbackReply(message);
        }
        return new AiChatResponse(normalizedThreadId, answer);
    }

    private String tryStrictReply(String message) {
        String text = message == null ? "" : message.trim();
        if (!StringUtils.hasText(text)) {
            return null;
        }

        if (isVoucherOverviewQuestion(text)) {
            return replyForStrictVoucherOverview(text);
        }
        return null;
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
            return "你好，我是酒店点餐后台 AI 助手。你可以让我查询订单、优惠券、顾客、菜品互动、菜品、套餐、分类、员工和营业状态。";
        }

        if (containsAny(text, "营业状态", "店铺状态", "营业中", "打烊")) {
            return replyForShopStatus(text);
        }
        if (containsAny(text, "订单", "堂食单", "外卖单")) {
            return replyForOrderQuery(text);
        }
        if (containsAny(text, "优惠券", "领券", "发券", "券")) {
            return replyForVoucherQuery(text);
        }
        if (containsAny(text, "评价", "种草", "笔记", "点赞", "收藏")) {
            return replyForInteractionQuery(text);
        }
        if (containsAny(text, "顾客", "用户", "手机号", "会员")) {
            return replyForUserQuery(text);
        }
        if (containsAny(text, "卖得最好", "销量", "热销", "卖得最好的", "销量最高")) {
            return replyForSalesQuery(text);
        }
        if (containsAny(text, "菜品", "商品")) {
            return replyForDishQuery(text);
        }
        if (containsAny(text, "套餐")) {
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
            String keyword = extractPhone(text);
            if (!StringUtils.hasText(keyword)) {
                keyword = extractLooseKeyword(text, "员工", "店员", "查询", "列表", "详情");
            }
            return summarizePagedResult("员工", adminAssistantTools.queryEmployees(1, 10, keyword), EMPLOYEE_FIELDS);
        }

        return "当前本地兜底模式已经支持订单、优惠券、顾客、菜品互动、菜品、套餐、分类、员工和营业状态查询。你可以直接说“查询今天订单摘要”、“查看优惠券 3 的详情”或“查询手机号 138xxxx 的消费画像”。";
    }

    private String replyForSalesQuery(String text) {
        if (containsAny(text, "套餐")) {
            return summarizeListResult("热销套餐", adminAssistantTools.queryTopSellingSetmeals(10, null, null), SALES_FIELDS);
        }
        return summarizeListResult("热销菜品", adminAssistantTools.queryTopSellingDishes(10, null, null), SALES_FIELDS);
    }

    private String replyForShopStatus(String text) {
        if (containsAny(text, "切换", "设置", "改成", "改为")) {
            if (containsAny(text, "营业")) {
                return summarizeSimpleResult(adminAssistantTools.setShopStatus(1), "店铺已切换为营业中。");
            }
            if (containsAny(text, "打烊", "停业")) {
                return summarizeSimpleResult(adminAssistantTools.setShopStatus(0), "店铺已切换为打烊中。");
            }
        }
        return summarizeSimpleResult(adminAssistantTools.getShopStatus(), "已获取当前店铺营业状态。");
    }

    private String replyForOrderQuery(String text) {
        Long orderId = extractFirstLong(text);
        String orderNumber = extractOrderNumber(text);
        if (orderId != null || StringUtils.hasText(orderNumber)) {
            return summarizeEntityDetail("订单", adminAssistantTools.getOrderDetail(orderId, orderNumber));
        }
        if (containsAny(text, "摘要", "统计", "今天", "今日", "经营")) {
            return summarizeEntityDetail("订单摘要", adminAssistantTools.countOrderSummary(null, null));
        }
        Integer status = extractOrderStatus(text);
        Integer orderType = extractOrderType(text);
        String phone = extractPhone(text);
        return summarizePagedResult("订单", adminAssistantTools.queryOrders(1, 10, null, phone, status, orderType, null, null), ORDER_FIELDS);
    }

    private String replyForVoucherQuery(String text) {
        Long voucherId = extractFirstLong(text);
        if (containsAny(text, "上架", "下架") && voucherId != null) {
            Integer status = containsAny(text, "上架") ? 1 : 0;
            return summarizeSimpleResult(adminAssistantTools.updateVoucherStatus(voucherId, status), status == 1 ? "优惠券已上架。" : "优惠券已下架。");
        }
        if (voucherId != null && containsAny(text, "领取记录", "领券记录", "谁领取", "谁领了")) {
            return summarizePagedResult("领券记录", adminAssistantTools.queryVoucherReceiveRecords(voucherId, 1, 10, null, null),
                    new String[]{"id", "phone", "nickname", "status", "receiveTime"});
        }
        if (voucherId != null) {
            return summarizeEntityDetail("优惠券", adminAssistantTools.getVoucherDetail(voucherId));
        }
        Integer status = extractEnableStatus(text);
        String channelType = extractChannelType(text);
        return summarizePagedResult("优惠券", adminAssistantTools.queryVouchers(1, 10, null, status, channelType), VOUCHER_FIELDS);
    }

    private String replyForStrictVoucherOverview(String text) {
        Integer status = containsAny(text, "上架", "可领", "可领取", "当前") ? 1 : extractEnableStatus(text);
        String channelType = extractChannelType(text);
        JSONObject payload = parseObject(adminAssistantTools.summarizeVoucherOverview(status, channelType, null));
        String errorMessage = extractErrorMessage(payload);
        if (errorMessage != null) {
            return errorMessage;
        }

        JSONObject data = payload.getJSONObject("data");
        if (data == null) {
            return "暂时无法获取优惠券概览。";
        }

        long voucherCount = data.getLongValue("voucherCount");
        int totalAvailableStock = data.getIntValue("totalAvailableStock");
        String channelText = data.getString("channelText");
        String statusText = data.getString("statusText");

        if (containsAny(text, "库存", "剩多少", "还剩多少", "剩余", "还剩")) {
            return String.format("当前%s%s优惠券共 %d 款，剩余库存合计 %d 张。", channelText, statusText, voucherCount, totalAvailableStock);
        }
        return String.format("当前%s%s优惠券共 %d 款，剩余库存合计 %d 张。", channelText, statusText, voucherCount, totalAvailableStock);
    }

    private String replyForInteractionQuery(String text) {
        Long dishId = extractFirstLong(text);
        if (dishId == null) {
            return "请提供菜品 id，例如“查看菜品 12 的评价”或“查询菜品 8 的互动摘要”。";
        }
        if (containsAny(text, "评价", "评论")) {
            return summarizePagedResult("菜品评价", adminAssistantTools.queryDishComments(dishId, 1, 10),
                    new String[]{"userName", "score", "content", "createTime"});
        }
        if (containsAny(text, "种草", "笔记")) {
            return summarizeListResult("菜品笔记", adminAssistantTools.queryDishNotes(dishId),
                    new String[]{"title", "userName", "liked", "createTime"});
        }
        return summarizeEntityDetail("菜品互动摘要", adminAssistantTools.queryDishInteractionSummary(dishId));
    }

    private String replyForUserQuery(String text) {
        Long userId = extractFirstLong(text);
        String phone = extractPhone(text);
        if (containsAny(text, "榜单", "排行", "高消费", "消费最高")) {
            return summarizeListResult("顾客消费榜", adminAssistantTools.topUsersByConsumption(10, null, null),
                    new String[]{"userId", "phone", "nickname", "completedOrders", "totalAmount", "lastOrderTime"});
        }
        if (userId != null || StringUtils.hasText(phone)) {
            return summarizeEntityDetail("顾客消费画像", adminAssistantTools.getUserConsumptionProfile(userId, phone));
        }
        return summarizePagedResult("顾客", adminAssistantTools.queryUsers(1, 10, phone, null), USER_FIELDS);
    }

    private String replyForDishQuery(String text) {
        if (containsAny(text, "卖得最好", "销量", "热销", "销量最高")) {
            return summarizeListResult("热销菜品", adminAssistantTools.queryTopSellingDishes(10, null, null), SALES_FIELDS);
        }
        Long id = extractFirstLong(text);
        if (id != null && containsAny(text, "详情", "详细", "查看", "查询")) {
            return summarizeEntityDetail("菜品", adminAssistantTools.getDishById(id));
        }
        String categoryName = adminAssistantTools.findMentionedDishCategoryName(text);
        String keyword = extractLooseKeyword(text, "菜品", "商品", "列表", "详情", "查询");
        Integer status = extractEnableStatus(text);
        return summarizePagedResult("菜品", adminAssistantTools.searchDishes(1, 10, keyword, categoryName, status), DISH_FIELDS);
    }

    private String replyForSetmealQuery(String text) {
        if (containsAny(text, "卖得最好", "销量", "热销", "销量最高")) {
            return summarizeListResult("热销套餐", adminAssistantTools.queryTopSellingSetmeals(10, null, null), SALES_FIELDS);
        }
        Long id = extractFirstLong(text);
        if (id != null && containsAny(text, "详情", "详细", "查看", "查询")) {
            return summarizeEntityDetail("套餐", adminAssistantTools.getSetmealById(id));
        }
        String categoryName = adminAssistantTools.findMentionedSetmealCategoryName(text);
        String keyword = extractLooseKeyword(text, "套餐", "列表", "详情", "查询");
        Integer status = extractEnableStatus(text);
        return summarizePagedResult("套餐", adminAssistantTools.querySetmeals(1, 10, keyword, null, status), SETMEAL_FIELDS);
    }

    private String summarizeSimpleResult(String json, String successTip) {
        JSONObject payload = parseObject(json);
        String errorMessage = extractErrorMessage(payload);
        if (errorMessage != null) {
            return errorMessage;
        }
        JSONObject data = payload.getJSONObject("data");
        if (data == null) {
            return successTip;
        }
        return successTip + "\n" + JSON.toJSONString(data, true);
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

        Object data = payload.get("data");
        if (data == null) {
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
        return switch (field) {
            case "status" -> formatStatusValue(String.valueOf(value));
            case "type" -> "1".equals(String.valueOf(value)) ? "菜品分类" : "套餐分类";
            case "orderType" -> "1".equals(String.valueOf(value)) ? "外卖" : "堂食";
            case "channelType", "scopeLabel" -> formatChannelType(String.valueOf(value));
            default -> String.valueOf(value);
        };
    }

    private String formatStatusValue(String value) {
        return switch (value) {
            case "0" -> "禁用/下架";
            case "1" -> "启用/上架/已领取";
            case "2" -> "待接单/已锁定";
            case "3" -> "已接单/已使用";
            case "4" -> "配送中/已过期";
            case "5" -> "已完成";
            case "6" -> "已取消";
            case "7" -> "待制作";
            case "8" -> "制作中";
            case "9" -> "已上桌";
            default -> value;
        };
    }

    private String formatChannelType(String value) {
        return switch (value) {
            case "UNIVERSAL" -> "全部场景";
            case "DINE_IN" -> "堂食";
            case "DELIVERY" -> "外卖";
            default -> value;
        };
    }

    private String displayFieldName(String field) {
        return switch (field) {
            case "id" -> "ID";
            case "name" -> "名称";
            case "categoryName" -> "分类";
            case "price" -> "价格";
            case "status" -> "状态";
            case "type" -> "类型";
            case "sort" -> "排序";
            case "username" -> "账号";
            case "number" -> "订单号";
            case "userName" -> "用户";
            case "phone" -> "手机号";
            case "orderType" -> "订单类型";
            case "amount" -> "金额";
            case "orderTime" -> "下单时间";
            case "scopeType" -> "作用范围";
            case "channelType", "scopeLabel" -> "适用渠道";
            case "actualValue", "discountAmount" -> "优惠金额";
            case "payValue", "thresholdAmount" -> "使用门槛";
            case "availableStock" -> "可用库存";
            case "nickname" -> "昵称";
            case "createTime" -> "创建时间";
            case "receiveTime" -> "领取时间";
            case "useTime" -> "使用时间";
            case "lastOrderTime" -> "最近下单时间";
            case "completedOrders" -> "已完成订单数";
            case "totalAmount" -> "累计消费";
            case "score" -> "评分";
            case "content" -> "内容";
            case "title" -> "标题";
            case "liked" -> "点赞数";
            default -> field;
        };
    }

    private Integer extractOrderStatus(String text) {
        if (containsAny(text, "待接单")) {
            return 2;
        }
        if (containsAny(text, "已接单")) {
            return 3;
        }
        if (containsAny(text, "配送中")) {
            return 4;
        }
        if (containsAny(text, "已完成", "完成订单")) {
            return 5;
        }
        if (containsAny(text, "已取消", "取消订单")) {
            return 6;
        }
        if (containsAny(text, "待制作")) {
            return 7;
        }
        if (containsAny(text, "制作中")) {
            return 8;
        }
        if (containsAny(text, "已上桌")) {
            return 9;
        }
        return null;
    }

    private Integer extractEnableStatus(String text) {
        if (containsAny(text, "启用", "在售", "上架")) {
            return 1;
        }
        if (containsAny(text, "禁用", "停售", "下架")) {
            return 0;
        }
        return null;
    }

    private Integer extractOrderType(String text) {
        if (containsAny(text, "堂食")) {
            return 2;
        }
        if (containsAny(text, "外卖", "配送")) {
            return 1;
        }
        return null;
    }

    private String extractChannelType(String text) {
        if (containsAny(text, "堂食")) {
            return "DINE_IN";
        }
        if (containsAny(text, "外卖", "配送")) {
            return "DELIVERY";
        }
        if (containsAny(text, "全部", "通用")) {
            return "UNIVERSAL";
        }
        return null;
    }

    private boolean isVoucherOverviewQuestion(String text) {
        if (!containsAny(text, "优惠券", "券")) {
            return false;
        }
        return containsAny(text, "几款", "多少款", "库存", "剩多少", "还剩多少", "剩余", "多少张");
    }

    private String extractOrderNumber(String text) {
        Matcher matcher = Pattern.compile("(?:订单号|订单)\\s*[:：#]?\\s*([A-Za-z0-9\\-]+)").matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private String extractPhone(String text) {
        Matcher matcher = PHONE_PATTERN.matcher(text);
        return matcher.find() ? matcher.group(1) : null;
    }

    private String extractLooseKeyword(String text, String... noiseWords) {
        String result = text;
        for (String noiseWord : noiseWords) {
            result = result.replace(noiseWord, " ");
        }
        result = result.replaceAll("\\d+", " ");
        result = result.replaceAll("[，。！？、,.?;:：()（）\\[\\]【】\\-]", " ");
        result = result.replaceAll("\\s+", " ").trim();
        return StringUtils.hasText(result) ? result : null;
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
