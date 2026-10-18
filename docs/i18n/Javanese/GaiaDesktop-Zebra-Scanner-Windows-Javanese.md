# Nyetel scanner Zebra — GAIA Desktop kanggo Windows

Carane masang lan ngatur scanner kode bar Zebra supaya GAIA Desktop bisa maca kode bar GS1 langsung saka
scanner kasebut: saben pindaian diurai lan ditambahake menyang riwayat, kanthi awalan AIM Code ID lan saben
pemisah FNC1 tetep wutuh. Dokumen asline:
[GaiaDesktop-Zebra-Scanner-Windows.md](../../GaiaDesktop-Zebra-Scanner-Windows.md).

GAIA Desktop digawe lan diuji nganggo imager genggam **Zebra DS8108** sing disambungake liwat USB.
Langkah-langkah iki bisa dienggo kanggo scanner 2D Zebra apa wae sing didhukung Scanner SDK saka Zebra;
kode bar setelan lan jeneng menu kanggo model tartamtu ana ing Quick Reference Guide utawa Product
Reference Guide model kasebut.

> **Mung Windows.** Driver scanner Zebra mung ana kanggo Windows, mula GAIA Desktop ora gawe kontrol
> scanner apa-apa ing macOS utawa Linux. Kabeh liyane ing aplikasi iki mlaku ing saben platform.

---

## Isi

1. [Apa sing kok butuhake](#1-apa-sing-kok-butuhake)
2. [Pasang Java](#2-pasang-java)
3. [Pasang Zebra Scanner SDK](#3-pasang-zebra-scanner-sdk)
4. [Pasang 123Scan](#4-pasang-123scan)
5. [Anyari firmware scanner Zebra](#5-anyari-firmware-scanner-zebra)
6. [Setel mode USB scanner Zebra](#6-setel-mode-usb-scanner-zebra)
7. [Atur scanner Zebra kanggo GS1](#7-atur-scanner-zebra-kanggo-gs1)
8. [Pasang lan bukak GAIA Desktop](#8-pasang-lan-bukak-gaia-desktop)
9. [Sambungake scanner](#9-sambungake-scanner)
10. [Referensi konfigurasi](#10-referensi-konfigurasi)
11. [Ngatasi masalah](#11-ngatasi-masalah)
12. [Scanner 2D Zebra sing didhukung](#12-scanner-2d-zebra-sing-didhukung)

---

## 1. Apa sing kok butuhake

| | |
|---|---|
| **Scanner** | Scanner 2D Zebra, sakkabel USB-ne — deleng [Scanner 2D Zebra sing didhukung](#12-scanner-2d-zebra-sing-didhukung) |
| **Komputer** | Windows 10 utawa 11, 64-bit |
| **Java** | Java 21 utawa sing luwih anyar, **64-bit** |
| **Zebra Scanner SDK for Windows** | **64-bit**, saka situs dhukungan Zebra — nyedhiyakake driver lan layanan JavaPOS sing dienggo GAIA Desktop |
| **Zebra 123Scan** | Piranti konfigurasi gratis saka Zebra, saka situs dhukungan Zebra — dienggo nganyari firmware scanner lan mriksa setelane |
| **GAIA Desktop** | `gaia-desktop-1.0.0-win.zip` |
| **Pilihan** | **Product Reference Guide** (PRG) scanner, sing isine kode bar setelan |

Kepiye bageyan-bageyane nyambung:

```
Zebra scanner ──USB──► Zebra CoreScanner driver ──► Zebra JavaPOS services ──► GAIA Desktop
                       (installed by the SDK)        (in the SDK's JPOS\bin)    (run.bat puts them on
                                                                                 the class path)
```

GAIA Desktop wis nggawa dhewe API JavaPOS lan konfigurasi sing menehi jeneng scanner (`jpos.xml`, ing
njero `gaia-desktop.jar`). Zebra SDK nyedhiyakake sisane: driver sing omong-omongan karo scanner, lan
kelas layanan Zebra sing ana ing antarane JavaPOS lan driver kasebut.

---

## 2. Pasang Java

GAIA Desktop butuh **Java 21 utawa sing luwih anyar**, lan kudu build **64-bit**, supaya cocog karo driver
Zebra 64-bit.

1. Pasang JDK utawa JRE Java 21 kanggo Windows x64 — upamane Eclipse Temurin saka
   adoptium.net. Jarna installer nyetel `JAVA_HOME` yen dheweke nawakake.
2. Priksa ing Command Prompt:

   ```
   java -version
   ```

   Baris kapisan kudu nuduhake versi 21 utawa luwih anyar; Java 64-bit uga nyithak `64-Bit Server VM`.

`run.bat` nggoleki Java kanthi urutan iki: folder `jdk*` ing sandhinge `run.bat`, banjur `JAVA_HOME`,
banjur `java` ing `PATH`. Apa wae sing luwih lawas tinimbang 21 ditolak kanthi pesen sing nerangake.

---

## 3. Pasang Zebra Scanner SDK

1. Undhuh **Zebra Scanner SDK for Windows (64-bit)** saka situs dhukungan Zebra.
2. Bukak installer. Yen ana pilihan komponen, priksa manawa komponen **JPOS** (JavaPOS) kalebu.
3. Tetepake lokasi pemasangan gawan. GAIA Desktop nggoleki bageyan JavaPOS saka SDK ing:

   ```
   C:\Program Files\Zebra Technologies\Barcode Scanners\Scanner SDK\JPOS\
   ```

   lan ngarepake jar layanan Zebra (`JposServiceScanner.jar`, `JposServiceJniScanner.jar`, …) lan pustaka
   native (`CSJPOSScanner64.dll`, …) ana ing folder `bin\`-e. Yen SDK dipasang ing papan liya, deleng
   [ZEBRA_HOME](#zebra_home).
4. **Wiwiti maneh Windows** sawise masang. Driver CoreScanner saka SDK mlaku ing latar mburi lan kudu wis
   mlaku sadurunge GAIA Desktop bisa tekan scanner; miwiti maneh iku cara sing paling gampang kanggo
   mesthekake.

---

## 4. Pasang 123Scan

123Scan iku piranti gratis saka Zebra kanggo ngatur scanner-scannere. Piranti iki dienggo ing langkah
sabanjure kanggo nganyari firmware scanner, lan dadi cara sing paling gampang kanggo mriksa lan ngganti
setelan scanner sabanjure.

1. Undhuh **123Scan** saka situs dhukungan Zebra lan bukak installer-e.
2. Colokake scanner Zebra lan bukak 123Scan. Mesthine bisa nemokake scanner lan nuduhake model, nomer
   seri lan versi firmware saiki. Yen ora, jajal port USB utawa kabel liya sadurunge nerusake — GAIA Desktop
   ora bakal weruh scanner sing ora katon ing 123Scan.

---

## 5. Anyari firmware scanner Zebra

Anyari firmware scanner nganti versi paling anyar sadurunge ngatur. Nganyari firmware ndandani masalah
dekode lan njaga scanner tetep selaras karo driver Zebra sing dipasang SDK.

1. Tutup **GAIA Desktop** lan program liya sing bisa wae lagi nganggo scanner: 123Scan butuh scanner
   kanggo awake dhewe nalika nganyari.
2. Ing 123Scan, nalika scanner kasambung, pilih opsi nganyari firmware-e. 123Scan mbandhingake firmware
   scanner karo sing paling anyar saka Zebra lan nawakake kanggo ngundhuh lan masang. Yen komputer ora
   duwe internet, undhuh firmware scanner saka kaca dhukungane ing situs Zebra lan muat berkase menyang
   123Scan.
3. Wiwiti nganyari lan **aja nyopot scanner nganti 123Scan ngabari yen wis rampung**. Butuh sawetara
   menit; scanner bakal miwiti maneh ing pungkasan. Yen dicopot ing tengah dalan, bisa-bisa scanner ora
   bisa dienggo.
4. Priksa manawa 123Scan saiki nuduhake versi firmware sing anyar.

Nganyari bisa mbalekake sawetara setelan menyang gawan, mula scanner diatur ing rong langkah sabanjure,
sawise firmware.

---

## 6. Setel mode USB scanner Zebra

Iki langkah sing paling kerep keliwatan. **Saka pabrik, scanner Zebra iku keyboard USB** (mode USB HID
Keyboard): dheweke "ngetik" saben kode bar menyang ngendi wae fokus ana. Ing mode kuwi, *Sambungake
scanner* ing GAIA Desktop ora bisa nemokake, lan apa wae sing diketik menyang kolom teks kelangan pemisah
FNC1-ne, sing ngowahi teges untaian elemen GS1.

Alihake scanner menyang mode sing langsung diajak omong driver Zebra:

| Mode USB scanner | Jeneng piranti ing GAIA Desktop | Cathetan |
|---|---|---|
| **IBM Hand-held USB** | `ZebraUSBHandHeld`, utawa `ZebraAllScanners` | Disaranake |
| SNAPI (nganggo utawa tanpa imaging) | `ZebraScannerSNAPI`, utawa `ZebraAllScanners` | Protokol USB duweke Zebra dhewe; uga bisa |
| IBM Table-top USB | `ZebraUSBTableTop`, utawa `ZebraAllScanners` | Kanggo scanner meja |
| USB OPOS (Hand-held) | `ZebraUSBOPOS`, utawa `ZebraAllScanners` | |
| USB HID Keyboard (gawan pabrik) | — | **Ora bakal nyambung** |

`ZebraAllScanners`, jeneng piranti gawan ing GAIA Desktop, nampa scanner ing mode apa wae sing didhukung,
mula yen mode wis disetel, gawane biasane langsung bisa.

Kanggo ngganti mode, salah siji:

- **pindai kode bar setelan** kanggo mode kasebut — upamane *IBM Hand-held USB* — ing Quick Reference Guide
  utawa Product Reference Guide scanner (bagean USB Host Types / USB Device Type); scanner muni bip minangka
  konfirmasi. Utawa
- **nganggo 123Scan** ([langkah 4](#4-pasang-123scan)): sambungake scanner, bukak konfigurasine, lan setel
  jinis host USB.

Sawise mode diganti, Windows ndeteksi maneh scanner minangka jinis piranti liya; enteni sawetara detik
sadurunge nyambungake.

Kanggo mbalekake scanner menyang mode keyboard mengko — kanggo dienggo karo piranti lunak liya — pindai kode
bar setelan *USB Keyboard (HID)*, utawa balekake setelan pabrike.

---

## 7. Atur scanner Zebra kanggo GS1

GAIA Desktop ngurai paling apik yen scanner ngabari jinis kode bar apa sing diwaca.

### Kirim AIM Code ID

Setel **Transmit Code ID Character** dadi **AIM Code ID Character**, nganggo kode bar setelan ing Product
Reference Guide (User Preferences) utawa ing 123Scan. Banjur saben pindaian diwiwiti karo pengenal
simbologine — `]C1` kanggo GS1-128, `]d2` kanggo GS1 DataMatrix, `]Q3` kanggo GS1 QR Code, `]e0` kanggo
GS1 DataBar — sing dienggo GAIA Desktop kanggo menehi jeneng pembawa data lan ngetrapake aturane.

Tanpa iku, GAIA Desktop tetep ngurai untaian elemen lan njupuk simbologi saka pindaian dhewe, nanging
laporan ora duwe AIM Code ID kanggo dituduhake, lan kode bar biasa kaya EAN-13 luwih angel dibedakake karo
untaian elemen GS1.

### Priksa simbologi

Priksa manawa jinis kode bar sing kok enggo wis **diaktifake**: biasane GS1-128, GS1 DataMatrix,
GS1 DataBar, GS1 QR Code lan EAN/UPC. Akeh-akehe wis urip kanthi gawan ing scanner 2D Zebra; 123Scan
nuduhake setelan saiki, lan Product Reference Guide duwe kode bar kanggo ngaktifake saben-saben.

### Pateni fitur pangurai GS1 saka Zebra

Firmware anyar nambahi fitur GS1 sing mlaku ing scanner dhewe — **Label Parse+**, **GS1 Digital Link**
lan **GS1 Retail POS parsing**. Jarna kabeh **mati**, kaya nalika scanner dikirim: fitur-fitur kuwi
ngowahi format utawa nyendhakake apa sing dikirim scanner, dene GAIA Desktop butuh data mentah kanggo ngurai
lan validasi. Deleng [Scanner 2D Zebra sing didhukung](#12-scanner-2d-zebra-sing-didhukung) kanggo apa sing
ditindakake saben fitur.

### Aja nganggo awalan lan akhiran

Aja nambahi awalan, akhiran utawa tombol Enter ing output scanner. GAIA Desktop nampa saben pindaian minangka
pesen wutuh liwat JavaPOS lan ora butuh siji wae saka kuwi; karakter tambahan mung bakal mlebu menyang
untaian elemen.

---

## 8. Pasang lan bukak GAIA Desktop

1. Ekstrak `gaia-desktop-1.0.0-win.zip`, upamane menyang `C:\Tools\`. Isine siji folder:

   ```
   gaia-desktop-1.0.0-win\
   ├── docs\
   │   └── GaiaDesktop-Zebra-Scanner-Windows.md      this guide
   ├── gaia-desktop.jar
   ├── lib\
   ├── readme.html                                   index of every guide
   └── run.bat
   ```

2. Colokake scanner.
3. Klik kaping pindho **`run.bat`**. Jendhela console bakal kebukak ing sandhinge aplikasi lan tetep
   kebukak: status scanner lan kesalahan JavaPOS dilaporake ing kono.
4. Waca baris kapisan ing console. Baris kuwi ngandhani apa sing ditemokake `run.bat`:

   ```
   run.bat: Java 21.0.8 from JAVA_HOME; JavaFX from lib\; JavaPOS from lib\; Zebra services from C:\Program Files\Zebra Technologies\Barcode Scanners\Scanner SDK\JPOS\
   ```

   Yen pungkasane **`Zebra services from none (Zebra Scanner SDK not installed)`**, SDK ora ditemokake ing
   lokasi gawane — pasangen ([langkah 3](#3-pasang-zebra-scanner-sdk)) utawa setel
   [ZEBRA_HOME](#zebra_home). Aplikasi tetep kebukak, nanging scanner ora bisa nyambung.

---

## 9. Sambungake scanner

1. Ing toolbar, **pamilih piranti** nampilake jeneng-jeneng scanner sing dikenal GAIA Desktop. Jarna ing
   **`ZebraAllScanners`**, utawa pilih jeneng kanggo mode USB scanner saka tabel ing
   [langkah 6](#6-setel-mode-usb-scanner-zebra).
2. Klik **Sambungake scanner**, utawa pencet **Ctrl+K** (uga ana ing menu **Scanner**).
3. Bilah status malih dadi **`scanner: ZebraAllScanners`** (utawa jeneng sing kok pilih), lan baris status
   ngabari *Scanner kasambung — ZebraAllScanners*. Tombole saiki tulisane **Pedhot scanner**.
4. Pindai kode bar GS1. Kode kuwi katon ing ndhuwur riwayat tangkepan lan laporane kebukak. Yen AIM Code ID
   urip, laporan nyebutake pembawa datane — *GS1-128*, *GS1 DataMatrix* — lan untaian elemen njaga saben
   pemisah FNC1.

Pamilih piranti ngelingi pilihanmu saka siji bukakan menyang liyane. Scanner tetep kasambung nganti kok
pedhot utawa aplikasi ditutup. Pindaian uga bisa ing mode **Gawe** lan **Gabung**: ing Gabung, saben pindaian
dadi salah siji kode bar sing digabungake.

Sajrone GAIA Desktop nyekel scanner, program liya ora bisa nganggo, semono uga kosok baline: tutup 123Scan
lan piranti lunak kasir (point-of-sale) utawa scanner liyane sadurunge nyambungake.

---

## 10. Referensi konfigurasi

### Jeneng piranti

Pamilih piranti diisi saka `jpos.xml` sing digawa ing `gaia-desktop.jar`. Entri scannere:

| Jeneng piranti | Jinis scanner | Dienggo kanggo |
|---|---|---|
| `ZebraAllScanners` | apa wae | **Gawan.** Mode USB apa wae sing didhukung |
| `ZebraAllScanners114` | apa wae | Versi JavaPOS 1.14 saka layanan sing padha |
| `ZebraUSBHandHeld` | IBM Hand-held USB | Scanner ing mode IBM Hand-held USB |
| `ZebraUSBTableTop` | IBM Table-top USB | Mode meja |
| `ZebraUSBOPOS` | USB OPOS | Mode USB OPOS |
| `ZebraScannerSNAPI` | SNAPI | Mode SNAPI |
| `ZebraScannerUSB` | USB apa wae | Mode USB apa wae |
| `ZebraScannerSerialSSI` | serial SSI | Scanner nganggo kabel serial (dudu USB) |
| `ZebraScannerSerialNIXMODB` | serial NIXMODB | Scanner nganggo kabel serial |
| `ZebraVDI` | VDI | Scanner sing diterusake menyang desktop virtual |

### ZEBRA_HOME

Yen Zebra SDK dipasang ing papan liya saka gawane, setel `ZEBRA_HOME` menyang folder `JPOS`-e sadurunge
mbukak `run.bat` — upamane ing berkas batch cilik ing sandhinge:

```bat
@echo off
set "ZEBRA_HOME=D:\Zebra\Scanner SDK\JPOS\"
call "%~dp0run.bat" %*
```

Banjur `run.bat` njupuk jar layanan Zebra saka `%ZEBRA_HOME%bin` lan nambahake folder kuwi menyang `PATH`
kanggo DLL Zebra.

### jpos.xml duwekmu dhewe

Kanggo nambah jeneng piranti utawa ngganti setelan sawijining entri, salin `jpos.xml` saka
`gaia-desktop.jar` (iku zip; piranti ekstrak apa wae bisa mbukak), owahi salinane, lan arahake
GAIA Desktop menyang kono:

```bat
set GAIA_DESKTOP_OPTS=-Djpos.config.populatorFileURL=file:///C:/Tools/jpos.xml
run.bat
```

Pamilih piranti banjur nampilake scanner ing berkas kuwi. Yen luwih saka siji scanner Zebra kasambung,
sawijining entri bisa ditaleni menyang salah sijine liwat properti `ScannerModel` utawa `SerialNumber`, kaya
ing entri `ZebraAllScanners`.

### Pilihan liyane

| Setelan | Carane |
|---|---|
| Basa aplikasi | `run.bat --language=FR` (mung kanggo bukakan iki), utawa Panel pilihan |
| Pilihan Java tambahan | `set GAIA_DESKTOP_OPTS=…` sadurunge `run.bat` |
| Java sing digawa | dokok folder `jdk-21…` ing sandhinge `run.bat`; dienggo sadurunge `JAVA_HOME` |

---

## 11. Ngatasi masalah

Kesalahan katon ing dialog nalika nyambungake, ing jendhela console, lan ing berkas log
`logs\gaia-desktop.log`, ing folder panggonan `run.bat` dibukak (folder GAIA Desktop, yen `run.bat` diklik
kaping pindho). Angka ing njero kurung kothak ing log iku kode kesalahan JavaPOS.

| Apa sing kok deleng | Sing bisa njalari | Apa sing kudu ditindakake |
|---|---|---|
| Ora ana tombol *Sambungake scanner* | Ora mlaku ing Windows | Mindai mung ana ing Windows |
| 123Scan ora nemokake scanner | Kabel, port USB, utawa scanner lagi dicekel program liya | Jajal kabel utawa port liya; tutup GAIA Desktop lan piranti lunak scanner liyane. GAIA Desktop ora bakal weruh scanner sing ora katon ing 123Scan |
| Console: `Zebra services from none (Zebra Scanner SDK not installed)` | SDK ora ana ing lokasi gawane | Pasang SDK 64-bit ([langkah 3](#3-pasang-zebra-scanner-sdk)), utawa setel [ZEBRA_HOME](#zebra_home) |
| Kesalahan **104**: *Could not connect to service with logicalName = …: …SymScannerSvc112Factory* | Jar layanan Zebra ora ditemokake | Kaya ing ndhuwur — priksa baris `Zebra services from` ing console |
| Kesalahan **109**: *Service does not exist in loaded JCL registry* | Jeneng piranti ora ana ing konfigurasi scanner, utawa `javaPOS114.jar` saka SDK mlebu class path | Pilih jeneng saka pamilih piranti; aja nambahi `javaPOS114.jar`; yen nganggo `jpos.xml` dhewe, priksa jenenge ana ing kono |
| Kesalahan **107** (ora ana hardware), utawa nyambung gagal sanajan scanner wis dicolokake | Scanner isih ing mode keyboard USB, utawa driver CoreScanner ora mlaku | Setel mode USB ([langkah 6](#6-setel-mode-usb-scanner-zebra)); wiwiti maneh Windows sawise masang SDK; jajal port USB liya |
| Kesalahan **112** (wektu entek) utawa **102** (wis dicekel) nalika nyambungake | Program liya lagi nyekel scanner | Tutup 123Scan lan piranti lunak scanner utawa kasir liyane, banjur sambungake maneh |
| `UnsatisfiedLinkError` … `CSJPOSScanner64` ing console | DLL Zebra ora bisa dimuat — asring SDK 32-bit karo Java 64-bit | Pasang SDK 64-bit; priksa `java -version` nglaporake VM 64-bit |
| Pindaian diketik menyang bilah input tinimbang teka minangka tangkepan | Scanner ing mode keyboard USB | Setel mode USB ([langkah 6](#6-setel-mode-usb-scanner-zebra)) |
| Wis kasambung, nanging pindaian ora teka | Jeneng piranti salah kanggo mode scanner, utawa scanner dicolokake maneh nalika kasambung | Pedhot, pilih `ZebraAllScanners`, sambungake maneh |
| Laporan ora nuduhake AIM Code ID / pembawa data | Pangiriman AIM Code ID mati | Uripake *Transmit Code ID Character → AIM Code ID* ([langkah 7](#7-atur-scanner-zebra-kanggo-gs1)) |
| Untaian elemen ilang pemisahe, utawa AI-ne nempel dadi siji | Dipindai ing mode keyboard, utawa diketik tangan tanpa `{GS}` | Nganggo mode USB JavaPOS; nalika ngetik, lebokake pemisah nganggo tombol `{GS}` |
| Ana jinis kode bar sing ora tau kewaca | Simbologi kuwi dipateni ing scanner | Aktifake ([langkah 7](#7-atur-scanner-zebra-kanggo-gs1)) |

Yen masalah isih ana, simpen bebarengan baris `run.bat:` saka console, teks dialog kesalahan lan
`logs\gaia-desktop.log`: bebarengan, kabeh kuwi nuduhake Java endi, layanan Zebra endi lan jeneng piranti endi
sing dienggo, lan ing ngendi persise sambungan gagal.

---

## 12. Scanner 2D Zebra sing didhukung

GAIA Desktop butuh scanner sing bisa maca kode bar sing dienggo nyithak data GS1 — GS1-128, GS1 DataBar,
EAN/UPC, lan, kanggo akeh-akehe kabutuhan, kode 2D: **GS1 DataMatrix** lan **GS1 QR Code**, sing uga dadi
cara biyasane URI **GS1 Digital Link** dicithak. Tegese butuh **imager 2D** Zebra: scanner linear (1D)
babar pisan ora bisa maca kode DataMatrix utawa QR.

Scanner dhewe ora perlu ngerti GS1 Application Identifiers utawa Digital Link.
**GAIA Desktop ngurai loro-lorone**: aplikasi njupuk pindaian kaya sing diwaca scanner — AIM Code ID, pemisah
FNC1 lan kabeh — lan dhewe mecah dadi AI, utawa ngowahi URI Digital Link dadi AI-ne.

### Wis diuji

| Scanner | Jinis | Status |
|---|---|---|
| **DS8108** | Imager 2D genggam nganggo kabel | **Digawe lan diuji** karo GAIA Desktop |

### Dikira bisa mlaku

Iki imager 2D Zebra sing, kaya DS8108, dilakokake liwat Scanner SDK lan layanan JavaPOS saka Zebra. Kabeh
**durung diuji** karo GAIA Desktop; setel saben-saben kaya ing pandhuan iki, nganggo Quick Reference Guide
utawa Product Reference Guide dhewe kanggo kode bar setelane.

| Kulawarga | Model (conto) |
|---|---|
| DS81x8 | DS8108 (nganggo kabel), DS8178 (tanpa kabel) |
| DS22x8 | DS2208 (nganggo kabel), DS2278 (tanpa kabel) — kanggo umum |
| DS36x8 | DS3608 (nganggo kabel), DS3678 (tanpa kabel) — kuwat, kalebu varian KD lan XR |
| DS46x8 | DS4608 (nganggo kabel), DS4678 (tanpa kabel) |
| DS9308 / DS9908 | Scanner presentasi lan tanpa tangan |
| MP7000 / MP7200 | Scanner bioptik multi-bidang lan scanner-timbangan |
| CS6080 | Scanner pendamping tanpa kabel |

Scanner tanpa kabel omong-omongan karo cradle-e, lan cradle kuwi sing dicolokake menyang komputer:
sambungake cradle liwat USB lan anggep pasangan kuwi minangka siji scanner. Scanner saka kulawarga Zebra liya
uga bisa wae mlaku, yen Scanner SDK saka Zebra ndhukung ing salah siji mode USB ing
[langkah 6](#6-setel-mode-usb-scanner-zebra).

### Fitur GS1 ing scanner Zebra dhewe

Saliyane maca kode bar, sawetara imager 2D Zebra bisa ngolah data GS1 ing scanner dhewe, sing diatur liwat
123Scan:

| Fitur | Apa sing ditindakake | Kanggo GAIA Desktop |
|---|---|---|
| **Label Parse+** | Ngurai AI label GS1 ing scanner lan ngirim kolom sing wis diowahi formate | **Jarna mati** — GAIA Desktop ora bakal weruh AI, pemisah FNC1 lan AIM Code ID maneh |
| **GS1 Digital Link** | Kanggo produk eceran sing ana EAN/UPC 1D lan kode Digital Link 2D bebarengan: milih endi sing dikirim | **Jarna mati** (gawan) |
| **GS1 Retail POS parsing** | Maca kode bar eceran 2D ing sintaksis URI Digital Link utawa untaian elemen | **Jarna mati** |

Setelan **GS1 Digital Link** duwe telung mode. **Mode 1 — GTIN only**, mode wiwitane sawise diaktifake, mung
ngirim GTIN, saka kode bar endi wae asale, mula batch, tanggal kadaluwarsa utawa nomer seri ing Digital Link
ora bakal tekan GAIA Desktop. **Mode 2 — Prioritize 2D** ngirim kode 2D yen katon sajrone wektu prioritas
(gawane 200 ms), yen ora, kode 1D. **Mode 3 — Corroborate 1D and 2D GTINs Match** ngirim kode 1D, lan uga
kode 2D yen GTIN-e cocog. Yen setelan iki kudu urip kanggo piranti lunak liya, nganggo Mode 2, dudu Mode 1.
Ing DS8108 iki parameter 2373 (urip/mati), 2374 (mode) lan 2491 (wektu entek).

Nganggo fitur-fitur iki butuh **123Scan versi 6 utawa luwih anyar** lan firmware sing ngemot fitur kasebut.
Zebra wis nambahi dhukungan GS1 Digital Link liwat nganyari firmware wiwit April 2025; tanggal ing ngisor iki
saka reseller Zebra, mula priksa cathetan rilis firmware saben model:

| Kulawarga model | Rilis firmware GS1 Digital Link |
|---|---|
| DS81x8 | 16 April 2025 |
| MP7000 | 21 April 2025 |
| DS9908 | 6 Juni 2025 |
| SP72 / SM72 | 11 Juli 2025 |
| DS46x8 | 28 Juli 2025 |
| MP7200 (Rev-C), DS5502 | 30 Juli 2025 |
| DS9308 | 29 Agustus 2025 |
| DS22x8, DS1000 | 5 September 2025 (mung Asia-Pasifik) |
| DS36x8 (kalebu KD lan XR), MS47 | 30 September 2025 |
| PL5000, SE4107 | 15 Oktober 2025 |
| CS6080 | 31 Desember 2025 |

Ora ana siji wae fitur iki sing dibutuhake kanggo maca kode GS1 Digital Link menyang GAIA Desktop: yen
kabeh mati, scanner 2D Zebra nganggo firmware saiki maca kode QR Digital Link kaya kode liyane, lan
GAIA Desktop ngowahi URI-ne dadi AI.

Sumber: Zebra, *DS8108 Product Reference Guide*, MN-002926-11EN Rev A (Agustus 2025), bab 12
"GS1 Digital Link Parameters" lan bab Data Parsing; Zebra, *123Scan release notes*;
Barcode Factory, *Upgrade Zebra Scanners for GS1 Digital Link* (tanggal firmware).
