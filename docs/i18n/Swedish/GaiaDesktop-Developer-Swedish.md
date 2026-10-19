# GAIA Desktop — Utvecklarhandbok

Hur modulen byggs, är uppbyggd och testas. Vad programmet gör sett utifrån beskrivs i
[Användarhandboken](GaiaDesktop-User-Swedish.md). Originaldokument:
[GaiaDesktop-Developer.md](../../GaiaDesktop-Developer.md).

## Innehåll

1. [Modulen i korthet](#modulen-i-korthet)
2. [Bygga och köra](#bygga-och-köra)
   - [Paketering](#paketering)
3. [Arkitektur](#arkitektur)
   - [Inmatningstratten](#inmatningstratten)
   - [Trådar](#trådar)
   - [Bygga om eller märka om](#bygga-om-eller-märka-om)
4. [{GS}-token](#gs-token)
5. [Skannerintegration](#skannerintegration)
   - [Plattformsspärren](#plattformsspärren)
   - [Symbologier](#symbologier)
6. [Lokalisering](#lokalisering)
   - [Argumentet `--language`](#argumentet---language)
   - [Lägga till ett språk](#lägga-till-ett-språk)
   - [Vad som medvetet inte översätts](#vad-som-medvetet-inte-översätts)
7. [Format för sparat tillstånd](#format-för-sparat-tillstånd)
8. [Testning](#testning)
9. [Felsökning av bygget](#felsökning-av-bygget)
10. [Kända begränsningar och öppet arbete](#kända-begränsningar-och-öppet-arbete)

---

## Modulen i korthet

```
tools.pantheum:gaia-desktop:1.0.0   Java 21 · JavaFX 21
        └── depends on tools.pantheum:gaia:1.1.0
```

En klient till kärnbiblioteket och inget mer. Den innehåller **ingen GS1-kunskap**: varje AI-definition,
valideringsregel, felmeddelande och lokaliserad beskrivning kommer från
[`GaiaParser`](../../../../gaia/docs/GaiaParser.md) och
[`GaiaBuilder`](../../../../gaia/docs/GaiaBuilder.md). I synnerhet kopieras AI-listan aldrig in i den här
modulen — byggarens väljare läser kärnans eget register och kan därför inte erbjuda en AI som byggaren skulle
avvisa.

**Startpunkter:** `tools.pantheum.gaia.desktop.GaiaDesktopApp` (JavaFX-`Application`) och
`tools.pantheum.gaia.desktop.Launcher` (den paketerade jar-filens `Main-Class`).

Modulen är ett löv: inget är beroende av den, och den är inte beroende av någon annan nedströmsmodul.

---

## Bygga och köra

- **Java 21** för att köra, och en JDK 21 eller nyare för att bygga — modulen kompileras med `release=21`.
  JavaFX 21 är motsvarande LTS-linje. Kärnans jar är fortfarande Java 11-bytekod och körs oförändrad på 21.
- **Maven** på `PATH`. Det finns ingen wrapper i det här repot.
- **Kärnan installerad först.** Den här modulen hämtar `gaia:1.1.0` från `~/.m2`; en inaktuell eller
  saknad kärn-jar är den vanliga orsaken till `package tools.pantheum.gaia does not exist`.

```bash
(cd gaia && mvn -q install -DskipTests)      # publish the core
(cd gaia-desktop && mvn javafx:run)          # run from source
(cd gaia-desktop && mvn clean package)       # target/dist/gaia-desktop-<version>-<platform>/ — jar (~290 KB), lib/, run.sh (run.bat on win)
(cd gaia-desktop && mvn clean -Pfat package) # one self-contained jar, ~50 MB
(cd gaia-desktop && mvn package -Dfx.platform=win)   # another platform; see Packaging
java -jar gaia-desktop/target/dist/gaia-desktop-1.0.0-mac-aarch64/gaia-desktop.jar
java -jar gaia-desktop/target/dist/gaia-desktop-1.0.0-mac-aarch64/gaia-desktop.jar --language=FR
```

`--language` är det enda kommandoradsargumentet och, bortsett från `-Dgaia.desktop.home=…` som lägger hela
profilen någon annanstans, det enda sättet att påverka starten.

`mvn javafx:run` är utvecklingsvägen: insticksprogrammet lägger JavaFX på **module path**, så verktygslådan
startar som en namngiven modul och `GaiaDesktopApp` kan startas direkt. En paketerad jar kan inte det — allt
ligger på class path —, och därför är dess `Main-Class` mellanlägget `Launcher`. En underklass till
`Application` som `Main-Class` i den uppställningen misslyckas med *"JavaFX runtime components are missing"*.

Använd alltid `clean` — en gång, innan du bygger en uppsättning plattformar, eftersom det tömmer
`target/dist/` på alla. Se [Felsökning av bygget](#felsökning-av-bygget) för varför.

### Paketering

Allt som levereras sätts ihop under `target/dist/gaia-desktop-<version>-<platform>/` och inget annat, så
katalogen kan zippas eller kopieras som en helhet utan att man behöver plocka ut artefakter ur `target/`
bredvid `classes/`, `maven-status/` och resten av byggets arbetsfiler. `${dist.dir}` namnger den en gång;
varje insticksprogram som skriver en artefakt pekar dit.

#### Ett bygge per plattform

JavaFX-jarfilerna innehåller inbyggd kod, så varje distribution är för en plattform, angiven med egenskapen
`fx.platform` som en OpenJFX-klassificerare: `mac-aarch64`, `mac`, `win`, `linux` eller `linux-aarch64`. Fem
OS-aktiverade `fx-*`-profiler sätter den till den byggande datorns egen, vilket är allt som ett vanligt
`mvn package` eller `mvn javafx:run` behöver; `-Dfx.platform=…` går före dem för att bygga vilken annan som
helst, från vilken dator som helst — JavaFX-jarfilerna är bara nedladdningar. `${dist.dir}` är
`target/dist/${fx.platform}`, så en plattforms bygge skriver aldrig över en annans.

```bash
(cd gaia-desktop && mvn -q clean package)    # this machine's platform, with the tests
for p in win linux mac mac-aarch64; do (cd gaia-desktop && mvn -q package -DskipTests -Dfx.platform=$p); done
```

Två detaljer i pom-filen får det att fungera, och båda ser ut att kunna tas bort:

- **Varje JavaFX-modul är ett direkt beroende** — base, graphics, controls, media och web —, var och en med
  `<classifier>${fx.platform}</classifier>` och ett undantag `org.openjfx:*`. OpenJFX:s pom-filer väljer sina
  transitiva jarfiler efter den *byggande datorns* OS, så om graphics eller base får komma transitivt hamnar
  Mac-biblioteken i ett Windows-bygge bredvid Windows-biblioteken.
- **Egenskapen heter `fx.platform`, inte `javafx.platform`.** Satt utifrån omdefinieras OpenJFX:s egen
  egenskap av dess överordnade pom i termer av sig själv; Maven avvisar den pom-filen som ogiltig och
  släpper tyst plattformsjarfilerna, vilket lämnar ett bygge helt utan JavaFX.

Båda uppställningarna skriver samma sökväg, `target/dist/gaia-desktop-<version>-<platform>/gaia-desktop.jar`,
och exakt en av dem körs per bygge. `${jar.phase}`, `assembly.skipAssembly` och `mdep.skip` hindrar dem från
att båda göra anspråk på den.

| | Standard | `-Pfat` |
|---|---|---|
| `gaia-desktop.jar` | ~290 KB — bara den här modulens klasser och resurser | ~50 MB — beroenden uppackade i den |
| `dist/gaia-desktop-<version>-<platform>/lib/` | 16 jarfiler, namngivna en och en i manifestets `Class-Path` | — |
| Leverera | jar-filen **och** `lib/` bredvid | en fil |
| Även i `dist/gaia-desktop-<version>-<platform>/` | `run.bat` (win) eller `run.sh` (övriga) | detsamma |
| Och | `readme.html` och `docs/` (varje plattform) | detsamma |

Varje distribution innehåller också handböckerna: `docs/` kopierad med strukturen bevarad, så att länkarna
mellan handböcker och översättningar fortfarande fungerar, minus översättningsverktygen (`check-i18n.py`,
`i18n-manifest.json`); och `readme.html` i roten, ett register med länkar till var och en
av dem, från `src/main/dist/readme.html` med `@VERSION@` filtrerad till pom-versionen. Båda skrivs av
antrun-steget `pick-launch-script`, som först tar bort `docs/` så att en borttagen handbok inte blir kvar.
Länkarna i readme-filen är skrivna för hand — en omdöpt handbok eller en översättning som läggs till under
`docs/` behöver också en egen rad där.

Manifestets `Class-Path` och `lib/` är två halvor av samma sak: jar-filen namnger `lib/<artifact>-<version>.jar`
för varje körningsberoende, och `copy-dependencies` fyller `lib/` med exakt de namnen. Det är det som får
`java -jar` att fungera.

#### macOS-appen och distributionszipen

En antrun-körning, `mac-app-and-zip`, avslutar fasen `package`. Antrun-insticksprogrammet deklareras därför
**efter** jar- och assembly-insticksprogrammen — Maven kör en fas körningar i den ordning
insticksprogrammen deklareras —, så jar-filen finns, i båda paketeringarna, när den körs. (Dess andra
körning, `pick-launch-script`, ligger i `prepare-package` och påverkas inte.)

För `mac` och `mac-aarch64` bygger den först `GAIA Desktop.app` i distributionskatalogen: `Info.plist` (med
`@VERSION@` filtrerad till pom-versionen), startprogrammet `Contents/MacOS/gaia-desktop` (gjort körbart) och
`gaia-desktop.icns`, allt från `src/main/macos/`, och **flyttar** sedan `gaia-desktop.jar` och `lib/` till
`Contents/Java`, så att paketet står på egna ben. `run.sh` letar där när ingen jar ligger bredvid den.
Startprogrammet hittar Java efter version (`java_home -v 21+`, sedan Homebrews openjdk — starter från Finder
ser varken `JAVA_HOME` eller `PATH`), visar en dialogruta via `osascript` när ingen finns, gör `cd` till
`~/.gaia-desktop` så att logbacks relativa `logs/` är skrivbar, och skickar `-Xdock:name`/`-Xdock:icon`.
macOS kopplar den körande `java` till paketet (dess paket-id och Dock-symbol är appens); menyraden visar
GAIA Desktop.

Därefter zippar den, för varje plattform, katalogen till `target/dist/gaia-desktop-<version>-<platform>.zip`
under en mapp med samma namn, med Ants `<zip>`: `run.sh` och `Contents/MacOS/**` som en separat filuppsättning
med `filemode="755"`, annars skulle de packas upp som icke körbara. Zipen **bifogas inte**: den är till för
GitHub-releaser, och de fyra plattformarna kommer från fyra byggen, som Maven Central — en oföränderlig
uppladdning per version — aldrig skulle kunna ta emot som en release.

`project.build.outputTimestamp` sätts till byggtiden. Annars daterar arkivverktygen i maven-jar-plugin 3.5
och maven-assembly-plugin 3.8 varje post till den 1 februari 1980.

Appsymbolen ritas av `tools/icon/GaiaIcon.java` (ingår inte i bygget; dess huvud beskriver stegen för
`sips`/`iconutil`). Dess utdata är incheckade: `src/main/macos/gaia-desktop.icns` för paketet, och
`src/main/resources/icons/gaia-desktop-{16..256}.png`, som `GaiaDesktopApp` sätter som fönstersymbol
(Windows och Linux; macOS ignorerar fönstersymboler).

#### Startskripten

`run.sh` och `run.bat` kommer från `src/main/scripts/` och kopieras ofiltrerade till
`dist/gaia-desktop-<version>-<platform>/` — `run.sh` är full av `${...}`-skalexpansioner som Mavens filtrering
skulle vara fri att skriva om. Ett antrun-steg behåller sedan bara det skript plattformen kör — `run.bat` för
`win`, `run.sh` för alla andra — och sätter körbarhetsbiten på `run.sh`, eftersom `copy-resources` inte för
med den. Det avgör utifrån det upplösta `${fx.platform}` i stället för via profiler, eftersom
profilaktivering inte ser ett värde som en annan profil har satt: ett Linux-bygge gjort på Windows skulle
annars få värdens skript.

De **förlitar sig inte på manifestet**. Varje skript bygger själv sin class path, `gaia-desktop.jar` följd av
varje `.jar` i `lib/`, namngivna en och en i stället för att lämnas åt ett jokertecken `lib/*`, så att en
sökväg med blanksteg förblir en post och räkningen av det som lagts till är verklig. Att starta med `-cp` är
skälet till att de uttryckligen namnger `tools.pantheum.gaia.desktop.Launcher`: `-cp` läser inte manifestets
`Main-Class`. Den följer **däremot** fortfarande dess `Class-Path` — en jar på `-cp` tar med de jarfiler dess
manifest namnger, genomsökta direkt efter den —, så varje jar i `lib/` laddas oavsett om ett skript listar
den eller inte, och att utelämna en från skriptets class path håller den inte utanför JVM:en. Skripten
listar dem ändå, så att en jar vars manifest är inaktuellt, handredigerat eller ofullständigt ändå startar.
Samma
skript tjänar båda paketeringarna: `-Pfat`-jarfilen har ingen `lib/` att lägga till och behöver ingen, vilket
också är varför en tom `lib/` är en anmärkning och inte ett fel.

#### Zebras inbyggda bibliotek

Bygget levererar inget från Zebra — inga DLL-filer, inga tjänst-jarfiler. På Windows kommer de från Zebras
Scanner SDK installerad på datorn: `run.bat` lägger till dess tjänst-jarfiler `Jpos*.jar` och `bin\` på class
path och `bin\` på `PATH`, där `System.loadLibrary` hittar `CSJPOSScanner.dll` / `CSJPOSScanner64.dll`
(`java.library.path` är som standard `PATH`). JavaPOS-API:t och dess konfigurationsladdare kommer alltid från
`lib\javapos-*.jar`, med eller utan SDK.

**`run.bat` lägger aldrig till SDK:ns egen JavaPOS-jar**, `External\JavaPOS-1.14.0-Dist\javaPOS114.jar`.
Det gjorde det en gång, och utelämnade `lib\javapos-*.jar` för att undvika två kopior av API:t — men
manifestets `Class-Path` laddade dem ändå (se ovan), så JVM:en fick båda: 4.x-laddarens klasser från `lib\`,
med `javaPOS114.jar`:s `jpos/res/jpos.properties` och `jcl.xsd` bredvid. Egenskapsfilen får
[`JposConfig`](#skannerintegration) att kliva åt sidan, och den `jcl.xsd` accepterar JavaPOS-versioner bara
upp till 1.10, så varje post i `jpos.xml` (1.12–1.14) misslyckades i valideringen, registret förblev tomt och
varje anslutning misslyckades med fel 109 innan skannern ens nåddes. Att ha jar-filen på class path över
huvud taget räcker för det, även bakom `lib\`:s jarfiler. Zebras tjänst-jarfiler körs oförändrade på 1.14-API:t
från `lib\` — öppna, reservera och skanna har verifierats så mot en riktig skanner. DLL-filer som placerats
för hand bredvid `run.bat` respekteras fortfarande: skriptet sätter då
`-Djava.library.path=<dist>;%PATH%` och lägger till `PATH`, eftersom egenskapen helt ersätter standardsökvägen.

`mvn javafx:run` gör inget av detta, så att reservera en skanner från en utvecklingskörning på Windows kräver
att SDK:ns jarfiler hamnar på class path och dess `bin\` på `PATH` på annat sätt; kör från `dist/` för det.

Utöver class path täcker de det som `java -jar` inte gör: att avgöra sin egen katalog så att de fungerar från
vilken arbetskatalog som helst, att föredra `JAVA_HOME` framför `PATH`, och att avvisa en JVM äldre än 11 med
en mening som anger åtgärden i stället för `UnsupportedClassVersionError`. `GAIA_DESKTOP_OPTS` skickar
JVM-alternativ vidare. `run.bat` använder `java.exe`, inte `javaw.exe`, så att konsolen förblir öppen —
Windows är den enda plattform där skannern körs, och dess status och JavaPOS-fel hamnar där.

`run.bat` väljer också sin Java, JavaFX och Zebras tjänster efter vad det hittar — en medföljande `jdk*\`,
en `javafx-sdk*\lib\` på module path, Zebras Scanner SDK vid `ZEBRA_HOME` —, och lämnar `lib\`:s kopior av
JavaFX utanför class path när det använder en SDK:s, samt skickar JDK:ns SAX-parser, Windows proxyservrar och
certifikatarkiv och `--enable-native-access` på Java 17 och senare. README-avsnittet
[What `run.bat` adds on Windows](../../../README.md#what-runbat-adds-on-windows) har tabellen. Behåll filens
CRLF-radslut: `cmd.exe` kan missa ett `call :label` i en batchfil med bara LF. `run.sh` skickar också
`--enable-native-access=ALL-UNNAMED` — JavaFX laddar sina egna inbyggda bibliotek från `lib/`, och med JavaFX
på class path är `ALL-UNNAMED` den modul som täcker det —, och hoppar över det, liksom `run.bat`, bara när
Java-versionen inte går att läsa.

Båda uppställningarna innehåller JavaFX inbyggda bibliotek **för en plattform**, `${fx.platform}`, så ingen
av dem är flyttbar mellan operativsystem eller arkitekturer. Bygg en gång per mål — se
[Ett bygge per plattform](#ett-bygge-per-plattform).

> **Eclipse.** Körningen `copy-dependencies` har en instruktion `<?m2e ignore?>`. m2e löser upp ett öppet
> arbetsytaprojekt till dess mapp `target/classes` i stället för till en jar, och `copy-dependencies` vägrar
> kopiera en opaketerad artefakt (MDEP-187), vilket skulle få det automatiska bygget att misslyckas varje
> gång projektet `gaia` är öppet bredvid det här. Följden är att ett bygge som bara görs i IDE:n inte fyller
> `target/dist/gaia-desktop-<version>-<platform>/lib/`; kör `mvn package` innan du använder själva jar-filen.

---

## Arkitektur

| Paket | Innehåller |
|---|---|
| `desktop` | `GaiaDesktopApp`, `Launcher`, `Theme`, `GaiaDesktopVersion` |
| `desktop.ui` | `MainView` (fönstret), `BuilderPane`, `OptionsPane`, `HistoryCell`, `EditCell` |
| `desktop.build` | `ElementBuilder` (bygglogik utan JavaFX), `AiPair` (en rad i redigeraren) |
| `desktop.parse` | `ParseOptions` (observerbar `ParseConfig`), `SystemDefaults`, `Languages`, `GsToken` |
| `desktop.scanner` | `ScannerService` (JavaPOS), `ScannerListener`, `Symbologies` |
| `desktop.html` | `GaiaDesktopReport` — rapportrenderaren, utan JavaFX och enhetstestad utan verktygslådan |
| `desktop.i18n` | `UiStrings` — etikettkatalogen |
| `desktop.store` | `AppPaths`, `OptionsStore`, `CaptureStore` |

### Inmatningstratten

Varje inmatning — skriven, inklistrad, öppnad från en fil, skannad eller byggd — når parsern genom en enda
metod, `MainView.capture(String, ScanSource, String)`.

Det är det enda designbeslut som appen är byggd kring. Ett bygge behöver ingen egen rapport, ingen egen
historik och ingen egen exportväg, eftersom en byggd sträng efter tratten inte går att skilja från en skannad.
Allt nytt som producerar en inmatning bör gå genom den och inte vid sidan om.

### Trådar

| Tråd | Kör |
|---|---|
| JavaFX-programtråden | All tolkning, rendering och allt gränssnitt. När AI-tabellerna är uppvärmda tar tolkningen under en millisekund, så den behöver ingen arbetstråd |
| `gaia-scanner` | Varje anrop till skannern — öppna, reservera, aktivera, inaktivera, släppa, stänga. En långlivad tråd som ägs av `ScannerService`; se [Skannerintegration](#skannerintegration) |
| JavaPOS-händelsetråden | `ScannerService`:s återanrop. `MainView.FxScannerListener` för över vart och ett med `Platform.runLater` |
| Bakgrund | Att vänta på skannerns `connect()` / `disconnect()` (reserveringen kan ta upp till 5 s), att återställa den sparade historiken vid start — vilket samtidigt värmer upp parsern — och uppdateringskontrollen (`gaia-update-check`, en GitHub-begäran, resultatet skickat tillbaka med `Platform.runLater`) |

**Ändra aldrig en kontrolls markering, och förstör aldrig kontrollen, inifrån dess egen
markeringshändelse.** Tre separata krascher i den här appen kom just därifrån: att återställa AI-väljaren
inuti popupfönstrets egen `clearAndSelect` (ett `IndexOutOfBoundsException` från `ListViewBehavior`), och att
bygga om panelen som äger språklistan inifrån listans egen hanterare. Båda skjuts nu upp med
`Platform.runLater`, och skälen finns antecknade vid båda anropsställena.

### Bygga om eller märka om

Ett språkbyte **bygger om** ramverket från katalogen — menyer, verktygsfält, alternativpanel —, vilket är
säkert eftersom inget av det äger tillstånd: `OptionsPane` är en dubbelriktad vy av `ParseOptions`, så
värdena lever i modellen och en ny panel plockar genast upp dem igen.

Kontroller som innehåller **användardata** är fält och märks i stället om på plats:
`BuilderPane.retext(UiStrings)` behåller AI-raderna, Digital Link-fälten och markeringen. Att bygga om den
skulle kasta bort pågående arbete — vilket den en gång gjorde.

---

## {GS}-token

Ett JavaFX-`TextField` kastar tyst bort varje tecken under `0x20` (`TextInputControl.filterInput`). Det
”visar det inte konstigt" — det tar bort det. Att skriva, klistra in, `setText` och `insertText` påverkas
alla, så `setText("01…" + GS + "10LOT")` ger `"01…10LOT"`: en elementsträng vars betydelse i tysthet har
ändrats, eftersom AI:n med variabel längd nu sväljer det som följde efter den.

`desktop.parse.GsToken` är hela svaret. Avgränsaren färdas genom gränssnittet som den bokstavliga texten
`{GS}` och blir `0x1D` igen på vägen till parsern. Token har inte hittats på här — det är exakt så kärnans
`ASCIIRevealerUtils.printable` visar `0x1D`, så ett värde som kopierats ur en rapport kan klistras tillbaka
oförändrat i inmatningsfältet.

Ett `TextFormatter`-filter på inmatningsfältet fångar varje väg som text kan komma in — skrivning, ⌘V,
snabbmenyn, dra och släpp, `setText` —, och därför sker omskrivningen där i stället för i varje hanterare.

Skannervägen går aldrig genom `GsToken`: skanningar når parsern som byte.

---

## Skannerintegration

`ScannerService` äger JavaPOS-kontrollen `Scanner`: öppna → reservera → aktivera, och återaktiverar
datahändelsen efter varje skanning. Logiska enhetsnamn kommer från `src/main/resources/jpos.xml`.

**Varje enhetsanrop körs på en enda tråd, `gaia-scanner`, som lever lika länge som tjänsten** — aldrig på
FX-tråden, och aldrig på en tråd som avslutas efter anropet. Zebras tjänst når skannern via JNI över
CoreScanners COM-API, och COM binder det den sätter upp till den anropande trådens apartment: öppna på en tråd
som sedan avslutas, och skannern reserveras men förblir tyst; stäng på en annan tråd, och fel apartment
avinitieras. Att stänga på FX-tråden är det värsta fallet, eftersom JavaFX där anropar `OleInitialize` och
registrerar varje fönsters släppmål mot den — ett `CoUninitialize` på den slår ut dra och släpp i hela appen.
Detta är JavaFX på Windows, inte en viss JavaFX-version. `connect()` och `disconnect()` lämnar över till den
tråden och väntar, så `MainView` anropar båda från en kortlivad bakgrundstråd.

Två regler hindrar en enhet som beter sig illa från att låsa anslutningen. **Nedmonteringen avbryts aldrig i
förtid:** `closeQuietly` inaktiverar, släpper och stänger i separata steg, släpper bara när reserveringen
hålls, och försöker alltid `close()` — efter en misslyckad reservering kastar `release()` ”not claimed", och
när stegen delade ett `try` hoppade det över stängningen och lämnade enheten öppen i Zebras tjänst.
**Datahändelsen återaktiveras i ett `finally`:** JavaPOS inaktiverar den efter varje händelse, så en läsning
eller hanterare som kastar ett undantag måste ändå återaktivera, annars förblir skannern tyst tills en ny
anslutning görs. `ScannerLifecycleTest` prövar båda reglerna mot en skriptad `jpos.Scanner`.

**`JposConfig` pekar JavaPOS mot den medföljande `jpos.xml`** före den första öppningen. Med JavaPOS-jarfilerna
från Maven (`javapos-config-loader` 4.x) och utan `jpos/res/jpos.properties` på class path misslyckas annars
varje öppning med *"Service does not exist in loaded JCL registry"* (fel 109), av tre separata skäl:
tjänsthanteraren faller tillbaka på `SimpleRegPopulator`, som läser en serialiserad `jpos.cfg` och aldrig
`jpos.xml`; XML-laddaren letar efter `jpos.xml` bara i arbetskatalogen, inte i jar-filen; och Xerces 2.12.2 i
`lib/` avvisar JAXP-egenskapen `accessExternalDTD` som laddaren sätter. `JposConfig` sätter
`jpos.config.regPopulatorClass` (`JavaxRegPopulator`), `jpos.config.populatorFileURL` (jar-filens egen
`jar:file:…!/jpos.xml`) och `javax.xml.parsers.SAXParserFactory` (JDK:ns) som systemegenskaper — aldrig en
som redan är satt, så `-Djpos.config.populatorFileURL=file:…` väljer fortfarande en annan `jpos.xml` — och
sätter ingenting när en `jpos.properties` finns på class path eller 4.x-laddaren saknas. Zebra SDK:ns
`javaPOS114.jar` är ett sådant fall och förstör registret rakt av, och därför håller `run.bat` den borta från
class path — se [Zebras inbyggda bibliotek](#zebras-inbyggda-bibliotek). `JposConfigTest` kontrollerar från
början till slut att registret då innehåller filens skannrar.

**Enhetsväljaren genereras från det registret**, i stället för att hållas som en lista i koden:
`ScannerService.logicalNames()` läser de laddade posterna på tråden `gaia-scanner` och behåller dem vars
enhetskategori är `Scanner` — de två vågposterna i `jpos.xml` hålls utanför — i ordningen `pickerOrder`:
`ZebraAllScanners` först när filen definierar den, resten i alfabetisk ordning (registret behåller inte
filens ordning). En fil utan den får den inte; när inget är laddat erbjuds bara standardvärdet, så väljaren är
aldrig tom. `MainView` visar standardvärdet direkt och fyller på listan från en bakgrundstråd, och behåller
det sparade valet. Så en skanner som läggs till i `jpos.xml` — eller en hel fil som anges med
`-Djpos.config.populatorFileURL` — dyker upp i väljaren utan kodändring, och `JposConfigTest` kontrollerar
väljaren mot en oberoende läsning av den medföljande filen. Tjänsterna som de posterna namnger är Zebras
klasser
(`com.zebra.jpos.service.scanner.*Factory`), som fortfarande måste finnas på class path — från Scanner SDK via
`run.bat`, eller som Zebras tjänst-jarfiler placerade i `lib\`.

Skanndata avkodas som **ISO-8859-1** så att varje byte motsvaras 1:1 och både AIM Code ID-prefixet och varje
`0x1D` överlever; rapporter skrivs som UTF-8.

Inget i tjänsten sätter ihop text för en användare. Händelser går ut som `ScannerListener.Event`-konstanter
plus de värden som varierar, och `MainView` står för formuleringen från katalogen — annars skulle statusraden
vara på engelska i ett program översatt på 35 sätt. Loggrader är undantaget och förblir engelska.

### Plattformsspärren

`ScannerService.isSupportedPlatform()` är ett enda test av `os.name`, och `MainView` frågar det innan någon
skannerkontroll byggs: på macOS och Linux finns ingen växel, ingen enhetsväljare, ingen meny Skanner (och
därmed ingen <kbd>⌘K</kbd>), och ingen statusetikett. De är inte dolda — de byggs aldrig.

Zebra levererar sina JavaPOS-tjänster bara för Windows, och på andra håll misslyckas inget tidigt: jarfilerna
löses upp och kontrollen konstrueras, och det är `open()` som till slut misslyckas på det saknade inbyggda
lagret, flera sekunder och en dialogruta senare.

Regeln gäller *tjänsterna*, inte JavaPOS. En leverantör som levererade en plattformsoberoende tjänst skulle
fungera här, och att vidga regeln är en ändring i `supportsJavaPos(String)` enbart — som är paketprivat och
testas mot `os.name`-strängar så att båda svaren täcks på vilken dator som helst som kör bygget.

### Symbologier

`Symbologies` mappar alla **78** JavaPOS-skanndatatyper `SCAN_SDT_*` till visningsnamn, och till kärnans
`DataCarrierType` där de två vokabulären stämmer överens. JavaPOS är den finare av de två — den skiljer UPC-A
från EAN-13, och vanlig Data Matrix från GS1 DataMatrix, där AIM grupperar en hel familj under ett enda
kodtecken.

En typ utanför tabellen får ändå ett användbart namn, hittat genom reflektion över `ScannerConst`, eftersom
leverantörers tjänster faktiskt rapporterar värden utanför specifikationens lista. Ett test går igenom
`ScannerConst` och misslyckas om någon deklarerad typ saknar namn — vilket också fångar konstanter som en
framtida JavaPOS lägger till.

Vissa mappningar av databärare lämnas medvetet som `UNKNOWN` i stället för att gissas: straight 2 of 5 (AIM
skiljer `]R`/`]S` efter antalet start-/stoppstreck, vilket JavaPOS inte rapporterar), postkoderna med fyra
tillstånd och TLC-39.

---

## Lokalisering

Etiketterna finns i en egenskapsfil per språk under `src/main/resources/i18n/ui_<CODE>.properties`, med samma
koder som kärnans felkataloger (`EN`, `FR`, `PCM`, …). Alla 35 finns och alla 35 har en identisk
nyckeluppsättning. Filerna läses som **UTF-8**, inte som det ISO-8859-1 som `Properties` förutsätter, så att
översättningar lagras läsbart i stället för som `\uXXXX`-escapesekvenser.

**Reservvärden används per nyckel, inte per fil** — det är det som gör ett delvis översatt språk användbart
från det ögonblick dess första nyckel kommer på plats. Engelska är kontraktet: varje nyckel måste finnas där.

| Lager | Nycklar | Täckning |
|---|---|---|
| Delat rapportordförråd | 31 | alla 35 språk |
| Skrivbordsramverk — menyer, knappar, byggare, alternativ, statusrad, rapportlayout, skannerstatus, dialogtexter | 114 | alla 35 språk |

Sex av de delade termerna (`generated`, `status`, `dlRole`, `interpretations`, `message`, `level`) används
inte av den här appens rapport.
De behålls avsiktligt; filens huvud säger det.

### Argumentet `--language`

`GaiaDesktopApp.languageOverride()` läser `--language=…` genom JavaFX egen `getParameters().getNamed()`,
löser upp det med `Languages.fromArgument` — katalogkod, enum-namn eller BCP 47-tagg, utan hänsyn till
skiftläge — och lämnar det till `MainView`.

**Åsidosättningen gäller körningen, inte filen.** `MainView` behåller det språk det laddade i
`savedLanguage` och skriver tillbaka *det*, tills `languageChosenInSession` säger att användaren själv har
valt ett språk; då sparas deras val som vanligt. Den uppdelningen är hela designen: en kioskprofil eller en
demogenväg kan öppna på spanska utan att skriva över det som datorns ägare öppnar med i morgon, medan ett
verkligt val gjort i alternativpanelen ändå står kvar.

Ett värde som inte anger någon katalog loggas tillsammans med de godkända koderna och ignoreras. Att vägra
starta på grund av ett skrivfel i en genväg vore det värre felet, och `Languages.fromArgument` returnerar
`null` i stället för att kasta ett undantag av samma skäl.

### Lägga till ett språk

Ingen kodändring behövs.

1. Redigera `ui_<CODE>.properties` — varje språk har redan en fil.
2. Översätt värdena. Låt platshållarna `{0}` / `{1}` stå kvar.
3. Bygg. Två tester upprätthåller kontraktet: det ena misslyckas om koden ber om en nyckel som engelska inte
   definierar, det andra om någon katalogs nyckeluppsättning glider ifrån den engelska.

Det första testet söker igenom **källkoden** efter nyckelliteraler i stället för att använda reflektion över
klasserna, eftersom nycklarna bara finns som strängliteraler vid anropsställena. Därför måste ett nytt
meddelande byggas med nyckeln skriven direkt — `strings.format("st.scannerError", …)` — i stället för att
slås upp via en variabel, annars kan testet inte se den.

### Vad som medvetet inte översätts

- de korta fältnamnen i fast bredd bredvid råvärden — elementkortets `format`, `position`, `length`, `raw`
  och `part`, remsans `AI`, `sep`, `pos` och `len`, och de fyra celler som lokaliserar ett fel (`AI`,
  `position`, `level`, `stage`), vars värden ändå är oöversatta enum-namn;
- namnen på exempelinmatningarna, som namnger GS1-konstruktioner, och symbologinamnen, som är egennamn;
- åtgärdsstycket för `GE-C084` — standardtext, där en halv översättning är sämre än engelska;
- `ElementBuilder`:s reservkontroll för okända AI:er, som inte kan nås från gränssnittet: redigeraren
  kontrollerar själv okända AI:er och rapporterar dem från katalogen;
- loggrader.

En lucka återstår: `ScanSource.label()` levererar `manual` / `scanner` / `file` / `built` på engelska till
historikraden och rapportens huvud. Att täppa till den kräver mer än en nyckel — `HistoryCell` har ingen
`UiStrings`, och listans cellfabrik installeras en gång, så cellerna skulle också behöva uppdateras vid ett
språkbyte.

---

## Format för sparat tillstånd

Två filer under `~/.gaia-desktop/`, hållna åtskilda eftersom de ändras i olika takt — en skadad eller
handredigerad inmatningslogg ska inte kosta användaren dennes inställningar. Ändra katalogen med
`-Dgaia.desktop.home=…`, vilket är så testerna håller sig borta från den verkliga hemkatalogen.

**`options.properties`** — skrivs när ett alternativ ändras, och vid avslut.

| Nyckel | Betydelse |
|---|---|
| `parse.mode`, `parse.language` | Bearbetningsdjup och meddelandespråk |
| `date.endian`, `date.separator`, `date.month`, `date.year` | Datumvisning |
| `check.skipRequires`, `check.skipExcludes` | Undantag från strukturkontrollerna |
| `ui.theme` | `LIGHT` eller `DARK` |
| `ui.optionsPane` | Panelen öppen? |
| `ui.scannerDevice` | Senaste logiska JavaPOS-namn |
| `ui.mode` | `PARSE`, `BUILD` eller `COMBINE` |
| `update.check` | Om den dagliga uppdateringskontrollen körs (standard `true`) |
| `update.lastCheck` | När den senast kördes, ISO-8601; saknas eller oläslig betyder att en kontroll ska göras |
| `update.skipVersion` | En release som användaren valde *Hoppa över den här versionen* för; den erbjuds inte igen automatiskt |

En saknad eller skadad fil är aldrig ödesdiger: varje värde faller tillbaka för sig, så en dålig rad kostar
bara den raden. Vid en första start, utan någon fil alls, fylls alternativen i av `SystemDefaults` utifrån
datorns regionala inställningar i stället för GAIA:s inbyggda standardvärden — språkinställningens korta
datummönster ger komponenternas ordning, avgränsaren och om månad och år förkortas.

Ge aldrig `Properties.setProperty` ett null: det kastar ett undantag, och `persistOptions()` körs från
händelsehanterare i gränssnittet, där ett undantag tar sig ut på FX-tråden. Det är en verklig krasch som den
här appen redan har haft.

**`captures.tsv`** — skrivs när historiken ändras, och vid avslut. Nyaste först, högst **500** poster.

```
captured(ISO)              <TAB> source  <TAB> symbology   <TAB> input
2026-08-09T14:22:05.123    SCANNER       Data Matrix       0109506000134352\x1D10LOT-001
```

Bara det som inte kan räknas fram igen lagras. `ParseResult` serialiseras **inte** — det härleds på nytt
genom att inmatningen tolkas igen vid laddning, så en återställd historik speglar de alternativ som gäller
*nu*. Inmatningar innehåller rutinmässigt FNC1 och vad en skanner än sänder, så inmatningsfältet escapas
(`\\`, `\t`, `\n`, `\r` och `\xNN` för allt annat under `0x20`) och filen läses och skrivs som ISO-8859-1,
byte för byte.

Filer som skrevs innan datumet började registreras innehåller bara en klocktid; de laddas fortfarande, daterade
till i dag, eftersom det verkliga datumet aldrig skrevs ner.

---

## Testning

```bash
(cd gaia-desktop && mvn test)
```

198 tester, medvetet **fria från JavaFX-verktygslådan**, vilket håller dem snabba och körbara utan skärm. De
täcker rapportrenderaren, bygglogiken och AI-uppslagningar, `{GS}`-konverteringen, de standardvärden som
härleds ur språkinställningen, båda lagren (inklusive en tur och retur med `0x1D` och avsiktligt skadade
filer), symbologitabellen, skannerns plattformsregel, versionsstubben och katalogerna.

Tre är värda att känna till eftersom de försvarar kontrakt snarare än funktioner:

| Test | Misslyckas när |
|---|---|
| `CatalogueCoverageTest` | Koden ber om en nyckel som `ui_EN.properties` inte definierar, eller någon av de 35 filerna glider ifrån den engelska nyckeluppsättningen |
| `SymbologiesTest` | Någon `SCAN_SDT_*`-konstant som JavaPOS deklarerar saknar visningsnamn — även en som en JavaPOS-uppgradering lägger till |
| `ScannerServiceTest` | Plattformsregeln byter svar för Windows eller för något annat |

`UiStrings` är medvetet förlåtande vid körning — en okänd nyckel visas som själva nyckeln i stället för att
kasta ett undantag mitt i layouten —, och just därför måste katalogtestet finnas.

**Inte täckt:** allt som behöver en körande verktygslåda — fokusbeteende, lägesväxeln, tabellredigering,
språkbytet och båda kraschar som beskrivs under [Trådar](#trådar). De verifierades genom att det riktiga
fönstret styrdes, men en regression i dem skulle inte få bygget att misslyckas. Att lägga till TestFX eller
Monocle skulle täppa till den luckan.

---

## Felsökning av bygget

| Symptom | Orsak och åtgärd |
|---|---|
| `package tools.pantheum.gaia does not exist` | Kärnan är inte installerad. `(cd gaia && mvn install -DskipTests)` |
| *"JavaFX runtime components are missing"* | Jar-filens `Main-Class` måste vara `Launcher`, inte `GaiaDesktopApp` |
| *"Unsupported JavaFX configuration: classes were loaded from unnamed module"* vid start | Väntat och ofarligt — JavaFX noterar att det ligger på class path. Precis därför finns `Launcher` |
| Jar-filen startar inte, `NoClassDefFoundError` | Standard-jar-filen innehåller bara den här modulens klasser; `dist/lib/` måste ligga bredvid. Använd `-Pfat` för en enda självständig fil |
| `Artifact has not been packaged yet … MDEP-187` i Eclipse | m2e:s arbetsyteupplösning. Se anmärkningen under [Paketering](#paketering) |
| Dubbletter av `… 2.properties`-filer dyker upp i ett bygge | Projektet ligger i en iCloud-synkad mapp och synkningen skapade konfliktkopior mitt i bygget. Kör alltid `mvn clean package`; ännu bättre, undanta `target/` från synkningen |
| Ett menyalternativ visar en nyckel som `act.hri` | Den nyckeln saknas i `ui_EN.properties`. Bygget borde ha fångat det — se [Testning](#testning) |

---

## Kända begränsningar och öppet arbete

- **Inga gränssnittstester utan skärm.** Se [Testning](#testning).
- **Plattformsspecifika artefakter.** Båda paketeringarna innehåller en plattforms inbyggda JavaFX-bibliotek;
  bygg en per mål med `-Dfx.platform` (vilken dator som helst kan bygga alla). Ingen innehåller en
  Java-körmiljö — det kräver `jpackage`, som bara bygger för det operativsystem det körs på.
- **Skannerinmatning bara på Windows.** Se [Plattformsspärren](#plattformsspärren).
- **`ScanSource.label()` är inte lokaliserad.** Se [Vad som medvetet inte
  översätts](#vad-som-medvetet-inte-översätts).
- **`UiState.mode` är en `String`** (`"PARSE"` / `"BUILD"`) medan `Theme` bredvid är en enum, så literalen
  förekommer på flera ställen. En enum skulle ta bort dem.
- **Digital Link-målet är bara domän och sökvägsprefix.** `BuilderDigitalLinkConfig` stöder också extra
  frågeparametrar och ett fragment; redigeraren exponerar dem inte.
- **Ömsesidigt uteslutande AI-par (`GE-S006`) flaggas inte i redigeraren** så som okända och dubblerade
  AI:er gör — bygget misslyckas helt enkelt.
- **Kärnans felkatalog dubblerar citattecken** (`value ''09506000134350''`), vilket syns ordagrant i
  byggfel och i rapporten. Det ser ut som citatteckenescaping i `MessageFormat` som inte tas bort; rättelsen
  hör hemma i kärnan.
