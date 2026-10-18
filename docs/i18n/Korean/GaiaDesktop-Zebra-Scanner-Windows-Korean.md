# Zebra 스캐너 설정 — Windows용 GAIA Desktop

GAIA Desktop이 Zebra 바코드 스캐너에서 GS1 바코드를 직접 읽을 수 있도록 스캐너를 설치하고 설정하는
방법입니다. 스캔할 때마다 파싱되어 기록에 추가되며, AIM Code ID 접두사와 모든 FNC1 구분자가 그대로
유지됩니다. 원문(영어):
[GaiaDesktop-Zebra-Scanner-Windows.md](../../GaiaDesktop-Zebra-Scanner-Windows.md).

GAIA Desktop은 USB로 연결한 **Zebra DS8108** 핸드헬드 이미저로 개발 및 테스트되었습니다. 이 단계는
Zebra Scanner SDK가 지원하는 모든 Zebra 2D 스캐너에 적용됩니다. 특정 모델의 설정 바코드와 메뉴
이름은 해당 모델의 Quick Reference Guide 또는 Product Reference Guide에 있습니다.

> **Windows 전용.** Zebra의 스캐너 드라이버는 Windows용만 있으므로, macOS나 Linux에서는
> GAIA Desktop이 스캐너 관련 컨트롤을 전혀 만들지 않습니다. 앱의 나머지 기능은 모든 플랫폼에서
> 동작합니다.

---

## 목차

1. [필요한 것](#1-필요한-것)
2. [Java 설치](#2-java-설치)
3. [Zebra Scanner SDK 설치](#3-zebra-scanner-sdk-설치)
4. [123Scan 설치](#4-123scan-설치)
5. [Zebra 스캐너 펌웨어 업데이트](#5-zebra-스캐너-펌웨어-업데이트)
6. [Zebra 스캐너의 USB 모드 설정](#6-zebra-스캐너의-usb-모드-설정)
7. [GS1용 Zebra 스캐너 설정](#7-gs1용-zebra-스캐너-설정)
8. [GAIA Desktop 설치 및 시작](#8-gaia-desktop-설치-및-시작)
9. [스캐너 연결](#9-스캐너-연결)
10. [설정 참조](#10-설정-참조)
11. [문제 해결](#11-문제-해결)
12. [지원되는 Zebra 2D 스캐너](#12-지원되는-zebra-2d-스캐너)

---

## 1. 필요한 것

| | |
|---|---|
| **스캐너** | Zebra 2D 스캐너와 USB 케이블 — [지원되는 Zebra 2D 스캐너](#12-지원되는-zebra-2d-스캐너) 참조 |
| **컴퓨터** | Windows 10 또는 11, 64비트 |
| **Java** | Java 21 이상, **64비트** |
| **Zebra Scanner SDK for Windows** | **64비트**, Zebra 지원 사이트에서 — GAIA Desktop이 사용하는 드라이버와 JavaPOS 서비스를 제공합니다 |
| **Zebra 123Scan** | Zebra의 무료 설정 유틸리티, 지원 사이트에서 — 스캐너 펌웨어 업데이트와 설정 확인에 사용합니다 |
| **GAIA Desktop** | `gaia-desktop-1.0.0-win.zip` |
| **선택 사항** | 설정 바코드가 들어 있는 스캐너의 **Product Reference Guide**(PRG) |

구성 요소가 맞물리는 방식:

```
Zebra scanner ──USB──► Zebra CoreScanner driver ──► Zebra JavaPOS services ──► GAIA Desktop
                       (installed by the SDK)        (in the SDK's JPOS\bin)    (run.bat puts them on
                                                                                 the class path)
```

GAIA Desktop은 JavaPOS API와, 스캐너에 이름을 붙이는 설정(`gaia-desktop.jar` 안의 `jpos.xml`)을
직접 포함합니다. 나머지는 Zebra SDK가 제공합니다. 스캐너와 통신하는 드라이버와, JavaPOS와 그
드라이버 사이에 있는 Zebra 서비스 클래스입니다.

---

## 2. Java 설치

GAIA Desktop에는 **Java 21 이상**이 필요하며, 64비트 Zebra 드라이버에 맞춰 **64비트** 버전이어야
합니다.

1. Windows x64용 Java 21 JDK 또는 JRE를 설치합니다 — 예: adoptium.net의 Eclipse Temurin.
   설치 프로그램이 `JAVA_HOME` 설정을 제안하면 설정하도록 두십시오.
2. 명령 프롬프트에서 확인합니다:

   ```
   java -version
   ```

   첫 줄에 버전 21 이상이 표시되어야 합니다. 64비트 Java는 `64-Bit Server VM`도 표시합니다.

`run.bat`은 다음 순서로 Java를 찾습니다. `run.bat` 옆의 `jdk*` 폴더, 그다음 `JAVA_HOME`, 그다음
`PATH`의 `java`. 21보다 오래된 버전은 그 사실을 알리는 메시지와 함께 거부합니다.

---

## 3. Zebra Scanner SDK 설치

1. Zebra 지원 사이트에서 **Zebra Scanner SDK for Windows(64비트)**를 다운로드합니다.
2. 설치 프로그램을 실행합니다. 구성 요소를 선택할 수 있으면 **JPOS**(JavaPOS) 구성 요소가 포함되어
   있는지 확인하십시오.
3. 기본 설치 위치를 그대로 둡니다. GAIA Desktop은 SDK의 JavaPOS 부분을 다음 위치에서 찾습니다:

   ```
   C:\Program Files\Zebra Technologies\Barcode Scanners\Scanner SDK\JPOS\
   ```

   그리고 그 `bin\` 폴더에 Zebra의 서비스 jar(`JposServiceScanner.jar`, `JposServiceJniScanner.jar`
   등)와 네이티브 라이브러리(`CSJPOSScanner64.dll` 등)가 있다고 가정합니다. SDK를 다른 위치에
   설치했다면 [ZEBRA_HOME](#zebra_home)을 참조하십시오.
4. 설치 후 **Windows를 다시 시작**합니다. SDK의 CoreScanner 드라이버는 백그라운드에서 실행되며,
   GAIA Desktop이 스캐너에 접근하기 전에 실행 중이어야 합니다. 이를 확실히 하는 가장 간단한 방법이
   다시 시작입니다.

---

## 4. 123Scan 설치

123Scan은 Zebra 스캐너를 설정하기 위한 Zebra의 무료 유틸리티입니다. 다음 단계에서 스캐너 펌웨어를
업데이트하는 데 사용하며, 이후에도 스캐너 설정을 확인하고 변경하는 가장 쉬운 방법입니다.

1. Zebra 지원 사이트에서 **123Scan**을 다운로드하고 설치 프로그램을 실행합니다.
2. Zebra 스캐너를 연결하고 123Scan을 시작합니다. 123Scan이 스캐너를 찾아 모델, 일련번호, 현재
   펌웨어 버전을 보여 주어야 합니다. 그렇지 않으면 다음으로 진행하기 전에 다른 USB 포트나 케이블을
   시도하십시오 — 123Scan이 보지 못하는 스캐너는 GAIA Desktop도 보지 못합니다.

---

## 5. Zebra 스캐너 펌웨어 업데이트

설정하기 전에 스캐너 펌웨어를 최신 버전으로 올리십시오. 펌웨어 업데이트는 디코딩 문제를 고치고,
스캐너를 SDK가 설치한 Zebra 드라이버와 맞는 상태로 유지합니다.

1. **GAIA Desktop**과 스캐너를 사용 중일 수 있는 다른 프로그램을 모두 닫습니다. 업데이트 중에는
   123Scan이 스캐너를 독점해야 합니다.
2. 스캐너가 연결된 상태에서 123Scan의 펌웨어 업데이트 옵션을 선택합니다. 123Scan은 스캐너의
   펌웨어를 Zebra가 공개한 최신 버전과 비교하고, 다운로드 및 설치를 제안합니다. 컴퓨터가 인터넷에
   연결되지 않았다면 Zebra 사이트의 해당 스캐너 지원 페이지에서 펌웨어를 다운로드하여 123Scan에
   불러오십시오.
3. 업데이트를 시작하고 **123Scan이 완료를 알릴 때까지 스캐너를 연결해 두십시오**. 몇 분이 걸리며,
   마지막에 스캐너가 다시 시작됩니다. 도중에 분리하면 스캐너를 사용할 수 없게 될 수 있습니다.
4. 123Scan에 새 펌웨어 버전이 표시되는지 확인합니다.

업데이트하면 일부 설정이 기본값으로 돌아갈 수 있습니다. 그래서 스캐너 설정은 펌웨어 업데이트 후,
다음 두 단계에서 합니다.

---

## 6. Zebra 스캐너의 USB 모드 설정

가장 자주 빠뜨리는 단계입니다. **출고 상태의 Zebra 스캐너는 USB 키보드처럼 동작합니다**(USB HID
Keyboard 모드). 각 바코드를 포커스가 있는 곳에 "입력"합니다. 이 모드에서는 GAIA Desktop의
*스캐너 연결*이 스캐너를 찾을 수 없고, 텍스트 필드에 입력된 내용은 FNC1 구분자를 잃어 GS1 요소
문자열의 의미가 바뀝니다.

스캐너를 Zebra 드라이버가 직접 통신하는 모드로 바꾸십시오:

| 스캐너 USB 모드 | GAIA Desktop 장치 이름 | 비고 |
|---|---|---|
| **IBM Hand-held USB** | `ZebraUSBHandHeld` 또는 `ZebraAllScanners` | 권장 |
| SNAPI(이미징 포함 또는 미포함) | `ZebraScannerSNAPI` 또는 `ZebraAllScanners` | Zebra 고유의 USB 프로토콜, 역시 동작합니다 |
| IBM Table-top USB | `ZebraUSBTableTop` 또는 `ZebraAllScanners` | 탁상형 스캐너용 |
| USB OPOS (Hand-held) | `ZebraUSBOPOS` 또는 `ZebraAllScanners` | |
| USB HID Keyboard(공장 기본값) | — | **연결되지 않음** |

GAIA Desktop의 기본 장치 이름인 `ZebraAllScanners`는 지원되는 모든 모드의 스캐너를 받아들이므로,
모드를 설정하고 나면 보통 기본값으로 충분합니다.

모드를 바꾸는 방법은 두 가지입니다:

- 스캐너의 Quick Reference Guide 또는 Product Reference Guide(USB Host Types / USB Device Type
  절)에서 해당 모드 — 예: *IBM Hand-held USB* — 의 **설정 바코드를 스캔합니다**. 스캐너가 비프음으로
  확인합니다. 또는
- **123Scan을 사용합니다**([4단계](#4-123scan-설치)). 스캐너를 연결하고 설정을 열어 USB 호스트
  유형을 지정합니다.

모드를 바꾸면 Windows가 스캐너를 다른 종류의 장치로 다시 인식합니다. 연결하기 전에 몇 초 기다리십시오.

나중에 스캐너를 다시 키보드 모드로 되돌리려면 — 다른 소프트웨어에서 쓰기 위해 — *USB Keyboard (HID)*
설정 바코드를 스캔하거나 공장 기본값으로 복원하십시오.

---

## 7. GS1용 Zebra 스캐너 설정

GAIA Desktop은 스캐너가 어떤 종류의 바코드를 읽었는지 알려 줄 때 가장 잘 파싱합니다.

### AIM Code ID 전송

Product Reference Guide(User Preferences)의 설정 바코드나 123Scan에서 **Transmit Code ID Character**를
**AIM Code ID Character**로 설정합니다. 그러면 각 스캔이 심볼로지 식별자 — GS1-128은 `]C1`,
GS1 DataMatrix는 `]d2`, GS1 QR Code는 `]Q3`, GS1 DataBar는 `]e0` — 로 시작하며, GAIA Desktop은
이를 이용해 데이터 캐리어를 식별하고 그 규칙을 적용합니다.

이것이 없어도 GAIA Desktop은 요소 문자열을 파싱하고 심볼로지를 스캔 자체에서 가져오지만, 보고서에
표시할 AIM Code ID가 없고, EAN-13 같은 단순한 바코드를 GS1 요소 문자열과 구별하기가 더 어려워집니다.

### 심볼로지 확인

사용하는 바코드 종류가 **활성화**되어 있는지 확인하십시오. 일반적으로 GS1-128, GS1 DataMatrix,
GS1 DataBar, GS1 QR Code, EAN/UPC입니다. Zebra 2D 스캐너에서는 대부분 기본으로 켜져 있습니다.
123Scan에서 현재 설정을 볼 수 있고, Product Reference Guide에 각각을 활성화하는 바코드가 있습니다.

### Zebra의 GS1 파싱 기능은 꺼 두기

최근 펌웨어에는 스캐너 자체에서 동작하는 GS1 기능 — **Label Parse+**, **GS1 Digital Link**,
**GS1 Retail POS parsing** — 이 추가되었습니다. 출고 상태 그대로 **꺼** 두십시오. 이 기능들은 스캐너가
보내는 내용을 다시 서식화하거나 줄이며, GAIA Desktop은 파싱과 검증을 위해 원시 데이터가 필요합니다.
각 기능이 하는 일은 [지원되는 Zebra 2D 스캐너](#12-지원되는-zebra-2d-스캐너)를 참조하십시오.

### 접두사와 접미사는 붙이지 않기

스캐너 출력에 접두사, 접미사, Enter 키를 붙이지 마십시오. GAIA Desktop은 각 스캔을 JavaPOS를 통해
완전한 메시지로 받으므로 이것들이 필요 없습니다. 여분의 문자는 요소 문자열에 들어가게 됩니다.

---

## 8. GAIA Desktop 설치 및 시작

1. `gaia-desktop-1.0.0-win.zip`을 예를 들어 `C:\Tools\`에 압축 해제합니다. 폴더 하나가 들어 있습니다:

   ```
   gaia-desktop-1.0.0-win\
   ├── docs\
   │   └── GaiaDesktop-Zebra-Scanner-Windows.md      this guide
   ├── gaia-desktop.jar
   ├── lib\
   ├── readme.html                                   index of every guide
   └── run.bat
   ```

2. 스캐너를 연결합니다.
3. **`run.bat`**을 두 번 클릭합니다. 앱 옆에 콘솔 창이 열려 계속 열려 있으며, 스캐너 상태와 JavaPOS
   오류가 그곳에 표시됩니다.
4. 콘솔의 첫 줄을 읽습니다. `run.bat`이 무엇을 찾았는지 알려 줍니다:

   ```
   run.bat: Java 21.0.8 from JAVA_HOME; JavaFX from lib\; JavaPOS from lib\; Zebra services from C:\Program Files\Zebra Technologies\Barcode Scanners\Scanner SDK\JPOS\
   ```

   끝이 **`Zebra services from none (Zebra Scanner SDK not installed)`**이면 SDK를 기본 위치에서 찾지
   못한 것입니다 — SDK를 설치하거나([3단계](#3-zebra-scanner-sdk-설치))
   [ZEBRA_HOME](#zebra_home)을 설정하십시오. 앱은 시작되지만 스캐너는 연결할 수 없습니다.

---

## 9. 스캐너 연결

1. 도구 모음의 **장치 선택 목록**에는 GAIA Desktop이 아는 스캐너 이름이 나열됩니다.
   **`ZebraAllScanners`**로 두거나, [6단계](#6-zebra-스캐너의-usb-모드-설정)의 표에서 스캐너의
   USB 모드에 맞는 이름을 고르십시오.
2. **스캐너 연결**을 클릭하거나 **Ctrl+K**를 누릅니다(**스캐너** 메뉴에도 있습니다).
3. 상태 표시줄이 **`스캐너: ZebraAllScanners`**(또는 선택한 이름)로 바뀌고, 상태 줄에
   *스캐너 연결됨 — ZebraAllScanners*가 표시됩니다. 버튼은 이제 **스캐너 연결 해제**로
   표시됩니다.
4. GS1 바코드를 스캔합니다. 캡처 기록 맨 위에 나타나고 그 보고서가 열립니다. AIM Code ID가 켜져
   있으면 보고서에 데이터 캐리어 — *GS1-128*, *GS1 DataMatrix* — 가 표시되고, 요소 문자열은 모든 FNC1
   구분자를 유지합니다.

장치 선택 목록은 실행 간에 선택을 기억합니다. 스캐너는 연결을 해제하거나 앱을 닫을 때까지 연결된
상태로 유지됩니다. 스캔은 **생성** 모드와 **결합** 모드에서도 동작합니다.
결합 모드에서는 각 스캔이 결합되는 바코드 중 하나가 됩니다.

GAIA Desktop이 스캐너를 점유하는 동안 다른 프로그램은 스캐너를 사용할 수 없고, 그 반대도 마찬가지입니다.
연결하기 전에 123Scan과 다른 POS 또는 스캐너 소프트웨어를 닫으십시오.

---

## 10. 설정 참조

### 장치 이름

장치 선택 목록은 `gaia-desktop.jar`에 포함된 `jpos.xml`에서 채워집니다. 그 안의 스캐너 항목:

| 장치 이름 | 스캐너 유형 | 용도 |
|---|---|---|
| `ZebraAllScanners` | 모두 | **기본값.** 지원되는 모든 USB 모드 |
| `ZebraAllScanners114` | 모두 | 같은 서비스의 JavaPOS 1.14 버전 |
| `ZebraUSBHandHeld` | IBM Hand-held USB | IBM Hand-held USB 모드의 스캐너 |
| `ZebraUSBTableTop` | IBM Table-top USB | 탁상형 모드 |
| `ZebraUSBOPOS` | USB OPOS | USB OPOS 모드 |
| `ZebraScannerSNAPI` | SNAPI | SNAPI 모드 |
| `ZebraScannerUSB` | 모든 USB | 모든 USB 모드 |
| `ZebraScannerSerialSSI` | 직렬 SSI | 직렬 케이블 스캐너(USB 아님) |
| `ZebraScannerSerialNIXMODB` | 직렬 NIXMODB | 직렬 케이블 스캐너 |
| `ZebraVDI` | VDI | 가상 데스크톱으로 전달되는 스캐너 |

### ZEBRA_HOME

Zebra SDK를 기본 위치가 아닌 곳에 설치했다면, `run.bat`을 시작하기 전에 `ZEBRA_HOME`을 그 `JPOS`
폴더로 설정하십시오 — 예를 들어 옆에 둔 작은 배치 파일에서:

```bat
@echo off
set "ZEBRA_HOME=D:\Zebra\Scanner SDK\JPOS\"
call "%~dp0run.bat" %*
```

그러면 `run.bat`은 Zebra의 서비스 jar를 `%ZEBRA_HOME%bin`에서 가져오고, Zebra DLL을 위해 그 폴더를
`PATH`에 추가합니다.

### 사용자 지정 jpos.xml

장치 이름을 추가하거나 항목의 설정을 바꾸려면 `gaia-desktop.jar`에서 `jpos.xml`을 꺼내(jar는
zip이므로 어떤 압축 해제 도구로도 열 수 있습니다) 사본을 편집하고 GAIA Desktop이 그것을 쓰도록
지정하십시오:

```bat
set GAIA_DESKTOP_OPTS=-Djpos.config.populatorFileURL=file:///C:/Tools/jpos.xml
run.bat
```

그러면 장치 선택 목록에 그 파일의 스캐너가 나열됩니다. Zebra 스캐너를 여러 대 연결한 경우,
`ZebraAllScanners` 항목처럼 `ScannerModel` 또는 `SerialNumber` 속성으로 항목을 그중 한 대에 연결할
수 있습니다.

### 기타 옵션

| 설정 | 방법 |
|---|---|
| 앱 언어 | `run.bat --language=FR`(이번 실행만), 또는 옵션 패널 |
| 추가 Java 옵션 | `run.bat` 전에 `set GAIA_DESKTOP_OPTS=…` |
| 함께 제공하는 Java | `run.bat` 옆에 `jdk-21…` 폴더를 둡니다. `JAVA_HOME`보다 먼저 사용됩니다 |

---

## 11. 문제 해결

오류는 연결할 때 대화 상자, 콘솔 창, 그리고 `run.bat`을 시작한 폴더(`run.bat`을 두 번 클릭한
경우 GAIA Desktop 폴더)의 로그 파일 `logs\gaia-desktop.log`에 나타납니다. 로그의 대괄호 안 숫자가
JavaPOS 오류 코드입니다.

| 보이는 현상 | 가능한 원인 | 조치 |
|---|---|---|
| *스캐너 연결* 버튼이 없음 | Windows에서 실행 중이 아님 | 스캔은 Windows에서만 가능합니다 |
| 123Scan이 스캐너를 찾지 못함 | 케이블, USB 포트, 또는 다른 프로그램이 스캐너를 점유 중 | 다른 케이블이나 포트를 시도하고, GAIA Desktop과 다른 스캐너 소프트웨어를 닫으십시오. 123Scan이 보지 못하는 스캐너는 GAIA Desktop도 보지 못합니다 |
| 콘솔: `Zebra services from none (Zebra Scanner SDK not installed)` | SDK가 기본 위치에 없음 | 64비트 SDK를 설치하거나([3단계](#3-zebra-scanner-sdk-설치)) [ZEBRA_HOME](#zebra_home)을 설정 |
| 오류 **104**: *Could not connect to service with logicalName = …: …SymScannerSvc112Factory* | Zebra의 서비스 jar를 찾지 못함 | 위와 같음 — 콘솔의 `Zebra services from` 줄을 확인 |
| 오류 **109**: *Service does not exist in loaded JCL registry* | 장치 이름이 스캐너 설정에 없거나, SDK의 `javaPOS114.jar`가 class path에 들어감 | 장치 선택 목록에서 이름을 고르고, `javaPOS114.jar`는 추가하지 마십시오. 사용자 지정 `jpos.xml`을 쓴다면 그 이름이 있는지 확인 |
| 오류 **107**(하드웨어 없음), 또는 스캐너를 연결했는데도 연결 실패 | 스캐너가 아직 USB 키보드 모드이거나 CoreScanner 드라이버가 실행되지 않음 | USB 모드 설정([6단계](#6-zebra-스캐너의-usb-모드-설정)). SDK 설치 후 Windows 다시 시작. 다른 USB 포트 시도 |
| 연결 시 오류 **112**(시간 초과) 또는 **102**(점유됨) | 다른 프로그램이 스캐너를 점유 중 | 123Scan과 다른 스캐너 또는 POS 소프트웨어를 닫은 뒤 다시 연결 |
| 콘솔에 `UnsatisfiedLinkError` … `CSJPOSScanner64` | Zebra DLL을 불러올 수 없음 — 흔히 64비트 Java에 32비트 SDK | 64비트 SDK를 설치하고 `java -version`이 64비트 VM을 보고하는지 확인 |
| 스캔이 캡처로 들어오지 않고 입력 표시줄에 입력됨 | 스캐너가 USB 키보드 모드임 | USB 모드 설정([6단계](#6-zebra-스캐너의-usb-모드-설정)) |
| 연결되었지만 스캔이 들어오지 않음 | 스캐너 모드에 맞지 않는 장치 이름, 또는 연결 중에 스캐너를 다시 꽂음 | 연결을 해제하고 `ZebraAllScanners`를 선택한 뒤 다시 연결 |
| 보고서에 AIM Code ID나 데이터 캐리어가 없음 | AIM Code ID 전송이 꺼져 있음 | *Transmit Code ID Character → AIM Code ID*를 켬([7단계](#7-gs1용-zebra-스캐너-설정)) |
| 요소 문자열에 구분자가 없거나 AI가 붙어 있음 | 키보드 모드에서 스캔했거나 `{GS}` 없이 직접 입력함 | JavaPOS USB 모드를 사용하고, 직접 입력할 때는 `{GS}` 버튼으로 구분자를 넣으십시오 |
| 어떤 바코드 종류가 전혀 읽히지 않음 | 스캐너에서 그 심볼로지가 비활성화됨 | 활성화([7단계](#7-gs1용-zebra-스캐너-설정)) |

문제가 계속되면 콘솔의 `run.bat:` 줄, 오류 대화 상자의 문구, `logs\gaia-desktop.log`를 함께
보관하십시오. 이것들로 어떤 Java, 어떤 Zebra 서비스, 어떤 장치 이름이 쓰였는지, 그리고 연결이 정확히
어디서 실패했는지 알 수 있습니다.

---

## 12. 지원되는 Zebra 2D 스캐너

GAIA Desktop에는 GS1 데이터가 인쇄되는 바코드 — GS1-128, GS1 DataBar, EAN/UPC, 그리고 대부분의
용도에서 2D 코드인 **GS1 DataMatrix**와 **GS1 QR Code**(**GS1 Digital Link** URI도 보통 이 형태로
인쇄됩니다) — 를 읽을 수 있는 스캐너가 필요합니다. 즉 Zebra **2D 이미저**입니다. 선형(1D) 스캐너는
DataMatrix나 QR 코드를 전혀 읽지 못합니다.

스캐너 자체가 GS1 Application Identifier나 Digital Link를 이해할 필요는 없습니다.
**GAIA Desktop이 둘 다 파싱합니다**. 스캐너가 읽은 그대로의 스캔 — AIM Code ID와 FNC1 구분자 포함 —
을 받아 AI로 나누거나, Digital Link URI를 그 AI로 직접 변환합니다.

### 테스트됨

| 스캐너 | 유형 | 상태 |
|---|---|---|
| **DS8108** | 유선 핸드헬드 2D 이미저 | GAIA Desktop으로 **개발 및 테스트됨** |

### 동작할 것으로 예상됨

DS8108처럼 Zebra의 Scanner SDK와 JavaPOS 서비스로 구동되는 Zebra 2D 이미저입니다. GAIA Desktop과
함께 **테스트되지 않았습니다**. 각각 이 가이드대로 설정하되, 설정 바코드는 해당 모델의 Quick
Reference Guide 또는 Product Reference Guide를 사용하십시오.

| 제품군 | 모델(예) |
|---|---|
| DS81x8 | DS8108(유선), DS8178(무선) |
| DS22x8 | DS2208(유선), DS2278(무선) — 범용 |
| DS36x8 | DS3608(유선), DS3678(무선) — 견고형, KD 및 XR 변형 포함 |
| DS46x8 | DS4608(유선), DS4678(무선) |
| DS9308 / DS9908 | 프레젠테이션형 및 핸즈프리 스캐너 |
| MP7000 / MP7200 | 다면 바이옵틱 스캐너 및 스캐너 저울 |
| CS6080 | 무선 컴패니언 스캐너 |

무선 스캐너는 크래들과 통신하고, 컴퓨터에 연결하는 것은 크래들입니다. 크래들을 USB로 연결하고 그
쌍을 한 대의 스캐너로 취급하십시오. 다른 Zebra 제품군의 스캐너도 Zebra의 Scanner SDK가
[6단계](#6-zebra-스캐너의-usb-모드-설정)의 USB 모드 중 하나로 지원한다면 동작할 수 있습니다.

### Zebra 스캐너 내장 GS1 기능

바코드를 읽는 것과는 별도로, 일부 Zebra 2D 이미저는 123Scan으로 설정하여 스캐너 안에서 GS1 데이터를
처리할 수 있습니다:

| 기능 | 하는 일 | GAIA Desktop의 경우 |
|---|---|---|
| **Label Parse+** | 스캐너 안에서 GS1 라벨의 AI를 파싱하고 다시 서식화한 필드를 보냄 | **꺼 두기** — GAIA Desktop이 AI, FNC1 구분자, AIM Code ID를 더 이상 볼 수 없음 |
| **GS1 Digital Link** | 1D EAN/UPC와 2D Digital Link 코드가 모두 붙은 소매 상품용: 어느 쪽을 보낼지 선택 | **꺼 두기**(기본값) |
| **GS1 Retail POS parsing** | Digital Link URI 구문이나 요소 문자열 구문의 소매용 2D 바코드를 읽음 | **꺼 두기** |

**GS1 Digital Link** 설정에는 세 가지 모드가 있습니다. **Mode 1 — GTIN only**는 켜자마자 적용되는
모드로, 어느 바코드에서 왔든 GTIN만 보냅니다. 따라서 Digital Link에 든 로트, 유효기간, 일련번호는
GAIA Desktop에 전혀 도달하지 않습니다. **Mode 2 — Prioritize 2D**는 우선순위 시간 제한(기본 200 ms)
안에 2D 코드가 보이면 그것을, 아니면 1D를 보냅니다. **Mode 3 — Corroborate 1D and 2D GTINs Match**는
1D를 보내고, GTIN이 일치하면 2D도 보냅니다. 다른 소프트웨어 때문에 이 설정을 켜 두어야 한다면
Mode 1이 아니라 Mode 2를 사용하십시오. DS8108에서는 파라미터 2373(켬/끔), 2374(모드), 2491(시간
제한)입니다.

이 기능들을 쓰려면 **123Scan 버전 6 이상**과 해당 기능이 들어 있는 펌웨어가 필요합니다. Zebra는
2025년 4월부터 펌웨어 업데이트로 GS1 Digital Link 지원을 추가해 왔습니다. 아래 날짜는 Zebra
리셀러가 밝힌 것이므로, 각 모델의 펌웨어 릴리스 노트를 확인하십시오:

| 모델 제품군 | GS1 Digital Link 펌웨어 출시일 |
|---|---|
| DS81x8 | 2025년 4월 16일 |
| MP7000 | 2025년 4월 21일 |
| DS9908 | 2025년 6월 6일 |
| SP72 / SM72 | 2025년 7월 11일 |
| DS46x8 | 2025년 7월 28일 |
| MP7200 (Rev-C), DS5502 | 2025년 7월 30일 |
| DS9308 | 2025년 8월 29일 |
| DS22x8, DS1000 | 2025년 9월 5일(아시아 태평양 지역만) |
| DS36x8(KD 및 XR 포함), MS47 | 2025년 9월 30일 |
| PL5000, SE4107 | 2025년 10월 15일 |
| CS6080 | 2025년 12월 31일 |

GS1 Digital Link 코드를 GAIA Desktop으로 읽어 들이는 데 이 기능들은 하나도 필요하지 않습니다. 기능을
꺼 둔 상태에서도 최신 펌웨어의 Zebra 2D 스캐너는 Digital Link QR 코드를 다른 코드와 똑같이 읽고,
GAIA Desktop이 URI를 그 AI로 변환합니다.

출처: Zebra, *DS8108 Product Reference Guide*, MN-002926-11EN Rev A(2025년 8월), 12장
"GS1 Digital Link Parameters" 및 Data Parsing 장; Zebra, *123Scan release notes*;
Barcode Factory, *Upgrade Zebra Scanners for GS1 Digital Link*(펌웨어 날짜).
