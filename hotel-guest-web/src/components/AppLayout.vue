<template>
  <div class="page-shell">
    <header class="topbar glass-card">
      <RouterLink to="/menu" class="topbar__brand">
        <span class="topbar__logo">YQ</span>
        <div>
          <strong>云栖酒店</strong>
          <p>Guest Dining</p>
        </div>
      </RouterLink>

      <div class="topbar__actions">
        <span class="topbar__status" :class="{ closed: !shop.isOpen }">
          {{ shop.statusText }}
        </span>
        <button v-if="!auth.isLoggedIn" class="secondary-button" @click="auth.openLogin">住客登录</button>
        <div v-else class="topbar__user">
          <span>{{ auth.user?.nickname || auth.user?.name || "酒店住客" }}</span>
          <button class="ghost-button" @click="handleLogout">退出</button>
        </div>
      </div>
    </header>

    <section v-if="!shop.isOpen" class="shop-closed-banner glass-card">
      当前酒店已打烊，顾客端暂不支持加购与下单。
    </section>

    <slot />
    <FloatingCart />
    <CartDrawer />
    <AuthDialog />
    <nav class="bottom-nav glass-card">
      <RouterLink to="/menu">点餐</RouterLink>
      <RouterLink to="/checkout">结算</RouterLink>
      <RouterLink to="/orders">订单</RouterLink>
      <RouterLink to="/service">服务</RouterLink>
    </nav>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted } from "vue";
import AuthDialog from "./AuthDialog.vue";
import CartDrawer from "./CartDrawer.vue";
import FloatingCart from "./FloatingCart.vue";
import { useAuthStore } from "../stores/auth";
import { useShopStore } from "../stores/shop";

const auth = useAuthStore();
const shop = useShopStore();

let timer = null;

onMounted(async () => {
  await shop.loadStatus();
  timer = window.setInterval(() => {
    shop.loadStatus().catch(() => {});
  }, 15000);
});

onUnmounted(() => {
  if (timer) {
    window.clearInterval(timer);
    timer = null;
  }
});

async function handleLogout() {
  await auth.logout();
}
</script>

<style scoped>
.topbar {
  padding: 14px 16px;
  border-radius: var(--radius-xl);
  display: flex;
  justify-content: space-between;
  gap: 14px;
  align-items: center;
  margin-bottom: 18px;
}

.topbar__brand,
.topbar__user {
  display: flex;
  gap: 12px;
  align-items: center;
}

.topbar__logo {
  width: 42px;
  height: 42px;
  border-radius: 14px;
  display: grid;
  place-items: center;
  color: #fff;
  font-weight: 700;
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-soft));
}

.topbar__brand p {
  color: var(--color-text-muted);
  font-size: 12px;
  margin-top: 4px;
}

.topbar__actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.topbar__status {
  padding: 8px 12px;
  border-radius: 999px;
  background: rgba(21, 34, 56, 0.08);
  color: var(--color-primary);
  font-size: 13px;
}

.topbar__status.closed {
  background: rgba(148, 33, 33, 0.12);
  color: #8f1d1d;
}

.shop-closed-banner {
  margin-bottom: 16px;
  padding: 14px 16px;
  border-radius: var(--radius-lg);
  border: 1px solid rgba(143, 29, 29, 0.16);
  color: #8f1d1d;
}
</style>
