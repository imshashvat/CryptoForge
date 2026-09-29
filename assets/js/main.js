/* ============================================================
   CryptoForge — main.js
   Production startup landing page
   Real-time data from /api/prices (backend) with
   CoinGecko fallback for market table enrichment.
   ============================================================ */

'use strict';

/* ---- CONFIG ---- */
const BACKEND_URL     = window.CRYPTOFORGE_BACKEND_URL ||
                        (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1'
                          ? 'http://localhost:8080'
                          : 'https://cryptoforge-api.onrender.com');
const API_BASE        = `${BACKEND_URL}/api`;
const COINGECKO_BASE  = 'https://api.coingecko.com/api/v3';
const PRICE_INTERVAL  = 15000; // 15s
const TICKER_INTERVAL = 15000;

// Coin metadata not in backend response
const COIN_META = {
  BTC:  { icon: '₿', bg: 'linear-gradient(135deg,#F7931A,#FFB347)', cg: 'bitcoin',       name: 'Bitcoin'    },
  ETH:  { icon: 'Ξ', bg: 'linear-gradient(135deg,#627EEA,#9FACF7)', cg: 'ethereum',      name: 'Ethereum'   },
  SOL:  { icon: '◎', bg: 'linear-gradient(135deg,#9945FF,#14F195)', cg: 'solana',        name: 'Solana'     },
  BNB:  { icon: 'B', bg: 'linear-gradient(135deg,#F3BA2F,#F0B90B)', cg: 'binancecoin',   name: 'BNB'        },
  XRP:  { icon: '✕', bg: 'linear-gradient(135deg,#346AA9,#00AAE4)', cg: 'ripple',        name: 'XRP'        },
  ADA:  { icon: 'A', bg: 'linear-gradient(135deg,#0D1E4D,#3CC8C8)', cg: 'cardano',       name: 'Cardano'    },
  AVAX: { icon: 'Δ', bg: 'linear-gradient(135deg,#E84142,#FF6B6B)', cg: 'avalanche-2',   name: 'Avalanche'  },
};

/* ---- GLOBAL STATE ---- */
let priceData     = {};   // { BTC: { price, change24h, volume, marketCap }, ... }
let activeTab     = 'all';
let heroTF        = '1H';
let heroChartData = [];
let animFrame     = null;
let sparklineData = {};  // { BTC: [prices...], ... }

/* ============================================================
   BOOT
   ============================================================ */
window.addEventListener('load', () => {
  setTimeout(() => {
    document.getElementById('loader').classList.add('done');
    boot();
  }, 1750);
});

async function boot() {
  initLenis();
  initNav();
  initScrollAnimations();
  initFAQ();
  initTimeframes();
  initMarketTabs();
  updateBackendLinks();

  // Load real data
  await fetchPrices();
  initTicker();
  renderMarketTable();
  drawDashMockup();
  initHeroChart();

  // Live refresh loops
  setInterval(async () => {
    await fetchPrices();
    updateTickerPrices();
    renderMarketTable();
    updateHeroPrice();
    updateOrderBook();
  }, PRICE_INTERVAL);

  // Animate hero price every 4s
  setInterval(updateHeroPrice, 4000);
  // Animate floating cards with real or simulated trades
  setInterval(updateFloatingCards, 6000);

  initStatCounters();
}

/* ============================================================
   FETCH PRICES FROM BACKEND /api/prices
   Falls back to CoinGecko if backend is down
   ============================================================ */
async function fetchPrices() {
  try {
    const res = await fetch(`${API_BASE}/prices`, { signal: AbortSignal.timeout(5000) });
    if (!res.ok) throw new Error('backend-down');
    const data = await res.json();
    data.forEach(item => {
      priceData[item.code] = {
        price:     parseFloat(item.price)     || 0,
        change24h: parseFloat(item.change24h) || 0,
        marketCap: parseFloat(item.marketCap) || 0,
        volume:    parseFloat(item.volume24h) || 0,
      };
    });
    // Fetch sparklines from backend too
    await fetchSparklines();
  } catch {
    // Fallback: CoinGecko direct
    await fetchCoinGecko();
  }
}

async function fetchCoinGecko() {
  try {
    const ids = Object.values(COIN_META).map(m => m.cg).join(',');
    const res  = await fetch(
      `${COINGECKO_BASE}/simple/price?ids=${ids}&vs_currencies=usd&include_24hr_change=true&include_24hr_vol=true&include_market_cap=true`,
      { signal: AbortSignal.timeout(8000) }
    );
    const json = await res.json();
    Object.entries(COIN_META).forEach(([code, meta]) => {
      const d = json[meta.cg];
      if (!d) return;
      priceData[code] = {
        price:     d.usd             || 0,
        change24h: d.usd_24h_change  || 0,
        volume:    d.usd_24h_vol     || 0,
        marketCap: d.usd_market_cap  || 0,
      };
    });
    // Fetch CoinGecko sparklines
    await fetchCoinGeckoSparklines();
  } catch { /* silence — will retry */ }
}

async function fetchSparklines() {
  const codes = Object.keys(COIN_META);
  await Promise.all(codes.map(async code => {
    try {
      const res = await fetch(`${API_BASE}/market/sparkline/${code}`, { signal: AbortSignal.timeout(4000) });
      if (res.ok) sparklineData[code] = await res.json();
    } catch { /* ignore */ }
  }));
}

async function fetchCoinGeckoSparklines() {
  try {
    const ids = Object.values(COIN_META).map(m => m.cg).join(',');
    const res  = await fetch(
      `${COINGECKO_BASE}/coins/markets?vs_currency=usd&ids=${ids}&sparkline=true&price_change_percentage=24h`,
      { signal: AbortSignal.timeout(8000) }
    );
    const data = await res.json();
    data.forEach(coin => {
      const code = Object.entries(COIN_META).find(([,m]) => m.cg === coin.id)?.[0];
      if (code && coin.sparkline_in_7d?.price) {
        // Use last 24 points of 7d sparkline
        const pts = coin.sparkline_in_7d.price;
        sparklineData[code] = pts.slice(-24).map(v => Math.round(v * 100) / 100);
      }
    });
  } catch { /* ignore */ }
}

/* ============================================================
   TOP TICKER
   ============================================================ */
function initTicker() {
  renderTicker();
}

function renderTicker() {
  const track = document.getElementById('ticker-track');
  if (!track) return;

  const coins = Object.entries(COIN_META);
  // Double for seamless loop
  const html = [...coins, ...coins].map(([code, meta]) => {
    const p = priceData[code];
    if (!p) return '';
    const up   = p.change24h >= 0;
    const price = fmtPrice(p.price);
    const chg   = (up ? '▲ +' : '▼ ') + Math.abs(p.change24h).toFixed(2) + '%';
    return `<div class="t-item"><span class="t-name">${code}/USDT</span><span class="t-price">$${price}</span><span class="t-chg ${up ? 'up' : 'down'}">${chg}</span></div>`;
  }).join('');
  track.innerHTML = html;
}

function updateTickerPrices() {
  renderTicker(); // re-render with new prices
}

/* ============================================================
   HERO PRICE + CHART
   ============================================================ */
function updateHeroPrice() {
  const btc = priceData['BTC'];
  if (!btc) return;
  const priceEl  = document.getElementById('hero-price');
  const changeEl = document.getElementById('hero-change');
  const volEl    = document.getElementById('hero-vol');
  const capEl    = document.getElementById('hero-cap');
  if (priceEl)  priceEl.textContent  = '$' + fmtPrice(btc.price);
  if (changeEl) {
    const up = btc.change24h >= 0;
    changeEl.textContent = (up ? '▲ +' : '▼ ') + Math.abs(btc.change24h).toFixed(2) + '%';
    changeEl.className   = 'chart-change ' + (up ? 'up' : 'down');
  }
  if (volEl && btc.volume)    volEl.textContent    = '$' + fmtLarge(btc.volume);
  if (capEl && btc.marketCap) capEl.textContent    = '$' + fmtLarge(btc.marketCap);
}

// placeholder removed — real initHeroChart is defined below

function buildChartData(tf = '1H') {
  const btc = priceData['BTC'];
  const base = btc ? btc.price : 67000;
  const sp   = sparklineData['BTC'];

  if (sp && sp.length && tf === '1H') {
    // Use real sparkline data if available
    heroChartData = [...sp];
  } else {
    const pts = tf === '1Y' ? 365 : tf === '1M' ? 90 : tf === '1W' ? 60 : tf === '24H' ? 48 : 40;
    const vol = tf === '1Y' ? 6000 : tf === '1M' ? 2500 : tf === '1W' ? 1500 : tf === '24H' ? 700 : 350;
    heroChartData = generateWalk(base - vol * 0.6, pts, vol / 18);
    heroChartData[heroChartData.length - 1] = base; // snap end to live price
  }
}

function generateWalk(start, points, vol) {
  const arr = [start];
  for (let i = 1; i < points; i++) {
    arr.push(Math.max(arr[i-1] * 0.8, arr[i-1] + (Math.random() - 0.47) * vol));
  }
  return arr;
}

let chartAnimProgress = 0;
function drawHeroChart(full = false) {
  const canvas = document.getElementById('hero-chart');
  if (!canvas || !heroChartData.length) return;
  const data = full ? heroChartData : heroChartData.slice(0, chartAnimProgress);
  if (data.length < 2) return;
  renderLineChart(canvas, data, { pad: 10, showLabels: true });
}

function animateChartIn() {
  cancelAnimationFrame(animFrame);
  chartAnimProgress = 2;
  const total = heroChartData.length;
  function step() {
    chartAnimProgress = Math.min(chartAnimProgress + Math.ceil(total / 38), total);
    drawHeroChart();
    if (chartAnimProgress < total) animFrame = requestAnimationFrame(step);
    else startLiveChartTick();
  }
  animFrame = requestAnimationFrame(step);
}

function startLiveChartTick() {
  let data = [...heroChartData];
  function tick() {
    const last   = data[data.length - 1];
    const btcNow = priceData['BTC']?.price || last;
    const delta  = (Math.random() - 0.48) * (last * 0.003);
    // Gently drift toward real price
    const drifted = last + delta + (btcNow - last) * 0.05;
    data.push(parseFloat(drifted.toFixed(2)));
    if (data.length > heroChartData.length + 20) data.shift();
    const canvas = document.getElementById('hero-chart');
    if (canvas) renderLineChart(canvas, data, { pad: 10, showLabels: true });
    animFrame = setTimeout(() => requestAnimationFrame(tick), 2800 + Math.random() * 2200);
  }
  animFrame = setTimeout(() => requestAnimationFrame(tick), 3500);
}

function initHeroChart() {
  buildChartData(heroTF);
  animateChartIn();
  updateHeroPrice();
  updateOrderBook();
  updateFloatingCards();
}

/* ============================================================
   CANVAS LINE CHART RENDERER
   ============================================================ */
function renderLineChart(canvas, data, opts = {}) {
  const ctx = canvas.getContext('2d');
  const W   = Math.max(canvas.clientWidth  || canvas.width  || 0, 10);
  const H   = Math.max(canvas.clientHeight || canvas.height || 0, 10);
  canvas.width  = W;
  canvas.height = H;

  if (!data || data.length < 2) return;

  const pad = opts.pad || 10;
  const col = opts.color || '#00D68F';
  const min = Math.min(...data);
  const max = Math.max(...data);
  const rng = max - min || 1;
  if (!isFinite(min) || !isFinite(max)) return;

  ctx.clearRect(0, 0, W, H);

  // Grid
  ctx.strokeStyle = 'rgba(255,255,255,0.04)';
  ctx.lineWidth   = 1;
  [0.25, 0.5, 0.75].forEach(t => {
    const y = pad + (1 - t) * (H - pad * 2);
    ctx.beginPath(); ctx.moveTo(pad, y); ctx.lineTo(W - pad, y); ctx.stroke();
  });

  const gx = i => pad + (i / (data.length - 1)) * (W - pad * 2);
  const gy = v => pad + (1 - (v - min) / rng) * (H - pad * 2);

  // Fill gradient
  ctx.beginPath();
  data.forEach((v, i) => i === 0 ? ctx.moveTo(gx(i), gy(v)) : ctx.lineTo(gx(i), gy(v)));
  ctx.lineTo(gx(data.length - 1), H);
  ctx.lineTo(gx(0), H);
  ctx.closePath();
  const grad = ctx.createLinearGradient(0, pad, 0, H);
  grad.addColorStop(0,   'rgba(0,214,143,.25)');
  grad.addColorStop(.6,  'rgba(0,214,143,.05)');
  grad.addColorStop(1,   'rgba(0,214,143,0)');
  ctx.fillStyle = grad;
  ctx.fill();

  // Line
  ctx.beginPath();
  data.forEach((v, i) => i === 0 ? ctx.moveTo(gx(i), gy(v)) : ctx.lineTo(gx(i), gy(v)));
  ctx.strokeStyle = col;
  ctx.lineWidth   = 2.2;
  ctx.lineJoin    = 'round';
  ctx.lineCap     = 'round';
  ctx.stroke();

  // Live dot
  const lx = gx(data.length - 1);
  const ly = gy(data[data.length - 1]);
  if (isFinite(lx) && isFinite(ly)) {
    try {
      const glo = ctx.createRadialGradient(lx, ly, 0, lx, ly, 14);
      glo.addColorStop(0, 'rgba(0,214,143,.3)');
      glo.addColorStop(1, 'rgba(0,214,143,0)');
      ctx.beginPath(); ctx.arc(lx, ly, 14, 0, Math.PI * 2);
      ctx.fillStyle = glo; ctx.fill();
    } catch { /* skip glow if canvas not visible */ }
    ctx.beginPath(); ctx.arc(lx, ly, 4, 0, Math.PI * 2);
    ctx.fillStyle = col; ctx.fill();
    ctx.strokeStyle = '#0B0E11'; ctx.lineWidth = 2; ctx.stroke();
  }

  // Price labels
  if (opts.showLabels) {
    ctx.font = '10px Inter,system-ui'; ctx.textAlign = 'right';
    [0.3, 0.65, 1].forEach(t => {
      const v = min + t * rng;
      const y = pad + (1 - t) * (H - pad * 2);
      ctx.fillStyle = 'rgba(156,165,177,.5)';
      ctx.fillText('$' + fmtPrice(v), W - pad, y + 3);
    });
  }
}

/* ============================================================
   SPARKLINE (mini chart for market table)
   ============================================================ */
function drawSparkline(canvas, data, isUp) {
  const ctx = canvas.getContext('2d');
  const W = canvas.width;
  const H = canvas.height;
  ctx.clearRect(0, 0, W, H);
  if (!data || data.length < 2) return;

  const min = Math.min(...data);
  const max = Math.max(...data);
  const rng = max - min || 1;
  const gx  = i => (i / (data.length - 1)) * W;
  const gy  = v => H - (((v - min) / rng) * (H * 0.8) + H * 0.1);
  const col = isUp ? '#00D68F' : '#F6465D';

  ctx.beginPath();
  data.forEach((v, i) => i === 0 ? ctx.moveTo(gx(i), gy(v)) : ctx.lineTo(gx(i), gy(v)));
  ctx.strokeStyle = col; ctx.lineWidth = 1.5; ctx.lineJoin = 'round'; ctx.stroke();
}

/* ============================================================
   TIMEFRAME BUTTONS
   ============================================================ */
function initTimeframes() {
  document.querySelectorAll('.tf').forEach(btn => {
    btn.addEventListener('click', () => {
      document.querySelectorAll('.tf').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      heroTF = btn.dataset.tf;
      clearTimeout(animFrame); cancelAnimationFrame(animFrame);
      buildChartData(heroTF);
      animateChartIn();
    });
  });
}

/* ============================================================
   ORDER BOOK (simulated around real BTC price)
   ============================================================ */
function updateOrderBook() {
  const p = priceData['BTC']?.price;
  if (!p) return;

  const spread = p * 0.0003;
  const asks   = [p + spread * 1.1, p + spread * 2.3].map(v => v.toFixed(2));
  const bids   = [p - spread * 0.9, p - spread * 2.1].map(v => v.toFixed(2));
  const qty    = () => (Math.random() * 0.3 + 0.01).toFixed(3);

  const set = (id, price, q) => {
    const el = document.getElementById(id);
    if (el) el.innerHTML = `<span>${price}</span><span>${q}</span>`;
  };
  set('ob-s1', asks[0], qty()); set('ob-s2', asks[1], qty());
  set('ob-b1', bids[0], qty()); set('ob-b2', bids[1], qty());

  const mid = document.getElementById('ob-mid');
  if (mid) mid.innerHTML = `${p.toLocaleString(undefined, {maximumFractionDigits:2})} <small>Spread ${((spread / p) * 100).toFixed(3)}%</small>`;
}

/* ============================================================
   FLOATING ORDER CARDS (real recent trades if available)
   ============================================================ */
async function updateFloatingCards() {
  try {
    const res    = await fetch(`${API_BASE}/market/recent-trades`, { signal: AbortSignal.timeout(4000) });
    const trades = await res.json();
    if (trades && trades.length >= 2) {
      const buys  = trades.filter(t => t.type === 'BUY');
      const sells = trades.filter(t => t.type === 'SELL');
      if (buys.length) {
        const b = buys[0];
        setFC('buy', `${parseFloat(b.quantity).toFixed(4)} ${b.assetCode}`, `$${fmtPrice(b.totalValue)}`);
      }
      if (sells.length) {
        const s = sells[0];
        setFC('sell', `${parseFloat(s.quantity).toFixed(4)} ${s.assetCode}`, `$${fmtPrice(s.totalValue)}`);
      }
      return;
    }
  } catch { /* fallback below */ }

  // Fallback: simulate plausible orders using real prices
  const coins = Object.keys(priceData);
  if (!coins.length) return;
  const rndCoin = coins[Math.floor(Math.random() * coins.length)];
  const p = priceData[rndCoin]?.price || 100;
  const qty = (Math.random() * 2 + 0.01).toFixed(4);
  const total = (qty * p).toFixed(2);
  setFC('buy',  `${qty} ${rndCoin}`, `$${fmtPrice(parseFloat(total))}`);

  const rndCoin2 = coins[Math.floor(Math.random() * coins.length)];
  const p2 = priceData[rndCoin2]?.price || 100;
  const qty2 = (Math.random() * 3 + 0.01).toFixed(4);
  const total2 = (qty2 * p2).toFixed(2);
  setFC('sell', `${qty2} ${rndCoin2}`, `$${fmtPrice(parseFloat(total2))}`);
}

function setFC(type, val, usd) {
  const valEl = document.getElementById(`fc-${type}-val`);
  const usdEl = document.getElementById(`fc-${type}-usd`);
  if (valEl) valEl.textContent = val;
  if (usdEl) usdEl.textContent = usd;
}

/* ============================================================
   DASHBOARD MOCKUP (about section) — real asset prices
   ============================================================ */
async function drawDashMockup() {
  // Draw portfolio chart
  const canvas = document.getElementById('portfolio-chart');
  if (canvas) {
    const btc = priceData['BTC']?.price || 67000;
    const data = generateWalk(10000, 60, 180);
    data[data.length - 1] = 10000 + (Math.random() * 3000 - 500);
    renderLineChart(canvas, data, { pad: 8, color: '#00D68F' });
  }

  // Populate assets
  const container = document.getElementById('dm-assets');
  if (!container || !Object.keys(priceData).length) return;

  const rows = Object.entries(COIN_META).slice(0, 3).map(([code, meta]) => {
    const p = priceData[code];
    if (!p) return '';
    const qty   = code === 'BTC' ? 0.05 : code === 'ETH' ? 2.5 : 12;
    const val   = (qty * p.price).toFixed(2);
    const up    = p.change24h >= 0;
    const chgTxt = (up ? '▲ +' : '▼ ') + Math.abs(p.change24h).toFixed(2) + '%';
    const iconCls = code === 'BTC' ? 'btc-badge' : code === 'ETH' ? 'eth-ico' : 'sol-ico';
    return `<div class="dm-row">
      <div class="dm-left">
        <div class="dm-ico ${iconCls}">${meta.icon}</div>
        <div><div class="dm-an">${meta.name}</div><div class="dm-am">${qty} ${code}</div></div>
      </div>
      <div class="dm-val">
        <div class="dm-pr">$${fmtPrice(parseFloat(val))}</div>
        <div class="dm-ch ${up ? 'up' : 'down'}">${chgTxt}</div>
      </div>
    </div>`;
  }).join('');
  container.innerHTML = rows;

  // Total value
  const total = Object.entries(COIN_META).slice(0, 3).reduce((sum, [code, _]) => {
    const qtys = { BTC: 0.05, ETH: 2.5, SOL: 12 };
    return sum + (qtys[code] || 0) * (priceData[code]?.price || 0);
  }, 0);
  const tot = document.getElementById('dm-total');
  const pnl = document.getElementById('dm-pnl');
  if (tot) tot.textContent = fmtPrice(total);
  if (pnl) {
    const pct = (((total - 10000) / 10000) * 100);
    pnl.textContent = (pct >= 0 ? '+' : '') + pct.toFixed(1) + '%';
    pnl.className = pct >= 0 ? 'up' : 'down';
  }
}

/* ============================================================
   MARKET TABLE
   ============================================================ */
function renderMarketTable() {
  const tbody = document.getElementById('market-tbody');
  if (!tbody) return;

  let coins = Object.entries(COIN_META).map(([code, meta]) => ({
    code, meta,
    price:     priceData[code]?.price     || 0,
    change24h: priceData[code]?.change24h || 0,
    volume:    priceData[code]?.volume    || 0,
    marketCap: priceData[code]?.marketCap || 0,
  })).filter(c => c.price > 0);

  if (!coins.length) return;

  // Sort by tab
  if (activeTab === 'gainers') coins.sort((a,b) => b.change24h - a.change24h);
  else if (activeTab === 'losers') coins.sort((a,b) => a.change24h - b.change24h);
  else coins.sort((a,b) => b.marketCap - a.marketCap);

  const rows = coins.map((c, i) => {
    const up    = c.change24h >= 0;
    const chg   = (up ? '▲ +' : '▼ ') + Math.abs(c.change24h).toFixed(2) + '%';
    const price = '$' + fmtPrice(c.price);
    const vol   = '$' + fmtLarge(c.volume);
    const cap   = '$' + fmtLarge(c.marketCap);
    const sp    = sparklineData[c.code];
    return `<tr>
      <td class="muted-cell">${i + 1}</td>
      <td><div class="coin-cell">
        <div class="ci" style="background:${c.meta.bg}">${c.meta.icon}</div>
        <div><div class="cn">${c.meta.name}</div><div class="cs">${c.code}/USDT</div></div>
      </div></td>
      <td class="price-cell">${price}</td>
      <td><span class="chg-pill ${up ? 'up' : 'down'}">${chg}</span></td>
      <td class="muted-cell">${vol}</td>
      <td class="muted-cell">${cap}</td>
      <td class="spark-cell"><canvas id="sp-${c.code}" width="80" height="32"></canvas></td>
      <td><a href="${BACKEND_URL}/trade?asset=${c.code}" class="trade-btn">Trade</a></td>
    </tr>`;
  }).join('');

  tbody.innerHTML = rows;

  // Draw sparklines
  coins.forEach(c => {
    const canvas = document.getElementById(`sp-${c.code}`);
    if (canvas && sparklineData[c.code]) {
      drawSparkline(canvas, sparklineData[c.code], c.change24h >= 0);
    }
  });
}

function initMarketTabs() {
  document.querySelectorAll('.mkt-tab').forEach(tab => {
    tab.addEventListener('click', () => {
      document.querySelectorAll('.mkt-tab').forEach(t => t.classList.remove('active'));
      tab.classList.add('active');
      activeTab = tab.dataset.t;
      renderMarketTable();
    });
  });
}

/* ============================================================
   LENIS
   ============================================================ */
function initLenis() {
  if (typeof Lenis === 'undefined') return;
  const lenis = new Lenis({ lerp: 0.08, duration: 1.2, smoothWheel: true });
  function raf(t) { lenis.raf(t); requestAnimationFrame(raf); }
  requestAnimationFrame(raf);
  if (typeof ScrollTrigger !== 'undefined') {
    lenis.on('scroll', ScrollTrigger.update);
    gsap.ticker.add(t => lenis.raf(t * 1000));
    gsap.ticker.lagSmoothing(0);
  }
}

/* ============================================================
   NAV
   ============================================================ */
function initNav() {
  const nav    = document.getElementById('nav');
  const burger = document.getElementById('nav-burger');
  const menu   = document.getElementById('mobile-menu');

  window.addEventListener('scroll', () => nav?.classList.toggle('scrolled', window.scrollY > 40), { passive: true });

  burger?.addEventListener('click', () => {
    const open = menu.classList.toggle('open');
    burger.setAttribute('aria-expanded', String(open));
    menu.setAttribute('aria-hidden', String(!open));
    const s = burger.querySelectorAll('span');
    if (open) { s[0].style.transform='rotate(45deg) translate(5px,5px)'; s[1].style.opacity='0'; s[2].style.transform='rotate(-45deg) translate(5px,-5px)'; }
    else       { s[0].style.transform=''; s[1].style.opacity=''; s[2].style.transform=''; }
  });
  document.querySelectorAll('.mobile-link').forEach(l => l.addEventListener('click', () => {
    menu.classList.remove('open');
    burger.setAttribute('aria-expanded','false');
    menu.setAttribute('aria-hidden','true');
    const s = burger.querySelectorAll('span');
    s[0].style.transform=''; s[1].style.opacity=''; s[2].style.transform='';
  }));

  // Active link
  const sections = document.querySelectorAll('section[id]');
  const links    = document.querySelectorAll('.nav-links a');
  new IntersectionObserver(entries => entries.forEach(e => {
    if (e.isIntersecting) links.forEach(l => l.classList.toggle('active', l.getAttribute('href') === '#' + e.target.id));
  }), { threshold: 0.35 }).observe(document.querySelectorAll('section')[0]);
  sections.forEach(s => new IntersectionObserver(entries => {
    if (entries[0].isIntersecting) links.forEach(l => l.classList.toggle('active', l.getAttribute('href') === '#' + s.id));
  }, { threshold: 0.35 }).observe(s));
}

/* ============================================================
   SCROLL ANIMATIONS
   ============================================================ */
function initScrollAnimations() {
  const io = new IntersectionObserver((entries) => {
    entries.forEach(e => { if (e.isIntersecting) { e.target.classList.add('in'); io.unobserve(e.target); } });
  }, { threshold: 0.1, rootMargin: '0px 0px -30px 0px' });
  document.querySelectorAll('.anim').forEach(el => io.observe(el));

  // GSAP extras
  if (typeof gsap === 'undefined') return;
  gsap.registerPlugin(ScrollTrigger);
  gsap.from('.hero-left > *', { y: 36, opacity: 0, duration: .9, stagger: .1, ease: 'power3.out', delay: .2 });
  gsap.from('.chart-panel',   { y: 50, opacity: 0, duration: 1,  ease: 'power3.out', delay: .5 });
  gsap.from('.fc-buy',  { x: -40, opacity: 0, duration: .8, ease: 'back.out(1.5)', delay: 1.0 });
  gsap.from('.fc-sell', { x:  40, opacity: 0, duration: .8, ease: 'back.out(1.5)', delay: 1.2 });
  gsap.from('.ob-mini', { y:  28, opacity: 0, duration: .8, ease: 'back.out(1.5)', delay: 1.1 });
}

/* ============================================================
   FAQ ACCORDION
   ============================================================ */
function initFAQ() {
  document.querySelectorAll('.faq-item').forEach(item => {
    const btn = item.querySelector('.faq-q');
    const ans = item.querySelector('.faq-ans');
    if (!btn || !ans) return;
    btn.addEventListener('click', () => {
      const open = btn.getAttribute('aria-expanded') === 'true';
      document.querySelectorAll('.faq-item').forEach(i => {
        i.querySelector('.faq-q')?.setAttribute('aria-expanded','false');
        i.querySelector('.faq-ans')?.classList.remove('open');
      });
      if (!open) { btn.setAttribute('aria-expanded','true'); ans.classList.add('open'); }
    });
  });
}

/* ============================================================
   STAT COUNTERS
   ============================================================ */
function initStatCounters() {
  const io = new IntersectionObserver(entries => {
    entries.forEach(e => {
      if (!e.isIntersecting) return;
      io.unobserve(e.target);
      const el = e.target;
      const target = parseInt(el.dataset.target, 10);
      const suffix = el.dataset.suffix || '';
      let cur = 0; const steps = 60; const inc = target / steps;
      const timer = setInterval(() => {
        cur = Math.min(cur + inc, target);
        el.textContent = Math.floor(cur).toLocaleString() + suffix;
        if (cur >= target) clearInterval(timer);
      }, 22);
    });
  }, { threshold: 0.6 });
  document.querySelectorAll('.stat-num[data-target]').forEach(el => io.observe(el));
}

/* ============================================================
   FORMAT HELPERS
   ============================================================ */
function fmtPrice(v) {
  if (!v && v !== 0) return '—';
  if (v >= 10000) return v.toLocaleString('en-US', { maximumFractionDigits: 2 });
  if (v >= 1)     return v.toLocaleString('en-US', { maximumFractionDigits: 4 });
  return v.toFixed(6);
}

function fmtLarge(v) {
  if (!v) return '—';
  if (v >= 1e12) return (v / 1e12).toFixed(2) + 'T';
  if (v >= 1e9)  return (v / 1e9).toFixed(2) + 'B';
  if (v >= 1e6)  return (v / 1e6).toFixed(2) + 'M';
  return v.toLocaleString('en-US', { maximumFractionDigits: 0 });
}

function updateBackendLinks() {
  document.querySelectorAll('a[href*="localhost:8080"]').forEach(a => {
    a.href = a.href.replace(/http:\/\/localhost:8080/g, BACKEND_URL);
  });
}

