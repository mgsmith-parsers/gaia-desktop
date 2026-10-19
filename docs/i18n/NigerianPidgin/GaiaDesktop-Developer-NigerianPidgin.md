# GAIA Desktop — Guide for Developer

How dem dey build di module, how e take arrange, and how dem dey test am. If na wetin di app dey do from outside
you wan know, check di [User Guide](GaiaDesktop-User-NigerianPidgin.md). Di original document:
[GaiaDesktop-Developer.md](../../GaiaDesktop-Developer.md).

## Wetin dey inside

1. [Di module for quick look](#di-module-for-quick-look)
2. [Build and run](#build-and-run)
   - [Packaging](#packaging)
3. [Architecture](#architecture)
   - [Di input funnel](#di-input-funnel)
   - [Threading](#threading)
   - [Rebuild versus re-label](#rebuild-versus-re-label)
4. [Di {GS} token](#di-gs-token)
5. [Scanner integration](#scanner-integration)
   - [Di platform gate](#di-platform-gate)
   - [Symbologies](#symbologies)
6. [Localization](#localization)
   - [Di `--language` argument](#di---language-argument)
   - [How to add language](#how-to-add-language)
   - [Wetin we no translate on purpose](#wetin-we-no-translate-on-purpose)
7. [How we dey store state](#how-we-dey-store-state)
8. [Testing](#testing)
9. [When build get wahala](#when-build-get-wahala)
10. [Known limits and work wey remain](#known-limits-and-work-wey-remain)

---

## Di module for quick look

```
tools.pantheum:gaia-desktop:1.0.0   Java 21 · JavaFX 21
        └── depends on tools.pantheum:gaia:1.1.0
```

E be client of di core library, nothing pass dat. **No GS1 knowledge dey inside am**: every AI definition, validation
rule, error message and localized description dey come from [`GaiaParser`](../../../../gaia/docs/GaiaParser.md) and
[`GaiaBuilder`](../../../../gaia/docs/GaiaBuilder.md). Mostly, dem no dey ever copy di AI list enter dis module — di
builder picker dey read di core im own registry, so e no fit offer AI wey di builder go reject.

**Entry points:** `tools.pantheum.gaia.desktop.GaiaDesktopApp` (di JavaFX `Application`) and
`tools.pantheum.gaia.desktop.Launcher` (di packaged jar im `Main-Class`).

Di module na leaf: nothing depend on am, and e no depend on any other downstream module.

---

## Build and run

- **Java 21** to run, and JDK 21 or di one wey new pass am to build — di module dey compile with `release=21`. JavaFX
  21 na di LTS line wey match am. Di core jar still be Java 11 bytecode and e dey run for 21 without any change.
- **Maven** for di `PATH`. No wrapper dey dis repo.
- **Install di core first.** Dis module dey find `gaia:1.1.0` from `~/.m2`; core jar wey don old or wey no dey na di
  usual reason for `package tools.pantheum.gaia does not exist`.

```bash
(cd gaia && mvn -q install -DskipTests)      # publish the core
(cd gaia-desktop && mvn javafx:run)          # run from source
(cd gaia-desktop && mvn clean package)       # target/dist/gaia-desktop-<version>-<platform>/ — jar (~290 KB), lib/, run.sh (run.bat on win)
(cd gaia-desktop && mvn clean -Pfat package) # one self-contained jar, ~50 MB
(cd gaia-desktop && mvn package -Dfx.platform=win)   # another platform; see Packaging
java -jar gaia-desktop/target/dist/gaia-desktop-1.0.0-mac-aarch64/gaia-desktop.jar
java -jar gaia-desktop/target/dist/gaia-desktop-1.0.0-mac-aarch64/gaia-desktop.jar --language=FR
```

`--language` na di only command-line argument, and di only way to touch how di app take start apart from
`-Dgaia.desktop.home=…`, wey dey point di whole profile go another place.

`mvn javafx:run` na di development road: di plugin dey put JavaFX for di **module path**, so di toolkit go start as
named module and you fit launch `GaiaDesktopApp` direct. Packaged jar no fit do dat — everything dey for di class path
— na why im `Main-Class` be di `Launcher` shim. If you use `Application` subclass as `Main-Class` for dat layout, e go
fail with *"JavaFX runtime components are missing"*.

Always use `clean` — one time, before you build set of platforms, because e dey empty `target/dist/` comot all of dem.
Check [When build get wahala](#when-build-get-wahala) to see why.

### Packaging

Everything wey dem fit ship dey gather under `target/dist/gaia-desktop-<version>-<platform>/` and nothing else dey dere, so
you fit zip or copy di folder as one set without to dey pick artefacts comot from `target/` near `classes/`,
`maven-status/` and di rest of di build scratch. `${dist.dir}` dey name am one time; every plugin wey dey write artefact
dey point to am.

#### One build for each platform

Di JavaFX jars carry native code, so each distribution na for one platform, wey di `fx.platform` property dey name as
OpenJFX classifier: `mac-aarch64`, `mac`, `win`, `linux` or `linux-aarch64`. Five `fx-*` profiles wey OS dey activate
dey set am to di platform of di machine wey dey build, and na only dat one plain `mvn package` or `mvn javafx:run`
need; `-Dfx.platform=…` dey pass all of dem to build any other one, from any machine — di JavaFX jars na only
download. `${dist.dir}` na `target/dist/${fx.platform}`, so one platform build no go ever overwrite another one.

```bash
(cd gaia-desktop && mvn -q clean package)    # this machine's platform, with the tests
for p in win linux mac mac-aarch64; do (cd gaia-desktop && mvn -q package -DskipTests -Dfx.platform=$p); done
```

Two pom details dey make am work, and both of dem be like say you fit remove dem:

- **Every JavaFX module na direct dependency** — base, graphics, controls, media and web — each one with
  `<classifier>${fx.platform}</classifier>` and `org.openjfx:*` exclusion. OpenJFX poms dey choose dia transitive jars
  by di OS of di *machine wey dey build*, so if you leave graphics or base make dem come transitive, di Mac natives go
  enter Windows build near di Windows ones.
- **Di property na `fx.platform`, no be `javafx.platform`.** If you set OpenJFX im own property from outside, im parent
  pom go redefine am with imself; Maven go reject dat pom say e no valid and quietly drop di platform jars, and di
  build wey remain no go get JavaFX at all.

Both layouts dey write di same path, `target/dist/gaia-desktop-<version>-<platform>/gaia-desktop.jar`, and na exactly one of
dem dey run for each build. `${jar.phase}`, `assembly.skipAssembly` and `mdep.skip` na wetin dey stop dem make both no
claim am.

| | Default | `-Pfat` |
|---|---|---|
| `gaia-desktop.jar` | ~290 KB — only dis module im classes and resources | ~50 MB — dependencies unpack inside am |
| `dist/gaia-desktop-<version>-<platform>/lib/` | 16 jars, dem name dem one by one for di manifest `Class-Path` | — |
| Wetin you go ship | di jar **and** di `lib/` wey dey near am | one file |
| Also inside `dist/gaia-desktop-<version>-<platform>/` | `run.bat` (win) or `run.sh` (di others) | di same |
| And | `readme.html` and `docs/` (every platform) | di same |

Every distribution also carry di guides: `docs/` wey dem copy with im layout, so di links between guides and
translations still dey work, minus di translation tools (`check-i18n.py`, `i18n-manifest.json`); and
`readme.html` for di root, one index wey link to each of dem, from `src/main/dist/readme.html` with `@VERSION@` changed
to di pom version. Na di `pick-launch-script` antrun step dey write both, and e dey delete `docs/` first so guide wey
dem remove no go still dey hang. Dem write di readme links by hand — if you rename guide or add translation under
`docs/`, e need im row for dere too.

Di manifest `Class-Path` and `lib/` na two halves of one thing: di jar dey name `lib/<artifact>-<version>.jar` for
every runtime dependency, and `copy-dependencies` dey fill `lib/` with exactly those names. Na dat one make
`java -jar` work.

#### Di macOS app and di distribution zip

One antrun execution, `mac-app-and-zip`, dey end di `package` phase. Na why dem declare di antrun plugin **after** di
jar and assembly plugins — Maven dey run di executions of one phase for di order wey dem declare di plugins — so di jar
don dey, for any of di two packagings, when e dey run. (Im other execution, `pick-launch-script`, dey for
`prepare-package` and e no touch am.)

For `mac` and `mac-aarch64` e first build `GAIA Desktop.app` inside di distribution folder: `Info.plist` (with
`@VERSION@` changed to di pom version), di launcher `Contents/MacOS/gaia-desktop` (dem make am executable) and
`gaia-desktop.icns`, all from `src/main/macos/`, then e **move** `gaia-desktop.jar` and `lib/` enter `Contents/Java`, so
di bundle fit stand by imself. `run.sh` dey check dere when no jar dey near am. Di launcher dey find Java by version
(`java_home -v 21+`, then Homebrew openjdk — Finder launch no dey see `JAVA_HOME` or `PATH`), e dey show dialog with
`osascript` when e no see any, e dey `cd` go `~/.gaia-desktop` so logback relative `logs/` go dey writable, and e dey
pass `-Xdock:name`/`-Xdock:icon`. macOS dey join di `java` wey dey run with di bundle (im bundle id and Dock icon na di
app own); di menu bar dey show GAIA Desktop.

Then, for every platform, e zip di folder to `target/dist/gaia-desktop-<version>-<platform>.zip` under folder wey get
di same name, with Ant `<zip>`: `run.sh` and `Contents/MacOS/**` as separate file set with `filemode="755"`, if not dem
no go fit run after you unzip. Dem **no dey attach** di zip: na for GitHub releases, and di four platforms dey come from
four builds, wey Maven Central — one upload wey no fit change for each version — no fit ever take as one release.

Dem set `project.build.outputTimestamp` to di build time. If not, di archivers inside maven-jar-plugin 3.5 and
maven-assembly-plugin 3.8 go put 1 February 1980 as di date for every entry.

Na `tools/icon/GaiaIcon.java` dey draw di app icon (e no be part of di build; im header get di `sips`/`iconutil`
steps). Dem check in im outputs: `src/main/macos/gaia-desktop.icns` for di bundle, and
`src/main/resources/icons/gaia-desktop-{16..256}.png`, wey `GaiaDesktopApp` dey set as di window icon (Windows and
Linux; macOS dey ignore window icons).

#### Di launch scripts

`run.sh` and `run.bat` dey come from `src/main/scripts/`, dem copy dem enter `dist/gaia-desktop-<version>-<platform>/`
without filter — `run.sh` full with `${...}` shell expansions wey Maven filtering fit rewrite anyhow. Then one antrun
step go keep only di one wey di platform dey run — `run.bat` for `win`, `run.sh` for every other one — and set di
executable bit for `run.sh`, because `copy-resources` no dey carry am. E dey decide from di resolved `${fx.platform}`
instead of through profiles, because profile activation no fit see value wey another profile set: if not, Linux build
wey dem do for Windows go collect di host script.

Dem **no dey depend on di manifest**. Each one dey build di class path by imself, `gaia-desktop.jar` and then every
`.jar` inside `lib/`, dem dey name dem one by one instead to leave am for `lib/*` wildcard, so path wey get space go
still be one entry and di count of wetin dem add go be real. Na because dem dey launch with `-cp` make dem dey name
`tools.pantheum.gaia.desktop.Launcher` clear: `-cp` no dey read di manifest `Main-Class`. But e **still** dey follow im
`Class-Path` — jar wey dey for `-cp` dey bring di jars wey im manifest name, and dem dey search dem straight after am —
so every `lib/` jar go load whether di script list am or not, and if you leave one out of di script class path e no go
keep am out of di JVM. Di scripts still dey list all of dem, so jar wey im manifest don old, wey dem edit by hand or wey
one entry miss go still start. Di same script dey serve both packagings: di `-Pfat` jar no get `lib/` to add and e no
need am, na also why empty `lib/` na note, no be error.

#### Di Zebra natives

Di build no dey ship anything wey be Zebra own — no DLLs, no service jars. For Windows dem dey come from Zebra Scanner
SDK wey dem install for di machine: `run.bat` dey add im `Jpos*.jar` service jars and `bin\` to di class path and
`bin\` to di `PATH`, where `System.loadLibrary` dey find `CSJPOSScanner.dll` / `CSJPOSScanner64.dll`
(`java.library.path` default na di `PATH`). Di JavaPOS API and im configuration loader always dey come from
`lib\javapos-*.jar`, whether SDK dey or not.

**`run.bat` no dey ever add di SDK im own JavaPOS jar**, `External\JavaPOS-1.14.0-Dist\javaPOS114.jar`. E dey do am
before, e leave `lib\javapos-*.jar` out so two copies of di API no go dey — but di manifest `Class-Path` still load
dem (check up), so di JVM collect both: di 4.x loader classes from `lib\`, with `javaPOS114.jar` im
`jpos/res/jpos.properties` and `jcl.xsd` near dem. Di properties file dey make [`JposConfig`](#scanner-integration)
step aside, and dat `jcl.xsd` dey accept JavaPOS versions only reach 1.10, so every entry for `jpos.xml` (1.12–1.14)
fail validation, di registry remain empty, and every connect fail with error 109 before e even reach di scanner. Just
to get dat jar for di class path don do am, even if e dey behind `lib\` jars. Zebra service jars dey run without
change for di 1.14 API from `lib\` — dem don verify open, claim and scan with real scanner like dat. DLLs wey you drop
near `run.bat` by hand still dey count: di script go then set `-Djava.library.path=<dist>;%PATH%`, e add di `PATH` for
back because di property dey replace di default search path finish.

`mvn javafx:run` no dey do any of dis, so to claim scanner from development run for Windows, you need put di SDK jars
for di class path and im `bin\` for di `PATH` another way; run from `dist/` for dat one.

Apart from di class path, dem dey handle wetin `java -jar` no dey do: dem dey resolve dia own folder so dem fit work
from any working directory, dem dey prefer `JAVA_HOME` pass `PATH`, and dem dey reject JVM wey old pass 11 with
sentence wey talk di remedy instead of `UnsupportedClassVersionError`. `GAIA_DESKTOP_OPTS` dey pass JVM options
through. `run.bat` dey use `java.exe`, no be `javaw.exe`, so di console go still dey open — Windows na di only platform
where di scanner dey run, and im status and JavaPOS errors dey go dere.

`run.bat` also dey choose im Java, JavaFX and Zebra services from wetin e find — bundled `jdk*\`, `javafx-sdk*\lib\`
for di module path, Zebra Scanner SDK for `ZEBRA_HOME` — e dey leave di `lib\` copies of JavaFX out of di class path
when e dey use di SDK own, and e dey pass di JDK SAX parser, Windows proxies and trust store, and
`--enable-native-access` for Java 17+. Di README [What `run.bat` adds on Windows](../../../README.md#what-runbat-adds-on-windows)
get di table. Keep di file CRLF line endings: `cmd.exe` fit miss `call :label` for batch file wey be LF only. `run.sh`
also dey pass `--enable-native-access=ALL-UNNAMED` — JavaFX dey load im own natives from `lib/`, and with JavaFX for di
class path na `ALL-UNNAMED` be di module wey cover am — dem dey skip am, like for `run.bat`, only when dem no fit read
di Java version.

Both layouts dey carry di JavaFX natives **of one platform**, `${fx.platform}`, so none of dem fit move across OS or
architecture. Build one time for each target — check [One build for each platform](#one-build-for-each-platform).

> **Eclipse.** Di `copy-dependencies` execution carry `<?m2e ignore?>` instruction. m2e dey resolve open workspace
> project to im `target/classes` folder instead of jar, and `copy-dependencies` dey refuse to copy artifact wey dem
> never package (MDEP-187), wey go make di auto-build fail anytime di `gaia` project dey open near dis one. Di result
> na say build wey you do only for IDE no go fill `target/dist/gaia-desktop-<version>-<platform>/lib/`; run
> `mvn package` before you use di jar by imself.

---

## Architecture

| Package | Wetin e hold |
|---|---|
| `desktop` | `GaiaDesktopApp`, `Launcher`, `Theme`, `GaiaDesktopVersion` |
| `desktop.ui` | `MainView` (di window), `BuilderPane`, `OptionsPane`, `HistoryCell`, `EditCell` |
| `desktop.build` | `ElementBuilder` (build logic wey no need JavaFX), `AiPair` (one editor row) |
| `desktop.parse` | `ParseOptions` (`ParseConfig` wey you fit observe), `SystemDefaults`, `Languages`, `GsToken` |
| `desktop.scanner` | `ScannerService` (JavaPOS), `ScannerListener`, `Symbologies` |
| `desktop.html` | `GaiaDesktopReport` — di report renderer, e no need JavaFX and dem dey unit-test am without di toolkit |
| `desktop.i18n` | `UiStrings` — di label catalogue |
| `desktop.store` | `AppPaths`, `OptionsStore`, `CaptureStore` |

### Di input funnel

Every input — wey you type, paste, open from file, scan, or build — dey reach di parser through one method,
`MainView.capture(String, ScanSource, String)`.

Na dat single design decision dem build di whole app round. Build no need im own report, im own history or im own
export road, because after di funnel, string wey you build no different from one wey you scan. Anything new wey dey
produce input suppose pass through am, no be near am.

### Threading

| Thread | Wetin e dey run |
|---|---|
| JavaFX application thread | All parsing, rendering and UI. Parsing no reach one millisecond once di AI tables don warm, so e no need worker |
| `gaia-scanner` | Every call wey enter di scanner — open, claim, enable, disable, release, close. One thread wey dey live long, `ScannerService` own am; check [Scanner integration](#scanner-integration) |
| JavaPOS event thread | `ScannerService` callbacks. `MainView.FxScannerListener` dey carry each one cross with `Platform.runLater` |
| Background | To wait for scanner `connect()` / `disconnect()` (di claim fit take reach 5 s), to restore di saved history for start-up — wey also dey warm di parser — and di update check (`gaia-update-check`, one GitHub request, di result dey come back with `Platform.runLater`) |

**No ever change control im selection, or destroy di control, from inside im own selection event.** Three different
crashes for dis app come from exactly dat one: to reset di AI picker inside di popup im own `clearAndSelect` (one
`IndexOutOfBoundsException` wey come out of `ListViewBehavior`), and to rebuild di panel wey own di language combo from
inside dat combo im own handler. Both of dem dey wait now with `Platform.runLater`, and dem write di reasons for both
call sites.

### Rebuild versus re-label

Language change dey **rebuild** di frame from di catalogue — menus, toolbar, options panel — and e safe because none
of dem get state: `OptionsPane` na two-way view of `ParseOptions`, so di values dey live for di model and new panel go
pick dem back sharp sharp.

Controls wey hold **user data** na fields, and dem dey re-label dem for where dem dey instead:
`BuilderPane.retext(UiStrings)` dey keep di AI rows, di Digital Link fields and di selection. If you rebuild am, e go
throw away work wey dey go on — and e don do am before.

---

## Di {GS} token

JavaFX `TextField` dey quietly throw away every character wey dey under `0x20` (`TextInputControl.filterInput`). E no
be say "e dey show am anyhow" — e dey remove am. Typing, pasting, `setText` and `insertText` all dey affected, so
`setText("01…" + GS + "10LOT")` go give `"01…10LOT"`: element string wey im meaning don change quietly, because di
variable-length AI go swallow wetin follow am.

`desktop.parse.GsToken` na di whole answer. Di separator dey waka through di UI as di literal text `{GS}` and e go
become `0x1D` again for road to di parser. Dem no invent di token for here — na exactly how di core
`ASCIIRevealerUtils.printable` dey show `0x1D`, so value wey you copy from report fit paste straight back inside di
input bar.

`TextFormatter` filter for di input field dey catch every road wey text fit take come — typing, ⌘V, di context menu,
drag and drop, `setText` — na why di rewrite dey live dere instead of inside each handler.

Di scanner road no dey ever pass through `GsToken`: scans dey reach di parser as bytes.

---

## Scanner integration

`ScannerService` own di JavaPOS `Scanner` control: open → claim → enable, and e dey re-arm di data event after every
scan. Logical device names dey come from `src/main/resources/jpos.xml`.

**Every device call dey run for one thread, `gaia-scanner`, wey dey live as long as di service** — never for di FX
thread, and never for thread wey go end after di call. Zebra service dey reach di scanner through JNI over di
CoreScanner COM API, and COM dey tie wetin e set up to di apartment of di thread wey call am: open am for thread wey go
end later, di scanner go claim but e go dey quiet; close am for another thread, and na di wrong apartment go
uninitialize. To close for di FX thread na di worst one, because JavaFX dey call `OleInitialize` for dere and e dey
register every window drop target against am — one `CoUninitialize` for dere go kill drag and drop for di whole app.
Dis one na JavaFX for Windows, no be one particular JavaFX version. `connect()` and `disconnect()` dey submit to dat
thread and wait, so `MainView` dey call both from short-lived background thread.

Two rules dey stop device wey dey misbehave make e no jam di connection. **Teardown no dey ever stop early:**
`closeQuietly` dey disable, release and close as separate steps, e dey release only when di claim dey hold, and e
always dey try `close()` — after claim fail, `release()` go throw "not claimed", and when di steps share one `try`,
dat one skip di close and leave di device open for Zebra service. **Dem dey re-arm di data event inside `finally`:**
JavaPOS dey disable am after every event, so read or handler wey throw must still re-arm, if not di scanner go quiet
till you reconnect. `ScannerLifecycleTest` dey test both against scripted `jpos.Scanner`.

**`JposConfig` dey point JavaPOS to di bundled `jpos.xml`** before di first open. With di Maven JavaPOS jars
(`javapos-config-loader` 4.x) and no `jpos/res/jpos.properties` for di class path, every open go fail with
*"Service does not exist in loaded JCL registry"* (error 109), for three different reasons: di service manager go fall
back to `SimpleRegPopulator`, wey dey read serialized `jpos.cfg` and e no dey ever read `jpos.xml`; di XML loader dey
find `jpos.xml` only for di working directory, no be inside di jar; and Xerces 2.12.2 for `lib/` dey reject di JAXP
`accessExternalDTD` property wey di loader set. `JposConfig` dey set `jpos.config.regPopulatorClass`
(`JavaxRegPopulator`), `jpos.config.populatorFileURL` (di jar im own `jar:file:…!/jpos.xml`) and
`javax.xml.parsers.SAXParserFactory` (di JDK own) as system properties — never one wey dem don set before, so
`-Djpos.config.populatorFileURL=file:…` still fit choose another `jpos.xml` — and e no dey set anything when
`jpos.properties` dey for di class path or di 4.x loader no dey. Di Zebra SDK `javaPOS114.jar` na one of those cases and
e dey break di registry finish, na why `run.bat` dey keep am out of di class path — check
[Di Zebra natives](#di-zebra-natives). `JposConfigTest` dey check end to end say di registry go then hold di file
scanners.

**Dem dey generate di device picker from dat registry**, dem no dey keep am as list for code:
`ScannerService.logicalNames()` dey read di loaded entries for di `gaia-scanner` thread and keep di ones wey dia device
category na `Scanner` — di two scale entries for `jpos.xml` dey stay outside — for `pickerOrder`: `ZebraAllScanners`
first when di file define am, di rest by alphabet (di registry no dey keep di file order). File wey no get am, dem no
go give am; when nothing load, na only di default dem go offer, so di picker no dey ever empty. `MainView` dey show di
default sharp sharp and e dey fill di list from background thread, e dey keep di choice wey you save. So scanner wey
you add to `jpos.xml` — or whole file wey you name with `-Djpos.config.populatorFileURL` — go show for di picker without
any code change, and `JposConfigTest` dey check di picker against independent read of di bundled file. Di services wey
those entries name na Zebra classes (`com.zebra.jpos.service.scanner.*Factory`), wey still need to dey for di class
path — from di Scanner SDK through `run.bat`, or Zebra service jars wey you drop inside `lib\`.

Dem dey decode scan data as **ISO-8859-1** so each byte go map 1:1 and both di AIM Code ID prefix and every `0x1D` go
survive; dem dey write reports as UTF-8.

Nothing for di service dey compose text for user. Events dey comot as `ScannerListener.Event` constants plus di values
wey dey change, and `MainView` dey supply di wording from di catalogue — if not, di status line go be English for app
wey dem translate 35 ways. Log lines na di exception and dem dey stay English.

### Di platform gate

`ScannerService.isSupportedPlatform()` na one `os.name` test, and `MainView` dey ask am before e build any scanner
control: for macOS and Linux no toggle, no device picker, no Scanner menu (so no <kbd>⌘K</kbd> too), and no state
label. Dem no hide dem — dem no dey ever build dem.

Zebra dey ship im JavaPOS services for Windows only, and nothing dey fail early for other places: di jars dey resolve
and di control dey build, and na `open()` go finally fail because di native layer no dey, after some seconds and one
dialog.

Di rule na about di *services*, no be about JavaPOS. Vendor wey ship cross-platform service go work for here, and to
widen di rule na change for `supportsJavaPos(String)` alone — wey be package-private and dem test am against `os.name`
strings so both answers dey covered for any machine wey dey run di build.

### Symbologies

`Symbologies` dey map all **78** JavaPOS `SCAN_SDT_*` scan-data types to display names, and to di core
`DataCarrierType` where di two vocabularies match. JavaPOS na di one wey fine pass for di two — e dey separate UPC-A
from EAN-13, and plain Data Matrix from GS1 DataMatrix, where AIM dey group whole family under one code character.

Type wey no dey di table still go get name wey you fit use, wey dem find by reflection over `ScannerConst`, because
vendor services dey really report values wey no dey di spec list. One test dey waka through `ScannerConst` and e go
fail if any declared type no get name — wey also dey catch constants wey future JavaPOS go add.

Some carrier mappings, dem leave dem as `UNKNOWN` on purpose instead to guess: straight 2 of 5 (AIM dey split
`]R`/`]S` by di count of start/stop bars, wey JavaPOS no dey report), di four-state postal codes, and TLC-39.

---

## Localization

Labels dey live for one properties file for each language under `src/main/resources/i18n/ui_<CODE>.properties`, dem
dey use di same codes as di core error catalogues (`EN`, `FR`, `PCM`, …). All 35 dey and all 35 carry di same key set.
Dem dey read di files as **UTF-8**, no be di ISO-8859-1 wey `Properties` dey assume, so translations dey store as
something wey you fit read instead of `\uXXXX` escapes.

**Fallback na per key, no be per file** — na wetin make language wey dem translate small small useful from di moment
im first key land. English na di contract: every key must dey dere.

| Layer | Keys | Coverage |
|---|---|---|
| Shared report vocabulary | 31 | all 35 languages |
| Desktop frame — menus, buttons, builder, options, status line, report layout, scanner status, dialog bodies | 114 | all 35 languages |

Six of di shared terms (`generated`, `status`, `dlRole`, `interpretations`, `message`, `level`) no dey used for dis app
report.
Dem keep dem on purpose; di file header talk am.

### Di `--language` argument

`GaiaDesktopApp.languageOverride()` dey read `--language=…` through JavaFX im own `getParameters().getNamed()`, e dey
resolve am with `Languages.fromArgument` — catalogue code, enum name, or BCP 47 tag, capital or small letter no matter
— and e dey hand am to `MainView`.

**Di override na for di run, no be for di file.** `MainView` dey keep di language wey e load inside `savedLanguage` and
na *dat one* e dey write back, until `languageChosenInSession` talk say di user pick language by demself; then dia
choice go save as normal. Dat split na di whole design: kiosk profile or demo shortcut fit open for Spanish without to
rewrite wetin di machine owner go open tomorrow, while real choice wey dem make for di options panel still go stick.

Value wey no name any catalogue, dem go log am with di codes wey dem accept and ignore am. To refuse to start because
of one typo for shortcut go be di worse failure, and na di same reason make `Languages.fromArgument` dey return `null`
instead to throw.

### How to add language

You no need change code.

1. Edit `ui_<CODE>.properties` — every language don already get file.
2. Translate di values. Leave di `{0}` / `{1}` placeholders for where dem dey.
3. Build. Two tests dey enforce di contract: one go fail if di code ask for key wey English no define, di other one if
   any catalogue key set don shift from English.

Di first test dey scan di **sources** for key literals instead to reflect over classes, because di keys dey only as
string literals for di call sites. Na why you must build new message with di key inline —
`strings.format("st.scannerError", …)` — instead to look am up through variable, if not di test no go see am.

### Wetin we no translate on purpose

- di short field names wey dey show for monospace near raw values — di element card `format`, `position`, `length`,
  `raw` and `part`, di tape `AI`, `sep`, `pos` and `len`, and di four cells wey dey locate error (`AI`, `position`,
  `level`, `stage`), wey dia values na enum names wey dem no translate anyway;
- di sample-input names, wey dey name GS1 constructs, and di symbology names, wey be proper nouns;
- di `GE-C084` remediation paragraph — standards writing where half translation worse pass English;
- `ElementBuilder` unknown-AI backstop, wey you no fit reach from di UI: di editor dey check for unknown AIs by imself
  and report dem from di catalogue;
- log lines.

One gap still remain: `ScanSource.label()` dey supply `manual` / `scanner` / `file` / `built` to di history row and di
report masthead for English. To close am need more than one key — `HistoryCell` no get `UiStrings`, and dem dey install
di list cell factory one time, so dem go need refresh di cells when language change too.

---

## How we dey store state

Two files under `~/.gaia-desktop/`, dem keep dem separate because dem dey change for different times — capture log wey
spoil or wey person edit by hand no suppose cost di user dia settings. Override di folder with
`-Dgaia.desktop.home=…`, na how di tests dey stay out of di real home folder.

**`options.properties`** — dem dey write am when option change, and when you exit.

| Key | Meaning |
|---|---|
| `parse.mode`, `parse.language` | Pipeline depth and message language |
| `date.endian`, `date.separator`, `date.month`, `date.year` | How date dey show |
| `check.skipRequires`, `check.skipExcludes` | Escape from structural check |
| `ui.theme` | `LIGHT` or `DARK` |
| `ui.optionsPane` | Options panel open? |
| `ui.scannerDevice` | Last JavaPOS logical name |
| `ui.mode` | `PARSE`, `BUILD` or `COMBINE` |
| `update.check` | Whether di daily update check dey run (default `true`) |
| `update.lastCheck` | When e run last, ISO-8601; if e no dey or you no fit read am, check don reach |
| `update.skipVersion` | Release wey di user choose *Skip dis version* for; dem no go offer am again by imself |

File wey miss or spoil no dey ever kill di app: every value dey fall back by imself, so one bad line cost only dat line
and nothing else. For first launch, when no file dey at all, `SystemDefaults` dey seed di options from di machine
regional settings instead of GAIA built-in defaults — di locale short date pattern dey give di component order, di
separator, and whether month and year dey short.

No ever give `Properties.setProperty` null: e go throw, and `persistOptions()` dey run from UI event handlers where
exception dey escape enter di FX thread. Na real crash wey dis app don already get.

**`captures.tsv`** — dem dey write am when history change, and when you exit. Di newest dey first, e stop for **500**
entries.

```
captured(ISO)              <TAB> source  <TAB> symbology   <TAB> input
2026-08-09T14:22:05.123    SCANNER       Data Matrix       0109506000134352\x1D10LOT-001
```

Na only wetin dem no fit calculate again dem dey store. Dem **no dey** serialise di `ParseResult` — dem dey derive am
again by parsing di input again when e load, so history wey dem restore dey show di options wey dey work *now*. Inputs
dey always contain FNC1 and any other thing wey scanner fit bring out, so dem dey escape di input field (`\\`, `\t`,
`\n`, `\r`, and `\xNN` for everything else under `0x20`) and dem dey read and write di file as ISO-8859-1, byte by byte.

Files wey dem write before dem start to record di date get only clock time; dem still dey load, with today date,
because dem no ever write di real date down.

---

## Testing

```bash
(cd gaia-desktop && mvn test)
```

198 tests, on purpose **dem no need di JavaFX toolkit**, wey make dem fast and headless. Dem cover di report renderer,
di build logic and AI lookups, di `{GS}` conversion, di defaults wey come from locale, both stores (including round
trip wey carry `0x1D` and files wey dem spoil on purpose), di symbology table, di scanner platform rule, di version
stub, and di catalogues.

Three of dem worth to know because dem dey defend contracts, no be functions:

| Test | E go fail when |
|---|---|
| `CatalogueCoverageTest` | Di code ask for key wey `ui_EN.properties` no define, or any of di 35 files don shift from di English key set |
| `SymbologiesTest` | Any `SCAN_SDT_*` constant wey JavaPOS declare no get display name — including one wey JavaPOS upgrade add |
| `ScannerServiceTest` | Di platform rule change im answer for Windows or for any other thing |

`UiStrings` dey forgive on purpose for runtime — unknown key dey show as di key imself instead to throw for middle of
layout — na exactly why di catalogue test must dey.

**Wetin e no cover:** anything wey need live toolkit — focus behaviour, di mode toggle, table editing, di language
switch, and both crashes wey dem describe under [Threading](#threading). Dem verify those ones by driving di real
window, but if dem spoil again di build no go fail. To add TestFX or Monocle go close dat gap.

---

## When build get wahala

| Wetin you see | Cause and how to fix am |
|---|---|
| `package tools.pantheum.gaia does not exist` | Di core never install. `(cd gaia && mvn install -DskipTests)` |
| *"JavaFX runtime components are missing"* | Di jar `Main-Class` must be `Launcher`, no be `GaiaDesktopApp` |
| *"Unsupported JavaFX configuration: classes were loaded from unnamed module"* for start-up | Dem expect am and e no harm anything — JavaFX dey note say e dey for di class path. Na exactly why `Launcher` dey |
| Di jar no gree start, `NoClassDefFoundError` | Di default jar carry only dis module classes; `dist/lib/` must dey near am. Use `-Pfat` for one self-contained file |
| `Artifact has not been packaged yet … MDEP-187` for Eclipse | m2e workspace resolution. Check di note under [Packaging](#packaging) |
| Duplicate `… 2.properties` files dey show for build | Di project dey inside folder wey iCloud dey sync and di sync make conflict copies for middle of di build. Always `mvn clean package`; better still, comot `target/` from sync |
| Menu item dey show key like `act.hri` | Dat key no dey `ui_EN.properties`. Di build for don catch am — check [Testing](#testing) |

---

## Known limits and work wey remain

- **No headless UI tests.** Check [Testing](#testing).
- **Artifacts wey be for one platform.** Both packagings dey bundle one platform JavaFX natives; build one for each
  target with `-Dfx.platform` (any machine fit build all of dem). None of dem bundle Java runtime — dat one need
  `jpackage`, wey dey build only for di OS wey e dey run on.
- **Scanner input na Windows only.** Check [Di platform gate](#di-platform-gate).
- **`ScanSource.label()` no dey localized.** Check [Wetin we no translate on
  purpose](#wetin-we-no-translate-on-purpose).
- **`UiState.mode` na `String`** (`"PARSE"` / `"BUILD"`) while `Theme` wey dey near am na enum, so di literal dey
  show for many places. Enum go remove dem.
- **Digital Link target na only domain and path prefix.** `BuilderDigitalLinkConfig` also support extra query
  parameters and fragment; di editor no dey show dem.
- **AI pairs wey no fit dey together (`GE-S006`) no dey flagged for di editor** di way unknown and duplicate AIs dey —
  di build go just fail.
- **Di core error catalogue dey double quotes** (`value ''09506000134350''`), wey dey show as e be for build failures
  and di report. E be like `MessageFormat` quote-escaping wey dem no dey unescape; di fix belong to di core.
