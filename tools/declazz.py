#!/usr/bin/env python3
"""GetAmped .clazz -> .class 복호화 (keel_clazz.dll 리버싱 결과).
포맷: [0:4]=magic 8b4b2a99?, [4:8]=len^r1 (BE), [8:12]=MT seed (BE), 이후 역순 XOR 스트림.
MT19937 1998년판(sgenrand: LCG 69069, 상위16비트 조합).
"""
import sys, os, struct

N, M = 624, 397
MATRIX_A, UPPER, LOWER = 0x9908b0df, 0x80000000, 0x7fffffff

class MT:
    def __init__(self, seed):
        self.mt = [0]*N
        for i in range(N):
            self.mt[i] = seed & 0xffff0000
            seed = (69069*seed + 1) & 0xffffffff
            self.mt[i] |= (seed & 0xffff0000) >> 16
            seed = (69069*seed + 1) & 0xffffffff
        self.mti = N
    def genrand(self):
        mt = self.mt
        if self.mti >= N:
            for kk in range(N):
                y = (mt[kk] & UPPER) | (mt[(kk+1) % N] & LOWER)
                mt[kk] = mt[(kk+M) % N] ^ (y >> 1) ^ (MATRIX_A if y & 1 else 0)
            self.mti = 0
        y = mt[self.mti]; self.mti += 1
        y ^= y >> 11
        y ^= (y << 7) & 0x9d2c5680
        y ^= (y << 15) & 0xefc60000
        y ^= y >> 18
        return y & 0xffffffff

def decode(buf: bytes) -> bytes:
    n = len(buf)
    seed = struct.unpack('>I', buf[8:12])[0]
    mt = MT(seed)
    r = mt.genrand()
    outlen = (struct.unpack('>I', buf[4:8])[0] ^ r) & 0xffffffff
    if outlen > n:
        raise ValueError(f'bad length {outlen} > {n}')
    out = bytearray(outlen)
    for i in range(outlen):
        sh = (i & 3) * 8
        if sh == 0:
            r = mt.genrand()
        out[i] = ((r >> sh) & 0xff) ^ buf[n-1-i]
    return bytes(out)

def main():
    src, dst = sys.argv[1], sys.argv[2]
    ok = bad = 0
    for root, _, files in os.walk(src):
        for f in files:
            if not f.endswith('.clazz'): continue
            p = os.path.join(root, f)
            rel = os.path.relpath(p, src)[:-6] + '.class'
            try:
                data = decode(open(p,'rb').read())
                if data[:4] != b'\xca\xfe\xba\xbe':
                    raise ValueError(f'magic {data[:4].hex()}')
                o = os.path.join(dst, rel); os.makedirs(os.path.dirname(o), exist_ok=True)
                open(o,'wb').write(data); ok += 1
            except Exception as e:
                bad += 1; print('FAIL', rel, e)
    print(f'ok={ok} bad={bad}')

if __name__ == '__main__':
    main()
