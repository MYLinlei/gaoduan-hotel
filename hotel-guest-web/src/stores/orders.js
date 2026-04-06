import { computed, ref } from "vue";
import { defineStore } from "pinia";
import { request } from "../api/http";
import { isActiveOrder, mapOrderStatus } from "../utils/order";

export const useOrdersStore = defineStore("orders", () => {
  const orders = ref([]);
  const loading = ref(false);

  const normalizedOrders = computed(() =>
    orders.value.map((order) => ({
      ...order,
      displayStatus: mapOrderStatus(order),
      payableAmount: Number(order.actualPayAmount ?? order.amount ?? 0),
      originalAmount: Number(order.amount ?? 0),
      discountAmount: Number(order.couponAmount ?? 0),
      items: order.orderDetailList || [],
      location: order.orderType === 2 ? order.tableNo : order.address || order.phone || "-"
    }))
  );

  const activeOrders = computed(() => normalizedOrders.value.filter(isActiveOrder));
  const historyOrders = computed(() => normalizedOrders.value.filter((order) => !isActiveOrder(order)));

  async function loadOrders() {
    loading.value = true;
    try {
      const page = await request("/user/order/historyOrders?page=1&pageSize=50");
      orders.value = page.records || [];
    } finally {
      loading.value = false;
    }
  }

  async function loadOrderDetail(id) {
    return request(`/user/order/details/${id}`);
  }

  async function cancelOrder(id) {
    await request(`/user/order/cancel/${id}`, {
      method: "PUT"
    });
    await loadOrders();
  }

  async function repetition(id) {
    await request(`/user/order/repetition/${id}`, {
      method: "POST"
    });
  }

  return {
    orders,
    loading,
    normalizedOrders,
    activeOrders,
    historyOrders,
    loadOrders,
    loadOrderDetail,
    cancelOrder,
    repetition
  };
});
