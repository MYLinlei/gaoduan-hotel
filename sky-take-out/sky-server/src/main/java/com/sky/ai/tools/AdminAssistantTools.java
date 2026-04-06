package com.sky.ai.tools;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.dto.GoodsSalesDTO;
import com.sky.dto.HotelHighVoucherPageQueryDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Category;
import com.sky.entity.Dish;
import com.sky.entity.Employee;
import com.sky.entity.HotelHighVoucher;
import com.sky.entity.Orders;
import com.sky.entity.Setmeal;
import com.sky.entity.User;
import com.sky.mapper.DishCommentMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.DishNoteMapper;
import com.sky.mapper.HotelHighVoucherMapper;
import com.sky.mapper.HotelHighVoucherOrderMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrdersMapper;
import com.sky.mapper.UserMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import com.sky.service.DishService;
import com.sky.service.EmployeeService;
import com.sky.service.HotelHighVoucherService;
import com.sky.service.OrderService;
import com.sky.service.SetmealService;
import com.sky.vo.DishCommentVO;
import com.sky.vo.DishNoteVO;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class AdminAssistantTools {

    private static final int DISH_CATEGORY_TYPE = 1;
    private static final int SETMEAL_CATEGORY_TYPE = 2;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final DishService dishService;
    private final SetmealService setmealService;
    private final CategoryService categoryService;
    private final EmployeeService employeeService;
    private final OrderService orderService;
    private final HotelHighVoucherService hotelHighVoucherService;
    private final DishMapper dishMapper;
    private final DishNoteMapper dishNoteMapper;
    private final DishCommentMapper dishCommentMapper;
    private final OrderDetailMapper orderDetailMapper;
    private final OrdersMapper ordersMapper;
    private final HotelHighVoucherMapper hotelHighVoucherMapper;
    private final HotelHighVoucherOrderMapper hotelHighVoucherOrderMapper;
    private final UserMapper userMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    public AdminAssistantTools(DishService dishService,
                               SetmealService setmealService,
                               CategoryService categoryService,
                               EmployeeService employeeService,
                               OrderService orderService,
                               HotelHighVoucherService hotelHighVoucherService,
                               DishMapper dishMapper,
                               DishNoteMapper dishNoteMapper,
                               DishCommentMapper dishCommentMapper,
                               OrderDetailMapper orderDetailMapper,
                               OrdersMapper ordersMapper,
                               HotelHighVoucherMapper hotelHighVoucherMapper,
                               HotelHighVoucherOrderMapper hotelHighVoucherOrderMapper,
                               UserMapper userMapper,
                               RedisTemplate<String, Object> redisTemplate) {
        this.dishService = dishService;
        this.setmealService = setmealService;
        this.categoryService = categoryService;
        this.employeeService = employeeService;
        this.orderService = orderService;
        this.hotelHighVoucherService = hotelHighVoucherService;
        this.dishMapper = dishMapper;
        this.dishNoteMapper = dishNoteMapper;
        this.dishCommentMapper = dishCommentMapper;
        this.orderDetailMapper = orderDetailMapper;
        this.ordersMapper = ordersMapper;
        this.hotelHighVoucherMapper = hotelHighVoucherMapper;
        this.hotelHighVoucherOrderMapper = hotelHighVoucherOrderMapper;
        this.userMapper = userMapper;
        this.redisTemplate = redisTemplate;
    }

    @Tool(description = "查询后台菜品分页列表，支持页码、每页数量、菜品名称模糊搜索、分类 id 和售卖状态。")
    public String queryDishes(
            @ToolParam(description = "页码，默认 1") Integer page,
            @ToolParam(description = "每页数量，默认 10") Integer pageSize,
            @ToolParam(description = "菜品名称关键字，可选") String name,
            @ToolParam(description = "菜品分类 id，可选") Long categoryId,
            @ToolParam(description = "菜品状态，可选。1 表示起售，0 表示停售") Integer status) {
        return JSON.toJSONString(Result.success(
                buildDishPage(defaultInt(page, 1), defaultInt(pageSize, 10), trimToNull(name), categoryId, status)
        ));
    }

    @Tool(description = "按自然语言过滤条件查询后台菜品列表，支持菜品名称、分类名称和售卖状态。")
    public String searchDishes(
            @ToolParam(description = "页码，默认 1") Integer page,
            @ToolParam(description = "每页数量，默认 10") Integer pageSize,
            @ToolParam(description = "菜品名称关键字，可选") String name,
            @ToolParam(description = "菜品分类名称，可选") String categoryName,
            @ToolParam(description = "菜品状态，可选。1 表示起售，0 表示停售") Integer status) {
        Long categoryId = resolveCategoryId(categoryName, DISH_CATEGORY_TYPE);
        if (StringUtils.hasText(categoryName) && categoryId == null) {
            return JSON.toJSONString(Result.error("未找到菜品分类：" + categoryName.trim()));
        }
        return JSON.toJSONString(Result.success(
                buildDishPage(defaultInt(page, 1), defaultInt(pageSize, 10), trimToNull(name), categoryId, status)
        ));
    }

    @Tool(description = "按菜品 id 查询菜品详情。")
    public String getDishById(@ToolParam(description = "菜品 id") Long id) {
        return JSON.toJSONString(Result.success(dishService.getByIdWithFlavor(id)));
    }

    @Tool(description = "按分类 id 查询当前起售中的菜品列表。")
    public String listDishesByCategory(@ToolParam(description = "菜品分类 id") Long categoryId) {
        return JSON.toJSONString(Result.success(dishService.getDishsList(categoryId)));
    }

    @Tool(description = "查询后台套餐分页列表，支持页码、每页数量、套餐名称模糊搜索、分类 id 和售卖状态。")
    public String querySetmeals(
            @ToolParam(description = "页码，默认 1") Integer page,
            @ToolParam(description = "每页数量，默认 10") Integer pageSize,
            @ToolParam(description = "套餐名称关键字，可选") String name,
            @ToolParam(description = "套餐分类 id，可选") Integer categoryId,
            @ToolParam(description = "套餐状态，可选。1 表示起售，0 表示停售") Integer status) {
        SetmealPageQueryDTO dto = new SetmealPageQueryDTO();
        dto.setPage(defaultInt(page, 1));
        dto.setPageSize(defaultInt(pageSize, 10));
        dto.setName(trimToNull(name));
        dto.setCategoryId(categoryId);
        dto.setStatus(status);
        return JSON.toJSONString(Result.success(setmealService.pageQuery(dto)));
    }

    @Tool(description = "按套餐 id 查询套餐详情。")
    public String getSetmealById(@ToolParam(description = "套餐 id") Long id) {
        return JSON.toJSONString(Result.success(setmealService.getByIdWithDish(id)));
    }

    @Tool(description = "查询分类列表，支持按分类类型、页码、每页数量和分类名称过滤。type=1 表示菜品分类，type=2 表示套餐分类。")
    public String listCategories(
            @ToolParam(description = "分类类型，可选。1 表示菜品分类，2 表示套餐分类") Integer type,
            @ToolParam(description = "页码，可选") Integer page,
            @ToolParam(description = "每页数量，可选") Integer pageSize,
            @ToolParam(description = "分类名称关键字，可选") String name) {
        if (page != null || pageSize != null || StringUtils.hasText(name)) {
            CategoryPageQueryDTO dto = new CategoryPageQueryDTO();
            dto.setPage(defaultInt(page, 1));
            dto.setPageSize(defaultInt(pageSize, 10));
            dto.setName(trimToNull(name));
            dto.setType(type);
            return JSON.toJSONString(Result.success(categoryService.pageQuery(dto)));
        }
        return JSON.toJSONString(Result.success(categoryService.list(type)));
    }

    @Tool(description = "查询员工分页列表，支持页码、每页数量和员工姓名模糊搜索。")
    public String queryEmployees(
            @ToolParam(description = "页码，默认 1") Integer page,
            @ToolParam(description = "每页数量，默认 10") Integer pageSize,
            @ToolParam(description = "员工姓名关键字，可选") String name) {
        EmployeePageQueryDTO dto = new EmployeePageQueryDTO();
        dto.setPage(defaultInt(page, 1));
        dto.setPageSize(defaultInt(pageSize, 10));
        dto.setName(trimToNull(name));
        return JSON.toJSONString(Result.success(employeeService.pageQuery(dto)));
    }

    @Tool(description = "按员工 id 查询员工详情。")
    public String getEmployeeById(@ToolParam(description = "员工 id") Long id) {
        Employee employee = employeeService.getById(id);
        return JSON.toJSONString(Result.success(employee));
    }

    @Tool(description = "获取当前店铺营业状态。")
    public String getShopStatus() {
        Integer status = (Integer) redisTemplate.opsForValue().get("SHOP_STATUS");
        Map<String, Object> payload = new HashMap<>();
        payload.put("status", status);
        payload.put("statusText", status != null && status == 1 ? "营业中" : "打烊中");
        return JSON.toJSONString(Result.success(payload));
    }

    @Tool(description = "更新店铺营业状态。1 表示营业中，0 表示打烊。")
    public String setShopStatus(@ToolParam(description = "店铺营业状态。1 表示营业中，0 表示打烊") Integer status) {
        redisTemplate.opsForValue().set("SHOP_STATUS", status);
        Map<String, Object> payload = new HashMap<>();
        payload.put("status", status);
        payload.put("statusText", status != null && status == 1 ? "营业中" : "打烊中");
        return JSON.toJSONString(Result.success(payload));
    }

    @Tool(description = "查询订单分页列表，支持订单号、手机号、订单状态、订单类型和时间范围。订单类型 1=外卖，2=堂食。")
    public String queryOrders(
            @ToolParam(description = "页码，默认 1") Integer page,
            @ToolParam(description = "每页数量，默认 10") Integer pageSize,
            @ToolParam(description = "订单号关键字，可选") String number,
            @ToolParam(description = "用户手机号关键字，可选") String phone,
            @ToolParam(description = "订单状态，可选") Integer status,
            @ToolParam(description = "订单类型，可选。1=外卖，2=堂食") Integer orderType,
            @ToolParam(description = "开始时间，可选，格式 yyyy-MM-dd 或 yyyy-MM-dd HH:mm:ss") String beginTime,
            @ToolParam(description = "结束时间，可选，格式 yyyy-MM-dd 或 yyyy-MM-dd HH:mm:ss") String endTime) {
        OrdersPageQueryDTO dto = new OrdersPageQueryDTO();
        dto.setPage(defaultInt(page, 1));
        dto.setPageSize(defaultInt(pageSize, 10));
        dto.setNumber(trimToNull(number));
        dto.setPhone(trimToNull(phone));
        dto.setStatus(status);
        dto.setOrderType(orderType);
        dto.setBeginTime(parseDateTime(beginTime, true));
        dto.setEndTime(parseDateTime(endTime, false));
        return JSON.toJSONString(Result.success(orderService.conditionSearch(dto)));
    }

    @Tool(description = "查询订单详情。优先使用订单 id，也可以传订单号。")
    public String getOrderDetail(
            @ToolParam(description = "订单 id，可选") Long orderId,
            @ToolParam(description = "订单号，可选") String orderNumber) {
        Long resolvedId = orderId;
        if (resolvedId == null && StringUtils.hasText(orderNumber)) {
            Orders orders = ordersMapper.getByNumber(orderNumber.trim());
            if (orders != null) {
                resolvedId = orders.getId();
            }
        }
        if (resolvedId == null) {
            return JSON.toJSONString(Result.error("请提供有效的订单 id 或订单号"));
        }
        return JSON.toJSONString(Result.success(orderService.details(resolvedId)));
    }

    @Tool(description = "统计订单经营摘要。默认统计今天，也可以传开始时间和结束时间。")
    public String countOrderSummary(
            @ToolParam(description = "开始时间，可选，默认今天开始，格式 yyyy-MM-dd 或 yyyy-MM-dd HH:mm:ss") String beginTime,
            @ToolParam(description = "结束时间，可选，默认明天开始，格式 yyyy-MM-dd 或 yyyy-MM-dd HH:mm:ss") String endTime) {
        LocalDateTime begin = parseDateTime(beginTime, true);
        LocalDateTime end = parseDateTime(endTime, false);
        if (begin == null && end == null) {
            begin = LocalDate.now().atStartOfDay();
            end = begin.plusDays(1);
        } else if (begin != null && end == null) {
            end = begin.plusDays(1);
        }

        Map<String, Object> summary = new HashMap<>();
        summary.put("beginTime", begin);
        summary.put("endTime", end);
        summary.put("totalOrders", countOrders(null, null, begin, end, null, null, null));
        summary.put("deliveryOrders", countOrders(null, Orders.DELIVERY_ORDER, begin, end, null, null, null));
        summary.put("dineInOrders", countOrders(null, Orders.DINE_IN_ORDER, begin, end, null, null, null));
        summary.put("toBeConfirmedOrders", countOrders(Orders.TO_BE_CONFIRMED, null, begin, end, null, null, null)
                + countOrders(Orders.DINE_IN_TO_BE_PREPARED, null, begin, end, null, null, null));
        summary.put("confirmedOrders", countOrders(Orders.CONFIRMED, null, begin, end, null, null, null)
                + countOrders(Orders.DINE_IN_IN_PROGRESS, null, begin, end, null, null, null));
        summary.put("deliveryInProgressOrders", countOrders(Orders.DELIVERY_IN_PROGRESS, null, begin, end, null, null, null)
                + countOrders(Orders.DINE_IN_SERVED, null, begin, end, null, null, null));
        summary.put("completedOrders", countOrders(Orders.COMPLETED, null, begin, end, null, null, null));
        summary.put("cancelledOrders", countOrders(Orders.CANCELLED, null, begin, end, null, null, null));
        summary.put("revenue", defaultAmount(ordersMapper.sumAmountByCondition(Orders.COMPLETED, begin, end)));
        return JSON.toJSONString(Result.success(summary));
    }

    @Tool(description = "统计热销菜品排行。默认统计全部历史，也可以传时间范围。")
    public String queryTopSellingDishes(
            @ToolParam(description = "返回数量，默认 10") Integer limit,
            @ToolParam(description = "开始时间，可选，格式 yyyy-MM-dd 或 yyyy-MM-dd HH:mm:ss") String beginTime,
            @ToolParam(description = "结束时间，可选，格式 yyyy-MM-dd 或 yyyy-MM-dd HH:mm:ss") String endTime) {
        LocalDateTime begin = parseDateTime(beginTime, true);
        LocalDateTime end = parseDateTime(endTime, false);
        List<GoodsSalesDTO> ranking = orderDetailMapper.getTopSellingDishes(begin, end, defaultInt(limit, 10));
        return JSON.toJSONString(Result.success(ranking));
    }

    @Tool(description = "统计热销套餐排行。默认统计全部历史，也可以传时间范围。")
    public String queryTopSellingSetmeals(
            @ToolParam(description = "返回数量，默认 10") Integer limit,
            @ToolParam(description = "开始时间，可选，格式 yyyy-MM-dd 或 yyyy-MM-dd HH:mm:ss") String beginTime,
            @ToolParam(description = "结束时间，可选，格式 yyyy-MM-dd 或 yyyy-MM-dd HH:mm:ss") String endTime) {
        LocalDateTime begin = parseDateTime(beginTime, true);
        LocalDateTime end = parseDateTime(endTime, false);
        List<GoodsSalesDTO> ranking = orderDetailMapper.getTopSellingSetmeals(begin, end, defaultInt(limit, 10));
        return JSON.toJSONString(Result.success(ranking));
    }

    @Tool(description = "查询优惠券分页列表，支持名称、状态和适用渠道过滤。")
    public String queryVouchers(
            @ToolParam(description = "页码，默认 1") Integer page,
            @ToolParam(description = "每页数量，默认 10") Integer pageSize,
            @ToolParam(description = "优惠券名称关键字，可选") String name,
            @ToolParam(description = "优惠券状态，可选。1=上架，0=下架") Integer status,
            @ToolParam(description = "适用渠道，可选。UNIVERSAL/DINE_IN/DELIVERY") String channelType) {
        HotelHighVoucherPageQueryDTO dto = new HotelHighVoucherPageQueryDTO();
        dto.setPage(defaultInt(page, 1));
        dto.setPageSize(defaultInt(pageSize, 10));
        dto.setName(trimToNull(name));
        dto.setStatus(status);
        dto.setChannelType(trimToNull(channelType));
        return JSON.toJSONString(Result.success(hotelHighVoucherService.pageQuery(dto)));
    }

    @Tool(description = "按优惠券 id 查询优惠券详情，并返回领券和用券统计。")
    public String getVoucherDetail(@ToolParam(description = "优惠券 id") Long id) {
        HotelHighVoucher voucher = hotelHighVoucherMapper.getById(id);
        if (voucher == null) {
            return JSON.toJSONString(Result.error("未找到优惠券"));
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("voucher", voucher);
        payload.put("receiveCount", defaultZero(hotelHighVoucherOrderMapper.countByVoucherId(id)));
        payload.put("usedCount", defaultZero(hotelHighVoucherOrderMapper.countByVoucherIdAndStatus(id, 3)));
        payload.put("statusText", voucher.getStatus() != null && voucher.getStatus() == 1 ? "已上架" : "已下架");
        payload.put("channelText", channelText(voucher.getChannelType()));
        return JSON.toJSONString(Result.success(payload));
    }

    @Tool(description = "汇总优惠券概览。可按状态、适用渠道和名称过滤，返回优惠券数量和剩余库存总和。")
    public String summarizeVoucherOverview(
            @ToolParam(description = "优惠券状态，可选。1=上架，0=下架") Integer status,
            @ToolParam(description = "适用渠道，可选。UNIVERSAL/DINE_IN/DELIVERY") String channelType,
            @ToolParam(description = "优惠券名称关键字，可选") String name) {
        String safeName = trimToNull(name);
        String safeChannelType = trimToNull(channelType);
        Long count = hotelHighVoucherMapper.countByCondition(safeName, safeChannelType, status);
        Integer stock = hotelHighVoucherMapper.sumAvailableStockByCondition(safeName, safeChannelType, status);

        Map<String, Object> payload = new HashMap<>();
        payload.put("voucherCount", defaultLong(count));
        payload.put("totalAvailableStock", defaultZero(stock));
        payload.put("status", status);
        payload.put("statusText", status == null ? "全部状态" : (status == 1 ? "已上架" : "已下架"));
        payload.put("channelType", safeChannelType == null ? "ALL" : safeChannelType);
        payload.put("channelText", safeChannelType == null ? "全部场景" : channelText(safeChannelType));
        payload.put("name", safeName);
        return JSON.toJSONString(Result.success(payload));
    }

    @Tool(description = "按优惠券 id 查询领券记录，支持状态和手机号过滤。状态 1=已领取，2=已锁定，3=已使用，4=已过期，5=已取消。")
    public String queryVoucherReceiveRecords(
            @ToolParam(description = "优惠券 id") Long voucherId,
            @ToolParam(description = "页码，默认 1") Integer page,
            @ToolParam(description = "每页数量，默认 10") Integer pageSize,
            @ToolParam(description = "领券记录状态，可选") Integer status,
            @ToolParam(description = "手机号关键字，可选") String phone) {
        if (voucherId == null) {
            return JSON.toJSONString(Result.error("请提供优惠券 id"));
        }
        PageHelper.startPage(defaultInt(page, 1), defaultInt(pageSize, 10));
        Page<Map<String, Object>> records = (Page<Map<String, Object>>) hotelHighVoucherOrderMapper.pageReceiveRecordsByVoucherId(
                voucherId, status, trimToNull(phone)
        );
        return JSON.toJSONString(Result.success(new PageResult(records.getTotal(), records.getResult())));
    }

    @Tool(description = "更新优惠券状态。1=上架，0=下架。")
    public String updateVoucherStatus(
            @ToolParam(description = "优惠券 id") Long id,
            @ToolParam(description = "状态。1=上架，0=下架") Integer status) {
        hotelHighVoucherService.updateStatus(id, status);
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", id);
        payload.put("status", status);
        payload.put("statusText", status != null && status == 1 ? "已上架" : "已下架");
        return JSON.toJSONString(Result.success(payload));
    }

    @Tool(description = "查询菜品互动摘要，返回点赞数、收藏数、笔记数、评价数、评分和推荐权重。")
    public String queryDishInteractionSummary(@ToolParam(description = "菜品 id") Long dishId) {
        Dish dish = dishMapper.getById(dishId);
        if (dish == null) {
            return JSON.toJSONString(Result.error("未找到菜品"));
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("dishId", dish.getId());
        payload.put("name", dish.getName());
        payload.put("tagType", dish.getTagType());
        payload.put("luxuryLevel", dish.getLuxuryLevel());
        payload.put("recommendWeight", dish.getRecommendWeight());
        payload.put("likeCount", dish.getLikeCount());
        payload.put("favoriteCount", dish.getFavoriteCount());
        payload.put("noteCount", dish.getNoteCount());
        payload.put("commentCount", dish.getCommentCount());
        payload.put("score", dish.getScore());
        return JSON.toJSONString(Result.success(payload));
    }

    @Tool(description = "查询某道菜的评价分页列表。")
    public String queryDishComments(
            @ToolParam(description = "菜品 id") Long dishId,
            @ToolParam(description = "页码，默认 1") Integer page,
            @ToolParam(description = "每页数量，默认 10") Integer pageSize) {
        if (dishMapper.getById(dishId) == null) {
            return JSON.toJSONString(Result.error("未找到菜品"));
        }
        PageHelper.startPage(defaultInt(page, 1), defaultInt(pageSize, 10));
        Page<DishCommentVO> records = dishCommentMapper.pageVisibleByDishId(dishId);
        return JSON.toJSONString(Result.success(new PageResult(records.getTotal(), records.getResult())));
    }

    @Tool(description = "查询某道菜的种草笔记列表。")
    public String queryDishNotes(@ToolParam(description = "菜品 id") Long dishId) {
        if (dishMapper.getById(dishId) == null) {
            return JSON.toJSONString(Result.error("未找到菜品"));
        }
        List<DishNoteVO> records = dishNoteMapper.listVisibleByDishId(dishId);
        return JSON.toJSONString(Result.success(records));
    }

    @Tool(description = "查询顾客分页列表，支持手机号和昵称模糊搜索。")
    public String queryUsers(
            @ToolParam(description = "页码，默认 1") Integer page,
            @ToolParam(description = "每页数量，默认 10") Integer pageSize,
            @ToolParam(description = "手机号关键字，可选") String phone,
            @ToolParam(description = "昵称关键字，可选") String nickname) {
        PageHelper.startPage(defaultInt(page, 1), defaultInt(pageSize, 10));
        Page<User> records = (Page<User>) userMapper.pageQuery(trimToNull(nickname), trimToNull(phone));
        return JSON.toJSONString(Result.success(new PageResult(records.getTotal(), records.getResult())));
    }

    @Tool(description = "查询顾客消费画像。优先使用用户 id，也可以传手机号。")
    public String getUserConsumptionProfile(
            @ToolParam(description = "用户 id，可选") Long userId,
            @ToolParam(description = "手机号，可选") String phone) {
        User user = resolveUser(userId, phone);
        if (user == null) {
            return JSON.toJSONString(Result.error("未找到顾客"));
        }

        Map<String, Object> summary = userMapper.getConsumptionSummary(user.getId(), null, null);
        Map<String, Object> payload = new HashMap<>();
        payload.put("user", user);
        payload.put("consumption", summary);
        payload.put("couponReceiveCount", defaultZero(hotelHighVoucherOrderMapper.countByUserId(user.getId())));
        payload.put("couponUsedCount", defaultZero(hotelHighVoucherOrderMapper.countByUserIdAndStatus(user.getId(), 3)));
        return JSON.toJSONString(Result.success(payload));
    }

    @Tool(description = "查询高消费顾客榜单。默认统计全部历史，也可以传时间范围。")
    public String topUsersByConsumption(
            @ToolParam(description = "返回数量，默认 10") Integer limit,
            @ToolParam(description = "开始时间，可选，格式 yyyy-MM-dd 或 yyyy-MM-dd HH:mm:ss") String beginTime,
            @ToolParam(description = "结束时间，可选，格式 yyyy-MM-dd 或 yyyy-MM-dd HH:mm:ss") String endTime) {
        LocalDateTime begin = parseDateTime(beginTime, true);
        LocalDateTime end = parseDateTime(endTime, false);
        List<Map<String, Object>> ranking = ordersMapper.listUserConsumptionRanking(begin, end, defaultInt(limit, 10));
        return JSON.toJSONString(Result.success(ranking));
    }

    public String findMentionedDishCategoryName(String text) {
        Category category = findCategoryMentionedInText(text, DISH_CATEGORY_TYPE);
        return category != null ? category.getName() : null;
    }

    public String findMentionedSetmealCategoryName(String text) {
        Category category = findCategoryMentionedInText(text, SETMEAL_CATEGORY_TYPE);
        return category != null ? category.getName() : null;
    }

    private PageResult buildDishPage(int page, int pageSize, String name, Long categoryId, Integer status) {
        DishPageQueryDTO dto = new DishPageQueryDTO();
        dto.setPage(page);
        dto.setPageSize(pageSize);
        dto.setName(name);
        dto.setCategoryId(categoryId == null ? null : categoryId.intValue());
        dto.setStatus(status);
        return dishService.pageQuery(dto);
    }

    private Long resolveCategoryId(String categoryName, Integer type) {
        Category category = findBestCategoryMatch(categoryName, type);
        return category != null ? category.getId() : null;
    }

    private Category findBestCategoryMatch(String categoryName, Integer type) {
        if (!StringUtils.hasText(categoryName)) {
            return null;
        }

        String expected = normalize(categoryName);
        Category fuzzyMatch = null;
        for (Category category : categoryService.list(type)) {
            if (category == null || !StringUtils.hasText(category.getName())) {
                continue;
            }

            String current = normalize(category.getName());
            if (current.equals(expected)) {
                return category;
            }
            if (current.contains(expected) || expected.contains(current)) {
                if (fuzzyMatch == null || category.getName().length() > fuzzyMatch.getName().length()) {
                    fuzzyMatch = category;
                }
            }
        }
        return fuzzyMatch;
    }

    private Category findCategoryMentionedInText(String text, Integer type) {
        if (!StringUtils.hasText(text)) {
            return null;
        }

        String normalizedText = normalize(text);
        Category bestMatch = null;
        for (Category category : categoryService.list(type)) {
            if (category == null || !StringUtils.hasText(category.getName())) {
                continue;
            }

            String normalizedCategoryName = normalize(category.getName());
            if (!normalizedText.contains(normalizedCategoryName)) {
                continue;
            }

            if (bestMatch == null || category.getName().length() > bestMatch.getName().length()) {
                bestMatch = category;
            }
        }
        return bestMatch;
    }

    private User resolveUser(Long userId, String phone) {
        if (userId != null) {
            return userMapper.getById(userId);
        }
        if (StringUtils.hasText(phone)) {
            return userMapper.getByPhone(phone.trim());
        }
        return null;
    }

    private long countOrders(Integer status,
                             Integer orderType,
                             LocalDateTime beginTime,
                             LocalDateTime endTime,
                             String number,
                             String phone,
                             Long userId) {
        OrdersPageQueryDTO dto = new OrdersPageQueryDTO();
        dto.setStatus(status);
        dto.setOrderType(orderType);
        dto.setBeginTime(beginTime);
        dto.setEndTime(endTime);
        dto.setNumber(trimToNull(number));
        dto.setPhone(trimToNull(phone));
        dto.setUserId(userId);
        return defaultLong(ordersMapper.countPageQuery(dto));
    }

    private LocalDateTime parseDateTime(String value, boolean beginOfDayWhenDateOnly) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String text = value.trim();
        try {
            return LocalDateTime.parse(text, DATE_TIME_FORMATTER);
        } catch (DateTimeParseException ignored) {
        }
        try {
            LocalDate date = LocalDate.parse(text, DATE_FORMATTER);
            return beginOfDayWhenDateOnly ? date.atStartOfDay() : date.plusDays(1).atStartOfDay();
        } catch (DateTimeParseException ignored) {
        }
        return null;
    }

    private BigDecimal defaultAmount(Double value) {
        return BigDecimal.valueOf(value == null ? 0D : value);
    }

    private int defaultInt(Integer value, int defaultValue) {
        return value == null || value <= 0 ? defaultValue : value;
    }

    private int defaultZero(Integer value) {
        return value == null ? 0 : value;
    }

    private long defaultLong(Long value) {
        return value == null ? 0L : value;
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private String normalize(String value) {
        String text = trimToNull(value);
        return text == null ? "" : text.toLowerCase(Locale.ROOT);
    }

    private String channelText(String channelType) {
        if (!StringUtils.hasText(channelType) || "UNIVERSAL".equalsIgnoreCase(channelType)) {
            return "全部场景";
        }
        if ("DINE_IN".equalsIgnoreCase(channelType)) {
            return "堂食";
        }
        if ("DELIVERY".equalsIgnoreCase(channelType)) {
            return "外卖";
        }
        return channelType;
    }
}
