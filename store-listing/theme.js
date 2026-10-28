// Wordkeep: a notebook page under the whole strip. The ruled lines and paper tone are laid out in panorama
// coordinates, and each Markdown mark sits on a seam, so neighbouring cards share it and the set reads as one page.
import {panoramaGradient} from './studio/shapes.js';

function brand(main) {
  const node = document.createElement('div');
  node.className = 'brand';
  node.dataset.checkBounds = '';
  node.innerHTML = '<img src="../../branding/wordkeep-icon.svg" alt="">';
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
  },

  /** Frames follow the captured pixels: bezel and corner radius scale with the frame width. */
  layout({main}) {
    for (const node of main.querySelectorAll('.device')) {
      if (main.classList.contains('compare')) break;
      const image = node.querySelector('img'), width = node.clientWidth, landscape = image.naturalWidth > image.naturalHeight;
      node.style.setProperty('--bezel', `${Math.round(width * (landscape ? .011 : .017))}px`);
      node.style.setProperty('--radius', `${Math.round(width * (landscape ? .03 : .09))}px`);
    }
  },

  // Single devices keep their status bar and left side inside the card (landscape may run off the right)
  // and show most of the screen.
  decorate({W, H, slide, devices}) {
    if (slide.layout === 'compare') {
      // Each name sits just under its screen.
      for (const label of devices[0].node.parentElement.querySelectorAll('.device-label')) label.style.setProperty('--h', `${devices[0].box.height}px`);
      return;
    }
    if (/stack|pair|duo|drop/.test(slide.layout || '')) return;
    for (const {box, spec} of devices) {
      if (box.left < 12 || box.top < 12 || (W < H && box.left + box.width > W - 12)) throw new Error(`Device leaves the card: ${spec.raw}`);
      if ((H - box.top) / box.height < .7) throw new Error(`Device runs too far off the bottom: ${spec.raw}`);
    }
  }
};
