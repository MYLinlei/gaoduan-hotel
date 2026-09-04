<template>
  <article class="coupon-ticket-card" :class="`is-${coupon.claimState}`">
    <div class="coupon-ticket-card__value"><strong><small>¥</small>{{ coupon.amount }}</strong><span>满 {{ coupon.threshold }} 元可用</span></div>
    <div class="coupon-ticket-card__body">
      <div><h2>{{ coupon.title }}</h2><span>{{ coupon.scope }}</span></div>
      <p>{{ coupon.validity }}</p>
      <button v-if="coupon.rules" class="coupon-ticket-card__rules" type="button" :aria-expanded="rulesOpen" @click="rulesOpen = !rulesOpen">{{ rulesOpen ? '收起规则' : '查看规则' }}</button>
      <p v-if="rulesOpen" class="coupon-ticket-card__rule-copy">{{ coupon.rules }}</p>
    </div>
    <button class="coupon-ticket-card__action" type="button" :disabled="!coupon.claimable" @click="$emit('claim', coupon.id)">{{ coupon.actionLabel }}</button>
  </article>
</template>

<script setup>
import { ref } from "vue";
defineProps({ coupon: { type: Object, required: true } });
defineEmits(["claim"]);
const rulesOpen = ref(false);
</script>

<style scoped>
.coupon-ticket-card { min-height: 176px; display: grid; grid-template-columns: 150px 1fr 88px; border: 1px solid #dfc7ba; background: #fbf7f3; }
.coupon-ticket-card__value { padding: 22px 18px; display: flex; flex-direction: column; justify-content: center; border-right: 1px dashed #d7b7a8; }
.coupon-ticket-card__value strong { color: var(--color-accent-dark); font-family: "Songti SC", serif; font-size: 42px; line-height: 1; font-variant-numeric: tabular-nums; }
.coupon-ticket-card__value small { font-size: 18px; }.coupon-ticket-card__value span { margin-top: 10px; color: var(--color-muted); font-size: 12px; }
.coupon-ticket-card__body { padding: 20px; align-content: center; display: grid; gap: 10px; }
.coupon-ticket-card__body > div { display: flex; justify-content: space-between; gap: 12px; }.coupon-ticket-card__body h2 { font-family: inherit; font-size: 17px; letter-spacing: 0; }.coupon-ticket-card__body > div span { color: var(--color-accent-dark); font-size: 12px; }
.coupon-ticket-card__body > p { color: var(--color-muted); font-size: 12px; }.coupon-ticket-card__rules { width: fit-content; min-height: 32px; padding: 0; background: transparent; border-bottom: 1px solid var(--color-line); font-size: 12px; }
.coupon-ticket-card__rule-copy { padding-top: 8px; border-top: 1px solid var(--color-line); line-height: 1.65; }
.coupon-ticket-card__action { color: #fff; background: var(--color-accent); }.coupon-ticket-card__action:hover:not(:disabled) { background: var(--color-accent-dark); }
.coupon-ticket-card__action:disabled { color: var(--color-muted); background: var(--color-warm); }
.coupon-ticket-card.is-expired, .coupon-ticket-card.is-sold-out { filter: saturate(.35); }
</style>
