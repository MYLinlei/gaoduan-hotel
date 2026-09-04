<template>
  <AppLayout>
    <section class="my-coupons-page">
      <Breadcrumbs :items="[{ label: '首页', to: '/' }, { label: '优惠专区', to: '/coupons' }, { label: '我的优惠券' }]" />
      <header class="my-coupons-page__header"><div><h1>我的优惠券</h1><p>查看已领取优惠券的使用状态和适用范围。</p></div><RouterLink class="secondary-button" to="/coupons">继续领券</RouterLink></header>
      <section v-if="!auth.isLoggedIn" class="my-coupons-page__empty"><h2>登录后查看已领取优惠券</h2><button class="primary-button" type="button" @click="auth.openLogin">立即登录</button></section>
      <template v-else>
        <nav class="my-coupons-tabs" aria-label="我的优惠券状态"><button v-for="tab in tabs" :key="tab.key" type="button" :aria-current="activeTab === tab.key ? 'page' : undefined" @click="activeTab = tab.key">{{ tab.label }}</button></nav>
        <UnifiedContentState v-if="loading" status="loading" subject="优惠券" />
        <UnifiedContentState v-else-if="loadError" :status="loadError.status" subject="优惠券" :message="loadError.message" @retry="loadCoupons" />
        <UnifiedContentState v-else-if="!visibleCoupons.length" status="empty" subject="优惠券" message="当前状态下没有优惠券，可以前往优惠专区查看。" />
        <section v-else class="my-coupons-page__list">
          <article v-for="coupon in visibleCoupons" :key="coupon.id" class="owned-coupon"><div class="owned-coupon__value"><strong><small>¥</small>{{ Number(coupon.discountAmount || 0).toFixed(0) }}</strong><span>满 {{ coupon.thresholdAmount }} 元可用</span></div><div class="owned-coupon__body"><div><h2>{{ coupon.voucherName }}</h2><span>{{ statusLabel(coupon) }}</span></div><p>{{ coupon.scopeLabel || '适用范围以券面信息为准' }}</p><dl><div><dt>领取时间</dt><dd>{{ formatTime(coupon.receiveTime) }}</dd></div><div><dt>有效期至</dt><dd>{{ formatTime(coupon.expireTime) }}</dd></div></dl></div></article>
        </section>
      </template>
    </section>
  </AppLayout>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import AppLayout from "../components/AppLayout.vue";
import Breadcrumbs from "../components/Breadcrumbs.vue";
import UnifiedContentState from "../components/UnifiedContentState.vue";
import { classifyRequestError } from "../services/catalogService";
import { useAuthStore } from "../stores/auth";
import { useCouponsStore } from "../stores/coupons";
const auth = useAuthStore(); const coupons = useCouponsStore(); const activeTab = ref("available"); const loading = ref(false); const loadError = ref(null);
const tabs = [{ key: "available", label: "可使用" }, { key: "used", label: "已使用" }, { key: "expired", label: "已过期" }, { key: "all", label: "全部" }];
function isExpired(coupon) { return coupon.expireTime && new Date(coupon.expireTime).getTime() < Date.now(); }
function couponState(coupon) { if (isExpired(coupon)) return "expired"; return Number(coupon.status) === 1 ? "available" : "used"; }
const visibleCoupons = computed(() => activeTab.value === "all" ? coupons.myCoupons : coupons.myCoupons.filter((item) => couponState(item) === activeTab.value));
function statusLabel(coupon) { return { available: "可使用", used: "已使用", expired: "已过期" }[couponState(coupon)]; }
function formatTime(value) { return value ? String(value).replace("T", " ").slice(0, 16) : "—"; }
async function loadCoupons() { loading.value = true; loadError.value = null; try { await coupons.loadMyCoupons(); } catch (error) { loadError.value = classifyRequestError(error); } finally { loading.value = false; } }
onMounted(() => { if (auth.isLoggedIn) loadCoupons(); });
</script>

<style scoped>
.my-coupons-page { width: min(1180px, calc(100% - var(--gutter) * 2)); margin: 0 auto 96px; }.my-coupons-page__header { min-height: 126px; padding: 28px 32px; display: flex; justify-content: space-between; align-items: center; border: 1px solid var(--color-line); background: var(--color-paper); }.my-coupons-page__header h1 { font-size: 36px; }.my-coupons-page__header p { margin-top: 9px; color: var(--color-muted); }
.my-coupons-tabs { height: 58px; margin-top: 18px; display: flex; border-top: 1px solid var(--color-ink); border-bottom: 1px solid var(--color-line); }.my-coupons-tabs button { min-width: 118px; background: transparent; }.my-coupons-tabs button[aria-current="page"] { color: var(--color-accent-dark); border-bottom: 2px solid var(--color-accent); }
.my-coupons-page__list { margin-top: 22px; display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }.owned-coupon { min-height: 174px; display: grid; grid-template-columns: 150px 1fr; border: 1px solid #dfc7ba; background: #fbf7f3; }.owned-coupon__value { padding: 20px; display: flex; flex-direction: column; justify-content: center; border-right: 1px dashed #d7b7a8; }.owned-coupon__value strong { color: var(--color-accent-dark); font-family: "Songti SC", serif; font-size: 40px; }.owned-coupon__value small { font-size: 18px; }.owned-coupon__value span, .owned-coupon__body p, .owned-coupon__body dl { color: var(--color-muted); font-size: 12px; }
.owned-coupon__body { padding: 20px; display: grid; align-content: center; gap: 12px; }.owned-coupon__body > div { display: flex; justify-content: space-between; gap: 12px; }.owned-coupon__body h2 { font-family: inherit; font-size: 17px; letter-spacing: 0; }.owned-coupon__body > div span { color: var(--color-accent-dark); }.owned-coupon__body dl { margin: 0; padding-top: 10px; display: grid; gap: 6px; border-top: 1px solid var(--color-line); }.owned-coupon__body dl div { display: flex; justify-content: space-between; }.owned-coupon__body dd { margin: 0; }
.my-coupons-page__empty { min-height: 280px; margin-top: 20px; display: grid; place-items: center; align-content: center; gap: 18px; border: 1px solid var(--color-line); background: var(--color-paper); }
</style>
