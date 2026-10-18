# Zebra スキャナーのセットアップ — Windows 版 GAIA Desktop

GAIA Desktop が GS1 バーコードを Zebra のバーコードスキャナーから直接読み取れるようにするための、
インストールと設定の手順です。スキャンのたびに解析されて履歴へ追加され、AIM Code ID 接頭辞と
すべての FNC1 区切り文字はそのまま保たれます。原文 (英語):
[GaiaDesktop-Zebra-Scanner-Windows.md](../../GaiaDesktop-Zebra-Scanner-Windows.md)。

GAIA Desktop は、USB 接続の **Zebra DS8108** ハンドヘルドイメージャーで開発・テストされています。
手順は Zebra の Scanner SDK が対応するすべての Zebra 2D スキャナーに当てはまります。機種ごとの
設定用バーコードとメニュー名は、その機種の Quick Reference Guide または Product Reference Guide
を参照してください。

> **Windows のみ。** Zebra のスキャナードライバーは Windows 版しかないため、macOS や Linux では
> GAIA Desktop はスキャナー関連の操作部品を一切作りません。それ以外の機能はすべての環境で動作します。

---

## 目次

1. [必要なもの](#1-必要なもの)
2. [Java のインストール](#2-java-のインストール)
3. [Zebra Scanner SDK のインストール](#3-zebra-scanner-sdk-のインストール)
4. [123Scan のインストール](#4-123scan-のインストール)
5. [Zebra スキャナーのファームウェア更新](#5-zebra-スキャナーのファームウェア更新)
6. [Zebra スキャナーの USB モード設定](#6-zebra-スキャナーの-usb-モード設定)
7. [Zebra スキャナーの GS1 向け設定](#7-zebra-スキャナーの-gs1-向け設定)
8. [GAIA Desktop のインストールと起動](#8-gaia-desktop-のインストールと起動)
9. [スキャナーの接続](#9-スキャナーの接続)
10. [設定リファレンス](#10-設定リファレンス)
11. [トラブルシューティング](#11-トラブルシューティング)
12. [対応する Zebra 2D スキャナー](#12-対応する-zebra-2d-スキャナー)

---

## 1. 必要なもの

| | |
|---|---|
| **スキャナー** | Zebra 2D スキャナーと USB ケーブル — [対応する Zebra 2D スキャナー](#12-対応する-zebra-2d-スキャナー)を参照 |
| **コンピューター** | Windows 10 または 11、64 ビット |
| **Java** | Java 21 以降、**64 ビット** |
| **Zebra Scanner SDK for Windows** | **64 ビット**版。Zebra のサポートサイトから入手 — GAIA Desktop が使うドライバーと JavaPOS サービスを提供します |
| **Zebra 123Scan** | Zebra の無料設定ユーティリティ。Zebra のサポートサイトから入手 — スキャナーのファームウェア更新と設定の確認に使います |
| **GAIA Desktop** | `gaia-desktop-1.0.0-win.zip` |
| **任意** | スキャナーの **Product Reference Guide** (PRG)。設定用バーコードが載っています |

構成要素のつながり:

```
Zebra scanner ──USB──► Zebra CoreScanner driver ──► Zebra JavaPOS services ──► GAIA Desktop
                       (installed by the SDK)        (in the SDK's JPOS\bin)    (run.bat puts them on
                                                                                 the class path)
```

GAIA Desktop は JavaPOS API と、スキャナーを名前で指定する設定 (`gaia-desktop.jar` 内の
`jpos.xml`) を自身に含んでいます。残りは Zebra の SDK が提供します。スキャナーと通信する
ドライバーと、JavaPOS とそのドライバーの間に入る Zebra のサービスクラスです。

---

## 2. Java のインストール

GAIA Desktop には **Java 21 以降**が必要で、64 ビットの Zebra ドライバーに合わせて **64 ビット**版
でなければなりません。

1. Windows x64 用の Java 21 JDK または JRE をインストールします — 例えば adoptium.net の Eclipse
   Temurin。インストーラーが `JAVA_HOME` の設定を提案したら、そのまま設定させてください。
2. コマンドプロンプトで確認します:

   ```
   java -version
   ```

   1 行目にバージョン 21 以降が表示されるはずです。64 ビット版の Java なら `64-Bit Server VM`
   とも表示されます。

`run.bat` は次の順で Java を探します: `run.bat` と同じ場所の `jdk*` フォルダー、次に `JAVA_HOME`、
次に `PATH` 上の `java`。21 より古いものは、その旨のメッセージを出して起動を拒否します。

---

## 3. Zebra Scanner SDK のインストール

1. Zebra のサポートサイトから **Zebra Scanner SDK for Windows (64 ビット)** をダウンロードします。
2. インストーラーを実行します。コンポーネントを選べる場合は、**JPOS** (JavaPOS) コンポーネントが
   含まれていることを確認してください。
3. インストール先は既定のままにします。GAIA Desktop は SDK の JavaPOS 部分を次の場所で探します:

   ```
   C:\Program Files\Zebra Technologies\Barcode Scanners\Scanner SDK\JPOS\
   ```

   そして、その `bin\` フォルダーに Zebra のサービス jar (`JposServiceScanner.jar`、
   `JposServiceJniScanner.jar` など) とネイティブライブラリ (`CSJPOSScanner64.dll` など) がある
   ことを前提とします。SDK を別の場所にインストールした場合は [ZEBRA_HOME](#zebra_home) を参照
   してください。
4. インストール後に **Windows を再起動**します。SDK の CoreScanner ドライバーはバックグラウンドで
   動作し、GAIA Desktop がスキャナーにアクセスする前に起動している必要があります。確実にする
   最も簡単な方法が再起動です。

---

## 4. 123Scan のインストール

123Scan は、Zebra のスキャナーを設定するための Zebra の無料ユーティリティです。次の手順で
スキャナーのファームウェアを更新するのに使い、その後もスキャナーの設定を確認・変更する最も
簡単な手段になります。

1. Zebra のサポートサイトから **123Scan** をダウンロードし、インストーラーを実行します。
2. Zebra スキャナーを接続して 123Scan を起動します。123Scan がスキャナーを見つけ、機種、
   シリアル番号、現在のファームウェアバージョンを表示するはずです。表示されない場合は、先へ
   進む前に別の USB ポートやケーブルを試してください — 123Scan が認識できないスキャナーは
   GAIA Desktop も認識できません。

---

## 5. Zebra スキャナーのファームウェア更新

設定の前に、スキャナーのファームウェアを最新版にしてください。ファームウェア更新は読み取りの
不具合を修正し、スキャナーを SDK がインストールした Zebra ドライバーと整合した状態に保ちます。

1. **GAIA Desktop** と、スキャナーを使っている可能性のある他のプログラムを終了します。更新中、
   123Scan はスキャナーを独占的に使う必要があります。
2. 123Scan でスキャナーを接続した状態で、ファームウェア更新のオプションを選びます。123Scan は
   スキャナーのファームウェアを Zebra が公開している最新版と比較し、ダウンロードとインストール
   を提案します。コンピューターがインターネットに接続できない場合は、Zebra のサイトにある
   その機種のサポートページからファームウェアをダウンロードし、そのファイルを 123Scan に
   読み込ませてください。
3. 更新を開始し、**123Scan が完了を報告するまでスキャナーを接続したままにします**。数分かかり、
   最後にスキャナーが再起動します。途中で抜くとスキャナーが使えなくなることがあります。
4. 123Scan に新しいファームウェアバージョンが表示されていることを確認します。

更新によって一部の設定が既定値に戻ることがあります。そのため、スキャナーの設定はファームウェア
の後、次の 2 つの手順で行います。

---

## 6. Zebra スキャナーの USB モード設定

最も見落とされやすい手順です。**出荷時の Zebra スキャナーは USB キーボードとして動作します**
(USB HID Keyboard モード)。バーコードを、フォーカスのある場所へ「入力」するのです。このモード
では GAIA Desktop の *スキャナーを接続* がスキャナーを見つけられず、テキストフィールドに入力
された内容は FNC1 区切り文字を失うため、GS1 要素ストリングの意味が変わってしまいます。

スキャナーを、Zebra ドライバーが直接通信するモードに切り替えてください:

| スキャナーの USB モード | GAIA Desktop のデバイス名 | 備考 |
|---|---|---|
| **IBM Hand-held USB** | `ZebraUSBHandHeld` または `ZebraAllScanners` | 推奨 |
| SNAPI (イメージングあり/なし) | `ZebraScannerSNAPI` または `ZebraAllScanners` | Zebra 独自の USB プロトコル。これも使えます |
| IBM Table-top USB | `ZebraUSBTableTop` または `ZebraAllScanners` | 卓上型スキャナー向け |
| USB OPOS (Hand-held) | `ZebraUSBOPOS` または `ZebraAllScanners` | |
| USB HID Keyboard (工場出荷時) | — | **接続できません** |

GAIA Desktop の既定のデバイス名 `ZebraAllScanners` は、対応するどのモードのスキャナーも受け付け
ます。モードを設定すれば、たいていは既定値のままで動作します。

モードの変更方法は次のいずれかです:

- スキャナーの Quick Reference Guide または Product Reference Guide (USB Host Types / USB Device
  Type の節) で、そのモード — 例えば *IBM Hand-held USB* — の**設定用バーコードを読み取る**。
  スキャナーがビープ音で確認します。または
- **123Scan を使う** ([手順 4](#4-123scan-のインストール)): スキャナーを接続し、設定を開いて USB
  ホストタイプを設定する。

モードを変えると、Windows はスキャナーを別の種類のデバイスとして再検出します。接続する前に
数秒待ってください。

後でスキャナーをキーボードモードに戻すには — 他のソフトウェアで使うため — *USB Keyboard (HID)*
の設定用バーコードを読み取るか、工場出荷時の設定に戻します。

---

## 7. Zebra スキャナーの GS1 向け設定

GAIA Desktop は、読み取ったバーコードの種類をスキャナーが伝えてくれるときに最もよく解析できます。

### AIM Code ID を送信する

Product Reference Guide (User Preferences) の設定用バーコードか 123Scan で、
**Transmit Code ID Character** を **AIM Code ID Character** に設定します。すると各スキャンの先頭に
シンボル体系の識別子 — GS1-128 は `]C1`、GS1 DataMatrix は `]d2`、GS1 QR Code は `]Q3`、
GS1 DataBar は `]e0` — が付き、GAIA Desktop はこれを使ってデータキャリアを特定し、その規則を
適用します。

これがなくても GAIA Desktop は要素ストリングを解析し、シンボル体系はスキャンそのものから
取得しますが、レポートに AIM Code ID を表示できず、EAN-13 のような単純なバーコードを GS1 要素
ストリングと区別しにくくなります。

### シンボル体系を確認する

使うバーコードの種類が**有効**になっていることを確認してください。一般的には GS1-128、
GS1 DataMatrix、GS1 DataBar、GS1 QR Code、EAN/UPC です。Zebra の 2D スキャナーでは多くが既定で
有効です。現在の設定は 123Scan で確認でき、Product Reference Guide にはそれぞれを有効にする
バーコードが載っています。

### Zebra の GS1 解析機能はオフのままにする

最近のファームウェアには、スキャナー自体で動作する GS1 機能 — **Label Parse+**、**GS1 Digital
Link**、**GS1 Retail POS parsing** — が追加されています。出荷時と同じく**オフ**のままにして
ください。これらはスキャナーが送る内容を整形し直したり短くしたりしますが、GAIA Desktop が解析と
検証を行うには生のデータが必要です。各機能の働きは
[対応する Zebra 2D スキャナー](#12-対応する-zebra-2d-スキャナー)を参照してください。

### プレフィックスとサフィックスは付けない

スキャナーの出力にプレフィックス、サフィックス、Enter キーを付けないでください。GAIA Desktop は
各スキャンを JavaPOS 経由で完結したメッセージとして受け取るため、どれも不要です。余分な文字は
要素ストリングに入り込んでしまいます。

---

## 8. GAIA Desktop のインストールと起動

1. `gaia-desktop-1.0.0-win.zip` を、例えば `C:\Tools\` に展開します。中にはフォルダーが 1 つ
   あります:

   ```
   gaia-desktop-1.0.0-win\
   ├── docs\
   │   └── GaiaDesktop-Zebra-Scanner-Windows.md      this guide
   ├── gaia-desktop.jar
   ├── lib\
   ├── readme.html                                   index of every guide
   └── run.bat
   ```

2. スキャナーを接続します。
3. **`run.bat`** をダブルクリックします。アプリの横にコンソールウィンドウが開いたままになり、
   スキャナーの状態と JavaPOS のエラーがそこに表示されます。
4. コンソールの 1 行目を読みます。`run.bat` が何を見つけたかが表示されています:

   ```
   run.bat: Java 21.0.8 from JAVA_HOME; JavaFX from lib\; JavaPOS from lib\; Zebra services from C:\Program Files\Zebra Technologies\Barcode Scanners\Scanner SDK\JPOS\
   ```

   末尾が **`Zebra services from none (Zebra Scanner SDK not installed)`** の場合、SDK が既定の
   場所で見つかっていません — SDK をインストールする ([手順 3](#3-zebra-scanner-sdk-のインストール))
   か、[ZEBRA_HOME](#zebra_home) を設定してください。アプリは起動しますが、スキャナーは接続
   できません。

---

## 9. スキャナーの接続

1. ツールバーの**デバイス選択欄**には、GAIA Desktop が知っているスキャナー名が並んでいます。
   **`ZebraAllScanners`** のままにするか、[手順 6](#6-zebra-スキャナーの-usb-モード設定) の表から
   スキャナーの USB モードに合う名前を選びます。
2. **スキャナーを接続** をクリックするか、**Ctrl+K** を押します (**スキャナー** メニューにも
   あります)。
3. ステータスバーが **`スキャナー: ZebraAllScanners`** (または選んだ名前) に変わり、状態行に
   *スキャナーを接続しました — ZebraAllScanners* と表示されます。ボタンの表示は
   **スキャナーを切断** に変わります。
4. GS1 バーコードをスキャンします。キャプチャ履歴の先頭に表示され、そのレポートが開きます。
   AIM Code ID がオンなら、レポートにデータキャリア — *GS1-128*、*GS1 DataMatrix* — が表示され、
   要素ストリングには FNC1 区切り文字がすべて残ります。

デバイス選択欄は選んだ値を次回の起動まで覚えています。スキャナーは、切断するかアプリを閉じる
まで接続されたままです。スキャンは **作成** モードと **結合** モードでも
使えます。結合 モードでは、各スキャンが結合するバーコードの 1 つになります。

GAIA Desktop がスキャナーを使用している間は他のプログラムから使えず、その逆も同じです。接続
する前に 123Scan や他の POS・スキャナー用ソフトウェアを終了してください。

---

## 10. 設定リファレンス

### デバイス名

デバイス選択欄は、`gaia-desktop.jar` に同梱の `jpos.xml` から作られます。スキャナーの項目:

| デバイス名 | スキャナーの種類 | 用途 |
|---|---|---|
| `ZebraAllScanners` | すべて | **既定。** 対応するすべての USB モード |
| `ZebraAllScanners114` | すべて | 同じサービスの JavaPOS 1.14 版 |
| `ZebraUSBHandHeld` | IBM Hand-held USB | IBM Hand-held USB モードのスキャナー |
| `ZebraUSBTableTop` | IBM Table-top USB | 卓上モード |
| `ZebraUSBOPOS` | USB OPOS | USB OPOS モード |
| `ZebraScannerSNAPI` | SNAPI | SNAPI モード |
| `ZebraScannerUSB` | すべての USB | すべての USB モード |
| `ZebraScannerSerialSSI` | シリアル SSI | シリアルケーブルのスキャナー (USB ではない) |
| `ZebraScannerSerialNIXMODB` | シリアル NIXMODB | シリアルケーブルのスキャナー |
| `ZebraVDI` | VDI | 仮想デスクトップへ転送されるスキャナー |

### ZEBRA_HOME

Zebra SDK を既定以外の場所にインストールした場合は、`run.bat` を起動する前に `ZEBRA_HOME` を
その `JPOS` フォルダーに設定します — 例えば、横に置いた小さなバッチファイルで:

```bat
@echo off
set "ZEBRA_HOME=D:\Zebra\Scanner SDK\JPOS\"
call "%~dp0run.bat" %*
```

`run.bat` は Zebra のサービス jar を `%ZEBRA_HOME%bin` から取り込み、Zebra の DLL のために
そのフォルダーを `PATH` に追加します。

### 独自の jpos.xml

デバイス名を追加したり項目の設定を変えたりするには、`gaia-desktop.jar` から `jpos.xml` を
取り出し (jar は zip なので、どの展開ツールでも開けます)、そのコピーを編集して GAIA Desktop に
指定します:

```bat
set GAIA_DESKTOP_OPTS=-Djpos.config.populatorFileURL=file:///C:/Tools/jpos.xml
run.bat
```

するとデバイス選択欄には、そのファイルのスキャナーが並びます。複数の Zebra スキャナーを接続
している場合は、`ZebraAllScanners` の項目にあるように、`ScannerModel` または `SerialNumber`
プロパティで項目を特定のスキャナーに結び付けられます。

### その他のオプション

| 設定 | 方法 |
|---|---|
| アプリの言語 | `run.bat --language=FR` (今回の起動のみ)、またはオプションパネル |
| 追加の Java オプション | `run.bat` の前に `set GAIA_DESKTOP_OPTS=…` |
| 同梱の Java | `run.bat` の横に `jdk-21…` フォルダーを置く。`JAVA_HOME` より優先されます |

---

## 11. トラブルシューティング

エラーは、接続時のダイアログ、コンソールウィンドウ、そして `run.bat` を起動したフォルダー
(`run.bat` をダブルクリックした場合は GAIA Desktop のフォルダー) のログファイル
`logs\gaia-desktop.log` に表示されます。ログの角かっこ内の数字が JavaPOS のエラーコードです。

| 表示される内容 | 考えられる原因 | 対処 |
|---|---|---|
| *スキャナーを接続* ボタンがない | Windows で動いていない | スキャンは Windows でのみ使えます |
| 123Scan がスキャナーを見つけない | ケーブル、USB ポート、または他のプログラムがスキャナーを占有している | 別のケーブルやポートを試し、GAIA Desktop や他のスキャナー用ソフトウェアを閉じる。123Scan が認識できないスキャナーは GAIA Desktop も認識できません |
| コンソール: `Zebra services from none (Zebra Scanner SDK not installed)` | SDK が既定の場所にない | 64 ビット版 SDK をインストールする ([手順 3](#3-zebra-scanner-sdk-のインストール)) か、[ZEBRA_HOME](#zebra_home) を設定する |
| エラー **104**: *Could not connect to service with logicalName = …: …SymScannerSvc112Factory* | Zebra のサービス jar が見つからない | 上と同じ — コンソールの `Zebra services from` 行を確認する |
| エラー **109**: *Service does not exist in loaded JCL registry* | デバイス名がスキャナーの設定にない、または SDK の `javaPOS114.jar` がクラスパスに入っている | デバイス選択欄から名前を選ぶ。`javaPOS114.jar` は追加しない。独自の `jpos.xml` を使っているなら、その名前があるか確認する |
| エラー **107** (ハードウェアなし)、またはスキャナーを接続しているのに接続に失敗する | スキャナーがまだ USB キーボードモード、または CoreScanner ドライバーが動いていない | USB モードを設定する ([手順 6](#6-zebra-スキャナーの-usb-モード設定))。SDK のインストール後に Windows を再起動する。別の USB ポートを試す |
| 接続時にエラー **112** (タイムアウト) または **102** (占有中) | 他のプログラムがスキャナーを使っている | 123Scan や他のスキャナー・POS 用ソフトウェアを閉じてから、再度接続する |
| コンソールに `UnsatisfiedLinkError` … `CSJPOSScanner64` | Zebra の DLL を読み込めない — 64 ビット Java に 32 ビット SDK の組み合わせが多い | 64 ビット版 SDK をインストールし、`java -version` が 64 ビット VM と表示するか確認する |
| スキャン内容がキャプチャにならず入力バーに入力される | スキャナーが USB キーボードモード | USB モードを設定する ([手順 6](#6-zebra-スキャナーの-usb-モード設定)) |
| 接続されたがスキャンが届かない | スキャナーのモードに合わないデバイス名、または接続中にスキャナーを抜き差しした | 切断し、`ZebraAllScanners` を選んで再接続する |
| レポートに AIM Code ID やデータキャリアが表示されない | AIM Code ID の送信がオフ | *Transmit Code ID Character → AIM Code ID* をオンにする ([手順 7](#7-zebra-スキャナーの-gs1-向け設定)) |
| 要素ストリングの区切り文字が消えている、または AI が続けてつながっている | キーボードモードでスキャンした、または `{GS}` なしで手入力した | JavaPOS の USB モードを使う。手入力では `{GS}` ボタンで区切り文字を挿入する |
| ある種類のバーコードがまったく読めない | スキャナーでそのシンボル体系が無効になっている | 有効にする ([手順 7](#7-zebra-スキャナーの-gs1-向け設定)) |

問題が解決しない場合は、コンソールの `run.bat:` 行、エラーダイアログの文面、
`logs\gaia-desktop.log` をまとめて保存してください。これらから、どの Java、どの Zebra サービス、
どのデバイス名が使われ、接続がどこで失敗したかが正確にわかります。

---

## 12. 対応する Zebra 2D スキャナー

GAIA Desktop には、GS1 データが印刷されているバーコード — GS1-128、GS1 DataBar、EAN/UPC、
そして多くの用途で 2D コードの **GS1 DataMatrix** と **GS1 QR Code** (**GS1 Digital Link** URI
も通常この形で印刷されます) — を読めるスキャナーが必要です。つまり Zebra の **2D イメージャー**
です。リニア (1D) スキャナーは DataMatrix や QR コードをまったく読めません。

スキャナー自身が GS1 Application Identifier や Digital Link を理解する必要はありません。
**GAIA Desktop が両方を解析します**。スキャナーが読み取ったとおりのスキャン — AIM Code ID も
FNC1 区切り文字もそのまま — を受け取り、AI に分割するか、Digital Link URI をその AI に自分で
変換します。

### テスト済み

| スキャナー | 種類 | 状況 |
|---|---|---|
| **DS8108** | 有線ハンドヘルド 2D イメージャー | GAIA Desktop で**開発・テスト済み** |

### 動作が見込まれるもの

DS8108 と同じく、Zebra の Scanner SDK と JavaPOS サービスで動作する Zebra の 2D イメージャー
です。GAIA Desktop では**テストされていません**。それぞれこのガイドのとおりに設定し、設定用
バーコードはその機種の Quick Reference Guide または Product Reference Guide を使ってください。

| ファミリー | 機種 (例) |
|---|---|
| DS81x8 | DS8108 (有線)、DS8178 (無線) |
| DS22x8 | DS2208 (有線)、DS2278 (無線) — 汎用 |
| DS36x8 | DS3608 (有線)、DS3678 (無線) — 堅牢型。KD と XR の派生モデルを含む |
| DS46x8 | DS4608 (有線)、DS4678 (無線) |
| DS9308 / DS9908 | プレゼンテーション型・ハンズフリー型スキャナー |
| MP7000 / MP7200 | マルチプレーンのバイオプティックスキャナーとスキャナースケール |
| CS6080 | 無線のコンパニオンスキャナー |

無線スキャナーはクレードルと通信し、コンピューターに接続するのはクレードルです。クレードルを
USB で接続し、その組を 1 台のスキャナーとして扱ってください。他の Zebra ファミリーのスキャナー
も、Zebra の Scanner SDK が [手順 6](#6-zebra-スキャナーの-usb-モード設定) のいずれかの USB モードで
対応していれば動作する可能性があります。

### Zebra スキャナー内蔵の GS1 機能

バーコードの読み取りとは別に、一部の Zebra 2D イメージャーは 123Scan で設定して、GS1 データを
スキャナー内で処理できます:

| 機能 | 内容 | GAIA Desktop では |
|---|---|---|
| **Label Parse+** | GS1 ラベルの AI をスキャナー内で解析し、整形し直したフィールドを送る | **オフのまま** — GAIA Desktop が AI、FNC1 区切り文字、AIM Code ID を受け取れなくなる |
| **GS1 Digital Link** | 1D の EAN/UPC と 2D の Digital Link コードの両方が付いた小売商品向け: どちらを送るかを選ぶ | **オフのまま** (既定) |
| **GS1 Retail POS parsing** | Digital Link URI 構文または要素ストリング構文の小売用 2D バーコードを読む | **オフのまま** |

**GS1 Digital Link** の設定には 3 つのモードがあります。**Mode 1 — GTIN only** は有効にした
直後のモードで、どのバーコード由来でも GTIN だけを送ります。そのため Digital Link に含まれる
ロット、有効期限、シリアル番号は GAIA Desktop に届きません。**Mode 2 — Prioritize 2D** は、優先
タイムアウト (既定 200 ms) 内に 2D コードが見えればそれを、見えなければ 1D を送ります。
**Mode 3 — Corroborate 1D and 2D GTINs Match** は 1D を送り、GTIN が一致すれば 2D も送ります。
他のソフトウェアのためにこの設定をオンにしておく必要がある場合は、Mode 1 ではなく Mode 2 を
使ってください。DS8108 では、パラメーター 2373 (オン/オフ)、2374 (モード)、2491 (タイムアウト)
です。

これらの機能を使うには、**123Scan バージョン 6 以降**と、それを含むファームウェアが必要です。
Zebra は 2025 年 4 月からファームウェア更新で GS1 Digital Link 対応を追加しています。以下の日付は
Zebra の販売代理店によるものなので、各機種のファームウェアのリリースノートで確認してください:

| 機種ファミリー | GS1 Digital Link 対応ファームウェアの公開日 |
|---|---|
| DS81x8 | 2025 年 4 月 16 日 |
| MP7000 | 2025 年 4 月 21 日 |
| DS9908 | 2025 年 6 月 6 日 |
| SP72 / SM72 | 2025 年 7 月 11 日 |
| DS46x8 | 2025 年 7 月 28 日 |
| MP7200 (Rev-C)、DS5502 | 2025 年 7 月 30 日 |
| DS9308 | 2025 年 8 月 29 日 |
| DS22x8、DS1000 | 2025 年 9 月 5 日 (アジア太平洋地域のみ) |
| DS36x8 (KD・XR を含む)、MS47 | 2025 年 9 月 30 日 |
| PL5000、SE4107 | 2025 年 10 月 15 日 |
| CS6080 | 2025 年 12 月 31 日 |

GS1 Digital Link コードを GAIA Desktop に読み込むのに、これらの機能はどれも必要ありません。
オフの状態でも、最新ファームウェアの Zebra 2D スキャナーは Digital Link の QR コードを他の
コードと同じように読み取り、GAIA Desktop が URI を AI に変換します。

出典: Zebra『*DS8108 Product Reference Guide*』MN-002926-11EN Rev A (2025 年 8 月)、第 12 章
「GS1 Digital Link Parameters」および Data Parsing の章。Zebra『*123Scan release notes*』。
Barcode Factory「*Upgrade Zebra Scanners for GS1 Digital Link*」(ファームウェアの日付)。
