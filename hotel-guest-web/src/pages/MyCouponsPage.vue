<template>
  <AppLayout>
    <section class="my-coupons-page">
      <header class="glass-card my-coupons-page__hero">
        <div>
          <p class="eyebrow">我的优惠券</p>
          <h1>已领取礼遇券</h1>
          <p>这里会保留已领取的全部优惠券，并区分未使用与已使用状态。</p>
        </div>
        <RouterLink class="secondary-button" to="/coupons">继续领券</RouterLink>
      </header>

      <section v-if="!auth.isLoggedIn" class="glass-card my-coupons-page__empty">
        <h2>请先登录后查看我的优惠券</h2>
        <button class="primary-button" @click="auth.openLogin">立即登录</button>
      </section>

      <section v-else-if="couponsStore.myCoupons.length" class="my-coupons-page__list">
        <article v-for="coupon in couponsStore.myCoupons" :key="coupon.id" class="coupon-card glass-card">
          <div class="coupon-card__amount">
            <small>礼遇抵扣</small>
            <strong>¥{{ Number(coupon.discountAmount || 0).toFixed(0) }}</strong>
          </div>
          <div class="coupon-card__content">
            <div class="coupon-card__title-row">
              <div>
                <h2>{{ coupon.voucherName }}</h2>
                <p>满 {{ coupon.thresholdAmount }} 可用 · {{ coupon.scopeLabel }}</p>
              </div>
              <span class="coupon-card__status" :class="coupon.status === 1 ? 'unused' : 'used'">
                {{ coupon.status === 1 ? "未使用" : "已使用" }}
              </span>
            </div>
            <p class="coupon-card__desc">领取时间：{{ formatTime(coupon.receiveTime) }}</p>
            <div class="coupon-card__meta">
              <span>过期时间：{{ formatTime(coupon.expireTime) }}</span>
              <span>{{ coupon.status === 1 ? "可在结算页直接抵扣" : `已用于订单：${coupon.orderId || "-"}` }}</span>
            </div>
          </div>
        </article>
      </section>

      <section v-else class="glass-card my-coupons-page__empty">
        <h2>还没有领取任何优惠券</h2>
        <p>前往领券中心，领取适用于高端正餐、酒水品鉴与轻食甜品的礼遇券。</p>
        <RouterLink class="primary-button" to="/coupons">前往领券中心</RouterLink>
      </section>
    </section>
  </AppLayout>
</template>

<script setup>
import { onMounted } from "vue";
import AppLayout from "../components/AppLayout.vue";
import { useAuthStore } from "../stores/auth";
import { useCouponsStore } from "../stores/coupons";

const auth = useAuthStore();
const couponsStore = useCouponsStore();

function formatTime(value) {
  return String(value || "").replace("T", " ").slice(0, 16) || "-";
}

onMounted(async () => {
  if (auth.isLoggedIn) {
    await couponsStore.loadMyCoupons();
  }
});
</script>

<style scoped>
.my-coupons-page {
  display: grid;
  gap: 18px;
}

.my-coupons-page__hero,
.my-coupons-page__empty {
  padding: 22px;
  border-radius: var(--radius-xl);
}

.my-coupons-page__hero {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: center;
}

.my-coupons-page__hero p:last-child,
.my-coupons-page__empty p,
.coupon-card__desc,
.coupon-card__meta {
  color: var(--color-text-muted);
  line-height: 1.7;
}

.my-coupons-page__list {
  display: grid;
  gap: 14px;
}

.coupon-card {
  display: grid;
  grid-template-columns: 116px 1fr;
  gap: 16px;
  padding: 18px;
  border-radius: var(--radius-lg);
  border: 1px solid rgba(185, 151, 91, 0.46);
}

.coupon-card__amount {
  border-radius: var(--radius-lg);
  background: linear-gradient(180deg, rgba(185, 151, 91, 0.18), rgba(255, 255, 255, 0.92));
  display: grid;
  place-items: center;
  padding: 16px;
  color: var(--color-primary);
}

.coupon-card__amount strong {
  font-size: 34px;
  line-height: 1;
}

.coupon-card__content {
  display: grid;
  gap: 12px;
}

.coupon-card__title-row,
.coupon-card__meta {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
}

.coupon-card__status {
  padding: 8px 12px;
  border-radius: 999px;
  font-size: 12px;
}

.coupon-card__status.unused {
  background: rgba(185, 151, 91, 0.16);
  color: var(--color-primary);
}

.coupon-card__status.used {
  background: rgba(103, 114, 132, 0.14);
  color: var(--color-text-muted);
}

.my-coupons-page__empty {
  display: grid;
  gap: 14px;
}

@media (max-width: 767px) {
  .my-coupons-page__hero,
  .coupon-card,
  .coupon-card__title-row,
  .coupon-card__meta {
    display: grid;
  }

  .coupon-card {
    grid-template-columns: 1fr;
  }
}
</style>
