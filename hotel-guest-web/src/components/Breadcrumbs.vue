<template>
  <nav class="breadcrumbs" aria-label="面包屑">
    <template v-for="(item, index) in items" :key="`${item.label}-${index}`">
      <RouterLink v-if="item.to" :to="item.to">{{ item.label }}</RouterLink>
      <span v-else aria-current="page">{{ item.label }}</span>
      <span v-if="index < items.length - 1" aria-hidden="true">/</span>
    </template>
  </nav>
</template>

<script setup>
defineProps({ items: { type: Array, default: () => [] } });
</script>

<style scoped>
.breadcrumbs { min-height: 44px; display: flex; align-items: center; gap: 10px; color: var(--color-muted); font-size: 13px; }
.breadcrumbs a:hover { color: var(--color-accent-dark); }
.breadcrumbs [aria-current="page"] { color: var(--color-ink); }
</style>
