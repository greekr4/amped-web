#!/bin/bash
# 싱글 겟앰프드 웹(CheerpJ) 빌드: 패치 jar + 복호화 amped.jar + 에셋 → web/game/
set -euo pipefail
HERE="$(cd "$(dirname "$0")" && pwd)"
GAME="${1:-/Users/tk/Downloads/getamped 1.0.12}"
JAVAC=/opt/homebrew/opt/openjdk/bin/javac; JAR=/opt/homebrew/opt/openjdk/bin/jar; JAVA=/opt/homebrew/opt/openjdk/bin/java
OUT="$HERE/web/game"; TMP="$HERE/.build"; rm -rf "$TMP"; mkdir -p "$TMP/patch" "$TMP/amped" "$OUT"

echo "[1/4] 패치 클래스 컴파일"
CP="$GAME/keel.jar:$GAME/keelx.jar:$GAME/png.jar"
find "$HERE/patch/src" -name '*.java' > "$TMP/sources.txt"
"$JAVAC" --release 8 -nowarn -Xlint:none -encoding UTF-8 -cp "$CP" -d "$TMP/patch" @"$TMP/sources.txt"
(cd "$TMP/patch" && "$JAR" cfM "$OUT/patch.jar" .)

echo "[2/4] amped.jar 복호화 + 경로 상수 패치"
mkdir -p "$TMP/clazz" && (cd "$TMP/clazz" && unzip -oq "$GAME/amped.jar")
python3 -I "$HERE/tools/declazz.py" "$TMP/clazz" "$TMP/amped" >/dev/null
cp -R "$TMP/amped" "$TMP/amped-classes"   # .class 상태 사본 (TexTool 컴파일용)
# 게임 클래스 오버라이드(디컴파일 소스 수정본) 컴파일 → 복호화 클래스 위에 덮어쓴다
if [ -d "$HERE/game-overrides" ]; then
  find "$HERE/game-overrides" -name '*.java' > "$TMP/ov.txt"
  "$JAVAC" --release 8 -nowarn -Xlint:none -encoding UTF-8 -cp "$TMP/amped:$TMP/patch:$GAME/keel.jar:$GAME/keelx.jar:$GAME/png.jar" -d "$TMP/amped" @"$TMP/ov.txt"
fi
# .class → .clazz 로 되돌려 CodedClassLoader(교체판)가 그대로 찾게 한다
find "$TMP/amped" -name '*.class' | while read f; do mv "$f" "${f%.class}.clazz"; done
python3 -I "$HERE/tools/patch_strings.py" "$TMP/amped"
cp -R "$TMP/clazz/META-INF" "$TMP/amped/" 2>/dev/null || true
(cd "$TMP/amped" && "$JAR" cfM "$OUT/amped.jar" .)

echo "[3/4] 에셋/런타임 jar 복사"
cp "$GAME/keel.jar" "$GAME/keelx.jar" "$GAME/png.jar" "$OUT/"
rsync -a --delete "$GAME/resource/" "$OUT/resource/"
# 추가 악세사리: mods/accs/*.txt → resource/accs/*.accs (NewSetting 오버라이드의 EXTRA_ACCESSORIES 와 짝)
"$JAVAC" -nowarn -cp "$TMP/amped-classes" -d "$TMP/accstool" "$HERE/tools/accstool/AccsTool.java"
"$JAVAC" -nowarn -cp "$TMP/amped-classes" -d "$TMP/poatool" "$HERE/tools/poatool/PoaTool.java"
for spec in "$HERE"/mods/accs/*.txt; do
  [ -f "$spec" ] || continue
  name=$(basename "$spec" .txt)
  # 기술(poa): mods/accs/<name>/poa.txt 가 있으면 먼저 생성 (악세 스펙의 playerdata 가 가리킨다)
  if [ -f "$HERE/mods/accs/$name/poa.txt" ]; then
    (cd "$HERE" && "$JAVA" -Dpoatool.defines="$HERE/tools/poatool/defines.sc" -cp "$TMP/poatool:$TMP/amped-classes:$GAME/keel.jar:$GAME/keelx.jar" PoaTool make "$HERE/mods/accs/$name/poa.txt" "$OUT/resource/accs/$name.poa" > "$TMP/poatool.log" 2>&1) || { echo "PoaTool 실패:"; grep -v '^GENCODE\|^PSH\|^FUNC\|^EXEC\|^POP\|^LBL\|^JMP\|^JPF\|^CAL' "$TMP/poatool.log" | tail -5; exit 1; }
    grep ' : techs=' "$TMP/poatool.log"
  fi
  "$JAVA" -cp "$TMP/accstool:$TMP/amped-classes" AccsTool make "$spec" "$OUT/resource/accs/$name.accs" | head -1
done
# 런타임 i18n 자료: 번역표 + 언어별 텍스처 (베이스 리소스는 원문 그대로)
rm -rf "$OUT/l10n"; mkdir -p "$OUT/l10n"
"$JAVAC" -nowarn -cp "$TMP/amped-classes:$GAME/keel.jar:$GAME/keelx.jar" -d "$TMP/textool" "$HERE/tools/textool/TexTool.java" 2>/dev/null || mkdir -p "$TMP/textool"
for tsv in "$HERE"/l10n/[a-z][a-z].tsv; do
  lang=$(basename "$tsv" .tsv); cp "$tsv" "$OUT/l10n/$lang.tsv"
  if [ -f "$HERE/l10n/tex_$lang.tsv" ]; then
    mkdir -p "$OUT/l10n/$lang/resource"
    (cd "$HERE" && "$JAVA" -cp "$TMP/textool:$TMP/amped-classes:$GAME/keel.jar:$GAME/keelx.jar" TexTool batch "$HERE/l10n/tex_$lang.tsv" "$OUT/resource" "$OUT/l10n/$lang/resource" | tail -1)
    # 덮어쓰기 파일 목록 → 런타임이 파일마다 존재 여부를 HTTP 로 묻지 않게 (I18n.override)
    (cd "$OUT/l10n/$lang/resource" && find . -type f ! -name index.list | sed 's|^\.||' | sort > "$OUT/l10n/$lang/files.txt")
  fi
  echo "l10n: $lang"
done
# 정적 호스팅용 디렉토리 목록 파일 (Vercel 등; 로컬 Rust 서버는 없어도 동적으로 만든다)
python3 -I "$HERE/tools/gen_index_list.py" "$OUT" "$HERE/vercel.json"
python3 -I "$HERE/tools/gen_accs_catalog.py" "$HERE/mods/accs" "$HERE/web/accs.json"
echo "[4/4] 완료: $OUT"; du -sh "$OUT" | tail -1
