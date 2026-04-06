<template>
  <Teleport to="body">
    <Transition name="fade-slide">
      <div v-if="ui.cartOpen" class="drawer">
        <div class="drawer__mask" @click="ui.closeCart" />
        <aside class="drawer__panel glass-card">
          <div class="section-title">
            <div>
              <p class="eyebrow">购物车</p>
              <h3>已点菜品</h3>
            </div>
            <button class="ghost-button" @click="ui.closeCart">关闭</button>
          </div>

          <div v-if="!auth.isLoggedIn" class="field-card">
            <p>登录后可同步真实购物车与优惠券状态。</p>
            <button class="primary-button drawer__auth-button" @click="auth.openLogin">立即登录</button>
          </div>

          <div v-else-if="!cart.items.length" class="drawer__empty field-card">
            当前购物车为空，请先选择菜品。
          </div>

          <div v-else class="drawer__list">
            <article v-for="item in cart.items" :key="item.id" class="drawer__item field-card">
              <div class="drawer__item-main">
                <h4>{{ item.name }}</h4>
                <p v-if="item.dishFlavor">{{ item.dishFlavor }}</p>
              </div>
              <div class="drawer__item-side">
                <strong>￥{{ Number(item.amount || 0).toFixed(2) }}</strong>
                <div class="drawer__stepper">
                  <button :disabled="!shop.isOpen" @click="cart.decrease(item)">-</button>
                  <span>{{ item.number }}</span>
                  <button :disabled="!shop.isOpen" @click="cart.increase(item)">+</button>
                </div>
              </div>
            </article>
          </div>

          <div class="drawer__remark">
            <textarea
              v-model="cart.orderRemark"
              class="field-textarea"
              placeholder="整单备注：如先送饮品、安静敲门、少辣等"
            />
          </div>

          <section v-if="auth.isLoggedIn" class="field-card drawer__coupon">
            <div class="section-title">
              <div>
                <p class="eyebrow">优惠券</p>
                <h4>选择优惠券</h4>
              </div>
              <RouterLink class="secondary-button drawer__coupon-link" to="/coupons" @click="ui.closeCart">
                去领券
              </RouterLink>
            </div>

            <select class="field-select" :value="selectedValue" @change="handleCouponChange">
              <option value="">不使用优惠券</option>
              <option v-for="coupon in availableCoupons" :key="coupon.id" :value="coupon.id">
                {{ coupon.voucherName }} · 满{{ coupon.thresholdAmount }} 减{{ coupon.discountAmount }}
              </option>
            </select>

            <p v-if="selectedCoupon" class="drawer__coupon-tip">
              已选 {{ selectedCoupon.voucherName }}，可优惠 ￥{{ Number(selectedCoupon.discountAmount).toFixed(2) }}
            </p>
            <p v-else-if="cart.totalAmount && !availableCoupons.length" class="drawer__coupon-tip">
              当前订单暂无可用优惠券，可前往领券中心查看。
            </p>
          </section>

          <div class="drawer__footer">
            <div class="drawer__summary">
              <span>原价 ￥{{ cart.totalAmount.toFixed(2) }}</span>
              <span>优惠 ￥{{ discountAmount.toFixed(2) }}</span>
              <strong>实付 ￥{{ payableAmount.toFixed(2) }}</strong>
            </div>
            <div class="drawer__footer-actions">
              <button class="ghost-button" @click="handleClear">清空</button>
              <button class="primary-button" :disabled="!shop.isOpen" @click="goCheckout">
                {{ shop.isOpen ? "确认结算" : "打烊中" }}
              </button>
            </div>
          </div>
        </aside>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { computed, onMounted, watch } from "vue";
import { useRouter } from "vue-router";
import { useAuthStore } from "../stores/auth";
import { useCartStore } from "../stores/cart";
import { useCouponsStore } from "../stores/coupons";
import { useShopStore } from "../stores/shop";
import { useUiStore } from "../stores/ui";

const auth = useAuthStore();
const cart = useCartStore();
const coupons = useCouponsStore();
const shop = useShopStore();
const ui = useUiStore();
const router = useRouter();

const availableCoupons = computed(() => coupons.getAvailableCoupons(cart.totalAmount));
const selectedCoupon = computed(() => coupons.getSelectedCoupon(cart.totalAmount));
const selectedValue = computed(() => selectedCoupon.value?.id || "");
const discountAmount = computed(() => coupons.getDiscountAmount(cart.totalAmount));
const payableAmount = computed(() => Math.max(cart.totalAmount - discountAmount.value, 0));

watch(
  () => cart.totalAmount,
  (amount) => coupons.syncSelectedCoupon(amount),
  { immediate: true }
);

onMounted(async () => {
  if (auth.isLoggedIn) {
    await Promise.all([cart.loadCart(), coupons.loadMyCoupons(), shop.loadStatus()]);
  }
});

function handleCouponChange(event) {
  coupons.selectCoupon(event.target.value || null, cart.totalAmount);
}

async function handleClear() {
  await cart.clear();
  coupons.clearSelectedCoupon();
}

function goCheckout() {
  if (!shop.isOpen) {
    window.alert("当前酒店已打烊，暂不支持结算。");
    return;
  }
  ui.closeCart();
  router.push("/checkout");
}
</script>

<style scoped>
.drawer {
  position: fixed;
  inset: 0;
  z-index: 30;
}

.drawer__mask {
  position: absolute;
  inset: 0;
  background: rgba(7, 18, 34, 0.34);
}

.drawer__panel {
  position: absolute;
  right: 0;
  bottom: 0;
  width: min(560px, 100%);
  max-height: 84vh;
  padding: 20px;
  border-radius: 28px 28px 0 0;
  display: grid;
  gap: 14px;
  overflow: auto;
}

.drawer__list {
  display: grid;
  gap: 12px;
}

.drawer__item,
.drawer__item-side,
.drawer__stepper,
.drawer__footer,
.drawer__footer-actions {
  display: flex;
}

.drawer__item,
.drawer__footer {
  justify-content: space-between;
  gap: 12px;
}

.drawer__item-side,
.drawer__stepper {
  align-items: center;
  gap: 10px;
}

.drawer__stepper button {
  width: 32px;
  height: 32px;
  border-radius: 999px;
  background: var(--color-gold-soft);
  color: var(--color-primary);
}

.drawer__item-main p {
  margin-top: 6px;
  color: var(--color-text-muted);
  font-size: 13px;
}

.drawer__coupon {
  display: grid;
  gap: 12px;
}

.drawer__coupon-link {
  padding: 10px 14px;
}

.drawer__coupon-tip {
  color: var(--color-text-muted);
  line-height: 1.6;
}

.drawer__summary {
  display: grid;
  gap: 4px;
}

.drawer__summary span {
  color: var(--color-text-muted);
  font-size: 13px;
}

.drawer__summary strong {
  font-size: 20px;
  color: var(--color-primary);
}

.drawer__footer {
  align-items: flex-end;
}

.drawer__footer-actions {
  gap: 10px;
}

.drawer__auth-button {
  margin-top: 12px;
}
</style>
