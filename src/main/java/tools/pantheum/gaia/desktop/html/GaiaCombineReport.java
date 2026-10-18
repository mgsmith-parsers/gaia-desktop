package tools.pantheum.gaia.desktop.html;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import tools.pantheum.gaia.GaiaConstants;
import tools.pantheum.gaia.datacarrier.registry.DataCarrierEntry;
import tools.pantheum.gaia.desktop.Theme;
import tools.pantheum.gaia.desktop.combine.ScanCombiner;
import tools.pantheum.gaia.desktop.combine.ScanCombiner.Combination;
import tools.pantheum.gaia.desktop.combine.ScanCombiner.Conflict;
import tools.pantheum.gaia.desktop.combine.ScanCombiner.Duplicate;
import tools.pantheum.gaia.desktop.combine.ScanCombiner.Refusal;
import tools.pantheum.gaia.desktop.combine.ScanCombiner.Scan;
import tools.pantheum.gaia.desktop.datamatrix.DataMatrixEncoder;
import tools.pantheum.gaia.desktop.datamatrix.DataMatrixSvg;
import tools.pantheum.gaia.desktop.i18n.UiStrings;
import tools.pantheum.gaia.error.GaiaError;
import tools.pantheum.gaia.gs1.model.GS1AIObjectElement;
import tools.pantheum.gaia.gs1.util.ASCIIRevealerUtils;
import tools.pantheum.gaia.result.ParseResult;

/**
 * The Combine strip's report: the scans held and what combining them makes, in GAIA Desktop's look.
 *
 * <p><b>Content</b>, section by section: the status and scan count, the scans held (each with its raw
 * input, its carrier, its element string and whatever the scan alone does not satisfy), what
 * combining them found — duplicates and conflicts — the combined element string, its AIs, the
 * engine's validation of it, and, when it is valid, the GS1 Data Matrix with its HRI beneath.
 * The AIs are the parse report's element cards ({@link GaiaDesktopReport#appendElementCards}), so a combined label's AIs read exactly
 * as a parsed barcode's do.
 *
 * <p><b>Look</b> is the parse report's: it renders through {@link GaiaDesktopReport}'s shell —
 * masthead, verdict bar, section headings, key/value cards, stylesheet, theme and text direction —
 * so switching strips does not change the window's character. The tables are drawn as cards in
 * that stylesheet ({@code COMBINE_CSS} there).
 *
 * <p>The Data Matrix is drawn here, as inline SVG, rather than fetched from an online barcode
 * service: nothing in GAIA Desktop sends data off the machine. Every word comes from
 * {@link UiStrings}, so the page reads in all 35 languages.
 *
 * <p>Pure functions returning a complete document; JavaFX-free, so testable without the toolkit.
 */
public final class GaiaCombineReport {

    private GaiaCombineReport() {}

    private static final DateTimeFormatter TS_HUMAN = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** One module of the drawn symbol, in pixels: a 24×24 symbol is 140 px with its quiet zone. */
    private static final int MODULE_PX = 5;

    /** A default file name for "save report as…", distinct from the parse report's. */
    public static String suggestedFileName(String extension) {
        return "gaia-combine-" + GaiaDesktopReport.suggestedFileName("").replace("gaia-report-", "")
                + extension;
    }

    /** The idle screen shown before the first scan and after Reset. */
    public static String idle(Theme theme, UiStrings s) {
        StringBuilder h = new StringBuilder(2048);
        open(h, theme, s, null);
        h.append("<div class=\"empty\">\n")
         .append("  <h2>").append(esc(s.format("combine.idleBox", ScanCombiner.MAX_SCANS))).append("</h2>\n")
         .append("  <p>").append(esc(s.get("combine.idleSub"))).append("</p>\n")
         .append("</div>\n");
        return close(h);
    }

    /**
     * The report for the scans held.
     *
     * @param scans       the scans, in the order they were taken
     * @param combination those scans combined
     * @param refusal     why the last barcode offered was turned away, or null when it was taken
     * @param theme       the colour scheme to pin the document to
     * @param s           the label catalogue for the language the app is running in
     */
    public static String build(List<Scan> scans, Combination combination, Refusal refusal,
                               Theme theme, UiStrings s) {
        StringBuilder h = new StringBuilder(8192);
        open(h, theme, s, s.format("combine.ctype", scans.size(), ScanCombiner.MAX_SCANS)
                + " · " + s.get("generated") + " " + LocalDateTime.now().format(TS_HUMAN));

        verdict(h, scans, combination, s);

        if (refusal != null) {
            h.append("<p class=\"refused\"><b>").append(esc(s.get("combine.refused"))).append("</b> ")
             .append(esc(resolve(refusal, s))).append("</p>\n");
        }

        if (!scans.isEmpty()) {
            appendScans(h, scans, s);
        }
        appendDuplicates(h, combination.duplicates(), s);
        appendConflicts(h, combination.conflicts(), s);

        if (combination.elementString() != null) {
            appendCombined(h, combination, s);
        }

        appendDataMatrix(h, scans, combination, s);

        return close(h);
    }

    /**
     * Resolves a refusal into the running language.
     *
     * <p>The combiner names a key and its arguments rather than a sentence, so that this is the
     * only place a refusal becomes words — and so that it becomes them in all 35 languages.
     */
    public static String resolve(Refusal refusal, UiStrings s) {
        return s.format(refusal.key(), refusal.args().toArray());
    }

    // ---------------------------------------------------------------- sections

    /** The status and scan count, as the parse report's verdict bar. */
    private static void verdict(StringBuilder h, List<Scan> scans, Combination combination, UiStrings s) {
        boolean conflict = !combination.conflicts().isEmpty();
        boolean bad = !scans.isEmpty() && !combination.encodable();

        String word = scans.isEmpty() ? s.get("combine.noScans")
                : conflict ? s.get("combine.conflict")
                : combination.encodable() ? s.get("valid") : s.get("invalid");

        h.append("<section class=\"verdict").append(bad ? " invalid" : "").append("\">\n")
         .append("  <span class=\"badge\"><span class=\"dot\"></span>").append(esc(word)).append("</span>\n");
        if (conflict) {
            h.append("  <span class=\"say\">").append(esc(s.get("combine.conflictNote"))).append("</span>\n");
        }
        h.append("  <span class=\"tally\"><span><b>")
         .append(esc(s.format("combine.count", scans.size(), ScanCombiner.MAX_SCANS))).append("</b> ")
         .append(esc(s.get("combine.scans"))).append("</span></span>\n")
         .append("</section>\n");
    }

    private static void appendScans(StringBuilder h, List<Scan> scans, UiStrings s) {
        heading(h, s.get("combine.scans"), null);
        openTable(h, 5, 33, 17, 45);
        h.append("<tr><th>#</th><th>").append(esc(s.get("input"))).append("</th><th>")
         .append(esc(s.get("carrier"))).append("</th><th>").append(esc(s.get("elementString")))
         .append("</th></tr>\n");

        for (int i = 0; i < scans.size(); i++) {
            Scan scan = scans.get(i);
            ParseResult result = scan.result();

            h.append("<tr><td>").append(i + 1).append("</td>")
             .append("<td>").append(code(ASCIIRevealerUtils.printable(scan.input()))).append("</td>")
             .append("<td>").append(carrier(result, s)).append("</td>")
             // Beside the input, what the engine stripped from it shows: correlation ID, AIM Code
             // ID, ECI.
             .append("<td>").append(code(ASCIIRevealerUtils.printable(scan.elementString())));

            // What the scan alone does not satisfy is judged on the combination; say so.
            for (GaiaError issue : result.getIssues()) {
                h.append("<span class=\"note\">").append(esc(issue.getId())).append(' ')
                 .append(esc(issue.getMessage())).append(" &mdash; ").append(esc(s.get("combine.judged")))
                 .append("</span>");
            }
            h.append("</td></tr>\n");
        }
        closeTable(h);
    }

    private static void appendDuplicates(StringBuilder h, List<Duplicate> duplicates, UiStrings s) {
        if (duplicates.isEmpty()) {
            return;
        }
        heading(h, s.get("combine.duplicates"), "warn");
        openTable(h, 12, 58, 30);
        h.append("<tr><th>AI</th><th>").append(esc(s.get("value"))).append("</th><th>")
         .append(esc(s.get("combine.readIn"))).append("</th></tr>\n");
        for (Duplicate duplicate : duplicates) {
            h.append("<tr class=\"warnrow\"><td><code>(").append(esc(duplicate.ai())).append(")</code></td>")
             .append("<td>").append(code(ASCIIRevealerUtils.printable(duplicate.value()))).append("</td>")
             .append("<td>").append(esc(join(duplicate.scans()))).append("</td></tr>\n");
        }
        closeTable(h);
    }

    private static void appendConflicts(StringBuilder h, List<Conflict> conflicts, UiStrings s) {
        if (conflicts.isEmpty()) {
            return;
        }
        heading(h, s.get("combine.conflicts"), "err");
        openTable(h, 12, 88);
        h.append("<tr><th>AI</th><th>").append(esc(s.get("combine.valuesByScan"))).append("</th></tr>\n");
        for (Conflict conflict : conflicts) {
            h.append("<tr class=\"bad\"><td><code>(").append(esc(conflict.ai())).append(")</code></td><td>");
            boolean first = true;
            for (Map.Entry<Integer, String> value : conflict.values().entrySet()) {
                h.append(first ? "" : "<br>").append(esc(s.format("combine.byScan", value.getKey()))).append(": ")
                 .append(code(ASCIIRevealerUtils.printable(value.getValue())));
                first = false;
            }
            h.append("</td></tr>\n");
        }
        closeTable(h);
    }

    private static void appendCombined(StringBuilder h, Combination combination, UiStrings s) {
        ParseResult validation = combination.validation();
        String elementString = combination.elementString();

        // The element string, HRI and length, as the parse report's key/value cards.
        heading(h, s.get("combine.combined"), null);
        h.append("<div class=\"grid\">\n");
        GaiaDesktopReport.cellWide(h, s.get("elementString"), code(ASCIIRevealerUtils.printable(elementString)));
        GaiaDesktopReport.cellWide(h, s.get("act.hri"),
                code(validation.getAiObject() != null ? validation.getAiObject().toHriString() : ""));
        GaiaDesktopReport.cellWide(h, s.get("combine.length"),
                esc(s.format("combine.chars", elementString.codePointCount(0, elementString.length()))));
        h.append("</div>\n");

        // The combined element string laid out as the parse report lays out its input: AI, value
        // and FNC1 segment by segment, the positions those of the combined string.
        List<GS1AIObjectElement> elements = validation.getAiObject() != null
                ? validation.getAiObject().getAis()
                : List.of();
        GaiaDesktopReport.appendInputTape(h, s.get("combine.input"), elementString, elements, validation.isValid(), s);

        // The AIs as the parse report shows them: one card each, an issue named on its AI's card.
        GaiaDesktopReport.appendElementCards(h, validation.getAiObject(), s);

        // Every issue in full — as in the parse report, the cards name an issue, and its message
        // is read here. getAllErrors / getAllWarnings fold in the issues attached to an AI.
        List<GaiaError> issues = new ArrayList<>();
        if (validation.getAiObject() != null) {
            issues.addAll(validation.getAiObject().getAllErrors());
            issues.addAll(validation.getAiObject().getAllWarnings());
        } else {
            issues.addAll(validation.getIssues());
        }
        if (!issues.isEmpty()) {
            boolean anyError = issues.stream().anyMatch(issue -> issue.getLevel() != GaiaConstants.ErrorLevel.WARNING);
            heading(h, s.get("combine.validation"), anyError ? "err" : "warn");
            openTable(h, 14, 18, 68);
            h.append("<tr><th>").append(esc(s.get("combine.id"))).append("</th><th>").append(esc(s.get("level")))
             .append("</th><th>").append(esc(s.get("message"))).append("</th></tr>\n");
            for (GaiaError issue : issues) {
                boolean warning = issue.getLevel() == GaiaConstants.ErrorLevel.WARNING;
                h.append("<tr class=\"").append(warning ? "warnrow" : "bad").append("\"><td><code>").append(esc(issue.getId()))
                 .append("</code></td><td>").append(esc(String.valueOf(issue.getLevel()))).append("</td><td>")
                 .append(esc(issue.getMessage())).append("</td></tr>\n");
            }
            closeTable(h);
        }
    }

    /**
     * The symbol, drawn only when the combination is encodable, with its HRI beneath. A Data Matrix of data the engine rejects is the one thing on this page
     * someone might print, so an invalid combination gets the reason instead.
     */
    private static void appendDataMatrix(StringBuilder h, List<Scan> scans, Combination combination, UiStrings s) {
        if (scans.isEmpty()) {
            return;
        }

        heading(h, s.get("combine.symbol"), null);

        if (!combination.encodable()) {
            String why = !combination.conflicts().isEmpty()
                    ? s.get("combine.notDrawnConflict")
                    : s.get("combine.notDrawnInvalid");
            h.append("<p class=\"muted\">").append(esc(why)).append("</p>\n");
            return;
        }

        String hri = combination.validation().getAiObject().toHriString();
        String svg;
        try {
            svg = DataMatrixSvg.render(DataMatrixEncoder.encode(combination.elementString()), MODULE_PX, hri);
        } catch (RuntimeException e) {
            // Too long for the largest symbol, or a character ASCII encodation cannot carry. The
            // combination is still valid GS1; it just will not fit in one Data Matrix.
            h.append("<p class=\"muted\">").append(esc(s.get("combine.notDrawnInvalid"))).append("</p>\n");
            return;
        }
        h.append("<div class=\"symbol\"><span class=\"tile\">").append(svg)
         .append("<div class=\"hri\">").append(esc(hri)).append("</div></span></div>\n");
    }

    // ---------------------------------------------------------------- fragments

    /** The document head, the page wrapper and the masthead, headed with the Combine title. */
    private static void open(StringBuilder h, Theme theme, UiStrings s, String meta) {
        GaiaDesktopReport.head(h, theme, s, s.get("combine.title"));
        h.append("<div class=\"wrap\">\n");
        // "GAIA · GS1 · Combine" over the title; the meta line, long here, stacked under it.
        GaiaDesktopReport.masthead(h, s, s.get("mode.combine"), meta, s.get("combine.title"), true);
    }

    private static String close(StringBuilder h) {
        return h.append("</div>\n</body>\n</html>\n").toString();
    }

    /** A section heading in the parse report's style; {@code variant} colours it for errors or warnings. */
    private static void heading(StringBuilder h, String title, String variant) {
        h.append("<div class=\"sec-head").append(variant != null ? " " + variant : "").append("\"><h2>")
         .append(esc(title)).append("</h2><span class=\"rule\"></span></div>\n");
    }

    private static void openTable(StringBuilder h, double... weights) {
        double total = 0;
        for (double w : weights) total += w;

        h.append("<div class=\"tcard\"><table class=\"ctable\"><colgroup>");
        for (double w : weights) {
            h.append("<col style=\"width:")
             .append(String.format(Locale.ROOT, "%.2f", w / total * 100))
             .append("%\">");
        }
        h.append("</colgroup>\n");
    }

    private static void closeTable(StringBuilder h) {
        h.append("</table></div>\n");
    }

    private static String carrier(ParseResult result, UiStrings s) {
        DataCarrierEntry carrier = result.getDataCarrier();
        return carrier != null
                ? esc(carrier.getAimCodeId() + " " + carrier.getName())
                : "<span class=\"muted\">" + esc(s.get("combine.noAim")) + "</span>";
    }

    private static String join(List<Integer> scans) {
        StringBuilder b = new StringBuilder();
        for (Integer scan : scans) {
            b.append(b.length() > 0 ? ", " : "").append(scan);
        }
        return b.toString();
    }

    private static String code(String text) {
        return "<code>" + esc(text) + "</code>";
    }

    private static String esc(String text) {
        return GaiaDesktopReport.esc(text);
    }
}
