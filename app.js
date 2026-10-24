(() => {
  const ICONS = [
    {id:'ducha',label:'Ducha',paths:['M12 3v3','M5 9h14l-2.5 3.5h-9z','M8.5 16v2','M12 17v2.5','M15.5 16v2']},
    {id:'banera',label:'Bañera',paths:['M3 11h18v3a4 4 0 0 1-4 4H7a4 4 0 0 1-4-4z','M7 11V6.5a2.5 2.5 0 0 1 5 0','M6 18.5V21','M18 18.5V21']},
    {id:'jabon',label:'Jabón',paths:['M4 13a4 4 0 0 1 4-4h8a4 4 0 0 1 4 4v2a3 3 0 0 1-3 3H7a3 3 0 0 1-3-3z','M8 6.5a1.5 1.5 0 1 0 0 3a1.5 1.5 0 1 0 0-3','M13 4a1 1 0 1 0 0 2a1 1 0 1 0 0-2']},
    {id:'champu',label:'Champú',paths:['M9 8.5h6v10.5a2 2 0 0 1-2 2h-2a2 2 0 0 1-2-2z','M10.5 3.5h3v5h-3z','M9 13h6']},
    {id:'esponja',label:'Esponja',paths:['M4 9a4 4 0 0 1 4-4h8a4 4 0 0 1 4 4v6a4 4 0 0 1-4 4H8a4 4 0 0 1-4-4z','M8.5 10a1 1 0 1 0 0 2a1 1 0 1 0 0-2','M13 13a1 1 0 1 0 0 2a1 1 0 1 0 0-2','M15.5 9.5a1 1 0 1 0 0 2a1 1 0 1 0 0-2']},
    {id:'burbujas',label:'Burbujas',paths:['M8.5 13a4.5 4.5 0 1 0 0 9a4.5 4.5 0 1 0 0-9','M16.5 9a3 3 0 1 0 0 6a3 3 0 1 0 0-6','M12 3.5a2 2 0 1 0 0 4a2 2 0 1 0 0-4']},
    {id:'gota',label:'Agua',paths:['M12 3.5c3.5 4.2 5.5 7 5.5 9.8A5.5 5.5 0 0 1 6.5 13.3C6.5 10.5 8.5 7.7 12 3.5z']},
    {id:'toalla',label:'Toalla',paths:['M6 3.5h12v14.5a3 3 0 0 1-3 3H9a3 3 0 0 1-3-3z','M6 8h12','M14.5 12.5h2']},
    {id:'cepillo',label:'Cepillo dental',paths:['M8.5 3h3.5v5.5H8.5z','M10.25 8.5V21','M8.5 4.6h3.5']},
    {id:'afeitar',label:'Afeitar',paths:['M6 4h12v4.5H6z','M11 8.5h2V21h-2z','M8 6h8']},
    {id:'peine',label:'Peine',paths:['M4 8.5h16v3.5H4z','M6.5 12v4.5','M10 12v4.5','M13.5 12v4.5','M17 12v4.5']},
    {id:'crema',label:'Crema',paths:['M8 9h8v9a3 3 0 0 1-3 3h-2a3 3 0 0 1-3-3z','M9.5 5h5v4h-5z','M12 5V3']},
    {id:'espejo',label:'Espejo',paths:['M12 2.5c3 0 5 3.4 5 7s-2 7-5 7-5-3.4-5-7 2-7 5-7z','M12 16.5V21','M9 21h6']},
    {id:'patito',label:'Patito',paths:['M4.5 14.5c0 3.5 3.4 5 7.5 5s7.5-1.5 7.5-5z','M14.5 6.5a3.2 3.2 0 1 0 0 6.4a3.2 3.2 0 1 0 0-6.4','M18 9.2h3l-2.6 2','M13.4 8.6a.5.5 0 1 0 0 1a.5.5 0 1 0 0-1']},
    {id:'reloj',label:'Tiempo',paths:['M12 4a8 8 0 1 0 0 16a8 8 0 1 0 0-16','M12 8v4.5l3 2']}
  ];

  const KEY = 'ritual-ducha-v1';
  const C = 540.35;
  const ACCENT = '#2e9aa8';

  const DEFAULTS = [
    {id:'a1', name:'Mojarse el cuerpo', seconds:30, icon:'ducha'},
    {id:'a2', name:'Champú en el cabello', seconds:60, icon:'champu'},
    {id:'a3', name:'Enjuagar el cabello', seconds:45, icon:'gota'},
    {id:'a4', name:'Enjabonar el cuerpo', seconds:90, icon:'jabon'},
    {id:'a5', name:'Enjuague final', seconds:45, icon:'burbujas'},
    {id:'a6', name:'Secarse con la toalla', seconds:60, icon:'toalla'}
  ];

  function pathsFor(iconId) {
    const ic = ICONS.find(x => x.id === iconId) || ICONS[0];
    return ic.paths;
  }
  function svgIcon(paths, size) {
    return `<svg viewBox="0 0 24 24" width="${size}" height="${size}" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round">${paths.map(d => `<path d="${d}"></path>`).join('')}</svg>`;
  }
  function mmss(sec) {
    const s = Math.max(0, Math.ceil(sec));
    return Math.floor(s / 60) + ':' + String(s % 60).padStart(2, '0');
  }
  function spokenTime(sec) {
    const s = Math.max(0, Math.round(sec));
    const m = Math.floor(s / 60), r = s % 60;
    const pm = m === 1 ? '1 minuto' : m + ' minutos';
    const ps = r === 1 ? '1 segundo' : r + ' segundos';
    if (m && r) return pm + ' y ' + ps;
    if (m) return pm;
    return ps;
  }

  const state = {
    name: 'Mi ritual de ducha',
    items: DEFAULTS.map(x => ({...x})),
    started: false,
    playing: false,
    idx: 0,
    remainMs: 0,
    pickerFor: null,
    voice: true,
    finished: false
  };

  try {
    const raw = localStorage.getItem(KEY);
    if (raw) {
      const d = JSON.parse(raw);
      if (d && Array.isArray(d.items) && d.items.length) {
        state.name = d.name || state.name;
        state.items = d.items;
      }
    }
  } catch (e) {}

  function save() {
    try { localStorage.setItem(KEY, JSON.stringify({name: state.name, items: state.items})); } catch (e) {}
  }

  let endAt = 0, pausedAt = 0, flags = {half: false, five: false};

  // Plain Android WebView doesn't implement the Web Speech API (speechSynthesis
  // silently no-ops there) — the native app injects window.AndroidTTS, backed by
  // Android's real TextToSpeech engine, as a bridge. Fall back to speechSynthesis
  // for testing in a desktop/mobile browser where AndroidTTS isn't present.
  const hasAndroidTTS = !!(window.AndroidTTS && window.AndroidTTS.speak);

  let voicesReady = hasAndroidTTS;
  if (!hasAndroidTTS && window.speechSynthesis) {
    const markReady = () => { voicesReady = window.speechSynthesis.getVoices().length > 0; };
    markReady();
    window.speechSynthesis.onvoiceschanged = markReady;
  }

  function speakNow(text) {
    if (!state.voice) return;
    if (hasAndroidTTS) {
      window.AndroidTTS.speak(text);
      return;
    }
    if (!window.speechSynthesis) return;
    const u = new SpeechSynthesisUtterance(text);
    u.lang = 'es-ES';
    window.speechSynthesis.speak(u);
  }

  let lastCancelAt = 0;
  function cancelSpeech() {
    if (hasAndroidTTS) {
      window.AndroidTTS.stop();
      lastCancelAt = Date.now();
      return;
    }
    if (!window.speechSynthesis) return;
    window.speechSynthesis.cancel();
    lastCancelAt = Date.now();
  }

  function speak(text) {
    if (!state.voice) return;
    if (hasAndroidTTS) {
      // Android's TTS.speak with QUEUE_FLUSH already interrupts cleanly.
      speakNow(text);
      return;
    }
    if (!window.speechSynthesis) return;
    // Browser speechSynthesis silently drops an utterance queued right after
    // cancel(), and drops it too if voices haven't finished loading yet.
    const justCancelled = Date.now() - lastCancelAt < 200;
    if (justCancelled || window.speechSynthesis.speaking || window.speechSynthesis.pending || !voicesReady) {
      setTimeout(() => speakNow(text), 150);
    } else {
      speakNow(text);
    }
  }

  function startAction(i, announce) {
    const it = state.items[i];
    if (!it) return finish();
    flags = {half: false, five: false};
    endAt = Date.now() + it.seconds * 1000;
    state.idx = i;
    state.started = true;
    state.playing = true;
    state.finished = false;
    state.remainMs = it.seconds * 1000;
    state.pickerFor = null;
    if (announce !== false) speak(it.name + '. ' + spokenTime(it.seconds) + '.');
    render();
  }
  function finish() {
    state.playing = false;
    state.finished = true;
    state.remainMs = 0;
    speak('Rutina completada. ¡Listo!');
    render();
  }
  function tick() {
    if (!state.playing) return;
    const it = state.items[state.idx];
    if (!it) return finish();
    const remain = endAt - Date.now();
    const dur = it.seconds * 1000;
    if (remain <= 0) {
      const n = state.idx + 1;
      if (n < state.items.length) startAction(n);
      else finish();
      return;
    }
    if (!flags.half && remain <= dur / 2 && dur >= 20000) {
      flags.half = true;
      speak('Mitad del tiempo. Quedan ' + spokenTime(remain / 1000) + '.');
    }
    if (!flags.five && remain <= dur * 0.05) {
      flags.five = true;
      speak('Quedan ' + spokenTime(remain / 1000) + '.');
    }
    state.remainMs = remain;
    renderPlayerTime();
  }

  function onPlay() {
    if (window.speechSynthesis && window.speechSynthesis.paused) window.speechSynthesis.resume();
    if (!state.started || state.finished) {
      if (!state.items.length) return;
      startAction(0);
    } else if (state.playing) {
      pausedAt = state.remainMs;
      state.playing = false;
      if (window.speechSynthesis) window.speechSynthesis.pause();
      render();
    } else {
      endAt = Date.now() + (pausedAt || state.remainMs);
      state.playing = true;
      render();
    }
  }
  function onSkip() {
    if (!state.started) return;
    const n = state.idx + 1;
    cancelSpeech();
    if (n < state.items.length) startAction(n);
    else finish();
  }
  function onReset() {
    cancelSpeech();
    state.started = false;
    state.playing = false;
    state.finished = false;
    state.idx = 0;
    state.remainMs = 0;
    render();
  }
  function onClose() { onReset(); }

  function patch(id, fields) {
    state.items = state.items.map(x => x.id === id ? Object.assign({}, x, fields) : x);
    save();
    render();
  }
  function move(id, dir) {
    const items = state.items.slice();
    const i = items.findIndex(x => x.id === id);
    const j = i + dir;
    if (i < 0 || j < 0 || j >= items.length) return;
    [items[i], items[j]] = [items[j], items[i]];
    state.items = items;
    save();
    render();
  }

  // --- DOM refs ---
  const el = {
    routineName: document.getElementById('routineName'),
    totalText: document.getElementById('totalText'),
    countText: document.getElementById('countText'),
    stepText: document.getElementById('stepText'),
    list: document.getElementById('list'),
    playBtn: document.getElementById('playBtn'),
    playBtn2: document.getElementById('playBtn2'),
    resetBtn: document.getElementById('resetBtn'),
    resetBtn2: document.getElementById('resetBtn2'),
    skipBtn: document.getElementById('skipBtn'),
    skipBtn2: document.getElementById('skipBtn2'),
    player: document.getElementById('player'),
    closeBtn: document.getElementById('closeBtn'),
    statusText: document.getElementById('statusText'),
    ring: document.getElementById('ring'),
    curIcon: document.getElementById('curIcon'),
    timeText: document.getElementById('timeText'),
    curName: document.getElementById('curName'),
    nextText: document.getElementById('nextText'),
    dots: document.getElementById('dots'),
    voiceToggle: document.getElementById('voiceToggle'),
    voiceToggle2: document.getElementById('voiceToggle2'),
  };

  el.routineName.value = state.name;
  el.routineName.addEventListener('input', e => { state.name = e.target.value; save(); });

  function toggleVoice() {
    if (state.voice) cancelSpeech();
    state.voice = !state.voice;
    render();
  }
  el.voiceToggle.addEventListener('click', toggleVoice);
  el.voiceToggle2.addEventListener('click', toggleVoice);

  el.playBtn.addEventListener('click', onPlay);
  el.playBtn2.addEventListener('click', onPlay);
  el.resetBtn.addEventListener('click', onReset);
  el.resetBtn2.addEventListener('click', onReset);
  el.skipBtn.addEventListener('click', onSkip);
  el.skipBtn2.addEventListener('click', onSkip);
  el.closeBtn.addEventListener('click', onClose);

  function renderList() {
    el.list.innerHTML = '';
    state.items.forEach(it => {
      const card = document.createElement('div');
      card.className = 'card';

      const row = document.createElement('div');
      row.className = 'card-row';

      const iconBtn = document.createElement('button');
      iconBtn.className = 'card-icon-btn' + (state.pickerFor === it.id ? ' open' : '');
      iconBtn.title = 'Cambiar icono';
      iconBtn.innerHTML = svgIcon(pathsFor(it.icon), 27);
      iconBtn.addEventListener('click', () => {
        state.pickerFor = state.pickerFor === it.id ? null : it.id;
        render();
      });

      const fields = document.createElement('div');
      fields.className = 'card-fields';
      const nameInput = document.createElement('input');
      nameInput.className = 'action-name';
      nameInput.value = it.name;
      nameInput.placeholder = 'Nombre de la acción';
      nameInput.addEventListener('input', e => {
        const item = state.items.find(x => x.id === it.id);
        if (item) item.name = e.target.value;
        save();
      });

      const timeRow = document.createElement('div');
      timeRow.className = 'time-row';
      const minInput = document.createElement('input');
      minInput.type = 'number'; minInput.min = 0; minInput.max = 59; minInput.inputMode = 'numeric';
      minInput.className = 'num-input';
      minInput.value = Math.floor(it.seconds / 60);
      minInput.addEventListener('change', e => {
        const m = Math.max(0, Math.min(59, parseInt(e.target.value, 10) || 0));
        patch(it.id, {seconds: Math.max(5, m * 60 + (it.seconds % 60))});
      });
      const minUnit = document.createElement('span'); minUnit.className = 'unit'; minUnit.textContent = 'min';
      const secInput = document.createElement('input');
      secInput.type = 'number'; secInput.min = 0; secInput.max = 59; secInput.inputMode = 'numeric';
      secInput.className = 'num-input'; secInput.style.marginLeft = '4px';
      secInput.value = it.seconds % 60;
      secInput.addEventListener('change', e => {
        const sc = Math.max(0, Math.min(59, parseInt(e.target.value, 10) || 0));
        patch(it.id, {seconds: Math.max(5, Math.floor(it.seconds / 60) * 60 + sc)});
      });
      const secUnit = document.createElement('span'); secUnit.className = 'unit'; secUnit.textContent = 'seg';
      timeRow.append(minInput, minUnit, secInput, secUnit);
      fields.append(nameInput, timeRow);

      const reorder = document.createElement('div');
      reorder.className = 'reorder';
      const upBtn = document.createElement('button'); upBtn.textContent = '▲'; upBtn.title = 'Subir';
      upBtn.addEventListener('click', () => move(it.id, -1));
      const downBtn = document.createElement('button'); downBtn.textContent = '▼'; downBtn.title = 'Bajar';
      downBtn.addEventListener('click', () => move(it.id, 1));
      reorder.append(upBtn, downBtn);

      const delBtn = document.createElement('button');
      delBtn.className = 'del-btn'; delBtn.title = 'Eliminar'; delBtn.textContent = '✕';
      delBtn.addEventListener('click', () => {
        state.items = state.items.filter(x => x.id !== it.id);
        save(); render();
      });

      row.append(iconBtn, fields, reorder, delBtn);
      card.append(row);

      if (state.pickerFor === it.id) {
        const picker = document.createElement('div');
        picker.className = 'picker';
        ICONS.forEach(ic => {
          const b = document.createElement('button');
          b.title = ic.label;
          b.className = ic.id === it.icon ? 'selected' : '';
          b.innerHTML = svgIcon(ic.paths, 24);
          b.addEventListener('click', () => {
            patch(it.id, {icon: ic.id});
            state.pickerFor = null;
            render();
          });
          picker.append(b);
        });
        card.append(picker);
      }

      el.list.append(card);
    });

    const addBtn = document.createElement('button');
    addBtn.className = 'add-btn';
    addBtn.textContent = '+ Añadir acción';
    addBtn.addEventListener('click', () => {
      state.items.push({id: 'a' + Date.now(), name: 'Nueva acción', seconds: 45, icon: 'burbujas'});
      save(); render();
    });
    el.list.append(addBtn);
  }

  function renderPlayerTime() {
    const cur = state.started && !state.finished ? state.items[state.idx] : null;
    const dur = cur ? cur.seconds * 1000 : 0;
    const frac = cur && dur ? Math.max(0, Math.min(1, state.remainMs / dur)) : (state.finished ? 0 : 1);
    el.ring.setAttribute('stroke', state.playing ? ACCENT : '#9dc0c8');
    el.ring.setAttribute('stroke-dashoffset', String(C * (1 - frac)));
    el.timeText.textContent = cur ? mmss(state.remainMs / 1000) : (state.finished ? '0:00' : mmss(totalSeconds()));
    el.statusText.textContent = state.finished ? 'Completada' : (state.playing ? 'En marcha' : 'En pausa');
  }

  function totalSeconds() {
    return state.items.reduce((a, x) => a + (x.seconds || 0), 0);
  }

  function renderPlayer() {
    el.player.hidden = !state.started;
    if (!state.started) return;
    const cur = !state.finished ? state.items[state.idx] : null;
    const nxt = cur ? state.items[state.idx + 1] : state.items[0];

    el.curIcon.innerHTML = svgIcon(pathsFor(cur ? cur.icon : 'patito'), 38);
    el.curName.textContent = cur ? cur.name : '¡Listo, a brillar!';
    el.nextText.textContent = cur ? (nxt ? 'Siguiente: ' + nxt.name : 'Último paso') : 'Rutina terminada';

    el.dots.innerHTML = '';
    state.items.forEach((x, i) => {
      const d = document.createElement('div');
      d.className = 'dot';
      d.title = x.name;
      if (state.started && !state.finished && i === state.idx) d.classList.add('active');
      else if ((state.started && i < state.idx) || state.finished) d.classList.add('done');
      el.dots.append(d);
    });

    renderPlayerTime();
  }

  function renderVoiceIcons() {
    [ [el.voiceToggle, document.getElementById('voicePathA')],
      [el.voiceToggle2, document.getElementById('voicePathA2')] ].forEach(([btn, path]) => {
      btn.classList.toggle('active', state.voice);
      path.setAttribute('d', state.voice ? 'M16 8.5a5 5 0 0 1 0 7' : 'M20 9l-5 6M15 9l5 6');
    });
  }

  function renderPlayLabels() {
    const label = state.playing ? '❙❙ Pausa' : (state.started && !state.finished ? '▶ Continuar' : '▶ Play');
    el.playBtn.textContent = label;
    el.playBtn2.textContent = label;
  }

  function render() {
    el.totalText.textContent = mmss(totalSeconds());
    el.countText.textContent = String(state.items.length);
    el.stepText.textContent = state.started ? (state.idx + 1) + '/' + state.items.length : '—';
    renderList();
    renderPlayer();
    renderVoiceIcons();
    renderPlayLabels();
  }

  render();
  setInterval(tick, 150);
})();
