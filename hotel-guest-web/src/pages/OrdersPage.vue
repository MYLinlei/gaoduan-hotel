<template>
  <AppLayout>
    <section class="orders-page">
      <header class="glass-card orders-page__hero">
        <p class="eyebrow">我的订单</p>
        <h1>当前订单与历史订单</h1>
        <p>此页面每 10 秒自动同步一次订单状态，后台操作后这里会及时更新。</p>
      </header>

      <section v-if="!auth.isLoggedIn" class="glass-card orders-page__detail">
        <h2>请先登录</h2>
        <button class="primary-button" @click="auth.openLogin">住客登录</button>
      </section>

      <template v-else>
        <section class="orders-page__section">
          <div class="section-title">
            <h2>进行中订单</h2>
          </div>
          <div class="orders-page__list">
            <OrderCard
              v-for="order in ordersStore.activeOrders"
              :key="order.id"
              :order="order"
              @detail="showDetail(order)"
              @urge="handleRefresh"
            />
            <div v-if="!ordersStore.activeOrders.length" class="field-card">当前没有进行中的订单。</div>
          </div>
        </section>

        <section class="orders-page__section">
          <div class="section-title">
            <h2>历史订单</h2>
          </div>
          <div class="orders-page__list">
            <OrderCard
              v-for="order in ordersStore.historyOrders"
              :key="order.id"
              :order="order"
              :show-urge="false"
              @detail="showDetail(order)"
            />
            <div v-if="!ordersStore.historyOrders.length" class="field-card">暂无历史订单。</div>
          </div>
        </section>

        <Transition name="fade-slide">
          <section v-if="selectedOrder" class="glass-card orders-page__detail">
            <div class="section-title">
              <div>
                <p class="eyebrow">订单详情</p>
                <h3>{{ selectedOrder.number || selectedOrder.id }}</h3>
              </div>
              <button class="ghost-button" @click="selectedOrder = null">关闭</button>
            </div>
            <div class="orders-page__detail-grid">
              <div class="field-card">
                <h4>订单状态</h4>
                <p>{{ selectedOrder.displayStatus }}</p>
              </div>
              <div class="field-card">
                <h4>用餐方式</h4>
                <p>{{ selectedOrder.orderType === 2 ? "堂食" : "客房送餐" }}</p>
              </div>
              <div class="field-card">
                <h4>桌台号 / 房号</h4>
                <p>{{ selectedOrder.location }}</p>
              </div>
              <div class="field-card">
                <h4>支付方式</h4>
                <p>{{ payMethodLabel(selectedOrder.payMethod) }}</p>
              </div>
            </div>
            <div class="orders-page__detail-items">
              <article v-for="item in selectedOrder.items" :key="item.name" class="field-card">
                <strong>{{ item.name }}</strong>
                <p>数量 x{{ item.number || item.quantity }} · 单价 ¥{{ item.amount || item.price }}</p>
              </article>
            </div>
          </section>
        </Transition>
      </template>
    </section>
  </AppLayout>
</template>

<script setup>
import { onMounted, onUnmounted, ref } from "vue";
import AppLayout from "../components/AppLayout.vue";
import OrderCard from "../components/OrderCard.vue";
import { useAuthStore } from "../stores/auth";
import { useOrdersStore } from "../stores/orders";
import { mapOrderStatus } from "../utils/order";

const auth = useAuthStore();
const ordersStore = useOrdersStore();
const selectedOrder = ref(null);
let timer = null;

function payMethodLabel(value) {
  if (value === 1) return "微信支付";
  if (value === 2) return "支付宝";
  if (value === 3) return "挂房账";
  return "未设置";
}

async function showDetail(order) {
  const detail = await ordersStore.loadOrderDetail(order.id);
  selectedOrder.value = {
    ...detail,
    displayStatus: mapOrderStatus(detail),
    items: detail.orderDetailList || [],
    location: detail.orderType === 2 ? detail.tableNo : detail.address || detail.phone || "-"
  };
}

async function handleRefresh() {
  await ordersStore.loadOrders();
}

onMounted(async () => {
  if (auth.isLoggedIn) {
    await ordersStore.loadOrders();
    timer = window.setInterval(() => {
      ordersStore.loadOrders();
    }, 10000);
  }
});

onUnmounted(() => {
  if (timer) {
    window.clearInterval(timer);
  }
});
</script>

<style scoped>
.orders-page {
  display: grid;
  gap: 18px;
}

.orders-page__hero,
.orders-page__detail {
  padding: 20px;
  border-radius: var(--radius-xl);
}

.orders-page__hero p:last-child {
  margin-top: 10px;
  color: var(--color-text-muted);
}

.orders-page__section,
.orders-page__list,
.orders-page__detail-items {
  display: grid;
  gap: 12px;
}

.orders-page__detail-grid {
  display: grid;
  gap: 12px;
  margin-bottom: 14px;
}

.orders-page__detail p {
  color: var(--color-text-muted);
  line-height: 1.7;
}

@media (min-width: 768px) {
  .orders-page__detail-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
