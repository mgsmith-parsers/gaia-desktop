package tools.pantheum.gaia.desktop.html;

import tools.pantheum.gaia.GaiaConstants;
import tools.pantheum.gaia.GaiaVersion;
import tools.pantheum.gaia.datacarrier.registry.DataCarrierEntry;
import tools.pantheum.gaia.desktop.GaiaDesktopVersion;
import tools.pantheum.gaia.desktop.Theme;
import tools.pantheum.gaia.desktop.i18n.UiStrings;
import tools.pantheum.gaia.desktop.parse.Languages;
import tools.pantheum.gaia.error.GaiaError;
import tools.pantheum.gaia.gs1.model.GS1AIComponentValue;
import tools.pantheum.gaia.gs1.model.GS1AIInterpretation;
import tools.pantheum.gaia.gs1.model.GS1AIObject;
import tools.pantheum.gaia.gs1.model.GS1AIObjectElement;
import tools.pantheum.gaia.gs1.util.ASCIIRevealerUtils;
import tools.pantheum.gaia.result.ParseResult;
import tools.pantheum.gaia.result.ProcessingTiming;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Renders a self-contained HTML report for a single {@link ParseResult} — an
 * instrument-readout design: a verdict banner, a decomposed element-string "tape",
 * a parse-metadata grid, per-element cards, and error / warning detail with
 * remediation hints.
 *
 * <p>Three design choices are specific to the desktop:
 * <ul>
 *   <li>the colour scheme is <em>pinned</em> by the caller ({@link Theme}) instead of
 *       following {@code prefers-color-scheme}, because the report is displayed in a
 *       {@code WebView}, which does not track the desktop's light/dark setting;</li>
 *   <li>the masthead carries a caller-supplied provenance line (where the input came
 *       from and when), which the desktop app uses to label scanner captures;</li>
 *   <li>every label comes from {@link UiStrings}, so the report follows the language
 *       chosen in the app rather than being fixed to one.</li>
 * </ul>
 *
 * <p>The terse field names set in monospace next to raw values are deliberately left
 * untranslated — they are identifiers, not prose. That covers the element card's
 * {@code format}, {@code position}, {@code length}, {@code raw} and {@code part}, the tape's
 * {@code AI}, {@code sep}, {@code pos} and {@code len}, and the four cells locating an error
 * ({@code AI}, {@code position}, {@code level}, {@code stage}), whose values are themselves
 * untranslated enum names.
 *
 * <p>The output is one standalone {@code <!DOCTYPE html>} document with no external
 * assets, so the same string can be shown in the WebView and written straight to disk.
 * Every method here is a pure function of its arguments.
 */
public final class GaiaDesktopReport {

    private GaiaDesktopReport() {}

    private static final DateTimeFormatter TS_FILE  = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS");
    private static final DateTimeFormatter TS_HUMAN = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter TS_START = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    /**
     * A default file name for "save report as…", timestamped so successive saves never
     * collide.
     *
     * @param extension the file extension to append, leading dot included (e.g. {@code ".html"})
     */
    public static String suggestedFileName(String extension) {
        return "gaia-report-" + LocalDateTime.now().format(TS_FILE) + extension;
    }

    /**
     * Writes an already-rendered report to {@code file} as UTF-8 (the document declares
     * {@code <meta charset="utf-8">}, and localized labels and error messages need it).
     */
    public static void writeTo(Path file, String html) throws IOException {
        Files.write(file, html.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * The placeholder document shown before anything has been parsed — same chrome as a
     * real report so the window does not change shape when the first result arrives.
     */
    public static String welcome(Theme theme, UiStrings s) {
        StringBuilder h = new StringBuilder(2048);
        head(h, theme, s, s.get("title"));
        h.append("<div class=\"wrap\">\n");
        masthead(h, s, null, null);
        h.append("<div class=\"empty\">\n")
         .append("  <h2>").append(esc(s.get("lbl.noCaptures"))).append("</h2>\n")
         .append("  <p>").append(esc(s.get("lbl.inputPrompt"))).append(" — ")
         .append(esc(s.get("act.parse"))).append(".</p>\n")
         .append("  <p class=\"muted\"><code>{GS}</code> · FNC1 · 0x1D</p>\n")
         .append("</div>\n</div>\n</body>\n</html>\n");
        return h.toString();
    }

    /**
     * Renders the full standalone HTML report for a single parse event.
     *
     * @param input the string handed to the parser
     * @param resp  the parse result to render
     * @param theme the colour scheme to pin the document to
     * @param meta  an optional provenance line for the masthead (e.g. {@code "scanner · Data Matrix · 14:22:05"});
     *              may be {@code null}
     * @param s     the label catalogue for the language the app is running in
     */
    public static String build(String input, ParseResult resp, Theme theme, String meta, UiStrings s) {
        GS1AIObject aiObject = resp.getAiObject();
        boolean valid = resp.isValid();
        List<GS1AIObjectElement> elements = aiObject != null ? aiObject.getAis() : List.of();
        // getAllErrors / getAllWarnings fold in element-level issues (e.g. a content
        // error like GE-C084 is attached to its element, not the object).
        List<GaiaError> errors   = aiObject != null ? aiObject.getAllErrors()   : List.of();
        List<GaiaError> warnings = aiObject != null ? aiObject.getAllWarnings() : List.of();

        StringBuilder h = new StringBuilder(8192);
        head(h, theme, s, s.get("title"));

        h.append("<div class=\"wrap\">\n");
        masthead(h, s, meta, (valid ? s.get("valid") : s.get("invalid"))
                + " · " + elements.size() + " " + s.get("elements"));

        // ---- verdict ----
        int errCount = errors.size(), warnCount = warnings.size();
        h.append("<div class=\"verdict ").append(valid ? "valid" : "invalid").append("\">\n")
         .append("  <span class=\"badge\"><span class=\"dot\"></span>")
         .append(esc(valid ? s.get("valid") : s.get("invalid"))).append("</span>\n")
         .append("  <span class=\"say\">").append(verdictLine(resp, s)).append("</span>\n")
         .append("  <span class=\"tally\">\n")
         .append("    <span").append(errCount > 0 ? " class=\"bad\"" : "").append("><b").append(errCount > 0 ? " class=\"bad\"" : "").append(">")
         .append(errCount).append("</b> ").append(esc(s.get("errors"))).append("</span>\n")
         .append("    <span><b>").append(warnCount).append("</b> ").append(esc(s.get("warnings"))).append("</span>\n")
         .append("    <span><b>").append(elements.size()).append("</b> ").append(esc(s.get("elements"))).append("</span>\n")
         .append("  </span>\n</div>\n");

        // ---- input tape ----
        appendInputTape(h, s.get("input"), input, elements, valid, s);

        // ---- parse metadata ----
        h.append(section(s.get("rep.metadata")));
        h.append("<div class=\"grid\">\n");
        cell(h, s.get("rep.result"),
                (valid ? s.get("valid") : s.get("invalid")) + " · "
                        + (resp.isParseComplete() ? s.get("rep.complete") : s.get("rep.incomplete")), valid);
        cell(h, s.get("contentType"), contentTypeLabel(resp, s), false);
        cell(h, s.get("rep.mode"), parseModeLabel(resp), false);
        DataCarrierEntry carrier = resp.getDataCarrier();
        cell(h, s.get("carrier"),
                carrier != null ? carrier.getAimCodeId() + " — " + carrier.getName() : s.get("rep.noneDeclared"), false);
        if (aiObject != null && aiObject.hasDigitalLink()) {
            cellWide(h, s.get("canonicalUrl"), "<code>" + esc(aiObject.getCanonicalDigitalLink()) + "</code>");
        } else {
            cell(h, s.get("digitalLink"), s.get("rep.noPrimaryKey"), false);
        }
        if (meta != null && !meta.isEmpty()) {
            cell(h, s.get("rep.capture"), meta, false);
        }
        cell(h, s.get("rep.gaiaVersion"), GaiaVersion.VERSION, false);
        ProcessingTiming timing = resp.getTiming();
        if (timing != null) {
            cell(h, s.get("startTime"), formatStart(timing), false);
            cell(h, s.get("duration"), formatDuration(timing), false);
        }
        if (resp.hasCorrelationId()) {
            cell(h, s.get("correlationId"), resp.getCorrelationInfo().getId(), false);
        }
        if (aiObject != null) {
            cellWide(h, s.get("rep.hri"), "<code>" + esc(aiObject.toHriString()) + "</code>");
            String es = aiObject.toElementString();
            cellWide(h, s.get("rep.roundTrip"),
                    es != null ? "<code>" + esc(ASCIIRevealerUtils.printable(es)) + "</code>"
                               : "<span class=\"muted\">— " + esc(s.get("rep.none")) + "</span>");
        }
        cellWide(h, s.get("input"), "<code>" + esc(ASCIIRevealerUtils.printable(input)) + "</code>");
        cell(h, s.get("inputLength"), formatInputLength(input), false);
        // When an input modifier rewrote the input, the "Input" cell above is no longer what was
        // parsed — show the string the pipeline actually saw, and which modifiers produced it.
        if (resp.isInputModified()) {
            cellWide(h, s.get("parsedInput"),
                    "<code>" + esc(ASCIIRevealerUtils.printable(resp.getModifierInfo().getModifiedInput())) + "</code>");
            cell(h, s.get("appliedModifiers"), String.join(", ", resp.getModifierInfo().getAppliedModifiers()), false);
        }
        h.append("</div>\n");

        // ---- elements ----
        appendElementCards(h, aiObject, s);

        // ---- errors ----
        if (!errors.isEmpty()) {
            h.append(section(s.get("errors")));
            for (GaiaError e : errors) {
                h.append("<div class=\"err-card\">\n  <div class=\"err-top\">\n")
                 .append("    <span class=\"err-id\">").append(esc(e.getId())).append("</span>\n");
                if (e.getCode() != null && !e.getCode().isEmpty()) {
                    h.append("    <span class=\"err-code\">").append(esc(e.getCode())).append("</span>\n");
                }
                h.append("    <span class=\"err-kind\">").append(esc(String.valueOf(e.getLevel())))
                 .append(" · <span class=\"stage\">").append(esc(String.valueOf(e.getStage()))).append("</span></span>\n")
                 .append("  </div>\n")
                 .append("  <p class=\"err-msg\">").append(esc(e.getMessage())).append("</p>\n")
                 .append("  <div class=\"err-loc\">\n");
                loc(h, "AI", nz(e.getAi()));
                loc(h, "position", String.valueOf(e.getPosition()));
                loc(h, "level", String.valueOf(e.getLevel()));
                loc(h, "stage", String.valueOf(e.getStage()));
                h.append("  </div>\n");
                String fix = remediation(e);
                if (fix != null) {
                    h.append("  <div class=\"fix\"><h3>").append(esc(s.get("rep.howToFix"))).append("</h3>").append(fix).append("</div>\n");
                }
                h.append("</div>\n");
            }
        }

        // ---- warnings ----
        if (!warnings.isEmpty()) {
            h.append(section(s.get("warnings")));
            for (GaiaError w : warnings) {
                h.append("<div class=\"err-card warn-card\">\n  <div class=\"err-top\">\n")
                 .append("    <span class=\"err-id warn\">").append(esc(w.getId())).append("</span>\n")
                 .append("    <span class=\"err-kind\"><span class=\"stage\">").append(esc(String.valueOf(w.getStage()))).append("</span></span>\n")
                 .append("  </div>\n  <p class=\"err-msg\">").append(esc(w.getMessage())).append("</p>\n</div>\n");
            }
        }

        // ---- footer ----
        // A saved report outlives the session that made it, so the footer names both the app
        // that wrote it and the parser that produced the verdict above.
        h.append("<footer>\n")
         .append("  <span>").append(esc(s.get("rep.generatedBy"))).append(" v").append(esc(GaiaDesktopVersion.VERSION))
         .append(" · GAIA v").append(esc(GaiaVersion.VERSION)).append("</span>\n")
         .append("  <span>").append(esc(LocalDateTime.now().format(TS_HUMAN))).append("</span>\n")
         .append("  <span>").append(esc(s.get("requestedParseMode"))).append(": ").append(esc(parseModeLabel(resp))).append("</span>\n")
         .append("</footer>\n");

        h.append("</div>\n</body>\n</html>\n");
        return h.toString();
    }

    // ------------------------------------------------------------------ helpers

    /**
     * The input tape: the element string laid out segment by segment — each AI, its value, and
     * the FNC1 separator after a variable-length value — under a section headed {@code title}.
     * Shared with {@link GaiaCombineReport}, which lays out the combined element string with it.
     */
    static void appendInputTape(StringBuilder h, String title, String input,
                                List<GS1AIObjectElement> elements, boolean valid, UiStrings s) {
        h.append(section(title));
        h.append("<div class=\"tape-card\">\n  <div class=\"tape\">\n");
        if (elements.isEmpty()) {
            h.append("    <div class=\"seg val bad\"><span class=\"lab\">").append(esc(s.get("input")))
             .append("</span><span class=\"cell\">")
             .append(esc(ASCIIRevealerUtils.printable(input))).append("</span><span class=\"pos\">—</span></div>\n");
        } else {
            for (int i = 0; i < elements.size(); i++) {
                GS1AIObjectElement e = elements.get(i);
                boolean bad = e.hasErrors();
                h.append("    <div class=\"seg ai").append(bad ? " bad" : "").append("\"><span class=\"lab\">AI</span><span class=\"cell\">")
                 .append(esc(e.getAi())).append("</span><span class=\"pos\">pos ").append(e.getPosition()).append("</span></div>\n");
                String fmt = componentFormatHint(e);
                h.append("    <div class=\"seg val").append(bad ? " bad" : "").append("\"><span class=\"lab\">")
                 .append(esc(s.get("value")))
                 .append(fmt != null ? " · " + esc(fmt) : "").append("</span><span class=\"cell\">")
                 .append(esc(ASCIIRevealerUtils.printable(e.getValue()))).append("</span><span class=\"pos\">")
                 .append(bad ? esc(s.get("rep.rejected")) : "len " + codePoints(e.getValue())).append("</span></div>\n");
                // FNC1 separator follows a variable-length element that is not the last (mirrors toElementString).
                if (i < elements.size() - 1 && !e.isFixedLength()) {
                    h.append("    <div class=\"seg sep\"><span class=\"lab\">sep</span><span class=\"cell\">FNC1</span><span class=\"pos\"></span></div>\n");
                }
            }
        }
        h.append("  </div>\n  <div class=\"tape-legend\">\n")
         .append("    <span><span class=\"swatch sw-ai\"></span>").append(esc(s.get("rep.legendAi"))).append("</span>\n")
         .append("    <span><span class=\"swatch sw-val\"></span>").append(esc(s.get("rep.legendValue"))).append("</span>\n")
         .append("    <span><span class=\"swatch sw-sep\"></span>").append(esc(s.get("rep.legendSep"))).append("</span>\n");
        if (!valid) {
            h.append("    <span><span class=\"swatch sw-bad\"></span>").append(esc(s.get("rep.legendBad"))).append("</span>\n");
        }
        h.append("  </div>\n</div>\n");
    }

    /**
     * The Elements section: one card per AI, with its raw component facts and its interpretation
     * or issues. Shared with {@link GaiaCombineReport}, so a combined label's AIs read exactly as
     * a parsed barcode's do. Draws nothing when there are no AIs.
     */
    static void appendElementCards(StringBuilder h, GS1AIObject aiObject, UiStrings s) {
        List<GS1AIObjectElement> elements = aiObject != null ? aiObject.getAis() : List.of();
        if (elements.isEmpty()) return;

        // A Digital Link puts each AI in one of three structural roles, and which role an
        // AI holds is what decides where it lands in the URI. Only shown for a Digital
        // Link: in a plain element string the AIs have no URI position to describe.
        boolean showRoles = aiObject.hasDigitalLink();

        h.append(section(s.get("elements")));
        for (GS1AIObjectElement e : elements) {
            String state = e.hasErrors() ? "bad" : (e.hasWarnings() ? "warn" : "ok");
            h.append("<article class=\"ai-card ").append(state).append("\">\n")
             .append("  <div class=\"ai-top\">\n")
             .append("    <span class=\"ai-code\">").append(esc(e.getAi())).append("</span>\n");
            if (showRoles && e.hasDigitalLinkAIType()) {
                h.append("    <span class=\"ai-role ").append(roleClass(e)).append("\">")
                 .append(esc(roleLabel(e, s))).append("</span>\n");
            }
            h.append("    <span class=\"ai-title\">").append(esc(nz(e.getTitle()))).append("</span>\n")
             .append("    <span class=\"ai-state ").append(state).append("\">").append(statePill(e, s)).append("</span>\n")
             .append("  </div>\n");
            if (e.getDescription() != null && !e.getDescription().isEmpty()) {
                h.append("  <p class=\"ai-desc\">").append(esc(e.getDescription())).append("</p>\n");
            }
            h.append("  <div class=\"ai-body\">\n");

            // raw component facts
            h.append("    <div class=\"field\">\n      <h3>").append(esc(s.get("rep.rawComponent"))).append("</h3>\n");
            kv(h, "format", nz(e.getFormatString()));
            kv(h, "position", String.valueOf(e.getPosition()));
            kv(h, "length", (e.isFixedLength() ? "fixed" : "variable") + " (" + codePoints(e.getValue()) + ")");
            kvCode(h, "raw", ASCIIRevealerUtils.printable(e.getValue()), e.hasErrors());
            for (GS1AIComponentValue cv : e.getGS1ComponentValues()) {
                kv(h, "part", esc(nz(cv.getType())) + (cv.getFormat() != null ? " · " + esc(cv.getFormat()) : ""));
            }
            h.append("    </div>\n");

            // interpretation / issues
            h.append("    <div class=\"field\">\n      <h3>")
             .append(esc(e.hasErrors() ? s.get("rep.issue") : s.get("rep.interpretation"))).append("</h3>\n");
            List<GS1AIInterpretation> interps = e.getInterpretations();
            List<GaiaError> issues = e.getIssues();
            if (!issues.isEmpty()) {
                h.append("      <div class=\"interp\">\n");
                for (GaiaError g : issues) {
                    boolean warn = g.getLevel() == GaiaConstants.ErrorLevel.WARNING;
                    h.append("        <div class=\"irow ").append(warn ? "warnrow" : "errrow").append("\">")
                     .append("<span class=\"ilabel\">").append(esc(g.getId())).append("</span>")
                     .append("<span class=\"ival\">").append(esc(String.valueOf(g.getLevel()))).append("</span></div>\n");
                }
                h.append("      </div>\n");
            } else if (!interps.isEmpty()) {
                h.append("      <div class=\"interp\">\n");
                boolean first = true;
                for (GS1AIInterpretation in : interps) {
                    h.append("        <div class=\"irow").append(first ? " headline" : "").append("\">")
                     .append("<span class=\"ilabel\">").append(esc(interpLabel(in))).append("</span>")
                     .append("<span class=\"ival\">").append(esc(nz(in.getValue()))).append("</span></div>\n");
                    first = false;
                }
                h.append("      </div>\n");
            } else {
                h.append("      <p class=\"muted\">— ").append(esc(s.get("rep.none"))).append("</p>\n");
            }
            h.append("    </div>\n  </div>\n</article>\n");
        }
    }

    /**
     * Emits everything up to and including the opening {@code <body>}: the theme pinned as
     * {@code data-theme}, the report language as {@code lang}, and {@code dir="rtl"} for the
     * right-to-left catalogues.
     */
    // head, masthead, cell and cellWide are package-private so that GaiaCombineReport renders in
    // this same shell and stylesheet: Combine and Parse share one look.
    static void head(StringBuilder h, Theme theme, UiStrings s, String title) {
        h.append("<!DOCTYPE html>\n<html lang=\"").append(Languages.tag(s.language()))
         .append("\" data-theme=\"")
         .append(theme != null ? theme.htmlAttribute() : Theme.LIGHT.htmlAttribute()).append("\"")
         .append(s.isRightToLeft() ? " dir=\"rtl\"" : "")
         .append(">\n<head>\n<meta charset=\"utf-8\">\n")
         .append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n")
         .append("<title>").append(esc(title)).append("</title>\n<style>\n").append(CSS).append("</style>\n</head>\n<body>\n");
    }

    /** The report masthead — eyebrow, title, and the right-hand meta line. */
    private static void masthead(StringBuilder h, UiStrings s, String provenance, String meta) {
        masthead(h, s, provenance, meta, s.get("title"));
    }

    /** As above, with the headline given: the four-argument form names the parse report. */
    static void masthead(StringBuilder h, UiStrings s, String provenance, String meta, String headline) {
        masthead(h, s, provenance, meta, headline, false);
    }

    /**
     * As above; {@code stacked} sets the meta line on its own line under the headline, left
     * aligned, instead of to the headline's right — for a meta line too long to sit beside it.
     */
    static void masthead(StringBuilder h, UiStrings s, String provenance, String meta, String headline,
                         boolean stacked) {
        h.append("<header class=\"mast").append(stacked ? " stacked" : "").append("\">\n")
         .append("  <span class=\"eyebrow\">GAIA · GS1");
        if (provenance != null && !provenance.isEmpty()) {
            h.append(" · ").append(esc(provenance));
        }
        h.append("</span>\n")
         // The product name is picked out of the localized title so the wordmark keeps its
         // accent colour in every language; a title without it simply renders unstyled.
         .append("  <h1>").append(esc(headline).replace("GAIA", "<span class=\"prod\">GAIA</span>")).append("</h1>\n")
         .append("  <span class=\"meta\">GAIA v").append(esc(GaiaVersion.VERSION));
        if (meta != null && !meta.isEmpty()) {
            h.append(" · ").append(esc(meta));
        }
        h.append("</span>\n</header>\n");
    }

    private static String section(String title) {
        return "<div class=\"sec-head\"><h2>" + esc(title) + "</h2><span class=\"rule\"></span></div>\n";
    }

    static void cell(StringBuilder h, String k, String v, boolean accent) {
        h.append("  <div class=\"cellm\"><div class=\"k\">").append(esc(k)).append("</div><div class=\"v")
         .append(accent ? " accent" : "").append("\">").append(esc(v)).append("</div></div>\n");
    }

    static void cellWide(StringBuilder h, String k, String htmlValue) {
        h.append("  <div class=\"cellm wide\"><div class=\"k\">").append(esc(k)).append("</div><div class=\"v\">")
         .append(htmlValue).append("</div></div>\n");
    }

    private static void kv(StringBuilder h, String k, String v) {
        h.append("      <div class=\"kv\"><span class=\"kk\">").append(esc(k)).append("</span><span class=\"vv\">").append(v).append("</span></div>\n");
    }

    private static void kvCode(StringBuilder h, String k, String v, boolean bad) {
        h.append("      <div class=\"kv\"><span class=\"kk\">").append(esc(k)).append("</span><span class=\"vv")
         .append(bad ? " bad" : "").append("\"><code>").append(esc(v)).append("</code></span></div>\n");
    }

    private static void loc(StringBuilder h, String k, String v) {
        h.append("    <div class=\"m\"><div class=\"k\">").append(esc(k)).append("</div><div class=\"v\">").append(esc(v)).append("</div></div>\n");
    }

    /**
     * The line next to the verdict badge: the mode the pipeline reached and whether it got
     * all the way there. Built from labels rather than a sentence, so it reads the same in
     * every catalogue without needing a translated sentence template.
     */
    private static String verdictLine(ParseResult resp, UiStrings s) {
        String achieved = resp.getAchievedParseMode() != null ? String.valueOf(resp.getAchievedParseMode()) : "—";
        return esc(s.get("requestedParseMode")) + ": <b>" + esc(achieved) + "</b> · "
                + esc(resp.isParseComplete() ? s.get("rep.complete") : s.get("rep.incomplete"));
    }

    /**
     * The localized Digital Link role of an element — primary key, key qualifier, or data
     * attribute.
     *
     * <p>The role itself is decided by the parser
     * ({@link GS1AIObjectElement#getDigitalLinkAIType()}); this only translates it. The
     * English values match the core enum's own labels exactly, so the two never disagree.
     */
    static String roleLabel(GS1AIObjectElement e, UiStrings s) {
        switch (e.getDigitalLinkAIType()) {
            case PRIMARY_IDENTIFICATION_KEY: return s.get("dl.primaryKey");
            case KEY_QUALIFIER:              return s.get("dl.keyQualifier");
            case DATA_ATTRIBUTE:             return s.get("dl.dataAttribute");
            default:                         return e.getDigitalLinkAIType().getLabel();
        }
    }

    /** The style-class modifier that colours a role chip by how far it sits from the key. */
    private static String roleClass(GS1AIObjectElement e) {
        switch (e.getDigitalLinkAIType()) {
            case PRIMARY_IDENTIFICATION_KEY: return "pk";
            case KEY_QUALIFIER:              return "kq";
            case DATA_ATTRIBUTE:             return "da";
            default:                         return "";
        }
    }

    /** The state pill text for an element card. */
    private static String statePill(GS1AIObjectElement e, UiStrings s) {
        if (e.hasErrors()) {
            String id = e.getIssues().stream()
                    .filter(g -> g.getLevel() != GaiaConstants.ErrorLevel.WARNING)
                    .map(GaiaError::getId).findFirst().orElse("");
            return esc(s.get("rep.rejected")) + (id.isEmpty() ? "" : " · " + esc(id));
        }
        if (e.hasWarnings()) return esc(s.get("warnings"));
        if (e.getInterpretations().isEmpty()) return esc(s.get("rep.recognized"));
        return esc(s.get("rep.interpreted"));
    }

    /** A short format hint for the tape value label (e.g. {@code "pcenc"} or {@code "N18"}). */
    private static String componentFormatHint(GS1AIObjectElement e) {
        List<GS1AIComponentValue> cvs = e.getGS1ComponentValues();
        if (cvs.isEmpty()) return null;
        GS1AIComponentValue cv = cvs.get(0);
        if (cv.getFormat() != null && !cv.getFormat().isEmpty()) return cv.getFormat();
        String v = e.getValue();
        return cv.getType() != null ? cv.getType() + (v != null ? String.valueOf(v.length()) : "") : null;
    }

    /**
     * Optional remediation guidance for known error codes; {@code null} when none applies.
     *
     * <p>Untranslated by design: it is a paragraph of standards prose, and a half-translated
     * fix instruction is worse than an English one. The error message above it is already
     * localized by the parser itself.
     */
    private static String remediation(GaiaError e) {
        if ("GE-C084".equals(e.getId())) {
            return "<p>A <code>pcenc</code> field admits only RFC&nbsp;3986 unreserved characters "
                    + "(<code>A–Z a–z 0–9 - _ . ~</code>) and <code>%HH</code> escapes. Percent-encode "
                    + "any other character — encode a space as <code>%20</code>, or, for the healthcare person "
                    + "AIs where a space is written as <code>+</code>, as <code>%2B</code> (both decode to a space).</p>";
        }
        return null;
    }

    static String interpLabel(GS1AIInterpretation in) {
        String label = in.getLabel();
        return (label != null && !label.isEmpty()) ? label : nz(in.getType());
    }

    private static String contentTypeLabel(ParseResult resp, UiStrings s) {
        switch (resp.getContentType()) {
            case GS1_APPLICATION_IDENTIFIERS:             return "GS1_APPLICATION_IDENTIFIERS";
            case GS1_DIGITAL_LINK:                        return "GS1_DIGITAL_LINK";
            case DATA_CARRIER_DOES_NOT_SUPPORT_GS1_AI_DL: return s.get("notGs1Carrier");
            case UNABLE_TO_DETERMINE_CONTENT:
            default:                                      return s.get("unknown");
        }
    }

    private static String parseModeLabel(ParseResult resp) {
        GaiaConstants.ParseMode achieved  = resp.getAchievedParseMode();
        GaiaConstants.ParseMode requested = resp.getRequestedParseMode();
        if (achieved == null) return requested != null ? requested.toString() : "—";
        return resp.isParseComplete() ? achieved.toString() : requested + " → " + achieved;
    }

    private static String formatStart(ProcessingTiming timing) {
        return LocalDateTime.ofInstant(timing.getStartTime(), ZoneId.systemDefault()).format(TS_START);
    }

    private static String formatDuration(ProcessingTiming timing) {
        return String.format(Locale.ROOT, "%.3f ms", timing.getProcessingTime().toNanos() / 1_000_000.0);
    }

    private static String formatInputLength(String input) {
        int chars = input == null ? 0 : input.codePointCount(0, input.length());
        return String.format(Locale.ROOT, "%,d", chars);
    }

    private static int codePoints(String s) {
        return s == null ? 0 : s.codePointCount(0, s.length());
    }

    private static String nz(String s) { return s == null ? "" : s; }

    /** Minimal HTML escaping for text inserted into the report. */
    static String esc(String s) {
        if (s == null) return "";
        StringBuilder b = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '&':  b.append("&amp;");  break;
                case '<':  b.append("&lt;");   break;
                case '>':  b.append("&gt;");   break;
                case '"':  b.append("&quot;"); break;
                case '\'': b.append("&#39;");  break;
                default:   b.append(c);
            }
        }
        return b.toString();
    }

    // ------------------------------------------------------------------ styles

    /** The light colour palette — the base, and what print output always uses. */
    private static final String LIGHT_PALETTE =
        "--paper:#eef1f2;--card:#fff;--ink:#131a1e;--ink-soft:#43535b;--ink-faint:#6b7d85;--line:#d5dcde;--line-soft:#e3e8ea;" +
        "--accent:#0b7c84;--accent-ink:#075258;--accent-wash:#dceff0;--ok:#1c7a4d;--ok-wash:#d8ecdf;--warn:#9a6a12;--warn-wash:#f3e6cc;" +
        "--err:#b23a33;--err-ink:#8f2b26;--err-wash:#f4dcd9;";

    /** The dark colour palette, selected by {@code [data-theme=dark]} on screen only. */
    private static final String DARK_PALETTE =
        "--paper:#0d1416;--card:#131c1f;--ink:#e7edee;--ink-soft:#a6b6bb;--ink-faint:#71858b;" +
        "--line:#26343a;--line-soft:#1d292e;--accent:#35b7bf;--accent-ink:#7fd6db;--accent-wash:#12312f;--ok:#46b981;--ok-wash:#123527;" +
        "--warn:#d3a24a;--warn-wash:#33280f;--err:#e0645d;--err-ink:#f0938d;--err-wash:#38191a;";

    /*
     * Print / PDF rules. Three things have to change on paper:
     *
     *  - the palette is forced back to light even when the screen theme is dark, because a
     *    dark PDF is unreadable printed and wastes toner;
     *  - the element-string tape wraps instead of scrolling — paper has no scrollbars, and
     *    an overflowing tape would simply be cut off at the page edge;
     *  - cards are kept whole across page breaks, and the print engine is told to honour
     *    the background colours that carry meaning (the AI chips, the verdict wash).
     */
    private static final String PRINT_CSS =
        "@media print{\n" +
        ":root,:root[data-theme=light],:root[data-theme=dark]{" + LIGHT_PALETTE + "--shadow:none;}\n" +
        "*{-webkit-print-color-adjust:exact;print-color-adjust:exact;}\n" +
        "html,body{background:#fff;}\n" +
        ".wrap{max-width:none;padding:0 0 6pt;}\n" +
        ".tape-card{overflow-x:visible;}\n" +
        ".tape{flex-wrap:wrap;min-width:0;}\n" +
        ".verdict,.tape-card,.grid,.ai-card,.err-card,.irow,.kv{break-inside:avoid;page-break-inside:avoid;}\n" +
        ".sec-head{break-after:avoid;page-break-after:avoid;}\n" +
        "footer{break-before:avoid;page-break-before:avoid;}\n" +
        "@page{margin:14mm;}\n" +
        "}\n";

    /**
     * What GaiaCombineReport adds: its tables (scans, duplicates, conflicts, elements,
     * validation) drawn as this report's cards, section headings coloured for errors and warnings,
     * the refusal banner, and the symbol tile. The tile is white in both themes on purpose — see
     * DataMatrixSvg. text-align:start so a right-to-left page aligns to its own side.
     */
    private static final String COMBINE_CSS =
        ".sec-head.err h2{color:var(--err);}\n.sec-head.warn h2{color:var(--warn);}\n" +
        ".tcard{background:var(--card);border:1px solid var(--line);border-radius:10px;box-shadow:var(--shadow);overflow:hidden;}\n" +
        ".ctable{width:100%;border-collapse:collapse;table-layout:fixed;font-size:.86rem;}\n" +
        ".ctable th{font-family:var(--mono);font-size:.62rem;letter-spacing:.12em;text-transform:uppercase;color:var(--ink-faint);" +
        "font-weight:600;text-align:start;padding:10px 14px;background:var(--line-soft);border-bottom:1px solid var(--line);}\n" +
        ".ctable td{padding:10px 14px;vertical-align:top;text-align:start;border-bottom:1px solid var(--line-soft);word-wrap:break-word;}\n" +
        ".ctable tr:last-child td{border-bottom:0;}\n" +
        ".ctable td:first-child code{white-space:nowrap;word-break:normal;}\n" +
        ".ctable code,.grid code{font-family:var(--mono);font-size:.82rem;background:var(--line-soft);padding:1px 5px;border-radius:4px;word-break:break-all;}\n" +
        ".ctable tr.bad td{background:var(--err-wash);}\n.ctable tr.warnrow td{background:var(--warn-wash);}\n" +
        ".ctable .err{color:var(--err-ink);font-weight:640;}\n.ctable .warn{color:var(--warn);font-weight:640;}\n" +
        ".ctable .ilabel{color:var(--ink-faint);}\n.ctable .note{display:block;margin-top:4px;font-size:.8rem;color:var(--ink-faint);}\n" +
        ".refused{margin:22px 0 0;padding:14px 18px;border:1px solid var(--err);border-left:4px solid var(--err);" +
        "border-radius:8px;background:var(--card);font-size:.92rem;}\n.refused b{color:var(--err);}\n" +
        ".symbol .tile{display:inline-block;padding:14px 18px;background:#fff;border:1px solid var(--line);border-radius:10px;" +
        "box-shadow:var(--shadow);text-align:center;}\n" +
        ".symbol .tile svg{display:block;margin:0 auto 8px;}\n" +
        ".symbol .hri{font-family:var(--mono);font-size:.78rem;color:#131a1e;}\n";

    // Light is the base; the dark palette is applied by [data-theme=dark] alone. There is
    // deliberately no prefers-color-scheme block — the WebView does not follow the desktop
    // setting, so the theme is whatever the application pins in head().
    private static final String CSS =
        ":root,:root[data-theme=light]{" + LIGHT_PALETTE +
        "--mono:ui-monospace,'SF Mono',Menlo,Consolas,'Liberation Mono',monospace;--sans:system-ui,-apple-system,'Segoe UI',Roboto,Helvetica,Arial,sans-serif;" +
        "--shadow:0 1px 2px rgba(19,26,30,.05),0 8px 24px -12px rgba(19,26,30,.18);}\n" +
        ":root[data-theme=dark]{" + DARK_PALETTE +
        "--shadow:0 1px 2px rgba(0,0,0,.3),0 10px 30px -14px rgba(0,0,0,.7);}\n" +
        "*{box-sizing:border-box;}html{background:var(--paper);}body{margin:0;background:var(--paper);color:var(--ink);font-family:var(--sans);line-height:1.55;-webkit-font-smoothing:antialiased;}\n" +
        ".wrap{max-width:940px;margin:0 auto;padding:clamp(20px,4vw,52px) clamp(16px,4vw,40px) 72px;}\n" +
        "code{font-family:var(--mono);}\n" +
        ".mast{display:flex;flex-wrap:wrap;align-items:baseline;gap:8px 16px;padding-bottom:18px;border-bottom:1px solid var(--line);}\n" +
        ".mast h1{font-size:clamp(1.35rem,3.4vw,1.9rem);margin:0;font-weight:640;letter-spacing:-.015em;}\n.mast .prod{color:var(--accent);}\n" +
        ".eyebrow{font-family:var(--mono);font-size:.68rem;letter-spacing:.22em;text-transform:uppercase;color:var(--ink-faint);flex-basis:100%;}\n" +
        ".mast .meta{margin-left:auto;font-family:var(--mono);font-size:.72rem;color:var(--ink-faint);}\n" +
        ".mast.stacked .meta{margin-left:0;flex-basis:100%;}\n" +
        ".empty{margin-top:40px;padding:28px 30px;border:1px solid var(--line);border-radius:12px;background:var(--card);box-shadow:var(--shadow);}\n" +
        ".empty h2{margin:0 0 10px;font-size:1.05rem;font-weight:660;letter-spacing:-.01em;}\n" +
        ".empty p{margin:0 0 10px;color:var(--ink-soft);font-size:.92rem;max-width:66ch;}\n.empty p:last-child{margin-bottom:0;}\n" +
        ".empty code{background:var(--line-soft);padding:1px 5px;border-radius:4px;font-size:.86em;}\n" +
        ".verdict{margin:26px 0 30px;display:flex;flex-wrap:wrap;align-items:center;gap:14px 22px;padding:18px 22px;border:1px solid var(--line);border-left:4px solid var(--ok);border-radius:10px;background:var(--card);box-shadow:var(--shadow);}\n" +
        ".verdict.invalid{border-left-color:var(--err);}\n" +
        ".badge{display:inline-flex;align-items:center;gap:9px;font-weight:680;font-size:1.02rem;color:var(--ok);}\n" +
        ".verdict.invalid .badge{color:var(--err);}\n" +
        ".badge .dot{width:11px;height:11px;border-radius:50%;background:var(--ok);box-shadow:0 0 0 4px var(--ok-wash);}\n" +
        ".verdict.invalid .badge .dot{background:var(--err);box-shadow:0 0 0 4px var(--err-wash);}\n" +
        ".verdict .say{font-family:var(--mono);font-size:.78rem;color:var(--ink-soft);}\n.verdict .say b{color:var(--ink);}\n" +
        ".verdict .tally{display:flex;gap:18px;margin-left:auto;font-family:var(--mono);font-size:.78rem;color:var(--ink-soft);}\n" +
        ".verdict .tally b{color:var(--ink);font-weight:640;}\n.verdict .tally .bad,.verdict .tally b.bad{color:var(--err-ink);}\n" +
        ".sec-head{display:flex;align-items:baseline;gap:12px;margin:34px 0 14px;}\n" +
        ".sec-head h2{font-size:.82rem;font-weight:680;letter-spacing:.14em;text-transform:uppercase;margin:0;color:var(--ink-soft);}\n" +
        ".sec-head .rule{flex:1;height:1px;background:var(--line-soft);}\n" +
        ".tape-card{background:var(--card);border:1px solid var(--line);border-radius:10px;padding:20px;box-shadow:var(--shadow);overflow-x:auto;}\n" +
        ".tape{display:flex;align-items:stretch;gap:6px;font-family:var(--mono);min-width:max-content;}\n" +
        ".seg{display:flex;flex-direction:column;}\n.seg .lab{font-size:.6rem;letter-spacing:.1em;text-transform:uppercase;color:var(--ink-faint);padding:0 2px 5px;}\n" +
        ".seg .cell{padding:9px 11px;border-radius:7px;font-size:.9rem;display:flex;align-items:center;white-space:pre;}\n" +
        ".seg.ai .cell{background:var(--accent);color:#fff;font-weight:660;}\n.seg.ai.bad .cell{background:var(--err);}\n" +
        ".seg.val .cell{background:var(--line-soft);border:1px solid var(--line);color:var(--ink);}\n.seg.val.bad .cell{background:var(--err-wash);border:1px solid var(--err);}\n" +
        ".seg.sep .cell{background:var(--warn-wash);color:var(--warn);font-weight:640;font-size:.74rem;letter-spacing:.04em;}\n" +
        ".seg .pos{font-family:var(--mono);font-size:.58rem;color:var(--ink-faint);padding:5px 2px 0;}\n.seg.val.bad .pos{color:var(--err-ink);}\n" +
        ".tape-legend{display:flex;flex-wrap:wrap;gap:6px 18px;margin-top:16px;font-family:var(--mono);font-size:.68rem;color:var(--ink-faint);}\n" +
        ".tape-legend span{display:inline-flex;align-items:center;gap:7px;}\n.swatch{width:11px;height:11px;border-radius:3px;}\n" +
        ".sw-ai{background:var(--accent);}\n.sw-val{background:var(--line-soft);border:1px solid var(--line);}\n.sw-sep{background:var(--warn-wash);}\n.sw-bad{background:var(--err);}\n" +
        ".grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(150px,1fr));gap:1px;background:var(--line);border:1px solid var(--line);border-radius:10px;overflow:hidden;}\n" +
        ".cellm{background:var(--card);padding:13px 16px;}\n.cellm.wide{grid-column:1/-1;}\n" +
        ".cellm .k{font-family:var(--mono);font-size:.62rem;letter-spacing:.12em;text-transform:uppercase;color:var(--ink-faint);}\n" +
        ".cellm .v{font-family:var(--mono);font-size:.84rem;margin-top:4px;color:var(--ink);word-break:break-word;}\n.cellm .v.accent{color:var(--accent-ink);}\n.muted{color:var(--ink-faint);}\n" +
        ".ai-card{background:var(--card);border:1px solid var(--line);border-radius:12px;box-shadow:var(--shadow);margin-top:16px;overflow:hidden;}\n" +
        ".ai-card.bad{border-left:4px solid var(--err);}\n.ai-card.warn{border-left:4px solid var(--warn);}\n" +
        ".ai-top{display:flex;flex-wrap:wrap;align-items:center;gap:10px 14px;padding:16px 20px;border-bottom:1px solid var(--line-soft);}\n" +
        ".ai-code{font-family:var(--mono);font-weight:680;font-size:1.02rem;color:#fff;background:var(--accent);padding:4px 10px;border-radius:6px;}\n" +
        ".ai-card.bad .ai-code{background:var(--err);}\n.ai-card.warn .ai-code{background:var(--warn);}\n" +
        ".ai-title{font-weight:660;font-size:1.02rem;letter-spacing:-.01em;}\n" +
        // Digital Link role chip. Filled for the primary key, outlined for a qualifier, plain
        // for a data attribute — the same order in which they appear in the URI, so the eye
        // can read the structure off the cards without reading the words.
        ".ai-role{font-family:var(--mono);font-size:.6rem;letter-spacing:.09em;text-transform:uppercase;" +
        "padding:3px 8px;border-radius:5px;white-space:nowrap;border:1px solid var(--accent);color:var(--accent-ink);background:var(--accent-wash);}\n" +
        ".ai-role.pk{background:var(--accent);color:#fff;}\n" +
        ".ai-role.da{border-color:var(--line);color:var(--ink-faint);background:var(--paper);}\n" +
        ".ai-state{margin-left:auto;font-family:var(--mono);font-size:.66rem;letter-spacing:.06em;text-transform:uppercase;padding:3px 9px;border-radius:20px;color:var(--accent-ink);background:var(--accent-wash);border:1px solid var(--line);}\n" +
        ".ai-state.bad{color:var(--err-ink);background:var(--err-wash);border-color:var(--err);}\n.ai-state.warn{color:var(--warn);background:var(--warn-wash);border-color:var(--warn);}\n" +
        ".ai-desc{padding:12px 20px 4px;color:var(--ink-soft);font-size:.9rem;max-width:68ch;}\n" +
        ".ai-body{padding:8px 20px 20px;display:grid;gap:18px;}\n@media(min-width:620px){.ai-body{grid-template-columns:1fr 1fr;}}\n" +
        ".field h3{font-size:.66rem;letter-spacing:.13em;text-transform:uppercase;color:var(--ink-faint);margin:0 0 9px;font-weight:680;}\n" +
        ".kv{display:flex;gap:10px;font-family:var(--mono);font-size:.8rem;padding:5px 0;border-bottom:1px dashed var(--line-soft);}\n.kv:last-child{border-bottom:0;}\n" +
        ".kv .kk{color:var(--ink-faint);min-width:84px;}\n.kv .vv{color:var(--ink);word-break:break-word;}\n.kv .vv.bad{color:var(--err-ink);}\n" +
        ".interp{display:flex;flex-direction:column;gap:8px;}\n" +
        ".irow{display:flex;align-items:baseline;gap:10px;padding:9px 11px;border:1px solid var(--line-soft);border-radius:8px;background:var(--paper);}\n" +
        ".irow .ilabel{font-family:var(--mono);font-size:.62rem;letter-spacing:.08em;text-transform:uppercase;color:var(--ink-faint);}\n" +
        ".irow .ival{margin-left:auto;font-family:var(--mono);font-size:.9rem;color:var(--ink);font-weight:600;text-align:right;}\n" +
        ".irow.headline{border-color:var(--accent);background:var(--accent-wash);}\n.irow.headline .ival{color:var(--accent-ink);}\n" +
        ".irow.errrow{border-color:var(--err);background:var(--err-wash);}\n.irow.errrow .ival{color:var(--err-ink);}\n" +
        ".irow.warnrow{border-color:var(--warn);background:var(--warn-wash);}\n.irow.warnrow .ival{color:var(--warn);}\n" +
        ".err-card{background:var(--card);border:1px solid var(--line);border-left:4px solid var(--err);border-radius:10px;box-shadow:var(--shadow);overflow:hidden;margin-top:16px;}\n" +
        ".err-card.warn-card{border-left-color:var(--warn);}\n" +
        ".err-top{display:flex;flex-wrap:wrap;align-items:center;gap:10px 14px;padding:16px 20px;border-bottom:1px solid var(--line-soft);}\n" +
        ".err-id{font-family:var(--mono);font-weight:700;font-size:1rem;color:#fff;background:var(--err);padding:4px 10px;border-radius:6px;}\n.err-id.warn{background:var(--warn);}\n" +
        ".err-code{font-family:var(--mono);font-size:.72rem;color:var(--err-ink);background:var(--err-wash);border:1px solid var(--err);padding:3px 9px;border-radius:5px;}\n" +
        ".err-kind{font-family:var(--mono);font-size:.72rem;color:var(--ink-faint);}\n.err-kind .stage{color:var(--ink-soft);}\n" +
        ".err-msg{padding:14px 20px;font-size:.94rem;color:var(--ink);max-width:74ch;}\n.err-msg code{font-family:var(--mono);background:var(--err-wash);color:var(--err-ink);padding:1px 5px;border-radius:4px;}\n" +
        ".err-loc{display:flex;flex-wrap:wrap;gap:1px;background:var(--line);border-top:1px solid var(--line-soft);}\n" +
        ".err-loc .m{background:var(--card);padding:11px 18px;flex:1 1 120px;}\n" +
        ".err-loc .m .k{font-family:var(--mono);font-size:.6rem;letter-spacing:.12em;text-transform:uppercase;color:var(--ink-faint);}\n" +
        ".err-loc .m .v{font-family:var(--mono);font-size:.82rem;margin-top:3px;color:var(--ink);}\n" +
        ".fix{padding:16px 20px;border-top:1px solid var(--line-soft);background:var(--accent-wash);}\n" +
        ".fix h3{margin:0 0 6px;font-size:.68rem;letter-spacing:.13em;text-transform:uppercase;color:var(--accent-ink);font-weight:700;}\n" +
        ".fix p{margin:0;color:var(--ink-soft);font-size:.88rem;max-width:74ch;}\n.fix code{font-family:var(--mono);background:var(--card);color:var(--accent-ink);padding:1px 5px;border-radius:4px;}\n" +
        "footer{margin-top:46px;padding-top:18px;border-top:1px solid var(--line);font-family:var(--mono);font-size:.68rem;color:var(--ink-faint);display:flex;flex-wrap:wrap;gap:6px 16px;}\n" +
        COMBINE_CSS +
        PRINT_CSS;

}
