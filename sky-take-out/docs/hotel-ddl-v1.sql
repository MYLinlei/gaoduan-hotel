-- 高端酒店品牌后端数据库 DDL（V1）
-- 目标：在现有 sky-take-out 单店模型上扩展高端酒店业务
-- 范围：堂食 / 外卖双模式、高端优惠券、菜品种草评价、自有骑手
-- 数据库：MySQL 8.x

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =========================
-- 1. 扩展基础表
-- =========================

ALTER TABLE dish
    ADD COLUMN tag_type VARCHAR(32) NOT NULL DEFAULT 'DINING'
        COMMENT '菜品标签类型：DINING=奢华餐食 WINE=名酒 BANQUET=宴会推荐'
        AFTER description,
    ADD COLUMN luxury_level TINYINT NOT NULL DEFAULT 1
        COMMENT '轻奢等级：1-5'
        AFTER tag_type,
    ADD COLUMN recommend_weight INT NOT NULL DEFAULT 0
        COMMENT '推荐权重，越大越靠前'
        AFTER luxury_level,
    ADD COLUMN like_count INT NOT NULL DEFAULT 0
        COMMENT '点赞数'
        AFTER recommend_weight,
    ADD COLUMN favorite_count INT NOT NULL DEFAULT 0
        COMMENT '收藏数'
        AFTER like_count,
    ADD COLUMN note_count INT NOT NULL DEFAULT 0
        COMMENT '种草笔记数'
        AFTER favorite_count,
    ADD COLUMN comment_count INT NOT NULL DEFAULT 0
        COMMENT '评价数'
        AFTER note_count,
    ADD COLUMN score DECIMAL(3,2) NOT NULL DEFAULT 5.00
        COMMENT '综合评分'
        AFTER comment_count;

CREATE INDEX idx_dish_tag_type ON dish(tag_type);
CREATE INDEX idx_dish_recommend_weight ON dish(recommend_weight);
CREATE INDEX idx_dish_score ON dish(score);

ALTER TABLE orders
    ADD COLUMN order_type TINYINT NOT NULL DEFAULT 1
        COMMENT '订单类型：1=外卖 2=堂食'
        AFTER pay_status,
    ADD COLUMN table_no VARCHAR(32) NULL
        COMMENT '堂食桌号'
        AFTER order_type,
    ADD COLUMN rider_id BIGINT NULL
        COMMENT '自有骑手ID，仅外卖使用'
        AFTER table_no,
    ADD COLUMN delivery_zone_code VARCHAR(64) NULL
        COMMENT '配送区域编码'
        AFTER rider_id,
    ADD COLUMN coupon_id BIGINT NULL
        COMMENT '使用的高端优惠券ID'
        AFTER delivery_zone_code,
    ADD COLUMN coupon_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00
        COMMENT '优惠券抵扣金额'
        AFTER coupon_id,
    ADD COLUMN actual_pay_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00
        COMMENT '用户实际支付金额'
        AFTER coupon_amount;

CREATE INDEX idx_orders_order_type ON orders(order_type);
CREATE INDEX idx_orders_rider_id ON orders(rider_id);
CREATE INDEX idx_orders_coupon_id ON orders(coupon_id);

-- =========================
-- 2. 堂食桌号与骑手
-- =========================

CREATE TABLE IF NOT EXISTS hotel_table (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    table_no VARCHAR(32) NOT NULL COMMENT '桌号，如A01/VIP-01',
    area_name VARCHAR(64) NOT NULL DEFAULT '主厅' COMMENT '区域名称',
    seat_count INT NOT NULL DEFAULT 2 COMMENT '座位数',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1可用 2占用 3停用',
    sort INT NOT NULL DEFAULT 0 COMMENT '排序',
    remark VARCHAR(255) NULL COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_hotel_table_table_no (table_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='酒店堂食桌号表';

CREATE TABLE IF NOT EXISTS hotel_rider (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    name VARCHAR(32) NOT NULL COMMENT '骑手姓名',
    phone VARCHAR(20) NOT NULL COMMENT '手机号',
    id_card_no VARCHAR(32) NULL COMMENT '身份证号',
    vehicle_type VARCHAR(32) NULL COMMENT '交通工具类型',
    vehicle_no VARCHAR(32) NULL COMMENT '车牌号',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1在职 0停用',
    delivery_zone_code VARCHAR(64) NULL COMMENT '负责配送区域编码',
    sort INT NOT NULL DEFAULT 0 COMMENT '排序',
    remark VARCHAR(255) NULL COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_hotel_rider_phone (phone),
    KEY idx_hotel_rider_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='酒店自有骑手表';

-- =========================
-- 3. 高端优惠券
-- =========================

CREATE TABLE IF NOT EXISTS hotel_high_voucher (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    name VARCHAR(64) NOT NULL COMMENT '优惠券名称',
    scope_type VARCHAR(32) NOT NULL COMMENT '作用域：ALL_STORE/CATEGORY/DISH',
    scope_id BIGINT NULL COMMENT '作用域ID，scope_type非全店时使用',
    coupon_type VARCHAR(32) NOT NULL DEFAULT 'CASH'
        COMMENT '券类型：CASH=代金券 DISCOUNT=折扣券',
    channel_type VARCHAR(32) NOT NULL DEFAULT 'UNIVERSAL'
        COMMENT '渠道：UNIVERSAL=全店 WINE=酒水 BANQUET=宴会',
    total_stock INT NOT NULL DEFAULT 0 COMMENT '总库存',
    available_stock INT NOT NULL DEFAULT 0 COMMENT '可用库存',
    pay_value DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '消费门槛',
    actual_value DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '抵扣金额',
    begin_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME NOT NULL COMMENT '结束时间',
    seckill_begin_time DATETIME NULL COMMENT '秒杀开始时间',
    seckill_end_time DATETIME NULL COMMENT '秒杀结束时间',
    per_limit INT NOT NULL DEFAULT 1 COMMENT '每用户限购张数',
    day_limit INT NOT NULL DEFAULT 1 COMMENT '每用户每日限购张数',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用 0停用',
    rules TEXT NULL COMMENT '使用规则',
    remark VARCHAR(255) NULL COMMENT '备注',
    create_user BIGINT NULL COMMENT '创建人',
    update_user BIGINT NULL COMMENT '更新人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_hotel_high_voucher_scope (scope_type, scope_id),
    KEY idx_hotel_high_voucher_status (status),
    KEY idx_hotel_high_voucher_time (begin_time, end_time),
    KEY idx_hotel_high_voucher_seckill_time (seckill_begin_time, seckill_end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='酒店高端优惠券表';

CREATE TABLE IF NOT EXISTS hotel_high_voucher_order (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    voucher_id BIGINT NOT NULL COMMENT '优惠券ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    order_id BIGINT NULL COMMENT '关联订单ID，未使用时可为空',
    order_no VARCHAR(64) NULL COMMENT '优惠券订单号',
    status TINYINT NOT NULL DEFAULT 1
        COMMENT '状态：1已领取 2已锁定 3已使用 4已过期 5已取消',
    receive_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '领取时间',
    lock_time DATETIME NULL COMMENT '锁券时间',
    use_time DATETIME NULL COMMENT '使用时间',
    expire_time DATETIME NULL COMMENT '失效时间',
    cancel_time DATETIME NULL COMMENT '取消时间',
    source_type VARCHAR(32) NOT NULL DEFAULT 'SECKILL'
        COMMENT '来源：SECKILL/MANUAL/GIFT',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_hotel_high_voucher_order_no (order_no),
    KEY idx_hotel_high_voucher_order_user (user_id),
    KEY idx_hotel_high_voucher_order_voucher (voucher_id),
    KEY idx_hotel_high_voucher_order_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='酒店高端优惠券领取/使用记录表';

-- =========================
-- 4. 菜品内容互动
-- =========================

CREATE TABLE IF NOT EXISTS dish_note (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    dish_id BIGINT NOT NULL COMMENT '菜品ID',
    user_id BIGINT NOT NULL COMMENT '发布用户ID',
    title VARCHAR(128) NOT NULL COMMENT '标题',
    content TEXT NOT NULL COMMENT '内容',
    images TEXT NULL COMMENT '图片，逗号分隔',
    liked INT NOT NULL DEFAULT 0 COMMENT '点赞数',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1展示 0隐藏',
    is_featured TINYINT NOT NULL DEFAULT 0 COMMENT '是否精选',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_dish_note_dish (dish_id),
    KEY idx_dish_note_user (user_id),
    KEY idx_dish_note_status (status, is_featured)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜品种草/名酒品鉴笔记表';

CREATE TABLE IF NOT EXISTS dish_comment (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    dish_id BIGINT NOT NULL COMMENT '菜品ID',
    order_id BIGINT NULL COMMENT '订单ID',
    user_id BIGINT NOT NULL COMMENT '评价用户ID',
    parent_id BIGINT NULL COMMENT '父评论ID，顶级评论为空',
    content TEXT NOT NULL COMMENT '评价内容',
    score DECIMAL(3,2) NOT NULL DEFAULT 5.00 COMMENT '评分',
    liked INT NOT NULL DEFAULT 0 COMMENT '点赞数',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1展示 0隐藏',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_dish_comment_dish (dish_id),
    KEY idx_dish_comment_order (order_id),
    KEY idx_dish_comment_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜品评价表';

CREATE TABLE IF NOT EXISTS dish_like (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    dish_id BIGINT NOT NULL COMMENT '菜品ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_dish_like_user_dish (user_id, dish_id),
    KEY idx_dish_like_dish (dish_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜品点赞表';

CREATE TABLE IF NOT EXISTS dish_favorite (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    dish_id BIGINT NOT NULL COMMENT '菜品ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_dish_favorite_user_dish (user_id, dish_id),
    KEY idx_dish_favorite_dish (dish_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜品收藏表';

-- =========================
-- 5. 可选初始化数据
-- =========================

INSERT INTO hotel_table (table_no, area_name, seat_count, status, sort)
VALUES
    ('A01', '主厅', 2, 1, 1),
    ('A02', '主厅', 4, 1, 2),
    ('VIP-01', '包间', 8, 1, 10)
AS new_rows
ON DUPLICATE KEY UPDATE
    area_name = new_rows.area_name,
    seat_count = new_rows.seat_count,
    status = new_rows.status,
    sort = new_rows.sort;

SET FOREIGN_KEY_CHECKS = 1;
