# GAIA Desktop — Příručka pro vývojáře

Jak se modul kompiluje, jak je uspořádán a jak se testuje. Co aplikace dělá při pohledu zvenčí, popisuje
[Uživatelská příručka](GaiaDesktop-User-Czech.md). Původní dokument:
[GaiaDesktop-Developer.md](../../GaiaDesktop-Developer.md).

## Obsah

1. [Modul v kostce](#modul-v-kostce)
2. [Kompilace a spuštění](#kompilace-a-spuštění)
   - [Balení](#balení)
3. [Architektura](#architektura)
   - [Trychtýř zachycení](#trychtýř-zachycení)
   - [Vlákna](#vlákna)
   - [Přestavět, nebo přepsat popisky](#přestavět-nebo-přepsat-popisky)
4. [Token {GS}](#token-gs)
5. [Integrace skeneru](#integrace-skeneru)
   - [Omezení na platformu](#omezení-na-platformu)
   - [Symbologie](#symbologie)
6. [Lokalizace](#lokalizace)
   - [Argument `--language`](#argument---language)
   - [Přidání jazyka](#přidání-jazyka)
   - [Co se záměrně nepřekládá](#co-se-záměrně-nepřekládá)
7. [Formáty uloženého stavu](#formáty-uloženého-stavu)
8. [Testování](#testování)
9. [Řešení problémů s kompilací](#řešení-problémů-s-kompilací)
10. [Známá omezení a otevřená práce](#známá-omezení-a-otevřená-práce)

---

## Modul v kostce

```
tools.pantheum:gaia-desktop:1.0.0   Java 21 · JavaFX 21
        └── depends on tools.pantheum:gaia:1.1.0
```

Klient základní knihovny a nic víc. Neobsahuje **žádné znalosti GS1**: každá definice AI, ověřovací pravidlo,
chybová zpráva a lokalizovaný popis pochází z [`GaiaParser`](../../../../gaia/docs/GaiaParser.md) a
[`GaiaBuilder`](../../../../gaia/docs/GaiaBuilder.md). Zejména seznam AI se do tohoto modulu nikdy
nekopíruje — výběr v nástroji pro sestavení čte vlastní registr jádra, takže nemůže nabídnout AI, které by
nástroj pro sestavení odmítl.

**Vstupní body:** `tools.pantheum.gaia.desktop.GaiaDesktopApp` (JavaFX `Application`) a
`tools.pantheum.gaia.desktop.Launcher` (`Main-Class` zabaleného jaru).

Modul je list: nic na něm nezávisí a sám nezávisí na žádném jiném navazujícím modulu.

---

## Kompilace a spuštění

- **Java 21** pro spuštění a JDK 21 nebo novější pro kompilaci — modul se kompiluje s `release=21`. JavaFX 21
  je odpovídající řada LTS. Jar jádra je stále bajtkód Javy 11 a na 21 běží beze změny.
- **Maven** v `PATH`. V tomto repozitáři není wrapper.
- **Nejprve nainstalované jádro.** Tento modul získává `gaia:1.1.0` z `~/.m2`; zastaralý nebo chybějící jar
  jádra je obvyklou příčinou chyby `package tools.pantheum.gaia does not exist`.

```bash
(cd gaia && mvn -q install -DskipTests)      # publish the core
(cd gaia-desktop && mvn javafx:run)          # run from source
(cd gaia-desktop && mvn clean package)       # target/dist/gaia-desktop-<version>-<platform>/ — jar (~290 KB), lib/, run.sh (run.bat on win)
(cd gaia-desktop && mvn clean -Pfat package) # one self-contained jar, ~50 MB
(cd gaia-desktop && mvn package -Dfx.platform=win)   # another platform; see Packaging
java -jar gaia-desktop/target/dist/gaia-desktop-1.0.0-mac-aarch64/gaia-desktop.jar
java -jar gaia-desktop/target/dist/gaia-desktop-1.0.0-mac-aarch64/gaia-desktop.jar --language=FR
```

`--language` je jediný argument příkazové řádky a vedle `-Dgaia.desktop.home=…`, které přesune celý profil
jinam, jediný způsob, jak ovlivnit spuštění.

`mvn javafx:run` je vývojová cesta: plugin umístí JavaFX na **module path**, takže sada nástrojů startuje jako
pojmenovaný modul a `GaiaDesktopApp` lze spustit přímo. Zabalený jar to nedokáže — vše je na class path —, a
proto je jeho `Main-Class` mezičlánek `Launcher`. Podtřída `Application` jako `Main-Class` v takovém
uspořádání selže s *"JavaFX runtime components are missing"*.

Vždy používejte `clean` — jednou, před kompilací sady platforem, protože vyprázdní `target/dist/` pro
všechny. Proč, viz [Řešení problémů s kompilací](#řešení-problémů-s-kompilací).

### Balení

Vše, co se dodává, se sestavuje v `target/dist/gaia-desktop-<version>-<platform>/` a nic jiného, takže
adresář lze zazipovat nebo zkopírovat jako celek bez vybírání artefaktů z `target/` vedle `classes/`,
`maven-status/` a zbytku pracovních souborů kompilace. `${dist.dir}` jej pojmenuje jednou; každý plugin, který
zapisuje artefakt, na něj ukazuje.

#### Jedna kompilace na platformu

Jary JavaFX obsahují nativní kód, takže každá distribuce je pro jednu platformu, určenou vlastností
`fx.platform` jako klasifikátor OpenJFX: `mac-aarch64`, `mac`, `win`, `linux` nebo `linux-aarch64`. Pět
profilů `fx-*` aktivovaných podle OS ji nastaví na platformu kompilujícího stroje, což stačí obyčejnému
`mvn package` nebo `mvn javafx:run`; `-Dfx.platform=…` má před nimi přednost a zkompiluje jakoukoli jinou, z
jakéhokoli stroje — jary JavaFX se jen stahují. `${dist.dir}` je `target/dist/${fx.platform}`, takže
kompilace jedné platformy nikdy nepřepíše jinou.

```bash
(cd gaia-desktop && mvn -q clean package)    # this machine's platform, with the tests
for p in win linux mac mac-aarch64; do (cd gaia-desktop && mvn -q package -DskipTests -Dfx.platform=$p); done
```

Aby to fungovalo, zajišťují dva detaily pomu, a oba vypadají jako zbytečné:

- **Každý modul JavaFX je přímou závislostí** — base, graphics, controls, media a web —, každý s
  `<classifier>${fx.platform}</classifier>` a vyloučením `org.openjfx:*`. Pomy OpenJFX vybírají své tranzitivní
  jary podle OS *kompilujícího stroje*, takže když se graphics nebo base nechají přijít tranzitivně, dostanou
  se nativní knihovny Macu do kompilace pro Windows vedle těch pro Windows.
- **Vlastnost se jmenuje `fx.platform`, ne `javafx.platform`.** Pokud se nastaví zvenčí, vlastní vlastnost
  OpenJFX je jeho rodičovským pomem předefinována pomocí sebe sama; Maven takový pom odmítne jako neplatný a
  potichu zahodí jary platformy, takže zbude kompilace úplně bez JavaFX.

Obě uspořádání zapisují tutéž cestu, `target/dist/gaia-desktop-<version>-<platform>/gaia-desktop.jar`, a při
každé kompilaci běží právě jedno z nich. `${jar.phase}`, `assembly.skipAssembly` a `mdep.skip` brání tomu,
aby si ji nárokovala obě.

| | Výchozí | `-Pfat` |
|---|---|---|
| `gaia-desktop.jar` | ~290 KB — jen třídy a prostředky tohoto modulu | ~50 MB — závislosti rozbalené dovnitř |
| `dist/gaia-desktop-<version>-<platform>/lib/` | 17 jarů, jmenovitě uvedených v `Class-Path` manifestu | — |
| Dodává se | jar **a** `lib/` vedle něj | jeden soubor |
| Také v `dist/gaia-desktop-<version>-<platform>/` | `run.bat` (win) nebo `run.sh` (ostatní) | totéž |
| A | `readme.html` a `docs/` (každá platforma) | totéž |

Každá distribuce obsahuje i příručky: `docs/` zkopírovaný se zachovanou strukturou, aby odkazy mezi
příručkami a překlady dál fungovaly, bez překladových nástrojů (`check-i18n.py`, `i18n-manifest.json`,
`i18n-plan.md`); a `readme.html` v kořeni, rejstřík s odkazem na každou z nich, ze
`src/main/dist/readme.html` s `@VERSION@` nahrazeným verzí z pomu. Oboje zapisuje krok antrun
`pick-launch-script`, který nejprve smaže `docs/`, aby odstraněná příručka nezůstala. Odkazy v readme jsou
psané ručně — přejmenovaná příručka nebo překlad přidaný do `docs/` potřebuje i svůj řádek tam.

`Class-Path` manifestu a `lib/` jsou dvě poloviny jedné věci: jar jmenuje `lib/<artifact>-<version>.jar` pro
každou běhovou závislost a `copy-dependencies` naplní `lib/` přesně těmito jmény. Právě díky tomu funguje
`java -jar`.

#### Aplikace pro macOS a distribuční zip

Jedno spuštění antrun, `mac-app-and-zip`, ukončuje fázi `package`. Plugin antrun je proto deklarován **za**
pluginy jar a assembly — Maven provádí spuštění fáze v pořadí deklarace pluginů —, takže když běží, jar v
obou způsobech balení už existuje. (Jeho další spuštění, `pick-launch-script`, je v `prepare-package` a
netýká se ho to.)

Pro `mac` a `mac-aarch64` nejprve sestaví `GAIA Desktop.app` v distribučním adresáři: `Info.plist` (s
`@VERSION@` nahrazeným verzí z pomu), spouštěč `Contents/MacOS/gaia-desktop` (nastavený jako spustitelný) a
`gaia-desktop.icns`, vše ze `src/main/macos/`, a pak **přesune** `gaia-desktop.jar` a `lib/` do
`Contents/Java`, aby balík byl samostatný. `run.sh` hledá tam, když vedle něj žádný jar není. Spouštěč najde
Javu podle verze (`java_home -v 21+`, potom openjdk z Homebrew — spuštění z Finderu nevidí `JAVA_HOME` ani
`PATH`), když žádná není, zobrazí dialog přes `osascript`, udělá `cd` do `~/.gaia-desktop`, aby byl relativní
adresář `logs/` knihovny logback zapisovatelný, a předá `-Xdock:name`/`-Xdock:icon`. macOS přiřadí běžící
`java` k balíku (jeho identifikátor balíku a ikona v Docku jsou ty aplikace); lišta nabídek ukazuje
GAIA Desktop.

Pak pro každou platformu zabalí adresář do `target/dist/gaia-desktop-<version>-<platform>.zip` pod složkou
stejného jména pomocí `<zip>` z Antu: `run.sh` a `Contents/MacOS/**` jako samostatná sada souborů s
`filemode="755"`, jinak by se rozbalily jako nespustitelné. Zip se **nepřikládá**: je určen pro vydání na
GitHubu a čtyři platformy pocházejí ze čtyř kompilací, které by Maven Central — jedno neměnné nahrání na
verzi — nikdy nemohl přijmout jako jedno vydání.

`project.build.outputTimestamp` je nastaven na čas kompilace. Jinak archivátory v maven-jar-plugin 3.5 a
maven-assembly-plugin 3.8 datují každou položku na 1. února 1980.

Ikonu aplikace kreslí `tools/icon/GaiaIcon.java` (není součástí kompilace; jeho hlavička uvádí kroky
`sips`/`iconutil`). Jeho výstupy jsou v repozitáři: `src/main/macos/gaia-desktop.icns` pro balík a
`src/main/resources/icons/gaia-desktop-{16..256}.png`, které `GaiaDesktopApp` nastavuje jako ikonu okna
(Windows a Linux; macOS ikony oken ignoruje).

#### Spouštěcí skripty

`run.sh` a `run.bat` pocházejí ze `src/main/scripts/` a kopírují se do `dist/gaia-desktop-<version>-<platform>/`
bez filtrování — `run.sh` je plný expanzí shellu `${...}`, které by filtrování Mavenu mohlo libovolně
přepsat. Krok antrun pak ponechá jen ten, který platforma spouští — `run.bat` pro `win`, `run.sh` pro všechny
ostatní — a nastaví na `run.sh` příznak spustitelnosti, protože `copy-resources` jej nezachová. Rozhoduje
podle vyhodnoceného `${fx.platform}`, ne pomocí profilů, protože aktivace profilu nevidí hodnotu nastavenou
jiným profilem: kompilace pro Linux provedená ve Windows by jinak dostala skript hostitele.

**Nespoléhají na manifest.** Každý si class path sestaví sám: `gaia-desktop.jar` a za ním každý `.jar` z
`lib/`, jmenované jednotlivě místo zástupného znaku `lib/*`, aby cesta s mezerami zůstala jednou položkou a
počet přidaných položek byl skutečný. Spouštění s `-cp` je důvod, proč explicitně jmenují
`tools.pantheum.gaia.desktop.Launcher`: `-cp` nečte `Main-Class` z manifestu. Jeho `Class-Path` však **stále**
následuje — jar na `-cp` přinese jary jmenované v jeho manifestu, prohledávané hned za ním —, takže každý jar
z `lib/` se načte, ať jej skript uvádí, nebo ne, a vynechání jednoho z class path skriptu jej z JVM
nevyloučí. Skripty je přesto uvádějí, aby se jar se zastaralým, ručně upraveným nebo neúplným manifestem
přesto spustil. Tentýž
skript slouží oběma způsobům balení: jar `-Pfat` nemá žádné `lib/` k přidání a žádné nepotřebuje, a proto je
prázdné `lib/` poznámkou, a ne chybou.

#### Nativní knihovny Zebra

Kompilace nedodává nic od Zebry — žádné DLL, žádné jary služeb. Ve Windows pocházejí ze Scanner SDK od Zebry
nainstalovaného v počítači: `run.bat` přidá jeho jary služeb `Jpos*.jar` a `bin\` na class path a `bin\` do
`PATH`, kde `System.loadLibrary` najde `CSJPOSScanner.dll` / `CSJPOSScanner64.dll` (`java.library.path` je
ve výchozím stavu `PATH`). API JavaPOS a jeho zavaděč konfigurace pocházejí vždy z `lib\javapos-*.jar`, se SDK
i bez něj.

**`run.bat` nikdy nepřidává vlastní jar JavaPOS ze SDK**, `External\JavaPOS-1.14.0-Dist\javaPOS114.jar`.
Kdysi to dělal a vynechával `lib\javapos-*.jar`, aby se vyhnul dvěma kopiím API — jenže `Class-Path`
manifestu je načítal i tak (viz výše), takže JVM dostávala obojí: třídy zavaděče 4.x z `lib\` a vedle nich
`jpos/res/jpos.properties` a `jcl.xsd` z `javaPOS114.jar`. Soubor vlastností přiměje
[`JposConfig`](#integrace-skeneru) ustoupit stranou a tento `jcl.xsd` přijímá verze JavaPOS jen do 1.10, takže
každá položka v `jpos.xml` (1.12–1.14) neprošla ověřením, registr zůstal prázdný a každé připojení selhalo s
chybou 109 dřív, než se vůbec dostalo ke skeneru. K tomu stačí mít ten jar na class path vůbec, i za jary z
`lib\`. Jary služeb Zebry běží beze změny na API 1.14 z `lib\` — otevření, obsazení a skenování jsou takto
ověřeny na skutečném skeneru. DLL ručně umístěné vedle `run.bat` se stále respektují: skript pak nastaví
`-Djava.library.path=<dist>;%PATH%` a připojí `PATH`, protože tato vlastnost výchozí vyhledávací cestu zcela
nahrazuje.

`mvn javafx:run` nic z toho nedělá, takže obsazení skeneru z vývojového spuštění ve Windows vyžaduje dostat
jary SDK na class path a jeho `bin\` do `PATH` jinými prostředky; k tomu spouštějte z `dist/`.

Mimo class path pokrývají to, co `java -jar` nedělá: zjistí vlastní adresář, aby fungovaly z libovolného
pracovního adresáře, dávají přednost `JAVA_HOME` před `PATH` a odmítnou JVM starší než 11 větou, která uvádí
nápravu, místo `UnsupportedClassVersionError`. `GAIA_DESKTOP_OPTS` předává volby JVM. `run.bat` používá
`java.exe`, ne `javaw.exe`, aby konzole zůstala otevřená — Windows je jediná platforma, na které skener
funguje, a jeho stav a chyby JavaPOS směřují právě tam.

`run.bat` si také vybírá svou Javu, JavaFX a služby Zebry podle toho, co najde — přibalený `jdk*\`,
`javafx-sdk*\lib\` na module path, Scanner SDK od Zebry v `ZEBRA_HOME` —, přičemž vynechá z class path kopie
JavaFX z `lib\`, když používá ty ze SDK, a předá parser SAX z JDK, proxy a úložiště certifikátů Windows a na
Javě 17 a novější `--enable-native-access`. Tabulku obsahuje oddíl README
[What `run.bat` adds on Windows](../../../README.md#what-runbat-adds-on-windows). Zachovejte v souboru konce
řádků CRLF: `cmd.exe` může v dávkovém souboru jen s LF přehlédnout `call :label`. Také `run.sh` předává
`--enable-native-access=ALL-UNNAMED` — JavaFX načítá vlastní nativní knihovny z `lib/` a s JavaFX na class
path je modulem, který to pokrývá, právě `ALL-UNNAMED` —, a stejně jako `run.bat` jej vynechá, jen když verzi
Javy nelze přečíst.

Každé z uspořádání obsahuje nativní knihovny JavaFX **jedné platformy**, `${fx.platform}`, takže žádné není
přenositelné mezi operačními systémy ani architekturami. Kompilujte jednou pro každý cíl — viz
[Jedna kompilace na platformu](#jedna-kompilace-na-platformu).

> **Eclipse.** Spuštění `copy-dependencies` nese instrukci `<?m2e ignore?>`. m2e převádí otevřený projekt
> pracovního prostoru na jeho složku `target/classes` místo jaru a `copy-dependencies` odmítá kopírovat
> nezabalený artefakt (MDEP-187), což by způsobilo selhání automatické kompilace pokaždé, když je vedle
> tohoto projektu otevřený projekt `gaia`. Důsledkem je, že kompilace provedená jen v IDE nenaplní
> `target/dist/gaia-desktop-<version>-<platform>/lib/`; než použijete samotný jar, spusťte `mvn package`.

---

## Architektura

| Balíček | Obsahuje |
|---|---|
| `desktop` | `GaiaDesktopApp`, `Launcher`, `Theme`, `GaiaDesktopVersion` |
| `desktop.ui` | `MainView` (okno), `BuilderPane`, `OptionsPane`, `HistoryCell`, `EditCell` |
| `desktop.build` | `ElementBuilder` (logika sestavení bez JavaFX), `AiPair` (jeden řádek editoru) |
| `desktop.parse` | `ParseOptions` (pozorovatelná `ParseConfig`), `SystemDefaults`, `Languages`, `GsToken` |
| `desktop.scanner` | `ScannerService` (JavaPOS), `ScannerListener`, `Symbologies` |
| `desktop.html` | `GaiaDesktopReport` — vykreslovač sestavy, bez JavaFX a s jednotkovými testy bez sady nástrojů |
| `desktop.i18n` | `UiStrings` — katalog popisků |
| `desktop.store` | `AppPaths`, `OptionsStore`, `CaptureStore` |

### Trychtýř zachycení

Každý vstup — napsaný, vložený, otevřený ze souboru, naskenovaný nebo sestavený — se dostane k analyzátoru
jedinou metodou, `MainView.capture(String, ScanSource, String)`.

To je jediné návrhové rozhodnutí, kolem kterého je aplikace postavena. Sestavený řetězec nepotřebuje vlastní
sestavu, vlastní historii ani vlastní cestu exportu, protože za trychtýřem je k nerozeznání od naskenovaného.
Cokoli nového, co vytváří vstup, by mělo projít jím, a ne vedle něj.

### Vlákna

| Vlákno | Provádí |
|---|---|
| Aplikační vlákno JavaFX | Veškerou analýzu, vykreslování a uživatelské rozhraní. Když jsou tabulky AI zahřáté, trvá analýza méně než milisekundu, takže nepotřebuje žádné pracovní vlákno |
| `gaia-scanner` | Každé volání skeneru — otevření, obsazení, zapnutí, vypnutí, uvolnění, zavření. Jedno dlouho žijící vlákno vlastněné `ScannerService`; viz [Integrace skeneru](#integrace-skeneru) |
| Vlákno událostí JavaPOS | Zpětná volání `ScannerService`. `MainView.FxScannerListener` každé z nich převede přes `Platform.runLater` |
| Na pozadí | Čekání na `connect()` / `disconnect()` skeneru (obsazení může trvat až 5 s), obnovení uložené historie při spuštění — které zároveň zahřeje analyzátor — a kontrolu aktualizací (`gaia-update-check`, jeden požadavek na GitHub, výsledek vrácený přes `Platform.runLater`) |

**Nikdy neměňte výběr ovládacího prvku ani ovládací prvek neničte zevnitř jeho vlastní události výběru.**
Tři samostatné pády této aplikace pocházely přesně odtud: resetování výběru AI uvnitř vlastního
`clearAndSelect` vyskakovacího okna (`IndexOutOfBoundsException` z `ListViewBehavior`) a přestavění panelu,
který vlastní pole pro výběr jazyka, z obslužné rutiny tohoto pole. Obojí se nyní odkládá přes
`Platform.runLater` a důvody jsou zaznamenány na obou místech volání.

### Přestavět, nebo přepsat popisky

Změna jazyka **přestaví** rámec z katalogu — nabídky, panel nástrojů, panel možností —, což je bezpečné,
protože nic z toho neuchovává stav: `OptionsPane` je obousměrný pohled na `ParseOptions`, takže hodnoty žijí
v modelu a nový panel si je hned znovu převezme.

Ovládací prvky, které drží **uživatelská data**, jsou pole a místo toho dostanou nové popisky na místě:
`BuilderPane.retext(UiStrings)` zachová řádky AI, pole Digital Link i výběr. Přestavění by zahodilo
rozpracovanou práci — což se jednou stalo.

---

## Token {GS}

JavaFX `TextField` potichu zahazuje každý znak pod `0x20` (`TextInputControl.filterInput`). Nezobrazí ho
„divně" — odstraní ho. Týká se to psaní, vkládání, `setText` i `insertText`, takže
`setText("01…" + GS + "10LOT")` dá `"01…10LOT"`: řetězec prvků, jehož význam se nepozorovaně změnil, protože
AI s proměnnou délkou teď pohltí to, co po něm následovalo.

`desktop.parse.GsToken` je celá odpověď. Oddělovač prochází rozhraním jako doslovný text `{GS}` a na cestě k
analyzátoru se z něj opět stane `0x1D`. Token zde není vymyšlený — přesně tak `0x1D` zobrazuje
`ASCIIRevealerUtils.printable` z jádra, takže hodnotu zkopírovanou ze sestavy lze beze změny vložit zpět do
vstupního řádku.

Filtr `TextFormatter` na vstupním poli zachytí každou cestu, kterou může text přijít — psaní, ⌘V, místní
nabídku, přetažení, `setText` —, a proto přepis žije tam, a ne v každé obslužné rutině.

Cesta skeneru nikdy nevede přes `GsToken`: skeny se k analyzátoru dostávají jako bajty.

---

## Integrace skeneru

`ScannerService` vlastní ovládací prvek `Scanner` z JavaPOS: otevření → obsazení → zapnutí, s opětovným
aktivováním datové události po každém skenu. Logické názvy zařízení pocházejí ze
`src/main/resources/jpos.xml`.

**Každé volání zařízení běží v jednom vlákně, `gaia-scanner`, které žije stejně dlouho jako služba** — nikdy
ve vlákně FX a nikdy ve vlákně, které po volání skončí. Služba Zebry se ke skeneru dostává přes JNI nad
COM API CoreScanner a COM váže to, co připraví, k apartmánu volajícího vlákna: otevřete ve vlákně, které pak
skončí, a skener se obsadí, ale zůstane němý; zavřete v jiném vlákně a deinicializuje se nesprávný apartmán.
Zavření ve vlákně FX je nejhorší případ, protože JavaFX tam volá `OleInitialize` a registruje v něm cíl
přetažení každého okna — `CoUninitialize` v něm vyřadí přetahování v celé aplikaci. Jde o JavaFX ve Windows,
ne o určitou verzi JavaFX. `connect()` a `disconnect()` předají práci tomuto vláknu a čekají, takže `MainView`
volá obě z krátce žijícího vlákna na pozadí; vzorem je `JposScannerSession` z prohlížeče, kde byla porucha
přetahování poprvé nalezena.

Dvě pravidla brání tomu, aby se spojení zaseklo kvůli zlobivému zařízení. **Rozebírání nikdy nekončí
předčasně:** `closeQuietly` vypíná, uvolňuje a zavírá v oddělených krocích, uvolňuje jen tehdy, když je
obsazení drženo, a vždy zkusí `close()` — po neúspěšném obsazení `release()` vyhodí „not claimed", a když
kroky sdílely jeden `try`, přeskočilo se zavření a zařízení zůstalo ve službě Zebry otevřené. **Datová
událost se znovu aktivuje ve `finally`:** JavaPOS ji po každé události vypne, takže čtení nebo obslužná
rutina, která vyhodí výjimku, ji musí přesto znovu aktivovat, jinak skener zůstane němý až do opětovného
připojení. `ScannerLifecycleTest` prověřuje obě pravidla proti skriptovanému `jpos.Scanner`.

**`JposConfig` nasměruje JavaPOS na přibalený `jpos.xml`** před prvním otevřením. S jary JavaPOS z Mavenu
(`javapos-config-loader` 4.x) a bez `jpos/res/jpos.properties` na class path jinak každé otevření selže s
*"Service does not exist in loaded JCL registry"* (chyba 109), a to ze tří samostatných důvodů: správce služeb
se vrátí k `SimpleRegPopulator`, který čte serializovaný `jpos.cfg` a nikdy `jpos.xml`; zavaděč XML hledá
`jpos.xml` jen v pracovním adresáři, ne v jaru; a Xerces 2.12.2 v `lib/` odmítá vlastnost JAXP
`accessExternalDTD`, kterou zavaděč nastavuje. `JposConfig` nastaví `jpos.config.regPopulatorClass`
(`JavaxRegPopulator`), `jpos.config.populatorFileURL` (vlastní `jar:file:…!/jpos.xml` jaru) a
`javax.xml.parsers.SAXParserFactory` (tu z JDK) jako systémové vlastnosti — nikdy už nastavenou, takže
`-Djpos.config.populatorFileURL=file:…` stále vybere jiný `jpos.xml` — a nenastaví nic, když je na class path
`jpos.properties` nebo tam zavaděč 4.x není. `javaPOS114.jar` ze Zebra SDK je přesně takový případ a registr
rovnou rozbije, a proto jej `run.bat` drží mimo class path — viz [Nativní knihovny Zebra](#nativní-knihovny-zebra).
`JposConfigTest` od začátku do konce ověřuje, že registr pak obsahuje skenery ze souboru.

**Výběr zařízení se generuje z tohoto registru**, neudržuje se jako seznam v kódu:
`ScannerService.logicalNames()` čte načtené položky ve vlákně `gaia-scanner` a ponechá ty, jejichž kategorie
zařízení je `Scanner` — dvě položky vah v `jpos.xml` zůstanou mimo —, v pořadí `pickerOrder`: nejprve
`ZebraAllScanners`, pokud jej soubor definuje, zbytek abecedně (registr pořadí v souboru nezachovává). Soubor
bez něj jej nedostane; když není načteno nic, nabídne se jen výchozí hodnota, takže výběr není nikdy prázdný.
`MainView` hned ukáže výchozí hodnotu a seznam doplní z vlákna na pozadí, přičemž zachová uloženou volbu. Tak
se skener přidaný do `jpos.xml` — nebo celý soubor určený přes `-Djpos.config.populatorFileURL` — objeví ve
výběru bez změny kódu a `JposConfigTest` porovná výběr s nezávislým přečtením přibaleného souboru. Služby,
které tyto položky jmenují, jsou třídy Zebry
(`com.zebra.jpos.service.scanner.*Factory`), jež stále musejí být na class path — ze Scanner SDK přes
`run.bat`, nebo jako jary služeb Zebry vložené do `lib\`.

Data ze skenu se dekódují jako **ISO-8859-1**, aby se každý bajt mapoval 1:1 a přežila předpona AIM Code ID
i každý `0x1D`; sestavy se zapisují v UTF-8.

Nic ve službě neskládá text pro uživatele. Události odcházejí jako konstanty `ScannerListener.Event` spolu s
proměnnými hodnotami a `MainView` dodá znění z katalogu — jinak by stavový řádek byl v aplikaci přeložené 35
způsoby anglicky. Výjimkou jsou řádky protokolu, které zůstávají anglicky.

### Omezení na platformu

`ScannerService.isSupportedPlatform()` je jeden test `os.name` a `MainView` se jej zeptá dřív, než sestaví
jakýkoli ovládací prvek skeneru: v macOS a Linuxu není přepínač, výběr zařízení, nabídka Skener (a tedy ani
<kbd>⌘K</kbd>) ani popisek stavu. Nejsou skryté — nikdy se nevytvoří.

Zebra dodává své služby JavaPOS jen pro Windows a jinde nic neselže brzy: jary se vyhodnotí a ovládací prvek
se vytvoří, a teprve `open()` nakonec selže na chybějící nativní vrstvě, několik sekund a jeden dialog
později.

Pravidlo se týká *služeb*, ne JavaPOS. Dodavatel, který by dodal multiplatformní službu, by zde fungoval, a
rozšíření pravidla je změna pouze v `supportsJavaPos(String)` — metodě viditelné v rámci balíčku a testované
proti řetězcům `os.name`, aby obě odpovědi byly pokryty na jakémkoli stroji, který kompiluje.

### Symbologie

`Symbologies` přiřazuje všech **78** typů dat skenu JavaPOS `SCAN_SDT_*` k zobrazovaným názvům a k
`DataCarrierType` jádra tam, kde se oba slovníky shodují. JavaPOS je jemnější z obou — odlišuje UPC-A od
EAN-13 a prostý Data Matrix od GS1 DataMatrix, kde AIM seskupuje celou rodinu pod jediný znak kódu.

Typ mimo tabulku přesto dostane použitelný název, nalezený reflexí přes `ScannerConst`, protože služby
dodavatelů skutečně hlásí hodnoty mimo seznam ze specifikace. Test projde `ScannerConst` a selže, pokud nějaký
deklarovaný typ nemá název — což zachytí i konstanty, které přidá budoucí JavaPOS.

Některá přiřazení nosičů jsou záměrně ponechána jako `UNKNOWN`, místo aby se hádala: prostý 2 z 5 (AIM
rozlišuje `]R`/`]S` podle počtu startovacích/stop čar, což JavaPOS nehlásí), čtyřstavové poštovní kódy a
TLC-39.

---

## Lokalizace

Popisky jsou v jednom souboru vlastností na jazyk v `src/main/resources/i18n/ui_<CODE>.properties`, se
stejnými kódy jako katalogy chyb jádra (`EN`, `FR`, `PCM`, …). Existuje všech 35 a všech 35 má shodnou sadu
klíčů. Soubory se čtou jako **UTF-8**, ne jako ISO-8859-1, které předpokládá `Properties`, aby se překlady
ukládaly čitelně, a ne jako escape sekvence `\uXXXX`.

**Záloha funguje po klíčích, ne po souborech** — díky tomu je částečně přeložený jazyk užitečný od chvíle,
kdy dorazí jeho první klíč. Angličtina je smlouva: každý klíč v ní musí existovat.

| Vrstva | Klíče | Pokrytí |
|---|---|---|
| Sdílený slovník sestavy | 31 | všech 35 jazyků |
| Rámec aplikace — nabídky, tlačítka, nástroj pro sestavení, možnosti, stavový řádek, rozvržení sestavy, stav skeneru, texty dialogů | 114 | všech 35 jazyků |

Šest sdílených pojmů (`generated`, `status`, `dlRole`, `interpretations`, `message`, `level`) sestava této
aplikace nepoužívá.
Jsou zachovány záměrně; hlavička souboru to uvádí.

### Argument `--language`

`GaiaDesktopApp.languageOverride()` čte `--language=…` přes vlastní `getParameters().getNamed()` JavaFX,
vyhodnotí jej pomocí `Languages.fromArgument` — kód katalogu, název výčtu nebo značka BCP 47, bez ohledu na
velikost písmen — a předá jej `MainView`.

**Přepsání platí pro spuštění, ne pro soubor.** `MainView` drží načtený jazyk v `savedLanguage` a zpět
zapisuje *ten*, dokud `languageChosenInSession` neukáže, že si uživatel jazyk zvolil sám; pak se jeho volba
ukládá jako obvykle. V tomto rozdělení spočívá celý návrh: profil kiosku nebo ukázkový zástupce se může
otevřít ve španělštině, aniž by přepsal to, v čem se zítra otevře vlastníkovi počítače, zatímco skutečná
volba provedená v panelu možností platí dál.

Hodnota, která neoznačuje žádný katalog, se zapíše do protokolu spolu s přijímanými kódy a ignoruje se.
Odmítnout spuštění kvůli překlepu v zástupci by byla horší porucha, a ze stejného důvodu
`Languages.fromArgument` vrací `null`, místo aby vyhodil výjimku.

### Přidání jazyka

Žádná změna kódu není potřeba.

1. Upravte `ui_<CODE>.properties` — každý jazyk už svůj soubor má.
2. Přeložte hodnoty. Zástupné symboly `{0}` / `{1}` ponechte na místě.
3. Zkompilujte. Smlouvu hlídají dva testy: jeden selže, když kód žádá klíč, který angličtina nedefinuje,
   druhý, když se sada klíčů některého katalogu odchýlí od angličtiny.

První test prohledává **zdrojové kódy** a hledá literály klíčů, místo aby používal reflexi nad třídami,
protože klíče existují jen jako řetězcové literály v místech volání. Proto se nová zpráva musí sestavit s
klíčem zapsaným přímo — `strings.format("st.scannerError", …)` —, a ne vyhledat přes proměnnou, jinak ji test
nevidí.

### Co se záměrně nepřekládá

- stručné názvy polí psané neproporcionálním písmem vedle surových hodnot — `format`, `position`, `length`,
  `raw` a `part` karty prvku, `AI`, `sep`, `pos` a `len` pásky a čtyři buňky, které určují místo chyby
  (`AI`, `position`, `level`, `stage`), jejichž hodnoty jsou beztak nepřeložené názvy výčtů;
- názvy ukázkových vstupů, které pojmenovávají konstrukce GS1, a názvy symbologií, které jsou vlastními
  jmény;
- odstavec s nápravou pro `GE-C084` — normativní text, kde je poloviční překlad horší než angličtina;
- pojistka `ElementBuilder` pro neznámé AI, z rozhraní nedosažitelná: editor sám kontroluje neznámé AI a
  hlásí je z katalogu;
- řádky protokolu.

Zůstává jedna mezera: `ScanSource.label()` dodává `manual` / `scanner` / `file` / `built` anglicky do řádku
historie a do záhlaví sestavy. Její uzavření vyžaduje víc než klíč — `HistoryCell` nemá `UiStrings` a továrna
buněk seznamu se instaluje jen jednou, takže by se při změně jazyka musely obnovovat i buňky.

---

## Formáty uloženého stavu

Dva soubory v `~/.gaia-desktop/`, držené odděleně, protože se mění v jiném rytmu — poškozený nebo ručně
upravený protokol zachycení by uživatele neměl připravit o jeho nastavení. Adresář lze změnit pomocí
`-Dgaia.desktop.home=…`, a tak se testy drží mimo skutečný domovský adresář.

**`options.properties`** — zapisuje se při změně možnosti a při ukončení.

| Klíč | Význam |
|---|---|
| `parse.mode`, `parse.language` | Hloubka zpracování a jazyk zpráv |
| `date.endian`, `date.separator`, `date.month`, `date.year` | Zobrazení dat |
| `check.skipRequires`, `check.skipExcludes` | Výjimky ze strukturních kontrol |
| `ui.theme` | `LIGHT` nebo `DARK` |
| `ui.optionsPane` | Panel otevřen? |
| `ui.scannerDevice` | Poslední logický název JavaPOS |
| `ui.mode` | `PARSE`, `BUILD` nebo `COMBINE` |
| `update.check` | Zda běží denní kontrola aktualizací (výchozí `true`) |
| `update.lastCheck` | Kdy naposledy proběhla, ISO-8601; chybějící nebo nečitelná hodnota znamená, že kontrola je na řadě |
| `update.skipVersion` | Vydání, pro které uživatel zvolil *Přeskočit tuto verzi*; znovu se automaticky nenabízí |

Chybějící nebo poškozený soubor nikdy není fatální: každá hodnota se vrací k záloze samostatně, takže jeden
špatný řádek stojí jen ten řádek. Při prvním spuštění, kdy žádný soubor neexistuje, naplní `SystemDefaults`
možnosti z místního nastavení počítače, ne z vestavěných výchozích hodnot GAIA — krátký formát data pro
národní prostředí udává pořadí složek, oddělovač a zda se měsíc a rok zkracují.

Nikdy nepředávejte `Properties.setProperty` hodnotu null: vyhodí výjimku a `persistOptions()` běží z
obslužných rutin událostí rozhraní, kde výjimka unikne do vlákna FX. Je to skutečný pád, který tato aplikace
už zažila.

**`captures.tsv`** — zapisuje se při změně historie a při ukončení. Nejnovější první, nejvýše **500**
položek.

```
captured(ISO)              <TAB> source  <TAB> symbology   <TAB> input
2026-08-09T14:22:05.123    SCANNER       Data Matrix       0109506000134352\x1D10LOT-001
```

Ukládá se jen to, co nelze znovu spočítat. `ParseResult` se **neserializuje** — při načtení se znovu odvodí
opětovnou analýzou vstupu, takže obnovená historie odráží možnosti platné *teď*. Vstupy běžně obsahují FNC1
a cokoli dalšího, co skener vyšle, takže vstupní pole je escapováno (`\\`, `\t`, `\n`, `\r` a `\xNN` pro vše
ostatní pod `0x20`) a soubor se čte a zapisuje jako ISO-8859-1, bajt po bajtu.

Soubory zapsané předtím, než se začalo ukládat datum, obsahují jen čas; stále se načtou, s dnešním datem,
protože skutečné datum nikdy zapsáno nebylo.

---

## Testování

```bash
(cd gaia-desktop && mvn test)
```

96 testů, záměrně **nezávislých na sadě nástrojů JavaFX**, což je udržuje rychlé a bez potřeby displeje.
Pokrývají vykreslovač sestavy, logiku sestavení a vyhledávání AI, převod `{GS}`, výchozí hodnoty odvozené z
národního prostředí, obě úložiště (včetně cesty tam a zpět s `0x1D` a záměrně poškozených souborů), tabulku
symbologií, pravidlo platformy skeneru, zástupnou verzi a katalogy.

Tři stojí za to znát, protože hájí smlouvy, nikoli funkce:

| Test | Selže, když |
|---|---|
| `CatalogueCoverageTest` | Kód žádá klíč, který `ui_EN.properties` nedefinuje, nebo se některý z 35 souborů odchýlí od anglické sady klíčů |
| `SymbologiesTest` | Některá konstanta `SCAN_SDT_*` deklarovaná JavaPOS nemá zobrazovaný název — včetně té, kterou přidá aktualizace JavaPOS |
| `ScannerServiceTest` | Pravidlo platformy změní odpověď pro Windows nebo pro cokoli jiného |

`UiStrings` je za běhu záměrně shovívavý — neznámý klíč se zobrazí jako klíč sám, místo aby uprostřed
rozvržení vyhodil výjimku —, a právě proto musí test katalogu existovat.

**Nepokryto:** vše, co potřebuje běžící sadu nástrojů — chování fokusu, přepínání režimu, úpravy tabulek,
změna jazyka a oba pády popsané v oddílu [Vlákna](#vlákna). Ověřily se ovládáním skutečného okna, ale
regrese v nich by kompilaci nezastavila. Přidání TestFX nebo Monocle by tuto mezeru uzavřelo.

---

## Řešení problémů s kompilací

| Příznak | Příčina a náprava |
|---|---|
| `package tools.pantheum.gaia does not exist` | Jádro není nainstalované. `(cd gaia && mvn install -DskipTests)` |
| *"JavaFX runtime components are missing"* | `Main-Class` jaru musí být `Launcher`, ne `GaiaDesktopApp` |
| *"Unsupported JavaFX configuration: classes were loaded from unnamed module"* při spuštění | Očekávané a neškodné — JavaFX zaznamenává, že je na class path. Právě proto existuje `Launcher` |
| Jar se nespustí, `NoClassDefFoundError` | Výchozí jar obsahuje jen třídy tohoto modulu; vedle něj musí ležet `dist/lib/`. Pro jediný samostatný soubor použijte `-Pfat` |
| `Artifact has not been packaged yet … MDEP-187` v Eclipse | Rozlišování pracovního prostoru v m2e. Viz poznámku v oddílu [Balení](#balení) |
| V kompilaci se objevují duplicitní soubory `… 2.properties` | Projekt je ve složce synchronizované s iCloudem a synchronizace během kompilace vytvořila konfliktní kopie. Vždy `mvn clean package`; lépe vyřaďte `target/` ze synchronizace |
| Položka nabídky ukazuje klíč jako `act.hri` | Tento klíč chybí v `ui_EN.properties`. Kompilace to měla zachytit — viz [Testování](#testování) |

---

## Známá omezení a otevřená práce

- **Žádné testy rozhraní bez displeje.** Viz [Testování](#testování).
- **Artefakty závislé na platformě.** Oba způsoby balení obsahují nativní knihovny JavaFX jedné platformy;
  kompilujte jeden pro každý cíl s `-Dfx.platform` (každý stroj může zkompilovat všechny). Žádný neobsahuje
  běhové prostředí Javy — k tomu je potřeba `jpackage`, který kompiluje jen pro OS, na kterém běží.
- **Vstup ze skeneru jen ve Windows.** Viz [Omezení na platformu](#omezení-na-platformu).
- **`ScanSource.label()` není lokalizováno.** Viz [Co se záměrně
  nepřekládá](#co-se-záměrně-nepřekládá).
- **`UiState.mode` je `String`** (`"PARSE"` / `"BUILD"`), zatímco `Theme` vedle je výčet, takže se literál
  vyskytuje na několika místech. Výčet by je odstranil.
- **Cíl Digital Link je jen doména a předpona cesty.** `BuilderDigitalLinkConfig` podporuje také další
  parametry dotazu a fragment; editor je nenabízí.
- **Vzájemně se vylučující dvojice AI (`GE-S006`) nejsou v editoru označeny** tak jako neznámé a duplicitní
  AI — sestavení prostě selže.
- **Katalog chyb jádra zdvojuje uvozovky** (`value ''09506000134350''`), což se doslova objevuje v chybách
  sestavení a v sestavě. Vypadá to na escapování uvozovek v `MessageFormat`, které se neruší; oprava patří do
  jádra.
