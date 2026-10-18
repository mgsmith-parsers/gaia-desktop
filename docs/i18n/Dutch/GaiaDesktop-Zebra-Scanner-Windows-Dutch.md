# Een Zebra-scanner instellen — GAIA Desktop voor Windows

Hoe u een Zebra-barcodescanner installeert en instelt zodat GAIA Desktop GS1-barcodes er
rechtstreeks mee leest: elke scan wordt geparseerd en aan de geschiedenis toegevoegd, met het
AIM Code ID-voorvoegsel en elk FNC1-scheidingsteken intact. Oorspronkelijke versie:
[GaiaDesktop-Zebra-Scanner-Windows.md](../../GaiaDesktop-Zebra-Scanner-Windows.md).

GAIA Desktop is ontwikkeld en getest met een via USB aangesloten handheld imager **Zebra DS8108**.
De stappen gelden voor elke Zebra 2D-scanner die Zebra's Scanner SDK ondersteunt; de
instelbarcodes en menunamen van een bepaald model staan in diens eigen Quick Reference Guide of
Product Reference Guide.

> **Alleen Windows.** De scannerstuurprogramma's van Zebra bestaan alleen voor Windows, dus op
> macOS of Linux maakt GAIA Desktop geen scannerbediening aan. Al het andere in de applicatie werkt
> op elk platform.

---

## Inhoud

1. [Wat u nodig hebt](#1-wat-u-nodig-hebt)
2. [Java installeren](#2-java-installeren)
3. [De Zebra Scanner SDK installeren](#3-de-zebra-scanner-sdk-installeren)
4. [123Scan installeren](#4-123scan-installeren)
5. [De firmware van de Zebra-scanner bijwerken](#5-de-firmware-van-de-zebra-scanner-bijwerken)
6. [De USB-modus van de Zebra-scanner instellen](#6-de-usb-modus-van-de-zebra-scanner-instellen)
7. [De Zebra-scanner instellen voor GS1](#7-de-zebra-scanner-instellen-voor-gs1)
8. [GAIA Desktop installeren en starten](#8-gaia-desktop-installeren-en-starten)
9. [De scanner verbinden](#9-de-scanner-verbinden)
10. [Configuratiereferentie](#10-configuratiereferentie)
11. [Problemen oplossen](#11-problemen-oplossen)
12. [Ondersteunde Zebra 2D-scanners](#12-ondersteunde-zebra-2d-scanners)

---

## 1. Wat u nodig hebt

| | |
|---|---|
| **Scanner** | Een Zebra 2D-scanner met USB-kabel — zie [Ondersteunde Zebra 2D-scanners](#12-ondersteunde-zebra-2d-scanners) |
| **Computer** | Windows 10 of 11, 64-bit |
| **Java** | Java 21 of nieuwer, **64-bit** |
| **Zebra Scanner SDK voor Windows** | **64-bit**, van de supportsite van Zebra — levert het stuurprogramma en de JavaPOS-services die GAIA Desktop gebruikt |
| **Zebra 123Scan** | Het gratis configuratieprogramma van Zebra, van de supportsite — om de firmware van de scanner bij te werken en de instellingen te controleren |
| **GAIA Desktop** | `gaia-desktop-1.0.0-win.zip` |
| **Optioneel** | De **Product Reference Guide** (PRG) van de scanner, met de instelbarcodes |

Hoe de onderdelen samenhangen:

```
Zebra scanner ──USB──► Zebra CoreScanner driver ──► Zebra JavaPOS services ──► GAIA Desktop
                       (installed by the SDK)        (in the SDK's JPOS\bin)    (run.bat puts them on
                                                                                 the class path)
```

GAIA Desktop bevat zelf de JavaPOS-API en de configuratie die de scanner een naam geeft
(`jpos.xml`, in `gaia-desktop.jar`). De Zebra SDK levert de rest: het stuurprogramma dat met de
scanner praat en de Zebra-serviceklassen tussen JavaPOS en dat stuurprogramma.

---

## 2. Java installeren

GAIA Desktop heeft **Java 21 of nieuwer** nodig, en dan de **64-bit**-versie, passend bij de
64-bit stuurprogramma's van Zebra.

1. Installeer een Java 21-JDK of -JRE voor Windows x64 — bijvoorbeeld Eclipse Temurin van
   adoptium.net. Laat het installatieprogramma `JAVA_HOME` instellen als het dat aanbiedt.
2. Controleer het in een opdrachtprompt:

   ```
   java -version
   ```

   De eerste regel moet versie 21 of hoger tonen; een 64-bit Java toont ook `64-Bit Server VM`.

`run.bat` zoekt Java in deze volgorde: een map `jdk*` naast `run.bat`, dan `JAVA_HOME`, dan `java`
in het `PATH`. Alles ouder dan 21 weigert het, met een melding die dat zegt.

---

## 3. De Zebra Scanner SDK installeren

1. Download de **Zebra Scanner SDK voor Windows (64-bit)** van de supportsite van Zebra.
2. Start het installatieprogramma. Biedt het een keuze uit onderdelen, zorg dan dat het onderdeel
   **JPOS** (JavaPOS) erbij zit.
3. Houd de standaardinstallatielocatie aan. GAIA Desktop zoekt het JavaPOS-deel van de SDK in:

   ```
   C:\Program Files\Zebra Technologies\Barcode Scanners\Scanner SDK\JPOS\
   ```

   en verwacht de service-jars van Zebra (`JposServiceScanner.jar`, `JposServiceJniScanner.jar`, …)
   en de native bibliotheken (`CSJPOSScanner64.dll`, …) in de map `bin\` daarvan. Is de SDK ergens
   anders geïnstalleerd, zie dan [ZEBRA_HOME](#zebra_home).
4. **Start Windows opnieuw op** na de installatie. Het CoreScanner-stuurprogramma van de SDK draait
   op de achtergrond en moet actief zijn voordat GAIA Desktop de scanner kan bereiken; opnieuw
   opstarten is de eenvoudigste manier om daar zeker van te zijn.

---

## 4. 123Scan installeren

123Scan is het gratis programma van Zebra om zijn scanners te configureren. Het wordt in de
volgende stap gebruikt om de firmware van de scanner bij te werken, en is daarna de eenvoudigste
manier om de instellingen van de scanner te controleren en te wijzigen.

1. Download **123Scan** van de supportsite van Zebra en start het installatieprogramma.
2. Sluit de Zebra-scanner aan en start 123Scan. Het moet de scanner vinden en het model, het
   serienummer en de huidige firmwareversie tonen. Zo niet, probeer dan een andere USB-poort of
   kabel voordat u verdergaat — een scanner die 123Scan niet ziet, ziet GAIA Desktop ook niet.

---

## 5. De firmware van de Zebra-scanner bijwerken

Werk de firmware van de scanner bij naar de nieuwste versie voordat u hem instelt.
Firmware-updates verhelpen decodeerproblemen en houden de scanner in lijn met het
Zebra-stuurprogramma dat de SDK heeft geïnstalleerd.

1. Sluit **GAIA Desktop** en elk ander programma dat de scanner kan gebruiken: 123Scan heeft de
   scanner tijdens de update voor zich alleen nodig.
2. Kies in 123Scan, met de scanner aangesloten, de optie om de firmware bij te werken. 123Scan
   vergelijkt de firmware van de scanner met de nieuwste die Zebra publiceert en biedt aan die te
   downloaden en te installeren. Heeft de computer geen internettoegang, download de firmware dan
   van de supportpagina van de scanner op de site van Zebra en laad het bestand in 123Scan.
3. Start de update en **laat de scanner aangesloten tot 123Scan meldt dat hij klaar is**. Dat duurt
   enkele minuten; aan het eind start de scanner opnieuw op. Halverwege loskoppelen kan de scanner
   onbruikbaar maken.
4. Controleer dat 123Scan nu de nieuwe firmwareversie toont.

Een update kan sommige instellingen terugzetten naar de standaard; daarom wordt de scanner pas in
de volgende twee stappen ingesteld, na de firmware.

---

## 6. De USB-modus van de Zebra-scanner instellen

Dit is de stap die het vaakst wordt vergeten. **Uit de doos gedraagt een Zebra-scanner zich als
een USB-toetsenbord** (modus USB HID Keyboard): hij "typt" elke barcode in wat de focus heeft. In
die modus kan *Scanner verbinden* van GAIA Desktop hem niet vinden, en alles wat in een tekstveld
wordt getypt verliest zijn FNC1-scheidingstekens — wat de betekenis van een GS1-elementreeks
verandert.

Zet de scanner in een modus waarmee het Zebra-stuurprogramma rechtstreeks praat:

| USB-modus van de scanner | Apparaatnaam in GAIA Desktop | Opmerkingen |
|---|---|---|
| **IBM Hand-held USB** | `ZebraUSBHandHeld`, of `ZebraAllScanners` | Aanbevolen |
| SNAPI (met of zonder imaging) | `ZebraScannerSNAPI`, of `ZebraAllScanners` | Het eigen USB-protocol van Zebra; werkt ook |
| IBM Table-top USB | `ZebraUSBTableTop`, of `ZebraAllScanners` | Bedoeld voor tafelscanners |
| USB OPOS (Hand-held) | `ZebraUSBOPOS`, of `ZebraAllScanners` | |
| USB HID Keyboard (fabrieksinstelling) | — | **Verbindt niet** |

`ZebraAllScanners`, de standaardapparaatnaam in GAIA Desktop, accepteert een scanner in elk van de
ondersteunde modi; is de modus eenmaal ingesteld, dan volstaat de standaard meestal.

De modus wijzigt u op een van twee manieren:

- **scan de instelbarcode** voor de modus — bijvoorbeeld *IBM Hand-held USB* — in de Quick
  Reference Guide of Product Reference Guide van de scanner (sectie USB Host Types / USB Device
  Type); de scanner piept ter bevestiging. Of
- **gebruik 123Scan** ([stap 4](#4-123scan-installeren)): sluit de scanner aan, open de configuratie en
  stel het USB-hosttype in.

Na het wijzigen van de modus herkent Windows de scanner opnieuw als een ander soort apparaat; wacht
een paar seconden voordat u verbindt.

Om de scanner later weer in toetsenbordmodus te zetten — voor andere software — scant u de
instelbarcode *USB Keyboard (HID)*, of herstelt u de fabrieksinstellingen.

---

## 7. De Zebra-scanner instellen voor GS1

GAIA Desktop parseert het best wanneer de scanner doorgeeft welk soort barcode hij heeft gelezen.

### De AIM Code ID meesturen

Stel **Transmit Code ID Character** in op **AIM Code ID Character**, met de instelbarcode uit de
Product Reference Guide (User Preferences) of in 123Scan. Elke scan begint dan met zijn
symbologie-aanduiding — `]C1` voor GS1-128, `]d2` voor GS1 DataMatrix, `]Q3` voor GS1 QR Code,
`]e0` voor GS1 DataBar — waarmee GAIA Desktop de gegevensdrager benoemt en de regels ervoor
toepast.

Zonder deze instelling parseert GAIA Desktop de elementreeks toch en haalt de symbologie uit de
scan zelf, maar het rapport heeft dan geen AIM Code ID om te tonen, en een eenvoudige barcode zoals
een EAN-13 is moeilijker te onderscheiden van een GS1-elementreeks.

### De symbologieën controleren

Zorg dat de barcodetypen waarmee u werkt **ingeschakeld** zijn: meestal GS1-128, GS1 DataMatrix,
GS1 DataBar, GS1 QR Code en EAN/UPC. De meeste staan bij Zebra 2D-scanners standaard aan; 123Scan
toont de huidige instellingen, en de Product Reference Guide heeft voor elk een barcode om het in
te schakelen.

### De GS1-parseerfuncties van Zebra uit laten

Recente firmware voegt GS1-functies toe die in de scanner zelf werken — **Label Parse+**,
**GS1 Digital Link** en **GS1 Retail POS parsing**. Laat ze **uit**, zoals de scanner wordt
geleverd: ze herformatteren of verkorten wat de scanner verstuurt, en GAIA Desktop heeft de ruwe
gegevens nodig om ze te parseren en te valideren. Zie
[Ondersteunde Zebra 2D-scanners](#12-ondersteunde-zebra-2d-scanners) voor wat elk ervan doet.

### Geen voorvoegsels en achtervoegsels

Voeg geen voorvoegsel, achtervoegsel of Enter-toets toe aan de uitvoer van de scanner. GAIA Desktop
ontvangt elke scan via JavaPOS als een volledig bericht en heeft niets daarvan nodig; extra tekens
zouden in de elementreeks terechtkomen.

---

## 8. GAIA Desktop installeren en starten

1. Pak `gaia-desktop-1.0.0-win.zip` uit, bijvoorbeeld naar `C:\Tools\`. Het bevat één map:

   ```
   gaia-desktop-1.0.0-win\
   ├── docs\
   │   └── GaiaDesktop-Zebra-Scanner-Windows.md      this guide
   ├── gaia-desktop.jar
   ├── lib\
   ├── readme.html                                   index of every guide
   └── run.bat
   ```

2. Sluit de scanner aan.
3. Dubbelklik op **`run.bat`**. Naast de applicatie opent een consolevenster dat open blijft:
   scannerstatus en JavaPOS-fouten worden daar gemeld.
4. Lees de eerste regel in de console. Die zegt wat `run.bat` heeft gevonden:

   ```
   run.bat: Java 21.0.8 from JAVA_HOME; JavaFX from lib\; JavaPOS from lib\; Zebra services from C:\Program Files\Zebra Technologies\Barcode Scanners\Scanner SDK\JPOS\
   ```

   Eindigt die op **`Zebra services from none (Zebra Scanner SDK not installed)`**, dan is de SDK
   niet op de standaardlocatie gevonden — installeer hem ([stap 3](#3-de-zebra-scanner-sdk-installeren))
   of stel [ZEBRA_HOME](#zebra_home) in. De applicatie start wel, maar de scanner kan niet
   verbinden.

---

## 9. De scanner verbinden

1. In de werkbalk toont de **apparaatkeuzelijst** de scannernamen die GAIA Desktop kent. Laat hem
   op **`ZebraAllScanners`** staan, of kies de naam voor de USB-modus van de scanner uit de tabel in
   [stap 6](#6-de-usb-modus-van-de-zebra-scanner-instellen).
2. Klik op **Scanner verbinden**, of druk op **Ctrl+K** (ook in het menu **Scanner**).
3. De statusbalk verandert in **`scanner: ZebraAllScanners`** (of de naam die u koos), en
   de statusregel meldt *Scanner verbonden — ZebraAllScanners*. De knop heet nu
   **Scanner loskoppelen**.
4. Scan een GS1-barcode. Die verschijnt bovenaan de geschiedenis van vastleggingen en het rapport
   ervan opent. Met de AIM Code ID aan benoemt het rapport de gegevensdrager — *GS1-128*,
   *GS1 DataMatrix* — en behoudt de elementreeks elk FNC1-scheidingsteken.

De apparaatkeuzelijst onthoudt uw keuze tussen sessies. De scanner blijft verbonden tot u hem
loskoppelt of de applicatie sluit. Scans werken ook in de modi **Samenstellen** en
**Combineren**: in Combineren wordt elke scan een van de barcodes die worden
gecombineerd.

Zolang GAIA Desktop de scanner vasthoudt, kunnen andere programma's hem niet gebruiken, en
omgekeerd: sluit 123Scan en alle andere kassa- of scannersoftware voordat u verbindt.

---

## 10. Configuratiereferentie

### Apparaatnamen

De apparaatkeuzelijst wordt gevuld vanuit de `jpos.xml` in `gaia-desktop.jar`. De scanneritems
daarin:

| Apparaatnaam | Scannertype | Gebruiken met |
|---|---|---|
| `ZebraAllScanners` | alle | **Standaard.** Elke ondersteunde USB-modus |
| `ZebraAllScanners114` | alle | De JavaPOS 1.14-versie van dezelfde service |
| `ZebraUSBHandHeld` | IBM Hand-held USB | Een scanner in de modus IBM Hand-held USB |
| `ZebraUSBTableTop` | IBM Table-top USB | Tafelmodus |
| `ZebraUSBOPOS` | USB OPOS | Modus USB OPOS |
| `ZebraScannerSNAPI` | SNAPI | Modus SNAPI |
| `ZebraScannerUSB` | elke USB | Elke USB-modus |
| `ZebraScannerSerialSSI` | serieel SSI | Scanners met seriële kabel (geen USB) |
| `ZebraScannerSerialNIXMODB` | serieel NIXMODB | Scanners met seriële kabel |
| `ZebraVDI` | VDI | Scanners die naar een virtuele desktop worden doorgestuurd |

### ZEBRA_HOME

Is de Zebra SDK op een andere dan de standaardlocatie geïnstalleerd, stel dan `ZEBRA_HOME` in op
de map `JPOS` daarvan voordat u `run.bat` start — bijvoorbeeld in een klein batchbestand ernaast:

```bat
@echo off
set "ZEBRA_HOME=D:\Zebra\Scanner SDK\JPOS\"
call "%~dp0run.bat" %*
```

`run.bat` neemt de service-jars van Zebra dan uit `%ZEBRA_HOME%bin` en voegt die map toe aan het
`PATH` voor de Zebra-DLL's.

### Een eigen jpos.xml

Om een apparaatnaam toe te voegen of de instellingen van een item te wijzigen, kopieert u
`jpos.xml` uit `gaia-desktop.jar` (het is een zip; elk uitpakprogramma opent het), bewerkt u de
kopie en wijst u GAIA Desktop ernaar:

```bat
set GAIA_DESKTOP_OPTS=-Djpos.config.populatorFileURL=file:///C:/Tools/jpos.xml
run.bat
```

De apparaatkeuzelijst toont dan de scanners uit dat bestand. Met meer dan één Zebra-scanner
aangesloten kan een item via zijn eigenschap `ScannerModel` of `SerialNumber` aan één ervan worden
gekoppeld, zoals in het item `ZebraAllScanners`.

### Overige opties

| Instelling | Hoe |
|---|---|
| Taal van de applicatie | `run.bat --language=FR` (alleen voor deze sessie), of het Optiepaneel |
| Extra Java-opties | `set GAIA_DESKTOP_OPTS=…` vóór `run.bat` |
| Een meegeleverde Java | zet een map `jdk-21…` naast `run.bat`; die wordt vóór `JAVA_HOME` gebruikt |

---

## 11. Problemen oplossen

Fouten verschijnen bij het verbinden in een dialoogvenster, in het consolevenster en in het
logbestand `logs\gaia-desktop.log`, in de map van waaruit `run.bat` is gestart (de map van GAIA
Desktop als op `run.bat` wordt gedubbelklikt). Het getal tussen vierkante haken in het log is de
JavaPOS-foutcode.

| Wat u ziet | Waarschijnlijke oorzaak | Wat te doen |
|---|---|---|
| Er is geen knop *Scanner verbinden* | Draait niet op Windows | Scannen bestaat alleen op Windows |
| 123Scan vindt de scanner niet | Kabel, USB-poort, of een ander programma houdt de scanner vast | Probeer een andere kabel of poort; sluit GAIA Desktop en andere scannersoftware. Een scanner die 123Scan niet ziet, ziet GAIA Desktop ook niet |
| Console: `Zebra services from none (Zebra Scanner SDK not installed)` | De SDK staat niet op de standaardlocatie | Installeer de 64-bit SDK ([stap 3](#3-de-zebra-scanner-sdk-installeren)), of stel [ZEBRA_HOME](#zebra_home) in |
| Fout **104**: *Could not connect to service with logicalName = …: …SymScannerSvc112Factory* | De service-jars van Zebra zijn niet gevonden | Zoals hierboven — controleer de consoleregel `Zebra services from` |
| Fout **109**: *Service does not exist in loaded JCL registry* | De apparaatnaam staat niet in de scannerconfiguratie, of de `javaPOS114.jar` van de SDK is in het class path beland | Kies een naam uit de apparaatkeuzelijst; voeg `javaPOS114.jar` niet toe; gebruikt u een eigen `jpos.xml`, controleer dan dat de naam erin staat |
| Fout **107** (geen hardware), of het verbinden mislukt terwijl de scanner is aangesloten | De scanner staat nog in USB-toetsenbordmodus, of het CoreScanner-stuurprogramma draait niet | Stel de USB-modus in ([stap 6](#6-de-usb-modus-van-de-zebra-scanner-instellen)); start Windows opnieuw op na de SDK-installatie; probeer een andere USB-poort |
| Fout **112** (time-out) of **102** (geclaimd) bij het verbinden | Een ander programma houdt de scanner vast | Sluit 123Scan en alle andere scanner- of kassasoftware en verbind opnieuw |
| `UnsatisfiedLinkError` … `CSJPOSScanner64` in de console | De Zebra-DLL's kunnen niet worden geladen — vaak een 32-bit SDK met een 64-bit Java | Installeer de 64-bit SDK; controleer dat `java -version` een 64-bit VM meldt |
| Scans worden in de invoerbalk getypt in plaats van als vastleggingen binnen te komen | De scanner staat in USB-toetsenbordmodus | Stel de USB-modus in ([stap 6](#6-de-usb-modus-van-de-zebra-scanner-instellen)) |
| Verbonden, maar scans komen niet binnen | Verkeerde apparaatnaam voor de modus van de scanner, of de scanner is opnieuw ingeplugd terwijl hij verbonden was | Koppel los, kies `ZebraAllScanners` en verbind opnieuw |
| Het rapport toont geen AIM Code ID of gegevensdrager | Het meesturen van de AIM Code ID staat uit | Zet *Transmit Code ID Character → AIM Code ID* aan ([stap 7](#7-de-zebra-scanner-instellen-voor-gs1)) |
| Een elementreeks mist zijn scheidingstekens, of AI's lopen in elkaar over | Gescand in toetsenbordmodus, of met de hand getypt zonder `{GS}` | Gebruik een JavaPOS-USB-modus; voeg bij het typen scheidingstekens in met de knop `{GS}` |
| Een barcodetype wordt nooit gelezen | Die symbologie is in de scanner uitgeschakeld | Schakel hem in ([stap 7](#7-de-zebra-scanner-instellen-voor-gs1)) |

Blijft een probleem bestaan, bewaar dan de consoleregel `run.bat:`, de tekst van het
foutdialoogvenster en `logs\gaia-desktop.log` samen: samen tonen ze welke Java, welke Zebra-services
en welke apparaatnaam zijn gebruikt, en waar het verbinden precies mislukte.

---

## 12. Ondersteunde Zebra 2D-scanners

GAIA Desktop heeft een scanner nodig die de barcodes kan lezen waarin GS1-gegevens worden
afgedrukt — GS1-128, GS1 DataBar, EAN/UPC en, voor de meeste toepassingen, de 2D-codes
**GS1 DataMatrix** en **GS1 QR Code**, waarin ook een **GS1 Digital Link**-URI meestal wordt
afgedrukt. Dat betekent een Zebra **2D-imager**: een lineaire (1D-)scanner kan DataMatrix- of
QR-codes helemaal niet lezen.

De scanner hoeft GS1 Application Identifiers of Digital Link niet zelf te begrijpen.
**GAIA Desktop parseert beide**: het neemt de scan zoals de scanner hem las — met AIM Code ID en
FNC1-scheidingstekens — en splitst hem in AI's, of zet een Digital Link-URI zelf om in zijn AI's.

### Getest

| Scanner | Type | Status |
|---|---|---|
| **DS8108** | Bedrade handheld 2D-imager | **Ontwikkeld en getest** met GAIA Desktop |

### Zouden moeten werken

Dit zijn Zebra 2D-imagers die, net als de DS8108, worden aangestuurd via de Scanner SDK en
JavaPOS-services van Zebra. Ze zijn **niet getest** met GAIA Desktop; stel elk ervan in zoals in
deze handleiding, met de eigen Quick Reference Guide of Product Reference Guide voor de
instelbarcodes.

| Familie | Modellen (voorbeelden) |
|---|---|
| DS81x8 | DS8108 (bedraad), DS8178 (draadloos) |
| DS22x8 | DS2208 (bedraad), DS2278 (draadloos) — algemeen gebruik |
| DS36x8 | DS3608 (bedraad), DS3678 (draadloos) — robuust, inclusief de varianten KD en XR |
| DS46x8 | DS4608 (bedraad), DS4678 (draadloos) |
| DS9308 / DS9908 | Presentatie- en handsfree-scanners |
| MP7000 / MP7200 | Bioptische scanners met meerdere vlakken en scanner-weegschalen |
| CS6080 | Draadloze companion-scanner |

Een draadloze scanner praat met zijn basisstation, en het basisstation wordt op de computer
aangesloten: sluit het station via USB aan en behandel het paar als één scanner. Scanners uit andere
Zebra-families kunnen ook werken, als Zebra's Scanner SDK ze ondersteunt in een van de USB-modi uit
[stap 6](#6-de-usb-modus-van-de-zebra-scanner-instellen).

### GS1-functies in Zebra-scanners

Los van het lezen van de barcodes kunnen sommige Zebra 2D-imagers GS1-gegevens in de scanner zelf
verwerken, ingesteld via 123Scan:

| Functie | Wat het doet | Voor GAIA Desktop |
|---|---|---|
| **Label Parse+** | Parseert de AI's van een GS1-label in de scanner en stuurt geherformatteerde velden | **Uit laten** — GAIA Desktop zou de AI's, FNC1-scheidingstekens en AIM Code ID niet meer zien |
| **GS1 Digital Link** | Voor retailproducten met zowel een 1D-EAN/UPC als een 2D-Digital Link-code: kiest welke wordt verstuurd | **Uit laten** (standaard) |
| **GS1 Retail POS parsing** | Leest 2D-retailbarcodes in Digital Link-URI- of elementreekssyntaxis | **Uit laten** |

De instelling **GS1 Digital Link** heeft drie modi. **Mode 1 — GTIN only**, de modus waarmee hij
start zodra hij aan staat, stuurt alleen de GTIN, uit welke barcode die ook komt, zodat een partij,
houdbaarheidsdatum of serienummer in de Digital Link GAIA Desktop nooit zou bereiken.
**Mode 2 — Prioritize 2D** stuurt de 2D-code als die binnen de prioriteringstijd (standaard 200 ms)
wordt gezien, anders de 1D. **Mode 3 — Corroborate 1D and 2D GTINs Match** stuurt de 1D, en ook de
2D wanneer de GTIN overeenkomt. Moet de instelling voor andere software aan staan, gebruik dan
Mode 2, niet Mode 1. Op de DS8108 zijn dit de parameters 2373 (aan/uit), 2374 (modus) en 2491
(time-out).

Deze functies vereisen **123Scan versie 6 of nieuwer** en firmware waarin ze zitten. Zebra voegt
sinds april 2025 ondersteuning voor GS1 Digital Link toe via firmware-updates; de datums hieronder
komen van een Zebra-wederverkoper, dus controleer de release notes van de firmware van elk model:

| Modelfamilie | Firmware met GS1 Digital Link uitgebracht |
|---|---|
| DS81x8 | 16 april 2025 |
| MP7000 | 21 april 2025 |
| DS9908 | 6 juni 2025 |
| SP72 / SM72 | 11 juli 2025 |
| DS46x8 | 28 juli 2025 |
| MP7200 (Rev-C), DS5502 | 30 juli 2025 |
| DS9308 | 29 augustus 2025 |
| DS22x8, DS1000 | 5 september 2025 (alleen Azië-Pacific) |
| DS36x8 (inclusief KD en XR), MS47 | 30 september 2025 |
| PL5000, SE4107 | 15 oktober 2025 |
| CS6080 | 31 december 2025 |

Geen van deze functies is nodig om GS1 Digital Link-codes in GAIA Desktop in te lezen: met de
functies uit leest een Zebra 2D-scanner met actuele firmware een Digital Link-QR-code net als elke
andere, en zet GAIA Desktop de URI om in zijn AI's.

Bronnen: Zebra, *DS8108 Product Reference Guide*, MN-002926-11EN Rev A (augustus 2025), hoofdstuk 12
"GS1 Digital Link Parameters" en het hoofdstuk Data Parsing; Zebra, *123Scan release notes*;
Barcode Factory, *Upgrade Zebra Scanners for GS1 Digital Link* (firmwaredatums).
