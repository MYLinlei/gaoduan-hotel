<template>
  <aside class="filter-sidebar" aria-label="商品筛选">
    <FilterGroup v-for="(group, index) in groups" :key="group.key" :group="group" :selected="modelValue[group.key] || []" :initially-open="index < 3" @change="update(group.key, $event)" />
    <button class="filter-sidebar__clear" type="button" @click="$emit('clear')">清除全部条件</button>
  </aside>
</template>

<script setup>
import FilterGroup from "./FilterGroup.vue";
const props = defineProps({ groups: { type: Array, default: () => [] }, modelValue: { type: Object, default: () => ({}) } });
const emit = defineEmits(["update:modelValue", "clear"]);
function update(key, value) { emit("update:modelValue", { ...props.modelValue, [key]: value }); }
</script>

<style scoped>
.filter-sidebar { width: 236px; padding: 0 16px 18px; align-self: start; border: 1px solid var(--color-line); background: var(--color-paper); }
.filter-sidebar__clear { width: 100%; min-height: 44px; margin-top: 16px; background: transparent; border: 1px solid var(--color-line); }
.filter-sidebar__clear:hover { color: var(--color-accent-dark); border-color: var(--color-accent); }
</style>
