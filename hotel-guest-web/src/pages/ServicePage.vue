<template>
  <AppLayout>
    <section class="service-page">
      <header class="glass-card service-page__hero">
        <p class="eyebrow">酒店服务</p>
        <h1>服务须知</h1>
        <p>仅保留点餐相关服务信息，无个人中心等冗余功能。</p>
      </header>

      <section class="glass-card service-page__contact">
        <h2>酒店服务电话</h2>
        <a :href="`tel:${serviceInfo.phone}`" class="service-page__phone">{{ serviceInfo.phone }}</a>
      </section>

      <section class="glass-card service-page__notes">
        <h2>服务须知</h2>
        <ul>
          <li v-for="note in serviceInfo.notes" :key="note">{{ note }}</li>
        </ul>
      </section>
    </section>
  </AppLayout>
</template>

<script setup>
import AppLayout from "../components/AppLayout.vue";
import { serviceInfo } from "../mock/menu";
</script>

<style scoped>
.service-page {
  display: grid;
  gap: 16px;
}

.service-page__hero,
.service-page__contact,
.service-page__notes {
  padding: 20px;
  border-radius: var(--radius-xl);
}

.service-page__hero p:last-child {
  margin-top: 10px;
  color: var(--color-text-muted);
}

.service-page__phone {
  display: inline-flex;
  margin-top: 14px;
  font-size: 32px;
  color: var(--color-primary);
  font-weight: 700;
}

.service-page__notes ul {
  padding-left: 18px;
  margin: 16px 0 0;
  color: var(--color-text-muted);
  line-height: 1.9;
}
</style>
