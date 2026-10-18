# GAIA Desktop — Developer Guide

How the module is built, laid out and tested. For what the application does from the outside,
see the [User Guide](GaiaDesktop-User.md).

## Table of Contents

1. [The module at a glance](#the-module-at-a-glance)
2. [Build and run](#build-and-run)
   - [Packaging](#packaging)
3. [Architecture](#architecture)
   - [The capture funnel](#the-capture-funnel)
   - [Threading](#threading)
   - [Rebuild versus re-label](#rebuild-versus-re-label)
4. [The {GS} token](#the-gs-token)
5. [Scanner integration](#scanner-integration)
   - [The platform gate](#the-platform-gate)
   - [Symbologies](#symbologies)
6. [Localization](#localization)
   - [The `--language` argument](#the---language-argument)
   - [Adding a language](#adding-a-language)
   - [What is deliberately untranslated](#what-is-deliberately-untranslated)
7. [Stored state formats](#stored-state-formats)
8. [Testing](#testing)
9. [Troubleshooting the build](#troubleshooting-the-build)
10. [Known limitations and open work](#known-limitations-and-open-work)

---

## The module at a glance

```
tools.pantheum:gaia-desktop:1.0.0   Java 21 · JavaFX 21
        └── depends on tools.pantheum:gaia:1.1.0
```

A client of the core library and nothing more. It embeds **no GS1 knowledge**: every AI
definition, validation rule, error message and localized description comes from
[`GaiaParser`](../../gaia/docs/GaiaParser.md) and
[`GaiaBuilder`](../../gaia/docs/GaiaBuilder.md). In particular the AI list is never copied into
this module — the builder's picker reads the core's own registry, so it cannot offer an AI the
builder would reject.

**Entry points:** `tools.pantheum.gaia.desktop.GaiaDesktopApp` (the JavaFX `Application`) and
`tools.pantheum.gaia.desktop.Launcher` (the packaged jar's `Main-Class`).

The module is a leaf: nothing depends on it, and it depends on no other downstream module. The
two classes it started from were **copied** out of `gaia-demo` rather than depended on —
`Symbologies` is still kept in step with its twin there by hand, and both carry a note saying
so.

---

## Build and run

- **Java 21** to run, and a JDK 21 or newer to build — the module compiles with `release=21`.
  JavaFX 21 is the matching LTS line. The core jar is still Java 11 bytecode and runs unchanged
  on 21.
- **Maven** on the `PATH`. There is no wrapper in this repo.
- **The core installed first.** This module resolves `gaia:1.1.0` from `~/.m2`; a stale or
  absent core jar is the usual cause of `package tools.pantheum.gaia does not exist`.

```bash
(cd gaia && mvn -q install -DskipTests)      # publish the core
(cd gaia-desktop && mvn javafx:run)          # run from source
(cd gaia-desktop && mvn clean package)       # target/dist/gaia-desktop-<version>-<platform>/ — jar (~290 KB), lib/, run.sh (run.bat on win)
(cd gaia-desktop && mvn clean -Pfat package) # one self-contained jar, ~50 MB
(cd gaia-desktop && mvn package -Dfx.platform=win)   # another platform; see Packaging
java -jar gaia-desktop/target/dist/gaia-desktop-1.0.0-mac-aarch64/gaia-desktop.jar
java -jar gaia-desktop/target/dist/gaia-desktop-1.0.0-mac-aarch64/gaia-desktop.jar --language=FR
```

`--language` is the only command-line argument, and the only way to influence start-up other
than `-Dgaia.desktop.home=…`, which points the whole profile somewhere else.

`mvn javafx:run` is the development path: the plugin puts JavaFX on the **module path**, so the
toolkit starts as a named module and `GaiaDesktopApp` can be launched directly. A packaged jar
cannot do that — everything is on the class path — which is why its `Main-Class` is the
`Launcher` shim. An `Application` subclass as `Main-Class` in that layout fails with *"JavaFX
runtime components are missing"*.

Always use `clean` — once, before building a set of platforms, since it empties `target/dist/`
of all of them. See [Troubleshooting the build](#troubleshooting-the-build) for why.

### Packaging

Everything shippable is assembled under `target/dist/gaia-desktop-<version>-<platform>/` and nothing else is, so the
directory can be zipped or copied as a set without picking artefacts out of `target/` next to
`classes/`, `maven-status/` and the rest of the build's scratch. `${dist.dir}` names it once;
every plugin that writes an artefact points at it.

#### One build per platform

The JavaFX jars carry native code, so each distribution is for one platform, named by the
`fx.platform` property as an OpenJFX classifier: `mac-aarch64`, `mac`, `win`, `linux` or
`linux-aarch64`. Five OS-activated `fx-*` profiles set it to the building machine's own, which is
all a plain `mvn package` or `mvn javafx:run` needs; `-Dfx.platform=…` outranks them to build any
other, from any machine — the JavaFX jars are only downloads. `${dist.dir}` is
`target/dist/${fx.platform}`, so one platform's build never overwrites another's.

```bash
(cd gaia-desktop && mvn -q clean package)    # this machine's platform, with the tests
for p in win linux mac mac-aarch64; do (cd gaia-desktop && mvn -q package -DskipTests -Dfx.platform=$p); done
```

Two pom details make that work, and both look removable:

- **Every JavaFX module is a direct dependency** — base, graphics, controls, media and web — each
  with `<classifier>${fx.platform}</classifier>` and an `org.openjfx:*` exclusion. OpenJFX's poms
  pick their transitive jars by the *building machine's* OS, so leaving graphics or base to arrive
  transitively puts the Mac natives into a Windows build next to the Windows ones.
- **The property is `fx.platform`, not `javafx.platform`.** Set from outside, OpenJFX's own
  property is redefined in terms of itself by its parent pom; Maven rejects that pom as invalid and
  quietly drops the platform jars, leaving a build with no JavaFX in it.

Both layouts write the same path, `target/dist/gaia-desktop-<version>-<platform>/gaia-desktop.jar`, and exactly one of them runs
per build. `${jar.phase}`, `assembly.skipAssembly` and `mdep.skip` are what keep them from both
claiming it.

| | Default | `-Pfat` |
|---|---|---|
| `gaia-desktop.jar` | ~290 KB — this module's classes and resources only | ~50 MB — dependencies unpacked into it |
| `dist/gaia-desktop-<version>-<platform>/lib/` | 17 jars, named one by one on the manifest `Class-Path` | — |
| Ship | the jar **and** `lib/` beside it | one file |
| Also in `dist/gaia-desktop-<version>-<platform>/` | `run.bat` (win) or `run.sh` (others) | the same |
| And | `readme.html` and `docs/` (every platform) | the same |

Every distribution also carries the guides: `docs/` copied with its layout kept, so the links
between guides and translations still resolve, minus the translation tooling (`check-i18n.py`,
`i18n-manifest.json`, `i18n-plan.md`); and `readme.html` at the root, an index linking to each
of them, from `src/main/dist/readme.html` with `@VERSION@` filtered to the pom version. Both are
written by the `pick-launch-script` antrun step, which deletes `docs/` first so a removed guide
does not linger. The readme's links are written out by hand — a guide renamed or a translation
added under `docs/` needs its row there too.

The manifest `Class-Path` and `lib/` are two halves of one thing: the jar names
`lib/<artifact>-<version>.jar` for every runtime dependency, and `copy-dependencies` fills
`lib/` with exactly those names. That is what makes `java -jar` work.

#### The macOS app and the distribution zip

One antrun execution, `mac-app-and-zip`, ends the `package` phase. The antrun plugin is declared
**after** the jar and assembly plugins for that reason — Maven runs a phase's executions in plugin
declaration order — so the jar exists, in either packaging, when it runs. (Its other execution,
`pick-launch-script`, is in `prepare-package` and unaffected.)

For `mac` and `mac-aarch64` it first builds `GAIA Desktop.app` in the distribution directory:
`Info.plist` (with `@VERSION@` filtered to the pom version), the launcher `Contents/MacOS/gaia-desktop`
(made executable) and `gaia-desktop.icns`, all from `src/main/macos/`, then **moves**
`gaia-desktop.jar` and `lib/` into `Contents/Java`, so the bundle stands alone. `run.sh` checks
there when no jar sits beside it. The launcher finds Java by version (`java_home -v 21+`, then
Homebrew's openjdk — Finder launches see no `JAVA_HOME` or `PATH`), shows a dialog via `osascript`
when there is none, `cd`s to `~/.gaia-desktop` so logback's relative `logs/` is writable, and
passes `-Xdock:name`/`-Xdock:icon`. macOS associates the running `java` with the bundle (its
bundle id and Dock icon are the app's); the menu bar reads GAIA Desktop.

Then, for every platform, it zips the directory to
`target/dist/gaia-desktop-<version>-<platform>.zip` under a folder of the same name, with Ant's
`<zip>`: `run.sh` and `Contents/MacOS/**` as a separate file set with `filemode="755"`, or they
would unzip unrunnable. The zip is **not attached**: it is for GitHub releases, and the four
platforms come from four builds, which Maven Central — one immutable upload per version — could
never take as one release.

`project.build.outputTimestamp` is set to the build time. The archivers in maven-jar-plugin 3.5
and maven-assembly-plugin 3.8 otherwise date every entry 1 February 1980.

The app icon is drawn by `tools/icon/GaiaIcon.java` (not part of the build; its header has the
`sips`/`iconutil` steps). Its outputs are checked in: `src/main/macos/gaia-desktop.icns` for the
bundle, and `src/main/resources/icons/gaia-desktop-{16..256}.png`, which `GaiaDesktopApp` sets as
the window icon (Windows and Linux; macOS ignores window icons).

#### The launch scripts

`run.sh` and `run.bat` come from `src/main/scripts/`, copied into `dist/gaia-desktop-<version>-<platform>/` unfiltered —
`run.sh` is full of `${...}` shell expansions that Maven filtering would be free to rewrite. An
antrun step then keeps only the one the platform runs — `run.bat` for `win`, `run.sh` for every
other — and sets the executable bit on `run.sh`, since `copy-resources` does not carry it. It
decides from the resolved `${fx.platform}` rather than through profiles, because profile
activation cannot see a value another profile set: a Linux build made on Windows would otherwise
get the host's script.

They **do not rely on the manifest**. Each builds the class path itself, `gaia-desktop.jar`
followed by every `.jar` in `lib/`, named one at a time rather than left to a `lib/*` wildcard
so that a path containing spaces stays a single entry and the count of what was added is real.
Launching with `-cp` is why they name `tools.pantheum.gaia.desktop.Launcher` explicitly: `-cp`
does not read the manifest's `Main-Class`. It **does** still follow its `Class-Path` — a jar on
`-cp` brings the jars its manifest names, searched straight after it — so every `lib/` jar loads
whether or not a script lists it, and leaving one off the script's class path does not keep it
out of the JVM. The scripts list them anyway, so a jar whose manifest is stale, hand-edited or
short an entry still starts. The
same script serves both packagings: the `-Pfat` jar has no `lib/` to add and needs none, which
is also why an empty `lib/` is a note rather than an error.

#### The Zebra natives

The build ships nothing of Zebra's — no DLLs, no service jars. On Windows they come from Zebra's
Scanner SDK installed on the machine: `run.bat` adds its `Jpos*.jar` service jars and `bin\` to the
class path and `bin\` to the `PATH`, where `System.loadLibrary` finds `CSJPOSScanner.dll` /
`CSJPOSScanner64.dll` (`java.library.path` defaults to the `PATH`). The JavaPOS API and its
configuration loader always come from `lib\javapos-*.jar`, SDK or not.

**`run.bat` never adds the SDK's own JavaPOS jar**, `External\JavaPOS-1.14.0-Dist\javaPOS114.jar`.
It once did, leaving `lib\javapos-*.jar` off to avoid two copies of the API — but the manifest
`Class-Path` loaded them regardless (see above), so the JVM got both: the 4.x loader classes from
`lib\`, with `javaPOS114.jar`'s `jpos/res/jpos.properties` and `jcl.xsd` beside them. The
properties file makes [`JposConfig`](#scanner-integration) stand aside, and that `jcl.xsd`
accepts JavaPOS versions only up to 1.10, so every entry in `jpos.xml` (1.12–1.14) failed
validation, the registry stayed empty, and every connect failed with error 109 before the
scanner was ever reached. Having the jar on the class path at all is enough for that, even behind
`lib\`'s jars. Zebra's service jars run unchanged on the 1.14 API from `lib\` — open, claim and
scan are verified against a real scanner that way. DLLs dropped beside `run.bat` by hand are still
honoured: the script then sets `-Djava.library.path=<dist>;%PATH%`, appending the `PATH` because
the property replaces the default search path outright.

`mvn javafx:run` does none of this, so a scanner claim from a development run on Windows needs
the SDK's jars on the class path and its `bin\` on the `PATH` by other means; run from `dist/`
for that.

Beyond the class path they cover what `java -jar` does not: resolving their own directory so
they work from any working directory, preferring `JAVA_HOME` over `PATH`, and rejecting a
pre-11 JVM with a sentence naming the remedy instead of `UnsupportedClassVersionError`.
`GAIA_DESKTOP_OPTS` passes JVM options through. `run.bat` uses `java.exe`, not `javaw.exe`, so
the console stays open — Windows is the only platform where the scanner runs, and its status
and JavaPOS errors go there.

`run.bat` also chooses its Java, JavaFX and Zebra services from what it finds — a bundled `jdk*\`,
a `javafx-sdk*\lib\` on the module path, Zebra's Scanner SDK at `ZEBRA_HOME` — leaving the `lib\`
copies of JavaFX off the class path when it uses an SDK's, and passes the
JDK's SAX parser, Windows proxies and trust store, and `--enable-native-access` on Java 17+. The
README's [What `run.bat` adds on Windows](../README.md#what-runbat-adds-on-windows) has the table.
Keep the file's CRLF line endings: `cmd.exe` can miss a `call :label` in an LF-only batch file.
`run.sh` passes `--enable-native-access=ALL-UNNAMED` as well — JavaFX loads its own natives from
`lib/`, and with JavaFX on the class path `ALL-UNNAMED` is the module that covers it — skipped, as
in `run.bat`, only when the Java version cannot be read.

Either layout carries the JavaFX natives **of one platform**, `${fx.platform}`, so neither is
portable across OS or architecture. Build once per target — see [One build per
platform](#one-build-per-platform).

> **Eclipse.** The `copy-dependencies` execution carries an `<?m2e ignore?>` instruction. m2e
> resolves an open workspace project to its `target/classes` folder rather than a jar, and
> `copy-dependencies` refuses to copy an unpackaged artifact (MDEP-187), which would fail the
> auto-build whenever the `gaia` project is open beside this one. The consequence is that an
> IDE-only build does not fill `target/dist/gaia-desktop-<version>-<platform>/lib/`; run `mvn package` before using the jar itself.

---

## Architecture

| Package | Holds |
|---|---|
| `desktop` | `GaiaDesktopApp`, `Launcher`, `Theme`, `GaiaDesktopVersion` |
| `desktop.ui` | `MainView` (the window), `BuilderPane`, `OptionsPane`, `HistoryCell`, `EditCell` |
| `desktop.build` | `ElementBuilder` (JavaFX-free build logic), `AiPair` (one editor row) |
| `desktop.parse` | `ParseOptions` (observable `ParseConfig`), `SystemDefaults`, `Languages`, `GsToken` |
| `desktop.scanner` | `ScannerService` (JavaPOS), `ScannerListener`, `Symbologies` |
| `desktop.html` | `GaiaDesktopReport` — the report renderer, JavaFX-free and unit-tested without the toolkit |
| `desktop.i18n` | `UiStrings` — the label catalogue |
| `desktop.store` | `AppPaths`, `OptionsStore`, `CaptureStore` |

### The capture funnel

Every input — typed, pasted, opened from a file, scanned, or built — reaches the parser through
one method, `MainView.capture(String, ScanSource, String)`.

That is the single design decision the app is built around. A build needs no report of its own,
no history of its own and no export path of its own, because after the funnel a built string is
indistinguishable from a scanned one. Anything new that produces an input should go through it
rather than beside it.

### Threading

| Thread | Runs |
|---|---|
| JavaFX application thread | All parsing, rendering and UI. Parsing is sub-millisecond once the AI tables are warm, so it needs no worker |
| `gaia-scanner` | Every call into the scanner — open, claim, enable, disable, release, close. One long-lived thread owned by `ScannerService`; see [Scanner integration](#scanner-integration) |
| JavaPOS event thread | `ScannerService` callbacks. `MainView.FxScannerListener` hops every one across with `Platform.runLater` |
| Background | Waiting on scanner `connect()` / `disconnect()` (the claim can take up to 5 s), restoring the saved history at start-up — which doubles as the parser warm-up — and the update check (`gaia-update-check`, one GitHub request, result posted back with `Platform.runLater`) |

**Never mutate a control's selection, or destroy the control, from inside its own selection
event.** Three separate crashes in this app came from exactly that: resetting the AI picker
inside the popup's own `clearAndSelect` (an `IndexOutOfBoundsException` out of
`ListViewBehavior`), and rebuilding the panel that owns the language combo from inside that
combo's own handler. Both are now deferred with `Platform.runLater`, and the reasons are
recorded at both call sites.

### Rebuild versus re-label

A language change **rebuilds** the chrome from the catalogue — menus, toolbar, options panel —
which is safe because none of it owns state: `OptionsPane` is a two-way view of `ParseOptions`,
so the values live in the model and a fresh panel picks them straight back up.

Controls that hold **user data** are fields, re-labelled in place instead:
`BuilderPane.retext(UiStrings)` keeps the AI rows, the Digital Link fields and the selection.
Rebuilding it would discard work in progress — which it once did.

---

## The {GS} token

A JavaFX `TextField` silently discards every character below `0x20`
(`TextInputControl.filterInput`). Not "renders it oddly" — removes it. Typing, pasting,
`setText` and `insertText` are all affected, so `setText("01…" + GS + "10LOT")` yields
`"01…10LOT"`: an element string whose meaning has quietly changed, because the variable-length
AI now swallows what followed it.

`desktop.parse.GsToken` is the whole answer. The separator travels through the UI as the
literal text `{GS}` and becomes `0x1D` again on the way to the parser. The token is not
invented here — it is exactly what the core's `ASCIIRevealerUtils.printable` renders `0x1D` as,
so a value copied out of a report pastes straight back into the input bar.

A `TextFormatter` filter on the input field catches every route text can arrive by — typing,
⌘V, the context menu, drag and drop, `setText` — which is why the rewrite lives there rather
than in each handler.

The scanner path never goes through `GsToken`: scans reach the parser as bytes.

---

## Scanner integration

`ScannerService` owns the JavaPOS `Scanner` control: open → claim → enable, re-arming the data
event after every scan. Logical device names come from `src/main/resources/jpos.xml`.

**Every device call runs on one thread, `gaia-scanner`, which lives as long as the service** —
never on the FX thread, and never on a thread that ends after the call. Zebra's service reaches
the scanner through JNI over the CoreScanner COM API, and COM binds what it sets up to the
calling thread's apartment: open on a thread that then exits and the scanner claims but stays
silent; close on another thread and the wrong apartment is uninitialized. Closing on the FX thread
is the worst case, because JavaFX calls `OleInitialize` there and registers every window's drop
target against it — a `CoUninitialize` on it kills drag and drop app-wide. This is JavaFX on
Windows, not any one JavaFX version. `connect()` and `disconnect()` submit to that thread and
wait, so `MainView` calls both from a short-lived background thread; the pattern is the viewer's
`JposScannerSession`, where the drag-and-drop failure was first found.

Two rules keep a misbehaving device from wedging the connection. **Teardown never stops early:**
`closeQuietly` disables, releases and closes as separate steps, releases only when the claim is
held, and always attempts `close()` — after a failed claim, `release()` throws "not claimed", and
when the steps shared one `try` that skipped the close and left the device open in Zebra's
service. **The data event is re-armed in a `finally`:** JavaPOS disables it after every event,
so a read or handler that throws must still re-arm, or the scanner goes silent until a
reconnect. `ScannerLifecycleTest` drives both against a scripted `jpos.Scanner`.

**`JposConfig` points JavaPOS at the bundled `jpos.xml`** before the first open. With the Maven
JavaPOS jars (`javapos-config-loader` 4.x) and no `jpos/res/jpos.properties` on the class path,
every open otherwise fails with *"Service does not exist in loaded JCL registry"* (error 109),
for three separate reasons: the service manager falls back to `SimpleRegPopulator`, which reads a
serialized `jpos.cfg` and never `jpos.xml`; the XML loader looks for `jpos.xml` in the working
directory only, not inside the jar; and Xerces 2.12.2 in `lib/` rejects the JAXP
`accessExternalDTD` property the loader sets. `JposConfig` sets `jpos.config.regPopulatorClass`
(`JavaxRegPopulator`), `jpos.config.populatorFileURL` (the jar's own `jar:file:…!/jpos.xml`) and
`javax.xml.parsers.SAXParserFactory` (the JDK's) as system properties — never one already set, so
`-Djpos.config.populatorFileURL=file:…` still selects another `jpos.xml` — and sets nothing when
a `jpos.properties` is on the class path or the 4.x loader is not. The Zebra SDK's
`javaPOS114.jar` is such a case and breaks the registry outright, which is why `run.bat` keeps it
off the class path — see [The Zebra natives](#the-zebra-natives). `JposConfigTest` checks end to end that the registry
then holds the file's scanners.

**The device picker is generated from that registry**, not kept as a list in code:
`ScannerService.logicalNames()` reads the loaded entries on the `gaia-scanner` thread and keeps
those whose device category is `Scanner` — the two scale entries in `jpos.xml` stay out — in
`pickerOrder`: `ZebraAllScanners` first when the file defines it, the rest alphabetically (the
registry does not keep file order). A file without it is not given it; with nothing loaded the
default alone is offered, so the picker is never empty. `MainView` shows the default at once and
fills in the list from a background thread, keeping the saved choice. So a scanner added to
`jpos.xml` — or a whole file named with `-Djpos.config.populatorFileURL` — appears in the picker
with no code change, and `JposConfigTest` checks the picker against an independent read of the
bundled file. The services those entries name are Zebra's
classes (`com.zebra.jpos.service.scanner.*Factory`), which still have to be on the class path —
from the Scanner SDK via `run.bat`, or Zebra's service jars dropped into `lib\`.

Scan data is decoded as **ISO-8859-1** so each byte maps 1:1 and both the AIM Code ID prefix
and every `0x1D` survive; reports are written as UTF-8.

Nothing in the service composes text for a user. Events go out as `ScannerListener.Event`
constants plus the values that vary, and `MainView` supplies the wording from the catalogue —
otherwise the status line would be English in an application translated 35 ways. Log lines are
the exception and stay English.

### The platform gate

`ScannerService.isSupportedPlatform()` is one `os.name` test, and `MainView` asks it before
building any scanner control: on macOS and Linux there is no toggle, no device picker, no
Scanner menu (and so no <kbd>⌘K</kbd>), and no state label. They are not hidden — they are
never constructed.

Zebra ships its JavaPOS services for Windows only, and nothing fails early elsewhere: the jars
resolve and the control constructs, and it is `open()` that finally fails on the missing native
layer, several seconds and one dialog later.

The rule is about the *services*, not about JavaPOS. A vendor shipping a cross-platform service
would work here, and widening the rule is a change to `supportsJavaPos(String)` alone — which
is package-private and tested against `os.name` strings so both answers are covered on
whichever machine runs the build.

### Symbologies

`Symbologies` maps all **78** JavaPOS `SCAN_SDT_*` scan-data types to display names, and to the
core's `DataCarrierType` where the two vocabularies line up. JavaPOS is the finer of the two —
it separates UPC-A from EAN-13, and plain Data Matrix from GS1 DataMatrix, where AIM groups a
whole family under one code character.

A type outside the table still gets a usable name, found by reflection over `ScannerConst`,
because vendor services do report values outside the spec list. A test walks `ScannerConst` and
fails if any declared type has no name — which also catches constants a future JavaPOS adds.

Some carrier mappings are deliberately left `UNKNOWN` rather than guessed: straight 2 of 5
(AIM splits `]R`/`]S` on start/stop bar count, which JavaPOS does not report), the four-state
postal codes, and TLC-39.

---

## Localization

Labels live in one properties file per language under
`src/main/resources/i18n/ui_<CODE>.properties`, using the same codes as the core's error
catalogues (`EN`, `FR`, `PCM`, …). All 35 exist and all 35 carry an identical key set. Files
are read as **UTF-8**, not the ISO-8859-1 `Properties` assumes, so translations are stored
readably rather than as `\uXXXX` escapes.

**Fallback is per key, not per file** — which is what lets a partly translated language be
useful the moment its first key lands. English is the contract: every key must exist there.

| Layer | Keys | Coverage |
|---|---|---|
| Shared report vocabulary, lifted from `gaia-demo`'s `GaiaHtmlReport` | 31 | all 35 languages |
| Desktop chrome — menus, buttons, builder, options, status line, report layout, scanner status, dialog bodies | 114 | all 35 languages |

Six of the shared terms (`generated`, `status`, `dlRole`, `interpretations`, `message`,
`level`) are not used by this app's report. They are kept so the shared block stays a faithful
copy of the demo vocabulary; the file header says so.

### The `--language` argument

`GaiaDesktopApp.languageOverride()` reads `--language=…` through JavaFX's own
`getParameters().getNamed()`, resolves it with `Languages.fromArgument` — catalogue code, enum
name, or BCP 47 tag, case-insensitive — and hands it to `MainView`.

**The override is for the run, not for the file.** `MainView` keeps the language it loaded in
`savedLanguage` and writes *that* back, until `languageChosenInSession` says the user picked a
language themselves; then their choice persists as normal. That split is the whole design: a
kiosk profile or a demo shortcut can open in Spanish without rewriting what the machine's owner
opens in tomorrow, while a real choice made in the options panel still sticks.

A value naming no catalogue is logged with the accepted codes and ignored. Refusing to start
over a typo in a shortcut would be the worse failure, and `Languages.fromArgument` returns
`null` rather than throwing for the same reason.

### Adding a language

No code change is needed.

1. Edit `ui_<CODE>.properties` — every language already has a file.
2. Translate the values. Leave `{0}` / `{1}` placeholders in place.
3. Build. Two tests enforce the contract: one fails if the code asks for a key English does not
   define, the other if any catalogue's key set drifts from English.

The first test scans the **sources** for key literals rather than reflecting over classes,
because the keys only exist as string literals at the call sites. That is why a new message
must be built with the key inline — `strings.format("st.scannerError", …)` — rather than
looked up through a variable, or the test cannot see it.

### What is deliberately untranslated

- the terse field names set in monospace beside raw values — the element card's `format`,
  `position`, `length`, `raw` and `part`, the tape's `AI`, `sep`, `pos` and `len`, and the four
  cells locating an error (`AI`, `position`, `level`, `stage`), whose values are untranslated
  enum names anyway;
- the sample-input names, which name GS1 constructs, and the symbology names, which are proper
  nouns;
- the `GE-C084` remediation paragraph — standards prose where a half-translation is worse than
  English;
- `ElementBuilder`'s unknown-AI backstop, which is unreachable from the UI: the editor checks
  for unknown AIs itself and reports them from the catalogue;
- log lines.

One gap remains: `ScanSource.label()` supplies `manual` / `scanner` / `file` / `built` to the
history row and the report masthead in English. Closing it needs more than a key — `HistoryCell`
has no `UiStrings`, and the list's cell factory is installed once, so cells would have to be
refreshed on a language change too.

---

## Stored state formats

Two files under `~/.gaia-desktop/`, kept apart because they change on different clocks — a
corrupt or hand-edited capture log should not cost the user their settings. Override the
directory with `-Dgaia.desktop.home=…`, which is how the tests stay out of the real home
directory.

**`options.properties`** — written when an option changes, and on exit.

| Key | Meaning |
|---|---|
| `parse.mode`, `parse.language` | Pipeline depth and message language |
| `date.endian`, `date.separator`, `date.month`, `date.year` | Date rendering |
| `check.skipRequires`, `check.skipExcludes` | Structural-check escapes |
| `ui.theme` | `LIGHT` or `DARK` |
| `ui.optionsPane` | Panel open? |
| `ui.scannerDevice` | Last JavaPOS logical name |
| `ui.mode` | `PARSE`, `BUILD` or `COMBINE` |
| `update.check` | Whether the daily update check runs (default `true`) |
| `update.lastCheck` | When it last ran, ISO-8601; absent or unreadable means a check is due |
| `update.skipVersion` | A release the user chose *Skip this version* for; not offered again automatically |

A missing or damaged file is never fatal: every value falls back individually, so one bad line
costs that line and nothing else. On a first launch, with no file at all, options are seeded
from the machine's regional settings by `SystemDefaults` rather than from GAIA's built-in
defaults — the locale's short date pattern gives the component order, separator, and whether
month and year are abbreviated.

Never hand `Properties.setProperty` a null: it throws, and `persistOptions()` runs from UI
event handlers where an exception escapes onto the FX thread. That is a real crash this app
has already had.

**`captures.tsv`** — written when the history changes, and on exit. Newest first, capped at
**500** entries.

```
captured(ISO)              <TAB> source  <TAB> symbology   <TAB> input
2026-08-09T14:22:05.123    SCANNER       Data Matrix       0109506000134352\x1D10LOT-001
```

Only what cannot be recomputed is stored. The `ParseResult` is **not** serialised — it is
re-derived by parsing the input again on load, so a restored history reflects the options in
force *now*. Inputs routinely contain FNC1 and whatever else a scanner emits, so the input
field is escaped (`\\`, `\t`, `\n`, `\r`, and `\xNN` for everything else below `0x20`) and the
file is read and written as ISO-8859-1, byte for byte.

Files written before the date was recorded hold a bare clock time; they still load, dated to
today, because the real date was never written down.

---

## Testing

```bash
(cd gaia-desktop && mvn test)
```

96 tests, deliberately **free of the JavaFX toolkit**, which keeps them fast and headless. They
cover the report renderer, the build logic and AI lookups, the `{GS}` conversion, the
locale-derived defaults, both stores (including a `0x1D`-bearing round trip and deliberately
damaged files), the symbology table, the scanner platform rule, the version stub, and the
catalogues.

Three are worth knowing about because they defend contracts rather than functions:

| Test | Fails when |
|---|---|
| `CatalogueCoverageTest` | The code asks for a key `ui_EN.properties` does not define, or any of the 35 files drifts from the English key set |
| `SymbologiesTest` | Any `SCAN_SDT_*` constant JavaPOS declares has no display name — including one a JavaPOS upgrade adds |
| `ScannerServiceTest` | The platform rule changes answer for Windows or for anything else |

`UiStrings` is deliberately forgiving at runtime — an unknown key renders as the key itself
rather than throwing mid-layout — which is exactly why the catalogue test has to exist.

**Not covered:** anything needing a live toolkit — focus behaviour, the mode toggle, table
editing, the language switch, and both of the crashes described under
[Threading](#threading). Those were verified by driving the real window, but a regression in
them would not fail the build. Adding TestFX or Monocle would close that gap.

---

## Troubleshooting the build

| Symptom | Cause and fix |
|---|---|
| `package tools.pantheum.gaia does not exist` | The core is not installed. `(cd gaia && mvn install -DskipTests)` |
| *"JavaFX runtime components are missing"* | The jar's `Main-Class` must be `Launcher`, not `GaiaDesktopApp` |
| *"Unsupported JavaFX configuration: classes were loaded from unnamed module"* at start-up | Expected and harmless — JavaFX noting it is on the class path. Exactly why `Launcher` exists |
| The jar will not start, `NoClassDefFoundError` | The default jar carries only this module's classes; `dist/lib/` must sit beside it. Use `-Pfat` for a single self-contained file |
| `Artifact has not been packaged yet … MDEP-187` in Eclipse | m2e workspace resolution. See the note under [Packaging](#packaging) |
| Duplicate `… 2.properties` files appear in a build | The project sits in an iCloud-synced folder and sync produced conflict copies mid-build. Always `mvn clean package`; better, exclude `target/` from sync |
| A menu item shows a key like `act.hri` | That key is missing from `ui_EN.properties`. The build should have caught it — see [Testing](#testing) |

---

## Known limitations and open work

- **No headless UI tests.** See [Testing](#testing).
- **Platform-specific artifacts.** Both packagings bundle one platform's JavaFX natives; build
  one per target with `-Dfx.platform` (any machine can build all of them). None bundles a Java
  runtime — that needs `jpackage`, which only builds for the OS it runs on.
- **Scanner input is Windows-only.** See [The platform gate](#the-platform-gate).
- **`ScanSource.label()` is not localized.** See [What is deliberately
  untranslated](#what-is-deliberately-untranslated).
- **`UiState.mode` is a `String`** (`"PARSE"` / `"BUILD"`) while `Theme` beside it is an enum,
  so the literal appears in several places. An enum would remove them.
- **Digital Link target is domain and path prefix only.** `BuilderDigitalLinkConfig` also
  supports extra query parameters and a fragment; the editor does not expose them.
- **Mutually exclusive AI pairs (`GE-S006`) are not flagged in the editor** the way unknown and
  duplicate AIs are — the build simply fails.
- **The core's error catalogue doubles quotes** (`value ''09506000134350''`), which shows up
  verbatim in build failures and the report. It looks like `MessageFormat` quote-escaping that
  is not being unescaped; the fix belongs in the core.
