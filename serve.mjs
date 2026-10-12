// Range 지원 정적 서버 (CheerpJ 필수)
import http from 'node:http'; import fs from 'node:fs'; import path from 'node:path';
const root = process.argv[2] || '.'; const port = +(process.argv[3] || 8787);
const types = { '.html':'text/html', '.js':'text/javascript', '.mjs':'text/javascript', '.jar':'application/java-archive', '.class':'application/octet-stream', '.json':'application/json', '.png':'image/png', '.gif':'image/gif', '.mp3':'audio/mpeg', '.wasm':'application/wasm', '.txt':'text/plain', '.css':'text/css' };
http.createServer((req, res) => {
  let p = decodeURIComponent(new URL(req.url, 'http://x').pathname);
  if (p === '/' || (p.endsWith('/') && fs.existsSync(path.join(root, p, 'index.html')))) p += 'index.html';
  const f = path.join(root, p);
  if (!f.startsWith(path.resolve(root)) && !f.startsWith(root)) { res.writeHead(403); return res.end(); }
  fs.stat(f, (err, st) => {
    const isList = path.basename(f) === 'index.list' && fs.existsSync(path.dirname(f));
    if (err && !isList) { res.writeHead(404); return res.end('nf'); }
    // CheerpJ 는 응답 URL 이 '/' 로 끝나야 디렉토리로 본다 → 슬래시 없는 디렉토리 요청은 301
    if (!isList && st.isDirectory() && !p.endsWith('/')) { res.writeHead(301, { Location: p + '/' }); return res.end(); }
    if (isList || st.isDirectory()) {
      // CheerpJ: 디렉토리 URL → 200, <dir>/index.list → 항목 목록(디렉토리는 뒤에 /). Range 도 지원해야 함.
      const dir = isList ? path.dirname(f) : f;
      let names = [];
      try { names = fs.readdirSync(dir, { withFileTypes: true }).filter(d => d.name !== 'index.list').map(d => d.name); } catch {}
      const buf = Buffer.from(names.join('\n') + (names.length ? '\n' : ''));
      const hd = { 'Accept-Ranges': 'bytes', 'Content-Type': 'text/plain', 'Cache-Control': 'no-store' };
      const r = req.headers.range;
      if (r) {
        const m = /bytes=(\d*)-(\d*)/.exec(r); let s = m[1] ? +m[1] : 0; let e = m[2] ? +m[2] : buf.length - 1;
        if (!m[1]) { s = Math.max(0, buf.length - +m[2]); e = buf.length - 1; }
        e = Math.min(e, buf.length - 1);
        hd['Content-Range'] = `bytes ${s}-${e}/${buf.length}`; hd['Content-Length'] = Math.max(0, e - s + 1);
        res.writeHead(206, hd); return res.end(buf.subarray(s, e + 1));
      }
      hd['Content-Length'] = buf.length; res.writeHead(200, hd); return res.end(buf);
    }
    const h = { 'Accept-Ranges': 'bytes', 'Content-Type': types[path.extname(f)] || 'application/octet-stream', 'Cache-Control': 'no-store', 'Access-Control-Allow-Origin': '*' };
    const r = req.headers.range;
    if (r) {
      const m = /bytes=(\d*)-(\d*)/.exec(r); let s = m[1] ? +m[1] : 0; let e = m[2] ? +m[2] : st.size - 1;
      if (!m[1]) { s = st.size - +m[2]; e = st.size - 1; }
      e = Math.min(e, st.size - 1);
      h['Content-Range'] = `bytes ${s}-${e}/${st.size}`; h['Content-Length'] = e - s + 1;
      res.writeHead(206, h); if (req.method === 'HEAD') return res.end();
      return fs.createReadStream(f, { start: s, end: e }).pipe(res);
    }
    h['Content-Length'] = st.size; res.writeHead(200, h); if (req.method === 'HEAD') return res.end();
    fs.createReadStream(f).pipe(res);
  });
}).listen(port, () => console.log('serving', root, 'on', port));
