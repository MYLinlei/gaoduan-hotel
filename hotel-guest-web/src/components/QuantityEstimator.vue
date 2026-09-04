<template>
  <section class="quantity-estimator" aria-labelledby="estimator-title">
    <div class="quantity-estimator__heading"><h3 id="estimator-title">{{ title }}</h3><span>估算参考</span></div>
    <div v-if="categoryKey === 'bath'" class="quantity-estimator__checks"><p><strong>坑距</strong> 请以排污口中心至完成墙面的距离为准。</p><p><strong>安装</strong> 下单前核对给排水、电源和产品外形尺寸。</p></div>
    <div v-else-if="categoryKey === 'cabinet'" class="quantity-estimator__form">
      <label>墙面可用长度<input v-model.number="length" type="number" min="0" step="0.1" placeholder="请输入长度" /><span>m</span></label>
      <output><small>参考延米</small><strong>{{ length > 0 ? `${length.toFixed(1)} 延米` : '—' }}</strong></output>
    </div>
    <div v-else class="quantity-estimator__form">
      <label>铺装面积<input v-model.number="area" type="number" min="0" step="0.1" placeholder="请输入面积" /><span>㎡</span></label>
      <label>预留损耗<select v-model.number="loss"><option :value="0">不预留</option><option :value="5">5%</option><option :value="8">8%</option><option :value="10">10%</option></select></label>
      <output><small>建议购买面积</small><strong>{{ estimatedArea }}</strong></output>
    </div>
    <p class="quantity-estimator__note">结果仅用于初步选购，请以实际测量、商品包装规格与施工排版为准。</p>
  </section>
</template>

<script setup>
import { computed, ref } from "vue";
const props = defineProps({ categoryKey: { type: String, required: true }, compact: { type: Boolean, default: false } });
const area = ref(null); const length = ref(null); const loss = ref(5);
const title = computed(() => ({ tile: "瓷砖用量估算", floor: "地板用量估算", bath: "安装条件核对", cabinet: "橱柜延米估算" }[props.categoryKey] || "用量估算"));
const estimatedArea = computed(() => area.value > 0 ? `${(area.value * (1 + loss.value / 100)).toFixed(1)} ㎡` : "—");
</script>

<style scoped>
.quantity-estimator { padding: 18px; border: 1px solid var(--color-line); background: #faf9f6; }
.quantity-estimator__heading { display: flex; align-items: baseline; justify-content: space-between; margin-bottom: 14px; }
.quantity-estimator__heading h3 { font-size: 17px; }.quantity-estimator__heading span { color: var(--color-accent-dark); font-size: 11px; letter-spacing: .12em; }
.quantity-estimator__form { display: grid; grid-template-columns: repeat(3, 1fr); gap: 10px; }
.quantity-estimator label { min-width: 0; display: grid; grid-template-columns: 1fr auto; position: relative; color: var(--color-muted); font-size: 12px; }
.quantity-estimator label input, .quantity-estimator label select { grid-column: 1 / -1; width: 100%; min-height: 42px; margin-top: 6px; padding: 0 30px 0 10px; border: 1px solid var(--color-line); background: var(--color-paper); }
.quantity-estimator label span { position: absolute; right: 10px; bottom: 13px; }
.quantity-estimator output { padding: 8px 10px; display: grid; align-content: center; border: 1px solid var(--color-line); background: var(--color-paper); }
.quantity-estimator output small { color: var(--color-muted); }.quantity-estimator output strong { margin-top: 4px; font-size: 16px; }
.quantity-estimator__checks { display: grid; gap: 9px; color: var(--color-muted); line-height: 1.6; }
.quantity-estimator__checks strong { color: var(--color-ink); margin-right: 8px; }
.quantity-estimator__note { margin-top: 11px; color: var(--color-muted); font-size: 11px; line-height: 1.6; }
</style>
