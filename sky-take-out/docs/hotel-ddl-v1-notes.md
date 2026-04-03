# 高端酒店定制数据库说明（V1）

## 目标

这份 DDL 不是一次性把所有业务都做完，而是先把后续 3 个阶段需要的数据库模型定下来：

- 第 3 阶段：订单状态机拆分堂食 / 外卖
- 第 4 阶段：高端优惠券与秒杀
- 第 5 阶段：菜品种草 / 评价 / 点赞 / 收藏
- 第 6 阶段：自有骑手与后台管理

对应 SQL 文件：

- [hotel-ddl-v1.sql](/D:/JAVASTUDY/take_out-heima/sky-take-out/docs/hotel-ddl-v1.sql)

## 设计原则

- 仍然坚持单酒店模型，不引入多商户
- 只在现有 `dish` 和 `orders` 上扩展，不推翻原表
- 优惠券必须支持全店、类目、单菜品三种作用域
- 骑手只用于外卖订单
- 堂食只用桌号，不使用地址

## 本次变更概览

### 扩展表

- `dish`
- `orders`

### 新增表

- `hotel_table`
- `hotel_rider`
- `hotel_high_voucher`
- `hotel_high_voucher_order`
- `dish_note`
- `dish_comment`
- `dish_like`
- `dish_favorite`

## 关键字段说明

### `dish`

新增字段用于支撑高端展示和推荐：

- `tag_type`
- `luxury_level`
- `recommend_weight`
- `like_count`
- `favorite_count`
- `note_count`
- `comment_count`
- `score`

这意味着下一步 Java 改造时，至少要同步更新：

- `Dish`
- `DishDTO`
- `DishVO`
- `DishMapper.xml`
- 管理端菜品编辑接口

### `orders`

新增字段用于支撑双模式订单：

- `order_type`
- `table_no`
- `rider_id`
- `delivery_zone_code`
- `coupon_id`
- `coupon_amount`
- `actual_pay_amount`

下一步 Java 改造时至少要同步更新：

- `Orders`
- `OrdersSubmitDTO`
- `OrderVO`
- `OrdersMapper.xml`
- 用户端下单逻辑
- 后台订单流转逻辑

## 优惠券模型说明

### 为什么不用单一 `dish_id`

如果只绑 `dish_id`，后续会很快受限，无法同时支持：

- 全店高端券
- 酒水专用券
- 宴会券
- 单菜品券

因此这里采用：

- `scope_type`
- `scope_id`

推荐规则：

- `ALL_STORE`：全店券，`scope_id` 为空
- `CATEGORY`：类目券，比如名酒类目
- `DISH`：单菜品券

## 订单与券的关系

当前设计里：

- `orders.coupon_id` 表示订单使用的券
- `orders.coupon_amount` 表示抵扣金额
- `orders.actual_pay_amount` 表示最终实付金额

而券本身的领取 / 使用记录在：

- `hotel_high_voucher_order`

这样做的好处是：

- 一张券的生命周期独立可追踪
- 订单支付前后可以锁券 / 解锁 / 回滚
- 方便第 4 阶段接入秒杀和过期回收

## 订单模式建议

虽然这一步还没改订单状态机，但数据库已经为下一步预留了字段。

建议后续状态流如下：

### 外卖

- 待支付
- 已支付
- 待接单
- 已接单
- 待取餐
- 配送中
- 已送达
- 已完成
- 已取消

### 堂食

- 待支付
- 已支付
- 待制作
- 制作中
- 已上桌
- 已完成
- 已取消

## 与当前第一阶段代码的关系

第一阶段刚补回的是“原始外卖式用户交易链路”。  
这份 DDL 不会破坏第一阶段逻辑，但也还没有让第一阶段自动支持堂食 / 优惠券。

也就是说：

- 第一阶段代码可以继续跑现有外卖式下单
- 第二阶段先把表结构设计好
- 第三阶段再真正改订单状态机和下单参数

## 下一阶段改造顺序

接下来应当直接进入：

1. 更新 `Orders` / `OrdersSubmitDTO` / `OrderVO`
2. 修改 `orders` 下单逻辑，支持 `order_type`
3. 堂食下单时隐藏地址，改用 `table_no`
4. 外卖下单时继续沿用地址
5. 后台订单状态按 `order_type` 分流

## 注意事项

- 这份 SQL 是 V1 草案，正式执行前要先核对你本地现有表结构
- 如果当前库里 `orders.amount` 已表示实付金额，后续需统一口径
- 如果后面决定保留微信真实支付，这里的 `actual_pay_amount` 可以直接沿用
- 这一步没有引入外键约束，目的是降低初期迁移成本；后续稳定后可补
