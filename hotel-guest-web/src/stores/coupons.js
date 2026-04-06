import { computed, ref, watch } from "vue";
import { defineStore } from "pinia";
import { request } from "../api/http";
import { readStorage, writeStorage } from "../utils/storage";

const SELECTED_KEY = "hotel-guest-coupons-selected";

export const useCouponsStore = defineStore("coupons", () => {
  const couponCenter = ref([]);
  const myCouponList = ref([]);
  const selectedCouponId = ref(readStorage(SELECTED_KEY, null));
  const loading = ref(false);

  const allCoupons = computed(() => couponCenter.value);
  const myCoupons = computed(() => myCouponList.value);

  async function loadCouponCenter() {
    couponCenter.value = await request("/user/hotelHighVoucher/list", {
      authRequired: false
    });
  }

  async function loadMyCoupons() {
    myCouponList.value = await request("/user/hotelHighVoucher/my");
  }

  function isClaimed(couponId) {
    return myCouponList.value.some((item) => item.voucherId === couponId);
  }

  async function claimCoupon(couponId) {
    await request(`/user/hotelHighVoucher/seckill/${couponId}`, {
      method: "POST"
    });
    await loadMyCoupons();
    return true;
  }

  function getAvailableCoupons(amount) {
    return myCouponList.value.filter((coupon) => coupon.status === 1 && Number(amount) >= Number(coupon.thresholdAmount || 0));
  }

  function getSelectedCoupon(amount) {
    if (!selectedCouponId.value) {
      return null;
    }
    return getAvailableCoupons(amount).find((coupon) => coupon.id === selectedCouponId.value) || null;
  }

  function getDiscountAmount(amount) {
    const coupon = getSelectedCoupon(amount);
    return coupon ? Number(coupon.discountAmount || 0) : 0;
  }

  function selectCoupon(couponId, amount) {
    if (!couponId) {
      selectedCouponId.value = null;
      return true;
    }
    const exists = getAvailableCoupons(amount).some((coupon) => coupon.id === Number(couponId) || coupon.id === couponId);
    if (!exists) {
      return false;
    }
    selectedCouponId.value = Number(couponId);
    return true;
  }

  function syncSelectedCoupon(amount) {
    if (selectedCouponId.value && !getSelectedCoupon(amount)) {
      selectedCouponId.value = null;
    }
  }

  function clearSelectedCoupon() {
    selectedCouponId.value = null;
  }

  function markCouponUsed(couponId, orderId) {
    myCouponList.value = myCouponList.value.map((item) =>
      item.id === couponId
        ? {
            ...item,
            status: 3,
            orderId
          }
        : item
    );
    selectedCouponId.value = null;
  }

  watch(selectedCouponId, (value) => writeStorage(SELECTED_KEY, value));

  return {
    couponCenter,
    myCouponList,
    loading,
    allCoupons,
    myCoupons,
    selectedCouponId,
    loadCouponCenter,
    loadMyCoupons,
    isClaimed,
    claimCoupon,
    getAvailableCoupons,
    getSelectedCoupon,
    getDiscountAmount,
    selectCoupon,
    syncSelectedCoupon,
    clearSelectedCoupon,
    markCouponUsed
  };
});
