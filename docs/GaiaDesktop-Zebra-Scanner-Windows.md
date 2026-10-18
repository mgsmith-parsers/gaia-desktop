# Zebra scanner setup — GAIA Desktop for Windows

How to install and configure a Zebra barcode scanner so that GAIA Desktop can read GS1 barcodes
from it directly: every scan is parsed and added to the history, with the AIM Code ID prefix
and every FNC1 separator intact.

GAIA Desktop was built and tested with a **Zebra DS8108** handheld imager connected by USB. The
steps apply to any Zebra 2D scanner that Zebra's Scanner SDK supports; the setup barcodes and
menu names for a particular model come from its own Quick Reference or Product Reference Guide.

> **Windows only.** Zebra's scanner drivers exist for Windows only, so GAIA Desktop builds no
> scanner controls on macOS or Linux. Everything else in the app works on every platform.

---

## Contents

1. [What you need](#1-what-you-need)
2. [Install Java](#2-install-java)
3. [Install the Zebra Scanner SDK](#3-install-the-zebra-scanner-sdk)
4. [Install 123Scan](#4-install-123scan)
5. [Update the Zebra scanner's firmware](#5-update-the-zebra-scanners-firmware)
6. [Set the Zebra scanner's USB mode](#6-set-the-zebra-scanners-usb-mode)
7. [Configure the Zebra scanner for GS1](#7-configure-the-zebra-scanner-for-gs1)
8. [Install and start GAIA Desktop](#8-install-and-start-gaia-desktop)
9. [Connect the scanner](#9-connect-the-scanner)
10. [Configuration reference](#10-configuration-reference)
11. [Troubleshooting](#11-troubleshooting)
12. [Supported Zebra 2D scanners](#12-supported-zebra-2d-scanners)

---

## 1. What you need

| | |
|---|---|
| **Scanner** | A Zebra 2D scanner, with its USB cable — see [Supported Zebra 2D scanners](#12-supported-zebra-2d-scanners) |
| **Computer** | Windows 10 or 11, 64-bit |
| **Java** | Java 21 or newer, **64-bit** |
| **Zebra Scanner SDK for Windows** | **64-bit**, from Zebra's support site — it provides the driver and the JavaPOS services GAIA Desktop uses |
| **Zebra 123Scan** | Zebra's free configuration utility, from Zebra's support site — used to update the scanner's firmware and to check its settings |
| **GAIA Desktop** | `gaia-desktop-1.0.0-win.zip` |
| **Optional** | The scanner's **Product Reference Guide** (PRG), which holds the setup barcodes |

How the pieces fit:

```
Zebra scanner ──USB──► Zebra CoreScanner driver ──► Zebra JavaPOS services ──► GAIA Desktop
                       (installed by the SDK)        (in the SDK's JPOS\bin)    (run.bat puts them on
                                                                                 the class path)
```

GAIA Desktop carries the JavaPOS API itself and the configuration that names the scanner
(`jpos.xml`, inside `gaia-desktop.jar`). The Zebra SDK supplies the rest: the driver that talks
to the scanner and the Zebra service classes that sit between JavaPOS and that driver.

---

## 2. Install Java

GAIA Desktop needs **Java 21 or newer**, and it must be the **64-bit** build, to match the
64-bit Zebra drivers.

1. Install a Java 21 JDK or JRE for Windows x64 — for example Eclipse Temurin from
   adoptium.net. Let the installer set `JAVA_HOME` if it offers to.
2. Check it in a Command Prompt:

   ```
   java -version
   ```

   The first line should show version 21 or later; a 64-bit Java also prints `64-Bit Server VM`.

`run.bat` looks for Java in this order: a `jdk*` folder beside `run.bat`, then `JAVA_HOME`, then
`java` on the `PATH`. It refuses anything older than 21 with a message saying so.

---

## 3. Install the Zebra Scanner SDK

1. Download the **Zebra Scanner SDK for Windows (64-bit)** from Zebra's support site.
2. Run the installer. If it offers a choice of components, make sure the **JPOS** (JavaPOS)
   component is included.
3. Keep the default install location. GAIA Desktop looks for the JavaPOS part of the SDK in:

   ```
   C:\Program Files\Zebra Technologies\Barcode Scanners\Scanner SDK\JPOS\
   ```

   and expects Zebra's service jars (`JposServiceScanner.jar`, `JposServiceJniScanner.jar`, …)
   and the native libraries (`CSJPOSScanner64.dll`, …) in its `bin\` folder. If the SDK is
   installed somewhere else, see [ZEBRA_HOME](#zebra_home).
4. **Restart Windows** after installing. The SDK's CoreScanner driver runs in the background and
   must be running before GAIA Desktop can reach the scanner; a restart is the simplest way to
   be sure it is.

---

## 4. Install 123Scan

123Scan is Zebra's free utility for configuring its scanners. It is used in the next step to
update the scanner's firmware, and is the easiest way to check and change the scanner's settings
afterwards.

1. Download **123Scan** from Zebra's support site and run the installer.
2. Plug in the Zebra scanner and start 123Scan. It should find the scanner and show its model, serial
   number and current firmware version. If it does not, try another USB port or cable before
   going further — GAIA Desktop will not see a scanner that 123Scan cannot.

---

## 5. Update the Zebra scanner's firmware

Bring the scanner's firmware up to the latest version before configuring it. Firmware updates
fix decoding problems and keep the scanner in step with the Zebra driver installed by the SDK.

1. Close **GAIA Desktop** and any other program that may be using the scanner: 123Scan needs
   the scanner to itself during the update.
2. In 123Scan, with the scanner connected, choose its firmware update option. 123Scan compares
   the scanner's firmware with the latest Zebra publishes and offers to download and install it.
   If the computer has no internet access, download the scanner's firmware from its support
   page on Zebra's site and load the file into 123Scan.
3. Start the update and **leave the scanner plugged in until 123Scan reports that it has
   finished**. It takes several minutes; the scanner restarts at the end. Unplugging it part-way
   can leave it unusable.
4. Check that 123Scan now shows the new firmware version.

An update can return some settings to their defaults, which is why the scanner is configured in
the next two steps, after the firmware.

---

## 6. Set the Zebra scanner's USB mode

This is the step most often missed. **Out of the box a Zebra scanner is a USB keyboard** (USB HID
Keyboard mode): it "types" each barcode into whatever has focus. In that mode GAIA Desktop's
*Connect scanner* cannot find it, and anything typed into a text field loses its FNC1
separators, which changes what a GS1 element string means.

Switch the scanner to a mode the Zebra driver talks to directly:

| Scanner USB mode | GAIA Desktop device name | Notes |
|---|---|---|
| **IBM Hand-held USB** | `ZebraUSBHandHeld`, or `ZebraAllScanners` | Recommended |
| SNAPI (with or without imaging) | `ZebraScannerSNAPI`, or `ZebraAllScanners` | Zebra's own USB protocol; also works |
| IBM Table-top USB | `ZebraUSBTableTop`, or `ZebraAllScanners` | Intended for table-top scanners |
| USB OPOS (Hand-held) | `ZebraUSBOPOS`, or `ZebraAllScanners` | |
| USB HID Keyboard (factory default) | — | **Will not connect** |

`ZebraAllScanners`, the default device name in GAIA Desktop, accepts a scanner in any of the
supported modes, so once the mode is set the default usually just works.

To change the mode, either:

- **scan the setup barcode** for the mode — *IBM Hand-held USB*, say — in the scanner's Quick
  Reference Guide or Product Reference Guide (USB Host Types / USB Device Type section); the
  scanner beeps to confirm. Or
- **use 123Scan** ([step 4](#4-install-123scan)): connect the scanner, open its configuration,
  and set the USB host type.

After changing the mode, Windows re-detects the scanner as a different kind of device; give it a
few seconds before connecting.

To return the scanner to keyboard mode later — for use with other software — scan the *USB
Keyboard (HID)* setup barcode, or restore its factory defaults.

---

## 7. Configure the Zebra scanner for GS1

GAIA Desktop parses best when the scanner tells it which kind of barcode it read.

### Transmit the AIM Code ID

Set **Transmit Code ID Character** to **AIM Code ID Character**, using the setup barcode in the
Product Reference Guide (User Preferences) or in 123Scan. Each scan then starts with its
symbology identifier — `]C1` for GS1-128, `]d2` for GS1 DataMatrix, `]Q3` for GS1 QR Code,
`]e0` for GS1 DataBar — which GAIA Desktop uses to name the data carrier and apply the rules
for it.

Without it, GAIA Desktop still parses the element string and takes the symbology from the scan
itself, but the report has no AIM Code ID to show, and a plain barcode such as an EAN-13 is
harder to tell from a GS1 element string.

### Check the symbologies

Make sure the barcode types you work with are **enabled**: typically GS1-128, GS1 DataMatrix,
GS1 DataBar, GS1 QR Code and EAN/UPC. Most are on by default on Zebra's 2D scanners; 123Scan shows the
current settings, and the Product Reference Guide has a barcode to enable each one.

### Leave Zebra's GS1 parsing features off

Recent firmware adds GS1 features that work on the scanner itself — **Label Parse+**, **GS1
Digital Link** and **GS1 Retail POS parsing**. Leave them **off**, which is how the scanner ships:
they reformat or shorten what the scanner sends, and GAIA Desktop needs the raw data to parse
and validate it. See [Supported Zebra 2D scanners](#12-supported-zebra-2d-scanners) for what each
one does.

### Leave prefixes and suffixes off

Do not add a prefix, suffix or Enter key to the scanner's output. GAIA Desktop receives each
scan as a complete message through JavaPOS and needs none of them; extra characters would end
up in the element string.

---

## 8. Install and start GAIA Desktop

1. Unzip `gaia-desktop-1.0.0-win.zip`, for example to `C:\Tools\`. It contains one folder:

   ```
   gaia-desktop-1.0.0-win\
   ├── docs\
   │   └── GaiaDesktop-Zebra-Scanner-Windows.md      this guide
   ├── gaia-desktop.jar
   ├── lib\
   ├── readme.html                                   index of every guide
   └── run.bat
   ```

2. Plug in the scanner.
3. Double-click **`run.bat`**. A console window opens beside the app and stays open: scanner
   status and JavaPOS errors are reported there.
4. Read the first line in the console. It says what `run.bat` found:

   ```
   run.bat: Java 21.0.8 from JAVA_HOME; JavaFX from lib\; JavaPOS from lib\; Zebra services from C:\Program Files\Zebra Technologies\Barcode Scanners\Scanner SDK\JPOS\
   ```

   If it ends **`Zebra services from none (Zebra Scanner SDK not installed)`**, the SDK was not
   found at its default location — install it ([step 3](#3-install-the-zebra-scanner-sdk)) or
   set [ZEBRA_HOME](#zebra_home). The app still starts, but the scanner cannot connect.

---

## 9. Connect the scanner

1. In the toolbar, the **device picker** lists the scanner names GAIA Desktop knows. Leave it on
   **`ZebraAllScanners`**, or pick the name for the scanner's USB mode from the table in
   [step 6](#6-set-the-zebra-scanners-usb-mode).
2. Click **Connect scanner**, or press **Ctrl+K** (also in the **Scanner** menu).
3. The status bar changes to **`scanner: ZebraAllScanners`** (or the name you picked), and the
   status line reports *Scanner connected*. The button now reads **Disconnect scanner**.
4. Scan a GS1 barcode. It appears at the top of the capture history and its report opens. With
   the AIM Code ID on, the report names the data carrier — *GS1-128*, *GS1 DataMatrix* — and the
   element string keeps each FNC1 separator.

The device picker remembers your choice between runs. The scanner stays connected until you
disconnect it or close the app. Scans also work in **Build** and **Combine** modes: in Combine,
each scan becomes one of the barcodes being combined.

While GAIA Desktop holds the scanner, other programs cannot use it, and the other way round:
close 123Scan and any other point-of-sale or scanner software before connecting.

---

## 10. Configuration reference

### Device names

The device picker is filled from the `jpos.xml` bundled in `gaia-desktop.jar`. Its scanner
entries:

| Device name | Scanner type | Use with |
|---|---|---|
| `ZebraAllScanners` | any | **Default.** Any supported USB mode |
| `ZebraAllScanners114` | any | The JavaPOS 1.14 version of the same service |
| `ZebraUSBHandHeld` | IBM Hand-held USB | A scanner in IBM Hand-held USB mode |
| `ZebraUSBTableTop` | IBM Table-top USB | Table-top mode |
| `ZebraUSBOPOS` | USB OPOS | USB OPOS mode |
| `ZebraScannerSNAPI` | SNAPI | SNAPI mode |
| `ZebraScannerUSB` | any USB | Any USB mode |
| `ZebraScannerSerialSSI` | serial SSI | Serial-cable scanners (not USB) |
| `ZebraScannerSerialNIXMODB` | serial NIXMODB | Serial-cable scanners |
| `ZebraVDI` | VDI | Scanners passed through to a virtual desktop |

### ZEBRA_HOME

If the Zebra SDK is installed somewhere other than the default, set `ZEBRA_HOME` to its `JPOS`
folder before starting `run.bat` — for example in a small batch file beside it:

```bat
@echo off
set "ZEBRA_HOME=D:\Zebra\Scanner SDK\JPOS\"
call "%~dp0run.bat" %*
```

`run.bat` then takes Zebra's service jars from `%ZEBRA_HOME%bin` and adds that folder to the
`PATH` for the Zebra DLLs.

### Your own jpos.xml

To add a device name or change an entry's settings, copy `jpos.xml` out of `gaia-desktop.jar`
(it is a zip; any unzip tool opens it), edit the copy, and point GAIA Desktop at it:

```bat
set GAIA_DESKTOP_OPTS=-Djpos.config.populatorFileURL=file:///C:/Tools/jpos.xml
run.bat
```

The device picker then lists the scanners in that file. With more than one Zebra scanner
attached, an entry can be tied to one of them through its `ScannerModel` or `SerialNumber`
property, as in the `ZebraAllScanners` entry.

### Other options

| Setting | How |
|---|---|
| Language of the app | `run.bat --language=FR` (this run only), or the Options panel |
| Extra Java options | `set GAIA_DESKTOP_OPTS=…` before `run.bat` |
| A bundled Java | put a `jdk-21…` folder beside `run.bat`; it is used before `JAVA_HOME` |

---

## 11. Troubleshooting

Errors appear in a dialog when connecting, in the console window, and in the log file
`logs\gaia-desktop.log`, in the folder `run.bat` was started from (the GAIA Desktop folder, when
`run.bat` is double-clicked). The number in square brackets in the log is the JavaPOS error code.

| What you see | Likely cause | What to do |
|---|---|---|
| There is no *Connect scanner* button | Not running on Windows | Scanning is Windows-only |
| 123Scan does not find the scanner | Cable, USB port, or the scanner is held by another program | Try another cable or port; close GAIA Desktop and other scanner software. GAIA Desktop will not see a scanner 123Scan cannot |
| Console: `Zebra services from none (Zebra Scanner SDK not installed)` | The SDK is not at its default location | Install the 64-bit SDK ([step 3](#3-install-the-zebra-scanner-sdk)), or set [ZEBRA_HOME](#zebra_home) |
| Error **104**: *Could not connect to service with logicalName = …: …SymScannerSvc112Factory* | Zebra's service jars were not found | As above — check the console's `Zebra services from` line |
| Error **109**: *Service does not exist in loaded JCL registry* | The device name is not in the scanner configuration, or the SDK's `javaPOS114.jar` got onto the class path | Pick a name from the device picker; do not add `javaPOS114.jar`; if you use your own `jpos.xml`, check the name is in it |
| Error **107** (no hardware), or the connect fails with the scanner plugged in | The scanner is still in USB keyboard mode, or the CoreScanner driver is not running | Set the USB mode ([step 6](#6-set-the-zebra-scanners-usb-mode)); restart Windows after installing the SDK; try another USB port |
| Error **112** (timeout) or **102** (claimed) when connecting | Another program holds the scanner | Close 123Scan and any other scanner or point-of-sale software, then connect again |
| `UnsatisfiedLinkError` … `CSJPOSScanner64` in the console | The Zebra DLLs cannot be loaded — often a 32-bit SDK with a 64-bit Java | Install the 64-bit SDK; check `java -version` reports a 64-bit VM |
| Scans type into the input bar instead of arriving as captures | The scanner is in USB keyboard mode | Set the USB mode ([step 6](#6-set-the-zebra-scanners-usb-mode)) |
| Connected, but scans do not arrive | Wrong device name for the scanner's mode, or the scanner was re-plugged while connected | Disconnect, pick `ZebraAllScanners`, connect again |
| The report shows no AIM Code ID / data carrier | AIM Code ID transmission is off | Turn on *Transmit Code ID Character → AIM Code ID* ([step 7](#7-configure-the-zebra-scanner-for-gs1)) |
| An element string is missing its separators, or AIs run together | Scanned in keyboard mode, or typed by hand without `{GS}` | Use a JavaPOS USB mode; when typing, insert separators with the `{GS}` button |
| A barcode type is never read | That symbology is disabled on the scanner | Enable it ([step 7](#7-configure-the-zebra-scanner-for-gs1)) |

If a problem persists, keep the console's `run.bat:` line, the error dialog's text and
`logs\gaia-desktop.log` together: between them they show which Java, which Zebra services and
which device name were used, and exactly where the connection failed.

---

## 12. Supported Zebra 2D scanners

GAIA Desktop needs a scanner that can read the barcodes GS1 data is printed in — GS1-128,
GS1 DataBar, EAN/UPC, and, for most uses, the 2D codes: **GS1 DataMatrix** and **GS1 QR Code**,
which is also how a **GS1 Digital Link** URI is usually printed. That means a Zebra **2D imager**:
a linear (1D) scanner cannot read DataMatrix or QR codes at all.

The scanner does not need to understand GS1 Application Identifiers or Digital Link itself.
**GAIA Desktop parses both**: it takes the scan as the scanner read it — AIM Code ID, FNC1
separators and all — and splits it into AIs, or turns a Digital Link URI into its AIs, itself.

### Tested

| Scanner | Type | Status |
|---|---|---|
| **DS8108** | Corded handheld 2D imager | **Built and tested** with GAIA Desktop |

### Expected to work

These are Zebra 2D imagers that, like the DS8108, are driven through Zebra's Scanner SDK and
JavaPOS services. They have **not been tested** with GAIA Desktop; set each up as in this guide,
using its own Quick Reference or Product Reference Guide for the setup barcodes.

| Family | Models (examples) |
|---|---|
| DS81x8 | DS8108 (corded), DS8178 (cordless) |
| DS22x8 | DS2208 (corded), DS2278 (cordless) — general purpose |
| DS36x8 | DS3608 (corded), DS3678 (cordless) — rugged, including the KD and XR variants |
| DS46x8 | DS4608 (corded), DS4678 (cordless) |
| DS9308 / DS9908 | Presentation and hands-free scanners |
| MP7000 / MP7200 | Multi-plane bioptic scanners and scanner-scales |
| CS6080 | Cordless companion scanner |

A cordless scanner talks to its cradle, and the cradle is what plugs into the computer: connect
the cradle by USB and treat the pair as one scanner. Scanners in other Zebra families may work
too, if Zebra's Scanner SDK supports them in one of the USB modes in
[step 6](#6-set-the-zebra-scanners-usb-mode).

### Zebra's on-scanner GS1 features

Separately from reading the barcodes, some Zebra 2D imagers can process GS1 data on the scanner,
configured through 123Scan:

| Feature | What it does | For GAIA Desktop |
|---|---|---|
| **Label Parse+** | Parses a GS1 label's AIs on the scanner and sends reformatted fields | **Leave off** — GAIA Desktop would no longer see the AIs, FNC1 separators and AIM Code ID |
| **GS1 Digital Link** | For retail products marked with both a 1D EAN/UPC and a 2D Digital Link code: chooses which to send | **Leave off** (the default) |
| **GS1 Retail POS parsing** | Reads 2D retail barcodes in Digital Link URI or element string syntax | **Leave off** |

The **GS1 Digital Link** setting has three modes. **Mode 1 — GTIN only**, the mode it starts in
once enabled, sends just the GTIN, whichever barcode it comes from, so a batch, expiry date or
serial number in the Digital Link would never reach GAIA Desktop. **Mode 2 — Prioritize 2D**
sends the 2D code when it is seen within the prioritization timeout (200 ms by default), else
the 1D. **Mode 3 — Corroborate 1D and 2D GTINs Match** sends the 1D, and the 2D as well when its
GTIN matches. If the setting must be on for other software, use Mode 2, not Mode 1. On the DS8108
these are parameters 2373 (on/off), 2374 (mode) and 2491 (timeout).

Using these features needs **123Scan version 6 or newer** and firmware that includes them.
Zebra has been adding GS1 Digital Link support by firmware update since April 2025; the dates
below come from a Zebra reseller, so check each model's firmware release notes:

| Model family | GS1 Digital Link firmware released |
|---|---|
| DS81x8 | 16 April 2025 |
| MP7000 | 21 April 2025 |
| DS9908 | 6 June 2025 |
| SP72 / SM72 | 11 July 2025 |
| DS46x8 | 28 July 2025 |
| MP7200 (Rev-C), DS5502 | 30 July 2025 |
| DS9308 | 29 August 2025 |
| DS22x8, DS1000 | 5 September 2025 (Asia-Pacific only) |
| DS36x8 (including KD and XR), MS47 | 30 September 2025 |
| PL5000, SE4107 | 15 October 2025 |
| CS6080 | 31 December 2025 |

None of these features is needed to read GS1 Digital Link codes into GAIA Desktop: with them
off, a Zebra 2D scanner on current firmware reads a Digital Link QR code like any other, and GAIA Desktop
converts the URI to its AIs.

Sources: Zebra, *DS8108 Product Reference Guide*, MN-002926-11EN Rev A (August 2025), chapter 12
"GS1 Digital Link Parameters" and the Data Parsing chapter; Zebra, *123Scan release notes*;
Barcode Factory, *Upgrade Zebra Scanners for GS1 Digital Link* (firmware dates).
