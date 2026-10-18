# GAIA Desktop — 開発者ガイド

このモジュールのビルド方法、構成、テスト方法を説明します。アプリケーションが外から見て何をするかについては、[ユーザーガイド](GaiaDesktop-User-Japanese.md)を参照してください。原文：[GaiaDesktop-Developer.md](../../GaiaDesktop-Developer.md)。

## 目次

1. [モジュールの概要](#モジュールの概要)
2. [ビルドと実行](#ビルドと実行)
   - [パッケージング](#パッケージング)
3. [アーキテクチャ](#アーキテクチャ)
   - [入力ファネル](#入力ファネル)
   - [スレッド](#スレッド)
   - [再構築と再ラベル付け](#再構築と再ラベル付け)
4. [{GS} トークン](#gs-トークン)
5. [スキャナーの統合](#スキャナーの統合)
   - [プラットフォームゲート](#プラットフォームゲート)
   - [シンボロジー](#シンボロジー)
6. [ローカライズ](#ローカライズ)
   - [`--language` 引数](#--language-引数)
   - [言語の追加](#言語の追加)
   - [意図的に翻訳しないもの](#意図的に翻訳しないもの)
7. [保存状態の形式](#保存状態の形式)
8. [テスト](#テスト)
9. [ビルドのトラブルシューティング](#ビルドのトラブルシューティング)
10. [既知の制限と今後の作業](#既知の制限と今後の作業)

---

## モジュールの概要

```
tools.pantheum:gaia-desktop:1.0.0   Java 21 · JavaFX 21
        └── depends on tools.pantheum:gaia:1.1.0
```

コアライブラリのクライアントであり、それ以上のものではありません。このモジュール自体には **GS1 の知識は一切含まれていません**。AI の定義、検証ルール、エラーメッセージ、ローカライズされた説明はすべて [`GaiaParser`](../../../../gaia/docs/GaiaParser.md) と [`GaiaBuilder`](../../../../gaia/docs/GaiaBuilder.md) から来ます。特に、AI の一覧がこのモジュールにコピーされることはありません。ビルダーのピッカーはコア自身のレジストリを読むため、ビルダーが拒否するような AI を提示することはありえません。

**エントリポイント：** `tools.pantheum.gaia.desktop.GaiaDesktopApp`（JavaFX の `Application`）と `tools.pantheum.gaia.desktop.Launcher`（パッケージ化された jar の `Main-Class`）。

このモジュールは末端（リーフ）です。これに依存するものはなく、これも他の下流モジュールに依存しません。出発点となった 2 つのクラスは、依存関係として参照するのではなく `gaia-demo` から**コピー**されました。`Symbologies` は今でもあちらの双子のクラスと手作業で同期が取られており、両方にその旨の注記があります。

---

## ビルドと実行

- 実行には **Java 21**、ビルドには JDK 21 以降が必要です。モジュールは `release=21` でコンパイルされます。JavaFX 21 は対応する LTS 系列です。コアの jar は今も Java 11 のバイトコードで、21 上でもそのまま動作します。
- `PATH` 上に **Maven** が必要です。このリポジトリには wrapper はありません。
- **先にコアをインストールしてください。** このモジュールは `gaia:1.1.0` を `~/.m2` から解決します。コアの jar が古いか存在しないことが、`package tools.pantheum.gaia does not exist` のよくある原因です。

```bash
(cd gaia && mvn -q install -DskipTests)      # publish the core
(cd gaia-desktop && mvn javafx:run)          # run from source
(cd gaia-desktop && mvn clean package)       # target/dist/gaia-desktop-<version>-<platform>/ — jar (~290 KB), lib/, run.sh (run.bat on win)
(cd gaia-desktop && mvn clean -Pfat package) # one self-contained jar, ~50 MB
(cd gaia-desktop && mvn package -Dfx.platform=win)   # another platform; see Packaging
java -jar gaia-desktop/target/dist/gaia-desktop-1.0.0-mac-aarch64/gaia-desktop.jar
java -jar gaia-desktop/target/dist/gaia-desktop-1.0.0-mac-aarch64/gaia-desktop.jar --language=FR
```

`--language` は唯一のコマンドライン引数であり、プロファイル全体を別の場所に向ける `-Dgaia.desktop.home=…` を除けば、起動に影響を与える唯一の手段です。

`mvn javafx:run` は開発用の経路です。プラグインが JavaFX を**モジュールパス**に置くため、ツールキットは名前付きモジュールとして起動し、`GaiaDesktopApp` を直接起動できます。パッケージ化された jar ではそれができません。すべてがクラスパス上にあるためで、そのため `Main-Class` は `Launcher` というシムになっています。この構成で `Application` のサブクラスを `Main-Class` にすると、*"JavaFX runtime components are missing"* で失敗します。

常に `clean` を使ってください。複数のプラットフォームをビルドする前に 1 回だけ実行します。`target/dist/` からすべてのプラットフォームの内容を空にするためです。理由は[ビルドのトラブルシューティング](#ビルドのトラブルシューティング)を参照してください。

### パッケージング

出荷するものはすべて `target/dist/gaia-desktop-<version>-<platform>/` の下に集められ、それ以外のものは置かれません。そのため、`target/` の中で `classes/`、`maven-status/` などのビルドの一時ファイルと並ぶ成果物を選り分けることなく、このディレクトリをひとまとめに zip 化したりコピーしたりできます。`${dist.dir}` がこれを一箇所で名付けており、成果物を書き出すすべてのプラグインがこれを指しています。

#### プラットフォームごとのビルド

JavaFX の jar にはネイティブコードが含まれるため、各配布物は 1 つのプラットフォーム専用で、`fx.platform` プロパティにより OpenJFX の分類子として命名されます：`mac-aarch64`、`mac`、`win`、`linux`、`linux-aarch64`。OS によって有効化される 5 つの `fx-*` プロファイルがこれをビルドするマシン自身のものに設定し、通常の `mvn package` や `mvn javafx:run` にはそれだけで足ります。`-Dfx.platform=…` はそれらより優先され、どのマシンからでも他のどのプラットフォームでもビルドできます。JavaFX の jar はダウンロードされるだけだからです。`${dist.dir}` は `target/dist/${fx.platform}` なので、あるプラットフォームのビルドが別のプラットフォームのビルドを上書きすることはありません。

```bash
(cd gaia-desktop && mvn -q clean package)    # this machine's platform, with the tests
for p in win linux mac mac-aarch64; do (cd gaia-desktop && mvn -q package -DskipTests -Dfx.platform=$p); done
```

これを可能にしている pom の詳細が 2 つあり、どちらも削除できそうに見えます（が、できません）：

- **すべての JavaFX モジュールが直接の依存関係です**。base、graphics、controls、media、web のそれぞれに `<classifier>${fx.platform}</classifier>` と `org.openjfx:*` の除外が付いています。OpenJFX の pom は推移的な jar を*ビルドするマシン*の OS で選ぶため、graphics や base を推移的に取り込ませると、Windows のビルドに Windows のネイティブと並んで Mac のネイティブが入ってしまいます。
- **プロパティは `javafx.platform` ではなく `fx.platform` です。** OpenJFX 自身のプロパティを外部から設定すると、その親 pom がそれを自分自身を使って再定義します。Maven はその pom を無効として拒否し、プラットフォームの jar を黙って落とすため、JavaFX をまったく含まないビルドができあがります。

どちらの構成も同じパス `target/dist/gaia-desktop-<version>-<platform>/gaia-desktop.jar` に書き出し、ビルドごとにちょうど一方だけが実行されます。両方がそれを奪い合わないようにしているのが `${jar.phase}`、`assembly.skipAssembly`、`mdep.skip` です。

| | デフォルト | `-Pfat` |
|---|---|---|
| `gaia-desktop.jar` | 約 290 KB — このモジュールのクラスとリソースのみ | 約 50 MB — 依存関係を展開して格納 |
| `dist/gaia-desktop-<version>-<platform>/lib/` | 17 個の jar。マニフェストの `Class-Path` に 1 つずつ記載 | — |
| 出荷物 | jar **と**その隣の `lib/` | ファイル 1 つ |
| `dist/gaia-desktop-<version>-<platform>/` にはほかに | `run.bat`（win）または `run.sh`（その他） | 同じ |
| さらに | `readme.html` と `docs/`（全プラットフォーム） | 同じ |

すべての配布物にはガイドも含まれます。`docs/` は構成を保ったままコピーされるため、ガイドと翻訳の間のリンクはそのまま機能します。ただし翻訳用ツール（`check-i18n.py`、`i18n-manifest.json`、`i18n-plan.md`）は除かれます。そしてルートには `readme.html` があり、各ガイドへのリンクを並べた索引です。これは `src/main/dist/readme.html` から作られ、`@VERSION@` が pom のバージョンに置き換えられます。どちらも `pick-launch-script` antrun ステップが書き出し、このステップは最初に `docs/` を削除するので、削除されたガイドが残ることはありません。readme のリンクは手書きです。`docs/` の下でガイドの名前を変えたり翻訳を追加したりしたら、そちらにも行を追加する必要があります。

マニフェストの `Class-Path` と `lib/` は 1 つのものの 2 つの半分です。jar はすべての実行時依存関係について `lib/<artifact>-<version>.jar` を記載し、`copy-dependencies` はまさにその名前で `lib/` を埋めます。これが `java -jar` が動作する理由です。

#### macOS アプリと配布用 zip

1 つの antrun 実行 `mac-app-and-zip` が `package` フェーズを締めくくります。そのため antrun プラグインは jar プラグインと assembly プラグインの**後に**宣言されています。Maven はフェーズ内の実行をプラグインの宣言順に実行するので、どちらのパッケージングでも、これが動くときには jar がすでに存在します。（もう一つの実行 `pick-launch-script` は `prepare-package` にあり、影響を受けません。）

`mac` と `mac-aarch64` の場合、まず配布ディレクトリに `GAIA Desktop.app` を作ります。`Info.plist`（`@VERSION@` を pom のバージョンに置換）、ランチャー `Contents/MacOS/gaia-desktop`（実行可能にする）、`gaia-desktop.icns` で、いずれも `src/main/macos/` から来ます。その後 `gaia-desktop.jar` と `lib/` を `Contents/Java` に**移動**し、バンドルが単独で動くようにします。`run.sh` は隣に jar がないときにそこを探します。ランチャーはバージョンで Java を探し（まず `java_home -v 21+`、次に Homebrew の openjdk。Finder からの起動では `JAVA_HOME` も `PATH` も見えません）、見つからなければ `osascript` でダイアログを表示し、logback の相対パス `logs/` に書き込めるよう `~/.gaia-desktop` に `cd` し、`-Xdock:name`/`-Xdock:icon` を渡します。macOS は実行中の `java` をバンドルに関連付けます（バンドル ID と Dock アイコンはアプリのものになります）。メニューバーには GAIA Desktop と表示されます。

次に、すべてのプラットフォームについて、Ant の `<zip>` でディレクトリを同名のフォルダの下に入れた `target/dist/gaia-desktop-<version>-<platform>.zip` に圧縮します。`run.sh` と `Contents/MacOS/**` は `filemode="755"` 付きの別のファイルセットにします。そうしないと、展開したときに実行できなくなります。この zip は**アタッチされません**。GitHub のリリース用であり、4 つのプラットフォームは 4 回のビルドから作られるため、バージョンごとに不変のアップロードを 1 回しか受け付けない Maven Central では 1 つのリリースとして受け取れないからです。

`project.build.outputTimestamp` はビルド時刻に設定されます。そうしないと、maven-jar-plugin 3.5 と maven-assembly-plugin 3.8 のアーカイバーがすべてのエントリの日付を 1980 年 2 月 1 日にしてしまいます。

アプリのアイコンは `tools/icon/GaiaIcon.java` が描画します（ビルドの一部ではありません。ヘッダーに `sips`/`iconutil` の手順があります）。その出力はリポジトリにコミットされています。バンドル用の `src/main/macos/gaia-desktop.icns` と、`GaiaDesktopApp` がウィンドウアイコンとして設定する `src/main/resources/icons/gaia-desktop-{16..256}.png` です（Windows と Linux。macOS はウィンドウアイコンを無視します）。

#### 起動スクリプト

`run.sh` と `run.bat` は `src/main/scripts/` から来ており、フィルタリングせずに `dist/gaia-desktop-<version>-<platform>/` にコピーされます。`run.sh` には `${...}` のシェル展開が多数あり、Maven のフィルタリングがそれを書き換えてしまいかねないからです。その後 antrun ステップが、そのプラットフォームで実行するものだけを残し（`win` なら `run.bat`、それ以外はすべて `run.sh`）、`copy-resources` が引き継がないため `run.sh` に実行ビットを設定します。プロファイル経由ではなく解決済みの `${fx.platform}` で判断するのは、プロファイルの有効化では別のプロファイルが設定した値が見えないからです。そうしないと、Windows 上で行った Linux 向けのビルドがホストのスクリプトを受け取ってしまいます。

スクリプトは**マニフェストに依存しません**。それぞれが自分でクラスパスを組み立てます。まず `gaia-desktop.jar`、続いて `lib/` 内のすべての `.jar` を、`lib/*` ワイルドカードに任せず 1 つずつ並べます。こうすることで、空白を含むパスも 1 つのエントリのままになり、追加した数も正確になります。`-cp` で起動することが、`tools.pantheum.gaia.desktop.Launcher` を明示的に指定している理由です。`-cp` はマニフェストの `Main-Class` を読まないからです。しかし `Class-Path` には**引き続き**従います。`-cp` 上の jar はそのマニフェストが記載する jar を連れてきて、その直後に検索されます。そのため、スクリプトが記載するかどうかにかかわらず `lib/` の jar はすべて読み込まれ、スクリプトのクラスパスから 1 つを外しても JVM から締め出すことはできません。それでもスクリプトはすべてを列挙するので、マニフェストが古い、手で編集された、あるいはエントリが欠けている jar でも起動します。同じスクリプトが両方のパッケージングに使われます。`-Pfat` の jar には追加する `lib/` がなく、必要もありません。空の `lib/` がエラーではなく注意にとどまるのもそのためです。

#### Zebra のネイティブライブラリ

ビルドには Zebra のものは何も含まれません。DLL もサービス jar もありません。Windows では、それらはマシンにインストールされた Zebra の Scanner SDK から来ます。`run.bat` はその `Jpos*.jar` サービス jar と `bin\` をクラスパスに、`bin\` を `PATH` に追加し、そこで `System.loadLibrary` が `CSJPOSScanner.dll` / `CSJPOSScanner64.dll` を見つけます（`java.library.path` のデフォルトは `PATH` です）。JavaPOS API とその設定ローダーは、SDK の有無にかかわらず常に `lib\javapos-*.jar` から来ます。

**`run.bat` は SDK 自身の JavaPOS jar** である `External\JavaPOS-1.14.0-Dist\javaPOS114.jar` **を決して追加しません**。かつては追加しており、API が 2 つにならないよう `lib\javapos-*.jar` を外していました。しかしマニフェストの `Class-Path` がいずれにせよそれらを読み込んだため（上記参照）、JVM は両方を受け取りました。`lib\` からの 4.x ローダークラスと、その隣に `javaPOS114.jar` の `jpos/res/jpos.properties` と `jcl.xsd` です。このプロパティファイルにより [`JposConfig`](#スキャナーの統合) は手を引き、その `jcl.xsd` は 1.10 までの JavaPOS バージョンしか受け付けないため、`jpos.xml` のすべてのエントリ（1.12–1.14）が検証に失敗し、レジストリは空のままとなり、スキャナーに到達する前にすべての接続がエラー 109 で失敗しました。その jar がクラスパス上にあるだけで、たとえ `lib\` の jar より後ろにあってもそうなります。Zebra のサービス jar は `lib\` の 1.14 API 上でそのまま動作します。オープン、クレーム、スキャンは、その構成で実機のスキャナーを使って検証済みです。`run.bat` の隣に手で置いた DLL も引き続き尊重されます。その場合スクリプトは `-Djava.library.path=<dist>;%PATH%` を設定します。このプロパティはデフォルトの検索パスを完全に置き換えるため、`PATH` を後ろに付け足しています。

`mvn javafx:run` はこれらを一切行わないため、Windows で開発実行からスキャナーをクレームするには、SDK の jar をクラスパスに、その `bin\` を `PATH` に別の手段で置く必要があります。そのためには `dist/` から実行してください。

クラスパス以外にも、スクリプトは `java -jar` がしないことを引き受けます。自分のディレクトリを解決してどの作業ディレクトリからでも動くようにすること、`PATH` より `JAVA_HOME` を優先すること、そして 11 より前の JVM を `UnsupportedClassVersionError` ではなく対処法を示す一文で拒否することです。`GAIA_DESKTOP_OPTS` は JVM オプションをそのまま渡します。`run.bat` は `javaw.exe` ではなく `java.exe` を使うので、コンソールは開いたままになります。スキャナーが動くのは Windows だけで、その状態や JavaPOS のエラーはそこに出力されます。

`run.bat` はさらに、見つかったものから Java、JavaFX、Zebra のサービスを選びます。同梱の `jdk*\`、モジュールパス上の `javafx-sdk*\lib\`、`ZEBRA_HOME` にある Zebra の Scanner SDK です。SDK の JavaFX を使うときは `lib\` の JavaFX のコピーをクラスパスから外し、JDK の SAX パーサー、Windows のプロキシとトラストストア、そして Java 17 以降では `--enable-native-access` を渡します。表は README の [What `run.bat` adds on Windows](../../../README.md#what-runbat-adds-on-windows) にあります。このファイルの CRLF 改行は維持してください。LF のみのバッチファイルでは、`cmd.exe` が `call :label` を見落とすことがあります。`run.sh` も `--enable-native-access=ALL-UNNAMED` を渡します。JavaFX は自身のネイティブを `lib/` から読み込み、JavaFX がクラスパス上にあるときにそれを包含するモジュールは `ALL-UNNAMED` だからです。`run.bat` と同様、Java のバージョンを読み取れないときだけ省略されます。

どちらの構成も **1 つのプラットフォーム**（`${fx.platform}`）の JavaFX ネイティブしか含まないため、OS やアーキテクチャをまたいで持ち運ぶことはできません。ターゲットごとに 1 回ビルドしてください。[プラットフォームごとのビルド](#プラットフォームごとのビルド)を参照してください。

> **Eclipse。** `copy-dependencies` の実行には `<?m2e ignore?>` 指示が付いています。m2e は開いているワークスペースのプロジェクトを jar ではなく `target/classes` フォルダーに解決し、`copy-dependencies` はパッケージ化されていない成果物のコピーを拒否する（MDEP-187）ため、`gaia` プロジェクトがこのプロジェクトと並んで開かれているときは自動ビルドが失敗してしまいます。その結果、IDE だけで行ったビルドでは `target/dist/gaia-desktop-<version>-<platform>/lib/` が埋まりません。jar そのものを使う前に `mvn package` を実行してください。

---

## アーキテクチャ

| パッケージ | 内容 |
|---|---|
| `desktop` | `GaiaDesktopApp`、`Launcher`、`Theme`、`GaiaDesktopVersion` |
| `desktop.ui` | `MainView`（ウィンドウ）、`BuilderPane`、`OptionsPane`、`HistoryCell`、`EditCell` |
| `desktop.build` | `ElementBuilder`（JavaFX に依存しないビルドロジック）、`AiPair`（エディターの 1 行） |
| `desktop.parse` | `ParseOptions`（監視可能な `ParseConfig`）、`SystemDefaults`、`Languages`、`GsToken` |
| `desktop.scanner` | `ScannerService`（JavaPOS）、`ScannerListener`、`Symbologies` |
| `desktop.html` | `GaiaDesktopReport` — レポートレンダラー。JavaFX に依存せず、ツールキットなしで単体テストされる |
| `desktop.i18n` | `UiStrings` — ラベルのカタログ |
| `desktop.store` | `AppPaths`、`OptionsStore`、`CaptureStore` |

### 入力ファネル

すべての入力（入力、貼り付け、ファイルから開いたもの、スキャン、ビルドしたもの）は、1 つのメソッド `MainView.capture(String, ScanSource, String)` を通ってパーサーに届きます。

これがアプリ全体の土台となっている唯一の設計判断です。ビルドには独自のレポートも、独自の履歴も、独自のエクスポート経路も要りません。ファネルを通った後では、ビルドした文字列はスキャンした文字列と区別がつかないからです。入力を生み出す新しいものは、その脇を通るのではなく、ここを通るべきです。

### スレッド

| スレッド | 実行するもの |
|---|---|
| JavaFX アプリケーションスレッド | すべての解析、描画、UI。AI テーブルがウォームアップされれば解析は 1 ミリ秒未満なので、ワーカーは不要 |
| `gaia-scanner` | スキャナーへのすべての呼び出し（オープン、クレーム、有効化、無効化、リリース、クローズ）。`ScannerService` が所有する長寿命のスレッド 1 本。[スキャナーの統合](#スキャナーの統合)を参照 |
| JavaPOS イベントスレッド | `ScannerService` のコールバック。`MainView.FxScannerListener` がそれぞれを `Platform.runLater` で引き渡す |
| バックグラウンド | スキャナーの `connect()` / `disconnect()` の待機（クレームには最大 5 秒かかることがある）、起動時の保存済み履歴の復元（パーサーのウォームアップを兼ねる）、そして更新チェック（`gaia-update-check`。GitHub へのリクエスト 1 回で、結果は `Platform.runLater` で戻される） |

**コントロール自身の選択イベントの中で、そのコントロールの選択を変更したり、コントロールを破棄したりしてはいけません。** このアプリで起きた 3 つの別々のクラッシュは、まさにそれが原因でした。ポップアップ自身の `clearAndSelect` の中で AI ピッカーをリセットしたこと（`ListViewBehavior` からの `IndexOutOfBoundsException`）と、言語コンボボックスを所有するパネルをそのコンボボックス自身のハンドラーの中で再構築したことです。どちらも現在は `Platform.runLater` で遅延させており、その理由は両方の呼び出し箇所に記録されています。

### 再構築と再ラベル付け

言語を変更すると、外枠（メニュー、ツールバー、オプションパネル）がカタログから**再構築**されます。これが安全なのは、どれも状態を持たないからです。`OptionsPane` は `ParseOptions` の双方向ビューなので、値はモデルの中にあり、新しいパネルはすぐにそれを読み直します。

**ユーザーデータ**を持つコントロールはフィールドであり、代わりにその場でラベルを付け直します。`BuilderPane.retext(UiStrings)` は AI の行、Digital Link のフィールド、選択を保持します。これを再構築すると作業中の内容が失われます。実際にかつてそうなっていました。

---

## {GS} トークン

JavaFX の `TextField` は `0x20` 未満の文字をすべて黙って捨てます（`TextInputControl.filterInput`）。「表示がおかしくなる」のではなく、削除されるのです。入力、貼り付け、`setText`、`insertText` のすべてが影響を受けるので、`setText("01…" + GS + "10LOT")` の結果は `"01…10LOT"` になります。可変長の AI が後に続くものまで飲み込んでしまうため、意味が知らないうちに変わったエレメント文字列です。

`desktop.parse.GsToken` がその答えのすべてです。区切り文字は UI の中ではリテラルのテキスト `{GS}` として運ばれ、パーサーに向かう途中で再び `0x1D` になります。このトークンはここで考え出されたものではありません。コアの `ASCIIRevealerUtils.printable` が `0x1D` を表示するときの形そのものなので、レポートからコピーした値はそのまま入力バーに貼り戻せます。

入力フィールドの `TextFormatter` フィルターは、テキストが届きうるすべての経路（入力、⌘V、コンテキストメニュー、ドラッグ＆ドロップ、`setText`）を捉えます。書き換えを各ハンドラーではなくそこに置いているのはそのためです。

スキャナーの経路は `GsToken` を通りません。スキャンはバイトとしてパーサーに届きます。

---

## スキャナーの統合

`ScannerService` は JavaPOS の `Scanner` コントロールを所有します。オープン → クレーム → 有効化の順で、スキャンのたびにデータイベントを再度有効にします。論理デバイス名は `src/main/resources/jpos.xml` から来ます。

**すべてのデバイス呼び出しは、サービスと同じだけ生き続ける 1 本のスレッド `gaia-scanner` で実行されます**。FX スレッドでも、呼び出し後に終了するスレッドでも決して実行しません。Zebra のサービスは CoreScanner COM API を介して JNI でスキャナーにアクセスし、COM は設定したものを呼び出し元スレッドのアパートメントに結び付けます。後で終了するスレッドでオープンすると、スキャナーはクレームできても沈黙したままになります。別のスレッドでクローズすると、間違ったアパートメントが初期化解除されます。FX スレッドでのクローズは最悪のケースです。JavaFX はそこで `OleInitialize` を呼び、すべてのウィンドウのドロップターゲットをそれに対して登録しているので、そこで `CoUninitialize` が呼ばれるとアプリ全体のドラッグ＆ドロップが使えなくなります。これは Windows 上の JavaFX の問題であって、特定の JavaFX バージョンの問題ではありません。`connect()` と `disconnect()` はそのスレッドに投入して待機するので、`MainView` は両方を短命のバックグラウンドスレッドから呼びます。このパターンはビューアーの `JposScannerSession` から来ており、ドラッグ＆ドロップの不具合が最初に見つかったのもそこでした。

2 つのルールが、不調なデバイスによって接続が固まるのを防いでいます。**後始末は決して途中でやめません。**`closeQuietly` は無効化、リリース、クローズを別々のステップとして行い、クレームを保持しているときだけリリースし、常に `close()` を試みます。クレームに失敗した後は `release()` が「クレームされていない」という例外を投げ、各ステップが 1 つの `try` を共有していた頃は、それによってクローズが飛ばされ、デバイスが Zebra のサービス内で開いたままになっていました。**データイベントは `finally` の中で再度有効にします。**JavaPOS はイベントのたびにそれを無効にするので、読み取りやハンドラーが例外を投げても再度有効にしなければなりません。そうしないと、再接続するまでスキャナーが沈黙します。`ScannerLifecycleTest` は、スクリプト化された `jpos.Scanner` を相手にこの両方を検証します。

**`JposConfig` は最初のオープンの前に、JavaPOS を同梱の `jpos.xml` に向けます。** Maven の JavaPOS jar（`javapos-config-loader` 4.x）を使い、クラスパス上に `jpos/res/jpos.properties` がない場合、そうしなければすべてのオープンが *"Service does not exist in loaded JCL registry"*（エラー 109）で失敗します。理由は 3 つあります。サービスマネージャーが `SimpleRegPopulator` にフォールバックし、これはシリアライズされた `jpos.cfg` を読み、`jpos.xml` は決して読まないこと。XML ローダーは `jpos.xml` を作業ディレクトリでしか探さず、jar の中は探さないこと。そして `lib/` にある Xerces 2.12.2 が、ローダーが設定する JAXP の `accessExternalDTD` プロパティを拒否することです。`JposConfig` は `jpos.config.regPopulatorClass`（`JavaxRegPopulator`）、`jpos.config.populatorFileURL`（jar 自身の `jar:file:…!/jpos.xml`）、`javax.xml.parsers.SAXParserFactory`（JDK のもの）をシステムプロパティとして設定します。すでに設定されているものは決して上書きしないので、`-Djpos.config.populatorFileURL=file:…` で別の `jpos.xml` を選ぶこともできます。また、クラスパス上に `jpos.properties` があるとき、あるいは 4.x ローダーがないときは何も設定しません。Zebra SDK の `javaPOS114.jar` がまさにそのケースで、レジストリを完全に壊してしまいます。`run.bat` がそれをクラスパスから外しているのはそのためです。[Zebra のネイティブライブラリ](#zebra-のネイティブライブラリ)を参照してください。`JposConfigTest` は、その後レジストリがファイルのスキャナーを保持していることをエンドツーエンドで確認します。

**デバイスピッカーはそのレジストリから生成され**、コード内のリストとしては保持されていません。`ScannerService.logicalNames()` は `gaia-scanner` スレッド上で読み込まれたエントリを読み、デバイスカテゴリーが `Scanner` のものだけを残し（`jpos.xml` にある 2 つのはかりのエントリは除外されます）、`pickerOrder` で並べます。ファイルが `ZebraAllScanners` を定義していればそれが先頭で、残りはアルファベット順です（レジストリはファイル内の順序を保持しません）。それを定義していないファイルに補われることはありません。何も読み込まれていないときはデフォルトだけが提示されるので、ピッカーが空になることはありません。`MainView` はすぐにデフォルトを表示し、保存された選択を保ったまま、バックグラウンドスレッドから一覧を埋めます。したがって、`jpos.xml` に追加したスキャナー、あるいは `-Djpos.config.populatorFileURL` で指定したファイル丸ごとが、コードを変更せずにピッカーに現れます。`JposConfigTest` は、ピッカーを同梱ファイルの独立した読み取り結果と照合します。それらのエントリが指すサービスは Zebra のクラス（`com.zebra.jpos.service.scanner.*Factory`）で、これは引き続きクラスパス上にある必要があります。`run.bat` 経由で Scanner SDK から来るか、`lib\` に置いた Zebra のサービス jar から来ます。

スキャンデータは **ISO-8859-1** としてデコードされるため、各バイトが 1 対 1 で対応し、AIM Code ID のプレフィックスもすべての `0x1D` も失われません。レポートは UTF-8 で書き出されます。

サービスの中には、ユーザー向けの文章を組み立てるものは何もありません。イベントは `ScannerListener.Event` 定数と変化する値として送り出され、`MainView` がカタログから文言を補います。そうしなければ、35 通りに翻訳されたアプリケーションの中でステータス行だけが英語になってしまいます。ログ行は例外で、英語のままです。

### プラットフォームゲート

`ScannerService.isSupportedPlatform()` は `os.name` の判定 1 つであり、`MainView` はスキャナーのコントロールを作る前にこれに問い合わせます。macOS と Linux では、トグルも、デバイスピッカーも、スキャナーメニューも（したがって <kbd>⌘K</kbd> も）、状態ラベルもありません。隠されているのではなく、そもそも作られないのです。

Zebra は JavaPOS サービスを Windows 向けにしか提供しておらず、他の環境では何も早い段階で失敗しません。jar は解決され、コントロールも作成され、ネイティブ層がないために最終的に失敗するのは `open()` で、そのときにはすでに数秒が経ち、ダイアログが 1 つ表示されています。

このルールは JavaPOS ではなく*サービス*に関するものです。クロスプラットフォームのサービスを提供するベンダーであればここでも動作し、ルールを広げるには `supportsJavaPos(String)` だけを変更すればよいのです。これはパッケージプライベートで、`os.name` の文字列に対してテストされているため、どのマシンでビルドを実行しても両方の答えがカバーされます。

### シンボロジー

`Symbologies` は、JavaPOS の **78** 種類すべての `SCAN_SDT_*` スキャンデータ型を表示名に対応付け、2 つの語彙が対応する場合はコアの `DataCarrierType` にも対応付けます。2 つのうち JavaPOS のほうが細かく、UPC-A と EAN-13、通常の Data Matrix と GS1 DataMatrix を区別します。一方 AIM は 1 つのファミリー全体を 1 つのコード文字の下にまとめます。

表にない型にも使える名前が付きます。ベンダーのサービスは実際に仕様の一覧にない値を報告するため、`ScannerConst` に対するリフレクションで名前を見つけるのです。あるテストは `ScannerConst` をたどり、宣言された型に名前のないものがあれば失敗します。これは将来の JavaPOS が追加する定数も捕捉します。

いくつかのキャリアの対応付けは、推測せずに意図的に `UNKNOWN` のままにしてあります。ストレート 2 of 5（AIM は開始・終了バーの本数で `]R`/`]S` を分けますが、JavaPOS はそれを報告しません）、4 ステートの郵便コード、そして TLC-39 です。

---

## ローカライズ

ラベルは言語ごとに 1 つのプロパティファイルとして `src/main/resources/i18n/ui_<CODE>.properties` の下にあり、コアのエラーカタログと同じコード（`EN`、`FR`、`PCM` など）を使います。35 個すべてが存在し、35 個すべてがまったく同じキーの集合を持っています。ファイルは `Properties` が前提とする ISO-8859-1 ではなく **UTF-8** として読まれるので、翻訳は `\uXXXX` エスケープではなく読める形で保存されています。

**フォールバックはファイル単位ではなくキー単位です**。そのおかげで、部分的に翻訳された言語でも、最初のキーが入った瞬間から役に立ちます。英語が契約です。すべてのキーは英語に存在しなければなりません。

| 層 | キー数 | 対応範囲 |
|---|---|---|
| 共有のレポート語彙。`gaia-demo` の `GaiaHtmlReport` から取り込んだもの | 31 | 全 35 言語 |
| デスクトップの外枠 — メニュー、ボタン、ビルダー、オプション、ステータス行、レポートのレイアウト、スキャナーの状態、ダイアログ本文 | 114 | 全 35 言語 |

共有の用語のうち 6 つ（`generated`、`status`、`dlRole`、`interpretations`、`message`、`level`）は、このアプリのレポートでは使われません。共有ブロックをデモの語彙の忠実なコピーに保つために残してあり、ファイルのヘッダーにその旨が書かれています。

### `--language` 引数

`GaiaDesktopApp.languageOverride()` は JavaFX 自身の `getParameters().getNamed()` を通じて `--language=…` を読み、`Languages.fromArgument` で解決し（カタログコード、enum 名、BCP 47 タグのいずれでもよく、大文字小文字は区別しません）、`MainView` に渡します。

**この上書きはファイルではなく、その実行に対するものです。** `MainView` は読み込んだ言語を `savedLanguage` に保持し、`languageChosenInSession` がユーザー自身が言語を選んだことを示すまでは、*その*言語を書き戻します。ユーザーが選んだ後は、その選択が通常どおり保存されます。この分離こそが設計のすべてです。キオスク用のプロファイルやデモ用のショートカットは、マシンの持ち主が明日開くときの言語を書き換えることなくスペイン語で開くことができ、一方でオプションパネルで行った本当の選択はきちんと残ります。

どのカタログにも当たらない値は、受け付けるコードと一緒にログに記録され、無視されます。ショートカットのタイプミスのせいで起動を拒否するほうが悪い失敗であり、`Languages.fromArgument` が例外を投げずに `null` を返すのも同じ理由です。

### 言語の追加

コードの変更は不要です。

1. `ui_<CODE>.properties` を編集します。どの言語にもすでにファイルがあります。
2. 値を翻訳します。`{0}` / `{1}` のプレースホルダーはそのまま残してください。
3. ビルドします。2 つのテストが契約を強制します。1 つはコードが英語に定義されていないキーを要求した場合に、もう 1 つはいずれかのカタログのキー集合が英語からずれた場合に失敗します。

最初のテストは、クラスをリフレクションで調べるのではなく、**ソースコード**からキーのリテラルを探します。キーは呼び出し箇所の文字列リテラルとしてしか存在しないからです。そのため、新しいメッセージは変数経由で参照するのではなく、`strings.format("st.scannerError", …)` のようにキーをインラインで書いて組み立てなければなりません。そうしないとテストから見えません。

### 意図的に翻訳しないもの

- 生の値の横に等幅フォントで表示される短いフィールド名。エレメントカードの `format`、`position`、`length`、`raw`、`part`、テープの `AI`、`sep`、`pos`、`len`、そしてエラーの位置を示す 4 つのセル（`AI`、`position`、`level`、`stage`）で、これらの値はそもそも翻訳されない enum 名です。
- GS1 の構成要素を指すサンプル入力の名前と、固有名詞であるシンボロジーの名前。
- `GE-C084` の対処方法の段落。中途半端な翻訳は英語より悪くなる規格の文章です。
- `ElementBuilder` の未知の AI に対する最後の防御。これは UI からは到達できません。エディターが自分で未知の AI をチェックし、カタログから報告するからです。
- ログ行。

1 つだけ穴が残っています。`ScanSource.label()` は履歴の行とレポートの見出しに `manual` / `scanner` / `file` / `built` を英語で提供しています。これを塞ぐにはキーを足すだけでは足りません。`HistoryCell` は `UiStrings` を持たず、リストのセルファクトリーは 1 回しかインストールされないので、言語変更時にセルも更新する必要があります。

---

## 保存状態の形式

`~/.gaia-desktop/` の下に 2 つのファイルがあり、変化する周期が違うので分けてあります。キャプチャログが壊れたり手で編集されたりしても、ユーザーの設定まで失われるべきではないからです。ディレクトリは `-Dgaia.desktop.home=…` で上書きでき、テストが実際のホームディレクトリに触れずに済むのもこの方法によります。

**`options.properties`** — オプションが変わったときと、終了時に書き込まれます。

| キー | 意味 |
|---|---|
| `parse.mode`、`parse.language` | パイプラインの深さとメッセージの言語 |
| `date.endian`、`date.separator`、`date.month`、`date.year` | 日付の表示 |
| `check.skipRequires`、`check.skipExcludes` | 構造チェックの回避 |
| `ui.theme` | `LIGHT` または `DARK` |
| `ui.optionsPane` | オプションパネルが開いているか |
| `ui.scannerDevice` | 最後に使った JavaPOS の論理名 |
| `ui.mode` | `PARSE`、`BUILD`、`COMBINE` のいずれか |
| `update.check` | 毎日の更新チェックを実行するかどうか（デフォルトは `true`） |
| `update.lastCheck` | 最後に実行した日時（ISO-8601）。ないか読めない場合はチェックが必要とみなす |
| `update.skipVersion` | ユーザーが*このバージョンをスキップ*を選んだリリース。自動では再び提示されない |

ファイルがない、または壊れていても致命的になることはありません。値はそれぞれ個別にフォールバックするので、1 行が壊れていても失われるのはその行だけです。初回起動時、ファイルがまったくない場合は、オプションは GAIA の組み込みデフォルトではなく、`SystemDefaults` によってマシンの地域設定から初期化されます。ロケールの短い日付パターンから、要素の順序、区切り文字、月と年を省略するかどうかが決まります。

`Properties.setProperty` に null を渡してはいけません。例外が投げられ、`persistOptions()` は UI のイベントハンドラーから実行されるため、例外が FX スレッドに漏れ出します。これはこのアプリですでに起きた実際のクラッシュです。

**`captures.tsv`** — 履歴が変わったときと、終了時に書き込まれます。新しいものが先頭で、上限は **500** 件です。

```
captured(ISO)              <TAB> source  <TAB> symbology   <TAB> input
2026-08-09T14:22:05.123    SCANNER       Data Matrix       0109506000134352\x1D10LOT-001
```

保存するのは再計算できないものだけです。`ParseResult` はシリアライズ**されません**。読み込み時に入力を解析し直して導き出すので、復元された履歴は*現在*有効なオプションを反映します。入力には FNC1 やスキャナーが出力するその他のものが日常的に含まれるため、入力フィールドはエスケープされ（`\\`、`\t`、`\n`、`\r`、そして `0x20` 未満のその他すべてには `\xNN`）、ファイルは ISO-8859-1 として 1 バイトずつそのまま読み書きされます。

日付が記録されるようになる前に書かれたファイルには時刻しかありません。それでも読み込むことができ、日付は今日になります。本当の日付は一度も書き留められていないからです。

---

## テスト

```bash
(cd gaia-desktop && mvn test)
```

テストは 96 個で、意図的に **JavaFX ツールキットに依存しない**ようにしてあり、そのおかげで高速かつヘッドレスで動作します。対象は、レポートレンダラー、ビルドロジックと AI の検索、`{GS}` の変換、ロケールから導いたデフォルト、2 つのストア（`0x1D` を含むラウンドトリップと、意図的に壊したファイルを含む）、シンボロジー表、スキャナーのプラットフォームルール、バージョンのスタブ、そしてカタログです。

そのうち 3 つは、機能ではなく契約を守っているので知っておく価値があります。

| テスト | 失敗する条件 |
|---|---|
| `CatalogueCoverageTest` | コードが `ui_EN.properties` に定義されていないキーを要求した、または 35 個のファイルのいずれかが英語のキー集合からずれた |
| `SymbologiesTest` | JavaPOS が宣言する `SCAN_SDT_*` 定数のいずれかに表示名がない（JavaPOS のアップグレードで追加されたものも含む） |
| `ScannerServiceTest` | プラットフォームルールの答えが Windows またはそれ以外について変わった |

`UiStrings` は実行時には意図的に寛容です。未知のキーはレイアウトの途中で例外を投げるのではなく、キーそのものとして表示されます。だからこそカタログのテストが必要なのです。

**カバーされていないもの：** 実際のツールキットを必要とするものすべて。フォーカスの挙動、モードの切り替え、表の編集、言語の切り替え、そして[スレッド](#スレッド)で説明した 2 つのクラッシュです。これらは実際のウィンドウを操作して検証しましたが、回帰が起きてもビルドは失敗しません。TestFX か Monocle を追加すればその穴を塞げます。

---

## ビルドのトラブルシューティング

| 症状 | 原因と対処 |
|---|---|
| `package tools.pantheum.gaia does not exist` | コアがインストールされていません。`(cd gaia && mvn install -DskipTests)` |
| *"JavaFX runtime components are missing"* | jar の `Main-Class` は `GaiaDesktopApp` ではなく `Launcher` でなければなりません |
| 起動時の *"Unsupported JavaFX configuration: classes were loaded from unnamed module"* | 想定どおりで無害です。JavaFX がクラスパス上にあることを知らせているだけです。`Launcher` が存在するのはまさにそのためです |
| jar が起動しない、`NoClassDefFoundError` | デフォルトの jar にはこのモジュールのクラスしか含まれません。`dist/lib/` を隣に置く必要があります。単一の自己完結ファイルが必要なら `-Pfat` を使ってください |
| Eclipse で `Artifact has not been packaged yet … MDEP-187` | m2e のワークスペース解決です。[パッケージング](#パッケージング)の注記を参照してください |
| ビルドに重複した `… 2.properties` ファイルが現れる | プロジェクトが iCloud で同期されるフォルダーにあり、ビルド中に同期が競合コピーを作りました。常に `mvn clean package` を使い、できれば `target/` を同期の対象外にしてください |
| メニュー項目に `act.hri` のようなキーが表示される | そのキーが `ui_EN.properties` にありません。ビルドで検出されるはずでした。[テスト](#テスト)を参照してください |

---

## 既知の制限と今後の作業

- **ヘッドレスの UI テストがありません。** [テスト](#テスト)を参照してください。
- **成果物はプラットフォーム固有です。** どちらのパッケージングも 1 つのプラットフォームの JavaFX ネイティブを同梱します。`-Dfx.platform` でターゲットごとに 1 つずつビルドしてください（どのマシンでもすべてをビルドできます）。どちらも Java ランタイムは同梱しません。それには `jpackage` が必要ですが、これは自分が動いている OS 向けにしかビルドできません。
- **スキャナー入力は Windows 専用です。** [プラットフォームゲート](#プラットフォームゲート)を参照してください。
- **`ScanSource.label()` はローカライズされていません。** [意図的に翻訳しないもの](#意図的に翻訳しないもの)を参照してください。
- **`UiState.mode` は `String` です**（`"PARSE"` / `"BUILD"`）。一方、隣の `Theme` は enum なので、このリテラルが何箇所にも現れます。enum にすればそれらはなくなります。
- **Digital Link のターゲットはドメインとパスのプレフィックスだけです。** `BuilderDigitalLinkConfig` は追加のクエリパラメーターとフラグメントにも対応していますが、エディターはそれらを公開していません。
- **相互に排他的な AI の組（`GE-S006`）は、未知の AI や重複した AI のようにはエディターで示されません**。ビルドがそのまま失敗します。
- **コアのエラーカタログは引用符を二重にします**（`value ''09506000134350''`）。これはビルドの失敗やレポートにそのまま現れます。`MessageFormat` の引用符エスケープが解除されていないように見えます。修正はコア側で行うべきです。
