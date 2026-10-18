# GAIA Desktop — Benutzerhandbuch

Wie die Anwendung benutzt wird. Zum Bauen, Ändern oder Hinzufügen einer Sprache siehe das
[Entwicklerhandbuch](../../GaiaDesktop-Developer.md) (auf Englisch). Originaldokument:
[GaiaDesktop-User.md](../../GaiaDesktop-User.md).

## Inhaltsverzeichnis

1. [Was es ist](#was-es-ist)
2. [Starten](#starten)
3. [Das Fenster](#das-fenster)
4. [Modus Analysieren](#modus-analysieren)
   - [Die Eingabezeile und das {GS}-Token](#die-eingabezeile-und-das-gs-token)
   - [Scannen](#scannen)
   - [Eine Datei öffnen](#eine-datei-öffnen)
   - [Der Erfassungsverlauf](#der-erfassungsverlauf)
5. [Modus Erstellen](#modus-erstellen)
   - [Der AI-Editor](#der-ai-editor)
   - [Was der Editor ablehnt](#was-der-editor-ablehnt)
   - [Elementkette oder Digital Link](#elementkette-oder-digital-link)
   - [Eine vorhandene Erfassung bearbeiten](#eine-vorhandene-erfassung-bearbeiten)
6. [Modus Kombinieren](#modus-kombinieren)
   - [Der Streifen Kombinieren](#der-streifen-kombinieren)
   - [Was Kombinieren ablehnt](#was-kombinieren-ablehnt)
   - [Duplikate und Konflikte](#duplikate-und-konflikte)
   - [Der Kombinationsbericht](#der-kombinationsbericht)
7. [Der Bericht](#der-bericht)
   - [Exportieren](#exportieren)
8. [Analyseoptionen](#analyseoptionen)
   - [Erster Start](#erster-start)
9. [Sprachen](#sprachen)
10. [Was gespeichert bleibt](#was-gespeichert-bleibt)
11. [Tastenkürzel](#tastenkürzel)
12. [Fehlerbehebung](#fehlerbehebung)
13. [Grenzen](#grenzen)

---

## Was es ist

GAIA Desktop erledigt drei Aufgaben mit GS1-Daten:

- **Analysieren** — geben Sie eine GS1-Elementkette oder einen Digital-Link-URI ein, fügen Sie
  sie ein, öffnen Sie sie aus einer Datei oder scannen Sie sie, und lesen Sie das Ergebnis als
  vollständigen Bericht: was jeder Anwendungsidentifikator (AI) bedeutet, was nicht stimmt und
  warum;
- **Erstellen** — AI-/Wert-Paare zu einer Elementkette oder einem Digital-Link-URI
  zusammensetzen;
- **Kombinieren** — bis zu drei GS1-128-Strichcodes eines Etiketts zu der einen
  Elementkette zusammenführen, die ein GS1 DataMatrix tragen würde, sie als Ganzes prüfen und
  das Symbol zeichnen.

Alles, was die Anwendung über GS1 weiß — jede AI-Definition, jede Prüfregel, jede Fehlermeldung
und jede lokalisierte Beschreibung — stammt aus der darunterliegenden GAIA-Engine. Die Anwendung
fügt keine eigenen Regeln hinzu: Sie akzeptiert genau das, was die Engine akzeptiert.

---

## Starten

```bash
java -jar gaia-desktop.jar
```

Die JAR-Datei benötigt das mitgelieferte Verzeichnis `lib/` daneben — lassen Sie beides
zusammen, sonst startet die Anwendung nicht. (Es lässt sich auch eine eigenständige JAR-Datei
bauen; siehe das [Entwicklerhandbuch](../../GaiaDesktop-Developer.md#packaging).)

**Java 21 oder neuer** muss installiert sein. Das Paket gilt jeweils für ein Betriebssystem und
eine Prozessorarchitektur; verwenden Sie die für Ihren Rechner gebaute JAR-Datei.

Um die Anwendung in einer bestimmten Sprache zu öffnen, ohne die gespeicherte Sprache zu ändern:

```bash
java -jar gaia-desktop.jar --language=FR
```

Als Wert sind der Katalogcode (`FR`, `PCM`, `ARZ`), der vollständige Name (`FRENCH`,
`WU-CHINESE`) oder ein Sprach-Tag (`fr`, `zh-HK` für Kantonesisch, `ar-EG` für ägyptisches
Arabisch) möglich — Groß- und Kleinschreibung spielt keine Rolle. Die Vorgabe gilt nur für
diesen Lauf: Ihre gespeicherte Sprache bleibt unangetastet, sodass eine Verknüpfung für eine
Kollegin nicht ändern kann, in welcher Sprache Ihre eigene Kopie startet. Eine Sprache *in* der
Anwendung zu wählen ist dagegen eine echte Entscheidung und wird wie gewohnt gespeichert. Ein
Wert, der keine Sprache benennt, wird mit einem Hinweis in der Konsole ignoriert; die Anwendung
startet dann in der gespeicherten Sprache, statt den Start zu verweigern.

Beim Start gibt die Konsole eine Zeile mit der Version der Anwendung, ihrem Build-Datum und der
Version der Engine aus. Genau diese Zeile gehört in einen Fehlerbericht.

---

## Das Fenster

```
┌────────────────────────────────────────────────────────────────────┐
│ menu bar                                                           │
├────────────────────────────────────────────────────────────────────┤
│ [Parse|Build|Combine]  … mode-specific controls …  Save   Options  │
├────────────────────────────────────────────────────────────────────┤
│ input bar (Parse)   or   AI editor (Build)                         │
├──────────────────┬─────────────────────────────────────────────────┤
│ Captures         │ Parse report                    │ Options       │
│ (newest first)   │                                 │ panel         │
├──────────────────┴─────────────────────────────────────────────────┤
│ status …                                     scanner: off  [About] │
└────────────────────────────────────────────────────────────────────┘
```

> Die Skizze behält die englischen Beschriftungen, wie alle Codeblöcke in dieser Dokumentation.
> Auf dem Bildschirm sehen Sie *Analysieren* / *Erstellen* / *Kombinieren*, *Erfassungen*, *Bericht speichern*,
> *Optionen*, *Scanner: aus* und *Über*.

**Unter macOS und Linux gibt es die Scanner-Zeile in diesem Bild nicht** — weder Schalter noch
Geräteauswahl in der Werkzeugleiste, kein Menü *Scanner*, kein *Scanner: aus* in der Statuszeile.
Siehe [Scannen](#scannen).

Der Umschalter **Analysieren ⇄ Erstellen** ändert nur den Streifen unter der Werkzeugleiste und
die Bedienelemente zur Erfassung. Verlauf, Bericht, Optionsbereich und Exportbefehle dienen
beiden Modi, denn eine gelungene Erstellung wird einfach zu einer weiteren Erfassung. Der Modus **Kombinieren** ist anders aufgebaut: siehe [Modus Kombinieren](#modus-kombinieren).

Der Optionsbereich ist **beim ersten Start geschlossen**. Modus, Sichtbarkeit des Bereichs,
Design und der zuletzt verwendete Scanner bleiben von Lauf zu Lauf erhalten.

**Neue Versionen.** Einige Sekunden nach dem Start, höchstens einmal am Tag, fragt die Anwendung
bei GitHub nach, ob eine neuere Version von GAIA Desktop veröffentlicht wurde. Wenn ja, erscheint
in der Statuszeile ein Link — *GAIA Desktop 1.1.0 ist verfügbar* —, der die Seite der Version in
Ihrem Browser öffnet; nichts wird für Sie heruntergeladen oder installiert.
*Diese Version überspringen* blendet den Hinweis bis zu einer späteren Version aus. Die Prüfung bleibt
stumm, wenn es nichts Neues gibt oder GitHub nicht erreichbar ist.
*Hilfe → Nach Updates suchen…* prüft sofort und meldet das Ergebnis;
*Hilfe → Automatisch nach Updates suchen* schaltet die tägliche Prüfung aus und wieder ein.

---

## Modus Analysieren

### Die Eingabezeile und das {GS}-Token

Die Zeile nimmt eine Elementkette, einen GS1-Digital-Link-URI oder einen rohen Scan samt
AIM-Code-ID-Präfix (`]d2…`) entgegen. <kbd>Eingabe</kbd> analysiert.

AIs variabler Länge werden durch **FNC1** getrennt — das unsichtbare Steuerzeichen `0x1D`. Ein
Textfeld kann dieses Zeichen nicht halten: es würde stillschweigend verschluckt, und
`01…` + FNC1 + `10LOT` käme als `01…10LOT` an — eine Elementkette mit anderer Bedeutung.

Deshalb zeigt die Anwendung das Trennzeichen als sichtbares Token **`{GS}`** und wandelt es auf
dem Weg zum Parser zurück:

- die **Schaltfläche `{GS}`** fügt das Token an der Cursorposition ein;
- werden echte Trennzeichen **eingefügt** — per Tastatur, Kontextmenü oder Drag-and-drop —,
  werden sie automatisch zu `{GS}`, es geht also nichts verloren;
- das Token wird genauso geschrieben wie im Bericht, sodass ein aus einem Bericht kopierter Wert
  unverändert wieder eingefügt werden kann.

Erfassung, Bericht und gespeicherter Verlauf enthalten alle das **echte** Trennzeichen. Das
Token ist nur das, was Sie sehen und tippen.

### Scannen

> **Nur Windows.** Zebra liefert seine Scanner-Treiber ausschließlich für Windows; unter macOS
> und Linux baut die Anwendung deshalb überhaupt keine Scanner-Bedienelemente — keinen Schalter
> *Scanner verbinden*, keine Geräteauswahl, kein Menü *Scanner* und keinen Scanner-Status in der
> Statuszeile. Nichts ist ausgegraut, denn es gibt weder eine Einstellung noch ein Gerät, das
> das aktivieren könnte. Alles Übrige, auch das Öffnen einer Datei mit gespeicherten Scans,
> funktioniert überall.

*Scanner verbinden* öffnet das in der Auswahl genannte Gerät; `ZebraAllScanners` ist die Vorgabe
und passt für die meisten Modelle. Jeder Scan wird analysiert und automatisch an den Verlauf
angehängt.

Die Scannerdaten werden Byte für Byte gelesen, sodass das AIM-Code-ID-Präfix und jedes
FNC1-Trennzeichen genau so beim Parser ankommen, wie der Scanner sie gesendet hat.

Im Modus Erstellen sind die Scanner-Bedienelemente ausgeblendet, ein verbundener Scanner
arbeitet aber weiter: *Scanner → Verbinden / Trennen* (<kbd>⌘K</kbd>) schaltet ihn weiterhin um,
die Statuszeile meldet ihn weiterhin, und ein Scan, der bei geöffnetem Editor eintrifft, wird
trotzdem erfasst. Im Modus Kombinieren bleiben die Bedienelemente sichtbar, und jeder Scan wird der
Kombination statt dem Verlauf hinzugefügt.

Die erste Einrichtung eines Scanners — das Zebra Scanner SDK, der USB-Modus des Scanners und die
GS1-Einstellungen — beschreibt die
[Einrichtungsanleitung für Zebra-Scanner](GaiaDesktop-Zebra-Scanner-Windows-German.md) Schritt für Schritt; sie liegt jedem Zip
unter `docs/` bei. Lässt sich ein Gerät nicht verbinden, siehe [Fehlerbehebung](#fehlerbehebung).

### Eine Datei öffnen

*Datei → Textdatei öffnen…* analysiert **jede nicht leere Zeile als eigene Erfassung** — der
schnelle Weg, einen Stapel Scans abzuarbeiten, den Ihnen jemand geschickt hat. Die Datei wird
Byte für Byte gelesen, rohe Trennzeichen darin überleben also.

### Der Erfassungsverlauf

Die neuesten zuerst. Jede Zeile zeigt einen Punkt für gültig / ungültig, eine einzeilige
Zusammenfassung sowie Erfassungszeit, Herkunft und die Anzahl der AIs. Das Datum erscheint nur
bei Erfassungen früherer Tage; die Zeilen von heute zeigen nur die Uhrzeit.

Die Zusammenfassung ist bei einem Digital Link der **Digital-Link-URI**, sonst der **HRI**: bei
einer Elementkette liest sich `(01)09506000134352 (17)261231` besser als
`01095060001343521726123110…`.

Mit einem Rechtsklick auf eine Erfassung können Sie ihre Eingabe oder ihren HRI kopieren, sie in
die Eingabezeile zurückschicken, sie im Editor öffnen, sie entfernen oder den Verlauf löschen.

---

## Modus Erstellen

### Der AI-Editor

Eine Tabelle aus AI-/Wert-Paaren. Tippen Sie einen AI-Code oder wählen Sie aus **allen AIs, die
die Engine kennt**, in der Liste *AI hinzufügen…*; die Spalten **Titel**, **Format** und
**Beschreibung** füllen sich beim Tippen, in der gewählten Sprache. Die Auswahl kann kein AI
anbieten, das die Erstellung ablehnen würde.

Die Tabelle hält am Ende immer **eine leere Zeile** bereit: Tippen Sie dort, erscheint die
nächste — eine Kette mit mehreren AIs braucht *Zeile hinzufügen* also nie.

Das Bearbeiten verhält sich so, wie eine Tabelle sich verhalten soll: <kbd>Eingabe</kbd>,
<kbd>Tab</kbd>, <kbd>Umschalt</kbd>+<kbd>Tab</kbd> und ein Klick daneben **übernehmen** alle,
was Sie getippt haben; <kbd>Esc</kbd> verwirft es. <kbd>Tab</kbd> geht von AI zu Wert und weiter
zur nächsten Zeile und überspringt die nicht bearbeitbaren Spalten.

### Was der Editor ablehnt

| | Was passiert |
|---|---|
| Ein AI, das die Engine nicht kennt | Die Zeile wird beim Tippen rot; die Erstellung wird mit einer Erklärung abgelehnt |
| Dasselbe AI zweimal, über die Auswahl | Wird nicht hinzugefügt — eine Meldung benennt es, und die Zeile, die es bereits enthält, wird ausgewählt |
| Dasselbe AI zweimal, von Hand getippt | Beide Zeilen werden markiert; die Erstellung wird mit dem Fehler `GE-S004` abgelehnt |
| Nichts zu erstellen | Eine Meldung; es entsteht keine Erfassung |

Ein AI darf in einer Elementkette nur einmal vorkommen — deshalb lehnt die Auswahl sofort ab,
statt eine von vornherein aussichtslose Erstellung versuchen zu lassen.

### Elementkette oder Digital Link

**Elementkette erstellen** setzt die Zeilen zusammen und fügt das FNC1-Trennzeichen überall dort
ein, wo die Norm es verlangt. **Digital Link erstellen** erzeugt einen URI; klappen Sie
*Digital-Link-Ziel* auf, um ihn statt auf das kanonische `id.gs1.org` auf Ihren eigenen Resolver
zu richten:

| Feld | Wirkung | Leer bedeutet |
|---|---|---|
| Domain | Der Host des Resolvers, z. B. `example.com` | `id.gs1.org` |
| Pfadpräfix | Ein Segment vor dem AI-Pfad, z. B. `/products` | kein Präfix |

Bei **Erfolg** verhält sich das Ergebnis genau wie ein Scan: analysiert, an den Verlauf
angehängt, als Bericht dargestellt und gespeichert. Die Erstellung prüft, indem sie ihre eigene
Ausgabe analysiert — was sie zurückgibt, ist also garantiert gültig.

Bei **Misserfolg** wird nichts erfasst. Der Grund bleibt unter den Erstellen-Schaltflächen
stehen, eine Zeile je Problem, jeweils mit dem Fehler, dem betroffenen AI und der Erklärung der
Engine selbst:

```
GE-C003  (01)  Check digit validation failed for AI (01) value ''09506000134350''
```

> Die doppelten Anführungszeichen in dieser Meldung stammen aus dem Fehlerkatalog der Engine,
> nicht aus dieser Anwendung.

### Eine vorhandene Erfassung bearbeiten

Rechtsklick auf eine Erfassung → **Erstellen** lädt ihre AIs in den Editor. Ändern Sie einen
Wert, erstellen Sie neu, und das Ergebnis landet als neue Erfassung — die ursprüngliche bleibt
unangetastet.

---

## Modus Kombinieren

Ältere Etiketten verteilen ihre Daten oft auf **mehrere GS1-128-Strichcodes** — die GTIN in
einem, Verfallsdatum und Charge in einem anderen —, weil ein einzelner linearer Strichcode nicht
alles fassen kann. Kombinieren nimmt bis zu **drei** dieser Strichcodes, führt sie zu der
einen Elementkette zusammen, die ein **GS1 DataMatrix** tragen würde, und prüft das Ergebnis als
Ganzes. Sobald es gültig ist, zeichnet die Anwendung das DataMatrix-Symbol.

Keiner dieser Strichcodes muss für sich allein vollständig sein. Eine Chargennummer ohne ihre
GTIN scheitert allein mit `GE-S005`, doch für einen Strichcode aus einem Satz ist das normal;
deshalb prüft Kombinieren sie stattdessen an der Kombination.

### Der Streifen Kombinieren

Der Wechsel zu **Kombinieren** ersetzt die Eingabezeile durch einen Streifen am unteren
Fensterrand. Der Erfassungsverlauf tritt zur Seite, denn Scans im Modus Kombinieren sind keine
Erfassungen und gelangen nie in ihn. Im Berichtsbereich erscheint stattdessen der
Kombinationsbericht:

```
┌────────────────────────────────────────────────────────────────────┐
│ [Parse|Build|Combine]   (scanner controls, on Windows)   Options   │
├────────────────────────────────────────────────────────────────────┤
│ Combine report                                   │ Options panel   │
├────────────────────────────────────────────────────────────────────┤
│ [Reset] [Remove Last] │ Barcode: [ …………………………………………… ] [Add]       │
├────────────────────────────────────────────────────────────────────┤
│ 2 of 3 GS1-128 barcodes                                   [About]  │
└────────────────────────────────────────────────────────────────────┘
```

Ein Strichcode kommt auf zwei Wegen hinein:

- **Scannen.** Unter Windows bleiben die Scanner-Bedienelemente im Modus Kombinieren in der
  Werkzeugleiste, und jeder Scan geht direkt in die Kombination statt in den Verlauf.
- **Eingeben oder einfügen** in das Feld *Strichcode* und <kbd>Eingabe</kbd> oder
  **Hinzufügen** drücken. Schreiben Sie FNC1 als **`{GS}`**, wie in der Eingabezeile. Ein
  ohne AIM Code ID eingegebener Strichcode gilt als GS1-128.

| Schaltfläche | Wirkung |
|---|---|
| **Hinzufügen** | Fügt den Strichcode aus dem Feld hinzu |
| **Letzten entfernen** | Entfernt den zuletzt hinzugefügten Strichcode (nicht verfügbar, wenn keiner da ist) |
| **Zurücksetzen** | Entfernt alle Strichcodes, leert das Feld und jede Ablehnung und beginnt von vorn |

Die Statuszeile zeigt stets, wie viele Strichcodes gehalten werden (*2 von 3 GS1-128-Strichcodes*), oder
warum der letzte abgelehnt wurde.

### Was Kombinieren ablehnt

Ein abgelehnter Strichcode wird nicht hinzugefügt. Bericht und Statuszeile nennen beide den
Grund:

| Strichcode | Abgelehnt, weil |
|---|---|
| Ein leeres Feld | Es gibt nichts hinzuzufügen |
| Ein vierter Strichcode | Ein Etikett wird aus höchstens drei kombiniert. Entfernen Sie den letzten, oder setzen Sie zurück |
| Eine andere Symbologie, laut AIM Code ID | Nur GS1-128 wird kombiniert; ein GS1 DataMatrix (`]d2`) oder ein QR Code wird mit seinem Namen abgelehnt |
| Nicht lesbare AIs | Ein Syntaxfehler; Fehlercode und Meldung der Engine werden angezeigt |
| Gar keine AIs | Es gibt nichts zu kombinieren |
| Ein bereits gescannter | Dieselbe Elementkette zweimal, etwa derselbe Strichcode erneut gescannt; der Bericht sagt, welchen früheren Strichcode er wiederholt |

Kombinieren lehnt nur ab, was es überhaupt nicht verwenden kann. Alles andere (eine falsche
Prüfziffer, ein ungültiges Datum, ein fehlender Begleit-AI) wird angenommen und an der
Kombination beurteilt.

### Duplikate und Konflikte

Die AIs werden in der Reihenfolge kombiniert, in der sie gescannt wurden. Tragen zwei
Strichcodes denselben AI:

- **mit demselben Wert**, wird er einmal behalten und unter *Duplikate — einmal behalten* aufgeführt,
  zusammen mit den Strichcodes, in denen er gelesen wurde;
- **mit unterschiedlichen Werten**, ist es ein **Konflikt**. Das Ergebnis lautet
  *KONFLIKT*, die widersprüchlichen Werte werden Strichcode für Strichcode aufgeführt,
  und **nichts wird kombiniert**, bis er gelöst ist. Entfernen Sie den fehlerhaften Strichcode,
  oder setzen Sie zurück.

### Der Kombinationsbericht

Von oben nach unten:

1. **Das Ergebnis**: *keine Scans*, *KONFLIKT*, gültig oder ungültig, und wie
   viele Strichcodes gehalten werden.
2. **Die Scans**: jeder Strichcode, wie er ankam, seine Symbologie und die Elementkette, die nach
   Entfernen der AIM Code ID übrig bleibt. Ein Problem, das nur die Kombination entscheiden kann,
   steht beim Strichcode, markiert mit *an der Kombination beurteilt*.
3. **Duplikate** und **Konflikte**, falls vorhanden.
4. **Kombiniert**: die Elementkette, ihre HRI und ihre Länge, dann die kombinierte
   Eingabe als beschriftetes Band und je eine Karte pro AI, beide so dargestellt wie im
   [Analysebericht](#der-bericht). Jeder Fehler und jede Warnung zur Kombination wird danach unter
   *Validierung* aufgeführt.
5. **Das GS1-DataMatrix-Symbol**, mit seiner HRI darunter. Es wird nur gezeichnet, wenn die
   Kombination gültig ist. Andernfalls sagt der Bericht, warum es nicht gezeichnet wurde, damit
   kein ungültiges Symbol versehentlich gedruckt werden kann.

Zum Beispiel ein Etikett mit zwei Strichcodes:

| Strichcode | Enthält | Für sich allein |
|---|---|---|
| `]C10109506000134352` | (01) GTIN | gültig |
| `]C11726123110LOT-ABC` | (17) Verfallsdatum, (10) Charge | `GE-S005` für beide AIs, an der Kombination beurteilt |

Sie ergeben zusammen `01095060001343521726123110LOT-ABC`, HRI
`(01)09506000134352 (17)261231 (10)LOT-ABC`, was gültig ist; also wird das Symbol gezeichnet. AI
`17` hat eine feste Länge, daher ist danach kein FNC1 nötig. Kombinieren fügt überall dort
eines ein, wo der Standard es verlangt.

**Bericht speichern** exportiert den Kombinationsbericht als HTML oder PDF, genauso wie einen
Analysebericht (siehe [Exportieren](#exportieren)), sobald ein Strichcode gehalten wird. Der Name
der HTML-Datei beginnt mit `gaia-combine-`.

Der Bericht folgt der gewählten Sprache und dem Design. Eine geänderte Analyseoption oder Sprache
prüft die bereits gehaltenen Strichcodes neu. Sie überstehen auch einen Wechsel zu
Analysieren oder Erstellen und zurück, aber keinen Neustart: Strichcodes im Modus
Kombinieren werden nicht gespeichert.

---

## Der Bericht

Die ausgewählte Erfassung wird als vollständiger Bericht dargestellt: ein Ergebnisbanner, die in
ein beschriftetes „Band“ zerlegte Elementkette, die Analyse-Metadaten, je eine Karte pro AI mit
seinen Interpretationen sowie Fehler und Warnungen samt Hinweisen zur Behebung.

Bei einem **Digital Link** trägt jede AI-Karte zusätzlich ihre strukturelle Rolle —
**Primärschlüssel**, **Schlüsselqualifizierer** oder **Datenattribut** — farblich in der
Reihenfolge ihres Auftretens im URI. Rollen erscheinen nur beim Digital Link: In einer einfachen
Elementkette hat ein AI keine URI-Position zu beschreiben.

Der Bericht folgt der Sprache und dem Design, die Sie gewählt haben.

### Exportieren

| | |
|---|---|
| **Bericht speichern → Als HTML…** (<kbd>⌘S</kbd>) | Schreibt genau das, was auf dem Bildschirm steht: eine einzelne, eigenständige Datei, die sich in jedem Browser öffnet |
| **Bericht speichern → Als PDF…** (<kbd>⌘P</kbd>) | Druckt den Bericht über den Druckdialog des Systems — wählen Sie dort ein PDF-Ziel |

PDF läuft über den Druckdialog, weil dort Ziel und Dateiname gewählt werden:
**PDF ▾ → Als PDF sichern** unter macOS, **Microsoft Print to PDF** unter Windows,
**In Datei drucken** unter Linux. Die Seiten sind umbrochen, der Text bleibt markierbar.

Druck und PDF verwenden immer die helle Palette, auch wenn die Anwendung im dunklen Design
läuft, und behalten die farbigen AI-Chips und das Ergebnisbanner bei.

Hat Ihr Rechner überhaupt keinen Drucker und kein PDF-Ziel eingerichtet, sagt die Anwendung das
und verweist auf den HTML-Export, der sich aus jedem Browser drucken lässt.

---

## Analyseoptionen

Der Bereich rechts enthält sämtliche Optionen der Engine. Jede Änderung **analysiert die
ausgewählte Erfassung sofort neu**; der Bereich beantwortet damit auch die Frage „wie sähe diese
Eingabe mit diesen Einstellungen aus?“ und wirkt sich nicht nur auf den nächsten Scan aus.

| Option | Werte |
|---|---|
| Analysemodus | `SYNTAX`, `CONTENT`, `DATA_CARRIER`, `INTERPRETATION` — wie weit die Analyse gehen soll |
| Sprache | 35 Meldungskataloge |
| Reihenfolge | `LITTLE` (TT/MM/JJJJ), `MIDDLE` (MM/TT/JJJJ), `BIG` (JJJJ/MM/TT) |
| Trennzeichen | `SLASH`, `HYPHEN`, `PERIOD` |
| Monat | `TWO_DIGIT` (12), `THREE_LETTER` (DEC) |
| Jahr | `FOUR_DIGIT` (2026), `TWO_DIGIT` (26) |
| „requires“-Prüfung überspringen | Weist ein AI nicht mehr ab, dessen erforderlicher Partner fehlt (`GE-S005`) |
| „excludes“-Prüfung überspringen | Weist AI-Kombinationen, die die Norm verbietet, nicht mehr ab (`GE-S006`) |

*Auf Standardwerte zurücksetzen* am Fuß des Bereichs stellt die Vorgaben der Engine wieder her.

### Erster Start

Solange nichts gespeichert ist, übernimmt die Anwendung ihre Einstellungen aus den
**Regionaleinstellungen Ihres Rechners** statt aus den Vorgaben der Engine:

- die **Sprache** stammt aus der Systemsprache, mit Rückfall auf Englisch, wenn es dafür keinen
  Katalog gibt;
- die **Datumsoptionen** werden aus dem kurzen Datumsformat Ihrer Regionaleinstellungen
  abgelesen: die Reihenfolge der Bestandteile, das Trennzeichen und ob Monat und Jahr abgekürzt
  werden.

> **Hinweis.** Eine Region, die kurze Datumsangaben mit zweistelliger Jahreszahl schreibt,
> startet hier folglich ebenfalls zweistellig: Das passt zum Rest Ihres Systems, weicht aber von
> der Vorgabe der Engine ab, die vierstellig ist. Ändern Sie es im Bereich, bleibt die Wahl
> erhalten.

---

## Sprachen

Eine Sprache zu wählen ändert **die gesamte Anwendung** — Menüs, Schaltflächen, Editor,
Optionsbereich, Statuszeile und Bericht —, nicht nur die Fehlermeldungen. Von rechts nach links
geschriebene Sprachen spiegeln Fenster und Bericht.

Alle 35 Sprachen stehen zur Verfügung, mit zwei Abdeckungsebenen:

| | Abdeckung |
|---|---|
| Analyseberichte und alle Fehlermeldungen der Engine | **alle 35 Sprachen** |
| Menüs, Schaltflächen und Beschriftungen der Anwendung | **alle 35 Sprachen** |

Die von rechts nach links geschriebenen Sprachen — Arabisch, ägyptisches Arabisch, Persisch und
Urdu — spiegeln Fenster und Bericht vom Öffnen der Anwendung an.

Die Sprache bleibt über Läufe hinweg gespeichert. Um nur einmal in einer anderen Sprache zu
starten, siehe [`--language`](#starten).

Einiges bleibt in allen Sprachen bewusst unübersetzt: die knappen Feldnamen neben den Rohwerten
(`format`, `position`, `raw`, `len` …), die Bezeichner und keine Wörter sind; die Namen der
Beispiele und der Symbologien, die GS1- und Branchenbegriffe sind; und ein langer Absatz aus der
Norm, bei dem eine halbe Übersetzung in die Irre führen würde.

---

## Was gespeichert bleibt

Zwei Dateien in `.gaia-desktop` in Ihrem Benutzerverzeichnis:

| Datei | Inhalt |
|---|---|
| `options.properties` | Alle Analyseoptionen, das Design, ob der Optionsbereich offen ist, der zuletzt verwendete Scanner, der Modus, in dem das Fenster verlassen wurde, und die Einstellungen der Update-Prüfung — an oder aus, wann sie zuletzt lief und jede Version, die Sie überspringen wollten |
| `captures.tsv` | Der Erfassungsverlauf, die neuesten zuerst, begrenzt auf 500 Einträge |

Die Optionen werden bei jeder Änderung geschrieben, der Verlauf bei jeder Änderung des Verlaufs.
Beide werden beim Beenden erneut geschrieben.

Eine der beiden Dateien zu löschen setzt genau diesen Teil zurück und sonst nichts — sie sind
getrennt, damit ein beschädigter Verlauf Sie nicht Ihre Einstellungen kostet. Auch eine von Hand
kaputt bearbeitete Datei ist nicht fatal: Jede Einstellung fällt einzeln auf ihren Standard
zurück, eine fehlerhafte Zeile kostet also nur diese Zeile.

Der Verlauf speichert die Eingabe, nicht die Analyse. Sie wird beim Laden neu analysiert — eine
wiederhergestellte Erfassung spiegelt also die **jetzt** geltenden Optionen wider, nicht die zum
Zeitpunkt der Erfassung.

Die im Modus Kombinieren gehaltenen Strichcodes werden nicht gespeichert: Nach einem Neustart
beginnt Kombinieren leer.

---

## Tastenkürzel

| | |
|---|---|
| <kbd>Eingabe</kbd> | Die Eingabezeile analysieren |
| <kbd>⌘O</kbd> | Eine Textdatei öffnen |
| <kbd>⌘S</kbd> | Den Bericht als HTML speichern |
| <kbd>⌘P</kbd> | Den Bericht als PDF speichern (Druckdialog des Systems) |
| <kbd>⌘G</kbd> | Das FNC1-Trennzeichen-Token einfügen |
| <kbd>⌘K</kbd> | Den Scanner verbinden / trennen (nur Windows) |
| <kbd>⌘D</kbd> | Das dunkle Design umschalten |
| <kbd>⌘Q</kbd> | Beenden |

Im Editor: <kbd>Tab</kbd> / <kbd>Umschalt</kbd>+<kbd>Tab</kbd> wechseln zwischen bearbeitbaren
Zellen, <kbd>Eingabe</kbd> übernimmt, <kbd>Esc</kbd> bricht ab.

Im Modus Kombinieren: <kbd>Eingabe</kbd> im Feld *Strichcode* fügt den
Strichcode hinzu. Dort schreiben Sie FNC1 als `{GS}`; <kbd>⌘G</kbd> gehört zur Eingabezeile des
Modus Analysieren.

---

## Fehlerbehebung

| Symptom | Ursache und Abhilfe |
|---|---|
| Die Anwendung startet nicht oder meldet `NoClassDefFoundError` | Das Verzeichnis `lib/` muss neben der JAR-Datei liegen. Lassen Sie beides zusammen oder verwenden Sie ein eigenständiges Paket |
| *„Unsupported JavaFX configuration…“* beim Start in der Konsole | Erwartet und harmlos — das Toolkit vermerkt nur, wie es geladen wurde. Die Anwendung läuft normal |
| Ein eingefügtes Trennzeichen scheint zu verschwinden | So gewollt: Es wird in das sichtbare Token `{GS}` umgeschrieben, der Parser erhält weiterhin das echte Zeichen. Siehe [das {GS}-Token](#die-eingabezeile-und-das-gs-token) |
| Es gibt keine Schaltfläche *Scanner verbinden* | Sie sind unter macOS oder Linux, wo es die Scanner-Treiber nicht gibt. Siehe [Scannen](#scannen) |
| Der Scanner verbindet sich nicht | Prüfen Sie, ob der logische Name in der Auswahl zu Ihrem Modell passt, ob der Scanner angeschlossen ist und ob ihn keine andere Anwendung belegt. Die [Einrichtungsanleitung für Zebra-Scanner](GaiaDesktop-Zebra-Scanner-Windows-German.md#11-fehlerbehebung) listet jeden Fehlercode auf. |
| Der PDF-Export meldet, es gebe kein Ziel | Ihr Rechner hat weder Drucker noch PDF-Ziel eingerichtet. Richten Sie eines ein, oder exportieren Sie HTML und drucken Sie aus einem Browser |
| Ein Menüeintrag zeigt so etwas wie `act.hri` | Eine fehlende Beschriftung — bitte melden; das ist ein Fehler im Build, keine Einstellung |

---

## Grenzen

- **Scannen gibt es nur unter Windows**, weil es die Treiber nur dort gibt.
- **PDF läuft über den Druckdialog** statt über einen Speicherdialog.
- **Digital-Link-Ziele akzeptieren eine Domain und ein Pfadpräfix**, aber keine zusätzlichen
  Abfrageparameter und kein Fragment.
- **Ein AI darf nur einmal vorkommen** in einer Elementkette — das ist die GS1-Regel, keine
  Grenze der Anwendung.
- **Kombinieren nimmt nur GS1-128-Strichcodes an, höchstens drei**, und behält sie nicht
  über einen Neustart hinaus.
