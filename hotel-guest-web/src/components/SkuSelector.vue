<template>
  <div class="sku-selector">
    <fieldset v-for="group in groups" :key="group.key">
      <legend>{{ group.label }}</legend>
      <div>
        <button v-for="option in group.options" :key="option" type="button" :aria-pressed="modelValue[group.key] === option" @click="select(group.key, option)">{{ option }}</button>
      </div>
    </fieldset>
  </div>
</template>

<script setup>
const props = defineProps({ groups: { type: Array, default: () => [] }, modelValue: { type: Object, default: () => ({}) } });
const emit = defineEmits(["update:modelValue"]);
function select(key, value) { emit("update:modelValue", { ...props.modelValue, [key]: value }); }
</script>

<style scoped>
.sku-selector { display: grid; gap: 14px; }
.sku-selector fieldset { margin: 0; padding: 0; display: grid; grid-template-columns: 78px 1fr; gap: 12px; border: 0; }
.sku-selector legend { padding-top: 12px; float: left; font-size: 13px; font-weight: 600; }
.sku-selector fieldset > div { display: flex; flex-wrap: wrap; gap: 8px; }
.sku-selector button { min-height: 42px; padding: 8px 14px; background: var(--color-paper); border: 1px solid var(--color-line); }
.sku-selector button[aria-pressed="true"] { color: var(--color-accent-dark); border-color: var(--color-accent); box-shadow: inset 0 0 0 1px var(--color-accent); }
</style>
