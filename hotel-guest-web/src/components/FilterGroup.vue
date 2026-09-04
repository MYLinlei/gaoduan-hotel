<template>
  <fieldset class="filter-group">
    <legend><button type="button" :aria-expanded="open" @click="open = !open"><span>{{ group.label }}</span><IconSymbol name="chevron" /></button></legend>
    <div v-show="open" class="filter-group__options">
      <label v-for="option in group.options" :key="option"><input type="checkbox" :checked="selected.includes(option)" @change="toggle(option, $event.target.checked)" /><span>{{ option }}</span></label>
    </div>
  </fieldset>
</template>

<script setup>
import { ref } from "vue";
import IconSymbol from "./IconSymbol.vue";
const props = defineProps({ group: { type: Object, required: true }, selected: { type: Array, default: () => [] }, initiallyOpen: { type: Boolean, default: false } });
const emit = defineEmits(["change"]);
const open = ref(props.initiallyOpen);
function toggle(option, checked) { emit("change", checked ? [...props.selected, option] : props.selected.filter((item) => item !== option)); }
</script>

<style scoped>
.filter-group { min-width: 0; margin: 0; padding: 0 0 15px; border: 0; border-bottom: 1px solid var(--color-line); }
.filter-group legend { width: 100%; padding: 0; }
.filter-group legend button { width: 100%; min-height: 44px; padding: 0; display: flex; align-items: center; justify-content: space-between; background: transparent; font-weight: 600; text-align: left; }
.filter-group legend .ui-icon { width: 16px; transition: transform var(--transition); }
.filter-group legend button[aria-expanded="true"] .ui-icon { transform: rotate(180deg); }
.filter-group__options { display: grid; grid-template-columns: 1fr 1fr; gap: 11px 8px; padding-top: 4px; }
.filter-group__options label { min-height: 24px; display: flex; align-items: center; gap: 8px; color: var(--color-muted); font-size: 12px; cursor: pointer; }
.filter-group__options input { width: 15px; height: 15px; margin: 0; accent-color: var(--color-accent); }
</style>
