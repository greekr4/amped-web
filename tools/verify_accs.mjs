#!/usr/bin/env node
// 악세사리 기술 검증 하네스. 크로미움 창을 띄워 돌린다(헤드리스는 입력 타이밍을 놓쳐 신뢰할 수 없음).
//   node tools/verify_accs.mjs <accs id> [base url]
// 필요: playwright-core (PW_CORE 환경변수로 모듈 경로 지정, 기본 ./node_modules) 와
//       크로미움 헤드리스 셸 (PW_EXE 로 실행 파일 지정, 없으면 playwright 기본 캐시).
// 출력: JSON { loaded, slots, errors, missing } + .shots/verify-<accs>-{ax,jx,dx}.png
// 판정 기준(docs/accs-convention.md §10): slots 에 14종(W_sss 는 원본 유지면 생략 가능), errors 0.
import { createRequire } from 'node:module';
import path from 'node:path';
import fs from 'node:fs';

const require = createRequire(import.meta.url);
const pwPath = process.env.PW_CORE || 'playwright-core';
const { chromium } = require(pwPath);
const accs = process.argv[2];
if (!accs) { console.error('usage: verify_accs.mjs <accs id> [base url]'); process.exit(2); }
const base = process.argv[3] || 'http://localhost:8777/';
const root = path.resolve(path.dirname(new URL(import.meta.url).pathname), '..');
const shots = path.join(root, '.shots'); fs.mkdirSync(shots, { recursive: true });
const NOISE = /dic_en|lpoint|rpoint|target_want2|Is a directory|ThrowItemAction|Stack trace|omusa/;
const EXPECT = ['W_s', 'W_ss', 'W_sss', 'W_b', 'W_bb', 'D_s', 'D_b', 'D_x', 'J_s', 'J_b', 'J_x', 'A_s', 'A_b', 'A_x'];

// 헤드리스 기본(SwiftShader)은 전투 중 프레임이 떨어져 100ms 단위 입력을 놓친다. GPU 를 켜면 60fps.
// 헤드리스는 GPU 를 켜도 대시·연계 같은 100ms 단위 입력을 놓친다(실측). 기본은 창을 띄운다. HEADLESS=1 이면 헤드리스.
const b = await chromium.launch({ headless: process.env.HEADLESS === '1', executablePath: process.env.PW_EXE || undefined, args: ['--enable-gpu', '--ignore-gpu-blocklist'] });
const ctx = await b.newContext({ viewport: { width: 1000, height: 700 } });
const p = await ctx.newPage();
const logs = []; let tail = 0; // 예외 뒤 스택 4줄도 같이 담는다
p.on('console', m => { const t = m.text(); if (/\[tech\]|loaded \/accs|Exception|Error/.test(t) && !NOISE.test(t)) { logs.push(t.slice(0, 200)); if (/Exception|Error/.test(t)) tail = 4; } else if (tail > 0 && /^\s*at /.test(t)) { logs.push(t.slice(0, 200)); tail--; } });
// 주의: NOISE 는 예외 '첫 줄' 에는 안 걸린다(스택에 ThrowItemAction 이 있음). 결과 정리에서 스택을 보고 거른다.
const battleStarted = () => logs.some(l => /loaded \/accs\/obj\//.test(l));
p.on('pageerror', e => logs.push('PAGEERROR ' + e.message));
// 키는 100ms 이상 눌러야 프레임 폴링에 잡힌다(modding-rules #8)
const tap = async (keys, ms) => { for (const k of keys) await p.keyboard.down(k); await p.waitForTimeout(ms); for (const k of [...keys].reverse()) await p.keyboard.up(k); };
const dashHold = async () => { await p.keyboard.down('ArrowRight'); await p.waitForTimeout(80); await p.keyboard.up('ArrowRight'); await p.waitForTimeout(80); await p.keyboard.down('ArrowRight'); await p.waitForTimeout(350); };
const shot = name => p.screenshot({ path: path.join(shots, `verify-${accs}-${name}.png`) });

await p.goto(`${base}?lang=ko&accs=${accs}&debug=1&preload=0&t=${Date.now()}`, { waitUntil: 'domcontentloaded' });
await p.waitForSelector('#game', { timeout: 120000 });
await p.waitForTimeout(9000);
// 타이틀 → 싱글 → 캐릭 → 전투: 고정 횟수가 아니라 악세 파츠(.prm) 로드 로그(전투 입장 시점)가 뜰 때까지 v 를 누른다
for (let i = 0; i < 24 && !battleStarted(); i++) { await tap(['v'], 120); await p.waitForTimeout(2500); }
if (!battleStarted()) { console.log(JSON.stringify({ accs, error: '전투 입장 실패(악세 파츠 로드 로그 없음)', logs: logs.slice(-10) })); await b.close(); process.exit(1); }
await p.waitForTimeout(9000); // 인트로
const start = logs.length;
// 필살기 3종 (발사체는 착탄까지 대기)
await p.keyboard.down('z'); await p.waitForTimeout(150); await tap(['x', 'c'], 250); await p.keyboard.up('z'); await p.waitForTimeout(600); await shot('ax'); await p.waitForTimeout(3000);
await tap(['v'], 120); await p.waitForTimeout(400); await tap(['x', 'c'], 250); await p.waitForTimeout(700); await shot('jx'); await p.waitForTimeout(3000);
await dashHold(); await tap(['x', 'c'], 250); await p.keyboard.up('ArrowRight'); await p.waitForTimeout(600); await shot('dx'); await p.waitForTimeout(3000);
// 기본 체인: 두 번째 입력은 1타 시작 후 ~100ms 안에 (modding-rules #19)
await tap(['c'], 120); await p.waitForTimeout(100); await tap(['c'], 120); await p.waitForTimeout(100); await tap(['c'], 120); await p.waitForTimeout(1500);
await tap(['x'], 120); await p.waitForTimeout(100); await tap(['x'], 120); await p.waitForTimeout(1800);
// 조준 C / X
await p.keyboard.down('z'); await p.waitForTimeout(150); await tap(['c'], 150); await p.waitForTimeout(1500); await tap(['x'], 150); await p.keyboard.up('z'); await p.waitForTimeout(2500);
// 점프 C / X
await tap(['v'], 120); await p.waitForTimeout(400); await tap(['c'], 150); await p.waitForTimeout(1500);
await tap(['v'], 120); await p.waitForTimeout(400); await tap(['x'], 150); await p.waitForTimeout(1500);
// 대시 C / X
await dashHold(); await tap(['c'], 150); await p.keyboard.up('ArrowRight'); await p.waitForTimeout(1600);
await dashHold(); await tap(['x'], 150); await p.keyboard.up('ArrowRight'); await p.waitForTimeout(3000);

const seen = () => { const techs = logs.slice(start).filter(l => l.includes('[tech]')); const a = techs.map(l => (l.match(/-> (\S+)/) || [])[1]).filter(s => s && s !== 'none'); const c = techs.map(l => (l.match(/chain from \S+ .*-> (\S+)/) || [])[1]).filter(Boolean); return [...new Set([...a, ...c])]; };
// 입력 타이밍 편차로 빠진 슬롯은 최대 2번 더 시도한다 (코드 문제와 타이밍 문제를 가르기 위해)
const RETRY = {
  W_ss: async () => { await tap(['c'], 120); await p.waitForTimeout(100); await tap(['c'], 120); await p.waitForTimeout(1500); },
  W_sss: async () => { await tap(['c'], 120); await p.waitForTimeout(100); await tap(['c'], 120); await p.waitForTimeout(100); await tap(['c'], 120); await p.waitForTimeout(1500); },
  W_bb: async () => { await tap(['x'], 120); await p.waitForTimeout(100); await tap(['x'], 120); await p.waitForTimeout(1800); },
  D_s: async () => { await dashHold(); await tap(['c'], 150); await p.keyboard.up('ArrowRight'); await p.waitForTimeout(1600); },
  D_b: async () => { await dashHold(); await tap(['x'], 150); await p.keyboard.up('ArrowRight'); await p.waitForTimeout(2500); },
  D_x: async () => { await dashHold(); await tap(['x', 'c'], 250); await p.keyboard.up('ArrowRight'); await p.waitForTimeout(3000); },
  J_s: async () => { await tap(['v'], 120); await p.waitForTimeout(400); await tap(['c'], 150); await p.waitForTimeout(1500); },
  J_b: async () => { await tap(['v'], 120); await p.waitForTimeout(400); await tap(['x'], 150); await p.waitForTimeout(1500); },
  J_x: async () => { await tap(['v'], 120); await p.waitForTimeout(400); await tap(['x', 'c'], 250); await p.waitForTimeout(3000); },
  A_s: async () => { await p.keyboard.down('z'); await p.waitForTimeout(150); await tap(['c'], 150); await p.keyboard.up('z'); await p.waitForTimeout(2000); },
  A_b: async () => { await p.keyboard.down('z'); await p.waitForTimeout(150); await tap(['x'], 150); await p.keyboard.up('z'); await p.waitForTimeout(2500); },
  A_x: async () => { await p.keyboard.down('z'); await p.waitForTimeout(150); await tap(['x', 'c'], 250); await p.keyboard.up('z'); await p.waitForTimeout(3000); },
  W_s: async () => { await tap(['c'], 120); await p.waitForTimeout(1200); },
  W_b: async () => { await tap(['x'], 120); await p.waitForTimeout(1500); },
};
const retried = [];
for (let round = 0; round < 2; round++) {
  const miss = EXPECT.filter(s => !seen().includes(s) && RETRY[s]);
  if (!miss.length) break;
  for (const m of miss) { retried.push(m); await RETRY[m](); }
}
const all = seen();
const out = {
  accs,
  loaded: logs.filter(l => l.includes('loaded')).map(l => l.replace(/.*loaded \/accs\//, '').replace(/ ->.*/, '')),
  slots: all,
  missing: EXPECT.filter(s => !all.includes(s)),
  errors: (() => { const L = logs.slice(start); const out = []; for (let i = 0; i < L.length; i++) { if (/Exception|Error/.test(L[i]) && !/^\s*at /.test(L[i])) { const st = L.slice(i + 1, i + 5).filter(x => /^\s*at /.test(x)); if (st.some(x => /ThrowItemAction|NPC\$/.test(x))) { i += st.length; continue; } out.push(L[i], ...st); i += st.length; } } return out.slice(0, 25); })(),
  retried,
  shots: ['ax', 'jx', 'dx'].map(n => path.join(shots, `verify-${accs}-${n}.png`)),
};
await ctx.close(); await b.close();
console.log(JSON.stringify(out, null, 1));
process.exit(out.errors.some(l => !/^\s*at /.test(l)) ? 1 : 0);
