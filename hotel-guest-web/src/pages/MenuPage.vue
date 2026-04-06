<template>
  <AppLayout>
    <section class="menu-page">
      <header class="menu-page__hero glass-card">
        <div class="menu-page__hero-top">
          <div>
            <p class="eyebrow">酒店点餐</p>
            <h1>云栖酒店餐饮菜单</h1>
            <p class="menu-page__desc">实时连接后端菜单数据，支持堂食与客房送餐。</p>
          </div>
          <RouterLink to="/coupons" class="menu-page__coupon-entry">
            <span class="menu-page__coupon-icon">券</span>
            <span>领券中心</span>
          </RouterLink>
        </div>
        <input v-model="keyword" class="field-input" placeholder="搜索菜名，如：和牛、鳕鱼、舒芙蕾" />
      </header>

      <div class="category-row">
        <button
          v-for="category in categories"
          :key="category.id"
          class="ghost-button"
          :class="{ active: activeCategory === category.id }"
          @click="activeCategory = category.id"
        >
          {{ category.name }}
        </button>
      </div>

      <section class="menu-page__grid">
        <DishCard v-for="dish in filteredDishes" :key="dish.id" :dish="dish" />
        <article v-if="!loading && !filteredDishes.length" class="field-card">当前分类暂无菜品。</article>
      </section>
    </section>
  </AppLayout>
</template>

<script setup>
import { computed, onMounted, ref, watch } from "vue";
import AppLayout from "../components/AppLayout.vue";
import DishCard from "../components/DishCard.vue";
import { request } from "../api/http";

const categories = ref([]);
const dishes = ref([]);
const activeCategory = ref(null);
const keyword = ref("");
const loading = ref(false);

function normalizeDish(dish) {
  return {
    ...dish,
    intro: dish.description || "主厨精选菜品",
    tags: buildTags(dish),
    image: dish.image || "linear-gradient(135deg, #152238, #2c4464)"
  };
}

function buildTags(dish) {
  const tags = [];
  if (dish.tagType === "WINE") tags.push("名酒");
  if (dish.tagType === "BANQUET") tags.push("宴会");
  if ((dish.score || 0) >= 4.5) tags.push("招牌");
  if ((dish.luxuryLevel || 0) >= 4) tags.push("轻奢");
  return tags.length ? tags : ["精选"];
}

async function loadCategories() {
  categories.value = await request("/user/category/list?type=1", {
    authRequired: false
  });
  if (!activeCategory.value && categories.value.length) {
    activeCategory.value = categories.value[0].id;
  }
}

async function loadDishes(categoryId) {
  if (!categoryId) return;
  loading.value = true;
  try {
    const data = await request(`/user/dish/list?categoryId=${categoryId}`, {
      authRequired: false
    });
    dishes.value = (data || []).map(normalizeDish);
  } finally {
    loading.value = false;
  }
}

const filteredDishes = computed(() =>
  dishes.value.filter((dish) => {
    const q = keyword.value.trim();
    return (
      !q ||
      dish.name.includes(q) ||
      (dish.intro || "").includes(q) ||
      (dish.tags || []).some((tag) => tag.includes(q))
    );
  })
);

watch(activeCategory, (value) => {
  loadDishes(value);
});

onMounted(async () => {
  await loadCategories();
});
</script>

<style scoped>
.menu-page {
  display: grid;
  gap: 18px;
}

.menu-page__hero {
  padding: 22px;
  border-radius: var(--radius-xl);
  display: grid;
  gap: 14px;
  position: sticky;
  top: 10px;
  z-index: 5;
}

.menu-page__hero-top {
  display: flex;
  justify-content: space-between;
  gap: 14px;
  align-items: start;
}

.menu-page__desc {
  margin-top: 10px;
  color: var(--color-text-muted);
  line-height: 1.7;
}

.menu-page__coupon-entry {
  min-width: 94px;
  padding: 12px 14px;
  border-radius: 22px;
  background: linear-gradient(135deg, rgba(17, 36, 60, 0.96), rgba(43, 68, 102, 0.92));
  color: #fff;
  display: grid;
  justify-items: center;
  gap: 6px;
  box-shadow: var(--shadow-md);
}

.menu-page__coupon-icon {
  width: 34px;
  height: 34px;
  border-radius: 999px;
  display: grid;
  place-items: center;
  background: rgba(255, 255, 255, 0.14);
  border: 1px solid rgba(255, 255, 255, 0.22);
}

.category-row {
  display: flex;
  gap: 10px;
  overflow-x: auto;
  padding-bottom: 4px;
  position: sticky;
  top: 188px;
  z-index: 4;
  background: linear-gradient(180deg, rgba(245, 243, 238, 0.96), rgba(245, 243, 238, 0.76));
  padding-top: 4px;
}

.category-row button {
  white-space: nowrap;
}

.category-row .active {
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-soft));
  color: #fff;
}

.menu-page__grid {
  display: grid;
  gap: 14px;
}

@media (min-width: 768px) {
  .menu-page__grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 767px) {
  .menu-page__hero-top {
    align-items: center;
  }

  .menu-page__coupon-entry {
    min-width: 82px;
    padding: 10px 12px;
  }
}
</style>
