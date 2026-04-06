import { computed, ref, watch } from "vue";
import { defineStore } from "pinia";
import { request } from "../api/http";
import { writeStorage, readStorage } from "../utils/storage";

const CART_REMARK_KEY = "hotel-guest-cart-remark";

export const useCartStore = defineStore("cart", () => {
  const items = ref([]);
  const orderRemark = ref(readStorage(CART_REMARK_KEY, ""));
  const loading = ref(false);

  const totalCount = computed(() =>
    items.value.reduce((sum, item) => sum + Number(item.number || 0), 0)
  );

  const totalAmount = computed(() =>
    items.value.reduce((sum, item) => sum + Number(item.number || 0) * Number(item.amount || 0), 0)
  );

  async function loadCart() {
    loading.value = true;
    try {
      items.value = await request("/user/shoppingCart/list");
    } finally {
      loading.value = false;
    }
  }

  async function addDish(dishId, payload = {}) {
    await request("/user/shoppingCart/add", {
      method: "POST",
      body: {
        dishId,
        dishFlavor: payload.remark?.trim() || ""
      }
    });
    await loadCart();
  }

  async function increase(item) {
    await request("/user/shoppingCart/add", {
      method: "POST",
      body: {
        dishId: item.dishId,
        setmealId: item.setmealId,
        dishFlavor: item.dishFlavor || ""
      }
    });
    await loadCart();
  }

  async function decrease(item) {
    await request("/user/shoppingCart/sub", {
      method: "POST",
      body: {
        dishId: item.dishId,
        setmealId: item.setmealId,
        dishFlavor: item.dishFlavor || ""
      }
    });
    await loadCart();
  }

  async function clear() {
    await request("/user/shoppingCart/clean", {
      method: "DELETE"
    });
    items.value = [];
    orderRemark.value = "";
  }

  watch(orderRemark, (value) => writeStorage(CART_REMARK_KEY, value));

  return {
    items,
    orderRemark,
    loading,
    totalCount,
    totalAmount,
    loadCart,
    addDish,
    increase,
    decrease,
    clear
  };
});
