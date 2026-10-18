# GAIA Desktop — Ontwikkelaarshandleiding

Hoe de module wordt gebouwd, ingedeeld en getest. Wat de toepassing van buitenaf doet, staat in de
[Gebruikershandleiding](GaiaDesktop-User-Dutch.md). Oorspronkelijk document:
[GaiaDesktop-Developer.md](../../GaiaDesktop-Developer.md).

## Inhoudsopgave

1. [De module in het kort](#de-module-in-het-kort)
2. [Bouwen en uitvoeren](#bouwen-en-uitvoeren)
   - [Verpakken](#verpakken)
3. [Architectuur](#architectuur)
   - [De vastlegtrechter](#de-vastlegtrechter)
   - [Threads](#threads)
   - [Herbouwen of herlabelen](#herbouwen-of-herlabelen)
4. [Het {GS}-token](#het-gs-token)
5. [Scannerintegratie](#scannerintegratie)
   - [De platformgrens](#de-platformgrens)
   - [Symbologieën](#symbologieën)
6. [Lokalisatie](#lokalisatie)
   - [Het argument `--language`](#het-argument---language)
   - [Een taal toevoegen](#een-taal-toevoegen)
   - [Wat bewust niet vertaald wordt](#wat-bewust-niet-vertaald-wordt)
7. [Formaten van de opgeslagen status](#formaten-van-de-opgeslagen-status)
8. [Tests](#tests)
9. [Problemen met de build oplossen](#problemen-met-de-build-oplossen)
10. [Bekende beperkingen en open werk](#bekende-beperkingen-en-open-werk)

---

## De module in het kort

```
tools.pantheum:gaia-desktop:1.0.0   Java 21 · JavaFX 21
        └── depends on tools.pantheum:gaia:1.1.0
```

Een client van de kernbibliotheek en verder niets. Er zit **geen GS1-kennis** in: elke AI-definitie,
validatieregel, foutmelding en gelokaliseerde beschrijving komt uit
[`GaiaParser`](../../../../gaia/docs/GaiaParser.md) en
[`GaiaBuilder`](../../../../gaia/docs/GaiaBuilder.md). In het bijzonder wordt de AI-lijst nooit naar deze
module gekopieerd — de kiezer van de samensteller leest het register van de kern zelf, en kan dus geen AI
aanbieden die de samensteller zou weigeren.

**Ingangspunten:** `tools.pantheum.gaia.desktop.GaiaDesktopApp` (de JavaFX-`Application`) en
`tools.pantheum.gaia.desktop.Launcher` (de `Main-Class` van de verpakte jar).

De module is een blad: niets is ervan afhankelijk, en zelf hangt ze van geen enkele andere stroomafwaartse
module af. De twee klassen waarmee ze begon zijn uit `gaia-demo` **gekopieerd** in plaats van als
afhankelijkheid gebruikt — `Symbologies` wordt nog met de hand gelijk gehouden met zijn tweelingbroer daar,
en beide dragen een opmerking die dat zegt.

---

## Bouwen en uitvoeren

- **Java 21** om uit te voeren, en een JDK 21 of nieuwer om te bouwen — de module compileert met
  `release=21`. JavaFX 21 is de bijpassende LTS-lijn. De kern-jar is nog Java 11-bytecode en draait
  ongewijzigd op 21.
- **Maven** op het `PATH`. Er is geen wrapper in deze repository.
- **Eerst de kern installeren.** Deze module haalt `gaia:1.1.0` uit `~/.m2`; een verouderde of ontbrekende
  kern-jar is de gebruikelijke oorzaak van `package tools.pantheum.gaia does not exist`.

```bash
(cd gaia && mvn -q install -DskipTests)      # publish the core
(cd gaia-desktop && mvn javafx:run)          # run from source
(cd gaia-desktop && mvn clean package)       # target/dist/gaia-desktop-<version>-<platform>/ — jar (~290 KB), lib/, run.sh (run.bat on win)
(cd gaia-desktop && mvn clean -Pfat package) # one self-contained jar, ~50 MB
(cd gaia-desktop && mvn package -Dfx.platform=win)   # another platform; see Packaging
java -jar gaia-desktop/target/dist/gaia-desktop-1.0.0-mac-aarch64/gaia-desktop.jar
java -jar gaia-desktop/target/dist/gaia-desktop-1.0.0-mac-aarch64/gaia-desktop.jar --language=FR
```

`--language` is het enige opdrachtregelargument, en naast `-Dgaia.desktop.home=…`, dat het hele profiel
ergens anders neerzet, de enige manier om het opstarten te beïnvloeden.

`mvn javafx:run` is de ontwikkelroute: de plug-in zet JavaFX op het **module path**, zodat de toolkit als
benoemde module start en `GaiaDesktopApp` direct kan worden gestart. Een verpakte jar kan dat niet — alles
staat op het class path —, en daarom is zijn `Main-Class` het tussenstuk `Launcher`. Een subklasse van
`Application` als `Main-Class` in die opzet faalt met *"JavaFX runtime components are missing"*.

Gebruik altijd `clean` — één keer, vóór het bouwen van een reeks platforms, omdat het `target/dist/` voor
allemaal leegmaakt. Zie [Problemen met de build oplossen](#problemen-met-de-build-oplossen) voor de reden.

### Verpakken

Alles wat geleverd wordt, wordt samengebracht onder `target/dist/gaia-desktop-<version>-<platform>/` en
verder niets, zodat de map als geheel gezipt of gekopieerd kan worden zonder artefacten uit `target/` te
hoeven kiezen naast `classes/`, `maven-status/` en de rest van het werkmateriaal van de build.
`${dist.dir}` benoemt haar één keer; elke plug-in die een artefact schrijft, wijst ernaar.

#### Eén build per platform

De JavaFX-jars bevatten native code, dus elke distributie is voor één platform, aangeduid met de eigenschap
`fx.platform` als OpenJFX-classifier: `mac-aarch64`, `mac`, `win`, `linux` of `linux-aarch64`. Vijf
OS-geactiveerde `fx-*`-profielen zetten haar op dat van de bouwende machine, wat alles is wat een gewone
`mvn package` of `mvn javafx:run` nodig heeft; `-Dfx.platform=…` gaat boven hen om elk ander platform te
bouwen, vanaf elke machine — de JavaFX-jars zijn slechts downloads. `${dist.dir}` is
`target/dist/${fx.platform}`, zodat de build van het ene platform nooit die van een ander overschrijft.

```bash
(cd gaia-desktop && mvn -q clean package)    # this machine's platform, with the tests
for p in win linux mac mac-aarch64; do (cd gaia-desktop && mvn -q package -DskipTests -Dfx.platform=$p); done
```

Twee details in de pom laten dat werken, en beide zien er overbodig uit:

- **Elke JavaFX-module is een directe afhankelijkheid** — base, graphics, controls, media en web —, elk
  met `<classifier>${fx.platform}</classifier>` en een uitsluiting `org.openjfx:*`. De poms van OpenJFX
  kiezen hun transitieve jars op basis van het OS van de *bouwende machine*, dus graphics of base transitief
  laten binnenkomen zet de Mac-natives in een Windows-build naast die van Windows.
- **De eigenschap heet `fx.platform`, niet `javafx.platform`.** Van buitenaf ingesteld wordt de eigen
  eigenschap van OpenJFX door zijn ouder-pom in termen van zichzelf opnieuw gedefinieerd; Maven verwerpt die
  pom als ongeldig en laat de platformjars stilletjes weg, wat een build zonder JavaFX oplevert.

Beide indelingen schrijven hetzelfde pad, `target/dist/gaia-desktop-<version>-<platform>/gaia-desktop.jar`,
en per build draait er precies één. `${jar.phase}`, `assembly.skipAssembly` en `mdep.skip` voorkomen dat ze
het allebei claimen.

| | Standaard | `-Pfat` |
|---|---|---|
| `gaia-desktop.jar` | ~290 KB — alleen de klassen en resources van deze module | ~50 MB — afhankelijkheden erin uitgepakt |
| `dist/gaia-desktop-<version>-<platform>/lib/` | 17 jars, één voor één genoemd in het `Class-Path` van het manifest | — |
| Leveren | de jar **en** `lib/` ernaast | één bestand |
| Ook in `dist/gaia-desktop-<version>-<platform>/` | `run.bat` (win) of `run.sh` (overige) | hetzelfde |
| En | `readme.html` en `docs/` (elk platform) | hetzelfde |

Elke distributie bevat ook de handleidingen: `docs/` gekopieerd met behoud van de indeling, zodat de links
tussen handleidingen en vertalingen blijven werken, zonder de vertaalgereedschappen (`check-i18n.py`,
`i18n-manifest.json`, `i18n-plan.md`); en `readme.html` in de hoofdmap, een index met een link naar elk
ervan, uit `src/main/dist/readme.html` met `@VERSION@` gefilterd naar de pom-versie. Beide worden geschreven
door de antrun-stap `pick-launch-script`, die eerst `docs/` verwijdert zodat een verwijderde handleiding niet
blijft hangen. De links in de readme zijn met de hand geschreven — een hernoemde handleiding of een onder
`docs/` toegevoegde vertaling heeft daar ook een eigen rij nodig.

Het `Class-Path` van het manifest en `lib/` zijn twee helften van één ding: de jar noemt
`lib/<artifact>-<version>.jar` voor elke runtime-afhankelijkheid, en `copy-dependencies` vult `lib/` met
precies die namen. Dat is wat `java -jar` laat werken.

#### De macOS-app en de distributie-zip

Eén antrun-uitvoering, `mac-app-and-zip`, sluit de fase `package` af. De antrun-plug-in wordt daarom **na**
de jar- en assembly-plug-ins gedeclareerd — Maven voert de uitvoeringen van een fase uit in de volgorde
waarin de plug-ins gedeclareerd zijn —, zodat de jar, in beide verpakkingen, bestaat wanneer ze draait.
(Haar andere uitvoering, `pick-launch-script`, zit in `prepare-package` en wordt niet geraakt.)

Voor `mac` en `mac-aarch64` bouwt ze eerst `GAIA Desktop.app` in de distributiemap: `Info.plist` (met
`@VERSION@` gefilterd naar de pom-versie), het startprogramma `Contents/MacOS/gaia-desktop` (uitvoerbaar
gemaakt) en `gaia-desktop.icns`, alles uit `src/main/macos/`, en **verplaatst** dan `gaia-desktop.jar` en
`lib/` naar `Contents/Java`, zodat de bundle op zichzelf staat. `run.sh` kijkt daar wanneer er geen jar
naast staat. Het startprogramma vindt Java op versie (`java_home -v 21+`, dan de openjdk van Homebrew —
starten vanuit de Finder ziet geen `JAVA_HOME` of `PATH`), toont via `osascript` een dialoogvenster als die
er niet is, doet een `cd` naar `~/.gaia-desktop` zodat de relatieve `logs/` van logback beschrijfbaar is, en
geeft `-Xdock:name`/`-Xdock:icon` door. macOS koppelt de draaiende `java` aan de bundle (de bundle-id en
het Dock-pictogram zijn die van de app); de menubalk toont GAIA Desktop.

Daarna zipt ze voor elk platform de map naar `target/dist/gaia-desktop-<version>-<platform>.zip` onder een
map met dezelfde naam, met de `<zip>` van Ant: `run.sh` en `Contents/MacOS/**` als aparte bestandsset met
`filemode="755"`, anders zouden ze niet-uitvoerbaar worden uitgepakt. De zip wordt **niet gekoppeld**: hij is
voor GitHub-releases, en de vier platforms komen uit vier builds, die Maven Central — één onveranderlijke
upload per versie — nooit als één release zou kunnen aannemen.

`project.build.outputTimestamp` wordt op de bouwtijd gezet. Anders dateren de archiveerders in
maven-jar-plugin 3.5 en maven-assembly-plugin 3.8 elke vermelding op 1 februari 1980.

Het app-pictogram wordt getekend door `tools/icon/GaiaIcon.java` (geen deel van de build; de kop beschrijft
de stappen voor `sips`/`iconutil`). De uitvoer ervan is ingecheckt: `src/main/macos/gaia-desktop.icns` voor
de bundle, en `src/main/resources/icons/gaia-desktop-{16..256}.png`, die `GaiaDesktopApp` als vensterpictogram
instelt (Windows en Linux; macOS negeert vensterpictogrammen).

#### De startscripts

`run.sh` en `run.bat` komen uit `src/main/scripts/` en worden ongefilterd naar
`dist/gaia-desktop-<version>-<platform>/` gekopieerd — `run.sh` zit vol `${...}`-shell-expansies die de
Maven-filtering vrij zou mogen herschrijven. Een antrun-stap houdt daarna alleen het script over dat het
platform uitvoert — `run.bat` voor `win`, `run.sh` voor alle andere — en zet het uitvoerbit op `run.sh`,
omdat `copy-resources` het niet meeneemt. Ze beslist op basis van het opgeloste `${fx.platform}` en niet via
profielen, omdat profielactivering geen waarde ziet die een ander profiel heeft gezet: een Linux-build
gemaakt op Windows zou anders het script van de host krijgen.

Ze **vertrouwen niet op het manifest**. Elk bouwt zelf het class path op, `gaia-desktop.jar` gevolgd door
elke `.jar` in `lib/`, één voor één genoemd in plaats van over te laten aan een jokerteken `lib/*`, zodat een
pad met spaties één vermelding blijft en de telling van wat is toegevoegd klopt. Starten met `-cp` is de
reden dat ze `tools.pantheum.gaia.desktop.Launcher` expliciet noemen: `-cp` leest de `Main-Class` van het
manifest niet. Het volgt **wel** nog zijn `Class-Path` — een jar op `-cp` brengt de jars mee die zijn
manifest noemt, direct daarna doorzocht —, zodat elke jar in `lib/` wordt geladen of een script hem nu
vermeldt of niet, en er één weglaten uit het class path van het script houdt hem niet buiten de JVM. De
scripts vermelden ze toch, zodat een jar met een verouderd, met de hand bewerkt of onvolledig manifest toch
start. Hetzelfde
script dient beide verpakkingen: de `-Pfat`-jar heeft geen `lib/` toe te voegen en heeft er geen nodig,
waardoor een lege `lib/` ook een opmerking is en geen fout.

#### De Zebra-natives

De build levert niets van Zebra — geen DLL's, geen service-jars. Op Windows komen ze uit de op de machine
geïnstalleerde Scanner SDK van Zebra: `run.bat` voegt de service-jars `Jpos*.jar` en `bin\` toe aan het class
path en `bin\` aan het `PATH`, waar `System.loadLibrary` `CSJPOSScanner.dll` / `CSJPOSScanner64.dll` vindt
(`java.library.path` is standaard het `PATH`). De JavaPOS-API en de configuratielader ervan komen altijd uit
`lib\javapos-*.jar`, met of zonder SDK.

**`run.bat` voegt nooit de eigen JavaPOS-jar van de SDK toe**, `External\JavaPOS-1.14.0-Dist\javaPOS114.jar`.
Vroeger deed het dat wel, en liet het `lib\javapos-*.jar` weg om twee kopieën van de API te vermijden — maar
het `Class-Path` van het manifest laadde ze toch (zie hierboven), dus de JVM kreeg beide: de klassen van de
4.x-lader uit `lib\`, met daarnaast `jpos/res/jpos.properties` en `jcl.xsd` uit `javaPOS114.jar`. Het
properties-bestand laat [`JposConfig`](#scannerintegratie) opzij stappen, en die `jcl.xsd` accepteert
JavaPOS-versies alleen tot en met 1.10, dus elke vermelding in `jpos.xml` (1.12–1.14) faalde bij de validatie,
het register bleef leeg en elke verbinding faalde met fout 109 voordat de scanner zelfs werd bereikt. De jar
alleen al op het class path hebben is daarvoor genoeg, zelfs achter de jars van `lib\`. De service-jars van
Zebra draaien ongewijzigd op de 1.14-API uit `lib\` — openen, claimen en scannen zijn zo met een echte scanner
geverifieerd. Met de hand naast `run.bat` neergezette DLL's worden nog steeds gerespecteerd: het script zet
dan `-Djava.library.path=<dist>;%PATH%`, met het `PATH` erachter omdat de eigenschap het standaard zoekpad
volledig vervangt.

`mvn javafx:run` doet niets hiervan, dus een scanner claimen vanuit een ontwikkelrun op Windows vereist dat
de jars van de SDK op een andere manier op het class path komen en de `bin\` ervan op het `PATH`; start
daarvoor vanuit `dist/`.

Naast het class path dekken ze af wat `java -jar` niet doet: hun eigen map bepalen zodat ze vanuit elke
werkmap werken, `JAVA_HOME` verkiezen boven `PATH`, en een JVM van vóór 11 weigeren met een zin die de
oplossing noemt in plaats van `UnsupportedClassVersionError`. `GAIA_DESKTOP_OPTS` geeft JVM-opties door.
`run.bat` gebruikt `java.exe`, niet `javaw.exe`, zodat de console open blijft — Windows is het enige platform
waarop de scanner draait, en de status ervan en de JavaPOS-fouten komen daar terecht.

`run.bat` kiest ook zijn Java, JavaFX en Zebra-services op basis van wat het aantreft — een meegeleverde
`jdk*\`, een `javafx-sdk*\lib\` op het module path, de Scanner SDK van Zebra op `ZEBRA_HOME` —, laat de
JavaFX-kopieën uit `lib\` van het class path af wanneer het die van een SDK gebruikt, en geeft de SAX-parser
van de JDK, de Windows-proxy's en het Windows-certificaatarchief door, plus `--enable-native-access` op Java
17 en hoger. De README-sectie [What `run.bat` adds on Windows](../../../README.md#what-runbat-adds-on-windows)
bevat de tabel. Behoud de CRLF-regeleinden van het bestand: `cmd.exe` kan een `call :label` missen in een
batchbestand met alleen LF. Ook `run.sh` geeft `--enable-native-access=ALL-UNNAMED` door — JavaFX laadt zijn
eigen natives uit `lib/`, en met JavaFX op het class path is `ALL-UNNAMED` de module die het dekt —, en laat
het, net als `run.bat`, alleen weg wanneer de Java-versie niet te lezen is.

Beide indelingen bevatten de JavaFX-natives **van één platform**, `${fx.platform}`, dus geen van beide is
overdraagbaar tussen besturingssystemen of architecturen. Bouw één keer per doel — zie
[Eén build per platform](#eén-build-per-platform).

> **Eclipse.** De uitvoering `copy-dependencies` draagt een instructie `<?m2e ignore?>`. m2e lost een open
> workspaceproject op naar zijn map `target/classes` in plaats van naar een jar, en `copy-dependencies`
> weigert een onverpakt artefact te kopiëren (MDEP-187), wat de automatische build zou laten falen telkens
> wanneer het project `gaia` naast dit project openstaat. Het gevolg is dat een build die alleen in de IDE
> wordt gedaan `target/dist/gaia-desktop-<version>-<platform>/lib/` niet vult; voer `mvn package` uit voordat
> u de jar zelf gebruikt.

---

## Architectuur

| Pakket | Bevat |
|---|---|
| `desktop` | `GaiaDesktopApp`, `Launcher`, `Theme`, `GaiaDesktopVersion` |
| `desktop.ui` | `MainView` (het venster), `BuilderPane`, `OptionsPane`, `HistoryCell`, `EditCell` |
| `desktop.build` | `ElementBuilder` (samenstellogica zonder JavaFX), `AiPair` (één editorrij) |
| `desktop.parse` | `ParseOptions` (observeerbare `ParseConfig`), `SystemDefaults`, `Languages`, `GsToken` |
| `desktop.scanner` | `ScannerService` (JavaPOS), `ScannerListener`, `Symbologies` |
| `desktop.html` | `GaiaDesktopReport` — de rapportrenderer, zonder JavaFX en met unittests zonder de toolkit |
| `desktop.i18n` | `UiStrings` — de labelcatalogus |
| `desktop.store` | `AppPaths`, `OptionsStore`, `CaptureStore` |

### De vastlegtrechter

Elke invoer — getypt, geplakt, uit een bestand geopend, gescand of samengesteld — bereikt de parser via één
methode, `MainView.capture(String, ScanSource, String)`.

Dat is de ene ontwerpbeslissing waar de app omheen is gebouwd. Een samenstelling heeft geen eigen rapport,
geen eigen geschiedenis en geen eigen exportroute nodig, want na de trechter is een samengestelde reeks niet
te onderscheiden van een gescande. Alles wat nieuw is en een invoer oplevert, hoort door de trechter te gaan
en niet erlangs.

### Threads

| Thread | Voert uit |
|---|---|
| JavaFX-applicatiethread | Al het parseren, renderen en de gebruikersinterface. Zodra de AI-tabellen warm zijn duurt parseren minder dan een milliseconde, dus is er geen worker nodig |
| `gaia-scanner` | Elke aanroep naar de scanner — openen, claimen, inschakelen, uitschakelen, vrijgeven, sluiten. Eén langlevende thread in bezit van `ScannerService`; zie [Scannerintegratie](#scannerintegratie) |
| JavaPOS-eventthread | De callbacks van `ScannerService`. `MainView.FxScannerListener` brengt elk ervan over met `Platform.runLater` |
| Achtergrond | Wachten op `connect()` / `disconnect()` van de scanner (het claimen kan tot 5 s duren), het herstellen van de opgeslagen geschiedenis bij het opstarten — wat tegelijk de parser opwarmt — en de updatecontrole (`gaia-update-check`, één GitHub-verzoek, resultaat teruggegeven met `Platform.runLater`) |

**Wijzig nooit de selectie van een besturingselement, en vernietig het besturingselement nooit, vanuit zijn
eigen selectiegebeurtenis.** Drie afzonderlijke crashes in deze app kwamen precies daarvandaan: de AI-kiezer
resetten binnen de eigen `clearAndSelect` van de pop-up (een `IndexOutOfBoundsException` uit
`ListViewBehavior`), en het paneel dat de taalkeuzelijst bezit herbouwen vanuit de eigen handler van die
keuzelijst. Beide worden nu uitgesteld met `Platform.runLater`, en de redenen staan op beide aanroepplekken.

### Herbouwen of herlabelen

Een taalwissel **herbouwt** de omlijsting uit de catalogus — menu's, werkbalk, optiepaneel —, wat veilig is
omdat niets daarvan toestand bezit: `OptionsPane` is een tweerichtingsweergave van `ParseOptions`, dus de
waarden leven in het model en een nieuw paneel pakt ze meteen weer op.

Besturingselementen die **gebruikersgegevens** bevatten, zijn velden en worden in plaats daarvan ter plekke
herlabeld: `BuilderPane.retext(UiStrings)` behoudt de AI-rijen, de Digital Link-velden en de selectie. Het
herbouwen zou lopend werk weggooien — wat het ooit deed.

---

## Het {GS}-token

Een JavaFX-`TextField` gooit stilletjes elk teken onder `0x20` weg (`TextInputControl.filterInput`). Het
„toont het niet vreemd" — het verwijdert het. Typen, plakken, `setText` en `insertText` worden allemaal
getroffen, dus `setText("01…" + GS + "10LOT")` levert `"01…10LOT"` op: een elementreeks waarvan de betekenis
ongemerkt is veranderd, omdat de AI met variabele lengte nu opslokt wat erop volgde.

`desktop.parse.GsToken` is het hele antwoord. Het scheidingsteken reist door de interface als de letterlijke
tekst `{GS}` en wordt op weg naar de parser weer `0x1D`. Het token is hier niet verzonnen — het is precies
hoe `ASCIIRevealerUtils.printable` van de kern `0x1D` weergeeft, zodat een uit een rapport gekopieerde waarde
ongewijzigd terug in de invoerbalk geplakt kan worden.

Een `TextFormatter`-filter op het invoerveld vangt elke route waarlangs tekst kan binnenkomen — typen, ⌘V,
het contextmenu, slepen en neerzetten, `setText` —, en daarom zit de herschrijving daar en niet in elke
handler.

Het scannerpad gaat nooit via `GsToken`: scans bereiken de parser als bytes.

---

## Scannerintegratie

`ScannerService` bezit het JavaPOS-besturingselement `Scanner`: openen → claimen → inschakelen, waarbij de
datagebeurtenis na elke scan opnieuw wordt gewapend. Logische apparaatnamen komen uit
`src/main/resources/jpos.xml`.

**Elke apparaataanroep draait op één thread, `gaia-scanner`, die net zo lang leeft als de service** — nooit
op de FX-thread, en nooit op een thread die na de aanroep eindigt. De service van Zebra bereikt de scanner via
JNI over de CoreScanner-COM-API, en COM bindt wat het opzet aan het apartment van de aanroepende thread:
open op een thread die daarna eindigt, en de scanner wordt geclaimd maar blijft stil; sluit op een andere
thread, en het verkeerde apartment wordt gedeïnitialiseerd. Sluiten op de FX-thread is het slechtste geval,
omdat JavaFX daar `OleInitialize` aanroept en het neerzetdoel van elk venster ertegen registreert — een
`CoUninitialize` erop schakelt slepen en neerzetten in de hele app uit. Dit is JavaFX op Windows, niet een
bepaalde JavaFX-versie. `connect()` en `disconnect()` geven het werk aan die thread en wachten, dus roept
`MainView` beide aan vanuit een kortlevende achtergrondthread; het patroon is de `JposScannerSession` van de
viewer, waar de storing in slepen en neerzetten voor het eerst werd gevonden.

Twee regels voorkomen dat een zich misdragend apparaat de verbinding vastzet. **Het afbreken stopt nooit
vroegtijdig:** `closeQuietly` schakelt uit, geeft vrij en sluit in afzonderlijke stappen, geeft alleen vrij
als de claim wordt vastgehouden, en probeert altijd `close()` — na een mislukte claim gooit `release()` „not
claimed", en toen de stappen één `try` deelden, sloeg dat het sluiten over en bleef het apparaat open in de
service van Zebra. **De datagebeurtenis wordt in een `finally` opnieuw gewapend:** JavaPOS schakelt haar na
elke gebeurtenis uit, dus een leesactie of handler die een uitzondering gooit, moet toch opnieuw wapenen,
anders blijft de scanner stil tot een nieuwe verbinding. `ScannerLifecycleTest` beproeft beide regels tegen
een gescripte `jpos.Scanner`.

**`JposConfig` wijst JavaPOS vóór het eerste openen op de meegeleverde `jpos.xml`.** Met de JavaPOS-jars uit
Maven (`javapos-config-loader` 4.x) en zonder `jpos/res/jpos.properties` op het class path faalt anders elke
opening met *"Service does not exist in loaded JCL registry"* (fout 109), om drie afzonderlijke redenen: de
servicemanager valt terug op `SimpleRegPopulator`, die een geserialiseerde `jpos.cfg` leest en nooit
`jpos.xml`; de XML-lader zoekt `jpos.xml` alleen in de werkmap, niet in de jar; en Xerces 2.12.2 in `lib/`
weigert de JAXP-eigenschap `accessExternalDTD` die de lader instelt. `JposConfig` stelt
`jpos.config.regPopulatorClass` (`JavaxRegPopulator`), `jpos.config.populatorFileURL` (de eigen
`jar:file:…!/jpos.xml` van de jar) en `javax.xml.parsers.SAXParserFactory` (die van de JDK) in als
systeemeigenschappen — nooit een die al is ingesteld, zodat `-Djpos.config.populatorFileURL=file:…` nog altijd
een andere `jpos.xml` selecteert — en stelt niets in wanneer er een `jpos.properties` op het class path staat
of de 4.x-lader er niet is. De `javaPOS114.jar` van de Zebra SDK is zo'n geval en breekt het register
regelrecht, en daarom houdt `run.bat` hem van het class path — zie [De Zebra-natives](#de-zebra-natives).
`JposConfigTest` controleert van begin tot eind dat het register dan de scanners uit het bestand bevat.

**De apparaatkiezer wordt uit dat register gegenereerd**, niet als lijst in de code bijgehouden:
`ScannerService.logicalNames()` leest de geladen vermeldingen op de thread `gaia-scanner` en houdt die met
apparaatcategorie `Scanner` — de twee weegschaalvermeldingen in `jpos.xml` blijven buiten —, in de volgorde
`pickerOrder`: `ZebraAllScanners` eerst als het bestand hem definieert, de rest alfabetisch (het register
bewaart de bestandsvolgorde niet). Een bestand zonder hem krijgt hem niet; als er niets is geladen, wordt
alleen de standaard aangeboden, zodat de kiezer nooit leeg is. `MainView` toont de standaard meteen en vult de
lijst aan vanuit een achtergrondthread, met behoud van de opgeslagen keuze. Zo verschijnt een aan `jpos.xml`
toegevoegde scanner — of een heel bestand dat met `-Djpos.config.populatorFileURL` is genoemd — zonder
codewijziging in de kiezer, en vergelijkt `JposConfigTest` de kiezer met een onafhankelijke lezing van het
meegeleverde bestand. De services die deze vermeldingen noemen, zijn klassen van Zebra
(`com.zebra.jpos.service.scanner.*Factory`), die nog steeds op het class path moeten staan — uit de Scanner
SDK via `run.bat`, of als service-jars van Zebra die in `lib\` zijn neergezet.

Scangegevens worden gedecodeerd als **ISO-8859-1**, zodat elke byte 1:1 wordt afgebeeld en zowel het AIM Code
ID-voorvoegsel als elke `0x1D` overleven; rapporten worden als UTF-8 geschreven.

Niets in de service stelt tekst voor een gebruiker samen. Gebeurtenissen gaan naar buiten als constanten van
`ScannerListener.Event` plus de waarden die variëren, en `MainView` levert de bewoording uit de catalogus —
anders zou de statusregel Engels zijn in een toepassing die op 35 manieren vertaald is. Logregels zijn de
uitzondering en blijven Engels.

### De platformgrens

`ScannerService.isSupportedPlatform()` is één test op `os.name`, en `MainView` vraagt die voordat het ook
maar één scannerbesturingselement bouwt: op macOS en Linux is er geen schakelaar, geen apparaatkiezer, geen
menu Scanner (en dus geen <kbd>⌘K</kbd>), en geen statuslabel. Ze zijn niet verborgen — ze worden nooit
gebouwd.

Zebra levert zijn JavaPOS-services alleen voor Windows, en elders faalt niets vroeg: de jars worden opgelost
en het besturingselement wordt gebouwd, en pas `open()` faalt uiteindelijk op de ontbrekende native laag,
enkele seconden en één dialoogvenster later.

De regel gaat over de *services*, niet over JavaPOS. Een leverancier die een platformonafhankelijke service
levert, zou hier werken, en de regel verruimen is uitsluitend een wijziging in `supportsJavaPos(String)` —
dat pakketprivé is en getest wordt tegen `os.name`-tekenreeksen, zodat beide antwoorden gedekt zijn op elke
machine die de build draait.

### Symbologieën

`Symbologies` beeldt alle **78** JavaPOS-scangegevenstypen `SCAN_SDT_*` af op weergavenamen, en op het
`DataCarrierType` van de kern waar de twee woordenschatten overeenkomen. JavaPOS is de fijnste van de twee —
het onderscheidt UPC-A van EAN-13, en gewone Data Matrix van GS1 DataMatrix, waar AIM een hele familie onder
één codeteken samenbrengt.

Een type buiten de tabel krijgt toch een bruikbare naam, gevonden via reflectie over `ScannerConst`, omdat
services van leveranciers wel degelijk waarden buiten de lijst van de specificatie melden. Een test loopt
`ScannerConst` door en faalt als een gedeclareerd type geen naam heeft — wat ook constanten vangt die een
toekomstige JavaPOS toevoegt.

Sommige toewijzingen van gegevensdragers blijven bewust `UNKNOWN` in plaats van geraden: straight 2 of 5
(AIM splitst `]R`/`]S` naar het aantal start-/stopstrepen, dat JavaPOS niet meldt), de postcodes met vier
toestanden, en TLC-39.

---

## Lokalisatie

Labels staan in één properties-bestand per taal onder `src/main/resources/i18n/ui_<CODE>.properties`, met
dezelfde codes als de foutcatalogi van de kern (`EN`, `FR`, `PCM`, …). Alle 35 bestaan en alle 35 hebben een
identieke sleutelset. Bestanden worden gelezen als **UTF-8**, niet als het ISO-8859-1 dat `Properties`
aanneemt, zodat vertalingen leesbaar worden opgeslagen in plaats van als `\uXXXX`-escapes.

**Terugvallen gebeurt per sleutel, niet per bestand** — dat maakt een gedeeltelijk vertaalde taal nuttig
zodra haar eerste sleutel binnenkomt. Engels is het contract: elke sleutel moet daar bestaan.

| Laag | Sleutels | Dekking |
|---|---|---|
| Gedeelde rapportwoordenschat, overgenomen uit `GaiaHtmlReport` van `gaia-demo` | 31 | alle 35 talen |
| Desktopomlijsting — menu's, knoppen, samensteller, opties, statusregel, rapportopmaak, scannerstatus, dialoogteksten | 114 | alle 35 talen |

Zes van de gedeelde termen (`generated`, `status`, `dlRole`, `interpretations`, `message`, `level`) worden
niet gebruikt door het rapport van deze app. Ze blijven behouden zodat het gedeelde blok een getrouwe kopie
van de demowoordenschat blijft; de kop van het bestand zegt dat.

### Het argument `--language`

`GaiaDesktopApp.languageOverride()` leest `--language=…` via de eigen `getParameters().getNamed()` van
JavaFX, lost het op met `Languages.fromArgument` — catalogcode, enumnaam of BCP 47-tag, hoofdletterongevoelig
— en geeft het aan `MainView`.

**De overschrijving geldt voor de run, niet voor het bestand.** `MainView` bewaart de geladen taal in
`savedLanguage` en schrijft *die* terug, totdat `languageChosenInSession` aangeeft dat de gebruiker zelf een
taal heeft gekozen; dan blijft die keuze zoals gewoonlijk bewaard. Die splitsing is het hele ontwerp: een
kioskprofiel of een demosnelkoppeling kan in het Spaans openen zonder te overschrijven waarmee de eigenaar van
de machine morgen opent, terwijl een echte keuze in het optiepaneel blijft staan.

Een waarde die geen catalogus noemt, wordt met de geaccepteerde codes gelogd en genegeerd. Weigeren te
starten om een tikfout in een snelkoppeling zou de ergere storing zijn, en `Languages.fromArgument` geeft om
dezelfde reden `null` terug in plaats van een uitzondering te gooien.

### Een taal toevoegen

Er is geen codewijziging nodig.

1. Bewerk `ui_<CODE>.properties` — elke taal heeft al een bestand.
2. Vertaal de waarden. Laat de plaatshouders `{0}` / `{1}` staan.
3. Bouw. Twee tests dwingen het contract af: de ene faalt als de code om een sleutel vraagt die Engels niet
   definieert, de andere als de sleutelset van een catalogus van die van Engels afwijkt.

De eerste test doorzoekt de **bronnen** op sleutelliteralen in plaats van reflectie over klassen te
gebruiken, omdat de sleutels alleen als tekenreeksliteralen op de aanroepplekken bestaan. Daarom moet een
nieuw bericht worden gebouwd met de sleutel er direct in — `strings.format("st.scannerError", …)` — en niet
via een variabele worden opgezocht, anders kan de test hem niet zien.

### Wat bewust niet vertaald wordt

- de beknopte veldnamen in vaste breedte naast ruwe waarden — `format`, `position`, `length`, `raw` en
  `part` van de elementkaart, `AI`, `sep`, `pos` en `len` van de strook, en de vier cellen die een fout
  lokaliseren (`AI`, `position`, `level`, `stage`), waarvan de waarden hoe dan ook onvertaalde enumnamen zijn;
- de namen van de voorbeeldinvoer, die GS1-constructies benoemen, en de symbologienamen, die eigennamen zijn;
- de herstelalinea van `GE-C084` — normtekst, waarbij een halve vertaling erger is dan Engels;
- het vangnet voor onbekende AI's in `ElementBuilder`, dat vanuit de interface onbereikbaar is: de editor
  controleert zelf op onbekende AI's en meldt ze vanuit de catalogus;
- logregels.

Er blijft één gat over: `ScanSource.label()` levert `manual` / `scanner` / `file` / `built` in het Engels aan
de geschiedenisrij en de rapportkop. Het dichten vergt meer dan een sleutel — `HistoryCell` heeft geen
`UiStrings`, en de celfabriek van de lijst wordt één keer geïnstalleerd, dus de cellen zouden bij een
taalwissel ook ververst moeten worden.

---

## Formaten van de opgeslagen status

Twee bestanden onder `~/.gaia-desktop/`, apart gehouden omdat ze in een verschillend ritme veranderen — een
beschadigd of met de hand bewerkt vastleglogboek mag de gebruiker zijn instellingen niet kosten. Wijzig de
map met `-Dgaia.desktop.home=…`, wat de manier is waarop de tests buiten de echte thuismap blijven.

**`options.properties`** — geschreven wanneer een optie verandert, en bij afsluiten.

| Sleutel | Betekenis |
|---|---|
| `parse.mode`, `parse.language` | Diepte van de verwerkingsketen en taal van de meldingen |
| `date.endian`, `date.separator`, `date.month`, `date.year` | Weergave van datums |
| `check.skipRequires`, `check.skipExcludes` | Uitzonderingen op de structuurcontroles |
| `ui.theme` | `LIGHT` of `DARK` |
| `ui.optionsPane` | Paneel open? |
| `ui.scannerDevice` | Laatste logische JavaPOS-naam |
| `ui.mode` | `PARSE`, `BUILD` of `COMBINE` |
| `update.check` | Of de dagelijkse updatecontrole draait (standaard `true`) |
| `update.lastCheck` | Wanneer die het laatst draaide, ISO-8601; ontbrekend of onleesbaar betekent dat een controle nodig is |
| `update.skipVersion` | Een release waarvoor de gebruiker *Deze versie overslaan* koos; die wordt niet meer automatisch aangeboden |

Een ontbrekend of beschadigd bestand is nooit fataal: elke waarde valt afzonderlijk terug, dus een slechte
regel kost alleen die regel. Bij een eerste start, zonder enig bestand, worden de opties door `SystemDefaults`
gevuld vanuit de landinstellingen van de machine in plaats van vanuit de ingebouwde standaardwaarden van
GAIA — het korte datumpatroon van de landinstelling geeft de volgorde van de onderdelen, het scheidingsteken,
en of maand en jaar afgekort worden.

Geef `Properties.setProperty` nooit een null: het gooit een uitzondering, en `persistOptions()` draait vanuit
gebeurtenishandlers van de interface, waar een uitzondering op de FX-thread ontsnapt. Dat is een echte crash
die deze app al heeft gehad.

**`captures.tsv`** — geschreven wanneer de geschiedenis verandert, en bij afsluiten. Nieuwste eerst, met een
maximum van **500** vermeldingen.

```
captured(ISO)              <TAB> source  <TAB> symbology   <TAB> input
2026-08-09T14:22:05.123    SCANNER       Data Matrix       0109506000134352\x1D10LOT-001
```

Alleen wat niet opnieuw berekend kan worden, wordt opgeslagen. Het `ParseResult` wordt **niet** geserialiseerd
— het wordt bij het laden opnieuw afgeleid door de invoer nogmaals te parseren, zodat een herstelde
geschiedenis de opties weerspiegelt die *nu* gelden. Invoer bevat routinematig FNC1 en wat een scanner verder
uitstuurt, dus het invoerveld wordt ge-escaped (`\\`, `\t`, `\n`, `\r`, en `\xNN` voor al het andere onder
`0x20`) en het bestand wordt als ISO-8859-1 gelezen en geschreven, byte voor byte.

Bestanden die geschreven zijn voordat de datum werd vastgelegd, bevatten alleen een tijdstip; ze laden nog
steeds, gedateerd op vandaag, omdat de echte datum nooit is genoteerd.

---

## Tests

```bash
(cd gaia-desktop && mvn test)
```

96 tests, bewust **vrij van de JavaFX-toolkit**, wat ze snel en zonder beeldscherm uitvoerbaar houdt. Ze
dekken de rapportrenderer, de samenstellogica en AI-opzoekingen, de `{GS}`-conversie, de uit de
landinstelling afgeleide standaardwaarden, beide opslagplaatsen (inclusief een heen-en-terug met `0x1D` en
opzettelijk beschadigde bestanden), de symbologietabel, de platformregel van de scanner, de versiestub en de
catalogi.

Drie zijn het kennen waard omdat ze contracten verdedigen in plaats van functies:

| Test | Faalt wanneer |
|---|---|
| `CatalogueCoverageTest` | De code om een sleutel vraagt die `ui_EN.properties` niet definieert, of een van de 35 bestanden afwijkt van de Engelse sleutelset |
| `SymbologiesTest` | Een door JavaPOS gedeclareerde `SCAN_SDT_*`-constante geen weergavenaam heeft — ook een die een JavaPOS-upgrade toevoegt |
| `ScannerServiceTest` | De platformregel van antwoord verandert voor Windows of voor iets anders |

`UiStrings` is tijdens het draaien bewust vergevingsgezind — een onbekende sleutel wordt als de sleutel zelf
weergegeven in plaats van midden in de opmaak een uitzondering te gooien —, en precies daarom moet de
catalogustest bestaan.

**Niet gedekt:** alles wat een draaiende toolkit nodig heeft — het focusgedrag, de moduswissel, het bewerken
van tabellen, de taalwissel, en beide crashes beschreven onder [Threads](#threads). Die zijn geverifieerd door
het echte venster te bedienen, maar een regressie erin zou de build niet laten falen. TestFX of Monocle
toevoegen zou dat gat dichten.

---

## Problemen met de build oplossen

| Symptoom | Oorzaak en oplossing |
|---|---|
| `package tools.pantheum.gaia does not exist` | De kern is niet geïnstalleerd. `(cd gaia && mvn install -DskipTests)` |
| *"JavaFX runtime components are missing"* | De `Main-Class` van de jar moet `Launcher` zijn, niet `GaiaDesktopApp` |
| *"Unsupported JavaFX configuration: classes were loaded from unnamed module"* bij het opstarten | Verwacht en onschuldig — JavaFX merkt op dat het op het class path staat. Precies daarom bestaat `Launcher` |
| De jar start niet, `NoClassDefFoundError` | De standaardjar bevat alleen de klassen van deze module; `dist/lib/` moet ernaast staan. Gebruik `-Pfat` voor één zelfstandig bestand |
| `Artifact has not been packaged yet … MDEP-187` in Eclipse | Workspace-resolutie van m2e. Zie de opmerking onder [Verpakken](#verpakken) |
| Dubbele `… 2.properties`-bestanden verschijnen in een build | Het project staat in een met iCloud gesynchroniseerde map en de synchronisatie maakte midden in de build conflictkopieën. Altijd `mvn clean package`; beter nog: sluit `target/` uit van synchronisatie |
| Een menu-item toont een sleutel zoals `act.hri` | Die sleutel ontbreekt in `ui_EN.properties`. De build had dat moeten vangen — zie [Tests](#tests) |

---

## Bekende beperkingen en open werk

- **Geen UI-tests zonder beeldscherm.** Zie [Tests](#tests).
- **Platformspecifieke artefacten.** Beide verpakkingen bevatten de JavaFX-natives van één platform; bouw er
  één per doel met `-Dfx.platform` (elke machine kan ze allemaal bouwen). Geen ervan bevat een
  Java-runtime — daarvoor is `jpackage` nodig, dat alleen bouwt voor het besturingssysteem waarop het draait.
- **Scannerinvoer is alleen voor Windows.** Zie [De platformgrens](#de-platformgrens).
- **`ScanSource.label()` is niet gelokaliseerd.** Zie [Wat bewust niet vertaald
  wordt](#wat-bewust-niet-vertaald-wordt).
- **`UiState.mode` is een `String`** (`"PARSE"` / `"BUILD"`) terwijl `Theme` ernaast een enum is, dus de
  letterlijke waarde komt op meerdere plaatsen voor. Een enum zou die wegnemen.
- **Het Digital Link-doel bestaat alleen uit domein en padvoorvoegsel.** `BuilderDigitalLinkConfig`
  ondersteunt ook extra queryparameters en een fragment; de editor biedt ze niet aan.
- **Elkaar uitsluitende AI-paren (`GE-S006`) worden in de editor niet gemarkeerd** zoals onbekende en dubbele
  AI's — de samenstelling faalt eenvoudigweg.
- **De foutcatalogus van de kern verdubbelt aanhalingstekens** (`value ''09506000134350''`), wat letterlijk
  verschijnt in samenstelfouten en in het rapport. Het lijkt op het escapen van aanhalingstekens door
  `MessageFormat` dat niet ongedaan wordt gemaakt; de oplossing hoort in de kern.
