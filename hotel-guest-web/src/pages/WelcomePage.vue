<template>
  <AppLayout>
    <div class="home-page">
      <section class="home-hero" aria-labelledby="home-title">
        <div class="home-hero__visual"><img :src="heroImage" alt="铺设浅色石材与木饰面的现代客厅空间" width="1536" height="832" fetchpriority="high" /></div>
        <div class="home-hero__content">
          <h1 id="home-title">把家的每一处，<br />选得明明白白</h1>
          <p>从瓷砖、卫浴到木地板与橱柜，把规格、材质和计价方式放在一起比较，按标价直接选购。</p>
          <form class="hero-search" role="search" @submit.prevent="searchProducts">
            <label class="sr-only" for="hero-search">搜索家装建材</label><input id="hero-search" v-model="heroKeyword" placeholder="搜索商品、材质、规格或用途" /><button type="submit"><IconSymbol name="search" /> 搜索</button>
          </form>
          <button class="home-hero__action" type="button" @click="goMenu">按空间选材 <IconSymbol name="arrow" /></button>
        </div>
      </section>

      <section class="material-index" aria-labelledby="category-title">
        <div class="section-heading section-heading--compact"><h2 id="category-title">四大品类，一站配齐</h2><RouterLink to="/menu">查看全部商品 <IconSymbol name="arrow" /></RouterLink></div>
        <div class="material-index__grid">
          <article v-for="category in catalog.categories" :key="category.id" class="material-item">
            <div class="material-item__head"><span>{{ category.number }}</span><h3>{{ category.name }}</h3></div>
            <button class="material-item__image" type="button" @click="goCategory(category)"><img :src="category.image" :alt="category.description" width="520" height="540" loading="lazy" /></button>
            <div class="material-item__info"><p>{{ category.description }}</p><ul><li v-for="spec in category.specs" :key="spec">{{ spec }}</li></ul><div class="material-item__buy"><span><strong>¥{{ category.price }}</strong> / {{ category.unit }}起</span><button type="button" @click="goCategory(category)">查看商品 <IconSymbol name="arrow" /></button></div></div>
          </article>
        </div>
      </section>

      <section class="campaign-grid">
        <div class="coupon-panel">
          <div class="section-heading"><div><h2>限量优惠券</h2><p>先领券，再下单更优惠</p></div><RouterLink to="/coupons">全部优惠券 <IconSymbol name="arrow" /></RouterLink></div>
          <div class="coupon-list">
            <article v-for="coupon in catalog.coupons" :key="coupon.id" class="coupon-ticket"><div><strong><small>¥</small>{{ coupon.amount }}</strong><span>满 {{ coupon.threshold }} 元可用</span></div><div><h3>{{ coupon.title }}</h3><p>{{ coupon.validity }}</p><small>{{ coupon.remaining }}</small></div><RouterLink to="/coupons">领取</RouterLink></article>
          </div>
        </div>
        <div class="ranking-panel">
          <div class="section-heading"><div><h2>热销榜</h2><p>根据近期成交与浏览趋势整理</p></div><RouterLink to="/menu">完整榜单 <IconSymbol name="arrow" /></RouterLink></div>
          <div class="ranking-tabs" role="tablist" aria-label="热销榜品类"><button v-for="name in rankingNames" :key="name" type="button" role="tab" :aria-selected="activeRanking === name" :class="{ active: activeRanking === name }" @click="activeRanking = name">{{ name }}</button></div>
          <ol class="ranking-list"><li v-for="item in activeRankingItems" :key="item.id"><b>{{ item.rank }}</b><img :src="item.image" :alt="item.name" width="72" height="72" loading="lazy" /><div><h3>{{ item.name }}</h3><p>{{ item.spec }} · {{ item.status }}</p></div><span><strong>¥{{ item.price }}</strong> / {{ item.unit }}</span><button type="button" :aria-label="`查看${item.name}`" @click="goRankingItem(item)"><IconSymbol name="arrow" /></button></li></ol>
        </div>
      </section>

      <section class="room-section" aria-labelledby="room-title">
        <div class="section-heading"><div><h2 id="room-title">按空间选材</h2><p>从正在改造的房间开始，更快找到适合的材料组合。</p></div><RouterLink to="/menu">进入全部分类 <IconSymbol name="arrow" /></RouterLink></div>
        <div class="room-grid"><button v-for="room in catalog.rooms" :key="room.name" type="button" @click="goRoom(room)"><img :src="room.image" :alt="`${room.name}选材`" width="680" height="420" loading="lazy" /><span><strong>{{ room.name }}</strong><small>{{ room.description }}</small><IconSymbol name="arrow" /></span></button></div>
      </section>

      <section class="featured-section" aria-labelledby="featured-title">
        <div class="section-heading"><div><h2 id="featured-title">本期精选商品</h2><p>把材质、规格和计价单位放在购买动作之前。</p></div><RouterLink to="/menu">浏览全部商品 <IconSymbol name="arrow" /></RouterLink></div>
        <div class="product-grid"><ProductCard v-for="product in catalog.featuredProducts" :key="product.id" :product="product" /></div>
      </section>

      <section class="guide-section">
        <div class="guide-section__intro"><h2>装修选材，不必从术语开始</h2><p>先确定空间与预算，再比较尺寸、材质、计价单位和安装条件。我们把常见问题整理为四个购买步骤。</p><RouterLink to="/service" class="primary-button">查看选材帮助</RouterLink></div>
        <ol class="guide-steps"><li><span>1</span><div><h3>测量空间</h3><p>记录长宽、门洞、管线和预留位置。</p></div></li><li><span>2</span><div><h3>确认计价</h3><p>区分元/片、元/㎡、元/套与元/延米。</p></div></li><li><span>3</span><div><h3>比较规格</h3><p>结合铺贴损耗、坑距和柜体尺寸选择 SKU。</p></div></li><li><span>4</span><div><h3>咨询交付</h3><p>下单前确认配送、安装与现场条件。</p></div></li></ol>
      </section>

      <section id="partners" class="partner-strip" aria-labelledby="partner-title"><h2 id="partner-title">合作品牌</h2><div><span v-for="brand in catalog.partnerBrands" :key="brand">{{ brand }}</span></div></section>
    </div>
  </AppLayout>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import { useRouter } from "vue-router";
import AppLayout from "../components/AppLayout.vue";
import IconSymbol from "../components/IconSymbol.vue";
import ProductCard from "../components/ProductCard.vue";
import { getHomeCatalog } from "../services/homeCatalog";

const router = useRouter();
const heroKeyword = ref(""); const activeRanking = ref("瓷砖");
const heroImage = `${import.meta.env.BASE_URL}images/hero-space.png`;
const catalog = reactive({ categories: [], coupons: [], rankings: {}, rooms: [], featuredProducts: [], partnerBrands: [] });
const rankingNames = ["瓷砖", "卫浴", "木地板", "橱柜"];
const activeRankingItems = computed(() => catalog.rankings[activeRanking.value] || []);
function goMenu() { router.push("/menu"); }
function searchProducts() { router.push({ path: "/menu", query: heroKeyword.value.trim() ? { q: heroKeyword.value.trim() } : {} }); }
function goCategory(category) { router.push({ path: "/menu", query: { category: category.name } }); }
function goRoom(room) { router.push({ path: "/menu", query: { categoryId: room.categoryId, room: room.name } }); }
function goRankingItem(item) { router.push({ path: "/menu", query: { category: activeRanking.value, q: item.name } }); }
onMounted(async () => Object.assign(catalog, await getHomeCatalog()));
</script>
