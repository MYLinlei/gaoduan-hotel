export function mapOrderStatus(order) {
  if (order.orderType === 2) {
    if (order.status === 7) return "待接单";
    if (order.status === 8) return "制作中";
    if (order.status === 9) return "已上桌";
  } else {
    if (order.status === 2) return "待接单";
    if (order.status === 3 || order.status === 4) return "派送中";
  }

  if (order.status === 5) return "已完成";
  if (order.status === 6) return "已取消";
  if (order.status === 1) return "待支付";
  return "处理中";
}

export function isActiveOrder(order) {
  return ["待接单", "制作中", "派送中", "已上桌", "待支付", "处理中"].includes(order.displayStatus);
}
