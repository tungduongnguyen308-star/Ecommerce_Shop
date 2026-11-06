/* tính tiền tức thời, đồng bộ số lượng về HttpSession trên Spring Boot. */
(() => {
  "use strict";
  const root = document.getElementById("cart-live-root");
  const content = document.getElementById("cart-live-content");
  if (!root || !content) return;
  const errorBox = document.getElementById("cart-live-error");
  const totalNode = document.getElementById("cart-live-total");
  const checkout = document.getElementById("cart-live-checkout");
  const syncHint = document.getElementById("cart-live-hint");
  const csrf = document.querySelector('meta[name="_csrf"]')?.content;
  const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;
  const endpoint = root.dataset.updateUrl || "/cart/api/update";
  const money = amount => new Intl.NumberFormat("vi-VN", { maximumFractionDigits: 0 }).format(amount) + " ₫";
  const states = new Map();

  function setError(message) {
    if (!errorBox) return;
    errorBox.textContent = message || "";
    errorBox.classList.toggle("d-none", !message);
  }
  function isValidQuantity(raw) {
    const n = Number(raw);
    return raw !== "" && Number.isInteger(n) && n >= 1 && n <= 100;
  }
  function hasPendingChanges() {
    for (const state of states.values()) {
      if (state.removed) continue;
      if (state.inFlight || state.timer || state.deleting || !isValidQuantity(state.input.value)
          || Number(state.input.value) !== state.committed) return true;
    }
    return false;
  }
  function recalculate() {
    let total = 0;
    for (const state of states.values()) {
      if (state.removed) continue;
      const qty = isValidQuantity(state.input.value) ? Number(state.input.value) : state.committed;
      const lineTotal = state.price * qty;
      state.row.querySelector(".cart-line-subtotal").textContent = money(lineTotal);
      total += lineTotal;
    }
    if (totalNode) totalNode.textContent = money(total);
    if (syncHint) syncHint.textContent = hasPendingChanges() ? "Đang cập nhật giỏ hàng…" : "";
    if (checkout) {
      checkout.classList.toggle("disabled", hasPendingChanges());
      checkout.setAttribute("aria-disabled", String(hasPendingChanges()));
    }
  }

  async function sendUpdate(productId, quantity) {
    const params = new URLSearchParams({ productId: String(productId), quantity: String(quantity) });
    const headers = { "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8", "Accept": "application/json" };
    if (csrf && csrfHeader) headers[csrfHeader] = csrf;
    const response = await fetch(endpoint, { method: "POST", credentials: "same-origin", headers, body: params });
    let data;
    try { data = await response.json(); } catch (_) { throw new Error("Không đọc được phản hồi từ máy chủ. Vui lòng tải lại trang."); }
    if (!response.ok || !data.ok) throw new Error(data.message || "Không thể cập nhật giỏ hàng.");
    return data;
  }

  function persist(state) {
    if (state.removed || state.deleting || state.inFlight) return state.request || Promise.resolve();
    if (!isValidQuantity(state.input.value)) {
      state.status.textContent = "Số lượng phải từ 1 đến 100";
      recalculate();
      return Promise.resolve();
    }
    const quantity = Number(state.input.value);
    if (quantity === state.committed) { state.status.textContent = ""; recalculate(); return Promise.resolve(); }
    state.inFlight = true;
    state.status.textContent = "Đang lưu…";
    recalculate();
    state.request = sendUpdate(state.id, quantity).then(() => {
      state.committed = quantity;
      state.status.textContent = "Đã cập nhật";
      setError("");
    }).catch(e => {
      if (!state.deleting) {
        state.input.value = String(state.committed);
        state.status.textContent = "Chưa lưu";
        setError(e.message || "Lỗi kết nối. Hãy thử lại.");
      }
    }).finally(() => {
      state.inFlight = false;
      state.request = null;
      if (!state.deleting && !state.removed && isValidQuantity(state.input.value)
          && Number(state.input.value) !== state.committed) {
        // Người dùng chỉnh số lượng trong khi yêu cầu trước đang chạy.
        state.timer = window.setTimeout(() => { state.timer = null; persist(state); }, 220);
      }
      recalculate();
    });
    return state.request;
  }

  for (const row of content.querySelectorAll(".cart-live-row")) {
    const input = row.querySelector(".cart-quantity-input");
    const status = row.querySelector(".cart-update-state");
    const form = row.querySelector(".cart-quantity-form");
    const removeForm = row.querySelector(".cart-remove-form");
    const state = { row, input, status, id: Number(row.dataset.productId), price: Number(row.dataset.unitPrice),
      committed: Number(input.value), timer: null, inFlight: false, request: null, deleting: false, removed: false };
    states.set(state.id, state);

    function scheduleSave() {
      if (state.timer) window.clearTimeout(state.timer);
      if (!isValidQuantity(input.value)) {
        status.textContent = "Số lượng phải từ 1 đến 100";
        setError("Vui lòng nhập số lượng nguyên từ 1 đến 100. Dùng nút × để xóa sản phẩm.");
        recalculate();
        return;
      }
      status.textContent = "";
      setError("");
      state.timer = window.setTimeout(() => { state.timer = null; persist(state); }, 380);
      recalculate();
    }
    input.addEventListener("input", scheduleSave);
    input.addEventListener("change", scheduleSave);
    // Khi bật JavaScript, thay việc submit trang bằng cập nhật trực tiếp.
    form.addEventListener("submit", e => { e.preventDefault(); if (state.timer) window.clearTimeout(state.timer); state.timer = null; persist(state); });

    removeForm.addEventListener("submit", async e => {
      e.preventDefault();
      if (state.deleting || state.removed) return;
      state.deleting = true;
      if (state.timer) { window.clearTimeout(state.timer); state.timer = null; }
      const removeBtn = removeForm.querySelector("button");
      removeBtn.disabled = true;
      input.disabled = true;
      status.textContent = "Đang xóa…";
      recalculate();
      try {
        // Bảo đảm yêu cầu lưu số lượng cũ kết thúc trước khi xóa.
        if (state.request) await state.request;
        await sendUpdate(state.id, 0);
        state.removed = true;
        states.delete(state.id);
        row.remove();
        setError("");
        if (!states.size) {
          content.remove();
          const empty = document.createElement("div");
          empty.className = "empty-state";
          empty.innerHTML = '<h3>Giỏ hàng đang trống</h3><p>Bạn có thể tiếp tục khám phá sản phẩm.</p>';
          const link = document.createElement("a");
          link.className = "btn btn-primary cart-empty-cta";
          link.href = document.querySelector(".cart-keep-shopping")?.getAttribute("href") || "/products";
          link.textContent = "Tiếp tục mua sắm →";
          empty.append(link);
          root.append(empty);
        }
      } catch (err) {
        state.deleting = false;
        removeBtn.disabled = false;
        input.disabled = false;
        status.textContent = "Chưa xóa";
        setError(err.message || "Không thể xóa sản phẩm.");
      }
      recalculate();
    });
  }
  checkout?.addEventListener("click", event => {
    if (hasPendingChanges()) {
      event.preventDefault();
      setError("Đang lưu số lượng. Vui lòng hoàn tất cập nhật trước khi đặt hàng.");
    }
  });
  recalculate();
})();
