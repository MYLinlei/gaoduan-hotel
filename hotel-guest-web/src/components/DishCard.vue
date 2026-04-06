<template>
  <article class="dish-card glass-card">
    <div class="dish-card__image" :style="{ background: dish.image }" @click="goDetail">
      <span v-for="tag in dish.tags.slice(0, 2)" :key="tag" class="dish-card__tag">{{ tag }}</span>
    </div>
    <div class="dish-card__content">
      <div class="dish-card__header" @click="goDetail">
        <h3>{{ dish.name }}</h3>
        <strong>￥{{ Number(dish.price || 0).toFixed(2) }}</strong>
      </div>
      <p class="dish-card__intro" @click="goDetail">{{ dish.intro }}</p>
      <div class="dish-card__footer">
        <button class="ghost-button" @click="goDetail">查看详情</button>
        <button class="primary-button" :disabled="!shop.isOpen" @click="handleAdd">
          {{ shop.isOpen ? "+ 加入购物车" : "打烊中" }}
        </button>
      </div>
    </div>
  </article>
</template>

<script setup>
import { useRouter } from "vue-router";
import { useAuthStore } from "../stores/auth";
import { useCartStore } from "../stores/cart";
import { useShopStore } from "../stores/shop";
import { useUiStore } from "../stores/ui";

const props = defineProps({
  dish: {
    type: Object,
    required: true
  }
});

const router = useRouter();
const auth = useAuthStore();
const cart = useCartStore();
const shop = useShopStore();
const ui = useUiStore();

function goDetail() {
  router.push(`/dish/${props.dish.id}`);
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
  await cart.addDish(props.dish.id, { quantity: 1 });
  ui.openCart();
}
</script>

<style scoped>
.dish-card {
  overflow: hidden;
  border-radius: var(--radius-lg);
}

.dish-card__image {
  min-height: 164px;
  padding: 14px;
  display: flex;
  gap: 8px;
  align-items: flex-start;
  cursor: pointer;
}

.dish-card__tag {
  display: inline-flex;
  padding: 6px 10px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.82);
  color: var(--color-primary);
  font-size: 12px;
}

.dish-card__content {
  padding: 16px;
}

.dish-card__header,
.dish-card__footer {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
}

.dish-card__header {
  cursor: pointer;
}

.dish-card__intro {
  margin: 12px 0 16px;
  color: var(--color-text-muted);
  line-height: 1.7;
  cursor: pointer;
}

.dish-card__footer {
  align-items: stretch;
}

.dish-card__footer button {
  flex: 1;
}
</style>
