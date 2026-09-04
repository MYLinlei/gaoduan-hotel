<template>
  <section class="product-gallery" aria-label="商品图片">
    <div class="product-gallery__main">
      <img :src="activeImage.src" :alt="activeImage.alt" width="900" height="900" fetchpriority="high" />
      <template v-if="images.length > 1">
        <button class="product-gallery__previous" type="button" aria-label="上一张图片" @click="move(-1)"><IconSymbol name="chevron-left" /></button>
        <button class="product-gallery__next" type="button" aria-label="下一张图片" @click="move(1)"><IconSymbol name="chevron-right" /></button>
      </template>
    </div>
    <div v-if="images.length > 1" class="product-gallery__thumbs" role="tablist" aria-label="商品图片选择" @keydown="handleKeydown">
      <button v-for="(item, index) in images" :key="`${item.src}-${index}`" type="button" role="tab" :aria-selected="index === activeIndex" :tabindex="index === activeIndex ? 0 : -1" @click="activeIndex = index">
        <img :src="item.src" alt="" width="180" height="120" loading="lazy" /><span>{{ item.label }}</span>
      </button>
    </div>
  </section>
</template>

<script setup>
import { computed, ref, watch } from "vue";
import IconSymbol from "./IconSymbol.vue";
const props = defineProps({ images: { type: Array, default: () => [] } });
const activeIndex = ref(0);
const activeImage = computed(() => props.images[activeIndex.value] || { src: "", alt: "商品图片" });
watch(() => props.images, () => { activeIndex.value = 0; });
function move(step) { if (!props.images.length) return; activeIndex.value = (activeIndex.value + step + props.images.length) % props.images.length; }
function handleKeydown(event) { if (event.key === "ArrowRight") move(1); else if (event.key === "ArrowLeft") move(-1); else if (event.key === "Home") activeIndex.value = 0; else if (event.key === "End") activeIndex.value = props.images.length - 1; else return; event.preventDefault(); }
</script>

<style scoped>
.product-gallery { min-width: 0; }
.product-gallery__main { aspect-ratio: 1 / 1; position: relative; overflow: hidden; border: 1px solid var(--color-line); background: var(--color-warm); }
.product-gallery__main > img { width: 100%; height: 100%; object-fit: cover; }
.product-gallery__main > button { width: 44px; height: 44px; position: absolute; top: 50%; display: grid; place-items: center; border: 1px solid var(--color-line); border-radius: 50%; background: rgba(255,255,255,.92); transform: translateY(-50%); }
.product-gallery__previous { left: 10px; }.product-gallery__next { right: 10px; }
.product-gallery__thumbs { margin-top: 10px; display: grid; grid-template-columns: repeat(4, 1fr); gap: 10px; }
.product-gallery__thumbs button { min-width: 0; padding: 0 0 9px; display: grid; gap: 7px; background: transparent; border-bottom: 2px solid transparent; color: var(--color-muted); }
.product-gallery__thumbs button[aria-selected="true"] { color: var(--color-ink); border-color: var(--color-accent); }
.product-gallery__thumbs img { width: 100%; aspect-ratio: 4 / 3; object-fit: cover; }
.product-gallery__thumbs span { font-size: 12px; }
</style>
