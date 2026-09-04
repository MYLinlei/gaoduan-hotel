const image = (name) => `${import.meta.env.BASE_URL}images/${name}.png`;

export const guideIndex = [{ id: "rooms", label: "按空间选材" }, { id: "categories", label: "按品类学习" }, { id: "calculator", label: "计算用量" }, { id: "rules", label: "交付与售后" }];
export const roomGuides = [
  { name: "客厅", image: image("hero-space"), text: "先确定整体色调和采光，再协调瓷砖、木地板与收纳表面。", categories: "瓷砖 · 木地板 · 收纳" },
  { name: "厨房", image: image("cabinet-product"), text: "围绕动线、柜体布局、台面高度和墙地面清洁需求选材。", categories: "橱柜 · 墙地砖 · 五金" },
  { name: "卫生间", image: image("bath-product"), text: "优先核对坑距、给排水、湿区防滑和设备安装尺寸。", categories: "卫浴 · 防滑砖 · 浴室柜" },
  { name: "卧室", image: image("floor-product"), text: "结合脚感、完成面高度、色温与地暖条件选择地面材料。", categories: "木地板 · 暖色材料" }
];
export const purchaseSteps = [{ title: "测量空间", text: "记录长宽、门洞、转角、管线和设备预留位置。" }, { title: "确认规格", text: "核对尺寸、材质、工艺、安装方式和计价单位。" }, { title: "计算用量", text: "结合排版、损耗和包装规格复核购买数量。" }, { title: "确认交付", text: "下单前确认配送、安装、现场条件和售后咨询方式。" }];
export const priceUnits = [{ unit: "元/片", text: "按单片计价，需要结合单片尺寸和铺装面积计算数量。" }, { unit: "元/㎡", text: "按面积计价，实际购买量通常还需考虑合理损耗。" }, { unit: "元/套", text: "按一套商品或组合计价，下单前应核对套内包含的具体部件。" }, { unit: "元/延米", text: "常见于橱柜基础报价，实际组合还与柜体、门板、台面和布局有关。" }];
export const serviceRules = [{ title: "配送确认", text: "配送安排以订单确认信息为准；大件材料下单前应核对收货环境。" }, { title: "安装条件", text: "需要安装的商品应提前确认给排水、电源、墙体和现场尺寸。" }, { title: "收货核对", text: "到货时建议核对商品名称、规格、数量、外观和包装状态。" }, { title: "售后咨询", text: "如需处理问题，请准备订单信息、商品照片和现场情况说明。" }];
export const faqItems = [{ question: "瓷砖和木地板应该买多少？", answer: "先测量实际铺装面积，再结合排版方式、损耗率和商品包装规格估算。计算器提供初步参考，最终数量应以现场复核为准。" }, { question: "卫浴商品下单前最重要的尺寸是什么？", answer: "坐便器重点核对坑距，浴室柜和花洒需要核对墙面、给排水、电源、门扇与使用空间。" }, { question: "橱柜的延米价格包含所有配置吗？", answer: "延米通常用于理解基础报价，柜体、门板、台面、五金和布局组合仍需以具体商品与订单确认为准。" }, { question: "页面价格是否可以直接下单？", answer: "平台商品按页面标价进入下单流程；涉及规格组合的商品，请先完成规格选择并核对订单信息。" }];
