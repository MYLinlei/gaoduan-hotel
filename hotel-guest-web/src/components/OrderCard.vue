<template>
  <article class="order-card glass-card">
    <div class="order-card__head">
      <div>
        <h3>{{ order.number || order.id }}</h3>
        <p>{{ order.orderType === 2 ? "堂食" : "客房送餐" }} · {{ order.location }}</p>
      </div>
      <OrderStatusPill :status="order.displayStatus" />
    </div>

    <div class="order-card__items">
      <div v-for="item in order.items.slice(0, 3)" :key="`${order.id}-${item.name}`" class="order-card__item">
        <span>{{ item.name }}</span>
        <span>x{{ item.number || item.quantity }}</span>
      </div>
    </div>

    <div class="order-card__meta">
      <span>{{ order.orderTime || order.createdAt || "-" }}</span>
      <strong>¥{{ Number(order.payableAmount || 0).toFixed(2) }}</strong>
    </div>

    <p class="order-card__eta">
      {{ order.displayStatus === "待接单" ? "后台确认后将自动同步最新状态" : "状态已同步到最新进度" }}
    </p>

    <div class="order-card__actions">
      <button class="ghost-button" @click="$emit('detail')">查看详情</button>
      <button v-if="showUrge" class="secondary-button" @click="$emit('urge')">刷新状态</button>
    </div>
  </article>
</template>

<script setup>
import OrderStatusPill from "./OrderStatusPill.vue";

defineProps({
  order: {
    type: Object,
    required: true
  },
  showUrge: {
    type: Boolean,
    default: true
  }
});

defineEmits(["detail", "urge"]);
</script>

<style scoped>
.order-card {
  padding: 18px;
  border-radius: var(--radius-lg);
  display: grid;
  gap: 14px;
}

.order-card__head,
.order-card__meta,
.order-card__item,
.order-card__actions {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  align-items: center;
}

.order-card__head p,
.order-card__eta {
  color: var(--color-text-muted);
}

.order-card__items {
  display: grid;
  gap: 8px;
}

.order-card__item {
  font-size: 14px;
}

.order-card__actions {
  margin-top: 4px;
}

.order-card__actions button {
  flex: 1;
}
</style>
