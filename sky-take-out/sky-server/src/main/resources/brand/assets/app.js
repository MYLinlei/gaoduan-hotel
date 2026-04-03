(() => {
    const state = {
        authToken: localStorage.getItem("hotelBrandToken") || "",
        shopOpen: false,
        categories: [],
        dishes: [],
        filteredDishes: [],
        selectedCategoryId: null,
        selectedDish: null,
        notes: [],
        comments: [],
        vouchers: []
    };

    const refs = {
        shopStatus: document.getElementById("shopStatus"),
        categoryTabs: document.getElementById("categoryTabs"),
        dishGrid: document.getElementById("dishGrid"),
        searchInput: document.getElementById("searchInput"),
        refreshButton: document.getElementById("refreshButton"),
        detailEmpty: document.getElementById("detailEmpty"),
        detailContent: document.getElementById("detailContent"),
        detailTag: document.getElementById("detailTag"),
        detailName: document.getElementById("detailName"),
        detailDesc: document.getElementById("detailDesc"),
        detailPrice: document.getElementById("detailPrice"),
        detailLuxury: document.getElementById("detailLuxury"),
        detailScore: document.getElementById("detailScore"),
        detailCounts: document.getElementById("detailCounts"),
        likeButton: document.getElementById("likeButton"),
        favoriteButton: document.getElementById("favoriteButton"),
        reserveButton: document.getElementById("reserveButton"),
        voucherRefreshButton: document.getElementById("voucherRefreshButton"),
        noteRefreshButton: document.getElementById("noteRefreshButton"),
        commentRefreshButton: document.getElementById("commentRefreshButton"),
        voucherList: document.getElementById("voucherList"),
        noteList: document.getElementById("noteList"),
        commentList: document.getElementById("commentList"),
        memberEntry: document.getElementById("memberEntry"),
        tokenModal: document.getElementById("tokenModal"),
        tokenInput: document.getElementById("tokenInput"),
        saveTokenButton: document.getElementById("saveTokenButton"),
        clearTokenButton: document.getElementById("clearTokenButton"),
        closeTokenModal: document.getElementById("closeTokenModal")
    };

    async function api(path, options = {}) {
        const requestOptions = {
            method: options.method || "GET",
            headers: {
                "Content-Type": "application/json",
                ...(options.headers || {})
            }
        };

        if (options.body !== undefined) {
            requestOptions.body = typeof options.body === "string"
                ? options.body
                : JSON.stringify(options.body);
        }

        if (options.auth && state.authToken) {
            requestOptions.headers.authentication = state.authToken;
        }

        const response = await fetch(path, requestOptions);
        const payload = await response.json().catch(() => null);

        if (!response.ok || !payload || payload.code !== 1) {
            const message = payload && payload.msg ? payload.msg : "Request failed";
            throw new Error(message);
        }

        return payload.data;
    }

    function showToast(message) {
        const toast = document.createElement("div");
        toast.className = "toast";
        toast.textContent = message;
        toast.style.position = "fixed";
        toast.style.right = "24px";
        toast.style.bottom = "24px";
        toast.style.zIndex = "50";
        document.body.appendChild(toast);
        window.setTimeout(() => toast.remove(), 2200);
    }

    function formatMoney(value) {
        const number = Number(value || 0);
        return `CNY ${number.toFixed(2)}`;
    }

    function escapeHtml(value) {
        return String(value || "")
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#39;");
    }

    function requireToken() {
        if (state.authToken) {
            return true;
        }
        openTokenModal();
        showToast("User token is required for this action.");
        return false;
    }

    function getTagLabel(tagType) {
        const labels = {
            DINING: "Luxury Dining",
            WINE: "Premium Wine",
            BANQUET: "Banquet Choice"
        };
        return labels[tagType] || "Brand Select";
    }

    function getLuxuryLabel(level) {
        return `Lv.${Number(level || 1)}`;
    }

    function updateMemberEntry() {
        refs.memberEntry.textContent = state.authToken ? "Token Saved" : "Member Token";
    }

    function openTokenModal() {
        refs.tokenInput.value = state.authToken;
        refs.tokenModal.classList.remove("hidden");
    }

    function closeTokenModal() {
        refs.tokenModal.classList.add("hidden");
    }

    async function loadShopStatus() {
        const open = await api("/user/shop/status");
        state.shopOpen = Boolean(open);
        refs.shopStatus.textContent = state.shopOpen ? "Open" : "Closed";
    }

    async function loadCategories() {
        state.categories = await api("/user/category/list?type=1");
        if (!state.selectedCategoryId && state.categories.length) {
            state.selectedCategoryId = state.categories[0].id;
        }
        renderCategories();
    }

    async function loadDishes(categoryId) {
        if (!categoryId) {
            state.dishes = [];
            state.filteredDishes = [];
            renderDishes();
            return;
        }

        state.dishes = await api(`/user/dish/list?categoryId=${categoryId}`);
        filterDishes();
        renderDishes();

        if (state.filteredDishes.length) {
            const stillSelected = state.selectedDish
                && state.filteredDishes.some((dish) => dish.id === state.selectedDish.id);
            await selectDish(stillSelected ? state.selectedDish.id : state.filteredDishes[0].id);
            return;
        }

        state.selectedDish = null;
        state.notes = [];
        state.comments = [];
        state.vouchers = [];
        renderDetail();
    }

    async function selectDish(dishId) {
        state.selectedDish = await api(`/user/dish/${dishId}`);
        renderDishes();
        await Promise.all([
            loadDishNotes(dishId),
            loadDishComments(dishId),
            loadDishVouchers(dishId)
        ]);
        renderDetail();
    }

    async function loadDishNotes(dishId) {
        state.notes = await api(`/user/dishNote/list?dishId=${dishId}`);
    }

    async function loadDishComments(dishId) {
        const pageResult = await api(`/user/dishComment/page?dishId=${dishId}&page=1&pageSize=6`);
        state.comments = pageResult.records || [];
    }

    async function loadDishVouchers(dishId) {
        state.vouchers = await api(`/user/hotelHighVoucher/list?scopeType=DISH&scopeId=${dishId}`);
    }

    function filterDishes() {
        const keyword = refs.searchInput.value.trim().toLowerCase();
        state.filteredDishes = state.dishes.filter((dish) => {
            if (!keyword) {
                return true;
            }
            const name = String(dish.name || "").toLowerCase();
            const description = String(dish.description || "").toLowerCase();
            return name.includes(keyword) || description.includes(keyword);
        });
    }

    function renderCategories() {
        refs.categoryTabs.innerHTML = "";

        state.categories.forEach((category) => {
            const button = document.createElement("button");
            button.className = "category-tab";
            button.textContent = category.name;
            if (category.id === state.selectedCategoryId) {
                button.classList.add("active");
            }
            button.addEventListener("click", async () => {
                state.selectedCategoryId = category.id;
                renderCategories();
                await loadDishes(category.id);
            });
            refs.categoryTabs.appendChild(button);
        });
    }

    function renderDishes() {
        refs.dishGrid.innerHTML = "";

        if (!state.filteredDishes.length) {
            refs.dishGrid.innerHTML = '<div class="dish-card"><p class="dish-card__desc">No dishes in this category yet.</p></div>';
            return;
        }

        state.filteredDishes.forEach((dish) => {
            const card = document.createElement("article");
            card.className = "dish-card";
            card.innerHTML = `
                <span class="dish-card__tag">${escapeHtml(getTagLabel(dish.tagType))}</span>
                <div>
                    <h3>${escapeHtml(dish.name)}</h3>
                    <p class="dish-card__desc">${escapeHtml(dish.description || "Chef recommendation for the brand site.")}</p>
                </div>
                <div class="dish-card__footer">
                    <strong class="dish-card__price">${formatMoney(dish.price)}</strong>
                    <span>${escapeHtml(getLuxuryLabel(dish.luxuryLevel))}</span>
                </div>
            `;
            if (state.selectedDish && state.selectedDish.id === dish.id) {
                card.style.outline = "2px solid rgba(95, 63, 32, 0.5)";
            }
            card.addEventListener("click", async () => {
                await selectDish(dish.id);
            });
            refs.dishGrid.appendChild(card);
        });
    }

    function renderVouchers() {
        refs.voucherList.innerHTML = "";

        if (!state.vouchers.length) {
            refs.voucherList.innerHTML = '<div class="voucher-card"><p class="voucher-card__rules">No active dish vouchers.</p></div>';
            return;
        }

        state.vouchers.forEach((voucher) => {
            const card = document.createElement("article");
            card.className = "voucher-card";
            card.innerHTML = `
                <span>${escapeHtml(voucher.name)}</span>
                <p class="voucher-card__value">${formatMoney(voucher.actualValue)}</p>
                <p class="voucher-card__rules">Threshold ${formatMoney(voucher.payValue)} | Stock ${voucher.availableStock}</p>
                <p class="voucher-card__rules">${escapeHtml(voucher.rules || "Private high-value voucher for premium spending.")}</p>
                <button class="ghost-button" data-voucher-id="${voucher.id}">Rush Voucher</button>
            `;
            refs.voucherList.appendChild(card);
        });

        refs.voucherList.querySelectorAll("[data-voucher-id]").forEach((button) => {
            button.addEventListener("click", async () => {
                if (!requireToken()) {
                    return;
                }
                const voucherId = button.getAttribute("data-voucher-id");
                try {
                    const recordId = await api(`/user/hotelHighVoucher/seckill/${voucherId}`, {
                        method: "POST",
                        auth: true
                    });
                    showToast(`Voucher rush success: ${recordId}`);
                    await loadDishVouchers(state.selectedDish.id);
                    renderVouchers();
                } catch (error) {
                    showToast(error.message);
                }
            });
        });
    }

    function renderNotes() {
        refs.noteList.innerHTML = "";

        if (!state.notes.length) {
            refs.noteList.innerHTML = '<div class="note-card"><p>No tasting notes yet.</p></div>';
            return;
        }

        state.notes.forEach((note) => {
            const card = document.createElement("article");
            card.className = "note-card";
            card.innerHTML = `
                <h4>${escapeHtml(note.title)}</h4>
                <p>${escapeHtml(note.content)}</p>
                <div class="note-card__meta">${escapeHtml(note.userName || "Brand Member")} | Likes ${note.liked || 0}</div>
            `;
            refs.noteList.appendChild(card);
        });
    }

    function renderComments() {
        refs.commentList.innerHTML = "";

        if (!state.comments.length) {
            refs.commentList.innerHTML = '<div class="comment-card"><p>No guest comments yet.</p></div>';
            return;
        }

        state.comments.forEach((comment) => {
            const card = document.createElement("article");
            card.className = "comment-card";
            card.innerHTML = `
                <h4>${escapeHtml(comment.userName || "Brand Member")}</h4>
                <p>${escapeHtml(comment.content)}</p>
                <div class="comment-card__meta">Score ${comment.score || "5.00"} | Likes ${comment.liked || 0}</div>
            `;
            refs.commentList.appendChild(card);
        });
    }

    function renderDetail() {
        if (!state.selectedDish) {
            refs.detailEmpty.classList.remove("hidden");
            refs.detailContent.classList.add("hidden");
            refs.voucherList.innerHTML = "";
            refs.noteList.innerHTML = "";
            refs.commentList.innerHTML = "";
            return;
        }

        refs.detailEmpty.classList.add("hidden");
        refs.detailContent.classList.remove("hidden");

        const dish = state.selectedDish;
        refs.detailTag.textContent = getTagLabel(dish.tagType);
        refs.detailName.textContent = dish.name || "";
        refs.detailDesc.textContent = dish.description || "Dish description will be expanded with richer brand storytelling next.";
        refs.detailPrice.textContent = formatMoney(dish.price);
        refs.detailLuxury.textContent = getLuxuryLabel(dish.luxuryLevel);
        refs.detailScore.textContent = String(dish.score || "5.00");
        refs.detailCounts.textContent = `${dish.likeCount || 0} likes / ${dish.favoriteCount || 0} favorites`;
        refs.likeButton.textContent = dish.liked ? "Liked" : "Like";
        refs.favoriteButton.textContent = dish.favorited ? "Favorited" : "Favorite";

        renderVouchers();
        renderNotes();
        renderComments();
    }

    async function toggleDishAction(action) {
        if (!state.selectedDish || !requireToken()) {
            return;
        }

        const endpoint = action === "like" ? "like" : "favorite";
        try {
            const active = await api(`/user/dish/${endpoint}/${state.selectedDish.id}`, {
                method: "POST",
                auth: true
            });

            if (action === "like") {
                state.selectedDish.liked = active;
                state.selectedDish.likeCount = Math.max(0, (state.selectedDish.likeCount || 0) + (active ? 1 : -1));
            } else {
                state.selectedDish.favorited = active;
                state.selectedDish.favoriteCount = Math.max(0, (state.selectedDish.favoriteCount || 0) + (active ? 1 : -1));
            }

            renderDetail();
        } catch (error) {
            showToast(error.message);
        }
    }

    async function refreshSelectedDishExtras() {
        if (!state.selectedDish) {
            return;
        }
        await Promise.all([
            loadDishNotes(state.selectedDish.id),
            loadDishComments(state.selectedDish.id),
            loadDishVouchers(state.selectedDish.id)
        ]);
        renderDetail();
    }

    async function bootstrap() {
        updateMemberEntry();
        try {
            await loadShopStatus();
            await loadCategories();
            await loadDishes(state.selectedCategoryId);
        } catch (error) {
            showToast(error.message);
        }
    }

    refs.searchInput.addEventListener("input", () => {
        filterDishes();
        renderDishes();
    });

    refs.refreshButton.addEventListener("click", async () => {
        await loadShopStatus();
        await loadDishes(state.selectedCategoryId);
    });

    refs.likeButton.addEventListener("click", async () => {
        await toggleDishAction("like");
    });

    refs.favoriteButton.addEventListener("click", async () => {
        await toggleDishAction("favorite");
    });

    refs.reserveButton.addEventListener("click", () => {
        showToast("Cart and order pages will be wired in the next front-end phase.");
    });

    refs.voucherRefreshButton.addEventListener("click", refreshSelectedDishExtras);
    refs.noteRefreshButton.addEventListener("click", refreshSelectedDishExtras);
    refs.commentRefreshButton.addEventListener("click", refreshSelectedDishExtras);

    refs.memberEntry.addEventListener("click", openTokenModal);
    refs.closeTokenModal.addEventListener("click", closeTokenModal);

    refs.saveTokenButton.addEventListener("click", () => {
        state.authToken = refs.tokenInput.value.trim();
        if (state.authToken) {
            localStorage.setItem("hotelBrandToken", state.authToken);
            showToast("User token saved.");
        } else {
            localStorage.removeItem("hotelBrandToken");
            showToast("User token cleared.");
        }
        updateMemberEntry();
        closeTokenModal();
    });

    refs.clearTokenButton.addEventListener("click", () => {
        state.authToken = "";
        refs.tokenInput.value = "";
        localStorage.removeItem("hotelBrandToken");
        updateMemberEntry();
        showToast("User token cleared.");
    });

    refs.tokenModal.addEventListener("click", (event) => {
        if (event.target === refs.tokenModal) {
            closeTokenModal();
        }
    });

    bootstrap();
})();
