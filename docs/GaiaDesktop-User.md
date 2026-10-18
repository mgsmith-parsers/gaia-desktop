# GAIA Desktop — User Guide

How to use the application. For building it, changing it, or adding a language, see the
[Developer Guide](GaiaDesktop-Developer.md).

## Table of Contents

1. [What it is](#what-it-is)
2. [Running it](#running-it)
3. [The window](#the-window)
4. [Parse mode](#parse-mode)
   - [The input bar and the {GS} token](#the-input-bar-and-the-gs-token)
   - [Scanning](#scanning)
   - [Opening a file](#opening-a-file)
   - [The capture history](#the-capture-history)
5. [Build mode](#build-mode)
   - [The AI editor](#the-ai-editor)
   - [What the editor refuses](#what-the-editor-refuses)
   - [Element string or Digital Link](#element-string-or-digital-link)
   - [Editing an existing capture](#editing-an-existing-capture)
6. [Combine mode](#combine-mode)
   - [The Combine strip](#the-combine-strip)
   - [What Combine refuses](#what-combine-refuses)
   - [Duplicates and conflicts](#duplicates-and-conflicts)
   - [The Combine report](#the-combine-report)
7. [The report](#the-report)
   - [Exporting](#exporting)
8. [Parser options](#parser-options)
   - [First launch](#first-launch)
9. [Languages](#languages)
10. [What is remembered](#what-is-remembered)
11. [Keyboard shortcuts](#keyboard-shortcuts)
12. [Troubleshooting](#troubleshooting)
13. [Limits](#limits)

---

## What it is

GAIA Desktop does three things with GS1 data:

- **Parse** — type, paste, open, or scan a GS1 element string or Digital Link URI and read the
  result as a full report: what each Application Identifier means, what is wrong with it, and
  why;
- **Build** — assemble AI / value pairs into an element string or a Digital Link URI;
- **Combine** — join up to three GS1-128 barcodes from one label into the single element string
  a GS1 DataMatrix would carry, check it as a whole, and draw the symbol.

Everything it knows about GS1 — every AI definition, validation rule, error message and
localized description — comes from the GAIA engine underneath it. The app adds no rules of its
own, so what it accepts is exactly what the engine accepts.

---

## Running it

```bash
java -jar gaia-desktop.jar
```

The jar needs the `lib/` directory that ships beside it — keep the two together, or the app
will not start. (A single self-contained jar can be built instead; see the [Developer
Guide](GaiaDesktop-Developer.md#packaging).)

**Java 21 or newer** must be installed. The build is specific to one operating system and
processor architecture, so use the jar built for your machine.

To open in a particular language, without changing the language you have saved:

```bash
java -jar gaia-desktop.jar --language=FR
```

The value can be the catalogue code (`FR`, `PCM`, `ARZ`), the full name (`FRENCH`,
`WU-CHINESE`), or a language tag (`fr`, `zh-HK` for Cantonese, `ar-EG` for Egyptian Arabic) —
any capitalisation. The override lasts for that run only: your saved language is untouched, so
a shortcut for a colleague cannot change what your own copy opens in. Pick a language *inside*
the app and that is a real choice, saved as usual. A value naming no language is ignored with a
note in the console, and the app opens in the saved language rather than refusing to start.

At start-up the console prints one line naming the app version, its build date and the engine
version. That is the line to quote in a bug report.

---

## The window

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

**On macOS and Linux there is no scanner row in that picture** — no toggle or device picker in
the toolbar, no Scanner menu, no `scanner: off` in the status bar. See [Scanning](#scanning).

The **Parse ⇄ Build** switch changes only the strip under the toolbar and the capture-side
controls. The history, the report, the options panel and the export actions serve both modes,
because a successful build simply becomes another capture. **Combine** is laid out differently:
see [Combine mode](#combine-mode).

The options panel is **closed on a first launch**. The mode, the panel's visibility, the theme
and the last scanner device are all remembered between runs.

**New versions.** A few seconds after it starts, at most once a day, the app asks GitHub whether
a newer GAIA Desktop has been released. If one has, a link appears in the status bar — *GAIA
Desktop 1.1.0 is available* — that opens the release page in your browser; nothing is downloaded
or installed for you. *Skip this version* hides the notice until a later release. The check is
silent when there is nothing new or when it cannot reach GitHub. *Help → Check for updates…*
checks straight away and says what it found; *Help → Check for updates automatically* turns the
daily check off and on.

---

## Parse mode

### The input bar and the {GS} token

The bar accepts an element string, a GS1 Digital Link URI, or a raw scan including its AIM Code
ID prefix (`]d2…`). <kbd>Enter</kbd> parses.

Variable-length AIs are separated by **FNC1** — the invisible control character `0x1D`. A text
field cannot hold that character: it would be silently swallowed, and `01…` + FNC1 + `10LOT`
would arrive as `01…10LOT`, an element string that means something different.

So the app shows the separator as the visible token **`{GS}`** and converts it back on the way
to the parser:

- the **`{GS}` button** inserts the token wherever the cursor is;
- **pasting** real separator characters — by keyboard, context menu, or drag and drop — turns
  them into `{GS}` automatically, so nothing is lost;
- the token is written the same way the report writes it, so a value copied out of a report can
  be pasted straight back in.

The capture, the report and the saved history all hold the **real** separator. The token is
only what you see and type.

### Scanning

> **Windows only.** Zebra ships its scanner drivers for Windows, so on macOS and Linux the app
> builds no scanner controls at all — no *Connect scanner* toggle, no device picker, no Scanner
> menu, and no scanner state in the status bar. Nothing is greyed out, because there is no
> setting or device that could switch it on. Everything else, including opening a file of
> captured scans, works everywhere.

*Connect scanner* opens the device named in the picker; `ZebraAllScanners` is the default and
works for most models. Each scan is parsed and appended to the history automatically.

Scanner data is read byte for byte, so the AIM Code ID prefix and every FNC1 separator reach
the parser exactly as the scanner sent them.

The scanner controls are hidden in Build mode, but a connected scanner keeps working:
*Scanner → Connect/disconnect* (<kbd>⌘K</kbd>) still toggles it, the status bar still reports
it, and a scan arriving while the builder is open is still captured. In Combine mode the
controls stay, and each scan is added to the combination instead of the history.

Setting up a scanner for the first time — the Zebra Scanner SDK, the scanner's USB mode, and the
GS1 settings — is covered step by step in the
[Zebra scanner setup guide](GaiaDesktop-Zebra-Scanner-Windows.md), shipped in every zip under
`docs/`. If a device will not connect, see also [Troubleshooting](#troubleshooting).

### Opening a file

*File → Open text file…* parses **each non-blank line as its own capture**, which is the quick
way to work through a batch of scans someone has sent you. The file is read byte for byte, so
raw separators inside it survive.

### The capture history

Newest first. Each row shows a valid/invalid dot, a one-line summary, and the capture time,
where it came from, and how many AIs it holds. The date appears only on captures from an
earlier day — today's rows show just the time.

The summary is the **Digital Link URI** for a Digital Link, and the **HRI** otherwise: for an
element string, `(01)09506000134352 (17)261231` is easier to read than
`01095060001343521726123110…`.

Right-click a capture to copy its input or HRI, send it back to the input bar, open it in the
builder, remove it, or clear the history.

---

## Build mode

### The AI editor

A table of AI / value pairs. Type an AI code or pick from **every AI the engine knows** in the
*Add an AI…* list; the **Title**, **Format** and **Description** columns fill themselves in as
you type, in your chosen language. The picker cannot offer an AI the builder would refuse.

The table always keeps **one blank row at the end**: type in it and the next appears, so a
multi-AI string never needs *Add row*.

Editing behaves the way a table should: <kbd>Enter</kbd>, <kbd>Tab</kbd>,
<kbd>Shift</kbd>+<kbd>Tab</kbd> and clicking away all **keep** what you typed;
<kbd>Escape</kbd> discards it. <kbd>Tab</kbd> moves AI → Value → next row, skipping the columns
you cannot edit.

### What the editor refuses

| | What happens |
|---|---|
| An AI the engine does not recognise | The row turns red as you type; the build is refused with an explanation |
| The same AI twice, from the picker | Not added — a message names it, and the row that already has it is selected |
| The same AI twice, typed by hand | Both rows are marked; the build is refused with error `GE-S004` |
| Nothing to build | A message; no capture is created |

An AI may appear only once in an element string, which is why the picker refuses outright
rather than letting a doomed build be attempted.

### Element string or Digital Link

**Build element string** assembles the rows, inserting the FNC1 separator wherever the standard
requires one. **Build Digital Link** produces a URI; expand *Digital Link target* to point it
at your own resolver instead of the canonical `id.gs1.org`:

| Field | Effect | Blank means |
|---|---|---|
| Domain | The resolver host, e.g. `example.com` | `id.gs1.org` |
| Path prefix | A segment before the AI path, e.g. `/products` | no prefix |

On **success** the result behaves exactly like a scan: parsed, added to the history, rendered
as a report, and saved. The builder validates by parsing its own output, so anything it hands
back is guaranteed to be valid.

On **failure** nothing is captured. The reason stays under the build buttons, one line per
problem, each naming the error, the AI it belongs to, and the engine's own explanation:

```
GE-C003  (01)  Check digit validation failed for AI (01) value ''09506000134350''
```

> The doubled quotes in that message come from the engine's error catalogue, not from this app.

### Editing an existing capture

Right-click a capture → **Build** loads its AIs into the editor. Change a value, rebuild, and
the result lands as a new capture — the original is left untouched.

---

## Combine mode

Older labels often spread their data over **several GS1-128 barcodes** — the GTIN in one, the
expiry date and batch in another — because a single linear barcode cannot hold it all. Combine
takes up to **three** of those barcodes, joins them into the one element string a **GS1
DataMatrix** would carry, and checks the result as a whole. Once it is valid, the app draws the
DataMatrix symbol.

None of those barcodes needs to be complete on its own. A batch number without its GTIN fails
on its own with `GE-S005`, but that is normal for one barcode of a set, so Combine checks it
against the combination instead.

### The Combine strip

Switching to **Combine** replaces the input bar with a strip along the bottom of the window.
The capture history steps aside, because Combine scans are not captures and never enter it. The
report area shows the Combine report instead:

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

There are two ways to bring in a barcode:

- **Scan it.** On Windows the scanner controls stay in the toolbar in Combine mode, and each
  scan goes straight into the combination rather than the history.
- **Type or paste it** into the *Barcode* field and press <kbd>Enter</kbd> or **Add**. Write
  FNC1 as **`{GS}`**, as in the input bar. A barcode typed without an AIM Code ID is taken as
  GS1-128.

| Button | Does |
|---|---|
| **Add** | Adds the barcode in the field |
| **Remove Last** | Drops the most recent barcode (unavailable when there are none) |
| **Reset** | Drops every barcode, clears the field and any refusal, and starts again |

The status bar always says how many barcodes are held (*2 of 3 GS1-128 barcodes*), or why the
last one was refused.

### What Combine refuses

A refused barcode is not added. The report and the status bar both say why:

| Barcode | Refused because |
|---|---|
| An empty field | There is nothing to add |
| A fourth barcode | Three is the most a label is combined from. Remove the last one, or reset |
| Another symbology, by its AIM Code ID | Only GS1-128 is combined, so a GS1 DataMatrix (`]d2`) or a QR Code is refused, with its name |
| AIs that cannot be read | A syntax error; the engine's error code and message are shown |
| No AIs at all | There is nothing to combine |
| One already scanned | The same element string twice, for example the same barcode scanned again; the report says which earlier barcode it repeats |

Combine refuses only what it cannot use at all. Everything else (a wrong check digit, a bad
date, a missing companion AI) is accepted and judged on the combination.

### Duplicates and conflicts

The AIs are combined in the order they were scanned. When two barcodes carry the same AI:

- **with the same value**, it is kept once and listed under *Duplicates — kept once*, with the
  barcodes it was read in;
- **with different values**, it is a **conflict**. The verdict reads *CONFLICT*, the clashing
  values are listed barcode by barcode, and **nothing is combined** until it is resolved.
  Remove the barcode at fault, or reset.

### The Combine report

Top to bottom:

1. **The verdict**: *no scans*, *CONFLICT*, valid or invalid, and how many barcodes are held.
2. **The scans**: each barcode as it arrived, its symbology, and the element string left once
   the AIM Code ID is stripped. A problem that only the combination can settle is shown on the
   barcode, marked *judged on the combination*.
3. **Duplicates** and **conflicts**, when there are any.
4. **Combined**: the element string, its HRI and its length, then the combined input as a
   labelled tape and one card per AI, both laid out as in the [parse report](#the-report). Every
   error and warning on the combination is then listed under *Validation*.
5. **The GS1 DataMatrix symbol**, with its HRI beneath. It is drawn only when the combination
   is valid. Otherwise the report says why it was not drawn, so an invalid symbol cannot be
   printed by mistake.

For example, a label with two barcodes:

| Barcode | Carries | On its own |
|---|---|---|
| `]C10109506000134352` | (01) GTIN | valid |
| `]C11726123110LOT-ABC` | (17) expiry date, (10) batch | `GE-S005` for both AIs, judged on the combination |

These combine into `01095060001343521726123110LOT-ABC`, HRI
`(01)09506000134352 (17)261231 (10)LOT-ABC`, which is valid, so the symbol is drawn. AI `17` is
fixed-length, so no FNC1 is needed after it. Combine inserts one wherever the standard requires
it.

**Save report** exports the Combine report as HTML or PDF, the same way as a parse report (see
[Exporting](#exporting)), as soon as one barcode is held. The HTML file name starts
`gaia-combine-`.

The report follows the chosen language and theme. Changing a parser option or the language
re-checks the barcodes already held. They also survive a switch to Parse or Build and back, but
not a restart: Combine barcodes are not saved.

---

## The report

The selected capture is rendered as a full report: a verdict banner, the element string broken
into a labelled "tape", the parse metadata, one card per AI with its interpretations, and any
errors and warnings with guidance on fixing them.

For a **Digital Link**, each AI card also carries its structural role — **primary key**, **key
qualifier** or **data attribute** — colour-coded in the order they appear in the URI. Roles are
shown only for a Digital Link; in a plain element string an AI has no URI position to describe.

The report follows the language and theme you have chosen.

### Exporting

| | |
|---|---|
| **Save report → As HTML…** (<kbd>⌘S</kbd>) | Writes exactly what is on screen: one self-contained file that opens in any browser |
| **Save report → As PDF…** (<kbd>⌘P</kbd>) | Prints the report through your system's print dialog — choose a PDF destination there |

PDF goes through the print dialog because that is where the destination and file name are
chosen: **PDF ▾ → Save as PDF** on macOS, **Microsoft Print to PDF** on Windows, **Print to
File** on Linux. The pages are paginated and the text stays selectable.

Printed and PDF output always uses the light palette, even when the app is in dark mode, and
keeps the coloured AI chips and verdict banner intact.

If your machine has no printer or PDF destination configured at all, the app says so and points
you at the HTML export, which prints from any browser.

---

## Parser options

The panel on the right is the full set of engine options. Changing anything **re-parses the
selected capture immediately**, so it doubles as a way to ask "what would this input look like
under these settings?" rather than only affecting the next scan.

| Option | Values |
|---|---|
| Parse mode | `SYNTAX`, `CONTENT`, `DATA_CARRIER`, `INTERPRETATION` — how deep to take the analysis |
| Language | 35 message catalogues |
| Date order | `LITTLE` (dd/mm/yyyy), `MIDDLE` (mm/dd/yyyy), `BIG` (yyyy/mm/dd) |
| Date separator | `SLASH`, `HYPHEN`, `PERIOD` |
| Month | `TWO_DIGIT` (12), `THREE_LETTER` (DEC) |
| Year | `FOUR_DIGIT` (2026), `TWO_DIGIT` (26) |
| Skip "requires" check | Stops rejecting an AI whose required companion is missing (`GE-S005`) |
| Skip "excludes" check | Stops rejecting AI pairings the standard forbids (`GE-S006`) |

*Reset to defaults* at the foot of the panel restores the engine's own defaults.

### First launch

With nothing saved yet, the app takes its settings from **your machine's regional settings**
rather than from the engine's defaults:

- the **language** comes from your system locale, falling back to English where there is no
  catalogue for it;
- the **date options** are read from your locale's short date format — the component order, the
  separator, and whether the month and year are abbreviated.

> **Note.** A locale that writes short dates with two-digit years therefore starts with
> two-digit years here, matching what the rest of your system shows but differing from the
> engine's own four-digit default. Change it in the panel and the choice sticks.

---

## Languages

Choosing a language switches **the whole application** — menus, buttons, the builder, the
options panel, the status line and the report — not just the error messages. Right-to-left
languages flip the window and the report.

All 35 languages are available, with two layers of coverage:

| | Coverage |
|---|---|
| Parse reports and all engine error messages | **all 35 languages** |
| The application's own menus, buttons and labels | **all 35 languages** |

Right-to-left languages — Arabic, Egyptian Arabic, Persian and Urdu — flip the window and the
report, from the moment the application opens.

The language is remembered between runs. To open in another language just once, see
[`--language`](#running-it).

A few things stay untranslated everywhere on purpose: the short field names printed beside raw
values (`format`, `position`, `raw`, `len` …), which are identifiers rather than words; the
sample input names and symbology names, which are GS1 and industry terms; and one long
standards paragraph where a half-translation would mislead.

---

## What is remembered

Two files in `.gaia-desktop` inside your home directory:

| File | Holds |
|---|---|
| `options.properties` | Every parser option, the theme, whether the options panel is open, the last scanner device, the mode the window was left in, and the update check's settings — on or off, when it last ran, and any version you chose to skip |
| `captures.tsv` | The capture history, newest first, capped at 500 entries |

Options are written whenever you change one; the history whenever it changes. Both are written
again on exit.

Deleting either file resets that part and nothing else — the two are kept separate so a
damaged history cannot cost you your settings. A file damaged by hand-editing is not fatal
either: each setting falls back individually, so one bad line costs that line alone.

The history stores the input, not the analysis. It is re-parsed when it loads, which means a
restored capture reflects the options in force **now**, not the ones in force when it was
captured.

The barcodes held in Combine mode are not saved: a restart starts Combine empty.

---

## Keyboard shortcuts

| | |
|---|---|
| <kbd>Enter</kbd> | Parse the input bar |
| <kbd>⌘O</kbd> | Open a text file |
| <kbd>⌘S</kbd> | Save the report as HTML |
| <kbd>⌘P</kbd> | Save the report as PDF (system print dialog) |
| <kbd>⌘G</kbd> | Insert the FNC1 separator token |
| <kbd>⌘K</kbd> | Connect / disconnect the scanner (Windows only) |
| <kbd>⌘D</kbd> | Toggle the dark theme |
| <kbd>⌘Q</kbd> | Quit |

In the builder: <kbd>Tab</kbd> / <kbd>Shift</kbd>+<kbd>Tab</kbd> move between editable cells,
<kbd>Enter</kbd> commits, <kbd>Escape</kbd> cancels.

In Combine mode: <kbd>Enter</kbd> in the *Barcode* field adds the barcode. There, type FNC1 as
`{GS}`; <kbd>⌘G</kbd> belongs to the Parse input bar.

---

## Troubleshooting

| Symptom | Cause and fix |
|---|---|
| The app will not start, or reports `NoClassDefFoundError` | The `lib/` directory must sit beside the jar. Keep them together, or use a self-contained build |
| *"Unsupported JavaFX configuration…"* in the console at start-up | Expected and harmless — the toolkit noting how it was loaded. The app runs normally |
| A separator you pasted seems to vanish | Expected: it is rewritten to the visible `{GS}` token, and the parser still receives the real character. See [the {GS} token](#the-input-bar-and-the-gs-token) |
| There is no *Connect scanner* button | You are on macOS or Linux, where the scanner drivers do not exist. See [Scanning](#scanning) |
| The scanner will not connect | Check that the logical name in the picker matches your model, that the scanner is attached, and that no other application is holding it. The [Zebra scanner setup guide](GaiaDesktop-Zebra-Scanner-Windows.md#11-troubleshooting) lists each error code |
| PDF export says there is no destination | Your machine has no printer or PDF destination configured. Add one, or export HTML and print from a browser |
| A menu item shows something like `act.hri` | A missing label — please report it; it is a build-time bug, not a setting |

---

## Limits

- **Scanning is Windows-only**, because the scanner drivers are.
- **PDF goes through the print dialog** rather than a save dialog.
- **Digital Link targets accept a domain and path prefix**, not extra query parameters or a
  fragment.
- **An AI may appear only once** in an element string — that is the GS1 rule, not an app limit.
- **Combine takes GS1-128 barcodes only, three at most**, and does not keep them between runs.
