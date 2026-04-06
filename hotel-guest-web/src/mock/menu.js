export const categories = [
  { id: "signature", name: "招牌推荐" },
  { id: "seasonal", name: "时令精选" },
  { id: "wine", name: "名酒饮品" },
  { id: "dessert", name: "甜品轻食" }
];

export const dishes = [
  {
    id: "d1",
    categoryId: "signature",
    name: "黑松露海盐和牛粒",
    price: 198,
    intro: "低温慢煎和牛粒，黑松露香气层层展开，口感丰润细腻。",
    description: "精选和牛搭配黑松露与海盐，适合作为晚宴主菜或客房夜宵的精致首选。",
    ingredients: "和牛、黑松露、海盐、黄油、迷迭香",
    flavor: "鲜香微咸，肉汁饱满",
    notice: "建议趁热食用，可在备注中选择熟度偏好。",
    image: "linear-gradient(135deg, #152238, #2c4464)",
    tags: ["招牌", "主厨推荐", "高蛋白"]
  },
  {
    id: "d2",
    categoryId: "signature",
    name: "香槟汁煎鳕鱼",
    price: 168,
    intro: "香槟汁轻盈提鲜，鳕鱼肉质细嫩，适合轻奢晚餐。",
    description: "以香槟汁点缀香煎鳕鱼，入口清雅不过分厚重，保留海鲜本味。",
    ingredients: "鳕鱼、香槟汁、白芦笋、柠檬皮",
    flavor: "清新鲜甜",
    notice: "海鲜过敏人群请谨慎选择。",
    image: "linear-gradient(135deg, #d4c2a1, #6c7f9d)",
    tags: ["招牌", "不含酒精", "时令"]
  },
  {
    id: "d3",
    categoryId: "seasonal",
    name: "瑶柱菌菇炖盅",
    price: 128,
    intro: "温润鲜香，适合作为晚间滋补汤品。",
    description: "瑶柱与多种菌菇文火慢炖，汤底澄澈回甘，口感细致温和。",
    ingredients: "瑶柱、羊肚菌、竹荪、虫草花",
    flavor: "鲜润温和",
    notice: "建议搭配主食或招牌热菜一同享用。",
    image: "linear-gradient(135deg, #60708a, #e4d8c3)",
    tags: ["时令", "滋补", "不含酒精"]
  },
  {
    id: "d4",
    categoryId: "seasonal",
    name: "微辣龙虾意面",
    price: 158,
    intro: "番茄底味清透，带一点微辣提鲜，适合分享。",
    description: "龙虾肉搭配手工意面，番茄与香草风味平衡，轻奢而不厚重。",
    ingredients: "龙虾、手工意面、番茄、罗勒、橄榄油",
    flavor: "微辣酸甜",
    notice: "可在备注中填写少辣、去蒜或面条软硬度偏好。",
    image: "linear-gradient(135deg, #7b2433, #e5bea1)",
    tags: ["微辣", "海鲜", "招牌"]
  },
  {
    id: "d5",
    categoryId: "wine",
    name: "零度白桃气泡饮",
    price: 48,
    intro: "白桃果香清雅，细腻气泡提升用餐仪式感。",
    description: "无酒精气泡饮，适合搭配晚餐与甜品，也适合儿童饮用。",
    ingredients: "白桃汁、气泡水、薄荷",
    flavor: "清甜爽口",
    notice: "不含酒精，可作为客房送餐常备饮品。",
    image: "linear-gradient(135deg, #8aa4b8, #f6ead7)",
    tags: ["不含酒精", "清爽", "人气"]
  },
  {
    id: "d6",
    categoryId: "wine",
    name: "珍藏白葡萄酒单杯",
    price: 88,
    intro: "果香柔和，入口圆润，适合海鲜与清淡主菜。",
    description: "精选白葡萄酒单杯，适合搭配鳕鱼、菌菇盅等清雅菜品。",
    ingredients: "精选葡萄酒",
    flavor: "果香柔和",
    notice: "含酒精，请酌情饮用。",
    image: "linear-gradient(135deg, #b09b69, #ece5d4)",
    tags: ["精品", "含酒精", "单杯"]
  },
  {
    id: "d7",
    categoryId: "dessert",
    name: "香草云朵舒芙蕾",
    price: 78,
    intro: "口感轻盈蓬松，香草气息温柔细腻。",
    description: "现烤舒芙蕾搭配香草奶油与当季莓果，适合作为晚餐收尾。",
    ingredients: "鸡蛋、香草荚、奶油、莓果",
    flavor: "香甜轻盈",
    notice: "制作时间约 20 分钟。",
    image: "linear-gradient(135deg, #efddcf, #6b7a94)",
    tags: ["甜品", "现烤", "时令"]
  },
  {
    id: "d8",
    categoryId: "dessert",
    name: "海盐焦糖布丁",
    price: 58,
    intro: "海盐平衡甜感，入口丝滑顺口。",
    description: "焦糖布丁搭配少量海盐与坚果碎，层次丰富，适合作为轻奢甜品。",
    ingredients: "牛乳、鸡蛋、焦糖、海盐",
    flavor: "香甜顺滑",
    notice: "含乳制品，如有忌口请提前备注。",
    image: "linear-gradient(135deg, #cda46e, #23324b)",
    tags: ["甜品", "人气", "经典"]
  }
];

export const serviceInfo = {
  phone: "400-188-8899",
  notes: [
    "客房送餐时间：07:00 - 23:30",
    "餐厅堂食请以现场桌台号为准。",
    "如有过敏源或特殊饮食需求，请在订单备注中提前说明。",
    "如需加急，请在订单备注中填写“加急”，服务团队会优先查看。"
  ]
};
