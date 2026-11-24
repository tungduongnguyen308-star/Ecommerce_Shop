/** điều hướng banner trang chủ, không phụ thuộc thư viện ngoài. */
(() => {
  'use strict';
  const carousel = document.getElementById('ecommerce-promo-carousel');
  if (!carousel) return;

  const cards = [...carousel.querySelectorAll('.promo-card')];
  const dots = [...carousel.querySelectorAll('[data-promo-dot]')];
  const previous = carousel.querySelector('[data-promo-prev]');
  const next = carousel.querySelector('[data-promo-next]');
  const mobile = window.matchMedia('(max-width: 767.98px)');
  let active = 0;

  function show(index) {
    active = (index + cards.length) % cards.length;
    cards.forEach((card, i) => {
      card.classList.toggle('is-active', i === active);
      card.style.order = String((i - active + cards.length) % cards.length);
      // Trên màn hình nhỏ chỉ một banner hiển thị; loại banner ẩn khỏi tab order.
      const hidden = mobile.matches && i !== active;
      if (hidden) card.setAttribute('tabindex', '-1');
      else card.removeAttribute('tabindex');
    });
    dots.forEach((dot, i) => {
      const selected = i === active;
      dot.classList.toggle('is-active', selected);
      dot.setAttribute('aria-pressed', String(selected));
    });
  }

  previous?.addEventListener('click', () => show(active - 1));
  next?.addEventListener('click', () => show(active + 1));
  dots.forEach((dot, index) => dot.addEventListener('click', () => show(index)));
  mobile.addEventListener?.('change', () => show(active));
  carousel.addEventListener('keydown', (event) => {
    if (event.key !== 'ArrowLeft' && event.key !== 'ArrowRight') return;
    if (event.target instanceof HTMLInputElement) return;
    event.preventDefault();
    show(active + (event.key === 'ArrowRight' ? 1 : -1));
  });
  show(0);
})();
