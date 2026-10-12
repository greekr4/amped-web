#!/usr/bin/env python3
"""클래스 파일 상수풀의 CONSTANT_Utf8 문자열을 치환한다(상대 경로 → /app/game 절대 경로).
사용: patch_strings.py <classes_dir>
"""
import os, struct, sys

BASE = '/app/game/'
def remap(s: str, cls: str):
    if s in ('conf/setting.set', 'conf/config', 'conf/system.conf'): return BASE + s
    if s.startswith('resource/') or s == 'resource': return BASE + s
    if s.startswith('music/') or s == 'music': return BASE + s
    if s == '.' and cls.endswith('com/cyberstep/ring/client/Game.class'): return '/app/game'
    return s

def patch(path):
    data = open(path, 'rb').read()
    if data[:4] != b'\xca\xfe\xba\xbe': return 0
    cp_count = struct.unpack('>H', data[8:10])[0]
    out = bytearray(data[:10]); pos = 10; changed = 0; i = 1
    while i < cp_count:
        tag = data[pos]
        if tag == 1:
            n = struct.unpack('>H', data[pos+1:pos+3])[0]
            raw = data[pos+3:pos+3+n]
            try: s = raw.decode('utf-8')
            except UnicodeDecodeError: s = None
            ns = remap(s, path) if s is not None else None
            if ns is not None and ns != s:
                enc = ns.encode('utf-8'); out += b'\x01' + struct.pack('>H', len(enc)) + enc; changed += 1
            else:
                out += data[pos:pos+3+n]
            pos += 3 + n
        else:
            size = {3:5,4:5,5:9,6:9,7:3,8:3,9:5,10:5,11:5,12:5,15:4,16:3,18:5}[tag]
            out += data[pos:pos+size]; pos += size
            if tag in (5, 6): i += 1
        i += 1
    out += data[pos:]
    if changed: open(path, 'wb').write(out)
    return changed

total = 0
for root, _, files in os.walk(sys.argv[1]):
    for f in files:
        if f.endswith('.class') or f.endswith('.clazz'):
            total += patch(os.path.join(root, f))
print('patched strings:', total)
