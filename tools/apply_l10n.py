#!/usr/bin/env python3
"""한글 패치 적용.
  apply_l10n.py classes <classes_dir>   : 게임 클래스 상수풀의 일본어 문자열을 ko.tsv 로 치환
  apply_l10n.py resources <resource_dir>: 직렬화 리소스(.md/.npc/.ioa 등)의 TC_STRING 레코드를 치환
ko.tsv: <일본어>\t<한국어> (리터럴 \\n 은 개행으로 해석)
"""
import os, struct, sys

HERE = os.path.dirname(os.path.abspath(__file__))

def load_map():
    m = {}
    for line in open(os.path.join(HERE, '..', 'l10n', 'ko.tsv'), encoding='utf-8'):
        line = line.rstrip('\n')
        if not line or '\t' not in line: continue
        ja, ko = line.split('\t', 1)
        m[ja.replace('\\n', '\n')] = ko.replace('\\n', '\n')
    return m

def mutf8_decode(b):
    # modified UTF-8: \xc0\x80 → NUL. 리소스/상수풀 문자열은 대부분 표준 UTF-8 과 같다
    return b.replace(b'\xc0\x80', b'\x00').decode('utf-8')
def mutf8_encode(s):
    return s.encode('utf-8').replace(b'\x00', b'\xc0\x80')

def patch_class(path, m):
    data = open(path, 'rb').read()
    if data[:4] != b'\xca\xfe\xba\xbe': return 0
    cp_count = struct.unpack('>H', data[8:10])[0]
    out = bytearray(data[:10]); pos = 10; changed = 0; i = 1
    while i < cp_count:
        tag = data[pos]
        if tag == 1:
            n = struct.unpack('>H', data[pos+1:pos+3])[0]
            raw = data[pos+3:pos+3+n]
            try: s = mutf8_decode(raw)
            except UnicodeDecodeError: s = None
            if s is not None and s in m:
                enc = mutf8_encode(m[s]); out += b'\x01' + struct.pack('>H', len(enc)) + enc; changed += 1
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

def patch_resource(path, m):
    b = open(path, 'rb').read()
    if b[:2] != b'\xac\xed': return 0
    out = bytearray(); i = 0; changed = 0
    while i < len(b):
        if b[i] == 0x74 and i + 3 <= len(b):
            n = struct.unpack('>H', b[i+1:i+3])[0]
            if 0 < n < 400 and i + 3 + n <= len(b):
                raw = b[i+3:i+3+n]
                try: s = mutf8_decode(raw)
                except UnicodeDecodeError: s = None
                if s is not None and s in m:
                    enc = mutf8_encode(m[s]); out += b'\x74' + struct.pack('>H', len(enc)) + enc
                    i += 3 + n; changed += 1; continue
        out.append(b[i]); i += 1
    if changed: open(path, 'wb').write(out)
    return changed

def main():
    mode, root = sys.argv[1], sys.argv[2]
    m = load_map(); total = 0; files = 0
    for dp, _, fs in os.walk(root):
        for f in fs:
            p = os.path.join(dp, f)
            if mode == 'classes' and (f.endswith('.class') or f.endswith('.clazz')):
                c = patch_class(p, m)
            elif mode == 'resources':
                c = patch_resource(p, m)
            else: c = 0
            if c: total += c; files += 1
    print(f'l10n {mode}: {total} strings in {files} files')

if __name__ == '__main__':
    main()
