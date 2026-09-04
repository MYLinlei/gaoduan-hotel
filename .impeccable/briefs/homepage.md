# 筑家优选用户端首页｜已确认设计 Brief

## 目标

将原酒店欢迎屏替换为可直接浏览和交易的家装建材商城首页。首页首先服务普通家庭消费者，同时以明确规格、计价单位和分类入口兼顾设计师与装修公司采购者。

## 已批准构图

- 采用 [方案 B：建筑目录横向索引](../mocks/homepage-comp-b.png)。
- 批准日期：2026-09-02。
- 本轮只实施桌面端，重点验证 1440px；移动端结构与 390px 验证留待后续。
- 页面不显示“演示信息”等提示字样。未经确认的具体配送范围、服务天数、资质与保障承诺不进入页面。

## 信息架构

1. 顶部服务信息条只提供中性的配送安装、售后咨询等入口，不写未经确认的具体承诺。
2. 桌面端全局导航：品牌、商品分类、搜索、优惠券、订单、账户、购物车。
3. 移动端顶部搜索入口与底部主要导航（后续范围）。
4. 首屏主视觉：一站式选购主张、真实感空间与材料、选购建材和领取优惠券行动。
5. 四大品类的不对称编辑式图文网格。
6. 限量优惠券：面额、门槛、有效期、剩余状态、领取操作。
7. 多类目热销榜：标签切换、名次、商品、计价单位和销量状态。
8. 按空间选材：客厅、厨房、卫生间、卧室。
9. 精选商品网格及完整商品状态。
10. 选材指南、配送安装与售后购买帮助。
11. 演示合作品牌与完整页脚。

## 视觉方向

“建筑材料样本册 × 当代家居编辑杂志”。画面像建筑师桌上的材料版与一本克制的家居刊物共同变成可交易界面：大幅真实感摄影、留白、细线分隔、材料标签、清晰价格与规格。石灰白、暖灰、炭黑构成基底，陶土棕为唯一强调色。

## 首页组件树

- `HomePage`
  - `ServiceStrip`
  - `CommerceHeader`
    - `BrandMark`
    - `CategoryMenu`
    - `ProductSearch`
    - `HeaderActions`
  - `HomeHero`
  - `CategoryEditorialGrid`
    - `CategoryTile`
  - `CouponSection`
    - `CouponTicket`
  - `HotRankingSection`
    - `CategoryTabs`
    - `RankingList`
  - `RoomSelectionSection`
  - `FeaturedProductsSection`
    - `ProductCard`
    - `ContentState`
  - `BuyingGuideSection`
  - `DemoBrandStrip`
  - `CommerceFooter`
  - `MobileBottomNav`

## 数据字段与契约

- `Category`: `id`, `name`, `slug`, `description`, `image`, `route`, `materialNote`。
- `Product`: `id`, `categoryId`, `brand`, `name`, `sellingPoint`, `specs[]`, `image`, `originalPrice`, `salePrice`, `unit`, `rank`, `salesLabel`, `promotion`, `stockStatus`, `status`。
- `Coupon`: `id`, `title`, `amount`, `threshold`, `validFrom`, `validTo`, `remainingLabel`, `claimStatus`, `scope`, `demo`。
- `RankingGroup`: `category`, `items<ProductSummary>[]`, `updatedAt`, `demo`。
- `Room`: `id`, `name`, `description`, `image`, `categoryIds[]`, `route`。
- `Guide`: `id`, `title`, `summary`, `cover`, `route`, `readingTime`。
- 现有商品接口通过 compatibility adapter 映射为 `Product`；首页演示榜单、空间和品牌由集中 home catalog service 提供。

## 实现范围

- 重建首页与全局用户端导航、页脚和 design tokens。
- 建立本地生成素材、Logo、演示品牌、首页数据适配层和可复用组件。
- 保留并接通现有登录、商品、购物车、优惠券、订单与结算路径。
- 清除用户可见的酒店与餐饮文案，并使其他交易页继承新视觉系统。
- 完成构建、1440px 截图与有限检查。

## 不做事项

- 不改 Spring Boot 接口和数据库结构。
- 不实现真实排行榜后端、真实支付、物流或安装调度。
- 不把演示配送、安装和售后文本当成已生效经营承诺。
- 不加入大型 UI 框架，不做高并发优化，不迁移技术栈。
- 本轮不实现移动端页面与 390px 验证。
