// OpenGL 1.1 고정 파이프라인(GLEngine 이 쓰는 부분집합) → WebGL2 에뮬레이터
// CheerpJ 네이티브: Java_keel_gfx_GLWeb_<name>(lib, ...args)  (모두 static)
export function createGL(canvas) {
  const gl = canvas.getContext('webgl2', { alpha: false, antialias: false, depth: true, stencil: true, preserveDrawingBuffer: false });
  if (!gl) throw new Error('WebGL2 not available');

  // ---------- 셰이더 ----------
  const vs = `#version 300 es
  layout(location=0) in vec3 aPos; layout(location=1) in vec4 aColor; layout(location=2) in vec2 aUV;
  uniform mat4 uMVP; uniform mat4 uTexMat; out vec4 vColor; out vec2 vUV;
  void main(){ gl_Position = uMVP * vec4(aPos,1.0); vColor = aColor; vUV = (uTexMat * vec4(aUV,0.0,1.0)).xy; }`;
  const fs = `#version 300 es
  precision mediump float; in vec4 vColor; in vec2 vUV; uniform sampler2D uTex; uniform int uUseTex; uniform int uAlphaTest; uniform float uAlphaRef; out vec4 o;
  void main(){ vec4 c = vColor; if (uUseTex == 1) c *= texture(uTex, vUV); if (uAlphaTest == 1 && !(c.a > uAlphaRef)) discard; o = c; }`;
  function sh(type, src) { const s = gl.createShader(type); gl.shaderSource(s, src); gl.compileShader(s); if (!gl.getShaderParameter(s, gl.COMPILE_STATUS)) throw new Error(gl.getShaderInfoLog(s)); return s; }
  const prog = gl.createProgram(); gl.attachShader(prog, sh(gl.VERTEX_SHADER, vs)); gl.attachShader(prog, sh(gl.FRAGMENT_SHADER, fs)); gl.linkProgram(prog);
  if (!gl.getProgramParameter(prog, gl.LINK_STATUS)) throw new Error(gl.getProgramInfoLog(prog));
  gl.useProgram(prog);
  const U = { mvp: gl.getUniformLocation(prog, 'uMVP'), tex: gl.getUniformLocation(prog, 'uTexMat'), useTex: gl.getUniformLocation(prog, 'uUseTex'), at: gl.getUniformLocation(prog, 'uAlphaTest'), aref: gl.getUniformLocation(prog, 'uAlphaRef'), smp: gl.getUniformLocation(prog, 'uTex') };
  gl.uniform1i(U.smp, 0);

  // ---------- 행렬 ----------
  const I = () => new Float32Array([1,0,0,0, 0,1,0,0, 0,0,1,0, 0,0,0,1]);
  function mul(a, b) { // a*b (column-major)
    const r = new Float32Array(16);
    for (let c = 0; c < 4; c++) for (let rr = 0; rr < 4; rr++) { let s = 0; for (let k = 0; k < 4; k++) s += a[k*4+rr] * b[c*4+k]; r[c*4+rr] = s; }
    return r;
  }
  const stacks = { 5888: [I()], 5889: [I()], 5890: [I()] }; // MODELVIEW, PROJECTION, TEXTURE
  let mode = 5888;
  const top = (m = mode) => stacks[m][stacks[m].length - 1];
  const setTop = (v, m = mode) => { stacks[m][stacks[m].length - 1] = v; dirty = true; };
  let dirty = true;
  function uploadMatrices() {
    if (!dirty) return; dirty = false;
    gl.uniformMatrix4fv(U.mvp, false, mul(top(5889), top(5888)));
    gl.uniformMatrix4fv(U.tex, false, top(5890));
  }

  // ---------- 상태 ----------
  const st = {
    color: [1, 1, 1, 1], uv: [0, 0],
    tex2d: false, alphaTest: false, alphaRef: 0, depthTest: false, blend: false, cull: false, polyOffset: false,
    lineMode: false, boundTex: 0,
    vertexArr: null, uvArr: null, vertexArrEnabled: false, uvArrEnabled: false,
    recording: null, lists: new Map(), nextList: 1,
    textures: new Map(), nextTex: 1,
    pixelMap: { 3186: null, 3187: null, 3188: null },
    stats: { tex: {}, batches: 0, batchesTex: 0, indexed: 0, indexedTex: 0, lists: 0 },
  };
  const texWrap = v => v === 10497 ? gl.REPEAT : gl.CLAMP_TO_EDGE;

  // ---------- 즉시 모드 버퍼 ----------
  let imm = null; // { mode, pos:[], col:[], uv:[] }
  const vboPos = gl.createBuffer(), vboCol = gl.createBuffer(), vboUV = gl.createBuffer(), ebo = gl.createBuffer();

  function applyFixedState() {
    uploadMatrices();
    gl.uniform1i(U.useTex, st.tex2d && st.boundTex ? 1 : 0);
    gl.uniform1i(U.at, st.alphaTest ? 1 : 0); gl.uniform1f(U.aref, st.alphaRef);
    if (st.tex2d && st.boundTex) { const t = st.textures.get(st.boundTex); gl.activeTexture(gl.TEXTURE0); gl.bindTexture(gl.TEXTURE_2D, t ? t.tex : null); }
  }

  function trianglesToLines(idx) { // 삼각형 인덱스 → 선분 인덱스
    const out = new Uint32Array(idx.length * 2);
    for (let i = 0, j = 0; i + 2 < idx.length; i += 3) { out[j++] = idx[i]; out[j++] = idx[i+1]; out[j++] = idx[i+1]; out[j++] = idx[i+2]; out[j++] = idx[i+2]; out[j++] = idx[i]; }
    return out;
  }

  // 배치 그리기: pos Float32Array(xyz*n), col Float32Array(rgba*n), uv Float32Array(st*n), 모드(GL enum)
  function drawBatch(b) {
    st.stats.batches++; if (st.tex2d && st.boundTex) st.stats.batchesTex++;
    applyFixedState();
    const n = b.pos.length / 3;
    gl.bindBuffer(gl.ARRAY_BUFFER, vboPos); gl.bufferData(gl.ARRAY_BUFFER, b.pos, gl.STREAM_DRAW); gl.enableVertexAttribArray(0); gl.vertexAttribPointer(0, 3, gl.FLOAT, false, 0, 0);
    gl.bindBuffer(gl.ARRAY_BUFFER, vboCol); gl.bufferData(gl.ARRAY_BUFFER, b.col, gl.STREAM_DRAW); gl.enableVertexAttribArray(1); gl.vertexAttribPointer(1, 4, gl.FLOAT, false, 0, 0);
    gl.bindBuffer(gl.ARRAY_BUFFER, vboUV); gl.bufferData(gl.ARRAY_BUFFER, b.uv, gl.STREAM_DRAW); gl.enableVertexAttribArray(2); gl.vertexAttribPointer(2, 2, gl.FLOAT, false, 0, 0);
    let idx = null, prim = gl.TRIANGLES;
    switch (b.mode) {
      case 1: prim = gl.LINES; break;                 // GL_LINES
      case 2: prim = gl.LINE_LOOP; break;
      case 3: prim = gl.LINE_STRIP; break;
      case 4: break;                                  // GL_TRIANGLES
      case 5: prim = gl.TRIANGLE_STRIP; break;
      case 6: case 9: {                               // FAN / POLYGON
        idx = new Uint32Array(Math.max(0, (n - 2) * 3)); for (let i = 0, j = 0; i < n - 2; i++) { idx[j++] = 0; idx[j++] = i + 1; idx[j++] = i + 2; } break; }
      case 7: {                                       // GL_QUADS
        const q = n >> 2; idx = new Uint32Array(q * 6); for (let i = 0, j = 0; i < q; i++) { const b0 = i*4; idx[j++]=b0; idx[j++]=b0+1; idx[j++]=b0+2; idx[j++]=b0; idx[j++]=b0+2; idx[j++]=b0+3; } break; }
      case 8: {                                       // GL_QUAD_STRIP
        const q = (n - 2) >> 1; idx = new Uint32Array(q * 6); for (let i = 0, j = 0; i < q; i++) { const b0 = i*2; idx[j++]=b0; idx[j++]=b0+1; idx[j++]=b0+3; idx[j++]=b0; idx[j++]=b0+3; idx[j++]=b0+2; } break; }
    }
    if (prim === gl.TRIANGLES && st.lineMode) { idx = trianglesToLines(idx || Uint32Array.from({ length: n }, (_, i) => i)); prim = gl.LINES; }
    if (idx) { gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER, ebo); gl.bufferData(gl.ELEMENT_ARRAY_BUFFER, idx, gl.STREAM_DRAW); gl.drawElements(prim, idx.length, gl.UNSIGNED_INT, 0); }
    else gl.drawArrays(prim, 0, n);
  }

  // 디스플레이 리스트 녹화: 상태 호출은 함수+인자로, 지오메트리는 배치로 저장
  function rec(fn, args) { if (st.recording) { st.recording.push([fn, args]); return true; } return false; }

  const N = {};
  // ---- 즉시 모드 ----
  N.glBegin = (m) => { imm = { mode: m, pos: [], col: [], uv: [] }; };
  N.glEnd = () => {
    if (!imm) return;
    const b = { mode: imm.mode, pos: new Float32Array(imm.pos), col: new Float32Array(imm.col), uv: new Float32Array(imm.uv) };
    imm = null;
    if (st.recording) st.recording.push(['__batch', [b]]); else drawBatch(b);
  };
  const vtx = (x, y, z) => { if (!imm) return; imm.pos.push(x, y, z); imm.col.push(st.color[0], st.color[1], st.color[2], st.color[3]); imm.uv.push(st.uv[0], st.uv[1]); };
  N.glVertex2f = (x, y) => vtx(x, y, 0); N.glVertex3f = vtx; N.glVertex2i = (x, y) => vtx(x, y, 0); N.glVertex3fv = (v) => vtx(v[0], v[1], v[2]);
  N.glTexCoord2f = (s, t) => { st.uv[0] = s; st.uv[1] = t; };
  N.glColor4f = (r, g, b, a) => { if (rec('glColor4f', [r, g, b, a])) return; st.color[0] = r; st.color[1] = g; st.color[2] = b; st.color[3] = a; };
  N.glColor3f = (r, g, b) => N.glColor4f(r, g, b, 1);
  N.glColor4fv = (v) => N.glColor4f(v[0], v[1], v[2], v[3]);
  N.glColor3fv = (v) => N.glColor4f(v[0], v[1], v[2], 1);

  // ---- 배열 ----
  N.arrayPointer = (kind, size, type, stride, data) => { const a = data ? { size, type, stride, data } : null; if (kind === 0) st.vertexArr = a; else if (kind === 1) st.uvArr = a; };
  N.glEnableClientState = (c) => { if (c === 32884) st.vertexArrEnabled = true; else if (c === 32888) st.uvArrEnabled = true; };
  N.glDisableClientState = (c) => { if (c === 32884) st.vertexArrEnabled = false; else if (c === 32888) st.uvArrEnabled = false; };
  function bindArray(loc, a, comps) {
    if (!a || !a.data) { gl.disableVertexAttribArray(loc); return; }
    const d = a.data; let gltype = gl.FLOAT, bytes = 4;
    if (d instanceof Int16Array) { gltype = gl.SHORT; bytes = 2; } else if (d instanceof Int32Array) { gltype = gl.INT; bytes = 4; } else if (d instanceof Float64Array) { bindArray(loc, { ...a, data: Float32Array.from(d) }, comps); return; }
    const buf = loc === 0 ? vboPos : vboUV;
    gl.bindBuffer(gl.ARRAY_BUFFER, buf); gl.bufferData(gl.ARRAY_BUFFER, d, gl.STREAM_DRAW);
    gl.enableVertexAttribArray(loc); gl.vertexAttribPointer(loc, a.size || comps, gltype, false, a.stride || 0, 0);
  }
  function drawIndexed(mode, count, idx) {
    if (!st.vertexArrEnabled || !st.vertexArr) return;
    st.stats.indexed++; if (st.tex2d && st.boundTex) st.stats.indexedTex++;
    applyFixedState();
    bindArray(0, st.vertexArr, 3);
    gl.disableVertexAttribArray(1); gl.vertexAttrib4f(1, st.color[0], st.color[1], st.color[2], st.color[3]);
    if (st.uvArrEnabled && st.uvArr) bindArray(2, st.uvArr, 2); else { gl.disableVertexAttribArray(2); gl.vertexAttrib2f(2, st.uv[0], st.uv[1]); }
    let prim = mode === 4 ? gl.TRIANGLES : mode === 1 ? gl.LINES : mode === 5 ? gl.TRIANGLE_STRIP : mode === 6 ? gl.TRIANGLE_FAN : gl.TRIANGLES;
    let ind = idx instanceof Int32Array ? new Uint32Array(idx.buffer, idx.byteOffset, count) : Uint32Array.from(idx.subarray(0, count));
    if (prim === gl.TRIANGLES && st.lineMode) { ind = trianglesToLines(ind); prim = gl.LINES; }
    gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER, ebo); gl.bufferData(gl.ELEMENT_ARRAY_BUFFER, ind, gl.STREAM_DRAW);
    gl.drawElements(prim, ind.length, gl.UNSIGNED_INT, 0);
  }
  N.glDrawElements_I = (mode, count, type, idx) => drawIndexed(mode, count, idx);
  N.glDrawElements_S = (mode, count, type, idx) => drawIndexed(mode, count, idx);
  N.glDrawRangeElements_I = (mode, s, e, count, type, idx) => drawIndexed(mode, count, idx);
  N.glDrawArrays = (mode, first, count) => { const ind = new Uint32Array(count); for (let i = 0; i < count; i++) ind[i] = first + i; drawIndexed(mode, count, ind); };
  N.glLockArraysEXT = () => {}; N.glUnlockArraysEXT = () => {};

  // ---- 상태 ----
  const enables = {
    3553: v => { st.tex2d = v; }, 3008: v => { st.alphaTest = v; },
    2929: v => { st.depthTest = v; v ? gl.enable(gl.DEPTH_TEST) : gl.disable(gl.DEPTH_TEST); },
    3042: v => { st.blend = v; v ? gl.enable(gl.BLEND) : gl.disable(gl.BLEND); },
    2884: v => { st.cull = v; v ? gl.enable(gl.CULL_FACE) : gl.disable(gl.CULL_FACE); },
    32823: v => { st.polyOffset = v; v ? gl.enable(gl.POLYGON_OFFSET_FILL) : gl.disable(gl.POLYGON_OFFSET_FILL); },
    3089: v => { v ? gl.enable(gl.SCISSOR_TEST) : gl.disable(gl.SCISSOR_TEST); },
  };
  N.glEnable = (c) => { if (rec('glEnable', [c])) return; const f = enables[c]; if (f) f(true); };
  N.glDisable = (c) => { if (rec('glDisable', [c])) return; const f = enables[c]; if (f) f(false); };
  N.glIsEnabled = (c) => (c === 3553 ? st.tex2d : c === 2929 ? st.depthTest : c === 3042 ? st.blend : c === 2884 ? st.cull : c === 3008 ? st.alphaTest : false);
  N.glAlphaFunc = (func, ref) => { st.alphaRef = ref; /* GLEngine 은 GL_GREATER 만 사용 */ };
  N.glBlendFunc = (s, d) => { if (rec('glBlendFunc', [s, d])) return; gl.blendFunc(s, d); };
  N.glDepthFunc = (f) => { if (rec('glDepthFunc', [f])) return; gl.depthFunc(f); };
  N.glDepthMask = (b) => { if (rec('glDepthMask', [b])) return; gl.depthMask(!!b); };
  N.glColorMask = (r, g, b, a) => gl.colorMask(!!r, !!g, !!b, !!a);
  N.glCullFace = (m) => { if (rec('glCullFace', [m])) return; gl.cullFace(m); };
  N.glFrontFace = (m) => gl.frontFace(m);
  N.glPolygonMode = (face, m) => { if (rec('glPolygonMode', [face, m])) return; st.lineMode = (m === 6913); };
  N.glPolygonOffset = (f, u) => gl.polygonOffset(f, u);
  N.glLineWidth = (w) => gl.lineWidth(Math.max(1, w));
  N.glShadeModel = () => {}; N.glHint = () => {}; N.glDrawBuffer = () => {}; N.glReadBuffer = () => {};
  N.glClearColor = (r, g, b, a) => gl.clearColor(r, g, b, a);
  N.glClearDepth = (d) => gl.clearDepth(d);
  N.glClear = (mask) => { let m = 0; if (mask & 16384) m |= gl.COLOR_BUFFER_BIT; if (mask & 256) m |= gl.DEPTH_BUFFER_BIT; if (mask & 1024) m |= gl.STENCIL_BUFFER_BIT; gl.clear(m); };
  N.glViewport = (x, y, w, h) => gl.viewport(x, y, Math.max(0, w), Math.max(0, h));
  N.glScissor = (x, y, w, h) => gl.scissor(x, y, w, h);
  N.glFlush = () => gl.flush(); N.glFinish = () => gl.flush();
  N.glGetError = () => 0;

  // ---- 행렬 ----
  N.glMatrixMode = (m) => { if (rec('glMatrixMode', [m])) return; if (stacks[m]) mode = m; };
  N.glLoadIdentity = () => { if (rec('glLoadIdentity', [])) return; setTop(I()); };
  N.glPushMatrix = () => { if (rec('glPushMatrix', [])) return; stacks[mode].push(new Float32Array(top())); };
  N.glPopMatrix = () => { if (rec('glPopMatrix', [])) return; if (stacks[mode].length > 1) stacks[mode].pop(); dirty = true; };
  N.glLoadMatrixf = (m) => { if (rec('glLoadMatrixf', [Float32Array.from(m)])) return; setTop(Float32Array.from(m)); };
  N.glMultMatrixf = (m) => { if (rec('glMultMatrixf', [Float32Array.from(m)])) return; setTop(mul(top(), Float32Array.from(m))); };
  N.glLoadMatrixd = (m) => N.glLoadMatrixf(m); N.glMultMatrixd = (m) => N.glMultMatrixf(m);
  N.glTranslatef = (x, y, z) => { if (rec('glTranslatef', [x, y, z])) return; const t = I(); t[12] = x; t[13] = y; t[14] = z; setTop(mul(top(), t)); };
  N.glScalef = (x, y, z) => { if (rec('glScalef', [x, y, z])) return; const t = I(); t[0] = x; t[5] = y; t[10] = z; setTop(mul(top(), t)); };
  N.glRotatef = (a, x, y, z) => {
    if (rec('glRotatef', [a, x, y, z])) return;
    const l = Math.hypot(x, y, z) || 1; x /= l; y /= l; z /= l; const r = a * Math.PI / 180, c = Math.cos(r), s = Math.sin(r), t = 1 - c;
    const m = new Float32Array([t*x*x+c, t*x*y+s*z, t*x*z-s*y, 0, t*x*y-s*z, t*y*y+c, t*y*z+s*x, 0, t*x*z+s*y, t*y*z-s*x, t*z*z+c, 0, 0, 0, 0, 1]);
    setTop(mul(top(), m));
  };
  N.glOrtho = (l, r, b, t, n, f) => {
    if (rec('glOrtho', [l, r, b, t, n, f])) return;
    const m = I(); m[0] = 2/(r-l); m[5] = 2/(t-b); m[10] = -2/(f-n); m[12] = -(r+l)/(r-l); m[13] = -(t+b)/(t-b); m[14] = -(f+n)/(f-n); setTop(mul(top(), m));
  };
  N.glFrustum = (l, r, b, t, n, f) => {
    const m = new Float32Array(16); m[0] = 2*n/(r-l); m[5] = 2*n/(t-b); m[8] = (r+l)/(r-l); m[9] = (t+b)/(t-b); m[10] = -(f+n)/(f-n); m[11] = -1; m[14] = -2*f*n/(f-n); setTop(mul(top(), m));
  };
  N.gluPerspective = (fovy, aspect, n, f) => {
    if (rec('gluPerspective', [fovy, aspect, n, f])) return;
    const fh = Math.tan(fovy / 360 * Math.PI) * n, fw = fh * aspect; N.glFrustum(-fw, fw, -fh, fh, n, f);
  };
  N.gluOrtho2D = (l, r, b, t) => N.glOrtho(l, r, b, t, -1, 1);

  // ---- 질의 ----
  N.glGetFloatv = (p, out) => {
    let src = null; if (p === 2982) src = top(5888); else if (p === 2983) src = top(5889); else if (p === 2984) src = top(5890); else if (p === 2816) src = st.color;
    if (src) for (let i = 0; i < Math.min(out.length, src.length); i++) out[i] = src[i]; else out[0] = 0;
  };
  N.glGetIntegerv = (p, out) => {
    switch (p) {
      case 2979: out[0] = stacks[5888].length; break;   // MODELVIEW_STACK_DEPTH
      case 2981: out[0] = stacks[5889].length; break;   // PROJECTION_STACK_DEPTH
      case 2978: { const v = gl.getParameter(gl.VIEWPORT); for (let i = 0; i < 4; i++) out[i] = v[i]; break; }
      case 3379: out[0] = gl.getParameter(gl.MAX_TEXTURE_SIZE); break;
      case 2976: out[0] = mode; break;
      default: out[0] = 0;
    }
  };
  N.glGetBooleanv = (p, out) => { out[0] = N.glIsEnabled(p) ? 1 : 0; };
  const strings = { 7936: 'CyberStep-Web', 7937: 'WebGL2 fixed-function emulation', 7938: '1.1 WebGL', 7939: 'GL_EXT_compiled_vertex_array GL_EXT_draw_range_elements' };
  N.glGetString = (n) => strings[n] || ''; N.glGetStringPrivete = (n) => strings[n] || '';

  // ---- 텍스처 ----
  N.glGenTextures = (n, ids) => { for (let i = 0; i < n; i++) { const id = st.nextTex++; st.textures.set(id, { tex: gl.createTexture(), loaded: false, wrapS: gl.REPEAT, wrapT: gl.REPEAT, min: gl.LINEAR, mag: gl.LINEAR }); ids[i] = id; } };
  N.glDeleteTextures = (n, ids) => { for (let i = 0; i < n; i++) { const t = st.textures.get(ids[i]); if (t) { gl.deleteTexture(t.tex); st.textures.delete(ids[i]); } if (st.boundTex === ids[i]) st.boundTex = 0; } };
  N.glBindTexture = (target, id) => {
    if (rec('glBindTexture', [target, id])) return;
    if (id && !st.textures.has(id)) st.textures.set(id, { tex: gl.createTexture(), loaded: false, wrapS: gl.REPEAT, wrapT: gl.REPEAT, min: gl.LINEAR, mag: gl.LINEAR });
    st.boundTex = id; const t = st.textures.get(id); gl.activeTexture(gl.TEXTURE0); gl.bindTexture(gl.TEXTURE_2D, t ? t.tex : null);
  };
  N.glIsTexture = (id) => st.textures.has(id);
  N.glAreTexturesResident = (n, ids, res) => { let all = true; for (let i = 0; i < n; i++) { const t = st.textures.get(ids[i]); const r = !!(t && t.loaded); res[i] = r ? 1 : 0; if (!r) all = false; } return all; };
  N.glPrioritizeTextures = () => {};
  N.glTexParameteri = (target, p, v) => {
    const t = st.textures.get(st.boundTex); if (!t) return;
    if (p === 10242) { t.wrapS = texWrap(v); gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_S, t.wrapS); }
    else if (p === 10243) { t.wrapT = texWrap(v); gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_T, t.wrapT); }
    else if (p === 10240) { t.mag = v === 9728 ? gl.NEAREST : gl.LINEAR; gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MAG_FILTER, t.mag); }
    else if (p === 10241) { t.min = v === 9728 ? gl.NEAREST : gl.LINEAR; gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, t.min); }
  };
  N.glTexParameterf = (target, p, v) => { if (p !== 32870) N.glTexParameteri(target, p, v); };
  N.glPixelMapfv = (map, size, v) => { st.pixelMap[map] = Float32Array.from(v.subarray ? v.subarray(0, size) : v); };
  N.glPixelStorei = () => {}; N.glTexEnvi = () => {}; N.glTexEnvf = () => {};
  // 포맷 변환 → RGBA8 (ALPHA 는 gl.ALPHA 로 그대로)
  N.glTexImage2D_B = (target, level, internal, w, h, border, format, type, pix) => {
    const t = st.textures.get(st.boundTex); if (!t) return;
    const u8 = pix instanceof Uint8Array ? pix : new Uint8Array(pix.buffer, pix.byteOffset, pix.length);
    const sk = format + ':' + w + 'x' + h; st.stats.tex[sk] = (st.stats.tex[sk] || 0) + 1;
    let fmt = gl.RGBA, data = null, ifmt = gl.RGBA;
    const n = w * h;
    switch (format) {
      case 6400: { // COLOR_INDEX → 팔레트(GL_PIXEL_MAP_I_TO_R/G/B) 적용
        const R = st.pixelMap[3186], G = st.pixelMap[3187], B = st.pixelMap[3188]; data = new Uint8Array(n * 4);
        for (let i = 0; i < n; i++) { const k = u8[i]; data[i*4] = R ? R[k] * 255 : k; data[i*4+1] = G ? G[k] * 255 : k; data[i*4+2] = B ? B[k] * 255 : k; data[i*4+3] = 255; }
        break; }
      case 6407: { data = new Uint8Array(n * 4); for (let i = 0; i < n; i++) { data[i*4] = u8[i*3]; data[i*4+1] = u8[i*3+1]; data[i*4+2] = u8[i*3+2]; data[i*4+3] = 255; } break; } // RGB
      case 6406: { // ALPHA: 고정파이프라인 MODULATE 에서 RGB 는 프래그먼트 색 그대로 → RGBA(255,255,255,a) 로 올린다
        data = new Uint8Array(n * 4); for (let i = 0; i < n; i++) { data[i*4] = data[i*4+1] = data[i*4+2] = 255; data[i*4+3] = u8[i]; } break; }
      case 6408: { data = u8.subarray(0, n * 4); break; } // RGBA
      case 6409: { data = new Uint8Array(n * 4); for (let i = 0; i < n; i++) { const v = u8[i]; data[i*4] = data[i*4+1] = data[i*4+2] = v; data[i*4+3] = 255; } break; } // LUMINANCE
      case 6656: { // BITMAP(1bpp, 행은 바이트 정렬) → 흑백
        data = new Uint8Array(n * 4); const rowBytes = Math.ceil(w / 8);
        for (let y = 0; y < h; y++) for (let x = 0; x < w; x++) { const bit = (u8[y * rowBytes + (x >> 3)] >> (7 - (x & 7))) & 1; const o = (y * w + x) * 4; data[o] = data[o+1] = data[o+2] = bit ? 255 : 0; data[o+3] = 255; }
        break; }
      default: console.warn('glTexImage2D unsupported format', format); return;
    }
    gl.pixelStorei(gl.UNPACK_ALIGNMENT, 1);
    gl.texImage2D(gl.TEXTURE_2D, level, ifmt, w, h, 0, fmt, gl.UNSIGNED_BYTE, data);
    gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_S, t.wrapS); gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_T, t.wrapT);
    gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, t.min); gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MAG_FILTER, t.mag);
    t.loaded = true; t.w = w; t.h = h;
  };

  // ---- 디스플레이 리스트 ----
  N.glGenLists = (range) => { const base = st.nextList; st.nextList += Math.max(1, range); return base; };
  N.glDeleteLists = (base, range) => { for (let i = 0; i < range; i++) st.lists.delete(base + i); };
  N.glIsList = (id) => st.lists.has(id);
  N.glNewList = (id, m) => { st.recording = []; st.recording.id = id; st.recording.execute = (m === 4866); };
  N.glEndList = () => { if (!st.recording) return; const r = st.recording; st.recording = null; st.lists.set(r.id, r); if (r.execute) N.glCallList(r.id); };
  N.glCallList = (id) => {
    if (rec('glCallList', [id])) return;
    const r = st.lists.get(id); if (!r) return;
    for (const [fn, args] of r) { if (fn === '__batch') drawBatch(args[0]); else N[fn](...args); }
  };
  N.glListBase = () => {};

  return { gl, N, state: st };
}
