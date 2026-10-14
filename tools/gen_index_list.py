#!/usr/bin/env python3
"""정적 호스팅(Vercel)용 보조 파일 생성.
1) 디렉토리마다 index.list(이름 목록, 슬래시 없음) — CheerpJ 가 /app 디렉토리 목록을 <dir>/index.list 로 읽는다.
2) vercel.json — 디렉토리 URL(슬래시 없음) → 슬래시 URL 308 리다이렉트를 디렉토리별로 명시한다.
   (trailingSlash:true 는 확장자 없는 파일 conf/config 까지 디렉토리로 취급해 깨뜨린다 — 시행착오)
   /game/**/ 는 index.list 로 rewrite 해 디렉토리 stat 이 200 이 되게 한다.
사용: gen_index_list.py <web/game 디렉토리> [vercel.json 출력 경로]"""
import json, os, sys
root = sys.argv[1].rstrip("/")
out_json = sys.argv[2] if len(sys.argv) > 2 else None
web = os.path.dirname(root)  # web/
dirs = []
for d, subdirs, files in os.walk(root):
    names = sorted(x for x in subdirs + files if x != "index.list" and not x.startswith("."))
    with open(os.path.join(d, "index.list"), "w") as f:
        f.write("\n".join(names) + ("\n" if names else ""))
    dirs.append("/" + os.path.relpath(d, web).replace(os.sep, "/"))
print(f"index.list: {len(dirs)} dirs")
if out_json:
    cfg = {
        "outputDirectory": "web",
        "redirects": [{"source": p, "destination": p + "/", "permanent": True} for p in sorted(dirs)],
        "rewrites": [{"source": "/game/:path*/", "destination": "/game/:path*/index.list"}],
        # jar 와 악세 파일은 자주 바뀌므로 매번 ETag 재검증(304). 나머지 게임 리소스는 1시간 캐시.
        # Vercel 은 겹치는 규칙 중 뒤의 것이 이기므로 일반 규칙을 앞에, 구체 규칙을 뒤에 둔다.
        "headers": [
            {"source": "/game/(.*)", "headers": [{"key": "Cache-Control", "value": "public, max-age=3600"}]},
            {"source": "/game/(amped|patch)\\.jar", "headers": [{"key": "Cache-Control", "value": "public, max-age=0, must-revalidate"}]},
            {"source": "/game/resource/accs/(.*)", "headers": [{"key": "Cache-Control", "value": "public, max-age=0, must-revalidate"}]},
        ],
    }
    with open(out_json, "w") as f:
        json.dump(cfg, f, ensure_ascii=False, indent=1)
    print(f"vercel.json: {len(dirs)} redirects")
