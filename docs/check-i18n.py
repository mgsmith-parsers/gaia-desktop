#!/usr/bin/env python3
"""Checks for the translated docs. Run from anywhere: python3 gaia-desktop/docs/check-i18n.py

Checks what a reviewer cannot see by eye across 34 languages × 4 documents:

  Markdown guides  code blocks byte-identical to English; same section structure; every
                   in-page anchor and relative link resolves; no English UI label left where
                   that language's catalogue has its own.
  Samples page     the sample barcodes — data, AIM Code ID, symbology, HRI — and the code that
                   draws them are identical to English, the page structure is the same, and the
                   page declares its language (and right-to-left where needed).
  Staleness        each translation against i18n-manifest.json: one made from an older English
                   revision is listed as behind. Reported, not failed — the exit status reflects
                   the structural checks only.

After translating or updating a document, record its translations as made from the current
English, so staleness is measured from there:

  python3 gaia-desktop/docs/check-i18n.py --record GaiaDesktop-Zebra-Scanner-Windows.md
  python3 gaia-desktop/docs/check-i18n.py --record GaiaDesktop-User.md French German
"""
import hashlib, json, pathlib, re, sys, unicodedata

DOCS = pathlib.Path(__file__).resolve().parent
I18N = DOCS / "i18n"
MANIFEST = DOCS / "i18n-manifest.json"
CATALOGUES = DOCS.parent / "src/main/resources/i18n"
# Each English document that has translations, and how it is checked: <stem>-<Language>.<ext>
# in i18n/<Language>/. A language directory without a translation of one is reported.
SOURCES = {
    "GaiaDesktop-User.md": "markdown",
    "GaiaDesktop-Zebra-Scanner-Windows.md": "markdown",
    "GaiaDesktop-Developer.md": "markdown",
    "GaiaDesktop-Samples.html": "samples",
}

LANG_CODE = {  # directory name -> catalogue code
    "Arabic":"AR","Bengali":"BN","Cantonese":"YUE","Chinese":"ZH","Czech":"CS","Dutch":"NL",
    "EgyptianArabic":"ARZ","French":"FR","German":"DE","Hausa":"HA","Hindi":"HI","Indonesian":"ID",
    "Italian":"IT","Japanese":"JA","Javanese":"JV","Korean":"KO","Marathi":"MR",
    "NigerianPidgin":"PCM","Persian":"FA","Polish":"PL","Portuguese":"PT","Punjabi":"PA",
    "Russian":"RU","Spanish":"ES","Swahili":"SW","Swedish":"SV","Tagalog":"TL","Tamil":"TA",
    "Telugu":"TE","Turkish":"TR","Ukrainian":"UK","Urdu":"UR","Vietnamese":"VI","WuChinese":"WUU",
}
RTL = {"Arabic", "EgyptianArabic", "Persian", "Urdu"}

def sha256(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()

def translation_path(source, lang):
    stem, ext = source.rsplit('.', 1)
    return I18N / lang / f"{stem}-{lang}.{ext}"

def languages():
    return sorted(d.name for d in I18N.iterdir() if d.is_dir()) if I18N.exists() else []

def fences(text):
    # Indented fences too: a code block inside a numbered step is indented to the step.
    return re.findall(r'^[ \t]*```.*?^[ \t]*```', text, re.M | re.S)

def headings(text):
    return [(len(m.group(1)), m.group(2)) for m in re.finditer(r'^(#{1,6})\s+(.+)$', text, re.M)]

def slug(h):
    """GitHub's anchor rule: lowercase, drop punctuation, spaces to hyphens.

    Letters, *combining marks* and digits are kept. The marks matter: Tamil vowel signs and
    Devanagari matras are Unicode category Mn, which `\\w` does not match — stripping them
    silently rewrites every anchor in those languages into something that resolves nowhere.
    """
    text = h.strip().lower().replace('<kbd>', '').replace('</kbd>', '')
    kept = [c for c in text if unicodedata.category(c)[0] in 'LMN' or c in ' -_']
    return re.sub(r'\s+', '-', ''.join(kept).strip())

def catalogue(code):
    values = set()
    for line in (CATALOGUES / f"ui_{code}.properties").read_text(encoding='utf-8').splitlines():
        s = line.strip()
        if s and not s.startswith('#') and '=' in s:
            values.add(s.split('=', 1)[1].strip())
    return values

# ------------------------------------------------------------------ Markdown guides

def check_markdown(english, f, lang, problems):
    code, text = LANG_CODE[lang], f.read_text(encoding='utf-8')
    en_fences, en_heads = fences(english), headings(english)

    # 2. code fences byte-identical to English
    if fences(text) != en_fences:
        problems.append(f"{f.name}: code fences differ from English "
                        f"({len(fences(text))} vs {len(en_fences)}) — fences are never translated")

    # 5. same section structure
    heads = headings(text)
    if [lvl for lvl, _ in heads] != [lvl for lvl, _ in en_heads]:
        problems.append(f"{f.name}: heading structure differs from English "
                        f"({len(heads)} headings vs {len(en_heads)})")

    # 3 + 4. every link resolves; every in-page anchor exists
    anchors = {slug(h) for _, h in heads}
    for m in re.finditer(r'\]\(([^)]+)\)', text):
        raw = m.group(1)
        path, _, frag = raw.partition('#')
        if path.startswith(('http://', 'https://', 'mailto:')):
            continue
        if path and not (f.parent / path).exists():
            problems.append(f"{f.name}: broken link {raw}")
        elif not path and frag and frag not in anchors:
            problems.append(f"{f.name}: dead anchor #{frag}")

    # 6. no English UI label left standing in a translated guide
    #
    # Phrased as "is this an *English* label?" rather than "is this in the target
    # catalogue?": prose emphasis is indistinguishable from a label by shape, but an
    # English catalogue value appearing in a translated doc is unambiguous — the
    # translator wrote the button name instead of harvesting it.
    english_values = catalogue("EN")
    target_values = catalogue(code)
    # *Italic* and **bold** alike: guides name buttons both ways.
    for m in re.finditer(r'(?<!\*)\*{1,2}([^*\n]{2,40})\*{1,2}(?!\*)', text):
        label = m.group(1).strip()
        if label in english_values and label not in target_values:
            problems.append(f"{f.name}: *{label}* is the English label — harvest the "
                            f"translation from ui_{code}.properties instead")

# ------------------------------------------------------------------ the samples page

# The sample barcodes are data in the page's script; every value and data expression of them.
SAMPLE_FIELDS = re.compile(r"\b(aim|tec|hri|fnc1First)\s*:\s*('[^']*'|true|false)")
SAMPLE_DATA = re.compile(r"\bdata\s*:\s*([^\n]+?),?\s*$", re.M)
# The code that turns them into barcode images and element strings.
SCRIPT_FUNCTIONS = ("tecUrl", "elementString")

def sample_values(text):
    return SAMPLE_FIELDS.findall(text), [d.strip() for d in SAMPLE_DATA.findall(text)]

def function_body(text, name):
    m = re.search(r'function ' + name + r'\(.*?\n  \}', text, re.S)
    return m.group(0) if m else None

def check_samples(english, f, lang, problems):
    text = f.read_text(encoding='utf-8')

    # The barcodes are data: translated, they would show the reader something GAIA never read.
    if sample_values(text) != sample_values(english):
        problems.append(f"{f.name}: sample barcode data (data/aim/tec/hri) differs from English — "
                        "it is never translated")
    if re.findall(r"const GS = '[^']*';", text) != re.findall(r"const GS = '[^']*';", english):
        problems.append(f"{f.name}: the {{GS}} constant differs from English")
    for name in SCRIPT_FUNCTIONS:
        if function_body(text, name) != function_body(english, name):
            problems.append(f"{f.name}: function {name}() differs from English — the code that "
                            "draws the barcodes is never translated")

    # Same page structure: the containers the script fills, and the sections.
    for container in re.findall(r'id="([^"]+)"', english):
        if f'id="{container}"' not in text:
            problems.append(f"{f.name}: element id=\"{container}\" is missing")
    if len(re.findall(r'<h2\b', text)) != len(re.findall(r'<h2\b', english)):
        problems.append(f"{f.name}: section count differs from English")

    # The page says what language it is in, and which way it reads.
    m = re.search(r'<html\b([^>]*)>', text)
    attrs = m.group(1) if m else ''
    if 'lang="' not in attrs or 'lang="en"' in attrs:
        problems.append(f"{f.name}: <html> has no lang attribute for {lang}")
    if (lang in RTL) != ('dir="rtl"' in attrs):
        problems.append(f"{f.name}: <html> dir=\"rtl\" is {'missing' if lang in RTL else 'unexpected'}")

    # Relative links resolve (the page's other links are external).
    for href in re.findall(r'href="([^"#]+)', text):
        if not href.startswith(('http://', 'https://', 'mailto:')) and not (f.parent / href).exists():
            problems.append(f"{f.name}: broken link {href}")

# ------------------------------------------------------------------ 7. staleness, via the manifest

def load_manifest():
    return json.loads(MANIFEST.read_text(encoding='utf-8')) if MANIFEST.exists() else {}

def save_manifest(manifest):
    MANIFEST.write_text(json.dumps(manifest, indent=2, ensure_ascii=False) + '\n', encoding='utf-8')

def record(source, langs):
    """Records the translations of `source` — all, or those named — as made from its current English."""
    if source not in SOURCES:
        sys.exit(f"unknown source {source}; one of: {', '.join(SOURCES)}")
    langs = langs or languages()
    manifest = load_manifest()
    entry = manifest.setdefault(source, {"translations": {}})
    entry["sha256"] = sha256(DOCS / source)
    for lang in langs:
        f = translation_path(source, lang)
        if not f.exists():
            sys.exit(f"{f.relative_to(DOCS)} does not exist")
        entry["translations"][lang] = {"translatedFromSourceSha256": entry["sha256"], "sha256": sha256(f)}
    entry["translations"] = dict(sorted(entry["translations"].items()))
    save_manifest(manifest)
    print(f"recorded {len(langs)} translation(s) of {source} as made from its current English")

def staleness():
    """Per source, the translations made from an older English revision, or never recorded."""
    manifest, behind = load_manifest(), {}
    for source in SOURCES:
        current = sha256(DOCS / source)
        recorded = manifest.get(source, {}).get("translations", {})
        for lang in languages():
            if translation_path(source, lang).exists() \
                    and recorded.get(lang, {}).get("translatedFromSourceSha256") != current:
                behind.setdefault(source, []).append(lang)
    return behind

# ------------------------------------------------------------------ main

def main():
    if len(sys.argv) >= 3 and sys.argv[1] == "--record":
        record(sys.argv[2], sys.argv[3:])
        return 0

    problems = []
    for source, kind in SOURCES.items():
        english = (DOCS / source).read_text(encoding='utf-8')
        checked = 0
        for lang in languages():
            if lang not in LANG_CODE:
                problems.append(f"{lang}: not a language directory name the catalogues know")
                continue
            f = translation_path(source, lang)
            if not f.exists():
                problems.append(f"{lang}: missing {f.name}")
                continue
            checked += 1
            (check_markdown if kind == "markdown" else check_samples)(english, f, lang, problems)
        print(f"checked {checked} translation(s) of {source}")

    for p in problems:
        print("  PROBLEM:", p)
    print(f"{len(problems)} problem(s)")

    behind = staleness()
    for source, langs in behind.items():
        print(f"  BEHIND: {len(langs)} translation(s) of {source} were made from an older English "
              f"revision, or never recorded: {', '.join(langs)}")
    if not behind:
        print("every translation is recorded as made from the current English")
    return 1 if problems else 0

if __name__ == "__main__":
    sys.exit(main())
