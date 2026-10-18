# Zebra 扫描器设置 — Windows 版 GAIA Desktop

哪能安装搭设置 Zebra 条码扫描器，让 GAIA Desktop 好直接从伊搭读 GS1 条码：每趟扫描侪会解析、加进记录，
AIM Code ID 前缀搭每只 FNC1 分隔符侪原封勿动。原文：
[GaiaDesktop-Zebra-Scanner-Windows.md](../../GaiaDesktop-Zebra-Scanner-Windows.md)。

GAIA Desktop 是用一只 USB 连牢个 **Zebra DS8108** 手持影像扫描器做出来、测试过个。搿眼步骤适用于
Zebra Scanner SDK 支持个任何 Zebra 2D 扫描器；某只型号个设置条码搭菜单名字，辣伊自家个 Quick Reference
Guide 或者 Product Reference Guide 里向寻得着。

> **只有 Windows。** Zebra 个扫描器驱动只有 Windows 版，所以辣 macOS 或者 Linux 浪，GAIA Desktop
> 勿会做任何扫描器控件。程序其他物事辣所有平台浪侪好用。

---

## 目录

1. [侬要准备点啥](#1-侬要准备点啥)
2. [安装 Java](#2-安装-java)
3. [安装 Zebra Scanner SDK](#3-安装-zebra-scanner-sdk)
4. [安装 123Scan](#4-安装-123scan)
5. [更新 Zebra 扫描器固件](#5-更新-zebra-扫描器固件)
6. [设置 Zebra 扫描器个 USB 模式](#6-设置-zebra-扫描器个-usb-模式)
7. [为 GS1 设置 Zebra 扫描器](#7-为-gs1-设置-zebra-扫描器)
8. [安装搭启动 GAIA Desktop](#8-安装搭启动-gaia-desktop)
9. [连接扫描器](#9-连接扫描器)
10. [设置参考](#10-设置参考)
11. [故障排除](#11-故障排除)
12. [支持个 Zebra 2D 扫描器](#12-支持个-zebra-2d-扫描器)

---

## 1. 侬要准备点啥

| | |
|---|---|
| **扫描器** | 一只 Zebra 2D 扫描器，连伊个 USB 线 — 看 [支持个 Zebra 2D 扫描器](#12-支持个-zebra-2d-扫描器) |
| **电脑** | Windows 10 或 11，64 位 |
| **Java** | Java 21 或者更新，**64 位** |
| **Zebra Scanner SDK for Windows** | **64 位**，从 Zebra 支持网站下载 — 伊提供驱动搭 GAIA Desktop 用个 JavaPOS 服务 |
| **Zebra 123Scan** | Zebra 个免费设置工具，从 Zebra 支持网站下载 — 用来更新扫描器固件、检查伊个设置 |
| **GAIA Desktop** | `gaia-desktop-1.0.0-win.zip` |
| **可选** | 扫描器个 **Product Reference Guide**（PRG），里向有设置条码 |

各部分哪能接起来：

```
Zebra scanner ──USB──► Zebra CoreScanner driver ──► Zebra JavaPOS services ──► GAIA Desktop
                       (installed by the SDK)        (in the SDK's JPOS\bin)    (run.bat puts them on
                                                                                 the class path)
```

GAIA Desktop 自家带了 JavaPOS API 搭拨扫描器起名字个配置（`jpos.xml`，辣 `gaia-desktop.jar` 里向）。
其余个由 Zebra SDK 提供：搭扫描器讲闲话个驱动，还有夹辣 JavaPOS 搭搿只驱动当中个 Zebra 服务类。

---

## 2. 安装 Java

GAIA Desktop 要 **Java 21 或者更新**，而且一定要是 **64 位** 版本，好搭 64 位个 Zebra 驱动对牢。

1. 安装 Windows x64 用个 Java 21 JDK 或者 JRE — 比方讲 adoptium.net 个
   Eclipse Temurin。安装程序要是讲好帮侬设 `JAVA_HOME`，就让伊设。
2. 辣命令提示符里检查：

   ```
   java -version
   ```

   第一行应该显示 21 或者以后个版本；64 位个 Java 还会打出 `64-Bit Server VM`。

`run.bat` 按搿个顺序寻 Java：`run.bat` 边浪个 `jdk*` 文件夹，然后 `JAVA_HOME`，再然后 `PATH` 浪个
`java`。比 21 老个，伊会拒绝，并且出消息讲清爽。

---

## 3. 安装 Zebra Scanner SDK

1. 从 Zebra 支持网站下载 **Zebra Scanner SDK for Windows (64-bit)**。
2. 运行安装程序。要是伊让侬拣组件，确保 **JPOS**（JavaPOS）组件拣进去了。
3. 用默认安装位置。GAIA Desktop 辣搿搭寻 SDK 个 JavaPOS 部分：

   ```
   C:\Program Files\Zebra Technologies\Barcode Scanners\Scanner SDK\JPOS\
   ```

   并且指望 Zebra 个服务 jar（`JposServiceScanner.jar`、`JposServiceJniScanner.jar`、…）搭原生库
   （`CSJPOSScanner64.dll`、…）辣伊个 `bin\` 文件夹里。SDK 要是装辣别个地方，看 [ZEBRA_HOME](#zebra_home)。
4. 装好以后 **重启 Windows**。SDK 个 CoreScanner 驱动辣后台运行，GAIA Desktop 碰着扫描器之前伊一定要
   已经辣跑；重启是确保搿点最便当个办法。

---

## 4. 安装 123Scan

123Scan 是 Zebra 拨自家扫描器做设置个免费工具。下一步用伊来更新扫描器固件，以后要检查、改扫描器设置，
伊也是最便当个办法。

1. 从 Zebra 支持网站下载 **123Scan**，运行安装程序。
2. 插好 Zebra 扫描器，开 123Scan。伊应该寻得着扫描器，显示型号、序列号搭现在个固件版本。要是寻勿着，
   继续之前换只 USB 口或者换根线试试看 — 123Scan 看勿见个扫描器，GAIA Desktop 也看勿见。

---

## 5. 更新 Zebra 扫描器固件

设置之前，先拿扫描器固件升到最新版本。固件更新会修解码问题，也让扫描器搭 SDK 装进去个 Zebra 驱动保持一致。

1. 关脱 **GAIA Desktop** 搭任何可能辣用扫描器个程序：更新个辰光 123Scan 要独占扫描器。
2. 辣 123Scan 里，扫描器连牢个辰光，拣伊个固件更新选项。123Scan 会拿扫描器个固件搭 Zebra 发布个最新版本
   比一比，再提出来下载搭安装。电脑要是上勿了网，就到 Zebra 网站浪搿只扫描器个支持页面下载固件，再拿
   文件载进 123Scan。
3. 开始更新，**一直插牢扫描器，等到 123Scan 讲好了为止**。要几分钟；最后扫描器会自家重启。做到一半
   拔脱，可能会让伊用勿来。
4. 检查 123Scan 现在显示个是新个固件版本。

更新可能会拿一眼设置还原成默认值，所以要辣固件之后，用下头两步来设置扫描器。

---

## 6. 设置 Zebra 扫描器个 USB 模式

搿一步顶容易漏脱。**Zebra 扫描器出厂个辰光是一只 USB 键盘**（USB HID Keyboard 模式）：伊会拿每只条码
"敲"进有焦点个地方。辣搿个模式里，GAIA Desktop 个 *连接扫描器* 寻勿着伊，而且敲进文本框个物事会脱落
FNC1 分隔符，GS1 元素串个意思也就变了。

拿扫描器换到 Zebra 驱动直接打交道个模式：

| 扫描器 USB 模式 | GAIA Desktop 设备名 | 备注 |
|---|---|---|
| **IBM Hand-held USB** | `ZebraUSBHandHeld`，或者 `ZebraAllScanners` | 推荐 |
| SNAPI（带或者勿带影像） | `ZebraScannerSNAPI`，或者 `ZebraAllScanners` | Zebra 自家个 USB 协议；也好用 |
| IBM Table-top USB | `ZebraUSBTableTop`，或者 `ZebraAllScanners` | 拨台式扫描器用个 |
| USB OPOS (Hand-held) | `ZebraUSBOPOS`，或者 `ZebraAllScanners` | |
| USB HID Keyboard（出厂默认） | — | **连勿上** |

`ZebraAllScanners` 是 GAIA Desktop 个默认设备名，任何支持模式个扫描器伊侪收，所以模式一设好，默认值
一般就直接好用。

要改模式，可以：

- **扫描**搿个模式个**设置条码** — 比方讲 *IBM Hand-held USB* — 辣扫描器个 Quick Reference Guide 或者
  Product Reference Guide（USB Host Types / USB Device Type 搿节）里；扫描器会"嘀"一声确认。或者
- **用 123Scan**（[第 4 步](#4-安装-123scan)）：连上扫描器，打开伊个配置，设置 USB 主机类型。

改好模式以后，Windows 会拿扫描器当另外一种设备重新识别；等几秒钟再连接。

以后要拿扫描器改回键盘模式 — 拨别个软件用 — 就扫 *USB Keyboard (HID)* 设置条码，或者恢复出厂默认。

---

## 7. 为 GS1 设置 Zebra 扫描器

扫描器告诉 GAIA Desktop 伊读个是啥条码，GAIA Desktop 解析得顶好。

### 发送 AIM Code ID

用 Product Reference Guide（User Preferences）里个设置条码，或者辣 123Scan 里，拿 **Transmit Code ID
Character** 设成 **AIM Code ID Character**。搿能每趟扫描侪会用伊个符号识别码开头 — GS1-128 是 `]C1`、
GS1 DataMatrix 是 `]d2`、GS1 QR Code 是 `]Q3`、GS1 DataBar 是 `]e0` — GAIA Desktop 用伊来讲出数据
载体是啥，再套用对应个规则。

呒没伊，GAIA Desktop 照样解析元素串，从扫描本身拿符号类型，不过报告呒没 AIM Code ID 好显示，像 EAN-13
搿种普通条码，也比较难搭 GS1 元素串分开来。

### 检查符号类型

确保侬用个条码类型侪 **启用** 了：一般是 GS1-128、GS1 DataMatrix、GS1 DataBar、GS1 QR Code 搭
EAN/UPC。Zebra 2D 扫描器浪大部分默认就开了；123Scan 会显示现在个设置，Product Reference Guide 里也有
一只只启用个条码。

### 关脱 Zebra 个 GS1 解析功能

新眼个固件加了辣扫描器本身浪跑个 GS1 功能 — **Label Parse+**、**GS1 Digital Link** 搭
**GS1 Retail POS parsing**。让伊拉保持 **关闭**，就是扫描器出厂个样子：伊拉会改写或者缩短扫描器发出来个
内容，而 GAIA Desktop 要原始数据来解析搭验证。每只功能做点啥，看
[支持个 Zebra 2D 扫描器](#12-支持个-zebra-2d-扫描器)。

### 勿要加前缀搭后缀

勿要辣扫描器输出浪加前缀、后缀或者 Enter 键。GAIA Desktop 通过 JavaPOS 拿每趟扫描当一条完整消息收，
啥也勿需要加；多出来个字符只会跑进元素串里。

---

## 8. 安装搭启动 GAIA Desktop

1. 解压 `gaia-desktop-1.0.0-win.zip`，比方讲解到 `C:\Tools\`。里向有一只文件夹：

   ```
   gaia-desktop-1.0.0-win\
   ├── docs\
   │   └── GaiaDesktop-Zebra-Scanner-Windows.md      this guide
   ├── gaia-desktop.jar
   ├── lib\
   ├── readme.html                                   index of every guide
   └── run.bat
   ```

2. 插好扫描器。
3. 双击 **`run.bat`**。程序边浪会开一只控制台窗口，一直开牢：扫描器状态搭 JavaPOS 错误侪辣搿搭报告。
4. 看控制台第一行。伊讲了 `run.bat` 寻着点啥：

   ```
   run.bat: Java 21.0.8 from JAVA_HOME; JavaFX from lib\; JavaPOS from lib\; Zebra services from C:\Program Files\Zebra Technologies\Barcode Scanners\Scanner SDK\JPOS\
   ```

   要是结尾是 **`Zebra services from none (Zebra Scanner SDK not installed)`**，就是辣默认位置寻勿着
   SDK — 安装伊（[第 3 步](#3-安装-zebra-scanner-sdk)）或者设置 [ZEBRA_HOME](#zebra_home)。程序照样
   开得起来，不过扫描器连勿上。

---

## 9. 连接扫描器

1. 辣工具栏里，**设备选择器** 列出 GAIA Desktop 认得个扫描器名字。保留 **`ZebraAllScanners`**，或者辣
   [第 6 步](#6-设置-zebra-扫描器个-usb-模式) 个表里拣扫描器 USB 模式对应个名字。
2. 揿 **连接扫描器**，或者按 **Ctrl+K**（**扫描器** 菜单里也有）。
3. 状态栏会变成 **`扫描器：ZebraAllScanners`**（或者侬拣个名字），状态行会显示
   *扫描器已连接 — ZebraAllScanners*。按钮现在写个是 **断开扫描器**。
4. 扫一只 GS1 条码。伊会出现辣采集记录顶浪，伊个报告也会打开。AIM Code ID 开了个话，报告会讲出数据载体
   — *GS1-128*、*GS1 DataMatrix* — 元素串会保留每只 FNC1 分隔符。

设备选择器会记牢侬每趟拣个物事。扫描器会一直连牢，等到侬断开或者关脱程序。**构建** 搭 **合并** 模式里
扫描也好用：辣合并模式里，每趟扫描会变成要合并个条码当中个一只。

GAIA Desktop 占牢扫描器个辰光，别个程序用勿了伊，反过来也一样：连接之前关脱 123Scan 搭任何别个收银
（point-of-sale）或者扫描器软件。

---

## 10. 设置参考

### 设备名

设备选择器个内容来自 `gaia-desktop.jar` 里向带个 `jpos.xml`。伊个扫描器条目：

| 设备名 | 扫描器类型 | 用于 |
|---|---|---|
| `ZebraAllScanners` | 任何 | **默认。** 任何支持个 USB 模式 |
| `ZebraAllScanners114` | 任何 | 同一服务个 JavaPOS 1.14 版本 |
| `ZebraUSBHandHeld` | IBM Hand-held USB | IBM Hand-held USB 模式个扫描器 |
| `ZebraUSBTableTop` | IBM Table-top USB | 台式模式 |
| `ZebraUSBOPOS` | USB OPOS | USB OPOS 模式 |
| `ZebraScannerSNAPI` | SNAPI | SNAPI 模式 |
| `ZebraScannerUSB` | 任何 USB | 任何 USB 模式 |
| `ZebraScannerSerialSSI` | 串口 SSI | 用串口线个扫描器（勿是 USB） |
| `ZebraScannerSerialNIXMODB` | 串口 NIXMODB | 用串口线个扫描器 |
| `ZebraVDI` | VDI | 转到虚拟桌面个扫描器 |

### ZEBRA_HOME

Zebra SDK 要是勿装辣默认位置，启动 `run.bat` 之前拿 `ZEBRA_HOME` 设成伊个 `JPOS` 文件夹 — 比方讲辣伊
边浪做一只小批处理文件：

```bat
@echo off
set "ZEBRA_HOME=D:\Zebra\Scanner SDK\JPOS\"
call "%~dp0run.bat" %*
```

搿能 `run.bat` 就会从 `%ZEBRA_HOME%bin` 拿 Zebra 个服务 jar，再拿搿只文件夹加进 `PATH`，拨 Zebra 个
DLL 用。

### 侬自家个 jpos.xml

要加设备名，或者改某只条目个设置，就拿 `jpos.xml` 从 `gaia-desktop.jar` 里复制出来（伊是 zip 文件；
随便啥解压工具侪打得开），改副本，再让 GAIA Desktop 用伊：

```bat
set GAIA_DESKTOP_OPTS=-Djpos.config.populatorFileURL=file:///C:/Tools/jpos.xml
run.bat
```

搿能设备选择器就会列出搿只文件里个扫描器。要是接了勿止一只 Zebra 扫描器，可以用条目个 `ScannerModel`
或者 `SerialNumber` 属性拿伊绑到其中一只浪，就像 `ZebraAllScanners` 条目一样。

### 其他选项

| 设置 | 做法 |
|---|---|
| 程序语言 | `run.bat --language=FR`（只管搿趟），或者选项面板 |
| 额外个 Java 选项 | 辣 `run.bat` 之前 `set GAIA_DESKTOP_OPTS=…` |
| 一道带来个 Java | 拿 `jdk-21…` 文件夹摆辣 `run.bat` 边浪；伊会比 `JAVA_HOME` 先用 |

---

## 11. 故障排除

错误会辣连接个辰光个对话框、控制台窗口，还有日志文件 `logs\gaia-desktop.log` 里出现；日志文件辣启动
`run.bat` 个文件夹里（双击 `run.bat` 个话，就是 GAIA Desktop 文件夹）。日志里方括号里个数字是 JavaPOS
错误码。

| 侬看到点啥 | 可能个原因 | 哪能办 |
|---|---|---|
| 呒没 *连接扫描器* 按钮 | 勿是辣 Windows 浪跑 | 扫描功能只有 Windows 有 |
| 123Scan 寻勿着扫描器 | 线、USB 口，或者扫描器拨别个程序占牢了 | 换根线或者换只口；关脱 GAIA Desktop 搭别个扫描器软件。123Scan 看勿见个扫描器，GAIA Desktop 也看勿见 |
| 控制台：`Zebra services from none (Zebra Scanner SDK not installed)` | SDK 勿辣默认位置 | 安装 64 位 SDK（[第 3 步](#3-安装-zebra-scanner-sdk)），或者设置 [ZEBRA_HOME](#zebra_home) |
| 错误 **104**：*Could not connect to service with logicalName = …: …SymScannerSvc112Factory* | 寻勿着 Zebra 个服务 jar | 同上 — 检查控制台个 `Zebra services from` 搿行 |
| 错误 **109**：*Service does not exist in loaded JCL registry* | 扫描器配置里呒没搿只设备名，或者 SDK 个 `javaPOS114.jar` 跑进了 class path | 辣设备选择器里拣一只名字；勿要加 `javaPOS114.jar`；用自家个 `jpos.xml` 个话，检查里向有搿只名字 |
| 错误 **107**（呒没硬件），或者扫描器插牢也连勿上 | 扫描器还是 USB 键盘模式，或者 CoreScanner 驱动呒没辣跑 | 设置 USB 模式（[第 6 步](#6-设置-zebra-扫描器个-usb-模式)）；装好 SDK 重启 Windows；换只 USB 口试试 |
| 连接个辰光出错误 **112**（超时）或者 **102**（已占用） | 别个程序占牢扫描器 | 关脱 123Scan 搭任何别个扫描器或者收银软件，再连一趟 |
| 控制台出现 `UnsatisfiedLinkError` … `CSJPOSScanner64` | Zebra 个 DLL 载勿进来 — 一般是 32 位 SDK 配 64 位 Java | 安装 64 位 SDK；检查 `java -version` 显示 64 位 VM |
| 扫描结果敲进了输入栏，勿是当采集进来 | 扫描器是 USB 键盘模式 | 设置 USB 模式（[第 6 步](#6-设置-zebra-扫描器个-usb-模式)） |
| 连上了，不过扫描勿进来 | 设备名搭扫描器模式对勿牢，或者连牢个辰光重新插过扫描器 | 断开，拣 `ZebraAllScanners`，再连接 |
| 报告勿显示 AIM Code ID／数据载体 | AIM Code ID 发送呒没开 | 开启 *Transmit Code ID Character → AIM Code ID*（[第 7 步](#7-为-gs1-设置-zebra-扫描器)） |
| 元素串脱落分隔符，或者 AI 粘辣一道 | 用键盘模式扫描，或者手敲个辰光呒没用 `{GS}` | 用 JavaPOS USB 模式；敲字个辰光用 `{GS}` 按钮插分隔符 |
| 某种条码一直读勿出 | 扫描器浪停用了搿种符号类型 | 启用伊（[第 7 步](#7-为-gs1-设置-zebra-扫描器)） |

问题要是还辣，拿控制台个 `run.bat:` 搿行、错误对话框个文字搭 `logs\gaia-desktop.log` 一道保留：三样拼
起来就看得出用了啥个 Java、啥个 Zebra 服务搭啥个设备名，还有连接到底辣啥地方失败。

---

## 12. 支持个 Zebra 2D 扫描器

GAIA Desktop 要一只读得出印 GS1 数据个条码个扫描器 — GS1-128、GS1 DataBar、EAN/UPC，还有大部分用场
侪要个 2D 码：**GS1 DataMatrix** 搭 **GS1 QR Code**，**GS1 Digital Link** URI 一般也是搿能印个。也就是
讲要一只 Zebra **2D 影像扫描器**：线性（1D）扫描器根本读勿出 DataMatrix 或者 QR 码。

扫描器本身勿需要懂 GS1 Application Identifiers 或者 Digital Link。
**GAIA Desktop 两样侪会解析**：伊照扫描器读到个样子收扫描 — AIM Code ID、FNC1 分隔符，样样侪有 —
再自家拆成 AI，或者拿 Digital Link URI 转成伊个 AI。

### 已经测试

| 扫描器 | 类型 | 状态 |
|---|---|---|
| **DS8108** | 有线手持 2D 影像扫描器 | 用 GAIA Desktop **开发并测试** |

### 估计好用

下头搿眼 Zebra 2D 影像扫描器搭 DS8108 一样，通过 Zebra 个 Scanner SDK 搭 JavaPOS 服务驱动。伊拉**还呒没**
用 GAIA Desktop **测试过**；每只侪照本指南设置，设置条码用伊自家个 Quick Reference Guide 或者
Product Reference Guide。

| 系列 | 型号（例子） |
|---|---|
| DS81x8 | DS8108（有线）、DS8178（无线） |
| DS22x8 | DS2208（有线）、DS2278（无线）— 一般用途 |
| DS36x8 | DS3608（有线）、DS3678（无线）— 坚固型，包括 KD 搭 XR 版本 |
| DS46x8 | DS4608（有线）、DS4678（无线） |
| DS9308 / DS9908 | 展示式搭免提扫描器 |
| MP7000 / MP7200 | 多平面双窗扫描器搭扫描秤 |
| CS6080 | 无线随身扫描器 |

无线扫描器搭伊个底座讲闲话，插到电脑浪个是底座：用 USB 接好底座，拿两样当一只扫描器就好。其他 Zebra 系列
个扫描器也可能好用，只要 Zebra Scanner SDK 辣 [第 6 步](#6-设置-zebra-扫描器个-usb-模式) 当中一只 USB
模式下支持伊拉。

### Zebra 扫描器本身个 GS1 功能

除脱读条码，有眼 Zebra 2D 影像扫描器好辣扫描器本身浪处理 GS1 数据，通过 123Scan 设置：

| 功能 | 做点啥 | 对 GAIA Desktop 来讲 |
|---|---|---|
| **Label Parse+** | 辣扫描器浪解析 GS1 标签个 AI，发出改写过格式个字段 | **保持关闭** — 勿然 GAIA Desktop 看勿见 AI、FNC1 分隔符搭 AIM Code ID |
| **GS1 Digital Link** | 对同时有 1D EAN/UPC 搭 2D Digital Link 码个零售商品：拣发哪一只 | **保持关闭**（默认） |
| **GS1 Retail POS parsing** | 读 Digital Link URI 或者元素串语法个 2D 零售条码 | **保持关闭** |

**GS1 Digital Link** 设置有三个模式。**Mode 1 — GTIN only** 是一开启就用个模式，勿管 GTIN 来自哪只条码，
只发 GTIN，所以 Digital Link 里个批号、到期日或者序列号永远到勿了 GAIA Desktop。**Mode 2 —
Prioritize 2D** 要是辣优先超时（默认 200 ms）里看到 2D 码就发 2D，勿然发 1D。**Mode 3 — Corroborate
1D and 2D GTINs Match** 发 1D，2D 个 GTIN 对得牢个辰光也一道发 2D。别个软件一定要开搿只设置个话，用
Mode 2，勿要用 Mode 1。辣 DS8108 浪，搿眼是参数 2373（开／关）、2374（模式）搭 2491（超时）。

用搿眼功能要 **123Scan 第 6 版或者更新**，还有包含搿眼功能个固件。Zebra 从 2025 年 4 月开始通过固件更新
加 GS1 Digital Link 支持；下头个日期来自一家 Zebra 经销商，所以请查各型号个固件发布说明：

| 型号系列 | GS1 Digital Link 固件发布日期 |
|---|---|
| DS81x8 | 2025 年 4 月 16 日 |
| MP7000 | 2025 年 4 月 21 日 |
| DS9908 | 2025 年 6 月 6 日 |
| SP72 / SM72 | 2025 年 7 月 11 日 |
| DS46x8 | 2025 年 7 月 28 日 |
| MP7200 (Rev-C)、DS5502 | 2025 年 7 月 30 日 |
| DS9308 | 2025 年 8 月 29 日 |
| DS22x8、DS1000 | 2025 年 9 月 5 日（只有亚太区） |
| DS36x8（包括 KD 搭 XR）、MS47 | 2025 年 9 月 30 日 |
| PL5000、SE4107 | 2025 年 10 月 15 日 |
| CS6080 | 2025 年 12 月 31 日 |

要拿 GS1 Digital Link 码读进 GAIA Desktop，上头搿眼功能一样也勿需要：伊拉关牢个辰光，用现行固件个
Zebra 2D 扫描器读 Digital Link QR 码搭读别个码呒没两样，GAIA Desktop 会拿 URI 转成伊个 AI。

资料来源：Zebra，*DS8108 Product Reference Guide*，MN-002926-11EN Rev A（2025 年 8 月），第 12 章
"GS1 Digital Link Parameters" 搭 Data Parsing 章节；Zebra，*123Scan release notes*；
Barcode Factory，*Upgrade Zebra Scanners for GS1 Digital Link*（固件日期）。
