package tools.pantheum.gaia.desktop.html;

import java.util.List;
import java.util.regex.Pattern;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import tools.pantheum.gaia.GaiaConstants;
import tools.pantheum.gaia.GaiaParser;
import tools.pantheum.gaia.GaiaVersion;
import tools.pantheum.gaia.config.ParseConfig;
import tools.pantheum.gaia.desktop.Theme;
import tools.pantheum.gaia.desktop.combine.ScanCombiner;
import tools.pantheum.gaia.desktop.i18n.UiStrings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link GaiaCombineReport} — the Combine strip's report: the viewer's Multi GS1-128
 * Validator content, in GAIA Desktop's look.
 *
 * <p>JavaFX-free: the report is a pure function of the scans, their combination, an optional
 * refusal, a theme and a catalogue. The combinations come from a real {@link ScanCombiner}, so a
 * change to what the combiner produces shows up in what the report says about it.
 *
 * <p>The most important checks are the negative ones: no symbol is drawn for a combination the
 * engine rejects, nothing in the page reaches off the machine, and no language leaves a raw
 * {@code {0}} or key name on screen.
 */
@DisplayName("GaiaCombineReport")
class GaiaCombineReportTest {

    private static final String GS = "\u001D";
    private static final UiStrings EN = UiStrings.of(GaiaConstants.Language.ENGLISH);

    /** The three barcodes of one legacy label: an SSCC, the contents and count, dates and batch. */
    private static final String SSCC     = "]C1" + "00" + "093123450000000012";
    private static final String CONTENTS = "]C1" + "02" + "09312345000012" + "37" + "24";
    private static final String DATES    = "]C1" + "15" + "261231" + "10" + "LOT1";

    private static final String HRI = "(00)093123450000000012 (02)09312345000012 (37)24 (15)261231 (10)LOT1";

    /** A placeholder that format() did not fill. */
    private static final Pattern UNFILLED = Pattern.compile("\\{[0-9]}");

    private static ScanCombiner combiner(String... barcodes) {
        ScanCombiner combiner = new ScanCombiner(new GaiaParser(), ParseConfig.builder()
                .requestedParseMode(GaiaConstants.ParseMode.INTERPRETATION)
                .language(GaiaConstants.Language.ENGLISH)
                .build());
        for (String barcode : barcodes) {
            ScanCombiner.Added added = combiner.add(barcode);
            assertTrue(added.accepted(), () -> barcode + " was refused: " + added.refusal());
        }
        return combiner;
    }

    private static String render(ScanCombiner combiner, ScanCombiner.Refusal refusal, Theme theme, UiStrings s) {
        return GaiaCombineReport.build(combiner.scans(), combiner.combine(), refusal, theme, s);
    }

    private static String render(ScanCombiner combiner) {
        return render(combiner, null, Theme.LIGHT, EN);
    }

    // ---------------------------------------------------------------- the document

    @Test
    @DisplayName("renders a self-contained document in the parse report's shell and stylesheet")
    void selfContainedDocument() {
        String html = render(combiner(SSCC, CONTENTS, DATES));

        assertTrue(html.startsWith("<!DOCTYPE html>"), "must be a standalone HTML document");
        assertTrue(html.trim().endsWith("</html>"), "must be a complete document");
        assertTrue(html.contains("<html lang=\"en\" data-theme=\"light\">"), "theme pinned on <html>");
        assertTrue(html.contains(".verdict{"), "the parse report's stylesheet");
        assertTrue(html.contains(".ctable{"), "with the Combine tables drawn in it");
        assertFalse(html.contains("<link ") || html.contains("<script"), "no external assets");
    }

    @Test
    @DisplayName("nothing in the page would make a network request")
    void fetchesNothing() {
        // The viewer draws its Data Matrix as an <img> from TEC-IT's barcode service, which sends
        // the element string off the machine. Here it is inline SVG. The SVG namespace is a URI
        // but not a fetch, so it is the one http reference allowed.
        String html = render(combiner(SSCC, CONTENTS, DATES)).replace("http://www.w3.org/2000/svg", "");

        for (String outbound : new String[] {"http:", "https:", "<img", "<image", "href=", "url(", "<iframe"}) {
            assertFalse(html.contains(outbound), "the report must not reference " + outbound);
        }
    }

    @Test
    @DisplayName("heads the page with the parse report's masthead, titled for Combine")
    void heading() {
        String html = render(combiner(SSCC, CONTENTS, DATES));

        assertTrue(html.contains("<title>" + EN.get("combine.title") + "</title>"));
        assertTrue(html.contains("<h1><span class=\"prod\">GAIA</span>"
                + EN.get("combine.title").substring("GAIA".length()) + "</h1>"));
        assertTrue(html.contains("<span class=\"meta\">GAIA v" + GaiaVersion.VERSION
                + " · 3 of 3 GS1-128 barcodes, combined into one GS1 Data Matrix · Generated "),
                "version, barcode count and the generated time in the masthead");
        assertTrue(html.contains("<header class=\"mast stacked\">\n  <span class=\"eyebrow\">GAIA · GS1 · Combine</span>"),
                "the eyebrow names the strip, and the meta line sits under the title");
        assertTrue(html.contains(".mast.stacked .meta{margin-left:0;flex-basis:100%;}"), "left aligned, on a line of its own");
        assertFalse(html.contains(EN.get("title")), "the parse report's title must not appear");
    }

    @Test
    @DisplayName("the idle screen asks for up to three barcodes and draws nothing")
    void idlePage() {
        String html = GaiaCombineReport.idle(Theme.LIGHT, EN);

        assertTrue(html.contains("<div class=\"empty\">\n  <h2>Scan up to 3 GS1-128 barcodes…</h2>"));
        assertTrue(html.contains(EN.get("combine.idleSub")));
        assertFalse(html.contains("GAIA engine"), "the version is in the masthead, not repeated in the box");
        assertFalse(html.contains("<svg"));
        assertFalse(html.contains("<table"), "no tables before there is anything to show");
    }

    // ---------------------------------------------------------------- what it says about a label

    @Test
    @DisplayName("a valid label: VALID, every section, and the Data Matrix with its HRI beneath")
    void validLabel() {
        String html = render(combiner(SSCC, CONTENTS, DATES));

        assertTrue(html.contains("<section class=\"verdict\">"), "a passing verdict bar");
        assertTrue(html.contains("<span class=\"dot\"></span>VALID</span>"));
        assertTrue(html.contains("<b>3 of 3</b> Scans"), "the scan count in the tally");

        // Combined: HRI, element string with FNC1 shown as {GS}, and length.
        assertTrue(html.contains("<h2>Combined</h2>"));
        assertTrue(html.contains("<div class=\"k\">HRI</div><div class=\"v\"><code>" + HRI + "</code></div>"),
                "the HRI card, labelled as the viewer labels it");
        assertTrue(html.contains("<code>" + "00093123450000000012" + "0209312345000012" + "3724"
                + "{GS}" + "15261231" + "10LOT1" + "</code>"), "the combined element string");
        assertTrue(html.contains("<div class=\"k\">Length</div><div class=\"v\">55 chars</div>"));

        // The symbol, inline, in its white tile, with the HRI printed under it as the viewer does.
        assertTrue(html.contains("<div class=\"symbol\"><span class=\"tile\"><svg "));
        assertTrue(html.contains("<div class=\"hri\">" + HRI + "</div></span></div>"));
        assertFalse(html.contains("Not drawn"));
    }

    @Test
    @DisplayName("the scans table gives each scan's raw input, carrier and element string")
    void scansTable() {
        String html = render(combiner(SSCC, CONTENTS, DATES));

        assertTrue(html.contains("<tr><th>#</th><th>Input</th><th>Carrier</th><th>Element string</th></tr>"));
        // The raw input keeps its AIM Code ID; the element string beside it shows what was stripped.
        assertTrue(html.contains("<tr><td>1</td><td><code>]C100093123450000000012</code></td>"));
        assertTrue(html.contains("<td><code>00093123450000000012</code>"));
        assertTrue(html.matches("(?s).*<td>\\]C1 [^<]*GS1-128[^<]*</td>.*"), "the carrier names its AIM Code ID");
    }

    @Test
    @DisplayName("what a scan alone does not satisfy is listed under it, as judged on the combination")
    void perScanIssues() {
        // (02) and (37) require (00), which only the first barcode carries.
        String html = render(combiner(SSCC, CONTENTS, DATES));

        assertTrue(html.contains("GE-S005"), "the second scan's own GE-S005 is shown beside it");
        assertTrue(html.contains(" &mdash; judged on the combination</span>"));
        assertTrue(html.contains("<span class=\"dot\"></span>VALID</span>"),
                "and the combination is still valid, because the label supplies what the scan lacks");
    }

    @Test
    @DisplayName("a barcode typed without an AIM Code ID is shown as taken for GS1-128")
    void noAimCodeId() {
        String html = render(combiner("00093123450000000012"));

        assertTrue(html.contains("<span class=\"muted\">no AIM Code ID; taken as GS1-128</span>"));
    }

    @Test
    @DisplayName("Combined lists the element string first, then the HRI, then the length")
    void combinedOrder() {
        String html = render(combiner(SSCC, CONTENTS, DATES));

        int elementString = html.indexOf("<div class=\"k\">Element string</div>");
        int hri           = html.indexOf("<div class=\"k\">HRI</div>");
        int length        = html.indexOf("<div class=\"k\">Length</div>");
        assertTrue(elementString > 0 && elementString < hri && hri < length);
    }

    @Test
    @DisplayName("above the elements, the combined element string is laid out as the parse report's input tape")
    void combinedInputTape() {
        ScanCombiner combiner = combiner(SSCC, CONTENTS, DATES);
        String html = render(combiner);

        // Byte for byte the tape the parse report draws, titled for the combination.
        ScanCombiner.Combination combination = combiner.combine();
        StringBuilder parse = new StringBuilder();
        GaiaDesktopReport.appendInputTape(parse, EN.get("combine.input"), combination.elementString(),
                combination.validation().getAiObject().getAis(), true, EN);
        assertTrue(html.contains(parse.toString()), "the Combine page carries the parse report's input tape");

        assertTrue(html.contains("<div class=\"sec-head\"><h2>Combined input</h2>"));
        // (37) is the one variable-length AI before the end, so the one FNC1 follows it.
        assertTrue(html.contains("<span class=\"cell\">24</span><span class=\"pos\">len 2</span></div>\n"
                + "    <div class=\"seg sep\">"));
        assertTrue(html.indexOf("<h2>Combined input</h2>") < html.indexOf("<h2>Elements</h2>"), "above the elements");
    }

    @Test
    @DisplayName("the elements are the parse report's element cards, exactly")
    void elementCards() {
        ScanCombiner combiner = combiner(SSCC, CONTENTS, DATES);
        String html = render(combiner);

        // Byte for byte what the parse report draws for the same AIs.
        StringBuilder parse = new StringBuilder();
        GaiaDesktopReport.appendElementCards(parse, combiner.combine().validation().getAiObject(), EN);
        assertTrue(html.contains(parse.toString()), "the Combine page carries the parse report's element cards");

        assertTrue(html.contains("<div class=\"sec-head\"><h2>Elements</h2>"));
        assertTrue(html.contains("<article class=\"ai-card ok\">\n  <div class=\"ai-top\">\n    <span class=\"ai-code\">00</span>"));
        assertTrue(html.contains("<span class=\"ai-title\">SSCC</span>"));
        assertTrue(html.contains("<div class=\"irow headline\">"), "interpretations are listed");
        assertFalse(html.contains("<th>AI</th>"), "no element table any more");
        assertFalse(html.contains("<span class=\"ai-role "), "no Digital Link role without a Digital Link");
    }

    @Test
    @DisplayName("an invalid combination: INVALID, the failing AIs marked, and no symbol")
    void invalidCombinationDrawsNoSymbol() {
        // (02) and (37) require (00), which is on a barcode not yet scanned.
        String html = render(combiner(CONTENTS));

        assertTrue(html.contains("<section class=\"verdict invalid\">"));
        assertTrue(html.contains("<span class=\"dot\"></span>INVALID</span>"));
        // GE-S005 belongs to the AIs that need (00): as in the parse report, their cards are
        // marked bad and name it, and its message is read in full under Validation.
        assertTrue(html.contains("<article class=\"ai-card bad\">\n  <div class=\"ai-top\">\n    <span class=\"ai-code\">02</span>"));
        assertTrue(html.contains("<div class=\"irow errrow\"><span class=\"ilabel\">GE-S005</span>"));
        assertTrue(html.contains("<div class=\"sec-head err\"><h2>" + EN.get("combine.validation") + "</h2>"));
        assertTrue(html.contains("<tr class=\"bad\"><td><code>GE-S005</code></td>"),
                "the issue an AI's card names is listed, message and all, under Validation");
        assertTrue(html.contains("<p class=\"muted\">" + EN.get("combine.notDrawnInvalid") + "</p>"));
        assertFalse(html.contains("<svg"),
                "a Data Matrix of data the engine rejects is the one thing that must not be printable");
    }

    @Test
    @DisplayName("a conflict: CONFLICT, the values by scan, nothing combined, no symbol")
    void conflict() {
        String html = render(combiner(SSCC, CONTENTS + GS + "10LOT1", "]C1" + "15" + "261231" + "10" + "LOT2"));

        assertTrue(html.contains("<span class=\"dot\"></span>CONFLICT</span>"));
        assertTrue(html.contains("<span class=\"say\">the scans disagree on an AI</span>"));
        assertTrue(html.contains("<div class=\"sec-head err\"><h2>" + EN.get("combine.conflicts") + "</h2>"));
        assertTrue(html.contains("<tr class=\"bad\"><td><code>(10)</code></td><td>scan 2: <code>LOT1</code><br>scan 3: <code>LOT2</code></td></tr>"));
        assertFalse(html.contains("<h2>Combined</h2>"), "nothing is combined");
        assertTrue(html.contains("<p class=\"muted\">" + EN.get("combine.notDrawnConflict") + "</p>"));
        assertFalse(html.contains("<svg"));
    }

    @Test
    @DisplayName("an AI read twice with the same value is listed once, with the scans it was read in")
    void duplicates() {
        String html = render(combiner(SSCC, CONTENTS + GS + "10LOT1", DATES));

        assertTrue(html.contains("<div class=\"sec-head warn\"><h2>" + EN.get("combine.duplicates") + "</h2>"));
        assertTrue(html.contains("<tr class=\"warnrow\"><td><code>(10)</code></td><td><code>LOT1</code></td><td>2, 3</td></tr>"));
        assertTrue(html.contains("<span class=\"dot\"></span>VALID</span>"), "a repeat is not a fault");
    }

    @Test
    @DisplayName("a refused barcode is explained, in words, with its arguments filled in")
    void refusal() {
        ScanCombiner combiner = combiner(SSCC);
        ScanCombiner.Added again = combiner.add("99999999~" + SSCC);
        assertFalse(again.accepted());

        String html = render(combiner, again.refusal(), Theme.LIGHT, EN);

        assertTrue(html.contains("<p class=\"refused\"><b>Barcode refused.</b> Already scanned, as barcode 1.</p>"));
        assertEquals("Already scanned, as barcode 1.", GaiaCombineReport.resolve(again.refusal(), EN));
    }

    @Test
    @DisplayName("barcode content is escaped")
    void escapesBarcodeContent() {
        // & and < are both in GS1's CSET 82, so (10) may legitimately carry them.
        String html = render(combiner("]C1" + "10" + "A&B<C"));

        assertFalse(html.contains("A&B<C"), "raw markup characters must not reach the page");
        assertTrue(html.contains("A&amp;B&lt;C"));
    }

    // ---------------------------------------------------------------- theme and language

    @Test
    @DisplayName("the dark theme recolours the page but keeps the symbol dark on white")
    void darkTheme() {
        String html = render(combiner(SSCC, CONTENTS, DATES), null, Theme.DARK, EN);

        assertTrue(html.contains("data-theme=\"dark\""));
        assertTrue(html.contains(":root[data-theme=dark]{"), "a dark palette ships");
        assertTrue(html.contains(".symbol .tile{display:inline-block;padding:14px 18px;background:#fff;"),
                "the tile is white in both themes");
        assertTrue(html.contains("fill=\"#ffffff\"") && html.contains("fill=\"#000000\""),
                "the symbol's own colours are fixed, never theme variables");
    }

    @Test
    @DisplayName("a right-to-left language lays the report out right to left")
    void rightToLeft() {
        String html = render(combiner(SSCC), null, Theme.LIGHT, UiStrings.of(GaiaConstants.Language.ARABIC));

        assertTrue(html.contains(" dir=\"rtl\""));
        assertTrue(html.contains("text-align:start"), "cells align to the page's own side");
    }

    @Test
    @DisplayName("every language fills every placeholder and shows no raw key, in every state")
    void everyLanguageRendersCleanly() {
        for (GaiaConstants.Language language : GaiaConstants.Language.values()) {
            UiStrings s = UiStrings.of(language);

            ScanCombiner full = combiner(SSCC, CONTENTS + GS + "10LOT1", DATES);
            ScanCombiner clash = combiner(SSCC, CONTENTS + GS + "10LOT1", "]C1" + "15" + "261231" + "10" + "LOT2");
            ScanCombiner.Added refused = full.add(SSCC);   // a fourth: refused, it is full

            List<String> pages = List.of(
                    GaiaCombineReport.idle(Theme.LIGHT, s),
                    render(full, refused.refusal(), Theme.LIGHT, s),   // symbol, duplicate, refusal
                    render(clash, null, Theme.LIGHT, s),               // conflict
                    render(combiner(CONTENTS), null, Theme.LIGHT, s),  // invalid, issues in the element rows
                    render(combiner("00093123450000000012"), null, Theme.LIGHT, s)); // no AIM Code ID

            for (String html : pages) {
                assertFalse(UNFILLED.matcher(html).find(), () -> language + ": unfilled placeholder in the page");
                assertFalse(html.contains("combine."), () -> language + ": a raw catalogue key reached the page");
            }
        }
    }

    @Test
    @DisplayName("every refusal resolves to words in every language")
    void everyRefusalResolvesEverywhere() {
        String[] keys = {
            "combine.refused.empty", "combine.refused.full", "combine.refused.notGs1128",
            "combine.refused.unreadable", "combine.refused.noAis", "combine.refused.alreadyScanned",
        };
        for (GaiaConstants.Language language : GaiaConstants.Language.values()) {
            UiStrings s = UiStrings.of(language);
            for (String key : keys) {
                String text = s.format(key, "]E0", "EAN-13");
                assertNotEquals(key, text, language + " has no text for " + key);
                assertFalse(UNFILLED.matcher(text).find(), language + " " + key + ": " + text);
            }
        }
    }

    @Test
    @DisplayName("suggests a timestamped file name distinct from the parse report's")
    void suggestedFileName() {
        String name = GaiaCombineReport.suggestedFileName(".html");

        assertTrue(name.matches("gaia-combine-\\d{8}-\\d{6}-\\d{3}\\.html"), name);
    }
}
