# 싱글 겟앰프드 모딩 가이드 (악세·기술·스크립트·맵)

출처: 네이버 카페 "싱글 겟앰프드 마스터 :: The S.G.M." 패키지 강좌 게시판(menu 154) 44편을 읽고 정리.
원글 작성자는 카와이, 프로필, 텔레토비, Celina, 위닝씨, 슈퍼맨, 타락의 다크니스, 에바, Kyubum 등(2010~2013).

> **전제 구분**: 강좌 대부분은 카페 매니저 핵매니아가 만든 **"패키지"**(싱겟 개조판 + Resource Editor)를 기준으로 한다.
> 이 저장소(amped-web)가 돌리는 것은 **원본 1.0.12** 라서 패키지 전용 기능(od/hm/exconfig.conf, 추가 명령어 일부)은 그대로 안 통한다.
> 아래에서 `[패키지]` 표시는 패키지 전용, `[원본]` 표시는 원본에도 해당하는 내용이다. 파일 포맷과 스크립트 언어의 뼈대는 둘 다 같다.

---

## 1. 리소스 구조 (`resource/`) `[원본]`

| 폴더 | 내용 | 포맷 |
|---|---|---|
| `player/style/<직업>/` | 플레이어 데이터(`player.poa`), 외형(머리·얼굴·몸 텍스처, 프리뷰) | poa, tex, prm |
| `player/common/` | 플레이어 기술·히트 스크립트 | sc |
| `player/dekachara/` | 빽쉴드(deka) 효과 플레이어용 스크립트 | sc |
| `player/action/` | 아이템의 플레이어 데이터 | poa |
| `player/anime/` | 모션(스트럭처) | srt |
| `player/body/`, `player/face/` | 몸·얼굴 형상(텍스처 제외) | shp, prm |
| `player/npc/`, `player/special/` | NPC 외형 / NPC 플레이어 데이터 | npc, poa |
| `map/` | 맵 데이터 | md |
| `item/` | 아이템 | ioa |
| `attack/` | 어택(탄·레이저 등 판정 모델) | aoa |
| `effect/` | 이펙트 오브젝트 | oa |
| `sound/` | 효과음 | sr |
| `accs/` | **원본 악세사리**(`AccessoryData` 직렬화) | accs |
| `outfit/` | `[패키지]` 악세사리(od) + 하위 obj/player/srt | od, oa, poa, srt |

직업 폴더 이름: KAKUTOUM/F(격투가 남/여), ARMYW/B(군인 백/흑), SPYW/B(스파이), SUPERMANY/B(초인), ARMORW/B(갑옷), ESPERW/B(에스퍼).

스크립트 탐색 우선순위: **poa 에 내장된 sc > `player/common/`** (deka 플레이어는 `dekachara/` 가 common 보다 우선). 경로 없이 이름만 쓰면 common 에서 찾는다. 히트(맞았을 때) 스크립트는 반드시 common 에 두고 이름만 적는다.

모든 리소스 파일은 **Java 직렬화 객체**다(`aced 0005`). 텍스처는 `Texture`(Externalizable, Deflate 압축 픽셀), 스크립트는 `Script{Prog}`(SJIS 문자열 풀 + opcode 배열). 이 저장소의 `tools/textool/TexTool.java` 가 텍스처를, `tools/apply_l10n.py`/`I18n` 이 문자열을 다룬다.

---

## 2. 악세사리 만들기

### 2-1. 원본 1.0.12 의 `.accs` `[원본]`
`AccessoryData` 직렬화. 필드 순서: `DEF, JMP, SPD, STR, TEC (int×5)` → `parts (Vector)` → `playerdata (String)`.
기존 파일을 템플릿으로 능력치만 바꾸는 것이 가장 안전하다(`../amped-emu/mcp/accessory.py` 참고). 기본 12종: backshield1/2, boxinggrab1/2, bracelet1, herobelt1/2, porch1/2 ….

### 2-2. 패키지의 Outfit(`.od`) `[패키지]`
필수 파일: **`.od`**(외형·능력치) + **`.poa`**(기술). od 만으로는 기술이 안 바뀐다.

**od Basic**
- Name: 악세 이름(한글 가능)
- PlayerData: 이 악세의 poa 경로(`/outfit/내이니셜/xxx.poa`)
- STR/TEC/SPD/JMP/DEF: 기본 스탯에 **더해지는** 값(음수면 감소). 패키지 버그로 -1 이 -2~3 깎일 때가 있음
- MP: 착용 시 초기 MP (1줄 = 50, 안 쓰면 1)
- is deka: 빽쉴드 효과 / no item: 아이템 줍기 금지
- Preview: 32×32 아이콘(없으면 손 모양) / Description: 한 줄 설명(제작자 적기)

**od Parts** (Add/Modify/Remove)
- Name(L/R 등), **Part 번호 1~16 = 캐릭터 부위**. 오른손 = 0 또는 15(15 는 아이템 파츠), 왼손 = 7
- Object: 붙일 모델(oa/shp/prm), Location/Size/Rotation(XYZ)
- Start Script: 오브젝트에 `start.sc` 가 있으면 파츠 적용 시 발동
- 상태: NORMAL / BLEND(반투명, Alpha 조절) / ADD(발광)

**파일명 규칙**: outfit 폴더는 모두가 공유하므로 `이니셜_이름.od`, `이니셜_이름.oa` 처럼 **자기만의 접두사**를 붙이고 개인 폴더(`outfit/내이니셜/`)에 모은다. 충돌하면 다른 작품이 깨진다.

### 2-3. 악세 poa 만들기 (RE: Make → Make Player Data) `[패키지]`
1. Techniques 탭 Add → 예: Name `W_s`, Script `Ljab.sc`, Button(x/c/xc), Status 체크 → 약공격 1타만 바뀌고 나머지는 원본 유지.
2. `[패키지]` 악세 poa 의 Techniques 는 **원본 poa 기술보다 우선**한다. (**원본 1.0.12 은 다르다**: 악세 poa 가 스타일 poa 를 통째로 대체한다 → `modding-rules.md` 2절) 이름이 같으면 MP 가 0 일 때 기술이 아예 안 나가므로, MP 를 쓰는 기술은 `W_s1` 처럼 **이름을 다르게** 두면 MP 소진 시 원본 기술이 나간다. Technique 의 MP 칸은 "이 값 미만이면 발동 안 함".
3. 기본 sc 를 악세에서 그대로 쓰려면 해당 sc 의 `attack("이름")` 에 쓰인 타격정보를 원본 `player.poa` Attacks 탭에서 Copy → 악세 poa 에 Paste. Attack Model 이 이름만(경로 없음)이면 Resources 탭에서도 같은 이름을 Copy/Paste 한다.

---

## 3. Player Data(`.poa`) 구조 `[원본·패키지 공통]`

- **Basic**: Name, Environ Size(크기), View(고릴라·드래곤처럼 플레이어 외형을 통째로 바꿀 때 shp/prm/tex), Checkfaces(충돌 마스크)
- **Bounding Boxes**: X/Y/Z/Size, Collision Check(오브젝트 충돌), Attack Check(aoa 와 충돌 — 끄면 무적), Check Type(맵 충돌). 악세 poa 에선 안 씀
- **Resource**: 내장 리소스(Export/Copy/Paste/Add/Modify/Remove)
- **Script**: 내장 스크립트(Export/Import/Add/Modify/Remove)
- **Attacks(타격정보)**
  - Name, Attack Model(aoa), Effect(맞은 쪽에서 발동할 히트 sc), Don't Use Floating(공중 판정 무시)
  - Power·Str: **데미지 = Power × ((STR × Str + 1) − Str)**, STR 은 캐릭터 힘(0~25)에 따른 배율표 `0,0.5,0.7,0.8,0.9,1.0,1.1,…,2.5`(방어 0 기준)
  - Time(ms, 판정 지속), X/Y/Z(판정 위치), Pitch(상하각, Head 체크 시 좌우각)
  - Feedback: 맞췄을 때 **공격자**에게 발동할 sc. Option: Hit Only / Floating Only 조건, Satisfied / Unsatisfied 스크립트
- **Techniques(기술)**
  - Name 규칙: 상태 접두 `W`(기본) `D`(대쉬) `J`(점프) `A`(조준) `B`(대쉬 중 멈춤) + 입력 `s`(약) `b`(강) `x`(동시). 예 `W_s` → `W_ss` → `W_sss`
  - Prev Tech: 이전 기술 이름(연계), From/To: 이전 기술 발동 후 **키 입력이 유효한 시간대(ms)**. 예 40~600
  - Button, Script, MP(최소 MP), Status(Standing/Brake/Walking/Jumping/Running/Aiming/Counter 복수 선택)
  - Preview(Player): sc 이름이나 Technique 이름을 적어 동작 미리보기

**고정 데미지**: 타격정보의 Str(힘 배율)을 0, Power 를 원하는 값으로 → 방어력 거의 무관(완전 고정은 아니고 ~5% 영향).
**스탯 수정**: `resource/setting.set` 열어 Style 탭 → 캐릭터 Modify → STR/TEC/SPD/JMP/DEF(원래 1~10, RE 는 25 까지), Environ Size 로 캐릭터 크기.

---

## 4. 스크립트(`.sc`) 언어 `[원본·패키지 공통, 명령어 일부는 패키지 추가]`

### 4-1. 형식
```
0 : {
    jump(5,true);
}
500 : {
    recover();
}
```
- 왼쪽 숫자 = **타임코드(ms)**. `{ }` 짝이 맞아야 컴파일된다(한 줄 한 명령이면 중괄호 생략 가능).
- 마지막엔 반드시 `recover();` — 캐릭터를 원래 상태로 되돌린다. **recover 와 같은 타임코드의 다른 명령은 무시될 수 있으니** 한 타임코드 앞에 따로 둔다.
- 문자열 인자는 `""`, 변수는 따옴표 없이. `$이름` 은 개인 변수(초기값 0).
- RE 에서 `Ctrl+Space` 자동완성, `(` 입력 시 인자 설명. `?` 붙은 인자는 true/false, 괄호 안 인자는 생략 가능.

### 4-2. 자주 쓰는 명령
| 명령 | 뜻 |
|---|---|
| `attack("타격정보명")` | poa Attacks 의 판정 발동. 여러 개 적으면 그중 하나가 랜덤 |
| `move(x,y,z)` / `move(x,z)` | 이동(좌우/위아래/앞뒤). 2개면 두 번째가 앞뒤 |
| `jump(강도, 중력?)` | 점프 |
| `nextTech()` / `nextTech(true)` | 다음 연계 기술 허용(없으면 2타가 안 나감). true 면 방향 전환 가능 |
| `setStructure("/경로/x.srt")` / `setStructure()` | 모션 교체 / 원래 모션 복귀. 기술 시작에 교체, 끝에 복귀 |
| `startAnimation(...)`, `fadeinAnimation(...)` | 교체한 srt 의 모션 재생 |
| `makeShapeModel("/effect/tornado.oa", vx,vy,vz, x,y,z, hasparent?)` | 이펙트 생성(hasparent=true 면 플레이어를 따라옴) |
| `outfitScript("파츠명")`, `outfitScript("파츠명","sc")` | 악세 파츠의 내장 sc 실행(잔상·길이 변화 등). 맞으면 상태가 남으므로 `hattacked.sc` 로 복구 |
| `play("/sound/1.sr",100,1)` | 효과음 |
| `AddMP(-10)` | MP 증감 |
| `ignoreAttack(ms)` | 지정 시간 피격 무시(무적) |
| `quake(x,y,강도,ms,대상)` `[패키지]` | 화면 흔들림. 예 `quake(0.3,0.3,13,500,1)` |
| `recover()` | 종료·복귀 |

### 4-3. 조건문과 변수
```
if(HP > 50) { move(0,10); jump(10,true); }
else if($count < 5) { $count = $count + 1; }
```
- 비교 `> < == >= <=`, 논리 `|| &&`. 등호는 `==`.
- 읽을 수 있는 값: `HP MP STR TEC SPD JMP DEF random currenttime` 등. `random` 은 0~1 실수.
- **쿨타임 패턴**: 기술 첫 줄에
  ```
  if($used==1 && currenttime-$last < 5000) recover();
  ...
  $used=1; $last=currenttime;
  ```
  `currenttime` 은 ms 단위 경과 시간. 변수를 여러 기술이 공유하면 공동 쿨타임이 된다.

### 4-4. 히트(피격) 스크립트 이름 규칙
- `hit_B1~B12`(타격), `hit_C*`(베기), `hit_H*`(부채), `hit_P*`(독), `hit_burn / hit_freeze(frz) / hit_shock`, `hit_explode`
- 접두 `f`(fhit_*) = 공중 피격, `g`(ghit_*) = 가드 피격, `d`(dhit_*) = 사망 피격. 해당 변형이 없으면 일반 hit → `die.sc` → `attacked.sc` 순으로 대체
- 타격자 쪽 효과는 Feedback(예 점프발차기: 피격자 `hit_B3.sc`, 공격자 `kickback1.sc`)

### 4-5. 어택 모델(aoa) 기본값
| aoa | 지속(ms) | 기본 히트 |
|---|---|---|
| /attack/biglaser.aoa | 800 | hit_explode.sc |
| /attack/hgrenade.aoa | 800 | (없음) |
| /attack/nageknife.aoa | 500 | hit_C2.sc |
| /attack/bombshell.aoa, bombshell2.aoa | 600 | hit_B5.sc |
| /attack/fireshot.aoa | 400 | hit_burn.sc |
| /attack/iceshot.aoa | 400 | hit_freeze.sc |
| /attack/hadou.aoa | 700 | hit_B5.sc |

---

## 5. 모션(srt) 제작 `[패키지 RE 기준]`
- Tool → Edit Player Motion → Edit Motion. Animation Test 는 `fadeinAnimation/startAnimation` 실험.
- Copy/Paste Frame(전체), Copy/Paste Parts(파츠 단위: All / Location / Size / Rotation / Size+Rotation / 좌우대칭), Exchange Parts, Mirror Horizontal(좌우반전, 사람만), Reverse H(회전 보정), Add/Remove(This / Before All / After All / Section).
- **0번 모션부터 새로 만들고** `setStructure()` 로 적용할 것. 기본 srt 뒤에 이어 붙이거나 무기 poa 를 고치면 다른 작품과 충돌한다.
- 걷기·조준·달리기·점프 등 **이동 모션은 37번까지만** 만들고, poa Resource 탭에 srt 를 `moveanimation` 이름으로 Add 하면 그 부분만 바뀐다.
- 완성은 Export Structure 로 srt 추출.

---

## 6. 3D 모델 교체 (LightWave 9.3) `[패키지 RE 기준]`
1. RE 에서 ioa/oa 열기 → Basic → Modify View(View 가 Null 이면 Resource 탭의 shp Modify) → Parts → Modify → **Export OBJ / Export Image**.
2. LightWave 9.3 에서 obj 열기 → Surface Editor → Color 옆 T → Projection UV, UV Map 지정, Image 에 텍스처 → Use Texture. Luminosity 100% 로 봐야 게임과 같다.
3. 저장 전 `w` 로 폴리곤 통계: **삼각형(3 Vertices)만** 남긴다(`Shift+T` 로 삼각화, Non-planar 는 삭제). 파츠(서페이스)가 여럿이면 각각 텍스처 매핑(`q` 로 등록).
4. File → Export → OBJ. **9.3 에서 저장한 obj 만 RE 가 읽는다.**
5. RE: Make → Make Shape 로 obj 를 shp 로(루트 파츠 선택) → Parts → Modify → Load Image 로 텍스처.
6. 불러오기가 안 되는 obj 는 Make MapView 로 열어 mpv 로 저장 → View Mode 의 Export OBJ(All) 로 다시 뽑으면 정화된다.

단축키(모델러): T 이동, Y 회전, H 스케일, E 확장, Shift+H 사이즈, ] 연결 선택.

---

## 7. 맵·스테이지·NPC `[패키지]`
- **스테이지 추가**: `resource/exconfig.conf` 의 `-stage`(맵 경로 목록), `-stageboss`(로딩 보스 이미지 = `/component/gaiden/<이름>/vs.tex`), `-stagemusic`(`music/` 파일명)에 한 줄씩 추가. 맵은 `challenge1.md` 등을 복사해 `stageN.md` 로.
  RE 로 열어 Rule Class Name 을 `com.cyberstep.ring.gaiden.SinglePlayRule` 로 바꾸면 Rule Data 에 `boss`(보스 코드, 예 bon) 와 `bonus`(예 1100)가 생긴다. 이걸 안 하면 클리어되지 않는다.
- **클리어 조건 바꾸기**: Rule Data 의 boss 이름을 맵의 아이템/NPC 이름(States 탭 Character List)으로 바꾸면 그 대상이 사라질 때 클리어(예: 라이플을 던지면 클리어).
- **죽어도 안 끝나는 맵**(앰프드공장): 맵의 플레이어 캐릭터 NAME 을 `PLAYER`, Community 를 `NOBENORE` 로.
- **NPC 기술 쓰기**: `player/special/<npc>.poa` 를 복사해 직업 폴더의 `player.poa` 로 덮으면 그 NPC 기술을 쓴다.
- **캐릭터 추가**: `setting.set` Style 탭 Add → 이름 입력 → 스타일 폴더는 `resource/player/style/` 아래 → 경로는 `resource` 를 빼고 `/` 부터. 패키지는 `dic_ko` 사전 파일에 `케릭명 : 표시이름` 을 추가해 게임 내 이름을 정한다(원본의 `Dict`/`dic_<lang>.unicode` 메커니즘과 같은 것).
- **아이템 추가**: `item/` 에 ioa 넣고 `setting.set` 에 Add → 필요하면 ioa Basic 의 Name 수정. 동봉된 poa/oa/aoa/sc 도 같이 넣는다.
- **head/face/body 폴더는 hm 화가 안 되므로** 스타일 배포 시 파일 그대로 첨부해야 한다(안 하면 검은 화면).

## 8. 배포(HM) `[패키지]`
RE Tool → Make Package → Add(작품에 쓰인 **모든** 파일 또는 폴더) → Make → `resource/` 에 `.hm` 저장. 하위 폴더에 두면 안 읽힌다. od 만 넣으면 모양만 바뀌고 기술·리소스는 안 바뀐다.

## 9. 트러블슈팅 모음
- javaw.exe 만 뜨고 실행 안 됨 → 패키지의 amped.jar 교체 후 업데이트 "예".
- 실행하자마자 검은 화면 → 그래픽 드라이버, 또는 추가 캐릭터 경로에 `resource` 가 들어간 경우(빼기), head/face/body 누락.
- `Throwitem` 명령이 컴파일 오류 → 패키지 `funclist.txt` 의 주석 오타(교체 파일 배포됨).
- 2타가 안 나감 → 1타 sc 에 `nextTech();` 없음.
- 모션이 기술 후 안 돌아옴 → 기술 끝에 `setStructure();` 누락.
- 데미지 0 힘 0 이어도 1/40 줄은 깎인다(완전 고정 데미지는 없음).

---

## 10. 이 저장소와의 접점
- 텍스처(.tex) 편집: `tools/textool/TexTool.java` (`dump`/`import`/`render`/`scan`/`batch`).
- 문자열(이름·대사): `l10n/*.tsv` + `amped.web.I18n`(런타임 치환) — 사전 파일 대신 상수풀/직렬화 문자열을 직접 바꾼다.
- 실측 규칙·시행착오: `docs/modding-rules.md` (이 절보다 우선)
- 기술(poa)·스크립트(.sc): `mods/accs/<이름>/poa.txt` + `.sc` → `tools/poatool/PoaTool.java` 가 `Scp` 로 컴파일해 `resource/accs/<이름>.poa` 생성. 명령 표는 `tools/poatool/defines.sc`.
- 악세(.accs) 제작: `mods/accs/<이름>.txt` 스펙(`stat`/`playerdata`/`part` 줄) → 빌드 시 `tools/accstool/AccsTool.java` 가 `resource/accs/<이름>.accs` 생성. 목록은 `game-overrides/.../NewSetting.java` 의 `EXTRA_ACCESSORIES` 에 추가. 싱글 플레이에는 악세 선택 UI 가 없으므로 `?accs=<이름>` 으로 외전 플레이어 캐릭터에 장착한다(예제: `heavygloves` = 1.5배 복싱 글러브, STR+3 DEF+1 SPD-2 JMP-1).
  부위 번호(원본 accs 기준): 0 오른손, 7 왼손, 6/12 손목, 13 몸통(등). 원본 11종은 모두 스탯 0·외형만.
- 스크립트 컴파일러(`com.cyberstep.ring.scp.Scp`)는 게임 jar 에 들어 있다. `PoaTool` 이 그대로 쓴다.

### 원글 목록 (카페 글번호)
91357 od 설명 / 91360 od Parts / 91366 싱겟·RE·리소스 개요 / 91368 악세 팁 / 91478 자동완성 / 91486 모션 / 91492 Player Data / 91645 스테이지 추가 / 91658 HM화 / 91665·91670·91701·91933 악세 제작 기초 1~4 / 91771 Throwitem 오류 / 91801 아이템 추가 / 91901 Outfit 팁 모음 / 91990 NPC 기술 / 92041·92170 맵 규칙 / 92256 모델 추출→shp / 92293 맵 NPC / 92383 스크린샷 / 92481 스탯 / 92526 캐릭터 추가 / 92867 hm 필수 / 92898 quake / 92952 LW 기초 / 93041 크기 / 93194 실행 오류 / 93205 스크립트 입문 / 93226 if문 / 93275 obj 불러오기 / 93276 LW 9.3 / 93470 타격효과 / 93918 FAQ / 93931·94495 캐릭터 추가 오류 / 95380 쿨타임 / 96978·100500 고정데미지 / 108525·108527 모델링
