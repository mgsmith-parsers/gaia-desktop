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
6. [The report](#the-report)
   - [Exporting](#exporting)
7. [Parser options](#parser-options)
   - [First launch](#first-launch)
8. [Languages](#languages)
9. [What is remembered](#what-is-remembered)
10. [Keyboard shortcuts](#keyboard-shortcuts)
11. [Troubleshooting](#troubleshooting)
12. [Limits](#limits)

---

## What it is

GAIA Desktop does two things with GS1 data:

- **Parse** — type, paste, open, or scan a GS1 element string or Digital Link URI and read the
  result as a full report: what each Application Identifier means, what is wrong with it, and
  why;
- **Build** — assemble AI / value pairs into an element string or a Digital Link URI.

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
│ [Parse|Build]  … mode-specific controls …   Save report   Options  │
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
because a successful build simply becomes another capture.

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
it, and a scan arriving while the builder is open is still captured.

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
