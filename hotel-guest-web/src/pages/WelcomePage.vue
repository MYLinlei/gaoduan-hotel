<template>
  <section class="welcome">
    <div class="welcome__backdrop" />
    <div class="welcome__card glass-card">
      <p class="eyebrow">云栖酒店 · 私享点餐</p>
      <div class="welcome__logo">YQ</div>
      <h1>欢迎来到云栖酒店</h1>
      <p class="welcome__slogan">轻奢餐食与静谧服务，在一键点餐中自然抵达。</p>
      <div class="welcome__hint">
        <span>客房送餐</span>
        <span>餐厅点餐</span>
      </div>
      <RouterLink to="/menu" class="primary-button welcome__cta">进入点餐</RouterLink>
    </div>
  </section>
</template>

<style scoped>
.welcome {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 24px;
  position: relative;
  overflow: hidden;
}

.welcome__backdrop {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(circle at top left, rgba(185, 151, 91, 0.2), transparent 28%),
    linear-gradient(145deg, #10233b 0%, #243754 40%, #f6f3ee 120%);
}

.welcome__card {
  position: relative;
  z-index: 1;
  width: min(520px, 100%);
  padding: 32px 26px;
  border-radius: 34px;
  text-align: center;
}

.welcome__logo {
  width: 78px;
  height: 78px;
  margin: 0 auto 18px;
  border-radius: 26px;
  display: grid;
  place-items: center;
  color: #fff;
  font-size: 28px;
  font-weight: 700;
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-soft));
}

.welcome__slogan {
  margin: 16px 0 22px;
  color: var(--color-text-muted);
  line-height: 1.8;
}

.welcome__hint {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-bottom: 24px;
}

.welcome__hint span {
  padding: 8px 12px;
  border-radius: 999px;
  background: var(--color-gold-soft);
  color: var(--color-primary);
  font-size: 13px;
}

.welcome__cta {
  display: inline-flex;
  justify-content: center;
  min-width: 180px;
}
</style>
