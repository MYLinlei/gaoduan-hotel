<template>
  <nav class="guide-index" aria-label="选材指南目录">
    <a v-for="item in items" :key="item.id" :href="`#${item.id}`"><span>{{ item.label }}</span><IconSymbol name="arrow" /></a>
  </nav>
</template>

<script setup>
import IconSymbol from "./IconSymbol.vue";
defineProps({ items: { type: Array, default: () => [] } });
</script>

<style scoped>
.guide-index { display: grid; grid-template-columns: repeat(4, 1fr); border-top: 1px solid var(--color-ink); border-bottom: 1px solid var(--color-line); }
.guide-index a { min-height: 66px; padding: 0 18px; display: flex; align-items: center; justify-content: space-between; border-right: 1px solid var(--color-line); }
.guide-index a:last-child { border-right: 0; }.guide-index a:hover { color: var(--color-accent-dark); }.guide-index .ui-icon { width: 17px; }
</style>
