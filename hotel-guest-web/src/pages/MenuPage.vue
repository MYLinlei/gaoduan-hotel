<template>
  <AppLayout>
    <CategoryPageShell :category="category">
      <div class="catalog-workspace">
        <FilterSidebar :groups="category.filters" :model-value="filters" @update:model-value="updateFilters" @clear="clearFilters" />
        <section class="catalog-results" aria-labelledby="catalog-results-title">
          <h2 id="catalog-results-title" class="sr-only">{{ category.name }}商品结果</h2>
          <ResultToolbar :count="resultCount" :selected-items="selectedItems" :sort="sort" @remove="removeFilter" @clear="clearFilters" @update:sort="updateSort" />
          <UnifiedContentState v-if="loading" status="loading" subject="商品" />
          <UnifiedContentState v-else-if="loadError" :status="loadError.status" subject="商品" :message="loadError.message" @retry="loadProducts" />
          <UnifiedContentState v-else-if="!pagedProducts.length" status="empty" subject="商品" message="可以清除部分筛选条件，或切换到其他品类继续浏览。" />
          <template v-else>
            <ProductGrid :products="firstProductRow" />
            <section class="category-guide-note" aria-labelledby="category-guide-title">
              <img :src="category.image" :alt="`${category.name}材质细节`" width="320" height="170" loading="lazy" />
              <div><h2 id="category-guide-title">{{ category.guide.title }}</h2><div class="category-guide-note__points"><article v-for="point in category.guide.points" :key="point.title"><h3>{{ point.title }}</h3><p>{{ point.text }}</p></article></div></div>
              <RouterLink :to="{ path: '/service', hash: `#${category.key}` }">查看完整指南 <IconSymbol name="arrow" /></RouterLink>
            </section>
            <ProductGrid v-if="remainingProducts.length" class="catalog-results__remaining" :products="remainingProducts" />
            <QuantityEstimator :category-key="category.key" compact />
            <nav v-if="pageCount > 1" class="catalog-pagination" aria-label="商品分页">
              <button type="button" :disabled="page <= 1" @click="updatePage(page - 1)">上一页</button>
              <button v-for="item in pageCount" :key="item" type="button" :aria-current="page === item ? 'page' : undefined" @click="updatePage(item)">{{ item }}</button>
              <button type="button" :disabled="page >= pageCount" @click="updatePage(page + 1)">下一页</button>
            </nav>
          </template>
        </section>
      </div>
    </CategoryPageShell>
  </AppLayout>
</template>

<script setup>
import { computed, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import AppLayout from "../components/AppLayout.vue";
import CategoryPageShell from "../components/CategoryPageShell.vue";
import FilterSidebar from "../components/FilterSidebar.vue";
import IconSymbol from "../components/IconSymbol.vue";
import ProductGrid from "../components/ProductGrid.vue";
import QuantityEstimator from "../components/QuantityEstimator.vue";
import ResultToolbar from "../components/ResultToolbar.vue";
import UnifiedContentState from "../components/UnifiedContentState.vue";
import { categoryCatalog, classifyRequestError, filterAndSortProducts, getCategoryConfig, loadCategoryProducts } from "../services/catalogService";

const route = useRoute(); const router = useRouter();
const category = ref(categoryCatalog[0]); const products = ref([]); const filters = ref({});
const loading = ref(false); const loadError = ref(null); const sort = ref("default"); const page = ref(1); const pageSize = 9;
const total = ref(0); const serverPaged = ref(true);
let loadSequence = 0;

const selectedItems = computed(() => category.value.filters.flatMap((group) => (filters.value[group.key] || []).map((value) => ({ key: group.key, label: group.label, value }))));
const hasLocalFilters = computed(() => selectedItems.value.length > 0);
const filteredProducts = computed(() => filterAndSortProducts(products.value, {
  keyword: serverPaged.value ? "" : String(route.query.q || ""),
  filters: filters.value,
  sort: serverPaged.value ? "default" : sort.value
}));
const resultCount = computed(() => serverPaged.value ? total.value : filteredProducts.value.length);
const pageCount = computed(() => Math.max(1, Math.ceil(resultCount.value / pageSize)));
const pagedProducts = computed(() => serverPaged.value ? filteredProducts.value : filteredProducts.value.slice((page.value - 1) * pageSize, page.value * pageSize));
const firstProductRow = computed(() => pagedProducts.value.slice(0, 3));
const remainingProducts = computed(() => pagedProducts.value.slice(3));

function queryForCategory() { return route.query.category || route.query.categoryKey || route.query.categoryId || "tile"; }
function readQueryState() {
  category.value = getCategoryConfig(queryForCategory()); sort.value = String(route.query.sort || "default"); page.value = Math.max(1, Number(route.query.page || 1));
  filters.value = Object.fromEntries(category.value.filters.map((group) => {
    const legacyRoom = group.key === "space" ? route.query.room : undefined;
    return [group.key, String(route.query[group.key] || legacyRoom || "").split("|").filter(Boolean)];
  }));
}
function patchQuery(patch) { router.push({ path: "/menu", query: { ...route.query, category: category.value.name, ...patch } }); }
function updateFilters(value) { filters.value = value; const patch = { page: undefined }; category.value.filters.forEach((group) => { patch[group.key] = value[group.key]?.length ? value[group.key].join("|") : undefined; }); patchQuery(patch); }
function clearFilters() { const patch = { page: undefined }; category.value.filters.forEach((group) => { patch[group.key] = undefined; }); patchQuery(patch); }
function removeFilter(item) { updateFilters({ ...filters.value, [item.key]: (filters.value[item.key] || []).filter((value) => value !== item.value) }); }
function updateSort(value) { patchQuery({ sort: value === "default" ? undefined : value, page: undefined }); }
function updatePage(value) { patchQuery({ page: value > 1 ? value : undefined }); window.scrollTo({ top: 250, behavior: window.matchMedia("(prefers-reduced-motion: reduce)").matches ? "auto" : "smooth" }); }
async function loadProducts() {
  const sequence = ++loadSequence;
  const categoryKey = category.value.key;
  const useServerPage = !hasLocalFilters.value;
  loading.value = true;
  loadError.value = null;
  try {
    const result = await loadCategoryProducts(categoryKey, {
      keyword: String(route.query.q || "").trim(),
      sort: sort.value,
      page: useServerPage ? page.value : 1,
      pageSize: useServerPage ? pageSize : 60,
      serverPaged: useServerPage
    });
    if (sequence === loadSequence && category.value.key === categoryKey) {
      products.value = result.products;
      total.value = result.total;
      serverPaged.value = result.serverPaged;
    }
  } catch (error) {
    if (sequence === loadSequence) loadError.value = classifyRequestError(error);
  } finally {
    if (sequence === loadSequence) loading.value = false;
  }
}
watch(() => route.fullPath, async () => { readQueryState(); await loadProducts(); }, { immediate: true });
</script>

<style scoped>
.catalog-workspace { display: grid; grid-template-columns: 236px minmax(0, 1fr); gap: 14px; align-items: start; }
.catalog-results { min-width: 0; }
.category-guide-note { min-height: 146px; margin-top: 14px; display: grid; grid-template-columns: 230px 1fr 150px; align-items: stretch; border: 1px solid var(--color-line); background: #faf8f5; }
.category-guide-note > img { width: 100%; height: 100%; object-fit: cover; }
.category-guide-note > div { padding: 19px 24px; }.category-guide-note h2 { font-size: 22px; }.category-guide-note__points { margin-top: 14px; display: grid; grid-template-columns: repeat(3, 1fr); gap: 24px; }
.category-guide-note__points h3 { font-size: 13px; }.category-guide-note__points p { margin-top: 5px; color: var(--color-muted); font-size: 12px; line-height: 1.6; }
.category-guide-note > a { padding: 20px; display: flex; align-items: center; justify-content: center; gap: 7px; color: var(--color-accent-dark); }.category-guide-note > a .ui-icon { width: 17px; }
.catalog-results__remaining { margin-top: 0; }
.catalog-results > :deep(.quantity-estimator) { margin-top: 14px; }
.catalog-pagination { min-height: 68px; margin-top: 14px; display: flex; align-items: center; justify-content: center; gap: 8px; border-top: 1px solid var(--color-line); }
.catalog-pagination button { min-width: 44px; min-height: 44px; background: var(--color-paper); border: 1px solid var(--color-line); }.catalog-pagination button[aria-current="page"] { color: #fff; background: var(--color-accent); border-color: var(--color-accent); }
</style>
