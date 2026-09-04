<template>
  <article class="product-card" :class="`is-${availability}`">
    <button class="product-card__image" type="button" @click="goDetail">
      <img :src="product.image" :alt="product.name" width="520" height="520" loading="lazy" />
      <span v-if="promotion" class="product-card__tag">{{ promotion }}</span>
    </button>
    <div class="product-card__body">
      <div class="product-card__brand">{{ product.brand }}</div>
      <h3><button type="button" @click="goDetail">{{ product.name }}</button></h3>
      <p>{{ product.sellingPoint || product.intro }}</p>
      <dl class="product-card__specs" aria-label="核心规格"><div v-for="(spec, index) in product.specs.slice(0, 3)" :key="spec"><dt>{{ specLabels[index] }}</dt><dd>{{ spec }}</dd></div></dl>
      <PriceUnit :price="price" :original-price="product.originalPrice" :unit="product.unit" />
      <div class="product-card__status" :class="`is-${availability}`">{{ availabilityLabel }}</div>
      <div class="product-card__actions">
        <button class="icon-button" type="button" :aria-label="product.favorited ? '取消收藏' : '收藏商品'" :aria-pressed="Boolean(product.favorited)" @click="handleFavorite"><IconSymbol name="heart" /></button>
        <button class="text-button" type="button" @click="goDetail">查看详情</button>
        <button class="primary-button" type="button" :disabled="!purchasable" @click="addToCart">{{ purchaseLabel }}</button>
      </div>
    </div>
  </article>
</template>

<script setup>
import { computed } from "vue";
import { useRouter } from "vue-router";
import IconSymbol from "./IconSymbol.vue";
import PriceUnit from "./PriceUnit.vue";
import { useAuthStore } from "../stores/auth";
import { useCartStore } from "../stores/cart";
import { useShopStore } from "../stores/shop";
import { useUiStore } from "../stores/ui";
import { toggleProductFavorite } from "../services/catalogService";

const props = defineProps({ product: { type: Object, required: true } });
const emit = defineEmits(["favorite"]);
const router = useRouter();
const auth = useAuthStore();
const cart = useCartStore();
const shop = useShopStore();
const ui = useUiStore();
const specLabels = ["规格", "工艺", "适用"];
const price = computed(() => Number(props.product.salePrice ?? props.product.price ?? 0));
const promotion = computed(() => props.product.promotion || props.product.tags?.[0] || "");
const availability = computed(() => props.product.availability || (props.product.stockStatus === "可购买" ? "available" : "sold-out"));
const availabilityLabel = computed(() => ({ available: "可购买", "low-stock": "库存不足", "sold-out": "已售罄", "off-shelf": "已下架" }[availability.value] || "暂不可购买"));
const purchasable = computed(() => ["available", "low-stock"].includes(availability.value) && shop.isOpen);
const purchaseLabel = computed(() => {
  if (!purchasable.value) return availabilityLabel.value;
  return props.product.skus?.length === 1 ? "加入购物车" : "选择规格";
});

function goDetail() { router.push(`/product/${props.product.id}`); }
async function handleFavorite() {
  if (!auth.isLoggedIn) { auth.openLogin(); return; }
  const value = await toggleProductFavorite(props.product);
  props.product.favorited = Boolean(value);
  emit("favorite", props.product);
}
async function addToCart() {
  if (props.product.skus?.length !== 1) { goDetail(); return; }
  if (!auth.isLoggedIn) { auth.openLogin(); return; }
  await cart.addProduct(props.product.id, props.product.skus[0].id, { quantity: 1 });
  ui.openCart();
}
</script>
