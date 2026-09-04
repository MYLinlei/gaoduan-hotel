<template>
  <div class="result-toolbar">
    <strong>共 {{ count }} 件商品</strong>
    <div class="result-toolbar__selected" aria-label="已选条件">
      <button v-for="item in selectedItems" :key="`${item.key}-${item.value}`" type="button" :aria-label="`移除${item.label}${item.value}`" @click="$emit('remove', item)">{{ item.label }}：{{ item.value }} <span aria-hidden="true">×</span></button>
      <button v-if="selectedItems.length" class="result-toolbar__clear" type="button" @click="$emit('clear')">清除条件</button>
    </div>
    <label><span class="sr-only">商品排序</span><select :value="sort" @change="$emit('update:sort', $event.target.value)"><option value="default">综合排序</option><option value="price-asc">价格从低到高</option><option value="price-desc">价格从高到低</option></select></label>
  </div>
</template>

<script setup>
defineProps({ count: { type: Number, default: 0 }, selectedItems: { type: Array, default: () => [] }, sort: { type: String, default: "default" } });
defineEmits(["remove", "clear", "update:sort"]);
</script>

<style scoped>
.result-toolbar { min-height: 58px; padding: 8px 14px; display: grid; grid-template-columns: auto 1fr auto; align-items: center; gap: 18px; border: 1px solid var(--color-line); border-bottom: 0; background: var(--color-paper); font-size: 13px; }
.result-toolbar__selected { display: flex; flex-wrap: wrap; gap: 7px; }
.result-toolbar__selected button { min-height: 34px; padding: 5px 10px; background: var(--color-warm); border: 1px solid transparent; }
.result-toolbar__selected .result-toolbar__clear { background: transparent; border-bottom: 1px solid var(--color-ink); }
.result-toolbar select { min-height: 40px; padding: 0 34px 0 12px; border: 0; background: transparent; color: var(--color-ink); }
</style>
