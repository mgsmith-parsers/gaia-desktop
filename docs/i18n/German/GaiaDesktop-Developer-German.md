# GAIA Desktop — Entwicklerhandbuch

Wie das Modul gebaut, aufgebaut und getestet wird. Was die Anwendung von außen betrachtet tut, steht im
[Benutzerhandbuch](GaiaDesktop-User-German.md). Originaldokument:
[GaiaDesktop-Developer.md](../../GaiaDesktop-Developer.md).

## Inhaltsverzeichnis

1. [Das Modul im Überblick](#das-modul-im-überblick)
2. [Bauen und ausführen](#bauen-und-ausführen)
   - [Paketierung](#paketierung)
3. [Architektur](#architektur)
   - [Der Erfassungstrichter](#der-erfassungstrichter)
   - [Threads](#threads)
   - [Neu aufbauen oder neu beschriften](#neu-aufbauen-oder-neu-beschriften)
4. [Das {GS}-Token](#das-gs-token)
5. [Scanner-Anbindung](#scanner-anbindung)
   - [Die Plattformsperre](#die-plattformsperre)
   - [Symbologien](#symbologien)
6. [Lokalisierung](#lokalisierung)
   - [Das Argument `--language`](#das-argument---language)
   - [Eine Sprache hinzufügen](#eine-sprache-hinzufügen)
   - [Was bewusst nicht übersetzt wird](#was-bewusst-nicht-übersetzt-wird)
7. [Formate des gespeicherten Zustands](#formate-des-gespeicherten-zustands)
8. [Tests](#tests)
9. [Fehlerbehebung beim Build](#fehlerbehebung-beim-build)
10. [Bekannte Einschränkungen und offene Arbeiten](#bekannte-einschränkungen-und-offene-arbeiten)

---

## Das Modul im Überblick

```
tools.pantheum:gaia-desktop:1.0.0   Java 21 · JavaFX 21
        └── depends on tools.pantheum:gaia:1.1.0
```

Ein Client der Kernbibliothek und nichts weiter. Es enthält **kein GS1-Wissen**: Jede AI-Definition,
Validierungsregel, Fehlermeldung und lokalisierte Beschreibung stammt aus
[`GaiaParser`](../../../../gaia/docs/GaiaParser.md) und
[`GaiaBuilder`](../../../../gaia/docs/GaiaBuilder.md). Insbesondere wird die AI-Liste nie in dieses Modul
kopiert — die Auswahl im Builder liest die Registry des Kerns selbst und kann daher keine AI anbieten, die
der Builder ablehnen würde.

**Einstiegspunkte:** `tools.pantheum.gaia.desktop.GaiaDesktopApp` (die JavaFX-`Application`) und
`tools.pantheum.gaia.desktop.Launcher` (die `Main-Class` des paketierten JARs).

Das Modul ist ein Blatt: Nichts hängt von ihm ab, und es hängt von keinem anderen nachgelagerten Modul ab.
Die beiden Klassen, mit denen es begann, wurden aus `gaia-demo` **kopiert**, statt sie als Abhängigkeit
einzubinden — `Symbologies` wird noch immer von Hand mit seinem Zwilling dort abgeglichen, und beide
tragen einen Hinweis darauf.

---

## Bauen und ausführen

- **Java 21** zum Ausführen und ein JDK 21 oder neuer zum Bauen — das Modul kompiliert mit `release=21`.
  JavaFX 21 ist die passende LTS-Linie. Das Kern-JAR ist weiterhin Java-11-Bytecode und läuft unverändert
  auf 21.
- **Maven** im `PATH`. In diesem Repository gibt es keinen Wrapper.
- **Zuerst den Kern installieren.** Dieses Modul löst `gaia:1.1.0` aus `~/.m2` auf; ein veraltetes oder
  fehlendes Kern-JAR ist die übliche Ursache für `package tools.pantheum.gaia does not exist`.

```bash
(cd gaia && mvn -q install -DskipTests)      # publish the core
(cd gaia-desktop && mvn javafx:run)          # run from source
(cd gaia-desktop && mvn clean package)       # target/dist/gaia-desktop-<version>-<platform>/ — jar (~290 KB), lib/, run.sh (run.bat on win)
(cd gaia-desktop && mvn clean -Pfat package) # one self-contained jar, ~50 MB
(cd gaia-desktop && mvn package -Dfx.platform=win)   # another platform; see Packaging
java -jar gaia-desktop/target/dist/gaia-desktop-1.0.0-mac-aarch64/gaia-desktop.jar
java -jar gaia-desktop/target/dist/gaia-desktop-1.0.0-mac-aarch64/gaia-desktop.jar --language=FR
```

`--language` ist das einzige Kommandozeilenargument und neben `-Dgaia.desktop.home=…`, das das ganze Profil
an einen anderen Ort verlegt, die einzige Möglichkeit, den Start zu beeinflussen.

`mvn javafx:run` ist der Entwicklungsweg: Das Plugin legt JavaFX auf den **Module Path**, sodass das Toolkit
als benanntes Modul startet und `GaiaDesktopApp` direkt gestartet werden kann. Ein paketiertes JAR kann das
nicht — alles liegt auf dem Class Path —, weshalb seine `Main-Class` der Zwischenschritt `Launcher` ist.
Eine `Application`-Unterklasse als `Main-Class` scheitert in diesem Aufbau mit *"JavaFX runtime components
are missing"*.

Verwenden Sie immer `clean` — einmal, bevor Sie eine Reihe von Plattformen bauen, da es `target/dist/` von
allen leert. Warum, steht unter [Fehlerbehebung beim Build](#fehlerbehebung-beim-build).

### Paketierung

Alles Auslieferbare wird unter `target/dist/gaia-desktop-<version>-<platform>/` zusammengestellt und sonst
nichts, sodass das Verzeichnis als Ganzes gezippt oder kopiert werden kann, ohne Artefakte aus `target/`
neben `classes/`, `maven-status/` und dem übrigen Arbeitsmaterial des Builds heraussuchen zu müssen.
`${dist.dir}` benennt es einmal; jedes Plugin, das ein Artefakt schreibt, zeigt darauf.

#### Ein Build pro Plattform

Die JavaFX-JARs enthalten nativen Code, daher ist jede Distribution für eine einzige Plattform, benannt
durch die Eigenschaft `fx.platform` als OpenJFX-Classifier: `mac-aarch64`, `mac`, `win`, `linux` oder
`linux-aarch64`. Fünf betriebssystemaktivierte `fx-*`-Profile setzen sie auf die Plattform der bauenden
Maschine, was ein schlichtes `mvn package` oder `mvn javafx:run` braucht; `-Dfx.platform=…` hat Vorrang vor
ihnen, um jede andere zu bauen, von jeder Maschine aus — die JavaFX-JARs sind nur Downloads. `${dist.dir}`
ist `target/dist/${fx.platform}`, sodass der Build einer Plattform nie den einer anderen überschreibt.

```bash
(cd gaia-desktop && mvn -q clean package)    # this machine's platform, with the tests
for p in win linux mac mac-aarch64; do (cd gaia-desktop && mvn -q package -DskipTests -Dfx.platform=$p); done
```

Zwei Details im POM machen das möglich, und beide sehen entbehrlich aus:

- **Jedes JavaFX-Modul ist eine direkte Abhängigkeit** — base, graphics, controls, media und web —, jedes
  mit `<classifier>${fx.platform}</classifier>` und einem Ausschluss `org.openjfx:*`. Die POMs von OpenJFX
  wählen ihre transitiven JARs nach dem Betriebssystem der *bauenden Maschine*; lässt man graphics oder base
  transitiv kommen, landen die Mac-Natives in einem Windows-Build neben den Windows-Natives.
- **Die Eigenschaft heißt `fx.platform`, nicht `javafx.platform`.** Von außen gesetzt, wird die eigene
  Eigenschaft von OpenJFX durch dessen Eltern-POM durch sich selbst neu definiert; Maven verwirft dieses POM
  als ungültig und lässt die Plattform-JARs stillschweigend weg, was einen Build ganz ohne JavaFX ergibt.

Beide Varianten schreiben denselben Pfad, `target/dist/gaia-desktop-<version>-<platform>/gaia-desktop.jar`,
und pro Build läuft genau eine davon. `${jar.phase}`, `assembly.skipAssembly` und `mdep.skip` verhindern,
dass beide ihn beanspruchen.

| | Standard | `-Pfat` |
|---|---|---|
| `gaia-desktop.jar` | ~290 KB — nur die Klassen und Ressourcen dieses Moduls | ~50 MB — Abhängigkeiten darin entpackt |
| `dist/gaia-desktop-<version>-<platform>/lib/` | 17 JARs, einzeln im `Class-Path` des Manifests genannt | — |
| Ausliefern | das JAR **und** `lib/` daneben | eine Datei |
| Außerdem in `dist/gaia-desktop-<version>-<platform>/` | `run.bat` (win) oder `run.sh` (andere) | dasselbe |
| Und | `readme.html` und `docs/` (jede Plattform) | dasselbe |

Jede Distribution enthält außerdem die Handbücher: `docs/` mit erhaltener Struktur kopiert, damit die Links
zwischen Handbüchern und Übersetzungen weiterhin funktionieren, ohne die Übersetzungswerkzeuge
(`check-i18n.py`, `i18n-manifest.json`, `i18n-plan.md`); und `readme.html` im Wurzelverzeichnis, ein Index
mit Links zu jedem davon, aus `src/main/dist/readme.html` mit `@VERSION@` auf die POM-Version gefiltert.
Beides schreibt der antrun-Schritt `pick-launch-script`, der `docs/` zuerst löscht, damit ein entferntes
Handbuch nicht liegen bleibt. Die Links der Readme sind von Hand geschrieben — ein umbenanntes Handbuch oder
eine unter `docs/` hinzugefügte Übersetzung braucht dort auch ihre Zeile.

Der `Class-Path` des Manifests und `lib/` sind zwei Hälften derselben Sache: Das JAR nennt
`lib/<artifact>-<version>.jar` für jede Laufzeitabhängigkeit, und `copy-dependencies` füllt `lib/` mit genau
diesen Namen. Das ist es, was `java -jar` funktionieren lässt.

#### Die macOS-App und das Distributions-Zip

Eine antrun-Ausführung, `mac-app-and-zip`, beendet die Phase `package`. Das antrun-Plugin wird deshalb
**nach** den jar- und assembly-Plugins deklariert — Maven führt die Ausführungen einer Phase in der
Reihenfolge der Plugin-Deklaration aus —, sodass das JAR in beiden Paketierungen existiert, wenn sie läuft.
(Ihre andere Ausführung, `pick-launch-script`, liegt in `prepare-package` und ist nicht betroffen.)

Für `mac` und `mac-aarch64` baut sie zuerst `GAIA Desktop.app` im Distributionsverzeichnis: `Info.plist`
(mit `@VERSION@` auf die POM-Version gefiltert), den Starter `Contents/MacOS/gaia-desktop` (ausführbar
gemacht) und `gaia-desktop.icns`, alles aus `src/main/macos/`, und **verschiebt** dann `gaia-desktop.jar`
und `lib/` nach `Contents/Java`, damit das Bundle eigenständig ist. `run.sh` sucht dort, wenn kein JAR neben
ihm liegt. Der Starter findet Java nach Version (`java_home -v 21+`, dann das openjdk von Homebrew — Starts
aus dem Finder sehen weder `JAVA_HOME` noch `PATH`), zeigt über `osascript` einen Dialog, wenn keines da ist,
wechselt mit `cd` nach `~/.gaia-desktop`, damit das relative `logs/` von logback beschreibbar ist, und
übergibt `-Xdock:name`/`-Xdock:icon`. macOS ordnet das laufende `java` dem Bundle zu (seine Bundle-ID und
sein Dock-Symbol sind die der App); die Menüleiste zeigt GAIA Desktop.

Danach zippt sie für jede Plattform das Verzeichnis nach `target/dist/gaia-desktop-<version>-<platform>.zip`
unter einem gleichnamigen Ordner, mit Ants `<zip>`: `run.sh` und `Contents/MacOS/**` als eigenes Dateiset
mit `filemode="755"`, sonst würden sie nicht ausführbar entpackt. Das Zip wird **nicht angehängt**: Es ist
für GitHub-Releases gedacht, und die vier Plattformen stammen aus vier Builds, die Maven Central — ein
unveränderlicher Upload pro Version — nie als ein Release annehmen könnte.

`project.build.outputTimestamp` wird auf die Build-Zeit gesetzt. Sonst datieren die Archivierer in
maven-jar-plugin 3.5 und maven-assembly-plugin 3.8 jeden Eintrag auf den 1. Februar 1980.

Das App-Symbol wird von `tools/icon/GaiaIcon.java` gezeichnet (nicht Teil des Builds; sein Kopf nennt die
Schritte für `sips`/`iconutil`). Seine Ausgaben sind eingecheckt: `src/main/macos/gaia-desktop.icns` für das
Bundle und `src/main/resources/icons/gaia-desktop-{16..256}.png`, die `GaiaDesktopApp` als Fenstersymbol
setzt (Windows und Linux; macOS ignoriert Fenstersymbole).

#### Die Startskripte

`run.sh` und `run.bat` stammen aus `src/main/scripts/` und werden ungefiltert nach
`dist/gaia-desktop-<version>-<platform>/` kopiert — `run.sh` steckt voller `${...}`-Shell-Expansionen, die
die Maven-Filterung frei umschreiben dürfte. Ein antrun-Schritt behält dann nur das, das die Plattform
ausführt — `run.bat` für `win`, `run.sh` für alle anderen — und setzt das Ausführungsbit auf `run.sh`, da
`copy-resources` es nicht übernimmt. Er entscheidet anhand des aufgelösten `${fx.platform}` und nicht über
Profile, weil die Profilaktivierung keinen Wert sieht, den ein anderes Profil gesetzt hat: Ein unter Windows
erstellter Linux-Build bekäme sonst das Skript des Hosts.

Sie **verlassen sich nicht auf das Manifest**. Jedes baut den Class Path selbst auf, `gaia-desktop.jar`
gefolgt von jedem `.jar` in `lib/`, einzeln genannt statt einem `lib/*`-Platzhalter überlassen, damit ein
Pfad mit Leerzeichen ein einziger Eintrag bleibt und die Zählung des Hinzugefügten stimmt. Der Start mit
`-cp` ist der Grund, warum sie `tools.pantheum.gaia.desktop.Launcher` ausdrücklich nennen: `-cp` liest die
`Main-Class` des Manifests nicht. Seinem `Class-Path` folgt es **dagegen** sehr wohl — ein JAR auf `-cp`
bringt die JARs mit, die sein Manifest nennt, direkt danach durchsucht —, sodass jedes JAR in `lib/` geladen
wird, ob ein Skript es aufführt oder nicht, und das Weglassen eines JARs aus dem Class Path des Skripts es
nicht aus der JVM fernhält. Die Skripte führen sie trotzdem auf, damit ein JAR mit veraltetem, von Hand
bearbeitetem oder unvollständigem Manifest trotzdem startet. Dasselbe
Skript dient beiden Paketierungen: Das `-Pfat`-JAR hat kein `lib/` hinzuzufügen und braucht keines, weshalb
ein leeres `lib/` auch nur ein Hinweis und kein Fehler ist.

#### Die Zebra-Nativbibliotheken

Der Build liefert nichts von Zebra aus — keine DLLs, keine Service-JARs. Unter Windows kommen sie aus dem auf
dem Rechner installierten Scanner SDK von Zebra: `run.bat` fügt dessen Service-JARs `Jpos*.jar` und `bin\`
dem Class Path und `bin\` dem `PATH` hinzu, wo `System.loadLibrary` `CSJPOSScanner.dll` /
`CSJPOSScanner64.dll` findet (`java.library.path` ist standardmäßig der `PATH`). Die JavaPOS-API und ihr
Konfigurationslader kommen immer aus `lib\javapos-*.jar`, mit oder ohne SDK.

**`run.bat` fügt nie das eigene JavaPOS-JAR des SDK hinzu**, `External\JavaPOS-1.14.0-Dist\javaPOS114.jar`.
Früher tat es das und ließ `lib\javapos-*.jar` weg, um zwei Kopien der API zu vermeiden — aber der
`Class-Path` des Manifests lud sie trotzdem (siehe oben), sodass die JVM beide bekam: die Klassen des
4.x-Laders aus `lib\`, daneben `jpos/res/jpos.properties` und `jcl.xsd` aus `javaPOS114.jar`. Die
Properties-Datei lässt [`JposConfig`](#scanner-anbindung) zurücktreten, und dieses `jcl.xsd` akzeptiert
JavaPOS-Versionen nur bis 1.10, sodass jeder Eintrag in `jpos.xml` (1.12–1.14) die Validierung nicht
bestand, die Registry leer blieb und jede Verbindung mit Fehler 109 scheiterte, bevor der Scanner überhaupt
erreicht wurde. Dafür genügt es, das JAR überhaupt auf dem Class Path zu haben, selbst hinter den JARs aus
`lib\`. Die Service-JARs von Zebra laufen unverändert auf der 1.14-API aus `lib\` — Öffnen, Belegen und
Scannen sind so an einem echten Scanner geprüft. Von Hand neben `run.bat` abgelegte DLLs werden weiterhin
berücksichtigt: Das Skript setzt dann `-Djava.library.path=<dist>;%PATH%` und hängt den `PATH` an, weil die
Eigenschaft den Standard-Suchpfad vollständig ersetzt.

`mvn javafx:run` macht nichts davon; das Belegen eines Scanners aus einem Entwicklungslauf unter Windows
erfordert daher, die JARs des SDK auf anderem Weg auf den Class Path und sein `bin\` in den `PATH` zu
bringen; starten Sie dafür aus `dist/`.

Über den Class Path hinaus decken sie ab, was `java -jar` nicht tut: ihr eigenes Verzeichnis auflösen, damit
sie aus jedem Arbeitsverzeichnis funktionieren, `JAVA_HOME` gegenüber `PATH` bevorzugen und eine JVM vor 11
mit einem Satz ablehnen, der die Abhilfe nennt, statt mit `UnsupportedClassVersionError`.
`GAIA_DESKTOP_OPTS` reicht JVM-Optionen durch. `run.bat` verwendet `java.exe`, nicht `javaw.exe`, damit die
Konsole offen bleibt — Windows ist die einzige Plattform, auf der der Scanner läuft, und sein Status und die
JavaPOS-Fehler erscheinen dort.

`run.bat` wählt außerdem Java, JavaFX und Zebra-Dienste nach dem, was es vorfindet — ein mitgeliefertes
`jdk*\`, ein `javafx-sdk*\lib\` auf dem Module Path, das Scanner SDK von Zebra unter `ZEBRA_HOME` —, lässt die
JavaFX-Kopien aus `lib\` vom Class Path weg, wenn es die eines SDK verwendet, und übergibt den SAX-Parser des
JDK, die Windows-Proxys und den Windows-Zertifikatspeicher sowie `--enable-native-access` ab Java 17. Der
README-Abschnitt [What `run.bat` adds on Windows](../../../README.md#what-runbat-adds-on-windows) enthält die
Tabelle. Behalten Sie die CRLF-Zeilenenden der Datei bei: `cmd.exe` kann in einer reinen LF-Batchdatei ein
`call :label` übersehen. Auch `run.sh` übergibt `--enable-native-access=ALL-UNNAMED` — JavaFX lädt seine
eigenen Natives aus `lib/`, und mit JavaFX auf dem Class Path ist `ALL-UNNAMED` das Modul, das es abdeckt —
und lässt es, wie `run.bat`, nur weg, wenn sich die Java-Version nicht lesen lässt.

Beide Varianten enthalten die JavaFX-Natives **einer einzigen Plattform**, `${fx.platform}`, daher ist keine
über Betriebssystem oder Architektur hinweg portabel. Bauen Sie einmal pro Ziel — siehe
[Ein Build pro Plattform](#ein-build-pro-plattform).

> **Eclipse.** Die Ausführung `copy-dependencies` trägt eine Anweisung `<?m2e ignore?>`. m2e löst ein offenes
> Workspace-Projekt zu seinem Ordner `target/classes` statt zu einem JAR auf, und `copy-dependencies`
> weigert sich, ein nicht paketiertes Artefakt zu kopieren (MDEP-187), was den automatischen Build jedes Mal
> scheitern ließe, wenn das Projekt `gaia` neben diesem geöffnet ist. Die Folge: Ein reiner IDE-Build füllt
> `target/dist/gaia-desktop-<version>-<platform>/lib/` nicht; führen Sie `mvn package` aus, bevor Sie das
> JAR selbst verwenden.

---

## Architektur

| Paket | Enthält |
|---|---|
| `desktop` | `GaiaDesktopApp`, `Launcher`, `Theme`, `GaiaDesktopVersion` |
| `desktop.ui` | `MainView` (das Fenster), `BuilderPane`, `OptionsPane`, `HistoryCell`, `EditCell` |
| `desktop.build` | `ElementBuilder` (JavaFX-freie Erstellungslogik), `AiPair` (eine Editorzeile) |
| `desktop.parse` | `ParseOptions` (beobachtbare `ParseConfig`), `SystemDefaults`, `Languages`, `GsToken` |
| `desktop.scanner` | `ScannerService` (JavaPOS), `ScannerListener`, `Symbologies` |
| `desktop.html` | `GaiaDesktopReport` — der Berichts-Renderer, JavaFX-frei und ohne Toolkit unit-getestet |
| `desktop.i18n` | `UiStrings` — der Beschriftungskatalog |
| `desktop.store` | `AppPaths`, `OptionsStore`, `CaptureStore` |

### Der Erfassungstrichter

Jede Eingabe — getippt, eingefügt, aus einer Datei geöffnet, gescannt oder erstellt — erreicht den Parser
über eine einzige Methode, `MainView.capture(String, ScanSource, String)`.

Das ist die eine Entwurfsentscheidung, um die die App herum gebaut ist. Eine Erstellung braucht keinen
eigenen Bericht, keinen eigenen Verlauf und keinen eigenen Exportweg, denn nach dem Trichter ist ein
erstellter String von einem gescannten nicht zu unterscheiden. Alles Neue, das eine Eingabe erzeugt, sollte
durch ihn hindurchgehen und nicht an ihm vorbei.

### Threads

| Thread | Führt aus |
|---|---|
| JavaFX-Application-Thread | Das gesamte Parsen, Rendern und die Oberfläche. Sind die AI-Tabellen warm, dauert das Parsen weniger als eine Millisekunde und braucht daher keinen Worker |
| `gaia-scanner` | Jeden Aufruf an den Scanner — öffnen, belegen, aktivieren, deaktivieren, freigeben, schließen. Ein langlebiger Thread im Besitz von `ScannerService`; siehe [Scanner-Anbindung](#scanner-anbindung) |
| JavaPOS-Event-Thread | Die Callbacks von `ScannerService`. `MainView.FxScannerListener` reicht jeden mit `Platform.runLater` hinüber |
| Hintergrund | Das Warten auf `connect()` / `disconnect()` des Scanners (das Belegen kann bis zu 5 s dauern), das Wiederherstellen des gespeicherten Verlaufs beim Start — das zugleich den Parser aufwärmt — und die Update-Prüfung (`gaia-update-check`, eine GitHub-Anfrage, Ergebnis mit `Platform.runLater` zurückgegeben) |

**Ändern Sie nie die Auswahl eines Steuerelements oder zerstören Sie das Steuerelement aus seinem eigenen
Auswahlereignis heraus.** Drei verschiedene Abstürze dieser App kamen genau daher: das Zurücksetzen der
AI-Auswahl innerhalb des eigenen `clearAndSelect` des Popups (eine `IndexOutOfBoundsException` aus
`ListViewBehavior`) und das Neuaufbauen des Bereichs, dem die Sprachauswahl gehört, aus dem eigenen Handler
dieser Auswahl. Beides wird jetzt mit `Platform.runLater` verschoben, und die Gründe sind an beiden
Aufrufstellen festgehalten.

### Neu aufbauen oder neu beschriften

Ein Sprachwechsel **baut** die Oberflächenteile aus dem Katalog **neu auf** — Menüs, Werkzeugleiste,
Optionsbereich —, was sicher ist, weil keines davon Zustand besitzt: `OptionsPane` ist eine
Zweiwege-Ansicht von `ParseOptions`, die Werte leben also im Modell, und ein frischer Bereich übernimmt sie
sofort wieder.

Steuerelemente, die **Benutzerdaten** halten, sind Felder und werden stattdessen an Ort und Stelle neu
beschriftet: `BuilderPane.retext(UiStrings)` behält die AI-Zeilen, die Digital-Link-Felder und die Auswahl.
Ein Neuaufbau würde laufende Arbeit verwerfen — was er einmal tat.

---

## Das {GS}-Token

Ein JavaFX-`TextField` verwirft stillschweigend jedes Zeichen unterhalb von `0x20`
(`TextInputControl.filterInput`). Es „stellt es nicht seltsam dar" — es entfernt es. Tippen, Einfügen,
`setText` und `insertText` sind alle betroffen, sodass `setText("01…" + GS + "10LOT")` `"01…10LOT"` ergibt:
eine Elementkette, deren Bedeutung sich unbemerkt geändert hat, weil die AI variabler Länge nun verschluckt,
was ihr folgte.

`desktop.parse.GsToken` ist die ganze Antwort. Das Trennzeichen läuft als literaler Text `{GS}` durch die
Oberfläche und wird auf dem Weg zum Parser wieder zu `0x1D`. Das Token ist nicht hier erfunden — genau so
stellt `ASCIIRevealerUtils.printable` aus dem Kern `0x1D` dar, sodass ein aus einem Bericht kopierter Wert
unverändert in die Eingabezeile zurück eingefügt werden kann.

Ein `TextFormatter`-Filter auf dem Eingabefeld fängt jeden Weg ab, auf dem Text ankommen kann — Tippen, ⌘V,
das Kontextmenü, Drag & Drop, `setText` —, weshalb die Umschreibung dort lebt statt in jedem Handler.

Der Scannerweg geht nie über `GsToken`: Scans erreichen den Parser als Bytes.

---

## Scanner-Anbindung

`ScannerService` besitzt das JavaPOS-Steuerelement `Scanner`: öffnen → belegen → aktivieren, wobei das
Datenereignis nach jedem Scan neu scharf gestellt wird. Die logischen Gerätenamen stammen aus
`src/main/resources/jpos.xml`.

**Jeder Geräteaufruf läuft auf einem einzigen Thread, `gaia-scanner`, der so lange lebt wie der Dienst** —
nie auf dem FX-Thread und nie auf einem Thread, der nach dem Aufruf endet. Der Dienst von Zebra erreicht den
Scanner per JNI über die CoreScanner-COM-API, und COM bindet, was es einrichtet, an das Apartment des
aufrufenden Threads: Öffnen Sie auf einem Thread, der danach endet, wird der Scanner belegt, bleibt aber
stumm; schließen Sie auf einem anderen Thread, wird das falsche Apartment deinitialisiert. Schließen auf dem
FX-Thread ist der schlimmste Fall, weil JavaFX dort `OleInitialize` aufruft und das Drop-Ziel jedes Fensters
dagegen registriert — ein `CoUninitialize` darauf legt Drag & Drop in der ganzen App lahm. Das ist JavaFX
unter Windows, nicht eine bestimmte JavaFX-Version. `connect()` und `disconnect()` übergeben an diesen
Thread und warten, daher ruft `MainView` beide aus einem kurzlebigen Hintergrund-Thread auf; das Muster
stammt aus `JposScannerSession` des Viewers, wo der Drag-&-Drop-Fehler zuerst gefunden wurde.

Zwei Regeln verhindern, dass ein sich fehlverhaltendes Gerät die Verbindung blockiert. **Der Abbau hört nie
vorzeitig auf:** `closeQuietly` deaktiviert, gibt frei und schließt in getrennten Schritten, gibt nur frei,
wenn die Belegung gehalten wird, und versucht immer `close()` — nach einem gescheiterten Belegen wirft
`release()` „not claimed", und als die Schritte sich ein `try` teilten, übersprang das das Schließen und
ließ das Gerät im Dienst von Zebra offen. **Das Datenereignis wird in einem `finally` neu scharf gestellt:**
JavaPOS deaktiviert es nach jedem Ereignis, also muss ein Lesen oder ein Handler, der eine Ausnahme wirft,
trotzdem neu scharf stellen, sonst bleibt der Scanner bis zu einer Neuverbindung stumm.
`ScannerLifecycleTest` prüft beides gegen einen geskripteten `jpos.Scanner`.

**`JposConfig` richtet JavaPOS vor dem ersten Öffnen auf das mitgelieferte `jpos.xml` aus.** Mit den
JavaPOS-JARs aus Maven (`javapos-config-loader` 4.x) und ohne `jpos/res/jpos.properties` auf dem Class Path
scheitert sonst jedes Öffnen mit *"Service does not exist in loaded JCL registry"* (Fehler 109), aus drei
verschiedenen Gründen: Der Service-Manager fällt auf `SimpleRegPopulator` zurück, der ein serialisiertes
`jpos.cfg` liest und nie `jpos.xml`; der XML-Lader sucht `jpos.xml` nur im Arbeitsverzeichnis, nicht im
JAR; und Xerces 2.12.2 in `lib/` weist die JAXP-Eigenschaft `accessExternalDTD` zurück, die der Lader setzt.
`JposConfig` setzt `jpos.config.regPopulatorClass` (`JavaxRegPopulator`), `jpos.config.populatorFileURL`
(das eigene `jar:file:…!/jpos.xml` des JARs) und `javax.xml.parsers.SAXParserFactory` (die des JDK) als
Systemeigenschaften — nie eine bereits gesetzte, sodass `-Djpos.config.populatorFileURL=file:…` weiterhin
ein anderes `jpos.xml` auswählt — und setzt nichts, wenn ein `jpos.properties` auf dem Class Path liegt oder
der 4.x-Lader fehlt. Das `javaPOS114.jar` des Zebra SDK ist ein solcher Fall und zerstört die Registry
geradewegs, weshalb `run.bat` es vom Class Path fernhält — siehe
[Die Zebra-Nativbibliotheken](#die-zebra-nativbibliotheken). `JposConfigTest` prüft durchgängig, dass die
Registry dann die Scanner der Datei enthält.

**Die Geräteauswahl wird aus dieser Registry erzeugt**, nicht als Liste im Code gepflegt:
`ScannerService.logicalNames()` liest die geladenen Einträge auf dem Thread `gaia-scanner` und behält jene,
deren Gerätekategorie `Scanner` ist — die beiden Waagen-Einträge in `jpos.xml` bleiben draußen —, in der
Reihenfolge `pickerOrder`: `ZebraAllScanners` zuerst, wenn die Datei ihn definiert, der Rest alphabetisch
(die Registry behält die Dateireihenfolge nicht bei). Eine Datei ohne ihn bekommt ihn nicht; ist nichts
geladen, wird nur der Standard angeboten, sodass die Auswahl nie leer ist. `MainView` zeigt den Standard
sofort und füllt die Liste aus einem Hintergrund-Thread auf, wobei die gespeicherte Wahl erhalten bleibt. So
erscheint ein zu `jpos.xml` hinzugefügter Scanner — oder eine ganze mit `-Djpos.config.populatorFileURL`
benannte Datei — ohne Codeänderung in der Auswahl, und `JposConfigTest` prüft die Auswahl gegen ein
unabhängiges Einlesen der mitgelieferten Datei. Die Dienste, die diese Einträge nennen, sind Klassen von
Zebra
(`com.zebra.jpos.service.scanner.*Factory`), die weiterhin auf dem Class Path liegen müssen — aus dem Scanner
SDK über `run.bat` oder als in `lib\` abgelegte Service-JARs von Zebra.

Scandaten werden als **ISO-8859-1** dekodiert, sodass jedes Byte 1:1 abgebildet wird und sowohl das AIM-Code-
ID-Präfix als auch jedes `0x1D` erhalten bleiben; Berichte werden als UTF-8 geschrieben.

Nichts im Dienst setzt Text für einen Benutzer zusammen. Ereignisse gehen als Konstanten von
`ScannerListener.Event` plus den veränderlichen Werten hinaus, und `MainView` liefert den Wortlaut aus dem
Katalog — sonst wäre die Statuszeile in einer 35-fach übersetzten Anwendung englisch. Logzeilen sind die
Ausnahme und bleiben englisch.

### Die Plattformsperre

`ScannerService.isSupportedPlatform()` ist ein einziger Test auf `os.name`, und `MainView` fragt ihn, bevor
es irgendein Scanner-Steuerelement baut: Unter macOS und Linux gibt es keinen Schalter, keine Geräteauswahl,
kein Menü Scanner (und damit kein <kbd>⌘K</kbd>) und keine Statusanzeige. Sie sind nicht verborgen — sie
werden nie erzeugt.

Zebra liefert seine JavaPOS-Dienste nur für Windows, und anderswo scheitert nichts früh: Die JARs werden
aufgelöst und das Steuerelement wird erzeugt, und erst `open()` scheitert schließlich an der fehlenden
nativen Schicht, mehrere Sekunden und einen Dialog später.

Die Regel betrifft die *Dienste*, nicht JavaPOS. Ein Hersteller, der einen plattformübergreifenden Dienst
liefert, würde hier funktionieren, und die Regel zu erweitern ist allein eine Änderung an
`supportsJavaPos(String)` — das paketprivat ist und gegen `os.name`-Strings getestet wird, sodass beide
Antworten auf jeder bauenden Maschine abgedeckt sind.

### Symbologien

`Symbologies` ordnet alle **78** JavaPOS-Scandatentypen `SCAN_SDT_*` Anzeigenamen zu und, wo die beiden
Vokabulare übereinstimmen, dem `DataCarrierType` des Kerns. JavaPOS ist das feinere der beiden — es trennt
UPC-A von EAN-13 und schlichtes Data Matrix von GS1 DataMatrix, wo AIM eine ganze Familie unter einem
einzigen Codezeichen zusammenfasst.

Ein Typ außerhalb der Tabelle erhält trotzdem einen brauchbaren Namen, per Reflection über `ScannerConst`
gefunden, weil Herstellerdienste durchaus Werte außerhalb der Spezifikationsliste melden. Ein Test
durchläuft `ScannerConst` und schlägt fehl, wenn ein deklarierter Typ keinen Namen hat — was auch
Konstanten erfasst, die ein künftiges JavaPOS hinzufügt.

Einige Datenträger-Zuordnungen bleiben bewusst `UNKNOWN`, statt geraten zu werden: Straight 2 of 5 (AIM
unterscheidet `]R`/`]S` nach der Anzahl der Start-/Stoppbalken, die JavaPOS nicht meldet), die
Vier-Zustands-Postcodes und TLC-39.

---

## Lokalisierung

Die Beschriftungen liegen in einer Properties-Datei pro Sprache unter
`src/main/resources/i18n/ui_<CODE>.properties`, mit denselben Codes wie die Fehlerkataloge des Kerns
(`EN`, `FR`, `PCM`, …). Alle 35 existieren, und alle 35 tragen einen identischen Schlüsselsatz. Die Dateien
werden als **UTF-8** gelesen, nicht als das ISO-8859-1, das `Properties` annimmt, damit Übersetzungen
lesbar statt als `\uXXXX`-Escapes gespeichert werden.

**Der Rückfall erfolgt pro Schlüssel, nicht pro Datei** — das macht eine teilweise übersetzte Sprache
nützlich, sobald ihr erster Schlüssel eintrifft. Englisch ist der Vertrag: Jeder Schlüssel muss dort
existieren.

| Schicht | Schlüssel | Abdeckung |
|---|---|---|
| Gemeinsames Berichtsvokabular, übernommen aus `GaiaHtmlReport` in `gaia-demo` | 31 | alle 35 Sprachen |
| Desktop-Oberfläche — Menüs, Schaltflächen, Builder, Optionen, Statuszeile, Berichtslayout, Scannerstatus, Dialogtexte | 114 | alle 35 Sprachen |

Sechs der gemeinsamen Begriffe (`generated`, `status`, `dlRole`, `interpretations`, `message`, `level`)
verwendet der Bericht dieser App nicht. Sie bleiben erhalten, damit der gemeinsame Block eine getreue Kopie
des Demo-Vokabulars bleibt; der Dateikopf sagt das.

### Das Argument `--language`

`GaiaDesktopApp.languageOverride()` liest `--language=…` über JavaFX' eigenes
`getParameters().getNamed()`, löst es mit `Languages.fromArgument` auf — Katalogcode, Enum-Name oder
BCP-47-Tag, ohne Beachtung der Groß-/Kleinschreibung — und übergibt es an `MainView`.

**Die Übersteuerung gilt für den Lauf, nicht für die Datei.** `MainView` behält die geladene Sprache in
`savedLanguage` und schreibt *diese* zurück, bis `languageChosenInSession` meldet, dass der Benutzer selbst
eine Sprache gewählt hat; dann bleibt seine Wahl wie üblich erhalten. Diese Trennung ist das ganze Design:
Ein Kiosk-Profil oder eine Demo-Verknüpfung kann auf Spanisch öffnen, ohne zu überschreiben, womit der
Besitzer des Rechners morgen startet, während eine echte Wahl im Optionsbereich bestehen bleibt.

Ein Wert, der keinen Katalog bezeichnet, wird mit den akzeptierten Codes protokolliert und ignoriert. Wegen
eines Tippfehlers in einer Verknüpfung den Start zu verweigern, wäre der schlimmere Fehler, und
`Languages.fromArgument` gibt aus demselben Grund `null` zurück, statt eine Ausnahme zu werfen.

### Eine Sprache hinzufügen

Es ist keine Codeänderung nötig.

1. Bearbeiten Sie `ui_<CODE>.properties` — jede Sprache hat bereits eine Datei.
2. Übersetzen Sie die Werte. Lassen Sie die Platzhalter `{0}` / `{1}` stehen.
3. Bauen Sie. Zwei Tests setzen den Vertrag durch: Der eine schlägt fehl, wenn der Code einen Schlüssel
   anfordert, den Englisch nicht definiert, der andere, wenn der Schlüsselsatz eines Katalogs von Englisch
   abweicht.

Der erste Test durchsucht die **Quellen** nach Schlüsselliteralen, statt per Reflection über Klassen zu
gehen, weil die Schlüssel nur als String-Literale an den Aufrufstellen existieren. Deshalb muss eine neue
Meldung mit dem Schlüssel direkt im Aufruf gebaut werden — `strings.format("st.scannerError", …)` — statt
über eine Variable nachgeschlagen zu werden, sonst kann der Test ihn nicht sehen.

### Was bewusst nicht übersetzt wird

- die knappen Feldnamen in Festbreitenschrift neben Rohwerten — `format`, `position`, `length`, `raw` und
  `part` der Elementkarte, `AI`, `sep`, `pos` und `len` des Bands und die vier Zellen, die einen Fehler
  verorten (`AI`, `position`, `level`, `stage`), deren Werte ohnehin unübersetzte Enum-Namen sind;
- die Namen der Beispieleingaben, die GS1-Konstrukte benennen, und die Symbologienamen, die Eigennamen sind;
- der Abhilfe-Absatz zu `GE-C084` — Normenprosa, bei der eine halbe Übersetzung schlimmer ist als Englisch;
- die Rückfallprüfung für unbekannte AIs in `ElementBuilder`, die über die Oberfläche nicht erreichbar ist:
  Der Editor prüft unbekannte AIs selbst und meldet sie aus dem Katalog;
- Logzeilen.

Eine Lücke bleibt: `ScanSource.label()` liefert `manual` / `scanner` / `file` / `built` auf Englisch an die
Verlaufszeile und den Berichtskopf. Sie zu schließen braucht mehr als einen Schlüssel — `HistoryCell` hat
keine `UiStrings`, und die Zellfabrik der Liste wird nur einmal installiert, sodass die Zellen bei einem
Sprachwechsel ebenfalls aufgefrischt werden müssten.

---

## Formate des gespeicherten Zustands

Zwei Dateien unter `~/.gaia-desktop/`, getrennt gehalten, weil sie sich in verschiedenem Takt ändern — ein
beschädigtes oder von Hand bearbeitetes Erfassungsprotokoll soll den Benutzer nicht seine Einstellungen
kosten. Das Verzeichnis lässt sich mit `-Dgaia.desktop.home=…` überschreiben, so bleiben die Tests aus dem
echten Home-Verzeichnis heraus.

**`options.properties`** — geschrieben, wenn sich eine Option ändert, und beim Beenden.

| Schlüssel | Bedeutung |
|---|---|
| `parse.mode`, `parse.language` | Tiefe der Verarbeitungskette und Sprache der Meldungen |
| `date.endian`, `date.separator`, `date.month`, `date.year` | Datumsdarstellung |
| `check.skipRequires`, `check.skipExcludes` | Ausnahmen von den Strukturprüfungen |
| `ui.theme` | `LIGHT` oder `DARK` |
| `ui.optionsPane` | Bereich geöffnet? |
| `ui.scannerDevice` | Letzter logischer JavaPOS-Name |
| `ui.mode` | `PARSE`, `BUILD` oder `COMBINE` |
| `update.check` | Ob die tägliche Update-Prüfung läuft (Standard `true`) |
| `update.lastCheck` | Wann sie zuletzt lief, ISO-8601; fehlt der Wert oder ist er unlesbar, ist eine Prüfung fällig |
| `update.skipVersion` | Ein Release, für das der Benutzer *Diese Version überspringen* gewählt hat; es wird nicht erneut automatisch angeboten |

Eine fehlende oder beschädigte Datei ist nie fatal: Jeder Wert fällt einzeln zurück, sodass eine fehlerhafte
Zeile nur diese Zeile kostet. Beim ersten Start, ganz ohne Datei, werden die Optionen von `SystemDefaults`
aus den regionalen Einstellungen des Rechners vorbelegt statt aus den eingebauten Standards von GAIA — das
kurze Datumsmuster des Gebietsschemas liefert die Reihenfolge der Bestandteile, das Trennzeichen und ob
Monat und Jahr abgekürzt sind.

Übergeben Sie `Properties.setProperty` nie ein null: Es wirft eine Ausnahme, und `persistOptions()` läuft
aus Ereignis-Handlern der Oberfläche, wo eine Ausnahme auf den FX-Thread entkommt. Das ist ein echter
Absturz, den diese App schon hatte.

**`captures.tsv`** — geschrieben, wenn sich der Verlauf ändert, und beim Beenden. Neueste zuerst, begrenzt
auf **500** Einträge.

```
captured(ISO)              <TAB> source  <TAB> symbology   <TAB> input
2026-08-09T14:22:05.123    SCANNER       Data Matrix       0109506000134352\x1D10LOT-001
```

Gespeichert wird nur, was sich nicht neu berechnen lässt. Das `ParseResult` wird **nicht** serialisiert —
es wird beim Laden durch erneutes Parsen der Eingabe wiederhergestellt, sodass ein wiederhergestellter
Verlauf die *jetzt* gültigen Optionen widerspiegelt. Eingaben enthalten regelmäßig FNC1 und was ein Scanner
sonst ausgibt, daher wird das Eingabefeld maskiert (`\\`, `\t`, `\n`, `\r` und `\xNN` für alles andere
unterhalb von `0x20`), und die Datei wird als ISO-8859-1 gelesen und geschrieben, Byte für Byte.

Dateien, die geschrieben wurden, bevor das Datum aufgezeichnet wurde, enthalten nur eine Uhrzeit; sie laden
weiterhin, auf heute datiert, weil das echte Datum nie festgehalten wurde.

---

## Tests

```bash
(cd gaia-desktop && mvn test)
```

96 Tests, bewusst **frei vom JavaFX-Toolkit**, was sie schnell und ohne Anzeige lauffähig hält. Sie decken
den Berichts-Renderer ab, die Erstellungslogik und AI-Abfragen, die `{GS}`-Umwandlung, die aus dem
Gebietsschema abgeleiteten Standards, beide Speicher (einschließlich eines Hin- und Rückwegs mit `0x1D` und
absichtlich beschädigter Dateien), die Symbologietabelle, die Plattformregel des Scanners, den
Versions-Stub und die Kataloge.

Drei sollte man kennen, weil sie Verträge statt Funktionen schützen:

| Test | Schlägt fehl, wenn |
|---|---|
| `CatalogueCoverageTest` | Der Code einen Schlüssel anfordert, den `ui_EN.properties` nicht definiert, oder eine der 35 Dateien vom englischen Schlüsselsatz abweicht |
| `SymbologiesTest` | Eine von JavaPOS deklarierte `SCAN_SDT_*`-Konstante keinen Anzeigenamen hat — auch eine, die ein JavaPOS-Upgrade hinzufügt |
| `ScannerServiceTest` | Die Plattformregel ihre Antwort für Windows oder für alles andere ändert |

`UiStrings` ist zur Laufzeit bewusst nachsichtig — ein unbekannter Schlüssel wird als der Schlüssel selbst
dargestellt, statt mitten im Layout eine Ausnahme zu werfen —, und genau deshalb muss es den Katalogtest
geben.

**Nicht abgedeckt:** alles, was ein laufendes Toolkit braucht — Fokusverhalten, der Moduswechsel, die
Tabellenbearbeitung, der Sprachwechsel und die beiden unter [Threads](#threads) beschriebenen Abstürze. Sie
wurden durch Bedienen des echten Fensters geprüft, aber eine Regression darin würde den Build nicht
scheitern lassen. TestFX oder Monocle hinzuzufügen würde diese Lücke schließen.

---

## Fehlerbehebung beim Build

| Symptom | Ursache und Abhilfe |
|---|---|
| `package tools.pantheum.gaia does not exist` | Der Kern ist nicht installiert. `(cd gaia && mvn install -DskipTests)` |
| *"JavaFX runtime components are missing"* | Die `Main-Class` des JARs muss `Launcher` sein, nicht `GaiaDesktopApp` |
| *"Unsupported JavaFX configuration: classes were loaded from unnamed module"* beim Start | Erwartet und harmlos — JavaFX vermerkt, dass es auf dem Class Path liegt. Genau dafür gibt es `Launcher` |
| Das JAR startet nicht, `NoClassDefFoundError` | Das Standard-JAR enthält nur die Klassen dieses Moduls; `dist/lib/` muss daneben liegen. Verwenden Sie `-Pfat` für eine einzige eigenständige Datei |
| `Artifact has not been packaged yet … MDEP-187` in Eclipse | Workspace-Auflösung von m2e. Siehe den Hinweis unter [Paketierung](#paketierung) |
| Doppelte `… 2.properties`-Dateien tauchen in einem Build auf | Das Projekt liegt in einem mit iCloud synchronisierten Ordner, und die Synchronisierung hat mitten im Build Konfliktkopien erzeugt. Immer `mvn clean package`; besser `target/` von der Synchronisierung ausnehmen |
| Ein Menüeintrag zeigt einen Schlüssel wie `act.hri` | Dieser Schlüssel fehlt in `ui_EN.properties`. Der Build hätte das erkennen sollen — siehe [Tests](#tests) |

---

## Bekannte Einschränkungen und offene Arbeiten

- **Keine UI-Tests ohne Anzeige.** Siehe [Tests](#tests).
- **Plattformspezifische Artefakte.** Beide Paketierungen enthalten die JavaFX-Natives einer einzigen
  Plattform; bauen Sie eines pro Ziel mit `-Dfx.platform` (jede Maschine kann alle bauen). Keine enthält
  eine Java-Laufzeit — dafür braucht es `jpackage`, das nur für das Betriebssystem baut, auf dem es läuft.
- **Scanner-Eingabe nur unter Windows.** Siehe [Die Plattformsperre](#die-plattformsperre).
- **`ScanSource.label()` ist nicht lokalisiert.** Siehe [Was bewusst nicht übersetzt
  wird](#was-bewusst-nicht-übersetzt-wird).
- **`UiState.mode` ist ein `String`** (`"PARSE"` / `"BUILD"`), während `Theme` daneben ein Enum ist, daher
  taucht das Literal an mehreren Stellen auf. Ein Enum würde sie beseitigen.
- **Das Digital-Link-Ziel besteht nur aus Domain und Pfadpräfix.** `BuilderDigitalLinkConfig` unterstützt
  auch zusätzliche Abfrageparameter und ein Fragment; der Editor bietet sie nicht an.
- **Sich gegenseitig ausschließende AI-Paare (`GE-S006`) werden im Editor nicht markiert**, wie es bei
  unbekannten und doppelten AIs geschieht — die Erstellung scheitert einfach.
- **Der Fehlerkatalog des Kerns verdoppelt Anführungszeichen** (`value ''09506000134350''`), was wörtlich in
  Erstellungsfehlern und im Bericht erscheint. Es sieht nach einer Anführungszeichen-Maskierung von
  `MessageFormat` aus, die nicht aufgehoben wird; die Korrektur gehört in den Kern.
