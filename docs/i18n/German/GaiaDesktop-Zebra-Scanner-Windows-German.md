# Einrichtung eines Zebra-Scanners — GAIA Desktop für Windows

Wie Sie einen Zebra-Barcodescanner installieren und einrichten, damit GAIA Desktop GS1-Barcodes
direkt von ihm liest: Jeder Scan wird analysiert und dem Verlauf hinzugefügt, mit unverändertem
AIM-Code-ID-Präfix und jedem FNC1-Trennzeichen. Originalfassung:
[GaiaDesktop-Zebra-Scanner-Windows.md](../../GaiaDesktop-Zebra-Scanner-Windows.md).

GAIA Desktop wurde mit einem per USB angeschlossenen Handheld-Imager **Zebra DS8108** entwickelt
und getestet. Die Schritte gelten für jeden Zebra-2D-Scanner, den Zebras Scanner SDK unterstützt;
die Konfigurationsbarcodes und Menünamen eines bestimmten Modells stehen in dessen eigenem Quick
Reference Guide oder Product Reference Guide.

> **Nur Windows.** Zebras Scannertreiber gibt es nur für Windows, daher erzeugt GAIA Desktop unter
> macOS oder Linux keine Scanner-Bedienelemente. Alles andere in der Anwendung funktioniert auf
> jeder Plattform.

---

## Inhalt

1. [Was Sie brauchen](#1-was-sie-brauchen)
2. [Java installieren](#2-java-installieren)
3. [Das Zebra Scanner SDK installieren](#3-das-zebra-scanner-sdk-installieren)
4. [123Scan installieren](#4-123scan-installieren)
5. [Die Firmware des Zebra-Scanners aktualisieren](#5-die-firmware-des-zebra-scanners-aktualisieren)
6. [Den USB-Modus des Zebra-Scanners einstellen](#6-den-usb-modus-des-zebra-scanners-einstellen)
7. [Den Zebra-Scanner für GS1 einrichten](#7-den-zebra-scanner-für-gs1-einrichten)
8. [GAIA Desktop installieren und starten](#8-gaia-desktop-installieren-und-starten)
9. [Den Scanner verbinden](#9-den-scanner-verbinden)
10. [Konfigurationsreferenz](#10-konfigurationsreferenz)
11. [Fehlerbehebung](#11-fehlerbehebung)
12. [Unterstützte Zebra-2D-Scanner](#12-unterstützte-zebra-2d-scanner)

---

## 1. Was Sie brauchen

| | |
|---|---|
| **Scanner** | Ein Zebra-2D-Scanner mit USB-Kabel — siehe [Unterstützte Zebra-2D-Scanner](#12-unterstützte-zebra-2d-scanner) |
| **Computer** | Windows 10 oder 11, 64 Bit |
| **Java** | Java 21 oder neuer, **64 Bit** |
| **Zebra Scanner SDK für Windows** | **64 Bit**, von Zebras Support-Website — liefert den Treiber und die JavaPOS-Dienste, die GAIA Desktop verwendet |
| **Zebra 123Scan** | Zebras kostenloses Konfigurationsprogramm, von Zebras Support-Website — zum Aktualisieren der Scanner-Firmware und zum Prüfen der Einstellungen |
| **GAIA Desktop** | `gaia-desktop-1.0.0-win.zip` |
| **Optional** | Der **Product Reference Guide** (PRG) des Scanners mit den Konfigurationsbarcodes |

So greifen die Teile ineinander:

```
Zebra scanner ──USB──► Zebra CoreScanner driver ──► Zebra JavaPOS services ──► GAIA Desktop
                       (installed by the SDK)        (in the SDK's JPOS\bin)    (run.bat puts them on
                                                                                 the class path)
```

GAIA Desktop bringt die JavaPOS-API selbst mit, ebenso die Konfiguration, die den Scanner benennt
(`jpos.xml`, in `gaia-desktop.jar`). Den Rest liefert das Zebra SDK: den Treiber, der mit dem
Scanner spricht, und die Zebra-Dienstklassen zwischen JavaPOS und diesem Treiber.

---

## 2. Java installieren

GAIA Desktop braucht **Java 21 oder neuer**, und zwar als **64-Bit**-Version, passend zu den
64-Bit-Treibern von Zebra.

1. Installieren Sie ein Java-21-JDK oder -JRE für Windows x64 — zum Beispiel Eclipse Temurin von
   adoptium.net. Lassen Sie das Installationsprogramm `JAVA_HOME` setzen, wenn es das anbietet.
2. Prüfen Sie es in einer Eingabeaufforderung:

   ```
   java -version
   ```

   Die erste Zeile sollte Version 21 oder höher zeigen; ein 64-Bit-Java gibt außerdem
   `64-Bit Server VM` aus.

`run.bat` sucht Java in dieser Reihenfolge: ein Ordner `jdk*` neben `run.bat`, dann `JAVA_HOME`,
dann `java` im `PATH`. Alles vor Version 21 lehnt es mit einer entsprechenden Meldung ab.

---

## 3. Das Zebra Scanner SDK installieren

1. Laden Sie das **Zebra Scanner SDK für Windows (64 Bit)** von Zebras Support-Website herunter.
2. Starten Sie das Installationsprogramm. Bietet es eine Auswahl von Komponenten an, achten Sie
   darauf, dass die Komponente **JPOS** (JavaPOS) enthalten ist.
3. Behalten Sie den vorgegebenen Installationsort bei. GAIA Desktop sucht den JavaPOS-Teil des SDK
   hier:

   ```
   C:\Program Files\Zebra Technologies\Barcode Scanners\Scanner SDK\JPOS\
   ```

   und erwartet Zebras Dienst-Jars (`JposServiceScanner.jar`, `JposServiceJniScanner.jar`, …) und
   die nativen Bibliotheken (`CSJPOSScanner64.dll`, …) in dessen Ordner `bin\`. Ist das SDK an
   anderer Stelle installiert, siehe [ZEBRA_HOME](#zebra_home).
4. **Starten Sie Windows neu**, nachdem Sie es installiert haben. Der CoreScanner-Treiber des SDK
   läuft im Hintergrund und muss laufen, bevor GAIA Desktop den Scanner erreichen kann; ein
   Neustart ist der einfachste Weg, das sicherzustellen.

---

## 4. 123Scan installieren

123Scan ist Zebras kostenloses Programm zum Konfigurieren seiner Scanner. Im nächsten Schritt
aktualisiert es die Firmware des Scanners, und danach ist es der einfachste Weg, die Einstellungen
des Scanners zu prüfen und zu ändern.

1. Laden Sie **123Scan** von Zebras Support-Website herunter und starten Sie das
   Installationsprogramm.
2. Schließen Sie den Zebra-Scanner an und starten Sie 123Scan. Es sollte den Scanner finden und
   Modell, Seriennummer und aktuelle Firmware-Version anzeigen. Wenn nicht, probieren Sie einen
   anderen USB-Anschluss oder ein anderes Kabel, bevor Sie weitermachen — einen Scanner, den
   123Scan nicht sieht, sieht auch GAIA Desktop nicht.

---

## 5. Die Firmware des Zebra-Scanners aktualisieren

Bringen Sie die Firmware des Scanners auf den neuesten Stand, bevor Sie ihn einrichten.
Firmware-Updates beheben Decodierprobleme und halten den Scanner im Einklang mit dem Zebra-Treiber,
den das SDK installiert hat.

1. Schließen Sie **GAIA Desktop** und jedes andere Programm, das den Scanner verwenden könnte:
   123Scan braucht den Scanner während des Updates für sich allein.
2. Wählen Sie in 123Scan bei angeschlossenem Scanner dessen Option zur Firmware-Aktualisierung.
   123Scan vergleicht die Firmware des Scanners mit der neuesten, die Zebra veröffentlicht, und
   bietet an, sie herunterzuladen und zu installieren. Hat der Computer keinen Internetzugang,
   laden Sie die Firmware des Scanners von seiner Support-Seite auf Zebras Website herunter und
   laden Sie die Datei in 123Scan.
3. Starten Sie das Update und **lassen Sie den Scanner angeschlossen, bis 123Scan meldet, dass es
   fertig ist**. Es dauert einige Minuten; am Ende startet der Scanner neu. Wird er mittendrin
   abgezogen, kann er unbrauchbar werden.
4. Prüfen Sie, dass 123Scan jetzt die neue Firmware-Version anzeigt.

Ein Update kann einige Einstellungen auf ihre Vorgaben zurücksetzen; deshalb wird der Scanner erst
in den nächsten beiden Schritten eingerichtet, nach der Firmware.

---

## 6. Den USB-Modus des Zebra-Scanners einstellen

Dieser Schritt wird am häufigsten übersehen. **Ab Werk verhält sich ein Zebra-Scanner wie eine
USB-Tastatur** (Modus USB HID Keyboard): Er „tippt“ jeden Barcode in das Element, das den Fokus
hat. In diesem Modus findet *Scanner verbinden* in GAIA Desktop ihn nicht, und alles, was in ein
Textfeld getippt wird, verliert seine FNC1-Trennzeichen — was die Bedeutung einer
GS1-Elementkette ändert.

Stellen Sie den Scanner auf einen Modus um, mit dem der Zebra-Treiber direkt spricht:

| USB-Modus des Scanners | Gerätename in GAIA Desktop | Hinweise |
|---|---|---|
| **IBM Hand-held USB** | `ZebraUSBHandHeld` oder `ZebraAllScanners` | Empfohlen |
| SNAPI (mit oder ohne Imaging) | `ZebraScannerSNAPI` oder `ZebraAllScanners` | Zebras eigenes USB-Protokoll; funktioniert ebenfalls |
| IBM Table-top USB | `ZebraUSBTableTop` oder `ZebraAllScanners` | Für Tischscanner gedacht |
| USB OPOS (Hand-held) | `ZebraUSBOPOS` oder `ZebraAllScanners` | |
| USB HID Keyboard (Werkseinstellung) | — | **Verbindet sich nicht** |

`ZebraAllScanners`, der vorgegebene Gerätename in GAIA Desktop, akzeptiert einen Scanner in jedem
der unterstützten Modi; ist der Modus eingestellt, genügt die Vorgabe daher meist.

Den Modus ändern Sie auf eine von zwei Arten:

- **Scannen Sie den Konfigurationsbarcode** für den Modus — etwa *IBM Hand-held USB* — im Quick
  Reference Guide oder Product Reference Guide des Scanners (Abschnitt USB Host Types / USB Device
  Type); der Scanner bestätigt mit einem Piepton. Oder
- **verwenden Sie 123Scan** ([Schritt 4](#4-123scan-installieren)): Scanner anschließen, seine
  Konfiguration öffnen und den USB-Host-Typ einstellen.

Nach einem Moduswechsel erkennt Windows den Scanner als andere Geräteart neu; warten Sie ein paar
Sekunden, bevor Sie verbinden.

Um den Scanner später wieder in den Tastaturmodus zu versetzen — für andere Software — scannen Sie
den Konfigurationsbarcode *USB Keyboard (HID)* oder stellen Sie seine Werkseinstellungen wieder her.

---

## 7. Den Zebra-Scanner für GS1 einrichten

GAIA Desktop analysiert am besten, wenn der Scanner mitteilt, welche Art Barcode er gelesen hat.

### Die AIM Code ID übertragen

Stellen Sie **Transmit Code ID Character** auf **AIM Code ID Character** — mit dem
Konfigurationsbarcode im Product Reference Guide (User Preferences) oder in 123Scan. Jeder Scan
beginnt dann mit seinem Symbologie-Kennzeichen — `]C1` für GS1-128, `]d2` für GS1 DataMatrix,
`]Q3` für GS1 QR Code, `]e0` für GS1 DataBar —, an dem GAIA Desktop den Datenträger erkennt und
dessen Regeln anwendet.

Ohne sie analysiert GAIA Desktop die Elementkette trotzdem und entnimmt die Symbologie dem Scan
selbst, aber der Bericht hat keine AIM Code ID anzuzeigen, und ein einfacher Barcode wie ein EAN-13
lässt sich schwerer von einer GS1-Elementkette unterscheiden.

### Die Symbologien prüfen

Stellen Sie sicher, dass die Barcodearten, mit denen Sie arbeiten, **aktiviert** sind: meist
GS1-128, GS1 DataMatrix, GS1 DataBar, GS1 QR Code und EAN/UPC. Die meisten sind bei Zebras
2D-Scannern ab Werk aktiv; 123Scan zeigt die aktuellen Einstellungen, und der Product Reference
Guide enthält für jede einen Barcode zum Aktivieren.

### Zebras GS1-Analysefunktionen ausgeschaltet lassen

Neuere Firmware bringt GS1-Funktionen mit, die im Scanner selbst arbeiten — **Label Parse+**,
**GS1 Digital Link** und **GS1 Retail POS parsing**. Lassen Sie sie **ausgeschaltet**, wie der
Scanner ausgeliefert wird: Sie formatieren um oder kürzen, was der Scanner sendet, und GAIA Desktop
braucht die Rohdaten, um sie zu analysieren und zu prüfen. Was jede davon tut, steht unter
[Unterstützte Zebra-2D-Scanner](#12-unterstützte-zebra-2d-scanner).

### Keine Präfixe und Suffixe

Fügen Sie der Ausgabe des Scanners kein Präfix, kein Suffix und keine Enter-Taste hinzu. GAIA
Desktop empfängt jeden Scan über JavaPOS als vollständige Nachricht und braucht nichts davon;
zusätzliche Zeichen würden in der Elementkette landen.

---

## 8. GAIA Desktop installieren und starten

1. Entpacken Sie `gaia-desktop-1.0.0-win.zip`, zum Beispiel nach `C:\Tools\`. Die Datei enthält
   einen einzigen Ordner:

   ```
   gaia-desktop-1.0.0-win\
   ├── docs\
   │   └── GaiaDesktop-Zebra-Scanner-Windows.md      this guide
   ├── gaia-desktop.jar
   ├── lib\
   ├── readme.html                                   index of every guide
   └── run.bat
   ```

2. Schließen Sie den Scanner an.
3. Doppelklicken Sie auf **`run.bat`**. Neben der Anwendung öffnet sich ein Konsolenfenster und
   bleibt offen: Scannerstatus und JavaPOS-Fehler werden dort gemeldet.
4. Lesen Sie die erste Zeile in der Konsole. Sie sagt, was `run.bat` gefunden hat:

   ```
   run.bat: Java 21.0.8 from JAVA_HOME; JavaFX from lib\; JavaPOS from lib\; Zebra services from C:\Program Files\Zebra Technologies\Barcode Scanners\Scanner SDK\JPOS\
   ```

   Endet sie auf **`Zebra services from none (Zebra Scanner SDK not installed)`**, wurde das SDK an
   seinem vorgegebenen Ort nicht gefunden — installieren Sie es
   ([Schritt 3](#3-das-zebra-scanner-sdk-installieren)) oder setzen Sie [ZEBRA_HOME](#zebra_home). Die
   Anwendung startet trotzdem, aber der Scanner kann sich nicht verbinden.

---

## 9. Den Scanner verbinden

1. In der Werkzeugleiste listet die **Geräteauswahl** die Scannernamen auf, die GAIA Desktop kennt.
   Lassen Sie sie auf **`ZebraAllScanners`**, oder wählen Sie den Namen für den USB-Modus des
   Scanners aus der Tabelle in [Schritt 6](#6-den-usb-modus-des-zebra-scanners-einstellen).
2. Klicken Sie auf **Scanner verbinden** oder drücken Sie **Strg+K** (auch im Menü
   **Scanner**).
3. Die Statusleiste wechselt zu **`Scanner: ZebraAllScanners`** (oder dem gewählten
   Namen), und die Statuszeile meldet *Scanner verbunden — ZebraAllScanners*. Die Schaltfläche
   heißt jetzt **Scanner trennen**.
4. Scannen Sie einen GS1-Barcode. Er erscheint oben im Erfassungsverlauf, und sein Bericht öffnet
   sich. Mit eingeschalteter AIM Code ID nennt der Bericht den Datenträger — *GS1-128*,
   *GS1 DataMatrix* —, und die Elementkette behält jedes FNC1-Trennzeichen.

Die Geräteauswahl merkt sich Ihre Wahl über Programmstarts hinweg. Der Scanner bleibt verbunden,
bis Sie ihn trennen oder die Anwendung schließen. Scans funktionieren auch in den Modi
**Erstellen** und **Kombinieren**: Im Modus Kombinieren wird jeder Scan zu einem der
Barcodes, die kombiniert werden.

Solange GAIA Desktop den Scanner belegt, können andere Programme ihn nicht verwenden, und
umgekehrt: Schließen Sie 123Scan und jede andere Kassen- oder Scannersoftware, bevor Sie verbinden.

---

## 10. Konfigurationsreferenz

### Gerätenamen

Die Geräteauswahl wird aus der in `gaia-desktop.jar` enthaltenen `jpos.xml` gefüllt. Ihre
Scanner-Einträge:

| Gerätename | Scannertyp | Verwenden mit |
|---|---|---|
| `ZebraAllScanners` | alle | **Vorgabe.** Jeder unterstützte USB-Modus |
| `ZebraAllScanners114` | alle | Die JavaPOS-1.14-Version desselben Dienstes |
| `ZebraUSBHandHeld` | IBM Hand-held USB | Ein Scanner im Modus IBM Hand-held USB |
| `ZebraUSBTableTop` | IBM Table-top USB | Tischmodus |
| `ZebraUSBOPOS` | USB OPOS | Modus USB OPOS |
| `ZebraScannerSNAPI` | SNAPI | Modus SNAPI |
| `ZebraScannerUSB` | jedes USB | Jeder USB-Modus |
| `ZebraScannerSerialSSI` | seriell SSI | Scanner mit seriellem Kabel (nicht USB) |
| `ZebraScannerSerialNIXMODB` | seriell NIXMODB | Scanner mit seriellem Kabel |
| `ZebraVDI` | VDI | An einen virtuellen Desktop durchgereichte Scanner |

### ZEBRA_HOME

Ist das Zebra SDK an einem anderen als dem vorgegebenen Ort installiert, setzen Sie `ZEBRA_HOME`
auf dessen Ordner `JPOS`, bevor Sie `run.bat` starten — zum Beispiel in einer kleinen Batchdatei
daneben:

```bat
@echo off
set "ZEBRA_HOME=D:\Zebra\Scanner SDK\JPOS\"
call "%~dp0run.bat" %*
```

`run.bat` nimmt Zebras Dienst-Jars dann aus `%ZEBRA_HOME%bin` und ergänzt diesen Ordner im `PATH`
für die Zebra-DLLs.

### Eine eigene jpos.xml

Um einen Gerätenamen hinzuzufügen oder die Einstellungen eines Eintrags zu ändern, kopieren Sie
`jpos.xml` aus `gaia-desktop.jar` heraus (es ist ein Zip; jedes Entpackprogramm öffnet es),
bearbeiten Sie die Kopie und verweisen Sie GAIA Desktop darauf:

```bat
set GAIA_DESKTOP_OPTS=-Djpos.config.populatorFileURL=file:///C:/Tools/jpos.xml
run.bat
```

Die Geräteauswahl listet dann die Scanner aus dieser Datei. Sind mehrere Zebra-Scanner
angeschlossen, lässt sich ein Eintrag über seine Eigenschaft `ScannerModel` oder `SerialNumber` an
einen von ihnen binden, wie im Eintrag `ZebraAllScanners`.

### Weitere Optionen

| Einstellung | Wie |
|---|---|
| Sprache der Anwendung | `run.bat --language=FR` (nur für diesen Start) oder der Optionsbereich |
| Zusätzliche Java-Optionen | `set GAIA_DESKTOP_OPTS=…` vor `run.bat` |
| Ein mitgeliefertes Java | einen Ordner `jdk-21…` neben `run.bat` legen; er wird vor `JAVA_HOME` verwendet |

---

## 11. Fehlerbehebung

Fehler erscheinen beim Verbinden in einem Dialog, im Konsolenfenster und in der Protokolldatei
`logs\gaia-desktop.log` in dem Ordner, aus dem `run.bat` gestartet wurde (dem GAIA-Desktop-Ordner,
wenn `run.bat` per Doppelklick gestartet wird). Die Zahl in eckigen Klammern im Protokoll ist der
JavaPOS-Fehlercode.

| Was Sie sehen | Wahrscheinliche Ursache | Was zu tun ist |
|---|---|---|
| Es gibt keine Schaltfläche *Scanner verbinden* | Läuft nicht unter Windows | Scannen gibt es nur unter Windows |
| 123Scan findet den Scanner nicht | Kabel, USB-Anschluss, oder ein anderes Programm belegt den Scanner | Anderes Kabel oder anderen Anschluss probieren; GAIA Desktop und andere Scannersoftware schließen. Einen Scanner, den 123Scan nicht sieht, sieht auch GAIA Desktop nicht |
| Konsole: `Zebra services from none (Zebra Scanner SDK not installed)` | Das SDK ist nicht an seinem vorgegebenen Ort | Das 64-Bit-SDK installieren ([Schritt 3](#3-das-zebra-scanner-sdk-installieren)) oder [ZEBRA_HOME](#zebra_home) setzen |
| Fehler **104**: *Could not connect to service with logicalName = …: …SymScannerSvc112Factory* | Zebras Dienst-Jars wurden nicht gefunden | Wie oben — die Konsolenzeile `Zebra services from` prüfen |
| Fehler **109**: *Service does not exist in loaded JCL registry* | Der Gerätename steht nicht in der Scannerkonfiguration, oder die `javaPOS114.jar` des SDK ist in den Klassenpfad geraten | Einen Namen aus der Geräteauswahl wählen; `javaPOS114.jar` nicht hinzufügen; bei eigener `jpos.xml` prüfen, dass der Name darin steht |
| Fehler **107** (keine Hardware), oder die Verbindung scheitert bei angeschlossenem Scanner | Der Scanner ist noch im USB-Tastaturmodus, oder der CoreScanner-Treiber läuft nicht | USB-Modus einstellen ([Schritt 6](#6-den-usb-modus-des-zebra-scanners-einstellen)); Windows nach der SDK-Installation neu starten; anderen USB-Anschluss probieren |
| Fehler **112** (Zeitüberschreitung) oder **102** (belegt) beim Verbinden | Ein anderes Programm belegt den Scanner | 123Scan und jede andere Scanner- oder Kassensoftware schließen, dann erneut verbinden |
| `UnsatisfiedLinkError` … `CSJPOSScanner64` in der Konsole | Die Zebra-DLLs lassen sich nicht laden — oft ein 32-Bit-SDK mit einem 64-Bit-Java | Das 64-Bit-SDK installieren; prüfen, dass `java -version` eine 64-Bit-VM meldet |
| Scans werden in die Eingabezeile getippt, statt als Erfassungen anzukommen | Der Scanner ist im USB-Tastaturmodus | USB-Modus einstellen ([Schritt 6](#6-den-usb-modus-des-zebra-scanners-einstellen)) |
| Verbunden, aber Scans kommen nicht an | Falscher Gerätename für den Modus des Scanners, oder der Scanner wurde bei bestehender Verbindung neu eingesteckt | Trennen, `ZebraAllScanners` wählen, erneut verbinden |
| Der Bericht zeigt keine AIM Code ID bzw. keinen Datenträger | Die Übertragung der AIM Code ID ist aus | *Transmit Code ID Character → AIM Code ID* einschalten ([Schritt 7](#7-den-zebra-scanner-für-gs1-einrichten)) |
| Einer Elementkette fehlen die Trennzeichen, oder AIs laufen ineinander | Im Tastaturmodus gescannt, oder von Hand ohne `{GS}` getippt | Einen JavaPOS-USB-Modus verwenden; beim Tippen Trennzeichen mit der Schaltfläche `{GS}` einfügen |
| Eine Barcodeart wird nie gelesen | Diese Symbologie ist im Scanner deaktiviert | Sie aktivieren ([Schritt 7](#7-den-zebra-scanner-für-gs1-einrichten)) |

Bleibt ein Problem bestehen, heben Sie die Konsolenzeile `run.bat:`, den Text des Fehlerdialogs und
`logs\gaia-desktop.log` zusammen auf: Zusammen zeigen sie, welches Java, welche Zebra-Dienste und
welcher Gerätename verwendet wurden und wo genau die Verbindung gescheitert ist.

---

## 12. Unterstützte Zebra-2D-Scanner

GAIA Desktop braucht einen Scanner, der die Barcodes lesen kann, in denen GS1-Daten gedruckt
werden — GS1-128, GS1 DataBar, EAN/UPC und für die meisten Zwecke die 2D-Codes **GS1 DataMatrix**
und **GS1 QR Code**, in dem auch eine **GS1-Digital-Link**-URI üblicherweise gedruckt wird. Das
heißt: ein Zebra-**2D-Imager**; ein linearer (1D-)Scanner kann DataMatrix- oder QR-Codes überhaupt
nicht lesen.

Der Scanner muss GS1 Application Identifiers oder Digital Link nicht selbst verstehen.
**GAIA Desktop analysiert beides**: Es nimmt den Scan so, wie der Scanner ihn gelesen hat — mit AIM
Code ID und FNC1-Trennzeichen —, zerlegt ihn in AIs oder wandelt eine Digital-Link-URI selbst in
ihre AIs um.

### Getestet

| Scanner | Typ | Status |
|---|---|---|
| **DS8108** | Kabelgebundener Handheld-2D-Imager | Mit GAIA Desktop **entwickelt und getestet** |

### Sollten funktionieren

Diese Zebra-2D-Imager werden wie der DS8108 über Zebras Scanner SDK und JavaPOS-Dienste
angesteuert. Sie wurden **nicht** mit GAIA Desktop **getestet**; richten Sie jeden wie in dieser
Anleitung ein, mit seinem eigenen Quick Reference Guide oder Product Reference Guide für die
Konfigurationsbarcodes.

| Familie | Modelle (Beispiele) |
|---|---|
| DS81x8 | DS8108 (kabelgebunden), DS8178 (kabellos) |
| DS22x8 | DS2208 (kabelgebunden), DS2278 (kabellos) — Allzweck |
| DS36x8 | DS3608 (kabelgebunden), DS3678 (kabellos) — robust, einschließlich der Varianten KD und XR |
| DS46x8 | DS4608 (kabelgebunden), DS4678 (kabellos) |
| DS9308 / DS9908 | Präsentations- und Freihandscanner |
| MP7000 / MP7200 | Mehrflächen-Bioptik-Scanner und Scanner-Waagen |
| CS6080 | Kabelloser Begleitscanner |

Ein kabelloser Scanner spricht mit seiner Ladestation, und die Ladestation wird an den Computer
angeschlossen: Verbinden Sie die Station per USB und behandeln Sie das Paar als einen Scanner.
Scanner anderer Zebra-Familien können ebenfalls funktionieren, wenn Zebras Scanner SDK sie in einem
der USB-Modi aus [Schritt 6](#6-den-usb-modus-des-zebra-scanners-einstellen) unterstützt.

### GS1-Funktionen im Zebra-Scanner

Unabhängig vom Lesen der Barcodes können manche Zebra-2D-Imager GS1-Daten im Scanner selbst
verarbeiten, eingerichtet über 123Scan:

| Funktion | Was sie tut | Für GAIA Desktop |
|---|---|---|
| **Label Parse+** | Analysiert die AIs eines GS1-Etiketts im Scanner und sendet umformatierte Felder | **Ausgeschaltet lassen** — GAIA Desktop sähe die AIs, FNC1-Trennzeichen und die AIM Code ID nicht mehr |
| **GS1 Digital Link** | Für Handelsartikel mit einem 1D-EAN/UPC und einem 2D-Digital-Link-Code: wählt, welcher gesendet wird | **Ausgeschaltet lassen** (Vorgabe) |
| **GS1 Retail POS parsing** | Liest 2D-Handelsbarcodes in Digital-Link-URI- oder Elementketten-Syntax | **Ausgeschaltet lassen** |

Die Einstellung **GS1 Digital Link** hat drei Modi. **Mode 1 — GTIN only**, der Modus nach dem
Einschalten, sendet nur die GTIN, gleich aus welchem Barcode, sodass eine Charge, ein
Verfallsdatum oder eine Seriennummer im Digital Link GAIA Desktop nie erreichen würde.
**Mode 2 — Prioritize 2D** sendet den 2D-Code, wenn er innerhalb der Priorisierungszeit (Vorgabe
200 ms) erkannt wird, sonst den 1D-Code. **Mode 3 — Corroborate 1D and 2D GTINs Match** sendet den
1D-Code und zusätzlich den 2D-Code, wenn dessen GTIN übereinstimmt. Muss die Einstellung für andere
Software eingeschaltet sein, verwenden Sie Mode 2, nicht Mode 1. Beim DS8108 sind das die Parameter
2373 (ein/aus), 2374 (Modus) und 2491 (Zeitlimit).

Diese Funktionen setzen **123Scan ab Version 6** und eine Firmware voraus, die sie enthält. Zebra
ergänzt die Unterstützung für GS1 Digital Link seit April 2025 per Firmware-Update; die folgenden
Daten stammen von einem Zebra-Händler, prüfen Sie daher die Firmware-Versionshinweise jedes Modells:

| Modellfamilie | Firmware mit GS1 Digital Link veröffentlicht |
|---|---|
| DS81x8 | 16. April 2025 |
| MP7000 | 21. April 2025 |
| DS9908 | 6. Juni 2025 |
| SP72 / SM72 | 11. Juli 2025 |
| DS46x8 | 28. Juli 2025 |
| MP7200 (Rev-C), DS5502 | 30. Juli 2025 |
| DS9308 | 29. August 2025 |
| DS22x8, DS1000 | 5. September 2025 (nur Asien-Pazifik) |
| DS36x8 (einschließlich KD und XR), MS47 | 30. September 2025 |
| PL5000, SE4107 | 15. Oktober 2025 |
| CS6080 | 31. Dezember 2025 |

Keine dieser Funktionen ist nötig, um GS1-Digital-Link-Codes in GAIA Desktop einzulesen: Bei
ausgeschalteten Funktionen liest ein Zebra-2D-Scanner mit aktueller Firmware einen Digital-Link-QR-Code
wie jeden anderen, und GAIA Desktop wandelt die URI in ihre AIs um.

Quellen: Zebra, *DS8108 Product Reference Guide*, MN-002926-11EN Rev A (August 2025), Kapitel 12
„GS1 Digital Link Parameters“ und Kapitel Data Parsing; Zebra, *123Scan release notes*;
Barcode Factory, *Upgrade Zebra Scanners for GS1 Digital Link* (Firmware-Daten).
