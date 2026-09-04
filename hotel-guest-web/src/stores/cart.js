import { computed, ref, watch } from "vue";
import { defineStore } from "pinia";
import { request } from "../api/http";
import { writeStorage, readStorage } from "../utils/storage";

const CART_REMARK_KEY = "zhujia-trade-cart-remark";
const MIN_ITEM_QUANTITY = 1;
const MAX_ITEM_QUANTITY = 99;

function normalizeQuantity(value, fallback = MIN_ITEM_QUANTITY) {
  const quantity = Number(value);
  if (!Number.isInteger(quantity)) return fallback;
  return quantity;
}

function requireQuantity(value) {
  const quantity = normalizeQuantity(value, 0);
  if (quantity < MIN_ITEM_QUANTITY || quantity > MAX_ITEM_QUANTITY) {
    throw new Error(`商品数量必须在 ${MIN_ITEM_QUANTITY} 到 ${MAX_ITEM_QUANTITY} 件之间`);
  }
  return quantity;
}

export const useCartStore = defineStore("cart", () => {
  const items = ref([]);
  const orderRemark = ref(readStorage(CART_REMARK_KEY, ""));
  const loading = ref(false);
  const mutating = ref(false);

  const totalCount = computed(() =>
    items.value.reduce((sum, item) => sum + Math.max(normalizeQuantity(item.number, 0), 0), 0)
  );

  const totalAmount = computed(() =>
    items.value.reduce((sum, item) => sum + Math.max(normalizeQuantity(item.number, 0), 0) * Number(item.amount || 0), 0)
  );

  const canCheckout = computed(() =>
    items.value.length > 0
    && items.value.every((item) => {
      const quantity = normalizeQuantity(item.number, 0);
      return quantity >= MIN_ITEM_QUANTITY && quantity <= MAX_ITEM_QUANTITY;
    })
    && totalCount.value >= MIN_ITEM_QUANTITY
  );

  const checkoutMessage = computed(() => {
    if (!items.value.length || totalCount.value < MIN_ITEM_QUANTITY) return "购物车至少需要 1 件商品才能结算";
    if (!canCheckout.value) return `单件商品数量必须在 ${MIN_ITEM_QUANTITY} 到 ${MAX_ITEM_QUANTITY} 件之间`;
    return "";
  });

  async function loadCart() {
    loading.value = true;
    try {
      items.value = await request("/user/shoppingCart/list");
    } finally {
      loading.value = false;
    }
  }

  async function addDish(dishId, payload = {}) {
    if (!dishId) throw new Error("商品信息不完整，请刷新页面后重试");
    const quantity = requireQuantity(payload.quantity ?? 1);
    mutating.value = true;
    try {
      await request("/user/shoppingCart/add", {
        method: "POST",
        body: {
          dishId,
          dishFlavor: payload.remark?.trim() || "",
          quantity
        }
      });
      await loadCart();
    } finally {
      mutating.value = false;
    }
  }

  async function addProduct(productId, skuId, payload = {}) {
    if (!productId || !skuId) throw new Error("商品规格信息不完整，请重新选择规格");
    const quantity = requireQuantity(payload.quantity ?? 1);
    mutating.value = true;
    try {
      await request("/user/shoppingCart/add", {
        method: "POST",
        body: { productId, skuId, quantity }
      });
      await loadCart();
    } finally {
      mutating.value = false;
    }
  }

  async function increase(item) {
    if (normalizeQuantity(item.number, 0) >= MAX_ITEM_QUANTITY) {
      throw new Error(`单件商品最多购买 ${MAX_ITEM_QUANTITY} 件`);
    }
    mutating.value = true;
    try {
      await request("/user/shoppingCart/add", {
        method: "POST",
        body: item.productId && item.skuId
          ? { productId: item.productId, skuId: item.skuId }
          : { dishId: item.dishId, setmealId: item.setmealId, dishFlavor: item.dishFlavor || "" }
      });
      await loadCart();
    } finally {
      mutating.value = false;
    }
  }

  async function decrease(item) {
    mutating.value = true;
    try {
      await request("/user/shoppingCart/sub", {
        method: "POST",
        body: item.productId && item.skuId
          ? { productId: item.productId, skuId: item.skuId }
          : { dishId: item.dishId, setmealId: item.setmealId, dishFlavor: item.dishFlavor || "" }
      });
      await loadCart();
    } finally {
      mutating.value = false;
    }
  }

  async function clear() {
    mutating.value = true;
    try {
      await request("/user/shoppingCart/clean", {
        method: "DELETE"
      });
      items.value = [];
      orderRemark.value = "";
    } finally {
      mutating.value = false;
    }
  }

  watch(orderRemark, (value) => writeStorage(CART_REMARK_KEY, value));

  return {
    items,
    orderRemark,
    loading,
    mutating,
    totalCount,
    totalAmount,
    canCheckout,
    checkoutMessage,
    maxItemQuantity: MAX_ITEM_QUANTITY,
    loadCart,
    addDish,
    addProduct,
    increase,
    decrease,
    clear
  };
});
