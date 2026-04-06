<template>
  <Teleport to="body">
    <Transition name="fade-slide">
      <div v-if="auth.loginOpen" class="auth-dialog">
        <div class="auth-dialog__mask" @click="auth.closeLogin" />
        <section class="auth-dialog__panel glass-card">
          <div class="section-title">
            <div>
              <p class="eyebrow">住客登录</p>
              <h3>短信验证码登录</h3>
            </div>
            <button class="ghost-button" @click="auth.closeLogin">关闭</button>
          </div>

          <input
            v-model="auth.loginForm.phone"
            class="field-input"
            maxlength="11"
            placeholder="请输入手机号"
          />

          <div class="auth-dialog__code-row">
            <input
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

          <p class="auth-dialog__hint">验证码会写入 Redis，当前为开发联调模式，后续可接短信 SDK。</p>

          <button class="primary-button" :disabled="auth.loggingIn" @click="handleLogin">
            {{ auth.loggingIn ? "登录中..." : "立即登录" }}
          </button>
        </section>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { useAuthStore } from "../stores/auth";
import { useCartStore } from "../stores/cart";
import { useCouponsStore } from "../stores/coupons";
import { useOrdersStore } from "../stores/orders";

const auth = useAuthStore();
const cart = useCartStore();
const coupons = useCouponsStore();
const orders = useOrdersStore();

async function handleSendCode() {
  try {
    await auth.sendCode();
    window.alert("验证码已发送，请查看短信服务或后端日志。");
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
  background: rgba(7, 18, 34, 0.34);
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
