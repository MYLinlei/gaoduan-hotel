<template>
  <div class="faq-accordion">
    <article v-for="(item, index) in items" :key="item.question">
      <h3><button type="button" :aria-expanded="openIndex === index" @click="openIndex = openIndex === index ? -1 : index"><span>{{ item.question }}</span><IconSymbol name="plus" /></button></h3>
      <p v-show="openIndex === index">{{ item.answer }}</p>
    </article>
  </div>
</template>

<script setup>
import { ref } from "vue";
import IconSymbol from "./IconSymbol.vue";
defineProps({ items: { type: Array, default: () => [] } });
const openIndex = ref(0);
</script>

<style scoped>
.faq-accordion { border-top: 1px solid var(--color-ink); }
.faq-accordion article { border-bottom: 1px solid var(--color-line); }.faq-accordion h3 { font-family: inherit; font-size: 15px; }
.faq-accordion button { width: 100%; min-height: 58px; padding: 0; display: flex; justify-content: space-between; align-items: center; background: transparent; text-align: left; }
.faq-accordion button[aria-expanded="true"] .ui-icon { transform: rotate(45deg); }.faq-accordion .ui-icon { transition: transform var(--transition); }
.faq-accordion p { max-width: 76ch; padding: 0 0 20px; color: var(--color-muted); line-height: 1.75; }
</style>
