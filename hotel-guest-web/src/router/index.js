import { createRouter, createWebHashHistory } from "vue-router";

const routes = [
  {
    path: "/",
    name: "welcome",
    component: () => import("../pages/WelcomePage.vue")
  },
  {
    path: "/menu",
    name: "menu",
    component: () => import("../pages/MenuPage.vue")
  },
  {
    path: "/dish/:id",
    name: "dish-detail",
    component: () => import("../pages/DishDetailPage.vue")
  },
  {
    path: "/checkout",
    name: "checkout",
    component: () => import("../pages/CheckoutPage.vue")
  },
  {
    path: "/coupons",
    name: "coupon-center",
    component: () => import("../pages/CouponCenterPage.vue")
  },
  {
    path: "/my-coupons",
    name: "my-coupons",
    component: () => import("../pages/MyCouponsPage.vue")
  },
  {
    path: "/orders",
    name: "orders",
    component: () => import("../pages/OrdersPage.vue")
  },
  {
    path: "/service",
    name: "service",
    component: () => import("../pages/ServicePage.vue")
  }
];

const router = createRouter({
  history: createWebHashHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior() {
    return { top: 0, behavior: "smooth" };
  }
});

export default router;
