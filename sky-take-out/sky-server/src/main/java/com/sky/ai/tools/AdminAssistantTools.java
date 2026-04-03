package com.sky.ai.tools;

import com.alibaba.fastjson.JSON;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Category;
import com.sky.entity.Dish;
import com.sky.entity.Employee;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import com.sky.service.DishService;
import com.sky.service.EmployeeService;
import com.sky.service.SetmealService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class AdminAssistantTools {

    private static final int DISH_CATEGORY_TYPE = 1;
    private static final int SETMEAL_CATEGORY_TYPE = 2;

    private final DishService dishService;
    private final SetmealService setmealService;
    private final CategoryService categoryService;
    private final EmployeeService employeeService;
    private final RedisTemplate<String, Object> redisTemplate;

    public AdminAssistantTools(DishService dishService,
                               SetmealService setmealService,
                               CategoryService categoryService,
                               EmployeeService employeeService,
                               RedisTemplate<String, Object> redisTemplate) {
        this.dishService = dishService;
        this.setmealService = setmealService;
        this.categoryService = categoryService;
        this.employeeService = employeeService;
        this.redisTemplate = redisTemplate;
    }

    @Tool(description = "Query dishes in the merchant backend. Supports page number, page size, fuzzy dish name, category id, and sale status where 1 means on sale and 0 means off sale.")
    public String queryDishes(
            @ToolParam(description = "Page number, default 1") Integer page,
            @ToolParam(description = "Page size, default 10") Integer pageSize,
            @ToolParam(description = "Fuzzy dish name, optional") String name,
            @ToolParam(description = "Dish category id, optional") Long categoryId,
            @ToolParam(description = "Dish status, optional. 1 means on sale, 0 means off sale") Integer status) {
        return JSON.toJSONString(Result.success(buildDishPage(defaultInt(page, 1), defaultInt(pageSize, 10), trimToNull(name), categoryId, status)));
    }

    @Tool(description = "Query dishes with natural filters. Supports fuzzy dish name, category name, and sale status where 1 means on sale and 0 means off sale.")
    public String searchDishes(
            @ToolParam(description = "Page number, default 1") Integer page,
            @ToolParam(description = "Page size, default 10") Integer pageSize,
            @ToolParam(description = "Fuzzy dish name, optional") String name,
            @ToolParam(description = "Dish category name, optional") String categoryName,
            @ToolParam(description = "Dish status, optional. 1 means on sale, 0 means off sale") Integer status) {
        Long categoryId = resolveCategoryId(categoryName, DISH_CATEGORY_TYPE);
        if (StringUtils.hasText(categoryName) && categoryId == null) {
            return JSON.toJSONString(Result.error("未找到菜品分类：" + categoryName.trim()));
        }
        return JSON.toJSONString(Result.success(buildDishPage(defaultInt(page, 1), defaultInt(pageSize, 10), trimToNull(name), categoryId, status)));
    }

    @Tool(description = "Query dish details by dish id.")
    public String getDishById(@ToolParam(description = "Dish id") Long id) {
        return JSON.toJSONString(Result.success(dishService.getByIdWithFlavor(id)));
    }

    @Tool(description = "List dishes by category id. Only dishes currently on sale are returned.")
    public String listDishesByCategory(@ToolParam(description = "Dish category id") Long categoryId) {
        List<Dish> dishes = dishService.getDishsList(categoryId);
        return JSON.toJSONString(Result.success(dishes));
    }

    @Tool(description = "Query setmeals in the merchant backend. Supports page number, page size, fuzzy setmeal name, category id, and sale status where 1 means on sale and 0 means off sale.")
    public String querySetmeals(
            @ToolParam(description = "Page number, default 1") Integer page,
            @ToolParam(description = "Page size, default 10") Integer pageSize,
            @ToolParam(description = "Fuzzy setmeal name, optional") String name,
            @ToolParam(description = "Setmeal category id, optional") Integer categoryId,
            @ToolParam(description = "Setmeal status, optional. 1 means on sale, 0 means off sale") Integer status) {
        SetmealPageQueryDTO dto = new SetmealPageQueryDTO();
        dto.setPage(defaultInt(page, 1));
        dto.setPageSize(defaultInt(pageSize, 10));
        dto.setName(trimToNull(name));
        dto.setCategoryId(categoryId);
        dto.setStatus(status);
        return JSON.toJSONString(Result.success(setmealService.pageQuery(dto)));
    }

    @Tool(description = "Query setmeal details by setmeal id.")
    public String getSetmealById(@ToolParam(description = "Setmeal id") Long id) {
        return JSON.toJSONString(Result.success(setmealService.getByIdWithDish(id)));
    }

    @Tool(description = "Query categories. Supports category type, page number, page size, and fuzzy category name. Type 1 means dish categories and type 2 means setmeal categories.")
    public String listCategories(
            @ToolParam(description = "Category type, optional. 1 means dish category, 2 means setmeal category") Integer type,
            @ToolParam(description = "Page number, optional") Integer page,
            @ToolParam(description = "Page size, optional") Integer pageSize,
            @ToolParam(description = "Fuzzy category name, optional") String name) {
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

    @Tool(description = "Query employees. Supports page number, page size, and fuzzy employee name.")
    public String queryEmployees(
            @ToolParam(description = "Page number, default 1") Integer page,
            @ToolParam(description = "Page size, default 10") Integer pageSize,
            @ToolParam(description = "Fuzzy employee name, optional") String name) {
        EmployeePageQueryDTO dto = new EmployeePageQueryDTO();
        dto.setPage(defaultInt(page, 1));
        dto.setPageSize(defaultInt(pageSize, 10));
        dto.setName(trimToNull(name));
        return JSON.toJSONString(Result.success(employeeService.pageQuery(dto)));
    }

    @Tool(description = "Query employee details by employee id.")
    public String getEmployeeById(@ToolParam(description = "Employee id") Long id) {
        Employee employee = employeeService.getById(id);
        return JSON.toJSONString(Result.success(employee));
    }

    @Tool(description = "Get current shop business status.")
    public String getShopStatus() {
        Integer status = (Integer) redisTemplate.opsForValue().get("SHOP_STATUS");
        Map<String, Object> payload = new HashMap<>();
        payload.put("status", status);
        payload.put("statusText", status != null && status == 1 ? "营业中" : "打烊中");
        return JSON.toJSONString(Result.success(payload));
    }

    @Tool(description = "Update shop business status. 1 means open for business and 0 means closed.")
    public String setShopStatus(@ToolParam(description = "Shop status. 1 means open for business and 0 means closed") Integer status) {
        redisTemplate.opsForValue().set("SHOP_STATUS", status);
        Map<String, Object> payload = new HashMap<>();
        payload.put("status", status);
        payload.put("statusText", status != null && status == 1 ? "营业中" : "打烊中");
        return JSON.toJSONString(Result.success(payload));
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

    private int defaultInt(Integer value, int defaultValue) {
        return value == null || value <= 0 ? defaultValue : value;
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private String normalize(String value) {
        return trimToNull(value).toLowerCase(Locale.ROOT);
    }
}
