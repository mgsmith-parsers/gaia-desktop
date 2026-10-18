# Ställa in en Zebra-skanner — GAIA Desktop för Windows

Så installerar och ställer du in en Zebra-streckkodsskanner så att GAIA Desktop kan läsa
GS1-streckkoder direkt från den: varje skanning analyseras och läggs till i historiken, med
AIM Code ID-prefixet och varje FNC1-avgränsare intakta. Original:
[GaiaDesktop-Zebra-Scanner-Windows.md](../../GaiaDesktop-Zebra-Scanner-Windows.md).

GAIA Desktop har utvecklats och testats med en handhållen imager av typen **Zebra DS8108**
ansluten via USB. Stegen gäller för alla Zebra 2D-skannrar som Zebras Scanner SDK stöder;
inställningsstreckkoder och menynamn för en viss modell finns i modellens egen Quick Reference Guide
eller Product Reference Guide.

> **Endast Windows.** Drivrutinerna för Zebras skannrar finns bara för Windows, så på macOS och
> Linux bygger GAIA Desktop inga skannerkontroller. Allt annat i appen fungerar på alla plattformar.

---

## Innehåll

1. [Det här behöver du](#1-det-här-behöver-du)
2. [Installera Java](#2-installera-java)
3. [Installera Zebra Scanner SDK](#3-installera-zebra-scanner-sdk)
4. [Installera 123Scan](#4-installera-123scan)
5. [Uppdatera Zebra-skannerns firmware](#5-uppdatera-zebra-skannerns-firmware)
6. [Ställa in Zebra-skannerns USB-läge](#6-ställa-in-zebra-skannerns-usb-läge)
7. [Konfigurera Zebra-skannern för GS1](#7-konfigurera-zebra-skannern-för-gs1)
8. [Installera och starta GAIA Desktop](#8-installera-och-starta-gaia-desktop)
9. [Ansluta skannern](#9-ansluta-skannern)
10. [Konfigurationsreferens](#10-konfigurationsreferens)
11. [Felsökning](#11-felsökning)
12. [Zebra 2D-skannrar som stöds](#12-zebra-2d-skannrar-som-stöds)

---

## 1. Det här behöver du

| | |
|---|---|
| **Skanner** | En Zebra 2D-skanner med USB-kabel — se [Zebra 2D-skannrar som stöds](#12-zebra-2d-skannrar-som-stöds) |
| **Dator** | Windows 10 eller 11, 64-bitars |
| **Java** | Java 21 eller senare, **64-bitars** |
| **Zebra Scanner SDK för Windows** | **64-bitars**, från Zebras supportwebbplats — tillhandahåller drivrutinen och de JavaPOS-tjänster som GAIA Desktop använder |
| **Zebra 123Scan** | Zebras kostnadsfria konfigurationsverktyg, från supportwebbplatsen — används för att uppdatera skannerns firmware och kontrollera dess inställningar |
| **GAIA Desktop** | `gaia-desktop-1.0.0-win.zip` |
| **Valfritt** | Skannerns **Product Reference Guide** (PRG), med inställningsstreckkoderna |

Så hänger delarna ihop:

```
Zebra scanner ──USB──► Zebra CoreScanner driver ──► Zebra JavaPOS services ──► GAIA Desktop
                       (installed by the SDK)        (in the SDK's JPOS\bin)    (run.bat puts them on
                                                                                 the class path)
```

GAIA Desktop har med sig själva JavaPOS-API:et och den konfiguration som namnger skannern
(`jpos.xml` i `gaia-desktop.jar`). Resten kommer från Zebras SDK: drivrutinen som pratar med
skannern och Zebras tjänsteklasser som sitter mellan JavaPOS och den drivrutinen.

---

## 2. Installera Java

GAIA Desktop kräver **Java 21 eller senare**, och det måste vara **64-bitarsversionen** för att
passa Zebras 64-bitars drivrutiner.

1. Installera en Java 21-JDK eller -JRE för Windows x64 — till exempel Eclipse Temurin från
   adoptium.net. Låt installationsprogrammet ställa in `JAVA_HOME` om det erbjuder det.
2. Kontrollera i en kommandotolk:

   ```
   java -version
   ```

   Första raden ska visa version 21 eller senare; en 64-bitars Java skriver också
   `64-Bit Server VM`.

`run.bat` letar efter Java i den här ordningen: en mapp `jdk*` bredvid `run.bat`, sedan
`JAVA_HOME`, sedan `java` i `PATH`. Allt äldre än 21 avvisas med ett meddelande som säger det.

---

## 3. Installera Zebra Scanner SDK

1. Ladda ned **Zebra Scanner SDK för Windows (64-bitars)** från Zebras supportwebbplats.
2. Kör installationsprogrammet. Om det går att välja komponenter, se till att komponenten **JPOS**
   (JavaPOS) är med.
3. Behåll standardplatsen för installationen. GAIA Desktop letar efter SDK:ns JavaPOS-del i:

   ```
   C:\Program Files\Zebra Technologies\Barcode Scanners\Scanner SDK\JPOS\
   ```

   och förväntar sig Zebras tjänste-JAR-filer (`JposServiceScanner.jar`, `JposServiceJniScanner.jar`, …)
   och de inbyggda biblioteken (`CSJPOSScanner64.dll`, …) i dess mapp `bin\`. Om SDK:n är
   installerad någon annanstans, se [ZEBRA_HOME](#zebra_home).
4. **Starta om Windows** efter installationen. SDK:ns CoreScanner-drivrutin körs i bakgrunden och
   måste vara igång innan GAIA Desktop kan nå skannern; en omstart är det enklaste sättet att vara
   säker.

---

## 4. Installera 123Scan

123Scan är Zebras kostnadsfria verktyg för att ställa in företagets skannrar. I nästa steg används
det för att uppdatera skannerns firmware, och sedan är det det enklaste sättet att kontrollera och
ändra skannerns inställningar.

1. Ladda ned **123Scan** från Zebras supportwebbplats och kör installationsprogrammet.
2. Anslut Zebra-skannern och starta 123Scan. Det ska hitta skannern och visa modell, serienummer och
   nuvarande firmwareversion. Om det inte gör det, prova en annan USB-port eller kabel innan du går
   vidare — GAIA Desktop ser ingen skanner som 123Scan inte ser.

---

## 5. Uppdatera Zebra-skannerns firmware

Uppdatera skannerns firmware till den senaste versionen innan du konfigurerar den.
Firmwareuppdateringar åtgärdar avkodningsproblem och håller skannern i takt med den
Zebra-drivrutin som SDK:n installerade.

1. Stäng **GAIA Desktop** och alla andra program som kan använda skannern: 123Scan behöver skannern
   för sig själv under uppdateringen.
2. Välj alternativet för firmwareuppdatering i 123Scan medan skannern är ansluten. 123Scan jämför
   skannerns firmware med den senaste som Zebra publicerat och erbjuder sig att ladda ned och
   installera den. Om datorn saknar internetåtkomst, ladda ned firmware från skannerns supportsida
   på Zebras webbplats och läs in filen i 123Scan.
3. Starta uppdateringen och **låt skannern vara ansluten tills 123Scan meddelar att den är klar**.
   Det tar flera minuter; i slutet startar skannern om. Att koppla ur den halvvägs kan göra den
   oanvändbar.
4. Kontrollera att 123Scan nu visar den nya firmwareversionen.

En uppdatering kan återställa vissa inställningar till standardvärdena; därför konfigureras skannern
i de två följande stegen, efter firmware.

---

## 6. Ställa in Zebra-skannerns USB-läge

Det här är steget som oftast missas. **Från fabrik är en Zebra-skanner ett USB-tangentbord**
(läget USB HID Keyboard): den ”skriver” varje streckkod där fokus finns. I det läget kan
*Anslut skanner* i GAIA Desktop inte hitta den, och allt som skrivs i ett textfält förlorar sina
FNC1-avgränsare, vilket ändrar betydelsen av en GS1-elementsträng.

Växla skannern till ett läge som Zebra-drivrutinen pratar direkt med:

| Skannerns USB-läge | Enhetsnamn i GAIA Desktop | Anmärkningar |
|---|---|---|
| **IBM Hand-held USB** | `ZebraUSBHandHeld` eller `ZebraAllScanners` | Rekommenderas |
| SNAPI (med eller utan bildfunktion) | `ZebraScannerSNAPI` eller `ZebraAllScanners` | Zebras eget USB-protokoll; fungerar också |
| IBM Table-top USB | `ZebraUSBTableTop` eller `ZebraAllScanners` | Avsett för bordsskannrar |
| USB OPOS (Hand-held) | `ZebraUSBOPOS` eller `ZebraAllScanners` | |
| USB HID Keyboard (fabriksinställning) | — | **Ansluter inte** |

`ZebraAllScanners`, standardenhetsnamnet i GAIA Desktop, accepterar en skanner i vilket som helst av
de lägen som stöds, så när läget är inställt fungerar standardvärdet oftast direkt.

Byt läge på något av dessa sätt:

- **skanna inställningsstreckkoden** för läget — till exempel *IBM Hand-held USB* — i skannerns
  Quick Reference Guide eller Product Reference Guide (avsnittet USB Host Types / USB Device Type);
  skannern piper som bekräftelse; eller
- **använd 123Scan** ([steg 4](#4-installera-123scan)): anslut skannern, öppna dess konfiguration och
  ställ in USB-värdtypen.

Efter lägesbytet identifierar Windows skannern på nytt som en annan sorts enhet; vänta några sekunder
innan du ansluter.

Om du senare vill återställa skannern till tangentbordsläge — för att använda den med annan
programvara — skannar du inställningsstreckkoden *USB Keyboard (HID)* eller återställer
fabriksinställningarna.

---

## 7. Konfigurera Zebra-skannern för GS1

GAIA Desktop analyserar bäst när skannern talar om vilken sorts streckkod den läste.

### Skicka AIM Code ID

Ställ in **Transmit Code ID Character** på **AIM Code ID Character**, med inställningsstreckkoden i
Product Reference Guide (User Preferences) eller i 123Scan. Varje skanning börjar då med sin
symbologiidentifierare — `]C1` för GS1-128, `]d2` för GS1 DataMatrix, `]Q3` för GS1 QR Code,
`]e0` för GS1 DataBar — som GAIA Desktop använder för att namnge databäraren och tillämpa dess regler.

Utan den analyserar GAIA Desktop ändå elementsträngen och tar symbologin från själva skanningen,
men rapporten har ingen AIM Code ID att visa, och en vanlig streckkod som en EAN-13 blir svårare att
skilja från en GS1-elementsträng.

### Kontrollera symbologierna

Se till att de streckkodstyper du arbetar med är **aktiverade**: vanligen GS1-128, GS1 DataMatrix,
GS1 DataBar, GS1 QR Code och EAN/UPC. De flesta är på som standard i Zebras 2D-skannrar; 123Scan
visar de aktuella inställningarna, och Product Reference Guide har en streckkod som aktiverar var
och en.

### Låt Zebras GS1-analysfunktioner vara avstängda

Nyare firmware lägger till GS1-funktioner som arbetar i själva skannern — **Label Parse+**,
**GS1 Digital Link** och **GS1 Retail POS parsing**. Låt dem vara **avstängda**, som skannern
levereras: de formaterar om eller kortar det skannern skickar, medan GAIA Desktop behöver rådata för
att kunna analysera och validera dem. Vad var och en gör finns i
[Zebra 2D-skannrar som stöds](#12-zebra-2d-skannrar-som-stöds).

### Inga prefix eller suffix

Lägg inte till prefix, suffix eller Enter-tangent till skannerns utdata. GAIA Desktop tar emot varje
skanning som ett komplett meddelande via JavaPOS och behöver inget av dem; extra tecken skulle hamna
i elementsträngen.

---

## 8. Installera och starta GAIA Desktop

1. Packa upp `gaia-desktop-1.0.0-win.zip`, till exempel till `C:\Tools\`. Den innehåller en enda
   mapp:

   ```
   gaia-desktop-1.0.0-win\
   ├── docs\
   │   └── GaiaDesktop-Zebra-Scanner-Windows.md      this guide
   ├── gaia-desktop.jar
   ├── lib\
   ├── readme.html                                   index of every guide
   └── run.bat
   ```

2. Anslut skannern.
3. Dubbelklicka på **`run.bat`**. Ett konsolfönster öppnas bredvid appen och förblir öppet: där
   rapporteras skannerns status och JavaPOS-fel.
4. Läs första raden i konsolen. Den talar om vad `run.bat` hittade:

   ```
   run.bat: Java 21.0.8 from JAVA_HOME; JavaFX from lib\; JavaPOS from lib\; Zebra services from C:\Program Files\Zebra Technologies\Barcode Scanners\Scanner SDK\JPOS\
   ```

   Om den slutar med **`Zebra services from none (Zebra Scanner SDK not installed)`** hittades
   SDK:n inte på standardplatsen — installera den ([steg 3](#3-installera-zebra-scanner-sdk)) eller
   ställ in [ZEBRA_HOME](#zebra_home). Appen startar ändå, men skannern kan inte ansluta.

---

## 9. Ansluta skannern

1. I verktygsfältet listar **enhetsväljaren** de skannernamn som GAIA Desktop känner till. Låt den
   stå på **`ZebraAllScanners`**, eller välj namnet för skannerns USB-läge i tabellen i
   [steg 6](#6-ställa-in-zebra-skannerns-usb-läge).
2. Klicka på **Anslut skanner** eller tryck **Ctrl+K** (finns även i menyn **Skanner**).
3. Statusfältet ändras till **`skanner: ZebraAllScanners`** (eller namnet du valde), och
   statusraden meddelar *Skanner ansluten*. Knappen heter nu **Koppla från skanner**.
4. Skanna en GS1-streckkod. Den visas överst i registreringshistoriken och dess rapport öppnas. Med
   AIM Code ID på namnger rapporten databäraren — *GS1-128*, *GS1 DataMatrix* — och elementsträngen
   behåller varje FNC1-avgränsare.

Enhetsväljaren kommer ihåg ditt val mellan körningar. Skannern förblir ansluten tills du kopplar från
den eller stänger appen. Skanningar fungerar också i lägena **Bygg** och
**Kombinera**: i Kombinera blir varje skanning en av streckkoderna som kombineras.

Så länge GAIA Desktop håller skannern kan andra program inte använda den, och tvärtom: stäng 123Scan
och all annan kassa- eller skannerprogramvara innan du ansluter.

---

## 10. Konfigurationsreferens

### Enhetsnamn

Enhetsväljaren fylls från den `jpos.xml` som följer med i `gaia-desktop.jar`. Dess skannerposter:

| Enhetsnamn | Skannertyp | Används med |
|---|---|---|
| `ZebraAllScanners` | alla | **Standard.** Alla USB-lägen som stöds |
| `ZebraAllScanners114` | alla | JavaPOS 1.14-versionen av samma tjänst |
| `ZebraUSBHandHeld` | IBM Hand-held USB | En skanner i läget IBM Hand-held USB |
| `ZebraUSBTableTop` | IBM Table-top USB | Table-top-läge |
| `ZebraUSBOPOS` | USB OPOS | USB OPOS-läge |
| `ZebraScannerSNAPI` | SNAPI | SNAPI-läge |
| `ZebraScannerUSB` | valfri USB | Alla USB-lägen |
| `ZebraScannerSerialSSI` | seriell SSI | Skannrar med seriell kabel (inte USB) |
| `ZebraScannerSerialNIXMODB` | seriell NIXMODB | Skannrar med seriell kabel |
| `ZebraVDI` | VDI | Skannrar som vidarebefordras till ett virtuellt skrivbord |

### ZEBRA_HOME

Om Zebra SDK är installerad någon annanstans än på standardplatsen ställer du in `ZEBRA_HOME` på dess
mapp `JPOS` innan du startar `run.bat` — till exempel i en liten kommandofil bredvid:

```bat
@echo off
set "ZEBRA_HOME=D:\Zebra\Scanner SDK\JPOS\"
call "%~dp0run.bat" %*
```

`run.bat` hämtar då Zebras tjänste-JAR-filer från `%ZEBRA_HOME%bin` och lägger till den mappen i
`PATH` för Zebras DLL-filer.

### Egen jpos.xml

För att lägga till ett enhetsnamn eller ändra inställningarna för en post kopierar du ut `jpos.xml`
ur `gaia-desktop.jar` (det är en zip; alla uppackningsverktyg öppnar den), redigerar kopian och pekar
GAIA Desktop på den:

```bat
set GAIA_DESKTOP_OPTS=-Djpos.config.populatorFileURL=file:///C:/Tools/jpos.xml
run.bat
```

Enhetsväljaren listar då skannrarna i den filen. Med fler än en Zebra-skanner ansluten kan en post
knytas till en av dem via egenskapen `ScannerModel` eller `SerialNumber`, som i posten
`ZebraAllScanners`.

### Övriga alternativ

| Inställning | Så här |
|---|---|
| Appens språk | `run.bat --language=FR` (bara den här körningen), eller Alternativpanel |
| Extra Java-alternativ | `set GAIA_DESKTOP_OPTS=…` före `run.bat` |
| En medföljande Java | lägg en mapp `jdk-21…` bredvid `run.bat`; den används före `JAVA_HOME` |

---

## 11. Felsökning

Fel visas i en dialogruta vid anslutning, i konsolfönstret och i loggfilen `logs\gaia-desktop.log`
i den mapp som `run.bat` startades från (GAIA Desktop-mappen när man dubbelklickar på `run.bat`).
Talet inom hakparentes i loggen är JavaPOS-felkoden.

| Det du ser | Trolig orsak | Åtgärd |
|---|---|---|
| Det finns ingen knapp *Anslut skanner* | Körs inte på Windows | Skanning finns bara på Windows |
| 123Scan hittar inte skannern | Kabel, USB-port, eller ett annat program håller skannern | Prova en annan kabel eller port; stäng GAIA Desktop och annan skannerprogramvara. GAIA Desktop ser ingen skanner som 123Scan inte ser |
| Konsolen: `Zebra services from none (Zebra Scanner SDK not installed)` | SDK:n finns inte på standardplatsen | Installera 64-bitars SDK:n ([steg 3](#3-installera-zebra-scanner-sdk)), eller ställ in [ZEBRA_HOME](#zebra_home) |
| Fel **104**: *Could not connect to service with logicalName = …: …SymScannerSvc112Factory* | Zebras tjänste-JAR-filer hittades inte | Som ovan — kontrollera raden `Zebra services from` i konsolen |
| Fel **109**: *Service does not exist in loaded JCL registry* | Enhetsnamnet finns inte i skannerkonfigurationen, eller SDK:ns `javaPOS114.jar` har hamnat i klassökvägen | Välj ett namn i enhetsväljaren; lägg inte till `javaPOS114.jar`; om du använder en egen `jpos.xml`, kontrollera att namnet finns i den |
| Fel **107** (ingen maskinvara), eller anslutningen misslyckas fast skannern är ansluten | Skannern är fortfarande i USB-tangentbordsläge, eller CoreScanner-drivrutinen körs inte | Ställ in USB-läget ([steg 6](#6-ställa-in-zebra-skannerns-usb-läge)); starta om Windows efter SDK-installationen; prova en annan USB-port |
| Fel **112** (tidsgräns) eller **102** (upptagen) vid anslutning | Ett annat program håller skannern | Stäng 123Scan och all annan skanner- eller kassaprogramvara och anslut igen |
| `UnsatisfiedLinkError` … `CSJPOSScanner64` i konsolen | Zebras DLL-filer kan inte läsas in — ofta en 32-bitars SDK med en 64-bitars Java | Installera 64-bitars SDK:n; kontrollera att `java -version` rapporterar en 64-bitars VM |
| Skanningar skrivs in i inmatningsfältet i stället för att komma som registreringar | Skannern är i USB-tangentbordsläge | Ställ in USB-läget ([steg 6](#6-ställa-in-zebra-skannerns-usb-läge)) |
| Ansluten, men skanningarna kommer inte fram | Fel enhetsnamn för skannerns läge, eller skannern kopplades in på nytt under anslutningen | Koppla från, välj `ZebraAllScanners` och anslut igen |
| Rapporten visar varken AIM Code ID eller databärare | Överföringen av AIM Code ID är avstängd | Slå på *Transmit Code ID Character → AIM Code ID* ([steg 7](#7-konfigurera-zebra-skannern-för-gs1)) |
| En elementsträng saknar sina avgränsare, eller AI:er flyter ihop | Skannad i tangentbordsläge, eller inskriven för hand utan `{GS}` | Använd ett JavaPOS-USB-läge; för in avgränsare med knappen `{GS}` när du skriver |
| En streckkodstyp läses aldrig | Den symbologin är avstängd i skannern | Aktivera den ([steg 7](#7-konfigurera-zebra-skannern-för-gs1)) |

Om problemet kvarstår, spara raden `run.bat:` från konsolen, texten i feldialogrutan och
`logs\gaia-desktop.log` tillsammans: tillsammans visar de vilken Java, vilka Zebra-tjänster och vilket
enhetsnamn som användes, och exakt var anslutningen misslyckades.

---

## 12. Zebra 2D-skannrar som stöds

GAIA Desktop behöver en skanner som kan läsa de streckkoder som GS1-data trycks i — GS1-128,
GS1 DataBar, EAN/UPC och, för de flesta användningar, 2D-koderna **GS1 DataMatrix** och
**GS1 QR Code**, som också är hur en **GS1 Digital Link**-URI oftast trycks. Det innebär en Zebra
**2D-imager**: en linjär (1D) skanner kan inte läsa DataMatrix- eller QR-koder alls.

Skannern behöver inte själv förstå GS1-applikationsidentifierare eller Digital Link. **GAIA Desktop
analyserar båda**: den tar skanningen som skannern läste den — AIM Code ID, FNC1-avgränsare och allt
— och delar själv upp den i AI:er, eller omvandlar en Digital Link-URI till dess AI:er.

### Testad

| Skanner | Typ | Status |
|---|---|---|
| **DS8108** | Trådbunden handhållen 2D-imager | **Utvecklad och testad** med GAIA Desktop |

### Bör fungera

Det här är Zebra 2D-imagers som, liksom DS8108, styrs via Zebras Scanner SDK och JavaPOS-tjänster.
De har **inte testats** med GAIA Desktop; ställ in var och en enligt den här guiden och använd
modellens egen Quick Reference Guide eller Product Reference Guide för inställningsstreckkoderna.

| Familj | Modeller (exempel) |
|---|---|
| DS81x8 | DS8108 (trådbunden), DS8178 (trådlös) |
| DS22x8 | DS2208 (trådbunden), DS2278 (trådlös) — allmänt bruk |
| DS36x8 | DS3608 (trådbunden), DS3678 (trådlös) — tåliga, inklusive varianterna KD och XR |
| DS46x8 | DS4608 (trådbunden), DS4678 (trådlös) |
| DS9308 / DS9908 | Presentationsskannrar och handsfree-skannrar |
| MP7000 / MP7200 | Bioptiska flerplansskannrar och skannervågar |
| CS6080 | Trådlös companion-skanner |

En trådlös skanner pratar med sin basstation, och det är basstationen som ansluts till datorn: anslut
basstationen via USB och behandla paret som en skanner. Skannrar i andra Zebra-familjer kan också
fungera, om Zebras Scanner SDK stöder dem i något av USB-lägena i
[steg 6](#6-ställa-in-zebra-skannerns-usb-läge).

### Zebra-skannrarnas inbyggda GS1-funktioner

Utöver att läsa streckkoderna kan vissa Zebra 2D-imagers bearbeta GS1-data i själva skannern,
inställt via 123Scan:

| Funktion | Vad den gör | För GAIA Desktop |
|---|---|---|
| **Label Parse+** | Analyserar AI:erna i en GS1-etikett i skannern och skickar omformaterade fält | **Låt vara avstängd** — GAIA Desktop skulle inte längre se AI:erna, FNC1-avgränsarna och AIM Code ID |
| **GS1 Digital Link** | För detaljhandelsprodukter märkta med både en 1D-EAN/UPC och en 2D-Digital Link-kod: väljer vilken som skickas | **Låt vara avstängd** (standard) |
| **GS1 Retail POS parsing** | Läser 2D-streckkoder för detaljhandeln i syntax för Digital Link-URI eller elementsträng | **Låt vara avstängd** |

Inställningen **GS1 Digital Link** har tre lägen. **Mode 1 — GTIN only**, läget den startar i när
den slås på, skickar bara GTIN, oavsett vilken streckkod den kommer från; ett parti, ett bäst
före-datum eller ett serienummer i Digital Link når alltså aldrig GAIA Desktop. **Mode 2 —
Prioritize 2D** skickar 2D-koden om den ses inom prioritetstiden (200 ms som standard), annars
1D-koden. **Mode 3 — Corroborate 1D and 2D GTINs Match** skickar 1D-koden, och även 2D-koden när dess
GTIN stämmer. Om inställningen måste vara på för annan programvara, använd Mode 2, inte Mode 1. På
DS8108 är det parametrarna 2373 (på/av), 2374 (läge) och 2491 (tid).

De här funktionerna kräver **123Scan version 6 eller senare** och firmware som innehåller dem. Zebra
har lagt till stöd för GS1 Digital Link via firmwareuppdateringar sedan april 2025; datumen nedan
kommer från en Zebra-återförsäljare, så kontrollera firmware-versionsinformationen för varje modell:

| Modellfamilj | Firmware med GS1 Digital Link släppt |
|---|---|
| DS81x8 | 16 april 2025 |
| MP7000 | 21 april 2025 |
| DS9908 | 6 juni 2025 |
| SP72 / SM72 | 11 juli 2025 |
| DS46x8 | 28 juli 2025 |
| MP7200 (Rev-C), DS5502 | 30 juli 2025 |
| DS9308 | 29 augusti 2025 |
| DS22x8, DS1000 | 5 september 2025 (endast Asien–Stillahavsområdet) |
| DS36x8 (inklusive KD och XR), MS47 | 30 september 2025 |
| PL5000, SE4107 | 15 oktober 2025 |
| CS6080 | 31 december 2025 |

Ingen av de här funktionerna behövs för att läsa in GS1 Digital Link-koder i GAIA Desktop: med dem
avstängda läser en Zebra 2D-skanner med aktuell firmware en Digital Link-QR-kod som vilken annan som
helst, och GAIA Desktop omvandlar URI:n till dess AI:er.

Källor: Zebra, *DS8108 Product Reference Guide*, MN-002926-11EN Rev A (augusti 2025), kapitel 12
”GS1 Digital Link Parameters” och kapitlet Data Parsing; Zebra, *123Scan release notes*;
Barcode Factory, *Upgrade Zebra Scanners for GS1 Digital Link* (firmwaredatum).
