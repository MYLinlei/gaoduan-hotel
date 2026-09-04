package com.sky.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sky.dto.ProductPageQueryDTO;
import com.sky.exception.BaseException;
import com.sky.mapper.ProductMapper;
import com.sky.mapper.ProductSkuMapper;
import com.sky.result.PageResult;
import com.sky.service.ProductService;
import com.sky.vo.ProductDetailVO;
import com.sky.vo.ProductListVO;
import com.sky.vo.ProductSkuVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private static final int DEFAULT_PAGE_SIZE = 12;
    private static final int MAX_PAGE_SIZE = 60;
    private static final int DEFAULT_RELATED_LIMIT = 4;
    private static final int MAX_RELATED_LIMIT = 12;
    private static final int LOW_STOCK_THRESHOLD = 10;
    private static final Set<String> ALLOWED_SORTS = Set.of("default", "price-asc", "price-desc");
    private static final Map<String, String> CATEGORY_NAMES = Map.of(
            "tile", "瓷砖",
            "bath", "卫浴",
            "floor", "木地板",
            "cabinet", "橱柜"
    );

    private final ProductMapper productMapper;
    private final ProductSkuMapper productSkuMapper;
    private final ObjectMapper objectMapper;

    @Override
    public PageResult pageQuery(ProductPageQueryDTO query) {
        normalizeQuery(query);
        long total = productMapper.count(query);
        if (total == 0) {
            return new PageResult(0, Collections.emptyList());
        }
        List<ProductListVO> products = productMapper.pageQuery(query);
        products.forEach(this::enrichListItem);
        return new PageResult(total, products);
    }

    @Override
    public ProductDetailVO getDetail(Long id) {
        requireProductId(id);
        ProductDetailVO detail = productMapper.getDetailById(id);
        if (detail == null) {
            throw new BaseException("商品不存在或已删除");
        }

        List<ProductSkuVO> skus = productSkuMapper.listByProductId(id);
        skus.forEach(this::enrichSku);
        detail.setSkus(skus);
        detail.setAttributes(readAttributes(detail.getAttributesJson()));
        detail.setStockStatus(resolveStockStatus(detail.getStatus(), detail.getEnabledSkuCount(),
                detail.getAvailableStock()));
        return detail;
    }

    @Override
    public List<ProductListVO> listRelated(Long id, Integer limit) {
        ProductDetailVO detail = getDetail(id);
        int safeLimit = limit == null ? DEFAULT_RELATED_LIMIT : Math.max(1, Math.min(limit, MAX_RELATED_LIMIT));
        List<ProductListVO> products = productMapper.listRelated(detail.getCategoryId(), id, safeLimit);
        products.forEach(this::enrichListItem);
        return products;
    }

    private void normalizeQuery(ProductPageQueryDTO query) {
        if (query == null) {
            throw new BaseException("查询条件不能为空");
        }
        query.setPage(query.getPage() == null ? 1 : Math.max(1, query.getPage()));
        int pageSize = query.getPageSize() == null ? DEFAULT_PAGE_SIZE : query.getPageSize();
        query.setPageSize(Math.max(1, Math.min(pageSize, MAX_PAGE_SIZE)));
        query.setOffset((query.getPage() - 1) * query.getPageSize());
        query.setKeyword(trimToNull(query.getKeyword()));

        String sort = trimToNull(query.getSort());
        sort = sort == null ? "default" : sort.toLowerCase(Locale.ROOT);
        query.setSort(ALLOWED_SORTS.contains(sort) ? sort : "default");

        String categoryCode = trimToNull(query.getCategoryCode());
        if (query.getCategoryId() == null && categoryCode != null) {
            String categoryName = CATEGORY_NAMES.get(categoryCode.toLowerCase(Locale.ROOT));
            if (categoryName == null) {
                throw new BaseException("不支持的商品分类编码: " + categoryCode);
            }
            query.setCategoryName(categoryName);
        }
    }

    private void enrichListItem(ProductListVO product) {
        product.setAttributes(readAttributes(product.getAttributesJson()));
        product.setStockStatus(resolveStockStatus(product.getStatus(), product.getEnabledSkuCount(),
                product.getAvailableStock()));
    }

    private void enrichSku(ProductSkuVO sku) {
        sku.setSpecs(readSpecs(sku.getSpecJson()));
        if (sku.getAvailableStock() == null) {
            int stock = sku.getStock() == null ? 0 : sku.getStock();
            int locked = sku.getLockedStock() == null ? 0 : sku.getLockedStock();
            sku.setAvailableStock(Math.max(stock - locked, 0));
        }
    }

    private String resolveStockStatus(Integer productStatus, Integer enabledSkuCount, Integer availableStock) {
        if (productStatus == null || productStatus == 0) {
            return "off-shelf";
        }
        int skuCount = enabledSkuCount == null ? 0 : enabledSkuCount;
        int stock = availableStock == null ? 0 : availableStock;
        if (skuCount == 0 || stock == 0) {
            return "sold-out";
        }
        return stock <= LOW_STOCK_THRESHOLD ? "low-stock" : "available";
    }

    private Map<String, Object> readAttributes(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<LinkedHashMap<String, Object>>() { });
        } catch (JsonProcessingException ex) {
            log.warn("商品属性 JSON 解析失败: {}", json, ex);
            return Collections.emptyMap();
        }
    }

    private Map<String, String> readSpecs(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<LinkedHashMap<String, String>>() { });
        } catch (JsonProcessingException ex) {
            log.warn("SKU 规格 JSON 解析失败: {}", json, ex);
            return Collections.emptyMap();
        }
    }

    private void requireProductId(Long id) {
        if (id == null || id <= 0) {
            throw new BaseException("商品 ID 不正确");
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
