# Saitin sikanan Zebra — GAIA Desktop don Windows

Yadda ake girka da saita sikanan barkod na Zebra domin GAIA Desktop ya iya karanta barkod na GS1 kai tsaye
daga gare shi: ana tantance kowane bincike kuma a ƙara shi cikin tarihi, tare da kariyar AIM Code ID da
kowane mai raba FNC1 kamar yadda suke. Ainihin takardar:
[GaiaDesktop-Zebra-Scanner-Windows.md](../../GaiaDesktop-Zebra-Scanner-Windows.md).

An gina GAIA Desktop kuma an gwada shi da na'urar ɗaukar hoto ta hannu **Zebra DS8108** da aka haɗa ta
USB. Matakan sun shafi kowane sikanan 2D na Zebra da Scanner SDK na Zebra ke goyon baya; barkod ɗin saiti
da sunayen menu na wani samfuri na musamman suna cikin Quick Reference Guide ko Product Reference Guide
nasa.

> **Windows kawai.** Direbobin sikanan Zebra suna samuwa ne don Windows kawai, don haka GAIA Desktop
> ba ya gina wani sarrafa sikana a macOS ko Linux. Duk sauran abubuwan manhajar suna aiki a kowane
> dandali.

---

## Abubuwan ciki

1. [Abin da kake bukata](#1-abin-da-kake-bukata)
2. [Girka Java](#2-girka-java)
3. [Girka Zebra Scanner SDK](#3-girka-zebra-scanner-sdk)
4. [Girka 123Scan](#4-girka-123scan)
5. [Sabunta firmware na sikanan Zebra](#5-sabunta-firmware-na-sikanan-zebra)
6. [Saita yanayin USB na sikanan Zebra](#6-saita-yanayin-usb-na-sikanan-zebra)
7. [Saita sikanan Zebra don GS1](#7-saita-sikanan-zebra-don-gs1)
8. [Girka kuma fara GAIA Desktop](#8-girka-kuma-fara-gaia-desktop)
9. [Haɗa sikana](#9-haɗa-sikana)
10. [Bayanin saiti](#10-bayanin-saiti)
11. [Magance matsaloli](#11-magance-matsaloli)
12. [Sikanan Zebra 2D da ake goyon baya](#12-sikanan-zebra-2d-da-ake-goyon-baya)

---

## 1. Abin da kake bukata

| | |
|---|---|
| **Sikana** | Sikanan Zebra 2D, tare da kebul ɗinsa na USB — duba [Sikanan Zebra 2D da ake goyon baya](#12-sikanan-zebra-2d-da-ake-goyon-baya) |
| **Kwamfuta** | Windows 10 ko 11, 64-bit |
| **Java** | Java 21 ko sabo, **64-bit** |
| **Zebra Scanner SDK for Windows** | **64-bit**, daga shafin tallafi na Zebra — yana samar da direba da sabis ɗin JavaPOS da GAIA Desktop ke amfani da su |
| **Zebra 123Scan** | Kayan aikin saiti na kyauta na Zebra, daga shafin tallafi na Zebra — ana amfani da shi don sabunta firmware na sikana da duba saitunansa |
| **GAIA Desktop** | `gaia-desktop-1.0.0-win.zip` |
| **Zaɓi** | **Product Reference Guide** (PRG) na sikana, wanda ke ɗauke da barkod ɗin saiti |

Yadda sassan suke haɗuwa:

```
Zebra scanner ──USB──► Zebra CoreScanner driver ──► Zebra JavaPOS services ──► GAIA Desktop
                       (installed by the SDK)        (in the SDK's JPOS\bin)    (run.bat puts them on
                                                                                 the class path)
```

GAIA Desktop yana ɗauke da JavaPOS API da kansa tare da saitin da ke sanya wa sikana suna (`jpos.xml`, a
cikin `gaia-desktop.jar`). Zebra SDK ne ke samar da sauran: direban da ke magana da sikana, da azuzuwan
sabis na Zebra da ke tsakanin JavaPOS da wannan direba.

---

## 2. Girka Java

GAIA Desktop yana bukatar **Java 21 ko sabo**, kuma dole ne ya zama ginin **64-bit**, domin ya dace da
direbobin Zebra na 64-bit.

1. Girka Java 21 JDK ko JRE don Windows x64 — misali Eclipse Temurin daga
   adoptium.net. Bari mai girkawa ya saita `JAVA_HOME` idan ya bayar da shawarar yin hakan.
2. Duba shi a Command Prompt:

   ```
   java -version
   ```

   Layin farko ya kamata ya nuna sigar 21 ko sama da haka; Java na 64-bit kuma yana buga `64-Bit Server VM`.

`run.bat` yana neman Java a wannan tsari: babban fayil na `jdk*` kusa da `run.bat`, sannan `JAVA_HOME`,
sannan `java` a `PATH`. Yana ƙin duk abin da ya girmi 21 tare da saƙo da ke faɗin haka.

---

## 3. Girka Zebra Scanner SDK

1. Sauke **Zebra Scanner SDK for Windows (64-bit)** daga shafin tallafi na Zebra.
2. Gudanar da mai girkawa. Idan ya ba da zaɓin sassa, tabbatar an haɗa sashen **JPOS** (JavaPOS).
3. Bar wurin girkawa na asali. GAIA Desktop yana neman sashen JavaPOS na SDK a:

   ```
   C:\Program Files\Zebra Technologies\Barcode Scanners\Scanner SDK\JPOS\
   ```

   kuma yana tsammanin jar ɗin sabis na Zebra (`JposServiceScanner.jar`, `JposServiceJniScanner.jar`, …)
   da ɗakunan karatu na asali (`CSJPOSScanner64.dll`, …) a cikin babban fayil ɗinsa na `bin\`. Idan an
   girka SDK a wani wuri, duba [ZEBRA_HOME](#zebra_home).
4. **Sake kunna Windows** bayan girkawa. Direban CoreScanner na SDK yana aiki a bayan fage kuma dole ne ya
   kasance yana aiki kafin GAIA Desktop ya iya kaiwa ga sikana; sake kunnawa ita ce hanya mafi sauƙi ta
   tabbatar da hakan.

---

## 4. Girka 123Scan

123Scan kayan aiki ne na kyauta na Zebra don saita sikanansa. Ana amfani da shi a mataki na gaba don
sabunta firmware na sikana, kuma shi ne hanya mafi sauƙi ta duba da canza saitunan sikana daga baya.

1. Sauke **123Scan** daga shafin tallafi na Zebra kuma gudanar da mai girkawa.
2. Toshe sikanan Zebra kuma fara 123Scan. Ya kamata ya gano sikana kuma ya nuna samfurinsa, lambar
   serial da sigar firmware ɗinsa ta yanzu. Idan bai yi ba, gwada wata tashar USB ko kebul kafin ka ci
   gaba — GAIA Desktop ba zai ga sikanan da 123Scan ba ya gani ba.

---

## 5. Sabunta firmware na sikanan Zebra

Kai firmware na sikana zuwa sabuwar sigar kafin ka saita shi. Sabuntawar firmware tana gyara matsalolin
karanta lamba kuma tana sa sikana ya dace da direban Zebra da SDK ya girka.

1. Rufe **GAIA Desktop** da duk wani shiri da ke iya amfani da sikana: 123Scan yana bukatar sikana shi
   kaɗai a lokacin sabuntawa.
2. A cikin 123Scan, yayin da sikana ke haɗe, zaɓi zaɓin sabunta firmware ɗinsa. 123Scan yana kwatanta
   firmware na sikana da sabuwar da Zebra ta fitar kuma ya ba da shawarar sauke ta da girka ta. Idan
   kwamfutar ba ta da intanet, sauke firmware na sikana daga shafin tallafinsa a shafin Zebra kuma ka loda
   fayil ɗin cikin 123Scan.
3. Fara sabuntawa kuma **kada ka cire sikana har sai 123Scan ya ce ya gama**. Yana ɗaukar mintuna da
   dama; sikana zai sake kunnawa a ƙarshe. Cire shi a tsakiya na iya sa ya daina aiki.
4. Tabbatar cewa 123Scan yanzu yana nuna sabuwar sigar firmware.

Sabuntawa na iya mayar da wasu saituna zuwa na asali, shi ya sa ake saita sikana a matakai biyu na gaba,
bayan firmware.

---

## 6. Saita yanayin USB na sikanan Zebra

Wannan shi ne matakin da aka fi mantawa da shi. **Idan aka fitar da sikanan Zebra daga akwati, kibod ɗin
USB ne** (yanayin USB HID Keyboard): yana "rubuta" kowane barkod a duk inda hankali yake. A wannan yanayin,
*Haɗa sikana* na GAIA Desktop ba zai iya gano shi ba, kuma duk abin da aka rubuta a filin rubutu yana rasa
masu raba FNC1 ɗinsa, wanda ke canza ma'anar igiyar abubuwa ta GS1.

Canza sikana zuwa yanayin da direban Zebra ke magana da shi kai tsaye:

| Yanayin USB na sikana | Sunan na'ura a GAIA Desktop | Bayani |
|---|---|---|
| **IBM Hand-held USB** | `ZebraUSBHandHeld`, ko `ZebraAllScanners` | Ana ba da shawara |
| SNAPI (da ɗaukar hoto ko ba tare da shi ba) | `ZebraScannerSNAPI`, ko `ZebraAllScanners` | Ka'idar USB ta Zebra kanta; ita ma tana aiki |
| IBM Table-top USB | `ZebraUSBTableTop`, ko `ZebraAllScanners` | Don sikanan tebur |
| USB OPOS (Hand-held) | `ZebraUSBOPOS`, ko `ZebraAllScanners` | |
| USB HID Keyboard (saitin masana'anta) | — | **Ba zai haɗu ba** |

`ZebraAllScanners`, sunan na'ura na asali a GAIA Desktop, yana karɓar sikana a kowane ɗaya daga cikin
yanayin da ake goyon baya, don haka da zarar an saita yanayin, na asalin yawanci yana aiki kai tsaye.

Don canza yanayin, ko dai:

- **ka binciki barkod ɗin saiti** na yanayin — misali *IBM Hand-held USB* — a cikin Quick Reference Guide
  ko Product Reference Guide na sikana (sashen USB Host Types / USB Device Type); sikana zai yi ƙara don
  tabbatarwa. Ko
- **ka yi amfani da 123Scan** ([mataki 4](#4-girka-123scan)): haɗa sikana, buɗe saitinsa, kuma saita nau'in
  mai masaukin USB.

Bayan canza yanayin, Windows yana sake gane sikana a matsayin wani nau'in na'ura daban; jira 'yan daƙiƙoƙi
kafin ka haɗa.

Don mayar da sikana zuwa yanayin kibod daga baya — don amfani da wasu manhajoji — binciki barkod ɗin saiti
na *USB Keyboard (HID)*, ko maido da saitunan masana'anta.

---

## 7. Saita sikanan Zebra don GS1

GAIA Desktop yana tantancewa mafi kyau idan sikana ya gaya masa irin barkod ɗin da ya karanta.

### Aika AIM Code ID

Saita **Transmit Code ID Character** zuwa **AIM Code ID Character**, ta amfani da barkod ɗin saiti a cikin
Product Reference Guide (User Preferences) ko a 123Scan. Sa'an nan kowane bincike zai fara da alamar
tantance nau'in alamarsa — `]C1` don GS1-128, `]d2` don GS1 DataMatrix, `]Q3` don GS1 QR Code, `]e0`
don GS1 DataBar — wadda GAIA Desktop ke amfani da ita don sanya wa mai ɗaukar bayanai suna da aiwatar da
dokokinsa.

Ba tare da ita ba, GAIA Desktop yana ci gaba da tantance igiyar abubuwa kuma yana ɗaukar nau'in alama daga
binciken kansa, amma rahoton ba shi da AIM Code ID da zai nuna, kuma barkod na yau da kullum kamar EAN-13
ya fi wuyar bambancewa da igiyar abubuwa ta GS1.

### Duba nau'ikan alama

Tabbatar cewa nau'ikan barkod da kake aiki da su **suna kunne**: yawanci GS1-128, GS1 DataMatrix,
GS1 DataBar, GS1 QR Code da EAN/UPC. Mafi yawansu suna kunne tun asali a sikanan Zebra 2D; 123Scan yana
nuna saitunan yanzu, kuma Product Reference Guide yana da barkod don kunna kowanne.

### Bar fasalolin tantance GS1 na Zebra a kashe

Sabuwar firmware tana ƙara fasalolin GS1 da ke aiki a kan sikana kansa — **Label Parse+**, **GS1
Digital Link** da **GS1 Retail POS parsing**. Bar su a **kashe**, kamar yadda sikana ke zuwa: suna sake
tsara ko taƙaita abin da sikana ke aikawa, alhali GAIA Desktop yana bukatar ɗanyen bayanai don tantancewa
da tabbatarwa. Duba [Sikanan Zebra 2D da ake goyon baya](#12-sikanan-zebra-2d-da-ake-goyon-baya) don abin da
kowanne ke yi.

### Kada a saka kari a gaba ko a baya

Kada ka ƙara kari a gaba, kari a baya ko maɓallin Enter ga abin da sikana ke fitarwa. GAIA Desktop yana
karɓar kowane bincike a matsayin cikakken saƙo ta hanyar JavaPOS kuma ba ya bukatar ko ɗaya daga cikinsu;
ƙarin haruffa za su shiga cikin igiyar abubuwa.

---

## 8. Girka kuma fara GAIA Desktop

1. Buɗe `gaia-desktop-1.0.0-win.zip`, misali zuwa `C:\Tools\`. Yana ɗauke da babban fayil ɗaya:

   ```
   gaia-desktop-1.0.0-win\
   ├── docs\
   │   └── GaiaDesktop-Zebra-Scanner-Windows.md      this guide
   ├── gaia-desktop.jar
   ├── lib\
   ├── readme.html                                   index of every guide
   └── run.bat
   ```

2. Toshe sikana.
3. Danna **`run.bat`** sau biyu. Tagar console tana buɗewa kusa da manhajar kuma tana kasancewa a buɗe:
   a can ake ba da rahoton halin sikana da kurakuran JavaPOS.
4. Karanta layin farko a console. Yana faɗin abin da `run.bat` ya samu:

   ```
   run.bat: Java 21.0.8 from JAVA_HOME; JavaFX from lib\; JavaPOS from lib\; Zebra services from C:\Program Files\Zebra Technologies\Barcode Scanners\Scanner SDK\JPOS\
   ```

   Idan ya ƙare da **`Zebra services from none (Zebra Scanner SDK not installed)`**, ba a sami SDK a wurinsa
   na asali ba — girka shi ([mataki 3](#3-girka-zebra-scanner-sdk)) ko saita [ZEBRA_HOME](#zebra_home).
   Manhajar tana farawa duk da haka, amma sikana ba zai iya haɗuwa ba.

---

## 9. Haɗa sikana

1. A sandar kayan aiki, **mai zaɓar na'ura** yana jera sunayen sikanan da GAIA Desktop ya sani. Bar shi a
   **`ZebraAllScanners`**, ko zaɓi sunan yanayin USB na sikana daga teburin a
   [mataki 6](#6-saita-yanayin-usb-na-sikanan-zebra).
2. Danna **Haɗa sikana**, ko danna **Ctrl+K** (yana kuma cikin menu na **Sikana**).
3. Sandar hali tana canzawa zuwa **`sikana: ZebraAllScanners`** (ko sunan da ka zaɓa), kuma layin hali
   yana ba da rahoton *An haɗa sikana — ZebraAllScanners*. Yanzu maɓallin yana karanta **Cire sikana**.
4. Binciki barkod na GS1. Yana bayyana a saman tarihin kamawa kuma rahotonsa yana buɗewa. Idan AIM Code ID
   yana kunne, rahoton yana sanya wa mai ɗaukar bayanai suna — *GS1-128*, *GS1 DataMatrix* — kuma igiyar
   abubuwa tana adana kowane mai raba FNC1.

Mai zaɓar na'ura yana tuna zaɓinka tsakanin gudanarwa. Sikana yana kasancewa a haɗe har sai ka cire shi ko
ka rufe manhajar. Bincike yana aiki kuma a yanayin **Gina** da **Haɗa**: a Haɗa, kowane bincike yana zama
ɗaya daga cikin barkod ɗin da ake haɗawa.

Yayin da GAIA Desktop ke riƙe da sikana, wasu shirye-shirye ba za su iya amfani da shi ba, haka ma akasin
haka: rufe 123Scan da duk wata manhajar wurin sayarwa (point-of-sale) ko ta sikana kafin ka haɗa.

---

## 10. Bayanin saiti

### Sunayen na'urori

Ana cika mai zaɓar na'ura daga `jpos.xml` da aka haɗa a cikin `gaia-desktop.jar`. Shigarwar sikanansa:

| Sunan na'ura | Nau'in sikana | Ana amfani da shi da |
|---|---|---|
| `ZebraAllScanners` | kowane | **Na asali.** Kowane yanayin USB da ake goyon baya |
| `ZebraAllScanners114` | kowane | Sigar JavaPOS 1.14 ta sabis ɗaya |
| `ZebraUSBHandHeld` | IBM Hand-held USB | Sikana a yanayin IBM Hand-held USB |
| `ZebraUSBTableTop` | IBM Table-top USB | Yanayin tebur |
| `ZebraUSBOPOS` | USB OPOS | Yanayin USB OPOS |
| `ZebraScannerSNAPI` | SNAPI | Yanayin SNAPI |
| `ZebraScannerUSB` | kowane USB | Kowane yanayin USB |
| `ZebraScannerSerialSSI` | serial SSI | Sikanan masu kebul na serial (ba USB ba) |
| `ZebraScannerSerialNIXMODB` | serial NIXMODB | Sikanan masu kebul na serial |
| `ZebraVDI` | VDI | Sikanan da aka tura zuwa kwamfutar kama-da-wane |

### ZEBRA_HOME

Idan an girka Zebra SDK a wani wuri ba na asali ba, saita `ZEBRA_HOME` zuwa babban fayil ɗinsa na `JPOS`
kafin fara `run.bat` — misali a cikin ƙaramin fayil na batch kusa da shi:

```bat
@echo off
set "ZEBRA_HOME=D:\Zebra\Scanner SDK\JPOS\"
call "%~dp0run.bat" %*
```

Sa'an nan `run.bat` yana ɗaukar jar ɗin sabis na Zebra daga `%ZEBRA_HOME%bin` kuma ya ƙara wannan babban
fayil zuwa `PATH` don DLL na Zebra.

### jpos.xml naka

Don ƙara sunan na'ura ko canza saitunan wata shigarwa, kwafi `jpos.xml` daga cikin `gaia-desktop.jar` (zip
ne; kowane kayan buɗe zip zai buɗe shi), gyara kwafin, kuma ka nuna wa GAIA Desktop shi:

```bat
set GAIA_DESKTOP_OPTS=-Djpos.config.populatorFileURL=file:///C:/Tools/jpos.xml
run.bat
```

Sa'an nan mai zaɓar na'ura yana jera sikanan da ke cikin wannan fayil. Idan an haɗa sikanan Zebra fiye da
ɗaya, ana iya ɗaure shigarwa ga ɗaya daga cikinsu ta siffarta ta `ScannerModel` ko `SerialNumber`, kamar
yadda yake a shigarwar `ZebraAllScanners`.

### Sauran zaɓuɓɓuka

| Saiti | Yadda ake |
|---|---|
| Harshen manhaja | `run.bat --language=FR` (don wannan gudanarwa kawai), ko Fanel ɗin zaɓuɓɓuka |
| Ƙarin zaɓuɓɓukan Java | `set GAIA_DESKTOP_OPTS=…` kafin `run.bat` |
| Java da aka haɗa | saka babban fayil na `jdk-21…` kusa da `run.bat`; ana amfani da shi kafin `JAVA_HOME` |

---

## 11. Magance matsaloli

Kurakurai suna bayyana a cikin akwatin saƙo lokacin haɗawa, a tagar console, da kuma a fayil ɗin log
`logs\gaia-desktop.log`, a babban fayil ɗin da aka fara `run.bat` daga cikinsa (babban fayil ɗin
GAIA Desktop, idan an danna `run.bat` sau biyu). Lambar da ke cikin bakan murabba'i a log ita ce lambar
kuskuren JavaPOS.

| Abin da kake gani | Abin da ka iya jawowa | Abin da za a yi |
|---|---|---|
| Babu maɓallin *Haɗa sikana* | Ba a Windows yake aiki ba | Bincike na Windows ne kawai |
| 123Scan bai sami sikana ba | Kebul, tashar USB, ko wani shiri yana riƙe da sikana | Gwada wani kebul ko tasha; rufe GAIA Desktop da sauran manhajojin sikana. GAIA Desktop ba zai ga sikanan da 123Scan ba ya gani ba |
| Console: `Zebra services from none (Zebra Scanner SDK not installed)` | SDK ba ya wurinsa na asali | Girka SDK na 64-bit ([mataki 3](#3-girka-zebra-scanner-sdk)), ko saita [ZEBRA_HOME](#zebra_home) |
| Kuskure **104**: *Could not connect to service with logicalName = …: …SymScannerSvc112Factory* | Ba a sami jar ɗin sabis na Zebra ba | Kamar na sama — duba layin `Zebra services from` na console |
| Kuskure **109**: *Service does not exist in loaded JCL registry* | Sunan na'ura ba ya cikin saitin sikana, ko `javaPOS114.jar` na SDK ya shiga class path | Zaɓi suna daga mai zaɓar na'ura; kada ka ƙara `javaPOS114.jar`; idan kana amfani da `jpos.xml` naka, tabbatar sunan yana ciki |
| Kuskure **107** (babu kayan aiki), ko haɗawa ta kasa duk da sikana yana toshe | Sikana har yanzu yana yanayin kibod na USB, ko direban CoreScanner ba ya aiki | Saita yanayin USB ([mataki 6](#6-saita-yanayin-usb-na-sikanan-zebra)); sake kunna Windows bayan girka SDK; gwada wata tashar USB |
| Kuskure **112** (lokaci ya ƙare) ko **102** (an riga an riƙe) lokacin haɗawa | Wani shiri yana riƙe da sikana | Rufe 123Scan da duk wata manhajar sikana ko wurin sayarwa, sannan ka sake haɗawa |
| `UnsatisfiedLinkError` … `CSJPOSScanner64` a console | Ba za a iya loda DLL na Zebra ba — sau da yawa SDK na 32-bit tare da Java na 64-bit | Girka SDK na 64-bit; tabbatar `java -version` yana ba da rahoton VM na 64-bit |
| Bincike yana rubutuwa a sandar shigarwa maimakon zuwa a matsayin kamawa | Sikana yana yanayin kibod na USB | Saita yanayin USB ([mataki 6](#6-saita-yanayin-usb-na-sikanan-zebra)) |
| An haɗa, amma bincike ba ya zuwa | Sunan na'ura mara kyau don yanayin sikana, ko an sake toshe sikana yayin da yake haɗe | Cire, zaɓi `ZebraAllScanners`, sake haɗawa |
| Rahoton ba ya nuna AIM Code ID / mai ɗaukar bayanai | Aikawa da AIM Code ID a kashe yake | Kunna *Transmit Code ID Character → AIM Code ID* ([mataki 7](#7-saita-sikanan-zebra-don-gs1)) |
| Igiyar abubuwa ta rasa masu rabawa, ko AI sun haɗe | An binciko a yanayin kibod, ko an rubuta da hannu ba tare da `{GS}` ba | Yi amfani da yanayin USB na JavaPOS; lokacin rubutu, saka masu rabawa da maɓallin `{GS}` |
| Wani nau'in barkod ba a taɓa karanta shi ba | An kashe wannan nau'in alama a kan sikana | Kunna shi ([mataki 7](#7-saita-sikanan-zebra-don-gs1)) |

Idan matsalar ta ci gaba, ajiye layin `run.bat:` na console, rubutun akwatin kuskure da
`logs\gaia-desktop.log` tare: a haɗe suna nuna wane Java, waɗanne sabis na Zebra da wane sunan na'ura aka
yi amfani da su, da kuma ainihin inda haɗin ya kasa.

---

## 12. Sikanan Zebra 2D da ake goyon baya

GAIA Desktop yana bukatar sikanan da zai iya karanta barkod ɗin da ake buga bayanan GS1 da su — GS1-128,
GS1 DataBar, EAN/UPC, kuma, don mafi yawan amfani, lambobin 2D: **GS1 DataMatrix** da **GS1 QR Code**,
wanda kuma haka ake yawan buga URI na **GS1 Digital Link**. Wato ana bukatar **na'urar ɗaukar hoto ta 2D**
ta Zebra: sikanan layi (1D) ba zai iya karanta lambobin DataMatrix ko QR ko kaɗan ba.

Sikana kansa ba ya bukatar fahimtar GS1 Application Identifiers ko Digital Link.
**GAIA Desktop yana tantance duka biyun**: yana ɗaukar binciken kamar yadda sikana ya karanta shi — AIM
Code ID, masu raba FNC1 da komai — kuma shi da kansa ya raba shi zuwa AI, ko ya mayar da URI na Digital
Link zuwa AI ɗinsa.

### An gwada

| Sikana | Nau'i | Hali |
|---|---|---|
| **DS8108** | Na'urar ɗaukar hoto ta 2D ta hannu mai waya | **An gina kuma an gwada** da GAIA Desktop |

### Ana sa ran za su yi aiki

Waɗannan na'urorin ɗaukar hoto na 2D na Zebra ne waɗanda, kamar DS8108, ake sarrafa su ta Scanner SDK da
sabis ɗin JavaPOS na Zebra. **Ba a gwada su ba** da GAIA Desktop; saita kowanne kamar yadda yake a wannan
jagora, ta amfani da Quick Reference Guide ko Product Reference Guide nasa don barkod ɗin saiti.

| Iyali | Samfura (misalai) |
|---|---|
| DS81x8 | DS8108 (mai waya), DS8178 (mara waya) |
| DS22x8 | DS2208 (mai waya), DS2278 (mara waya) — amfani na gama-gari |
| DS36x8 | DS3608 (mai waya), DS3678 (mara waya) — masu ƙarfi, har da nau'ikan KD da XR |
| DS46x8 | DS4608 (mai waya), DS4678 (mara waya) |
| DS9308 / DS9908 | Sikanan gabatarwa da marasa riƙo da hannu |
| MP7000 / MP7200 | Sikanan bioptic masu fuskoki da yawa da sikana-ma'auni |
| CS6080 | Sikanan abokin tafiya mara waya |

Sikanan mara waya yana magana da madogararsa, kuma madogarar ce ake toshewa a kwamfuta: haɗa madogarar ta
USB kuma ka ɗauki su biyun a matsayin sikana ɗaya. Sikanan wasu iyalan Zebra ma na iya aiki, idan Scanner
SDK na Zebra yana goyon bayansu a ɗaya daga cikin yanayin USB a [mataki 6](#6-saita-yanayin-usb-na-sikanan-zebra).

### Fasalolin GS1 a kan sikanan Zebra kansa

Baya ga karanta barkod, wasu na'urorin ɗaukar hoto na 2D na Zebra na iya sarrafa bayanan GS1 a kan sikana
kansa, ana saita su ta 123Scan:

| Fasali | Abin da yake yi | Ga GAIA Desktop |
|---|---|---|
| **Label Parse+** | Yana tantance AI na lakabin GS1 a kan sikana kuma ya aika filayen da aka sake tsarawa | **Bar shi a kashe** — GAIA Desktop ba zai ƙara ganin AI, masu raba FNC1 da AIM Code ID ba |
| **GS1 Digital Link** | Don kayayyakin sayarwa da ke da EAN/UPC na 1D da lambar Digital Link ta 2D tare: yana zaɓar wanda za a aika | **Bar shi a kashe** (na asali) |
| **GS1 Retail POS parsing** | Yana karanta barkod na sayarwa na 2D a tsarin URI na Digital Link ko igiyar abubuwa | **Bar shi a kashe** |

Saitin **GS1 Digital Link** yana da yanayi uku. **Mode 1 — GTIN only**, yanayin da yake farawa da shi
da zarar an kunna shi, yana aika GTIN kawai, ko daga wane barkod ya fito, don haka batch, ranar ƙarewa ko
lambar serial da ke cikin Digital Link ba za su taɓa kaiwa GAIA Desktop ba. **Mode 2 — Prioritize 2D**
yana aika lambar 2D idan an gan ta a cikin lokacin fifiko (200 ms na asali), in ba haka ba kuma ta 1D.
**Mode 3 — Corroborate 1D and 2D GTINs Match** yana aika ta 1D, da ta 2D kuma idan GTIN ɗinta ya dace.
Idan dole ne saitin ya kasance a kunne don wata manhaja, yi amfani da Mode 2, ba Mode 1 ba. A DS8108
waɗannan su ne sigogi 2373 (kunna/kashe), 2374 (yanayi) da 2491 (lokacin jira).

Amfani da waɗannan fasaloli yana bukatar **123Scan sigar 6 ko sabo** da firmware da ke ɗauke da su. Zebra
tana ƙara goyon bayan GS1 Digital Link ta sabunta firmware tun Afrilu 2025; kwanakin da ke ƙasa sun fito
daga wani mai sake sayar da Zebra, don haka duba bayanan fitar da firmware na kowane samfuri:

| Iyalin samfuri | Fitar da firmware na GS1 Digital Link |
|---|---|
| DS81x8 | 16 Afrilu 2025 |
| MP7000 | 21 Afrilu 2025 |
| DS9908 | 6 Yuni 2025 |
| SP72 / SM72 | 11 Yuli 2025 |
| DS46x8 | 28 Yuli 2025 |
| MP7200 (Rev-C), DS5502 | 30 Yuli 2025 |
| DS9308 | 29 Agusta 2025 |
| DS22x8, DS1000 | 5 Satumba 2025 (Asiya da Tekun Pasifik kawai) |
| DS36x8 (har da KD da XR), MS47 | 30 Satumba 2025 |
| PL5000, SE4107 | 15 Oktoba 2025 |
| CS6080 | 31 Disamba 2025 |

Babu ɗaya daga cikin waɗannan fasalolin da ake bukata don karanta lambobin GS1 Digital Link a GAIA Desktop:
idan suna a kashe, sikanan Zebra 2D mai firmware na yanzu yana karanta lambar QR ta Digital Link kamar
kowace, kuma GAIA Desktop yana mayar da URI zuwa AI ɗinsa.

Majiyoyi: Zebra, *DS8108 Product Reference Guide*, MN-002926-11EN Rev A (Agusta 2025), babi na 12
"GS1 Digital Link Parameters" da babin Data Parsing; Zebra, *123Scan release notes*;
Barcode Factory, *Upgrade Zebra Scanners for GS1 Digital Link* (kwanakin firmware).
