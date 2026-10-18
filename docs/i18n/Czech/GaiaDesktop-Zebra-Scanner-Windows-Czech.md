# Nastavení skeneru Zebra — GAIA Desktop pro Windows

Jak nainstalovat a nastavit čtečku čárových kódů Zebra tak, aby z ní GAIA Desktop četl čárové kódy
GS1 přímo: každé snímání se analyzuje a přidá do historie, s nedotčeným prefixem AIM Code ID a
každým oddělovačem FNC1. Originál:
[GaiaDesktop-Zebra-Scanner-Windows.md](../../GaiaDesktop-Zebra-Scanner-Windows.md).

GAIA Desktop byl vyvinut a otestován s ručním imagerem **Zebra DS8108** připojeným přes USB. Kroky
platí pro každý 2D skener Zebra, který podporuje Scanner SDK od Zebry; nastavovací čárové kódy a
názvy nabídek konkrétního modelu najdete v jeho vlastní příručce Quick Reference Guide nebo Product
Reference Guide.

> **Pouze Windows.** Ovladače skenerů Zebra existují jen pro Windows, a proto GAIA Desktop v macOS
> a Linuxu nevytváří žádné ovládací prvky skeneru. Vše ostatní v aplikaci funguje na každé
> platformě.

---

## Obsah

1. [Co budete potřebovat](#1-co-budete-potřebovat)
2. [Instalace Javy](#2-instalace-javy)
3. [Instalace Zebra Scanner SDK](#3-instalace-zebra-scanner-sdk)
4. [Instalace 123Scan](#4-instalace-123scan)
5. [Aktualizace firmwaru skeneru Zebra](#5-aktualizace-firmwaru-skeneru-zebra)
6. [Nastavení režimu USB skeneru Zebra](#6-nastavení-režimu-usb-skeneru-zebra)
7. [Konfigurace skeneru Zebra pro GS1](#7-konfigurace-skeneru-zebra-pro-gs1)
8. [Instalace a spuštění GAIA Desktop](#8-instalace-a-spuštění-gaia-desktop)
9. [Připojení skeneru](#9-připojení-skeneru)
10. [Přehled konfigurace](#10-přehled-konfigurace)
11. [Řešení potíží](#11-řešení-potíží)
12. [Podporované 2D skenery Zebra](#12-podporované-2d-skenery-zebra)

---

## 1. Co budete potřebovat

| | |
|---|---|
| **Skener** | 2D skener Zebra s kabelem USB — viz [Podporované 2D skenery Zebra](#12-podporované-2d-skenery-zebra) |
| **Počítač** | Windows 10 nebo 11, 64bitový |
| **Java** | Java 21 nebo novější, **64bitová** |
| **Zebra Scanner SDK pro Windows** | **64bitový**, ze stránek podpory Zebra — poskytuje ovladač a služby JavaPOS, které GAIA Desktop používá |
| **Zebra 123Scan** | Bezplatný konfigurační nástroj Zebra, ze stránek podpory — slouží k aktualizaci firmwaru skeneru a kontrole jeho nastavení |
| **GAIA Desktop** | `gaia-desktop-1.0.0-win.zip` |
| **Volitelně** | **Product Reference Guide** (PRG) skeneru s nastavovacími čárovými kódy |

Jak do sebe části zapadají:

```
Zebra scanner ──USB──► Zebra CoreScanner driver ──► Zebra JavaPOS services ──► GAIA Desktop
                       (installed by the SDK)        (in the SDK's JPOS\bin)    (run.bat puts them on
                                                                                 the class path)
```

GAIA Desktop obsahuje samotné API JavaPOS a konfiguraci, která skener pojmenovává (`jpos.xml` v
`gaia-desktop.jar`). Zbytek dodává SDK od Zebry: ovladač, který se skenerem komunikuje, a třídy
služeb Zebra, které stojí mezi JavaPOS a tímto ovladačem.

---

## 2. Instalace Javy

GAIA Desktop vyžaduje **Javu 21 nebo novější**, a to ve **64bitové** verzi, aby odpovídala
64bitovým ovladačům Zebra.

1. Nainstalujte JDK nebo JRE Javy 21 pro Windows x64 — například Eclipse Temurin z adoptium.net.
   Pokud instalátor nabídne nastavení `JAVA_HOME`, dovolte mu to.
2. Ověřte to v příkazovém řádku:

   ```
   java -version
   ```

   První řádek by měl ukázat verzi 21 nebo vyšší; 64bitová Java navíc vypíše
   `64-Bit Server VM`.

`run.bat` hledá Javu v tomto pořadí: složka `jdk*` vedle `run.bat`, pak `JAVA_HOME`, pak `java` v
`PATH`. Cokoli staršího než 21 odmítne se zprávou, která to říká.

---

## 3. Instalace Zebra Scanner SDK

1. Stáhněte **Zebra Scanner SDK pro Windows (64bitový)** ze stránek podpory Zebra.
2. Spusťte instalátor. Pokud nabízí výběr součástí, ujistěte se, že je zahrnuta součást **JPOS**
   (JavaPOS).
3. Ponechte výchozí umístění instalace. GAIA Desktop hledá část SDK pro JavaPOS v:

   ```
   C:\Program Files\Zebra Technologies\Barcode Scanners\Scanner SDK\JPOS\
   ```

   a očekává servisní soubory JAR od Zebry (`JposServiceScanner.jar`, `JposServiceJniScanner.jar`, …)
   a nativní knihovny (`CSJPOSScanner64.dll`, …) v jeho složce `bin\`. Pokud je SDK nainstalován
   jinde, viz [ZEBRA_HOME](#zebra_home).
4. Po instalaci **restartujte Windows**. Ovladač CoreScanner ze SDK běží na pozadí a musí běžet
   dřív, než GAIA Desktop může skener oslovit; restart je nejjednodušší způsob, jak si tím být jisti.

---

## 4. Instalace 123Scan

123Scan je bezplatný nástroj Zebry pro nastavování jejích skenerů. V dalším kroku se jím aktualizuje
firmware skeneru a poté je to nejjednodušší způsob, jak kontrolovat a měnit nastavení skeneru.

1. Stáhněte **123Scan** ze stránek podpory Zebra a spusťte instalátor.
2. Připojte skener Zebra a spusťte 123Scan. Měl by skener najít a zobrazit jeho model, sériové
   číslo a aktuální verzi firmwaru. Pokud ne, zkuste před pokračováním jiný port USB nebo kabel —
   GAIA Desktop neuvidí skener, který nevidí 123Scan.

---

## 5. Aktualizace firmwaru skeneru Zebra

Před konfigurací aktualizujte firmware skeneru na nejnovější verzi. Aktualizace firmwaru opravují
potíže s dekódováním a udržují skener v souladu s ovladačem Zebra, který nainstaloval SDK.

1. Zavřete **GAIA Desktop** a jakýkoli jiný program, který by mohl skener používat: 123Scan
   potřebuje mít skener během aktualizace jen pro sebe.
2. V 123Scan při připojeném skeneru zvolte jeho možnost aktualizace firmwaru. 123Scan porovná
   firmware skeneru s nejnovějším, který Zebra vydala, a nabídne jeho stažení a instalaci. Pokud
   počítač nemá přístup k internetu, stáhněte firmware ze stránky podpory skeneru na webu Zebry a
   načtěte soubor do 123Scan.
3. Spusťte aktualizaci a **nechte skener připojený, dokud 123Scan neohlásí dokončení**. Trvá to
   několik minut; na konci se skener restartuje. Odpojení v průběhu ho může učinit nepoužitelným.
4. Ověřte, že 123Scan nyní zobrazuje novou verzi firmwaru.

Aktualizace může některá nastavení vrátit na výchozí hodnoty; proto se skener konfiguruje v dalších
dvou krocích, až po firmwaru.

---

## 6. Nastavení režimu USB skeneru Zebra

Tento krok se vynechává nejčastěji. **Z výroby je skener Zebra klávesnicí USB** (režim USB HID
Keyboard): každý čárový kód „napíše“ tam, kde je fokus. V tomto režimu ho *Připojit skener* v GAIA
Desktop nenajde a vše, co se napíše do textového pole, ztratí oddělovače FNC1, čímž se změní význam
řetězce prvků GS1.

Přepněte skener do režimu, se kterým ovladač Zebra komunikuje přímo:

| Režim USB skeneru | Název zařízení v GAIA Desktop | Poznámky |
|---|---|---|
| **IBM Hand-held USB** | `ZebraUSBHandHeld` nebo `ZebraAllScanners` | Doporučeno |
| SNAPI (se snímáním obrazu nebo bez) | `ZebraScannerSNAPI` nebo `ZebraAllScanners` | Vlastní protokol USB od Zebry; také funguje |
| IBM Table-top USB | `ZebraUSBTableTop` nebo `ZebraAllScanners` | Určeno pro stolní skenery |
| USB OPOS (Hand-held) | `ZebraUSBOPOS` nebo `ZebraAllScanners` | |
| USB HID Keyboard (tovární nastavení) | — | **Nepřipojí se** |

`ZebraAllScanners`, výchozí název zařízení v GAIA Desktop, přijme skener v kterémkoli z
podporovaných režimů, takže po nastavení režimu výchozí hodnota obvykle prostě funguje.

Režim změníte jedním ze dvou způsobů:

- **naskenujte nastavovací čárový kód** režimu — například *IBM Hand-held USB* — z příručky Quick
  Reference Guide nebo Product Reference Guide skeneru (oddíl USB Host Types / USB Device Type);
  skener to potvrdí pípnutím; nebo
- **použijte 123Scan** ([krok 4](#4-instalace-123scan)): připojte skener, otevřete jeho konfiguraci a
  nastavte typ hostitele USB.

Po změně režimu Windows skener znovu rozpozná jako jiný druh zařízení; před připojením počkejte
několik sekund.

Chcete-li skener později vrátit do režimu klávesnice — kvůli jinému softwaru — naskenujte nastavovací
čárový kód *USB Keyboard (HID)* nebo obnovte tovární nastavení.

---

## 7. Konfigurace skeneru Zebra pro GS1

GAIA Desktop analyzuje nejlépe, když mu skener sdělí, jaký druh čárového kódu přečetl.

### Odesílání AIM Code ID

Nastavte **Transmit Code ID Character** na **AIM Code ID Character** — nastavovacím čárovým kódem z
Product Reference Guide (User Preferences) nebo v 123Scan. Každé snímání pak začíná identifikátorem
symboliky — `]C1` pro GS1-128, `]d2` pro GS1 DataMatrix, `]Q3` pro GS1 QR Code, `]e0` pro
GS1 DataBar —, podle kterého GAIA Desktop pojmenuje nosič dat a použije jeho pravidla.

Bez něj GAIA Desktop řetězec prvků stejně analyzuje a symboliku převezme ze samotného snímání, ale
report nemá žádné AIM Code ID k zobrazení a obyčejný čárový kód, například EAN-13, se hůř odlišuje od
řetězce prvků GS1.

### Kontrola symbolik

Ujistěte se, že druhy čárových kódů, se kterými pracujete, jsou **zapnuté**: obvykle GS1-128,
GS1 DataMatrix, GS1 DataBar, GS1 QR Code a EAN/UPC. U 2D skenerů Zebra je většina zapnutá ve
výchozím stavu; 123Scan ukazuje aktuální nastavení a Product Reference Guide obsahuje pro každou
čárový kód, který ji zapne.

### Funkce analýzy GS1 od Zebry nechte vypnuté

Novější firmware přidává funkce GS1, které pracují přímo ve skeneru — **Label Parse+**,
**GS1 Digital Link** a **GS1 Retail POS parsing**. Nechte je **vypnuté**, jak se skener dodává:
přeformátují nebo zkracují to, co skener posílá, a GAIA Desktop potřebuje nezpracovaná data, aby je
analyzoval a ověřil. Co která dělá, viz
[Podporované 2D skenery Zebra](#12-podporované-2d-skenery-zebra).

### Bez prefixů a sufixů

Nepřidávejte k výstupu skeneru prefix, sufix ani klávesu Enter. GAIA Desktop přijímá každé snímání
jako úplnou zprávu přes JavaPOS a nic z toho nepotřebuje; nadbytečné znaky by skončily v řetězci
prvků.

---

## 8. Instalace a spuštění GAIA Desktop

1. Rozbalte `gaia-desktop-1.0.0-win.zip`, například do `C:\Tools\`. Obsahuje jedinou složku:

   ```
   gaia-desktop-1.0.0-win\
   ├── docs\
   │   └── GaiaDesktop-Zebra-Scanner-Windows.md      this guide
   ├── gaia-desktop.jar
   ├── lib\
   ├── readme.html                                   index of every guide
   └── run.bat
   ```

2. Připojte skener.
3. Poklepejte na **`run.bat`**. Vedle aplikace se otevře okno konzole a zůstane otevřené: hlásí se v
   něm stav skeneru a chyby JavaPOS.
4. Přečtěte si první řádek konzole. Říká, co `run.bat` našel:

   ```
   run.bat: Java 21.0.8 from JAVA_HOME; JavaFX from lib\; JavaPOS from lib\; Zebra services from C:\Program Files\Zebra Technologies\Barcode Scanners\Scanner SDK\JPOS\
   ```

   Pokud končí na **`Zebra services from none (Zebra Scanner SDK not installed)`**, SDK nebyl ve
   výchozím umístění nalezen — nainstalujte ho ([krok 3](#3-instalace-zebra-scanner-sdk)) nebo
   nastavte [ZEBRA_HOME](#zebra_home). Aplikace se i tak spustí, ale skener se připojit nemůže.

---

## 9. Připojení skeneru

1. Na panelu nástrojů **výběr zařízení** uvádí názvy skenerů, které GAIA Desktop zná. Ponechte
   **`ZebraAllScanners`** nebo vyberte název pro režim USB skeneru z tabulky v
   [kroku 6](#6-nastavení-režimu-usb-skeneru-zebra).
2. Klepněte na **Připojit skener** nebo stiskněte **Ctrl+K** (je i v nabídce **Skener**).
3. Stavový řádek se změní na **`skener: ZebraAllScanners`** (nebo vybraný název) a
   stavová zpráva oznámí *Skener připojen*. Tlačítko teď nese nápis
   **Odpojit skener**.
4. Naskenujte čárový kód GS1. Objeví se nahoře v historii záznamů a otevře se jeho report. Se
   zapnutým AIM Code ID report pojmenuje nosič dat — *GS1-128*, *GS1 DataMatrix* — a řetězec prvků
   zachová každý oddělovač FNC1.

Výběr zařízení si vaši volbu pamatuje mezi spuštěními. Skener zůstane připojený, dokud ho neodpojíte
nebo nezavřete aplikaci. Snímání funguje i v režimech **Sestavení** a **Spojení**: v
režimu Spojení se každé snímání stane jedním ze spojovaných čárových kódů.

Dokud skener drží GAIA Desktop, jiné programy ho používat nemohou, a naopak: před připojením
zavřete 123Scan a veškerý další pokladní software nebo software pro skenery.

---

## 10. Přehled konfigurace

### Názvy zařízení

Výběr zařízení se plní ze souboru `jpos.xml` dodávaného v `gaia-desktop.jar`. Jeho záznamy skenerů:

| Název zařízení | Typ skeneru | Použití |
|---|---|---|
| `ZebraAllScanners` | libovolný | **Výchozí.** Jakýkoli podporovaný režim USB |
| `ZebraAllScanners114` | libovolný | Verze téže služby pro JavaPOS 1.14 |
| `ZebraUSBHandHeld` | IBM Hand-held USB | Skener v režimu IBM Hand-held USB |
| `ZebraUSBTableTop` | IBM Table-top USB | Režim table-top |
| `ZebraUSBOPOS` | USB OPOS | Režim USB OPOS |
| `ZebraScannerSNAPI` | SNAPI | Režim SNAPI |
| `ZebraScannerUSB` | libovolné USB | Jakýkoli režim USB |
| `ZebraScannerSerialSSI` | sériový SSI | Skenery se sériovým kabelem (ne USB) |
| `ZebraScannerSerialNIXMODB` | sériový NIXMODB | Skenery se sériovým kabelem |
| `ZebraVDI` | VDI | Skenery předávané do virtuální plochy |

### ZEBRA_HOME

Pokud je Zebra SDK nainstalován jinde než ve výchozím umístění, nastavte před spuštěním `run.bat`
proměnnou `ZEBRA_HOME` na jeho složku `JPOS` — například v malém dávkovém souboru vedle něj:

```bat
@echo off
set "ZEBRA_HOME=D:\Zebra\Scanner SDK\JPOS\"
call "%~dp0run.bat" %*
```

`run.bat` pak vezme servisní soubory JAR od Zebry z `%ZEBRA_HOME%bin` a přidá tuto složku do `PATH`
kvůli knihovnám DLL od Zebry.

### Vlastní jpos.xml

Chcete-li přidat název zařízení nebo změnit nastavení záznamu, zkopírujte `jpos.xml` ven z
`gaia-desktop.jar` (je to zip; otevře ho jakýkoli rozbalovací nástroj), upravte kopii a nasměrujte
na ni GAIA Desktop:

```bat
set GAIA_DESKTOP_OPTS=-Djpos.config.populatorFileURL=file:///C:/Tools/jpos.xml
run.bat
```

Výběr zařízení pak uvádí skenery z tohoto souboru. Je-li připojeno více skenerů Zebra, lze záznam
svázat s jedním z nich vlastností `ScannerModel` nebo `SerialNumber`, jako v záznamu
`ZebraAllScanners`.

### Další možnosti

| Nastavení | Jak |
|---|---|
| Jazyk aplikace | `run.bat --language=FR` (jen pro toto spuštění) nebo Panel možností |
| Další volby Javy | `set GAIA_DESKTOP_OPTS=…` před `run.bat` |
| Přibalená Java | umístěte složku `jdk-21…` vedle `run.bat`; použije se dřív než `JAVA_HOME` |

---

## 11. Řešení potíží

Chyby se objevují v dialogovém okně při připojování, v okně konzole a v souboru protokolu
`logs\gaia-desktop.log` ve složce, ze které byl spuštěn `run.bat` (ve složce GAIA Desktop, pokud se
na `run.bat` poklepe). Číslo v hranatých závorkách v protokolu je kód chyby JavaPOS.

| Co vidíte | Pravděpodobná příčina | Co dělat |
|---|---|---|
| Chybí tlačítko *Připojit skener* | Nejde o Windows | Snímání existuje jen ve Windows |
| 123Scan skener nenajde | Kabel, port USB, nebo skener drží jiný program | Zkuste jiný kabel nebo port; zavřete GAIA Desktop a další software pro skenery. GAIA Desktop neuvidí skener, který nevidí 123Scan |
| Konzole: `Zebra services from none (Zebra Scanner SDK not installed)` | SDK není ve výchozím umístění | Nainstalujte 64bitový SDK ([krok 3](#3-instalace-zebra-scanner-sdk)) nebo nastavte [ZEBRA_HOME](#zebra_home) |
| Chyba **104**: *Could not connect to service with logicalName = …: …SymScannerSvc112Factory* | Servisní soubory JAR od Zebry nebyly nalezeny | Jako výše — zkontrolujte řádek `Zebra services from` v konzoli |
| Chyba **109**: *Service does not exist in loaded JCL registry* | Název zařízení není v konfiguraci skeneru, nebo se `javaPOS114.jar` ze SDK dostal do class path | Vyberte název z výběru zařízení; nepřidávejte `javaPOS114.jar`; používáte-li vlastní `jpos.xml`, ověřte, že v něm název je |
| Chyba **107** (žádný hardware), nebo připojení selže při připojeném skeneru | Skener je stále v režimu klávesnice USB, nebo ovladač CoreScanner neběží | Nastavte režim USB ([krok 6](#6-nastavení-režimu-usb-skeneru-zebra)); po instalaci SDK restartujte Windows; zkuste jiný port USB |
| Chyba **112** (vypršení času) nebo **102** (obsazeno) při připojování | Skener drží jiný program | Zavřete 123Scan a veškerý další software pro skenery nebo pokladní software a připojte se znovu |
| `UnsatisfiedLinkError` … `CSJPOSScanner64` v konzoli | Knihovny DLL od Zebry nelze načíst — často 32bitový SDK se 64bitovou Javou | Nainstalujte 64bitový SDK; ověřte, že `java -version` hlásí 64bitový VM |
| Snímání se píše do vstupního řádku, místo aby přicházelo jako záznamy | Skener je v režimu klávesnice USB | Nastavte režim USB ([krok 6](#6-nastavení-režimu-usb-skeneru-zebra)) |
| Připojeno, ale snímání nepřichází | Nesprávný název zařízení pro režim skeneru, nebo byl skener během připojení znovu zapojen | Odpojte, vyberte `ZebraAllScanners`, připojte znovu |
| Report neukazuje AIM Code ID ani nosič dat | Odesílání AIM Code ID je vypnuté | Zapněte *Transmit Code ID Character → AIM Code ID* ([krok 7](#7-konfigurace-skeneru-zebra-pro-gs1)) |
| Řetězci prvků chybějí oddělovače, nebo AI splývají | Snímáno v režimu klávesnice, nebo psáno ručně bez `{GS}` | Použijte režim USB s JavaPOS; při psaní vkládejte oddělovače tlačítkem `{GS}` |
| Některý druh čárového kódu se nikdy nepřečte | Tato symbolika je ve skeneru vypnutá | Zapněte ji ([krok 7](#7-konfigurace-skeneru-zebra-pro-gs1)) |

Pokud potíže přetrvávají, uschovejte spolu řádek `run.bat:` z konzole, text chybového dialogu a
`logs\gaia-desktop.log`: dohromady ukazují, která Java, které služby Zebra a který název zařízení
byly použity a kde přesně připojení selhalo.

---

## 12. Podporované 2D skenery Zebra

GAIA Desktop potřebuje skener, který umí číst čárové kódy, v nichž se tisknou data GS1 — GS1-128,
GS1 DataBar, EAN/UPC a pro většinu použití 2D kódy **GS1 DataMatrix** a **GS1 QR Code**, ve kterém
se obvykle tiskne i URI **GS1 Digital Link**. To znamená 2D **imager** Zebra: lineární (1D) skener
kódy DataMatrix ani QR vůbec nepřečte.

Skener sám nemusí rozumět aplikačním identifikátorům GS1 ani Digital Link. **GAIA Desktop analyzuje
obojí**: vezme snímání tak, jak ho skener přečetl — s AIM Code ID, oddělovači FNC1 a vším ostatním — a
sám ho rozdělí na AI, nebo převede URI Digital Link na jeho AI.

### Otestováno

| Skener | Typ | Stav |
|---|---|---|
| **DS8108** | Kabelový ruční 2D imager | **Vyvinuto a otestováno** s GAIA Desktop |

### Mělo by fungovat

Jde o 2D imagery Zebra, které se stejně jako DS8108 ovládají přes Scanner SDK a služby JavaPOS od
Zebry. S GAIA Desktop **nebyly otestovány**; každý nastavte podle této příručky a pro nastavovací
čárové kódy použijte jeho vlastní Quick Reference Guide nebo Product Reference Guide.

| Řada | Modely (příklady) |
|---|---|
| DS81x8 | DS8108 (kabelový), DS8178 (bezdrátový) |
| DS22x8 | DS2208 (kabelový), DS2278 (bezdrátový) — univerzální |
| DS36x8 | DS3608 (kabelový), DS3678 (bezdrátový) — odolné, včetně variant KD a XR |
| DS46x8 | DS4608 (kabelový), DS4678 (bezdrátový) |
| DS9308 / DS9908 | Prezentační skenery a skenery pro práci bez rukou |
| MP7000 / MP7200 | Víceplošné bioptické skenery a skenery s váhou |
| CS6080 | Bezdrátový doprovodný (companion) skener |

Bezdrátový skener komunikuje se svou základnou a k počítači se připojuje právě základna: připojte ji
přes USB a s párem zacházejte jako s jedním skenerem. Fungovat mohou i skenery z jiných řad Zebra,
pokud je Scanner SDK od Zebry podporuje v některém z režimů USB z
[kroku 6](#6-nastavení-režimu-usb-skeneru-zebra).

### Funkce GS1 přímo ve skenerech Zebra

Nezávisle na čtení čárových kódů umějí některé 2D imagery Zebra zpracovávat data GS1 přímo ve
skeneru, nastavené přes 123Scan:

| Funkce | Co dělá | Pro GAIA Desktop |
|---|---|---|
| **Label Parse+** | Analyzuje AI etikety GS1 ve skeneru a posílá přeformátovaná pole | **Nechat vypnuté** — GAIA Desktop by už neviděl AI, oddělovače FNC1 ani AIM Code ID |
| **GS1 Digital Link** | U maloobchodních výrobků označených 1D kódem EAN/UPC i 2D kódem Digital Link vybírá, který poslat | **Nechat vypnuté** (výchozí) |
| **GS1 Retail POS parsing** | Čte maloobchodní 2D kódy v syntaxi URI Digital Link nebo řetězce prvků | **Nechat vypnuté** |

Nastavení **GS1 Digital Link** má tři režimy. **Mode 1 — GTIN only**, režim, ve kterém po zapnutí
začíná, posílá jen GTIN, ať pochází z kteréhokoli čárového kódu; šarže, datum použitelnosti nebo
sériové číslo z Digital Link by se tak do GAIA Desktop nikdy nedostaly. **Mode 2 — Prioritize 2D**
pošle 2D kód, pokud ho uvidí v rámci prioritního času (výchozí 200 ms), jinak 1D kód. **Mode 3 —
Corroborate 1D and 2D GTINs Match** pošle 1D kód a navíc 2D kód, pokud se jeho GTIN shoduje. Musí-li
nastavení zůstat zapnuté kvůli jinému softwaru, použijte Mode 2, ne Mode 1. U DS8108 jde o parametry
2373 (zap./vyp.), 2374 (režim) a 2491 (čas).

Tyto funkce vyžadují **123Scan verze 6 nebo novější** a firmware, který je obsahuje. Zebra přidává
podporu GS1 Digital Link aktualizacemi firmwaru od dubna 2025; následující data pocházejí od
prodejce Zebra, proto si ověřte poznámky k vydání firmwaru jednotlivých modelů:

| Řada modelů | Vydání firmwaru s GS1 Digital Link |
|---|---|
| DS81x8 | 16. dubna 2025 |
| MP7000 | 21. dubna 2025 |
| DS9908 | 6. června 2025 |
| SP72 / SM72 | 11. července 2025 |
| DS46x8 | 28. července 2025 |
| MP7200 (Rev-C), DS5502 | 30. července 2025 |
| DS9308 | 29. srpna 2025 |
| DS22x8, DS1000 | 5. září 2025 (jen Asie a Tichomoří) |
| DS36x8 (včetně KD a XR), MS47 | 30. září 2025 |
| PL5000, SE4107 | 15. října 2025 |
| CS6080 | 31. prosince 2025 |

Žádná z těchto funkcí není potřeba ke čtení kódů GS1 Digital Link do GAIA Desktop: když jsou
vypnuté, 2D skener Zebra s aktuálním firmwarem přečte QR kód Digital Link jako kterýkoli jiný a GAIA
Desktop převede URI na jeho AI.

Zdroje: Zebra, *DS8108 Product Reference Guide*, MN-002926-11EN Rev A (srpen 2025), kapitola 12
„GS1 Digital Link Parameters“ a kapitola Data Parsing; Zebra, *123Scan release notes*;
Barcode Factory, *Upgrade Zebra Scanners for GS1 Digital Link* (data firmwaru).
