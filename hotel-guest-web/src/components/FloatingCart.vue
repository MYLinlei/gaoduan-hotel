<template>
  <button class="floating-cart__button glass-card" @click="ui.toggleCart">
    <span>购物车</span>
    <strong>{{ cart.totalCount }}</strong>
  </button>
</template>

<script setup>
import { useCartStore } from "../stores/cart";
import { useUiStore } from "../stores/ui";

const cart = useCartStore();
const ui = useUiStore();
</script>

<style scoped>
.floating-cart__button {
  min-width: 88px;
  padding: 14px 16px;
  border-radius: 18px;
  display: grid;
  gap: 4px;
  color: var(--color-primary);
}

.floating-cart__button strong {
  font-size: 18px;
}
</style>
