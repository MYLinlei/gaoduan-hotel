<template>
  <AppLayout>
    <section class="checkout-page">
      <header class="glass-card checkout-page__hero">
        <p class="eyebrow">提交订单</p>
        <h1>确认订单信息</h1>
        <p>请核对收货方式、商品明细与优惠信息，提交后可在“我的订单”查看进度。</p>
      </header>

      <section v-if="!auth.isLoggedIn" class="glass-card checkout-page__block">
        <h2>请先登录</h2>
        <p class="checkout-page__hint">登录后可继续结算，并同步购物车、优惠券与订单状态。</p>
        <button class="primary-button" type="button" @click="auth.openLogin">账户登录</button>
      </section>

      <template v-else>
        <section v-if="!shop.isOpen" class="glass-card checkout-page__block checkout-page__closed">
          <h2>当前暂停提交订单</h2>
          <p class="checkout-page__hint">已选择的商品会继续保留，请稍后再试。</p>
        </section>

        <section class="glass-card checkout-page__block">
          <div class="section-title">
            <h2>收货方式</h2>
          </div>
          <div class="checkout-page__mode">
            <button class="ghost-button" :class="{ active: deliveryType === '门店自提' }" @click="deliveryType = '门店自提'">
              门店自提
            </button>
            <button
              class="ghost-button"
              :class="{ active: deliveryType === '配送到家' }"
              @click="deliveryType = '配送到家'"
            >
              配送到家
            </button>
          </div>

          <label class="sr-only" for="checkout-location">{{ deliveryType === '门店自提' ? '自提人姓名与联系电话' : '详细配送地址' }}</label>
          <input
            id="checkout-location"
            v-model="location"
            class="field-input"
            :placeholder="deliveryType === '门店自提' ? '请输入自提人姓名与联系电话' : '请输入详细配送地址'"
          />
        </section>

        <section class="glass-card checkout-page__block">
          <div class="section-title">
            <h2>订单明细</h2>
          </div>
          <div v-if="!cart.items.length" class="field-card checkout-page__empty">
            <span>购物车为空，至少选择 1 件商品后才能结算。</span>
            <RouterLink class="secondary-button" to="/menu">返回选购</RouterLink>
          </div>
          <div v-else class="checkout-page__items">
            <article v-for="item in cart.items" :key="item.id" class="field-card checkout-page__item">
              <div>
                <h3>{{ item.name }}</h3>
                <p v-if="item.skuSpec || item.dishFlavor">{{ item.skuSpec || item.dishFlavor }}</p>
              </div>
              <div class="checkout-page__price">
                <div>
                  <span>￥{{ Number(item.amount || 0).toFixed(2) }}{{ item.unit ? ` / ${item.unit}` : "" }}</span>
                  <strong>x{{ item.number }}</strong>
                </div>
                <strong>￥{{ (Number(item.amount || 0) * Number(item.number || 0)).toFixed(2) }}</strong>
              </div>
            </article>
          </div>
          <label class="sr-only" for="checkout-remark">订单备注</label>
          <textarea
            id="checkout-remark"
            v-model="cart.orderRemark"
            class="field-textarea"
            placeholder="订单备注：请填写配送时间、安装条件或材料颜色要求"
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
            {{ submitting || cart.loading || cart.mutating ? "处理中…" : shop.isOpen ? "提交订单" : "暂停下单" }}
          </button>
        </section>
        <p v-if="checkoutError" class="checkout-page__error" role="alert" aria-live="assertive">
          {{ checkoutError }}
        </p>

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

const deliveryType = ref("配送到家");
const location = ref("");
const paymentMethod = ref(1);
const paymentOptions = [
  { label: "微信支付", value: 1 },
  { label: "支付宝", value: 2 },
  { label: "到店支付", value: 3 }
];
const submitting = ref(false);
const successOrder = ref(null);
const checkoutError = ref("");

const availableCoupons = computed(() => coupons.getAvailableCoupons(cart.totalAmount));
const selectedCoupon = computed(() => coupons.getSelectedCoupon(cart.totalAmount));
const discountAmount = computed(() => coupons.getDiscountAmount(cart.totalAmount));
const payableAmount = computed(() => Math.max(cart.totalAmount - discountAmount.value, 0));
const submitDisabled = computed(() =>
  submitting.value || cart.loading || cart.mutating || !cart.canCheckout || !shop.isOpen
);

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

async function ensureDeliveryAddress() {
  const list = await request("/user/addressBook/list");
  const existing = list.find((item) => item.label === "配送地址") || list.find((item) => item.isDefault === 1);
  const payload = {
    consignee: auth.user?.name || "顾客",
    phone: auth.user?.phone || "13800138000",
    sex: "1",
    detail: location.value.trim(),
    label: "配送地址",
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
  if (submitting.value || cart.loading || cart.mutating) return;
  checkoutError.value = "";
  if (!cart.canCheckout) {
    checkoutError.value = cart.checkoutMessage;
    return;
  }
  if (!location.value.trim()) {
    checkoutError.value = deliveryType.value === "门店自提" ? "请填写自提联系人" : "请填写详细配送地址";
    return;
  }

  submitting.value = true;
  try {
    await shop.loadStatus();
    if (!shop.isOpen) {
      checkoutError.value = "当前暂停提交新订单，请稍后再试";
      return;
    }
    const addressBookId = deliveryType.value === "配送到家" ? await ensureDeliveryAddress() : null;
    const submitRes = await request("/user/order/submit", {
      method: "POST",
      body: {
        orderType: deliveryType.value === "门店自提" ? 2 : 1,
        addressBookId,
        tableNo: deliveryType.value === "门店自提" ? location.value.trim() : "",
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
  } catch (error) {
    checkoutError.value = error?.message || "订单提交失败，请核对商品与收货信息后重试";
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
  background: var(--color-ink);
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
  border: 1px solid var(--color-line);
  background: var(--color-paper);
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
  color: var(--color-accent-dark);
}

.checkout-page__coupon-card p,
.checkout-page__coupon-card small {
  color: var(--color-text-muted);
}

.checkout-page__coupon-card.active {
  border-color: var(--color-accent);
  box-shadow: inset 3px 0 0 var(--color-accent);
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

.checkout-page__empty {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.checkout-page__error {
  margin: 0;
  padding: 12px 16px;
  border-left: 3px solid var(--color-danger, #8f1d1d);
  background: rgba(143, 29, 29, 0.06);
  color: var(--color-danger, #8f1d1d);
  line-height: 1.6;
}
</style>
