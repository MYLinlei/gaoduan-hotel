-- 筑家优选交易平台：商品与 SKU 基线数据
-- 依赖：现有 category 表，并先执行 01-product-domain.sql。
-- 范围：四大品类各 2 个商品，每个商品 2 个 SKU；可重复执行。

SET NAMES utf8mb4;
SET time_zone = '+08:00';

USE sky_take_out;

INSERT INTO category (type, name, sort, status, create_user, update_user)
VALUES
    (1, '瓷砖', 1, 1, 1, 1),
    (1, '卫浴', 2, 1, 1, 1),
    (1, '木地板', 3, 1, 1, 1),
    (1, '橱柜', 4, 1, 1, 1)
ON DUPLICATE KEY UPDATE
    sort = VALUES(sort), status = VALUES(status), update_user = VALUES(update_user);

INSERT INTO product
    (id, category_id, legacy_dish_id, product_code, brand_name, name, subtitle,
     unit, main_image, original_price, attributes_json, detail_description,
     status, sort, create_user, update_user)
VALUES
    (1, (SELECT id FROM category WHERE type = 1 AND name = '瓷砖' LIMIT 1), 1,
     'ZJ-TILE-001', '砚川', '云灰柔光砖 800×800', '低饱和云灰色，适合客厅与餐厅通铺。',
     '㎡', '/guest/images/tile-product.png', 109.00,
     JSON_OBJECT('space', JSON_ARRAY('客厅', '餐厅'), 'size', JSON_ARRAY('800×800mm'),
                 'finish', JSON_ARRAY('柔光', '哑光'), 'color', JSON_ARRAY('灰色'),
                 'slip', JSON_ARRAY('R10'), 'texture', JSON_ARRAY('水泥纹')),
     '云灰低饱和色调，柔光表面适合现代简约客餐厅。', 1, 100, 1, 1),
    (2, (SELECT id FROM category WHERE type = 1 AND name = '瓷砖' LIMIT 1), 2,
     'ZJ-TILE-002', '石见', '雪域白通体大理石砖', '通体纹理，耐磨防滑，适合现代简约空间。',
     '㎡', '/guest/images/tile-product.png', 158.00,
     JSON_OBJECT('space', JSON_ARRAY('客厅', '玄关'), 'size', JSON_ARRAY('750×1500mm', '900×1800mm'),
                 'finish', JSON_ARRAY('哑光'), 'color', JSON_ARRAY('白色'),
                 'slip', JSON_ARRAY('R10'), 'texture', JSON_ARRAY('大理石纹')),
     '白色大理石纹理与通体工艺，购买前需核对批次与铺贴排版。', 1, 90, 1, 1),
    (3, (SELECT id FROM category WHERE type = 1 AND name = '卫浴' LIMIT 1), 3,
     'ZJ-BATH-001', '白屿', '恒温智能坐便器 Z1', '即热冲洗、座圈加热和离座冲水。',
     '套', '/guest/images/bath-product.png', 3699.00,
     JSON_OBJECT('type', JSON_ARRAY('智能坐便器'), 'pit', JSON_ARRAY('305mm', '400mm'),
                 'size', JSON_ARRAY('标准尺寸'), 'installation', JSON_ARRAY('落地式'),
                 'flush', JSON_ARRAY('虹吸式'), 'function', JSON_ARRAY('座圈加热', '即热冲洗')),
     '下单前需核对坑距、给排水、电源与安装空间。', 1, 100, 1, 1),
    (4, (SELECT id FROM category WHERE type = 1 AND name = '卫浴' LIMIT 1), 4,
     'ZJ-BATH-002', '白屿', '雅黑恒温花洒套装', '恒温阀芯，多模式出水，雅黑耐指纹表面。',
     '套', '/guest/images/bath-product.png', 1899.00,
     JSON_OBJECT('type', JSON_ARRAY('花洒'), 'pit', JSON_ARRAY(), 'size', JSON_ARRAY('标准尺寸'),
                 'installation', JSON_ARRAY('壁挂式'), 'flush', JSON_ARRAY(),
                 'function', JSON_ARRAY('恒温', '多模式出水')),
     '安装前需确认冷热水接口间距、水压与墙体条件。', 1, 80, 1, 1),
    (5, (SELECT id FROM category WHERE type = 1 AND name = '木地板' LIMIT 1), 5,
     'ZJ-FLOOR-001', '森序', '橡木原色三层实木地板', '稳定三层结构，自然橡木纹理。',
     '㎡', '/guest/images/floor-product.png', 328.00,
     JSON_OBJECT('material', JSON_ARRAY('三层实木'), 'thickness', JSON_ARRAY('15mm'),
                 'color', JSON_ARRAY('原木色'), 'lock', JSON_ARRAY('锁扣'),
                 'heating', JSON_ARRAY('适配地暖'), 'finish', JSON_ARRAY('哑光', '柔光')),
     '自然橡木纹理与锁扣结构，铺装前需确认基层平整度和完成面高度。', 1, 100, 1, 1),
    (6, (SELECT id FROM category WHERE type = 1 AND name = '木地板' LIMIT 1), 6,
     'ZJ-FLOOR-002', '森序', '胡桃木色强化复合地板', '耐磨易打理，适合卧室和书房。',
     '㎡', '/guest/images/floor-product.png', 169.00,
     JSON_OBJECT('material', JSON_ARRAY('强化复合'), 'thickness', JSON_ARRAY('10mm'),
                 'color', JSON_ARRAY('胡桃木色', '浅胡桃色'), 'lock', JSON_ARRAY('锁扣'),
                 'heating', JSON_ARRAY('需咨询'), 'finish', JSON_ARRAY('耐磨面')),
     '强化复合结构与耐磨表面，购买前需核对包装面积与铺装损耗。', 1, 70, 1, 1),
    (7, (SELECT id FROM category WHERE type = 1 AND name = '橱柜' LIMIT 1), 7,
     'ZJ-CABINET-001', '木衡', '奶油白一字型定制橱柜', '按延米计价，多层板柜体与隐藏式拉手。',
     '延米', '/guest/images/cabinet-product.png', 1599.00,
     JSON_OBJECT('door', JSON_ARRAY('平板门'), 'board', JSON_ARRAY('多层板'),
                 'counter', JSON_ARRAY('待选台面', '石英石'), 'color', JSON_ARRAY('奶油白'),
                 'layout', JSON_ARRAY('一字型'), 'pricing', JSON_ARRAY('按延米')),
     '基础价格按延米展示，最终组合需结合现场测量、柜体、门板与台面确认。', 1, 100, 1, 1),
    (8, (SELECT id FROM category WHERE type = 1 AND name = '橱柜' LIMIT 1), 8,
     'ZJ-CABINET-002', '木衡', '岩板台面现代橱柜', '岩板台面搭配高柜收纳系统，按延米计价。',
     '延米', '/guest/images/cabinet-product.png', 2299.00,
     JSON_OBJECT('door', JSON_ARRAY('平板门'), 'board', JSON_ARRAY('多层板'),
                 'counter', JSON_ARRAY('岩板'), 'color', JSON_ARRAY('暖灰'),
                 'layout', JSON_ARRAY('一字型'), 'pricing', JSON_ARRAY('按延米')),
     '岩板台面与高柜收纳组合，最终价格需结合布局、五金和现场尺寸确认。', 1, 90, 1, 1)
ON DUPLICATE KEY UPDATE
    category_id = VALUES(category_id), legacy_dish_id = VALUES(legacy_dish_id),
    brand_name = VALUES(brand_name), name = VALUES(name), subtitle = VALUES(subtitle),
    unit = VALUES(unit), main_image = VALUES(main_image), original_price = VALUES(original_price),
    attributes_json = VALUES(attributes_json), detail_description = VALUES(detail_description),
    status = VALUES(status), sort = VALUES(sort), update_user = VALUES(update_user);

INSERT INTO product_sku
    (id, product_id, sku_code, sku_name, spec_json, sale_price, original_price,
     stock, locked_stock, status, sort, version, create_user, update_user)
VALUES
    (1001, (SELECT id FROM product WHERE product_code = 'ZJ-TILE-001'), 'ZJ-TILE-001-800-SOFT', '800×800mm 柔光', JSON_OBJECT('尺寸', '800×800mm', '表面', '柔光'), 89.00, 109.00, 240, 0, 1, 20, 0, 1, 1),
    (1002, (SELECT id FROM product WHERE product_code = 'ZJ-TILE-001'), 'ZJ-TILE-001-800-MATT', '800×800mm 哑光', JSON_OBJECT('尺寸', '800×800mm', '表面', '哑光'), 92.00, 112.00, 160, 0, 1, 10, 0, 1, 1),
    (1003, (SELECT id FROM product WHERE product_code = 'ZJ-TILE-002'), 'ZJ-TILE-002-750', '750×1500mm 哑光', JSON_OBJECT('尺寸', '750×1500mm', '表面', '哑光'), 128.00, 158.00, 180, 0, 1, 20, 0, 1, 1),
    (1004, (SELECT id FROM product WHERE product_code = 'ZJ-TILE-002'), 'ZJ-TILE-002-900', '900×1800mm 哑光', JSON_OBJECT('尺寸', '900×1800mm', '表面', '哑光'), 168.00, 198.00, 90, 0, 1, 10, 0, 1, 1),
    (1005, (SELECT id FROM product WHERE product_code = 'ZJ-BATH-001'), 'ZJ-BATH-001-305', '305mm 坑距', JSON_OBJECT('坑距', '305mm', '安装方式', '落地式'), 3299.00, 3699.00, 12, 0, 1, 20, 0, 1, 1),
    (1006, (SELECT id FROM product WHERE product_code = 'ZJ-BATH-001'), 'ZJ-BATH-001-400', '400mm 坑距', JSON_OBJECT('坑距', '400mm', '安装方式', '落地式'), 3299.00, 3699.00, 8, 0, 1, 10, 0, 1, 1),
    (1007, (SELECT id FROM product WHERE product_code = 'ZJ-BATH-002'), 'ZJ-BATH-002-BLACK', '雅黑恒温款', JSON_OBJECT('颜色', '雅黑', '功能', '恒温'), 1599.00, 1899.00, 24, 0, 1, 20, 0, 1, 1),
    (1008, (SELECT id FROM product WHERE product_code = 'ZJ-BATH-002'), 'ZJ-BATH-002-GRAY', '枪灰恒温款', JSON_OBJECT('颜色', '枪灰', '功能', '恒温'), 1659.00, 1959.00, 18, 0, 1, 10, 0, 1, 1),
    (1009, (SELECT id FROM product WHERE product_code = 'ZJ-FLOOR-001'), 'ZJ-FLOOR-001-MATT', '1900×190×15mm 哑光', JSON_OBJECT('规格', '1900×190×15mm', '表面', '哑光'), 268.00, 328.00, 320, 0, 1, 20, 0, 1, 1),
    (1010, (SELECT id FROM product WHERE product_code = 'ZJ-FLOOR-001'), 'ZJ-FLOOR-001-SOFT', '1900×190×15mm 柔光', JSON_OBJECT('规格', '1900×190×15mm', '表面', '柔光'), 278.00, 338.00, 200, 0, 1, 10, 0, 1, 1),
    (1011, (SELECT id FROM product WHERE product_code = 'ZJ-FLOOR-002'), 'ZJ-FLOOR-002-WALNUT', '1215×195×10mm 胡桃木色', JSON_OBJECT('规格', '1215×195×10mm', '颜色', '胡桃木色'), 139.00, 169.00, 360, 0, 1, 20, 0, 1, 1),
    (1012, (SELECT id FROM product WHERE product_code = 'ZJ-FLOOR-002'), 'ZJ-FLOOR-002-LIGHT', '1215×195×10mm 浅胡桃色', JSON_OBJECT('规格', '1215×195×10mm', '颜色', '浅胡桃色'), 145.00, 175.00, 280, 0, 1, 10, 0, 1, 1),
    (1013, (SELECT id FROM product WHERE product_code = 'ZJ-CABINET-001'), 'ZJ-CABINET-001-BASE', '多层板柜体+平板门', JSON_OBJECT('柜体', '多层板', '门板', '平板门', '台面', '待选'), 1299.00, 1599.00, 30, 0, 1, 20, 0, 1, 1),
    (1014, (SELECT id FROM product WHERE product_code = 'ZJ-CABINET-001'), 'ZJ-CABINET-001-QUARTZ', '多层板柜体+石英石台面', JSON_OBJECT('柜体', '多层板', '门板', '平板门', '台面', '石英石'), 1599.00, 1899.00, 20, 0, 1, 10, 0, 1, 1),
    (1015, (SELECT id FROM product WHERE product_code = 'ZJ-CABINET-002'), 'ZJ-CABINET-002-12', '12mm 岩板台面', JSON_OBJECT('柜体', '多层板', '台面', '12mm岩板'), 1899.00, 2299.00, 18, 0, 1, 20, 0, 1, 1),
    (1016, (SELECT id FROM product WHERE product_code = 'ZJ-CABINET-002'), 'ZJ-CABINET-002-15', '15mm 岩板台面', JSON_OBJECT('柜体', '多层板', '台面', '15mm岩板'), 2099.00, 2499.00, 12, 0, 1, 10, 0, 1, 1)
ON DUPLICATE KEY UPDATE
    product_id = VALUES(product_id), sku_name = VALUES(sku_name), spec_json = VALUES(spec_json),
    sale_price = VALUES(sale_price), original_price = VALUES(original_price),
    stock = VALUES(stock), locked_stock = VALUES(locked_stock), status = VALUES(status),
    sort = VALUES(sort), update_user = VALUES(update_user);
