-- 筑家优选交易平台：现有基座完整数据库基线
-- MySQL 8.x；可在一个全新的 MySQL 实例上直接执行。
-- 保留旧 dish 模型供现有链路兼容，新商品域使用 product / product_sku。

SET NAMES utf8mb4;
SET time_zone = '+08:00';
SET FOREIGN_KEY_CHECKS = 0;

CREATE DATABASE IF NOT EXISTS sky_take_out
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE sky_take_out;

CREATE TABLE IF NOT EXISTS employee (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    name VARCHAR(32) NOT NULL COMMENT '姓名',
    username VARCHAR(32) NOT NULL COMMENT '用户名',
    password VARCHAR(64) NOT NULL COMMENT 'MD5密码',
    phone VARCHAR(20) NOT NULL COMMENT '手机号',
    sex VARCHAR(2) NOT NULL DEFAULT '1' COMMENT '性别',
    id_number VARCHAR(32) NULL COMMENT '身份证号',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1启用 0禁用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_user BIGINT NULL,
    update_user BIGINT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_employee_username (username),
    UNIQUE KEY uk_employee_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='后台员工';

CREATE TABLE IF NOT EXISTS user (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    openid VARCHAR(64) NOT NULL COMMENT '用户唯一标识',
    name VARCHAR(64) NULL COMMENT '昵称',
    phone VARCHAR(20) NULL COMMENT '手机号',
    sex VARCHAR(2) NULL COMMENT '性别',
    id_number VARCHAR(32) NULL COMMENT '身份证号',
    avatar VARCHAR(512) NULL COMMENT '头像',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_openid (openid),
    UNIQUE KEY uk_user_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商城用户';

CREATE TABLE IF NOT EXISTS category (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    type TINYINT NOT NULL COMMENT '1商品分类 2组合分类',
    name VARCHAR(64) NOT NULL COMMENT '分类名称',
    sort INT NOT NULL DEFAULT 0 COMMENT '排序',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1启用 0禁用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_user BIGINT NULL,
    update_user BIGINT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_category_type_name (type, name),
    KEY idx_category_status_sort (status, sort)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类';

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

CREATE TABLE IF NOT EXISTS dish (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    name VARCHAR(128) NOT NULL COMMENT '商品名称',
    category_id BIGINT NOT NULL COMMENT '分类ID',
    price DECIMAL(10,2) NOT NULL COMMENT '销售价',
    image VARCHAR(512) NULL COMMENT '主图',
    description VARCHAR(512) NULL COMMENT '描述',
    tag_type VARCHAR(32) NOT NULL DEFAULT 'DINING' COMMENT '基座标签类型',
    luxury_level TINYINT NOT NULL DEFAULT 1 COMMENT '推荐等级1-5',
    recommend_weight INT NOT NULL DEFAULT 0 COMMENT '推荐权重',
    like_count INT NOT NULL DEFAULT 0,
    favorite_count INT NOT NULL DEFAULT 0,
    note_count INT NOT NULL DEFAULT 0,
    comment_count INT NOT NULL DEFAULT 0,
    score DECIMAL(3,2) NOT NULL DEFAULT 5.00,
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1上架 0下架',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_user BIGINT NULL,
    update_user BIGINT NULL,
    PRIMARY KEY (id),
    KEY idx_dish_category_status (category_id, status),
    KEY idx_dish_tag_type (tag_type),
    KEY idx_dish_recommend_weight (recommend_weight),
    KEY idx_dish_score (score)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='基座商品表';

CREATE TABLE IF NOT EXISTS dish_flavor (
    id BIGINT NOT NULL AUTO_INCREMENT,
    dish_id BIGINT NOT NULL COMMENT '商品ID',
    name VARCHAR(64) NOT NULL COMMENT '规格名称',
    value VARCHAR(512) NOT NULL COMMENT '规格值JSON',
    PRIMARY KEY (id),
    KEY idx_dish_flavor_dish (dish_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品规格';

CREATE TABLE IF NOT EXISTS setmeal (
    id BIGINT NOT NULL AUTO_INCREMENT,
    category_id BIGINT NOT NULL,
    name VARCHAR(128) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    status TINYINT NOT NULL DEFAULT 1,
    description VARCHAR(512) NULL,
    image VARCHAR(512) NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_user BIGINT NULL,
    update_user BIGINT NULL,
    PRIMARY KEY (id),
    KEY idx_setmeal_category_status (category_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品组合';

CREATE TABLE IF NOT EXISTS setmeal_dish (
    id BIGINT NOT NULL AUTO_INCREMENT,
    setmeal_id BIGINT NOT NULL,
    dish_id BIGINT NOT NULL,
    name VARCHAR(128) NULL,
    price DECIMAL(10,2) NULL,
    copies INT NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    UNIQUE KEY uk_setmeal_dish (setmeal_id, dish_id),
    KEY idx_setmeal_dish_dish (dish_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='组合商品明细';

CREATE TABLE IF NOT EXISTS address_book (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    consignee VARCHAR(64) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    sex VARCHAR(2) NULL,
    province_code VARCHAR(16) NULL,
    province_name VARCHAR(32) NULL,
    city_code VARCHAR(16) NULL,
    city_name VARCHAR(32) NULL,
    district_code VARCHAR(16) NULL,
    district_name VARCHAR(32) NULL,
    detail VARCHAR(255) NOT NULL,
    label VARCHAR(32) NULL,
    is_default TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_address_book_user (user_id),
    KEY idx_address_book_default (user_id, is_default)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收货地址';

CREATE TABLE IF NOT EXISTS shopping_cart (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(128) NOT NULL,
    user_id BIGINT NOT NULL,
    dish_id BIGINT NULL,
    product_id BIGINT NULL,
    sku_id BIGINT NULL,
    setmeal_id BIGINT NULL,
    dish_flavor VARCHAR(512) NULL,
    sku_spec VARCHAR(512) NULL,
    number INT NOT NULL DEFAULT 1,
    amount DECIMAL(10,2) NOT NULL,
    image VARCHAR(512) NULL,
    unit VARCHAR(16) NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_shopping_cart_user (user_id),
    KEY idx_shopping_cart_dish (user_id, dish_id),
    KEY idx_shopping_cart_product_sku (user_id, product_id, sku_id),
    KEY idx_shopping_cart_setmeal (user_id, setmeal_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车';

CREATE TABLE IF NOT EXISTS orders (
    id BIGINT NOT NULL AUTO_INCREMENT,
    number VARCHAR(64) NOT NULL COMMENT '订单号',
    status TINYINT NOT NULL COMMENT '订单状态',
    user_id BIGINT NOT NULL,
    address_book_id BIGINT NULL,
    order_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    checkout_time DATETIME NULL,
    pay_method TINYINT NULL,
    pay_status TINYINT NOT NULL DEFAULT 0,
    order_type TINYINT NOT NULL DEFAULT 1 COMMENT '1配送 2堂食',
    table_no VARCHAR(32) NULL,
    rider_id BIGINT NULL,
    delivery_zone_code VARCHAR(64) NULL,
    coupon_id BIGINT NULL,
    coupon_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    actual_pay_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    amount DECIMAL(10,2) NOT NULL,
    remark VARCHAR(255) NULL,
    user_name VARCHAR(64) NULL,
    phone VARCHAR(20) NULL,
    address VARCHAR(255) NULL,
    consignee VARCHAR(64) NULL,
    cancel_reason VARCHAR(255) NULL,
    rejection_reason VARCHAR(255) NULL,
    cancel_time DATETIME NULL,
    estimated_delivery_time DATETIME NULL,
    delivery_status TINYINT NULL,
    delivery_time DATETIME NULL,
    pack_amount INT NOT NULL DEFAULT 0,
    tableware_number INT NOT NULL DEFAULT 0,
    tableware_status TINYINT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_orders_number (number),
    KEY idx_orders_user_time (user_id, order_time),
    KEY idx_orders_status_time (status, order_time),
    KEY idx_orders_order_type (order_type),
    KEY idx_orders_rider_id (rider_id),
    KEY idx_orders_coupon_id (coupon_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单';

CREATE TABLE IF NOT EXISTS order_detail (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(128) NOT NULL,
    order_id BIGINT NOT NULL,
    dish_id BIGINT NULL,
    product_id BIGINT NULL,
    sku_id BIGINT NULL,
    setmeal_id BIGINT NULL,
    dish_flavor VARCHAR(512) NULL,
    sku_spec VARCHAR(512) NULL,
    number INT NOT NULL DEFAULT 1,
    amount DECIMAL(10,2) NOT NULL,
    image VARCHAR(512) NULL,
    unit VARCHAR(16) NULL,
    PRIMARY KEY (id),
    KEY idx_order_detail_order (order_id),
    KEY idx_order_detail_dish (dish_id),
    KEY idx_order_detail_product_sku (product_id, sku_id),
    KEY idx_order_detail_setmeal (setmeal_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细';

CREATE TABLE IF NOT EXISTS sku_stock_reservation (
    id BIGINT NOT NULL AUTO_INCREMENT,
    order_id BIGINT NOT NULL COMMENT '订单ID',
    order_number VARCHAR(64) NOT NULL COMMENT '订单号快照',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    sku_id BIGINT NOT NULL COMMENT 'SKU ID',
    quantity INT NOT NULL COMMENT '预占数量',
    status TINYINT NOT NULL COMMENT '1预占 2已成交 3已释放',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_sku_reservation_order_sku (order_id, sku_id),
    KEY idx_sku_reservation_status_time (status, update_time),
    KEY idx_sku_reservation_sku (sku_id),
    CONSTRAINT chk_sku_reservation_quantity CHECK (quantity > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单SKU库存预占';

CREATE TABLE IF NOT EXISTS sku_stock_log (
    id BIGINT NOT NULL AUTO_INCREMENT,
    order_id BIGINT NOT NULL COMMENT '订单ID',
    order_number VARCHAR(64) NOT NULL COMMENT '订单号快照',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    sku_id BIGINT NOT NULL COMMENT 'SKU ID',
    change_type VARCHAR(32) NOT NULL COMMENT 'RESERVE/CONFIRM/CANCEL_RELEASE/CANCEL_RESTORE',
    stock_delta INT NOT NULL DEFAULT 0,
    locked_stock_delta INT NOT NULL DEFAULT 0,
    before_stock INT NOT NULL,
    after_stock INT NOT NULL,
    before_locked_stock INT NOT NULL,
    after_locked_stock INT NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_sku_stock_log_business (order_id, sku_id, change_type),
    KEY idx_sku_stock_log_sku_time (sku_id, create_time),
    KEY idx_sku_stock_log_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SKU库存变更流水';

CREATE TABLE IF NOT EXISTS hotel_table (
    id BIGINT NOT NULL AUTO_INCREMENT,
    table_no VARCHAR(32) NOT NULL,
    area_name VARCHAR(64) NOT NULL DEFAULT '主厅',
    seat_count INT NOT NULL DEFAULT 2,
    status TINYINT NOT NULL DEFAULT 1,
    sort INT NOT NULL DEFAULT 0,
    remark VARCHAR(255) NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_hotel_table_table_no (table_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='堂食桌号';

CREATE TABLE IF NOT EXISTS hotel_rider (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(32) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    id_card_no VARCHAR(32) NULL,
    vehicle_type VARCHAR(32) NULL,
    vehicle_no VARCHAR(32) NULL,
    status TINYINT NOT NULL DEFAULT 1,
    delivery_zone_code VARCHAR(64) NULL,
    sort INT NOT NULL DEFAULT 0,
    remark VARCHAR(255) NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_hotel_rider_phone (phone),
    KEY idx_hotel_rider_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='配送人员';

CREATE TABLE IF NOT EXISTS hotel_high_voucher (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(64) NOT NULL,
    scope_type VARCHAR(32) NOT NULL,
    scope_id BIGINT NULL,
    coupon_type VARCHAR(32) NOT NULL DEFAULT 'CASH',
    channel_type VARCHAR(32) NOT NULL DEFAULT 'UNIVERSAL',
    total_stock INT NOT NULL DEFAULT 0,
    available_stock INT NOT NULL DEFAULT 0,
    pay_value DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    actual_value DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    begin_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    seckill_begin_time DATETIME NULL,
    seckill_end_time DATETIME NULL,
    per_limit INT NOT NULL DEFAULT 1,
    day_limit INT NOT NULL DEFAULT 1,
    status TINYINT NOT NULL DEFAULT 1,
    rules TEXT NULL,
    remark VARCHAR(255) NULL,
    create_user BIGINT NULL,
    update_user BIGINT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_voucher_scope (scope_type, scope_id),
    KEY idx_voucher_status (status),
    KEY idx_voucher_time (begin_time, end_time),
    KEY idx_voucher_seckill_time (seckill_begin_time, seckill_end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券活动';

CREATE TABLE IF NOT EXISTS hotel_high_voucher_order (
    id BIGINT NOT NULL AUTO_INCREMENT,
    voucher_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    order_id BIGINT NULL,
    order_no VARCHAR(64) NULL,
    status TINYINT NOT NULL DEFAULT 1,
    receive_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    lock_time DATETIME NULL,
    use_time DATETIME NULL,
    expire_time DATETIME NULL,
    cancel_time DATETIME NULL,
    source_type VARCHAR(32) NOT NULL DEFAULT 'SECKILL',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_voucher_order_no (order_no),
    KEY idx_voucher_order_user (user_id),
    KEY idx_voucher_order_voucher (voucher_id),
    KEY idx_voucher_order_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户优惠券';

CREATE TABLE IF NOT EXISTS dish_note (
    id BIGINT NOT NULL AUTO_INCREMENT,
    dish_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    title VARCHAR(128) NOT NULL,
    content TEXT NOT NULL,
    images TEXT NULL,
    liked INT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 1,
    is_featured TINYINT NOT NULL DEFAULT 0,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_dish_note_dish (dish_id),
    KEY idx_dish_note_user (user_id),
    KEY idx_dish_note_status (status, is_featured)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品内容笔记';

CREATE TABLE IF NOT EXISTS dish_comment (
    id BIGINT NOT NULL AUTO_INCREMENT,
    dish_id BIGINT NOT NULL,
    order_id BIGINT NULL,
    user_id BIGINT NOT NULL,
    parent_id BIGINT NULL,
    content TEXT NOT NULL,
    score DECIMAL(3,2) NOT NULL DEFAULT 5.00,
    liked INT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_dish_comment_dish (dish_id),
    KEY idx_dish_comment_order (order_id),
    KEY idx_dish_comment_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品评价';

CREATE TABLE IF NOT EXISTS dish_like (
    id BIGINT NOT NULL AUTO_INCREMENT,
    dish_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_dish_like_user_dish (user_id, dish_id),
    KEY idx_dish_like_dish (dish_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品点赞';

CREATE TABLE IF NOT EXISTS dish_favorite (
    id BIGINT NOT NULL AUTO_INCREMENT,
    dish_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_dish_favorite_user_dish (user_id, dish_id),
    KEY idx_dish_favorite_dish (dish_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品收藏';

-- 管理员：admin / 123456
INSERT INTO employee
    (id, name, username, password, phone, sex, id_number, status, create_user, update_user)
VALUES
    (1, '系统管理员', 'admin', 'e10adc3949ba59abbe56e057f20f883e', '13800000000', '1', '110101199001010000', 1, 1, 1)
ON DUPLICATE KEY UPDATE name = VALUES(name), status = VALUES(status);

INSERT INTO user (id, openid, name, phone, sex)
VALUES (1, 'sms-13800138000', '筑家体验用户', '13800138000', '1')
ON DUPLICATE KEY UPDATE name = VALUES(name), phone = VALUES(phone);

INSERT INTO category (id, type, name, sort, status, create_user, update_user)
VALUES
    (1, 1, '瓷砖', 1, 1, 1, 1),
    (2, 1, '卫浴', 2, 1, 1, 1),
    (3, 1, '木地板', 3, 1, 1, 1),
    (4, 1, '橱柜', 4, 1, 1, 1),
    (5, 2, '整装组合', 10, 1, 1, 1)
ON DUPLICATE KEY UPDATE name = VALUES(name), sort = VALUES(sort), status = VALUES(status);

INSERT INTO dish
    (id, name, category_id, price, image, description, tag_type, luxury_level, recommend_weight, score, status, create_user, update_user)
VALUES
    (1, '云灰柔光砖 800×800', 1, 89.00, NULL, '低饱和云灰色，适合客厅与餐厅通铺。', 'DINING', 4, 100, 4.90, 1, 1, 1),
    (2, '雪域白通体大理石砖', 1, 128.00, NULL, '通体纹理，耐磨防滑，适合现代简约空间。', 'DINING', 5, 90, 4.80, 1, 1, 1),
    (3, '恒温智能坐便器 Z1', 2, 3299.00, NULL, '即热冲洗、座圈加热和离座冲水。', 'DINING', 5, 100, 4.90, 1, 1, 1),
    (4, '雅黑恒温花洒套装', 2, 1599.00, NULL, '恒温阀芯，多模式出水，雅黑耐指纹表面。', 'DINING', 4, 80, 4.70, 1, 1, 1),
    (5, '橡木原色三层实木地板', 3, 268.00, NULL, '稳定三层结构，自然橡木纹理。', 'DINING', 5, 100, 4.90, 1, 1, 1),
    (6, '胡桃木色强化复合地板', 3, 139.00, NULL, '耐磨易打理，适合卧室和书房。', 'DINING', 3, 70, 4.60, 1, 1, 1),
    (7, '奶油白一字型定制橱柜', 4, 1299.00, NULL, '按延米计价，环保板材与隐藏式拉手。', 'BANQUET', 5, 100, 4.90, 1, 1, 1),
    (8, '岩板台面现代橱柜', 4, 1899.00, NULL, '岩板台面搭配高柜收纳系统，按延米计价。', 'BANQUET', 5, 90, 4.80, 1, 1, 1)
ON DUPLICATE KEY UPDATE
    name = VALUES(name), category_id = VALUES(category_id), price = VALUES(price),
    description = VALUES(description), status = VALUES(status);

INSERT INTO dish_flavor (id, dish_id, name, value)
VALUES
    (1, 1, '规格', '["800×800mm"]'),
    (2, 1, '表面', '["柔光","哑光"]'),
    (3, 2, '规格', '["750×1500mm","900×1800mm"]'),
    (4, 3, '坑距', '["305mm","400mm"]'),
    (5, 4, '颜色', '["雅黑","枪灰"]'),
    (6, 5, '规格', '["1900×190×15mm"]'),
    (7, 6, '规格', '["1215×195×10mm"]'),
    (8, 7, '计价单位', '["延米"]'),
    (9, 8, '计价单位', '["延米"]')
ON DUPLICATE KEY UPDATE name = VALUES(name), value = VALUES(value);

INSERT INTO product
    (id, category_id, legacy_dish_id, product_code, brand_name, name, subtitle,
     unit, main_image, original_price, attributes_json, detail_description,
     status, sort, create_user, update_user)
VALUES
    (1, 1, 1, 'ZJ-TILE-001', '砚川', '云灰柔光砖 800×800', '低饱和云灰色，适合客厅与餐厅通铺。', '㎡', '/guest/images/tile-product.png', 109.00, JSON_OBJECT('space', JSON_ARRAY('客厅', '餐厅'), 'size', JSON_ARRAY('800×800mm'), 'finish', JSON_ARRAY('柔光', '哑光'), 'color', JSON_ARRAY('灰色'), 'slip', JSON_ARRAY('R10'), 'texture', JSON_ARRAY('水泥纹')), '云灰低饱和色调，柔光表面适合现代简约客餐厅。', 1, 100, 1, 1),
    (2, 1, 2, 'ZJ-TILE-002', '石见', '雪域白通体大理石砖', '通体纹理，耐磨防滑，适合现代简约空间。', '㎡', '/guest/images/tile-product.png', 158.00, JSON_OBJECT('space', JSON_ARRAY('客厅', '玄关'), 'size', JSON_ARRAY('750×1500mm', '900×1800mm'), 'finish', JSON_ARRAY('哑光'), 'color', JSON_ARRAY('白色'), 'slip', JSON_ARRAY('R10'), 'texture', JSON_ARRAY('大理石纹')), '白色大理石纹理与通体工艺，购买前需核对批次与铺贴排版。', 1, 90, 1, 1),
    (3, 2, 3, 'ZJ-BATH-001', '白屿', '恒温智能坐便器 Z1', '即热冲洗、座圈加热和离座冲水。', '套', '/guest/images/bath-product.png', 3699.00, JSON_OBJECT('type', JSON_ARRAY('智能坐便器'), 'pit', JSON_ARRAY('305mm', '400mm'), 'size', JSON_ARRAY('标准尺寸'), 'installation', JSON_ARRAY('落地式'), 'flush', JSON_ARRAY('虹吸式'), 'function', JSON_ARRAY('座圈加热', '即热冲洗')), '下单前需核对坑距、给排水、电源与安装空间。', 1, 100, 1, 1),
    (4, 2, 4, 'ZJ-BATH-002', '白屿', '雅黑恒温花洒套装', '恒温阀芯，多模式出水，雅黑耐指纹表面。', '套', '/guest/images/bath-product.png', 1899.00, JSON_OBJECT('type', JSON_ARRAY('花洒'), 'pit', JSON_ARRAY(), 'size', JSON_ARRAY('标准尺寸'), 'installation', JSON_ARRAY('壁挂式'), 'flush', JSON_ARRAY(), 'function', JSON_ARRAY('恒温', '多模式出水')), '安装前需确认冷热水接口间距、水压与墙体条件。', 1, 80, 1, 1),
    (5, 3, 5, 'ZJ-FLOOR-001', '森序', '橡木原色三层实木地板', '稳定三层结构，自然橡木纹理。', '㎡', '/guest/images/floor-product.png', 328.00, JSON_OBJECT('material', JSON_ARRAY('三层实木'), 'thickness', JSON_ARRAY('15mm'), 'color', JSON_ARRAY('原木色'), 'lock', JSON_ARRAY('锁扣'), 'heating', JSON_ARRAY('适配地暖'), 'finish', JSON_ARRAY('哑光', '柔光')), '自然橡木纹理与锁扣结构，铺装前需确认基层平整度和完成面高度。', 1, 100, 1, 1),
    (6, 3, 6, 'ZJ-FLOOR-002', '森序', '胡桃木色强化复合地板', '耐磨易打理，适合卧室和书房。', '㎡', '/guest/images/floor-product.png', 169.00, JSON_OBJECT('material', JSON_ARRAY('强化复合'), 'thickness', JSON_ARRAY('10mm'), 'color', JSON_ARRAY('胡桃木色', '浅胡桃色'), 'lock', JSON_ARRAY('锁扣'), 'heating', JSON_ARRAY('需咨询'), 'finish', JSON_ARRAY('耐磨面')), '强化复合结构与耐磨表面，购买前需核对包装面积与铺装损耗。', 1, 70, 1, 1),
    (7, 4, 7, 'ZJ-CABINET-001', '木衡', '奶油白一字型定制橱柜', '按延米计价，多层板柜体与隐藏式拉手。', '延米', '/guest/images/cabinet-product.png', 1599.00, JSON_OBJECT('door', JSON_ARRAY('平板门'), 'board', JSON_ARRAY('多层板'), 'counter', JSON_ARRAY('待选台面', '石英石'), 'color', JSON_ARRAY('奶油白'), 'layout', JSON_ARRAY('一字型'), 'pricing', JSON_ARRAY('按延米')), '基础价格按延米展示，最终组合需结合现场测量、柜体、门板与台面确认。', 1, 100, 1, 1),
    (8, 4, 8, 'ZJ-CABINET-002', '木衡', '岩板台面现代橱柜', '岩板台面搭配高柜收纳系统，按延米计价。', '延米', '/guest/images/cabinet-product.png', 2299.00, JSON_OBJECT('door', JSON_ARRAY('平板门'), 'board', JSON_ARRAY('多层板'), 'counter', JSON_ARRAY('岩板'), 'color', JSON_ARRAY('暖灰'), 'layout', JSON_ARRAY('一字型'), 'pricing', JSON_ARRAY('按延米')), '岩板台面与高柜收纳组合，最终价格需结合布局、五金和现场尺寸确认。', 1, 90, 1, 1)
ON DUPLICATE KEY UPDATE
    category_id = VALUES(category_id), legacy_dish_id = VALUES(legacy_dish_id), brand_name = VALUES(brand_name),
    name = VALUES(name), subtitle = VALUES(subtitle), unit = VALUES(unit), main_image = VALUES(main_image),
    original_price = VALUES(original_price), attributes_json = VALUES(attributes_json), detail_description = VALUES(detail_description),
    status = VALUES(status), sort = VALUES(sort), update_user = VALUES(update_user);

INSERT INTO product_sku
    (id, product_id, sku_code, sku_name, spec_json, sale_price, original_price,
     stock, locked_stock, status, sort, version, create_user, update_user)
VALUES
    (1001, 1, 'ZJ-TILE-001-800-SOFT', '800×800mm 柔光', JSON_OBJECT('尺寸', '800×800mm', '表面', '柔光'), 89.00, 109.00, 240, 0, 1, 20, 0, 1, 1),
    (1002, 1, 'ZJ-TILE-001-800-MATT', '800×800mm 哑光', JSON_OBJECT('尺寸', '800×800mm', '表面', '哑光'), 92.00, 112.00, 160, 0, 1, 10, 0, 1, 1),
    (1003, 2, 'ZJ-TILE-002-750', '750×1500mm 哑光', JSON_OBJECT('尺寸', '750×1500mm', '表面', '哑光'), 128.00, 158.00, 180, 0, 1, 20, 0, 1, 1),
    (1004, 2, 'ZJ-TILE-002-900', '900×1800mm 哑光', JSON_OBJECT('尺寸', '900×1800mm', '表面', '哑光'), 168.00, 198.00, 90, 0, 1, 10, 0, 1, 1),
    (1005, 3, 'ZJ-BATH-001-305', '305mm 坑距', JSON_OBJECT('坑距', '305mm', '安装方式', '落地式'), 3299.00, 3699.00, 12, 0, 1, 20, 0, 1, 1),
    (1006, 3, 'ZJ-BATH-001-400', '400mm 坑距', JSON_OBJECT('坑距', '400mm', '安装方式', '落地式'), 3299.00, 3699.00, 8, 0, 1, 10, 0, 1, 1),
    (1007, 4, 'ZJ-BATH-002-BLACK', '雅黑恒温款', JSON_OBJECT('颜色', '雅黑', '功能', '恒温'), 1599.00, 1899.00, 24, 0, 1, 20, 0, 1, 1),
    (1008, 4, 'ZJ-BATH-002-GRAY', '枪灰恒温款', JSON_OBJECT('颜色', '枪灰', '功能', '恒温'), 1659.00, 1959.00, 18, 0, 1, 10, 0, 1, 1),
    (1009, 5, 'ZJ-FLOOR-001-MATT', '1900×190×15mm 哑光', JSON_OBJECT('规格', '1900×190×15mm', '表面', '哑光'), 268.00, 328.00, 320, 0, 1, 20, 0, 1, 1),
    (1010, 5, 'ZJ-FLOOR-001-SOFT', '1900×190×15mm 柔光', JSON_OBJECT('规格', '1900×190×15mm', '表面', '柔光'), 278.00, 338.00, 200, 0, 1, 10, 0, 1, 1),
    (1011, 6, 'ZJ-FLOOR-002-WALNUT', '1215×195×10mm 胡桃木色', JSON_OBJECT('规格', '1215×195×10mm', '颜色', '胡桃木色'), 139.00, 169.00, 360, 0, 1, 20, 0, 1, 1),
    (1012, 6, 'ZJ-FLOOR-002-LIGHT', '1215×195×10mm 浅胡桃色', JSON_OBJECT('规格', '1215×195×10mm', '颜色', '浅胡桃色'), 145.00, 175.00, 280, 0, 1, 10, 0, 1, 1),
    (1013, 7, 'ZJ-CABINET-001-BASE', '多层板柜体+平板门', JSON_OBJECT('柜体', '多层板', '门板', '平板门', '台面', '待选'), 1299.00, 1599.00, 30, 0, 1, 20, 0, 1, 1),
    (1014, 7, 'ZJ-CABINET-001-QUARTZ', '多层板柜体+石英石台面', JSON_OBJECT('柜体', '多层板', '门板', '平板门', '台面', '石英石'), 1599.00, 1899.00, 20, 0, 1, 10, 0, 1, 1),
    (1015, 8, 'ZJ-CABINET-002-12', '12mm 岩板台面', JSON_OBJECT('柜体', '多层板', '台面', '12mm岩板'), 1899.00, 2299.00, 18, 0, 1, 20, 0, 1, 1),
    (1016, 8, 'ZJ-CABINET-002-15', '15mm 岩板台面', JSON_OBJECT('柜体', '多层板', '台面', '15mm岩板'), 2099.00, 2499.00, 12, 0, 1, 10, 0, 1, 1)
ON DUPLICATE KEY UPDATE
    product_id = VALUES(product_id), sku_name = VALUES(sku_name), spec_json = VALUES(spec_json),
    sale_price = VALUES(sale_price), original_price = VALUES(original_price), stock = VALUES(stock),
    locked_stock = VALUES(locked_stock), status = VALUES(status), sort = VALUES(sort), update_user = VALUES(update_user);

INSERT INTO setmeal (id, category_id, name, price, status, description, create_user, update_user)
VALUES (1, 5, '现代简约厨房组合', 18999.00, 1, '橱柜、岩板台面及基础五金组合。', 1, 1)
ON DUPLICATE KEY UPDATE name = VALUES(name), price = VALUES(price), status = VALUES(status);

INSERT INTO setmeal_dish (id, setmeal_id, dish_id, name, price, copies)
VALUES (1, 1, 8, '岩板台面现代橱柜', 1899.00, 10)
ON DUPLICATE KEY UPDATE copies = VALUES(copies), price = VALUES(price);

INSERT INTO address_book
    (id, user_id, consignee, phone, sex, province_code, province_name, city_code, city_name, district_code, district_name, detail, label, is_default)
VALUES
    (1, 1, '体验用户', '13800138000', '1', '110000', '北京市', '110100', '北京市', '110105', '朝阳区', '建材体验中心 1 号', '家', 1)
ON DUPLICATE KEY UPDATE detail = VALUES(detail), is_default = VALUES(is_default);

INSERT INTO hotel_table (id, table_no, area_name, seat_count, status, sort)
VALUES
    (1, 'A01', '展厅洽谈区', 4, 1, 1),
    (2, 'A02', '展厅洽谈区', 6, 1, 2),
    (3, 'VIP-01', '设计师专区', 8, 1, 10)
ON DUPLICATE KEY UPDATE area_name = VALUES(area_name), seat_count = VALUES(seat_count), status = VALUES(status);

INSERT INTO hotel_rider
    (id, name, phone, vehicle_type, vehicle_no, status, delivery_zone_code, sort, remark)
VALUES
    (1, '配送测试员', '13900000001', '厢式货车', '京A00001', 1, 'BJ-CY', 1, '本地冒烟测试数据')
ON DUPLICATE KEY UPDATE name = VALUES(name), status = VALUES(status);

INSERT INTO hotel_high_voucher
    (id, name, scope_type, scope_id, coupon_type, channel_type, total_stock, available_stock,
     pay_value, actual_value, begin_time, end_time, seckill_begin_time, seckill_end_time,
     per_limit, day_limit, status, rules, remark, create_user, update_user)
VALUES
    (1, '筑家新人满500减50券', 'ALL_STORE', NULL, 'CASH', 'UNIVERSAL', 100, 100,
     500.00, 50.00, NOW() - INTERVAL 1 DAY, NOW() + INTERVAL 365 DAY,
     NOW() - INTERVAL 1 DAY, NOW() + INTERVAL 365 DAY, 1, 1, 1,
     '订单满500元可用，每位用户限领1张。', '初始化演示券', 1, 1)
ON DUPLICATE KEY UPDATE
    name = VALUES(name), total_stock = VALUES(total_stock), available_stock = VALUES(available_stock),
    begin_time = VALUES(begin_time), end_time = VALUES(end_time), status = VALUES(status);

SET FOREIGN_KEY_CHECKS = 1;
