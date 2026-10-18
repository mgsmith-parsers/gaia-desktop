# Kuweka skana ya Zebra — GAIA Desktop kwa Windows

Jinsi ya kusakinisha na kusanidi skana ya msimbopau ya Zebra ili GAIA Desktop isome misimbopau ya GS1
moja kwa moja kutoka kwayo: kila uchanganuzi huchanganuliwa na kuongezwa kwenye historia, huku kiambishi
awali cha AIM Code ID na kila kitenganishi cha FNC1 vikibaki kama vilivyo. Hati asili:
[GaiaDesktop-Zebra-Scanner-Windows.md](../../GaiaDesktop-Zebra-Scanner-Windows.md).

GAIA Desktop ilijengwa na kujaribiwa kwa kipiga-picha cha mkononi **Zebra DS8108** kilichounganishwa kwa
USB. Hatua hizi zinatumika kwa skana yoyote ya 2D ya Zebra inayoungwa mkono na Scanner SDK ya Zebra;
misimbopau ya kuweka na majina ya menyu ya modeli fulani yamo katika Quick Reference Guide au Product
Reference Guide yake yenyewe.

> **Windows pekee.** Viendeshi vya skana za Zebra vipo kwa Windows pekee, kwa hivyo GAIA Desktop
> haijengi vidhibiti vyovyote vya skana kwenye macOS au Linux. Kila kitu kingine katika programu
> hufanya kazi kwenye kila jukwaa.

---

## Yaliyomo

1. [Unachohitaji](#1-unachohitaji)
2. [Sakinisha Java](#2-sakinisha-java)
3. [Sakinisha Zebra Scanner SDK](#3-sakinisha-zebra-scanner-sdk)
4. [Sakinisha 123Scan](#4-sakinisha-123scan)
5. [Sasisha programu dhibiti ya skana ya Zebra](#5-sasisha-programu-dhibiti-ya-skana-ya-zebra)
6. [Weka hali ya USB ya skana ya Zebra](#6-weka-hali-ya-usb-ya-skana-ya-zebra)
7. [Sanidi skana ya Zebra kwa GS1](#7-sanidi-skana-ya-zebra-kwa-gs1)
8. [Sakinisha na uanzishe GAIA Desktop](#8-sakinisha-na-uanzishe-gaia-desktop)
9. [Unganisha skana](#9-unganisha-skana)
10. [Marejeo ya usanidi](#10-marejeo-ya-usanidi)
11. [Utatuzi wa matatizo](#11-utatuzi-wa-matatizo)
12. [Skana za 2D za Zebra zinazoungwa mkono](#12-skana-za-2d-za-zebra-zinazoungwa-mkono)

---

## 1. Unachohitaji

| | |
|---|---|
| **Skana** | Skana ya 2D ya Zebra, pamoja na kebo yake ya USB — tazama [Skana za 2D za Zebra zinazoungwa mkono](#12-skana-za-2d-za-zebra-zinazoungwa-mkono) |
| **Kompyuta** | Windows 10 au 11, biti 64 |
| **Java** | Java 21 au mpya zaidi, **biti 64** |
| **Zebra Scanner SDK for Windows** | **Biti 64**, kutoka tovuti ya usaidizi ya Zebra — hutoa kiendeshi na huduma za JavaPOS ambazo GAIA Desktop hutumia |
| **Zebra 123Scan** | Zana ya bure ya usanidi ya Zebra, kutoka tovuti ya usaidizi ya Zebra — hutumika kusasisha programu dhibiti ya skana na kukagua mipangilio yake |
| **GAIA Desktop** | `gaia-desktop-1.0.0-win.zip` |
| **Si lazima** | **Product Reference Guide** (PRG) ya skana, yenye misimbopau ya kuweka |

Jinsi sehemu zinavyoungana:

```
Zebra scanner ──USB──► Zebra CoreScanner driver ──► Zebra JavaPOS services ──► GAIA Desktop
                       (installed by the SDK)        (in the SDK's JPOS\bin)    (run.bat puts them on
                                                                                 the class path)
```

GAIA Desktop inabeba yenyewe API ya JavaPOS na usanidi unaoipa skana jina (`jpos.xml`, ndani ya
`gaia-desktop.jar`). Zebra SDK hutoa vingine: kiendeshi kinachozungumza na skana, na madaraja ya huduma ya
Zebra yaliyo kati ya JavaPOS na kiendeshi hicho.

---

## 2. Sakinisha Java

GAIA Desktop inahitaji **Java 21 au mpya zaidi**, na lazima iwe toleo la **biti 64**, ili ilingane na
viendeshi vya Zebra vya biti 64.

1. Sakinisha JDK au JRE ya Java 21 kwa Windows x64 — kwa mfano Eclipse Temurin kutoka
   adoptium.net. Mwache kisakinishi aweke `JAVA_HOME` ikiwa atapendekeza.
2. Ikague katika Command Prompt:

   ```
   java -version
   ```

   Mstari wa kwanza unapaswa kuonyesha toleo la 21 au la baadaye; Java ya biti 64 pia huchapisha `64-Bit Server VM`.

`run.bat` hutafuta Java kwa mpangilio huu: folda ya `jdk*` kando ya `run.bat`, kisha `JAVA_HOME`, kisha
`java` kwenye `PATH`. Hukataa chochote kilicho cha zamani kuliko 21 kwa ujumbe unaoeleza hivyo.

---

## 3. Sakinisha Zebra Scanner SDK

1. Pakua **Zebra Scanner SDK for Windows (64-bit)** kutoka tovuti ya usaidizi ya Zebra.
2. Endesha kisakinishi. Ikiwa kitatoa chaguo la vipengele, hakikisha kipengele cha **JPOS** (JavaPOS)
   kimejumuishwa.
3. Acha mahali chaguomsingi pa usakinishaji. GAIA Desktop hutafuta sehemu ya JavaPOS ya SDK katika:

   ```
   C:\Program Files\Zebra Technologies\Barcode Scanners\Scanner SDK\JPOS\
   ```

   na hutarajia jar za huduma za Zebra (`JposServiceScanner.jar`, `JposServiceJniScanner.jar`, …) na
   maktaba asilia (`CSJPOSScanner64.dll`, …) ziwe katika folda yake ya `bin\`. Ikiwa SDK imesakinishwa
   mahali pengine, tazama [ZEBRA_HOME](#zebra_home).
4. **Anzisha upya Windows** baada ya kusakinisha. Kiendeshi cha CoreScanner cha SDK hufanya kazi chinichini
   na lazima kiwe kinafanya kazi kabla GAIA Desktop haijaweza kufikia skana; kuanzisha upya ndiyo njia
   rahisi zaidi ya kuhakikisha hilo.

---

## 4. Sakinisha 123Scan

123Scan ni zana ya bure ya Zebra ya kusanidi skana zake. Hutumika katika hatua inayofuata kusasisha
programu dhibiti ya skana, na ndiyo njia rahisi zaidi ya kukagua na kubadilisha mipangilio ya skana baadaye.

1. Pakua **123Scan** kutoka tovuti ya usaidizi ya Zebra na uendeshe kisakinishi.
2. Chomeka skana ya Zebra na uanzishe 123Scan. Inapaswa kuipata skana na kuonyesha modeli, nambari ya
   utambulisho na toleo la sasa la programu dhibiti yake. Isipofanya hivyo, jaribu mlango mwingine wa USB
   au kebo nyingine kabla ya kuendelea — GAIA Desktop haitaona skana ambayo 123Scan haioni.

---

## 5. Sasisha programu dhibiti ya skana ya Zebra

Leta programu dhibiti (firmware) ya skana hadi toleo la karibuni kabla ya kuisanidi. Masasisho ya programu
dhibiti hurekebisha matatizo ya usimbuaji na huifanya skana iende sambamba na kiendeshi cha Zebra
kilichosakinishwa na SDK.

1. Funga **GAIA Desktop** na programu nyingine yoyote inayoweza kuwa inatumia skana: 123Scan inahitaji skana
   peke yake wakati wa kusasisha.
2. Katika 123Scan, skana ikiwa imeunganishwa, chagua chaguo lake la kusasisha programu dhibiti. 123Scan
   hulinganisha programu dhibiti ya skana na ya karibuni zaidi ambayo Zebra imechapisha na hupendekeza
   kuipakua na kuisakinisha. Ikiwa kompyuta haina intaneti, pakua programu dhibiti ya skana kutoka ukurasa
   wake wa usaidizi kwenye tovuti ya Zebra na upakie faili kwenye 123Scan.
3. Anzisha sasisho na **acha skana ikiwa imechomekwa hadi 123Scan iripoti kuwa imemaliza**. Huchukua dakika
   kadhaa; skana huanza upya mwishoni. Kuichomoa katikati kunaweza kuifanya isitumike.
4. Hakikisha kuwa 123Scan sasa inaonyesha toleo jipya la programu dhibiti.

Sasisho linaweza kurudisha baadhi ya mipangilio kwenye chaguomsingi, ndiyo maana skana husanidiwa katika
hatua mbili zinazofuata, baada ya programu dhibiti.

---

## 6. Weka hali ya USB ya skana ya Zebra

Hii ndiyo hatua inayorukwa mara nyingi zaidi. **Ikitoka kiwandani, skana ya Zebra ni kibodi ya USB** (hali
ya USB HID Keyboard): "huandika" kila msimbopau popote palipo na mwelekeo. Katika hali hiyo, *Unganisha
skana* ya GAIA Desktop haiwezi kuipata, na chochote kinachoandikwa kwenye sehemu ya maandishi hupoteza
vitenganishi vyake vya FNC1, jambo linalobadilisha maana ya mfuatano wa vipengele wa GS1.

Badilisha skana iwe katika hali ambayo kiendeshi cha Zebra huzungumza nayo moja kwa moja:

| Hali ya USB ya skana | Jina la kifaa katika GAIA Desktop | Maelezo |
|---|---|---|
| **IBM Hand-held USB** | `ZebraUSBHandHeld`, au `ZebraAllScanners` | Inapendekezwa |
| SNAPI (pamoja na au bila upigaji picha) | `ZebraScannerSNAPI`, au `ZebraAllScanners` | Itifaki ya USB ya Zebra yenyewe; nayo hufanya kazi |
| IBM Table-top USB | `ZebraUSBTableTop`, au `ZebraAllScanners` | Kwa skana za mezani |
| USB OPOS (Hand-held) | `ZebraUSBOPOS`, au `ZebraAllScanners` | |
| USB HID Keyboard (chaguomsingi la kiwandani) | — | **Haitaunganika** |

`ZebraAllScanners`, jina la kifaa chaguomsingi katika GAIA Desktop, hukubali skana katika hali yoyote
inayoungwa mkono, kwa hivyo hali ikishawekwa, chaguomsingi kwa kawaida hufanya kazi moja kwa moja.

Ili kubadilisha hali, ama:

- **changanua msimbopau wa kuweka** wa hali hiyo — kwa mfano *IBM Hand-held USB* — katika Quick Reference
  Guide au Product Reference Guide ya skana (sehemu ya USB Host Types / USB Device Type); skana hulia
  kuthibitisha. Au
- **tumia 123Scan** ([hatua 4](#4-sakinisha-123scan)): unganisha skana, fungua usanidi wake, na uweke aina
  ya mwenyeji wa USB.

Baada ya kubadilisha hali, Windows huitambua skana upya kama aina nyingine ya kifaa; subiri sekunde chache
kabla ya kuunganisha.

Ili kurudisha skana kwenye hali ya kibodi baadaye — kwa matumizi na programu nyingine — changanua msimbopau
wa kuweka wa *USB Keyboard (HID)*, au rejesha mipangilio yake ya kiwandani.

---

## 7. Sanidi skana ya Zebra kwa GS1

GAIA Desktop huchanganua vizuri zaidi skana inapoiambia ni aina gani ya msimbopau iliyosoma.

### Tuma AIM Code ID

Weka **Transmit Code ID Character** kuwa **AIM Code ID Character**, kwa kutumia msimbopau wa kuweka ulio
katika Product Reference Guide (User Preferences) au katika 123Scan. Kisha kila uchanganuzi huanza na
kitambulisho chake cha alama — `]C1` kwa GS1-128, `]d2` kwa GS1 DataMatrix, `]Q3` kwa GS1 QR Code, `]e0`
kwa GS1 DataBar — ambacho GAIA Desktop hukitumia kutaja mbeba data na kutumia kanuni zake.

Bila hicho, GAIA Desktop bado huchanganua mfuatano wa vipengele na kuchukua aina ya alama kutoka kwa
uchanganuzi wenyewe, lakini ripoti haina AIM Code ID ya kuonyesha, na msimbopau wa kawaida kama EAN-13 ni
vigumu zaidi kutofautishwa na mfuatano wa vipengele wa GS1.

### Kagua aina za alama

Hakikisha aina za misimbopau unazofanya nazo kazi **zimewashwa**: kwa kawaida GS1-128, GS1 DataMatrix,
GS1 DataBar, GS1 QR Code na EAN/UPC. Nyingi zimewashwa tangu mwanzo kwenye skana za 2D za Zebra; 123Scan
huonyesha mipangilio ya sasa, na Product Reference Guide ina msimbopau wa kuwasha kila moja.

### Acha vipengele vya uchanganuzi wa GS1 vya Zebra vikiwa vimezimwa

Programu dhibiti mpya huongeza vipengele vya GS1 vinavyofanya kazi kwenye skana yenyewe — **Label Parse+**,
**GS1 Digital Link** na **GS1 Retail POS parsing**. Viache **vimezimwa**, jinsi skana inavyokuja:
hubadilisha muundo au hufupisha kile ambacho skana hutuma, ilhali GAIA Desktop inahitaji data ghafi ili
kuichanganua na kuithibitisha. Tazama
[Skana za 2D za Zebra zinazoungwa mkono](#12-skana-za-2d-za-zebra-zinazoungwa-mkono) kwa kile kila kimoja
hufanya.

### Bila viambishi awali na tamati

Usiongeze kiambishi awali, kiambishi tamati wala kitufe cha Enter kwenye matokeo ya skana. GAIA Desktop
hupokea kila uchanganuzi kama ujumbe kamili kupitia JavaPOS na haihitaji chochote kati ya hivyo; herufi za
ziada zingeishia ndani ya mfuatano wa vipengele.

---

## 8. Sakinisha na uanzishe GAIA Desktop

1. Fungua `gaia-desktop-1.0.0-win.zip`, kwa mfano ndani ya `C:\Tools\`. Ina folda moja:

   ```
   gaia-desktop-1.0.0-win\
   ├── docs\
   │   └── GaiaDesktop-Zebra-Scanner-Windows.md      this guide
   ├── gaia-desktop.jar
   ├── lib\
   ├── readme.html                                   index of every guide
   └── run.bat
   ```

2. Chomeka skana.
3. Bofya mara mbili **`run.bat`**. Dirisha la koni hufunguka kando ya programu na hubaki wazi: hali ya skana
   na hitilafu za JavaPOS huripotiwa hapo.
4. Soma mstari wa kwanza kwenye koni. Unasema kile `run.bat` ilichopata:

   ```
   run.bat: Java 21.0.8 from JAVA_HOME; JavaFX from lib\; JavaPOS from lib\; Zebra services from C:\Program Files\Zebra Technologies\Barcode Scanners\Scanner SDK\JPOS\
   ```

   Ukiishia na **`Zebra services from none (Zebra Scanner SDK not installed)`**, SDK haikupatikana mahali
   pake chaguomsingi — isakinishe ([hatua 3](#3-sakinisha-zebra-scanner-sdk)) au weka
   [ZEBRA_HOME](#zebra_home). Programu bado huanza, lakini skana haiwezi kuunganika.

---

## 9. Unganisha skana

1. Kwenye upau wa zana, **kichaguzi cha kifaa** huorodhesha majina ya skana ambazo GAIA Desktop inazijua.
   Kiache kwenye **`ZebraAllScanners`**, au chagua jina la hali ya USB ya skana kutoka jedwali katika
   [hatua 6](#6-weka-hali-ya-usb-ya-skana-ya-zebra).
2. Bofya **Unganisha skana**, au bonyeza **Ctrl+K** (pia kipo kwenye menyu ya **Skana**).
3. Upau wa hali hubadilika kuwa **`skana: ZebraAllScanners`** (au jina ulilochagua), na mstari wa hali
   huripoti *Skana imeunganishwa — ZebraAllScanners*. Kitufe sasa kinasomeka **Tenganisha skana**.
4. Changanua msimbopau wa GS1. Unaonekana juu kabisa ya historia ya vilivyonaswa na ripoti yake hufunguka.
   AIM Code ID ikiwa imewashwa, ripoti hutaja mbeba data — *GS1-128*, *GS1 DataMatrix* — na mfuatano wa
   vipengele huhifadhi kila kitenganishi cha FNC1.

Kichaguzi cha kifaa hukumbuka chaguo lako kati ya uendeshaji mmoja na mwingine. Skana hubaki imeunganishwa
hadi uitenganishe au ufunge programu. Uchanganuzi pia hufanya kazi katika hali za **Tengeneza** na
**Unganisha**: katika Unganisha, kila uchanganuzi huwa mojawapo ya misimbopau inayounganishwa.

GAIA Desktop inaposhikilia skana, programu nyingine haziwezi kuitumia, na vivyo hivyo kinyume chake: funga
123Scan na programu nyingine yoyote ya mahali pa mauzo (point-of-sale) au ya skana kabla ya kuunganisha.

---

## 10. Marejeo ya usanidi

### Majina ya vifaa

Kichaguzi cha kifaa hujazwa kutoka `jpos.xml` iliyo ndani ya `gaia-desktop.jar`. Maingizo yake ya skana:

| Jina la kifaa | Aina ya skana | Hutumika na |
|---|---|---|
| `ZebraAllScanners` | yoyote | **Chaguomsingi.** Hali yoyote ya USB inayoungwa mkono |
| `ZebraAllScanners114` | yoyote | Toleo la JavaPOS 1.14 la huduma hiyo hiyo |
| `ZebraUSBHandHeld` | IBM Hand-held USB | Skana katika hali ya IBM Hand-held USB |
| `ZebraUSBTableTop` | IBM Table-top USB | Hali ya mezani |
| `ZebraUSBOPOS` | USB OPOS | Hali ya USB OPOS |
| `ZebraScannerSNAPI` | SNAPI | Hali ya SNAPI |
| `ZebraScannerUSB` | USB yoyote | Hali yoyote ya USB |
| `ZebraScannerSerialSSI` | serial SSI | Skana za kebo ya serial (si USB) |
| `ZebraScannerSerialNIXMODB` | serial NIXMODB | Skana za kebo ya serial |
| `ZebraVDI` | VDI | Skana zinazopitishwa kwenye kompyuta ya mezani pepe |

### ZEBRA_HOME

Ikiwa Zebra SDK imesakinishwa mahali pengine tofauti na chaguomsingi, weka `ZEBRA_HOME` kuwa folda yake ya
`JPOS` kabla ya kuanzisha `run.bat` — kwa mfano katika faili dogo la batch kando yake:

```bat
@echo off
set "ZEBRA_HOME=D:\Zebra\Scanner SDK\JPOS\"
call "%~dp0run.bat" %*
```

Kisha `run.bat` huchukua jar za huduma za Zebra kutoka `%ZEBRA_HOME%bin` na kuongeza folda hiyo kwenye
`PATH` kwa ajili ya DLL za Zebra.

### jpos.xml yako mwenyewe

Ili kuongeza jina la kifaa au kubadilisha mipangilio ya ingizo, nakili `jpos.xml` kutoka ndani ya
`gaia-desktop.jar` (ni zip; zana yoyote ya kufungua zip huifungua), hariri nakala, na uielekeze
GAIA Desktop kwake:

```bat
set GAIA_DESKTOP_OPTS=-Djpos.config.populatorFileURL=file:///C:/Tools/jpos.xml
run.bat
```

Kisha kichaguzi cha kifaa huorodhesha skana zilizo katika faili hilo. Skana zaidi ya moja za Zebra
zikiunganishwa, ingizo linaweza kufungamanishwa na mojawapo kupitia sifa yake ya `ScannerModel` au
`SerialNumber`, kama ilivyo katika ingizo la `ZebraAllScanners`.

### Machaguo mengine

| Mpangilio | Jinsi |
|---|---|
| Lugha ya programu | `run.bat --language=FR` (kwa uendeshaji huu pekee), au Paneli ya machaguo |
| Machaguo ya ziada ya Java | `set GAIA_DESKTOP_OPTS=…` kabla ya `run.bat` |
| Java iliyoambatanishwa | weka folda ya `jdk-21…` kando ya `run.bat`; hutumika kabla ya `JAVA_HOME` |

---

## 11. Utatuzi wa matatizo

Hitilafu huonekana kwenye kidirisha wakati wa kuunganisha, kwenye dirisha la koni, na kwenye faili la
kumbukumbu `logs\gaia-desktop.log`, katika folda ambamo `run.bat` ilianzishwa (folda ya GAIA Desktop,
`run.bat` inapobofywa mara mbili). Nambari iliyo ndani ya mabano ya mraba kwenye kumbukumbu ni msimbo wa
hitilafu wa JavaPOS.

| Unachokiona | Sababu inayowezekana | La kufanya |
|---|---|---|
| Hakuna kitufe cha *Unganisha skana* | Haiendeshwi kwenye Windows | Uchanganuzi ni wa Windows pekee |
| 123Scan haipati skana | Kebo, mlango wa USB, au skana imeshikiliwa na programu nyingine | Jaribu kebo au mlango mwingine; funga GAIA Desktop na programu nyingine za skana. GAIA Desktop haitaona skana ambayo 123Scan haioni |
| Koni: `Zebra services from none (Zebra Scanner SDK not installed)` | SDK haiko mahali pake chaguomsingi | Sakinisha SDK ya biti 64 ([hatua 3](#3-sakinisha-zebra-scanner-sdk)), au weka [ZEBRA_HOME](#zebra_home) |
| Hitilafu **104**: *Could not connect to service with logicalName = …: …SymScannerSvc112Factory* | Jar za huduma za Zebra hazikupatikana | Kama hapo juu — kagua mstari wa `Zebra services from` kwenye koni |
| Hitilafu **109**: *Service does not exist in loaded JCL registry* | Jina la kifaa halimo katika usanidi wa skana, au `javaPOS114.jar` ya SDK imeingia kwenye class path | Chagua jina kutoka kichaguzi cha kifaa; usiongeze `javaPOS114.jar`; ukitumia `jpos.xml` yako mwenyewe, hakikisha jina limo ndani yake |
| Hitilafu **107** (hakuna maunzi), au kuunganisha kunashindwa ingawa skana imechomekwa | Skana bado iko katika hali ya kibodi ya USB, au kiendeshi cha CoreScanner hakifanyi kazi | Weka hali ya USB ([hatua 6](#6-weka-hali-ya-usb-ya-skana-ya-zebra)); anzisha upya Windows baada ya kusakinisha SDK; jaribu mlango mwingine wa USB |
| Hitilafu **112** (muda umekwisha) au **102** (imeshikiliwa) wakati wa kuunganisha | Programu nyingine imeshikilia skana | Funga 123Scan na programu nyingine yoyote ya skana au ya mahali pa mauzo, kisha unganisha tena |
| `UnsatisfiedLinkError` … `CSJPOSScanner64` kwenye koni | DLL za Zebra haziwezi kupakiwa — mara nyingi SDK ya biti 32 pamoja na Java ya biti 64 | Sakinisha SDK ya biti 64; hakikisha `java -version` inaripoti VM ya biti 64 |
| Uchanganuzi unaandikwa kwenye upau wa kuingiza badala ya kuwasili kama vilivyonaswa | Skana iko katika hali ya kibodi ya USB | Weka hali ya USB ([hatua 6](#6-weka-hali-ya-usb-ya-skana-ya-zebra)) |
| Imeunganishwa, lakini uchanganuzi hauwasili | Jina la kifaa si sahihi kwa hali ya skana, au skana ilichomekwa upya ikiwa imeunganishwa | Tenganisha, chagua `ZebraAllScanners`, unganisha tena |
| Ripoti haionyeshi AIM Code ID / mbeba data | Utumaji wa AIM Code ID umezimwa | Washa *Transmit Code ID Character → AIM Code ID* ([hatua 7](#7-sanidi-skana-ya-zebra-kwa-gs1)) |
| Mfuatano wa vipengele umekosa vitenganishi, au AI zimeshikamana | Ulichanganuliwa katika hali ya kibodi, au uliandikwa kwa mkono bila `{GS}` | Tumia hali ya USB ya JavaPOS; unapoandika, weka vitenganishi kwa kitufe cha `{GS}` |
| Aina fulani ya msimbopau haisomwi kamwe | Aina hiyo ya alama imezimwa kwenye skana | Iwashe ([hatua 7](#7-sanidi-skana-ya-zebra-kwa-gs1)) |

Tatizo likiendelea, hifadhi pamoja mstari wa `run.bat:` wa koni, maandishi ya kidirisha cha hitilafu na
`logs\gaia-desktop.log`: kwa pamoja huonyesha ni Java ipi, huduma zipi za Zebra na jina lipi la kifaa
vilitumika, na hasa ni wapi muunganisho ulishindwa.

---

## 12. Skana za 2D za Zebra zinazoungwa mkono

GAIA Desktop inahitaji skana inayoweza kusoma misimbopau ambayo data ya GS1 huchapishwa kwayo — GS1-128,
GS1 DataBar, EAN/UPC, na, kwa matumizi mengi, misimbo ya 2D: **GS1 DataMatrix** na **GS1 QR Code**, ambayo
pia ndiyo njia ambayo URI ya **GS1 Digital Link** kwa kawaida huchapishwa. Hiyo inamaanisha **kipiga-picha
cha 2D** cha Zebra: skana ya mstari (1D) haiwezi kabisa kusoma misimbo ya DataMatrix au QR.

Skana yenyewe haihitaji kuelewa GS1 Application Identifiers wala Digital Link.
**GAIA Desktop huchanganua vyote viwili**: huchukua uchanganuzi kama skana ilivyousoma — AIM Code ID,
vitenganishi vya FNC1 na vyote — na yenyewe huugawanya kuwa AI, au hubadilisha URI ya Digital Link kuwa AI
zake.

### Imejaribiwa

| Skana | Aina | Hali |
|---|---|---|
| **DS8108** | Kipiga-picha cha 2D cha mkononi chenye waya | **Imejengwa na kujaribiwa** na GAIA Desktop |

### Zinatarajiwa kufanya kazi

Hivi ni vipiga-picha vya 2D vya Zebra ambavyo, kama DS8108, huendeshwa kupitia Scanner SDK na huduma za
JavaPOS za Zebra. **Havijajaribiwa** na GAIA Desktop; weka kila kimoja kama katika mwongozo huu, ukitumia
Quick Reference Guide au Product Reference Guide yake kwa misimbopau ya kuweka.

| Familia | Modeli (mifano) |
|---|---|
| DS81x8 | DS8108 (ya waya), DS8178 (isiyo na waya) |
| DS22x8 | DS2208 (ya waya), DS2278 (isiyo na waya) — matumizi ya jumla |
| DS36x8 | DS3608 (ya waya), DS3678 (isiyo na waya) — imara, pamoja na aina za KD na XR |
| DS46x8 | DS4608 (ya waya), DS4678 (isiyo na waya) |
| DS9308 / DS9908 | Skana za maonyesho na zisizohitaji mikono |
| MP7000 / MP7200 | Skana za bioptic za ndege nyingi na skana-mizani |
| CS6080 | Skana msaidizi isiyo na waya |

Skana isiyo na waya huzungumza na kitako chake, na kitako ndicho kinachochomekwa kwenye kompyuta: unganisha
kitako kwa USB na uchukulie jozi hiyo kama skana moja. Skana za familia nyingine za Zebra zinaweza pia
kufanya kazi, ikiwa Scanner SDK ya Zebra inaziunga mkono katika mojawapo ya hali za USB zilizo katika
[hatua 6](#6-weka-hali-ya-usb-ya-skana-ya-zebra).

### Vipengele vya GS1 kwenye skana ya Zebra yenyewe

Mbali na kusoma misimbopau, baadhi ya vipiga-picha vya 2D vya Zebra vinaweza kuchakata data ya GS1 kwenye
skana yenyewe, vikisanidiwa kupitia 123Scan:

| Kipengele | Kinachofanya | Kwa GAIA Desktop |
|---|---|---|
| **Label Parse+** | Huchanganua AI za lebo ya GS1 kwenye skana na kutuma sehemu zilizobadilishwa muundo | **Kiache kimezimwa** — GAIA Desktop haitaona tena AI, vitenganishi vya FNC1 na AIM Code ID |
| **GS1 Digital Link** | Kwa bidhaa za rejareja zenye EAN/UPC ya 1D na msimbo wa Digital Link wa 2D pamoja: huchagua kipi kitumwe | **Kiache kimezimwa** (chaguomsingi) |
| **GS1 Retail POS parsing** | Husoma misimbopau ya rejareja ya 2D katika sintaksia ya URI ya Digital Link au ya mfuatano wa vipengele | **Kiache kimezimwa** |

Mpangilio wa **GS1 Digital Link** una hali tatu. **Mode 1 — GTIN only**, hali unayoanza nayo ukishauwasha,
hutuma GTIN pekee, kutoka msimbopau wowote ule, kwa hivyo bechi, tarehe ya kuisha muda au nambari ya
utambulisho iliyo katika Digital Link haitawahi kufika GAIA Desktop. **Mode 2 — Prioritize 2D** hutuma
msimbo wa 2D ukionekana ndani ya muda wa kipaumbele (ms 200 kwa chaguomsingi), la sivyo wa 1D. **Mode 3 —
Corroborate 1D and 2D GTINs Match** hutuma wa 1D, na wa 2D pia GTIN yake ikilingana. Ikiwa ni lazima
mpangilio uwe umewashwa kwa ajili ya programu nyingine, tumia Mode 2, si Mode 1. Kwenye DS8108 hivi ni
vigezo 2373 (washa/zima), 2374 (hali) na 2491 (muda wa kusubiri).

Kutumia vipengele hivi kunahitaji **123Scan toleo la 6 au jipya zaidi** na programu dhibiti yenye vipengele
hivyo. Zebra imekuwa ikiongeza usaidizi wa GS1 Digital Link kupitia masasisho ya programu dhibiti tangu Aprili
2025; tarehe zilizo hapa chini zimetoka kwa muuzaji wa Zebra, kwa hivyo kagua maelezo ya toleo la programu
dhibiti ya kila modeli:

| Familia ya modeli | Kutolewa kwa programu dhibiti ya GS1 Digital Link |
|---|---|
| DS81x8 | 16 Aprili 2025 |
| MP7000 | 21 Aprili 2025 |
| DS9908 | 6 Juni 2025 |
| SP72 / SM72 | 11 Julai 2025 |
| DS46x8 | 28 Julai 2025 |
| MP7200 (Rev-C), DS5502 | 30 Julai 2025 |
| DS9308 | 29 Agosti 2025 |
| DS22x8, DS1000 | 5 Septemba 2025 (Asia-Pasifiki pekee) |
| DS36x8 (pamoja na KD na XR), MS47 | 30 Septemba 2025 |
| PL5000, SE4107 | 15 Oktoba 2025 |
| CS6080 | 31 Desemba 2025 |

Hakuna kati ya vipengele hivi kinachohitajika kusoma misimbo ya GS1 Digital Link katika GAIA Desktop:
vikiwa vimezimwa, skana ya 2D ya Zebra yenye programu dhibiti ya sasa husoma msimbo wa QR wa Digital Link
kama mwingine wowote, na GAIA Desktop hubadilisha URI kuwa AI zake.

Vyanzo: Zebra, *DS8108 Product Reference Guide*, MN-002926-11EN Rev A (Agosti 2025), sura ya 12
"GS1 Digital Link Parameters" na sura ya Data Parsing; Zebra, *123Scan release notes*;
Barcode Factory, *Upgrade Zebra Scanners for GS1 Digital Link* (tarehe za programu dhibiti).
