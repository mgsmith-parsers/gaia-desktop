# GAIA Desktop — 开发者指南

本指南介绍该模块如何构建、如何组织以及如何测试。关于应用程序从外部看能做什么，请参阅[用户指南](GaiaDesktop-User-Chinese.md)。原文：[GaiaDesktop-Developer.md](../../GaiaDesktop-Developer.md)。

## 目录

1. [模块概览](#模块概览)
2. [构建与运行](#构建与运行)
   - [打包](#打包)
3. [架构](#架构)
   - [输入漏斗](#输入漏斗)
   - [线程](#线程)
   - [重建与重新标注](#重建与重新标注)
4. [{GS} 令牌](#gs-令牌)
5. [扫描器集成](#扫描器集成)
   - [平台门控](#平台门控)
   - [码制](#码制)
6. [本地化](#本地化)
   - [`--language` 参数](#--language-参数)
   - [添加语言](#添加语言)
   - [刻意不翻译的内容](#刻意不翻译的内容)
7. [存储状态的格式](#存储状态的格式)
8. [测试](#测试)
9. [构建故障排除](#构建故障排除)
10. [已知限制与待办工作](#已知限制与待办工作)

---

## 模块概览

```
tools.pantheum:gaia-desktop:1.0.0   Java 21 · JavaFX 21
        └── depends on tools.pantheum:gaia:1.1.0
```

它只是核心库的一个客户端，仅此而已。它本身**不包含任何 GS1 知识**：每一个 AI 定义、校验规则、错误消息和本地化描述都来自 [`GaiaParser`](../../../../gaia/docs/GaiaParser.md) 和 [`GaiaBuilder`](../../../../gaia/docs/GaiaBuilder.md)。特别是，AI 列表从不复制到本模块中——构建器的选择器直接读取核心自己的注册表，因此它不可能提供一个构建器会拒绝的 AI。

**入口点：** `tools.pantheum.gaia.desktop.GaiaDesktopApp`（JavaFX 的 `Application`）和 `tools.pantheum.gaia.desktop.Launcher`（打包后 jar 的 `Main-Class`）。

本模块是一个叶子模块：没有任何模块依赖它，它也不依赖任何其他下游模块。

---

## 构建与运行

- 运行需要 **Java 21**，构建需要 JDK 21 或更新版本——模块以 `release=21` 编译。JavaFX 21 是与之对应的 LTS 版本线。核心 jar 仍是 Java 11 字节码，在 21 上无需修改即可运行。
- `PATH` 上要有 **Maven**。本仓库没有 wrapper。
- **先安装核心。** 本模块从 `~/.m2` 解析 `gaia:1.1.0`；核心 jar 过期或缺失，通常就是出现 `package tools.pantheum.gaia does not exist` 的原因。

```bash
(cd gaia && mvn -q install -DskipTests)      # publish the core
(cd gaia-desktop && mvn javafx:run)          # run from source
(cd gaia-desktop && mvn clean package)       # target/dist/gaia-desktop-<version>-<platform>/ — jar (~290 KB), lib/, run.sh (run.bat on win)
(cd gaia-desktop && mvn clean -Pfat package) # one self-contained jar, ~50 MB
(cd gaia-desktop && mvn package -Dfx.platform=win)   # another platform; see Packaging
java -jar gaia-desktop/target/dist/gaia-desktop-1.0.0-mac-aarch64/gaia-desktop.jar
java -jar gaia-desktop/target/dist/gaia-desktop-1.0.0-mac-aarch64/gaia-desktop.jar --language=FR
```

`--language` 是唯一的命令行参数；除了把整个配置目录指向别处的 `-Dgaia.desktop.home=…` 之外，它也是影响启动的唯一方式。

`mvn javafx:run` 是开发路径：插件把 JavaFX 放在**模块路径**上，因此工具包作为命名模块启动，可以直接启动 `GaiaDesktopApp`。打包后的 jar 做不到这一点——所有内容都在类路径上——这就是它的 `Main-Class` 是 `Launcher` 垫片的原因。在那种布局下，把 `Application` 子类作为 `Main-Class` 会失败，并提示 *"JavaFX runtime components are missing"*。

始终使用 `clean`——在构建一组平台之前执行一次即可，因为它会把 `target/dist/` 中所有平台的内容清空。原因见[构建故障排除](#构建故障排除)。

### 打包

所有可交付的内容都汇集在 `target/dist/gaia-desktop-<version>-<platform>/` 下，且只有它们在那里，因此可以把该目录作为一个整体压缩或复制，而不必从 `target/` 中与 `classes/`、`maven-status/` 及其他构建临时文件混在一起的产物里挑拣。`${dist.dir}` 只在一处命名它；每个写出产物的插件都指向它。

#### 每个平台一次构建

JavaFX 的 jar 带有原生代码，因此每个分发包只针对一个平台，由 `fx.platform` 属性以 OpenJFX 分类器的形式命名：`mac-aarch64`、`mac`、`win`、`linux` 或 `linux-aarch64`。五个按操作系统激活的 `fx-*` profile 会把它设为构建机器自身的平台，普通的 `mvn package` 或 `mvn javafx:run` 只需要这些；`-Dfx.platform=…` 优先级高于它们，可以在任何机器上构建任何其他平台——JavaFX 的 jar 只是下载而已。`${dist.dir}` 是 `target/dist/${fx.platform}`，因此一个平台的构建永远不会覆盖另一个平台的构建。

```bash
(cd gaia-desktop && mvn -q clean package)    # this machine's platform, with the tests
for p in win linux mac mac-aarch64; do (cd gaia-desktop && mvn -q package -DskipTests -Dfx.platform=$p); done
```

有两处 pom 细节让这一切成为可能，而且两者看起来都可以删掉（其实不能）：

- **每个 JavaFX 模块都是直接依赖**——base、graphics、controls、media 和 web——每个都带有 `<classifier>${fx.platform}</classifier>` 和一个 `org.openjfx:*` 排除项。OpenJFX 的 pom 根据*构建机器*的操作系统选择传递依赖的 jar，因此如果让 graphics 或 base 以传递方式引入，Mac 的原生库就会和 Windows 的原生库一起进入 Windows 构建。
- **属性是 `fx.platform`，而不是 `javafx.platform`。** 如果从外部设置 OpenJFX 自己的那个属性，它的父 pom 会用它自身重新定义它；Maven 会认为该 pom 无效并悄悄丢弃平台 jar，留下一个完全不含 JavaFX 的构建。

两种布局写出的是同一路径 `target/dist/gaia-desktop-<version>-<platform>/gaia-desktop.jar`，每次构建只有其中一种运行。正是 `${jar.phase}`、`assembly.skipAssembly` 和 `mdep.skip` 防止了二者同时占用它。

| | 默认 | `-Pfat` |
|---|---|---|
| `gaia-desktop.jar` | 约 290 KB——仅本模块的类和资源 | 约 50 MB——依赖解包后放入其中 |
| `dist/gaia-desktop-<version>-<platform>/lib/` | 17 个 jar，在清单的 `Class-Path` 中逐个列出 | — |
| 交付 | jar **以及**旁边的 `lib/` | 一个文件 |
| `dist/gaia-desktop-<version>-<platform>/` 中还有 | `run.bat`（win）或 `run.sh`（其他平台） | 相同 |
| 以及 | `readme.html` 和 `docs/`（所有平台） | 相同 |

每个分发包还带有各份指南：`docs/` 按原有结构复制，使指南与译文之间的链接仍然有效，但不含翻译工具（`check-i18n.py`、`i18n-manifest.json`）；以及位于根目录的 `readme.html`，它是一个链接到每份指南的索引，来自 `src/main/dist/readme.html`，其中的 `@VERSION@` 会被替换为 pom 版本。两者都由 `pick-launch-script` antrun 步骤写出，该步骤会先删除 `docs/`，以免已删除的指南残留下来。readme 中的链接是手工写的——在 `docs/` 下重命名一份指南或添加一份译文时，也需要在那里加上对应的行。

清单的 `Class-Path` 和 `lib/` 是同一件事的两半：jar 为每个运行时依赖列出 `lib/<artifact>-<version>.jar`，而 `copy-dependencies` 正好用这些名字填充 `lib/`。这就是 `java -jar` 能够工作的原因。

#### macOS 应用与分发 zip

一个 antrun 执行 `mac-app-and-zip` 结束 `package` 阶段。正因如此，antrun 插件被声明在 jar 和 assembly 插件**之后**——Maven 按插件声明顺序运行一个阶段的各个执行——这样无论哪种打包方式，它运行时 jar 都已存在。（它的另一个执行 `pick-launch-script` 位于 `prepare-package`，不受影响。）

对于 `mac` 和 `mac-aarch64`，它首先在分发目录中构建 `GAIA Desktop.app`：`Info.plist`（其中 `@VERSION@` 被替换为 pom 版本）、启动器 `Contents/MacOS/gaia-desktop`（设为可执行）和 `gaia-desktop.icns`，全部来自 `src/main/macos/`，然后把 `gaia-desktop.jar` 和 `lib/` **移动**到 `Contents/Java` 中，使应用包能够独立运行。当旁边没有 jar 时，`run.sh` 会到那里查找。启动器按版本查找 Java（先 `java_home -v 21+`，再 Homebrew 的 openjdk——从 Finder 启动时看不到 `JAVA_HOME` 或 `PATH`），找不到时通过 `osascript` 显示对话框，`cd` 到 `~/.gaia-desktop` 使 logback 的相对路径 `logs/` 可写，并传递 `-Xdock:name`/`-Xdock:icon`。macOS 会把正在运行的 `java` 与应用包关联起来（其 bundle id 和程序坞图标都是该应用的）；菜单栏显示 GAIA Desktop。

接着，对每个平台，它用 Ant 的 `<zip>` 把目录压缩为 `target/dist/gaia-desktop-<version>-<platform>.zip`，放在同名文件夹下：`run.sh` 和 `Contents/MacOS/**` 作为单独的文件集并带有 `filemode="755"`，否则解压后它们将无法运行。该 zip **不会作为附件发布**：它用于 GitHub 发布，而四个平台来自四次构建，Maven Central——每个版本只允许一次不可变的上传——永远无法把它们作为一个发布接收。

`project.build.outputTimestamp` 被设为构建时间。否则 maven-jar-plugin 3.5 和 maven-assembly-plugin 3.8 中的归档器会把每个条目的日期都设为 1980 年 2 月 1 日。

应用图标由 `tools/icon/GaiaIcon.java` 绘制（不属于构建的一部分；其文件头写有 `sips`/`iconutil` 步骤）。它的输出已提交到仓库：用于应用包的 `src/main/macos/gaia-desktop.icns`，以及 `src/main/resources/icons/gaia-desktop-{16..256}.png`，`GaiaDesktopApp` 把后者设为窗口图标（Windows 和 Linux；macOS 会忽略窗口图标）。

#### 启动脚本

`run.sh` 和 `run.bat` 来自 `src/main/scripts/`，不经过滤地复制到 `dist/gaia-desktop-<version>-<platform>/` 中——`run.sh` 里满是 `${...}` shell 展开，Maven 过滤可能会随意改写它们。随后一个 antrun 步骤只保留该平台要运行的那一个——`win` 用 `run.bat`，其他平台都用 `run.sh`——并为 `run.sh` 设置可执行位，因为 `copy-resources` 不会保留它。它根据解析后的 `${fx.platform}` 来决定，而不是通过 profile，因为 profile 激活看不到另一个 profile 设置的值：否则在 Windows 上为 Linux 做的构建会拿到宿主机的脚本。

它们**不依赖清单**。每个脚本都自行构建类路径：先 `gaia-desktop.jar`，再逐个列出 `lib/` 中的每个 `.jar`，而不是交给 `lib/*` 通配符，这样包含空格的路径仍是单个条目，而且添加数量的统计是真实的。使用 `-cp` 启动，正是它们显式指定 `tools.pantheum.gaia.desktop.Launcher` 的原因：`-cp` 不读取清单的 `Main-Class`。但它**仍然**会遵循清单的 `Class-Path`——`-cp` 上的 jar 会带上其清单列出的 jar，并紧接其后搜索——因此无论脚本是否列出，每个 `lib/` jar 都会被加载，从脚本的类路径中漏掉某个 jar 并不能把它挡在 JVM 之外。脚本仍然把它们全部列出，这样清单过期、被手工修改或缺少某个条目的 jar 也仍能启动。同一个脚本适用于两种打包方式：`-Pfat` jar 没有可添加的 `lib/`，也不需要，这也是空的 `lib/` 只是一条提示而不是错误的原因。

#### Zebra 原生库

构建不附带任何 Zebra 的东西——没有 DLL，也没有服务 jar。在 Windows 上，它们来自安装在机器上的 Zebra Scanner SDK：`run.bat` 把其 `Jpos*.jar` 服务 jar 和 `bin\` 加入类路径，并把 `bin\` 加入 `PATH`，`System.loadLibrary` 会在那里找到 `CSJPOSScanner.dll` / `CSJPOSScanner64.dll`（`java.library.path` 默认就是 `PATH`）。无论有没有 SDK，JavaPOS API 及其配置加载器始终来自 `lib\javapos-*.jar`。

**`run.bat` 从不添加 SDK 自带的 JavaPOS jar**，即 `External\JavaPOS-1.14.0-Dist\javaPOS114.jar`。它曾经这样做过，并为避免出现两份 API 而去掉了 `lib\javapos-*.jar`——但清单的 `Class-Path` 无论如何都会加载它们（见上文），于是 JVM 两者都拿到了：来自 `lib\` 的 4.x 加载器类，旁边是 `javaPOS114.jar` 的 `jpos/res/jpos.properties` 和 `jcl.xsd`。该属性文件会让 [`JposConfig`](#扫描器集成) 退让，而那个 `jcl.xsd` 只接受 1.10 及以下的 JavaPOS 版本，因此 `jpos.xml` 中的每个条目（1.12–1.14）都未通过校验，注册表保持为空，每次连接都在触及扫描器之前就以错误 109 失败。只要这个 jar 出现在类路径上就足以造成这种情况，即使排在 `lib\` 的 jar 之后也一样。Zebra 的服务 jar 在来自 `lib\` 的 1.14 API 上无需修改即可运行——打开、占用和扫描都已用真实扫描器以这种方式验证过。手工放在 `run.bat` 旁边的 DLL 仍然会被采用：此时脚本会设置 `-Djava.library.path=<dist>;%PATH%`，之所以追加 `PATH`，是因为该属性会彻底替换默认搜索路径。

`mvn javafx:run` 不做这些事，因此在 Windows 上从开发运行中占用扫描器，需要用其他方式把 SDK 的 jar 放到类路径上、把其 `bin\` 放到 `PATH` 上；要这样做，请从 `dist/` 运行。

除了类路径之外，脚本还处理 `java -jar` 不做的事：解析自身所在目录，使其可以从任何工作目录运行；优先使用 `JAVA_HOME` 而非 `PATH`；以及拒绝 11 之前的 JVM，并给出一句说明解决办法的话，而不是抛出 `UnsupportedClassVersionError`。`GAIA_DESKTOP_OPTS` 用于传递 JVM 选项。`run.bat` 使用 `java.exe` 而不是 `javaw.exe`，这样控制台会保持打开——Windows 是唯一能运行扫描器的平台，其状态和 JavaPOS 错误都输出到那里。

`run.bat` 还会根据找到的内容选择 Java、JavaFX 和 Zebra 服务——捆绑的 `jdk*\`、模块路径上的 `javafx-sdk*\lib\`、位于 `ZEBRA_HOME` 的 Zebra Scanner SDK——使用 SDK 的 JavaFX 时，会把 `lib\` 中的 JavaFX 副本排除在类路径之外，并传递 JDK 的 SAX 解析器、Windows 代理和信任库，以及 Java 17+ 上的 `--enable-native-access`。README 的 [What `run.bat` adds on Windows](../../../README.md#what-runbat-adds-on-windows) 中有对应的表格。请保留该文件的 CRLF 换行符：在只用 LF 的批处理文件中，`cmd.exe` 可能会漏掉 `call :label`。`run.sh` 同样传递 `--enable-native-access=ALL-UNNAMED`——JavaFX 从 `lib/` 加载自己的原生库，而当 JavaFX 位于类路径上时，覆盖它的模块就是 `ALL-UNNAMED`——与 `run.bat` 一样，只有在无法读取 Java 版本时才会跳过。

两种布局都只带有**一个平台**（`${fx.platform}`）的 JavaFX 原生库，因此都不能跨操作系统或架构移植。请为每个目标构建一次——参见[每个平台一次构建](#每个平台一次构建)。

> **Eclipse。** `copy-dependencies` 执行带有一条 `<?m2e ignore?>` 指令。m2e 会把打开的工作区项目解析为其 `target/classes` 文件夹而不是 jar，而 `copy-dependencies` 拒绝复制未打包的构件（MDEP-187），这样每当 `gaia` 项目与本项目一起打开时，自动构建就会失败。其后果是，仅在 IDE 中进行的构建不会填充 `target/dist/gaia-desktop-<version>-<platform>/lib/`；在使用 jar 本身之前，请先运行 `mvn package`。

---

## 架构

| 包 | 包含 |
|---|---|
| `desktop` | `GaiaDesktopApp`、`Launcher`、`Theme`、`GaiaDesktopVersion` |
| `desktop.ui` | `MainView`（窗口）、`BuilderPane`、`OptionsPane`、`HistoryCell`、`EditCell` |
| `desktop.build` | `ElementBuilder`（不依赖 JavaFX 的构建逻辑）、`AiPair`（编辑器中的一行） |
| `desktop.parse` | `ParseOptions`（可观察的 `ParseConfig`）、`SystemDefaults`、`Languages`、`GsToken` |
| `desktop.scanner` | `ScannerService`（JavaPOS）、`ScannerListener`、`Symbologies` |
| `desktop.html` | `GaiaDesktopReport`——报告渲染器，不依赖 JavaFX，无需工具包即可进行单元测试 |
| `desktop.i18n` | `UiStrings`——标签目录 |
| `desktop.store` | `AppPaths`、`OptionsStore`、`CaptureStore` |

### 输入漏斗

每一种输入——键入的、粘贴的、从文件打开的、扫描的或构建的——都通过同一个方法 `MainView.capture(String, ScanSource, String)` 到达解析器。

这是整个应用围绕其构建的唯一设计决策。构建不需要自己的报告、自己的历史记录或自己的导出路径，因为经过漏斗之后，构建出的字符串与扫描得到的字符串无法区分。任何新的输入来源都应该经过它，而不是绕过它。

### 线程

| 线程 | 运行内容 |
|---|---|
| JavaFX 应用线程 | 所有解析、渲染和 UI。AI 表预热后解析耗时不到一毫秒，因此不需要工作线程 |
| `gaia-scanner` | 对扫描器的每一次调用——打开、占用、启用、禁用、释放、关闭。一个由 `ScannerService` 拥有的长期线程；参见[扫描器集成](#扫描器集成) |
| JavaPOS 事件线程 | `ScannerService` 回调。`MainView.FxScannerListener` 用 `Platform.runLater` 把每一个回调转交过去 |
| 后台 | 等待扫描器 `connect()` / `disconnect()`（占用最长可能需要 5 秒）、在启动时恢复保存的历史记录——这同时充当解析器预热——以及更新检查（`gaia-update-check`，一次 GitHub 请求，结果通过 `Platform.runLater` 发回） |

**切勿在控件自身的选择事件中修改该控件的选择，或销毁该控件。** 这个应用中有三次不同的崩溃正是由此引起：在弹出框自身的 `clearAndSelect` 中重置 AI 选择器（从 `ListViewBehavior` 抛出 `IndexOutOfBoundsException`），以及在语言下拉框自身的处理程序中重建拥有该下拉框的面板。两者现在都用 `Platform.runLater` 推迟执行，原因记录在两个调用点。

### 重建与重新标注

切换语言时会根据目录**重建**界面外框——菜单、工具栏、选项面板——这是安全的，因为它们都不持有状态：`OptionsPane` 是 `ParseOptions` 的双向视图，值保存在模型中，新面板会直接重新读取它们。

持有**用户数据**的控件是字段，改为就地重新标注：`BuilderPane.retext(UiStrings)` 会保留 AI 行、Digital Link 字段和当前选择。重建它会丢弃正在进行的工作——它曾经就是这样。

---

## {GS} 令牌

JavaFX 的 `TextField` 会悄悄丢弃所有低于 `0x20` 的字符（`TextInputControl.filterInput`）。不是“显示得很奇怪”——而是直接删除。键入、粘贴、`setText` 和 `insertText` 都受影响，因此 `setText("01…" + GS + "10LOT")` 得到的是 `"01…10LOT"`：一个含义已被悄悄改变的元素字符串，因为可变长度的 AI 现在吞掉了它后面的内容。

`desktop.parse.GsToken` 就是完整的答案。分隔符在 UI 中以字面文本 `{GS}` 传递，在送往解析器的途中再变回 `0x1D`。这个令牌并不是这里发明的——它恰好就是核心的 `ASCIIRevealerUtils.printable` 渲染 `0x1D` 的方式，因此从报告中复制出来的值可以直接粘贴回输入栏。

输入字段上的 `TextFormatter` 过滤器会拦截文本可能到达的每一条途径——键入、⌘V、上下文菜单、拖放、`setText`——这就是改写逻辑放在那里而不是放在每个处理程序中的原因。

扫描器路径从不经过 `GsToken`：扫描结果以字节形式到达解析器。

---

## 扫描器集成

`ScannerService` 拥有 JavaPOS 的 `Scanner` 控件：打开 → 占用 → 启用，并在每次扫描后重新启用数据事件。逻辑设备名称来自 `src/main/resources/jpos.xml`。

**每一次设备调用都在同一个线程 `gaia-scanner` 上运行，该线程的生命周期与服务相同**——从不在 FX 线程上，也从不在调用结束后就退出的线程上。Zebra 的服务通过 JNI 经由 CoreScanner COM API 访问扫描器，而 COM 会把它所建立的东西绑定到调用线程的套间：在一个随后退出的线程上打开，扫描器虽能占用却一直沉默；在另一个线程上关闭，则被反初始化的是错误的套间。在 FX 线程上关闭是最糟糕的情况，因为 JavaFX 在那里调用了 `OleInitialize`，并据此注册每个窗口的拖放目标——在它上面执行 `CoUninitialize` 会使整个应用的拖放失效。这是 Windows 上 JavaFX 的问题，而不是某个特定 JavaFX 版本的问题。`connect()` 和 `disconnect()` 会提交到该线程并等待，因此 `MainView` 从一个短期的后台线程调用这两者。

有两条规则防止行为异常的设备卡死连接。**拆除过程从不提前停止：**`closeQuietly` 把禁用、释放和关闭作为独立的步骤执行，只在持有占用时才释放，并且始终尝试 `close()`——占用失败后，`release()` 会抛出“未占用”，而当这些步骤共用一个 `try` 时，关闭就会被跳过，设备在 Zebra 的服务中保持打开状态。**数据事件在 `finally` 中重新启用：**JavaPOS 在每个事件之后都会禁用它，因此即使读取或处理程序抛出异常也必须重新启用，否则扫描器会一直沉默到重新连接为止。`ScannerLifecycleTest` 针对一个脚本化的 `jpos.Scanner` 验证这两条规则。

**`JposConfig` 在第一次打开之前把 JavaPOS 指向捆绑的 `jpos.xml`。** 使用 Maven 的 JavaPOS jar（`javapos-config-loader` 4.x）且类路径上没有 `jpos/res/jpos.properties` 时，每次打开都会以 *"Service does not exist in loaded JCL registry"*（错误 109）失败，原因有三：服务管理器会退回到 `SimpleRegPopulator`，它读取序列化的 `jpos.cfg`，从不读取 `jpos.xml`；XML 加载器只在工作目录中查找 `jpos.xml`，而不在 jar 内部查找；以及 `lib/` 中的 Xerces 2.12.2 拒绝加载器设置的 JAXP `accessExternalDTD` 属性。`JposConfig` 把 `jpos.config.regPopulatorClass`（`JavaxRegPopulator`）、`jpos.config.populatorFileURL`（jar 自己的 `jar:file:…!/jpos.xml`）和 `javax.xml.parsers.SAXParserFactory`（JDK 的）设置为系统属性——从不覆盖已经设置的属性，因此 `-Djpos.config.populatorFileURL=file:…` 仍可选择另一个 `jpos.xml`——而当类路径上有 `jpos.properties` 或没有 4.x 加载器时，它什么也不设置。Zebra SDK 的 `javaPOS114.jar` 就属于这种情况，并且会彻底破坏注册表，这就是 `run.bat` 把它排除在类路径之外的原因——参见 [Zebra 原生库](#zebra-原生库)。`JposConfigTest` 端到端地检查注册表随后确实包含该文件中的扫描器。

**设备选择器是根据该注册表生成的**，而不是在代码中维护一个列表：`ScannerService.logicalNames()` 在 `gaia-scanner` 线程上读取已加载的条目，保留设备类别为 `Scanner` 的条目——`jpos.xml` 中的两个秤条目被排除在外——并按 `pickerOrder` 排序：如果文件中定义了 `ZebraAllScanners`，它排在第一位，其余按字母顺序排列（注册表不保留文件中的顺序）。文件中没有它时也不会补上它；什么都没有加载时只提供默认项，因此选择器永远不会为空。`MainView` 会立即显示默认项，并从后台线程填充列表，同时保留已保存的选择。因此，添加到 `jpos.xml` 中的扫描器——或者用 `-Djpos.config.populatorFileURL` 指定的整个文件——无需修改代码就会出现在选择器中，而 `JposConfigTest` 会把选择器与对捆绑文件的独立读取结果进行对照检查。这些条目所指定的服务是 Zebra 的类（`com.zebra.jpos.service.scanner.*Factory`），它们仍然必须位于类路径上——要么通过 `run.bat` 来自 Scanner SDK，要么是放入 `lib\` 的 Zebra 服务 jar。

扫描数据按 **ISO-8859-1** 解码，使每个字节一一对应，AIM Code ID 前缀和每个 `0x1D` 都得以保留；报告以 UTF-8 写出。

服务中没有任何代码为用户组织文字。事件以 `ScannerListener.Event` 常量加上变化的值发出，由 `MainView` 从目录中提供措辞——否则状态行在一个被翻译成 35 种语言的应用中会是英文。日志行是例外，保持英文。

### 平台门控

`ScannerService.isSupportedPlatform()` 只是一次 `os.name` 判断，`MainView` 在构建任何扫描器控件之前都会询问它：在 macOS 和 Linux 上，没有开关、没有设备选择器、没有扫描器菜单（因此也没有 <kbd>⌘K</kbd>），也没有状态标签。它们不是被隐藏了——而是从未被创建。

Zebra 只为 Windows 提供其 JavaPOS 服务，而在其他平台上不会有任何东西提前失败：jar 能够解析，控件能够创建，直到 `open()` 才最终因缺少原生层而失败，此时已经过去了好几秒，还弹出了一个对话框。

这条规则针对的是*服务*，而不是 JavaPOS 本身。提供跨平台服务的厂商在这里也能工作，而放宽这条规则只需修改 `supportsJavaPos(String)`——它是包私有的，并针对 `os.name` 字符串进行测试，因此无论在哪台机器上运行构建，两种答案都会被覆盖。

### 码制

`Symbologies` 把全部 **78** 种 JavaPOS `SCAN_SDT_*` 扫描数据类型映射为显示名称，并在两套词汇能够对应时映射到核心的 `DataCarrierType`。JavaPOS 是两者中更细的那一个——它把 UPC-A 与 EAN-13 区分开，把普通 Data Matrix 与 GS1 DataMatrix 区分开，而 AIM 则把一整个系列归在一个代码字符之下。

表外的类型仍然会得到一个可用的名称，它通过对 `ScannerConst` 的反射找到，因为厂商服务确实会报告规范列表之外的值。有一个测试会遍历 `ScannerConst`，如果任何已声明的类型没有名称就会失败——这也能捕获未来 JavaPOS 新增的常量。

某些载体映射被刻意保留为 `UNKNOWN`，而不是去猜测：标准 2 of 5（AIM 根据起始/终止条数把 `]R`/`]S` 分开，而 JavaPOS 不报告这一信息）、四态邮政码以及 TLC-39。

---

## 本地化

标签按语言存放在 `src/main/resources/i18n/ui_<CODE>.properties` 下的属性文件中，每种语言一个文件，使用与核心错误目录相同的代码（`EN`、`FR`、`PCM`……）。全部 35 个文件都已存在，并且全部 35 个都具有完全相同的键集。文件按 **UTF-8** 读取，而不是 `Properties` 所假定的 ISO-8859-1，因此译文以可读形式存储，而不是 `\uXXXX` 转义。

**回退是按键进行的，而不是按文件**——正因如此，一种只翻译了部分内容的语言在第一个键加入的那一刻就已经有用。英文是契约：每个键都必须在英文中存在。

| 层 | 键数 | 覆盖范围 |
|---|---|---|
| 共享的报告词汇 | 31 | 全部 35 种语言 |
| 桌面界面外框——菜单、按钮、构建器、选项、状态行、报告布局、扫描器状态、对话框正文 | 114 | 全部 35 种语言 |

其中六个共享术语（`generated`、`status`、`dlRole`、`interpretations`、`message`、`level`）并未被本应用的报告使用。
保留它们是有意为之；文件头中对此有说明。

### `--language` 参数

`GaiaDesktopApp.languageOverride()` 通过 JavaFX 自己的 `getParameters().getNamed()` 读取 `--language=…`，用 `Languages.fromArgument` 解析它——目录代码、枚举名或 BCP 47 标签，不区分大小写——然后把它交给 `MainView`。

**这个覆盖只作用于本次运行，而不作用于文件。** `MainView` 把它加载时的语言保存在 `savedLanguage` 中，并写回*那个*语言，直到 `languageChosenInSession` 表明用户自己选择了一种语言；此后用户的选择照常持久保存。这种分离就是整个设计：信息亭配置或演示快捷方式可以用西班牙语打开，而不会改写机器主人明天打开时的语言，同时在选项面板中做出的真正选择仍然会保留下来。

不对应任何目录的值会连同可接受的代码一起记入日志，然后被忽略。因为快捷方式中的一个拼写错误而拒绝启动才是更糟的失败，出于同样的原因，`Languages.fromArgument` 返回 `null` 而不是抛出异常。

### 添加语言

不需要修改代码。

1. 编辑 `ui_<CODE>.properties`——每种语言都已经有一个文件。
2. 翻译各个值。保留 `{0}` / `{1}` 占位符不动。
3. 构建。有两个测试强制执行这一契约：一个在代码请求了英文未定义的键时失败，另一个在任何目录的键集与英文不一致时失败。

第一个测试扫描的是**源代码**中的键字面量，而不是对类进行反射，因为键只以字符串字面量的形式存在于调用点。这就是为什么新消息必须以内联键的方式构建——`strings.format("st.scannerError", …)`——而不能通过变量查找，否则测试就看不到它。

### 刻意不翻译的内容

- 在原始值旁边以等宽字体显示的简短字段名——元素卡片的 `format`、`position`、`length`、`raw` 和 `part`，纸带的 `AI`、`sep`、`pos` 和 `len`，以及定位错误的四个单元格（`AI`、`position`、`level`、`stage`），它们的值本来就是未翻译的枚举名；
- 示例输入的名称，它们命名的是 GS1 结构，以及码制名称，它们是专有名词；
- `GE-C084` 的补救说明段落——这是标准文本，翻译一半比保留英文更糟；
- `ElementBuilder` 针对未知 AI 的兜底处理，它在 UI 中无法触及：编辑器会自行检查未知 AI，并从目录中报告它们；
- 日志行。

还剩一个缺口：`ScanSource.label()` 以英文向历史记录行和报告标题提供 `manual` / `scanner` / `file` / `built`。要弥补它，仅加一个键是不够的——`HistoryCell` 没有 `UiStrings`，而列表的单元格工厂只安装一次，因此切换语言时还必须刷新单元格。

---

## 存储状态的格式

`~/.gaia-desktop/` 下有两个文件，之所以分开，是因为它们的变化节奏不同——一个损坏或被手工编辑的采集日志不应让用户丢掉设置。可以用 `-Dgaia.desktop.home=…` 覆盖该目录，测试正是借此避开真实的主目录。

**`options.properties`**——在选项改变时以及退出时写入。

| 键 | 含义 |
|---|---|
| `parse.mode`、`parse.language` | 处理流水线深度和消息语言 |
| `date.endian`、`date.separator`、`date.month`、`date.year` | 日期显示 |
| `check.skipRequires`、`check.skipExcludes` | 跳过结构检查 |
| `ui.theme` | `LIGHT` 或 `DARK` |
| `ui.optionsPane` | 选项面板是否打开？ |
| `ui.scannerDevice` | 上次使用的 JavaPOS 逻辑名称 |
| `ui.mode` | `PARSE`、`BUILD` 或 `COMBINE` |
| `update.check` | 是否运行每日更新检查（默认 `true`） |
| `update.lastCheck` | 上次运行的时间，ISO-8601 格式；缺失或无法读取表示需要检查 |
| `update.skipVersion` | 用户选择了*跳过此版本*的发布版本；不会再自动提示 |

文件缺失或损坏从来都不是致命的：每个值都单独回退，因此一行坏数据只损失那一行，别无其他。首次启动时，在完全没有文件的情况下，选项由 `SystemDefaults` 根据机器的区域设置初始化，而不是使用 GAIA 的内置默认值——区域设置的短日期格式决定了组成部分的顺序、分隔符，以及月份和年份是否缩写。

切勿把 null 交给 `Properties.setProperty`：它会抛出异常，而 `persistOptions()` 是从 UI 事件处理程序中运行的，异常会逃逸到 FX 线程上。这是这个应用已经发生过的真实崩溃。

**`captures.tsv`**——在历史记录改变时以及退出时写入。最新的在前，最多 **500** 条。

```
captured(ISO)              <TAB> source  <TAB> symbology   <TAB> input
2026-08-09T14:22:05.123    SCANNER       Data Matrix       0109506000134352\x1D10LOT-001
```

只存储无法重新计算的内容。`ParseResult` **不会**被序列化——加载时通过重新解析输入来重新得出，因此恢复的历史记录反映的是*当前*生效的选项。输入中经常包含 FNC1 以及扫描器发出的其他任何内容，因此输入字段会被转义（`\\`、`\t`、`\n`、`\r`，其他所有低于 `0x20` 的字符用 `\xNN`），文件按 ISO-8859-1 逐字节读写。

在开始记录日期之前写入的文件只保存了时钟时间；它们仍然可以加载，日期记为今天，因为真实日期从未被写下来。

---

## 测试

```bash
(cd gaia-desktop && mvn test)
```

198 个测试，刻意**不依赖 JavaFX 工具包**，这使它们快速且可无头运行。它们覆盖报告渲染器、构建逻辑和 AI 查找、`{GS}` 转换、从区域设置得出的默认值、两个存储（包括含 `0x1D` 的往返测试和刻意损坏的文件）、码制表、扫描器平台规则、版本桩以及各个目录。

其中有三个值得了解，因为它们守护的是契约而不是功能：

| 测试 | 失败条件 |
|---|---|
| `CatalogueCoverageTest` | 代码请求了 `ui_EN.properties` 未定义的键，或 35 个文件中任何一个的键集与英文不一致 |
| `SymbologiesTest` | JavaPOS 声明的任何 `SCAN_SDT_*` 常量没有显示名称——包括 JavaPOS 升级后新增的常量 |
| `ScannerServiceTest` | 平台规则对 Windows 或其他任何系统的回答发生了变化 |

`UiStrings` 在运行时刻意宽容——未知的键会显示为键本身，而不是在布局过程中抛出异常——这正是目录测试必须存在的原因。

**未覆盖：** 任何需要实时工具包的内容——焦点行为、模式切换、表格编辑、语言切换，以及[线程](#线程)一节中描述的两次崩溃。这些都通过操作真实窗口验证过，但它们出现回归时不会导致构建失败。添加 TestFX 或 Monocle 可以弥补这一缺口。

---

## 构建故障排除

| 症状 | 原因与解决办法 |
|---|---|
| `package tools.pantheum.gaia does not exist` | 核心未安装。`(cd gaia && mvn install -DskipTests)` |
| *"JavaFX runtime components are missing"* | jar 的 `Main-Class` 必须是 `Launcher`，而不是 `GaiaDesktopApp` |
| 启动时出现 *"Unsupported JavaFX configuration: classes were loaded from unnamed module"* | 预期之中且无害——JavaFX 只是提示它位于类路径上。这正是 `Launcher` 存在的原因 |
| jar 无法启动，`NoClassDefFoundError` | 默认 jar 只包含本模块的类；`dist/lib/` 必须放在它旁边。需要单个自包含文件时请使用 `-Pfat` |
| Eclipse 中出现 `Artifact has not been packaged yet … MDEP-187` | m2e 工作区解析。参见[打包](#打包)下的说明 |
| 构建中出现重复的 `… 2.properties` 文件 | 项目位于 iCloud 同步的文件夹中，同步在构建过程中生成了冲突副本。始终使用 `mvn clean package`；更好的做法是把 `target/` 排除在同步之外 |
| 菜单项显示 `act.hri` 这样的键 | 该键在 `ui_EN.properties` 中缺失。构建本应发现它——参见[测试](#测试) |

---

## 已知限制与待办工作

- **没有无头 UI 测试。** 参见[测试](#测试)。
- **构件与平台相关。** 两种打包方式都捆绑一个平台的 JavaFX 原生库；请用 `-Dfx.platform` 为每个目标构建一次（任何机器都能构建所有平台）。两者都不捆绑 Java 运行时——那需要 `jpackage`，而它只能为其运行所在的操作系统构建。
- **扫描器输入仅限 Windows。** 参见[平台门控](#平台门控)。
- **`ScanSource.label()` 未本地化。** 参见[刻意不翻译的内容](#刻意不翻译的内容)。
- **`UiState.mode` 是 `String`**（`"PARSE"` / `"BUILD"`），而它旁边的 `Theme` 是枚举，因此该字面量出现在多个地方。改为枚举就能消除它们。
- **Digital Link 目标仅包含域名和路径前缀。** `BuilderDigitalLinkConfig` 还支持额外的查询参数和片段；编辑器没有提供它们。
- **互斥的 AI 对（`GE-S006`）不会像未知 AI 和重复 AI 那样在编辑器中标出**——构建只是直接失败。
- **核心的错误目录会把引号加倍**（`value ''09506000134350''`），这会原样出现在构建失败信息和报告中。看起来像是 `MessageFormat` 的引号转义没有被还原；修复应当在核心中进行。
