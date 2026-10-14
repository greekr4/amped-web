# 모딩 규칙 (실측판) — 원본 1.0.12 · 이 저장소 기준

`modding-guide.md` 는 카페 강좌(팬 개조판 "패키지" 기준)를 정리한 것이고, 이 문서는 **이 저장소에서 직접 만들어 돌려 보며 확인한 것만** 적는다.
강좌와 다른 부분은 이 문서가 우선한다. 각 규칙 끝의 `[#n]` 은 아래 "시행착오 기록" 번호.

## 0. 한 장 요약

| 하고 싶은 것 | 만드는 것 | 도구 | 확인 방법 |
|---|---|---|---|
| 악세 외형·스탯 | `mods/accs/<이름>.txt` | `tools/accstool/AccsTool.java` (빌드가 자동 실행) | `?accs=<이름>` 으로 들어가 착용 확인 |
| 악세로 기술 교체·추가 | `mods/accs/<이름>/poa.txt` + `.sc` | `tools/poatool/PoaTool.java` (빌드가 자동 실행) | `?accs=<이름>&debug=1` → 콘솔 `[tech]` 로그 |
| 목록에 노출 | `game-overrides/.../NewSetting.java` 의 `EXTRA_ACCESSORIES` | 빌드 | 싱글 플레이엔 선택 UI 가 없으므로 `?accs=` 로만 장착 |

빌드: `./build.sh` (약 1분). 서버는 `web/` 를 그대로 서빙하므로 빌드 후 새로고침만 하면 된다.

## 1. 악세사리(.accs) 규칙

- `.accs` 는 `AccessoryData` 의 Java 직렬화다. 필드: `STR TEC SPD JMP DEF`(int, 기본 스탯에 **더해짐**), `parts`(Vector), `playerdata`(String, poa 경로 또는 null).
- 원본 11종은 전부 스탯 0·외형만이다. 스탯 있는 악세는 코드상 지원되지만(`UserData.getStatus` 가 더한다) 원본 데이터엔 없다.
- `parts` 한 줄 = `부위번호 모델경로 x y z sx sy sz rx ry rz(도)`. 모델은 `.prm`(Primitive) / `.shp` / `.oa`(ObjectData) 가 모두 붙는다(`PlayerEditPanel.AccessoryView.addAccessory` 가 instanceof 로 분기).
- 부위 번호(원본 accs 에서 실측): **0 오른손, 7 왼손, 6/12 손목(오/왼), 13 몸통(등)**. 나머지는 미확인.
- 왼손 파츠는 Y축 180도 회전(`0 180 0`)이 원본 관례(boxinggrab1).
- 악세 목록은 리소스가 아니라 **`NewSetting` 클래스 정적 블록에 하드코딩**되어 있다. 새 악세는 코드(오버라이드)를 고쳐야 목록에 뜬다. `[#2]`
- 싱글 플레이(외전)에는 악세 선택 UI 가 없다. 플레이어 에디트는 넷플레이 로비 전용이고 브라우저에선 로그인이 안 되므로 못 들어간다. 그래서 `?accs=<이름>` → `-accs` 인자 → `UserDataTable` 오버라이드가 외전 플레이어 5명에게 장착한다. `[#3]`

## 2. 기술(poa) 규칙 — 가장 중요한 것부터

1. **악세 `playerdata` 는 스타일 poa 를 "통째로 대체"한다. 병합이 아니다.** `UserData.getSourceName()` 이 악세 poa 가 있으면 그 경로를 돌려주고 끝이다. 강좌의 "악세 poa 기술이 원본보다 우선, 없으면 원본 기술" 은 패키지 얘기다. 그러므로 **반드시 스타일 poa 를 base 로 복사한 뒤 바꿔야** 하고(`PoaTool` 의 `base` 줄), 기술 하나만 담은 poa 를 물리면 나머지 기술이 전부 사라진다. `[#4]`
2. 그래서 한 악세 poa 는 한 스타일(예: KAKUTOU_M) 전용이 된다. 다른 스타일 캐릭터가 끼면 그 스타일의 기술이 격투가 기술로 바뀐다(애니메이션 프레임은 공용 `default.srt` 라 깨지진 않는다).
3. 기술(`Technique`) 필드: `name button(1=A 2=B 3=AB) status(비트) script prevtech from to`.
   - status 비트: `1 S 서있음, 2 W 걷기, 4 R 달리기(대시), 8 B 브레이크, 0x20 F 공중, 0x800 A 조준`. 원본 `W_b` 는 `SWA`(0x803), `D_b` 는 `R`, `J_b` 는 `F`.
   - 버튼: `c` 키 = A(2001, 약), `x` 키 = B(2002, 강), 동시 = AB. `v` = 점프/결정(2000). 설정은 `Config.Key.keyboard` 에 **문자 코드**(99,120,118)로 들어 있다.
   - 연계: `prevtech` + `from~to`(ms) 는 "이전 기술 발동 후 입력이 유효한 시간". 교체한 1타 스크립트가 `nextTech()` 를 불러야 2타가 나간다.
4. 타격정보(`Attack`): `attackmodel`(poa `resources` 키 `attack_s1.aoa` 또는 절대경로 `/attack/hadou.aoa`), `effect`(맞은 쪽 sc, 예 `hit_B5.sc`), `power`, `str`, `time`(ms), `x y z`, `pitch`, `feedback`(때린 쪽 sc, `!` 접두면 피격자 쪽에서 실행되는 듯—원본 `ranbuon` 이 `!ranbu.sc`). 
   - **디컴파일된 `PlayerData.newAttack()` 은 테이블에 넣지 않는다.** 직접 `attacks.put(name, attack)` 해야 한다. `[#5]`
5. 발사체의 **착탄 폭발은 aoa 안의 `end.sc` 가 정한다**: `attackNow(x,y,z, 반경, 위력, 피격sc, -1, false)`. 수류탄·포탄 원본은 반경 0.5·위력 8~10 으로 작다. 키우려면 poa 스펙의 `aoa <키> <바탕 aoa> end.sc <파일> attack.sc <파일>` 로 전용 사본을 만들어 `attack` 의 모델로 그 키를 쓴다(`attack.sc` 의 `setSize` 로 발사체 크기도 바뀜). 몸체 명중(`attack` 의 power)과 착탄 폭발(`attackNow` 의 위력)은 별개다.
6. 발사체는 `/attack/hadou.aoa` 처럼 `hasparent=false` + motion(z 0→4.05) 인 aoa 를 쓰면 된다. 이 aoa 는 `attack.sc`/`end.sc` 로 **자기 모델(hadou.shp)을 스스로 그린다**. 따로 `makeShapeModel` 로 이펙트를 만들 필요가 없다. `[#6]`
6. 공격 모델 `/effect/hadouken.aoa` 는 원본 poa 가 참조하지만 **파일이 없다**(hadouken 기술은 데드 데이터).

## 3. 스크립트(.sc) 규칙

- `.sc` 파일/poa 내장 스크립트는 `Script{source, prog}` 직렬화. `prog` 는 `Scp`(JavaCC 파서, **게임 jar 에 포함**)가 만든 바이트코드. `Scp.generate(String)` 으로 빌드 시 컴파일한다. 별도 툴 불필요.
- **명령 이름은 `#define 이름 번호` 로 정의되어야 컴파일된다.** 원본 정의 헤더는 리소스에 없다. 이 저장소는 컴파일된 스크립트 638개의 소스와 바이트코드를 대조해 60개 명령의 표를 복원했다(`tools/poatool/defines.sc`, 충돌 0건). `PoaTool` 이 자동으로 앞에 붙인다. `[#7]`
- 소스에 남길 때는 헤더 없이 본문만 저장한다(원본과 동일).
- **`dhit_*.sc` 는 피격이 아니라 사망 연출이다**(`dhit_freeze` = 얼린 뒤 `explode`+`die()`, `dhit_explode`/`dhit_burn` 도 같음). 타격의 `effect` 로 쓰면 위력과 무관하게 즉사한다. 보통 타격엔 `hit_*.sc` 만 쓴다. `[#15]`
- `hit_freeze.sc` 는 피격자에게 `ignoreAttack(2500)` 을 걸어 그 뒤 2.5초간 추가 타격이 들어가지 않는다. 다단 히트 기술에 빙결을 쓰면 첫 타만 들어간다(의도된 밸런스).
- 이펙트를 한 번에 많이 띄우지 말 것. `explode1/2.oa` 는 안에서 파티클(`p1.ptc`·`afterburn.ptc`)을 또 만든다. 발사체 5개 × 폭발 5개처럼 쌓이면 `GLEngine.display()` 에서 `ArrayIndexOutOfBoundsException` 이 수 초간 반복된다(렌더 버퍼 한계). 착탄 하나당 파티클 있는 이펙트는 1개, 나머지는 `crash`·`honoo`·`cold1` 같은 가벼운 것으로. `[#16]`
- **배포 방식**: `web/game/` 은 git 에 없다(저작권). Vercel 깃 자동 배포는 `vercel.json` 의 `git.deploymentEnabled.main=false` 로 꺼 두었고, 배포는 로컬에서 `./build.sh` 후 `vercel --prod --yes`. `.vercelignore` 는 `web/game/` 을 제외하지 않는다. `[#24]`
- **배포 캐시**: `vercel.json` 에서 `amped.jar`·`patch.jar`·`resource/accs/*` 는 `max-age=0, must-revalidate`(ETag 로 매번 확인), 나머지 게임 리소스는 1시간 캐시. 악세를 고쳐 배포했는데 브라우저에 옛 기술이 나오면 이 캐시가 원인이었다(강력 새로고침으로 확인). 규칙은 `tools/gen_index_list.py` 가 생성하므로 `vercel.json` 을 직접 고치면 빌드가 되돌린다. **Vercel 은 겹치는 `headers` 규칙 중 뒤의 것이 이기므로** 일반 규칙(`/game/(.*)`)을 앞에, 구체 규칙을 뒤에 둔다. `[#23]`
- **발사체 유도는 엔진에 없다. `game-overrides/.../world3d/obj/AttackModel.java` 가 넣은 기능이다.** aoa 의 `correctvec` 은 `Player.attack` 에서 발사 순간 조준 대상 쪽으로 초기 방향만 보정하고, 비행 중엔 `basem`(발사 시 행렬) 기준 고정 궤적(`getMotion(f)`)을 따른다. 오버라이드는 AttackData `resources` 의 `HOMING`(Float, 초당 이동량)을 보고 매 프레임 가장 가까운 살아 있는 적 쪽으로 누적 보정량(`homingOff`)을 더한다. poa 스펙 `aoa ... homing=9` + `attack` 의 `time` 1500(비행시간이 끝나면 그 자리에서 터지므로 넉넉히). `world.getPlayerModels()` 는 사람 플레이어만 돌려주므로(적 NPC 제외) `getAllModels()` 에서 `Player`(NPC 는 Player 의 하위) 를 골라야 한다. 디버그면 `[homing] target=...` 로그. `[#20]`
- **공중에서 떨어져 착지할 때 뭔가 하려면 시간으로 맞추지 말고 `landScript("x.sc")` 를 쓴다.** 점프 높이·지형에 따라 착지 시각이 달라져 시간 블록의 타격은 빗나간다. `landScript` 로 등록한 스크립트가 착지 순간 실행되고(원본 `hit_B5.sc` 의 `landScript("down.sc")` 와 같은 방식), 그 안에서 이펙트·타격·`recover()` 를 한다. 스크립트는 poa `script` 항목으로 같이 넣는다. `[#17]`
- **기술 스크립트 중 다른 기술 입력을 막으려면 `bind(ms)`.** 피격 스크립트가 쓰는 그 명령이다. 스크립트가 `jump()` 로 공중에 뜨면 상태가 F 가 되어 `J_x` 같은 공중 기술이 겹쳐 발동했는데, 첫 블록에 `bind(총 길이)` 를 넣으면 기술 조회 자체가 안 일어난다(`[tech]` 로그가 안 찍힘). `[#18]`
- 근접 타격의 판정 반경은 공격 모델의 `boundingboxies[].size`(`attack_s1` 0.2, `attack_s2/s3` 0.3). poa `aoa <키> attack_s2.aoa size=0.9` 로 키운 사본을 만들어 `attack` 모델로 쓴다.
- `true`/`false` 도 define(1/0)이다. 정의가 없으면 식별자(변수)로 컴파일되어 실행 시 `ScriptEngine.execute` 에서 NPE 가 난다(원본 `attackNow(...,-1,false)` 를 복사해 쓰다 발견). `[#14]`
- 발사체(AttackModel) 안에서 도는 `attack.sc`/`end.sc` 는 플레이어 스크립트가 아니다. `makeShapeModel` 의 hasparent 인자처럼 부모를 전제하는 호출은 피하고, 원본 aoa 스크립트에 있는 명령(`setSize` `startAnimation` `attackNow` `makeShapeModel` 6인자 `invisible` `kill` `play` `checkMask`)만 쓴다.
- 명령 번호대: 1~45 플레이어 동작(서버측 `Player.handle`), 80~84 스탯, 300 사운드, **10000번대는 뷰 전용**(`fadeinAnimation`·`startAnimation`·`makeShapeModel`·`changeFace`·`setShape`·`explode` 등 그리기/이펙트).
- `makeShapeModel(path, x,y,z, dx,dy,dz [,hasparent, parentParts])`: 뒤 세 개는 **방향 벡터**(unit 처리)다. `0,0,0` 을 주면 0벡터 정규화가 되어 위험하니 `0,0,1`(앞) 또는 `0,1,0`(위) 처럼 준다.
- `#이름(...)` 문법은 Java 리플렉션 호출(FUNC2, `Player` 의 public 메서드)로 컴파일된다. 컴파일은 되지만 **게임 안 동작은 미검증**(아래 #9 참고). 쓰지 말 것.
- 애니메이션 프레임 번호는 `default.srt` 공용. 원본 스크립트에서 가져다 쓸 것: 200~208 훅, 220~223 해머, 230~231 잽, 235~238 스트레이트, 245~248 블로우, 250~254 카운터, 275~277 장저, 360~364 대시킥, 380~385 돌진.
- 매 기술 마지막엔 `recover();`. 발동 중 `stop()`·`move(0,n)` 으로 전진량 조절. 원본 패턴 그대로 복사해서 숫자만 바꾸는 게 안전하다.

### 명령 표 (복원, 60개)
`addCaps(82) addHP(17) addMP(84) attack(30) attackNow(33) bind(10) bindAttack(13) changeBody(10019) changeFace(10007) changeHand(10008) changeStatus(81) checkMask(18) colorFadein(7) counterOn(37) die(32) doGuard(36) explode(10011) fadeinAnimation(2) findItem(25) flicker(10016) guard(31) ignoreAttack(15) imageSize(10018) invisible(10010) jump(22) kill(42) landScript(16) lockrecover(44) makeParticle(3) makeShapeModel(10013) makeShapeModel2(10021) makeSpectrum(4) move(20) movespd(23) nextTech(39) outline(10012) pickItem(26) play(300) recover(40) releaseHead(28) releaseItem(27) reverseCull(10015) setAnimation(10017) setBillBoard(10004) setBlendType(9) setBone(10014) setCaps(80) setColor(8) setLockable(19) setShape(5) setSize(10000) startAnimation(1) stop(29) texAnime(10005) throwItem(35) turn(21) unlockrecover(45) visible(10009)`

`move(n)` 은 1인자면 이동상태 바이트, 2인자면 `(방향, 속도)`, 3인자면 직선 이동 벡터. `jump(힘[, 중력])`. `ignoreAttack(ms)`. `landScript("x.sc")`. `addCaps/setCaps(STR,TEC,SPD,JMP,DEF)`.

## 4. 검증(테스트) 규칙 — 여기서 시간을 제일 많이 썼다

- **키는 100ms 이상 눌러라.** 공격·점프는 이벤트가 아니라 프레임마다 `isPressed` 를 폴링한다(`CtrlDevice.getUpdate`). Playwright 의 `press()` 는 수 ms 라 프레임 사이에 사라져 **아무 일도 안 일어난다**. `keyboard.down → 120ms → up`. 메뉴의 `v` 는 이벤트 기반이라 짧아도 된다. `[#8]`
- 대시는 같은 방향을 300ms 안에 두 번(두 번째는 유지). 그 상태(status=4)에서 B 를 눌러야 `D_b`.
- 입장 직후 몇 초는 인트로. 7~10초 기다린 뒤 조작. 가만히 있으면 적에게 맞아 죽고 배드엔드 화면으로 넘어가니 **한 번 로드에 한 번만 테스트**하고 다시 로드한다.
- **자동 검증은 `tools/verify_accs.mjs`(헤드리스, 창 안 뜸).** `PW_CORE=<playwright-core 경로> PW_EXE=<chrome-headless-shell 경로> node tools/verify_accs.mjs <accs id>` → 14슬롯 발동 여부(`slots`/`missing`), 예외(`errors`), 필살기 스크린샷 3장(`.shots/verify-<id>-{ax,jx,dx}.png`). 약 1분. Playwright MCP 브라우저는 창이 활성화되어 작업을 방해하므로 쓰지 않는다.
- 눈으로 판정하지 말 것. `?debug=1` 을 붙이면 `PlayerData` 오버라이드가 `[tech] button=.. status=.. -> 기술 (스크립트)` 를 콘솔에 찍는다. 이게 유일하게 확실한 증거다. 리소스 로드는 `[amped.web] loaded /accs/...` 로 확인.
- 콘솔에서 늘 나오는 잡음: `rpoint/lpoint.prm 없음`, `/app/game/resource (Is a directory)`, `dic_en.unicode 없음`, `NPC$ThrowItemAction NPE`, `target_want2.tex 없음`. 전부 원본 게임 내부 문제라 무시.

## 5. 빌드 스크립트 규칙

- `set -o pipefail` 상태에서 `java ... | grep -v` 처럼 파이프로 거르면 실패 원인이 안 보이고 빌드가 조용히 멈춘다. 도구 출력은 로그 파일로 받고 실패 시 꼬리를 보여 준다(`$TMP/poatool.log`). `[#10]`
- `Scp` 는 컴파일 과정(`GENCODE …`)을 stdout/stderr 에 모두 뿌린다. 거르려면 파일로 받아 `grep ' : techs='`.
- CFR 디컴파일 소스를 오버라이드로 쓸 때 컴파일 오류가 나는 전형: `compareTo(Object)` 에 Object 넘김(→ `String.valueOf`), `this.cache[c] = this.cacheCount++` 류 증감 위치, `newAttack` 처럼 부수효과가 빠진 메서드. 디컴파일 결과를 믿지 말고 **직렬화 데이터(원본 poa 덤프)와 맞춰 볼 것**.

## 6. 시행착오 기록

| # | 증상 | 원인 | 결론 |
|---|---|---|---|
| 1 | 빌드 후 `attacks=40`(추가한 3개가 없음) | 디컴파일된 `PlayerData.newAttack` 이 테이블에 안 넣음 | `attacks.put` 직접 호출 |
| 2 | 새 accs 파일을 넣어도 목록에 없음 | 목록이 `NewSetting` 정적 블록 하드코딩 | 오버라이드 + `EXTRA_ACCESSORIES` |
| 3 | 플레이어 에디트 화면을 못 찾음 | 넷플레이 로비(`MainMenuPanel`) 전용, 로그인 필요 | `?accs=` 로 외전 캐릭터에 직접 장착 |
| 4 | "악세 poa 에 기술 하나만" 으로 만들면 나머지 기술 소실 | `getSourceName` 이 poa 경로를 통째로 바꿈 | base 복사 후 수정 |
| 5 | (=1) | | |
| 6 | 발사체에 `makeShapeModel("/effect/hadoushot.oa",…,0,0,0)` 추가 | 불필요(aoa 가 자체 렌더) + 방향 0벡터 | 제거 |
| 7 | 새 `.sc` 컴파일 시 명령을 못 알아봄 | `#define` 표가 리소스에 없음 | 638개 스크립트 대조로 표 복원(`defines.sc`) |
| 8 | 기술이 "안 나가는 것처럼" 보임(수 회 재빌드) | Playwright `press()` 가 프레임 폴링보다 짧음 | 키를 120ms 유지. 코드 문제가 아니었음 |
| 9 | `#notifySendMessage("…")` 를 넣은 빌드에서 기술 안 나감 | 당시엔 #8 때문이었을 가능성이 큼. 리플렉션 호출 자체의 동작은 **미검증** | 문서에 "쓰지 말 것"으로 표기, 추후 `?debug=1` 로 재검증 가능 |
| 10 | 빌드가 [3/4] 에서 메시지 없이 종료 | `pipefail` + 파이프 grep | 로그 파일 방식으로 변경 |
| 11 | `Enter` 로 타이틀 메뉴 선택 안 됨 | 브라우저 keyCode 13 ≠ AWT VK_ENTER 10 | 결정은 `v`. (키 매핑 보정은 미적용) |
| 13 | 조준 중 C/X 가 `A_s/A_b` 대신 `W_s/W_b` 발동 | `W_s/W_b` 상태 `SWA` 가 먼저 매칭 | 상태를 `SW` 로 |
| 18 | 필살기 공중 구간에 XC 를 누르면 점프 필살기가 겹침 | `jump()` 후 상태 F 라 `J_x` 매칭 | 필살기 첫 블록 `bind(총 길이)` |
| 17 | 볼케이노 크래시 착지 폭발이 헛침 | 시간 블록(1400ms)과 실제 착지 시각 불일치 | `landScript("volcanoland.sc")` 로 착지 이벤트에서 타격 |
| 16 | 메테오 착탄 중 렌더 예외 반복 | 폭발 이펙트 25개(파티클 포함) 동시 생성 | 착탄당 `explode2` 1 + 가벼운 이펙트 3 |
| 15 | 얼음 장갑 필살기·어퍼가 위력과 무관하게 즉사 | `dhit_freeze.sc` 가 사망 연출 스크립트 | 전부 `hit_freeze.sc` 로 교체 |
| 14 | 전용 발사체 착탄 시 `ScriptEngine.execute` NPE, 게임 멈춤 | `false` 가 define 에 없어 변수 참조로 컴파일 | `defines.sc` 에 `true 1` `false 0` 추가 |
| 12 | 웹 키 상태 테이블에 문자 코드 추가(`KeelRuntimeWindowsImpl`) | 실제 판정은 `ComponentManager.keyStateTable`(이벤트의 keyChar)이라 이 수정은 원인과 무관 | 무해하므로 유지, 효과 없음으로 기록 |

## 7. 예제: 헤비 글러브

- `mods/accs/heavygloves.txt`: 양손 `boxergrub2.prm` ×1.5, STR+3 DEF+1 SPD-2 JMP-1, `playerdata /accs/heavygloves.poa`.
- `mods/accs/heavygloves/poa.txt`: base = 격투가 poa. 슬롯 배치는 `accs-convention.md` 규칙대로: `W_b` 헤비 스트레이트(`megapunch.sc`, 2타 + `kickback1.sc`), `D_b` 글러브 태클, `J_b` 헤비 킥, `A_s` 글러브 미사일(`/attack/hadou.aoa`), `A_b` 그라운드 스매시(앞·양옆 `pitch 60/300`), 필살기 `A_x` 기가 임팩트 / `J_x` 헤비 드롭(사방 4타) / `D_x` 폭주 러시(3연타 + 마무리). C 계열과 `W_bb`·`D_s`·`J_s` 는 원본 그대로(`W_s` 만 상태를 `SW` 로).
- 확인: `http://localhost:8777/?lang=ko&accs=heavygloves&debug=1` → 입장 10초 후 `x` 꾹 → 콘솔 `[tech] button=2 status=1 -> W_b (megapunch.sc)`; →→(유지) + `x` → `[tech] button=2 status=4 -> D_b (glovetackle.sc)`.
- `J_x`(점프 중 X+C, 상태 `F`)와 `D_x`(대시 중 X+C, 상태 `R`)는 원본 poa 에 없는 기술이라 악세 poa 가 새로 추가한다. 원본의 `W_x`(X+C = 가드, 상태 `SW`)와 상태가 겹치지 않아 순서 문제는 없다.

## 8. 예제 2: 볼케이노 건틀릿 (전 기술 교체) — `mods/accs/volcano*`

외형: 빨간 복싱 글러브(양손 ×1.35) + 양 손목 팔찌 ×1.3 + 등의 대형 방패 ×1.3 + 히어로 벨트 + 몸에 두른 `/effect/aura.shp`. 스탯 STR+4 TEC+1 JMP+1 DEF-2.

| 입력 | 기술 | 스크립트 | 핵심 재료 |
|---|---|---|---|
| C | 화염 잽 | `firejab.sc` | 230-231 프레임, `fire1.oa`, `hit_burn.sc` |
| C C | 화염 훅 | `firehook.sc` | 200-203, `prev W_s 40-420` |
| C C C | 폭염 어퍼 | `blazeupper.sc` | 410-412, `fcharge.ptc`, `explode1.oa`, `hit_B4.sc` |
| X | 폭발 스트레이트 | `bakustraight.sc` | 235-238, `crash.oa`+`explode2.oa`, 2타(`hit_burn`→`hit_explode`), `kickback1.sc` |
| X X | (원본 keriage) | – | 기본공격 규칙: 2타 연계는 원본 유지 |
| D+C | 불꽃 어깨치기 | `flameshoulder.sc` | 275-277, `honoo.oa` |
| D+X | 화염 돌진 | `blazerush.sc` | 380-385 돌진, `jet.oa`+`honoo.oa`, 2히트 |
| V+C | (원본 팔꿈치) | – | 점프 C 는 바꾸지 않는 관례 |
| V+X | 화염 낙하 킥 | `flamekick.sc` | 370-374, `move(0,-4,5)` |
| Z+C | 화염탄 | `fireball.sc` | `/attack/fireshot.aoa`(자체 렌더 유도 발사체) pow 12 |
| Z+X | 빅 레이저 | `biglaser.sc` | `/attack/biglaser.aoa`(가드 무시, 5.3유닛), `biglaser_fire.oa` |
| Z+XC | 볼케이노 크래시 | `volcanoburst.sc` | `move(0,1.6)`+`jump(11.0)` 전방 도약 → `move(0,-11,0.4)` 급강하 → 착지(`landScript`) `explode2`×2+`explode1`×4+`quake`+8방향 타격(반경 1.1, `y -0.3`) |
| V+XC | 메테오 스톰 | `meteor.sc` | `/attack/hgrenade.aoa`(포물선 5점 궤적, 자체 모델) `jump(3.5)` 로 띄운 뒤 5연발 `pitch 0/±25/±50`. 전용 `meteor.aoa`(poa `aoa` 항목): 수류탄 바탕, `setSize 1.8`, 착탄 `attackNow` 반경 1.5·위력 18 |
| D+XC | 이럽션 | `eruption.sc` | 220-223 해머 모션, `explode1/2.oa` 를 z 0.8→1.6→2.4 로 연달아, 타격 3개 |

확인 로그(전부 `?accs=volcano&debug=1` 에서 실측): `W_s→W_ss→W_sss`, `W_b→W_bb`, `A_s A_b A_x`, `J_s J_b J_x`, `D_s D_b D_x` 모두 `[tech]` 로 발동. 슬롯 배치 이유는 `accs-convention.md`.

추가로 배운 규칙:
- **기술 매칭은 목록 순서대로 첫 번째 일치**(`PlayerData.getTechnique`). `W_s` 의 상태에 `A`(조준)를 남겨 두면 조준 중 C 가 `A_s` 가 아니라 `W_s` 로 간다. 조준 전용 기술을 두려면 `W_s/W_b` 상태를 `SW` 로 줄여라. `[#13]`
- 조준(Z 유지)은 `status=2048`, 공중은 32, 대시는 4. 이 숫자가 `[tech]` 로그에 그대로 나온다.
- `.shp` 를 악세 파츠로 바로 붙일 수 있다(`/effect/aura.shp`). `.oa` 는 파츠로 붙여도 `start.sc` 가 안 돌아 모양이 없을 수 있으니(`hadoushot.oa` 는 스크립트에서 setShape) 파츠에는 `.prm/.shp` 를 쓴다.
- 폭발·화염 이펙트는 전부 `start.sc` 로 자기 모양을 만든다: `fire1 honoo explode1 explode2 crash spark jet biglaser_fire kiai_red`. `makeShapeModel` 한 줄이면 된다. 파티클은 `makeParticle("fcharge.ptc", x,y,z, dx,dy,dz)`.
- `/attack/fireshot.aoa`·`biglaser.aoa`·`hgrenade.aoa`·`bombshell.aoa` 는 모두 자체 모델이 있는 발사체. `time` 은 aoa 의 비행 시간에 맞춘다(fireshot 400, biglaser 800, hgrenade 800).
- 한 번의 로드에서 12개 기술을 다 테스트하려면 입력 사이 1~1.5초 간격, 체인은 250~300ms 간격. 공격을 많이 하면 적이 몰려와 죽으니 두 번에 나눠 찍었다.

## 10. 예제 4: 드론 컨트롤러 (전부 유도 드론 발사) — `mods/accs/dronecontroller*`

외형: 등(13) `rucksack3.prm` ×1.15(드론 도킹 배낭) + 양 손목(6/12) `magajinporch2.prm` ×0.9(조종 단말). 스탯 STR-1 TEC+3 SPD+1 DEF-1(합계 +2). 조준·필살기 5슬롯은 **전부 자동 추적 드론**(`homing=` 발사체, #20)을 내보내고, 차이는 드론 수·궤적·크기·위력으로 낸다. 피격은 `hit_shock.sc`(감전, 피격자 `ignoreAttack(1800)`), 소리는 `/sound/3.sr`.

전용 발사체(전부 `homing=9`, `time 1500` 으로 추적. `correctvec` 은 발사 시 조준 보정일 뿐이라 추적과 무관):

| 키 | 바탕 | 비행(attack.sc) | 착탄(end.sc) |
|---|---|---|---|
| `drone.aoa` | `bombshell.aoa`(포물선 유도, roketto 모델) | `setSize 1.6` + 원본 `startAnimation(10,13,13,8,0)` + 발사 순간 `spark.oa` | 반경 1.0·위력 10, `thunder`+`elec`+`spark` |
| `dronedart.aoa` | `fireshot.aoa`(직선 유도·correctvert) | 원본 attack.sc 복사(`setShape("fireball2.shp")`), `setSize 1.0` | 반경 0.9·위력 10, `elec`+`spark` |
| `dronesalvo.aoa` | `bombshell.aoa` | `setSize 1.8` | 반경 1.3·위력 12, `thunder`+`elec`×2+`spark` |
| `dronedrop.aoa` | `bombshell.aoa` | `setSize 2.0` | 반경 1.3·위력 14, `thunder`+`elec` |

| 입력 | 기술 | 스크립트 | 핵심 재료 |
|---|---|---|---|
| C / C→C / C→C→C | (원본) | – | `W_s` 상태만 `SW` |
| X | 드론 쇼크 블로우 | `shockblow.sc` | 원본 Rbblow 245-248 + `spark.oa`·`shock.oa`, 2타째 `hit_shock`. 원본의 `-1: findItem(...)` 블록은 뺐다(#19) |
| X→X | (원본 keriage) | – | 연계 유지 |
| D+C | 드론 어깨치기 | `droneshoulder.sc` | 원본 shoutei + `elec.oa`, `hit_shock` 10 |
| D+X | 드론 대시 킥 | `dronedashkick.sc` | 원본 Dgeri + `spark`·`flash`, `hit_B5` 11 + `kickback1.sc` |
| V+C | (원본 팔꿈치) | – | 관례 |
| V+X | 드론 낙하 킥 | `dronedropkick.sc` | 원본 Rntgeri + `elec`·`shock`, `hit_shock` 11 |
| Z+C | 드론 출격 | `dronelaunch.sc` | `dronedart.aoa` 1기 직선 유도, 몸체 12, `time 400` |
| Z+X | 드론 더블 출격 | `dronedouble.sc` | `drone.aoa` 2기 `pitch 20/340` 을 380/580ms 시간차, 몸체 10, `time 800` |
| Z+XC | 드론 일제 사격 | `dronesalvo.sc` | 115-118 충전 포즈 + `lcharge`, `ignoreAttack(1400)`+`bind(1400)`, `dronesalvo.aoa` 5기 `pitch 0/±25/±50` 를 400/520/640ms 에 |
| V+XC | 드론 공중 투하 | `dronedrop.sc` | `jump(3.5)` + `bind(1300)`, `dronedrop.aoa` 3기 `pitch 0/±20` 를 400/600ms 에 |
| D+XC | 드론 돌격 러시 | `dronerush.sc` | 380-385 돌진 `move(0,2.6)` + `bind(1500)`, `drone.aoa` 3기 `pitch 0/15/345` 를 350/550/750ms 에 연사 |

확인(`?accs=dronecontroller&debug=1`): `W_s W_ss W_b W_bb A_s A_b A_x J_s J_b J_x D_s D_b D_x` 전부 `[tech]`, 예외 0. 스크린샷 `.shots/drone-ax.png`(일제 사격 발사 직후) `drone-jx.png`(공중 투하) `drone-dx.png`(돌진 연사) `drone-as.png`(Z+C) `drone-ab.png`(Z+X).

추가로 배운 규칙:
- **전기 이펙트 자체 렌더 여부(AoaDump 로 확인)**: `shock.oa`(kaminari1, 작음 0.2) `elec.oa`(0.8) `epee_denki.oa`(1.0) `thunder.oa`(1.5, 안에서 `p1.ptc`·`afterburn.ptc` 파티클 생성 → "파티클 있는 이펙트 1개" 로 센다) `flash.oa` `spark.oa` 모두 `start.sc` 에 `setShape` 가 있어 그냥 띄우면 보인다.
- `hit_shock.sc` 는 `hit_freeze.sc` 처럼 피격자에게 `ignoreAttack(1800)` 을 건다. 감전 계열 다단 히트는 첫 타만 들어간다.
- **`setBlendType(ADD)` 의 `ADD`·`BLEND` 는 define 이 필요하다**(#22). `defines.sc` 에 `ADD 2` `BLEND 1` 을 추가했다(원본 fireshot.aoa·epee_denki.oa·thunder.oa·elec.oa 바이트코드와 대조해 동일 확인).
- `bombshell.aoa` 는 `view` 에 roketto(로켓) 모델이 있고 `attack.sc` 가 `startAnimation(10,13,13,8,0)` 만 한다. `setSize` 로 크기, `makeShapeModel`(6인자) 로 발사 순간 스파크를 붙일 수 있다. `fireshot.aoa` 는 `view=null` 이고 `attack.sc` 가 `setShape("fireball2.shp")` 로 그리므로 attack.sc 를 바꿀 때 그 줄을 **반드시 베껴야** 보인다.
- 한 기술에서 발사체 여러 발은 시간차(120~200ms)를 두면 유도라도 한 점에 겹치지 않고 연달아 착탄한다.
- AoaDump 는 `keelcls` 스텁 디렉토리를 **keel.jar 보다 앞에** 두어야 `keel_gl` 네이티브 로드 없이 돈다: `-cp "$S/scdump:$S/keelcls:.build/amped-classes:$G/keel.jar:$G/keelx.jar"`. 출력에 `use native: false` 가 찍히면 성공.
- zsh 에서 `echo =====` 는 `=cmd` 확장으로 실패한다(`===== not found`). 구분선은 따옴표로.

| # | 증상 | 원인 | 결론 |
|---|---|---|---|
| 20 | "유도" 발사체가 그냥 바닥에 떨어짐 | 엔진에 추적 없음(`correctvec` 은 발사 시 조준 보정뿐). 첫 구현은 `getPlayerModels()` 가 NPC 를 안 돌려줘 목표 없음 | `AttackModel` 오버라이드 + `homing=` 옵션, 목표는 `getAllModels()` 의 `Player` |
| 19 | X→X(`W_bb`) 체인이 안 나감(볼케이노 `W_bb`·`W_ss` 도 같은 하네스에서 안 나감) | **실측 원인은 입력 타이밍**: 체인 창은 40~600ms 이지만 브라우저 지연 ~100ms 가 붙어 두 번째 입력은 1타 시작 후 270~400ms 에 들어가야 잡혔다(`x 120ms → 100ms 대기 → x` 성공, `150ms → 300ms 대기` 실패). 의심 원인(미검증): 원본 `Rbblow.sc` 의 `-1: findItem(...)` 은 바닥에 아이템이 있으면 줍기(`anothercommand`)로 빠지고 `notifyAnotherAttack` 이 `combo=false` 로 만들어 연계를 끊을 수 있다 | 하네스 체인 간격을 100ms 로. 교체 스크립트에서는 `findItem` 블록을 빼 두었다. 1타·2타 체인이 안 찍히면 코드보다 이 둘을 먼저 본다 |
| 24 | 저장소를 공개로 바꾸면서 게임 에셋이 전 히스토리에 남음 | 첫 커밋부터 `web/game/` 포함 | `git filter-repo --path web/game/ --invert-paths` 후 force push, 배포는 CLI 로 전환 |
| 23 | 캐시 규칙을 넣었는데 프로덕션 헤더가 그대로 3600 | ① 빌드가 `vercel.json` 을 다시 생성해 덮어씀 ② Vercel 은 뒤 규칙이 이기는데 일반 규칙을 마지막에 둠 | 생성기에 규칙 추가 + 구체 규칙을 뒤로 |
| 22 | 드론 출격(fireshot 바탕 dart_fly.sc) 비행 중 `ScriptEngine.execute` NPE 가 `AttackModel.spendTime`·`ShapeModel.spendTime` 에서 반복 | 원본 attack.sc 를 베낀 `setBlendType(ADD)` 의 `ADD` 가 define 에 없어 변수 참조로 컴파일(#14 와 같은 유형). 60fps 헤드 브라우저에선 콘솔에 안 잡혔고 느린 헤드리스 셸에서만 드러났다 | `defines.sc` 에 `ADD 2` `BLEND 1` 추가. 원본 스크립트를 베낄 때 대문자 상수가 보이면 define 표부터 확인 |
| 21 | 헤드리스 검증(`tools/verify_accs.mjs`) 결과가 실행마다 다름(한 번에 7~11슬롯) | `chrome-headless-shell` 은 소프트웨어 GL 이라 3~25fps·전투 입장까지 40초 이상이고, 입장 후에도 적에게 맞는 동안 입력이 씹힌다. 풀 크로미움(`Google Chrome for Testing` 실행 파일을 `PW_EXE` 로) 은 헤드리스여도 Metal 로 50~70fps | 느리면 `PW_EXE` 를 풀 크로미움으로. 슬롯 누락은 2~3회 돌려 합집합으로 보고, errors 는 매 회 0 이어야 한다 |
