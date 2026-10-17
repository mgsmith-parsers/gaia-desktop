package tools.pantheum.gaia.desktop.html;

import tools.pantheum.gaia.GaiaParser;
import tools.pantheum.gaia.GaiaConstants;
import tools.pantheum.gaia.desktop.Theme;
import tools.pantheum.gaia.desktop.i18n.UiStrings;
import tools.pantheum.gaia.result.ParseResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link GaiaDesktopReport} — the WebView-hosted parse report.
 *
 * <p>Deliberately free of JavaFX: the renderer is a pure function of its arguments, so it
 * is testable without starting the toolkit. Only {@link Theme} crosses over, and that is a
 * plain enum.
 */
@DisplayName("GaiaDesktopReport")
class GaiaDesktopReportTest {

    private static final GaiaParser PARSER = new GaiaParser();
    private static final String GS = String.valueOf((char) 0x1D);
    private static final UiStrings EN = UiStrings.of(GaiaConstants.Language.ENGLISH);

    @Test
    @DisplayName("renders a self-contained document with the theme pinned")
    void selfContainedDocument() {
        String in = "01095060001343521726123110LOT-001";
        String html = GaiaDesktopReport.build(in, PARSER.parse(in), Theme.DARK, null, EN);

        assertTrue(html.startsWith("<!DOCTYPE html>"), "must be a standalone HTML document");
        assertTrue(html.trim().endsWith("</html>"), "must be a complete document");
        assertTrue(html.contains("<html lang=\"en\" data-theme=\"dark\">"), "the theme must be pinned on <html>");
        assertFalse(html.contains("prefers-color-scheme"),
                "the WebView does not follow the OS setting — the theme comes from the caller");
        // Assets, not content: a canonical Digital Link is legitimately an https URL in the body.
        assertFalse(html.contains("<link ") || html.contains("<script"),
                "report must be self-contained (no external stylesheets or scripts)");
    }

    @Test
    @DisplayName("both themes carry the same rules, selected by the pinned attribute")
    void themesShareOneStylesheet() {
        String in = "0109506000134352";
        ParseResult resp = PARSER.parse(in);

        String light = GaiaDesktopReport.build(in, resp, Theme.LIGHT, null, EN);
        String dark  = GaiaDesktopReport.build(in, resp, Theme.DARK, null, EN);

        assertTrue(light.contains("data-theme=\"light\""));
        assertTrue(dark.contains("data-theme=\"dark\""));
        for (String html : new String[] {light, dark}) {
            assertTrue(html.contains(":root[data-theme=dark]"), "the dark palette must always ship");
            assertTrue(html.contains(":root,:root[data-theme=light]"), "the light palette must always ship");
        }
    }

    @Test
    @DisplayName("a valid element string produces a VALID verdict and shows every AI")
    void validReport() {
        String in = "01095060001343521726123110LOT-001";
        ParseResult resp = PARSER.parse(in);
        assertTrue(resp.isValid(), "precondition: input must parse as valid");

        String html = GaiaDesktopReport.build(in, resp, Theme.LIGHT, null, EN);
        assertTrue(html.contains(">VALID<"), "verdict badge must read VALID");
        assertFalse(html.contains(">INVALID<"), "a valid parse must not render an INVALID badge");
        assertTrue(html.contains(">01<") && html.contains(">17<") && html.contains(">10<"),
                "all three AIs must appear on the tape");
        assertTrue(html.contains("LOT-001"), "the batch value must be shown");
    }

    @Test
    @DisplayName("a bad check digit produces an INVALID verdict")
    void invalidReport() {
        String in = "0109506000134350";  // last digit is wrong
        ParseResult resp = PARSER.parse(in);
        assertFalse(resp.isValid(), "precondition: a bad check digit must be rejected");

        String html = GaiaDesktopReport.build(in, resp, Theme.LIGHT, null, EN);
        assertTrue(html.contains(">INVALID<"), "verdict badge must read INVALID");
        assertTrue(html.contains("Errors"), "the errors section must be rendered");
    }

    @Test
    @DisplayName("the capture provenance reaches the masthead and the metadata grid")
    void provenanceIsRendered() {
        String in = "0109506000134352";
        String html = GaiaDesktopReport.build(in, PARSER.parse(in), Theme.LIGHT, "scanner · Data Matrix · 14:22:05", EN);

        assertTrue(html.contains("scanner · Data Matrix · 14:22:05"), "provenance must be shown");
        assertTrue(html.contains("Capture"), "provenance must also appear as a metadata cell");
    }

    @Test
    @DisplayName("all dynamic text is HTML-escaped")
    void escapesInput() {
        String in = "10<script>" + GS + "0109506000134352";
        String html = GaiaDesktopReport.build(in, PARSER.parse(in), Theme.LIGHT, null, EN);

        assertFalse(html.contains("<script>"), "raw angle brackets from input must be escaped");
        assertTrue(html.contains("&lt;script&gt;"), "input must appear escaped");
    }

    @Test
    @DisplayName("print output forces the light palette whatever the screen theme is")
    void printStylesheetPinsLight() {
        String in = "0109506000134352";
        String dark = GaiaDesktopReport.build(in, PARSER.parse(in), Theme.DARK, null, EN);

        int print = dark.indexOf("@media print{");
        assertTrue(print > 0, "a print stylesheet must ship with every report");

        String printRules = dark.substring(print);
        assertTrue(printRules.contains(":root,:root[data-theme=light],:root[data-theme=dark]{--paper:#eef1f2"),
                "print must re-declare the light palette for the dark theme too — a dark PDF is unreadable");
        assertTrue(printRules.contains("print-color-adjust:exact"),
                "the AI chips and verdict wash carry meaning and must survive printing");
        assertTrue(printRules.contains("page-break-inside:avoid"), "cards must not be split across pages");
        assertTrue(printRules.contains(".tape{flex-wrap:wrap"),
                "the tape scrolls on screen but must wrap on paper, which has no scrollbars");
    }

    @Test
    @DisplayName("suggested file names are timestamped and carry the requested extension")
    void suggestedFileNames() {
        String html = GaiaDesktopReport.suggestedFileName(".html");
        String pdf  = GaiaDesktopReport.suggestedFileName(".pdf");

        assertTrue(html.startsWith("gaia-report-") && html.endsWith(".html"), "got: " + html);
        assertTrue(pdf.startsWith("gaia-report-") && pdf.endsWith(".pdf"), "got: " + pdf);
        assertTrue(html.matches("gaia-report-\\d{8}-\\d{6}-\\d{3}\\.html"), "got: " + html);
    }

    @Test
    @DisplayName("the report follows the chosen language, right down to the html lang attribute")
    void reportIsLocalized() {
        String in = "01095060001343521726123110LOT-001";
        ParseResult resp = PARSER.parse(in);

        String fr = GaiaDesktopReport.build(in, resp, Theme.LIGHT, null,
                UiStrings.of(GaiaConstants.Language.FRENCH));
        assertTrue(fr.contains("<html lang=\"fr\""), "the document language must follow the catalogue");
        assertTrue(fr.contains(">VALIDE<"), "the verdict badge must be localized");
        assertTrue(fr.contains("Métadonnées d&#39;analyse"), "section headings must be localized");
        assertFalse(fr.contains(">Errors<"), "no English section heading should survive");
    }

    @Test
    @DisplayName("a right-to-left language marks the document dir=rtl")
    void rightToLeftReport() {
        String in = "0109506000134352";
        String ar = GaiaDesktopReport.build(in, PARSER.parse(in), Theme.LIGHT, null,
                UiStrings.of(GaiaConstants.Language.ARABIC));

        assertTrue(ar.contains("<html lang=\"ar\""), "got: " + ar.substring(0, 80));
        assertTrue(ar.contains("dir=\"rtl\""), "Arabic must render right to left");
    }

    @Test
    @DisplayName("a Digital Link labels each AI with its structural role")
    void digitalLinkRoles() {
        // 01 is the primary key (first path segment), 10 a key qualifier (later path
        // segment), 17 a data attribute (query string).
        String in = "https://example.com/01/09506000134352/10/LOT-ABC?17=271231";
        ParseResult resp = PARSER.parse(in);
        assertTrue(resp.getAiObject().hasDigitalLink(), "precondition: input must parse as a Digital Link");

        String html = GaiaDesktopReport.build(in, resp, Theme.LIGHT, null, EN);

        assertTrue(html.contains("class=\"ai-role pk\">Primary key<"),    "AI 01 must be marked the primary key");
        assertTrue(html.contains("ai-role kq\">Key qualifier<"),  "AI 10 must be marked a key qualifier");
        assertTrue(html.contains("ai-role da\">Data attribute<"), "AI 17 must be marked a data attribute");
    }

    @Test
    @DisplayName("the roles are localized with the rest of the report")
    void digitalLinkRolesAreLocalized() {
        String in = "https://example.com/01/09506000134352/10/LOT-ABC?17=271231";
        String de = GaiaDesktopReport.build(in, PARSER.parse(in), Theme.LIGHT, null,
                UiStrings.of(GaiaConstants.Language.GERMAN));

        assertTrue(de.contains("Primärschlüssel"), "the role must follow the report language");
        assertFalse(de.contains(">Primary key<"), "no English role should survive");
    }

    @Test
    @DisplayName("a plain element string carries no roles — its AIs have no URI position")
    void noRolesWithoutADigitalLink() {
        String in = "01095060001343521726123110LOT-001";
        ParseResult resp = PARSER.parse(in);
        assertFalse(resp.getAiObject().hasDigitalLink(), "precondition: not a Digital Link");

        String html = GaiaDesktopReport.build(in, resp, Theme.LIGHT, null, EN);
        // The chip markup, not the bare class name — the stylesheet always defines .ai-role.
        assertFalse(html.contains("<span class=\"ai-role"), "role chips belong to Digital Links only");
    }

    @Test
    @DisplayName("the welcome page is a complete document")
    void welcomePage() {
        String html = GaiaDesktopReport.welcome(Theme.LIGHT, EN);

        assertTrue(html.startsWith("<!DOCTYPE html>"));
        assertTrue(html.trim().endsWith("</html>"));
        assertTrue(html.contains("No captures yet"));
    }
}
