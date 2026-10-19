# GAIA-Desktop

A JavaFX desktop front-end for the GAIA GS1 parser: type, paste, open, or scan an input and
read the result as the instrument-readout HTML report — rendered live in a `WebView` rather
than written to a file.

- **Parse** element strings, GS1 Digital Link URIs, and raw scans carrying an AIM Code ID —
  every AI split out, typed, validated, and explained, with errors and their remedies.
- **Build** element strings and Digital Link URIs from AI/value pairs, picking from all 541 AIs.
- **Combine** up to three GS1-128 barcodes from a legacy label — scanned or typed — into the one
  element string a GS1 DataMatrix would carry, validated as a whole and, once valid, drawn as the
  symbol. An AI repeated with the same value is kept once; one repeated with a different value is
  flagged as a conflict.
- **2D Zebra scanner support** on Windows — connect a Zebra scanner through JavaPOS (tested with
  the DS8108) and every scan, linear or 2D such as GS1 DataMatrix and GS1 QR Code, is parsed as it
  arrives.
- **Runs on Windows, macOS and Linux** — a `GAIA Desktop.app` on macOS — with Java 21 or newer.
- **35 languages** — the whole application, the report, and every guide.

**GAIA Desktop 1.0.0**, built on **GAIA 1.1.0** — GS1 General Specifications Release 26.0 and GS1
Digital Link URI Syntax Release 1.7.0.

```
tools.pantheum:gaia-desktop:1.0.0   Java 21 · JavaFX 21   →  depends on tools.pantheum:gaia:1.1.0
```

## What are GS1 Application Identifiers?

An Application Identifier is a short numeric prefix — two to four digits — that declares
what the value after it means. `01` introduces a 14-digit GTIN, `10` a batch number, `17`
an expiry date, and so on across 541 of them.

Because every value announces its own meaning, one barcode can carry several unrelated
fields in a single unbroken string, and anything reading it can pick out the parts it
cares about without knowing what the product is:

| | |
|---|---|
| `01` | an Application Identifier — a 14-digit GTIN follows |
| `09506000134352` | the GTIN |
| `10` | an Application Identifier — a batch number follows |
| `LOT-ABC` | the batch number |

Concatenated, that is `010950600013435210LOT-ABC` — one string, two fields, no separators
needed because `01` is defined as fixed-length. GAIA reads strings like this, splits them
into typed values, and checks each one against the rules GS1 defines for that AI.

## Multi-language support

The application itself runs in 35 languages (see [Languages](#languages)), and every guide —
the user guide, the samples, the Zebra scanner setup guide and the developer guide — is
translated into the 34 languages below. Each distribution's `readme.html` links the same table.

| Language | | User guide | Samples | Zebra scanner | Developer guide |
|---|---|---|---|---|---|
| English | English | [User guide](docs/GaiaDesktop-User.md) | [Samples](docs/GaiaDesktop-Samples.html) | [Zebra scanner](docs/GaiaDesktop-Zebra-Scanner-Windows.md) | [Developer guide](docs/GaiaDesktop-Developer.md) |
| Arabic | العربية | [دليل المستخدم](docs/i18n/Arabic/GaiaDesktop-User-Arabic.md) | [نماذج](docs/i18n/Arabic/GaiaDesktop-Samples-Arabic.html) | [ماسح Zebra](docs/i18n/Arabic/GaiaDesktop-Zebra-Scanner-Windows-Arabic.md) | [دليل المطوّر](docs/i18n/Arabic/GaiaDesktop-Developer-Arabic.md) |
| Bengali | বাংলা | [ব্যবহারকারীর নির্দেশিকা](docs/i18n/Bengali/GaiaDesktop-User-Bengali.md) | [নমুনা](docs/i18n/Bengali/GaiaDesktop-Samples-Bengali.html) | [Zebra স্ক্যানার](docs/i18n/Bengali/GaiaDesktop-Zebra-Scanner-Windows-Bengali.md) | [ডেভেলপার নির্দেশিকা](docs/i18n/Bengali/GaiaDesktop-Developer-Bengali.md) |
| Cantonese | 廣東話 | [使用者指南](docs/i18n/Cantonese/GaiaDesktop-User-Cantonese.md) | [範例](docs/i18n/Cantonese/GaiaDesktop-Samples-Cantonese.html) | [Zebra 掃描器](docs/i18n/Cantonese/GaiaDesktop-Zebra-Scanner-Windows-Cantonese.md) | [開發者指南](docs/i18n/Cantonese/GaiaDesktop-Developer-Cantonese.md) |
| Chinese (Simplified) | 简体中文 | [用户指南](docs/i18n/Chinese/GaiaDesktop-User-Chinese.md) | [示例](docs/i18n/Chinese/GaiaDesktop-Samples-Chinese.html) | [Zebra 扫描器](docs/i18n/Chinese/GaiaDesktop-Zebra-Scanner-Windows-Chinese.md) | [开发者指南](docs/i18n/Chinese/GaiaDesktop-Developer-Chinese.md) |
| Czech | Čeština | [Uživatelská příručka](docs/i18n/Czech/GaiaDesktop-User-Czech.md) | [Ukázky](docs/i18n/Czech/GaiaDesktop-Samples-Czech.html) | [Skener Zebra](docs/i18n/Czech/GaiaDesktop-Zebra-Scanner-Windows-Czech.md) | [Příručka pro vývojáře](docs/i18n/Czech/GaiaDesktop-Developer-Czech.md) |
| Dutch | Nederlands | [Gebruikershandleiding](docs/i18n/Dutch/GaiaDesktop-User-Dutch.md) | [Voorbeelden](docs/i18n/Dutch/GaiaDesktop-Samples-Dutch.html) | [Zebra-scanner](docs/i18n/Dutch/GaiaDesktop-Zebra-Scanner-Windows-Dutch.md) | [Ontwikkelaarshandleiding](docs/i18n/Dutch/GaiaDesktop-Developer-Dutch.md) |
| Egyptian Arabic | مصري | [دليل المستخدم](docs/i18n/EgyptianArabic/GaiaDesktop-User-EgyptianArabic.md) | [نماذج](docs/i18n/EgyptianArabic/GaiaDesktop-Samples-EgyptianArabic.html) | [ماسح Zebra](docs/i18n/EgyptianArabic/GaiaDesktop-Zebra-Scanner-Windows-EgyptianArabic.md) | [دليل المطوّر](docs/i18n/EgyptianArabic/GaiaDesktop-Developer-EgyptianArabic.md) |
| French | Français | [Guide de l'utilisateur](docs/i18n/French/GaiaDesktop-User-French.md) | [Exemples](docs/i18n/French/GaiaDesktop-Samples-French.html) | [Lecteur Zebra](docs/i18n/French/GaiaDesktop-Zebra-Scanner-Windows-French.md) | [Guide du développeur](docs/i18n/French/GaiaDesktop-Developer-French.md) |
| German | Deutsch | [Benutzerhandbuch](docs/i18n/German/GaiaDesktop-User-German.md) | [Beispiele](docs/i18n/German/GaiaDesktop-Samples-German.html) | [Zebra-Scanner](docs/i18n/German/GaiaDesktop-Zebra-Scanner-Windows-German.md) | [Entwicklerhandbuch](docs/i18n/German/GaiaDesktop-Developer-German.md) |
| Hausa | Hausa | [Jagorar mai amfani](docs/i18n/Hausa/GaiaDesktop-User-Hausa.md) | [Misalai](docs/i18n/Hausa/GaiaDesktop-Samples-Hausa.html) | [Sikanan Zebra](docs/i18n/Hausa/GaiaDesktop-Zebra-Scanner-Windows-Hausa.md) | [Jagorar mai haɓakawa](docs/i18n/Hausa/GaiaDesktop-Developer-Hausa.md) |
| Hindi | हिन्दी | [उपयोगकर्ता मार्गदर्शिका](docs/i18n/Hindi/GaiaDesktop-User-Hindi.md) | [नमूने](docs/i18n/Hindi/GaiaDesktop-Samples-Hindi.html) | [Zebra स्कैनर](docs/i18n/Hindi/GaiaDesktop-Zebra-Scanner-Windows-Hindi.md) | [डेवलपर मार्गदर्शिका](docs/i18n/Hindi/GaiaDesktop-Developer-Hindi.md) |
| Indonesian | Bahasa Indonesia | [Panduan Pengguna](docs/i18n/Indonesian/GaiaDesktop-User-Indonesian.md) | [Contoh](docs/i18n/Indonesian/GaiaDesktop-Samples-Indonesian.html) | [Pemindai Zebra](docs/i18n/Indonesian/GaiaDesktop-Zebra-Scanner-Windows-Indonesian.md) | [Panduan Pengembang](docs/i18n/Indonesian/GaiaDesktop-Developer-Indonesian.md) |
| Italian | Italiano | [Guida dell'utente](docs/i18n/Italian/GaiaDesktop-User-Italian.md) | [Esempi](docs/i18n/Italian/GaiaDesktop-Samples-Italian.html) | [Lettore Zebra](docs/i18n/Italian/GaiaDesktop-Zebra-Scanner-Windows-Italian.md) | [Guida per sviluppatori](docs/i18n/Italian/GaiaDesktop-Developer-Italian.md) |
| Japanese | 日本語 | [ユーザーガイド](docs/i18n/Japanese/GaiaDesktop-User-Japanese.md) | [サンプル](docs/i18n/Japanese/GaiaDesktop-Samples-Japanese.html) | [Zebra スキャナー](docs/i18n/Japanese/GaiaDesktop-Zebra-Scanner-Windows-Japanese.md) | [開発者ガイド](docs/i18n/Japanese/GaiaDesktop-Developer-Japanese.md) |
| Javanese | Basa Jawa | [Pandhuan panganggo](docs/i18n/Javanese/GaiaDesktop-User-Javanese.md) | [Conto](docs/i18n/Javanese/GaiaDesktop-Samples-Javanese.html) | [Scanner Zebra](docs/i18n/Javanese/GaiaDesktop-Zebra-Scanner-Windows-Javanese.md) | [Pandhuan pangembang](docs/i18n/Javanese/GaiaDesktop-Developer-Javanese.md) |
| Korean | 한국어 | [사용자 안내서](docs/i18n/Korean/GaiaDesktop-User-Korean.md) | [샘플](docs/i18n/Korean/GaiaDesktop-Samples-Korean.html) | [Zebra 스캐너](docs/i18n/Korean/GaiaDesktop-Zebra-Scanner-Windows-Korean.md) | [개발자 가이드](docs/i18n/Korean/GaiaDesktop-Developer-Korean.md) |
| Marathi | मराठी | [वापरकर्ता मार्गदर्शिका](docs/i18n/Marathi/GaiaDesktop-User-Marathi.md) | [नमुने](docs/i18n/Marathi/GaiaDesktop-Samples-Marathi.html) | [Zebra स्कॅनर](docs/i18n/Marathi/GaiaDesktop-Zebra-Scanner-Windows-Marathi.md) | [विकसक मार्गदर्शिका](docs/i18n/Marathi/GaiaDesktop-Developer-Marathi.md) |
| Nigerian Pidgin | Naijá | [Guide for di person wey dey use am](docs/i18n/NigerianPidgin/GaiaDesktop-User-NigerianPidgin.md) | [Samples](docs/i18n/NigerianPidgin/GaiaDesktop-Samples-NigerianPidgin.html) | [Zebra scanner](docs/i18n/NigerianPidgin/GaiaDesktop-Zebra-Scanner-Windows-NigerianPidgin.md) | [Guide for Developer](docs/i18n/NigerianPidgin/GaiaDesktop-Developer-NigerianPidgin.md) |
| Persian | فارسی | [راهنمای کاربر](docs/i18n/Persian/GaiaDesktop-User-Persian.md) | [نمونه‌ها](docs/i18n/Persian/GaiaDesktop-Samples-Persian.html) | [اسکنر Zebra](docs/i18n/Persian/GaiaDesktop-Zebra-Scanner-Windows-Persian.md) | [راهنمای توسعه‌دهنده](docs/i18n/Persian/GaiaDesktop-Developer-Persian.md) |
| Polish | Polski | [Podręcznik użytkownika](docs/i18n/Polish/GaiaDesktop-User-Polish.md) | [Przykłady](docs/i18n/Polish/GaiaDesktop-Samples-Polish.html) | [Skaner Zebra](docs/i18n/Polish/GaiaDesktop-Zebra-Scanner-Windows-Polish.md) | [Podręcznik programisty](docs/i18n/Polish/GaiaDesktop-Developer-Polish.md) |
| Portuguese | Português | [Guia do utilizador](docs/i18n/Portuguese/GaiaDesktop-User-Portuguese.md) | [Exemplos](docs/i18n/Portuguese/GaiaDesktop-Samples-Portuguese.html) | [Leitor Zebra](docs/i18n/Portuguese/GaiaDesktop-Zebra-Scanner-Windows-Portuguese.md) | [Guia do programador](docs/i18n/Portuguese/GaiaDesktop-Developer-Portuguese.md) |
| Punjabi | ਪੰਜਾਬੀ | [ਵਰਤੋਂਕਾਰ ਗਾਈਡ](docs/i18n/Punjabi/GaiaDesktop-User-Punjabi.md) | [ਨਮੂਨੇ](docs/i18n/Punjabi/GaiaDesktop-Samples-Punjabi.html) | [Zebra ਸਕੈਨਰ](docs/i18n/Punjabi/GaiaDesktop-Zebra-Scanner-Windows-Punjabi.md) | [ਡਿਵੈਲਪਰ ਗਾਈਡ](docs/i18n/Punjabi/GaiaDesktop-Developer-Punjabi.md) |
| Russian | Русский | [Руководство пользователя](docs/i18n/Russian/GaiaDesktop-User-Russian.md) | [Примеры](docs/i18n/Russian/GaiaDesktop-Samples-Russian.html) | [Сканер Zebra](docs/i18n/Russian/GaiaDesktop-Zebra-Scanner-Windows-Russian.md) | [Руководство разработчика](docs/i18n/Russian/GaiaDesktop-Developer-Russian.md) |
| Spanish | Español | [Guía del usuario](docs/i18n/Spanish/GaiaDesktop-User-Spanish.md) | [Ejemplos](docs/i18n/Spanish/GaiaDesktop-Samples-Spanish.html) | [Lector Zebra](docs/i18n/Spanish/GaiaDesktop-Zebra-Scanner-Windows-Spanish.md) | [Guía del desarrollador](docs/i18n/Spanish/GaiaDesktop-Developer-Spanish.md) |
| Swahili | Kiswahili | [Mwongozo wa mtumiaji](docs/i18n/Swahili/GaiaDesktop-User-Swahili.md) | [Mifano](docs/i18n/Swahili/GaiaDesktop-Samples-Swahili.html) | [Skana ya Zebra](docs/i18n/Swahili/GaiaDesktop-Zebra-Scanner-Windows-Swahili.md) | [Mwongozo wa msanidi](docs/i18n/Swahili/GaiaDesktop-Developer-Swahili.md) |
| Swedish | Svenska | [Användarhandbok](docs/i18n/Swedish/GaiaDesktop-User-Swedish.md) | [Exempel](docs/i18n/Swedish/GaiaDesktop-Samples-Swedish.html) | [Zebra-skanner](docs/i18n/Swedish/GaiaDesktop-Zebra-Scanner-Windows-Swedish.md) | [Utvecklarhandbok](docs/i18n/Swedish/GaiaDesktop-Developer-Swedish.md) |
| Tagalog | Tagalog | [Gabay ng gumagamit](docs/i18n/Tagalog/GaiaDesktop-User-Tagalog.md) | [Mga sample](docs/i18n/Tagalog/GaiaDesktop-Samples-Tagalog.html) | [Zebra scanner](docs/i18n/Tagalog/GaiaDesktop-Zebra-Scanner-Windows-Tagalog.md) | [Gabay ng developer](docs/i18n/Tagalog/GaiaDesktop-Developer-Tagalog.md) |
| Tamil | தமிழ் | [பயனர் வழிகாட்டி](docs/i18n/Tamil/GaiaDesktop-User-Tamil.md) | [மாதிரிகள்](docs/i18n/Tamil/GaiaDesktop-Samples-Tamil.html) | [Zebra ஸ்கேனர்](docs/i18n/Tamil/GaiaDesktop-Zebra-Scanner-Windows-Tamil.md) | [உருவாக்குநர் வழிகாட்டி](docs/i18n/Tamil/GaiaDesktop-Developer-Tamil.md) |
| Telugu | తెలుగు | [వినియోగదారు మార్గదర్శిని](docs/i18n/Telugu/GaiaDesktop-User-Telugu.md) | [నమూనాలు](docs/i18n/Telugu/GaiaDesktop-Samples-Telugu.html) | [Zebra స్కానర్](docs/i18n/Telugu/GaiaDesktop-Zebra-Scanner-Windows-Telugu.md) | [డెవలపర్ మార్గదర్శిని](docs/i18n/Telugu/GaiaDesktop-Developer-Telugu.md) |
| Turkish | Türkçe | [Kullanıcı Kılavuzu](docs/i18n/Turkish/GaiaDesktop-User-Turkish.md) | [Örnekler](docs/i18n/Turkish/GaiaDesktop-Samples-Turkish.html) | [Zebra tarayıcı](docs/i18n/Turkish/GaiaDesktop-Zebra-Scanner-Windows-Turkish.md) | [Geliştirici Kılavuzu](docs/i18n/Turkish/GaiaDesktop-Developer-Turkish.md) |
| Ukrainian | Українська | [Посібник користувача](docs/i18n/Ukrainian/GaiaDesktop-User-Ukrainian.md) | [Приклади](docs/i18n/Ukrainian/GaiaDesktop-Samples-Ukrainian.html) | [Сканер Zebra](docs/i18n/Ukrainian/GaiaDesktop-Zebra-Scanner-Windows-Ukrainian.md) | [Посібник розробника](docs/i18n/Ukrainian/GaiaDesktop-Developer-Ukrainian.md) |
| Urdu | اردو | [صارف گائیڈ](docs/i18n/Urdu/GaiaDesktop-User-Urdu.md) | [نمونے](docs/i18n/Urdu/GaiaDesktop-Samples-Urdu.html) | [Zebra اسکینر](docs/i18n/Urdu/GaiaDesktop-Zebra-Scanner-Windows-Urdu.md) | [ڈویلپر گائیڈ](docs/i18n/Urdu/GaiaDesktop-Developer-Urdu.md) |
| Vietnamese | Tiếng Việt | [Hướng dẫn người dùng](docs/i18n/Vietnamese/GaiaDesktop-User-Vietnamese.md) | [Mẫu](docs/i18n/Vietnamese/GaiaDesktop-Samples-Vietnamese.html) | [Máy quét Zebra](docs/i18n/Vietnamese/GaiaDesktop-Zebra-Scanner-Windows-Vietnamese.md) | [Hướng dẫn dành cho nhà phát triển](docs/i18n/Vietnamese/GaiaDesktop-Developer-Vietnamese.md) |
| Wu Chinese | 吴语 | [用户指南](docs/i18n/WuChinese/GaiaDesktop-User-WuChinese.md) | [样例](docs/i18n/WuChinese/GaiaDesktop-Samples-WuChinese.html) | [Zebra 扫描器](docs/i18n/WuChinese/GaiaDesktop-Zebra-Scanner-Windows-WuChinese.md) | [开发者指南](docs/i18n/WuChinese/GaiaDesktop-Developer-WuChinese.md) |

English is the source of truth.
[`docs/i18n-manifest.json`](docs/i18n-manifest.json) records which English revision each
translation was made from, and `python3 docs/check-i18n.py` checks them all: structure, code
blocks, links and UI labels for the guides, the sample barcode data for the samples page, and which
translations are behind their English. After updating a translation, record it with
`python3 docs/check-i18n.py --record <document> [Language …]`.

> **Full guides:**
> [`docs/GaiaDesktop-User.md`](docs/GaiaDesktop-User.md) — the UI in detail: parsing, the
> builder, Combine, the report, options, languages, shortcuts and troubleshooting.
> [`docs/GaiaDesktop-Developer.md`](docs/GaiaDesktop-Developer.md) — packaging, architecture,
> the `{GS}` token, the scanner gate, the localization contract, the state file formats and
> testing.
> [`docs/GaiaDesktop-Zebra-Scanner-Windows.md`](docs/GaiaDesktop-Zebra-Scanner-Windows.md) —
> installing and configuring a Zebra scanner on Windows: Java, the Zebra Scanner SDK, 123Scan,
> the scanner's firmware, USB mode and GS1 settings, the supported 2D models, connecting, and
> troubleshooting.
> [`docs/GaiaDesktop-Samples.html`](docs/GaiaDesktop-Samples.html) — sample barcodes, one per GS1
> data carrier and a three-barcode label for Combine, drawn by TEC-IT's online generator.
> Every distribution carries all of `docs/` (the guides and their translations), indexed by
> `readme.html` at its root.
> This page is the short build-and-run note.

## Build & run

The core must be installed to `~/.m2` first — this module resolves `gaia:1.1.0` from the
local repository.

```bash
(cd gaia && mvn -q install -DskipTests)     # publish the core
(cd gaia-desktop && mvn -q javafx:run)      # run it
(cd gaia-desktop && mvn -q package)         # target/dist/gaia-desktop-<version>-<platform>/ — jar (~340 KB), lib/, run.sh (run.bat on win) — and the same, zipped
(cd gaia-desktop && mvn -q -Pfat package)   # target/dist/gaia-desktop-<version>-<platform>/ — one ~50 MB jar, run.sh (run.bat on win)
(cd gaia-desktop && mvn -q package -Dfx.platform=win)   # another platform's; see below
java -jar gaia-desktop/target/dist/gaia-desktop-1.0.0-mac-aarch64/gaia-desktop.jar
java -jar gaia-desktop/target/dist/gaia-desktop-1.0.0-mac-aarch64/gaia-desktop.jar --language=FR   # this run only
```

### The launch scripts

Everything shippable is assembled under **`target/dist/gaia-desktop-<version>-<platform>/`** —
`gaia-desktop-1.0.0-win`, `gaia-desktop-1.0.0-mac-aarch64` and so on, the version taken from the
pom, see [Building for other platforms](#building-for-other-platforms) — and
nothing else is, so the directory can be zipped or copied as a set without picking artefacts out of `target/` next to
`classes/`, `maven-status/` and the rest of the build's scratch:

```
target/dist/gaia-desktop-1.0.0-win/
├── gaia-desktop.jar
├── lib/                    16 Maven jars (absent in -Pfat)
├── docs/                   the guides, the samples and their translations
├── readme.html             the index to docs/, version filled in from the pom
└── run.bat                 (run.sh in the mac, mac-aarch64 and linux distributions)
```

The build ships **nothing of Zebra's**. On Windows the scanner's JavaPOS services and their
`CSJPOS*.dll` natives come from Zebra's Scanner SDK installed on the machine, which `run.bat`
finds and wires in — see [What `run.bat` adds on Windows](#what-runbat-adds-on-windows). Any
DLLs placed beside `run.bat` by hand still go on `java.library.path` ahead of the `PATH`.

`mvn package` writes the platform's launcher there alongside the jar — **`run.bat`** in the `win`
distribution, **`run.sh`** in every other, so each directory carries only the one its users can run:

```bash
./run.sh                  # macOS, Linux
./run.sh --language=FR    # arguments pass straight through
run.bat                   # Windows
```

Each script **builds the class path itself** — `gaia-desktop.jar` first, then every `.jar` in
`lib/` beside it, named one by one rather than left to a `lib/*` wildcard — and launches with
`-cp`. That is why they name the main class: `-cp` does not read the manifest's `Main-Class`.
(It does still follow its `Class-Path`, so the `lib/` jars load either way.) Running
`java -jar gaia-desktop.jar` by hand works through the manifest alone; the scripts simply do not
depend on it, so a jar whose manifest is stale, hand-edited or short an entry still starts. The same script also covers both
packagings, since the `-Pfat` jar has no `lib/` to add and needs none.

They do the rest of what `java -jar` does not, once the app leaves a developer's machine:
resolve their own directory (so they work from any working directory and survive spaces in the
path), prefer `JAVA_HOME` and fall back to `java` on the `PATH`, and refuse an old JVM with a
sentence about installing Java 21 rather than letting it fail with
`UnsupportedClassVersionError`, which names no remedy. A missing jar, or an empty `lib/`, is
reported in the same terms the [troubleshooting table](docs/GaiaDesktop-User.md#troubleshooting)
uses. Extra JVM options go in `GAIA_DESKTOP_OPTS`:

```bash
GAIA_DESKTOP_OPTS=-Xmx1g ./run.sh
```

#### What `run.bat` adds on Windows

`run.bat` adapts to what is installed beside it and on the machine, and prints one line saying
what it picked (`run.bat: Java … from …; JavaFX from …; JavaPOS from lib\; Zebra services from …`):

| | First choice | Otherwise |
|---|---|---|
| **Java** | a bundled runtime, `jdk*\` beside the script (e.g. `jdk-27\`) | `JAVA_HOME`, then `java` on the `PATH` |
| **JavaFX** | a JavaFX SDK, `javafx-sdk*\lib\` beside the script, on the module path — `lib\javafx-*.jar` then stay off the class path | `lib\javafx-*.jar` on the class path |
| **Zebra services** | Zebra's Scanner SDK at `ZEBRA_HOME` (default `%ProgramFiles%\Zebra Technologies\Barcode Scanners\Scanner SDK\JPOS\`): its `Jpos*.jar` service jars and `bin\` on the class path, `bin\` on the `PATH` for the `CSJPOS*.dll` natives | none — the app runs, but cannot connect a scanner |

The JavaPOS API itself always comes from `lib\javapos-*.jar`. The SDK's own JavaPOS jar,
`javaPOS114.jar`, is deliberately left off: its `jcl.xsd` accepts JavaPOS versions only up to
1.10 and its `jpos.properties` overrides the app's configuration, so with it on the class path
every Connect fails with *"Service does not exist in loaded JCL registry"*. The
[developer guide](docs/GaiaDesktop-Developer.md#the-zebra-natives) has the details. Set
`ZEBRA_HOME` before running the script to use an SDK installed elsewhere.

It always passes three system properties: the JDK's own SAX parser for `jpos.xml`
(`javax.xml.parsers.SAXParserFactory`), because `lib\`'s Xerces 2.12.2 rejects the JAXP property
the JavaPOS loader sets and then loads no devices at all; `java.net.useSystemProxies=true`; and
`javax.net.ssl.trustStoreType=Windows-ROOT`, so proxies and trusted certificates come from
Windows. On Java 17 and later it adds `--enable-native-access` (`ALL-UNNAMED`, plus
`javafx.graphics,javafx.web` when JavaFX runs from the SDK as modules), which newer JDKs require
before code may load native libraries — `run.sh` passes `--enable-native-access=ALL-UNNAMED`
too, for the JavaFX natives on macOS and Linux. The script is stored with CRLF line endings, which
`cmd.exe` needs to find its `call :label` subroutines reliably.

`run.bat` deliberately uses `java.exe`, not `javaw.exe`, so the console window stays open —
Windows is the only platform where the scanner runs, and its status, the JavaPOS errors and
the startup version line all go to the console. Change the one word if a console-free launch
matters more.

The sources are `src/main/scripts/`; the build copies them unfiltered (`run.sh` is full of
`${...}` shell expansions that Maven filtering would be free to rewrite) and sets the
executable bit on `run.sh`.

### Building for other platforms

The JavaFX jars in `lib/` hold native code, so a distribution runs on one platform only. The
build picks the platform with the `fx.platform` property — set automatically to the building
machine's, and overridden on the command line to build any other. The JavaFX jars are ordinary
Maven downloads, so **one machine can build every platform**; nothing has to run on Windows to
produce the Windows distribution.

| `-Dfx.platform=` | For | Output |
|---|---|---|
| `mac-aarch64` | macOS, Apple silicon | `target/dist/gaia-desktop-1.0.0-mac-aarch64/` |
| `mac` | macOS, Intel | `target/dist/gaia-desktop-1.0.0-mac/` |
| `win` | Windows x64 | `target/dist/gaia-desktop-1.0.0-win/` |
| `linux` | Linux x64 | `target/dist/gaia-desktop-1.0.0-linux/` |
| `linux-aarch64` | Linux ARM64 | `target/dist/gaia-desktop-1.0.0-linux-aarch64/` |

Each platform gets its own directory, so building one leaves the others in place. All four
desktop platforms, from a clean start:

```bash
(cd gaia-desktop && mvn -q clean package)    # this machine's platform, with the tests
for p in win linux mac mac-aarch64; do (cd gaia-desktop && mvn -q package -DskipTests -Dfx.platform=$p); done
```

The tests are platform-independent, so running them once is enough. `-Pfat` takes
`-Dfx.platform` the same way. Each distribution still needs a Java 21+ runtime on the target
machine; the run scripts say so when there is none.

The property is `fx.platform` and not OpenJFX's own `javafx.platform` on purpose: setting that one
from outside makes OpenJFX's pom define it in terms of itself, Maven rejects the pom, and the
platform jars silently drop out of the build.

### macOS: GAIA Desktop.app

The two macOS distributions hold an app bundle instead of a loose jar, so the app opens from
Finder, the Dock or Launchpad like any other — no Terminal:

```
gaia-desktop-1.0.0-mac-aarch64/
├── GAIA Desktop.app/
│   └── Contents/
│       ├── Info.plist            name, version (from the pom), bundle id tools.pantheum.gaia.desktop
│       ├── MacOS/gaia-desktop    the launcher script
│       ├── Resources/gaia-desktop.icns
│       └── Java/                 gaia-desktop.jar and lib/, moved in by the build
└── run.sh                        still works: it finds the jar inside the app
```

The app is self-contained — drag it to Applications on its own — but does not bundle a Java
runtime. Its launcher asks macOS for a Java 21 or newer (`/usr/libexec/java_home -v 21+`), then
tries Homebrew's `openjdk`, since an app opened from Finder sees neither `JAVA_HOME` nor the
shell's `PATH`; with none found it says so in a dialog. It starts in `~/.gaia-desktop`, so the
log goes to `~/.gaia-desktop/logs/gaia-desktop.log`, and passes `-Xdock:name` / `-Xdock:icon` so
the menu bar and Dock read **GAIA Desktop** with its icon rather than "java".

The app is **not signed or notarised**. The first time a copy downloaded from GitHub is opened,
macOS refuses it as from an unidentified developer: right-click it → **Open**, or allow it under
**System Settings → Privacy & Security**, once; after that it opens normally. Removing that step
takes an Apple Developer ID, code signing and notarisation.

The build makes the bundle from `src/main/macos/` (Info.plist, launcher, icon) in the
`mac-app-and-zip` antrun step, on any machine, for `mac` and `mac-aarch64` only. The icon comes
from `tools/icon/GaiaIcon.java`; the same artwork, as PNGs in `src/main/resources/icons/`, is the
window icon on Windows and Linux.

### Releasing on GitHub

Every build also zips its distribution, beside the directory:

```
target/dist/
├── gaia-desktop-1.0.0-win/              the directory, as above
├── gaia-desktop-1.0.0-win.zip           the same, under one gaia-desktop-1.0.0-win/ folder
├── gaia-desktop-1.0.0-mac-aarch64.zip
├── gaia-desktop-1.0.0-mac.zip
└── gaia-desktop-1.0.0-linux.zip
```

Unzipping gives one folder, not files strewn into the current directory, and `run.sh` and the
macOS app's launcher keep their executable bit inside the zip, so they run straight after
unzipping. The macOS zips hold `GAIA Desktop.app` and `run.sh` (see above); the Windows and
Linux zips the jar, `lib/` and their script. The zips
are for a **GitHub release**, not a Maven repository: they are not attached to the build, so
`mvn install` does not copy them into `~/.m2`. Maven Central publishes a version once, as one
upload, which four separate per-platform builds cannot be. Each zip is that platform's
self-contained download; the user still needs Java 21 or newer.

After building all four (above), publish them with the GitHub CLI, from the gaia-desktop
repository:

```bash
gh release create v1.0.0 gaia-desktop/target/dist/gaia-desktop-1.0.0-*.zip --title "GAIA Desktop 1.0.0" --generate-notes
```

### Thin or fat

Both layouts write the same path, `target/dist/gaia-desktop-<version>-<platform>/gaia-desktop.jar`, and exactly one of them runs per
build — the default skips the assembly, and `-Pfat` skips the jar plugin and the dependency
copy.

| | Default | `-Pfat` |
|---|---|---|
| `gaia-desktop.jar` | ~340 KB, this module's classes and resources only | ~50 MB, dependencies unpacked into it |
| `dist/gaia-desktop-<version>-<platform>/lib/` | 16 jars, ~46–55 MB by platform, named one by one on the jar's manifest `Class-Path` | — |
| Ship | the jar **and** `lib/` beside it | one file |

Both start the same way — `./run.sh`, or `java -jar target/dist/gaia-desktop-<version>-<platform>/gaia-desktop.jar` — and both
use the `Launcher` shim, since JavaFX is on the class path either way.

The default is thin because a rebuild then rewrites only what changed — almost the entire fat
jar is one file, `libjfxwebkit.dylib` — the app's own classes stay readable in a small,
diffable artifact, and a deployment can share or swap the JavaFX jars without rebuilding.
Reach for `-Pfat` when the deliverable has to be a single file. Neither is more portable: both
carry the JavaFX jars of one platform, `${fx.platform}`.

The manifest `Class-Path` and `lib/` are two halves of one thing — the jar names
`lib/<artifact>-<version>.jar` for every runtime dependency, and `copy-dependencies` fills
`lib/` with exactly those names. Move the jar somewhere without `lib/` beside it and it will
not start. That coupling governs `java -jar`; the launch scripts reach the same class path a
second, independent way, by listing `lib/` themselves.

`mvn javafx:run` is the development path: the plugin puts JavaFX on the **module path**, so
the toolkit starts as a named module. The packaged jar cannot do that — everything is on the
class path — so its `Main-Class` is the `Launcher` shim rather than `GaiaDesktopApp`, which
is what keeps the JavaFX launcher from refusing to start with "JavaFX runtime components are
missing". Either layout carries the JavaFX natives of one platform, so **both are
platform-specific** — build one per target with `-Dfx.platform`, from any machine.

## What it does

| Area | Behaviour |
|------|-----------|
| **Modes** | **Parse** (the default), **Build** and **Combine**, switched by the segmented control at the left of the toolbar. Build hides the capture-side controls (input bar, `{GS}`, Parse, and — where they exist — the scanner and its device picker) and shows the AI editor instead; history, report, options and export serve Parse and Build alike. A connected scanner keeps working — *Scanner → Connect/disconnect* (<kbd>⌘K</kbd>) still toggles it and the status bar still reports it. The mode is remembered between runs. |
| **Combine** | Up to three GS1-128 barcodes, scanned or typed into the Combine strip at the bottom of the window (FNC1 written as `{GS}`), joined into one element string and judged as a whole. The scanner pair stays, since a scan is the main way in, but the parse input bar and the capture history step aside — Combine scans never enter the history. A barcode that is empty, is not GS1-128 by its AIM Code ID, carries no AIs, or was already scanned is refused with the reason, as is a fourth. The Combine report shows the combined input tape, the element string, its HRI and length, the element cards, the duplicates kept once and the conflicts, the validation of the combination, and — once it is valid — the GS1 DataMatrix symbol, drawn by the app itself. *Add*, *Remove Last* and *Reset* manage the scans; a changed option or language re-parses them. |
| **Input bar** | Element strings, GS1 Digital Link URIs, and raw scans with an AIM Code ID prefix. `{GS}` inserts a real FNC1 (0x1D) at the caret. <kbd>Enter</kbd> parses. |
| **Scanner** | **Windows only** — Zebra ships its JavaPOS services for Windows, so on macOS and Linux no scanner control is built at all (no toggle, no device picker, no Scanner menu, no state in the status bar). Where it is supported: connect/disconnect a JavaPOS scanner and every scan is parsed and appended to the history (or, in Combine mode, added to the combination). The device picker lists every `Scanner` entry in `jpos.xml`, `ZebraAllScanners` first. The rule is one `os.name` test, `ScannerService.isSupportedPlatform()`. Setting up the scanner itself is the [Zebra scanner guide](docs/GaiaDesktop-Zebra-Scanner-Windows.md). |
| **File** | *File → Open text file…* parses each non-blank line as its own capture. Read as ISO-8859-1 so a raw 0x1D separator survives the round trip. |
| **History** | Every capture, newest first, with a valid/invalid dot, HRI, source, and AI count. Right-click to copy the input or HRI, send it back to the input bar, or remove it. |
| **Report** | The selected capture as a full parse report: verdict, element-string tape, parse metadata, per-AI cards with interpretations, and errors/warnings with remediation. |
| **Export** | **Save report → As HTML…** (<kbd>⌘S</kbd>) writes exactly what is on screen — one self-contained file, no external assets. **As PDF…** (<kbd>⌘P</kbd>) prints the document through the platform pipeline; pick the PDF destination in the system dialog. |
| **Build** | A table of AI/value pairs — type a code or pick from all 541 AIs — with the data title and format filling in from the core registry as you type, and an unrecognised AI marked before you press anything. *Build element string* / *Build Digital Link* (optional custom domain and path prefix). |
| **Options** | Every `ParseConfig` option — parse mode, language (35), date rendering, and the two structural-check escapes. Changing one **re-parses the selected capture in place**, so the panel doubles as a "what does this input look like under these settings" control. Hidden on a first launch; open it from the toolbar or *View → Options panel*. |
| **Language** | Switches the whole application, not just parser messages: menus, buttons, the options panel, the status line, and the report. Right-to-left catalogues flip the window and set `dir="rtl"` on the report. |
| **Theme** | Light/dark (<kbd>⌘D</kbd>). The chrome and the report share one palette and switch together — the WebView does not follow the OS setting, so the app pins it. |
| **State** | Options and captures are remembered between runs — see [State](#state) below. |
| **Updates** | A few seconds after start-up, at most once a day, the app asks GitHub's API for the latest published release (`update/UpdateChecker`; drafts and pre-releases never count). If it is newer than the running version, a status-bar link opens its release page — nothing is downloaded or installed — beside *Skip this version*. Silent when nothing is new or GitHub cannot be reached; never asks from a development build, whose version is `unknown`. *Help → Check for updates…* checks now and reports either way; *Help → Check for updates automatically* switches the daily check off. |
| **About** | A button at the right of the status bar. The dialog reports the app version, its build timestamp, the GAIA parser version and the GS1 releases implemented — the status bar itself carries no version text. Also *Help → About*, which on macOS lives in the system menu bar. |

### PDF export

JavaFX has no API that writes a page straight to a PDF file, so PDF goes through the
platform's print pipeline: `WebEngine.print(PrinterJob)` lays the document out under the
report's `@media print` rules and hands paginated, **text-selectable** pages to the job. The
destination — and, for a PDF destination, the file name — is chosen in the system dialog:

| Platform | Destination to pick |
|----------|--------------------|
| macOS | **PDF ▾ → Save as PDF** |
| Windows | **Microsoft Print to PDF** |
| Linux / CUPS | **Print to File**, output format PDF |

The print stylesheet forces the light palette even when the app is in dark mode (a dark PDF
is unreadable on paper), wraps the element-string tape instead of scrolling it, keeps cards
whole across page breaks, and marks the meaningful backgrounds — the AI chips, the verdict
wash — `print-color-adjust: exact` so they survive.

If the machine has no printer or PDF destination at all, `PrinterJob.createPrinterJob()`
returns `null`; the app says so and points at the HTML export, which prints from any browser.

## State

Two files under `~/.gaia-desktop/`, deliberately kept apart: they change on different clocks,
and a hand-edited or corrupt capture log should not cost you your settings.

| File | Holds | Written when |
|------|-------|--------------|
| `options.properties` | parser options, theme, options-panel visibility, last scanner device, last mode (Parse/Build/Combine), update-check settings | an option changes, and on exit |
| `captures.tsv` | the capture history, newest first, capped at 500 | the history changes, and on exit |

Point `-Dgaia.desktop.home=…` somewhere else to move both (the tests use it to stay out of
the real home directory).

Only what cannot be recomputed is stored in `captures.tsv`: the input, its provenance, and the
capture time. The `ParseResult` is **not** serialised — it is re-derived by parsing the input
again on load, so a restored history reflects the options in force now rather than the ones in
force when it was captured. Inputs routinely contain FNC1 (0x1D) and whatever else a scanner
emits, so the input field is escaped (`\\`, `\t`, `\n`, `\r`, `\xNN`) and the file is read and
written as ISO-8859-1, byte for byte.

Restoring happens on a background thread, so a long history never delays the first frame.

### First launch

With no `options.properties`, the options are seeded from the machine's own regional settings
rather than from GAIA's built-in defaults:

- the **message language** comes from `Locale.getDefault()`, falling back to English when the
  parser has no catalogue for it (`zh-HK` → Cantonese and `ar-EG` → Egyptian Arabic are
  special-cased, since the language code alone cannot carry them);
- the **date options** are read out of the locale's short date pattern — `d/MM/yy`, `M/d/yy`,
  `dd.MM.yyyy`, `yyyy-MM-dd` — which gives the component order, the separator, and whether the
  month and year are abbreviated.

Note the last one: a locale whose short pattern uses `yy` starts with **two-digit years**,
which is what the OS shows everywhere else but differs from GAIA's own `FOUR_DIGIT` default.
Change it in the options panel and the choice sticks.

## Languages

The label catalogue is one properties file per language in
[`src/main/resources/i18n`](src/main/resources/i18n), named with the same code the core uses
for its error catalogues (`ui_EN.properties`, `ui_PCM.properties`, …). All 35 exist and all 35
carry an identical key set — a test enforces that.

Coverage is in two layers, because **fallback is per key, not per file**:

- the **shared report vocabulary** (31 keys — input, elements, errors, start time, …) is
  translated in **all 35 languages**;
- the **desktop chrome** (160 keys — menus, buttons, the builder, Combine, the options panel,
  the status line, the scanner status, the dialog bodies, the report's own layout labels) is
  translated in **all 35** as well.

The per-key fallback is now a safety net rather than something a user meets: it catches a key
added to English before the translations catch up. Two values stay English everywhere on
purpose — `act.hri` (HRI) and `build.colAi` (AI), both GS1 abbreviations — and Nigerian Pidgin,
being English-based, legitimately matches English on much of its chrome. The English catalogue
is the contract, and every key must exist there.

Deliberately left untranslated: the terse field names set in monospace next to raw values,
which are identifiers rather than prose (`format`, `position`, `length`, `raw`, `part` in the
element cards, `AI`, `sep`, `pos`, `len` on the tape, and the four cells locating an error);
the sample-input names, which name GS1 constructs; the symbology names, which are proper nouns;
the `GE-C084` remediation paragraph, which is standards prose where a half-translation would be
worse than English; and `ElementBuilder`'s unknown-AI backstop, which is unreachable from the
UI — the editor checks for unknown AIs itself and reports them from the catalogue.

The scanner is not an exception to any of this: it reports a `ScannerListener.Event` and the
values that vary, and `MainView` turns that into text from the catalogue. Log lines stay English
by design.

Still outstanding: `ScanSource.label()` (`manual`, `scanner`, `file`, `built`), shown on the
history row and in the report masthead. It needs more than a key — `HistoryCell` has no
`UiStrings`, and its cell factory is installed once, so the cells would have to be refreshed on
a language change too.

Two tests keep the catalogues honest: one fails if the code asks for a key English does not
define, the other if any of the 35 files drifts from the English key set.

### How building fits in

`GaiaBuilder` validates by **parsing its own candidate output**, so anything it returns is
guaranteed to parse. A successful build is therefore just an input: it goes through the same
`capture(...)` funnel as a scan or a paste and gets the ordinary parse report, history entry,
export and persistence — there is no second rendering path for built strings, and a build is
stored as `ScanSource.BUILT`.

A *failed* build stays in the editor next to the rows that caused it, because a `BuildResult`
carries errors but no `ParseResult` for the report to render.

Right-click a capture → **Build** opens its AIs in the editor, which is how you take a scan,
change a value, and rebuild it.

## Where things are

- **Entry points** — `GaiaDesktopApp` (the `Application`), `Launcher` (fat-jar `Main-Class`).
- **`ui/MainView`** — the window, and the single funnel every input passes through
  (`capture(...)`), whatever its source.
- **`ui/OptionsPane`** — the parser-options panel, bound to `parse/ParseOptions`.
- **`parse/ParseOptions`** — the observable form of `ParseConfig`; `toParseConfig()` mints
  an immutable config per parse. `parse/Languages` supplies the picker labels, the locale
  mapping, and the RTL flag; `parse/SystemDefaults` seeds a first launch from the OS.
- **`build/ElementBuilder`** — the build half, JavaFX-free and unit-tested: AI lookups and
  the two build calls. The AI list is **not** copied — it reads the core's
  `AiDefinitionRegistry`, so the picker cannot offer an AI the builder would reject.
  `build/AiPair` is one editable row; `ui/BuilderPane` is the editor.
- **`i18n/UiStrings`** — the label catalogue, with per-key fallback to English.
- **`store/`** — `AppPaths` (where state lives), `OptionsStore`, `CaptureStore`.
- **`combine/ScanCombiner`** — Combine mode's model, JavaFX-free: holds up to three GS1-128
  scans, refuses the ones that cannot join, merges their AIs (duplicates kept once, conflicts
  flagged) and parses the combination. `ui/CombinePane` is its strip; `MainView` owns the
  combiner, so a scanned barcode and a typed one take the same path.
- **`scanner/ScannerService`** — the JavaPOS control: open → claim → enable, re-arming the
  data event after each scan. Every device call runs on its own long-lived **`gaia-scanner`**
  thread (Zebra's COM layer binds to the thread that opened the device); callbacks arrive on the
  **JavaPOS event thread**, and `MainView.FxScannerListener` hops them to the FX thread.
  `logicalNames()` reads the device picker out of the JavaPOS registry.
- **`scanner/JposConfig`** — points JavaPOS at the bundled `jpos.xml` and the JDK's SAX parser
  before the first open, unless the machine already configures it; without it every Connect
  fails with error 109, *"Service does not exist in loaded JCL registry"*.
- **`html/GaiaDesktopReport`** — the report renderer. A pure function of its arguments and
  free of JavaFX, so it is unit-tested without starting the toolkit. `html/GaiaCombineReport`
  is the Combine report, built from the same shell, tape and element cards.
- **`datamatrix/`** — the app's own GS1 DataMatrix (ECC 200) encoder and its SVG drawing, for
  the Combine symbol. ZXing cannot write one (it has no FNC1 codeword, so its symbols read as
  plain Data Matrix, `]d1`); it is a test-scope dependency only, decoding every symbol the encoder
  writes and checking it reads as GS1, `]d2`.
- **`update/UpdateChecker`** — the once-a-day GitHub release check.
- **`model/ScanRecord`** — one capture: input, provenance, and the current result. Input and
  result are separate because re-parsing under new options replaces only the result.

## Scanner configuration

`src/main/resources/jpos.xml` is the JavaPOS device registry, bundled in the jar. Each
`<JposEntry>` is a logical device — `ZebraAllScanners` (any Zebra scanner the SDK can see, the
default), `ZebraScannerUSB`, `ZebraUSBHandHeld`, `ZebraUSBTableTop`, `ZebraScannerSNAPI` and so
on — naming the Zebra service class that drives it and its properties, such as `ScannerType`.
The device picker is **generated from it**: every entry whose category is `Scanner` is offered,
`ZebraAllScanners` first and the rest alphabetically, so adding an entry adds it to the picker
with no code change. The same file also lists Zebra's scales, which the picker leaves out.

`JposConfig` points JavaPOS at this file only when nothing else is configured: a
`jpos.properties` on the class path, or `jpos.config.*` system properties set on the command
line, win. The [Zebra scanner guide](docs/GaiaDesktop-Zebra-Scanner-Windows.md#10-configuration-reference)
has the configuration reference.

Scanner bytes are read as ISO-8859-1 (each byte maps 1:1) so the AIM Code ID prefix and
every FNC1 separator reach the parser intact.

## Reporting issues

Bugs and feature requests go to
[GitHub Issues](https://github.com/mgsmith-parsers/gaia-desktop/issues). Include the input string,
what you expected, what GAIA Desktop showed — and, most importantly, a reference to the standard, with its release number and section.

## Licence

[Apache License 2.0](LICENSE) — this covers GAIA Desktop's own source code and documentation only.

## GS1 standards

All GS1 standards, specifications and identification keys are owned by GS1 and remain the
property of GS1. This includes the GS1 General Specifications, GS1 Digital Link, the
Application Identifiers, and identification keys such as GTIN, SSCC, GLN, GRAI and GIAI.
"GS1" and the identification key names are trademarks of GS1 AISBL.

GAIA Desktop is an independent implementation of those published standards. It is not affiliated
with, endorsed by, or certified by GS1. The Apache licence above applies to this application,
not to the standards it implements — for the standards themselves, and for permission to
use them, refer to [gs1.org](https://www.gs1.org/standards).

Using GS1 identification keys in your own products generally requires membership of a GS1
Member Organisation. This application does not grant any such right.
