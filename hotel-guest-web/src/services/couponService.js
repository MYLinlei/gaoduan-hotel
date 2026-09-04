import { request } from "../api/http";

function timeValue(value) { return value ? new Date(value).getTime() : null; }
function formatDate(value) { if (!value) return "以券面信息为准"; const date = new Date(value); return Number.isNaN(date.getTime()) ? String(value).slice(0, 10) : date.toLocaleDateString("zh-CN", { year: "numeric", month: "2-digit", day: "2-digit" }); }

function scopeFromCoupon(coupon) {
  const channel = String(coupon.channelType || "").toUpperCase();
  if (channel.includes("TILE")) return { key: "tile", label: "瓷砖" };
  if (channel.includes("BATH")) return { key: "bath", label: "卫浴" };
  if (channel.includes("FLOOR")) return { key: "floor", label: "木地板" };
  if (channel.includes("CABINET")) return { key: "cabinet", label: "橱柜" };
  if (channel.includes("BANQUET") || channel.includes("WHOLE")) return { key: "whole", label: "整屋采购" };
  return { key: "all", label: "全场通用" };
}

export function normalizeCoupon(coupon, claimed = false) {
  const now = Date.now(); const start = timeValue(coupon.seckillBeginTime || coupon.beginTime); const end = timeValue(coupon.seckillEndTime || coupon.endTime);
  let claimState = "available"; let actionLabel = "立即领取"; let claimable = true;
  if (claimed) { claimState = "claimed"; actionLabel = "已领取"; claimable = false; }
  else if (Number(coupon.status) === 0 || (end && now > end)) { claimState = "expired"; actionLabel = "已过期"; claimable = false; }
  else if (start && now < start) { claimState = "upcoming"; actionLabel = "未开始"; claimable = false; }
  else if (Number(coupon.availableStock) <= 0) { claimState = "sold-out"; actionLabel = "已抢完"; claimable = false; }
  const scope = scopeFromCoupon(coupon);
  return { id: coupon.id, title: coupon.name, amount: Number(coupon.actualValue || 0), threshold: Number(coupon.payValue || 0), scope: scope.label, categoryKey: scope.key, validity: `${formatDate(coupon.beginTime)} 至 ${formatDate(coupon.endTime)}`, rules: coupon.rules || "适用范围与使用条件以券面信息为准。", claimState, actionLabel, claimable, raw: coupon };
}

export async function fetchCouponCatalog(claimedIds = []) {
  const rows = await request("/user/hotelHighVoucher/list", { authRequired: false });
  return (rows || []).map((coupon) => normalizeCoupon(coupon, claimedIds.includes(coupon.id)));
}
export function fetchMyCoupons() { return request("/user/hotelHighVoucher/my"); }
export function claimCouponById(id) { return request(`/user/hotelHighVoucher/seckill/${id}`, { method: "POST" }); }
