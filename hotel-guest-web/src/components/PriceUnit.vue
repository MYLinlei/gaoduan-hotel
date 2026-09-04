<template>
  <div class="price-unit">
    <strong><small>¥</small>{{ formatted }}</strong><span>/ {{ unit }}</span><del v-if="originalPrice">¥{{ originalPrice }}</del>
  </div>
</template>

<script setup>
import { computed } from "vue";
const props = defineProps({ price: { type: Number, default: 0 }, originalPrice: { type: Number, default: null }, unit: { type: String, default: "件" } });
const formatted = computed(() => Number.isInteger(props.price) ? props.price : props.price.toFixed(2));
</script>

<style scoped>
.price-unit { display: flex; align-items: baseline; gap: 5px; font-variant-numeric: tabular-nums; }
.price-unit strong { color: var(--color-accent-dark); font-family: "Songti SC", "STSong", serif; font-size: 29px; }
.price-unit strong small { font-size: .56em; }
.price-unit span, .price-unit del { color: var(--color-muted); font-size: 12px; }
.price-unit del { margin-left: 8px; }
</style>
