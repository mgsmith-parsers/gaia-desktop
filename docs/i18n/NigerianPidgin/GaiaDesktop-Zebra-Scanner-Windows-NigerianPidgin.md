# How to set up Zebra scanner — GAIA Desktop for Windows

How you go install and set up Zebra barcode scanner so GAIA Desktop fit read GS1 barcodes straight
from am: every scan go parse and enter di history, with di AIM Code ID prefix and every FNC1
separator still dey complete. Di original document:
[GaiaDesktop-Zebra-Scanner-Windows.md](../../GaiaDesktop-Zebra-Scanner-Windows.md).

Dem build and test GAIA Desktop with **Zebra DS8108** handheld imager wey connect with USB. Di steps
work for any Zebra 2D scanner wey Zebra Scanner SDK support; di setup barcodes and menu names for one
particular model dey inside im own Quick Reference Guide or Product Reference Guide.

> **Na only Windows.** Zebra scanner drivers dey only for Windows, so GAIA Desktop no dey build any
> scanner control for macOS or Linux. Every other thing for di app dey work for every platform.

---

## Wetin dey inside

1. [Wetin you need](#1-wetin-you-need)
2. [Install Java](#2-install-java)
3. [Install di Zebra Scanner SDK](#3-install-di-zebra-scanner-sdk)
4. [Install 123Scan](#4-install-123scan)
5. [Update di Zebra scanner firmware](#5-update-di-zebra-scanner-firmware)
6. [Set di Zebra scanner USB mode](#6-set-di-zebra-scanner-usb-mode)
7. [Set up di Zebra scanner for GS1](#7-set-up-di-zebra-scanner-for-gs1)
8. [Install and start GAIA Desktop](#8-install-and-start-gaia-desktop)
9. [Connect di scanner](#9-connect-di-scanner)
10. [Configuration reference](#10-configuration-reference)
11. [When wahala dey](#11-when-wahala-dey)
12. [Zebra 2D scanners wey dem support](#12-zebra-2d-scanners-wey-dem-support)

---

## 1. Wetin you need

| | |
|---|---|
| **Scanner** | One Zebra 2D scanner, with im USB cable — check [Zebra 2D scanners wey dem support](#12-zebra-2d-scanners-wey-dem-support) |
| **Computer** | Windows 10 or 11, 64-bit |
| **Java** | Java 21 or di one wey new pass am, **64-bit** |
| **Zebra Scanner SDK for Windows** | **64-bit**, from Zebra support site — na im dey give di driver and di JavaPOS services wey GAIA Desktop dey use |
| **Zebra 123Scan** | Zebra free configuration utility, from Zebra support site — e dey for updating di scanner firmware and to check im settings |
| **GAIA Desktop** | `gaia-desktop-1.0.0-win.zip` |
| **If you like** | Di scanner **Product Reference Guide** (PRG), wey carry di setup barcodes |

How di pieces take join:

```
Zebra scanner ──USB──► Zebra CoreScanner driver ──► Zebra JavaPOS services ──► GAIA Desktop
                       (installed by the SDK)        (in the SDK's JPOS\bin)    (run.bat puts them on
                                                                                 the class path)
```

GAIA Desktop dey carry di JavaPOS API by imself, plus di configuration wey name di scanner (`jpos.xml`,
inside `gaia-desktop.jar`). Di Zebra SDK dey bring di rest: di driver wey dey talk to di scanner, and di
Zebra service classes wey sidon between JavaPOS and dat driver.

---

## 2. Install Java

GAIA Desktop need **Java 21 or di one wey new pass am**, and e must be di **64-bit** build, so e go
match di 64-bit Zebra drivers.

1. Install Java 21 JDK or JRE for Windows x64 — like Eclipse Temurin from
   adoptium.net. If di installer offer to set `JAVA_HOME`, allow am.
2. Check am for Command Prompt:

   ```
   java -version
   ```

   Di first line suppose show version 21 or later; 64-bit Java go also print `64-Bit Server VM`.

`run.bat` dey find Java for dis order: one `jdk*` folder wey dey beside `run.bat`, then `JAVA_HOME`,
then `java` wey dey for `PATH`. Anything wey old pass 21, e go refuse am and talk why.

---

## 3. Install di Zebra Scanner SDK

1. Download **Zebra Scanner SDK for Windows (64-bit)** from Zebra support site.
2. Run di installer. If e give you chance to choose components, make sure say di **JPOS** (JavaPOS)
   component dey inside.
3. Leave di default install location. GAIA Desktop dey find di JavaPOS part of di SDK for here:

   ```
   C:\Program Files\Zebra Technologies\Barcode Scanners\Scanner SDK\JPOS\
   ```

   and e dey expect Zebra service jars (`JposServiceScanner.jar`, `JposServiceJniScanner.jar`, …)
   and di native libraries (`CSJPOSScanner64.dll`, …) inside im `bin\` folder. If you install di SDK
   for another place, check [ZEBRA_HOME](#zebra_home).
4. **Restart Windows** after you don install am. Di SDK CoreScanner driver dey run for background,
   and e must dey run before GAIA Desktop fit reach di scanner; restart na di easiest way to sure am.

---

## 4. Install 123Scan

123Scan na Zebra free utility for setting up im scanners. Na im you go use for di next step to update
di scanner firmware, and na di easiest way to check and change di scanner settings after.

1. Download **123Scan** from Zebra support site and run di installer.
2. Plug di Zebra scanner and start 123Scan. E suppose find di scanner and show im model, serial number
   and di firmware version wey dey inside now. If e no find am, try another USB port or cable before you
   continue — GAIA Desktop no go see scanner wey 123Scan no fit see.

---

## 5. Update di Zebra scanner firmware

Bring di scanner firmware reach di latest version before you set am up. Firmware update dey fix decoding
wahala, and e dey keep di scanner in step with di Zebra driver wey di SDK install.

1. Close **GAIA Desktop** and any other program wey fit dey use di scanner: 123Scan need di scanner for
   imself alone during di update.
2. For 123Scan, as di scanner connect, choose im firmware update option. 123Scan go compare di scanner
   firmware with di latest one wey Zebra publish, and e go offer to download and install am. If di
   computer no get internet, download di scanner firmware from im support page for Zebra site and load
   di file inside 123Scan.
3. Start di update and **leave di scanner plug until 123Scan talk say e don finish**. E dey take some
   minutes; di scanner go restart for di end. If you pull am comot for middle, e fit spoil.
4. Check say 123Scan don dey show di new firmware version.

Update fit return some settings to their default, na why dem dey set up di scanner for di next two
steps, after di firmware.

---

## 6. Set di Zebra scanner USB mode

Na dis step people dey miss pass. **As Zebra scanner comot from di box, e be USB keyboard** (USB HID
Keyboard mode): e dey "type" every barcode inside anywhere wey get focus. For dat mode, GAIA Desktop
*Connect scanner* no fit find am, and anything wey dem type inside text field go lose im FNC1
separators, and dat one dey change wetin GS1 element string mean.

Switch di scanner to one mode wey di Zebra driver dey talk to directly:

| Scanner USB mode | GAIA Desktop device name | Notes |
|---|---|---|
| **IBM Hand-held USB** | `ZebraUSBHandHeld`, or `ZebraAllScanners` | Na dis one we recommend |
| SNAPI (with or without imaging) | `ZebraScannerSNAPI`, or `ZebraAllScanners` | Zebra own USB protocol; e also work |
| IBM Table-top USB | `ZebraUSBTableTop`, or `ZebraAllScanners` | Na for table-top scanners |
| USB OPOS (Hand-held) | `ZebraUSBOPOS`, or `ZebraAllScanners` | |
| USB HID Keyboard (factory default) | — | **E no go connect** |

`ZebraAllScanners`, di default device name for GAIA Desktop, dey accept scanner wey dey any of di modes
wey dem support, so once you set di mode, di default go just work most times.

To change di mode, either:

- **scan di setup barcode** for di mode — like *IBM Hand-held USB* — inside di scanner Quick Reference
  Guide or Product Reference Guide (di USB Host Types / USB Device Type section); di scanner go beep
  to confirm. Or
- **use 123Scan** ([step 4](#4-install-123scan)): connect di scanner, open im configuration, and set
  di USB host type.

After you change di mode, Windows go detect di scanner again as another kind device; wait small before
you connect.

To return di scanner to keyboard mode later — to use am with other software — scan di *USB Keyboard
(HID)* setup barcode, or restore im factory defaults.

---

## 7. Set up di Zebra scanner for GS1

GAIA Desktop dey parse best when di scanner tell am which kind barcode e read.

### Send di AIM Code ID

Set **Transmit Code ID Character** to **AIM Code ID Character**, with di setup barcode inside di
Product Reference Guide (User Preferences) or for 123Scan. Then every scan go start with im symbology
identifier — `]C1` for GS1-128, `]d2` for GS1 DataMatrix, `]Q3` for GS1 QR Code, `]e0` for GS1 DataBar —
wey GAIA Desktop dey use to name di data carrier and apply di rules for am.

Without am, GAIA Desktop go still parse di element string and take di symbology from di scan itself, but
di report no go get AIM Code ID to show, and e go hard pass to know plain barcode like EAN-13 from GS1
element string.

### Check di symbologies

Make sure say di barcode types wey you dey work with dey **on**: normally GS1-128, GS1 DataMatrix,
GS1 DataBar, GS1 QR Code and EAN/UPC. Most of dem dey on by default for Zebra 2D scanners; 123Scan dey
show di current settings, and di Product Reference Guide get barcode to on each one.

### Leave Zebra GS1 parsing features off

New firmware don add GS1 features wey dey work for di scanner itself — **Label Parse+**, **GS1
Digital Link** and **GS1 Retail POS parsing**. Leave dem **off**, as di scanner take come: dem dey
reformat or shorten wetin di scanner dey send, and GAIA Desktop need di raw data to parse and validate
am. Check [Zebra 2D scanners wey dem support](#12-zebra-2d-scanners-wey-dem-support) for wetin each one
dey do.

### No put prefix or suffix

No add prefix, suffix or Enter key to wetin di scanner dey send. GAIA Desktop dey receive every scan as
one complete message through JavaPOS and e no need any of dem; extra characters go just enter di element
string.

---

## 8. Install and start GAIA Desktop

1. Unzip `gaia-desktop-1.0.0-win.zip`, for example inside `C:\Tools\`. One folder dey inside:

   ```
   gaia-desktop-1.0.0-win\
   ├── docs\
   │   └── GaiaDesktop-Zebra-Scanner-Windows.md      this guide
   ├── gaia-desktop.jar
   ├── lib\
   ├── readme.html                                   index of every guide
   └── run.bat
   ```

2. Plug di scanner.
3. Double-click **`run.bat`**. One console window go open beside di app and e go stay open: na there
   scanner status and JavaPOS errors dey show.
4. Read di first line for di console. E dey talk wetin `run.bat` find:

   ```
   run.bat: Java 21.0.8 from JAVA_HOME; JavaFX from lib\; JavaPOS from lib\; Zebra services from C:\Program Files\Zebra Technologies\Barcode Scanners\Scanner SDK\JPOS\
   ```

   If e end with **`Zebra services from none (Zebra Scanner SDK not installed)`**, dem no find di SDK
   for im default location — install am ([step 3](#3-install-di-zebra-scanner-sdk)) or set
   [ZEBRA_HOME](#zebra_home). Di app go still start, but di scanner no go fit connect.

---

## 9. Connect di scanner

1. For di toolbar, di **device picker** dey list di scanner names wey GAIA Desktop sabi. Leave am for
   **`ZebraAllScanners`**, or pick di name for di scanner USB mode from di table for
   [step 6](#6-set-di-zebra-scanner-usb-mode).
2. Click **Connect scanner**, or press **Ctrl+K** (e also dey for di **Scanner** menu).
3. Di status bar go change to **`scanner: ZebraAllScanners`** (or di name wey you pick), and di status
   line go talk *Scanner don connect — ZebraAllScanners*. Di button go now read **Disconnect scanner**.
4. Scan one GS1 barcode. E go show for di top of di capture history and im report go open. With AIM Code
   ID on, di report go name di data carrier — *GS1-128*, *GS1 DataMatrix* — and di element string go
   keep every FNC1 separator.

Di device picker dey remember wetin you choose from one run to another. Di scanner go stay connected
until you disconnect am or close di app. Scans also dey work for **Build** and **Combine** modes: for
Combine, every scan go become one of di barcodes wey dem dey combine.

As GAIA Desktop dey hold di scanner, other programs no fit use am, and na di same for di other side:
close 123Scan and any other point-of-sale or scanner software before you connect.

---

## 10. Configuration reference

### Device names

Na di `jpos.xml` wey dey inside `gaia-desktop.jar` dey fill di device picker. Im scanner entries:

| Device name | Scanner type | Use am with |
|---|---|---|
| `ZebraAllScanners` | any | **Default.** Any USB mode wey dem support |
| `ZebraAllScanners114` | any | Di JavaPOS 1.14 version of di same service |
| `ZebraUSBHandHeld` | IBM Hand-held USB | Scanner wey dey IBM Hand-held USB mode |
| `ZebraUSBTableTop` | IBM Table-top USB | Table-top mode |
| `ZebraUSBOPOS` | USB OPOS | USB OPOS mode |
| `ZebraScannerSNAPI` | SNAPI | SNAPI mode |
| `ZebraScannerUSB` | any USB | Any USB mode |
| `ZebraScannerSerialSSI` | serial SSI | Scanners wey use serial cable (no be USB) |
| `ZebraScannerSerialNIXMODB` | serial NIXMODB | Scanners wey use serial cable |
| `ZebraVDI` | VDI | Scanners wey dem pass go virtual desktop |

### ZEBRA_HOME

If you install di Zebra SDK for another place wey no be di default, set `ZEBRA_HOME` to im `JPOS`
folder before you start `run.bat` — for example inside one small batch file beside am:

```bat
@echo off
set "ZEBRA_HOME=D:\Zebra\Scanner SDK\JPOS\"
call "%~dp0run.bat" %*
```

`run.bat` go then take Zebra service jars from `%ZEBRA_HOME%bin` and add dat folder to `PATH` for di
Zebra DLLs.

### Your own jpos.xml

To add device name or change one entry settings, copy `jpos.xml` comot from `gaia-desktop.jar` (na zip;
any unzip tool fit open am), edit di copy, and point GAIA Desktop to am:

```bat
set GAIA_DESKTOP_OPTS=-Djpos.config.populatorFileURL=file:///C:/Tools/jpos.xml
run.bat
```

Di device picker go then list di scanners wey dey dat file. If pass one Zebra scanner dey attached, you
fit tie one entry to one of dem through im `ScannerModel` or `SerialNumber` property, like for di
`ZebraAllScanners` entry.

### Other options

| Setting | How |
|---|---|
| Di app language | `run.bat --language=FR` (only for dis run), or di Options panel |
| Extra Java options | `set GAIA_DESKTOP_OPTS=…` before `run.bat` |
| Java wey you bundle | put one `jdk-21…` folder beside `run.bat`; e go use am before `JAVA_HOME` |

---

## 11. When wahala dey

Errors dey show for one dialog when you dey connect, for di console window, and for di log file
`logs\gaia-desktop.log`, inside di folder wey dem start `run.bat` from (na di GAIA Desktop folder, if
you double-click `run.bat`). Di number wey dey inside square bracket for di log na di JavaPOS error code.

| Wetin you see | Wetin fit cause am | Wetin to do |
|---|---|---|
| No *Connect scanner* button | E no dey run for Windows | Scanning na only for Windows |
| 123Scan no find di scanner | Cable, USB port, or another program dey hold di scanner | Try another cable or port; close GAIA Desktop and other scanner software. GAIA Desktop no go see scanner wey 123Scan no fit see |
| Console: `Zebra services from none (Zebra Scanner SDK not installed)` | Di SDK no dey im default location | Install di 64-bit SDK ([step 3](#3-install-di-zebra-scanner-sdk)), or set [ZEBRA_HOME](#zebra_home) |
| Error **104**: *Could not connect to service with logicalName = …: …SymScannerSvc112Factory* | Dem no find Zebra service jars | Same as above — check di console `Zebra services from` line |
| Error **109**: *Service does not exist in loaded JCL registry* | Di device name no dey di scanner configuration, or di SDK `javaPOS114.jar` don enter di class path | Pick name from di device picker; no add `javaPOS114.jar`; if you dey use your own `jpos.xml`, check say di name dey inside |
| Error **107** (no hardware), or connect fail even as di scanner plug | Di scanner still dey USB keyboard mode, or di CoreScanner driver no dey run | Set di USB mode ([step 6](#6-set-di-zebra-scanner-usb-mode)); restart Windows after you install di SDK; try another USB port |
| Error **112** (timeout) or **102** (claimed) when you dey connect | Another program dey hold di scanner | Close 123Scan and any other scanner or point-of-sale software, then connect again |
| `UnsatisfiedLinkError` … `CSJPOSScanner64` for di console | Di Zebra DLLs no fit load — many times na 32-bit SDK with 64-bit Java | Install di 64-bit SDK; check say `java -version` dey report 64-bit VM |
| Scans dey type inside di input bar instead of to come as captures | Di scanner dey USB keyboard mode | Set di USB mode ([step 6](#6-set-di-zebra-scanner-usb-mode)) |
| E don connect, but scans no dey come | Wrong device name for di scanner mode, or dem replug di scanner while e dey connected | Disconnect, pick `ZebraAllScanners`, connect again |
| Di report no dey show AIM Code ID / data carrier | AIM Code ID transmission dey off | On *Transmit Code ID Character → AIM Code ID* ([step 7](#7-set-up-di-zebra-scanner-for-gs1)) |
| Element string no get im separators, or AIs don join together | Dem scan am for keyboard mode, or type am by hand without `{GS}` | Use JavaPOS USB mode; when you dey type, put separators with di `{GS}` button |
| One barcode type no dey ever read | Dat symbology dey off for di scanner | On am ([step 7](#7-set-up-di-zebra-scanner-for-gs1)) |

If di wahala no gree go, keep di console `run.bat:` line, di error dialog text and
`logs\gaia-desktop.log` together: all of dem together dey show which Java, which Zebra services and which
device name dem use, and exactly where di connection fail.

---

## 12. Zebra 2D scanners wey dem support

GAIA Desktop need scanner wey fit read di barcodes wey dem dey print GS1 data with — GS1-128,
GS1 DataBar, EAN/UPC, and, for most work, di 2D codes: **GS1 DataMatrix** and **GS1 QR Code**, wey be
also how dem dey usually print **GS1 Digital Link** URI. Dat one mean say you need Zebra **2D imager**:
linear (1D) scanner no fit read DataMatrix or QR codes at all.

Di scanner no need to understand GS1 Application Identifiers or Digital Link by imself.
**GAIA Desktop dey parse both**: e dey take di scan as di scanner read am — AIM Code ID, FNC1 separators
and everything — and e go split am into AIs, or turn Digital Link URI into im AIs, by imself.

### Dem don test am

| Scanner | Type | Status |
|---|---|---|
| **DS8108** | Corded handheld 2D imager | **Dem build and test am** with GAIA Desktop |

### E suppose work

Dis ones na Zebra 2D imagers wey, like DS8108, dey run through Zebra Scanner SDK and JavaPOS services.
Dem **never test dem** with GAIA Desktop; set each one up as dis guide talk, and use im own Quick
Reference Guide or Product Reference Guide for di setup barcodes.

| Family | Models (examples) |
|---|---|
| DS81x8 | DS8108 (with cord), DS8178 (cordless) |
| DS22x8 | DS2208 (with cord), DS2278 (cordless) — for general work |
| DS36x8 | DS3608 (with cord), DS3678 (cordless) — strong well well, plus di KD and XR types |
| DS46x8 | DS4608 (with cord), DS4678 (cordless) |
| DS9308 / DS9908 | Presentation and hands-free scanners |
| MP7000 / MP7200 | Multi-plane bioptic scanners and scanner-scales |
| CS6080 | Cordless companion scanner |

Cordless scanner dey talk to im cradle, and na di cradle dey plug inside di computer: connect di cradle
with USB and treat di two as one scanner. Scanners from other Zebra families fit work too, if Zebra
Scanner SDK support dem for one of di USB modes for [step 6](#6-set-di-zebra-scanner-usb-mode).

### Di GS1 features wey dey inside Zebra scanner

Apart from reading di barcodes, some Zebra 2D imagers fit process GS1 data for di scanner itself, and
dem dey set am through 123Scan:

| Feature | Wetin e dey do | For GAIA Desktop |
|---|---|---|
| **Label Parse+** | E dey parse GS1 label AIs for di scanner and send fields wey e don reformat | **Leave am off** — GAIA Desktop no go see di AIs, FNC1 separators and AIM Code ID again |
| **GS1 Digital Link** | For retail products wey get both 1D EAN/UPC and 2D Digital Link code: e dey choose which one to send | **Leave am off** (na di default) |
| **GS1 Retail POS parsing** | E dey read 2D retail barcodes for Digital Link URI or element string syntax | **Leave am off** |

Di **GS1 Digital Link** setting get three modes. **Mode 1 — GTIN only**, di mode wey e dey start with
once you on am, dey send only di GTIN, no matter which barcode e come from, so batch, expiry date or
serial number for di Digital Link no go ever reach GAIA Desktop. **Mode 2 — Prioritize 2D** dey send di
2D code if e see am inside di prioritization timeout (200 ms by default), if not, di 1D. **Mode 3 —
Corroborate 1D and 2D GTINs Match** dey send di 1D, and di 2D too if im GTIN match. If di setting must
dey on for another software, use Mode 2, no be Mode 1. For DS8108 na parameters 2373 (on/off), 2374
(mode) and 2491 (timeout).

To use dis features, you need **123Scan version 6 or newer** and firmware wey get dem. Zebra don dey add
GS1 Digital Link support through firmware update since April 2025; di dates wey dey below come from one
Zebra reseller, so check di firmware release notes for each model:

| Model family | When GS1 Digital Link firmware comot |
|---|---|
| DS81x8 | 16 April 2025 |
| MP7000 | 21 April 2025 |
| DS9908 | 6 June 2025 |
| SP72 / SM72 | 11 July 2025 |
| DS46x8 | 28 July 2025 |
| MP7200 (Rev-C), DS5502 | 30 July 2025 |
| DS9308 | 29 August 2025 |
| DS22x8, DS1000 | 5 September 2025 (only Asia-Pacific) |
| DS36x8 (plus KD and XR), MS47 | 30 September 2025 |
| PL5000, SE4107 | 15 October 2025 |
| CS6080 | 31 December 2025 |

You no need any of dis features to read GS1 Digital Link codes inside GAIA Desktop: when dem dey off,
Zebra 2D scanner wey get current firmware dey read Digital Link QR code like any other one, and
GAIA Desktop go change di URI to im AIs.

Sources: Zebra, *DS8108 Product Reference Guide*, MN-002926-11EN Rev A (August 2025), chapter 12
"GS1 Digital Link Parameters" and di Data Parsing chapter; Zebra, *123Scan release notes*;
Barcode Factory, *Upgrade Zebra Scanners for GS1 Digital Link* (firmware dates).
