<template>
  <section class="category-shell">
    <Breadcrumbs :items="breadcrumbs" />
    <header class="category-shell__masthead">
      <div><h1>{{ category.name }}</h1><p>{{ category.intro }}</p></div>
      <img :src="category.image" :alt="`${category.name}材质局部`" width="680" height="180" />
    </header>
    <div class="category-shell__workspace"><slot /></div>
  </section>
</template>

<script setup>
import { computed } from "vue";
import Breadcrumbs from "./Breadcrumbs.vue";
const props = defineProps({ category: { type: Object, required: true } });
const breadcrumbs = computed(() => [{ label: "首页", to: "/" }, { label: "商品分类", to: "/menu" }, { label: props.category.name }]);
</script>

<style scoped>
.category-shell { width: min(1340px, calc(100% - var(--gutter) * 2)); margin: 0 auto 96px; }
.category-shell__masthead { height: 180px; display: grid; grid-template-columns: minmax(520px, 1fr) 38%; overflow: hidden; border: 1px solid var(--color-line); background: var(--color-paper); }
.category-shell__masthead > div { padding: 24px 30px; display: flex; align-items: baseline; gap: 36px; }
.category-shell__masthead h1 { flex: 0 0 auto; font-size: 38px; }
.category-shell__masthead p { max-width: 620px; color: var(--color-muted); line-height: 1.7; }
.category-shell__masthead img { width: 100%; height: 180px; object-fit: cover; filter: saturate(.72); }
.category-shell__workspace { margin-top: 8px; }
</style>
