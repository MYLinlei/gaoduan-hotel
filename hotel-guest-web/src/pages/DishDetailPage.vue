<template>
  <AppLayout>
    <section v-if="dish" class="detail-page">
      <button class="ghost-button detail-page__back" @click="router.back()">返回列表</button>

      <div class="detail-page__hero glass-card" :style="{ background: dish.image }">
        <div class="detail-page__overlay">
          <div class="tag-row">
            <span v-for="tag in dish.tags" :key="tag" class="tag-chip">{{ tag }}</span>
          </div>
        </div>
      </div>

      <section class="detail-page__body glass-card">
        <div class="section-title">
          <div>
            <p class="eyebrow">菜品详情</p>
            <h1>{{ dish.name }}</h1>
          </div>
          <strong class="detail-page__price">￥{{ Number(dish.price || 0).toFixed(2) }}</strong>
        </div>

        <div class="detail-page__info">
          <div class="field-card">
            <h3>用料</h3>
            <p>{{ dish.ingredients }}</p>
          </div>
          <div class="field-card">
            <h3>口味说明</h3>
            <p>{{ dish.flavor }}</p>
          </div>
          <div class="field-card">
            <h3>温馨提示</h3>
            <p>{{ dish.notice }}</p>
          </div>
        </div>

        <p class="detail-page__desc">{{ dish.description }}</p>

        <div class="detail-page__quantity field-card">
          <span>数量</span>
          <div class="detail-page__stepper">
            <button @click="decrease">-</button>
            <strong>{{ quantity }}</strong>
            <button @click="increase">+</button>
          </div>
        </div>

        <textarea
          v-model="remark"
          class="field-textarea"
          placeholder="备注：少辣、去葱、不加香菜、加急等"
        />

        <div class="detail-page__actions">
          <button class="secondary-button" @click="router.back()">返回列表</button>
          <button class="primary-button" :disabled="!shop.isOpen" @click="handleAdd">
            {{ shop.isOpen ? "加入购物车" : "打烊中" }}
          </button>
        </div>
      </section>
    </section>
  </AppLayout>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import AppLayout from "../components/AppLayout.vue";
import { request } from "../api/http";
import { useAuthStore } from "../stores/auth";
import { useCartStore } from "../stores/cart";
import { useShopStore } from "../stores/shop";
import { useUiStore } from "../stores/ui";

const route = useRoute();
const router = useRouter();
const auth = useAuthStore();
const cart = useCartStore();
const shop = useShopStore();
const ui = useUiStore();

const quantity = ref(1);
const remark = ref("");
const dish = ref(null);

function normalizeDish(data) {
  return {
    ...data,
    ingredients: "主厨精选食材",
    flavor: data.flavors?.length ? data.flavors.map((item) => item.value).join(" / ") : "以当日出品为准",
    notice: "如有忌口或过敏源，请在备注中提前说明。",
    tags: [data.tagType === "WINE" ? "名酒" : "精选", (data.score || 0) >= 4.5 ? "招牌" : "主厨推荐"],
    image: data.image || "linear-gradient(135deg, #152238, #2c4464)"
  };
}

function increase() {
  if (quantity.value < 99) quantity.value += 1;
}

function decrease() {
  if (quantity.value > 1) quantity.value -= 1;
}

async function handleAdd() {
  if (!shop.isOpen) {
    window.alert("当前酒店已打烊，暂不支持加购。");
    return;
  }
  if (!auth.isLoggedIn) {
    auth.openLogin();
    return;
  }
  if (!dish.value) return;
  for (let index = 0; index < quantity.value; index += 1) {
    await cart.addDish(dish.value.id, { remark: remark.value });
  }
  ui.openCart();
}

onMounted(async () => {
  await shop.loadStatus();
  const data = await request(`/user/dish/${route.params.id}`, {
    authRequired: false
  });
  dish.value = normalizeDish(data);
});
</script>

<style scoped>
.detail-page {
  display: grid;
  gap: 16px;
}

.detail-page__back {
  width: fit-content;
}

.detail-page__hero {
  min-height: 260px;
  border-radius: var(--radius-xl);
  overflow: hidden;
}

.detail-page__overlay {
  min-height: 260px;
  padding: 18px;
  display: flex;
  align-items: flex-end;
  background: linear-gradient(180deg, transparent 20%, rgba(17, 36, 60, 0.42) 100%);
}

.detail-page__body {
  padding: 20px;
  border-radius: var(--radius-xl);
  display: grid;
  gap: 16px;
}

.detail-page__price {
  font-size: 28px;
  color: var(--color-primary);
}

.detail-page__info {
  display: grid;
  gap: 12px;
}

.detail-page__info p,
.detail-page__desc {
  color: var(--color-text-muted);
  line-height: 1.8;
}

.detail-page__quantity,
.detail-page__stepper,
.detail-page__actions {
  display: flex;
}

.detail-page__quantity {
  justify-content: space-between;
  align-items: center;
}

.detail-page__stepper {
  gap: 14px;
  align-items: center;
}

.detail-page__stepper button {
  width: 38px;
  height: 38px;
  border-radius: 999px;
  background: var(--color-gold-soft);
}

.detail-page__actions {
  gap: 12px;
}

.detail-page__actions button {
  flex: 1;
}
</style>
