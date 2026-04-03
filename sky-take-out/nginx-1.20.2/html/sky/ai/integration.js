(function () {
  var AI_QUERY_KEY = "view";
  var AI_QUERY_VALUE = "ai-assistant";
  var THREAD_KEY = "sky_take_out_ai_thread_id";
  var HISTORY_KEY = "sky_take_out_ai_history";
  var TOAST_WRAP_ID = "sky-ai-toast-wrap";
  var PANEL_ID = "sky-ai-panel-host";
  var MENU_CLASS = "sky-ai-sidebar-item";
  var MENU_ACTIVE_CLASS = "sky-ai-sidebar-item-active";

  var state = {
    active: false,
    sending: false,
    threadId: "",
    history: [],
    refreshQueued: false,
    syncingStatus: false,
    lastStatusSyncAt: 0
  };

  function isAIViewActive() {
    try {
      var params = new URLSearchParams(location.search || "");
      return params.get(AI_QUERY_KEY) === AI_QUERY_VALUE;
    } catch (error) {
      return false;
    }
  }

  function setAIViewInUrl(active) {
    try {
      var params = new URLSearchParams(location.search || "");
      if (active) {
        params.set(AI_QUERY_KEY, AI_QUERY_VALUE);
      } else {
        params.delete(AI_QUERY_KEY);
      }

      var query = params.toString();
      var nextUrl = location.pathname + (query ? "?" + query : "") + location.hash;
      history.replaceState(history.state, "", nextUrl);
    } catch (error) {
      console.warn("Failed to sync AI view state to URL", error);
    }
  }

  function ready(fn) {
    if (document.readyState === "loading") {
      document.addEventListener("DOMContentLoaded", fn);
    } else {
      fn();
    }
  }

  function getToken() {
    var cookieToken = getCookie("token");
    return cookieToken || localStorage.getItem("token") || "";
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

  function escapeHtml(text) {
    return String(text)
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;")
      .replace(/"/g, "&quot;")
      .replace(/'/g, "&#39;");
  }

  function getThreadId() {
    if (!state.threadId) {
      state.threadId = localStorage.getItem(THREAD_KEY) || generateThreadId();
      localStorage.setItem(THREAD_KEY, state.threadId);
    }
    return state.threadId;
  }

  function generateThreadId() {
    if (window.crypto && window.crypto.randomUUID) {
      return window.crypto.randomUUID();
    }
    return "thread_" + Date.now() + "_" + Math.random().toString(16).slice(2);
  }

  function welcomeMessage() {
    return "你好，我是苍穹外卖后台 AI 助手。你可以直接问我当前有哪些菜品、套餐是否在售、员工信息，或者切换店铺营业状态。";
  }

  function loadHistory() {
    try {
      var cached = localStorage.getItem(HISTORY_KEY);
      var parsed = cached ? JSON.parse(cached) : null;
      if (Array.isArray(parsed) && parsed.length) {
        state.history = parsed;
        return;
      }
    } catch (error) {
      console.warn("Failed to read AI history from localStorage", error);
    }
    state.history = [{ role: "assistant", text: welcomeMessage() }];
  }

  function saveHistory() {
    localStorage.setItem(HISTORY_KEY, JSON.stringify(state.history));
  }

  function pushMessage(role, text) {
    state.history.push({ role: role, text: text });
    saveHistory();
  }

  function isElementNode(node) {
    return node && node.nodeType === 1;
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
      element.closest("#" + TOAST_WRAP_ID) ||
      element.closest("." + MENU_CLASS)
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
      panel.className = "sky-ai-panel-host";
      panel.style.display = "none";
      panel.innerHTML =
        '<div class="sky-ai-shell">' +
          '<div class="sky-ai-layout">' +
            '<section class="sky-ai-card">' +
              '<div class="sky-ai-messages" id="sky-ai-messages"></div>' +
              '<div class="sky-ai-composer">' +
                '<div class="sky-ai-composer-row">' +
                  '<textarea id="sky-ai-input" class="sky-ai-textarea" placeholder="输入你的问题，例如：查询当前在售菜品，或者把店铺切换为营业中。"></textarea>' +
                  '<button id="sky-ai-send" class="el-button el-button--primary sky-ai-send" type="button">发送</button>' +
                '</div>' +
                '<div class="sky-ai-actions">' +
                  '<button id="sky-ai-clear" class="el-button sky-ai-clear" type="button">清空对话</button>' +
                  '<p class="sky-ai-hint">按 Enter 发送，Shift + Enter 换行</p>' +
                '</div>' +
              '</div>' +
            '</section>' +
            '<aside class="sky-ai-sidecard">' +
              '<div class="sky-ai-status-chip">多轮上下文已保持</div>' +
              '<div>' +
                '<div class="sky-ai-sidecard-title">常用指令</div>' +
                '<div class="sky-ai-shortcuts">' +
                  '<button class="sky-ai-shortcut" type="button" data-shortcut="查询当前在售菜品">查询在售菜品</button>' +
                  '<button class="sky-ai-shortcut" type="button" data-shortcut="查询主食分类的菜品">查询主食分类菜品</button>' +
                  '<button class="sky-ai-shortcut" type="button" data-shortcut="查看员工分页信息">查看员工信息</button>' +
                  '<button class="sky-ai-shortcut" type="button" data-shortcut="把店铺切换为营业中">切换为营业中</button>' +
                  '<button class="sky-ai-shortcut" type="button" data-shortcut="把店铺切换为打烊">切换为打烊</button>' +
                '</div>' +
              '</div>' +
              '<p class="sky-ai-quick-note">AI 助手会沿用当前后台登录态，并保留会话上下文。你可以连续追问同一批菜品、套餐或员工信息。</p>' +
            '</aside>' +
          '</div>' +
        '</div>';
      appMain.appendChild(panel);
      bindPanelEvents(panel);
    }

    return panel;
  }

  function bindPanelEvents(panel) {
    var sendButton = panel.querySelector("#sky-ai-send");
    var clearButton = panel.querySelector("#sky-ai-clear");
    var input = panel.querySelector("#sky-ai-input");
    var shortcuts = panel.querySelectorAll("[data-shortcut]");

    sendButton.addEventListener("click", function () {
      sendMessage();
    });

    clearButton.addEventListener("click", function () {
      clearConversation();
    });

    input.addEventListener("keydown", function (event) {
      if (event.key === "Enter" && !event.shiftKey) {
        event.preventDefault();
        sendMessage();
      }
    });

    Array.prototype.forEach.call(shortcuts, function (button) {
      button.addEventListener("click", function () {
        var text = button.getAttribute("data-shortcut") || "";
        sendMessage(text);
      });
    });
  }

  function setComposerState(busy) {
    var panel = document.getElementById(PANEL_ID);
    if (!panel) {
      return;
    }

    var input = panel.querySelector("#sky-ai-input");
    var sendButton = panel.querySelector("#sky-ai-send");
    var clearButton = panel.querySelector("#sky-ai-clear");
    var shortcutButtons = panel.querySelectorAll("[data-shortcut]");

    input.disabled = busy;
    sendButton.disabled = busy;
    clearButton.disabled = busy;
    sendButton.textContent = busy ? "发送中..." : "发送";
    Array.prototype.forEach.call(shortcutButtons, function (button) {
      button.disabled = busy;
    });
  }

  function renderMessages() {
    var panel = document.getElementById(PANEL_ID);
    if (!panel) {
      return;
    }

    if (!state.history.length) {
      state.history = [{ role: "assistant", text: welcomeMessage() }];
      saveHistory();
    }

    var messages = panel.querySelector("#sky-ai-messages");
    var html = state.history.map(function (item) {
      var roleClass = item.role === "user" ? "user" : "assistant";
      return (
        '<div class="sky-ai-message ' + roleClass + '">' +
          '<div class="sky-ai-bubble">' + escapeHtml(item.text) + "</div>" +
        "</div>"
      );
    }).join("");

    if (state.sending) {
      html +=
        '<div class="sky-ai-message loading">' +
          '<div class="sky-ai-bubble">' +
            '<span class="sky-ai-typing"><span></span><span></span><span></span></span>' +
          "</div>" +
        "</div>";
    }

    messages.innerHTML = html;
    requestAnimationFrame(function () {
      messages.scrollTop = messages.scrollHeight;
    });
  }

  function showToast(message, type) {
    if (window.ELEMENT && window.ELEMENT.Message) {
      window.ELEMENT.Message({
        message: message,
        type: type || "info",
        duration: 2500
      });
      return;
    }

    var wrap = document.getElementById(TOAST_WRAP_ID);
    if (!wrap) {
      wrap = document.createElement("div");
      wrap.id = TOAST_WRAP_ID;
      wrap.className = "sky-ai-toast-wrap";
      document.body.appendChild(wrap);
    }

    var toast = document.createElement("div");
    toast.className = "sky-ai-toast " + (type || "info");
    toast.textContent = message;
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

    var contentType = response.headers.get("content-type") || "";
    var trimmed = rawText.trim();
    var looksLikeJson = trimmed.indexOf("{") === 0 || trimmed.indexOf("[") === 0;

    if (contentType.indexOf("application/json") !== -1 || looksLikeJson) {
      return JSON.parse(rawText);
    }

    var preview = trimmed
      .replace(/<script[\s\S]*?<\/script>/gi, " ")
      .replace(/<style[\s\S]*?<\/style>/gi, " ")
      .replace(/<[^>]+>/g, " ")
      .replace(/\s+/g, " ")
      .trim()
      .slice(0, 120);

    throw new Error("后端返回了非 JSON 响应，HTTP " + response.status + (preview ? "：" + preview : ""));
  }

  function sendMessage(presetText) {
    if (state.sending) {
      return;
    }

    var panel = document.getElementById(PANEL_ID);
    if (!panel) {
      return;
    }

    var input = panel.querySelector("#sky-ai-input");
    var token = getToken();
    var message = (presetText || input.value || "").trim();

    if (!message) {
      input.focus();
      return;
    }

    if (!token) {
      showToast("请先登录商家后台后再使用 AI 助手", "error");
      return;
    }

    getThreadId();
    pushMessage("user", message);
    if (!presetText) {
      input.value = "";
    }

    state.sending = true;
    setComposerState(true);
    renderMessages();

    fetch("/api/ai/chat", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        token: token
      },
      body: JSON.stringify({
        message: message,
        threadId: state.threadId
      })
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
          throw new Error("登录状态已失效，请重新登录后再试");
        }

        if (!payload) {
          throw new Error("后端返回了空响应，HTTP " + response.status);
        }

        if (!response.ok || payload.code !== 1) {
          throw new Error(payload.msg || payload.error || ("请求失败，HTTP " + response.status));
        }

        if (payload.data && payload.data.threadId) {
          state.threadId = payload.data.threadId;
          localStorage.setItem(THREAD_KEY, state.threadId);
        }

        pushMessage("assistant", (payload.data && payload.data.answer) || "没有返回内容");
        syncShopStatus();
      })
      .catch(function (error) {
        var messageText = error && error.message ? error.message : "未知错误";
        pushMessage("assistant", "请求失败：" + messageText);
        showToast(messageText, "error");
      })
      .finally(function () {
        state.sending = false;
        setComposerState(false);
        renderMessages();
        input.focus();
      });
  }

  function clearConversation() {
    state.history = [{ role: "assistant", text: welcomeMessage() }];
    saveHistory();
    renderMessages();
    showToast("已清空当前对话", "success");
  }

  function syncShopStatus() {
    var token = getToken();
    if (!token) {
      return;
    }

    if (state.syncingStatus) {
      return;
    }

    if (Date.now() - state.lastStatusSyncAt < 1200) {
      return;
    }

    state.syncingStatus = true;
    state.lastStatusSyncAt = Date.now();

    fetch("/api/shop/status", {
      headers: {
        token: token
      }
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
        var payload = result.payload;
        if (!payload || payload.code !== 1) {
          return;
        }
        updateBusinessStatus(payload.data);
      })
      .catch(function () {
      })
      .finally(function () {
        state.syncingStatus = false;
      });
  }

  function updateBusinessStatus(status) {
    var button = document.querySelector(".navbar .businessBtn");
    if (!button) {
      return;
    }

    var normalized = Number(status) === 1 ? 1 : 0;
    button.textContent = normalized === 1 ? "营业中" : "打烊中";
    button.classList.toggle("closing", normalized !== 1);
  }

  function injectMenuItem() {
    var menu = document.querySelector(".sidebar-container .el-menu");
    if (!menu) {
      return;
    }

    if (!menu.dataset.skyAiBound) {
      menu.dataset.skyAiBound = "1";
      menu.addEventListener("click", function (event) {
        var item = event.target.closest(".el-menu-item, .el-submenu__title, ." + MENU_CLASS);
        if (!item) {
          return;
        }

        if (item.classList.contains(MENU_CLASS)) {
          return;
        }

        if (state.active) {
          deactivateAI();
        }
      }, true);
    }

    if (menu.querySelector("." + MENU_CLASS)) {
      updateMenuState();
      return;
    }

    var menuItems = Array.prototype.slice.call(menu.querySelectorAll(".el-menu-item"));
    var employeeItem = null;
    for (var i = 0; i < menuItems.length; i += 1) {
      if (menuItems[i].textContent.indexOf("员工管理") !== -1) {
        employeeItem = menuItems[i];
        break;
      }
    }

    if (!employeeItem) {
      return;
    }

    var aiItem = document.createElement("li");
    aiItem.className = MENU_CLASS;
    aiItem.setAttribute("role", "menuitem");
    aiItem.setAttribute("tabindex", "0");
    aiItem.innerHTML = '<i class="iconfont icon-inform sky-ai-menu-icon"></i><span>AI助手</span>';
    aiItem.addEventListener("click", function (event) {
      event.preventDefault();
      activateAI();
    });
    aiItem.addEventListener("keydown", function (event) {
      if (event.key === "Enter" || event.key === " ") {
        event.preventDefault();
        activateAI();
      }
    });

    employeeItem.insertAdjacentElement("afterend", aiItem);
    updateMenuState();
  }

  function syncNativeMenuState() {
    var menu = document.querySelector(".sidebar-container .el-menu");
    if (!menu) {
      return;
    }
    menu.classList.toggle("sky-ai-native-muted", state.active);
  }

  function updateMenuState() {
    var aiItem = document.querySelector("." + MENU_CLASS);
    if (!aiItem) {
      return;
    }
    aiItem.classList.toggle(MENU_ACTIVE_CLASS, state.active);
    aiItem.setAttribute("aria-current", state.active ? "page" : "false");
    document.body.classList.toggle("sky-ai-active", state.active);
    syncNativeMenuState();
  }

  function activateAI() {
    var appMain = document.querySelector(".app-main");
    var panel = ensurePanel();
    if (!appMain || !panel) {
      return;
    }

    if (state.active && panel.style.display !== "none") {
      if (!isAIViewActive()) {
        setAIViewInUrl(true);
      }
      return;
    }

    state.active = true;
    updateMenuState();

    Array.prototype.slice.call(appMain.children).forEach(function (child) {
      if (child === panel) {
        return;
      }
      if (!child.dataset.skyAiDisplay) {
        child.dataset.skyAiDisplay = child.style.display || "";
      }
      child.style.display = "none";
    });

    panel.style.display = "";
    renderMessages();
    syncShopStatus();

    if (!isAIViewActive()) {
      setAIViewInUrl(true);
    }
  }

  function deactivateAI() {
    var appMain = document.querySelector(".app-main");
    var panel = document.getElementById(PANEL_ID);
    state.active = false;
    updateMenuState();

    if (appMain) {
      Array.prototype.slice.call(appMain.children).forEach(function (child) {
        if (child === panel) {
          return;
        }
        child.style.display = child.dataset.skyAiDisplay || "";
      });
    }

    if (panel) {
      panel.style.display = "none";
    }

    if (isAIViewActive()) {
      setAIViewInUrl(false);
    }
  }

  function syncWithHash() {
    if (isAIViewActive()) {
      if (!state.active) {
        activateAI();
      }
    } else if (state.active) {
      deactivateAI();
    }
  }

  function refresh() {
    injectMenuItem();
    ensurePanel();
    syncWithHash();
  }

  ready(function () {
    loadHistory();
    getThreadId();
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

    window.addEventListener("hashchange", syncWithHash);
    window.addEventListener("popstate", syncWithHash);
  });
})();
