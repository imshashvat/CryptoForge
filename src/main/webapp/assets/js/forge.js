/**
 * CryptoForge — forge.js v2
 * Theme toggle · Market table sort/search · Sparklines · Live prices · Donut chart
 */
(function () {
  'use strict';

  /* ── Theme ────────────────────────────────────────────────────────────── */
  const THEME_KEY = 'cf-theme';
  function applyTheme(t) {
    document.documentElement.setAttribute('data-theme', t);
    // SVG sun/moon toggle: sun shows when dark (to switch to light), moon shows when light (to switch to dark)
    var sun  = document.getElementById('themeIconSun');
    var moon = document.getElementById('themeIconMoon');
    if (sun && moon) {
      sun.style.display  = t === 'dark' ? 'block' : 'none';
      moon.style.display = t === 'dark' ? 'none'  : 'block';
    }
    localStorage.setItem(THEME_KEY, t);
  }
  function toggleTheme() {
    var cur = document.documentElement.getAttribute('data-theme') || 'light';
    applyTheme(cur === 'dark' ? 'light' : 'dark');
  }
  // Init theme from localStorage immediately
  var saved = localStorage.getItem(THEME_KEY) || 'light';
  applyTheme(saved);

  /* ── Nav scroll effect ──────────────────────────────────────────────── */
  var nav = document.getElementById('siteNav');
  if (nav) {
    window.addEventListener('scroll', function () {
      nav.classList.toggle('scrolled', window.scrollY > 10);
    }, { passive: true });
  }

  /* ── Sparklines (Canvas) ────────────────────────────────────────────── */
  function drawSparkline(canvas, data, color) {
    if (!canvas || !data || data.length < 2) return;
    var w = canvas.width = canvas.offsetWidth || 80;
    var h = canvas.height = canvas.offsetHeight || 32;
    var ctx = canvas.getContext('2d');
    ctx.clearRect(0, 0, w, h);
    var min = Math.min.apply(null, data);
    var max = Math.max.apply(null, data);
    var range = max - min || 1;
    var step = w / (data.length - 1);
    ctx.beginPath();
    data.forEach(function (v, i) {
      var x = i * step;
      var y = h - ((v - min) / range) * (h - 4) - 2;
      i === 0 ? ctx.moveTo(x, y) : ctx.lineTo(x, y);
    });
    ctx.strokeStyle = color;
    ctx.lineWidth = 1.5;
    ctx.lineJoin = 'round';
    ctx.stroke();
    // Fill area under line
    ctx.lineTo(w, h); ctx.lineTo(0, h); ctx.closePath();
    var grad = ctx.createLinearGradient(0, 0, 0, h);
    grad.addColorStop(0, color.replace(')', ',0.15)').replace('rgb', 'rgba'));
    grad.addColorStop(1, color.replace(')', ',0)').replace('rgb', 'rgba'));
    ctx.fillStyle = grad;
    ctx.fill();
  }

  /* ── Donut Chart (Portfolio) ────────────────────────────────────────── */
  window.drawDonut = function (canvasId, data) {
    var canvas = document.getElementById(canvasId);
    if (!canvas) return;
    var ctx = canvas.getContext('2d');
    var w = canvas.width = canvas.height = 180;
    var cx = w / 2, cy = w / 2, r = 72, inner = 44;
    var colors = ['#2563EB','#16A34A','#DC2626','#D97706','#7C3AED','#0891B2','#DB2777'];
    var total = data.reduce(function (s, d) { return s + d.value; }, 0);
    if (total === 0) return;
    var angle = -Math.PI / 2;
    ctx.clearRect(0, 0, w, w);
    data.forEach(function (d, i) {
      var slice = (d.value / total) * 2 * Math.PI;
      ctx.beginPath();
      ctx.moveTo(cx, cy);
      ctx.arc(cx, cy, r, angle, angle + slice);
      ctx.closePath();
      ctx.fillStyle = colors[i % colors.length];
      ctx.fill();
      angle += slice;
    });
    // Inner hole
    ctx.beginPath();
    ctx.arc(cx, cy, inner, 0, 2 * Math.PI);
    ctx.fillStyle = getComputedStyle(document.documentElement).getPropertyValue('--bg-surface').trim() || '#fff';
    ctx.fill();
  };

  /* ── Market Table ──────────────────────────────────────────────────── */
  var marketRows = [];
  var sortCol = 'marketCap', sortDir = -1;

  function formatPrice(p) {
    if (p === null || p === undefined) return '—';
    p = parseFloat(p);
    if (p >= 1) return '$' + p.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    return '$' + p.toFixed(6);
  }
  function formatLarge(n) {
    if (!n) return '—';
    n = parseFloat(n);
    if (n >= 1e12) return '$' + (n / 1e12).toFixed(2) + 'T';
    if (n >= 1e9)  return '$' + (n / 1e9).toFixed(2) + 'B';
    if (n >= 1e6)  return '$' + (n / 1e6).toFixed(2) + 'M';
    return '$' + n.toLocaleString('en-US');
  }
  function changeClass(v) {
    return parseFloat(v) >= 0 ? 'change-up' : 'change-down';
  }
  function formatChange(v) {
    var n = parseFloat(v);
    return (n >= 0 ? '+' : '') + n.toFixed(2) + '%';
  }

  var COIN_ICONS = {
    BTC: '₿', ETH: 'Ξ', BNB: '🟡', SOL: '◎', DOGE: 'Ð',
    XRP: '✕', ADA: '₳', AVAX: '🔺', DOT: '●', MATIC: '🔷',
    SHIB: '🐕', LTC: 'Ł', LINK: '⬡', UNI: '🦄', ATOM: '⚛'
  };

  function renderMarketTable(rows, tbody) {
    if (!tbody) return;
    tbody.innerHTML = '';
    rows.forEach(function (asset, idx) {
      var change = asset.priceChange24h || 0;
      var icon = COIN_ICONS[asset.code] || asset.code.charAt(0);
      var tr = document.createElement('tr');
      tr.setAttribute('data-code', asset.code);
      tr.innerHTML = [
        '<td class="text-muted" style="font-size:12px;width:40px;">' + (idx + 1) + '</td>',
        '<td>',
          '<div class="coin-info">',
            '<div class="coin-icon">' + icon + '</div>',
            '<div>',
              '<div class="coin-name">' + (asset.name || asset.code) + '</div>',
              '<div class="coin-symbol">' + asset.code + '</div>',
            '</div>',
          '</div>',
        '</td>',
        '<td class="price-mono" data-price="' + asset.code + '">' + formatPrice(asset.currentPriceUsd) + '</td>',
        '<td><span class="change-pill ' + changeClass(change) + '">' + formatChange(change) + '</span></td>',
        '<td class="price-mono text-secondary">' + formatLarge(asset.marketCapUsd) + '</td>',
        '<td style="width:90px;padding-right:16px;"><canvas style="width:80px;height:32px;" data-spark="' + asset.code + '"></canvas></td>',
        '<td>',
          '<div class="flex gap-2">',
            '<a href="/order?asset=' + asset.code + '" class="btn btn-sm btn-buy" style="width:60px;padding:5px 0;">Buy</a>',
            '<a href="/order?asset=' + asset.code + '&type=SELL" class="btn btn-sm btn-sell" style="width:60px;padding:5px 0;">Sell</a>',
          '</div>',
        '</td>'
      ].join('');
      tbody.appendChild(tr);

      // Draw sparkline
      var sparkCanvas = tr.querySelector('[data-spark]');
      if (sparkCanvas) {
        var mockData = generateMockSparkline(asset.currentPriceUsd, change);
        var color = parseFloat(change) >= 0 ? '#16A34A' : '#DC2626';
        setTimeout(function (c, d, col) {
          drawSparkline(c, d, col);
        }, 10, sparkCanvas, mockData, color);
      }
    });
  }

  function generateMockSparkline(price, change) {
    var points = 20;
    var data = [];
    var cur = parseFloat(price) || 100;
    var trend = parseFloat(change) / 100 / points;
    for (var i = 0; i < points; i++) {
      cur = cur * (1 + trend + (Math.random() - 0.5) * 0.008);
      data.push(cur);
    }
    return data;
  }

  function sortAndRender(tbody) {
    if (!tbody) return;
    var sorted = marketRows.slice().sort(function (a, b) {
      var av = a[sortCol] || 0, bv = b[sortCol] || 0;
      return (parseFloat(av) - parseFloat(bv)) * sortDir;
    });
    renderMarketTable(sorted, tbody);
  }

  // Init market table
  var marketTbody = document.getElementById('marketTbody');
  if (marketTbody) {
    // Pre-fill from DOM data
    var rows = marketTbody.querySelectorAll('[data-asset]');
    rows.forEach(function (r) {
      try { marketRows.push(JSON.parse(r.dataset.asset)); } catch (e) {}
    });

    // Sort headers
    document.querySelectorAll('.cf-table thead th[data-sort]').forEach(function (th) {
      th.addEventListener('click', function () {
        var col = th.dataset.sort;
        if (col === sortCol) { sortDir *= -1; }
        else { sortCol = col; sortDir = -1; }
        document.querySelectorAll('.cf-table thead th').forEach(function (h) { h.classList.remove('sorted'); });
        th.classList.add('sorted');
        th.querySelector('.sort-icon').textContent = sortDir === -1 ? '↓' : '↑';
        sortAndRender(marketTbody);
      });
    });

    // Search
    var searchInput = document.getElementById('marketSearch');
    if (searchInput) {
      searchInput.addEventListener('input', function () {
        var q = searchInput.value.toLowerCase();
        var filtered = marketRows.filter(function (a) {
          return a.code.toLowerCase().includes(q) || (a.name || '').toLowerCase().includes(q);
        });
        renderMarketTable(filtered, marketTbody);
      });
    }
  }

  /* ── Live Price Polling ─────────────────────────────────────────────── */
  function pollPrices() {
    fetch('/api/assets')
      .then(function (r) { return r.json(); })
      .then(function (data) {
        if (!Array.isArray(data)) return;
        // Update market rows data
        marketRows = data;
        // Update price cells in market table
        data.forEach(function (asset) {
          var cell = document.querySelector('[data-price="' + asset.code + '"]');
          if (cell) {
            var old = cell.textContent;
            var newPrice = formatPrice(asset.currentPriceUsd);
            if (old !== newPrice) {
              cell.textContent = newPrice;
              cell.style.transition = 'color 0.3s';
              cell.style.color = parseFloat(asset.priceChange24h) >= 0 ? 'var(--green)' : 'var(--red)';
              setTimeout(function () { cell.style.color = ''; }, 800);
            }
          }
          // Update ticker
          var tickerPrice = document.querySelector('.ticker-item[data-code="' + asset.code + '"] .ticker-price');
          if (tickerPrice) tickerPrice.textContent = formatPrice(asset.currentPriceUsd);
        });
        // Duplicate ticker items for seamless scroll
        var track = document.getElementById('tickerTrack');
        if (track && track.children.length <= data.length) {
          var clone = track.innerHTML;
          track.innerHTML = clone + clone;
        }
      })
      .catch(function () {});
  }
  // Initial poll + ticker duplication
  setTimeout(pollPrices, 500);
  setInterval(pollPrices, 5000);

  /* ── Ticker duplication for initial load ────────────────────────────── */
  window.addEventListener('load', function () {
    var track = document.getElementById('tickerTrack');
    if (track && track.children.length > 0) {
      track.innerHTML += track.innerHTML;
    }
  });

  /* ── Transaction filter tabs ────────────────────────────────────────── */
  document.querySelectorAll('.filter-tab[data-filter]').forEach(function (tab) {
    tab.addEventListener('click', function () {
      document.querySelectorAll('.filter-tab[data-filter]').forEach(function (t) {
        t.classList.remove('active');
      });
      tab.classList.add('active');
      var filter = tab.dataset.filter;
      var rows = document.querySelectorAll('.txn-row');
      rows.forEach(function (row) {
        row.style.display = (filter === 'ALL' || row.dataset.type === filter) ? '' : 'none';
      });
    });
  });

  /* ── Order form: BUY/SELL tab + cost calculator ─────────────────────── */
  var buyTab  = document.getElementById('buyTab');
  var sellTab = document.getElementById('sellTab');
  var orderTypeInput = document.getElementById('orderTypeInput');
  var submitBtn = document.getElementById('orderSubmitBtn');

  function setOrderType(type) {
    if (!orderTypeInput) return;
    orderTypeInput.value = type;
    if (buyTab && sellTab) {
      buyTab.classList.toggle('active', type === 'BUY');
      sellTab.classList.toggle('active', type === 'SELL');
    }
    if (submitBtn) {
      submitBtn.className = 'btn btn-full btn-lg ' + (type === 'BUY' ? 'btn-buy' : 'btn-sell');
      submitBtn.textContent = type === 'BUY' ? 'Buy Now' : 'Sell Now';
    }
  }
  if (buyTab)  buyTab.addEventListener('click', function () { setOrderType('BUY'); });
  if (sellTab) sellTab.addEventListener('click', function () { setOrderType('SELL'); });

  // Cost calculator
  var qtyInput   = document.getElementById('quantityInput');
  var priceEl    = document.getElementById('assetCurrentPrice');
  var totalEl    = document.getElementById('estimatedTotal');
  var feeEl      = document.getElementById('estimatedFee');
  function recalc() {
    if (!qtyInput || !priceEl || !totalEl) return;
    var qty   = parseFloat(qtyInput.value) || 0;
    var price = parseFloat(priceEl.dataset.price) || 0;
    var total = qty * price;
    var fee   = total * 0.001;
    totalEl.textContent = '$' + total.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    if (feeEl) feeEl.textContent = '$' + fee.toFixed(4);
  }
  if (qtyInput) qtyInput.addEventListener('input', recalc);

  // Asset chips
  document.querySelectorAll('.asset-chip[data-asset]').forEach(function (chip) {
    chip.addEventListener('click', function () {
      var code = chip.dataset.asset;
      var assetInput = document.getElementById('assetCodeInput');
      if (assetInput) assetInput.value = code;
      document.querySelectorAll('.asset-chip').forEach(function (c) { c.classList.remove('active'); });
      chip.classList.add('active');
      // Update price display
      fetch('/api/assets')
        .then(function (r) { return r.json(); })
        .then(function (data) {
          var asset = data.find(function (a) { return a.code === code; });
          if (asset && priceEl) {
            priceEl.textContent = formatPrice(asset.currentPriceUsd);
            priceEl.dataset.price = asset.currentPriceUsd;
            recalc();
          }
        });
    });
  });

  /* ── Password show/hide ─────────────────────────────────────────────── */
  document.querySelectorAll('.password-toggle').forEach(function (btn) {
    btn.addEventListener('click', function () {
      var input = btn.previousElementSibling;
      if (input && input.type === 'password') {
        input.type = 'text';
        btn.textContent = '🙈';
      } else if (input) {
        input.type = 'password';
        btn.textContent = '👁';
      }
    });
  });

  /* ── Password strength ─────────────────────────────────────────────── */
  var pwInput = document.getElementById('passwordInput');
  var pwStrength = document.getElementById('strengthFill');
  if (pwInput && pwStrength) {
    pwInput.addEventListener('input', function () {
      var v = pwInput.value;
      var score = 0;
      if (v.length >= 8) score++;
      if (/[A-Z]/.test(v)) score++;
      if (/[0-9]/.test(v)) score++;
      if (/[^A-Za-z0-9]/.test(v)) score++;
      var colors = ['#DC2626','#D97706','#D97706','#16A34A','#16A34A'];
      pwStrength.style.width = (score * 25) + '%';
      pwStrength.style.background = colors[score] || '#DC2626';
    });
  }

  /* ── Public API ─────────────────────────────────────────────────────── */
  window.CryptoForge = {
    toggleTheme: toggleTheme,
    drawDonut: window.drawDonut,
    drawSparkline: drawSparkline
  };
})();
