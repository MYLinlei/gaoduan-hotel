<template>
  <header class="commerce-header">
    <div class="service-strip">
      <div class="site-container service-strip__inner">
        <div><IconSymbol name="truck" /> 配送与安装咨询</div>
        <nav aria-label="服务导航">
          <RouterLink to="/service">选材帮助</RouterLink>
          <RouterLink to="/service">售后咨询</RouterLink>
          <a href="#partners">合作品牌</a>
        </nav>
      </div>
    </div>

    <div class="site-container commerce-header__main">
      <RouterLink to="/" class="brand-mark" aria-label="筑家优选首页">
        <svg viewBox="0 0 44 44" aria-hidden="true">
          <path d="M6 20 22 7l16 13v17H26V26h-8v11H6V20Z" />
          <path d="m13 17 9-7 9 7M12 23h20" />
        </svg>
        <span><strong>筑家优选</strong><small>家装建材交易平台</small></span>
      </RouterLink>

      <button class="category-trigger" type="button" @click="router.push('/menu')">
        <IconSymbol name="menu" /> 商品分类
      </button>

      <form class="global-search" role="search" @submit.prevent="submitSearch">
        <label class="sr-only" for="global-search">搜索商品</label>
        <input id="global-search" v-model="keyword" placeholder="搜索商品、品牌、材质、规格" />
        <button type="submit"><IconSymbol name="search" /> 搜索</button>
      </form>

      <nav class="commerce-actions" aria-label="交易导航">
        <RouterLink to="/coupons"><IconSymbol name="ticket" /><span>优惠券<small>领券中心</small></span></RouterLink>
        <RouterLink to="/orders"><IconSymbol name="order" /><span>我的订单<small>查看订单</small></span></RouterLink>
        <button v-if="!auth.isLoggedIn" type="button" @click="auth.openLogin"><IconSymbol name="user" /><span>账户<small>登录 / 注册</small></span></button>
        <button v-else type="button" @click="handleLogout"><IconSymbol name="user" /><span>{{ auth.user?.nickname || auth.user?.name || '我的账户' }}<small>退出登录</small></span></button>
        <button class="cart-action" type="button" @click="ui.openCart"><IconSymbol name="cart" /><span>购物车<small>{{ cart.totalCount }} 件商品</small></span><b v-if="cart.totalCount">{{ cart.totalCount }}</b></button>
      </nav>
    </div>

    <nav class="site-container category-nav" aria-label="商品品类">
      <RouterLink to="/menu">全部分类</RouterLink>
      <RouterLink to="/">首页</RouterLink>
      <RouterLink v-for="item in categoryLinks" :key="item" :to="`/menu?category=${encodeURIComponent(item)}`">{{ item }}</RouterLink>
      <RouterLink to="/coupons">优惠专区</RouterLink>
      <RouterLink to="/service">选材指南</RouterLink>
    </nav>
  </header>
</template>

<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";
import IconSymbol from "./IconSymbol.vue";
import { useAuthStore } from "../stores/auth";
import { useCartStore } from "../stores/cart";
import { useUiStore } from "../stores/ui";

const router = useRouter();
const auth = useAuthStore();
const cart = useCartStore();
const ui = useUiStore();
const keyword = ref("");
const categoryLinks = ["瓷砖", "卫浴", "木地板", "橱柜"];

function submitSearch() {
  const query = keyword.value.trim();
  router.push({ path: "/menu", query: query ? { q: query } : {} });
}

async function handleLogout() {
  await auth.logout();
}
</script>
