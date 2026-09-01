-- 筑家优选交易平台：现有基座完整数据库基线
-- MySQL 8.x；可在一个全新的 MySQL 实例上直接执行。
-- 当前表名仍与基座代码保持一致，后续领域改造阶段再迁移为 product / sku 模型。

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
    setmeal_id BIGINT NULL,
    dish_flavor VARCHAR(512) NULL,
    number INT NOT NULL DEFAULT 1,
    amount DECIMAL(10,2) NOT NULL,
    image VARCHAR(512) NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_shopping_cart_user (user_id),
    KEY idx_shopping_cart_dish (user_id, dish_id),
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
    setmeal_id BIGINT NULL,
    dish_flavor VARCHAR(512) NULL,
    number INT NOT NULL DEFAULT 1,
    amount DECIMAL(10,2) NOT NULL,
    image VARCHAR(512) NULL,
    PRIMARY KEY (id),
    KEY idx_order_detail_order (order_id),
    KEY idx_order_detail_dish (dish_id),
    KEY idx_order_detail_setmeal (setmeal_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细';

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
