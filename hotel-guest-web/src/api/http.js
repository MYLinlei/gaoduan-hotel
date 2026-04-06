const BASE_URL = "";

function buildHeaders(authRequired, body) {
  const headers = {};
  if (body !== undefined) {
    headers["Content-Type"] = "application/json";
  }

  if (authRequired) {
    const token = window.localStorage.getItem("hotel-guest-token");
    if (token) {
      headers.authentication = token;
    }
  }
  return headers;
}

export async function request(url, options = {}) {
  const {
    method = "GET",
    body,
    authRequired = true
  } = options;

  const response = await fetch(`${BASE_URL}${url}`, {
    method,
    headers: buildHeaders(authRequired, body),
    body: body === undefined ? undefined : JSON.stringify(body)
  });

  const payload = await response.json().catch(() => ({}));
  if (!response.ok || payload.code !== 1) {
    if (response.status === 401) {
      window.localStorage.removeItem("hotel-guest-token");
      window.localStorage.removeItem("hotel-guest-user");
    }
    const message = payload.msg || `请求失败：${response.status}`;
    throw new Error(message);
  }
  return payload.data;
}
