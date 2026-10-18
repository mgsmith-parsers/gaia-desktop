# GAIA Desktop — Guide for di person wey dey use am

Dis document dey explain how you go take use di app. If na build, change, or add new language you
wan do, check di [developer guide](../../GaiaDesktop-Developer.md) (na English). Di original:
[GaiaDesktop-User.md](../../GaiaDesktop-User.md).

## Wetin dey inside

1. [Wetin be dis](#wetin-be-dis)
2. [How to run am](#how-to-run-am)
3. [Di window](#di-window)
4. [Parse mode](#parse-mode)
   - [Di input bar and di {GS} token](#di-input-bar-and-di-gs-token)
   - [Scanning](#scanning)
   - [Opening file](#opening-file)
   - [History of captures](#history-of-captures)
5. [Build mode](#build-mode)
   - [Di AI editor](#di-ai-editor)
   - [Wetin di editor no go take](#wetin-di-editor-no-go-take)
   - [Element string or Digital Link](#element-string-or-digital-link)
   - [Change capture wey don dey](#change-capture-wey-don-dey)
6. [Combine mode](#combine-mode)
   - [Di Combine strip](#di-combine-strip)
   - [Wetin Combine no go take](#wetin-combine-no-go-take)
   - [Duplicates and conflicts](#duplicates-and-conflicts)
   - [Di combine report](#di-combine-report)
7. [Di report](#di-report)
   - [Export](#export)
8. [Parse options](#parse-options)
   - [Di first time you run am](#di-first-time-you-run-am)
9. [Languages](#languages)
10. [Wetin e dey remember](#wetin-e-dey-remember)
11. [Keyboard shortcuts](#keyboard-shortcuts)
12. [If wahala dey](#if-wahala-dey)
13. [Limits](#limits)

---

## Wetin be dis

GAIA Desktop dey do three things with GS1 data:

- **Parse** — you fit type GS1 element string or Digital Link URI, or paste am, or open am from
  file, or scan am, den read wetin comot as full report: wetin every Application Identifier (AI)
  mean, which place di error dey, and why;
- **Build** — you go join AI and value together make e produce element string or Digital Link URI;
- **Combine** — you go join up to three GS1-128 barcode from one label into di one element
  string wey GS1 DataMatrix go carry, check am as one whole, and draw di symbol.

Everything wey dis app sabi about GS1 — di meaning of every AI, every check rule, every error
message and every language detail — na from di GAIA engine wey dey run under am e dey come. Di app
no add rule of im own: wetin di engine take, na exactly wetin di app go take.

---

## How to run am

```bash
java -jar gaia-desktop.jar
```

Dis JAR file need di `lib/` folder wey dem ship together with am — keep di two of dem for one
place, if not di app no go start. (Dem fit build one JAR file wey complete by itself; check di
[developer guide](../../GaiaDesktop-Developer.md#packaging).)

You need **Java 21 or newer**. Dem dey build di package for one operating system and one processor
type, so use di JAR file wey dem build for your own machine.

If you wan open di app for one particular language but you no wan change di language wey e don
save:

```bash
java -jar gaia-desktop.jar --language=FR
```

Di value fit be catalogue code (`FR`, `PCM`, `ARZ`), full name (`FRENCH`, `WU-CHINESE`), or
language tag (`fr`; for Cantonese `zh-HK`, for Egyptian Arabic `ar-EG`) — big or small letter no
matter. Dis one na for dat one run alone: di language wey you save go remain as e dey, so shortcut
wey you make for your colleague no fit change which language your own copy dey open with. But if
you pick language from *inside* di app, na real choice be dat and e go save like always. Any value
wey no point to any language, di app go leave am with small note for di console, den e go still
open with di language wey e save instead of say e go refuse to run.

Wen e dey start, di console dey print one line wey carry di app version, di date wey dem build am,
and di engine version. Na dat same line you suppose attach if you dey report any problem.

---

## Di window

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

> Like every other code block for dis document, dis drawing keep di English labels. For screen you
> go see *Parse* / *Build* / *Combine*, *Captures*, *Save report*, *Options*, *scanner: off* and *About* —
> for dis language di app labels resemble di drawing pass how e be for other language.

**For macOS and Linux di scanner line for dis drawing no dey at all** — no button for di toolbar,
no list to pick device, no *Scanner* menu, and no *scanner: off* for di status bar. Check
[Scanning](#scanning).

Di **Parse ⇄ Build** switch dey change only di bar wey dey under di toolbar and di controls wey
concern capture. History, report, di options panel and all di export actions dey work for both
modes, because build wey succeed sef just turn to another capture. **Combine** mode dey arrange different: check [Combine mode](#combine-mode).

Di options panel **dey closed di first time you run am**. Di mode, whether di panel show or hide,
di theme and di scanner wey you last use — e go remember all of dem for next time.

**New versions.** Small seconds after e start, once for one day at most, di app dey ask GitHub whether
dem don release newer GAIA Desktop. If dem don release am, one link go show for di status bar —
*GAIA Desktop 1.1.0 don land* — wey go open di release page for your browser; nothing go download or
install for your side. *Skip dis version* dey hide di notice until another release. Di check no dey
talk anything when nothing new dey or when e no fit reach GitHub. *Help → Check for new update…*
dey check sharp-sharp and tell you wetin e see; *Help → Dey check for update by itself* dey off di daily
check and on am back.

---

## Parse mode

### Di input bar and di {GS} token

Dis bar dey take element string, GS1 Digital Link URI, or raw scan wey carry AIM Code ID prefix
(`]d2…`). <kbd>Enter</kbd> go start di parse.

Wetin dey separate AI wey length no fixed na **FNC1** — di control character wey person no fit see,
`0x1D`. No text field fit hold dis character: e dey quiet quiet disappear, so `01…` + FNC1 +
`10LOT` go reach there as `01…10LOT` — wey be element string wey di meaning don change.

Na why di app dey show di separator as token wey you fit see, **`{GS}`**, den e dey change am back
as e dey go meet di parser:

- Di **`{GS}` button** dey put di token where di cursor dey;
- Wen you **paste** di real separator character — from keyboard, from di right-click menu, or by
  dragging am — dem go turn to `{GS}` by demself, so nothing dey lost;
- Dem dey write di token exactly how di report dey write am, so value wey you copy from di report
  you fit paste am back straight.

Di captures, di report and di history wey dem save — all three dey carry di **real** separator. Di
token na only wetin you dey see and wetin you dey type.

### Scanning

> **Na Windows alone.** Zebra dey release dia scanner driver for Windows alone, so for macOS and
> Linux di app no dey create any scanner control at all — no *Connect scanner* button, no device
> list, no *Scanner* menu, no scanner status for di status bar. Dem no dey show anything for grey
> either, because no setting or device dey wey fit turn am on. Everything else, including opening
> file wey full with scan wey dem save, dey work everywhere.

*Connect scanner* dey open di device wey dem name for di list; `ZebraAllScanners` na di default and
e dey fit most models. Every scan dey parse and e dey enter di history by imself.

Dem dey read di scanner data byte by byte, so di AIM Code ID prefix and every FNC1 separator dey
reach di parser exactly di way di scanner send dem.

For build mode dem dey hide di scanner controls, but scanner wey connect go still dey work:
*Scanner → Connect / Disconnect* (<kbd>⌘K</kbd>) still dey switch am, di status bar still dey
talk, and scan wey enter while di editor open dem go still capture am. For Combine mode di controls go still dey, and every scan go enter di combination instead of
di history.

How to set up scanner for di first time — di Zebra Scanner SDK, di scanner USB mode, and di GS1
settings — dey explain step by step for di [Zebra scanner setup guide](GaiaDesktop-Zebra-Scanner-Windows-NigerianPidgin.md), wey dey inside every
zip under `docs/`. If no device gree connect, check [If wahala dey](#if-wahala-dey).

### Opening file

*File → Open text file…* dey parse **every line wey no empty as im own separate capture** — na di
fastest way to clear plenty scan wey person send give you. Dem dey read di file byte by byte, so
di real separator wey dey inside am dey survive too.

### History of captures

New one dey on top. Every line dey show one dot for valid / invalid, one line summary, and beside
dem di time of di capture, where e come from, and how many AI dey inside. Date dey show only for
capture of di days wey don pass; today own line dey carry only time.

For Digital Link di summary na di **Digital Link URI**, and for every other thing na **HRI**: for
element string, to read `(01)09506000134352 (17)261231` easy pass to read
`01095060001343521726123110…`.

If you right-click one capture you fit copy im input or im HRI, send am back go di input bar, open
am for di editor, remove am, or clear di whole history.

---

## Build mode

### Di AI editor

Na table of AI and value wey dey pair. Type AI code, or pick from di *Add one AI…* list among
**every AI wey di engine sabi**; as you dey type, di **Title**, **Format** and **Description**
columns dey fill for di language wey you pick. Di list no dey offer AI wey dem go reject wen you
build.

For di end of di table one **empty row** dey always: write something inside am and di next one go
show, so you no go ever need *Add row* for string wey get plenty AI.

Editing dey behave di way table suppose behave: <kbd>Enter</kbd>, <kbd>Tab</kbd>,
<kbd>Shift</kbd>+<kbd>Tab</kbd> and clicking another place — all of dem dey **keep** wetin you
type; <kbd>Esc</kbd> dey throw am away. <kbd>Tab</kbd> dey move from AI go value den go di next
row, and e dey jump di columns wey you no fit edit.

### Wetin di editor no go take

| | Wetin dey happen |
|---|---|
| AI wey di engine no sabi | Di row dey turn red as you dey type; dem go reject di build with explanation |
| Di same AI second time from di list | E no go enter — di message go call im name, and di row wey already carry am go select |
| Di same AI second time wey you type by hand | Dem go mark di two rows; dem go reject di build with `GE-S004` error |
| Nothing dey to build | One message; no capture dey created |

AI fit show only once inside one element string — na why di list dey refuse sharp sharp instead of
say e go let you try build wey don already fail.

### Element string or Digital Link

**Build element string** dey join di rows together and e dey put FNC1 separator for every place wey
di standard want am. **Build Digital Link** dey produce URI; if you wan point am to your own
resolver instead of di standard `id.gs1.org`, open *Digital Link target*:

| Field | Wetin e dey do | If you leave am empty |
|---|---|---|
| Domain | di resolver host, like `example.com` | `id.gs1.org` |
| Path prefix | one part before di AI path, like `/products` | No prefix |

If e **succeed**, di result dey behave exactly like scan: dem go parse am, e go join di history,
dem go show am as report and dem go save am. Di build dey check im own result by parsing am, so
wetin e give back must be valid.

If e **fail**, nothing dey captured. Di reason dey remain under di build buttons — one line for
every problem, and each one carry di error, di AI wey concern, and di engine own explanation:

```
GE-C003  (01)  Check digit validation failed for AI (01) value ''09506000134350''
```

> Di double quote wey dey inside dat message no be dis app put am — na di engine own error
> catalogue be dat.

### Change capture wey don dey

Right-click one capture → **Build** dey carry am enter di AI editor. Change one value, build am
again, and di result go show as new capture — di original capture go remain as e dey.

---

## Combine mode

Old labels dey often share their data inside **plenty GS1-128 barcodes** — di GTIN for one, di expiry
date and batch for another one — because one straight-line barcode no fit carry everything.
Combine mode dey take up to **three** of those barcodes, join dem into di one element string
wey **GS1 DataMatrix** go carry, and check di result as one whole. Once e correct, di app go draw di
DataMatrix symbol.

None of those barcodes need to complete by itself. Batch number wey no get im GTIN go fail by itself
with `GE-S005`, but dat one normal for one barcode inside a set, so Combine mode dey check am
on top di combination.

### Di Combine strip

When you switch to **Combine**, one strip for di bottom of di window go replace di input bar.
Di capture history go comot for road, because Combine mode scans no be captures and dem no dey
ever enter am. Di report area go show di combine report instead:

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

Two ways dey to bring barcode enter:

- **Scan am.** For Windows, di scanner controls go still dey di toolbar for Combine mode, and
  every scan go straight enter di combination, no be di history.
- **Type or paste am** inside di *Barcode* field, then press <kbd>Enter</kbd> or
  **Add**. Write FNC1 as **`{GS}`**, like for di input bar. Barcode wey you type without
  AIM Code ID, we go take am as GS1-128.

| Button | Wetin e dey do |
|---|---|
| **Add** | E dey add di barcode wey dey di field |
| **Comot di last one** | E dey comot di newest barcode (you no fit use am when none dey) |
| **Start am again** | E dey comot all di barcodes, clear di field and any refusal, and start again |

Di status bar go always talk how many barcodes dey hand (*2 out of 3 GS1-128 barcode*), or why dem refuse
di last one.

### Wetin Combine no go take

Barcode wey dem refuse no go add. Di report and di status bar go both talk why:

| Barcode | Dem refuse am because |
|---|---|
| Empty field | Nothing dey to add |
| Fourth barcode | Na three at most we dey combine one label from. Comot di last one, or start again |
| Another symbology, by im AIM Code ID | Na only GS1-128 we dey combine, so GS1 DataMatrix (`]d2`) or QR Code go get refusal, with im name |
| AIs wey no fit read | Syntax error; di engine error code and message go show |
| No AI at all | Nothing dey to combine |
| One wey you don scan before | Di same element string two times, like when you scan di same barcode again; di report go talk which barcode before e dey repeat |

Combine mode dey refuse only wetin e no fit use at all. Every other thing (wrong check digit,
bad date, companion AI wey miss) we go take am and judge am on top di combination.

### Duplicates and conflicts

We dey combine di AIs for di order wey you scan dem. When two barcodes carry di same AI:

- **with di same value**, we go keep am once and list am under *Duplicates — we keep am once*, with di
  barcodes wey we read am from;
- **with different values**, na **conflict** be dat. Di verdict go read *DEM NO GREE*, di values
  wey dey fight go show barcode by barcode, and **nothing go combine** until you settle am. Comot di
  barcode wey get wahala, or start again.

### Di combine report

From top go bottom:

1. **Di verdict**: *no scan*, *DEM NO GREE*, valid or invalid, and how many
   barcodes dey hand.
2. **Di scans**: every barcode as e land, im symbology, and di element string wey remain once we comot
   di AIM Code ID. Problem wey na only di combination fit settle go show for di barcode itself, with di
   mark *we go judge am for di combination*.
3. **Duplicates** and **conflicts**, if dem dey.
4. **Wetin combine**: di element string, im HRI and im length, then di combined input as tape
   wey get label and one card for every AI, both arrange like for di [parse report](#di-report).
   After dat, every error and warning on top di combination go list under *Validation*.
5. **Di GS1 DataMatrix symbol**, with im HRI under am. We dey draw am only when di combination valid.
   If e no valid, di report go talk why we no draw am, so nobody go print bad symbol by mistake.

For example, one label wey get two barcodes:

| Barcode | Wetin e carry | By itself |
|---|---|---|
| `]C10109506000134352` | (01) GTIN | valid |
| `]C11726123110LOT-ABC` | (17) expiry date, (10) batch | `GE-S005` for both AIs, we go judge am for di combination |

Dem go combine into `01095060001343521726123110LOT-ABC`, HRI
`(01)09506000134352 (17)261231 (10)LOT-ABC`, wey valid, so di symbol go draw. AI `17` get fixed length,
so e no need FNC1 after am. Combine mode dey put one anywhere di standard need am.

Once one barcode dey hand, **Save report** go export di combine report as HTML or PDF, di same way
like parse report (check [Exporting](#export)). Di HTML file name dey start with `gaia-combine-`.

Di report dey follow di language and theme wey you choose. If you change parse option or di language,
e go check di barcodes wey dey hand again. Dem go still dey if you switch go Parse or
Build and come back, but no be after restart: Combine mode barcodes no dey save.

---

## Di report

Dem dey show di capture wey you select as full report: di verdict bar, di element string wey dem
divide into “band” wey get name, di parse metadata, one card for every AI with im meaning, and di
errors and warnings with guide on how to fix dem.

For **Digital Link**, every AI card dey talk im structure role too — **Primary key**,
**Key qualifier** or **Data attribute** — with colour wey follow di order wey dem dey appear inside
di URI. Na for Digital Link alone you dey see dis roles: for ordinary element string di AI no get
any place for URI wey person go describe.

Di report dey follow di language and theme wey you pick.

### Export

| | |
|---|---|
| **Save report → As HTML…** (<kbd>⌘S</kbd>) | E dey write exactly wetin dey screen: file wey complete by itself, wey any browser fit open |
| **Save report → As PDF…** (<kbd>⌘P</kbd>) | E dey print di report through di system print dialog — pick di PDF destination for there |

PDF dey pass through di print dialog because na for there dem dey pick di place and di file name:
for macOS **PDF ▾ → Save as PDF**, for Windows **Microsoft Print to PDF**, for Linux
**Print to File**. Di pages dey divide well and di text still dey selectable.

Print and PDF dey always use di light theme, even if di app dey for dark theme, and dem dey keep
di AI marks wey get colour and di verdict bar as dem be.

If your machine no get printer or PDF destination wey dem set at all, di app go tell you and e go
point you go di HTML export, wey you fit print from any browser.

---

## Parse options

Di panel for right side dey gather all di engine options for one place. If you change anything,
**dem go parse di capture wey you select again sharp sharp**, so dis panel no dey affect only di
next scan — na also way to ask: “How dis input go look with dis settings?”.

| Option | Values |
|---|---|
| Parse mode | `SYNTAX`, `CONTENT`, `DATA_CARRIER`, `INTERPRETATION` — how deep di parse go go |
| Language | 35 message catalogue |
| Order | `LITTLE` (dd/mm/yyyy), `MIDDLE` (mm/dd/yyyy), `BIG` (yyyy/mm/dd) |
| Separator | `SLASH`, `HYPHEN`, `PERIOD` |
| Month | `TWO_DIGIT` (12), `THREE_LETTER` (DEC) |
| Year | `FOUR_DIGIT` (2026), `TWO_DIGIT` (26) |
| Skip di "requires" check | E go stop to reject AI wey im compulsory partner no dey (`GE-S005`) |
| Skip di "excludes" check | E go stop to reject AI pairs wey di standard forbid (`GE-S006`) |

For di bottom of di panel, *Put am back to default* dey bring back di engine own default values.

### Di first time you run am

As long as nothing never save, di app dey take im settings from **di regional settings of your
machine** instead of from di engine defaults:

- **Language** dey come from di system locale, and e dey fall back to English where catalogue no
  dey;
- **Di date options** dem dey read from di short date format of your locale: di order of di parts,
  di separator, and whether dem dey shorten month and year or not.

> **Take note.** So locale wey dey write short date with two-digit year go start here too with
> two-digit year: dis one match di rest of your system, but e different from di engine own
> four-digit default. Change am for di panel, and di choice go stay.

---

## Languages

To pick language no be only di error messages e dey change, na **di whole app** — di menus, di
buttons, di editor, di options panel, di status line and di report. Language wey dem dey write from
right to left dey flip di window and di report.

All 35 languages dey available, with two layer of coverage:

| | Coverage |
|---|---|
| Parse reports and all di engine error messages | **All 35 languages** |
| Di app own menus, buttons and labels | **All 35 languages** |

Di languages wey dem dey write from right to left — Arabic, Egyptian Arabic, Persian and Urdu —
dey flip di window and di report from di moment wey di app open.

E dey remember di language for next times. If na only one time you wan open with another language,
check [`--language`](#how-to-run-am).

Some things dem no dey translate on purpose for any language: di short field names wey dem dey
print beside di raw values (`format`, `position`, `raw`, `len` …) — na identifier dem be, no be
word; di names of pattern and symbology — na GS1 and industry vocabulary; and one long paragraph
from di standard, wey half translation go confuse pass how e go explain.

---

## Wetin e dey remember

Two file inside `.gaia-desktop` for your home folder:

| File | Wetin dey inside |
|---|---|
| `options.properties` | All di parse options, di theme, whether di options panel open, di scanner wey you last use, which mode you leave di window for, and di update check settings — on or off, when e last run, and any version wey you choose to skip |
| `captures.tsv` | Di history of captures, new one first, up to 500 |

Any time you change one option, dem dey write am; any time di history change, dem dey write am. Dem
dey write di two again wen you comot.

If you delete one of dis two files, na only dat part go go back to im first state and nothing else
— na why dem separate dem, so dat history wey spoil no go carry your settings enter inside. Even
file wey spoil because person edit am by hand no be death sentence: every setting dey fall back to
im own default one by one, so di price of one bad line na just dat line.

Di history dey save di input, no be di parse. Dem dey parse am again wen e dey load, meaning say di
capture wey come back dey show di options wey dey work **now**, no be di ones wey dey work wen dem
capture am.

Di barcodes wey dey hand for Combine mode no dey save: after restart, Combine mode go
start empty.

---

## Keyboard shortcuts

| | |
|---|---|
| <kbd>Enter</kbd> | Parse di input bar |
| <kbd>⌘O</kbd> | Open text file |
| <kbd>⌘S</kbd> | Save report as HTML |
| <kbd>⌘P</kbd> | Save report as PDF (system print dialog) |
| <kbd>⌘G</kbd> | Put di FNC1 separator token |
| <kbd>⌘K</kbd> | Connect / Disconnect scanner (na Windows alone) |
| <kbd>⌘D</kbd> | Switch di dark theme |
| <kbd>⌘Q</kbd> | Comot |

For di editor: <kbd>Tab</kbd> / <kbd>Shift</kbd>+<kbd>Tab</kbd> dey move between di fields wey you
fit edit, <kbd>Enter</kbd> dey confirm, <kbd>Esc</kbd> dey cancel.

For Combine mode: <kbd>Enter</kbd> inside di *Barcode* field dey add di
barcode. For there, type FNC1 as `{GS}`; <kbd>⌘G</kbd> na for di Parse mode input bar.

---

## If wahala dey

| Wetin you see | Why and how to fix am |
|---|---|
| Di app no dey start, or e dey give `NoClassDefFoundError` | Di `lib/` folder must dey beside di JAR file. Keep dem for one place, or use di package wey complete by itself |
| For di console wen e dey start: *“Unsupported JavaFX configuration…”* | Dem expect am and e no dey harm anything — na di toolkit dey just talk how dem load am. Di app dey work normal |
| Di separator wey you paste be like say e don disappear | Na so e suppose be: e dey turn to di `{GS}` token wey you fit see, and na di real character di parser dey receive. Check [di {GS} token](#di-input-bar-and-di-gs-token) |
| Di *Connect scanner* button no dey at all | You dey for macOS or Linux, where scanner driver no dey. Check [Scanning](#scanning) |
| Di scanner no gree connect | Check say di logical name for di list match your model, say di scanner dey plugged, and say no other app hold am. Di [Zebra scanner setup guide](GaiaDesktop-Zebra-Scanner-Windows-NigerianPidgin.md#11-when-wahala-dey) list every error code. |
| PDF export talk say no destination dey | No printer or PDF destination dey set for your machine. Add one, or export as HTML den print from browser |
| Something like `act.hri` dey show for one menu item | One label miss — abeg report am; dis one no be setting, na defect from di time dem build am |

---

## Limits

- **Scanning na for Windows alone**, because na there di drivers dey.
- **PDF dey pass through di print dialog**, no be through save dialog.
- **Digital Link target dey take only domain and path prefix**; no extra query parameter and no
  fragment.
- **AI dey show only once** inside one element string — na GS1 rule be dat, no be limit of di app.
- **Combine mode dey take only GS1-128 barcodes, three at most**, and e no dey keep dem from
  one run to another.
