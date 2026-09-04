<template>
  <AppLayout>
    <article class="guide-page">
      <Breadcrumbs :items="[{ label: '首页', to: '/' }, { label: '选材指南' }]" />
      <header class="guide-page__header"><div><h1>从空间开始，读懂材料与规格</h1><p>先测量，再确认计价方式、商品规格和交付条件。这里把家装建材购买过程整理成可以逐步核对的选材手册。</p></div><RouterLink class="primary-button" to="/menu">开始选购</RouterLink></header>
      <GuideIndex :items="guideIndex" />

      <section id="rooms" class="guide-page__section"><div class="guide-page__section-heading"><h2>按空间选材</h2><p>从正在改造的房间出发，先解决使用条件，再选择材料。</p></div><div class="room-guide-grid"><article v-for="room in roomGuides" :key="room.name"><img :src="room.image" :alt="`${room.name}选材参考`" width="620" height="420" loading="lazy" /><div><h3>{{ room.name }}</h3><span>{{ room.categories }}</span><p>{{ room.text }}</p></div></article></div></section>

      <section id="categories" class="guide-page__section"><div class="guide-page__section-heading"><h2>按品类学习</h2><p>了解每类材料下单前最值得核对的信息。</p></div><div class="category-learning"><article v-for="category in categoryCatalog" :id="category.key" :key="category.key"><img :src="category.image" :alt="`${category.name}材料`" width="280" height="190" loading="lazy" /><div><h3>{{ category.name }}</h3><p>{{ category.intro }}</p><RouterLink :to="{ path: '/menu', query: { category: category.name } }">查看{{ category.name }} <IconSymbol name="arrow" /></RouterLink></div></article></div></section>

      <section class="guide-page__section"><div class="guide-page__section-heading"><h2>四步完成选材</h2><p>每一步都有可以回到订单中核对的结果。</p></div><ol class="purchase-process"><li v-for="(step, index) in purchaseSteps" :key="step.title"><span>{{ index + 1 }}</span><div><h3>{{ step.title }}</h3><p>{{ step.text }}</p></div></li></ol></section>

      <section id="calculator" class="guide-page__section guide-page__calculators"><div class="guide-page__section-heading"><h2>用量与尺寸计算</h2><p>计算结果用于初步比较，最终购买数量仍需结合实际测量和商品包装。</p></div><div class="calculator-tabs" role="tablist" aria-label="计算器品类"><button v-for="category in categoryCatalog" :key="category.key" type="button" role="tab" :aria-selected="calculatorCategory === category.key" @click="calculatorCategory = category.key">{{ category.name }}</button></div><QuantityEstimator :category-key="calculatorCategory" /></section>

      <section class="guide-page__section"><div class="guide-page__section-heading"><h2>看懂计价单位</h2><p>同一预算在不同计价单位下，需要比较的数量基础并不相同。</p></div><dl class="price-unit-guide"><div v-for="item in priceUnits" :key="item.unit"><dt>{{ item.unit }}</dt><dd>{{ item.text }}</dd></div></dl></section>

      <section id="rules" class="guide-page__section guide-page__rules"><div class="guide-page__section-heading"><h2>配送、安装与售后</h2><p>以下内容用于下单前核对，不构成具体时效或范围承诺。</p></div><div><article v-for="rule in serviceRules" :key="rule.title"><h3>{{ rule.title }}</h3><p>{{ rule.text }}</p></article></div></section>

      <section class="guide-page__section guide-page__faq"><div class="guide-page__section-heading"><h2>常见问题</h2><p>如果商品页面缺少关键参数，请在下单前先完成咨询。</p></div><FaqAccordion :items="faqItems" /></section>
      <section class="guide-page__contact"><div><h2>还没有确定从哪里开始？</h2><p>可以先浏览四大品类并记录空间尺寸，再通过页面内的咨询入口核对具体商品条件。</p></div><RouterLink class="primary-button" to="/menu">浏览全部商品</RouterLink></section>
    </article>
  </AppLayout>
</template>

<script setup>
import { ref } from "vue";
import AppLayout from "../components/AppLayout.vue";
import Breadcrumbs from "../components/Breadcrumbs.vue";
import FaqAccordion from "../components/FaqAccordion.vue";
import GuideIndex from "../components/GuideIndex.vue";
import IconSymbol from "../components/IconSymbol.vue";
import QuantityEstimator from "../components/QuantityEstimator.vue";
import { categoryCatalog } from "../services/catalogService";
import { faqItems, guideIndex, priceUnits, purchaseSteps, roomGuides, serviceRules } from "../services/guideContent";
const calculatorCategory = ref("tile");
</script>

<style scoped>
.guide-page { width: min(1340px, calc(100% - var(--gutter) * 2)); margin: 0 auto 96px; }
.guide-page__header { min-height: 230px; padding: 48px 54px; display: flex; align-items: center; justify-content: space-between; gap: 80px; color: #f8f6f2; background: var(--color-ink); }.guide-page__header h1 { max-width: 680px; font-size: 44px; line-height: 1.18; }.guide-page__header p { max-width: 760px; margin-top: 20px; color: #c9c3bc; line-height: 1.8; }.guide-page__header .primary-button { flex: 0 0 auto; background: var(--color-accent); }
.guide-page > :deep(.guide-index) { margin-top: 24px; }.guide-page__section { padding: 72px 0; border-bottom: 1px solid var(--color-line); scroll-margin-top: 68px; }.guide-page__section-heading { display: flex; align-items: end; justify-content: space-between; gap: 32px; margin-bottom: 30px; }.guide-page__section-heading h2 { font-size: 34px; }.guide-page__section-heading p { max-width: 680px; color: var(--color-muted); line-height: 1.7; }
.room-guide-grid { display: grid; grid-template-columns: 1.25fr .75fr .75fr; grid-template-rows: 240px 240px; gap: 14px; }.room-guide-grid article { position: relative; overflow: hidden; }.room-guide-grid article:first-child { grid-row: 1 / 3; }.room-guide-grid article:nth-child(4) { grid-column: 2 / 4; }.room-guide-grid img { width: 100%; height: 100%; object-fit: cover; filter: saturate(.82); }.room-guide-grid article > div { position: absolute; inset: auto 0 0; padding: 42px 22px 20px; color: #fff; background: linear-gradient(to bottom, transparent, rgba(33,31,28,.86)); }.room-guide-grid h3 { font-family: "Songti SC", serif; font-size: 27px; }.room-guide-grid span { font-size: 12px; }.room-guide-grid p { margin-top: 7px; color: #e6e1db; font-size: 12px; line-height: 1.6; }
.category-learning { display: grid; grid-template-columns: 1fr 1fr; border-top: 1px solid var(--color-ink); }.category-learning article { min-height: 210px; padding: 22px; display: grid; grid-template-columns: 190px 1fr; gap: 22px; border-right: 1px solid var(--color-line); border-bottom: 1px solid var(--color-line); scroll-margin-top: 70px; }.category-learning article:nth-child(even) { border-right: 0; }.category-learning img { width: 100%; height: 150px; object-fit: cover; }.category-learning h3 { font-family: "Songti SC", serif; font-size: 25px; }.category-learning p { margin-top: 9px; color: var(--color-muted); font-size: 13px; line-height: 1.65; }.category-learning a { min-height: 42px; margin-top: 12px; display: inline-flex; align-items: center; gap: 7px; border-bottom: 1px solid var(--color-line); }.category-learning .ui-icon { width: 16px; }
.purchase-process { margin: 0; padding: 0; display: grid; grid-template-columns: repeat(4, 1fr); list-style: none; border-top: 1px solid var(--color-ink); }.purchase-process li { min-height: 170px; padding: 26px; display: flex; gap: 18px; border-right: 1px solid var(--color-line); border-bottom: 1px solid var(--color-line); }.purchase-process li:last-child { border-right: 0; }.purchase-process li > span { color: var(--color-accent); font-family: "Songti SC", serif; font-size: 28px; }.purchase-process p { margin-top: 10px; color: var(--color-muted); line-height: 1.7; }
.calculator-tabs { margin-bottom: 14px; display: flex; border: 1px solid var(--color-line); }.calculator-tabs button { min-width: 120px; min-height: 46px; background: var(--color-paper); border-right: 1px solid var(--color-line); }.calculator-tabs button[aria-selected="true"] { color: #fff; background: var(--color-ink); }
.price-unit-guide { display: grid; grid-template-columns: 1fr 1fr; border-top: 1px solid var(--color-ink); }.price-unit-guide div { min-height: 116px; padding: 24px; display: grid; grid-template-columns: 110px 1fr; gap: 18px; border-right: 1px solid var(--color-line); border-bottom: 1px solid var(--color-line); }.price-unit-guide div:nth-child(even) { border-right: 0; }.price-unit-guide dt { color: var(--color-accent-dark); font-family: "Songti SC", serif; font-size: 23px; }.price-unit-guide dd { margin: 0; color: var(--color-muted); line-height: 1.7; }
.guide-page__rules > div:last-child { display: grid; grid-template-columns: repeat(4, 1fr); border-top: 1px solid var(--color-ink); }.guide-page__rules article { min-height: 150px; padding: 24px; border-right: 1px solid var(--color-line); border-bottom: 1px solid var(--color-line); }.guide-page__rules article:last-child { border-right: 0; }.guide-page__rules p { margin-top: 10px; color: var(--color-muted); line-height: 1.7; }
.guide-page__faq { display: grid; grid-template-columns: 340px 1fr; gap: 72px; }.guide-page__faq .guide-page__section-heading { display: block; }.guide-page__faq .guide-page__section-heading p { margin-top: 14px; }
.guide-page__contact { margin-top: 72px; padding: 40px 48px; display: flex; align-items: center; justify-content: space-between; gap: 60px; color: #fff; background: var(--color-ink); }.guide-page__contact h2 { font-size: 30px; }.guide-page__contact p { margin-top: 12px; color: #c9c3bc; }.guide-page__contact .primary-button { background: var(--color-accent); }
</style>
