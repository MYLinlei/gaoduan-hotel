<template>
  <span class="pill" :class="statusClass">{{ status }}</span>
</template>

<script setup>
import { computed } from "vue";

const props = defineProps({
  status: {
    type: String,
    required: true
  }
});

const statusClass = computed(() => {
  if (props.status === "待接单") return "pending";
  if (props.status === "制作中" || props.status === "派送中") return "active";
  if (props.status === "已完成") return "done";
  return "muted";
});
</script>

<style scoped>
.pill {
  display: inline-flex;
  padding: 7px 12px;
  border-radius: 999px;
  font-size: 12px;
}

.pending {
  background: rgba(185, 151, 91, 0.16);
  color: var(--color-primary);
}

.active {
  background: rgba(17, 36, 60, 0.12);
  color: var(--color-primary);
}

.done {
  background: rgba(90, 143, 106, 0.16);
  color: #3c6d4a;
}

.muted {
  background: rgba(103, 114, 132, 0.14);
  color: var(--color-text-muted);
}
</style>
