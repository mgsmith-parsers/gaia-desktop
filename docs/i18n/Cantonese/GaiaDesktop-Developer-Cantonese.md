# GAIA Desktop — 開發者指南

呢份文件講個模組點樣建置、點樣編排同點樣測試。想知個程式由外面睇做到啲乜，就睇[使用者指南](GaiaDesktop-User-Cantonese.md)。原文：[GaiaDesktop-Developer.md](../../GaiaDesktop-Developer.md)。

## 目錄

1. [模組一覽](#模組一覽)
2. [建置同執行](#建置同執行)
   - [封裝](#封裝)
3. [架構](#架構)
   - [輸入漏斗](#輸入漏斗)
   - [執行緒](#執行緒)
   - [重建定係重新標籤](#重建定係重新標籤)
4. [{GS} 記號](#gs-記號)
5. [掃描器整合](#掃描器整合)
   - [平台閘門](#平台閘門)
   - [條碼制式](#條碼制式)
6. [本地化](#本地化)
   - [`--language` 參數](#--language-參數)
   - [加語言](#加語言)
   - [刻意唔翻譯嘅嘢](#刻意唔翻譯嘅嘢)
7. [儲存狀態嘅格式](#儲存狀態嘅格式)
8. [測試](#測試)
9. [建置問題排解](#建置問題排解)
10. [已知限制同未完成嘅工作](#已知限制同未完成嘅工作)

---

## 模組一覽

```
tools.pantheum:gaia-desktop:1.0.0   Java 21 · JavaFX 21
        └── depends on tools.pantheum:gaia:1.1.0
```

佢只係核心程式庫嘅一個用戶端，冇其他嘢。佢入面**完全冇 GS1 知識**：所有 AI 定義、驗證規則、錯誤訊息同本地化描述，全部嚟自 [`GaiaParser`](../../../../gaia/docs/GaiaParser.md) 同 [`GaiaBuilder`](../../../../gaia/docs/GaiaBuilder.md)。特別係 AI 清單從來唔會複製入呢個模組——建立器嘅揀選器直接讀核心自己嘅登記表，所以佢冇可能提供一個建立器會拒絕嘅 AI。

**入口點：** `tools.pantheum.gaia.desktop.GaiaDesktopApp`（JavaFX 嘅 `Application`）同 `tools.pantheum.gaia.desktop.Launcher`（封裝好嘅 jar 嘅 `Main-Class`）。

呢個模組係一塊葉：冇嘢依賴佢，佢亦唔依賴任何其他下游模組。

---

## 建置同執行

- 執行要 **Java 21**，建置要 JDK 21 或者更新——模組用 `release=21` 編譯。JavaFX 21 係對應嘅 LTS 系列。核心 jar 仍然係 Java 11 位元組碼，喺 21 上面原封不動咁行。
- `PATH` 上面要有 **Maven**。呢個儲存庫冇 wrapper。
- **先安裝核心。** 呢個模組由 `~/.m2` 解析 `gaia:1.1.0`；核心 jar 過期或者唔見咗，通常就係出現 `package tools.pantheum.gaia does not exist` 嘅原因。

```bash
(cd gaia && mvn -q install -DskipTests)      # publish the core
(cd gaia-desktop && mvn javafx:run)          # run from source
(cd gaia-desktop && mvn clean package)       # target/dist/gaia-desktop-<version>-<platform>/ — jar (~290 KB), lib/, run.sh (run.bat on win)
(cd gaia-desktop && mvn clean -Pfat package) # one self-contained jar, ~50 MB
(cd gaia-desktop && mvn package -Dfx.platform=win)   # another platform; see Packaging
java -jar gaia-desktop/target/dist/gaia-desktop-1.0.0-mac-aarch64/gaia-desktop.jar
java -jar gaia-desktop/target/dist/gaia-desktop-1.0.0-mac-aarch64/gaia-desktop.jar --language=FR
```

`--language` 係唯一嘅命令列參數；除咗會將成個設定檔指去第二度嘅 `-Dgaia.desktop.home=…` 之外，佢亦係影響啟動嘅唯一方法。

`mvn javafx:run` 係開發用嘅路徑：外掛將 JavaFX 放喺**模組路徑**上，所以工具包以具名模組身份啟動，`GaiaDesktopApp` 可以直接啟動。封裝好嘅 jar 做唔到——所有嘢都喺類別路徑上——所以佢嘅 `Main-Class` 係 `Launcher` 呢個墊片。喺嗰個佈局入面將 `Application` 子類別做 `Main-Class`，會出 *"JavaFX runtime components are missing"* 然後失敗。

一定要用 `clean`——喺建置一組平台之前做一次就得，因為佢會將 `target/dist/` 入面所有平台清空。原因睇[建置問題排解](#建置問題排解)。

### 封裝

所有要出貨嘅嘢都集中喺 `target/dist/gaia-desktop-<version>-<platform>/` 底下，而且只有佢哋喺度，咁成個目錄就可以一齊壓縮或者複製，唔使喺 `target/` 入面同 `classes/`、`maven-status/` 同其他建置暫存檔撈埋一齊嘅產物度逐個揀。`${dist.dir}` 喺一個地方幫佢命名；每個寫產物嘅外掛都指向佢。

#### 每個平台一次建置

JavaFX 嘅 jar 帶住原生程式碼，所以每個發佈版只係一個平台用，由 `fx.platform` 屬性以 OpenJFX 分類器命名：`mac-aarch64`、`mac`、`win`、`linux` 或者 `linux-aarch64`。五個按作業系統啟用嘅 `fx-*` 設定檔會將佢設做建置部機自己嘅平台，普通嘅 `mvn package` 或者 `mvn javafx:run` 淨係需要咁多；`-Dfx.platform=…` 優先過佢哋，可以喺任何一部機建置任何其他平台——JavaFX 嘅 jar 只不過係下載落嚟嘅檔案。`${dist.dir}` 係 `target/dist/${fx.platform}`，所以一個平台嘅建置永遠唔會覆寫另一個平台嘅建置。

```bash
(cd gaia-desktop && mvn -q clean package)    # this machine's platform, with the tests
for p in win linux mac mac-aarch64; do (cd gaia-desktop && mvn -q package -DskipTests -Dfx.platform=$p); done
```

pom 入面有兩個細節令呢樣嘢行得通，而兩樣睇落都好似可以刪走：

- **每個 JavaFX 模組都係直接依賴**——base、graphics、controls、media 同 web——每個都有 `<classifier>${fx.platform}</classifier>` 同一個 `org.openjfx:*` 排除。OpenJFX 嘅 pom 係按*建置部機*嘅作業系統去揀遞移 jar，所以如果由得 graphics 或者 base 遞移咁入嚟，Windows 建置入面就會喺 Windows 原生程式庫隔籬多埋 Mac 嘅。
- **屬性係 `fx.platform`，唔係 `javafx.platform`。** 如果由外面設定 OpenJFX 自己嗰個屬性，佢嘅父 pom 會用佢自己去重新定義佢；Maven 會當嗰個 pom 無效而拒絕，靜靜雞咁唔要啲平台 jar，結果剩低一個完全冇 JavaFX 嘅建置。

兩種佈局都寫同一條路徑 `target/dist/gaia-desktop-<version>-<platform>/gaia-desktop.jar`，每次建置只有其中一種會行。防止兩個同時霸住佢嘅，就係 `${jar.phase}`、`assembly.skipAssembly` 同 `mdep.skip`。

| | 預設 | `-Pfat` |
|---|---|---|
| `gaia-desktop.jar` | 約 290 KB — 淨係呢個模組嘅類別同資源 | 約 50 MB — 依賴解壓咗入去 |
| `dist/gaia-desktop-<version>-<platform>/lib/` | 17 個 jar，喺清單 `Class-Path` 入面逐個列出 | — |
| 出貨 | jar **同埋**佢隔籬嘅 `lib/` | 一個檔案 |
| `dist/gaia-desktop-<version>-<platform>/` 入面仲有 | `run.bat`（win）或者 `run.sh`（其他） | 一樣 |
| 同埋 | `readme.html` 同 `docs/`（所有平台） | 一樣 |

每個發佈版都會帶埋各份指南：`docs/` 保留原本佈局咁複製，等指南同翻譯之間嘅連結繼續用得，不過唔包翻譯工具（`check-i18n.py`、`i18n-manifest.json`、`i18n-plan.md`）；仲有根目錄嘅 `readme.html`，係一個連去每份指南嘅索引，嚟自 `src/main/dist/readme.html`，入面嘅 `@VERSION@` 會換成 pom 版本。兩樣都係由 `pick-launch-script` 呢個 antrun 步驟寫出，佢會先刪走 `docs/`，等已經移除咗嘅指南唔會殘留。readme 嘅連結係人手寫嘅——喺 `docs/` 底下改指南名或者加翻譯，嗰度都要加返佢嗰行。

清單嘅 `Class-Path` 同 `lib/` 係同一樣嘢嘅兩半：jar 會為每個執行期依賴列出 `lib/<artifact>-<version>.jar`，而 `copy-dependencies` 就正正用呢啲名去填 `lib/`。`java -jar` 行得到就係靠呢樣。

#### macOS 應用程式同發佈 zip

一個 antrun 執行 `mac-app-and-zip` 負責收尾 `package` 階段。正因為咁，antrun 外掛先至喺 jar 同 assembly 外掛**之後**宣告——Maven 係按外掛宣告次序行一個階段入面嘅執行——咁佢行嘅時候，無論邊種封裝，jar 都已經存在。（佢另一個執行 `pick-launch-script` 喺 `prepare-package`，唔受影響。）

對於 `mac` 同 `mac-aarch64`，佢會先喺發佈目錄整 `GAIA Desktop.app`：`Info.plist`（入面嘅 `@VERSION@` 換成 pom 版本）、啟動器 `Contents/MacOS/gaia-desktop`（設成可執行）同 `gaia-desktop.icns`，全部嚟自 `src/main/macos/`，然後將 `gaia-desktop.jar` 同 `lib/` **搬**入 `Contents/Java`，等個套件自己企得穩。隔籬冇 jar 嘅時候，`run.sh` 會去嗰度搵。啟動器按版本搵 Java（先 `java_home -v 21+`，再到 Homebrew 嘅 openjdk——由 Finder 開嘅話睇唔到 `JAVA_HOME` 或者 `PATH`），搵唔到就用 `osascript` 彈個對話框，`cd` 去 `~/.gaia-desktop` 等 logback 嘅相對路徑 `logs/` 寫得入，再傳 `-Xdock:name`/`-Xdock:icon`。macOS 會將運行緊嘅 `java` 同個套件連埋（套件 ID 同 Dock 圖示都係個程式嘅）；選單列會顯示 GAIA Desktop。

之後，每個平台都會用 Ant 嘅 `<zip>` 將目錄壓縮成 `target/dist/gaia-desktop-<version>-<platform>.zip`，放喺同名資料夾底下：`run.sh` 同 `Contents/MacOS/**` 會帶 `filemode="755"` 做一個獨立嘅檔案集，否則解壓之後就行唔到。呢個 zip **唔會附加**：佢係俾 GitHub 發佈用，四個平台出自四次建置，而 Maven Central——每個版本只收一次唔可以改嘅上載——永遠冇辦法當佢哋係一個發佈。

`project.build.outputTimestamp` 設做建置時間。唔係嘅話，maven-jar-plugin 3.5 同 maven-assembly-plugin 3.8 嘅封存工具會將每個項目嘅日期寫做 1980 年 2 月 1 日。

程式圖示由 `tools/icon/GaiaIcon.java` 畫（唔屬於建置；佢嘅檔頭有 `sips`/`iconutil` 步驟）。佢嘅輸出已經提交咗入儲存庫：套件用嘅 `src/main/macos/gaia-desktop.icns`，同埋 `GaiaDesktopApp` 設做視窗圖示嘅 `src/main/resources/icons/gaia-desktop-{16..256}.png`（Windows 同 Linux；macOS 會無視視窗圖示）。

#### 啟動指令碼

`run.sh` 同 `run.bat` 嚟自 `src/main/scripts/`，唔經過濾咁複製去 `dist/gaia-desktop-<version>-<platform>/`——`run.sh` 入面成堆 `${...}` shell 展開，Maven 過濾隨時會亂咁改寫佢哋。之後一個 antrun 步驟淨係留低嗰個平台要行嘅——`win` 用 `run.bat`，其他全部用 `run.sh`——再幫 `run.sh` 設可執行位元，因為 `copy-resources` 唔會帶埋佢。佢係睇解析咗嘅 `${fx.platform}` 去決定，而唔係靠設定檔，因為設定檔啟用睇唔到另一個設定檔設嘅值：唔係嘅話，喺 Windows 整嘅 Linux 建置就會攞到主機嘅指令碼。

佢哋**唔靠清單**。每個都自己砌類別路徑：先 `gaia-desktop.jar`，再逐個列出 `lib/` 入面每個 `.jar`，唔交俾 `lib/*` 萬用字元處理，咁有空格嘅路徑仍然係一個項目，加咗幾多個亦數得準。用 `-cp` 啟動，就係佢哋要明確寫出 `tools.pantheum.gaia.desktop.Launcher` 嘅原因：`-cp` 唔會讀清單嘅 `Main-Class`。但佢**仍然**會跟佢嘅 `Class-Path`——`-cp` 上面嘅 jar 會帶埋佢清單列出嘅 jar，緊接住佢之後搜尋——所以無論指令碼有冇列，每個 `lib/` jar 都會載入，喺指令碼嘅類別路徑度漏咗一個都唔會令佢留喺 JVM 外面。指令碼照樣全部列出，咁就算 jar 嘅清單過期、俾人手改過或者少咗一項，都一樣開得到。同一個指令碼兩種封裝都啱用：`-Pfat` 嘅 jar 冇 `lib/` 要加，亦唔需要，所以空嘅 `lib/` 只係提示而唔係錯誤。

#### Zebra 原生程式庫

建置唔會出貨任何 Zebra 嘅嘢——冇 DLL，冇服務 jar。喺 Windows 上面，佢哋嚟自部機安裝咗嘅 Zebra Scanner SDK：`run.bat` 會將佢嘅 `Jpos*.jar` 服務 jar 同 `bin\` 加入類別路徑，再將 `bin\` 加入 `PATH`，`System.loadLibrary` 就喺嗰度搵到 `CSJPOSScanner.dll` / `CSJPOSScanner64.dll`（`java.library.path` 預設就係 `PATH`）。JavaPOS API 同佢嘅設定載入器永遠嚟自 `lib\javapos-*.jar`，有冇 SDK 都一樣。

**`run.bat` 永遠唔會加 SDK 自己嘅 JavaPOS jar**，即係 `External\JavaPOS-1.14.0-Dist\javaPOS114.jar`。以前佢有加，仲為咗避免 API 有兩份而唔加 `lib\javapos-*.jar`——但清單 `Class-Path` 無論如何都會載入佢哋（見上面），結果 JVM 兩樣都攞到：`lib\` 嘅 4.x 載入器類別，隔籬就係 `javaPOS114.jar` 嘅 `jpos/res/jpos.properties` 同 `jcl.xsd`。個屬性檔會令 [`JposConfig`](#掃描器整合) 讓開，而嗰個 `jcl.xsd` 只接受 1.10 或之前嘅 JavaPOS 版本，於是 `jpos.xml` 入面每個項目（1.12–1.14）都驗證失敗，登記表一直係空，每次連接都喺未掂到掃描器之前就以錯誤 109 失敗。只要嗰個 jar 喺類別路徑上就足以搞成咁，就算排喺 `lib\` 啲 jar 後面都一樣。Zebra 嘅服務 jar 喺 `lib\` 嘅 1.14 API 上面原封不動咁行——開啟、佔用同掃描都係咁樣用真掃描器驗證過。人手放喺 `run.bat` 隔籬嘅 DLL 仍然會照用：嗰陣指令碼會設 `-Djava.library.path=<dist>;%PATH%`，喺尾加返 `PATH`，因為呢個屬性會成個取代預設搜尋路徑。

`mvn javafx:run` 呢啲全部唔做，所以喺 Windows 用開發模式執行去佔用掃描器，就要用其他方法將 SDK 嘅 jar 放上類別路徑、佢嘅 `bin\` 放上 `PATH`；要咁做就由 `dist/` 行。

除咗類別路徑，佢哋仲處理 `java -jar` 唔理嘅嘢：解析自己所在嘅目錄，等喺任何工作目錄都行得到；`JAVA_HOME` 優先過 `PATH`；同埋遇到 11 之前嘅 JVM，就用一句講明解決方法嘅說話拒絕，而唔係拋 `UnsupportedClassVersionError`。`GAIA_DESKTOP_OPTS` 會將 JVM 選項傳落去。`run.bat` 用 `java.exe` 而唔係 `javaw.exe`，等主控台保持開住——Windows 係唯一行到掃描器嘅平台，佢嘅狀態同 JavaPOS 錯誤都會出喺嗰度。

`run.bat` 仲會按搵到嘅嘢去揀 Java、JavaFX 同 Zebra 服務——附帶嘅 `jdk*\`、模組路徑上嘅 `javafx-sdk*\lib\`、`ZEBRA_HOME` 嘅 Zebra Scanner SDK——用 SDK 嘅 JavaFX 時就唔將 `lib\` 嘅 JavaFX 副本放入類別路徑，並傳 JDK 嘅 SAX 剖析器、Windows 代理伺服器同信任儲存區，以及喺 Java 17+ 上面傳 `--enable-native-access`。README 嘅 [What `run.bat` adds on Windows](../../../README.md#what-runbat-adds-on-windows) 有張表。記住保留呢個檔嘅 CRLF 換行：喺淨係 LF 嘅批次檔入面，`cmd.exe` 可能會漏咗 `call :label`。`run.sh` 一樣會傳 `--enable-native-access=ALL-UNNAMED`——JavaFX 由 `lib/` 載入自己嘅原生程式庫，而 JavaFX 喺類別路徑上嘅時候，涵蓋佢嘅模組就係 `ALL-UNNAMED`——同 `run.bat` 一樣，只係讀唔到 Java 版本先會略過。

兩種佈局都只帶**一個平台**（`${fx.platform}`）嘅 JavaFX 原生程式庫，所以兩種都唔可以跨作業系統或者架構搬嚟搬去。每個目標建置一次——睇[每個平台一次建置](#每個平台一次建置)。

> **Eclipse。** `copy-dependencies` 執行帶咗一個 `<?m2e ignore?>` 指示。m2e 會將開住嘅工作區專案解析成佢嘅 `target/classes` 資料夾而唔係 jar，而 `copy-dependencies` 拒絕複製未封裝嘅產物（MDEP-187），咁只要 `gaia` 專案同呢個一齊開住，自動建置就會失敗。後果係淨係喺 IDE 入面建置唔會填 `target/dist/gaia-desktop-<version>-<platform>/lib/`；用 jar 本身之前要先行 `mvn package`。

---

## 架構

| 套件 | 內容 |
|---|---|
| `desktop` | `GaiaDesktopApp`、`Launcher`、`Theme`、`GaiaDesktopVersion` |
| `desktop.ui` | `MainView`（視窗）、`BuilderPane`、`OptionsPane`、`HistoryCell`、`EditCell` |
| `desktop.build` | `ElementBuilder`（唔使 JavaFX 嘅建立邏輯）、`AiPair`（編輯器入面一行） |
| `desktop.parse` | `ParseOptions`（可觀察嘅 `ParseConfig`）、`SystemDefaults`、`Languages`、`GsToken` |
| `desktop.scanner` | `ScannerService`（JavaPOS）、`ScannerListener`、`Symbologies` |
| `desktop.html` | `GaiaDesktopReport` — 報告轉譯器，唔使 JavaFX，冇工具包都做到單元測試 |
| `desktop.i18n` | `UiStrings` — 標籤目錄 |
| `desktop.store` | `AppPaths`、`OptionsStore`、`CaptureStore` |

### 輸入漏斗

所有輸入——打字、貼上、由檔案開、掃描或者建立——都係經同一個方法 `MainView.capture(String, ScanSource, String)` 去到剖析器。

成個程式就係圍繞呢一個設計決定而建。建立唔需要自己嘅報告、自己嘅歷史或者自己嘅匯出路徑，因為經過漏斗之後，建立出嚟嘅字串同掃描得嚟嘅字串根本分唔出。任何會產生輸入嘅新嘢都應該行入呢個漏斗，而唔係喺佢隔籬兜過去。

### 執行緒

| 執行緒 | 行乜嘢 |
|---|---|
| JavaFX 應用程式執行緒 | 所有剖析、轉譯同 UI。AI 表熱身咗之後剖析唔使一毫秒，所以唔使工作執行緒 |
| `gaia-scanner` | 每一個對掃描器嘅呼叫——開啟、佔用、啟用、停用、釋放、關閉。由 `ScannerService` 擁有嘅一條長命執行緒；睇[掃描器整合](#掃描器整合) |
| JavaPOS 事件執行緒 | `ScannerService` 回呼。`MainView.FxScannerListener` 用 `Platform.runLater` 將每個都轉過去 |
| 背景 | 等掃描器 `connect()` / `disconnect()`（佔用可能要成 5 秒）、啟動時還原儲存咗嘅歷史——順便幫剖析器熱身——同埋檢查更新（`gaia-update-check`，一個 GitHub 請求，結果用 `Platform.runLater` 送返嚟） |

**千祈唔好喺一個控制項自己嘅選取事件入面改佢嘅選取，或者銷毀佢。** 呢個程式有三次唔同嘅當機正正係咁嚟：喺彈出視窗自己嘅 `clearAndSelect` 入面重設 AI 揀選器（由 `ListViewBehavior` 拋出一個 `IndexOutOfBoundsException`），同埋喺語言下拉選單自己嘅處理常式入面重建擁有佢嘅面板。兩個而家都用 `Platform.runLater` 延後，原因喺兩個呼叫位置都有記低。

### 重建定係重新標籤

轉語言會由目錄**重建**外框——選單、工具列、選項面板——咁做係安全嘅，因為佢哋冇一個持有狀態：`OptionsPane` 係 `ParseOptions` 嘅雙向檢視，數值住喺模型入面，新面板即刻就攞返。

持有**使用者資料**嘅控制項係欄位，改為原地重新標籤：`BuilderPane.retext(UiStrings)` 會保留 AI 行、Digital Link 欄位同選取。重建佢會掉咗做緊嘅嘢——以前真係試過。

---

## {GS} 記號

JavaFX 嘅 `TextField` 會靜靜雞丟走所有低過 `0x20` 嘅字元（`TextInputControl.filterInput`）。唔係「顯示得怪怪哋」——係直頭刪走。打字、貼上、`setText` 同 `insertText` 全部受影響，所以 `setText("01…" + GS + "10LOT")` 會得出 `"01…10LOT"`：一個意思靜靜雞變咗嘅元素字串，因為可變長度嘅 AI 而家連後面嘅嘢都吞埋。

`desktop.parse.GsToken` 就係全部答案。分隔符喺 UI 入面以字面文字 `{GS}` 傳遞，去剖析器途中先變返 `0x1D`。呢個記號唔係喺度發明嘅——佢正正係核心嘅 `ASCIIRevealerUtils.printable` 顯示 `0x1D` 嘅樣，所以由報告複製出嚟嘅值可以直接貼返入輸入列。

輸入欄上面嘅 `TextFormatter` 過濾器會截住文字可以入嚟嘅每一條路——打字、⌘V、內容選單、拖放、`setText`——所以改寫放喺嗰度，而唔係每個處理常式都整一次。

掃描器路徑永遠唔經 `GsToken`：掃描係以位元組形式去到剖析器。

---

## 掃描器整合

`ScannerService` 擁有 JavaPOS 嘅 `Scanner` 控制項：開啟 → 佔用 → 啟用，每次掃描之後重新啟動資料事件。邏輯裝置名稱嚟自 `src/main/resources/jpos.xml`。

**每個裝置呼叫都喺同一條執行緒 `gaia-scanner` 上面行，佢同服務一樣長命**——永遠唔喺 FX 執行緒，亦永遠唔喺呼叫完就完結嘅執行緒。Zebra 嘅服務經 CoreScanner COM API 用 JNI 接觸掃描器，而 COM 會將佢設定嘅嘢綁定落呼叫執行緒嘅 apartment：喺一條之後會完結嘅執行緒開啟，掃描器佔用到但一直冇反應；喺另一條執行緒關閉，就會解除錯嘅 apartment 嘅初始化。喺 FX 執行緒關閉係最差嘅情況，因為 JavaFX 喺嗰度呼叫 `OleInitialize`，仲將每個視窗嘅放置目標登記落去——喺嗰度一個 `CoUninitialize` 就會令成個程式嘅拖放壞晒。呢個係 Windows 上 JavaFX 嘅問題，唔係某個 JavaFX 版本嘅問題。`connect()` 同 `disconnect()` 會交工作俾嗰條執行緒再等，所以 `MainView` 喺一條短命嘅背景執行緒度呼叫兩者；呢個模式嚟自檢視器嘅 `JposScannerSession`，拖放故障最初就係喺嗰度發現。

有兩條規則防止一部失常嘅裝置卡死連線。**拆除永遠唔會提早停：** `closeQuietly` 將停用、釋放同關閉分開做，只有持有佔用時先釋放，而且一定會試 `close()`——佔用失敗之後 `release()` 會拋「not claimed」，以前啲步驟共用一個 `try` 嘅時候，就會因此跳過關閉，令裝置喺 Zebra 服務入面一直開住。**資料事件喺 `finally` 入面重新啟動：** JavaPOS 每次事件之後都會停用佢，所以就算讀取或者處理常式拋咗例外都一定要重新啟動，否則掃描器會一直冇反應，直到重新連線為止。`ScannerLifecycleTest` 用一個預先編好劇本嘅 `jpos.Scanner` 測試兩條規則。

**`JposConfig` 喺第一次開啟之前將 JavaPOS 指向附帶嘅 `jpos.xml`。** 用 Maven 嘅 JavaPOS jar（`javapos-config-loader` 4.x）而類別路徑上冇 `jpos/res/jpos.properties` 嘅話，唔咁做每次開啟都會以 *"Service does not exist in loaded JCL registry"*（錯誤 109）失敗，原因有三個：服務管理器會退返去用 `SimpleRegPopulator`，佢讀序列化嘅 `jpos.cfg`，從來唔讀 `jpos.xml`；XML 載入器淨係喺工作目錄搵 `jpos.xml`，唔會搵 jar 入面；同埋 `lib/` 嘅 Xerces 2.12.2 拒絕載入器設定嘅 JAXP `accessExternalDTD` 屬性。`JposConfig` 將 `jpos.config.regPopulatorClass`（`JavaxRegPopulator`）、`jpos.config.populatorFileURL`（jar 自己嘅 `jar:file:…!/jpos.xml`）同 `javax.xml.parsers.SAXParserFactory`（JDK 嘅）設做系統屬性——已經設咗嘅就唔掂，所以 `-Djpos.config.populatorFileURL=file:…` 仍然可以揀另一個 `jpos.xml`——而當類別路徑上有 `jpos.properties`，或者冇 4.x 載入器嘅時候，佢乜都唔設。Zebra SDK 嘅 `javaPOS114.jar` 就係呢類情況，仲會成個登記表搞爛，所以 `run.bat` 將佢擺喺類別路徑外面——睇 [Zebra 原生程式庫](#zebra-原生程式庫)。`JposConfigTest` 由頭到尾檢查登記表之後的確載入咗個檔嘅掃描器。

**裝置揀選器係由嗰個登記表生成**，唔係喺程式碼度擺一張清單：`ScannerService.logicalNames()` 喺 `gaia-scanner` 執行緒讀已載入嘅項目，留低裝置類別係 `Scanner` 嘅——`jpos.xml` 入面兩個磅嘅項目唔會入——按 `pickerOrder` 排：檔案有定義嘅話 `ZebraAllScanners` 排頭，其餘按字母次序（登記表唔會保留檔案次序）。冇定義佢嘅檔案唔會幫佢補上；乜都冇載入嘅話就淨係提供預設，所以揀選器永遠唔會空。`MainView` 即刻顯示預設，再喺背景執行緒填清單，同時保留儲存咗嘅選擇。所以加入 `jpos.xml` 嘅掃描器——或者用 `-Djpos.config.populatorFileURL` 指定嘅成個檔案——唔使改程式碼就會喺揀選器出現，而 `JposConfigTest` 會將揀選器同獨立讀取附帶檔案嘅結果對照。嗰啲項目指名嘅服務係 Zebra 嘅類別（`com.zebra.jpos.service.scanner.*Factory`），佢哋仍然要喺類別路徑上——經 `run.bat` 由 Scanner SDK 嚟，或者係放入 `lib\` 嘅 Zebra 服務 jar。

掃描資料用 **ISO-8859-1** 解碼，咁每個位元組都一對一對應，AIM Code ID 前綴同每個 `0x1D` 都保得住；報告用 UTF-8 寫。

服務入面冇任何嘢會為使用者組織文字。事件以 `ScannerListener.Event` 常數加上會變嘅值送出，由 `MainView` 喺目錄攞字眼——唔係嘅話，喺一個翻譯成 35 種語言嘅程式入面，狀態列就會係英文。記錄行係例外，保持英文。

### 平台閘門

`ScannerService.isSupportedPlatform()` 係一個 `os.name` 測試，`MainView` 整任何掃描器控制項之前都會問佢：喺 macOS 同 Linux 上面，冇開關、冇裝置揀選器、冇掃描器選單（所以亦冇 <kbd>⌘K</kbd>），亦冇狀態標籤。佢哋唔係收埋——係根本冇整過。

Zebra 嘅 JavaPOS 服務淨係出 Windows 版，而喺其他平台冇嘢會早啲失敗：jar 解析到、控制項整得出，最後到 `open()` 先因為冇原生層而失敗，嗰陣已經過咗幾秒仲彈咗個對話框。

呢條規則講嘅係*服務*，唔係 JavaPOS。如果有供應商出跨平台嘅服務，喺度一樣用得，而放寬規則只需要改 `supportsJavaPos(String)`——佢係 package-private，而且用 `os.name` 字串測試，所以無論喺邊部機行建置，兩個答案都會覆蓋到。

### 條碼制式

`Symbologies` 將 JavaPOS 全部 **78** 種 `SCAN_SDT_*` 掃描資料類型對應去顯示名稱，兩套詞彙對得上嘅地方就再對應去核心嘅 `DataCarrierType`。兩者之中 JavaPOS 分得比較細——佢將 UPC-A 同 EAN-13 分開、普通 Data Matrix 同 GS1 DataMatrix 分開，而 AIM 就將成個家族歸喺一個代碼字元底下。

表以外嘅類型都會有個用得嘅名，係透過對 `ScannerConst` 做反射搵返嚟，因為供應商服務真係會報規格清單以外嘅值。有個測試會行勻 `ScannerConst`，有任何已宣告嘅類型冇名就會失敗——將來 JavaPOS 新加嘅常數都捉得到。

有啲載體對應刻意留做 `UNKNOWN`，而唔係靠估：straight 2 of 5（AIM 按起止條數目分 `]R`/`]S`，但 JavaPOS 唔會報）、四態郵政碼，同埋 TLC-39。

---

## 本地化

標籤每種語言一個屬性檔，放喺 `src/main/resources/i18n/ui_<CODE>.properties` 底下，用同核心錯誤目錄一樣嘅代碼（`EN`、`FR`、`PCM`……）。35 個全部都有，而且 35 個嘅鍵集一模一樣。檔案用 **UTF-8** 讀，唔係 `Properties` 假設嘅 ISO-8859-1，所以翻譯係以睇得明嘅形式儲存，而唔係 `\uXXXX` 跳脫序列。

**後備係逐個鍵，唔係逐個檔**——正因為咁，一種只翻譯咗一部分嘅語言，由第一個鍵入嚟嗰刻起已經有用。英文係合約：每個鍵都一定要喺英文入面有。

| 層 | 鍵數 | 覆蓋範圍 |
|---|---|---|
| 共用報告詞彙 | 31 | 全部 35 種語言 |
| 桌面外框 — 選單、按鈕、建立器、選項、狀態列、報告版面、掃描器狀態、對話框內文 | 114 | 全部 35 種語言 |

共用詞語入面有六個（`generated`、`status`、`dlRole`、`interpretations`、`message`、`level`）呢個程式嘅報告冇用到。
佢哋係刻意保留嘅；檔頭有講明。

### `--language` 參數

`GaiaDesktopApp.languageOverride()` 透過 JavaFX 自己嘅 `getParameters().getNamed()` 讀 `--language=…`，用 `Languages.fromArgument` 解析——目錄代碼、enum 名或者 BCP 47 標籤都得，唔分大細楷——再交俾 `MainView`。

**呢個覆寫係針對今次執行，唔係針對個檔。** `MainView` 將佢載入嘅語言放喺 `savedLanguage`，寫返出去嘅都係*嗰個*，直到 `languageChosenInSession` 話使用者自己揀咗語言為止；之後就照平常保存佢哋嘅選擇。成個設計就係呢個分隔：資訊亭設定檔或者示範捷徑可以用西班牙文開，而唔會改寫部機主人聽日開嘅語言，但喺選項面板做嘅真正選擇仍然會保留。

一個唔對應任何目錄嘅值，會連同接受嘅代碼記錄落嚟然後無視。因為捷徑打錯一個字就拒絕啟動，先係更差嘅失敗；`Languages.fromArgument` 返回 `null` 而唔拋例外，都係同一個原因。

### 加語言

唔使改程式碼。

1. 編輯 `ui_<CODE>.properties`——每種語言都已經有檔案。
2. 翻譯啲值。`{0}` / `{1}` 佔位符原封不動。
3. 建置。有兩個測試執行呢個合約：一個係程式碼要一個英文冇定義嘅鍵就失敗，另一個係任何目錄嘅鍵集同英文唔同就失敗。

第一個測試係喺**原始碼**入面掃鍵嘅字面值，而唔係對類別做反射，因為啲鍵淨係以字串字面值形式喺呼叫位置出現。所以新訊息一定要直接寫埋個鍵去建——`strings.format("st.scannerError", …)`——唔可以經變數去查，否則測試睇唔到。

### 刻意唔翻譯嘅嘢

- 喺原始值隔籬用等寬字型顯示嘅簡短欄位名——元素卡嘅 `format`、`position`、`length`、`raw` 同 `part`，紙帶嘅 `AI`、`sep`、`pos` 同 `len`，同埋標示錯誤位置嘅四格（`AI`、`position`、`level`、`stage`），佢哋嘅值本身已經係冇翻譯嘅 enum 名；
- 範例輸入嘅名稱（佢哋係 GS1 結構嘅名），同條碼制式名稱（佢哋係專有名詞）；
- `GE-C084` 嘅補救段落——標準文本，翻一半仲衰過英文；
- `ElementBuilder` 對未知 AI 嘅最後防線，UI 根本去唔到：編輯器會自己檢查未知 AI，再用目錄報告；
- 記錄行。

仲有一個缺口：`ScanSource.label()` 用英文俾歷史行同報告標頭提供 `manual` / `scanner` / `file` / `built`。要補返唔止加一個鍵咁簡單——`HistoryCell` 冇 `UiStrings`，而清單嘅儲存格工廠只會裝一次，所以轉語言嘅時候連儲存格都要刷新。

---

## 儲存狀態嘅格式

`~/.gaia-desktop/` 底下有兩個檔，分開放係因為佢哋變化嘅節奏唔同——一個壞咗或者俾人手改過嘅擷取記錄，唔應該連累使用者冇咗設定。用 `-Dgaia.desktop.home=…` 覆寫目錄，測試就係咁樣避開真正嘅主目錄。

**`options.properties`** — 選項一改同埋離開時寫入。

| 鍵 | 意思 |
|---|---|
| `parse.mode`、`parse.language` | 管線深度同訊息語言 |
| `date.endian`、`date.separator`、`date.month`、`date.year` | 日期顯示 |
| `check.skipRequires`、`check.skipExcludes` | 跳過結構檢查 |
| `ui.theme` | `LIGHT` 或者 `DARK` |
| `ui.optionsPane` | 選項面板開咗未？ |
| `ui.scannerDevice` | 上次用嘅 JavaPOS 邏輯名稱 |
| `ui.mode` | `PARSE`、`BUILD` 或者 `COMBINE` |
| `update.check` | 每日更新檢查行唔行（預設 `true`） |
| `update.lastCheck` | 上次幾時行，ISO-8601；冇或者讀唔到就當係夠鐘檢查 |
| `update.skipVersion` | 使用者揀咗*略過呢個版本*嘅發佈；唔會再自動提供 |

檔案唔見咗或者壞咗都唔會致命：每個值各自後備，所以一行壞咗只係損失嗰一行，其他嘢冇事。第一次啟動、完全冇檔案嘅時候，選項由 `SystemDefaults` 按部機嘅地區設定初始化，而唔係用 GAIA 內建嘅預設——地區設定嘅短日期格式決定組成部分嘅次序、分隔符，同埋月同年係咪縮寫。

千祈唔好俾 null 落 `Properties.setProperty`：佢會拋例外，而 `persistOptions()` 係由 UI 事件處理常式度行，例外會漏去 FX 執行緒。呢個程式真係試過咁當機。

**`captures.tsv`** — 歷史一改同埋離開時寫入。最新嘅排頭，上限 **500** 項。

```
captured(ISO)              <TAB> source  <TAB> symbology   <TAB> input
2026-08-09T14:22:05.123    SCANNER       Data Matrix       0109506000134352\x1D10LOT-001
```

淨係儲存計唔返嘅嘢。`ParseResult` **唔會**序列化——載入時重新剖析輸入再推返出嚟，所以還原咗嘅歷史反映嘅係*而家*生效嘅選項。輸入經常有 FNC1 同掃描器射出嚟嘅任何嘢，所以輸入欄會做跳脫處理（`\\`、`\t`、`\n`、`\r`，其他所有低過 `0x20` 嘅就用 `\xNN`），而個檔案用 ISO-8859-1 逐個位元組讀寫。

開始記錄日期之前寫嘅檔案淨係有時鐘時間；佢哋仍然載入得到，日期當係今日，因為真正嘅日期從來冇寫落嚟。

---

## 測試

```bash
(cd gaia-desktop && mvn test)
```

96 個測試，刻意**唔使 JavaFX 工具包**，所以又快又可以冇畫面咁行。佢哋涵蓋報告轉譯器、建立邏輯同 AI 查詢、`{GS}` 轉換、由地區設定推出嚟嘅預設、兩個儲存（包括一個帶 `0x1D` 嘅來回測試同刻意整壞嘅檔案）、條碼制式表、掃描器平台規則、版本存根，同埋各個目錄。

其中三個值得知，因為佢哋守護嘅係合約而唔係功能：

| 測試 | 幾時失敗 |
|---|---|
| `CatalogueCoverageTest` | 程式碼要一個 `ui_EN.properties` 冇定義嘅鍵，或者 35 個檔入面任何一個嘅鍵集同英文唔同 |
| `SymbologiesTest` | JavaPOS 宣告嘅任何 `SCAN_SDT_*` 常數冇顯示名稱——包括 JavaPOS 升級新加嘅 |
| `ScannerServiceTest` | 平台規則對 Windows 或者其他任何系統嘅答案變咗 |

`UiStrings` 喺執行期刻意寬鬆——未知嘅鍵會直接顯示個鍵本身，而唔係喺排版中途拋例外——正正因為咁，先一定要有目錄測試。

**冇涵蓋：** 任何需要真正工具包嘅嘢——焦點行為、模式切換、表格編輯、轉語言，同埋[執行緒](#執行緒)講過嘅兩次當機。呢啲係開真視窗操作驗證過，但如果佢哋退化，建置唔會失敗。加入 TestFX 或者 Monocle 就可以補返呢個缺口。

---

## 建置問題排解

| 症狀 | 原因同解決方法 |
|---|---|
| `package tools.pantheum.gaia does not exist` | 核心未安裝。`(cd gaia && mvn install -DskipTests)` |
| *"JavaFX runtime components are missing"* | jar 嘅 `Main-Class` 一定要係 `Launcher`，唔係 `GaiaDesktopApp` |
| 啟動時出 *"Unsupported JavaFX configuration: classes were loaded from unnamed module"* | 預料之內、冇害——JavaFX 只係話佢喺類別路徑上。`Launcher` 存在正正係為咗呢個 |
| jar 開唔到，`NoClassDefFoundError` | 預設 jar 淨係帶呢個模組嘅類別；`dist/lib/` 一定要喺佢隔籬。想要單一自足檔案就用 `-Pfat` |
| Eclipse 出 `Artifact has not been packaged yet … MDEP-187` | m2e 工作區解析。睇[封裝](#封裝)底下嘅註 |
| 建置入面出現重複嘅 `… 2.properties` 檔 | 專案喺 iCloud 同步嘅資料夾，同步喺建置中途整咗衝突副本。一定要用 `mvn clean package`；最好將 `target/` 排除喺同步之外 |
| 選單項目顯示 `act.hri` 呢類鍵 | 嗰個鍵喺 `ui_EN.properties` 冇。建置本應捉到——睇[測試](#測試) |

---

## 已知限制同未完成嘅工作

- **冇無畫面 UI 測試。** 睇[測試](#測試)。
- **產物係按平台分。** 兩種封裝都只帶一個平台嘅 JavaFX 原生程式庫；用 `-Dfx.platform` 每個目標整一個（任何一部機都整得晒）。兩種都冇附帶 Java 執行環境——要咁做就要 `jpackage`，而佢只可以為自己運行緊嘅作業系統建置。
- **掃描器輸入淨係得 Windows。** 睇[平台閘門](#平台閘門)。
- **`ScanSource.label()` 未本地化。** 睇[刻意唔翻譯嘅嘢](#刻意唔翻譯嘅嘢)。
- **`UiState.mode` 係一個 `String`**（`"PARSE"` / `"BUILD"`），但佢隔籬嘅 `Theme` 係 enum，所以呢個字面值喺好多地方出現。改做 enum 就可以清走佢哋。
- **Digital Link 目標淨係得網域同路徑前綴。** `BuilderDigitalLinkConfig` 仲支援額外查詢參數同片段；編輯器冇開放。
- **互斥嘅 AI 對（`GE-S006`）唔會好似未知同重複 AI 咁喺編輯器標示**——建立會直接失敗。
- **核心錯誤目錄會將引號加倍**（`value ''09506000134350''`），喺建立失敗同報告入面原樣出現。睇落係 `MessageFormat` 嘅引號跳脫冇還原；應該喺核心度修正。
