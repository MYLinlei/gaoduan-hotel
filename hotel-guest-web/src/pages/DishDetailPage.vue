<template>
  <AppLayout>
    <section class="product-detail-page">
      <UnifiedContentState v-if="loading" status="loading" subject="商品详情" />
      <UnifiedContentState v-else-if="loadError" :status="loadError.status" subject="商品详情" :message="loadError.message" @retry="loadDetail" />
      <UnifiedContentState v-else-if="!product" status="empty" subject="商品" message="该商品可能已经下架，可以返回分类页继续选购。" />
      <template v-else>
        <Breadcrumbs :items="breadcrumbs" />
        <div class="product-detail-page__hero">
          <ProductGallery :images="product.gallery" />
          <aside class="purchase-panel" aria-labelledby="product-title">
            <div class="purchase-panel__brand">{{ product.brand }}</div>
            <h1 id="product-title">{{ product.name }}</h1>
            <p class="purchase-panel__intro">{{ product.sellingPoint }}</p>
            <PriceUnit :price="displayPrice" :original-price="displayOriginalPrice" :unit="product.unit" />
            <RouterLink class="purchase-panel__coupon" to="/coupons"><IconSymbol name="ticket" /><span>查看可用优惠券</span><IconSymbol name="chevron-right" /></RouterLink>
            <SkuSelector v-model="selectedSku" :groups="product.skuGroups" />
            <div class="purchase-panel__availability"><span :class="`is-${selectionAvailability}`">{{ selectionAvailabilityLabel }}</span><button type="button" :aria-pressed="product.favorited" @click="handleFavorite"><IconSymbol name="heart" />{{ product.favorited ? '已收藏' : '收藏' }}</button></div>
            <div class="purchase-panel__quantity"><span>数量</span><div><button type="button" aria-label="减少数量" :disabled="quantity <= 1" @click="quantity -= 1">−</button><strong>{{ quantity }}</strong><button type="button" aria-label="增加数量" :disabled="quantity >= maximumQuantity" @click="quantity += 1">＋</button><span>{{ product.unit }}</span></div></div>
            <p v-if="validationMessage" class="purchase-panel__validation" role="alert">{{ validationMessage }}</p>
            <div class="purchase-panel__actions"><button class="secondary-button" type="button" :disabled="!purchasable || submitting" @click="addToCart">{{ submittingAction === 'cart' ? '加入中…' : '加入购物车' }}</button><button class="accent-button" type="button" :disabled="!purchasable || submitting" @click="buyNow">{{ submittingAction === 'buy' ? '处理中…' : '立即购买' }}</button></div>
            <QuantityEstimator :category-key="product.categoryKey" />
            <p class="purchase-panel__notice"><IconSymbol name="info" />下单前请再次核对尺寸、批次与现场安装条件。</p>
          </aside>
        </div>

        <nav class="detail-anchor-nav" aria-label="商品详情目录"><a href="#specifications">规格参数</a><a href="#materials">材质与工艺</a><a href="#installation">测量安装</a><a href="#delivery">配送售后</a></nav>
        <section id="specifications" class="detail-section"><div class="detail-section__heading"><h2>规格参数</h2><p>购买前请逐项核对所选规格。</p></div><dl class="specification-table"><div><dt>商品名称</dt><dd>{{ product.name }}</dd></div><div><dt>商品品牌</dt><dd>{{ product.brand }}</dd></div><div><dt>计价单位</dt><dd>元/{{ product.unit }}</dd></div><div v-for="item in product.parameters" :key="`${item.label}-${item.value}`"><dt>{{ item.label }}</dt><dd>{{ item.value }}</dd></div></dl></section>
        <section id="materials" class="detail-section detail-section--split"><div><h2>材质与工艺</h2><p>商品材质、表面效果与颜色可能受显示设备和批次影响，请以实际商品及所选规格为准。</p></div><img :src="product.gallery[1]?.src || product.image" :alt="`${product.name}材质与工艺`" width="680" height="360" loading="lazy" /></section>
        <section id="installation" class="detail-section"><div class="detail-section__heading"><h2>测量与安装条件</h2><p>先确认空间，再核对规格和现场条件。</p></div><div class="condition-grid"><article><h3>测量空间</h3><p>记录长宽、门洞、转角、管线和设备预留位置。</p></article><article><h3>核对规格</h3><p>确认尺寸、铺装方向、安装方式与完成面高度。</p></article><article><h3>计算用量</h3><p>根据实际排版、损耗和包装规格复核购买数量。</p></article></div></section>
        <section id="delivery" class="detail-section detail-section--rules"><div><h2>配送与售后</h2><p>提交订单前，可就配送、安装和售后处理方式进行咨询；具体安排以订单确认信息为准。</p></div><RouterLink class="secondary-button" to="/service#rules">查看购买帮助</RouterLink></section>
        <section class="detail-section related-products" aria-labelledby="related-products-title">
          <div class="detail-section__heading"><div><h2 id="related-products-title">同品类继续选购</h2><p>根据当前品类提供其他可选商品。</p></div><RouterLink :to="{ path: '/menu', query: { category: product.categoryName } }">查看全部</RouterLink></div>
          <UnifiedContentState v-if="relatedLoading" status="loading" subject="相关商品" />
          <UnifiedContentState v-else-if="relatedError" :status="relatedError.status" subject="相关商品" :message="relatedError.message" @retry="loadRelated" />
          <UnifiedContentState v-else-if="!relatedProducts.length" status="empty" subject="相关商品" message="当前暂无其他同品类商品。" />
          <ProductGrid v-else :products="relatedProducts" />
        </section>
      </template>
    </section>
  </AppLayout>
</template>

<script setup>
import { computed, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import AppLayout from "../components/AppLayout.vue";
import Breadcrumbs from "../components/Breadcrumbs.vue";
import IconSymbol from "../components/IconSymbol.vue";
import PriceUnit from "../components/PriceUnit.vue";
import ProductGallery from "../components/ProductGallery.vue";
import ProductGrid from "../components/ProductGrid.vue";
import QuantityEstimator from "../components/QuantityEstimator.vue";
import SkuSelector from "../components/SkuSelector.vue";
import UnifiedContentState from "../components/UnifiedContentState.vue";
import { classifyRequestError, loadProductDetail, loadRelatedProducts, toggleProductFavorite } from "../services/catalogService";
import { useAuthStore } from "../stores/auth";
import { useCartStore } from "../stores/cart";
import { useShopStore } from "../stores/shop";
import { useUiStore } from "../stores/ui";

const route = useRoute(); const router = useRouter(); const auth = useAuthStore(); const cart = useCartStore(); const shop = useShopStore(); const ui = useUiStore();
const product = ref(null); const selectedSku = ref({}); const quantity = ref(1); const loading = ref(true); const loadError = ref(null); const validationMessage = ref(""); const submitting = ref(false); const submittingAction = ref("");
const relatedProducts = ref([]); const relatedLoading = ref(false); const relatedError = ref(null);
const breadcrumbs = computed(() => [{ label: "首页", to: "/" }, { label: product.value?.categoryName || "商品分类", to: { path: "/menu", query: { category: product.value?.categoryName } } }, { label: product.value?.name || "商品详情" }]);
const selectedSkuRecord = computed(() => {
  if (!product.value?.skus?.length) return null;
  return product.value.skus.find((sku) => Object.entries(selectedSku.value).every(([key, value]) => sku.specs[key] === value)) || null;
});
const selectionAvailability = computed(() => {
  const sku = selectedSkuRecord.value;
  if (!sku) return product.value?.availability || "off-shelf";
  if (sku.status === 0) return "off-shelf";
  if (sku.availableStock <= 0) return "sold-out";
  return sku.availableStock <= 10 ? "low-stock" : "available";
});
const selectionAvailabilityLabel = computed(() => ({ available: "可购买", "low-stock": "库存不足", "sold-out": "已售罄", "off-shelf": "已下架" }[selectionAvailability.value] || "暂不可购买"));
const displayPrice = computed(() => selectedSkuRecord.value?.salePrice ?? product.value?.salePrice ?? 0);
const displayOriginalPrice = computed(() => selectedSkuRecord.value?.originalPrice ?? product.value?.originalPrice ?? null);
const maximumQuantity = computed(() => Math.max(1, Math.min(99, selectedSkuRecord.value?.availableStock ?? product.value?.availableStock ?? 99)));
const purchasable = computed(() => ["available", "low-stock"].includes(selectionAvailability.value) && shop.isOpen);

function validateSku() {
  const missing = product.value.skuGroups.find((group) => !selectedSku.value[group.key]);
  if (missing) validationMessage.value = `请选择${missing.label}`;
  else if (product.value.skus.length && !selectedSkuRecord.value) validationMessage.value = "当前规格组合不可用，请重新选择。";
  else if (!["available", "low-stock"].includes(selectionAvailability.value)) validationMessage.value = `${selectionAvailabilityLabel.value}，请选择其他规格。`;
  else validationMessage.value = "";
  return !validationMessage.value;
}
async function submit(addOnly) {
  if (!auth.isLoggedIn) { validationMessage.value = "请先登录账户，再继续购买。"; auth.openLogin(); return; }
  if (!validateSku()) return;
  if (submitting.value) return;
  submitting.value = true;
  submittingAction.value = addOnly ? "cart" : "buy";
  try {
    if (selectedSkuRecord.value?.id) {
      await cart.addProduct(product.value.id, selectedSkuRecord.value.id, { quantity: quantity.value });
    } else {
      const specification = Object.entries(selectedSku.value)
        .map(([key, value]) => `${key}：${value}`)
        .join("；");
      await cart.addDish(product.value.legacyDishId || product.value.id, {
        remark: specification,
        quantity: quantity.value
      });
    }
    if (addOnly) ui.openCart(); else router.push("/checkout");
  } catch (error) {
    validationMessage.value = error?.message || "加入购物车失败，请稍后重试。";
  } finally {
    submitting.value = false;
    submittingAction.value = "";
  }
}
function addToCart() { submit(true); } function buyNow() { submit(false); }
async function handleFavorite() { if (!auth.isLoggedIn) { auth.openLogin(); return; } product.value.favorited = await toggleProductFavorite(product.value); }
async function loadRelated() { if (!product.value) return; relatedLoading.value = true; relatedError.value = null; try { relatedProducts.value = await loadRelatedProducts(product.value); } catch (error) { relatedError.value = classifyRequestError(error); } finally { relatedLoading.value = false; } }
async function loadDetail() {
  loading.value = true; loadError.value = null; relatedProducts.value = []; validationMessage.value = ""; quantity.value = 1;
  try {
    product.value = await loadProductDetail(route.params.id);
    const preferredSku = product.value.skus.find((sku) => sku.status === 1 && sku.availableStock > 0) || product.value.skus[0];
    selectedSku.value = preferredSku ? { ...preferredSku.specs } : Object.fromEntries(product.value.skuGroups.map((group) => [group.key, group.options[0]]));
    void loadRelated();
  } catch (error) { loadError.value = classifyRequestError(error); } finally { loading.value = false; }
}
watch(selectedSkuRecord, () => { quantity.value = Math.min(quantity.value, maximumQuantity.value); validationMessage.value = ""; });
watch(() => route.params.id, loadDetail, { immediate: true });
</script>

<style scoped>
.product-detail-page { width: min(1340px, calc(100% - var(--gutter) * 2)); margin: 0 auto 96px; }
.product-detail-page__hero { display: grid; grid-template-columns: minmax(0, 55fr) minmax(500px, 45fr); gap: 30px; align-items: start; }
.purchase-panel { padding: 24px; position: sticky; top: 12px; display: grid; gap: 16px; border: 1px solid var(--color-line); background: var(--color-paper); }
.purchase-panel__brand { color: var(--color-accent-dark); font-size: 12px; letter-spacing: .14em; }.purchase-panel h1 { font-size: 38px; line-height: 1.2; }.purchase-panel__intro { color: var(--color-muted); line-height: 1.7; }
.purchase-panel__coupon { min-height: 46px; padding: 0 14px; display: grid; grid-template-columns: auto 1fr auto; align-items: center; gap: 10px; border: 1px solid var(--color-line); }.purchase-panel__coupon:hover { border-color: var(--color-accent); }
.purchase-panel__availability, .purchase-panel__quantity { min-height: 48px; display: flex; align-items: center; justify-content: space-between; border-top: 1px solid var(--color-line); }.purchase-panel__availability > span { color: var(--color-success); }.purchase-panel__availability > span.is-low-stock { color: var(--color-accent-dark); }.purchase-panel__availability > span.is-sold-out, .purchase-panel__availability > span.is-off-shelf { color: var(--color-danger); }.purchase-panel__availability button { min-height: 44px; display: flex; align-items: center; gap: 7px; background: transparent; }
.purchase-panel__quantity > div { display: flex; align-items: center; }.purchase-panel__quantity button { width: 40px; height: 40px; background: var(--color-warm); }.purchase-panel__quantity strong { min-width: 42px; text-align: center; }.purchase-panel__quantity div > span { margin-left: 10px; color: var(--color-muted); font-size: 12px; }
.purchase-panel__validation { color: var(--color-danger); font-size: 13px; }.purchase-panel__actions { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; }.purchase-panel__actions button { min-height: 52px; }.accent-button { color: #fff; background: var(--color-accent); }.accent-button:hover { background: var(--color-accent-dark); }
.purchase-panel :deep(.quantity-estimator__form) { grid-template-columns: 1fr 1fr; }.purchase-panel :deep(.quantity-estimator output) { grid-column: 1 / -1; }
.purchase-panel__notice { display: flex; gap: 8px; color: var(--color-muted); font-size: 12px; line-height: 1.6; }.purchase-panel__notice .ui-icon { flex: 0 0 auto; width: 17px; }
.detail-anchor-nav { height: 58px; margin-top: 30px; display: flex; align-items: center; gap: 48px; position: sticky; top: 0; z-index: 3; border: 1px solid var(--color-line); background: rgba(255,255,255,.97); }.detail-anchor-nav a { min-height: 56px; padding: 0 24px; display: flex; align-items: center; }.detail-anchor-nav a:hover { color: var(--color-accent-dark); }
.detail-section { padding: 58px 0; border-bottom: 1px solid var(--color-line); scroll-margin-top: 72px; }.detail-section__heading { display: flex; align-items: end; justify-content: space-between; gap: 30px; margin-bottom: 28px; }.detail-section h2 { font-size: 32px; }.detail-section__heading p, .detail-section--split p, .detail-section--rules p { color: var(--color-muted); line-height: 1.75; }
.specification-table { display: grid; grid-template-columns: 1fr 1fr; border-top: 1px solid var(--color-ink); }.specification-table div { min-height: 58px; display: grid; grid-template-columns: 130px 1fr; align-items: center; border-bottom: 1px solid var(--color-line); }.specification-table div:nth-child(odd) { border-right: 1px solid var(--color-line); }.specification-table dt { padding-left: 18px; color: var(--color-muted); }.specification-table dd { margin: 0; font-weight: 600; }
.detail-section--split { display: grid; grid-template-columns: .8fr 1.2fr; gap: 70px; align-items: center; }.detail-section--split p { max-width: 54ch; margin-top: 18px; }.detail-section--split img { width: 100%; height: 330px; object-fit: cover; }
.condition-grid { display: grid; grid-template-columns: repeat(3, 1fr); border-top: 1px solid var(--color-ink); }.condition-grid article { min-height: 150px; padding: 26px; border-right: 1px solid var(--color-line); border-bottom: 1px solid var(--color-line); }.condition-grid article:last-child { border-right: 0; }.condition-grid p { margin-top: 10px; color: var(--color-muted); line-height: 1.7; }
.detail-section--rules { display: flex; justify-content: space-between; align-items: center; gap: 60px; }.detail-section--rules p { max-width: 75ch; margin-top: 12px; }
.related-products .detail-section__heading > div { display: grid; gap: 8px; }.related-products .detail-section__heading > a { color: var(--color-accent-dark); }
</style>
