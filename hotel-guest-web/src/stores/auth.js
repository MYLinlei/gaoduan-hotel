import { computed, ref, watch } from "vue";
import { defineStore } from "pinia";
import { request } from "../api/http";

const TOKEN_KEY = "hotel-guest-token";
const USER_KEY = "hotel-guest-user";
const PHONE_REGEX = /^1\d{10}$/;

function readJson(key, fallback) {
  try {
    const value = window.localStorage.getItem(key);
    return value ? JSON.parse(value) : fallback;
  } catch (error) {
    return fallback;
  }
}

export const useAuthStore = defineStore("auth", () => {
  const token = ref(window.localStorage.getItem(TOKEN_KEY) || "");
  const user = ref(readJson(USER_KEY, null));
  if (user.value && !user.value.nickname && user.value.name) {
    user.value.nickname = user.value.name;
  }
  const loginOpen = ref(false);
  const loginForm = ref({
    phone: user.value?.phone || "",
    code: ""
  });
  const loggingIn = ref(false);
  const sendingCode = ref(false);
  const codeCooldown = ref(0);
  let cooldownTimer = null;

  const isLoggedIn = computed(() => Boolean(token.value));

  function normalizePhone() {
    return loginForm.value.phone?.trim() || "";
  }

  function validatePhone() {
    return PHONE_REGEX.test(normalizePhone());
  }

  function startCooldown(seconds = 60) {
    codeCooldown.value = seconds;
    if (cooldownTimer) {
      window.clearInterval(cooldownTimer);
    }
    cooldownTimer = window.setInterval(() => {
      codeCooldown.value -= 1;
      if (codeCooldown.value <= 0) {
        window.clearInterval(cooldownTimer);
        cooldownTimer = null;
      }
    }, 1000);
  }

  async function sendCode() {
    if (!validatePhone()) {
      throw new Error("请输入正确的 11 位手机号");
    }
    if (codeCooldown.value > 0) {
      return;
    }
    sendingCode.value = true;
    try {
      await request(`/user/sendCode?phone=${encodeURIComponent(normalizePhone())}`, {
        method: "POST",
        authRequired: false
      });
      startCooldown();
    } finally {
      sendingCode.value = false;
    }
  }

  async function login() {
    if (!validatePhone()) {
      throw new Error("请输入正确的 11 位手机号");
    }
    if (!/^\d{6}$/.test(loginForm.value.code?.trim() || "")) {
      throw new Error("请输入 6 位验证码");
    }

    loggingIn.value = true;
    try {
      const data = await request("/user/login", {
        method: "POST",
        authRequired: false,
        body: {
          phone: normalizePhone(),
          code: loginForm.value.code.trim()
        }
      });
      token.value = data.token;
      user.value = {
        id: data.id,
        phone: data.phone,
        nickname: data.nickname,
        name: data.nickname
      };
      loginForm.value.code = "";
      loginOpen.value = false;
      return true;
    } finally {
      loggingIn.value = false;
    }
  }

  function openLogin() {
    loginOpen.value = true;
  }

  function closeLogin() {
    loginOpen.value = false;
  }

  async function logout() {
    try {
      if (token.value) {
        await request("/user/logout", {
          method: "POST"
        });
      }
    } catch (error) {
      // Ignore logout request failure and clear local session anyway.
    } finally {
      token.value = "";
      user.value = null;
      loginForm.value.code = "";
    }
  }

  watch(token, (value) => {
    if (value) {
      window.localStorage.setItem(TOKEN_KEY, value);
    } else {
      window.localStorage.removeItem(TOKEN_KEY);
    }
  });

  watch(
    user,
    (value) => {
      if (value) {
        window.localStorage.setItem(USER_KEY, JSON.stringify(value));
        loginForm.value.phone = value.phone || "";
      } else {
        window.localStorage.removeItem(USER_KEY);
      }
    },
    { deep: true }
  );

  return {
    token,
    user,
    loginOpen,
    loginForm,
    loggingIn,
    sendingCode,
    codeCooldown,
    isLoggedIn,
    sendCode,
    login,
    openLogin,
    closeLogin,
    logout
  };
});
