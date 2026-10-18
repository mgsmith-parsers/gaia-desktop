# Zebra 扫描器设置 — Windows 版 GAIA Desktop

本指南说明如何安装和配置 Zebra 条码扫描器，使 GAIA Desktop 能直接从扫描器读取 GS1 条码：每次扫描
都会被解析并添加到历史中，AIM Code ID 前缀和每个 FNC1 分隔符都完整保留。原文（英文）：
[GaiaDesktop-Zebra-Scanner-Windows.md](../../GaiaDesktop-Zebra-Scanner-Windows.md)。

GAIA Desktop 使用通过 USB 连接的 **Zebra DS8108** 手持式成像扫描器开发和测试。这些步骤适用于
Zebra Scanner SDK 支持的所有 Zebra 2D 扫描器；具体型号的设置条码和菜单名称，请参阅该型号自己的
Quick Reference Guide 或 Product Reference Guide。

> **仅限 Windows。** Zebra 的扫描器驱动程序只有 Windows 版，因此在 macOS 或 Linux 上
> GAIA Desktop 不会创建任何扫描器控件。应用的其他所有功能在各个平台上都能使用。

---

## 目录

1. [所需条件](#1-所需条件)
2. [安装 Java](#2-安装-java)
3. [安装 Zebra Scanner SDK](#3-安装-zebra-scanner-sdk)
4. [安装 123Scan](#4-安装-123scan)
5. [更新 Zebra 扫描器固件](#5-更新-zebra-扫描器固件)
6. [设置 Zebra 扫描器的 USB 模式](#6-设置-zebra-扫描器的-usb-模式)
7. [为 GS1 配置 Zebra 扫描器](#7-为-gs1-配置-zebra-扫描器)
8. [安装并启动 GAIA Desktop](#8-安装并启动-gaia-desktop)
9. [连接扫描器](#9-连接扫描器)
10. [配置参考](#10-配置参考)
11. [故障排除](#11-故障排除)
12. [支持的 Zebra 2D 扫描器](#12-支持的-zebra-2d-扫描器)

---

## 1. 所需条件

| | |
|---|---|
| **扫描器** | 一台 Zebra 2D 扫描器及其 USB 线缆 — 见[支持的 Zebra 2D 扫描器](#12-支持的-zebra-2d-扫描器) |
| **计算机** | Windows 10 或 11，64 位 |
| **Java** | Java 21 或更高版本，**64 位** |
| **Zebra Scanner SDK for Windows** | **64 位**，来自 Zebra 支持网站 — 提供 GAIA Desktop 使用的驱动程序和 JavaPOS 服务 |
| **Zebra 123Scan** | Zebra 的免费配置工具，来自其支持网站 — 用于更新扫描器固件和检查其设置 |
| **GAIA Desktop** | `gaia-desktop-1.0.0-win.zip` |
| **可选** | 扫描器的 **Product Reference Guide**（PRG），其中有设置条码 |

各部分如何协同工作：

```
Zebra scanner ──USB──► Zebra CoreScanner driver ──► Zebra JavaPOS services ──► GAIA Desktop
                       (installed by the SDK)        (in the SDK's JPOS\bin)    (run.bat puts them on
                                                                                 the class path)
```

GAIA Desktop 自带 JavaPOS API 以及为扫描器命名的配置（`gaia-desktop.jar` 中的 `jpos.xml`）。其余
部分由 Zebra SDK 提供：与扫描器通信的驱动程序，以及位于 JavaPOS 与该驱动程序之间的 Zebra 服务类。

---

## 2. 安装 Java

GAIA Desktop 需要 **Java 21 或更高版本**，而且必须是 **64 位**版本，以匹配 64 位的 Zebra 驱动程序。

1. 安装适用于 Windows x64 的 Java 21 JDK 或 JRE — 例如 adoptium.net 上的 Eclipse Temurin。如果
   安装程序提议设置 `JAVA_HOME`，请让它设置。
2. 在命令提示符中检查：

   ```
   java -version
   ```

   第一行应显示 21 或更高版本；64 位 Java 还会显示 `64-Bit Server VM`。

`run.bat` 按以下顺序查找 Java：`run.bat` 旁边的 `jdk*` 文件夹，然后是 `JAVA_HOME`，最后是 `PATH`
中的 `java`。低于 21 的版本会被拒绝，并给出相应提示。

---

## 3. 安装 Zebra Scanner SDK

1. 从 Zebra 支持网站下载 **Zebra Scanner SDK for Windows（64 位）**。
2. 运行安装程序。如果可以选择组件，请确保包含 **JPOS**（JavaPOS）组件。
3. 保留默认安装位置。GAIA Desktop 在以下位置查找 SDK 的 JavaPOS 部分：

   ```
   C:\Program Files\Zebra Technologies\Barcode Scanners\Scanner SDK\JPOS\
   ```

   并要求其 `bin\` 文件夹中有 Zebra 的服务 jar（`JposServiceScanner.jar`、`JposServiceJniScanner.jar`
   等）和本机库（`CSJPOSScanner64.dll` 等）。如果 SDK 安装在其他位置，请参阅
   [ZEBRA_HOME](#zebra_home)。
4. 安装后**重新启动 Windows**。SDK 的 CoreScanner 驱动程序在后台运行，必须在 GAIA Desktop 访问
   扫描器之前运行；重新启动是确保这一点的最简单方法。

---

## 4. 安装 123Scan

123Scan 是 Zebra 用于配置其扫描器的免费工具。下一步将用它更新扫描器固件，之后它也是检查和更改
扫描器设置的最简单方式。

1. 从 Zebra 支持网站下载 **123Scan** 并运行安装程序。
2. 插入 Zebra 扫描器并启动 123Scan。它应能找到扫描器，并显示其型号、序列号和当前固件版本。如果
   没有，请在继续之前尝试其他 USB 端口或线缆 — 123Scan 看不到的扫描器，GAIA Desktop 也看不到。

---

## 5. 更新 Zebra 扫描器固件

在配置之前，请将扫描器固件更新到最新版本。固件更新可修复解码问题，并使扫描器与 SDK 安装的 Zebra
驱动程序保持一致。

1. 关闭 **GAIA Desktop** 以及可能正在使用扫描器的任何其他程序：更新期间 123Scan 需要独占扫描器。
2. 在 123Scan 中，在扫描器已连接的状态下，选择其固件更新选项。123Scan 会将扫描器的固件与 Zebra
   发布的最新版本进行比较，并提供下载和安装。如果计算机无法访问互联网，请从 Zebra 网站上该扫描器的
   支持页面下载固件，再将文件加载到 123Scan 中。
3. 开始更新，并**在 123Scan 报告完成之前保持扫描器连接**。这需要几分钟；最后扫描器会重新启动。
   中途拔出可能导致扫描器无法使用。
4. 确认 123Scan 现在显示新的固件版本。

更新可能会将部分设置恢复为默认值，因此扫描器要在固件更新之后、在接下来的两个步骤中进行配置。

---

## 6. 设置 Zebra 扫描器的 USB 模式

这是最常被遗漏的一步。**出厂时，Zebra 扫描器的行为就像一个 USB 键盘**（USB HID Keyboard 模式）：
它把每个条码“键入”到当前获得焦点的位置。在该模式下，GAIA Desktop 的 *连接扫描器* 找不到它，
而键入文本字段的任何内容都会丢失 FNC1 分隔符，从而改变 GS1 元素串的含义。

请将扫描器切换到 Zebra 驱动程序可直接通信的模式：

| 扫描器 USB 模式 | GAIA Desktop 设备名称 | 说明 |
|---|---|---|
| **IBM Hand-held USB** | `ZebraUSBHandHeld` 或 `ZebraAllScanners` | 推荐 |
| SNAPI（带或不带图像） | `ZebraScannerSNAPI` 或 `ZebraAllScanners` | Zebra 自有的 USB 协议；同样可用 |
| IBM Table-top USB | `ZebraUSBTableTop` 或 `ZebraAllScanners` | 适用于台式扫描器 |
| USB OPOS (Hand-held) | `ZebraUSBOPOS` 或 `ZebraAllScanners` | |
| USB HID Keyboard（出厂默认） | — | **无法连接** |

GAIA Desktop 的默认设备名称 `ZebraAllScanners` 接受处于任何受支持模式的扫描器，因此设置好模式后，
通常使用默认值即可。

更改模式的方法有两种：

- 在扫描器的 Quick Reference Guide 或 Product Reference Guide（USB Host Types / USB Device Type
  一节）中，**扫描该模式的设置条码** — 例如 *IBM Hand-held USB*；扫描器会以蜂鸣声确认。或者
- **使用 123Scan**（[第 4 步](#4-安装-123scan)）：连接扫描器，打开其配置，并设置 USB 主机类型。

更改模式后，Windows 会将扫描器重新识别为另一类设备；连接前请等待几秒钟。

日后若要将扫描器恢复为键盘模式 — 以便配合其他软件使用 — 请扫描 *USB Keyboard (HID)* 设置条码，
或恢复其出厂设置。

---

## 7. 为 GS1 配置 Zebra 扫描器

当扫描器告诉 GAIA Desktop 它读取的是哪种条码时，GAIA Desktop 的解析效果最好。

### 发送 AIM Code ID

使用 Product Reference Guide（User Preferences）中的设置条码或在 123Scan 中，将
**Transmit Code ID Character** 设置为 **AIM Code ID Character**。之后每次扫描都会以其码制标识符
开头 — GS1-128 为 `]C1`，GS1 DataMatrix 为 `]d2`，GS1 QR Code 为 `]Q3`，GS1 DataBar 为 `]e0` —
GAIA Desktop 用它来确定数据载体并应用相应规则。

没有它时，GAIA Desktop 仍会解析元素串，并从扫描本身获取码制，但报告中没有可显示的 AIM Code ID，
而且像 EAN-13 这样的普通条码也更难与 GS1 元素串区分开。

### 检查码制

确保您使用的条码类型已**启用**：通常为 GS1-128、GS1 DataMatrix、GS1 DataBar、GS1 QR Code 和
EAN/UPC。在 Zebra 2D 扫描器上，大多数默认已启用；123Scan 会显示当前设置，Product Reference Guide
中也有启用各码制的条码。

### 保持关闭 Zebra 的 GS1 解析功能

较新的固件增加了在扫描器内部工作的 GS1 功能 — **Label Parse+**、**GS1 Digital Link** 和
**GS1 Retail POS parsing**。请保持其**关闭**，即出厂时的状态：它们会重新格式化或缩短扫描器发送的
内容，而 GAIA Desktop 需要原始数据来解析和验证。各功能的作用请参阅
[支持的 Zebra 2D 扫描器](#12-支持的-zebra-2d-扫描器)。

### 不要添加前缀和后缀

不要在扫描器的输出中添加前缀、后缀或 Enter 键。GAIA Desktop 通过 JavaPOS 将每次扫描作为完整消息
接收，不需要这些内容；多余的字符会进入元素串。

---

## 8. 安装并启动 GAIA Desktop

1. 解压 `gaia-desktop-1.0.0-win.zip`，例如解压到 `C:\Tools\`。其中只有一个文件夹：

   ```
   gaia-desktop-1.0.0-win\
   ├── docs\
   │   └── GaiaDesktop-Zebra-Scanner-Windows.md      this guide
   ├── gaia-desktop.jar
   ├── lib\
   ├── readme.html                                   index of every guide
   └── run.bat
   ```

2. 插入扫描器。
3. 双击 **`run.bat`**。应用旁边会打开一个控制台窗口并保持打开：扫描器状态和 JavaPOS 错误都会
   显示在那里。
4. 阅读控制台的第一行。它说明了 `run.bat` 找到了什么：

   ```
   run.bat: Java 21.0.8 from JAVA_HOME; JavaFX from lib\; JavaPOS from lib\; Zebra services from C:\Program Files\Zebra Technologies\Barcode Scanners\Scanner SDK\JPOS\
   ```

   如果它以 **`Zebra services from none (Zebra Scanner SDK not installed)`** 结尾，说明未在默认
   位置找到 SDK — 请安装 SDK（[第 3 步](#3-安装-zebra-scanner-sdk)）或设置
   [ZEBRA_HOME](#zebra_home)。应用仍会启动，但扫描器无法连接。

---

## 9. 连接扫描器

1. 工具栏中的**设备选择框**列出 GAIA Desktop 已知的扫描器名称。保持为 **`ZebraAllScanners`**，或从
   [第 6 步](#6-设置-zebra-扫描器的-usb-模式)的表中选择与扫描器 USB 模式对应的名称。
2. 单击 **连接扫描器**，或按 **Ctrl+K**（也在 **扫描器** 菜单中）。
3. 状态栏变为 **`扫描器：ZebraAllScanners`**（或您选择的名称），状态行显示
   *扫描器已连接 — ZebraAllScanners*。按钮现在显示为 **断开扫描器**。
4. 扫描一个 GS1 条码。它会出现在采集历史的顶部，并打开其报告。启用 AIM Code ID 后，报告会给出
   数据载体 — *GS1-128*、*GS1 DataMatrix* — 元素串也会保留每个 FNC1 分隔符。

设备选择框会在多次运行之间记住您的选择。扫描器会一直保持连接，直到您断开连接或关闭应用。在
**构建** 和 **合并** 模式下扫描同样可用：在合并模式下，每次扫描都
成为要合并的条码之一。

GAIA Desktop 占用扫描器期间，其他程序无法使用它，反之亦然：连接之前，请关闭 123Scan 以及任何其他
POS 或扫描器软件。

---

## 10. 配置参考

### 设备名称

设备选择框的内容来自 `gaia-desktop.jar` 中附带的 `jpos.xml`。其中的扫描器条目：

| 设备名称 | 扫描器类型 | 适用情况 |
|---|---|---|
| `ZebraAllScanners` | 任意 | **默认。** 任何受支持的 USB 模式 |
| `ZebraAllScanners114` | 任意 | 同一服务的 JavaPOS 1.14 版本 |
| `ZebraUSBHandHeld` | IBM Hand-held USB | 处于 IBM Hand-held USB 模式的扫描器 |
| `ZebraUSBTableTop` | IBM Table-top USB | 台式模式 |
| `ZebraUSBOPOS` | USB OPOS | USB OPOS 模式 |
| `ZebraScannerSNAPI` | SNAPI | SNAPI 模式 |
| `ZebraScannerUSB` | 任意 USB | 任何 USB 模式 |
| `ZebraScannerSerialSSI` | 串口 SSI | 串口线缆扫描器（非 USB） |
| `ZebraScannerSerialNIXMODB` | 串口 NIXMODB | 串口线缆扫描器 |
| `ZebraVDI` | VDI | 重定向到虚拟桌面的扫描器 |

### ZEBRA_HOME

如果 Zebra SDK 安装在非默认位置，请在启动 `run.bat` 之前将 `ZEBRA_HOME` 设置为其 `JPOS`
文件夹 — 例如在旁边放一个小批处理文件：

```bat
@echo off
set "ZEBRA_HOME=D:\Zebra\Scanner SDK\JPOS\"
call "%~dp0run.bat" %*
```

这样 `run.bat` 就会从 `%ZEBRA_HOME%bin` 获取 Zebra 的服务 jar，并为 Zebra DLL 将该文件夹添加到
`PATH`。

### 自定义 jpos.xml

要添加设备名称或更改某个条目的设置，请从 `gaia-desktop.jar` 中复制出 `jpos.xml`（jar 就是 zip；
任何解压工具都能打开），编辑副本，然后让 GAIA Desktop 使用它：

```bat
set GAIA_DESKTOP_OPTS=-Djpos.config.populatorFileURL=file:///C:/Tools/jpos.xml
run.bat
```

设备选择框随后会列出该文件中的扫描器。连接了多台 Zebra 扫描器时，可以像 `ZebraAllScanners` 条目
那样，通过 `ScannerModel` 或 `SerialNumber` 属性将某个条目绑定到其中一台。

### 其他选项

| 设置 | 方法 |
|---|---|
| 应用语言 | `run.bat --language=FR`（仅本次运行），或选项面板 |
| 额外的 Java 选项 | 在 `run.bat` 之前 `set GAIA_DESKTOP_OPTS=…` |
| 随附的 Java | 在 `run.bat` 旁边放一个 `jdk-21…` 文件夹；它优先于 `JAVA_HOME` |

---

## 11. 故障排除

错误会出现在连接时的对话框、控制台窗口，以及启动 `run.bat` 所在文件夹中的日志文件
`logs\gaia-desktop.log`（双击 `run.bat` 时即 GAIA Desktop 文件夹）。日志中方括号内的数字是
JavaPOS 错误代码。

| 现象 | 可能原因 | 处理方法 |
|---|---|---|
| 没有 *连接扫描器* 按钮 | 未在 Windows 上运行 | 扫描功能仅限 Windows |
| 123Scan 找不到扫描器 | 线缆、USB 端口，或其他程序占用了扫描器 | 尝试其他线缆或端口；关闭 GAIA Desktop 和其他扫描器软件。123Scan 看不到的扫描器，GAIA Desktop 也看不到 |
| 控制台：`Zebra services from none (Zebra Scanner SDK not installed)` | SDK 不在默认位置 | 安装 64 位 SDK（[第 3 步](#3-安装-zebra-scanner-sdk)），或设置 [ZEBRA_HOME](#zebra_home) |
| 错误 **104**：*Could not connect to service with logicalName = …: …SymScannerSvc112Factory* | 找不到 Zebra 的服务 jar | 同上 — 检查控制台的 `Zebra services from` 行 |
| 错误 **109**：*Service does not exist in loaded JCL registry* | 设备名称不在扫描器配置中，或 SDK 的 `javaPOS114.jar` 进入了 class path | 从设备选择框中选择名称；不要添加 `javaPOS114.jar`；如果使用自定义 `jpos.xml`，确认其中有该名称 |
| 错误 **107**（无硬件），或扫描器已插入但连接失败 | 扫描器仍处于 USB 键盘模式，或 CoreScanner 驱动程序未运行 | 设置 USB 模式（[第 6 步](#6-设置-zebra-扫描器的-usb-模式)）；安装 SDK 后重新启动 Windows；尝试其他 USB 端口 |
| 连接时出现错误 **112**（超时）或 **102**（已被占用） | 其他程序占用了扫描器 | 关闭 123Scan 和其他扫描器或 POS 软件，然后重新连接 |
| 控制台中出现 `UnsatisfiedLinkError` … `CSJPOSScanner64` | 无法加载 Zebra DLL — 常见原因是 64 位 Java 配 32 位 SDK | 安装 64 位 SDK；确认 `java -version` 报告的是 64 位 VM |
| 扫描内容被键入输入栏，而不是作为采集项到达 | 扫描器处于 USB 键盘模式 | 设置 USB 模式（[第 6 步](#6-设置-zebra-扫描器的-usb-模式)） |
| 已连接，但扫描内容没有到达 | 设备名称与扫描器模式不符，或在连接状态下重新插拔了扫描器 | 断开连接，选择 `ZebraAllScanners`，重新连接 |
| 报告中没有 AIM Code ID 或数据载体 | AIM Code ID 发送已关闭 | 打开 *Transmit Code ID Character → AIM Code ID*（[第 7 步](#7-为-gs1-配置-zebra-扫描器)） |
| 元素串缺少分隔符，或 AI 连在一起 | 在键盘模式下扫描，或手动键入时未使用 `{GS}` | 使用 JavaPOS USB 模式；手动键入时用 `{GS}` 按钮插入分隔符 |
| 某种条码始终无法读取 | 扫描器上禁用了该码制 | 启用它（[第 7 步](#7-为-gs1-配置-zebra-扫描器)） |

如果问题仍然存在，请把控制台的 `run.bat:` 行、错误对话框的文字和 `logs\gaia-desktop.log` 一起
保存：它们共同说明了使用的是哪个 Java、哪些 Zebra 服务、哪个设备名称，以及连接究竟在哪里失败。

---

## 12. 支持的 Zebra 2D 扫描器

GAIA Desktop 需要一台能够读取印有 GS1 数据的条码的扫描器 — GS1-128、GS1 DataBar、EAN/UPC，以及
在大多数用途中需要的 2D 码：**GS1 DataMatrix** 和 **GS1 QR Code**（**GS1 Digital Link** URI 通常
也以这种形式印刷）。也就是说需要一台 Zebra **2D 成像扫描器**：线性（1D）扫描器根本无法读取
DataMatrix 或 QR 码。

扫描器本身不需要理解 GS1 应用标识符（Application Identifier）或 Digital Link。
**GAIA Desktop 负责解析两者**：它按扫描器读取的原样接收扫描内容 — 包括 AIM Code ID 和 FNC1
分隔符 — 并将其拆分为 AI，或自行将 Digital Link URI 转换为对应的 AI。

### 已测试

| 扫描器 | 类型 | 状态 |
|---|---|---|
| **DS8108** | 有线手持式 2D 成像扫描器 | 已与 GAIA Desktop **一起开发和测试** |

### 预计可用

这些 Zebra 2D 成像扫描器与 DS8108 一样，通过 Zebra 的 Scanner SDK 和 JavaPOS 服务驱动。它们
**尚未**与 GAIA Desktop **一起测试**；请按本指南逐一设置，设置条码使用各型号自己的
Quick Reference Guide 或 Product Reference Guide。

| 系列 | 型号（示例） |
|---|---|
| DS81x8 | DS8108（有线）、DS8178（无线） |
| DS22x8 | DS2208（有线）、DS2278（无线）— 通用型 |
| DS36x8 | DS3608（有线）、DS3678（无线）— 坚固型，包括 KD 和 XR 变体 |
| DS46x8 | DS4608（有线）、DS4678（无线） |
| DS9308 / DS9908 | 展示型和免提型扫描器 |
| MP7000 / MP7200 | 多平面双光窗扫描器和扫描秤 |
| CS6080 | 无线伴侣式扫描器 |

无线扫描器与其底座通信，插到计算机上的是底座：用 USB 连接底座，并把这一对设备当作一台扫描器。
其他 Zebra 系列的扫描器也可能可用，前提是 Zebra 的 Scanner SDK 在[第 6 步](#6-设置-zebra-扫描器的-usb-模式)
中的某种 USB 模式下支持它们。

### Zebra 扫描器内置的 GS1 功能

除了读取条码之外，部分 Zebra 2D 成像扫描器还能通过 123Scan 配置，在扫描器内部处理 GS1 数据：

| 功能 | 作用 | 对 GAIA Desktop 而言 |
|---|---|---|
| **Label Parse+** | 在扫描器内解析 GS1 标签的 AI，并发送重新格式化的字段 | **保持关闭** — GAIA Desktop 将看不到 AI、FNC1 分隔符和 AIM Code ID |
| **GS1 Digital Link** | 适用于同时带有 1D EAN/UPC 和 2D Digital Link 码的零售商品：选择发送哪一个 | **保持关闭**（默认） |
| **GS1 Retail POS parsing** | 读取采用 Digital Link URI 语法或元素串语法的零售 2D 条码 | **保持关闭** |

**GS1 Digital Link** 设置有三种模式。**Mode 1 — GTIN only** 是启用后的初始模式，无论来自哪个
条码，都只发送 GTIN，因此 Digital Link 中的批号、有效期或序列号永远不会到达 GAIA Desktop。
**Mode 2 — Prioritize 2D** 在优先超时时间（默认 200 ms）内看到 2D 码则发送 2D 码，否则发送 1D 码。
**Mode 3 — Corroborate 1D and 2D GTINs Match** 发送 1D 码，GTIN 匹配时还会发送 2D 码。如果为了
其他软件必须启用此设置，请使用 Mode 2，而不是 Mode 1。在 DS8108 上，这些是参数 2373（开/关）、
2374（模式）和 2491（超时）。

使用这些功能需要 **123Scan 6 或更高版本**以及包含这些功能的固件。自 2025 年 4 月起，Zebra 通过
固件更新陆续增加对 GS1 Digital Link 的支持；以下日期来自一家 Zebra 经销商，请核对各型号固件的
发行说明：

| 型号系列 | GS1 Digital Link 固件发布日期 |
|---|---|
| DS81x8 | 2025 年 4 月 16 日 |
| MP7000 | 2025 年 4 月 21 日 |
| DS9908 | 2025 年 6 月 6 日 |
| SP72 / SM72 | 2025 年 7 月 11 日 |
| DS46x8 | 2025 年 7 月 28 日 |
| MP7200 (Rev-C)、DS5502 | 2025 年 7 月 30 日 |
| DS9308 | 2025 年 8 月 29 日 |
| DS22x8、DS1000 | 2025 年 9 月 5 日（仅限亚太地区） |
| DS36x8（含 KD 和 XR）、MS47 | 2025 年 9 月 30 日 |
| PL5000、SE4107 | 2025 年 10 月 15 日 |
| CS6080 | 2025 年 12 月 31 日 |

要把 GS1 Digital Link 码读入 GAIA Desktop，并不需要这些功能中的任何一项：在它们关闭的情况下，
使用当前固件的 Zebra 2D 扫描器会像读取其他任何码一样读取 Digital Link QR 码，再由 GAIA Desktop
把 URI 转换为对应的 AI。

资料来源：Zebra，*DS8108 Product Reference Guide*，MN-002926-11EN Rev A（2025 年 8 月），第 12 章
“GS1 Digital Link Parameters”及 Data Parsing 章；Zebra，*123Scan release notes*；
Barcode Factory，*Upgrade Zebra Scanners for GS1 Digital Link*（固件日期）。
