// 브라우저 런타임 네이티브: 창/입력/프레임(amped.web.WebRuntime), BGM(backstage.runtime.audio.WebMpegPlayer)
import { createGL } from './webgl.js';

const APP_PREFIX = '/app'; // CheerpJ /app → 서버 루트
export function createRuntime(container, statusEl) {
  const q = []; // 이벤트 큐: 레코드당 6 int
  let canvas = null, glx = null, gameW = 640, gameH = 480, focused = true;
  let pushed = 0, polled = 0;
  const push = (...r) => { pushed++; while (r.length < 6) r.push(0); q.push(r); if (q.length > 4096) q.splice(0, q.length - 4096); };

  // ---- 키 매핑: 브라우저 keyCode ≒ AWT VK (문자/숫자/화살표/펑션/넘패드) ----
  function vk(e) {
    const k = e.keyCode || 0;
    if (k === 229) return 0;
    // 넘패드 숫자: 브라우저 96~105 == VK_NUMPAD0~9, 화살표 37~40, Home35/36.. 그대로
    return k;
  }
  function keyChar(e) {
    if (e.key && e.key.length === 1) return e.key.charCodeAt(0);
    if (e.key === 'Enter') return 10; if (e.key === 'Backspace') return 8; if (e.key === 'Tab') return 9; if (e.key === 'Escape') return 27;
    return 0xFFFF;
  }
  const mods = e => (e.shiftKey ? 1 : 0) | (e.ctrlKey ? 2 : 0) | (e.altKey ? 8 : 0) | (e.metaKey ? 4 : 0);
  const btnMask = e => ((e.buttons & 1) ? 16 : 0) | ((e.buttons & 2) ? 4 : 0) | ((e.buttons & 4) ? 8 : 0);
  const btnOf = e => e.button === 0 ? 16 : e.button === 2 ? 4 : 8;
  function pos(e) { const r = canvas.getBoundingClientRect(); return [Math.round((e.clientX - r.left) * gameW / r.width), Math.round((e.clientY - r.top) * gameH / r.height)]; }

  function attach() {
    const onKey = (e, id) => {
      if (document.activeElement && /INPUT|TEXTAREA/.test(document.activeElement.tagName)) return;
      const code = vk(e); if (!code) return;
      push(1, id, mods(e), code, keyChar(e));
      if (id === 401 && keyChar(e) !== 0xFFFF && !e.ctrlKey && !e.metaKey) push(1, 400, mods(e), 0, keyChar(e));
      if (!(e.metaKey && (e.key === 'r' || e.key === 'R'))) e.preventDefault();
    };
    window.addEventListener('keydown', e => onKey(e, 401));
    window.addEventListener('keyup', e => onKey(e, 402));
    canvas.addEventListener('contextmenu', e => e.preventDefault());
    canvas.addEventListener('mousedown', e => { canvas.focus(); const [x, y] = pos(e); push(2, 501, mods(e) | btnOf(e), x, y, 1); resumeAudio(); });
    canvas.addEventListener('mouseup', e => { const [x, y] = pos(e); push(2, 502, mods(e) | btnOf(e), x, y, 1); push(2, 500, mods(e) | btnOf(e), x, y, 1); });
    canvas.addEventListener('mousemove', e => { const [x, y] = pos(e); push(2, e.buttons ? 504 : 503, mods(e) | btnMask(e), x, y, 0); });
    canvas.addEventListener('mouseenter', e => { const [x, y] = pos(e); push(2, 506, 0, x, y, 0); });
    canvas.addEventListener('mouseleave', e => { const [x, y] = pos(e); push(2, 507, 0, x, y, 0); });
    canvas.addEventListener('wheel', e => { const [x, y] = pos(e); push(2, 505, mods(e), x, y, e.deltaY > 0 ? -1 : 1); e.preventDefault(); }, { passive: false });
    window.addEventListener('blur', () => { focused = false; push(3, 2103); });
    window.addEventListener('focus', () => { focused = true; push(3, 2102); });
  }

  function fit(fullscreen) {
    const cw = container.clientWidth || gameW, ch = container.clientHeight || gameH;
    const s = fullscreen ? Math.min(cw / gameW, ch / gameH) : Math.min(1, cw / gameW, ch / gameH);
    canvas.style.width = Math.floor(gameW * s) + 'px'; canvas.style.height = Math.floor(gameH * s) + 'px';
  }

  // ---- BGM ----
  let audio = null, audioPending = false;
  let glyphCanvas = null, glyphCtx = null;
  function resumeAudio() { if (audio && audioPending) { audio.play().then(() => { audioPending = false; }).catch(() => {}); } }
  const toUrl = p => p.startsWith(APP_PREFIX) ? p.slice(APP_PREFIX.length) : p;

  let frames = 0, lastFps = performance.now();
  const natives = {
    // amped.web.WebRuntime
    Java_amped_web_WebRuntime_createWindow: async (lib, title, w, h, fullscreen) => {
      gameW = w; gameH = h;
      if (!canvas) { canvas = document.createElement('canvas'); canvas.tabIndex = 0; canvas.id = 'game'; container.appendChild(canvas); attach(); }
      canvas.width = w; canvas.height = h; fit(fullscreen); canvas.focus();
      if (!glx) glx = createGL(canvas);
      glx.gl.viewport(0, 0, w, h);
      document.title = title || 'GetAmped'; if (statusEl) statusEl.textContent = '';
      window.addEventListener('resize', () => fit(fullscreen));
    },
    Java_amped_web_WebRuntime_disposeWindow: async () => { },
    Java_amped_web_WebRuntime_pollEvents: async (lib, buf) => {
      const max = Math.floor(buf.length / 6); let n = 0;
      while (n < max && q.length) { const r = q.shift(); for (let i = 0; i < 6; i++) buf[n * 6 + i] = r[i] | 0; n++; }
      polled += n; return n;
    },
    Java_amped_web_WebRuntime_swapBuffers: async () => {
      frames++; const now = performance.now();
      if (now - lastFps > 2000) { const fps = frames * 1000 / (now - lastFps); frames = 0; lastFps = now; if (statusEl) statusEl.textContent = fps.toFixed(0) + ' fps'; }
      await new Promise(r => requestAnimationFrame(r));
    },
    Java_amped_web_WebRuntime_setCursorVisible: async (lib, b) => { if (canvas) canvas.style.cursor = b ? 'default' : 'none'; },
    Java_amped_web_WebRuntime_hasFocus: async () => focused,
    Java_amped_web_WebRuntime_log: async (lib, s) => console.log('[java]', s),
    // 글리프 래스터화: size×size 알파. 원본 Java2DFont 와 같은 배치(베이스라인 y=size, descent 만큼 위에서 읽기)
    Java_amped_web_WebRuntime_renderGlyph: async (lib, c, size, out) => {
      try {
        if (!glyphCanvas) { glyphCanvas = document.createElement('canvas'); glyphCanvas.width = 64; glyphCanvas.height = 64; glyphCtx = glyphCanvas.getContext('2d', { willReadFrequently: true }); }
        const ctx = glyphCtx, px = size - 1;
        ctx.clearRect(0, 0, 64, 64);
        ctx.font = `${px}px "AmpedKR", "AmpedJP", "Noto Sans KR", "Noto Sans JP", "Hiragino Sans", "Apple SD Gothic Neo", "Malgun Gothic", "Yu Gothic", sans-serif`;
        ctx.textBaseline = 'alphabetic'; ctx.fillStyle = '#fff';
        const ch = String.fromCharCode(c);
        const m = ctx.measureText(ch);
        const descent = Math.round(m.fontBoundingBoxDescent || px * 0.22);
        ctx.fillText(ch, 0, size);
        const img = ctx.getImageData(0, descent, size, size).data;
        for (let i = 0; i < size * size; i++) out[i] = img[i * 4 + 3];
        return (m.width || px) / size;
      } catch (e) { console.warn('renderGlyph', e); return -1; }
    },

    // backstage.runtime.audio.WebMpegPlayer
    Java_backstage_runtime_audio_WebMpegPlayer_openNative: async (lib, path) => {
      if (audio) { audio.pause(); audio.src = ''; }
      audio = new Audio(toUrl(path)); audio.preload = 'auto';
      return 0;
    },
    Java_backstage_runtime_audio_WebMpegPlayer_closeNative: async () => { if (audio) { audio.pause(); audio.src = ''; audio = null; } },
    Java_backstage_runtime_audio_WebMpegPlayer_playNative: async () => { if (!audio) return; audio.play().then(() => { audioPending = false; }).catch(() => { audioPending = true; }); },
    Java_backstage_runtime_audio_WebMpegPlayer_stopNative: async () => { if (audio) { audio.pause(); audio.currentTime = 0; audioPending = false; } },
    Java_backstage_runtime_audio_WebMpegPlayer_setVolumeNative: async (lib, v) => { if (audio) audio.volume = Math.max(0, Math.min(1, v)); },
    Java_backstage_runtime_audio_WebMpegPlayer_setLoopNative: async (lib, b) => { if (audio) audio.loop = !!b; },
    Java_backstage_runtime_audio_WebMpegPlayer_seekNative: async (lib, ms) => { if (audio) audio.currentTime = ms / 1000; },
    Java_backstage_runtime_audio_WebMpegPlayer_currentTimeNative: async () => audio ? Math.floor(audio.currentTime * 1000) : 0,
  };

  // GLWeb 네이티브: 생성된 이름 목록 전체에 대해 등록(미구현은 1회 경고)
  function installGL(names) {
    const warned = new Set();
    for (const name of names) {
      // JNI 맹글링: 메서드명의 '_' 는 '_1' 로 바뀐다
      natives['Java_keel_gfx_GLWeb_' + name.replace(/_/g, '_1')] = (lib, ...args) => {
        if (!glx) return undefined;
        const f = glx.N[name];
        if (f) return f(...args);
        if (!warned.has(name)) { warned.add(name); console.warn('GL native not implemented:', name); }
        return name.startsWith('glIs') || name.startsWith('glAre') ? false : 0;
      };
    }
  }
  // 가상 패드/외부 입력용: 브라우저 키 이벤트와 같은 레코드를 큐에 넣는다 (code=AWT VK, ch=문자 코드 또는 0xFFFF)
  function key(code, ch, down) { push(1, down ? 401 : 402, 0, code, ch); if (down && ch !== 0xFFFF) push(1, 400, 0, 0, ch); if (down) resumeAudio(); }
  return { natives, installGL, key, getGL: () => glx, debug: () => ({ queued: q.length, pushed, polled, focused, active: document.activeElement && document.activeElement.id }) };
}
