# Zebra 掃描器設定 — Windows 版 GAIA Desktop

點樣安裝同設定 Zebra 條碼掃描器，等 GAIA Desktop 可以直接由佢度讀 GS1 條碼：每次掃描都會解析同加入
記錄，AIM Code ID 前綴同每個 FNC1 分隔符都原封不動。原文：
[GaiaDesktop-Zebra-Scanner-Windows.md](../../GaiaDesktop-Zebra-Scanner-Windows.md)。

GAIA Desktop 係用一部經 USB 連接嘅 **Zebra DS8108** 手持影像掃描器嚟開發同測試。呢啲步驟適用於
Zebra Scanner SDK 支援嘅任何 Zebra 2D 掃描器；個別型號嘅設定條碼同選單名稱，喺佢自己嘅 Quick
Reference Guide 或者 Product Reference Guide 度搵到。

> **淨係 Windows。** Zebra 嘅掃描器驅動程式淨係有 Windows 版，所以喺 macOS 或者 Linux 上面，
> GAIA Desktop 唔會整任何掃描器控制項。程式其他嘢喺所有平台都用得。

---

## 目錄

1. [你需要乜嘢](#1-你需要乜嘢)
2. [安裝 Java](#2-安裝-java)
3. [安裝 Zebra Scanner SDK](#3-安裝-zebra-scanner-sdk)
4. [安裝 123Scan](#4-安裝-123scan)
5. [更新 Zebra 掃描器韌體](#5-更新-zebra-掃描器韌體)
6. [設定 Zebra 掃描器嘅 USB 模式](#6-設定-zebra-掃描器嘅-usb-模式)
7. [為 GS1 設定 Zebra 掃描器](#7-為-gs1-設定-zebra-掃描器)
8. [安裝同啟動 GAIA Desktop](#8-安裝同啟動-gaia-desktop)
9. [連接掃描器](#9-連接掃描器)
10. [設定參考](#10-設定參考)
11. [疑難排解](#11-疑難排解)
12. [支援嘅 Zebra 2D 掃描器](#12-支援嘅-zebra-2d-掃描器)

---

## 1. 你需要乜嘢

| | |
|---|---|
| **掃描器** | 一部 Zebra 2D 掃描器，連埋佢條 USB 線 — 睇 [支援嘅 Zebra 2D 掃描器](#12-支援嘅-zebra-2d-掃描器) |
| **電腦** | Windows 10 或 11，64 位元 |
| **Java** | Java 21 或更新，**64 位元** |
| **Zebra Scanner SDK for Windows** | **64 位元**，喺 Zebra 支援網站下載 — 佢提供驅動程式同 GAIA Desktop 用嘅 JavaPOS 服務 |
| **Zebra 123Scan** | Zebra 嘅免費設定工具，喺 Zebra 支援網站下載 — 用嚟更新掃描器韌體同檢查佢嘅設定 |
| **GAIA Desktop** | `gaia-desktop-1.0.0-win.zip` |
| **可選** | 掃描器嘅 **Product Reference Guide**（PRG），入面有設定條碼 |

各部分點樣接埋一齊：

```
Zebra scanner ──USB──► Zebra CoreScanner driver ──► Zebra JavaPOS services ──► GAIA Desktop
                       (installed by the SDK)        (in the SDK's JPOS\bin)    (run.bat puts them on
                                                                                 the class path)
```

GAIA Desktop 自己帶住 JavaPOS API 同埋為掃描器命名嘅設定（`jpos.xml`，喺 `gaia-desktop.jar` 入面）。
其餘嘅由 Zebra SDK 提供：同掃描器溝通嘅驅動程式，同埋夾喺 JavaPOS 同嗰個驅動程式之間嘅 Zebra 服務類別。

---

## 2. 安裝 Java

GAIA Desktop 需要 **Java 21 或更新**，而且一定要係 **64 位元**版本，先夾到 64 位元嘅 Zebra 驅動程式。

1. 安裝 Windows x64 用嘅 Java 21 JDK 或 JRE — 例如 adoptium.net 嘅
   Eclipse Temurin。如果安裝程式話可以幫你設定 `JAVA_HOME`，就由佢設定。
2. 喺命令提示字元度檢查：

   ```
   java -version
   ```

   第一行應該顯示 21 或之後嘅版本；64 位元 Java 仲會印出 `64-Bit Server VM`。

`run.bat` 會按呢個次序搵 Java：`run.bat` 隔籬嘅 `jdk*` 資料夾，然後 `JAVA_HOME`，然後 `PATH` 上面嘅
`java`。舊過 21 嘅佢會拒絕，並且出訊息講明。

---

## 3. 安裝 Zebra Scanner SDK

1. 喺 Zebra 支援網站下載 **Zebra Scanner SDK for Windows (64-bit)**。
2. 執行安裝程式。如果佢俾你揀元件，確保有剔 **JPOS**（JavaPOS）元件。
3. 用返預設安裝位置。GAIA Desktop 會喺呢度搵 SDK 嘅 JavaPOS 部分：

   ```
   C:\Program Files\Zebra Technologies\Barcode Scanners\Scanner SDK\JPOS\
   ```

   並且預期 Zebra 嘅服務 jar（`JposServiceScanner.jar`、`JposServiceJniScanner.jar`、…）同原生程式庫
   （`CSJPOSScanner64.dll`、…）喺佢嘅 `bin\` 資料夾入面。如果 SDK 裝咗喺第二度，睇
   [ZEBRA_HOME](#zebra_home)。
4. 安裝完之後 **重新啟動 Windows**。SDK 嘅 CoreScanner 驅動程式喺背景運行，GAIA Desktop 要接觸到掃描器
   之前佢一定要已經行緊；重新啟動係確保呢點最簡單嘅方法。

---

## 4. 安裝 123Scan

123Scan 係 Zebra 用嚟設定自家掃描器嘅免費工具。下一步會用佢嚟更新掃描器韌體，之後要檢查同更改掃描器
設定，佢都係最方便嘅方法。

1. 喺 Zebra 支援網站下載 **123Scan**，然後執行安裝程式。
2. 插好 Zebra 掃描器，開 123Scan。佢應該搵到部掃描器，並顯示型號、序號同現時嘅韌體版本。如果搵唔到，
   繼續之前試吓第二個 USB 埠或者第二條線 — 123Scan 睇唔到嘅掃描器，GAIA Desktop 一樣睇唔到。

---

## 5. 更新 Zebra 掃描器韌體

設定之前，先將掃描器韌體升到最新版本。韌體更新會修正解碼問題，亦令掃描器同 SDK 裝落去嘅 Zebra 驅動程式
保持一致。

1. 閂咗 **GAIA Desktop** 同任何可能用緊掃描器嘅程式：更新期間 123Scan 要獨佔部掃描器。
2. 喺 123Scan 度，掃描器連接住嘅時候，揀佢嘅韌體更新選項。123Scan 會將掃描器嘅韌體同 Zebra 發佈嘅最新
   版本比較，然後提出下載同安裝。如果部電腦上唔到網，就喺 Zebra 網站嗰部掃描器嘅支援頁下載韌體，再將
   檔案載入 123Scan。
3. 開始更新，**一直插住掃描器，直到 123Scan 話完成為止**。要幾分鐘；最後掃描器會自己重新啟動。做到
   一半拔咗佢，可能會令佢用唔到。
4. 檢查 123Scan 而家顯示緊新嘅韌體版本。

更新可能會將部分設定還原做預設值，所以先至喺韌體之後，用接住落嚟兩步設定掃描器。

---

## 6. 設定 Zebra 掃描器嘅 USB 模式

呢步最多人漏咗。**Zebra 掃描器出廠時係一個 USB 鍵盤**（USB HID Keyboard 模式）：佢會將每個條碼
「打」入任何有焦點嘅地方。喺呢個模式下，GAIA Desktop 嘅 *連接掃描器* 搵唔到佢，而且打入文字欄嘅
嘢會冇晒 FNC1 分隔符，令 GS1 元素串嘅意思都變埋。

將掃描器轉去一個 Zebra 驅動程式直接溝通嘅模式：

| 掃描器 USB 模式 | GAIA Desktop 裝置名稱 | 備註 |
|---|---|---|
| **IBM Hand-held USB** | `ZebraUSBHandHeld`，或者 `ZebraAllScanners` | 建議使用 |
| SNAPI（有或冇影像功能） | `ZebraScannerSNAPI`，或者 `ZebraAllScanners` | Zebra 自家嘅 USB 協定；都用得 |
| IBM Table-top USB | `ZebraUSBTableTop`，或者 `ZebraAllScanners` | 用於枱面式掃描器 |
| USB OPOS (Hand-held) | `ZebraUSBOPOS`，或者 `ZebraAllScanners` | |
| USB HID Keyboard（出廠預設） | — | **連接唔到** |

`ZebraAllScanners` 係 GAIA Desktop 嘅預設裝置名稱，任何支援模式嘅掃描器佢都接受，所以模式一設好，
預設值通常即刻用得。

要改模式，可以：

- **掃描**該模式嘅**設定條碼** — 例如 *IBM Hand-held USB* — 喺掃描器嘅 Quick Reference Guide 或
  Product Reference Guide（USB Host Types / USB Device Type 一節）度；掃描器會「嗶」一聲確認。或者
- **用 123Scan**（[第 4 步](#4-安裝-123scan)）：連接掃描器，打開佢嘅設定，然後設定 USB 主機類型。

改完模式之後，Windows 會當部掃描器係另一類裝置重新偵測；等幾秒先好連接。

之後想將掃描器改返做鍵盤模式 — 俾其他軟件用 — 就掃 *USB Keyboard (HID)* 設定條碼，或者還原出廠預設。

---

## 7. 為 GS1 設定 Zebra 掃描器

掃描器話俾 GAIA Desktop 知佢讀咗邊種條碼，GAIA Desktop 就解析得最好。

### 傳送 AIM Code ID

用 Product Reference Guide（User Preferences）入面嘅設定條碼，或者喺 123Scan 度，將 **Transmit Code ID
Character** 設做 **AIM Code ID Character**。咁每次掃描都會以佢嘅符號識別碼開頭 — GS1-128 係 `]C1`、
GS1 DataMatrix 係 `]d2`、GS1 QR Code 係 `]Q3`、GS1 DataBar 係 `]e0` — GAIA Desktop 用佢嚟講出數據
載體係乜同埋套用相應規則。

冇咗佢，GAIA Desktop 一樣會解析元素串，並由掃描本身攞符號類型，但報告就冇 AIM Code ID 可以顯示，而且
好似 EAN-13 咁嘅普通條碼，會比較難同 GS1 元素串分得開。

### 檢查符號類型

確保你用開嘅條碼類型已經 **啟用**：通常係 GS1-128、GS1 DataMatrix、GS1 DataBar、GS1 QR Code 同
EAN/UPC。Zebra 2D 掃描器大部分預設已經開咗；123Scan 會顯示現時設定，Product Reference Guide 亦有
逐個啟用嘅條碼。

### 閂咗 Zebra 嘅 GS1 解析功能

新啲嘅韌體加咗喺掃描器本身運作嘅 GS1 功能 — **Label Parse+**、**GS1 Digital Link** 同
**GS1 Retail POS parsing**。保持佢哋 **關閉**，即係掃描器出廠時嘅狀態：佢哋會改寫或者縮短掃描器
傳出嘅內容，而 GAIA Desktop 需要原始數據嚟解析同驗證。每個功能做乜，睇
[支援嘅 Zebra 2D 掃描器](#12-支援嘅-zebra-2d-掃描器)。

### 唔好加前綴同後綴

唔好喺掃描器輸出加前綴、後綴或者 Enter 鍵。GAIA Desktop 經 JavaPOS 以完整訊息接收每次掃描，乜都唔使
加；多出嚟嘅字元只會走咗入元素串度。

---

## 8. 安裝同啟動 GAIA Desktop

1. 解壓 `gaia-desktop-1.0.0-win.zip`，例如解去 `C:\Tools\`。入面有一個資料夾：

   ```
   gaia-desktop-1.0.0-win\
   ├── docs\
   │   └── GaiaDesktop-Zebra-Scanner-Windows.md      this guide
   ├── gaia-desktop.jar
   ├── lib\
   ├── readme.html                                   index of every guide
   └── run.bat
   ```

2. 插好掃描器。
3. 撳兩下 **`run.bat`**。程式隔籬會開一個主控台視窗，並一直開住：掃描器狀態同 JavaPOS 錯誤都喺嗰度報告。
4. 睇主控台第一行。佢講咗 `run.bat` 搵到乜：

   ```
   run.bat: Java 21.0.8 from JAVA_HOME; JavaFX from lib\; JavaPOS from lib\; Zebra services from C:\Program Files\Zebra Technologies\Barcode Scanners\Scanner SDK\JPOS\
   ```

   如果結尾係 **`Zebra services from none (Zebra Scanner SDK not installed)`**，即係喺預設位置搵唔到
   SDK — 安裝佢（[第 3 步](#3-安裝-zebra-scanner-sdk)）或者設定 [ZEBRA_HOME](#zebra_home)。程式照樣
   開到，但掃描器連接唔到。

---

## 9. 連接掃描器

1. 喺工具列度，**裝置選擇器**列出 GAIA Desktop 識嘅掃描器名稱。保留 **`ZebraAllScanners`**，或者喺
   [第 6 步](#6-設定-zebra-掃描器嘅-usb-模式) 個表度揀返掃描器 USB 模式對應嘅名稱。
2. 撳 **連接掃描器**，或者按 **Ctrl+K**（**掃描器** 選單入面都有）。
3. 狀態列會變成 **`掃描器：ZebraAllScanners`**（或者你揀嘅名稱），狀態行會顯示
   *掃描器已連接 — ZebraAllScanners*。個掣而家寫住 **中斷掃描器**。
4. 掃一個 GS1 條碼。佢會出現喺擷取記錄最頂，並打開佢嘅報告。開咗 AIM Code ID 嘅話，報告會講出數據載體
   — *GS1-128*、*GS1 DataMatrix* — 而元素串會保留每個 FNC1 分隔符。

裝置選擇器會記住你每次揀嘅嘢。掃描器會一直連接住，直到你中斷佢或者閂咗程式。**建立** 同 **合併** 模式下
掃描一樣用得：喺合併模式，每次掃描都會變成要合併嘅其中一個條碼。

GAIA Desktop 佔用住掃描器嗰陣，其他程式用唔到佢，反之亦然：連接之前閂咗 123Scan 同任何其他收銀
（point-of-sale）或掃描器軟件。

---

## 10. 設定參考

### 裝置名稱

裝置選擇器嘅內容嚟自 `gaia-desktop.jar` 入面附帶嘅 `jpos.xml`。佢嘅掃描器項目：

| 裝置名稱 | 掃描器類型 | 用於 |
|---|---|---|
| `ZebraAllScanners` | 任何 | **預設。** 任何支援嘅 USB 模式 |
| `ZebraAllScanners114` | 任何 | 同一服務嘅 JavaPOS 1.14 版本 |
| `ZebraUSBHandHeld` | IBM Hand-held USB | IBM Hand-held USB 模式嘅掃描器 |
| `ZebraUSBTableTop` | IBM Table-top USB | 枱面模式 |
| `ZebraUSBOPOS` | USB OPOS | USB OPOS 模式 |
| `ZebraScannerSNAPI` | SNAPI | SNAPI 模式 |
| `ZebraScannerUSB` | 任何 USB | 任何 USB 模式 |
| `ZebraScannerSerialSSI` | 序列 SSI | 用序列線嘅掃描器（唔係 USB） |
| `ZebraScannerSerialNIXMODB` | 序列 NIXMODB | 用序列線嘅掃描器 |
| `ZebraVDI` | VDI | 轉駁去虛擬桌面嘅掃描器 |

### ZEBRA_HOME

如果 Zebra SDK 唔係裝喺預設位置，啟動 `run.bat` 之前將 `ZEBRA_HOME` 設做佢個 `JPOS` 資料夾 — 例如喺
佢隔籬整個細批次檔：

```bat
@echo off
set "ZEBRA_HOME=D:\Zebra\Scanner SDK\JPOS\"
call "%~dp0run.bat" %*
```

咁 `run.bat` 就會由 `%ZEBRA_HOME%bin` 攞 Zebra 嘅服務 jar，並將嗰個資料夾加入 `PATH`，俾 Zebra 嘅 DLL
用。

### 你自己嘅 jpos.xml

想加裝置名稱或者改某個項目嘅設定，就將 `jpos.xml` 由 `gaia-desktop.jar` 複製出嚟（佢係 zip 檔；任何
解壓工具都開到），改個副本，再叫 GAIA Desktop 用佢：

```bat
set GAIA_DESKTOP_OPTS=-Djpos.config.populatorFileURL=file:///C:/Tools/jpos.xml
run.bat
```

咁裝置選擇器就會列出嗰個檔案入面嘅掃描器。如果接咗多過一部 Zebra 掃描器，可以用項目嘅 `ScannerModel`
或 `SerialNumber` 屬性將佢綁定去其中一部，好似 `ZebraAllScanners` 項目咁。

### 其他選項

| 設定 | 做法 |
|---|---|
| 程式語言 | `run.bat --language=FR`（只限今次），或者選項面板 |
| 額外 Java 選項 | 喺 `run.bat` 之前 `set GAIA_DESKTOP_OPTS=…` |
| 隨附嘅 Java | 將 `jdk-21…` 資料夾放喺 `run.bat` 隔籬；佢會比 `JAVA_HOME` 優先使用 |

---

## 11. 疑難排解

錯誤會喺連接時嘅對話框、主控台視窗，同埋日誌檔 `logs\gaia-desktop.log` 度出現；日誌檔喺啟動
`run.bat` 嘅資料夾入面（撳兩下 `run.bat` 嘅話，就係 GAIA Desktop 資料夾）。日誌入面方括號內嘅數字係
JavaPOS 錯誤碼。

| 你見到乜 | 可能原因 | 點做 |
|---|---|---|
| 冇 *連接掃描器* 掣 | 唔係喺 Windows 上運行 | 掃描功能只限 Windows |
| 123Scan 搵唔到掃描器 | 線、USB 埠，或者掃描器俾另一個程式佔用緊 | 試第二條線或者第二個埠；閂咗 GAIA Desktop 同其他掃描器軟件。123Scan 睇唔到嘅掃描器，GAIA Desktop 一樣睇唔到 |
| 主控台：`Zebra services from none (Zebra Scanner SDK not installed)` | SDK 唔喺預設位置 | 安裝 64 位元 SDK（[第 3 步](#3-安裝-zebra-scanner-sdk)），或者設定 [ZEBRA_HOME](#zebra_home) |
| 錯誤 **104**：*Could not connect to service with logicalName = …: …SymScannerSvc112Factory* | 搵唔到 Zebra 嘅服務 jar | 同上 — 檢查主控台嘅 `Zebra services from` 嗰行 |
| 錯誤 **109**：*Service does not exist in loaded JCL registry* | 掃描器設定入面冇呢個裝置名稱，或者 SDK 嘅 `javaPOS114.jar` 走咗入 class path | 喺裝置選擇器揀一個名稱；唔好加 `javaPOS114.jar`；如果用緊自己嘅 `jpos.xml`，檢查入面有冇嗰個名稱 |
| 錯誤 **107**（冇硬件），或者插住掃描器都連接失敗 | 掃描器仲係 USB 鍵盤模式，或者 CoreScanner 驅動程式冇行 | 設定 USB 模式（[第 6 步](#6-設定-zebra-掃描器嘅-usb-模式)）；裝完 SDK 重新啟動 Windows；試第二個 USB 埠 |
| 連接時出錯誤 **112**（逾時）或 **102**（已被佔用） | 另一個程式佔用緊掃描器 | 閂咗 123Scan 同任何其他掃描器或收銀軟件，再連接過 |
| 主控台出現 `UnsatisfiedLinkError` … `CSJPOSScanner64` | Zebra 嘅 DLL 載入唔到 — 通常係 32 位元 SDK 配 64 位元 Java | 安裝 64 位元 SDK；檢查 `java -version` 顯示 64 位元 VM |
| 掃描結果打咗入輸入欄，而唔係以擷取形式出現 | 掃描器係 USB 鍵盤模式 | 設定 USB 模式（[第 6 步](#6-設定-zebra-掃描器嘅-usb-模式)） |
| 連接咗，但掃描冇嚟到 | 裝置名稱同掃描器模式唔夾，或者連接住嗰陣重新插過掃描器 | 中斷，揀 `ZebraAllScanners`，再連接 |
| 報告冇顯示 AIM Code ID／數據載體 | AIM Code ID 傳送冇開 | 開啟 *Transmit Code ID Character → AIM Code ID*（[第 7 步](#7-為-gs1-設定-zebra-掃描器)） |
| 元素串冇咗分隔符，或者啲 AI 黐埋一齊 | 用鍵盤模式掃描，或者手打時冇用 `{GS}` | 用 JavaPOS USB 模式；打字時用 `{GS}` 掣插入分隔符 |
| 某種條碼一直讀唔到 | 掃描器停用咗嗰種符號類型 | 啟用佢（[第 7 步](#7-為-gs1-設定-zebra-掃描器)） |

如果問題仲係有，將主控台嘅 `run.bat:` 嗰行、錯誤對話框嘅文字同 `logs\gaia-desktop.log` 一齊保留：
三樣加埋就睇得出用咗邊個 Java、邊啲 Zebra 服務同邊個裝置名稱，同埋連接究竟喺邊度失敗。

---

## 12. 支援嘅 Zebra 2D 掃描器

GAIA Desktop 需要一部讀得到印 GS1 數據嘅條碼嘅掃描器 — GS1-128、GS1 DataBar、EAN/UPC，以及大部分用途
都會用到嘅 2D 碼：**GS1 DataMatrix** 同 **GS1 QR Code**，**GS1 Digital Link** URI 通常亦係咁印。
即係要一部 Zebra **2D 影像掃描器**：線性（1D）掃描器完全讀唔到 DataMatrix 或 QR 碼。

掃描器本身唔使識 GS1 Application Identifiers 或 Digital Link。
**GAIA Desktop 兩樣都識解析**：佢照掃描器讀到嘅樣接收掃描 — AIM Code ID、FNC1 分隔符，乜都齊 —
然後自己拆做 AI，或者將 Digital Link URI 轉做佢嘅 AI。

### 已測試

| 掃描器 | 類型 | 狀態 |
|---|---|---|
| **DS8108** | 有線手持 2D 影像掃描器 | 用 GAIA Desktop **開發及測試** |

### 預計可用

以下 Zebra 2D 影像掃描器同 DS8108 一樣，經 Zebra 嘅 Scanner SDK 同 JavaPOS 服務驅動。佢哋**未有**用
GAIA Desktop **測試過**；每部都按本指南設定，設定條碼就用佢自己嘅 Quick Reference Guide 或
Product Reference Guide。

| 系列 | 型號（例子） |
|---|---|
| DS81x8 | DS8108（有線）、DS8178（無線） |
| DS22x8 | DS2208（有線）、DS2278（無線）— 一般用途 |
| DS36x8 | DS3608（有線）、DS3678（無線）— 耐用型，包括 KD 同 XR 版本 |
| DS46x8 | DS4608（有線）、DS4678（無線） |
| DS9308 / DS9908 | 展示式同免提掃描器 |
| MP7000 / MP7200 | 多平面雙窗掃描器同掃描磅 |
| CS6080 | 無線隨身掃描器 |

無線掃描器同佢個座溝通，插落電腦嘅係個座：用 USB 接好個座，當兩樣係一部掃描器就得。其他 Zebra 系列嘅
掃描器都可能用得，只要 Zebra Scanner SDK 喺 [第 6 步](#6-設定-zebra-掃描器嘅-usb-模式) 其中一個 USB
模式下支援佢哋。

### Zebra 掃描器本身嘅 GS1 功能

除咗讀條碼，部分 Zebra 2D 影像掃描器可以喺掃描器本身處理 GS1 數據，經 123Scan 設定：

| 功能 | 做乜 | 對 GAIA Desktop 嚟講 |
|---|---|---|
| **Label Parse+** | 喺掃描器上解析 GS1 標籤嘅 AI，傳送改寫咗格式嘅欄位 | **保持關閉** — 否則 GAIA Desktop 會睇唔到 AI、FNC1 分隔符同 AIM Code ID |
| **GS1 Digital Link** | 針對同時有 1D EAN/UPC 同 2D Digital Link 碼嘅零售產品：揀傳送邊個 | **保持關閉**（預設） |
| **GS1 Retail POS parsing** | 讀取 Digital Link URI 或元素串語法嘅 2D 零售條碼 | **保持關閉** |

**GS1 Digital Link** 設定有三個模式。**Mode 1 — GTIN only** 係一開啟就用嘅模式，無論 GTIN 嚟自邊個條碼，
都淨係傳 GTIN，所以 Digital Link 入面嘅批號、到期日或序號永遠去唔到 GAIA Desktop。**Mode 2 —
Prioritize 2D** 如果喺優先逾時（預設 200 ms）內見到 2D 碼就傳 2D，否則傳 1D。**Mode 3 — Corroborate 1D
and 2D GTINs Match** 傳 1D，而 2D 嘅 GTIN 吻合時亦會傳埋 2D。如果其他軟件一定要開呢個設定，用 Mode 2，
唔好用 Mode 1。喺 DS8108 上，呢啲係參數 2373（開／關）、2374（模式）同 2491（逾時）。

用呢啲功能需要 **123Scan 第 6 版或更新**，同埋包含呢啲功能嘅韌體。Zebra 由 2025 年 4 月起經韌體更新
加入 GS1 Digital Link 支援；下面日期嚟自一間 Zebra 經銷商，所以請查閱各型號嘅韌體發佈說明：

| 型號系列 | GS1 Digital Link 韌體發佈日期 |
|---|---|
| DS81x8 | 2025 年 4 月 16 日 |
| MP7000 | 2025 年 4 月 21 日 |
| DS9908 | 2025 年 6 月 6 日 |
| SP72 / SM72 | 2025 年 7 月 11 日 |
| DS46x8 | 2025 年 7 月 28 日 |
| MP7200 (Rev-C)、DS5502 | 2025 年 7 月 30 日 |
| DS9308 | 2025 年 8 月 29 日 |
| DS22x8、DS1000 | 2025 年 9 月 5 日（只限亞太區） |
| DS36x8（包括 KD 同 XR）、MS47 | 2025 年 9 月 30 日 |
| PL5000、SE4107 | 2025 年 10 月 15 日 |
| CS6080 | 2025 年 12 月 31 日 |

要將 GS1 Digital Link 碼讀入 GAIA Desktop，以上功能一樣都唔需要：佢哋關閉嘅時候，用緊現行韌體嘅
Zebra 2D 掃描器讀 Digital Link QR 碼同讀其他碼冇分別，GAIA Desktop 會將 URI 轉做佢嘅 AI。

資料來源：Zebra，*DS8108 Product Reference Guide*，MN-002926-11EN Rev A（2025 年 8 月），第 12 章
「GS1 Digital Link Parameters」同 Data Parsing 章節；Zebra，*123Scan release notes*；
Barcode Factory，*Upgrade Zebra Scanners for GS1 Digital Link*（韌體日期）。
