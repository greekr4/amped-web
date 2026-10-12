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
5. 발사체는 `/attack/hadou.aoa` 처럼 `hasparent=false` + motion(z 0→4.05) 인 aoa 를 쓰면 된다. 이 aoa 는 `attack.sc`/`end.sc` 로 **자기 모델(hadou.shp)을 스스로 그린다**. 따로 `makeShapeModel` 로 이펙트를 만들 필요가 없다. `[#6]`
6. 공격 모델 `/effect/hadouken.aoa` 는 원본 poa 가 참조하지만 **파일이 없다**(hadouken 기술은 데드 데이터).

## 3. 스크립트(.sc) 규칙

- `.sc` 파일/poa 내장 스크립트는 `Script{source, prog}` 직렬화. `prog` 는 `Scp`(JavaCC 파서, **게임 jar 에 포함**)가 만든 바이트코드. `Scp.generate(String)` 으로 빌드 시 컴파일한다. 별도 툴 불필요.
- **명령 이름은 `#define 이름 번호` 로 정의되어야 컴파일된다.** 원본 정의 헤더는 리소스에 없다. 이 저장소는 컴파일된 스크립트 638개의 소스와 바이트코드를 대조해 60개 명령의 표를 복원했다(`tools/poatool/defines.sc`, 충돌 0건). `PoaTool` 이 자동으로 앞에 붙인다. `[#7]`
- 소스에 남길 때는 헤더 없이 본문만 저장한다(원본과 동일).
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
| 12 | 웹 키 상태 테이블에 문자 코드 추가(`KeelRuntimeWindowsImpl`) | 실제 판정은 `ComponentManager.keyStateTable`(이벤트의 keyChar)이라 이 수정은 원인과 무관 | 무해하므로 유지, 효과 없음으로 기록 |

## 7. 예제: 헤비 글러브

- `mods/accs/heavygloves.txt`: 양손 `boxergrub2.prm` ×1.5, STR+3 DEF+1 SPD-2 JMP-1, `playerdata /accs/heavygloves.poa`.
- `mods/accs/heavygloves/poa.txt`: base = 격투가 poa. `W_b`(서서 강공격) → `megapunch.sc`(2타, 파란 충전 이펙트 + 플래시, 두 번째 타격 `hit_B6.sc` + 피드백 `kickback1.sc`). `D_b`(대시 강공격) → `glovemissile.sc`(`/attack/hadou.aoa` 파동, pow 12).
- 확인: `http://localhost:8777/?lang=ko&accs=heavygloves&debug=1` → 입장 10초 후 `x` 꾹 → 콘솔 `[tech] button=2 status=1 -> W_b (megapunch.sc)`; →→(유지) + `x` → `[tech] button=2 status=4 -> D_b (glovemissile.sc)`.

## 8. 예제 2: 볼케이노 건틀릿 (전 기술 교체) — `mods/accs/volcano*`

외형: 빨간 복싱 글러브(양손 ×1.35) + 양 손목 팔찌 ×1.3 + 등의 대형 방패 ×1.3 + 히어로 벨트 + 몸에 두른 `/effect/aura.shp`. 스탯 STR+4 TEC+1 JMP+1 DEF-2.

| 입력 | 기술 | 스크립트 | 핵심 재료 |
|---|---|---|---|
| C | 화염 잽 | `firejab.sc` | 230-231 프레임, `fire1.oa`, `hit_burn.sc` |
| C C | 화염 훅 | `firehook.sc` | 200-203, `prev W_s 40-420` |
| C C C | 폭염 어퍼 | `blazeupper.sc` | 410-412, `fcharge.ptc`, `explode1.oa`, `hit_B4.sc` |
| X | 폭발 스트레이트 | `bakustraight.sc` | 235-238, `crash.oa`+`explode2.oa`, 2타(`hit_burn`→`hit_explode`), `kickback1.sc` |
| X X | 화염 돌진 | `blazerush.sc` | 380-385 돌진, `jet.oa`+`honoo.oa`, 2히트 |
| 대시 C | 불꽃 어깨치기 | `flameshoulder.sc` | 275-277, `honoo.oa` |
| 대시 X | 파이어샷 | `fireshot.sc` | `/attack/fireshot.aoa`(자체 렌더 발사체, 유도 `correctvec`) |
| Z+C | 화염탄 | `fireball.sc` | `/attack/fireshot.aoa` pow 10 |
| Z+X | 빅 레이저 | `biglaser.sc` | `/attack/biglaser.aoa`(가드 무시, 5.3유닛), `biglaser_fire.oa` |
| Z+X+C | 볼케이노 버스트 | `volcanoburst.sc` | `ignoreAttack(900)`, 사방 `explode1/2.oa`, 전후 타격 2개(`pitch 180`) |
| 점프 C | 화염 낙하 킥 | `flamekick.sc` | 370-374, `move(0,-4,5)` |
| 점프 X | 메테오 | `meteor.sc` | `/attack/hgrenade.aoa`(포물선 5점 궤적, 자체 모델) |

확인 로그(전부 `?accs=volcano&debug=1` 에서 실측): `W_s→W_ss→W_sss`, `W_b→W_bb`, `A_s A_b A_x`, `J_s J_b`, `D_s D_b` 모두 `[tech]` 로 발동.

추가로 배운 규칙:
- **기술 매칭은 목록 순서대로 첫 번째 일치**(`PlayerData.getTechnique`). `W_s` 의 상태에 `A`(조준)를 남겨 두면 조준 중 C 가 `A_s` 가 아니라 `W_s` 로 간다. 조준 전용 기술을 두려면 `W_s/W_b` 상태를 `SW` 로 줄여라. `[#13]`
- 조준(Z 유지)은 `status=2048`, 공중은 32, 대시는 4. 이 숫자가 `[tech]` 로그에 그대로 나온다.
- `.shp` 를 악세 파츠로 바로 붙일 수 있다(`/effect/aura.shp`). `.oa` 는 파츠로 붙여도 `start.sc` 가 안 돌아 모양이 없을 수 있으니(`hadoushot.oa` 는 스크립트에서 setShape) 파츠에는 `.prm/.shp` 를 쓴다.
- 폭발·화염 이펙트는 전부 `start.sc` 로 자기 모양을 만든다: `fire1 honoo explode1 explode2 crash spark jet biglaser_fire kiai_red`. `makeShapeModel` 한 줄이면 된다. 파티클은 `makeParticle("fcharge.ptc", x,y,z, dx,dy,dz)`.
- `/attack/fireshot.aoa`·`biglaser.aoa`·`hgrenade.aoa`·`bombshell.aoa` 는 모두 자체 모델이 있는 발사체. `time` 은 aoa 의 비행 시간에 맞춘다(fireshot 400, biglaser 800, hgrenade 800).
- 한 번의 로드에서 12개 기술을 다 테스트하려면 입력 사이 1~1.5초 간격, 체인은 250~300ms 간격. 공격을 많이 하면 적이 몰려와 죽으니 두 번에 나눠 찍었다.
