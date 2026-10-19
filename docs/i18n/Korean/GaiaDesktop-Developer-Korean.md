# GAIA Desktop — 개발자 가이드

이 모듈을 어떻게 빌드하고, 어떻게 구성되어 있으며, 어떻게 테스트하는지 설명합니다. 애플리케이션이 겉으로 무엇을 하는지는 [사용자 가이드](GaiaDesktop-User-Korean.md)를 참조하십시오. 원문: [GaiaDesktop-Developer.md](../../GaiaDesktop-Developer.md).

## 목차

1. [모듈 한눈에 보기](#모듈-한눈에-보기)
2. [빌드와 실행](#빌드와-실행)
   - [패키징](#패키징)
3. [아키텍처](#아키텍처)
   - [입력 깔때기](#입력-깔때기)
   - [스레드](#스레드)
   - [재구성과 레이블 갱신](#재구성과-레이블-갱신)
4. [{GS} 토큰](#gs-토큰)
5. [스캐너 통합](#스캐너-통합)
   - [플랫폼 게이트](#플랫폼-게이트)
   - [심볼로지](#심볼로지)
6. [현지화](#현지화)
   - [`--language` 인수](#--language-인수)
   - [언어 추가](#언어-추가)
   - [의도적으로 번역하지 않는 것](#의도적으로-번역하지-않는-것)
7. [저장 상태 형식](#저장-상태-형식)
8. [테스트](#테스트)
9. [빌드 문제 해결](#빌드-문제-해결)
10. [알려진 제한 사항과 남은 작업](#알려진-제한-사항과-남은-작업)

---

## 모듈 한눈에 보기

```
tools.pantheum:gaia-desktop:1.0.0   Java 21 · JavaFX 21
        └── depends on tools.pantheum:gaia:1.1.0
```

코어 라이브러리의 클라이언트일 뿐, 그 이상은 아닙니다. 이 모듈에는 **GS1 지식이 전혀 들어 있지 않습니다**. 모든 AI 정의, 검증 규칙, 오류 메시지, 현지화된 설명은 [`GaiaParser`](../../../../gaia/docs/GaiaParser.md)와 [`GaiaBuilder`](../../../../gaia/docs/GaiaBuilder.md)에서 옵니다. 특히 AI 목록은 이 모듈에 절대 복사되지 않습니다. 빌더의 선택기는 코어 자체의 레지스트리를 읽으므로, 빌더가 거부할 AI를 제시할 수 없습니다.

**진입점:** `tools.pantheum.gaia.desktop.GaiaDesktopApp`(JavaFX `Application`)와 `tools.pantheum.gaia.desktop.Launcher`(패키징된 jar의 `Main-Class`).

이 모듈은 말단(리프)입니다. 이 모듈에 의존하는 것은 없으며, 이 모듈도 다른 하위 모듈에 의존하지 않습니다.

---

## 빌드와 실행

- 실행에는 **Java 21**, 빌드에는 JDK 21 이상이 필요합니다. 모듈은 `release=21`로 컴파일됩니다. JavaFX 21은 이에 맞는 LTS 계열입니다. 코어 jar는 여전히 Java 11 바이트코드이며 21에서도 수정 없이 실행됩니다.
- `PATH`에 **Maven**이 있어야 합니다. 이 저장소에는 wrapper가 없습니다.
- **코어를 먼저 설치하십시오.** 이 모듈은 `gaia:1.1.0`을 `~/.m2`에서 가져옵니다. 코어 jar가 오래되었거나 없는 것이 `package tools.pantheum.gaia does not exist`의 흔한 원인입니다.

```bash
(cd gaia && mvn -q install -DskipTests)      # publish the core
(cd gaia-desktop && mvn javafx:run)          # run from source
(cd gaia-desktop && mvn clean package)       # target/dist/gaia-desktop-<version>-<platform>/ — jar (~290 KB), lib/, run.sh (run.bat on win)
(cd gaia-desktop && mvn clean -Pfat package) # one self-contained jar, ~50 MB
(cd gaia-desktop && mvn package -Dfx.platform=win)   # another platform; see Packaging
java -jar gaia-desktop/target/dist/gaia-desktop-1.0.0-mac-aarch64/gaia-desktop.jar
java -jar gaia-desktop/target/dist/gaia-desktop-1.0.0-mac-aarch64/gaia-desktop.jar --language=FR
```

`--language`는 유일한 명령줄 인수이며, 프로필 전체를 다른 곳으로 가리키는 `-Dgaia.desktop.home=…`를 제외하면 시작에 영향을 주는 유일한 방법입니다.

`mvn javafx:run`은 개발용 경로입니다. 플러그인이 JavaFX를 **모듈 경로**에 두므로 툴킷이 이름 있는 모듈로 시작하고 `GaiaDesktopApp`을 직접 실행할 수 있습니다. 패키징된 jar는 그렇게 할 수 없습니다. 모든 것이 클래스 경로에 있기 때문이며, 그래서 `Main-Class`가 `Launcher` 심(shim)입니다. 그 구성에서 `Application` 하위 클래스를 `Main-Class`로 쓰면 *"JavaFX runtime components are missing"*과 함께 실패합니다.

항상 `clean`을 사용하십시오. 여러 플랫폼을 빌드하기 전에 한 번만 실행하면 됩니다. `target/dist/`에서 모든 플랫폼의 내용을 비우기 때문입니다. 이유는 [빌드 문제 해결](#빌드-문제-해결)을 참조하십시오.

### 패키징

배포할 모든 것은 `target/dist/gaia-desktop-<version>-<platform>/` 아래에 모이며 그 밖의 것은 모이지 않습니다. 따라서 `target/` 안에서 `classes/`, `maven-status/` 및 기타 빌드 임시 파일과 섞인 산출물을 골라낼 필요 없이 이 디렉터리를 통째로 압축하거나 복사할 수 있습니다. `${dist.dir}`이 이를 한 곳에서 이름 붙이며, 산출물을 쓰는 모든 플러그인이 이를 가리킵니다.

#### 플랫폼별 빌드

JavaFX jar에는 네이티브 코드가 들어 있으므로 각 배포본은 하나의 플랫폼용이며, `fx.platform` 속성에 의해 OpenJFX 분류자로 이름이 붙습니다: `mac-aarch64`, `mac`, `win`, `linux`, `linux-aarch64`. OS에 따라 활성화되는 다섯 개의 `fx-*` 프로필이 이를 빌드하는 머신 자신의 값으로 설정하며, 일반적인 `mvn package`나 `mvn javafx:run`에는 그것만으로 충분합니다. `-Dfx.platform=…`은 이들보다 우선하여 어느 머신에서든 다른 어떤 플랫폼이든 빌드할 수 있게 합니다. JavaFX jar는 내려받기만 하면 되기 때문입니다. `${dist.dir}`은 `target/dist/${fx.platform}`이므로, 한 플랫폼의 빌드가 다른 플랫폼의 빌드를 덮어쓰는 일은 없습니다.

```bash
(cd gaia-desktop && mvn -q clean package)    # this machine's platform, with the tests
for p in win linux mac mac-aarch64; do (cd gaia-desktop && mvn -q package -DskipTests -Dfx.platform=$p); done
```

이를 가능하게 하는 pom의 세부 사항이 두 가지 있으며, 둘 다 지워도 될 것처럼 보입니다(그렇지 않습니다):

- **모든 JavaFX 모듈이 직접 의존성입니다**. base, graphics, controls, media, web 각각에 `<classifier>${fx.platform}</classifier>`와 `org.openjfx:*` 제외 항목이 붙어 있습니다. OpenJFX의 pom은 전이 jar를 *빌드하는 머신*의 OS에 따라 고르므로, graphics나 base를 전이적으로 들어오게 두면 Windows 빌드에 Windows 네이티브와 함께 Mac 네이티브가 들어갑니다.
- **속성은 `javafx.platform`이 아니라 `fx.platform`입니다.** OpenJFX 자체의 속성을 외부에서 설정하면 상위 pom이 그것을 자기 자신으로 재정의합니다. Maven은 그 pom을 유효하지 않다고 거부하고 플랫폼 jar를 조용히 빼 버려, JavaFX가 전혀 없는 빌드가 남습니다.

두 구성 모두 같은 경로 `target/dist/gaia-desktop-<version>-<platform>/gaia-desktop.jar`에 쓰며, 빌드마다 정확히 하나만 실행됩니다. 둘이 동시에 그 경로를 차지하지 않도록 하는 것이 `${jar.phase}`, `assembly.skipAssembly`, `mdep.skip`입니다.

| | 기본 | `-Pfat` |
|---|---|---|
| `gaia-desktop.jar` | 약 290 KB — 이 모듈의 클래스와 리소스만 | 약 50 MB — 의존성을 풀어서 포함 |
| `dist/gaia-desktop-<version>-<platform>/lib/` | jar 16개, 매니페스트 `Class-Path`에 하나씩 나열 | — |
| 배포 대상 | jar **와** 그 옆의 `lib/` | 파일 하나 |
| `dist/gaia-desktop-<version>-<platform>/`에 추가로 | `run.bat`(win) 또는 `run.sh`(기타) | 동일 |
| 그리고 | `readme.html`과 `docs/`(모든 플랫폼) | 동일 |

모든 배포본에는 가이드도 들어 있습니다. `docs/`는 구조를 유지한 채 복사되므로 가이드와 번역 사이의 링크가 그대로 작동하며, 번역 도구(`check-i18n.py`, `i18n-manifest.json`)는 제외됩니다. 그리고 루트에는 각 가이드로 연결되는 색인인 `readme.html`이 있으며, `src/main/dist/readme.html`에서 만들어지고 `@VERSION@`은 pom 버전으로 치환됩니다. 둘 다 `pick-launch-script` antrun 단계가 쓰며, 이 단계는 먼저 `docs/`를 삭제하므로 제거된 가이드가 남지 않습니다. readme의 링크는 손으로 작성되어 있습니다. `docs/` 아래에서 가이드 이름을 바꾸거나 번역을 추가하면 그곳에도 행을 추가해야 합니다.

매니페스트 `Class-Path`와 `lib/`는 한 가지의 두 반쪽입니다. jar는 모든 런타임 의존성에 대해 `lib/<artifact>-<version>.jar`를 나열하고, `copy-dependencies`는 정확히 그 이름들로 `lib/`를 채웁니다. 이것이 `java -jar`가 작동하는 이유입니다.

#### macOS 앱과 배포 zip

하나의 antrun 실행 `mac-app-and-zip`이 `package` 단계를 마무리합니다. 그래서 antrun 플러그인은 jar 플러그인과 assembly 플러그인 **뒤에** 선언되어 있습니다. Maven은 한 단계의 실행들을 플러그인 선언 순서대로 실행하므로, 어느 패키징이든 이것이 실행될 때는 jar가 이미 존재합니다. (다른 실행인 `pick-launch-script`는 `prepare-package`에 있으며 영향을 받지 않습니다.)

`mac`과 `mac-aarch64`의 경우, 먼저 배포 디렉터리에 `GAIA Desktop.app`을 만듭니다. `Info.plist`(`@VERSION@`을 pom 버전으로 치환), 런처 `Contents/MacOS/gaia-desktop`(실행 가능하게 설정), `gaia-desktop.icns`로, 모두 `src/main/macos/`에서 옵니다. 그런 다음 `gaia-desktop.jar`와 `lib/`를 `Contents/Java`로 **이동**하여 번들이 단독으로 동작하게 합니다. `run.sh`는 옆에 jar가 없으면 그곳을 확인합니다. 런처는 버전으로 Java를 찾고(먼저 `java_home -v 21+`, 그다음 Homebrew의 openjdk. Finder에서 실행하면 `JAVA_HOME`도 `PATH`도 보이지 않습니다), 없으면 `osascript`로 대화 상자를 띄우고, logback의 상대 경로 `logs/`에 쓸 수 있도록 `~/.gaia-desktop`으로 `cd`하며, `-Xdock:name`/`-Xdock:icon`을 전달합니다. macOS는 실행 중인 `java`를 번들과 연결합니다(번들 ID와 Dock 아이콘이 앱의 것이 됩니다). 메뉴 막대에는 GAIA Desktop이 표시됩니다.

이어서 모든 플랫폼에 대해 Ant의 `<zip>`으로 디렉터리를 같은 이름의 폴더 아래에 넣어 `target/dist/gaia-desktop-<version>-<platform>.zip`으로 압축합니다. `run.sh`와 `Contents/MacOS/**`는 `filemode="755"`가 붙은 별도의 파일 집합으로 넣는데, 그렇지 않으면 압축을 풀었을 때 실행할 수 없게 됩니다. 이 zip은 **첨부되지 않습니다**. GitHub 릴리스용이며, 네 플랫폼은 네 번의 빌드에서 나오는데, 버전마다 변경 불가능한 업로드를 한 번만 받는 Maven Central은 이를 하나의 릴리스로 받을 수 없기 때문입니다.

`project.build.outputTimestamp`는 빌드 시각으로 설정됩니다. 그렇지 않으면 maven-jar-plugin 3.5와 maven-assembly-plugin 3.8의 아카이버가 모든 항목의 날짜를 1980년 2월 1일로 기록합니다.

앱 아이콘은 `tools/icon/GaiaIcon.java`가 그립니다(빌드의 일부가 아니며, 헤더에 `sips`/`iconutil` 단계가 적혀 있습니다). 그 결과물은 저장소에 커밋되어 있습니다. 번들용 `src/main/macos/gaia-desktop.icns`와, `GaiaDesktopApp`이 창 아이콘으로 설정하는 `src/main/resources/icons/gaia-desktop-{16..256}.png`입니다(Windows와 Linux. macOS는 창 아이콘을 무시합니다).

#### 실행 스크립트

`run.sh`와 `run.bat`은 `src/main/scripts/`에서 오며, 필터링 없이 `dist/gaia-desktop-<version>-<platform>/`에 복사됩니다. `run.sh`에는 `${...}` 셸 확장이 가득한데, Maven 필터링이 이를 마음대로 바꿔 쓸 수 있기 때문입니다. 그런 다음 antrun 단계가 해당 플랫폼에서 실행할 것만 남기고(`win`에는 `run.bat`, 나머지 모두에는 `run.sh`), `copy-resources`가 실행 비트를 유지하지 않으므로 `run.sh`에 실행 비트를 설정합니다. 프로필이 아니라 해석된 `${fx.platform}`으로 판단하는 이유는, 프로필 활성화가 다른 프로필이 설정한 값을 볼 수 없기 때문입니다. 그렇지 않으면 Windows에서 만든 Linux 빌드가 호스트의 스크립트를 받게 됩니다.

스크립트는 **매니페스트에 의존하지 않습니다**. 각 스크립트는 클래스 경로를 스스로 구성합니다. 먼저 `gaia-desktop.jar`, 이어서 `lib/`의 모든 `.jar`를 `lib/*` 와일드카드에 맡기지 않고 하나씩 나열하므로, 공백이 들어 있는 경로도 하나의 항목으로 유지되고 추가된 개수도 정확합니다. `-cp`로 실행하는 것이 `tools.pantheum.gaia.desktop.Launcher`를 명시적으로 지정하는 이유입니다. `-cp`는 매니페스트의 `Main-Class`를 읽지 않기 때문입니다. 그러나 `Class-Path`는 **여전히** 따릅니다. `-cp`에 있는 jar는 그 매니페스트가 나열한 jar를 함께 가져오며 바로 뒤에서 검색됩니다. 따라서 스크립트가 나열하든 말든 모든 `lib/` jar가 로드되며, 스크립트의 클래스 경로에서 하나를 빼도 JVM에서 막을 수는 없습니다. 그래도 스크립트는 모두 나열하므로, 매니페스트가 오래되었거나 손으로 수정되었거나 항목이 빠진 jar도 여전히 시작됩니다. 같은 스크립트가 두 가지 패키징 모두에 쓰입니다. `-Pfat` jar에는 추가할 `lib/`가 없고 필요도 없으며, 빈 `lib/`가 오류가 아니라 알림인 것도 그 때문입니다.

#### Zebra 네이티브 라이브러리

빌드에는 Zebra의 것이 전혀 포함되지 않습니다. DLL도 서비스 jar도 없습니다. Windows에서는 이들이 머신에 설치된 Zebra Scanner SDK에서 옵니다. `run.bat`은 SDK의 `Jpos*.jar` 서비스 jar와 `bin\`을 클래스 경로에, `bin\`을 `PATH`에 추가하며, `System.loadLibrary`는 그곳에서 `CSJPOSScanner.dll` / `CSJPOSScanner64.dll`을 찾습니다(`java.library.path`의 기본값은 `PATH`입니다). JavaPOS API와 그 구성 로더는 SDK가 있든 없든 항상 `lib\javapos-*.jar`에서 옵니다.

**`run.bat`은 SDK 자체의 JavaPOS jar**인 `External\JavaPOS-1.14.0-Dist\javaPOS114.jar`**를 절대 추가하지 않습니다**. 예전에는 추가했고, API가 두 벌이 되지 않도록 `lib\javapos-*.jar`를 뺐습니다. 하지만 매니페스트 `Class-Path`가 어쨌든 그것들을 로드했기 때문에(위 참조) JVM은 둘 다 받았습니다. `lib\`의 4.x 로더 클래스와, 그 옆에 `javaPOS114.jar`의 `jpos/res/jpos.properties`와 `jcl.xsd`입니다. 이 속성 파일은 [`JposConfig`](#스캐너-통합)가 물러나게 만들고, 그 `jcl.xsd`는 1.10까지의 JavaPOS 버전만 받아들이므로, `jpos.xml`의 모든 항목(1.12–1.14)이 검증에 실패하고 레지스트리는 비어 있었으며, 스캐너에 닿기도 전에 모든 연결이 오류 109로 실패했습니다. 그 jar가 클래스 경로에 있기만 해도, 심지어 `lib\`의 jar들 뒤에 있어도 그렇게 됩니다. Zebra의 서비스 jar는 `lib\`의 1.14 API에서 수정 없이 동작합니다. 열기, 점유, 스캔은 그 구성으로 실제 스캐너에서 검증되었습니다. `run.bat` 옆에 손으로 둔 DLL도 여전히 존중됩니다. 이때 스크립트는 `-Djava.library.path=<dist>;%PATH%`를 설정하는데, 이 속성이 기본 검색 경로를 완전히 대체하므로 `PATH`를 뒤에 덧붙입니다.

`mvn javafx:run`은 이 중 아무것도 하지 않으므로, Windows에서 개발 실행으로 스캐너를 점유하려면 다른 방법으로 SDK의 jar를 클래스 경로에, 그 `bin\`을 `PATH`에 두어야 합니다. 그럴 때는 `dist/`에서 실행하십시오.

클래스 경로 외에도 스크립트는 `java -jar`가 하지 않는 일을 맡습니다. 자신의 디렉터리를 해석하여 어떤 작업 디렉터리에서든 동작하게 하고, `PATH`보다 `JAVA_HOME`을 우선하며, 11 이전의 JVM을 `UnsupportedClassVersionError` 대신 해결 방법을 알려 주는 한 문장으로 거부합니다. `GAIA_DESKTOP_OPTS`는 JVM 옵션을 전달합니다. `run.bat`은 `javaw.exe`가 아니라 `java.exe`를 쓰므로 콘솔이 열린 채로 남습니다. 스캐너가 동작하는 플랫폼은 Windows뿐이며, 그 상태와 JavaPOS 오류가 그곳에 출력됩니다.

`run.bat`은 또한 찾아낸 것에서 Java, JavaFX, Zebra 서비스를 고릅니다. 번들된 `jdk*\`, 모듈 경로의 `javafx-sdk*\lib\`, `ZEBRA_HOME`에 있는 Zebra Scanner SDK입니다. SDK의 JavaFX를 쓸 때는 `lib\`의 JavaFX 사본을 클래스 경로에서 빼고, JDK의 SAX 파서, Windows 프록시와 신뢰 저장소, 그리고 Java 17 이상에서는 `--enable-native-access`를 전달합니다. 표는 README의 [What `run.bat` adds on Windows](../../../README.md#what-runbat-adds-on-windows)에 있습니다. 이 파일의 CRLF 줄 끝을 유지하십시오. LF만 있는 배치 파일에서는 `cmd.exe`가 `call :label`을 놓칠 수 있습니다. `run.sh`도 `--enable-native-access=ALL-UNNAMED`를 전달합니다. JavaFX는 자체 네이티브를 `lib/`에서 로드하며, JavaFX가 클래스 경로에 있을 때 이를 포괄하는 모듈이 `ALL-UNNAMED`이기 때문입니다. `run.bat`과 마찬가지로 Java 버전을 읽을 수 없을 때만 생략됩니다.

두 구성 모두 **하나의 플랫폼**(`${fx.platform}`)의 JavaFX 네이티브만 담고 있으므로, 어느 것도 OS나 아키텍처를 넘어 옮길 수 없습니다. 대상마다 한 번씩 빌드하십시오. [플랫폼별 빌드](#플랫폼별-빌드)를 참조하십시오.

> **Eclipse.** `copy-dependencies` 실행에는 `<?m2e ignore?>` 지시가 붙어 있습니다. m2e는 열려 있는 작업 공간 프로젝트를 jar가 아니라 `target/classes` 폴더로 해석하고, `copy-dependencies`는 패키징되지 않은 아티팩트의 복사를 거부하므로(MDEP-187), `gaia` 프로젝트가 이 프로젝트와 함께 열려 있으면 자동 빌드가 실패하게 됩니다. 그 결과 IDE에서만 한 빌드는 `target/dist/gaia-desktop-<version>-<platform>/lib/`를 채우지 않습니다. jar 자체를 사용하기 전에 `mvn package`를 실행하십시오.

---

## 아키텍처

| 패키지 | 내용 |
|---|---|
| `desktop` | `GaiaDesktopApp`, `Launcher`, `Theme`, `GaiaDesktopVersion` |
| `desktop.ui` | `MainView`(창), `BuilderPane`, `OptionsPane`, `HistoryCell`, `EditCell` |
| `desktop.build` | `ElementBuilder`(JavaFX에 의존하지 않는 빌드 로직), `AiPair`(편집기의 한 행) |
| `desktop.parse` | `ParseOptions`(관찰 가능한 `ParseConfig`), `SystemDefaults`, `Languages`, `GsToken` |
| `desktop.scanner` | `ScannerService`(JavaPOS), `ScannerListener`, `Symbologies` |
| `desktop.html` | `GaiaDesktopReport` — 보고서 렌더러. JavaFX에 의존하지 않으며 툴킷 없이 단위 테스트됨 |
| `desktop.i18n` | `UiStrings` — 레이블 카탈로그 |
| `desktop.store` | `AppPaths`, `OptionsStore`, `CaptureStore` |

### 입력 깔때기

모든 입력, 즉 입력한 것, 붙여 넣은 것, 파일에서 연 것, 스캔한 것, 빌드한 것은 하나의 메서드 `MainView.capture(String, ScanSource, String)`를 통해 파서에 도달합니다.

이것이 앱 전체가 기반으로 삼는 단 하나의 설계 결정입니다. 빌드에는 자체 보고서도, 자체 기록도, 자체 내보내기 경로도 필요 없습니다. 깔때기를 지나고 나면 빌드한 문자열과 스캔한 문자열을 구별할 수 없기 때문입니다. 입력을 만들어 내는 새로운 것은 그 옆으로 돌아가지 말고 이것을 거쳐야 합니다.

### 스레드

| 스레드 | 실행하는 것 |
|---|---|
| JavaFX 애플리케이션 스레드 | 모든 파싱, 렌더링, UI. AI 테이블이 준비되면 파싱은 1밀리초도 걸리지 않으므로 작업자 스레드가 필요 없음 |
| `gaia-scanner` | 스캐너에 대한 모든 호출(열기, 점유, 활성화, 비활성화, 해제, 닫기). `ScannerService`가 소유하는 오래 사는 스레드 하나. [스캐너 통합](#스캐너-통합) 참조 |
| JavaPOS 이벤트 스레드 | `ScannerService` 콜백. `MainView.FxScannerListener`가 각각을 `Platform.runLater`로 넘겨줌 |
| 백그라운드 | 스캐너 `connect()` / `disconnect()` 대기(점유에 최대 5초가 걸릴 수 있음), 시작 시 저장된 기록 복원(파서 예열을 겸함), 업데이트 확인(`gaia-update-check`, GitHub 요청 한 번, 결과는 `Platform.runLater`로 전달) |

**컨트롤 자신의 선택 이벤트 안에서 그 컨트롤의 선택을 바꾸거나 컨트롤을 파괴하지 마십시오.** 이 앱에서 일어난 세 번의 서로 다른 충돌이 바로 그것 때문이었습니다. 팝업 자신의 `clearAndSelect` 안에서 AI 선택기를 초기화한 것(`ListViewBehavior`에서 발생한 `IndexOutOfBoundsException`)과, 언어 콤보 상자를 소유한 패널을 그 콤보 상자 자신의 핸들러 안에서 재구성한 것입니다. 둘 다 이제 `Platform.runLater`로 미뤄지며, 그 이유는 두 호출 지점 모두에 기록되어 있습니다.

### 재구성과 레이블 갱신

언어를 바꾸면 화면의 틀(메뉴, 도구 모음, 옵션 패널)이 카탈로그로부터 **재구성**됩니다. 이것이 안전한 이유는 그 어느 것도 상태를 갖지 않기 때문입니다. `OptionsPane`은 `ParseOptions`의 양방향 뷰이므로 값은 모델에 있고, 새 패널이 곧바로 그 값을 다시 읽어 옵니다.

**사용자 데이터**를 가진 컨트롤은 필드이며, 대신 그 자리에서 레이블만 갱신합니다. `BuilderPane.retext(UiStrings)`는 AI 행, Digital Link 필드, 선택을 유지합니다. 이를 재구성하면 진행 중인 작업이 사라집니다. 실제로 예전에 그랬습니다.

---

## {GS} 토큰

JavaFX `TextField`는 `0x20` 미만의 모든 문자를 조용히 버립니다(`TextInputControl.filterInput`). "이상하게 표시되는" 것이 아니라 제거됩니다. 입력, 붙여 넣기, `setText`, `insertText`가 모두 영향을 받으므로 `setText("01…" + GS + "10LOT")`의 결과는 `"01…10LOT"`가 됩니다. 가변 길이 AI가 뒤따르는 내용까지 삼켜 버려 의미가 조용히 바뀐 요소 문자열입니다.

`desktop.parse.GsToken`이 그 해답의 전부입니다. 구분자는 UI에서 리터럴 텍스트 `{GS}`로 전달되고, 파서로 가는 길에 다시 `0x1D`가 됩니다. 이 토큰은 여기서 고안한 것이 아닙니다. 코어의 `ASCIIRevealerUtils.printable`이 `0x1D`를 표시하는 형태 그대로이므로, 보고서에서 복사한 값을 입력 막대에 바로 다시 붙여 넣을 수 있습니다.

입력 필드의 `TextFormatter` 필터는 텍스트가 들어올 수 있는 모든 경로(입력, ⌘V, 컨텍스트 메뉴, 끌어서 놓기, `setText`)를 잡아냅니다. 변환을 각 핸들러가 아니라 그곳에 둔 이유가 그것입니다.

스캐너 경로는 `GsToken`을 거치지 않습니다. 스캔은 바이트로 파서에 도달합니다.

---

## 스캐너 통합

`ScannerService`는 JavaPOS `Scanner` 컨트롤을 소유합니다. 열기 → 점유 → 활성화 순서이며, 스캔할 때마다 데이터 이벤트를 다시 활성화합니다. 논리 장치 이름은 `src/main/resources/jpos.xml`에서 옵니다.

**모든 장치 호출은 서비스와 수명이 같은 하나의 스레드 `gaia-scanner`에서 실행됩니다**. FX 스레드에서도, 호출이 끝나면 종료되는 스레드에서도 절대 실행하지 않습니다. Zebra의 서비스는 CoreScanner COM API를 거쳐 JNI로 스캐너에 접근하며, COM은 자신이 설정한 것을 호출 스레드의 아파트먼트에 묶습니다. 나중에 종료되는 스레드에서 열면 스캐너는 점유되지만 아무 반응이 없고, 다른 스레드에서 닫으면 엉뚱한 아파트먼트가 초기화 해제됩니다. FX 스레드에서 닫는 것이 최악의 경우입니다. JavaFX가 그곳에서 `OleInitialize`를 호출하고 모든 창의 끌어서 놓기 대상을 그에 대해 등록하므로, 그 스레드에서 `CoUninitialize`가 호출되면 앱 전체의 끌어서 놓기가 망가집니다. 이것은 특정 JavaFX 버전이 아니라 Windows의 JavaFX 문제입니다. `connect()`와 `disconnect()`는 그 스레드에 작업을 넘기고 기다리므로, `MainView`는 둘 다 수명이 짧은 백그라운드 스레드에서 호출합니다.

두 가지 규칙이 이상 동작하는 장치 때문에 연결이 꼼짝 못 하게 되는 것을 막습니다. **정리 작업은 절대 중간에 멈추지 않습니다.** `closeQuietly`는 비활성화, 해제, 닫기를 별도의 단계로 수행하고, 점유를 보유하고 있을 때만 해제하며, 항상 `close()`를 시도합니다. 점유에 실패한 뒤에는 `release()`가 "점유되지 않음"을 던지는데, 단계들이 하나의 `try`를 공유하던 시절에는 그 때문에 닫기가 건너뛰어져 장치가 Zebra의 서비스에서 열린 채로 남았습니다. **데이터 이벤트는 `finally`에서 다시 활성화합니다.** JavaPOS는 이벤트마다 이를 비활성화하므로, 읽기나 핸들러가 예외를 던져도 반드시 다시 활성화해야 합니다. 그렇지 않으면 다시 연결할 때까지 스캐너가 반응하지 않습니다. `ScannerLifecycleTest`는 스크립트로 동작하는 `jpos.Scanner`를 상대로 두 규칙을 모두 검증합니다.

**`JposConfig`는 첫 번째 열기 전에 JavaPOS가 번들된 `jpos.xml`을 가리키게 합니다.** Maven의 JavaPOS jar(`javapos-config-loader` 4.x)를 쓰면서 클래스 경로에 `jpos/res/jpos.properties`가 없으면, 그렇게 하지 않을 경우 모든 열기가 *"Service does not exist in loaded JCL registry"*(오류 109)로 실패하며, 그 이유는 세 가지입니다. 서비스 관리자가 `SimpleRegPopulator`로 되돌아가는데, 이것은 직렬화된 `jpos.cfg`를 읽고 `jpos.xml`은 절대 읽지 않습니다. XML 로더는 `jpos.xml`을 작업 디렉터리에서만 찾고 jar 안에서는 찾지 않습니다. 그리고 `lib/`에 있는 Xerces 2.12.2는 로더가 설정하는 JAXP `accessExternalDTD` 속성을 거부합니다. `JposConfig`는 `jpos.config.regPopulatorClass`(`JavaxRegPopulator`), `jpos.config.populatorFileURL`(jar 자신의 `jar:file:…!/jpos.xml`), `javax.xml.parsers.SAXParserFactory`(JDK의 것)를 시스템 속성으로 설정합니다. 이미 설정된 것은 절대 덮어쓰지 않으므로 `-Djpos.config.populatorFileURL=file:…`로 다른 `jpos.xml`을 고를 수도 있습니다. 또한 클래스 경로에 `jpos.properties`가 있거나 4.x 로더가 없으면 아무것도 설정하지 않습니다. Zebra SDK의 `javaPOS114.jar`가 바로 그런 경우로, 레지스트리를 완전히 망가뜨립니다. `run.bat`이 그것을 클래스 경로에서 빼는 이유가 그것입니다. [Zebra 네이티브 라이브러리](#zebra-네이티브-라이브러리)를 참조하십시오. `JposConfigTest`는 그 후 레지스트리에 파일의 스캐너가 들어 있는지 처음부터 끝까지 확인합니다.

**장치 선택기는 그 레지스트리에서 생성되며**, 코드에 목록으로 유지되지 않습니다. `ScannerService.logicalNames()`는 `gaia-scanner` 스레드에서 로드된 항목을 읽어 장치 범주가 `Scanner`인 것만 남기고(`jpos.xml`의 저울 항목 두 개는 제외됩니다), `pickerOrder`로 정렬합니다. 파일에 `ZebraAllScanners`가 정의되어 있으면 그것이 맨 앞이고, 나머지는 알파벳순입니다(레지스트리는 파일의 순서를 유지하지 않습니다). 그것이 없는 파일에 그것을 덧붙이지는 않습니다. 아무것도 로드되지 않으면 기본값만 제시되므로 선택기가 비어 있는 일은 없습니다. `MainView`는 기본값을 즉시 보여 주고, 저장된 선택을 유지한 채 백그라운드 스레드에서 목록을 채웁니다. 따라서 `jpos.xml`에 추가한 스캐너, 또는 `-Djpos.config.populatorFileURL`로 지정한 파일 전체가 코드 변경 없이 선택기에 나타나며, `JposConfigTest`는 선택기를 번들 파일을 독립적으로 읽은 결과와 대조합니다. 그 항목들이 가리키는 서비스는 Zebra의 클래스(`com.zebra.jpos.service.scanner.*Factory`)이며, 이것은 여전히 클래스 경로에 있어야 합니다. `run.bat`을 통해 Scanner SDK에서 오거나, `lib\`에 넣은 Zebra 서비스 jar에서 옵니다.

스캔 데이터는 **ISO-8859-1**로 디코딩되므로 각 바이트가 1:1로 대응하며, AIM Code ID 접두사와 모든 `0x1D`가 그대로 보존됩니다. 보고서는 UTF-8로 기록됩니다.

서비스 안에는 사용자를 위한 문장을 만드는 코드가 전혀 없습니다. 이벤트는 `ScannerListener.Event` 상수와 바뀌는 값으로 나가고, `MainView`가 카탈로그에서 문구를 제공합니다. 그렇지 않으면 35개 언어로 번역된 애플리케이션에서 상태 줄만 영어로 나올 것입니다. 로그 줄은 예외로, 영어로 유지됩니다.

### 플랫폼 게이트

`ScannerService.isSupportedPlatform()`은 `os.name` 검사 하나이며, `MainView`는 스캐너 컨트롤을 만들기 전에 이것을 확인합니다. macOS와 Linux에는 토글도, 장치 선택기도, 스캐너 메뉴도(따라서 <kbd>⌘K</kbd>도), 상태 레이블도 없습니다. 숨겨진 것이 아니라 아예 만들어지지 않습니다.

Zebra는 JavaPOS 서비스를 Windows용으로만 제공하며, 다른 곳에서는 아무것도 일찍 실패하지 않습니다. jar는 해석되고 컨트롤도 생성되며, 네이티브 계층이 없어서 마침내 실패하는 것은 `open()`인데, 그때는 이미 몇 초가 지나고 대화 상자가 하나 뜬 뒤입니다.

이 규칙은 JavaPOS가 아니라 *서비스*에 관한 것입니다. 크로스 플랫폼 서비스를 제공하는 공급업체라면 여기서도 동작할 것이며, 규칙을 넓히려면 `supportsJavaPos(String)` 하나만 바꾸면 됩니다. 이것은 패키지 전용이며 `os.name` 문자열을 대상으로 테스트되므로, 어느 머신에서 빌드를 실행하든 두 가지 답이 모두 검증됩니다.

### 심볼로지

`Symbologies`는 JavaPOS의 **78**가지 `SCAN_SDT_*` 스캔 데이터 유형 전부를 표시 이름에, 그리고 두 어휘가 서로 대응하는 경우에는 코어의 `DataCarrierType`에 대응시킵니다. 둘 중에서는 JavaPOS가 더 세밀합니다. UPC-A와 EAN-13을, 일반 Data Matrix와 GS1 DataMatrix를 구분하는 반면, AIM은 한 계열 전체를 하나의 코드 문자 아래에 묶습니다.

표에 없는 유형도 쓸 만한 이름을 얻습니다. 공급업체 서비스는 실제로 규격 목록에 없는 값을 보고하기 때문에, `ScannerConst`에 대한 리플렉션으로 이름을 찾아냅니다. 한 테스트는 `ScannerConst`를 훑으며 선언된 유형 중 이름이 없는 것이 있으면 실패합니다. 이는 향후 JavaPOS가 추가하는 상수도 잡아냅니다.

일부 캐리어 대응은 추측하지 않고 의도적으로 `UNKNOWN`으로 남겨 두었습니다. 스트레이트 2 of 5(AIM은 시작/정지 바 개수로 `]R`/`]S`를 나누지만 JavaPOS는 이를 보고하지 않습니다), 4상태 우편 코드, 그리고 TLC-39입니다.

---

## 현지화

레이블은 언어마다 하나의 속성 파일로 `src/main/resources/i18n/ui_<CODE>.properties` 아래에 있으며, 코어의 오류 카탈로그와 같은 코드(`EN`, `FR`, `PCM` 등)를 사용합니다. 35개 모두 존재하며, 35개 모두 정확히 같은 키 집합을 가지고 있습니다. 파일은 `Properties`가 가정하는 ISO-8859-1이 아니라 **UTF-8**로 읽히므로, 번역은 `\uXXXX` 이스케이프가 아니라 읽을 수 있는 형태로 저장됩니다.

**대체(fallback)는 파일 단위가 아니라 키 단위입니다**. 덕분에 일부만 번역된 언어도 첫 번째 키가 들어가는 순간부터 쓸모가 있습니다. 영어가 계약입니다. 모든 키는 영어에 존재해야 합니다.

| 계층 | 키 수 | 범위 |
|---|---|---|
| 공유 보고서 어휘 | 31 | 35개 언어 전부 |
| 데스크톱 화면 틀 — 메뉴, 버튼, 빌더, 옵션, 상태 줄, 보고서 레이아웃, 스캐너 상태, 대화 상자 본문 | 114 | 35개 언어 전부 |

공유 용어 중 여섯 개(`generated`, `status`, `dlRole`, `interpretations`, `message`, `level`)는 이 앱의 보고서에서 쓰이지 않습니다.
이들은 의도적으로 남겨 두었으며, 파일 머리글에 그렇게 적혀 있습니다.

### `--language` 인수

`GaiaDesktopApp.languageOverride()`는 JavaFX 자체의 `getParameters().getNamed()`를 통해 `--language=…`를 읽고, `Languages.fromArgument`로 해석한 뒤(카탈로그 코드, enum 이름 또는 BCP 47 태그, 대소문자 구분 없음) `MainView`에 넘깁니다.

**이 재정의는 파일이 아니라 이번 실행에 대한 것입니다.** `MainView`는 불러온 언어를 `savedLanguage`에 보관하고, `languageChosenInSession`이 사용자가 직접 언어를 골랐음을 알려 줄 때까지는 *그* 언어를 다시 기록합니다. 그 후에는 사용자의 선택이 평소처럼 저장됩니다. 이 분리가 설계의 전부입니다. 키오스크 프로필이나 데모 바로 가기는 머신 주인이 내일 열 때의 언어를 바꿔 쓰지 않고 스페인어로 열 수 있으며, 옵션 패널에서 한 진짜 선택은 그대로 유지됩니다.

어떤 카탈로그에도 해당하지 않는 값은 허용되는 코드와 함께 로그에 기록되고 무시됩니다. 바로 가기의 오타 때문에 시작을 거부하는 쪽이 더 나쁜 실패이며, `Languages.fromArgument`가 예외를 던지지 않고 `null`을 반환하는 것도 같은 이유입니다.

### 언어 추가

코드를 변경할 필요가 없습니다.

1. `ui_<CODE>.properties`를 편집합니다. 모든 언어에 이미 파일이 있습니다.
2. 값을 번역합니다. `{0}` / `{1}` 자리 표시자는 그대로 두십시오.
3. 빌드합니다. 두 개의 테스트가 계약을 강제합니다. 하나는 코드가 영어에 정의되지 않은 키를 요청하면, 다른 하나는 어떤 카탈로그의 키 집합이 영어와 달라지면 실패합니다.

첫 번째 테스트는 클래스를 리플렉션으로 살피지 않고 **소스 코드**에서 키 리터럴을 찾습니다. 키는 호출 지점의 문자열 리터럴로만 존재하기 때문입니다. 그래서 새 메시지는 변수를 통해 조회하지 말고 `strings.format("st.scannerError", …)`처럼 키를 직접 써서 만들어야 합니다. 그렇지 않으면 테스트가 그것을 볼 수 없습니다.

### 의도적으로 번역하지 않는 것

- 원시 값 옆에 고정폭 글꼴로 표시되는 짧은 필드 이름. 요소 카드의 `format`, `position`, `length`, `raw`, `part`, 테이프의 `AI`, `sep`, `pos`, `len`, 그리고 오류 위치를 나타내는 네 개의 셀(`AI`, `position`, `level`, `stage`)이며, 이들의 값은 어차피 번역되지 않는 enum 이름입니다.
- GS1 구조를 가리키는 샘플 입력 이름과, 고유 명사인 심볼로지 이름.
- `GE-C084`의 해결 방법 단락. 어설프게 번역하면 영어보다 못한 표준 문서입니다.
- `ElementBuilder`의 알 수 없는 AI에 대한 최후 방어선. UI에서는 도달할 수 없습니다. 편집기가 알 수 없는 AI를 직접 검사하고 카탈로그에서 보고하기 때문입니다.
- 로그 줄.

빈틈이 하나 남아 있습니다. `ScanSource.label()`은 기록 행과 보고서 머리말에 `manual` / `scanner` / `file` / `built`를 영어로 제공합니다. 이를 메우려면 키 하나로는 부족합니다. `HistoryCell`에는 `UiStrings`가 없고 목록의 셀 팩토리는 한 번만 설치되므로, 언어가 바뀔 때 셀도 새로 고쳐야 합니다.

---

## 저장 상태 형식

`~/.gaia-desktop/` 아래에 두 개의 파일이 있으며, 바뀌는 주기가 다르기 때문에 따로 둡니다. 캡처 로그가 손상되거나 손으로 편집되었다고 해서 사용자가 설정까지 잃어서는 안 되기 때문입니다. 디렉터리는 `-Dgaia.desktop.home=…`로 재정의할 수 있으며, 테스트가 실제 홈 디렉터리를 건드리지 않는 것도 이 방법 덕분입니다.

**`options.properties`** — 옵션이 바뀔 때와 종료할 때 기록됩니다.

| 키 | 의미 |
|---|---|
| `parse.mode`, `parse.language` | 파이프라인 깊이와 메시지 언어 |
| `date.endian`, `date.separator`, `date.month`, `date.year` | 날짜 표시 |
| `check.skipRequires`, `check.skipExcludes` | 구조 검사 건너뛰기 |
| `ui.theme` | `LIGHT` 또는 `DARK` |
| `ui.optionsPane` | 옵션 패널이 열려 있는지 여부 |
| `ui.scannerDevice` | 마지막으로 사용한 JavaPOS 논리 이름 |
| `ui.mode` | `PARSE`, `BUILD` 또는 `COMBINE` |
| `update.check` | 매일 업데이트 확인을 실행할지 여부(기본값 `true`) |
| `update.lastCheck` | 마지막으로 실행한 시각, ISO-8601. 없거나 읽을 수 없으면 확인이 필요한 것으로 간주 |
| `update.skipVersion` | 사용자가 *이 버전 건너뛰기*를 선택한 릴리스. 자동으로 다시 제안되지 않음 |

파일이 없거나 손상되어도 치명적이지 않습니다. 값마다 개별적으로 대체되므로, 잘못된 한 줄은 그 줄만 잃고 다른 것은 잃지 않습니다. 처음 실행할 때 파일이 전혀 없으면, 옵션은 GAIA의 내장 기본값이 아니라 `SystemDefaults`가 머신의 지역 설정을 바탕으로 초기화합니다. 로캘의 짧은 날짜 패턴이 구성 요소의 순서, 구분자, 그리고 월과 연도의 축약 여부를 결정합니다.

`Properties.setProperty`에 null을 넘기지 마십시오. 예외를 던지는데, `persistOptions()`는 UI 이벤트 핸들러에서 실행되므로 예외가 FX 스레드로 새어 나갑니다. 이 앱에서 이미 일어난 실제 충돌입니다.

**`captures.tsv`** — 기록이 바뀔 때와 종료할 때 기록됩니다. 최신 항목이 먼저 오며 최대 **500**개까지 보관합니다.

```
captured(ISO)              <TAB> source  <TAB> symbology   <TAB> input
2026-08-09T14:22:05.123    SCANNER       Data Matrix       0109506000134352\x1D10LOT-001
```

다시 계산할 수 없는 것만 저장합니다. `ParseResult`는 직렬화되지 **않습니다**. 불러올 때 입력을 다시 파싱하여 도출하므로, 복원된 기록은 *지금* 적용 중인 옵션을 반영합니다. 입력에는 FNC1과 스캐너가 내보내는 그 밖의 무엇이든 흔히 들어 있으므로, 입력 필드는 이스케이프되고(`\\`, `\t`, `\n`, `\r`, 그리고 `0x20` 미만의 나머지 모든 문자에는 `\xNN`), 파일은 ISO-8859-1로 바이트 단위 그대로 읽고 씁니다.

날짜를 기록하기 전에 작성된 파일에는 시각만 들어 있습니다. 이런 파일도 여전히 불러올 수 있으며 날짜는 오늘로 매겨집니다. 실제 날짜는 한 번도 기록된 적이 없기 때문입니다.

---

## 테스트

```bash
(cd gaia-desktop && mvn test)
```

테스트는 198개이며, 의도적으로 **JavaFX 툴킷에 의존하지 않도록** 만들어 빠르고 헤드리스로 실행됩니다. 보고서 렌더러, 빌드 로직과 AI 조회, `{GS}` 변환, 로캘에서 도출한 기본값, 두 저장소(`0x1D`가 들어 있는 왕복 테스트와 일부러 손상시킨 파일 포함), 심볼로지 표, 스캐너 플랫폼 규칙, 버전 스텁, 그리고 카탈로그를 다룹니다.

그중 세 개는 기능이 아니라 계약을 지키기 때문에 알아 둘 가치가 있습니다.

| 테스트 | 실패 조건 |
|---|---|
| `CatalogueCoverageTest` | 코드가 `ui_EN.properties`에 정의되지 않은 키를 요청하거나, 35개 파일 중 하나라도 영어 키 집합과 달라짐 |
| `SymbologiesTest` | JavaPOS가 선언한 `SCAN_SDT_*` 상수 중 표시 이름이 없는 것이 있음 — JavaPOS 업그레이드로 추가된 것 포함 |
| `ScannerServiceTest` | 플랫폼 규칙이 Windows 또는 그 밖의 시스템에 대해 다른 답을 냄 |

`UiStrings`는 실행 시 의도적으로 관대합니다. 알 수 없는 키는 레이아웃 도중에 예외를 던지지 않고 키 자체로 표시됩니다. 바로 그 때문에 카탈로그 테스트가 반드시 있어야 합니다.

**다루지 않는 것:** 실제 툴킷이 필요한 모든 것, 즉 포커스 동작, 모드 전환, 표 편집, 언어 전환, 그리고 [스레드](#스레드)에서 설명한 두 충돌입니다. 이들은 실제 창을 조작하여 검증했지만, 회귀가 생겨도 빌드는 실패하지 않습니다. TestFX나 Monocle을 추가하면 그 빈틈을 메울 수 있습니다.

---

## 빌드 문제 해결

| 증상 | 원인과 해결 방법 |
|---|---|
| `package tools.pantheum.gaia does not exist` | 코어가 설치되지 않았습니다. `(cd gaia && mvn install -DskipTests)` |
| *"JavaFX runtime components are missing"* | jar의 `Main-Class`는 `GaiaDesktopApp`이 아니라 `Launcher`여야 합니다 |
| 시작 시 *"Unsupported JavaFX configuration: classes were loaded from unnamed module"* | 예상된 것이며 무해합니다. JavaFX가 클래스 경로에 있다는 것을 알리는 것일 뿐입니다. `Launcher`가 존재하는 이유가 바로 이것입니다 |
| jar가 시작되지 않음, `NoClassDefFoundError` | 기본 jar에는 이 모듈의 클래스만 들어 있습니다. `dist/lib/`가 그 옆에 있어야 합니다. 독립된 단일 파일이 필요하면 `-Pfat`을 사용하십시오 |
| Eclipse에서 `Artifact has not been packaged yet … MDEP-187` | m2e 작업 공간 해석 때문입니다. [패키징](#패키징) 아래의 참고를 보십시오 |
| 빌드에 중복된 `… 2.properties` 파일이 나타남 | 프로젝트가 iCloud로 동기화되는 폴더에 있어 빌드 도중 동기화가 충돌 사본을 만들었습니다. 항상 `mvn clean package`를 쓰고, 가능하면 `target/`을 동기화에서 제외하십시오 |
| 메뉴 항목에 `act.hri` 같은 키가 표시됨 | 그 키가 `ui_EN.properties`에 없습니다. 빌드에서 잡혔어야 합니다. [테스트](#테스트)를 참조하십시오 |

---

## 알려진 제한 사항과 남은 작업

- **헤드리스 UI 테스트가 없습니다.** [테스트](#테스트)를 참조하십시오.
- **플랫폼별 산출물.** 두 패키징 모두 한 플랫폼의 JavaFX 네이티브를 번들합니다. `-Dfx.platform`으로 대상마다 하나씩 빌드하십시오(어느 머신에서든 전부 빌드할 수 있습니다). 어느 쪽도 Java 런타임은 번들하지 않습니다. 그러려면 `jpackage`가 필요한데, 이것은 자신이 실행되는 OS용으로만 빌드합니다.
- **스캐너 입력은 Windows 전용입니다.** [플랫폼 게이트](#플랫폼-게이트)를 참조하십시오.
- **`ScanSource.label()`은 현지화되어 있지 않습니다.** [의도적으로 번역하지 않는 것](#의도적으로-번역하지-않는-것)을 참조하십시오.
- **`UiState.mode`는 `String`입니다**(`"PARSE"` / `"BUILD"`). 반면 그 옆의 `Theme`은 enum이어서 이 리터럴이 여러 곳에 나타납니다. enum으로 바꾸면 이들이 사라집니다.
- **Digital Link 대상은 도메인과 경로 접두사뿐입니다.** `BuilderDigitalLinkConfig`는 추가 쿼리 매개변수와 프래그먼트도 지원하지만, 편집기는 이를 노출하지 않습니다.
- **상호 배타적인 AI 쌍(`GE-S006`)은 알 수 없는 AI나 중복 AI처럼 편집기에서 표시되지 않습니다**. 빌드가 그냥 실패합니다.
- **코어의 오류 카탈로그는 따옴표를 두 번 씁니다**(`value ''09506000134350''`). 이것이 빌드 실패와 보고서에 그대로 나타납니다. `MessageFormat`의 따옴표 이스케이프가 해제되지 않는 것으로 보이며, 수정은 코어에서 이루어져야 합니다.
