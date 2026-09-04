<template>
  <Teleport to="body">
    <Transition name="fade-slide">
      <div v-if="auth.loginOpen" class="auth-dialog">
        <div class="auth-dialog__mask" aria-hidden="true" @click="auth.closeLogin" />
        <section ref="panel" class="auth-dialog__panel glass-card" role="dialog" aria-modal="true" aria-labelledby="auth-dialog-title">
          <div class="section-title">
            <div>
              <p class="eyebrow">账户登录</p>
              <h3 id="auth-dialog-title">短信验证码登录</h3>
            </div>
            <button class="ghost-button" type="button" aria-label="关闭登录窗口" @click="auth.closeLogin">关闭</button>
          </div>

          <label class="sr-only" for="login-phone">手机号</label>
          <input
            id="login-phone"
            v-model="auth.loginForm.phone"
            class="field-input"
            maxlength="11"
            placeholder="请输入手机号"
          />

          <div class="auth-dialog__code-row">
            <label class="sr-only" for="login-code">短信验证码</label>
            <input
              id="login-code"
              v-model="auth.loginForm.code"
              class="field-input"
              maxlength="6"
              placeholder="请输入 6 位验证码"
            />
            <button
              class="secondary-button auth-dialog__code-button"
              :disabled="auth.sendingCode || auth.codeCooldown > 0"
              @click="handleSendCode"
            >
              {{ auth.codeCooldown > 0 ? `${auth.codeCooldown}s 后重试` : auth.sendingCode ? "发送中..." : "获取验证码" }}
            </button>
          </div>

          <p class="auth-dialog__hint">验证码发送后请在有效时间内完成登录；未收到时可在倒计时结束后重新获取。</p>

          <button class="primary-button" :disabled="auth.loggingIn" @click="handleLogin">
            {{ auth.loggingIn ? "登录中..." : "立即登录" }}
          </button>
        </section>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { nextTick, onBeforeUnmount, ref, watch } from "vue";
import { useAuthStore } from "../stores/auth";
import { useCartStore } from "../stores/cart";
import { useCouponsStore } from "../stores/coupons";
import { useOrdersStore } from "../stores/orders";

const auth = useAuthStore();
const cart = useCartStore();
const coupons = useCouponsStore();
const orders = useOrdersStore();
const panel = ref(null);
let previouslyFocused = null;
const focusableSelector = 'button:not([disabled]), input:not([disabled]), select:not([disabled]), textarea:not([disabled]), a[href], [tabindex]:not([tabindex="-1"])';

function handleDialogKeydown(event) {
  if (event.key === "Escape") { event.preventDefault(); auth.closeLogin(); return; }
  if (event.key !== "Tab" || !panel.value) return;
  const controls = [...panel.value.querySelectorAll(focusableSelector)];
  if (!controls.length) return;
  const first = controls[0]; const last = controls[controls.length - 1];
  if (event.shiftKey && document.activeElement === first) { event.preventDefault(); last.focus(); }
  else if (!event.shiftKey && document.activeElement === last) { event.preventDefault(); first.focus(); }
}

watch(() => auth.loginOpen, async (open) => {
  if (open) {
    previouslyFocused = document.activeElement;
    await nextTick();
    document.addEventListener("keydown", handleDialogKeydown);
    panel.value?.querySelector(focusableSelector)?.focus();
  } else {
    document.removeEventListener("keydown", handleDialogKeydown);
    previouslyFocused?.focus?.();
    previouslyFocused = null;
  }
});
onBeforeUnmount(() => document.removeEventListener("keydown", handleDialogKeydown));

async function handleSendCode() {
  try {
    await auth.sendCode();
    window.alert("验证码已发送，请留意短信通知。");
  } catch (error) {
    window.alert(error.message);
  }
}

async function handleLogin() {
  try {
    await auth.login();
    await Promise.all([cart.loadCart(), coupons.loadMyCoupons(), orders.loadOrders()]);
  } catch (error) {
    window.alert(error.message);
  }
}
</script>

<style scoped>
.auth-dialog {
  position: fixed;
  inset: 0;
  z-index: 40;
}

.auth-dialog__mask {
  position: absolute;
  inset: 0;
  background: rgba(31, 27, 22, 0.48);
}

.auth-dialog__panel {
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  width: min(420px, calc(100% - 28px));
  padding: 22px;
  border-radius: var(--radius-xl);
  display: grid;
  gap: 14px;
}

.auth-dialog__code-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 10px;
}

.auth-dialog__code-button {
  min-width: 116px;
}

.auth-dialog__hint {
  color: var(--color-text-muted);
  font-size: 13px;
  line-height: 1.6;
}

@media (max-width: 560px) {
  .auth-dialog__code-row {
    grid-template-columns: 1fr;
  }
}
</style>
