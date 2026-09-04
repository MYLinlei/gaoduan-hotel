<template>
  <Teleport to="body">
    <Transition name="fade-slide">
      <div v-if="ui.cartOpen" class="drawer">
        <div class="drawer__mask" aria-hidden="true" @click="ui.closeCart" />
        <aside ref="panel" class="drawer__panel glass-card" role="dialog" aria-modal="true" aria-labelledby="cart-drawer-title">
          <div class="section-title">
            <div>
              <p class="eyebrow">购物车</p>
              <h3 id="cart-drawer-title">已选商品</h3>
            </div>
            <button class="ghost-button" type="button" aria-label="关闭购物车" @click="ui.closeCart">关闭</button>
          </div>

          <div v-if="!auth.isLoggedIn" class="field-card">
            <p>登录后可同步购物车与优惠券状态。</p>
            <button class="primary-button drawer__auth-button" type="button" @click="auth.openLogin">立即登录</button>
          </div>

          <div v-else-if="!cart.items.length" class="drawer__empty field-card">
            当前购物车为空，请先选择商品。
          </div>

          <div v-else class="drawer__list">
            <article v-for="item in cart.items" :key="item.id" class="drawer__item field-card">
              <div class="drawer__item-main">
                <h4>{{ item.name }}</h4>
                <p v-if="item.skuSpec || item.dishFlavor">{{ item.skuSpec || item.dishFlavor }}</p>
              </div>
              <div class="drawer__item-side">
                <strong>￥{{ Number(item.amount || 0).toFixed(2) }}<small v-if="item.unit"> / {{ item.unit }}</small></strong>
                <div class="drawer__stepper">
                  <button
                    type="button"
                    :disabled="!shop.isOpen || cart.mutating || Number(item.number || 0) < 1"
                    :aria-label="`减少${item.name}数量`"
                    @click="handleDecrease(item)"
                  >-</button>
                  <span>{{ item.number }}</span>
                  <button
                    type="button"
                    :disabled="!shop.isOpen || cart.mutating || Number(item.number || 0) >= cart.maxItemQuantity"
                    :aria-label="`增加${item.name}数量，最多${cart.maxItemQuantity}件`"
                    @click="handleIncrease(item)"
                  >+</button>
                </div>
              </div>
            </article>
          </div>

          <div class="drawer__remark">
            <label class="sr-only" for="cart-remark">订单备注</label>
            <textarea
              id="cart-remark"
              v-model="cart.orderRemark"
              class="field-textarea"
              placeholder="订单备注：如配送时间、安装条件或材料颜色要求"
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

            <label class="sr-only" for="cart-coupon">选择优惠券</label>
            <select id="cart-coupon" class="field-select" :value="selectedValue" @change="handleCouponChange">
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
              <button class="ghost-button" type="button" :disabled="!cart.items.length || cart.mutating" @click="handleClear">清空</button>
              <button class="primary-button" type="button" :disabled="checkoutDisabled" @click="goCheckout">
                {{ cart.loading || cart.mutating ? "处理中…" : shop.isOpen ? "确认结算" : "暂停下单" }}
              </button>
            </div>
          </div>
          <p v-if="actionError" class="drawer__error" role="alert" aria-live="assertive">{{ actionError }}</p>
          <p v-else-if="auth.isLoggedIn && cart.checkoutMessage" class="drawer__limit" aria-live="polite">
            {{ cart.checkoutMessage }}
          </p>
        </aside>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from "vue";
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
const panel = ref(null);
const actionError = ref("");
let previouslyFocused = null;
const focusableSelector = 'button:not([disabled]), input:not([disabled]), select:not([disabled]), textarea:not([disabled]), a[href], [tabindex]:not([tabindex="-1"])';

function handleDrawerKeydown(event) {
  if (event.key === "Escape") { event.preventDefault(); ui.closeCart(); return; }
  if (event.key !== "Tab" || !panel.value) return;
  const controls = [...panel.value.querySelectorAll(focusableSelector)];
  if (!controls.length) return;
  const first = controls[0]; const last = controls[controls.length - 1];
  if (event.shiftKey && document.activeElement === first) { event.preventDefault(); last.focus(); }
  else if (!event.shiftKey && document.activeElement === last) { event.preventDefault(); first.focus(); }
}

watch(() => ui.cartOpen, async (open) => {
  if (open) {
    previouslyFocused = document.activeElement;
    await nextTick();
    document.addEventListener("keydown", handleDrawerKeydown);
    panel.value?.querySelector(focusableSelector)?.focus();
  } else {
    document.removeEventListener("keydown", handleDrawerKeydown);
    previouslyFocused?.focus?.();
    previouslyFocused = null;
  }
});
onBeforeUnmount(() => document.removeEventListener("keydown", handleDrawerKeydown));

const availableCoupons = computed(() => coupons.getAvailableCoupons(cart.totalAmount));
const selectedCoupon = computed(() => coupons.getSelectedCoupon(cart.totalAmount));
const selectedValue = computed(() => selectedCoupon.value?.id || "");
const discountAmount = computed(() => coupons.getDiscountAmount(cart.totalAmount));
const payableAmount = computed(() => Math.max(cart.totalAmount - discountAmount.value, 0));
const checkoutDisabled = computed(() =>
  !auth.isLoggedIn || !shop.isOpen || !cart.canCheckout || cart.loading || cart.mutating
);

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
  if (!cart.items.length || cart.mutating) return;
  actionError.value = "";
  try {
    await cart.clear();
    coupons.clearSelectedCoupon();
  } catch (error) {
    actionError.value = error?.message || "购物车清空失败，请稍后重试";
  }
}

async function handleIncrease(item) {
  actionError.value = "";
  try {
    await cart.increase(item);
  } catch (error) {
    actionError.value = error?.message || "商品数量更新失败，请稍后重试";
  }
}

async function handleDecrease(item) {
  actionError.value = "";
  try {
    await cart.decrease(item);
  } catch (error) {
    actionError.value = error?.message || "商品数量更新失败，请稍后重试";
  }
}

function goCheckout() {
  actionError.value = "";
  if (!auth.isLoggedIn) {
    actionError.value = "请先登录账户，再继续结算";
    auth.openLogin();
    return;
  }
  if (!shop.isOpen) {
    actionError.value = "当前暂停提交新订单，请稍后再试";
    return;
  }
  if (!cart.canCheckout) {
    actionError.value = cart.checkoutMessage;
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
  background: rgba(31, 27, 22, 0.48);
}

.drawer__panel {
  position: absolute;
  right: 0;
  bottom: 0;
  width: min(560px, 100%);
  max-height: 84vh;
  padding: 20px;
  border-radius: 0;
  border-width: 0 0 0 1px;
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
  border-radius: var(--radius-sm);
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

.drawer__error,
.drawer__limit {
  margin: 0;
  font-size: 13px;
  line-height: 1.6;
}

.drawer__error {
  color: var(--color-danger, #8f1d1d);
}

.drawer__limit {
  color: var(--color-text-muted);
}
</style>
