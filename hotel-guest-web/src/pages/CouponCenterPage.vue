<template>
  <AppLayout>
    <section class="coupon-center-page">
      <Breadcrumbs :items="[{ label: '首页', to: '/' }, { label: '优惠专区' }]" />
      <header class="coupon-center-page__header">
        <div><h1>优惠专区</h1><p>按商品范围查看可领取优惠券，实际可用情况以结算页校验结果为准。</p></div>
        <RouterLink class="secondary-button" to="/my-coupons">查看我的优惠券</RouterLink>
      </header>
      <nav class="coupon-tabs" aria-label="优惠券分类">
        <button v-for="tab in tabs" :key="tab.key" type="button" :aria-current="activeTab === tab.key ? 'page' : undefined" @click="activeTab = tab.key">{{ tab.label }}</button>
      </nav>
      <UnifiedContentState v-if="couponsStore.loading" status="loading" subject="优惠券" />
      <UnifiedContentState v-else-if="loadError" :status="loadError.status" subject="优惠券" :message="loadError.message" @retry="loadCoupons" />
      <UnifiedContentState v-else-if="!visibleCoupons.length" status="empty" subject="优惠券" message="当前分类暂无可领取优惠券，可以切换其他分类查看。" />
      <section v-else class="coupon-center-page__grid" aria-live="polite">
        <CouponTicket v-for="coupon in visibleCoupons" :key="coupon.id" :coupon="coupon" @claim="handleClaim" />
      </section>
      <p class="coupon-center-page__feedback" role="status">{{ feedback }}</p>
    </section>
  </AppLayout>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import AppLayout from "../components/AppLayout.vue";
import Breadcrumbs from "../components/Breadcrumbs.vue";
import CouponTicket from "../components/CouponTicket.vue";
import UnifiedContentState from "../components/UnifiedContentState.vue";
import { classifyRequestError } from "../services/catalogService";
import { useAuthStore } from "../stores/auth";
import { useCouponsStore } from "../stores/coupons";

const tabs = [{ key: "all", label: "全部" }, { key: "tile", label: "瓷砖" }, { key: "bath", label: "卫浴" }, { key: "floor", label: "木地板" }, { key: "cabinet", label: "橱柜" }, { key: "whole", label: "整屋采购" }];
const auth = useAuthStore(); const couponsStore = useCouponsStore(); const activeTab = ref("all"); const loadError = ref(null); const feedback = ref("");
const visibleCoupons = computed(() => activeTab.value === "all" ? couponsStore.allCoupons : couponsStore.allCoupons.filter((coupon) => coupon.categoryKey === activeTab.value));
async function loadCoupons() { loadError.value = null; try { if (auth.isLoggedIn) await couponsStore.loadMyCoupons(); await couponsStore.loadCouponCenter(); } catch (error) { loadError.value = classifyRequestError(error); } }
async function handleClaim(id) { if (!auth.isLoggedIn) { auth.openLogin(); return; } feedback.value = ""; try { await couponsStore.claimCoupon(id); feedback.value = "领取成功，优惠券已放入“我的优惠券”。"; } catch (error) { feedback.value = error.message || "领取失败，请稍后重试。"; } }
onMounted(loadCoupons);
</script>

<style scoped>
.coupon-center-page { width: min(1340px, calc(100% - var(--gutter) * 2)); margin: 0 auto 96px; }
.coupon-center-page__header { min-height: 132px; padding: 28px 32px; display: flex; align-items: center; justify-content: space-between; gap: 30px; border: 1px solid var(--color-line); background: var(--color-paper); }.coupon-center-page__header h1 { font-size: 38px; }.coupon-center-page__header p { max-width: 680px; margin-top: 10px; color: var(--color-muted); line-height: 1.7; }
.coupon-tabs { height: 62px; margin-top: 18px; display: flex; align-items: stretch; border-top: 1px solid var(--color-ink); border-bottom: 1px solid var(--color-line); }.coupon-tabs button { min-width: 116px; padding: 0 22px; position: relative; background: transparent; }.coupon-tabs button[aria-current="page"] { color: var(--color-accent-dark); font-weight: 600; }.coupon-tabs button[aria-current="page"]::after { content: ""; position: absolute; left: 22px; right: 22px; bottom: -1px; height: 2px; background: var(--color-accent); }
.coupon-center-page__grid { margin-top: 24px; display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }.coupon-center-page__feedback { min-height: 24px; margin-top: 18px; color: var(--color-success); }
</style>
