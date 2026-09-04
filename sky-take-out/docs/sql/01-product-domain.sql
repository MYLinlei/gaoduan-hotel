-- 筑家优选交易平台：商品域第一批建表脚本
-- 范围：只创建商品 SPU 与 SKU 表，不迁移旧 dish 数据。
-- 环境：MySQL 8.x；可重复执行。

SET NAMES utf8mb4;
SET time_zone = '+08:00';

CREATE DATABASE IF NOT EXISTS sky_take_out
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE sky_take_out;

CREATE TABLE IF NOT EXISTS product (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '商品ID',
    category_id BIGINT NOT NULL COMMENT '分类ID，复用category表',
    legacy_dish_id BIGINT NULL COMMENT '旧dish ID，仅用于过渡期兼容',
    product_code VARCHAR(64) NOT NULL COMMENT '商品编码',
    brand_name VARCHAR(64) NOT NULL COMMENT '品牌名称',
    name VARCHAR(128) NOT NULL COMMENT '商品名称',
    subtitle VARCHAR(255) NULL COMMENT '商品卖点',
    unit VARCHAR(16) NOT NULL COMMENT '计价单位：片/㎡/套/延米等',
    main_image VARCHAR(512) NULL COMMENT '商品主图',
    original_price DECIMAL(10,2) NULL COMMENT '建议原价，实际售价以SKU为准',
    attributes_json JSON NULL COMMENT '品类筛选属性与展示参数',
    detail_description TEXT NULL COMMENT '材质、工艺与商品说明',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1上架 0下架',
    sort INT NOT NULL DEFAULT 0 COMMENT '展示排序，越大越靠前',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_user BIGINT NULL,
    update_user BIGINT NULL,
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '1已删除 0正常',
    PRIMARY KEY (id),
    UNIQUE KEY uk_product_code (product_code),
    UNIQUE KEY uk_product_legacy_dish (legacy_dish_id),
    KEY idx_product_category_status (category_id, status, is_deleted),
    KEY idx_product_brand_status (brand_name, status, is_deleted),
    KEY idx_product_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品SPU';

CREATE TABLE IF NOT EXISTS product_sku (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'SKU ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    sku_code VARCHAR(64) NOT NULL COMMENT 'SKU编码',
    sku_name VARCHAR(128) NOT NULL COMMENT 'SKU名称',
    spec_json JSON NOT NULL COMMENT '规格组合，例如尺寸与表面工艺',
    sale_price DECIMAL(10,2) NOT NULL COMMENT '销售价',
    original_price DECIMAL(10,2) NULL COMMENT 'SKU原价',
    stock INT NOT NULL DEFAULT 0 COMMENT '物理库存',
    locked_stock INT NOT NULL DEFAULT 0 COMMENT '预占库存',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
    sort INT NOT NULL DEFAULT 0 COMMENT '规格排序，越大越靠前',
    version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_user BIGINT NULL,
    update_user BIGINT NULL,
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '1已删除 0正常',
    PRIMARY KEY (id),
    UNIQUE KEY uk_product_sku_code (sku_code),
    KEY idx_product_sku_product (product_id, status, is_deleted),
    KEY idx_product_sku_stock (product_id, stock, locked_stock),
    CONSTRAINT chk_product_sku_stock
        CHECK (stock >= 0 AND locked_stock >= 0 AND locked_stock <= stock),
    CONSTRAINT chk_product_sku_price
        CHECK (sale_price >= 0 AND (original_price IS NULL OR original_price >= 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品SKU';

