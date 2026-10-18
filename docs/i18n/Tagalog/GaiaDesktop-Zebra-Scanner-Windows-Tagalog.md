# Pag-set up ng Zebra scanner — GAIA Desktop para sa Windows

Paano mag-install at mag-configure ng Zebra barcode scanner para direktang mabasa ng GAIA Desktop ang mga
GS1 barcode mula rito: sinusuri ang bawat scan at idinaragdag sa history, nang buo pa rin ang AIM Code ID
prefix at bawat FNC1 separator. Orihinal na dokumento:
[GaiaDesktop-Zebra-Scanner-Windows.md](../../GaiaDesktop-Zebra-Scanner-Windows.md).

Binuo at sinubok ang GAIA Desktop gamit ang **Zebra DS8108** handheld imager na nakakonekta sa USB.
Angkop ang mga hakbang sa anumang Zebra 2D scanner na sinusuportahan ng Scanner SDK ng Zebra; ang mga
setup barcode at pangalan ng menu para sa isang partikular na modelo ay nasa sarili nitong Quick
Reference Guide o Product Reference Guide.

> **Windows lang.** Para sa Windows lang ang mga driver ng Zebra scanner, kaya walang ginagawang
> kontrol ng scanner ang GAIA Desktop sa macOS o Linux. Gumagana ang lahat ng iba pa sa app sa bawat
> platform.

---

## Nilalaman

1. [Ang mga kailangan mo](#1-ang-mga-kailangan-mo)
2. [I-install ang Java](#2-i-install-ang-java)
3. [I-install ang Zebra Scanner SDK](#3-i-install-ang-zebra-scanner-sdk)
4. [I-install ang 123Scan](#4-i-install-ang-123scan)
5. [I-update ang firmware ng Zebra scanner](#5-i-update-ang-firmware-ng-zebra-scanner)
6. [Itakda ang USB mode ng Zebra scanner](#6-itakda-ang-usb-mode-ng-zebra-scanner)
7. [I-configure ang Zebra scanner para sa GS1](#7-i-configure-ang-zebra-scanner-para-sa-gs1)
8. [I-install at simulan ang GAIA Desktop](#8-i-install-at-simulan-ang-gaia-desktop)
9. [Ikonekta ang scanner](#9-ikonekta-ang-scanner)
10. [Sanggunian sa configuration](#10-sanggunian-sa-configuration)
11. [Pag-aayos ng problema](#11-pag-aayos-ng-problema)
12. [Mga suportadong Zebra 2D scanner](#12-mga-suportadong-zebra-2d-scanner)

---

## 1. Ang mga kailangan mo

| | |
|---|---|
| **Scanner** | Isang Zebra 2D scanner, kasama ang USB cable nito — tingnan ang [Mga suportadong Zebra 2D scanner](#12-mga-suportadong-zebra-2d-scanner) |
| **Computer** | Windows 10 o 11, 64-bit |
| **Java** | Java 21 o mas bago, **64-bit** |
| **Zebra Scanner SDK for Windows** | **64-bit**, mula sa support site ng Zebra — nagbibigay ito ng driver at ng mga JavaPOS service na ginagamit ng GAIA Desktop |
| **Zebra 123Scan** | Libreng configuration utility ng Zebra, mula sa support site ng Zebra — ginagamit para i-update ang firmware ng scanner at tingnan ang mga setting nito |
| **GAIA Desktop** | `gaia-desktop-1.0.0-win.zip` |
| **Opsyonal** | Ang **Product Reference Guide** (PRG) ng scanner, na naglalaman ng mga setup barcode |

Paano nagkakabit-kabit ang mga bahagi:

```
Zebra scanner ──USB──► Zebra CoreScanner driver ──► Zebra JavaPOS services ──► GAIA Desktop
                       (installed by the SDK)        (in the SDK's JPOS\bin)    (run.bat puts them on
                                                                                 the class path)
```

Dala na ng GAIA Desktop ang JavaPOS API at ang configuration na nagpapangalan sa scanner (`jpos.xml`, nasa
loob ng `gaia-desktop.jar`). Ang Zebra SDK ang nagbibigay ng iba: ang driver na nakikipag-usap sa scanner,
at ang mga Zebra service class na nasa pagitan ng JavaPOS at ng driver na iyon.

---

## 2. I-install ang Java

Kailangan ng GAIA Desktop ang **Java 21 o mas bago**, at dapat itong **64-bit** na build, para tumugma sa
mga 64-bit na Zebra driver.

1. Mag-install ng Java 21 JDK o JRE para sa Windows x64 — halimbawa ang Eclipse Temurin mula sa
   adoptium.net. Hayaang itakda ng installer ang `JAVA_HOME` kung iaalok nito.
2. Tingnan ito sa Command Prompt:

   ```
   java -version
   ```

   Dapat ipakita ng unang linya ang bersyon 21 o mas bago; nagpi-print din ang 64-bit na Java ng `64-Bit Server VM`.

Hinahanap ng `run.bat` ang Java sa ganitong pagkakasunod: isang `jdk*` folder sa tabi ng `run.bat`,
pagkatapos ang `JAVA_HOME`, pagkatapos ang `java` sa `PATH`. Tinatanggihan nito ang anumang mas luma
kaysa 21, kasama ang mensaheng nagsasabi nito.

---

## 3. I-install ang Zebra Scanner SDK

1. I-download ang **Zebra Scanner SDK for Windows (64-bit)** mula sa support site ng Zebra.
2. Patakbuhin ang installer. Kung may pagpipilian ng mga component, siguraduhing kasama ang **JPOS**
   (JavaPOS) component.
3. Panatilihin ang default na lokasyon ng pag-install. Hinahanap ng GAIA Desktop ang JavaPOS na bahagi ng
   SDK sa:

   ```
   C:\Program Files\Zebra Technologies\Barcode Scanners\Scanner SDK\JPOS\
   ```

   at inaasahan nito ang mga service jar ng Zebra (`JposServiceScanner.jar`, `JposServiceJniScanner.jar`, …)
   at ang mga native library (`CSJPOSScanner64.dll`, …) sa `bin\` folder nito. Kung naka-install ang SDK sa
   ibang lugar, tingnan ang [ZEBRA_HOME](#zebra_home).
4. **I-restart ang Windows** pagkatapos mag-install. Tumatakbo sa background ang CoreScanner driver ng SDK
   at dapat ay tumatakbo na ito bago maabot ng GAIA Desktop ang scanner; ang pag-restart ang
   pinakasimpleng paraan para matiyak iyon.

---

## 4. I-install ang 123Scan

Ang 123Scan ay libreng utility ng Zebra para i-configure ang mga scanner nito. Ginagamit ito sa susunod na
hakbang para i-update ang firmware ng scanner, at ito ang pinakamadaling paraan para tingnan at baguhin ang
mga setting ng scanner pagkatapos.

1. I-download ang **123Scan** mula sa support site ng Zebra at patakbuhin ang installer.
2. Isaksak ang Zebra scanner at simulan ang 123Scan. Dapat nitong mahanap ang scanner at ipakita ang
   modelo, serial number at kasalukuyang bersyon ng firmware nito. Kung hindi, subukan ang ibang USB port
   o cable bago magpatuloy — hindi makikita ng GAIA Desktop ang scanner na hindi makita ng 123Scan.

---

## 5. I-update ang firmware ng Zebra scanner

Dalhin sa pinakabagong bersyon ang firmware ng scanner bago ito i-configure. Inaayos ng mga firmware update
ang mga problema sa pag-decode at pinananatiling kaayon ng scanner ang Zebra driver na ini-install ng SDK.

1. Isara ang **GAIA Desktop** at anumang ibang program na maaaring gumagamit ng scanner: kailangan ng
   123Scan na solohin ang scanner habang nag-a-update.
2. Sa 123Scan, habang nakakonekta ang scanner, piliin ang firmware update option nito. Ikinukumpara ng
   123Scan ang firmware ng scanner sa pinakabagong inilalabas ng Zebra at nag-aalok na i-download at
   i-install ito. Kung walang internet ang computer, i-download ang firmware ng scanner mula sa support page
   nito sa site ng Zebra at i-load ang file sa 123Scan.
3. Simulan ang update at **huwag tanggalin sa saksakan ang scanner hangga't hindi sinasabi ng 123Scan na
   tapos na ito**. Ilang minuto ito; magre-restart ang scanner sa dulo. Kapag tinanggal ito sa gitna, maaari
   itong hindi na magamit.
4. Tiyaking ipinapakita na ngayon ng 123Scan ang bagong bersyon ng firmware.

Maaaring ibalik ng update ang ilang setting sa default, kaya kino-configure ang scanner sa susunod na
dalawang hakbang, pagkatapos ng firmware.

---

## 6. Itakda ang USB mode ng Zebra scanner

Ito ang hakbang na pinakamadalas makaligtaan. **Paglabas sa kahon, USB keyboard ang Zebra scanner** (USB HID
Keyboard mode): "itina-type" nito ang bawat barcode kung saan man naroon ang focus. Sa mode na iyon, hindi
ito mahanap ng *Ikonekta ang scanner* ng GAIA Desktop, at nawawala ang mga FNC1 separator ng anumang
itina-type sa text field, na nagpapabago sa kahulugan ng isang GS1 element string.

Ilipat ang scanner sa mode na direktang kinakausap ng Zebra driver:

| USB mode ng scanner | Pangalan ng device sa GAIA Desktop | Mga tala |
|---|---|---|
| **IBM Hand-held USB** | `ZebraUSBHandHeld`, o `ZebraAllScanners` | Inirerekomenda |
| SNAPI (may imaging o wala) | `ZebraScannerSNAPI`, o `ZebraAllScanners` | Sariling USB protocol ng Zebra; gumagana rin |
| IBM Table-top USB | `ZebraUSBTableTop`, o `ZebraAllScanners` | Para sa mga table-top na scanner |
| USB OPOS (Hand-held) | `ZebraUSBOPOS`, o `ZebraAllScanners` | |
| USB HID Keyboard (factory default) | — | **Hindi kokonekta** |

Ang `ZebraAllScanners`, ang default na pangalan ng device sa GAIA Desktop, ay tumatanggap ng scanner sa
alinman sa mga suportadong mode, kaya kapag naitakda na ang mode, karaniwang gumagana na agad ang default.

Para baguhin ang mode, alinman sa:

- **i-scan ang setup barcode** para sa mode — halimbawa *IBM Hand-held USB* — sa Quick Reference Guide o
  Product Reference Guide ng scanner (seksiyong USB Host Types / USB Device Type); magbi-beep ang scanner
  bilang kumpirmasyon. O
- **gamitin ang 123Scan** ([hakbang 4](#4-i-install-ang-123scan)): ikonekta ang scanner, buksan ang
  configuration nito, at itakda ang USB host type.

Pagkatapos baguhin ang mode, muling dine-detect ng Windows ang scanner bilang ibang uri ng device; maghintay
ng ilang segundo bago kumonekta.

Para ibalik sa keyboard mode ang scanner sa ibang pagkakataon — para gamitin sa ibang software — i-scan ang
*USB Keyboard (HID)* na setup barcode, o i-restore ang factory defaults nito.

---

## 7. I-configure ang Zebra scanner para sa GS1

Pinakamahusay na nagsusuri ang GAIA Desktop kapag sinasabi ng scanner kung anong uri ng barcode ang nabasa
nito.

### Ipadala ang AIM Code ID

Itakda ang **Transmit Code ID Character** sa **AIM Code ID Character**, gamit ang setup barcode sa Product
Reference Guide (User Preferences) o sa 123Scan. Pagkatapos ay nagsisimula ang bawat scan sa symbology
identifier nito — `]C1` para sa GS1-128, `]d2` para sa GS1 DataMatrix, `]Q3` para sa GS1 QR Code, `]e0`
para sa GS1 DataBar — na ginagamit ng GAIA Desktop para pangalanan ang tagadala ng datos at ilapat ang mga
patakaran para dito.

Kung wala ito, sinusuri pa rin ng GAIA Desktop ang element string at kinukuha ang symbology mula sa scan
mismo, pero walang AIM Code ID na maipapakita ang report, at mas mahirap ihiwalay ang isang simpleng barcode
gaya ng EAN-13 sa isang GS1 element string.

### Tingnan ang mga symbology

Siguraduhing **naka-enable** ang mga uri ng barcode na ginagamit mo: karaniwan ay GS1-128, GS1 DataMatrix,
GS1 DataBar, GS1 QR Code at EAN/UPC. Karamihan ay naka-on bilang default sa mga 2D scanner ng Zebra;
ipinapakita ng 123Scan ang kasalukuyang mga setting, at may barcode ang Product Reference Guide para i-enable
ang bawat isa.

### Panatilihing naka-off ang mga GS1 parsing feature ng Zebra

Nagdaragdag ang bagong firmware ng mga GS1 feature na gumagana sa scanner mismo — **Label Parse+**, **GS1
Digital Link** at **GS1 Retail POS parsing**. Panatilihin silang **naka-off**, gaya ng pagkadating ng
scanner: binabago nila ang format o pinaiikli ang ipinapadala ng scanner, samantalang kailangan ng
GAIA Desktop ang raw na datos para suriin at i-validate ito. Tingnan ang
[Mga suportadong Zebra 2D scanner](#12-mga-suportadong-zebra-2d-scanner) kung ano ang ginagawa ng bawat isa.

### Walang prefix at suffix

Huwag magdagdag ng prefix, suffix o Enter key sa output ng scanner. Tinatanggap ng GAIA Desktop ang bawat
scan bilang isang buong mensahe sa pamamagitan ng JavaPOS at hindi nito kailangan ang alinman sa mga iyon;
mapupunta lang sa element string ang mga sobrang character.

---

## 8. I-install at simulan ang GAIA Desktop

1. I-unzip ang `gaia-desktop-1.0.0-win.zip`, halimbawa sa `C:\Tools\`. May isang folder ito:

   ```
   gaia-desktop-1.0.0-win\
   ├── docs\
   │   └── GaiaDesktop-Zebra-Scanner-Windows.md      this guide
   ├── gaia-desktop.jar
   ├── lib\
   ├── readme.html                                   index of every guide
   └── run.bat
   ```

2. Isaksak ang scanner.
3. I-double-click ang **`run.bat`**. May bubukas na console window sa tabi ng app at mananatili itong
   bukas: doon iniuulat ang status ng scanner at mga error ng JavaPOS.
4. Basahin ang unang linya sa console. Sinasabi nito kung ano ang nahanap ng `run.bat`:

   ```
   run.bat: Java 21.0.8 from JAVA_HOME; JavaFX from lib\; JavaPOS from lib\; Zebra services from C:\Program Files\Zebra Technologies\Barcode Scanners\Scanner SDK\JPOS\
   ```

   Kung nagtatapos ito sa **`Zebra services from none (Zebra Scanner SDK not installed)`**, hindi nahanap
   ang SDK sa default na lokasyon nito — i-install ito ([hakbang 3](#3-i-install-ang-zebra-scanner-sdk)) o
   itakda ang [ZEBRA_HOME](#zebra_home). Magsisimula pa rin ang app, pero hindi makakakonekta ang scanner.

---

## 9. Ikonekta ang scanner

1. Sa toolbar, inililista ng **device picker** ang mga pangalan ng scanner na kilala ng GAIA Desktop.
   Iwanan ito sa **`ZebraAllScanners`**, o piliin ang pangalan para sa USB mode ng scanner mula sa
   talahanayan sa [hakbang 6](#6-itakda-ang-usb-mode-ng-zebra-scanner).
2. I-click ang **Ikonekta ang scanner**, o pindutin ang **Ctrl+K** (nasa menu na **Scanner** din).
3. Magiging **`scanner: ZebraAllScanners`** (o ang pangalang pinili mo) ang status bar, at iuulat ng status
   line ang *Nakakonekta ang scanner — ZebraAllScanners*. Ang nakasulat na ngayon sa button ay
   **Idiskonekta ang scanner**.
4. Mag-scan ng isang GS1 barcode. Lalabas ito sa itaas ng capture history at bubukas ang report nito. Kapag
   naka-on ang AIM Code ID, pinangangalanan ng report ang tagadala ng datos — *GS1-128*, *GS1 DataMatrix* —
   at pinananatili ng element string ang bawat FNC1 separator.

Tinatandaan ng device picker ang pinili mo sa pagitan ng mga pagtakbo. Nananatiling nakakonekta ang scanner
hanggang idiskonekta mo ito o isara ang app. Gumagana rin ang mga scan sa mga mode na **Bumuo** at
**Pagsamahin**: sa Pagsamahin, nagiging isa sa mga barcode na pinagsasama ang bawat scan.

Habang hawak ng GAIA Desktop ang scanner, hindi ito magagamit ng ibang program, at ganoon din sa kabila:
isara ang 123Scan at anumang ibang point-of-sale o scanner software bago kumonekta.

---

## 10. Sanggunian sa configuration

### Mga pangalan ng device

Pinupuno ang device picker mula sa `jpos.xml` na kasama sa `gaia-desktop.jar`. Ang mga entry nito para sa
scanner:

| Pangalan ng device | Uri ng scanner | Gamitin sa |
|---|---|---|
| `ZebraAllScanners` | anuman | **Default.** Anumang suportadong USB mode |
| `ZebraAllScanners114` | anuman | Ang JavaPOS 1.14 na bersyon ng parehong service |
| `ZebraUSBHandHeld` | IBM Hand-held USB | Scanner na nasa IBM Hand-held USB mode |
| `ZebraUSBTableTop` | IBM Table-top USB | Table-top mode |
| `ZebraUSBOPOS` | USB OPOS | USB OPOS mode |
| `ZebraScannerSNAPI` | SNAPI | SNAPI mode |
| `ZebraScannerUSB` | anumang USB | Anumang USB mode |
| `ZebraScannerSerialSSI` | serial SSI | Mga scanner na naka-serial cable (hindi USB) |
| `ZebraScannerSerialNIXMODB` | serial NIXMODB | Mga scanner na naka-serial cable |
| `ZebraVDI` | VDI | Mga scanner na ipinapasa sa virtual desktop |

### ZEBRA_HOME

Kung naka-install ang Zebra SDK sa ibang lugar maliban sa default, itakda ang `ZEBRA_HOME` sa `JPOS` folder
nito bago simulan ang `run.bat` — halimbawa sa isang maliit na batch file sa tabi nito:

```bat
@echo off
set "ZEBRA_HOME=D:\Zebra\Scanner SDK\JPOS\"
call "%~dp0run.bat" %*
```

Kukunin noon ng `run.bat` ang mga service jar ng Zebra mula sa `%ZEBRA_HOME%bin` at idaragdag ang folder na
iyon sa `PATH` para sa mga Zebra DLL.

### Sariling jpos.xml

Para magdagdag ng pangalan ng device o baguhin ang mga setting ng isang entry, kopyahin ang `jpos.xml` mula
sa `gaia-desktop.jar` (zip ito; mabubuksan ng anumang unzip tool), i-edit ang kopya, at ituro ang
GAIA Desktop dito:

```bat
set GAIA_DESKTOP_OPTS=-Djpos.config.populatorFileURL=file:///C:/Tools/jpos.xml
run.bat
```

Ililista noon ng device picker ang mga scanner sa file na iyon. Kapag higit sa isang Zebra scanner ang
nakakabit, maaaring itali ang isang entry sa isa sa kanila sa pamamagitan ng `ScannerModel` o
`SerialNumber` na property nito, gaya ng sa entry na `ZebraAllScanners`.

### Iba pang opsyon

| Setting | Paano |
|---|---|
| Wika ng app | `run.bat --language=FR` (sa pagtakbong ito lang), o ang Panel ng mga opsyon |
| Dagdag na opsyon sa Java | `set GAIA_DESKTOP_OPTS=…` bago ang `run.bat` |
| Kasamang Java | maglagay ng `jdk-21…` folder sa tabi ng `run.bat`; ginagamit ito bago ang `JAVA_HOME` |

---

## 11. Pag-aayos ng problema

Lumalabas ang mga error sa isang dialog kapag kumokonekta, sa console window, at sa log file
`logs\gaia-desktop.log`, sa folder kung saan sinimulan ang `run.bat` (ang folder ng GAIA Desktop, kapag
dinobleng-click ang `run.bat`). Ang numero sa loob ng square bracket sa log ay ang JavaPOS error code.

| Ang nakikita mo | Malamang na sanhi | Ang gagawin |
|---|---|---|
| Walang button na *Ikonekta ang scanner* | Hindi tumatakbo sa Windows | Sa Windows lang ang pag-scan |
| Hindi mahanap ng 123Scan ang scanner | Cable, USB port, o hawak ng ibang program ang scanner | Subukan ang ibang cable o port; isara ang GAIA Desktop at ibang scanner software. Hindi makikita ng GAIA Desktop ang scanner na hindi makita ng 123Scan |
| Console: `Zebra services from none (Zebra Scanner SDK not installed)` | Wala ang SDK sa default na lokasyon nito | I-install ang 64-bit na SDK ([hakbang 3](#3-i-install-ang-zebra-scanner-sdk)), o itakda ang [ZEBRA_HOME](#zebra_home) |
| Error **104**: *Could not connect to service with logicalName = …: …SymScannerSvc112Factory* | Hindi nahanap ang mga service jar ng Zebra | Gaya sa itaas — tingnan ang linyang `Zebra services from` sa console |
| Error **109**: *Service does not exist in loaded JCL registry* | Wala sa configuration ng scanner ang pangalan ng device, o napunta sa class path ang `javaPOS114.jar` ng SDK | Pumili ng pangalan mula sa device picker; huwag idagdag ang `javaPOS114.jar`; kung sariling `jpos.xml` ang gamit mo, tiyaking nandoon ang pangalan |
| Error **107** (walang hardware), o pumapalya ang pagkonekta kahit nakasaksak ang scanner | Nasa USB keyboard mode pa ang scanner, o hindi tumatakbo ang CoreScanner driver | Itakda ang USB mode ([hakbang 6](#6-itakda-ang-usb-mode-ng-zebra-scanner)); i-restart ang Windows pagkatapos i-install ang SDK; subukan ang ibang USB port |
| Error **112** (timeout) o **102** (claimed) kapag kumokonekta | May ibang program na humahawak sa scanner | Isara ang 123Scan at anumang ibang scanner o point-of-sale software, saka kumonekta ulit |
| `UnsatisfiedLinkError` … `CSJPOSScanner64` sa console | Hindi ma-load ang mga Zebra DLL — kadalasan ay 32-bit na SDK na may 64-bit na Java | I-install ang 64-bit na SDK; tiyaking 64-bit na VM ang iniuulat ng `java -version` |
| Naita-type ang mga scan sa input bar sa halip na dumating bilang mga capture | Nasa USB keyboard mode ang scanner | Itakda ang USB mode ([hakbang 6](#6-itakda-ang-usb-mode-ng-zebra-scanner)) |
| Nakakonekta, pero hindi dumarating ang mga scan | Maling pangalan ng device para sa mode ng scanner, o isinaksak ulit ang scanner habang nakakonekta | Idiskonekta, piliin ang `ZebraAllScanners`, kumonekta ulit |
| Walang ipinapakitang AIM Code ID / tagadala ng datos ang report | Naka-off ang pagpapadala ng AIM Code ID | I-on ang *Transmit Code ID Character → AIM Code ID* ([hakbang 7](#7-i-configure-ang-zebra-scanner-para-sa-gs1)) |
| Nawawala ang mga separator ng isang element string, o nagdikit-dikit ang mga AI | Na-scan sa keyboard mode, o mano-manong na-type nang walang `{GS}` | Gumamit ng JavaPOS USB mode; kapag nagta-type, maglagay ng separator gamit ang button na `{GS}` |
| May isang uri ng barcode na hindi kailanman nababasa | Naka-disable ang symbology na iyon sa scanner | I-enable ito ([hakbang 7](#7-i-configure-ang-zebra-scanner-para-sa-gs1)) |

Kung magpatuloy ang problema, itabi nang magkakasama ang linyang `run.bat:` sa console, ang teksto ng error
dialog at ang `logs\gaia-desktop.log`: magkakasama, ipinapakita nila kung aling Java, aling mga Zebra service
at aling pangalan ng device ang ginamit, at kung saan eksaktong pumalya ang koneksiyon.

---

## 12. Mga suportadong Zebra 2D scanner

Kailangan ng GAIA Desktop ng scanner na nakababasa ng mga barcode kung saan iniimprenta ang datos ng GS1 —
GS1-128, GS1 DataBar, EAN/UPC, at, para sa karamihan ng gamit, ang mga 2D code: **GS1 DataMatrix** at
**GS1 QR Code**, na siya ring karaniwang paraan ng pag-imprenta ng isang **GS1 Digital Link** URI. Ibig
sabihin, isang Zebra **2D imager**: hindi talaga kayang basahin ng linear (1D) scanner ang mga DataMatrix o
QR code.

Hindi kailangang maintindihan mismo ng scanner ang GS1 Application Identifiers o Digital Link.
**Sinusuri ng GAIA Desktop ang dalawa**: kinukuha nito ang scan kung paano ito nabasa ng scanner — AIM Code ID,
mga FNC1 separator at lahat — at ito mismo ang naghahati dito sa mga AI, o nagko-convert ng Digital Link URI
sa mga AI nito.

### Nasubok na

| Scanner | Uri | Katayuan |
|---|---|---|
| **DS8108** | Corded handheld 2D imager | **Binuo at sinubok** kasama ang GAIA Desktop |

### Inaasahang gagana

Ito ang mga Zebra 2D imager na, gaya ng DS8108, ay pinapatakbo sa pamamagitan ng Scanner SDK at mga JavaPOS
service ng Zebra. **Hindi pa sila nasusubok** kasama ang GAIA Desktop; i-set up ang bawat isa ayon sa gabay
na ito, gamit ang sarili nitong Quick Reference Guide o Product Reference Guide para sa mga setup barcode.

| Pamilya | Mga modelo (halimbawa) |
|---|---|
| DS81x8 | DS8108 (may cord), DS8178 (cordless) |
| DS22x8 | DS2208 (may cord), DS2278 (cordless) — pangkalahatang gamit |
| DS36x8 | DS3608 (may cord), DS3678 (cordless) — matibay, kasama ang mga variant na KD at XR |
| DS46x8 | DS4608 (may cord), DS4678 (cordless) |
| DS9308 / DS9908 | Mga presentation at hands-free na scanner |
| MP7000 / MP7200 | Mga multi-plane bioptic scanner at scanner-scale |
| CS6080 | Cordless na companion scanner |

Nakikipag-usap ang cordless na scanner sa cradle nito, at ang cradle ang isinasaksak sa computer:
ikonekta ang cradle sa USB at ituring ang pares bilang iisang scanner. Maaari ring gumana ang mga scanner sa
ibang pamilya ng Zebra, kung sinusuportahan sila ng Scanner SDK ng Zebra sa isa sa mga USB mode sa
[hakbang 6](#6-itakda-ang-usb-mode-ng-zebra-scanner).

### Mga GS1 feature sa mismong Zebra scanner

Bukod sa pagbasa ng mga barcode, kayang iproseso ng ilang Zebra 2D imager ang datos ng GS1 sa scanner mismo,
na kino-configure sa pamamagitan ng 123Scan:

| Feature | Ang ginagawa nito | Para sa GAIA Desktop |
|---|---|---|
| **Label Parse+** | Sinusuri ang mga AI ng isang GS1 label sa scanner at nagpapadala ng mga field na binago ang format | **Panatilihing naka-off** — hindi na makikita ng GAIA Desktop ang mga AI, FNC1 separator at AIM Code ID |
| **GS1 Digital Link** | Para sa mga retail product na may parehong 1D EAN/UPC at 2D Digital Link code: pinipili kung alin ang ipapadala | **Panatilihing naka-off** (ang default) |
| **GS1 Retail POS parsing** | Binabasa ang mga 2D retail barcode sa syntax ng Digital Link URI o element string | **Panatilihing naka-off** |

May tatlong mode ang setting na **GS1 Digital Link**. Ang **Mode 1 — GTIN only**, ang mode na
pinagsisimulan nito kapag na-enable, ay nagpapadala lang ng GTIN, saanmang barcode ito galing, kaya hindi
kailanman aabot sa GAIA Desktop ang batch, petsa ng expiry o serial number sa Digital Link. Ang **Mode 2 —
Prioritize 2D** ay nagpapadala ng 2D code kapag nakita ito sa loob ng prioritization timeout (200 ms bilang
default), kung hindi ay ang 1D. Ang **Mode 3 — Corroborate 1D and 2D GTINs Match** ay nagpapadala ng 1D, at
ng 2D din kapag tugma ang GTIN nito. Kung kailangang naka-on ang setting para sa ibang software, gamitin ang
Mode 2, hindi ang Mode 1. Sa DS8108, ito ang mga parameter na 2373 (on/off), 2374 (mode) at 2491 (timeout).

Kailangan ng **123Scan na bersyon 6 o mas bago** at firmware na may mga feature na ito para magamit sila.
Nagdaragdag ang Zebra ng suporta sa GS1 Digital Link sa pamamagitan ng firmware update mula Abril 2025; ang
mga petsa sa ibaba ay mula sa isang Zebra reseller, kaya tingnan ang firmware release notes ng bawat modelo:

| Pamilya ng modelo | Paglabas ng GS1 Digital Link firmware |
|---|---|
| DS81x8 | 16 Abril 2025 |
| MP7000 | 21 Abril 2025 |
| DS9908 | 6 Hunyo 2025 |
| SP72 / SM72 | 11 Hulyo 2025 |
| DS46x8 | 28 Hulyo 2025 |
| MP7200 (Rev-C), DS5502 | 30 Hulyo 2025 |
| DS9308 | 29 Agosto 2025 |
| DS22x8, DS1000 | 5 Setyembre 2025 (Asia-Pacific lang) |
| DS36x8 (kasama ang KD at XR), MS47 | 30 Setyembre 2025 |
| PL5000, SE4107 | 15 Oktubre 2025 |
| CS6080 | 31 Disyembre 2025 |

Wala sa mga feature na ito ang kailangan para mabasa ang mga GS1 Digital Link code sa GAIA Desktop: kapag
naka-off ang mga ito, binabasa ng Zebra 2D scanner na may kasalukuyang firmware ang isang Digital Link QR code
gaya ng iba pa, at kino-convert ng GAIA Desktop ang URI sa mga AI nito.

Mga pinagkunan: Zebra, *DS8108 Product Reference Guide*, MN-002926-11EN Rev A (Agosto 2025), kabanata 12
"GS1 Digital Link Parameters" at ang kabanatang Data Parsing; Zebra, *123Scan release notes*;
Barcode Factory, *Upgrade Zebra Scanners for GS1 Digital Link* (mga petsa ng firmware).
