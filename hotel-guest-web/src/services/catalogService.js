import { request } from "../api/http";

const image = (name) => `${import.meta.env.BASE_URL}images/${name}.png`;
const categoryImages = { tile: "tile-product", bath: "bath-product", floor: "floor-product", cabinet: "cabinet-product" };

// 旧 dish 接口回退时才使用；新 product 接口的字段始终优先。
const legacyMeta = {
  1: { brand: "砚川", unit: "㎡", specs: ["800 × 800 mm", "柔光面", "客餐厅"], filters: { space: ["客厅", "餐厅"], size: ["800×800mm"], finish: ["柔光"], color: ["灰色"], slip: ["R10"], texture: ["水泥纹"] } },
  2: { brand: "石见", unit: "㎡", specs: ["750 × 1500 mm", "通体工艺", "耐磨防滑"], filters: { space: ["客厅", "玄关"], size: ["750×1500mm"], finish: ["哑光"], color: ["白色"], slip: ["R10"], texture: ["大理石纹"] } },
  3: { brand: "白屿", unit: "套", specs: ["305 / 400 mm", "一体式", "即热冲洗"], filters: { type: ["智能坐便器"], pit: ["305mm", "400mm"], size: ["标准尺寸"], installation: ["落地式"], flush: ["虹吸式"], function: ["座圈加热", "即热冲洗"] } },
  4: { brand: "白屿", unit: "套", specs: ["恒温阀芯", "多模式出水", "雅黑饰面"], filters: { type: ["花洒"], size: ["标准尺寸"], installation: ["壁挂式"], function: ["恒温", "多模式出水"] } },
  5: { brand: "森序", unit: "㎡", specs: ["1900 × 190 × 15 mm", "三层实木", "地暖适配"], filters: { material: ["三层实木"], thickness: ["15mm"], color: ["原木色"], lock: ["锁扣"], heating: ["适配地暖"], finish: ["哑光"] } },
  6: { brand: "森序", unit: "㎡", specs: ["1215 × 195 × 10 mm", "强化复合", "耐磨易打理"], filters: { material: ["强化复合"], thickness: ["10mm"], color: ["胡桃木色"], lock: ["锁扣"], heating: ["需咨询"], finish: ["耐磨面"] } },
  7: { brand: "木衡", unit: "延米", specs: ["多层板柜体", "平板门", "一字型布局"], filters: { door: ["平板门"], board: ["多层板"], counter: ["待选台面"], color: ["奶油白"], layout: ["一字型"], pricing: ["按延米"] } },
  8: { brand: "木衡", unit: "延米", specs: ["岩板台面", "高柜收纳", "按延米计价"], filters: { door: ["平板门"], board: ["多层板"], counter: ["岩板"], color: ["暖灰"], layout: ["一字型"], pricing: ["按延米"] } }
};

const rawCategories = [
  {
    id: 1, key: "tile", name: "瓷砖", unit: "㎡",
    intro: "从空间、尺寸、表面工艺与防滑要求开始筛选，再核对铺贴面积和损耗。",
    guide: ["瓷砖选购提示", ["尺寸", "结合空间尺度和铺贴方式选择规格。"], ["表面", "柔光、哑光与亮光适合不同采光。"], ["防滑", "湿区需要重点核对防滑等级。"]],
    filters: [["space", "空间", ["客厅", "厨房", "卫生间", "阳台", "玄关"]], ["size", "尺寸", ["300×600mm", "600×600mm", "750×1500mm", "800×800mm", "900×900mm"]], ["finish", "表面工艺", ["亮光", "柔光", "哑光", "仿古面", "肌理面"]], ["color", "颜色", ["白色", "米色", "灰色", "棕色", "黑色"]], ["slip", "防滑等级", ["R9", "R10", "R11及以上"]], ["texture", "纹理", ["大理石纹", "水泥纹", "石材纹", "木纹", "纯色"]]]
  },
  {
    id: 2, key: "bath", name: "卫浴", unit: "套",
    intro: "先确认产品类型、坑距和现场安装尺寸，再比较冲水方式与使用功能。",
    guide: ["卫浴选购提示", ["坑距", "坐便器下单前必须核对排污口中心距。"], ["尺寸", "预留开门、检修和日常使用空间。"], ["安装", "确认给排水、电源与墙体条件。"]],
    filters: [["type", "类型", ["智能坐便器", "普通坐便器", "浴室柜", "花洒"]], ["pit", "坑距", ["305mm", "400mm"]], ["size", "尺寸", ["紧凑型", "标准尺寸", "大尺寸"]], ["installation", "安装方式", ["落地式", "壁挂式", "台上式", "台下式"]], ["flush", "冲水方式", ["虹吸式", "直冲式"]], ["function", "功能", ["恒温", "座圈加热", "即热冲洗", "多模式出水"]]]
  },
  {
    id: 3, key: "floor", name: "木地板", unit: "㎡",
    intro: "从材质、厚度、色调和地暖适配开始，结合铺装方式估算包装数量。",
    guide: ["木地板选购提示", ["材质", "结合预算、稳定性与脚感选择结构。"], ["厚度", "核对完成面高度和门扇预留。"], ["地暖", "地暖空间需确认产品适配说明。"]],
    filters: [["material", "材质", ["三层实木", "多层实木", "强化复合"]], ["thickness", "厚度", ["10mm", "12mm", "15mm", "18mm"]], ["color", "颜色", ["浅原木", "原木色", "胡桃木色", "深棕色"]], ["lock", "锁扣方式", ["平扣", "V型扣", "大锁扣", "锁扣"]], ["heating", "地暖适配", ["适配地暖", "需咨询"]], ["finish", "表面工艺", ["哑光", "柔光", "耐磨面", "手抓纹"]]]
  },
  {
    id: 4, key: "cabinet", name: "橱柜", unit: "延米",
    intro: "按布局和延米理解基础报价，再确认柜体、门板、台面与现场测量条件。",
    guide: ["橱柜选购提示", ["布局", "先按厨房尺寸确定一字型、L型或U型。"], ["板材", "柜体与门板需要分别核对材质。"], ["计价", "延米价格需结合实际组合进一步确认。"]],
    filters: [["door", "门板", ["平板门", "造型门", "玻璃门"]], ["board", "柜体板材", ["多层板", "颗粒板", "生态板"]], ["counter", "台面", ["岩板", "石英石", "不锈钢", "待选台面"]], ["color", "颜色", ["奶油白", "暖灰", "原木色", "深胡桃"]], ["layout", "布局", ["一字型", "L型", "U型", "岛台组合"]], ["pricing", "计价方式", ["按延米", "按组合"]]]
  }
];

export const categoryCatalog = rawCategories.map((item) => ({
  ...item,
  backendName: item.name,
  image: image(categoryImages[item.key]),
  guide: { title: item.guide[0], points: item.guide.slice(1).map(([title, text]) => ({ title, text })) },
  filters: item.filters.map(([key, label, options]) => ({ key, label, options }))
}));

export function getCategoryConfig(value) {
  const text = String(value || "");
  return categoryCatalog.find((item) => item.key === text || item.name === text || String(item.id) === text) || categoryCatalog[0];
}

function parseFlavorValues(flavor) {
  if (!flavor?.value) return [];
  try {
    const parsed = JSON.parse(flavor.value);
    return Array.isArray(parsed) ? parsed.map(String) : [String(parsed)];
  } catch {
    return String(flavor.value).split(/[,，/]/).map((item) => item.trim()).filter(Boolean);
  }
}

function categoryFromRaw(raw, categories = []) {
  if (raw.categoryName) return categoryCatalog.find((item) => item.name === raw.categoryName) || categoryCatalog[0];
  const backend = categories.find((item) => Number(item.id) === Number(raw.categoryId));
  return categoryCatalog.find((item) => item.name === backend?.name)
    || categoryCatalog[(Math.max(Number(raw.categoryId || 1), 1) - 1) % 4];
}

function normalizeValues(value) {
  if (Array.isArray(value)) return value.map(String).filter(Boolean);
  return value === undefined || value === null || value === "" ? [] : [String(value)];
}

function normalizeAttributes(raw, fallback = {}) {
  const source = raw && typeof raw === "object" && !Array.isArray(raw) ? raw : fallback;
  return Object.fromEntries(Object.entries(source || {}).map(([key, value]) => [key, normalizeValues(value)]));
}

function assetPath(value, fallback) {
  if (!value) return image(fallback);
  if (/^(https?:)?\/\//.test(value) || value.startsWith("data:") || value.startsWith("/")) return value;
  return `${import.meta.env.BASE_URL}${value.replace(/^\.\//, "")}`;
}

function parametersFrom(attributes, category) {
  const labels = Object.fromEntries(category.filters.map((item) => [item.key, item.label]));
  return Object.entries(attributes)
    .filter(([, values]) => values.length)
    .map(([key, values]) => ({ label: labels[key] || key, value: values.join(" / ") }));
}

function skusFrom(rawSkus = []) {
  const skus = rawSkus.map((sku) => ({
    id: sku.id,
    skuCode: sku.skuCode,
    name: sku.skuName,
    specs: sku.specs && typeof sku.specs === "object" ? sku.specs : {},
    salePrice: Number(sku.salePrice || 0),
    originalPrice: sku.originalPrice == null ? null : Number(sku.originalPrice),
    availableStock: Number(sku.availableStock || 0),
    status: Number(sku.status ?? 1)
  }));
  const labels = [...new Set(skus.flatMap((sku) => Object.keys(sku.specs)))];
  const groups = labels.map((label) => ({
    key: label,
    label,
    options: [...new Set(skus.map((sku) => sku.specs[label]).filter(Boolean).map(String))]
  })).filter((group) => group.options.length);
  return { skus, groups };
}

export function normalizeAvailability(raw = {}) {
  const explicit = String(raw.stockStatus || "").toLowerCase();
  if (["available", "low-stock", "sold-out", "off-shelf"].includes(explicit)) return explicit;
  if (Number(raw.status ?? 1) === 0) return "off-shelf";
  const stock = [raw.availableStock, raw.stock, raw.inventory, raw.skuStock, raw.remainingStock]
    .find((value) => value !== undefined && value !== null && value !== "");
  if (stock !== undefined && Number(stock) <= 0) return "sold-out";
  if (raw.lowStock === true || ["low", "insufficient"].includes(explicit)) return "low-stock";
  return "available";
}

export function normalizeProduct(raw, categories = []) {
  const legacyId = Number(raw.legacyDishId ?? raw.id);
  const meta = legacyMeta[legacyId] || {};
  const category = categoryFromRaw(raw, categories);
  const attributes = normalizeAttributes(raw.attributes, meta.filters);
  const parameters = parametersFrom(attributes, category);
  const skuModel = skusFrom(raw.skus);
  const flavors = (raw.flavors || []).flatMap(parseFlavorValues);
  const baseImage = assetPath(raw.mainImage || raw.image, categoryImages[category.key]);
  const availability = normalizeAvailability(raw);
  const fallbackParameters = [...new Set([...(meta.specs || []), ...flavors])]
    .map((value, index) => ({ label: ["核心规格", "工艺或功能", "适用提示"][index] || "补充参数", value }));

  return {
    id: raw.id,
    legacyDishId: raw.legacyDishId ?? raw.id,
    productCode: raw.productCode || null,
    categoryId: raw.categoryId,
    categoryKey: category.key,
    categoryName: category.name,
    brand: raw.brandName || meta.brand || "筑家优选",
    name: raw.name,
    sellingPoint: raw.subtitle || raw.detailDescription || raw.description || "材质与规格信息以商品页面为准。",
    detailDescription: raw.detailDescription || null,
    image: baseImage,
    gallery: [{ src: baseImage, label: "商品图片", alt: `${raw.name}商品图片` }],
    specs: [...new Set([...parameters.map((item) => item.value), ...(meta.specs || []), ...flavors])].slice(0, 3),
    parameters: parameters.length ? parameters : fallbackParameters,
    skus: skuModel.skus,
    skuGroups: skuModel.groups.length ? skuModel.groups : (raw.flavors || [])
      .map((flavor) => ({ key: String(flavor.id || flavor.name), label: flavor.name, options: parseFlavorValues(flavor) }))
      .filter((group) => group.options.length),
    salePrice: Number(raw.salePrice ?? raw.price ?? skuModel.skus[0]?.salePrice ?? 0),
    originalPrice: raw.originalPrice == null ? null : Number(raw.originalPrice),
    unit: raw.unit || meta.unit || category.unit,
    promotion: null,
    availableStock: raw.availableStock == null ? null : Number(raw.availableStock),
    availability,
    availabilityLabel: { available: "可购买", "low-stock": "库存不足", "sold-out": "已售罄", "off-shelf": "已下架" }[availability],
    favorited: Boolean(raw.favorited),
    filterValues: attributes,
    raw
  };
}

function queryString(params) {
  const query = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== "") query.set(key, String(value));
  });
  return query.toString();
}

const missingProductApi = (error) => Number(error?.status) === 404;

async function loadLegacyCategory(config) {
  const categories = await request("/user/category/list?type=1", { authRequired: false });
  const backend = categories.find((item) => item.name === config.name) || categories[0];
  if (!backend) return { category: config, categories, products: [], total: 0, serverPaged: false };
  const rows = await request(`/user/dish/list?categoryId=${backend.id}`, { authRequired: false });
  const products = (rows || []).map((row) => normalizeProduct(row, categories));
  return { category: config, categories, products, total: products.length, serverPaged: false };
}

export async function loadCategoryProducts(categoryKey, options = {}) {
  const category = getCategoryConfig(categoryKey);
  const params = {
    categoryCode: category.key,
    keyword: options.keyword,
    sort: options.sort === "default" ? undefined : options.sort,
    page: options.page || 1,
    pageSize: options.pageSize || 9
  };
  try {
    const result = await request(`/user/product/page?${queryString(params)}`, { authRequired: false });
    return {
      category,
      categories: [],
      products: (result?.records || []).map((row) => normalizeProduct(row)),
      total: Number(result?.total || 0),
      serverPaged: options.serverPaged !== false
    };
  } catch (error) {
    if (!missingProductApi(error)) throw error;
    return loadLegacyCategory(category);
  }
}

export async function loadProductDetail(id) {
  try {
    return normalizeProduct(await request(`/user/product/${encodeURIComponent(id)}`, { authRequired: false }));
  } catch (error) {
    if (!missingProductApi(error)) throw error;
    const [categories, raw] = await Promise.all([
      request("/user/category/list?type=1", { authRequired: false }),
      request(`/user/dish/${encodeURIComponent(id)}`, { authRequired: false })
    ]);
    return normalizeProduct(raw, categories);
  }
}

export async function loadRelatedProducts(product, limit = 3) {
  const id = typeof product === "object" ? product.id : product;
  try {
    const safeLimit = Math.max(1, Math.min(Number(limit) || 3, 12));
    const rows = await request(`/user/product/${encodeURIComponent(id)}/related?limit=${safeLimit}`, { authRequired: false });
    return (rows || []).map((row) => normalizeProduct(row));
  } catch (error) {
    if (!missingProductApi(error)) throw error;
    const category = getCategoryConfig(typeof product === "object" ? product.categoryKey : "tile");
    const result = await loadLegacyCategory(category);
    return result.products.filter((item) => String(item.id) !== String(id)).slice(0, limit);
  }
}

export function toggleProductFavorite(product) {
  const id = typeof product === "object" ? product.legacyDishId ?? product.id : product;
  return request(`/user/dish/favorite/${id}`, { method: "POST" });
}

export function filterAndSortProducts(products, { keyword = "", filters = {}, sort = "default" } = {}) {
  const query = keyword.trim().toLowerCase();
  const result = products.filter((product) => {
    const searchable = [product.brand, product.name, product.sellingPoint, ...product.specs].join(" ").toLowerCase();
    if (query && !searchable.includes(query)) return false;
    return Object.entries(filters).every(([key, selected]) => {
      if (!selected?.length) return true;
      const values = product.filterValues[key] || [];
      return selected.some((value) => values.includes(value));
    });
  });
  if (sort === "price-asc") result.sort((a, b) => a.salePrice - b.salePrice);
  if (sort === "price-desc") result.sort((a, b) => b.salePrice - a.salePrice);
  return result;
}

export function classifyCatalogError(error) {
  return {
    type: typeof navigator !== "undefined" && !navigator.onLine ? "offline" : "error",
    message: error?.message || "商品信息加载失败，请稍后重试。"
  };
}

export function classifyRequestError(error) {
  const result = classifyCatalogError(error);
  return { status: result.type, message: result.message };
}
