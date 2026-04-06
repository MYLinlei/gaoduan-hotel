import { computed, ref } from "vue";
import { defineStore } from "pinia";
import { request } from "../api/http";

export const useShopStore = defineStore("shop", () => {
  const status = ref(1);
  const loading = ref(false);
  const loaded = ref(false);

  const isOpen = computed(() => status.value !== 0);
  const statusText = computed(() => (isOpen.value ? "营业中" : "打烊中"));

  async function loadStatus() {
    loading.value = true;
    try {
      const value = await request("/user/shop/status", {
        authRequired: false
      });
      status.value = value === 0 ? 0 : 1;
      loaded.value = true;
      return status.value;
    } finally {
      loading.value = false;
    }
  }

  return {
    status,
    loading,
    loaded,
    isOpen,
    statusText,
    loadStatus
  };
});
