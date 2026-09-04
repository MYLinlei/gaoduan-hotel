-- 筑家优选交易平台：购物车与订单商品化迁移
-- 作用：在保留 dish/setmeal 兼容字段的同时，增加 product + SKU 快照。
-- 环境：MySQL 8.x；可重复执行。

SET NAMES utf8mb4;
USE sky_take_out;

DROP PROCEDURE IF EXISTS add_column_if_missing;
DROP PROCEDURE IF EXISTS add_index_if_missing;

DELIMITER $$

CREATE PROCEDURE add_column_if_missing(
    IN table_name_value VARCHAR(64),
    IN column_name_value VARCHAR(64),
    IN column_definition_value VARCHAR(512)
)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = table_name_value
          AND column_name = column_name_value
    ) THEN
        SET @ddl = CONCAT('ALTER TABLE `', table_name_value, '` ADD COLUMN ', column_definition_value);
        PREPARE statement FROM @ddl;
        EXECUTE statement;
        DEALLOCATE PREPARE statement;
    END IF;
END$$

CREATE PROCEDURE add_index_if_missing(
    IN table_name_value VARCHAR(64),
    IN index_name_value VARCHAR(64),
    IN index_definition_value VARCHAR(512)
)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.statistics
        WHERE table_schema = DATABASE()
          AND table_name = table_name_value
          AND index_name = index_name_value
    ) THEN
        SET @ddl = CONCAT('ALTER TABLE `', table_name_value, '` ADD INDEX `', index_name_value, '` ', index_definition_value);
        PREPARE statement FROM @ddl;
        EXECUTE statement;
        DEALLOCATE PREPARE statement;
    END IF;
END$$

DELIMITER ;

CALL add_column_if_missing('shopping_cart', 'product_id', '`product_id` BIGINT NULL AFTER `dish_id`');
CALL add_column_if_missing('shopping_cart', 'sku_id', '`sku_id` BIGINT NULL AFTER `product_id`');
CALL add_column_if_missing('shopping_cart', 'sku_spec', '`sku_spec` VARCHAR(512) NULL AFTER `dish_flavor`');
CALL add_column_if_missing('shopping_cart', 'unit', '`unit` VARCHAR(16) NULL AFTER `image`');
CALL add_index_if_missing('shopping_cart', 'idx_shopping_cart_product_sku', '(`user_id`, `product_id`, `sku_id`)');

CALL add_column_if_missing('order_detail', 'product_id', '`product_id` BIGINT NULL AFTER `dish_id`');
CALL add_column_if_missing('order_detail', 'sku_id', '`sku_id` BIGINT NULL AFTER `product_id`');
CALL add_column_if_missing('order_detail', 'sku_spec', '`sku_spec` VARCHAR(512) NULL AFTER `dish_flavor`');
CALL add_column_if_missing('order_detail', 'unit', '`unit` VARCHAR(16) NULL AFTER `image`');
CALL add_index_if_missing('order_detail', 'idx_order_detail_product_sku', '(`product_id`, `sku_id`)');

DROP PROCEDURE add_column_if_missing;
DROP PROCEDURE add_index_if_missing;
