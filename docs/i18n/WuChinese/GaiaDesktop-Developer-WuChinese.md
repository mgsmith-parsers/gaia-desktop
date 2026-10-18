# GAIA Desktop — 开发者指南

迭份文档讲迭个模块哪能构建、哪能安排、哪能测试。想晓得程序从外头看做点啥，请看[用户指南](GaiaDesktop-User-WuChinese.md)。原文：[GaiaDesktop-Developer.md](../../GaiaDesktop-Developer.md)。

## 目录

1. [模块一览](#模块一览)
2. [构建搭运行](#构建搭运行)
   - [打包](#打包)
3. [架构](#架构)
   - [输入漏斗](#输入漏斗)
   - [线程](#线程)
   - [重建搭重新贴标签](#重建搭重新贴标签)
4. [{GS} 记号](#gs-记号)
5. [扫描器集成](#扫描器集成)
   - [平台门槛](#平台门槛)
   - [条码制式](#条码制式)
6. [本地化](#本地化)
   - [`--language` 参数](#--language-参数)
   - [加语言](#加语言)
   - [特为勿翻译个物事](#特为勿翻译个物事)
7. [存储状态个格式](#存储状态个格式)
8. [测试](#测试)
9. [构建问题排查](#构建问题排查)
10. [已知限制搭还没做好个事体](#已知限制搭还没做好个事体)

---

## 模块一览

```
tools.pantheum:gaia-desktop:1.0.0   Java 21 · JavaFX 21
        └── depends on tools.pantheum:gaia:1.1.0
```

伊只是核心库个一个客户端，别个啥也没。里向**一眼眼 GS1 知识也没**：所有 AI 定义、校验规则、错误消息搭本地化描述，统统来自 [`GaiaParser`](../../../../gaia/docs/GaiaParser.md) 搭 [`GaiaBuilder`](../../../../gaia/docs/GaiaBuilder.md)。特别是 AI 清单从来勿会拷到迭个模块里来——构建器个选择器直接读核心自家个注册表，所以伊勿可能提供一个构建器会拒绝个 AI。

**入口点：** `tools.pantheum.gaia.desktop.GaiaDesktopApp`（JavaFX 个 `Application`）搭 `tools.pantheum.gaia.desktop.Launcher`（打好包个 jar 个 `Main-Class`）。

迭个模块是一片叶子：没物事依赖伊，伊也勿依赖别个下游模块。伊最早个两只类是从 `gaia-demo` **拷**过来个，勿是当依赖引用个——`Symbologies` 到现在还是靠手工搭那面个双胞胎类保持一致，两只里向侪有注释讲明迭桩事体。

---

## 构建搭运行

- 运行要 **Java 21**，构建要 JDK 21 或者更新——模块用 `release=21` 编译。JavaFX 21 是对应个 LTS 系列。核心 jar 还是 Java 11 字节码，辣 21 上头原封勿动好跑。
- `PATH` 上要有 **Maven**。迭个仓库里没 wrapper。
- **先装核心。** 迭个模块从 `~/.m2` 解析 `gaia:1.1.0`；核心 jar 过期或者寻勿着，一般就是出 `package tools.pantheum.gaia does not exist` 个原因。

```bash
(cd gaia && mvn -q install -DskipTests)      # publish the core
(cd gaia-desktop && mvn javafx:run)          # run from source
(cd gaia-desktop && mvn clean package)       # target/dist/gaia-desktop-<version>-<platform>/ — jar (~290 KB), lib/, run.sh (run.bat on win)
(cd gaia-desktop && mvn clean -Pfat package) # one self-contained jar, ~50 MB
(cd gaia-desktop && mvn package -Dfx.platform=win)   # another platform; see Packaging
java -jar gaia-desktop/target/dist/gaia-desktop-1.0.0-mac-aarch64/gaia-desktop.jar
java -jar gaia-desktop/target/dist/gaia-desktop-1.0.0-mac-aarch64/gaia-desktop.jar --language=FR
```

`--language` 是唯一个命令行参数；除脱会拿整个配置目录指到别处去个 `-Dgaia.desktop.home=…`，伊也是影响启动个唯一办法。

`mvn javafx:run` 是开发用个路子：插件拿 JavaFX 摆辣**模块路径**上，所以工具包作为具名模块启动，`GaiaDesktopApp` 好直接启动。打好包个 jar 做勿到——物事侪辣类路径上——所以伊个 `Main-Class` 是 `Launcher` 迭只垫片。辣迭种布局里拿 `Application` 子类当 `Main-Class`，会报 *"JavaFX runtime components are missing"* 然后失败。

一定要用 `clean`——构建一批平台之前做一趟就够，因为伊会拿 `target/dist/` 里所有平台个物事清清爽爽。原因请看[构建问题排查](#构建问题排查)。

### 打包

要发出去个物事统统集中辣 `target/dist/gaia-desktop-<version>-<platform>/` 下头，而且只有伊拉辣海，迭能整个目录好一道压缩或者拷贝，勿要辣 `target/` 里搭 `classes/`、`maven-status/` 搭别个构建临时文件混辣一道个产物里一只一只挑。`${dist.dir}` 辣一个地方帮伊起名字；每只写产物个插件侪指向伊。

#### 每只平台构建一趟

JavaFX 个 jar 带原生代码，所以每个发布包只对一只平台，由 `fx.platform` 属性用 OpenJFX 分类器来命名：`mac-aarch64`、`mac`、`win`、`linux` 或者 `linux-aarch64`。五只按操作系统激活个 `fx-*` 配置会拿伊设成构建迭部机器自家个平台，普通个 `mvn package` 或者 `mvn javafx:run` 只要迭眼就够；`-Dfx.platform=…` 比伊拉优先，好辣随便啥机器上构建随便啥别个平台——JavaFX 个 jar 只是下载落来个文件。`${dist.dir}` 是 `target/dist/${fx.platform}`，所以一只平台个构建永远勿会覆盖另一只平台个构建。

```bash
(cd gaia-desktop && mvn -q clean package)    # this machine's platform, with the tests
for p in win linux mac mac-aarch64; do (cd gaia-desktop && mvn -q package -DskipTests -Dfx.platform=$p); done
```

pom 里有两只细节让迭桩事体做得成，两只看上去侪好像好删脱：

- **每只 JavaFX 模块侪是直接依赖**——base、graphics、controls、media 搭 web——每只侪带 `<classifier>${fx.platform}</classifier>` 搭一只 `org.openjfx:*` 排除。OpenJFX 个 pom 是按*构建机器*个操作系统来挑传递 jar 个，所以要是让 graphics 或者 base 传递进来，Windows 构建里就会辣 Windows 原生库边上多出 Mac 个。
- **属性是 `fx.platform`，勿是 `javafx.platform`。** 要是从外头设 OpenJFX 自家个属性，伊个父 pom 会用伊自家去重新定义伊；Maven 会当迭只 pom 无效拒绝脱，悄悄叫脱平台 jar，结果剩落来一个一眼 JavaFX 也没个构建。

两种布局写同一条路径 `target/dist/gaia-desktop-<version>-<platform>/gaia-desktop.jar`，每趟构建只有其中一种会跑。勿让两种同时霸牢伊个，就是 `${jar.phase}`、`assembly.skipAssembly` 搭 `mdep.skip`。

| | 默认 | `-Pfat` |
|---|---|---|
| `gaia-desktop.jar` | 约 290 KB — 只有迭个模块个类搭资源 | 约 50 MB — 依赖解开摆辣里向 |
| `dist/gaia-desktop-<version>-<platform>/lib/` | 17 只 jar，辣清单 `Class-Path` 里一只一只列出来 | — |
| 发出去个 | jar **搭**伊边上个 `lib/` | 一只文件 |
| `dist/gaia-desktop-<version>-<platform>/` 里还有 | `run.bat`（win）或者 `run.sh`（别个） | 一样 |
| 还有 | `readme.html` 搭 `docs/`（所有平台） | 一样 |

每个发布包侪会带上各份指南：`docs/` 保留原来布局拷过去，让指南搭翻译之间个链接照样好用，只是勿带翻译工具（`check-i18n.py`、`i18n-manifest.json`、`i18n-plan.md`）；还有根目录个 `readme.html`，是一张链到每份指南个索引，来自 `src/main/dist/readme.html`，里向个 `@VERSION@` 会换成 pom 版本。两样侪是 `pick-launch-script` 迭只 antrun 步骤写出来个，伊会先删脱 `docs/`，免得已经拿脱个指南还留辣海。readme 个链接是手写个——辣 `docs/` 下头改指南名字或者加翻译，迭面也要加上伊个一行。

清单个 `Class-Path` 搭 `lib/` 是同一样物事个两半：jar 会为每只运行期依赖列出 `lib/<artifact>-<version>.jar`，`copy-dependencies` 就正好用迭眼名字填 `lib/`。`java -jar` 跑得起来靠个就是迭个。

#### macOS 应用搭发布 zip

一只 antrun 执行 `mac-app-and-zip` 收尾 `package` 阶段。就是为了迭个，antrun 插件辣 jar 搭 assembly 插件**后头**声明——Maven 是按插件声明顺序跑一个阶段里个执行个——迭能伊跑个辰光，勿管哪种打包，jar 侪已经有了。（伊另外一只执行 `pick-launch-script` 辣 `prepare-package`，勿受影响。）

对 `mac` 搭 `mac-aarch64`，伊先辣发布目录里做 `GAIA Desktop.app`：`Info.plist`（里向 `@VERSION@` 换成 pom 版本）、启动器 `Contents/MacOS/gaia-desktop`（设成可执行）搭 `gaia-desktop.icns`，统统来自 `src/main/macos/`，然后拿 `gaia-desktop.jar` 搭 `lib/` **搬**到 `Contents/Java` 里，让迭只包自家立得牢。边上没 jar 个辰光，`run.sh` 会到迭面去寻。启动器按版本寻 Java（先 `java_home -v 21+`，再 Homebrew 个 openjdk——从 Finder 开个话看勿见 `JAVA_HOME` 或者 `PATH`），寻勿着就用 `osascript` 弹一只对话框，`cd` 到 `~/.gaia-desktop` 让 logback 个相对路径 `logs/` 写得进，再传 `-Xdock:name`/`-Xdock:icon`。macOS 会拿辣跑个 `java` 搭迭只包关联起来（包 ID 搭 Dock 图标侪是程序个）；菜单栏上显示 GAIA Desktop。

再下来，每只平台侪会用 Ant 个 `<zip>` 拿目录压成 `target/dist/gaia-desktop-<version>-<platform>.zip`，摆辣同名文件夹下头：`run.sh` 搭 `Contents/MacOS/**` 带 `filemode="755"` 做一只单独个文件集，勿然解压出来就跑勿起来。迭只 zip **勿附加**：伊是给 GitHub 发布用个，四只平台出自四趟构建，而 Maven Central——每只版本只收一趟改勿脱个上传——永远没办法当伊拉是一个发布。

`project.build.outputTimestamp` 设成构建辰光。勿然 maven-jar-plugin 3.5 搭 maven-assembly-plugin 3.8 个归档工具会拿每只条目个日期写成 1980 年 2 月 1 号。

程序图标是 `tools/icon/GaiaIcon.java` 画个（勿算构建一部分；伊个文件头里有 `sips`/`iconutil` 步骤）。伊个输出已经提交进仓库：包用个 `src/main/macos/gaia-desktop.icns`，搭 `GaiaDesktopApp` 设成窗口图标个 `src/main/resources/icons/gaia-desktop-{16..256}.png`（Windows 搭 Linux；macOS 勿睬窗口图标）。

#### 启动脚本

`run.sh` 搭 `run.bat` 来自 `src/main/scripts/`，勿过滤直接拷到 `dist/gaia-desktop-<version>-<platform>/`——`run.sh` 里一大堆 `${...}` shell 展开，Maven 过滤随时会瞎改。然后一只 antrun 步骤只留迭只平台要跑个——`win` 用 `run.bat`，别个侪用 `run.sh`——再帮 `run.sh` 设可执行位，因为 `copy-resources` 勿会带过来。伊是看解析好个 `${fx.platform}` 来决定个，勿是靠配置，因为配置激活看勿见别个配置设个值：勿然辣 Windows 上做个 Linux 构建就会拿着主机个脚本。

伊拉**勿靠清单**。每只侪自家拼类路径：先 `gaia-desktop.jar`，再一只一只列出 `lib/` 里每只 `.jar`，勿交给 `lib/*` 通配符，迭能带空格个路径还是一只条目，加了几只也数得准。用 `-cp` 启动，就是伊拉要明明白白写出 `tools.pantheum.gaia.desktop.Launcher` 个原因：`-cp` 勿读清单个 `Main-Class`。但是伊**还是**会跟牢伊个 `Class-Path`——`-cp` 上个 jar 会带上伊清单里列个 jar，紧跟辣伊后头搜——所以勿管脚本有没列，每只 `lib/` jar 侪会加载，辣脚本个类路径里漏脱一只也勿会让伊待辣 JVM 外头。脚本照样统统列出来，迭能就算 jar 个清单过期、被人手改过或者少了一项，也照样开得起来。同一只脚本两种打包侪好用：`-Pfat` 个 jar 没 `lib/` 要加，也勿需要，所以空个 `lib/` 只是提示，勿是错误。

#### Zebra 原生库

构建勿带任何 Zebra 个物事——没 DLL，没服务 jar。辣 Windows 上，伊拉来自机器上装好个 Zebra Scanner SDK：`run.bat` 拿伊个 `Jpos*.jar` 服务 jar 搭 `bin\` 加到类路径，再拿 `bin\` 加到 `PATH`，`System.loadLibrary` 就辣迭面寻着 `CSJPOSScanner.dll` / `CSJPOSScanner64.dll`（`java.library.path` 默认就是 `PATH`）。JavaPOS API 搭伊个配置加载器永远来自 `lib\javapos-*.jar`，有没 SDK 侪一样。

**`run.bat` 永远勿加 SDK 自家个 JavaPOS jar**，就是 `External\JavaPOS-1.14.0-Dist\javaPOS114.jar`。老早伊加过，还为了勿让 API 有两份而勿加 `lib\javapos-*.jar`——但是清单 `Class-Path` 反正会加载伊拉（见上头），结果 JVM 两样侪拿着：`lib\` 个 4.x 加载器类，边上就是 `javaPOS114.jar` 个 `jpos/res/jpos.properties` 搭 `jcl.xsd`。迭只属性文件会让 [`JposConfig`](#扫描器集成) 让开，而迭只 `jcl.xsd` 只认 1.10 为止个 JavaPOS 版本，于是 `jpos.xml` 里每只条目（1.12–1.14）侪校验失败，注册表一直是空个，每趟连接还没碰着扫描器就以错误 109 失败。只要迭只 jar 辣类路径上就足够搞成迭能，就算排辣 `lib\` 个 jar 后头也一样。Zebra 个服务 jar 辣 `lib\` 个 1.14 API 上原封勿动好跑——打开、占用搭扫描侪是迭能用真扫描器验证过个。手工摆辣 `run.bat` 边上个 DLL 照样认：迭辰光脚本会设 `-Djava.library.path=<dist>;%PATH%`，辣后头加上 `PATH`，因为迭只属性会整个替换默认搜索路径。

`mvn javafx:run` 迭眼侪勿做，所以辣 Windows 上用开发模式跑去占用扫描器，就要用别个办法拿 SDK 个 jar 摆到类路径上、伊个 `bin\` 摆到 `PATH` 上；要迭能做就从 `dist/` 跑。

除脱类路径，伊拉还管 `java -jar` 勿管个事体：解析自家所在个目录，让伊辣随便啥工作目录侪跑得起来；`JAVA_HOME` 比 `PATH` 优先；碰着 11 之前个 JVM，用一句讲明解决办法个闲话拒绝，勿是抛 `UnsupportedClassVersionError`。`GAIA_DESKTOP_OPTS` 会拿 JVM 选项传落去。`run.bat` 用 `java.exe` 勿用 `javaw.exe`，让控制台一直开牢——Windows 是唯一跑得起扫描器个平台，伊个状态搭 JavaPOS 错误侪出辣迭面。

`run.bat` 还会按寻着个物事挑 Java、JavaFX 搭 Zebra 服务——自带个 `jdk*\`、模块路径上个 `javafx-sdk*\lib\`、`ZEBRA_HOME` 个 Zebra Scanner SDK——用 SDK 个 JavaFX 个辰光就勿拿 `lib\` 个 JavaFX 副本摆进类路径，并且传 JDK 个 SAX 解析器、Windows 代理搭信任存储，还有辣 Java 17+ 上传 `--enable-native-access`。README 个 [What `run.bat` adds on Windows](../../../README.md#what-runbat-adds-on-windows) 里有张表。记牢保留迭只文件个 CRLF 换行：辣只有 LF 个批处理文件里，`cmd.exe` 可能会漏脱 `call :label`。`run.sh` 也会传 `--enable-native-access=ALL-UNNAMED`——JavaFX 从 `lib/` 加载自家个原生库，JavaFX 辣类路径上个辰光，罩牢伊个模块就是 `ALL-UNNAMED`——搭 `run.bat` 一样，只有读勿出 Java 版本个辰光才跳过。

两种布局侪只带**一只平台**（`${fx.platform}`）个 JavaFX 原生库，所以两种侪勿好跨操作系统或者架构搬来搬去。每只目标构建一趟——请看[每只平台构建一趟](#每只平台构建一趟)。

> **Eclipse。** `copy-dependencies` 执行带了一只 `<?m2e ignore?>` 指示。m2e 会拿开牢个工作区项目解析成伊个 `target/classes` 文件夹，勿是 jar，而 `copy-dependencies` 拒绝拷还没打包个产物（MDEP-187），迭能只要 `gaia` 项目搭迭个一道开牢，自动构建就会失败。结果是只辣 IDE 里构建勿会填 `target/dist/gaia-desktop-<version>-<platform>/lib/`；用 jar 本身之前要先跑 `mvn package`。

---

## 架构

| 包 | 里向有 |
|---|---|
| `desktop` | `GaiaDesktopApp`、`Launcher`、`Theme`、`GaiaDesktopVersion` |
| `desktop.ui` | `MainView`（窗口）、`BuilderPane`、`OptionsPane`、`HistoryCell`、`EditCell` |
| `desktop.build` | `ElementBuilder`（勿靠 JavaFX 个构建逻辑）、`AiPair`（编辑器里一行） |
| `desktop.parse` | `ParseOptions`（好观察个 `ParseConfig`）、`SystemDefaults`、`Languages`、`GsToken` |
| `desktop.scanner` | `ScannerService`（JavaPOS）、`ScannerListener`、`Symbologies` |
| `desktop.html` | `GaiaDesktopReport` — 报告渲染器，勿靠 JavaFX，没工具包也好做单元测试 |
| `desktop.i18n` | `UiStrings` — 标签目录 |
| `desktop.store` | `AppPaths`、`OptionsStore`、`CaptureStore` |

### 输入漏斗

所有输入——打字、粘贴、从文件打开、扫描或者构建——侪经过同一只方法 `MainView.capture(String, ScanSource, String)` 到解析器。

整个程序就是围牢迭一只设计决定做起来个。构建勿需要自家个报告、自家个历史或者自家个导出路子，因为过了漏斗以后，构建出来个字符串搭扫描来个字符串根本分勿清。随便啥会产生输入个新物事侪应该走迭只漏斗，勿要辣伊边上兜过去。

### 线程

| 线程 | 跑点啥 |
|---|---|
| JavaFX 应用线程 | 所有解析、渲染搭 UI。AI 表热身好以后解析用勿着一毫秒，所以勿要工作线程 |
| `gaia-scanner` | 对扫描器个每一趟调用——打开、占用、启用、停用、释放、关闭。由 `ScannerService` 拥有个一根长命线程；请看[扫描器集成](#扫描器集成) |
| JavaPOS 事件线程 | `ScannerService` 回调。`MainView.FxScannerListener` 用 `Platform.runLater` 拿每只侪转过去 |
| 后台 | 等扫描器 `connect()` / `disconnect()`（占用可能要 5 秒）、启动个辰光恢复存好个历史——顺带帮解析器热身——还有检查更新（`gaia-update-check`，一趟 GitHub 请求，结果用 `Platform.runLater` 送回来） |

**千万勿要辣一只控件自家个选择事件里改伊个选择，或者拿伊销毁脱。** 迭个程序有三趟勿一样个崩溃正好是迭能来个：辣弹出窗口自家个 `clearAndSelect` 里重置 AI 选择器（从 `ListViewBehavior` 抛出一只 `IndexOutOfBoundsException`），还有辣语言下拉框自家个处理函数里重建拥有伊个面板。两只现在侪用 `Platform.runLater` 延后，原因辣两只调用位置侪记牢辣海。

### 重建搭重新贴标签

换语言会从目录**重建**外框——菜单、工具栏、选项面板——迭能做是安全个，因为伊拉一只也勿带状态：`OptionsPane` 是 `ParseOptions` 个双向视图，值住辣模型里，新面板马上就拿回来。

带**用户数据**个控件是字段，改成原地重新贴标签：`BuilderPane.retext(UiStrings)` 会保留 AI 行、Digital Link 字段搭选择。重建伊会拿做到一半个事体甩脱——老早真个出过迭种事体。

---

## {GS} 记号

JavaFX 个 `TextField` 会悄悄叫甩脱所有低于 `0x20` 个字符（`TextInputControl.filterInput`）。勿是"显示得怪怪个"——是直接删脱。打字、粘贴、`setText` 搭 `insertText` 侪受影响，所以 `setText("01…" + GS + "10LOT")` 会得到 `"01…10LOT"`：一只意思悄悄叫变脱个元素字符串，因为可变长度个 AI 现在连后头个物事一道吞脱了。

`desktop.parse.GsToken` 就是全部答案。分隔符辣 UI 里用字面文字 `{GS}` 传来传去，到解析器去个路上再变回 `0x1D`。迭只记号勿是辣此地发明个——伊正好就是核心个 `ASCIIRevealerUtils.printable` 显示 `0x1D` 个样子，所以从报告里拷出来个值好直接粘回输入栏。

输入框上个 `TextFormatter` 过滤器会拦牢文字好进来个每一条路——打字、⌘V、右键菜单、拖放、`setText`——所以改写摆辣迭面，勿是每只处理函数侪做一趟。

扫描器个路子永远勿经过 `GsToken`：扫描是以字节个形式到解析器个。

---

## 扫描器集成

`ScannerService` 拥有 JavaPOS 个 `Scanner` 控件：打开 → 占用 → 启用，每趟扫描以后重新启动数据事件。逻辑设备名字来自 `src/main/resources/jpos.xml`。

**每趟设备调用侪辣同一根线程 `gaia-scanner` 上跑，伊搭服务一样长命**——永远勿辣 FX 线程上，也永远勿辣调用好就结束个线程上。Zebra 个服务通过 CoreScanner COM API 用 JNI 碰着扫描器，COM 会拿伊设好个物事绑到调用线程个 apartment 上：辣一根后来会结束个线程上打开，扫描器占得牢但是一直没反应；辣另一根线程上关闭，就会解除错个 apartment 个初始化。辣 FX 线程上关闭是最坏个情况，因为 JavaFX 辣迭面调用 `OleInitialize`，还拿每只窗口个放置目标登记上去——辣迭面一趟 `CoUninitialize` 就会让整个程序个拖放侪坏脱。迭个是 Windows 上 JavaFX 个问题，勿是某一个 JavaFX 版本个问题。`connect()` 搭 `disconnect()` 会拿工作交给迭根线程再等，所以 `MainView` 辣一根短命个后台线程里调用两只；迭个模式来自查看器个 `JposScannerSession`，拖放故障最早就是辣迭面发现个。

有两条规矩防止一部出毛病个设备拿连接卡死。**拆除永远勿会提早停：** `closeQuietly` 拿停用、释放搭关闭分开来做，只有占牢个辰光才释放，而且一定会试 `close()`——占用失败以后 `release()` 会抛"not claimed"，老早几只步骤合用一只 `try` 个辰光，就会因此跳过关闭，让设备辣 Zebra 服务里一直开牢。**数据事件辣 `finally` 里重新启动：** JavaPOS 每趟事件以后侪会停用伊，所以就算读取或者处理函数抛了异常也一定要重新启动，勿然扫描器会一直没反应，一直到重新连接为止。`ScannerLifecycleTest` 用一只预先编好剧本个 `jpos.Scanner` 测试两条规矩。

**`JposConfig` 辣第一趟打开之前拿 JavaPOS 指到自带个 `jpos.xml`。** 用 Maven 个 JavaPOS jar（`javapos-config-loader` 4.x）而类路径上没 `jpos/res/jpos.properties` 个话，勿迭能做每趟打开侪会以 *"Service does not exist in loaded JCL registry"*（错误 109）失败，原因有三只：服务管理器会退回去用 `SimpleRegPopulator`，伊读序列化个 `jpos.cfg`，从来勿读 `jpos.xml`；XML 加载器只辣工作目录里寻 `jpos.xml`，勿到 jar 里向去寻；还有 `lib/` 里个 Xerces 2.12.2 拒绝加载器设个 JAXP `accessExternalDTD` 属性。`JposConfig` 拿 `jpos.config.regPopulatorClass`（`JavaxRegPopulator`）、`jpos.config.populatorFileURL`（jar 自家个 `jar:file:…!/jpos.xml`）搭 `javax.xml.parsers.SAXParserFactory`（JDK 个）设成系统属性——已经设好个就勿碰，所以 `-Djpos.config.populatorFileURL=file:…` 照样好挑另一只 `jpos.xml`——而类路径上有 `jpos.properties`，或者没 4.x 加载器个辰光，伊啥也勿设。Zebra SDK 个 `javaPOS114.jar` 就是迭种情况，还会拿注册表整个搞坏，所以 `run.bat` 拿伊摆辣类路径外头——请看 [Zebra 原生库](#zebra-原生库)。`JposConfigTest` 从头到尾检查注册表后来确实有文件里个扫描器。

**设备选择器是从迭只注册表生成个**，勿是辣代码里摆一张清单：`ScannerService.logicalNames()` 辣 `gaia-scanner` 线程上读已加载个条目，留牢设备类别是 `Scanner` 个——`jpos.xml` 里两只秤个条目勿进来——按 `pickerOrder` 排：文件里有定义个话 `ZebraAllScanners` 排第一，其余按字母顺序（注册表勿保留文件里个顺序）。没定义伊个文件勿会帮伊补上；啥也没加载个话就只提供默认，所以选择器永远勿会空。`MainView` 马上显示默认，再辣后台线程里填清单，同时保留存好个选择。所以加到 `jpos.xml` 里个扫描器——或者用 `-Djpos.config.populatorFileURL` 指定个整只文件——勿改代码就会辣选择器里出现，`JposConfigTest` 会拿选择器搭独立读取自带文件个结果对一对。迭眼条目点名个服务是 Zebra 个类（`com.zebra.jpos.service.scanner.*Factory`），伊拉还是要辣类路径上——通过 `run.bat` 从 Scanner SDK 来，或者是摆进 `lib\` 个 Zebra 服务 jar。

扫描数据用 **ISO-8859-1** 解码，迭能每只字节侪一对一，AIM Code ID 前缀搭每只 `0x1D` 侪保得牢；报告用 UTF-8 写。

服务里没任何物事会帮用户组织文字。事件以 `ScannerListener.Event` 常量加上会变个值送出去，由 `MainView` 从目录里拿字眼——勿然辣一个翻译成 35 种语言个程序里，状态栏就会是英文。日志行是例外，保持英文。

### 平台门槛

`ScannerService.isSupportedPlatform()` 是一只 `os.name` 测试，`MainView` 做任何扫描器控件之前侪会问伊：辣 macOS 搭 Linux 上，没开关、没设备选择器、没扫描器菜单（所以也没 <kbd>⌘K</kbd>），也没状态标签。伊拉勿是囥起来——是根本没做过。

Zebra 个 JavaPOS 服务只出 Windows 版，辣别个平台上没物事会早点失败：jar 解析得出、控件做得出，最后到 `open()` 才因为没原生层失败，迭辰光已经过脱好几秒还弹了一只对话框。

迭条规矩讲个是*服务*，勿是 JavaPOS。要是有供应商出跨平台个服务，辣此地一样好用，放宽规矩只要改 `supportsJavaPos(String)`——伊是 package-private，而且用 `os.name` 字符串测试过，所以勿管辣哪部机器上跑构建，两只答案侪覆盖得着。

### 条码制式

`Symbologies` 拿 JavaPOS 全部 **78** 种 `SCAN_SDT_*` 扫描数据类型对应到显示名字，两套词汇对得上个地方再对应到核心个 `DataCarrierType`。两只里向 JavaPOS 分得细——伊拿 UPC-A 搭 EAN-13 分开、普通 Data Matrix 搭 GS1 DataMatrix 分开，而 AIM 拿整个家族归到一只代码字符下头。

表外头个类型也会有一只好用个名字，是通过对 `ScannerConst` 做反射寻来个，因为供应商服务真个会报规格清单外头个值。有只测试会兜一圈 `ScannerConst`，有任何已声明个类型没名字就失败——将来 JavaPOS 新加个常量也捉得着。

有眼载体对应特为留成 `UNKNOWN`，勿是靠猜：straight 2 of 5（AIM 按起止条个数目分 `]R`/`]S`，但是 JavaPOS 勿报迭个）、四态邮政码，还有 TLC-39。

---

## 本地化

标签每种语言一只属性文件，摆辣 `src/main/resources/i18n/ui_<CODE>.properties` 下头，用搭核心错误目录一样个代码（`EN`、`FR`、`PCM`……）。35 只统统有，而且 35 只个键集一模一样。文件用 **UTF-8** 读，勿是 `Properties` 假定个 ISO-8859-1，所以翻译是以看得懂个样子存个，勿是 `\uXXXX` 转义。

**后备是一只键一只键来个，勿是一只文件一只文件**——就是为了迭个，一种只翻了一部分个语言，从第一只键进来个辰光起就已经有用了。英文是合约：每只键侪一定要辣英文里有。

| 层 | 键数 | 覆盖 |
|---|---|---|
| 共用报告词汇，拿自 `gaia-demo` 个 `GaiaHtmlReport` | 31 | 全部 35 种语言 |
| 桌面外框 — 菜单、按钮、构建器、选项、状态栏、报告版面、扫描器状态、对话框正文 | 114 | 全部 35 种语言 |

共用词语里有六只（`generated`、`status`、`dlRole`、`interpretations`、`message`、`level`）迭个程序个报告用勿着。留牢伊拉是为了让共用区块忠实拷贝演示程序个词汇；文件头里有讲明。

### `--language` 参数

`GaiaDesktopApp.languageOverride()` 通过 JavaFX 自家个 `getParameters().getNamed()` 读 `--language=…`，用 `Languages.fromArgument` 解析——目录代码、enum 名或者 BCP 47 标签侪好，勿分大小写——再交给 `MainView`。

**迭个覆盖是对迭趟运行个，勿是对文件个。** `MainView` 拿伊加载个语言摆辣 `savedLanguage` 里，写回去个也是*迭只*，一直到 `languageChosenInSession` 讲用户自家挑了语言为止；之后就照常保存伊拉个选择。整个设计就是迭个分隔：信息亭配置或者演示快捷方式好用西班牙文开，勿会改写机器主人明朝开个语言，但是辣选项面板里做个真选择照样会保留。

一只对勿上任何目录个值，会连同接受个代码记到日志里然后勿睬。因为快捷方式打错一只字就拒绝启动，才是更坏个失败；`Languages.fromArgument` 返回 `null` 而勿抛异常，也是同一个原因。

### 加语言

勿要改代码。

1. 编辑 `ui_<CODE>.properties`——每种语言侪已经有文件了。
2. 翻译值。`{0}` / `{1}` 占位符原样留牢。
3. 构建。有两只测试执行迭只合约：一只是代码要一只英文没定义个键就失败，另一只是任何目录个键集搭英文对勿上就失败。

第一只测试是辣**源代码**里扫键个字面值，勿是对类做反射，因为键只以字符串字面值个形式辣调用位置出现。所以新消息一定要直接写上键去构建——`strings.format("st.scannerError", …)`——勿好通过变量去查，勿然测试看勿见。

### 特为勿翻译个物事

- 辣原始值边上用等宽字体显示个简短字段名——元素卡个 `format`、`position`、`length`、`raw` 搭 `part`，纸带个 `AI`、`sep`、`pos` 搭 `len`，还有标出错误位置个四格（`AI`、`position`、`level`、`stage`），伊拉个值本来就是没翻译个 enum 名；
- 示例输入个名字（伊拉是 GS1 结构个名字），搭条码制式名字（伊拉是专有名词）；
- `GE-C084` 个补救段落——标准文本，翻一半还勿如英文；
- `ElementBuilder` 对未知 AI 个最后一道防线，UI 根本走勿到：编辑器会自家检查未知 AI，再用目录报出来；
- 日志行。

还有一只缺口：`ScanSource.label()` 用英文给历史行搭报告标头提供 `manual` / `scanner` / `file` / `built`。要补上勿只是加一只键迭能简单——`HistoryCell` 没 `UiStrings`，而清单个单元格工厂只装一趟，所以换语言个辰光连单元格也要刷新。

---

## 存储状态个格式

`~/.gaia-desktop/` 下头有两只文件，分开摆是因为伊拉变个节奏勿一样——一只坏脱或者被人手改过个采集日志，勿应该连累用户设置也没脱。用 `-Dgaia.desktop.home=…` 覆盖目录，测试就是迭能避开真正个主目录。

**`options.properties`** — 选项一改搭退出个辰光写。

| 键 | 意思 |
|---|---|
| `parse.mode`、`parse.language` | 流水线深度搭消息语言 |
| `date.endian`、`date.separator`、`date.month`、`date.year` | 日期显示 |
| `check.skipRequires`、`check.skipExcludes` | 跳过结构检查 |
| `ui.theme` | `LIGHT` 或者 `DARK` |
| `ui.optionsPane` | 选项面板开牢伐？ |
| `ui.scannerDevice` | 上趟用个 JavaPOS 逻辑名字 |
| `ui.mode` | `PARSE`、`BUILD` 或者 `COMBINE` |
| `update.check` | 每日更新检查跑勿跑（默认 `true`） |
| `update.lastCheck` | 上趟啥辰光跑个，ISO-8601；没或者读勿出就当是到辰光检查了 |
| `update.skipVersion` | 用户挑了*跳脱迭个版本*个发布；勿会再自动提供 |

文件寻勿着或者坏脱侪勿会要命：每只值各自后备，所以一行坏脱只损失迭一行，别个物事没事体。第一趟启动、一只文件也没个辰光，选项由 `SystemDefaults` 按机器个地区设置初始化，勿是用 GAIA 内置个默认——地区设置个短日期格式决定组成部分个顺序、分隔符，还有月搭年是勿是缩写。

千万勿要拿 null 交给 `Properties.setProperty`：伊会抛异常，而 `persistOptions()` 是从 UI 事件处理函数里跑个，异常会漏到 FX 线程上去。迭个程序真个迭能崩过。

**`captures.tsv`** — 历史一改搭退出个辰光写。最新个排头里，最多 **500** 条。

```
captured(ISO)              <TAB> source  <TAB> symbology   <TAB> input
2026-08-09T14:22:05.123    SCANNER       Data Matrix       0109506000134352\x1D10LOT-001
```

只存算勿回来个物事。`ParseResult` **勿**序列化——加载个辰光重新解析输入再推出来，所以恢复个历史反映个是*现在*生效个选项。输入里经常有 FNC1 搭扫描器射出来个随便啥物事，所以输入字段会做转义（`\\`、`\t`、`\n`、`\r`，别个所有低于 `0x20` 个就用 `\xNN`），文件用 ISO-8859-1 一只字节一只字节读写。

开始记日期之前写个文件只有钟点辰光；伊拉照样加载得进，日期当是今朝，因为真正个日期从来没写落来过。

---

## 测试

```bash
(cd gaia-desktop && mvn test)
```

96 只测试，特为**勿靠 JavaFX 工具包**，所以又快又好没画面跑。伊拉覆盖报告渲染器、构建逻辑搭 AI 查询、`{GS}` 转换、从地区设置推出来个默认、两只存储（包括一只带 `0x1D` 个来回测试搭特为搞坏个文件）、条码制式表、扫描器平台规矩、版本桩，还有各只目录。

其中三只值得晓得，因为伊拉守个是合约勿是功能：

| 测试 | 啥辰光失败 |
|---|---|
| `CatalogueCoverageTest` | 代码要一只 `ui_EN.properties` 没定义个键，或者 35 只文件里随便哪只个键集搭英文对勿上 |
| `SymbologiesTest` | JavaPOS 声明个任何 `SCAN_SDT_*` 常量没显示名字——包括 JavaPOS 升级新加个 |
| `ScannerServiceTest` | 平台规矩对 Windows 或者别个随便啥系统个答案变脱了 |

`UiStrings` 辣运行期特为宽松——未知个键会直接显示键本身，勿会辣排版半当中抛异常——就是为了迭个，才一定要有目录测试。

**没覆盖个：** 任何要真工具包个物事——焦点行为、模式切换、表格编辑、换语言，还有[线程](#线程)里讲过个两趟崩溃。迭眼是开真窗口操作验证过个，但是伊拉要是退化，构建勿会失败。加上 TestFX 或者 Monocle 就好补上迭只缺口。

---

## 构建问题排查

| 症状 | 原因搭解决办法 |
|---|---|
| `package tools.pantheum.gaia does not exist` | 核心没装。`(cd gaia && mvn install -DskipTests)` |
| *"JavaFX runtime components are missing"* | jar 个 `Main-Class` 一定要是 `Launcher`，勿是 `GaiaDesktopApp` |
| 启动个辰光出 *"Unsupported JavaFX configuration: classes were loaded from unnamed module"* | 预料之中、没害处——JavaFX 只是讲伊辣类路径上。`Launcher` 存在就是为了迭个 |
| jar 开勿起来，`NoClassDefFoundError` | 默认 jar 只带迭个模块个类；`dist/lib/` 一定要辣伊边上。要单只自足文件就用 `-Pfat` |
| Eclipse 里出 `Artifact has not been packaged yet … MDEP-187` | m2e 工作区解析。请看[打包](#打包)下头个注 |
| 构建里出现重复个 `… 2.properties` 文件 | 项目辣 iCloud 同步个文件夹里，同步辣构建半当中做出冲突副本。一定要用 `mvn clean package`；最好拿 `target/` 排除辣同步外头 |
| 菜单项显示 `act.hri` 迭种键 | 迭只键 `ui_EN.properties` 里没。构建本来应该捉得着——请看[测试](#测试) |

---

## 已知限制搭还没做好个事体

- **没无画面 UI 测试。** 请看[测试](#测试)。
- **产物分平台。** 两种打包侪只带一只平台个 JavaFX 原生库；用 `-Dfx.platform` 每只目标做一只（随便哪部机器侪做得全）。两种侪勿带 Java 运行环境——要迭能就要 `jpackage`，而伊只好为自家辣跑个操作系统构建。
- **扫描器输入只有 Windows。** 请看[平台门槛](#平台门槛)。
- **`ScanSource.label()` 还没本地化。** 请看[特为勿翻译个物事](#特为勿翻译个物事)。
- **`UiState.mode` 是一只 `String`**（`"PARSE"` / `"BUILD"`），但是伊边上个 `Theme` 是 enum，所以迭只字面值辣交关地方出现。改成 enum 就好清脱伊拉。
- **Digital Link 目标只有域名搭路径前缀。** `BuilderDigitalLinkConfig` 还支持额外查询参数搭片段；编辑器没放出来。
- **互斥个 AI 对（`GE-S006`）勿会像未知搭重复 AI 一样辣编辑器里标出来**——构建会直接失败。
- **核心错误目录会拿引号加倍**（`value ''09506000134350''`），辣构建失败搭报告里原样出现。看上去是 `MessageFormat` 个引号转义没还原；应该辣核心里修。
