// Wordkeep: a notebook page under the whole strip. The ruled lines and paper tone are laid out in panorama
// coordinates, and each Markdown mark sits on a seam, so neighbouring cards share it and the set reads as one page.
import {panoramaGradient} from './studio/shapes.js';

function brand(main) {
  const node = document.createElement('div');
  node.className = 'brand';
  node.dataset.checkBounds = '';
  node.innerHTML = '<img src="../brand/icon.svg" alt="">';
  node.append(document.createTextNode('Wordkeep'));
  main.prepend(node);
}

/** One Markdown mark per seam: the mark of card k sits on the seam between card k and k + 1. */
function glyph(main, {W, H, kind, index, slides}, k) {
  const s = slides[k];
  if (!s || !s.glyph || k >= slides.length - 1) return;
  const node = document.createElement('div');
  // The mark of seam k is drawn on card k (its right edge) and card k + 1 (its left edge).
  node.className = `glyph ${k === index ? 'seam-right' : 'seam-left'}`;
  node.setAttribute('aria-hidden', 'true');
  node.textContent = s.glyph;
  const landscape = W > H;
  const y = landscape ? H * (k % 2 ? .2 : .82) : H * [.62, .8, .7][k % 3];
  node.style.left = `${(k + 1 - index) * W}px`;
  node.style.top = `${y}px`;
  node.style.setProperty('--glyph-size', `${Math.round(Math.min(W, H) * (s.glyph.length > 2 ? .26 : .38) * (landscape ? .7 : 1))}px`);
  main.append(node);
}

export default {
  // The ruled page runs across every seam.
  continuous: true,

  art(ctx) {
    const {W, H} = ctx;
    // College-ruled paper: a faint blue rule every line, starting below a top margin.
    const gap = Math.round(Math.min(W, H) / 15);
    const lines = [];
    for (let y = gap * 2; y < H; y += gap) lines.push(`<path d="M0 ${y}H${W}" stroke="#5C7FA8" stroke-opacity=".14" stroke-width="2"/>`);
    return `<defs>${panoramaGradient(ctx, 'paper', ['#F8F2E6', '#F5EEE1', '#F7F0E3', '#F4EDDF'])}</defs>` +
      `<rect width="${W}" height="${H}" fill="url(#paper)"/>${lines.join('')}`;
  },

  compose(ctx) {
    const {slide, main, index} = ctx;
    if (slide.hl) main.style.setProperty('--hl', slide.hl);
    if (slide.brand) brand(main);
    glyph(main, ctx, index - 1);
    glyph(main, ctx, index);
  }
};
