<template>
  <AppLayout>
    <section class="checkout-page">
      <header class="glass-card checkout-page__hero">
        <p class="eyebrow">提交订单</p>
        <h1>确认点单信息</h1>
        <p>已切换为真实后端下单流程，提交后会同步生成正式订单。</p>
      </header>

      <section v-if="!auth.isLoggedIn" class="glass-card checkout-page__block">
        <h2>请先登录</h2>
        <p class="checkout-page__hint">登录后可获取真实购物车、优惠券与订单状态。</p>
        <button class="primary-button" @click="auth.openLogin">住客登录</button>
      </section>

      <template v-else>
        <section v-if="!shop.isOpen" class="glass-card checkout-page__block checkout-page__closed">
          <h2>当前酒店已打烊</h2>
          <p class="checkout-page__hint">打烊期间暂不支持提交新订单，请稍后再试。</p>
        </section>

        <section class="glass-card checkout-page__block">
          <div class="section-title">
            <h2>用餐方式</h2>
          </div>
          <div class="checkout-page__mode">
            <button class="ghost-button" :class="{ active: diningType === '堂食' }" @click="diningType = '堂食'">
              堂食
            </button>
            <button
              class="ghost-button"
              :class="{ active: diningType === '客房送餐' }"
              @click="diningType = '客房送餐'"
            >
              客房送餐
            </button>
          </div>

          <input
            v-if="diningType === '堂食'"
            v-model="location"
            class="field-input"
            placeholder="请输入桌台号，例如 A08 / 包间 2"
          />
          <input
            v-else
            v-model="location"
            class="field-input"
            placeholder="请输入房号，例如 1808"
          />
        </section>

        <section class="glass-card checkout-page__block">
          <div class="section-title">
            <h2>订单明细</h2>
          </div>
          <div v-if="!cart.items.length" class="field-card">购物车为空，请先选择菜品。</div>
          <div v-else class="checkout-page__items">
            <article v-for="item in cart.items" :key="item.id" class="field-card checkout-page__item">
              <div>
                <h3>{{ item.name }}</h3>
                <p v-if="item.dishFlavor">{{ item.dishFlavor }}</p>
              </div>
              <div class="checkout-page__price">
                <div>
                  <span>￥{{ Number(item.amount || 0).toFixed(2) }}</span>
                  <strong>x{{ item.number }}</strong>
                </div>
                <strong>￥{{ (Number(item.amount || 0) * Number(item.number || 0)).toFixed(2) }}</strong>
              </div>
            </article>
          </div>
          <textarea
            v-model="cart.orderRemark"
            class="field-textarea"
            placeholder="整单备注：请填写送餐偏好、加急说明、安静敲门等"
          />
        </section>

        <section class="glass-card checkout-page__block">
          <div class="section-title">
            <div>
              <h2>选择优惠券</h2>
              <p class="checkout-page__hint">自动筛选当前订单可用优惠券。</p>
            </div>
            <RouterLink class="secondary-button" to="/my-coupons">我的优惠券</RouterLink>
          </div>
          <div v-if="availableCoupons.length" class="checkout-page__coupon-list">
            <button
              v-for="coupon in availableCoupons"
              :key="coupon.id"
              class="checkout-page__coupon-card"
              :class="{ active: selectedCoupon?.id === coupon.id }"
              @click="handleCouponPick(coupon.id)"
            >
              <div>
                <strong>￥{{ Number(coupon.discountAmount || 0).toFixed(0) }}</strong>
                <span>{{ coupon.voucherName }}</span>
              </div>
              <p>满{{ coupon.thresholdAmount }} 可用 · {{ coupon.scopeLabel }}</p>
              <small>领取时间：{{ formatTime(coupon.receiveTime) }}</small>
            </button>
          </div>
          <div v-else class="field-card">当前订单暂无可用优惠券，可前往领券中心领取后再使用。</div>
        </section>

        <section class="glass-card checkout-page__block">
          <div class="section-title">
            <h2>支付方式</h2>
          </div>
          <div class="checkout-page__mode">
            <button
              v-for="option in paymentOptions"
              :key="option.label"
              class="ghost-button"
              :class="{ active: paymentMethod === option.value }"
              @click="paymentMethod = option.value"
            >
              {{ option.label }}
            </button>
          </div>
        </section>

        <section class="glass-card checkout-page__summary">
          <div class="checkout-page__summary-text">
            <span>优惠前金额 ￥{{ cart.totalAmount.toFixed(2) }}</span>
            <span>优惠金额 ￥{{ discountAmount.toFixed(2) }}</span>
            <strong>实付金额 ￥{{ payableAmount.toFixed(2) }}</strong>
          </div>
          <button class="primary-button" :disabled="submitDisabled" @click="submitOrder">
            {{ submitting ? "提交中..." : shop.isOpen ? "提交订单" : "打烊中" }}
          </button>
        </section>

        <Transition name="fade-slide">
          <section v-if="successOrder" class="glass-card checkout-page__result">
            <p class="eyebrow">下单成功</p>
            <h2>订单号 {{ successOrder.orderNumber }}</h2>
            <p>订单已进入待接单队列，请前往订单页查看最新状态。</p>
            <RouterLink class="secondary-button" to="/orders">查看我的订单</RouterLink>
          </section>
        </Transition>
      </template>
    </section>
  </AppLayout>
</template>

<script setup>
import { computed, onMounted, ref, watch } from "vue";
import { useRouter } from "vue-router";
import AppLayout from "../components/AppLayout.vue";
import { request } from "../api/http";
import { useAuthStore } from "../stores/auth";
import { useCartStore } from "../stores/cart";
import { useCouponsStore } from "../stores/coupons";
import { useOrdersStore } from "../stores/orders";
import { useShopStore } from "../stores/shop";

const auth = useAuthStore();
const cart = useCartStore();
const coupons = useCouponsStore();
const orders = useOrdersStore();
const shop = useShopStore();
const router = useRouter();

const diningType = ref("客房送餐");
const location = ref("");
const paymentMethod = ref(1);
const paymentOptions = [
  { label: "微信支付", value: 1 },
  { label: "支付宝", value: 2 },
  { label: "挂房账", value: 3 }
];
const submitting = ref(false);
const successOrder = ref(null);

const availableCoupons = computed(() => coupons.getAvailableCoupons(cart.totalAmount));
const selectedCoupon = computed(() => coupons.getSelectedCoupon(cart.totalAmount));
const discountAmount = computed(() => coupons.getDiscountAmount(cart.totalAmount));
const payableAmount = computed(() => Math.max(cart.totalAmount - discountAmount.value, 0));
const submitDisabled = computed(() => submitting.value || !cart.items.length || !shop.isOpen);

watch(
  () => cart.totalAmount,
  (amount) => coupons.syncSelectedCoupon(amount),
  { immediate: true }
);

function formatTime(value) {
  return String(value || "").replace("T", " ").slice(0, 16) || "-";
}

function handleCouponPick(couponId) {
  if (selectedCoupon.value?.id === couponId) {
    coupons.clearSelectedCoupon();
    return;
  }
  coupons.selectCoupon(couponId, cart.totalAmount);
}

async function ensureRoomAddress() {
  const list = await request("/user/addressBook/list");
  const existing = list.find((item) => item.label === "客房送餐") || list.find((item) => item.isDefault === 1);
  const payload = {
    consignee: auth.user?.name || "住客",
    phone: auth.user?.phone || "13800138000",
    sex: "1",
    detail: `房号 ${location.value.trim()}`,
    label: "客房送餐",
    isDefault: 1
  };

  if (existing) {
    await request("/user/addressBook", {
      method: "PUT",
      body: {
        ...existing,
        ...payload
      }
    });
    return existing.id;
  }

  await request("/user/addressBook", {
    method: "POST",
    body: payload
  });
  const latest = await request("/user/addressBook/default");
  return latest?.id;
}

async function submitOrder() {
  if (!cart.items.length || submitting.value) return;
  await shop.loadStatus();
  if (!shop.isOpen) {
    window.alert("当前酒店已打烊，暂不支持下单。");
    return;
  }
  if (!location.value.trim()) {
    window.alert(diningType.value === "堂食" ? "桌台号不能为空" : "房号不能为空");
    return;
  }

  submitting.value = true;
  try {
    const addressBookId = diningType.value === "客房送餐" ? await ensureRoomAddress() : null;
    const submitRes = await request("/user/order/submit", {
      method: "POST",
      body: {
        orderType: diningType.value === "堂食" ? 2 : 1,
        addressBookId,
        tableNo: diningType.value === "堂食" ? location.value.trim() : "",
        couponId: selectedCoupon.value?.id || null,
        payMethod: paymentMethod.value,
        remark: cart.orderRemark,
        packAmount: 0,
        tablewareNumber: 1,
        tablewareStatus: 1,
        amount: cart.totalAmount
      }
    });

    await request("/user/order/payment", {
      method: "PUT",
      body: {
        orderNumber: submitRes.orderNumber,
        payMethod: paymentMethod.value
      }
    });

    if (selectedCoupon.value) {
      coupons.markCouponUsed(selectedCoupon.value.id, submitRes.id);
    }
    await cart.clear();
    await orders.loadOrders();
    successOrder.value = submitRes;
    location.value = "";

    window.setTimeout(() => {
      router.push("/orders");
    }, 1000);
  } finally {
    submitting.value = false;
  }
}

onMounted(async () => {
  await shop.loadStatus();
  if (auth.isLoggedIn) {
    await Promise.all([cart.loadCart(), coupons.loadMyCoupons(), orders.loadOrders()]);
  }
});
</script>

<style scoped>
.checkout-page {
  display: grid;
  gap: 16px;
}

.checkout-page__hero,
.checkout-page__block,
.checkout-page__summary,
.checkout-page__result {
  padding: 20px;
  border-radius: var(--radius-xl);
}

.checkout-page__hero p:last-child {
  margin-top: 10px;
  color: var(--color-text-muted);
}

.checkout-page__mode,
.checkout-page__summary,
.checkout-page__price {
  display: flex;
}

.checkout-page__mode {
  gap: 12px;
  margin-bottom: 14px;
  flex-wrap: wrap;
}

.checkout-page__mode .active {
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-soft));
  color: #fff;
}

.checkout-page__items {
  display: grid;
  gap: 12px;
  margin-bottom: 14px;
}

.checkout-page__item,
.checkout-page__summary,
.checkout-page__price {
  justify-content: space-between;
  align-items: center;
}

.checkout-page__price > div {
  display: grid;
  justify-items: end;
  gap: 4px;
}

.checkout-page__item p,
.checkout-page__hint {
  margin-top: 6px;
  color: var(--color-text-muted);
}

.checkout-page__coupon-list {
  display: grid;
  gap: 12px;
}

.checkout-page__coupon-card {
  padding: 16px;
  border-radius: var(--radius-lg);
  border: 1px solid rgba(185, 151, 91, 0.32);
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.96), rgba(252, 248, 241, 0.88));
  text-align: left;
  display: grid;
  gap: 8px;
}

.checkout-page__coupon-card div {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
}

.checkout-page__coupon-card strong {
  font-size: 26px;
  color: var(--color-primary);
}

.checkout-page__coupon-card p,
.checkout-page__coupon-card small {
  color: var(--color-text-muted);
}

.checkout-page__coupon-card.active {
  border-color: var(--color-gold);
  box-shadow: 0 0 0 2px rgba(185, 151, 91, 0.12);
}

.checkout-page__summary button {
  min-width: 140px;
}

.checkout-page__summary button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.checkout-page__summary-text {
  display: grid;
  gap: 4px;
}

.checkout-page__summary-text span {
  color: var(--color-text-muted);
}

.checkout-page__summary-text strong {
  font-size: 22px;
  color: var(--color-primary);
}

.checkout-page__result {
  display: grid;
  gap: 12px;
}

.checkout-page__closed {
  border: 1px solid rgba(143, 29, 29, 0.16);
}
</style>
