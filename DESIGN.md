---
name: "筑家优选"
description: "建筑材料样本册与当代家居编辑杂志共同构成的桌面端交易系统"
colors:
  canvas: "#f7f6f3"
  paper: "#ffffff"
  warm-surface: "#eeeae4"
  ink: "#211f1c"
  muted-ink: "#69645e"
  divider: "#d8d3cc"
  terracotta: "#a95837"
  terracotta-deep: "#814128"
  terracotta-soft: "#f2e8e1"
  danger: "#9b352d"
  success: "#3e5c49"
  footer: "#282520"
typography:
  display:
    fontFamily: "Songti SC, STSong, Noto Serif CJK SC, serif"
    fontSize: "clamp(46px, 3.5vw, 56px)"
    fontWeight: 600
    lineHeight: 1.14
    letterSpacing: "-0.025em"
  headline:
    fontFamily: "Songti SC, STSong, Noto Serif CJK SC, serif"
    fontSize: "34px"
    fontWeight: 600
    lineHeight: 1.2
  title:
    fontFamily: "PingFang SC, Noto Sans CJK SC, Microsoft YaHei, sans-serif"
    fontSize: "18px"
    fontWeight: 600
    lineHeight: 1.4
  body:
    fontFamily: "PingFang SC, Noto Sans CJK SC, Microsoft YaHei, sans-serif"
    fontSize: "14px"
    fontWeight: 400
    lineHeight: 1.7
  label:
    fontFamily: "PingFang SC, Noto Sans CJK SC, Microsoft YaHei, sans-serif"
    fontSize: "12px"
    fontWeight: 400
    lineHeight: 1.4
    letterSpacing: "0.14em"
rounded:
  sm: "3px"
  md: "4px"
  lg: "6px"
  xl: "8px"
  pill: "99px"
spacing:
  xs: "4px"
  sm: "8px"
  md: "14px"
  lg: "20px"
  xl: "28px"
  section: "70px"
  gutter: "clamp(28px, 3vw, 52px)"
components:
  button-primary:
    backgroundColor: "{colors.ink}"
    textColor: "{colors.paper}"
    rounded: "{rounded.sm}"
    padding: "11px 18px"
    height: "44px"
  button-primary-hover:
    backgroundColor: "{colors.terracotta-deep}"
    textColor: "{colors.paper}"
  button-accent:
    backgroundColor: "{colors.terracotta}"
    textColor: "{colors.paper}"
    rounded: "{rounded.sm}"
    padding: "11px 28px"
    height: "54px"
  field:
    backgroundColor: "{colors.paper}"
    textColor: "{colors.ink}"
    rounded: "{rounded.sm}"
    padding: "13px 14px"
    height: "44px"
---

# Design System: 筑家优选

## Overview

**Creative North Star: "门店选材长桌"**

这是一套桌面端先行的“建筑材料样本册 × 当代家居编辑杂志”系统：用材料大图建立空间感，用细线、基线与规格标签建立专业秩序，再让价格、计价单位和购买动作拥有最快的扫读路径。温暖来自石灰白、暖灰、陶土棕与木石摄影，不来自饱和促销色或装饰渐变。

交易页保留同一材料感，但降低图像比重；信息越密，排版与状态越克制。分类、详情与优惠券页是“门店选材工作台”，选材指南是共享同一基线的“材料手册”。新屏幕必须继续优先展示品牌、材质、规格、库存状态、价格和单位，不把内容包装成通用电商卡片墙。

**Key Characteristics:**

- 宽幅、不对称的编辑网格，以细线而非浮起卡片分区。
- 陶土棕是唯一品牌强调，炭黑承担高对比交易操作。
- 中文宋体标题搭配清晰无衬线正文，价格使用等宽数字特性。
- 产品图、空间图、规格和计价方式共同作为内容证据。
- 二级页面以筛选账本、规格基线和可复用状态组件提升交易密度。

## Colors

色彩像暖光下的样本台：石灰色表面承托内容，炭黑提供结构，陶土棕只标记关键动作、当前状态和价格。

### Primary

- **陶土棕**：主行动、价格、选中标记和数量徽标。深色变体用于悬停及更高文字对比，浅色变体只作选择与步进器背景。

### Neutral

- **石灰画布 / 白纸 / 暖表面**：分别用于页面底、主内容面和低优先层。
- **炭黑 / 柔墨**：分别用于正文与主操作、辅助文字。
- **温灰分隔线**：承担网格、边界和表单描边，是系统的主要层级工具。

**The One Clay Voice Rule.** 除危险与成功状态外，不增加第二高饱和品牌色；陶土棕的稀缺性就是它的强度。

## Typography

**Display Font:** Songti SC（STSong、Noto Serif CJK SC 后备）  
**Body Font:** PingFang SC（Noto Sans CJK SC、Microsoft YaHei 后备）

**Character:** 宋体只负责编辑层级和价格的书刊气质；导航、规格、状态与表单坚持无衬线，保持交易信息的清晰度。

### Hierarchy

- **Display:** 首屏标题，限短句与少量换行。
- **Headline:** 主区块标题，与说明和查看全部链接形成水平基线。
- **Title:** 商品名、优惠券名与容器标题。
- **Body:** 功能性正文，默认宽松行高，避免大段说明压过规格与价格。
- **Label:** 品牌、眉题、规格和微型状态；字号小时用字距而非额外色彩建立层级。

**The Serif Reserve Rule.** 不在长段正文或密集表单中滥用宋体；仅首屏主行动可作为按钮中有意的例外。

## Layout

当前实现为桌面端专用：`html` 最小宽度为 1120px，首页主容器上限为 1440px，二级页主容器上限为 1340px，两侧间距由流式 gutter 保持比例。首屏是 65/35 图文分栏；品类与精选商品使用四列，优惠与榜单使用等分双列，空间入口用不对称拼图。大区块以约 70–76px 的垂直留白与单像素分隔线保持编辑节奏。

分类页使用已验证的 **A1 主体 + A2 首行商品后指南条 + A3 规格基线**：180px 紧凑品类简介后，主工作区为 236px 筛选账本、14px 沟槽和三列商品结果。首行三件商品后插入一次横向指南，下方再继续商品、估算器与分页；商品规格表使用固定最小高度，保证同行价格、状态与操作对齐。详情页首区为 55/45 图文结构，右侧购买决策面板粘性停靠，之后才是规格、工艺、安装、配送与同品类商品。

1440px 桌面视口已完成最终截图验证；该结论不外推到其他设备类型。

**Mobile is explicitly deferred.** 本轮不把页面压成手机端、不保证 390px 无溢出，也不将现有局部小屏媒体查询视为完成的移动系统。后续需单独重构导航、首屏、四列商品、拼图、购物车和底部操作，并在 390px 重新验收。

## Elevation & Depth

系统默认是平面的：白纸、暖表面、炭黑反相区与细边框划分层级，卡片静止时不使用阴影。深度主要来自图像裁切、区块色调和覆盖层；购物车与登录对话框使用半透明炭黑遮罩建立模态关系。基础样式保留的两个环境阴影令牌不是默认卡片效果。

**The Flat Sample Rule.** 静态内容面先用色调、线条和裁切建立空间；只有真正浮在页面上方的临时层才能获得额外深度。

## Shapes

表面接近直角，3–8px 的轻微圆角仅用于按钮、输入、对话框与需要触感的交互容器。商品卡以顶部炭黑线与方形图像开始；大型区块和导航不包装成圆角卡片。胶囊仅限标签和状态，不用于普通行动。图标统一为 21px 左右、1.65px 描边的内联线性 SVG。

## Components

### Header & Navigation

三层桌面导航由 32px 服务条、88px 交易主栏和 48px 品类导航组成。品牌、分类、搜索、优惠券、订单、账户和购物车同屏常驻；活动路由陶土文字与 2px 底线标记。

### Buttons, Fields & States

主按钮是炭黑实心面，重要首页行动可用陶土棕；次按钮使用暖灰面与细边框，幽灵按钮使用白纸面。互动控件默认至少 44px 高；禁用态降低透明度。字段使用白纸底和温灰边框，聚焦时切换为陶土边框和克制的 3px 色晕。

### Product, Coupon & Ranking Patterns

商品卡顺序固定为图像、品牌、名称、卖点、三行规格基线、价格/单位、可用状态、操作。`PriceUnit` 集中处理价格、单位和可选原价；`ProductGrid` 在二级页为三列细线网格，与 `ProductCard` 的内部规格高度共同完成 A3 对齐。榜单仍使用紧凑行与单像素分隔，不包装成浮起卡片。

### Category Workspace

`CategoryPageShell` 固定面包屑、品类标题/说明与材料局部图；`FilterSidebar` 组织可清空的筛选账本，`FilterGroup` 用 `fieldset`/`legend`、`aria-expanded` 和原生复选框表达分组与展开状态。`ResultToolbar` 并置结果数、可单独移除的已选条件、清空与排序；这些组件不承担请求或 URL 解析。

### Product Detail Decisions

`ProductGallery` 必须诚实反映图源：当 adapter 只有一张商品图时，只显示单张大图，不生成伪缩略图、视角或轮播控件；仅在多图存在时开启前后切换、键盘方向/Home/End 导航与缩略图 tablist。`SkuSelector` 用 `aria-pressed` 按钮组织规格，未选完时以 `role="alert"` 阻止提交。`QuantityEstimator` 按品类分别提供瓷砖/地板面积与损耗、卫浴安装条件、橱柜延米估算，所有结果都明示为参考。详情底部仅请求当前品类的其他商品，排除当前 ID，最多显示三件。

### Coupon Tickets

`CouponTicket` 是双列优惠中心里的三区采购凭证：金额/门槛、名称/适用范围/有效期/可展开规则、状态动作。状态为 `available`、`claimed`、`upcoming`、`expired` 或 `sold-out`；仅 `available` 允许领取，其余状态使用禁用按钮，过期与抢完进一步降低饱和度。页面以 `aria-live`/`role="status"` 回传列表和领取结果。

### Guide & Content States

`GuideIndex` 是四列手册目录，链接空间、品类、估算和交付区块；`FaqAccordion` 默认展开第一项，用 `aria-expanded` 标记当前问题，同时只打开一项。`UnifiedContentState` 是商品、详情、相关商品和优惠券的统一加载/空/离线/失败界面：加载使用三列骨架与 polite live region，其他状态使用 assertive live region，仅离线和失败提供“重新加载”。

### Dialogs & Accessibility

登录面板与购物车侧抽屉都是 Teleport 到 `body` 的模态面，使用 `role="dialog"`、`aria-modal="true"` 和可追踪标题。打开时保存原焦点、将焦点移入第一个可操作控件，Tab / Shift+Tab 在面板内循环，Escape 或点击遮罩关闭，关闭后将焦点还给原触发器。全局按钮、链接与字段共享 3px 陶土半透明 `:focus-visible` 轮廓；纯装饰图标对辅助技术隐藏，图像提供中文替代文本。

### Material Imagery

现有空间图与四类商品图都是保存在 `hotel-guest-web/public/images/` 的本地生成 PNG。通过 `import.meta.env.BASE_URL` 组装路径，首图优先加载，非首屏图片延迟加载并用 `object-fit: cover` 裁切。继续使用低饱和、自然侧光、可辨识材质和尺度的真实感摄影；不允许远程图片热链。

### Data & Service Boundaries

视图只消费组件 props、Pinia 状态或明确的 service adapter。登录、购物车、优惠券、订单与营业状态通过 stores 调用共享 `api/http.js` 并保留现有 Spring Boot 合约；首页示例数据仍集中在 `services/homeCatalog.js`。

二级页的边界是：`catalogService.js` 负责品类配置、后端 category/dish 请求、商品/画廊/SKU/规格归一化、同品类与收藏；`couponService.js` 负责券范围、日期、可领状态和领取请求归一化；`guideContent.js` 集中维护手册索引、空间、流程、计价单位、交付规则与 FAQ。页面不得重复构造这些模型。商品 `availability` 只使用 `available | low-stock | sold-out | off-shelf`：已下架优先于库存判定，可见库存不大于零为已售罄，显式低库存标记为库存不足，其余才是可购买；只有前两种且门店可下单时开放购买操作。

分类页的品类、搜索、筛选、排序和分页均与 URL query 双向同步：多选值用 `|` 分隔，默认排序与第一页不写入 URL。品类可读取 `category`、`categoryKey` 或旧 `categoryId`，旧 `room` 映射为空间筛选；新链接写入可读品类名。商品详情使用 `/product/:id`，同时保留 `/dish/:id` 别名。后端 `dish` 等兼容命名可保留在路由与数据层，但不得渗透到用户可见文案。

### Motion

“翻开材料样本”是唯一主动作语法：首屏材料图以 820ms 柔和错位展开，图片悬停只做约 3% 的克制缩放，常规状态变化使用 180–200ms 缓出。`prefers-reduced-motion` 下取消平滑滚动，并将动画与过渡压缩到近乎即时，内容从不依赖动画才可见。

## Do's and Don'ts

### Do:

- **Do** 让材质、规格、价格和计价单位在购买动作前就可扫读。
- **Do** 复用全局令牌、线性 SVG、44px 最小操作高度与可见键盘焦点。
- **Do** 将新的首页示例数据放入 service adapter，将真实交易状态放入 Pinia/API 边界。
- **Do** 将新的生成材料图本地化，标注固有尺寸与意义明确的 alt。
- **Do** 继续用 `CategoryPageShell` 和组合组件构建品类页，保留 A1/A2/A3 节奏与参数对齐。
- **Do** 在 service adapter 内归一化商品、优惠券和错误状态，并保持 URL 筛选可刷新、可返回、可分享。

### Don't:

- **Don't** 引入蓝金酒店语义、玻璃拟态、随机渐变、Emoji 正式图标或无法追踪的远程图片。
- **Don't** 把所有内容都包装成同尺寸圆角卡片，也不用阴影取代基线、分隔线和留白。
- **Don't** 在当前轮次宣称移动端已支持；手机导航、网格、抽屉与底部动作需要一次独立的设计和验收。
- **Don't** 在 UI 中暴露“酒店、住客、点餐、菜品、打烊”等内部兼容文案，也不虚构已生效的品牌授权或服务承诺。
- **Don't** 在单图商品上伪造多图画廊，也不用虚构库存数量、销量、评分、产地或配送承诺填补空缺字段。
- **Don't** 绕过 catalog/coupon/guide adapter 在页面模板里写死转换、状态标签或兼容规则。
