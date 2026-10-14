#!/usr/bin/env python3
"""mods/accs/*.txt 스펙에서 악세 갤러리용 카탈로그(web/accs.json)를 만든다.
스펙 파일의 맨 위 주석 블록이 설명이다:
  첫 줄  "# 이름 — 한 줄 소개"           → name / summary
  "# tech <입력> <기술 이름> — <설명>"  → 기술 수(techs)만 넣는다. 목록은 공개하지 않는다(찾아보는 재미). docs/accs-convention.md 5절
  그 외 "# ..." 줄                     → detail (메모)
stat 줄은 그대로 모은다.
사용: gen_accs_catalog.py <mods/accs 디렉토리> <web/accs.json>"""
import glob, json, os, re, sys

src, out = sys.argv[1], sys.argv[2]
items = []
for path in sorted(glob.glob(os.path.join(src, "*.txt"))):
    accs_id = os.path.splitext(os.path.basename(path))[0]
    head, stats, techs = [], {}, []
    with open(path, encoding="utf-8") as f:
        lines = f.read().splitlines()
    for ln in lines:
        s = ln.strip()
        if not s:
            continue
        if s.startswith("#"):
            if stats:  # 맨 위 주석 블록만 설명으로 취급
                continue
            body = s.lstrip("#").strip()
            if body.startswith("tech "):
                m = re.match(r"tech\s+(\S+)\s+(.+?)\s+[—-]\s+(.+)$", body)
                if m:
                    techs.append({"input": m.group(1), "name": m.group(2).strip(), "desc": m.group(3).strip()})
                    continue
            head.append(body)
            continue
        parts = s.split()
        if parts[0] == "stat" and len(parts) >= 3:
            stats[parts[1]] = int(parts[2])
    name, summary = accs_id, ""
    if head:
        first = head[0]
        for sep in (" — ", " - ", ":"):
            if sep in first:
                name, summary = (x.strip() for x in first.split(sep, 1))
                break
        else:
            name = first
    items.append({
        "id": accs_id,
        "name": name,
        "summary": summary,
        "detail": " ".join(head[1:]),
        "stats": {k: v for k, v in stats.items() if v},
        "techs": len(techs),
    })
os.makedirs(os.path.dirname(out) or ".", exist_ok=True)
with open(out, "w", encoding="utf-8") as f:
    json.dump(items, f, ensure_ascii=False, indent=1)
print(f"accs.json: {len(items)} items")
