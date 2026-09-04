<template>
  <section class="content-state" :class="`is-${status}`" :aria-live="status === 'loading' ? 'polite' : 'assertive'">
    <template v-if="status === 'loading'">
      <div class="content-state__skeleton" aria-hidden="true"><i v-for="item in 3" :key="item" /></div>
      <p>正在加载{{ subject }}…</p>
    </template>
    <template v-else>
      <IconSymbol :name="status === 'offline' ? 'wifi-off' : status === 'empty' ? 'search' : 'info'" />
      <h2>{{ title }}</h2>
      <p>{{ message }}</p>
      <button v-if="retryable" class="secondary-button" type="button" @click="$emit('retry')">重新加载</button>
    </template>
  </section>
</template>

<script setup>
import { computed } from "vue";
import IconSymbol from "./IconSymbol.vue";
const props = defineProps({ status: { type: String, required: true }, subject: { type: String, default: "内容" }, message: { type: String, default: "" } });
defineEmits(["retry"]);
const title = computed(() => ({ empty: `暂时没有符合条件的${props.subject}`, offline: "当前处于离线状态", error: `${props.subject}加载失败` }[props.status] || "请稍候"));
const retryable = computed(() => props.status === "error" || props.status === "offline");
</script>

<style scoped>
.content-state { min-height: 300px; padding: 48px 28px; display: grid; place-items: center; align-content: center; gap: 12px; text-align: center; border: 1px solid var(--color-line); background: var(--color-paper); }
.content-state > .ui-icon { width: 30px; height: 30px; color: var(--color-accent); }
.content-state h2 { font-size: 26px; }
.content-state p { color: var(--color-muted); }
.content-state__skeleton { width: 100%; display: grid; grid-template-columns: repeat(3, 1fr); gap: 18px; }
.content-state__skeleton i { aspect-ratio: 4 / 3; background: var(--color-warm); animation: state-pulse 1.4s ease-in-out infinite alternate; }
@keyframes state-pulse { to { opacity: .5; } }
</style>
