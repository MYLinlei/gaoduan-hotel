(function () {
  var VIEW_QUERY_KEY = "view";
  var VIEW_QUERY_VALUE = "coupon-admin";
  var PANEL_ID = "sky-coupon-panel-host";
  var MENU_CLASS = "sky-coupon-sidebar-item";
  var MENU_ACTIVE_CLASS = "sky-coupon-sidebar-item-active";
  var TOAST_WRAP_ID = "sky-coupon-toast-wrap";

  var state = {
    active: false,
    loading: false,
    saving: false,
    refreshQueued: false,
    page: 1,
    pageSize: 10,
    total: 0,
    filters: {
      name: "",
      status: ""
    },
    records: [],
    modalOpen: false,
    modalMode: "create",
    currentId: null,
    form: defaultForm()
  };

  function defaultForm() {
    return {
      name: "",
      scopeType: "ALL_STORE",
      scopeId: "",
      couponType: "FULL_REDUCTION",
      channelType: "UNIVERSAL",
      totalStock: 100,
      availableStock: 100,
      payValue: "",
      actualValue: "",
      beginTime: "",
      endTime: "",
      seckillBeginTime: "",
      seckillEndTime: "",
      perLimit: 1,
      dayLimit: 1,
      rules: "",
      remark: ""
    };
  }

  function ready(fn) {
    if (document.readyState === "loading") {
      document.addEventListener("DOMContentLoaded", fn);
    } else {
      fn();
    }
  }

  function getCookie(name) {
    var prefix = name + "=";
    var parts = document.cookie ? document.cookie.split("; ") : [];
    for (var i = 0; i < parts.length; i += 1) {
      if (parts[i].indexOf(prefix) === 0) {
        return decodeURIComponent(parts[i].slice(prefix.length));
      }
    }
    return "";
  }

  function getToken() {
    return getCookie("token") || localStorage.getItem("token") || "";
  }

  function escapeHtml(text) {
    return String(text == null ? "" : text)
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;")
      .replace(/"/g, "&quot;")
      .replace(/'/g, "&#39;");
  }

  function showToast(message, type) {
    if (window.ELEMENT && window.ELEMENT.Message) {
      window.ELEMENT.Message({
        message: message,
        type: type || "info",
        duration: 2600
      });
      return;
    }

    var wrap = document.getElementById(TOAST_WRAP_ID);
    if (!wrap) {
      wrap = document.createElement("div");
      wrap.id = TOAST_WRAP_ID;
      wrap.style.position = "fixed";
      wrap.style.top = "20px";
      wrap.style.right = "20px";
      wrap.style.zIndex = "4000";
      document.body.appendChild(wrap);
    }

    var toast = document.createElement("div");
    toast.textContent = message;
    toast.style.marginTop = "10px";
    toast.style.padding = "12px 14px";
    toast.style.borderRadius = "10px";
    toast.style.color = "#fff";
    toast.style.background = type === "error" ? "#ef4444" : type === "success" ? "#16a34a" : "#334155";
    wrap.appendChild(toast);

    setTimeout(function () {
      if (toast.parentNode) {
        toast.parentNode.removeChild(toast);
      }
    }, 2600);
  }

  function parseApiPayload(response, rawText) {
    if (!rawText) {
      return null;
    }

    var trimmed = rawText.trim();
    var contentType = response.headers.get("content-type") || "";
    var looksLikeJson = trimmed.indexOf("{") === 0 || trimmed.indexOf("[") === 0;
    if (contentType.indexOf("application/json") !== -1 || looksLikeJson) {
      return JSON.parse(trimmed);
    }

    throw new Error("后台返回了非 JSON 响应，HTTP " + response.status);
  }

  function request(url, options) {
    var token = getToken();
    if (!token) {
      return Promise.reject(new Error("请先登录后台后再使用优惠券管理"));
    }

    options = options || {};
    var headers = {
      token: token
    };

    if (options.body !== undefined) {
      headers["Content-Type"] = "application/json";
    }

    return fetch(url, {
      method: options.method || "GET",
      headers: headers,
      body: options.body === undefined ? undefined : JSON.stringify(options.body)
    })
      .then(function (response) {
        return response.text().then(function (rawText) {
          return {
            response: response,
            payload: parseApiPayload(response, rawText)
          };
        });
      })
      .then(function (result) {
        var response = result.response;
        var payload = result.payload;
        if (response.status === 401) {
          throw new Error("后台登录状态已失效，请重新登录");
        }
        if (!payload || payload.code !== 1) {
          throw new Error((payload && payload.msg) || ("请求失败，HTTP " + response.status));
        }
        return payload.data;
      });
  }

  function isCouponViewActive() {
    try {
      var params = new URLSearchParams(location.search || "");
      return params.get(VIEW_QUERY_KEY) === VIEW_QUERY_VALUE;
    } catch (error) {
      return false;
    }
  }

  function setCouponViewInUrl(active) {
    try {
      var params = new URLSearchParams(location.search || "");
      if (active) {
        params.set(VIEW_QUERY_KEY, VIEW_QUERY_VALUE);
      } else {
        params.delete(VIEW_QUERY_KEY);
      }
      var query = params.toString();
      var nextUrl = location.pathname + (query ? "?" + query : "") + location.hash;
      history.replaceState(history.state, "", nextUrl);
    } catch (error) {
      console.warn("Failed to sync coupon view state to URL", error);
    }
  }

  function buildPageQuery() {
    var params = [
      "page=" + encodeURIComponent(state.page),
      "pageSize=" + encodeURIComponent(state.pageSize)
    ];

    if (state.filters.name) {
      params.push("name=" + encodeURIComponent(state.filters.name));
    }
    if (state.filters.status !== "") {
      params.push("status=" + encodeURIComponent(state.filters.status));
    }
    return params.join("&");
  }

  function formatDate(value) {
    if (!value) {
      return "-";
    }
    return String(value).replace("T", " ").slice(0, 16);
  }

  function toDateTimeLocal(value) {
    if (!value) {
      return "";
    }
    return String(value).replace(" ", "T").slice(0, 16);
  }

  function fromDateTimeLocal(value) {
    if (!value) {
      return null;
    }
    return value.replace("T", " ") + ":00";
  }

  function statusText(status) {
    return Number(status) === 1 ? "已上架" : "已下架";
  }

  function scopeText(scopeType, scopeId) {
    if (scopeType === "CATEGORY") {
      return "分类券" + (scopeId ? " #" + scopeId : "");
    }
    if (scopeType === "DISH") {
      return "菜品券" + (scopeId ? " #" + scopeId : "");
    }
    return "全店通用";
  }

  function channelText(channelType) {
    if (!channelType || channelType === "UNIVERSAL") {
      return "全部场景";
    }
    if (channelType === "DINE_IN") {
      return "堂食";
    }
    if (channelType === "DELIVERY") {
      return "外卖";
    }
    return channelType;
  }

  function getClosestElement(node) {
    if (!node) {
      return null;
    }
    if (node.nodeType === 1) {
      return node;
    }
    return node.parentElement || null;
  }

  function isManagedNode(node) {
    var element = getClosestElement(node);
    if (!element) {
      return false;
    }
    return Boolean(
      element.closest("#" + PANEL_ID) ||
      element.closest("." + MENU_CLASS) ||
      element.closest("#" + TOAST_WRAP_ID)
    );
  }

  function shouldRefreshForMutations(mutations) {
    var menu = document.querySelector(".sidebar-container .el-menu");
    var appMain = document.querySelector(".app-main");
    if (!menu || !appMain) {
      return true;
    }
    if (!menu.querySelector("." + MENU_CLASS)) {
      return true;
    }
    for (var i = 0; i < mutations.length; i += 1) {
      var mutation = mutations[i];
      if (!isManagedNode(mutation.target)) {
        return true;
      }
      for (var j = 0; j < mutation.addedNodes.length; j += 1) {
        if (!isManagedNode(mutation.addedNodes[j])) {
          return true;
        }
      }
      for (var k = 0; k < mutation.removedNodes.length; k += 1) {
        if (!isManagedNode(mutation.removedNodes[k])) {
          return true;
        }
      }
    }
    return false;
  }

  function ensurePanel() {
    var appMain = document.querySelector(".app-main");
    if (!appMain) {
      return null;
    }

    var panel = document.getElementById(PANEL_ID);
    if (panel && panel.parentNode !== appMain) {
      panel.parentNode.removeChild(panel);
      panel = null;
    }

    if (!panel) {
      panel = document.createElement("div");
      panel.id = PANEL_ID;
      panel.className = "sky-coupon-panel-host";
      panel.style.display = "none";
      panel.innerHTML =
        '<div class="sky-coupon-shell">' +
          '<section class="sky-coupon-card">' +
            '<div class="sky-coupon-header">' +
              '<div>' +
                '<p class="sky-coupon-eyebrow">PUBLIC VOUCHERS</p>' +
                '<h2>优惠券管理</h2>' +
                '<p class="sky-coupon-desc">管理员在这里配置公开发券，发布后会同步到用户端领券中心。</p>' +
              '</div>' +
              '<div class="sky-coupon-actions">' +
                '<button type="button" class="sky-coupon-button secondary" data-action="refresh">刷新列表</button>' +
                '<button type="button" class="sky-coupon-button gold" data-action="create">新建优惠券</button>' +
              '</div>' +
            '</div>' +
            '<div class="sky-coupon-filters">' +
              '<input class="sky-coupon-input" id="sky-coupon-filter-name" placeholder="按优惠券名称搜索" />' +
              '<select class="sky-coupon-select" id="sky-coupon-filter-status">' +
                '<option value="">全部状态</option>' +
                '<option value="1">已上架</option>' +
                '<option value="0">已下架</option>' +
              '</select>' +
              '<button type="button" class="sky-coupon-button primary" data-action="search">查询</button>' +
              '<button type="button" class="sky-coupon-button secondary" data-action="reset">重置</button>' +
            '</div>' +
            '<div class="sky-coupon-table-wrap">' +
              '<table class="sky-coupon-table">' +
                '<thead>' +
                  '<tr>' +
                    '<th>优惠券</th>' +
                    '<th>面额 / 门槛</th>' +
                    '<th>适用范围</th>' +
                    '<th>库存</th>' +
                    '<th>有效期</th>' +
                    '<th>状态</th>' +
                    '<th>操作</th>' +
                  '</tr>' +
                '</thead>' +
                '<tbody id="sky-coupon-table-body"></tbody>' +
              '</table>' +
            '</div>' +
            '<div class="sky-coupon-pagination">' +
              '<span id="sky-coupon-page-summary">共 0 条</span>' +
              '<button type="button" class="sky-coupon-button secondary" data-action="prev">上一页</button>' +
              '<button type="button" class="sky-coupon-button secondary" data-action="next">下一页</button>' +
            '</div>' +
          '</section>' +
        '</div>' +
        '<div class="sky-coupon-modal" id="sky-coupon-modal" style="display:none;">' +
          '<div class="sky-coupon-modal__mask" data-action="close-modal"></div>' +
          '<section class="sky-coupon-modal__panel sky-coupon-modal-card">' +
            '<div class="sky-coupon-header">' +
              '<div>' +
                '<p class="sky-coupon-eyebrow">VOUCHER FORM</p>' +
                '<h2 id="sky-coupon-modal-title">新建优惠券</h2>' +
                '<p class="sky-coupon-desc">填写完成后保存，下架券不会出现在顾客端领券中心。</p>' +
              '</div>' +
              '<button type="button" class="sky-coupon-button secondary" data-action="close-modal">关闭</button>' +
            '</div>' +
            '<div class="sky-coupon-form">' +
              fieldHtml("name", "优惠券名称", "text", "例如：高端晚宴礼遇券") +
              selectHtml("scopeType", "作用范围", [
                { value: "ALL_STORE", label: "全店通用" },
                { value: "CATEGORY", label: "分类券" },
                { value: "DISH", label: "菜品券" }
              ]) +
              fieldHtml("scopeId", "范围 ID", "number", "分类或菜品 ID，可留空") +
              selectHtml("couponType", "券类型", [
                { value: "FULL_REDUCTION", label: "满减券" }
              ]) +
              selectHtml("channelType", "适用渠道", [
                { value: "", label: "全部场景" },
                { value: "DINE_IN", label: "堂食" },
                { value: "DELIVERY", label: "外卖" }
              ]) +
              fieldHtml("actualValue", "优惠面额", "number", "例如 100") +
              fieldHtml("payValue", "使用门槛", "number", "例如 1000") +
              fieldHtml("totalStock", "总库存", "number", "例如 200") +
              fieldHtml("availableStock", "可用库存", "number", "默认与总库存一致") +
              fieldHtml("perLimit", "每人总限领", "number", "默认 1") +
              fieldHtml("dayLimit", "每日限领", "number", "默认 1") +
              fieldHtml("beginTime", "生效时间", "datetime-local") +
              fieldHtml("endTime", "失效时间", "datetime-local") +
              fieldHtml("seckillBeginTime", "抢券开始时间", "datetime-local") +
              fieldHtml("seckillEndTime", "抢券结束时间", "datetime-local") +
              textareaHtml("rules", "使用规则", "例如：全店通用，不可与其他礼遇叠加。", true) +
              textareaHtml("remark", "备注说明", "内部备注，用户端不展示。", true) +
            '</div>' +
            '<div class="sky-coupon-modal__footer">' +
              '<button type="button" class="sky-coupon-button secondary" data-action="close-modal">取消</button>' +
              '<button type="button" class="sky-coupon-button primary" id="sky-coupon-save-button" data-action="save">保存</button>' +
            '</div>' +
          '</section>' +
        '</div>';
      appMain.appendChild(panel);
      bindPanelEvents(panel);
    }

    return panel;
  }

  function fieldHtml(name, label, type, placeholder) {
    return (
      '<div class="sky-coupon-field">' +
        '<label for="coupon-field-' + name + '">' + label + '</label>' +
        '<input class="sky-coupon-input" id="coupon-field-' + name + '" data-field="' + name + '" type="' + type + '"' +
        (placeholder ? ' placeholder="' + escapeHtml(placeholder) + '"' : "") +
        " />" +
      "</div>"
    );
  }

  function selectHtml(name, label, options) {
    var html =
      '<div class="sky-coupon-field">' +
        '<label for="coupon-field-' + name + '">' + label + "</label>" +
        '<select class="sky-coupon-select" id="coupon-field-' + name + '" data-field="' + name + '">';
    for (var i = 0; i < options.length; i += 1) {
      html += '<option value="' + escapeHtml(options[i].value) + '">' + escapeHtml(options[i].label) + "</option>";
    }
    html += "</select></div>";
    return html;
  }

  function textareaHtml(name, label, placeholder, span2) {
    return (
      '<div class="sky-coupon-field' + (span2 ? " span-2" : "") + '">' +
        '<label for="coupon-field-' + name + '">' + label + '</label>' +
        '<textarea class="sky-coupon-textarea" id="coupon-field-' + name + '" data-field="' + name + '" placeholder="' + escapeHtml(placeholder || "") + '"></textarea>' +
      "</div>"
    );
  }

  function bindPanelEvents(panel) {
    panel.addEventListener("click", function (event) {
      var actionNode = event.target.closest("[data-action]");
      if (!actionNode) {
        return;
      }

      var action = actionNode.getAttribute("data-action");
      if (action === "refresh") {
        loadVouchers();
      } else if (action === "create") {
        openCreateModal();
      } else if (action === "search") {
        state.page = 1;
        readFilters();
        loadVouchers();
      } else if (action === "reset") {
        resetFilters();
      } else if (action === "prev") {
        if (state.page > 1) {
          state.page -= 1;
          loadVouchers();
        }
      } else if (action === "next") {
        if (state.page * state.pageSize < state.total) {
          state.page += 1;
          loadVouchers();
        }
      } else if (action === "close-modal") {
        closeModal();
      } else if (action === "save") {
        saveVoucher();
      } else if (action === "edit") {
        openEditModal(actionNode.getAttribute("data-id"));
      } else if (action === "toggle-status") {
        toggleStatus(actionNode.getAttribute("data-id"), actionNode.getAttribute("data-status"));
      }
    });
  }

  function readFilters() {
    var panel = document.getElementById(PANEL_ID);
    if (!panel) {
      return;
    }
    state.filters.name = (panel.querySelector("#sky-coupon-filter-name").value || "").trim();
    state.filters.status = panel.querySelector("#sky-coupon-filter-status").value || "";
  }

  function resetFilters() {
    var panel = document.getElementById(PANEL_ID);
    if (!panel) {
      return;
    }
    panel.querySelector("#sky-coupon-filter-name").value = "";
    panel.querySelector("#sky-coupon-filter-status").value = "";
    state.filters.name = "";
    state.filters.status = "";
    state.page = 1;
    loadVouchers();
  }

  function setLoading(loading) {
    state.loading = loading;
    renderTable();
  }

  function loadVouchers() {
    setLoading(true);
    return request("/api/hotelHighVoucher/page?" + buildPageQuery())
      .then(function (data) {
        state.records = data && data.records ? data.records : [];
        state.total = data && data.total ? Number(data.total) : 0;
      })
      .catch(function (error) {
        showToast(error.message, "error");
      })
      .finally(function () {
        setLoading(false);
      });
  }

  function renderTable() {
    var panel = document.getElementById(PANEL_ID);
    if (!panel) {
      return;
    }

    var body = panel.querySelector("#sky-coupon-table-body");
    var summary = panel.querySelector("#sky-coupon-page-summary");
    summary.textContent = "共 " + state.total + " 条，当前第 " + state.page + " 页";

    if (state.loading) {
      body.innerHTML = '<tr><td colspan="7" class="sky-coupon-empty">加载中...</td></tr>';
      return;
    }

    if (!state.records.length) {
      body.innerHTML = '<tr><td colspan="7" class="sky-coupon-empty">当前没有优惠券数据，请先新建一张券。</td></tr>';
      return;
    }

    body.innerHTML = state.records.map(function (item) {
      var nextStatus = Number(item.status) === 1 ? 0 : 1;
      return (
        "<tr>" +
          "<td>" +
            "<strong>" + escapeHtml(item.name) + "</strong>" +
            '<div class="sky-coupon-meta">' +
              "<span>ID #" + escapeHtml(item.id) + "</span>" +
              "<span>" + escapeHtml(item.rules || "暂无规则说明") + "</span>" +
            "</div>" +
          "</td>" +
          "<td>" +
            '<div class="sky-coupon-meta">' +
              "<span>面额：¥" + escapeHtml(item.actualValue) + "</span>" +
              "<span>门槛：满 ¥" + escapeHtml(item.payValue) + "</span>" +
              "<span>类型：" + escapeHtml(item.couponType || "FULL_REDUCTION") + "</span>" +
            "</div>" +
          "</td>" +
          "<td>" +
            '<div class="sky-coupon-meta">' +
              "<span>" + escapeHtml(scopeText(item.scopeType, item.scopeId)) + "</span>" +
              "<span>" + escapeHtml(channelText(item.channelType)) + "</span>" +
            "</div>" +
          "</td>" +
          "<td>" +
            '<div class="sky-coupon-meta">' +
              "<span>总库存：" + escapeHtml(item.totalStock) + "</span>" +
              "<span>可用库存：" + escapeHtml(item.availableStock) + "</span>" +
              "<span>每人限领：" + escapeHtml(item.perLimit) + " / 每日：" + escapeHtml(item.dayLimit) + "</span>" +
            "</div>" +
          "</td>" +
          "<td>" +
            '<div class="sky-coupon-meta">' +
              "<span>生效：" + escapeHtml(formatDate(item.beginTime)) + "</span>" +
              "<span>失效：" + escapeHtml(formatDate(item.endTime)) + "</span>" +
              "<span>抢券：" + escapeHtml(formatDate(item.seckillBeginTime)) + " ~ " + escapeHtml(formatDate(item.seckillEndTime)) + "</span>" +
            "</div>" +
          "</td>" +
          "<td><span class=\"sky-coupon-badge " + (Number(item.status) === 1 ? "on" : "off") + "\">" + statusText(item.status) + "</span></td>" +
          "<td>" +
            '<div class="sky-coupon-row-actions">' +
              '<button type="button" class="sky-coupon-button ghost" data-action="edit" data-id="' + escapeHtml(item.id) + '">编辑</button>' +
              '<button type="button" class="sky-coupon-button ' + (Number(item.status) === 1 ? "warn" : "gold") + '" data-action="toggle-status" data-id="' + escapeHtml(item.id) + '" data-status="' + nextStatus + '">' +
                (Number(item.status) === 1 ? "下架" : "上架") +
              "</button>" +
            "</div>" +
          "</td>" +
        "</tr>"
      );
    }).join("");
  }

  function openCreateModal() {
    state.modalOpen = true;
    state.modalMode = "create";
    state.currentId = null;
    state.form = defaultForm();
    renderModal();
  }

  function openEditModal(id) {
    if (!id) {
      return;
    }
    request("/api/hotelHighVoucher/" + encodeURIComponent(id))
      .then(function (data) {
        state.modalOpen = true;
        state.modalMode = "edit";
        state.currentId = data.id;
        state.form = {
          name: data.name || "",
          scopeType: data.scopeType || "ALL_STORE",
          scopeId: data.scopeId == null ? "" : data.scopeId,
          couponType: data.couponType || "FULL_REDUCTION",
          channelType: data.channelType || "UNIVERSAL",
          totalStock: data.totalStock == null ? "" : data.totalStock,
          availableStock: data.availableStock == null ? "" : data.availableStock,
          payValue: data.payValue == null ? "" : data.payValue,
          actualValue: data.actualValue == null ? "" : data.actualValue,
          beginTime: toDateTimeLocal(data.beginTime),
          endTime: toDateTimeLocal(data.endTime),
          seckillBeginTime: toDateTimeLocal(data.seckillBeginTime),
          seckillEndTime: toDateTimeLocal(data.seckillEndTime),
          perLimit: data.perLimit == null ? 1 : data.perLimit,
          dayLimit: data.dayLimit == null ? 1 : data.dayLimit,
          rules: data.rules || "",
          remark: data.remark || ""
        };
        renderModal();
      })
      .catch(function (error) {
        showToast(error.message, "error");
      });
  }

  function closeModal() {
    state.modalOpen = false;
    renderModal();
  }

  function renderModal() {
    var panel = document.getElementById(PANEL_ID);
    if (!panel) {
      return;
    }

    var modal = panel.querySelector("#sky-coupon-modal");
    var title = panel.querySelector("#sky-coupon-modal-title");
    var saveButton = panel.querySelector("#sky-coupon-save-button");
    modal.style.display = state.modalOpen ? "" : "none";
    title.textContent = state.modalMode === "edit" ? "编辑优惠券" : "新建优惠券";
    saveButton.textContent = state.saving ? "保存中..." : "保存";
    saveButton.disabled = state.saving;

    if (!state.modalOpen) {
      return;
    }

    Object.keys(state.form).forEach(function (key) {
      var field = panel.querySelector('[data-field="' + key + '"]');
      if (field) {
        field.value = state.form[key] == null ? "" : state.form[key];
      }
    });
  }

  function collectForm() {
    var panel = document.getElementById(PANEL_ID);
    var result = {};
    Object.keys(defaultForm()).forEach(function (key) {
      var field = panel.querySelector('[data-field="' + key + '"]');
      result[key] = field ? field.value : "";
    });
    return result;
  }

  function validateForm(form) {
    if (!form.name) {
      return "请输入优惠券名称";
    }
    if (!form.actualValue || Number(form.actualValue) <= 0) {
      return "请输入正确的优惠面额";
    }
    if (!form.payValue || Number(form.payValue) <= 0) {
      return "请输入正确的使用门槛";
    }
    if (!form.totalStock || Number(form.totalStock) <= 0) {
      return "请输入正确的总库存";
    }
    if (!form.beginTime || !form.endTime) {
      return "请填写生效时间和失效时间";
    }
    if (!form.seckillBeginTime || !form.seckillEndTime) {
      return "请填写抢券开始和结束时间";
    }
    return "";
  }

  function buildPayload(form) {
    var availableStock = form.availableStock === "" ? form.totalStock : form.availableStock;
    var payload = {
      name: form.name.trim(),
      scopeType: form.scopeType || "ALL_STORE",
      scopeId: form.scopeId === "" ? null : Number(form.scopeId),
      couponType: form.couponType || "FULL_REDUCTION",
      channelType: form.channelType || "UNIVERSAL",
      totalStock: Number(form.totalStock),
      availableStock: Number(availableStock),
      payValue: Number(form.payValue),
      actualValue: Number(form.actualValue),
      beginTime: fromDateTimeLocal(form.beginTime),
      endTime: fromDateTimeLocal(form.endTime),
      seckillBeginTime: fromDateTimeLocal(form.seckillBeginTime),
      seckillEndTime: fromDateTimeLocal(form.seckillEndTime),
      perLimit: Number(form.perLimit || 1),
      dayLimit: Number(form.dayLimit || 1),
      status: state.modalMode === "create" ? 0 : null,
      rules: form.rules || "",
      remark: form.remark || ""
    };

    if (state.modalMode === "edit") {
      payload.id = state.currentId;
      delete payload.status;
    }
    return payload;
  }

  function saveVoucher() {
    if (state.saving) {
      return;
    }
    var form = collectForm();
    var error = validateForm(form);
    if (error) {
      showToast(error, "error");
      return;
    }

    state.saving = true;
    renderModal();
    var payload = buildPayload(form);
    var method = state.modalMode === "edit" ? "PUT" : "POST";

    request("/api/hotelHighVoucher", {
      method: method,
      body: payload
    })
      .then(function () {
        showToast(state.modalMode === "edit" ? "优惠券已更新" : "优惠券已创建，请按需上架", "success");
        closeModal();
        loadVouchers();
      })
      .catch(function (requestError) {
        showToast(requestError.message, "error");
      })
      .finally(function () {
        state.saving = false;
        renderModal();
      });
  }

  function toggleStatus(id, status) {
    request("/api/hotelHighVoucher/" + encodeURIComponent(id) + "/status/" + encodeURIComponent(status), {
      method: "PUT"
    })
      .then(function () {
        showToast(Number(status) === 1 ? "优惠券已上架，顾客端将可见" : "优惠券已下架", "success");
        loadVouchers();
      })
      .catch(function (error) {
        showToast(error.message, "error");
      });
  }

  function syncNativeMenuState() {
    var menu = document.querySelector(".sidebar-container .el-menu");
    if (!menu) {
      return;
    }
    menu.classList.toggle("sky-coupon-native-muted", state.active);
  }

  function updateMenuState() {
    var couponItem = document.querySelector("." + MENU_CLASS);
    if (!couponItem) {
      return;
    }
    couponItem.classList.toggle(MENU_ACTIVE_CLASS, state.active);
    couponItem.setAttribute("aria-current", state.active ? "page" : "false");
    document.body.classList.toggle("sky-coupon-active", state.active);
    syncNativeMenuState();
  }

  function injectMenuItem() {
    var menu = document.querySelector(".sidebar-container .el-menu");
    if (!menu) {
      return;
    }

    if (!menu.dataset.skyCouponBound) {
      menu.dataset.skyCouponBound = "1";
      menu.addEventListener("click", function (event) {
        var item = event.target.closest(".el-menu-item, .el-submenu__title, ." + MENU_CLASS);
        if (!item) {
          return;
        }
        if (item.classList.contains(MENU_CLASS)) {
          return;
        }
        if (state.active) {
          deactivateCoupon();
        }
      }, true);
    }

    if (menu.querySelector("." + MENU_CLASS)) {
      updateMenuState();
      return;
    }

    var anchor = menu.querySelector(".sky-ai-sidebar-item");
    if (!anchor) {
      var menuItems = Array.prototype.slice.call(menu.querySelectorAll(".el-menu-item"));
      for (var i = 0; i < menuItems.length; i += 1) {
        if (menuItems[i].textContent.indexOf("员工管理") !== -1) {
          anchor = menuItems[i];
          break;
        }
      }
    }

    if (!anchor) {
      return;
    }

    var couponItem = document.createElement("li");
    couponItem.className = MENU_CLASS;
    couponItem.setAttribute("role", "menuitem");
    couponItem.setAttribute("tabindex", "0");
    couponItem.innerHTML = '<i class="el-icon-tickets"></i><span>优惠券管理</span>';
    couponItem.addEventListener("click", function (event) {
      event.preventDefault();
      activateCoupon();
    });
    couponItem.addEventListener("keydown", function (event) {
      if (event.key === "Enter" || event.key === " ") {
        event.preventDefault();
        activateCoupon();
      }
    });

    anchor.insertAdjacentElement("afterend", couponItem);
    updateMenuState();
  }

  function activateCoupon() {
    var appMain = document.querySelector(".app-main");
    var panel = ensurePanel();
    if (!appMain || !panel) {
      return;
    }

    state.active = true;
    updateMenuState();

    Array.prototype.slice.call(appMain.children).forEach(function (child) {
      if (child === panel) {
        return;
      }
      if (!child.dataset.skyCouponDisplay) {
        child.dataset.skyCouponDisplay = child.style.display || "";
      }
      child.style.display = "none";
    });

    panel.style.display = "";
    renderModal();
    renderTable();
    setCouponViewInUrl(true);
    loadVouchers();
  }

  function deactivateCoupon() {
    var appMain = document.querySelector(".app-main");
    var panel = document.getElementById(PANEL_ID);
    state.active = false;
    state.modalOpen = false;
    updateMenuState();

    if (appMain) {
      Array.prototype.slice.call(appMain.children).forEach(function (child) {
        if (child === panel) {
          return;
        }
        child.style.display = child.dataset.skyCouponDisplay || "";
      });
    }

    if (panel) {
      panel.style.display = "none";
    }
    setCouponViewInUrl(false);
  }

  function syncWithQuery() {
    if (isCouponViewActive()) {
      if (!state.active) {
        activateCoupon();
      }
    } else if (state.active) {
      deactivateCoupon();
    }
  }

  function refresh() {
    injectMenuItem();
    ensurePanel();
    syncWithQuery();
  }

  ready(function () {
    refresh();

    var observer = new MutationObserver(function (mutations) {
      if (!shouldRefreshForMutations(mutations) || state.refreshQueued) {
        return;
      }
      state.refreshQueued = true;
      requestAnimationFrame(function () {
        state.refreshQueued = false;
        refresh();
      });
    });

    observer.observe(document.getElementById("app") || document.body, {
      childList: true,
      subtree: true
    });

    window.addEventListener("hashchange", syncWithQuery);
    window.addEventListener("popstate", syncWithQuery);
  });
})();
