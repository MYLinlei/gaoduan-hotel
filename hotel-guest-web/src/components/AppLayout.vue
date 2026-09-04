<template>
  <div class="page-shell">
    <CommerceHeader />
    <section v-if="!shop.isOpen" class="shop-closed-banner site-container">
      当前暂停新订单提交，已加入购物车的商品仍会保留。
    </section>
    <main><slot /></main>
    <SiteFooter />
    <FloatingCart />
    <CartDrawer />
    <AuthDialog />
  </div>
</template>

<script setup>
import { onMounted, onUnmounted } from "vue";
import AuthDialog from "./AuthDialog.vue";
import CartDrawer from "./CartDrawer.vue";
import CommerceHeader from "./CommerceHeader.vue";
import FloatingCart from "./FloatingCart.vue";
import SiteFooter from "./SiteFooter.vue";
import { useShopStore } from "../stores/shop";

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

</script>

<style scoped>
.shop-closed-banner {
  margin-block: 16px;
  padding: 14px 18px;
  border: 1px solid var(--color-danger);
  color: var(--color-danger);
}
</style>
