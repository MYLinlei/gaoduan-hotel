<template>
  <AppLayout>
    <section class="coupon-page">
      <header class="glass-card coupon-page__hero">
        <div>
          <p class="eyebrow">领券中心</p>
          <h1>酒店礼遇优惠券</h1>
          <p>精选高端礼遇券，适用于堂食、客房送餐与酒水品鉴场景。</p>
        </div>
        <RouterLink class="secondary-button" to="/my-coupons">我的优惠券</RouterLink>
      </header>

      <section class="coupon-page__list">
        <article v-for="coupon in couponsStore.allCoupons" :key="coupon.id" class="coupon-card glass-card">
          <div class="coupon-card__amount">
            <small>礼遇抵扣</small>
            <strong>¥{{ Number(coupon.actualValue || 0).toFixed(0) }}</strong>
          </div>
          <div class="coupon-card__content">
            <div class="coupon-card__title-row">
              <div>
                <h2>{{ coupon.name }}</h2>
                <p>满 {{ coupon.payValue }} 可用 · {{ scopeLabel(coupon) }}</p>
              </div>
              <span class="coupon-card__badge">{{ scopeLabel(coupon) }}</span>
            </div>
            <p class="coupon-card__desc">{{ coupon.rules || "适用于酒店高端餐饮场景，请在有效期内使用。" }}</p>
            <div class="coupon-card__meta">
              <span>有效期：{{ dateLabel(coupon.beginTime, coupon.endTime) }}</span>
              <button
                class="primary-button"
                :class="{ 'coupon-card__disabled': couponsStore.isClaimed(coupon.id) }"
                :disabled="couponsStore.isClaimed(coupon.id)"
                @click="handleClaim(coupon.id)"
              >
                {{ couponsStore.isClaimed(coupon.id) ? "已领取" : "立即领取" }}
              </button>
            </div>
          </div>
        </article>
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

function dateLabel(begin, end) {
  return `${String(begin || "").replace("T", " ").slice(0, 16)} - ${String(end || "").replace("T", " ").slice(0, 16)}`;
}

function scopeLabel(coupon) {
  if (coupon.channelType === "WINE") return "酒水饮品";
  if (coupon.channelType === "BANQUET") return "宴会专区";
  return "全场通用";
}

async function handleClaim(couponId) {
  if (!auth.isLoggedIn) {
    auth.openLogin();
    return;
  }
  await couponsStore.claimCoupon(couponId);
  window.alert("领取成功，已放入“我的优惠券”。");
}

onMounted(async () => {
  await couponsStore.loadCouponCenter();
  if (auth.isLoggedIn) {
    await couponsStore.loadMyCoupons();
  }
});
</script>

<style scoped>
.coupon-page {
  display: grid;
  gap: 18px;
}

.coupon-page__hero {
  padding: 22px;
  border-radius: var(--radius-xl);
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: center;
}

.coupon-page__hero p:last-child {
  margin-top: 10px;
  color: var(--color-text-muted);
  line-height: 1.7;
}

.coupon-page__list {
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

.coupon-card__amount small {
  color: var(--color-text-muted);
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

.coupon-card__title-row p,
.coupon-card__desc {
  color: var(--color-text-muted);
  line-height: 1.7;
}

.coupon-card__badge {
  padding: 8px 12px;
  border-radius: 999px;
  background: var(--color-gold-soft);
  color: var(--color-primary);
  font-size: 12px;
}

.coupon-card__disabled {
  opacity: 0.65;
  cursor: not-allowed;
}

@media (max-width: 767px) {
  .coupon-page__hero,
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
