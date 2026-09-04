const image = (name) => `${import.meta.env.BASE_URL}images/${name}.png`;

export const homeCategories = [
  {
    id: 1,
    name: "瓷砖",
    number: "01",
    description: "通体大理石瓷砖",
    specs: ["900 × 900 mm", "哑光面", "防滑 R10"],
    price: 128,
    unit: "㎡",
    image: image("tile-product")
  },
  {
    id: 2,
    name: "卫浴",
    number: "02",
    description: "一体式智能坐便器",
    specs: ["305 / 400 mm", "双水路", "即热冲洗"],
    price: 899,
    unit: "套",
    image: image("bath-product")
  },
  {
    id: 3,
    name: "木地板",
    number: "03",
    description: "三层实木复合地板",
    specs: ["1215 × 165 × 15 mm", "暖橡木", "锁扣安装"],
    price: 198,
    unit: "㎡",
    image: image("floor-product")
  },
  {
    id: 4,
    name: "橱柜",
    number: "04",
    description: "深胡桃木整体橱柜",
    specs: ["18 mm 多层板", "哑光饰面", "按延米计价"],
    price: 980,
    unit: "延米",
    image: image("cabinet-product")
  }
];

export const homeCoupons = [
  { id: "home-200", amount: 200, threshold: 3999, title: "全场建材券", validity: "领取后 15 天内有效", remaining: "余量充足" },
  { id: "home-500", amount: 500, threshold: 8999, title: "整屋选材券", validity: "领取后 15 天内有效", remaining: "即将领完" },
  { id: "home-1000", amount: 1000, threshold: 14999, title: "大额采购券", validity: "领取后 15 天内有效", remaining: "限量领取" }
];

const rankingSeed = {
  瓷砖: [
    ["素岩柔光大板砖", "750 × 1500 mm", 158, "㎡", "近期开单较多"],
    ["云灰通体大理石砖", "900 × 900 mm", 128, "㎡", "门店常选"],
    ["微水泥防滑地砖", "600 × 1200 mm", 139, "㎡", "适合客餐厅"]
  ],
  卫浴: [
    ["净界一体式智能坐便器", "305 / 400 mm", 899, "套", "近期开单较多"],
    ["悬浮岩板浴室柜", "900 mm", 1680, "套", "小户型常选"],
    ["恒温花洒三件套", "全铜主体", 759, "套", "套装选购"]
  ],
  木地板: [
    ["暖橡三层实木复合地板", "15 mm", 198, "㎡", "近期开单较多"],
    ["烟熏胡桃多层地板", "14 mm", 229, "㎡", "深色空间常选"],
    ["原木浅栎锁扣地板", "12 mm", 169, "㎡", "卧室常选"]
  ],
  橱柜: [
    ["深胡桃哑光整体橱柜", "18 mm 多层板", 980, "延米", "近期开单较多"],
    ["暖白平板门整体橱柜", "PET 饰面", 860, "延米", "小户型常选"],
    ["岩板岛台组合", "定制尺寸", 2980, "组", "开放厨房常选"]
  ]
};

export const homeRankings = Object.fromEntries(
  Object.entries(rankingSeed).map(([category, entries]) => [
    category,
    entries.map(([name, spec, price, unit, status], index) => ({
      id: `${category}-${index + 1}`,
      rank: index + 1,
      name,
      spec,
      price,
      unit,
      status,
      image: homeCategories.find((item) => item.name === category)?.image
    }))
  ])
);

export const homeRooms = [
  { name: "客厅", description: "地砖 · 木地板 · 收纳", image: image("hero-space"), categoryId: 1 },
  { name: "厨房", description: "橱柜 · 墙地砖 · 五金", image: image("cabinet-product"), categoryId: 4 },
  { name: "卫生间", description: "卫浴 · 防滑砖 · 浴室柜", image: image("bath-product"), categoryId: 2 },
  { name: "卧室", description: "木地板 · 暖色材料", image: image("floor-product"), categoryId: 3 }
];

export const featuredProducts = homeCategories.map((item, index) => ({
  id: item.id,
  brand: ["砚川", "白屿", "森序", "木衡"][index],
  name: item.description,
  intro: ["低饱和石纹，适合客餐厅连续铺贴", "简洁一体外观，适配多种坑距", "自然橡木色，锁扣铺装更利落", "深木色平板门，适合开放式厨房"][index],
  specs: item.specs,
  price: item.price,
  originalPrice: [168, 1099, 239, 1180][index],
  unit: item.unit,
  image: item.image,
  tags: ["本期精选", index === 0 ? "柔光" : index === 1 ? "套装" : index === 2 ? "锁扣" : "定制"],
  stockStatus: "可购买"
}));

export const partnerBrands = ["砚川 YANCHUAN", "白屿 BAIYU", "森序 SENXU", "木衡 MUHENG", "石见 SHIJIAN"];

export function getHomeCatalog() {
  return Promise.resolve({
    categories: homeCategories,
    coupons: homeCoupons,
    rankings: homeRankings,
    rooms: homeRooms,
    featuredProducts,
    partnerBrands
  });
}
