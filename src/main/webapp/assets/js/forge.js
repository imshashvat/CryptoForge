/**
 * CryptoForge — forge.js v3
 * Theme · Market table · Sparklines · Live prices · Donut · 7D Chart
 *
 * FIXES:
 * 1. Theme defaults 'dark' not 'light' — no white flash after login
 * 2. Profile avatar shows dropdown — NOT /logout onclick
 * 3. 7D chart column fetches real /api/market/candles/{code} with OHLC tooltip
 * 4. Price polling 10s not 5s
 * 5. Ticker duplication is idempotent
 */
(function () {
  'use strict';

  /* ── Theme ─────────────────────────────────────────────────────── */
  var THEME_KEY = 'cf-theme-v2';
  function applyTheme(t) {
    var theme = (t === 'light') ? 'light' : 'dark';
    document.documentElement.setAttribute('data-theme', theme);
    document.querySelectorAll('.icon-sun, #themeIconSun').forEach(function(s){
      s.style.display = (theme === 'dark') ? 'block' : 'none';
    });
    document.querySelectorAll('.icon-moon, #themeIconMoon').forEach(function(m){
      m.style.display = (theme === 'dark') ? 'none' : 'block';
    });
    try { localStorage.setItem(THEME_KEY, theme); } catch(e){}
  }
  function toggleTheme() {
    var cur = document.documentElement.getAttribute('data-theme') || 'dark';
    applyTheme(cur === 'dark' ? 'light' : 'dark');
  }
  // THEME INIT — purge legacy 'cf-theme' key which saved 'light' on initial visits,
  // and ensure dark mode is enforced everywhere unless explicitly chosen otherwise.
  (function() {
    try { localStorage.removeItem('cf-theme'); } catch(e){}
    var saved = null;
    try { saved = localStorage.getItem(THEME_KEY); } catch(e){}
    if (saved === 'light') {
      applyTheme('light');
    } else {
      applyTheme('dark');
    }
  })();

  /* ── Nav scroll ─────────────────────────────────────────────────── */
  var nav = document.getElementById('siteNav');
  if (nav) { window.addEventListener('scroll', function(){ nav.classList.toggle('scrolled', window.scrollY > 10); }, {passive:true}); }

  /* ── FIX 2: Profile Dropdown (was: onclick="/logout") ──────────── */
  (function initProfileDropdown() {
    var avatar = document.querySelector('.nav-avatar');
    if (!avatar) return;
    avatar.removeAttribute('onclick');
    avatar.title = 'Account menu';
    var navUsername = (document.querySelector('.nav-username')||{}).textContent||'';
    var navRole     = (document.querySelector('.nav-role')||{}).textContent||'';
    var dd = document.createElement('div');
    dd.id = 'profileDropdown';
    dd.style.cssText = 'position:absolute;top:calc(100% + 8px);right:0;min-width:188px;background:var(--bg-surface);border:1px solid var(--border);border-radius:var(--radius-lg);box-shadow:var(--shadow-lg);z-index:9999;padding:6px;display:none;';
    dd.innerHTML =
      '<div style="padding:10px 12px 8px;border-bottom:1px solid var(--border);margin-bottom:4px;">' +
        '<div style="font-weight:700;font-size:.88rem;color:var(--text-primary);">' + navUsername + '</div>' +
        '<div style="font-size:.72rem;color:var(--text-muted);margin-top:2px;">' + navRole + '</div>' +
      '</div>' +
      '<a href="/portfolio" class="dd-item" style="display:flex;align-items:center;gap:8px;padding:9px 12px;border-radius:var(--radius-md);color:var(--text-secondary);font-size:.85rem;font-weight:500;text-decoration:none;">' +
        '<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 16V8a2 2 0 00-1-1.73l-7-4a2 2 0 00-2 0l-7 4A2 2 0 003 8v8a2 2 0 001 1.73l7 4a2 2 0 002 0l7-4A2 2 0 0021 16z"/></svg>Portfolio</a>' +
      '<a href="/transactions" class="dd-item" style="display:flex;align-items:center;gap:8px;padding:9px 12px;border-radius:var(--radius-md);color:var(--text-secondary);font-size:.85rem;font-weight:500;text-decoration:none;">' +
        '<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="23 6 13.5 15.5 8.5 10.5 1 18"/><polyline points="17 6 23 6 23 12"/></svg>Transactions</a>' +
      '<div style="border-top:1px solid var(--border);margin:4px 0;"></div>' +
      '<a href="/logout" class="dd-item dd-logout" style="display:flex;align-items:center;gap:8px;padding:9px 12px;border-radius:var(--radius-md);color:var(--red);font-size:.85rem;font-weight:600;text-decoration:none;">' +
        '<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 21H5a2 2 0 01-2-2V5a2 2 0 012-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>Sign Out</a>';
    setTimeout(function(){
      dd.querySelectorAll('.dd-item').forEach(function(a){
        var isLogout = a.classList.contains('dd-logout');
        a.addEventListener('mouseenter', function(){ a.style.background = isLogout ? 'var(--red-light)' : 'var(--bg-surface-alt)'; if(!isLogout) a.style.color='var(--text-primary)'; });
        a.addEventListener('mouseleave', function(){ a.style.background = ''; a.style.color = isLogout ? 'var(--red)' : 'var(--text-secondary)'; });
      });
    }, 0);
    var navUser = avatar.closest('.nav-user') || avatar.parentElement;
    navUser.style.position = 'relative';
    navUser.appendChild(dd);
    var open = false;
    function openMenu(){ dd.style.display='block'; open=true; avatar.style.background='var(--accent)'; avatar.style.color='#0B0E11'; }
    function closeMenu(){ dd.style.display='none'; open=false; avatar.style.background=''; avatar.style.color=''; }
    avatar.addEventListener('click', function(e){ e.stopPropagation(); open ? closeMenu() : openMenu(); });
    document.addEventListener('click', function(e){ if(open && !dd.contains(e.target) && e.target!==avatar) closeMenu(); });
    document.addEventListener('keydown', function(e){ if(e.key==='Escape'&&open) closeMenu(); });
  })();

  /* ── Sparklines ─────────────────────────────────────────────────── */
  function drawSparkline(canvas, data, color) {
    if (!canvas || !data || data.length < 2) return;
    var w = canvas.width = canvas.offsetWidth || 80, h = canvas.height = canvas.offsetHeight || 32;
    var ctx = canvas.getContext('2d'); ctx.clearRect(0,0,w,h);
    var min=Math.min.apply(null,data), max=Math.max.apply(null,data), range=max-min||1, step=w/(data.length-1);
    ctx.beginPath();
    data.forEach(function(v,i){ var x=i*step,y=h-((v-min)/range)*(h-4)-2; i===0?ctx.moveTo(x,y):ctx.lineTo(x,y); });
    ctx.strokeStyle=color; ctx.lineWidth=1.5; ctx.lineJoin='round'; ctx.stroke();
    ctx.lineTo(w,h); ctx.lineTo(0,h); ctx.closePath();
    var g=ctx.createLinearGradient(0,0,0,h);
    g.addColorStop(0,color.replace(')' ,',0.18)').replace('rgb','rgba'));
    g.addColorStop(1,color.replace(')' ,',0)'  ).replace('rgb','rgba'));
    ctx.fillStyle=g; ctx.fill();
  }

  /* ── FIX 3: 7D Chart — 72-hour OHLC from /api/market/candles/{code} ─ */
  function draw7dChart(canvas, candles, isUp) {
    if (!canvas || !candles || candles.length < 2) return;
    var w=canvas.width=canvas.offsetWidth||88, h=canvas.height=canvas.offsetHeight||36;
    var ctx=canvas.getContext('2d'), color=isUp?'#00D68F':'#F6465D';
    var closes=candles.map(function(c){return c.close;}), min=Math.min.apply(null,closes), max=Math.max.apply(null,closes);
    var range=max-min||1, step=w/(closes.length-1);
    var gy=function(v){return h-((v-min)/range)*(h-4)-2;};
    ctx.clearRect(0,0,w,h);
    ctx.beginPath();
    closes.forEach(function(v,i){i===0?ctx.moveTo(i*step,gy(v)):ctx.lineTo(i*step,gy(v));});
    ctx.lineTo(w,h); ctx.lineTo(0,h); ctx.closePath();
    var g=ctx.createLinearGradient(0,0,0,h);
    g.addColorStop(0,isUp?'rgba(0,214,143,0.22)':'rgba(246,70,93,0.22)');
    g.addColorStop(1,isUp?'rgba(0,214,143,0)':'rgba(246,70,93,0)');
    ctx.fillStyle=g; ctx.fill();
    ctx.beginPath();
    closes.forEach(function(v,i){i===0?ctx.moveTo(i*step,gy(v)):ctx.lineTo(i*step,gy(v));});
    ctx.strokeStyle=color; ctx.lineWidth=1.5; ctx.lineJoin='round'; ctx.stroke();
    var lx=(closes.length-1)*step, ly=gy(closes[closes.length-1]);
    ctx.beginPath(); ctx.arc(lx,ly,2.5,0,Math.PI*2); ctx.fillStyle=color; ctx.fill();
  }
  var _tip=null;
  function getTooltip(){
    if(_tip) return _tip;
    _tip=document.createElement('div'); _tip.id='cf7dTooltip';
    _tip.style.cssText='position:fixed;z-index:99999;pointer-events:none;background:var(--bg-surface-2,#242B32);border:1px solid var(--border,#242B32);border-radius:8px;padding:7px 11px;font-size:11px;font-family:monospace;color:var(--text-primary,#fff);box-shadow:0 8px 24px rgba(0,0,0,.6);display:none;white-space:nowrap;line-height:1.7;';
    document.body.appendChild(_tip); return _tip;
  }
  function attach7dTooltip(canvas, candles) {
    if (!canvas || !candles || candles.length < 2) return;
    canvas.addEventListener('mousemove', function(e){
      var tip=getTooltip(), rect=canvas.getBoundingClientRect(), x=e.clientX-rect.left;
      var idx=Math.round((x/rect.width)*(candles.length-1)); idx=Math.max(0,Math.min(idx,candles.length-1));
      var c=candles[idx], d=new Date(c.time*1000);
      var dt=d.toLocaleDateString('en-US',{month:'short',day:'numeric'})+' '+d.toLocaleTimeString('en-US',{hour:'2-digit',minute:'2-digit',hour12:false});
      var fmt=function(n){return '$'+parseFloat(n).toLocaleString('en-US',{minimumFractionDigits:2,maximumFractionDigits:2});};
      var cl=c.close>=c.open?'color:var(--green,#00D68F)':'color:var(--red,#F6465D)';
      tip.innerHTML='<span style="color:#9AA5B1">'+dt+'</span><br>O: <b>'+fmt(c.open)+'</b>  H: <b>'+fmt(c.high)+'</b><br>L: <b>'+fmt(c.low)+'</b>  C: <b style="'+cl+'">'+fmt(c.close)+'</b>';
      tip.style.display='block'; tip.style.left=(e.clientX+14)+'px'; tip.style.top=(e.clientY-12)+'px';
    });
    canvas.addEventListener('mouseleave', function(){ getTooltip().style.display='none'; });
  }
  var candleCache={};
  function load7dChart(canvas, code, change) {
    if (!canvas) return;
    var isUp=parseFloat(change)>=0;
    if (candleCache[code]) { draw7dChart(canvas,candleCache[code],isUp); attach7dTooltip(canvas,candleCache[code]); return; }
    fetch('/api/market/candles/'+code)
      .then(function(r){ if(!r.ok) throw new Error('HTTP '+r.status); return r.json(); })
      .then(function(candles){
        if (!Array.isArray(candles)||candles.length===0) throw new Error('empty');
        candleCache[code]=candles; draw7dChart(canvas,candles,isUp); attach7dTooltip(canvas,candles);
      })
      .catch(function(){
        var price=parseFloat(canvas.dataset.priceVal)||100;
        var mock=generateMockSparkline(price,parseFloat(change)||0);
        drawSparkline(canvas,mock,isUp?'#00D68F':'#F6465D');
      });
  }

  /* ── Donut ──────────────────────────────────────────────────────── */
  window.drawDonut=function(canvasId,data){
    var canvas=document.getElementById(canvasId); if(!canvas) return;
    var ctx=canvas.getContext('2d'), w=canvas.width=canvas.height=180, cx=w/2, cy=w/2, r=72, inner=44;
    var colors=['#2563EB','#16A34A','#DC2626','#D97706','#7C3AED','#0891B2','#DB2777'];
    var total=data.reduce(function(s,d){return s+d.value;},0); if(!total) return;
    var angle=-Math.PI/2; ctx.clearRect(0,0,w,w);
    data.forEach(function(d,i){ var sl=(d.value/total)*2*Math.PI; ctx.beginPath(); ctx.moveTo(cx,cy); ctx.arc(cx,cy,r,angle,angle+sl); ctx.closePath(); ctx.fillStyle=colors[i%colors.length]; ctx.fill(); angle+=sl; });
    ctx.beginPath(); ctx.arc(cx,cy,inner,0,2*Math.PI);
    ctx.fillStyle=getComputedStyle(document.documentElement).getPropertyValue('--bg-surface').trim()||'#151A1F'; ctx.fill();
  };

  /* ── Market Table ───────────────────────────────────────────────── */
  var marketRows=[], sortCol='marketCap', sortDir=-1;
  function formatPrice(p){ if(p===null||p===undefined) return '—'; p=parseFloat(p); return p>=1?'$'+p.toLocaleString('en-US',{minimumFractionDigits:2,maximumFractionDigits:2}):'$'+p.toFixed(6); }
  function formatLarge(n){ if(!n) return '—'; n=parseFloat(n); if(n>=1e12) return '$'+(n/1e12).toFixed(2)+'T'; if(n>=1e9) return '$'+(n/1e9).toFixed(2)+'B'; if(n>=1e6) return '$'+(n/1e6).toFixed(2)+'M'; return '$'+n.toLocaleString('en-US'); }
  function changeClass(v){ return parseFloat(v)>=0?'change-up':'change-down'; }
  function formatChange(v){ var n=parseFloat(v); return (n>=0?'+':'')+n.toFixed(2)+'%'; }
  var COIN_ICONS={BTC:'₿',ETH:'Ξ',BNB:'🟡',SOL:'◎',DOGE:'Ð',XRP:'✕',ADA:'₳',AVAX:'🔺',DOT:'●',MATIC:'🔷',SHIB:'🐕',LTC:'Ł',LINK:'⬡',UNI:'🦄',ATOM:'⚛'};
  function renderMarketTable(rows,tbody){
    if(!tbody) return; tbody.innerHTML='';
    rows.forEach(function(asset,idx){
      var change=asset.priceChange24h||0, icon=COIN_ICONS[asset.code]||asset.code.charAt(0), tr=document.createElement('tr');
      tr.setAttribute('data-code',asset.code);
      tr.innerHTML=
        '<td class="text-muted" style="font-size:12px;width:40px;">'+(idx+1)+'</td>'+
        '<td><div class="coin-info"><div class="coin-icon">'+icon+'</div><div><div class="coin-name">'+(asset.name||asset.code)+'</div><div class="coin-symbol">'+asset.code+'</div></div></div></td>'+
        '<td class="price-mono" data-price="'+asset.code+'">'+formatPrice(asset.currentPriceUsd)+'</td>'+
        '<td><span class="change-pill '+changeClass(change)+'">'+formatChange(change)+'</span></td>'+
        '<td class="price-mono text-secondary">'+formatLarge(asset.marketCapUsd)+'</td>'+
        '<td style="width:96px;padding-right:16px;"><canvas style="width:88px;height:36px;display:block;cursor:pointer;" title="Click to open detailed 72-hour chart" data-spark="'+asset.code+'" data-change="'+change+'" data-price-val="'+(asset.currentPriceUsd||0)+'"></canvas></td>'+
        '<td><div class="flex gap-2"><a href="/order?asset='+asset.code+'" class="btn btn-sm btn-buy" style="width:60px;padding:5px 0;">Buy</a><a href="/order?asset='+asset.code+'&type=SELL" class="btn btn-sm btn-sell" style="width:60px;padding:5px 0;">Sell</a></div></td>';
      tbody.appendChild(tr);
      var sc=tr.querySelector('[data-spark]');
      if(sc){ (function(c,code,ch){ setTimeout(function(){load7dChart(c,code,ch);},60); })(sc,asset.code,change); }
    });
  }
  function generateMockSparkline(price,change){ var pts=20,data=[],cur=parseFloat(price)||100,trend=parseFloat(change)/100/pts; for(var i=0;i<pts;i++){cur=cur*(1+trend+(Math.random()-.5)*.008);data.push(cur);}return data; }
  function sortAndRender(tbody){ var s=marketRows.slice().sort(function(a,b){var av=a[sortCol]||0,bv=b[sortCol]||0;return(parseFloat(av)-parseFloat(bv))*sortDir;});renderMarketTable(s,tbody); }
  var marketTbody=document.getElementById('marketTbody');
  if(marketTbody){
    var dataRows=marketTbody.querySelectorAll('[data-asset]');
    dataRows.forEach(function(r){try{marketRows.push(JSON.parse(r.dataset.asset));}catch(e){}});
    if(marketRows.length>0) sortAndRender(marketTbody);
    document.querySelectorAll('.cf-table thead th[data-sort]').forEach(function(th){
      th.addEventListener('click',function(){
        var col=th.dataset.sort; if(col===sortCol){sortDir*=-1;}else{sortCol=col;sortDir=-1;}
        document.querySelectorAll('.cf-table thead th').forEach(function(h){h.classList.remove('sorted');}); th.classList.add('sorted');
        var si=th.querySelector('.sort-icon'); if(si) si.textContent=sortDir===-1?'↓':'↑';
        sortAndRender(marketTbody);
      });
    });
    var searchInput=document.getElementById('marketSearch');
    if(searchInput){ searchInput.addEventListener('input',function(){ var q=searchInput.value.toLowerCase(); renderMarketTable(marketRows.filter(function(a){return a.code.toLowerCase().indexOf(q)!==-1||(a.name||'').toLowerCase().indexOf(q)!==-1;}),marketTbody); }); }
  }

  /* ── Live Price Polling (10s, idempotent ticker) ───────────────── */
  var tickerDuplicated=false;
  function pollPrices(){
    fetch('/api/assets').then(function(r){return r.json();}).then(function(data){
      if(!Array.isArray(data)) return;
      marketRows=data;
      data.forEach(function(asset){
        var cell=document.querySelector('[data-price="'+asset.code+'"]');
        if(cell){var np=formatPrice(asset.currentPriceUsd);if(cell.textContent!==np){cell.textContent=np;cell.style.transition='color 0.3s';cell.style.color=parseFloat(asset.priceChange24h)>=0?'var(--green)':'var(--red)';setTimeout(function(){cell.style.color='';},800);}}
        var ti=document.querySelector('.ticker-item[data-code="'+asset.code+'"] .ticker-price');
        if(ti) ti.textContent=formatPrice(asset.currentPriceUsd);
      });
      if(!tickerDuplicated){var track=document.getElementById('tickerTrack');if(track&&track.children.length>0){track.innerHTML+=track.innerHTML;tickerDuplicated=true;}}
    }).catch(function(){});
  }
  setTimeout(pollPrices,800);
  setInterval(pollPrices,10000);
  window.addEventListener('load',function(){if(!tickerDuplicated){var track=document.getElementById('tickerTrack');if(track&&track.children.length>0){track.innerHTML+=track.innerHTML;tickerDuplicated=true;}}});

  /* ── Transaction filter tabs ────────────────────────────────────── */
  document.querySelectorAll('.filter-tab[data-filter]').forEach(function(tab){
    tab.addEventListener('click',function(){ document.querySelectorAll('.filter-tab[data-filter]').forEach(function(t){t.classList.remove('active');}); tab.classList.add('active'); var f=tab.dataset.filter; document.querySelectorAll('.txn-row').forEach(function(row){row.style.display=(f==='ALL'||row.dataset.type===f)?'':'none';}); });
  });

  /* ── Order form ─────────────────────────────────────────────────── */
  var buyTab=document.getElementById('buyTab'), sellTab=document.getElementById('sellTab');
  var orderTypeInput=document.getElementById('orderTypeInput'), submitBtn=document.getElementById('orderSubmitBtn');
  function setOrderType(type){
    if(!orderTypeInput) return; orderTypeInput.value=type;
    if(buyTab&&sellTab){buyTab.classList.toggle('active',type==='BUY');sellTab.classList.toggle('active',type==='SELL');}
    if(submitBtn){submitBtn.className='btn btn-full btn-lg '+(type==='BUY'?'btn-buy':'btn-sell');submitBtn.textContent=type==='BUY'?'Buy Now':'Sell Now';}
  }
  if(buyTab)  buyTab.addEventListener('click',  function(){setOrderType('BUY');});
  if(sellTab) sellTab.addEventListener('click', function(){setOrderType('SELL');});
  var qtyInput=document.getElementById('quantityInput'), priceEl=document.getElementById('assetCurrentPrice');
  var totalEl=document.getElementById('estimatedTotal'), feeEl=document.getElementById('estimatedFee');
  function recalc(){
    if(!qtyInput||!priceEl||!totalEl) return;
    var qty=parseFloat(qtyInput.value)||0, price=parseFloat(priceEl.dataset.price)||0, total=qty*price, fee=total*.001;
    totalEl.textContent='$'+total.toLocaleString('en-US',{minimumFractionDigits:2,maximumFractionDigits:2});
    if(feeEl) feeEl.textContent='$'+fee.toFixed(4);
  }
  if(qtyInput) qtyInput.addEventListener('input',recalc);
  document.querySelectorAll('.asset-chip[data-asset]').forEach(function(chip){
    chip.addEventListener('click',function(){
      var code=chip.dataset.asset, assetInput=document.getElementById('assetCodeInput');
      if(assetInput) assetInput.value=code;
      document.querySelectorAll('.asset-chip').forEach(function(c){c.classList.remove('active');}); chip.classList.add('active');
      fetch('/api/assets/'+code).then(function(r){return r.json();}).then(function(asset){if(priceEl){priceEl.textContent=formatPrice(asset.currentPriceUsd);priceEl.dataset.price=asset.currentPriceUsd;recalc();}}).catch(function(){});
    });
  });

  /* ── Password ───────────────────────────────────────────────────── */
  document.querySelectorAll('.password-toggle').forEach(function(btn){
    btn.addEventListener('click',function(){ var inp=btn.previousElementSibling; if(!inp) return; inp.type=inp.type==='password'?'text':'password'; btn.textContent=inp.type==='password'?'👁':'🙈'; });
  });
  var pwInput=document.getElementById('passwordInput'), pwStrength=document.getElementById('strengthFill');
  if(pwInput&&pwStrength){ pwInput.addEventListener('input',function(){ var v=pwInput.value,s=0; if(v.length>=8)s++;if(/[A-Z]/.test(v))s++;if(/[0-9]/.test(v))s++;if(/[^A-Za-z0-9]/.test(v))s++; var colors=['#DC2626','#D97706','#D97706','#16A34A','#16A34A']; pwStrength.style.width=(s*25)+'%'; pwStrength.style.background=colors[s]||'#DC2626'; }); }


  /* -- 7D Chart Modal -- opens when user clicks any spark canvas in the market table */
  (function initChartModal() {
    var overlay = document.createElement('div');
    overlay.id = 'chartModal';
    overlay.style.cssText = 'display:none;position:fixed;inset:0;z-index:99990;background:rgba(0,0,0,.72);backdrop-filter:blur(6px);-webkit-backdrop-filter:blur(6px);align-items:center;justify-content:center;';
    var box = document.createElement('div');
    box.style.cssText = 'background:var(--bg-surface);border:1px solid var(--border);border-radius:var(--radius-lg);padding:24px 28px 20px;width:min(680px,95vw);box-shadow:0 24px 80px rgba(0,0,0,.6);position:relative;';
    box.innerHTML =
      '<button id="chartModalClose" style="position:absolute;top:14px;right:16px;background:var(--bg-surface-alt);border:1px solid var(--border);border-radius:50%;width:30px;height:30px;cursor:pointer;font-size:16px;color:var(--text-secondary);display:flex;align-items:center;justify-content:center;">&times;</button>' +
      '<div style="display:flex;align-items:flex-start;gap:14px;margin-bottom:18px;">' +
        '<div id="cmIcon" style="width:40px;height:40px;border-radius:50%;background:var(--accent-muted);border:1px solid rgba(0,214,143,.25);display:flex;align-items:center;justify-content:center;font-size:1.1rem;font-weight:800;flex-shrink:0;color:var(--accent);"></div>' +
        '<div>' +
          '<div style="display:flex;align-items:center;gap:10px;flex-wrap:wrap;">' +
            '<span id="cmName" style="font-weight:800;font-size:1.1rem;color:var(--text-primary);"></span>' +
            '<span id="cmCode" style="font-size:.78rem;color:var(--text-muted);background:var(--bg-surface-alt);padding:3px 8px;border-radius:20px;"></span>' +
            '<span id="cmChange" style="font-size:.82rem;font-weight:700;padding:3px 9px;border-radius:20px;"></span>' +
          '</div>' +
          '<div id="cmPrice" style="font-family:var(--font-display,inherit);font-size:1.7rem;font-weight:900;color:var(--text-primary);letter-spacing:-.03em;margin-top:3px;"></div>' +
        '</div>' +
      '</div>' +
      '<div style="position:relative;">' +
        '<canvas id="cmCanvas" style="width:100%;height:200px;display:block;border-radius:8px;background:var(--bg-page,#0B0E11);cursor:crosshair;"></canvas>' +
        '<div id="cmCrosshair" style="display:none;position:absolute;top:0;width:1px;height:100%;background:rgba(255,255,255,.18);pointer-events:none;"></div>' +
        '<div id="cmOhlc" style="position:absolute;top:8px;left:8px;font-size:11px;font-family:monospace;color:var(--text-secondary);pointer-events:none;background:rgba(0,0,0,.5);padding:3px 7px;border-radius:4px;"></div>' +
      '</div>' +
      '<div style="display:flex;align-items:center;justify-content:space-between;margin-top:16px;">' +
        '<div style="font-size:.75rem;color:var(--text-muted);">72-hour OHLC &bull; Hover to inspect candles</div>' +
        '<div style="display:flex;gap:8px;">' +
          '<a id="cmBuyBtn"  href="#" class="btn btn-sm btn-buy"  style="width:84px;text-align:center;display:inline-flex;align-items:center;justify-content:center;">Buy</a>' +
          '<a id="cmSellBtn" href="#" class="btn btn-sm btn-sell" style="width:84px;text-align:center;display:inline-flex;align-items:center;justify-content:center;">Sell</a>' +
        '</div>' +
      '</div>';
    overlay.appendChild(box);
    document.body.appendChild(overlay);

    var COIN_LABELS = {BTC:'B',ETH:'E',BNB:'B',SOL:'S',DOGE:'D',XRP:'X',ADA:'A',AVAX:'A',DOT:'D',MATIC:'M',SHIB:'S',LTC:'L',LINK:'L',UNI:'U',ATOM:'A'};
    var COIN_COLORS = {BTC:'#F7931A',ETH:'#627EEA',BNB:'#F0B90B',SOL:'#9945FF',DOGE:'#C2A633',XRP:'#00AAE4',ADA:'#0033AD',AVAX:'#E84142',DOT:'#E6007A',MATIC:'#8247E5',SHIB:'#FFA409',LTC:'#BFBBBB',LINK:'#375BD2',UNI:'#FF007A',ATOM:'#2E3148'};

    function drawModalChart(candles, isUp) {
      var canvas = document.getElementById('cmCanvas');
      if (!canvas || !candles || candles.length < 2) return;
      canvas.width  = canvas.offsetWidth  || 620;
      canvas.height = canvas.offsetHeight || 200;
      var w = canvas.width, h = canvas.height, ctx = canvas.getContext('2d');
      var color = isUp ? '#00D68F' : '#F6465D';
      var closes = candles.map(function(c){return c.close;});
      var min = Math.min.apply(null,closes), max = Math.max.apply(null,closes);
      var range = max-min||1, step = w/(closes.length-1);
      var gy = function(v){return h-((v-min)/range)*(h-24)-12;};
      ctx.clearRect(0,0,w,h);
      // Grid
      ctx.strokeStyle='rgba(255,255,255,.04)'; ctx.lineWidth=1;
      [0.25,0.5,0.75].forEach(function(r){ctx.beginPath();ctx.moveTo(0,h*r);ctx.lineTo(w,h*r);ctx.stroke();});
      // Area
      ctx.beginPath();
      closes.forEach(function(v,i){i===0?ctx.moveTo(i*step,gy(v)):ctx.lineTo(i*step,gy(v));});
      ctx.lineTo(w,h);ctx.lineTo(0,h);ctx.closePath();
      var g=ctx.createLinearGradient(0,0,0,h);
      g.addColorStop(0,isUp?'rgba(0,214,143,0.2)':'rgba(246,70,93,0.2)');
      g.addColorStop(1,isUp?'rgba(0,214,143,0)':'rgba(246,70,93,0)');
      ctx.fillStyle=g;ctx.fill();
      // Line
      ctx.beginPath();
      closes.forEach(function(v,i){i===0?ctx.moveTo(i*step,gy(v)):ctx.lineTo(i*step,gy(v));});
      ctx.strokeStyle=color;ctx.lineWidth=2;ctx.lineJoin='round';ctx.stroke();
      // Price labels
      ctx.fillStyle='rgba(155,165,180,.65)';ctx.font='10px monospace';ctx.textAlign='left';
      var fmt=function(n){return '$'+parseFloat(n).toLocaleString('en-US',{minimumFractionDigits:2,maximumFractionDigits:2});};
      [[min,'bottom'],[max,'top'],[(min+max)/2,'mid']].forEach(function(pair){
        ctx.fillText(fmt(pair[0]),4,gy(pair[0])+(pair[1]==='bottom'?-3:13));
      });
      // Store for crosshair
      canvas._candles=candles; canvas._step=step; canvas._gy=gy;
    }

    function openModal(code, name, price, change, candles) {
      var isUp=parseFloat(change)>=0;
      var color=isUp?'#00D68F':'#F6465D';
      var bgc=COIN_COLORS[code]||'var(--accent-muted)';
      var icon=document.getElementById('cmIcon');
      icon.textContent=COIN_LABELS[code]||code.charAt(0);
      icon.style.background=bgc; icon.style.color='#fff';
      document.getElementById('cmName').textContent=name||code;
      document.getElementById('cmCode').textContent=code;
      var fmt2=function(n){return '$'+parseFloat(n).toLocaleString('en-US',{minimumFractionDigits:2,maximumFractionDigits:2});};
      document.getElementById('cmPrice').textContent=fmt2(price);
      var chgEl=document.getElementById('cmChange');
      chgEl.textContent=(parseFloat(change)>=0?'+':'')+parseFloat(change).toFixed(2)+'%';
      chgEl.style.color=color; chgEl.style.background=isUp?'rgba(0,214,143,.12)':'rgba(246,70,93,.12)';
      document.getElementById('cmBuyBtn').href='/order?asset='+code;
      document.getElementById('cmSellBtn').href='/order?asset='+code+'&type=SELL';
      overlay.style.display='flex';
      document.body.style.overflow='hidden';
      setTimeout(function(){drawModalChart(candles,isUp);},30);
    }

    function closeModal(){overlay.style.display='none';document.body.style.overflow='';}

    document.getElementById('chartModalClose').addEventListener('click',closeModal);
    overlay.addEventListener('click',function(e){if(e.target===overlay)closeModal();});
    document.addEventListener('keydown',function(e){if(e.key==='Escape')closeModal();});

    // Crosshair on modal canvas
    document.addEventListener('mousemove',function(e){
      var canvas=document.getElementById('cmCanvas');
      if(!canvas||!canvas._candles||overlay.style.display==='none') return;
      var rect=canvas.getBoundingClientRect();
      if(e.clientX<rect.left||e.clientX>rect.right||e.clientY<rect.top||e.clientY>rect.bottom) return;
      var x=e.clientX-rect.left, idx=Math.round((x/rect.width)*(canvas._candles.length-1));
      idx=Math.max(0,Math.min(idx,canvas._candles.length-1));
      var c=canvas._candles[idx];
      var d=new Date(c.time*1000);
      var dt=d.toLocaleDateString('en-US',{month:'short',day:'numeric'})+' '+d.toLocaleTimeString('en-US',{hour:'2-digit',minute:'2-digit',hour12:false});
      var fmt=function(n){return '$'+parseFloat(n).toLocaleString('en-US',{minimumFractionDigits:2,maximumFractionDigits:2});};
      var cl=c.close>=c.open?'#00D68F':'#F6465D';
      var ohlcEl=document.getElementById('cmOhlc');
      if(ohlcEl){ohlcEl.innerHTML='<span style="color:#9AA5B1">'+dt+'</span> &nbsp; O:&nbsp;<b>'+fmt(c.open)+'</b> &nbsp;H:&nbsp;<b>'+fmt(c.high)+'</b> &nbsp;L:&nbsp;<b>'+fmt(c.low)+'</b> &nbsp;C:&nbsp;<b style="color:'+cl+'">'+fmt(c.close)+'</b>';}
      var crossEl=document.getElementById('cmCrosshair');
      if(crossEl){crossEl.style.display='block';crossEl.style.left=(idx*canvas._step)+'px';}
    });

    // Click delegation — any [data-spark] canvas opens the modal
    document.addEventListener('click',function(e){
      var canvas=null;
      if(e.target && e.target.tagName==='CANVAS' && e.target.dataset && e.target.dataset.spark) {
        canvas=e.target;
      } else if(e.target && e.target.closest) {
        canvas=e.target.closest('canvas[data-spark]');
      }
      if(!canvas||canvas.id==='cmCanvas') return;
      e.stopPropagation();
      var code=canvas.dataset.spark, change=canvas.dataset.change||'0', price=canvas.dataset.priceVal||'0';
      var row=canvas.closest('tr'), nameEl=row?row.querySelector('.coin-name'):null, name=nameEl?nameEl.textContent:code;
      if(candleCache[code]){
        openModal(code,name,price,change,candleCache[code]);
      } else {
        fetch('/api/market/candles/'+code)
          .then(function(r){return r.json();})
          .then(function(candles){candleCache[code]=candles;openModal(code,name,price,change,candles);})
          .catch(function(){});
      }
    });
  })();
  /* ── Public API ─────────────────────────────────────────────────── */
  window.CryptoForge={toggleTheme:toggleTheme,drawDonut:window.drawDonut,drawSparkline:drawSparkline,draw7dChart:draw7dChart,load7dChart:load7dChart};
})();


