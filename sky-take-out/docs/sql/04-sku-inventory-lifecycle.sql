-- 筑家优选交易平台：SKU 库存交易闭环
-- 下单预占、支付确认、取消/超时释放所需的幂等记录与审计流水。
-- MySQL 8.x；可重复执行。

SET NAMES utf8mb4;
USE sky_take_out;

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
