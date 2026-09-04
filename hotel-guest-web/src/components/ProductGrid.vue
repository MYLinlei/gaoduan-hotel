<template>
  <div class="catalog-product-grid" aria-live="polite">
    <ProductCard v-for="product in products" :key="product.id" :product="product" @favorite="$emit('favorite', product)" />
  </div>
</template>

<script setup>
import ProductCard from "./ProductCard.vue";
defineProps({ products: { type: Array, default: () => [] } });
defineEmits(["favorite"]);
</script>

<style scoped>
.catalog-product-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); border-left: 1px solid var(--color-line); border-top: 1px solid var(--color-line); }
.catalog-product-grid :deep(.product-card) { padding: 14px; border-top: 0; border-right: 1px solid var(--color-line); border-bottom: 1px solid var(--color-line); }
</style>
