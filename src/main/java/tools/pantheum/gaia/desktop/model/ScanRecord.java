package tools.pantheum.gaia.desktop.model;

import tools.pantheum.gaia.gs1.util.ASCIIRevealerUtils;
import tools.pantheum.gaia.result.ParseResult;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * One capture in the session history: the raw input, where it came from, when it arrived,
 * and the result of parsing it.
 *
 * <p>The input is kept separately from {@code result.getPayload()} because a record can be
 * re-parsed under different {@link tools.pantheum.gaia.desktop.parse.ParseOptions} — the
 * input is the fixed part, the result is replaced (via {@link #withResult}) each time the
 * options change.
 */
public final class ScanRecord {

    private static final DateTimeFormatter TIME      = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final String      input;
    private final ScanSource  source;
    /** The symbology reported by the scanner, or {@code null} for non-scanner sources. */
    private final String      symbology;
    private final LocalDateTime captured;
    private final ParseResult result;

    public ScanRecord(String input, ScanSource source, String symbology, ParseResult result) {
        this(input, source, symbology, LocalDateTime.now(), result);
    }

    private ScanRecord(String input, ScanSource source, String symbology,
                       LocalDateTime captured, ParseResult result) {
        this.input     = input;
        this.source    = source;
        this.symbology = symbology;
        this.captured  = captured;
        this.result    = result;
    }

    /**
     * A record rebuilt from the saved history, keeping its original capture time rather than
     * taking the time it was reloaded.
     */
    public static ScanRecord restored(String input, ScanSource source, String symbology,
                                      LocalDateTime captured, ParseResult result) {
        return new ScanRecord(input, source, symbology, captured, result);
    }

    /** A copy of this record carrying a freshly-parsed result, keeping input and provenance. */
    public ScanRecord withResult(ParseResult newResult) {
        return new ScanRecord(input, source, symbology, captured, newResult);
    }

    public String      input()     { return input; }
    public ScanSource  source()    { return source; }
    public String      symbology() { return symbology; }
    public LocalDateTime captured() { return captured; }
    public ParseResult result()    { return result; }

    public boolean isValid() { return result != null && result.isValid(); }

    /** The capture time alone, as {@code HH:mm:ss}. */
    public String time() { return captured.format(TIME); }

    /**
     * The timestamp as it should be shown: the time alone for a capture made today, the full
     * date and time for anything older.
     *
     * <p>A history is overwhelmingly today's work, so repeating today's date on every row
     * costs width and tells the reader nothing. A row from a previous session is exactly
     * where the date earns its place.
     */
    public String timestamp() { return timestamp(LocalDate.now()); }

    /** {@link #timestamp()} against a caller-supplied "today", so the rule can be tested. */
    public String timestamp(LocalDate today) {
        return captured.toLocalDate().equals(today) ? captured.format(TIME) : captured.format(DATE_TIME);
    }

    /**
     * The provenance line for the report masthead, e.g.
     * {@code "scanner · Data Matrix · 14:22:05"}.
     */
    public String provenance() {
        StringBuilder b = new StringBuilder(source.label());
        if (symbology != null && !symbology.isEmpty()) b.append(" · ").append(symbology);
        return b.append(" · ").append(timestamp()).toString();
    }

    /**
     * A one-line summary for the history list.
     *
     * <p>A Digital Link shows as its URI: that is the thing that was built, pasted or
     * scanned, it is already readable, and replacing it with the HRI hides the one part the
     * user is looking for — the URL.
     *
     * <p>Everything else shows as its HRI, because the alternative there is the raw element
     * string, where {@code (01)09506000134352 (17)261231} beats
     * {@code 01095060001343521726123110LOT-001}. Only when there is no HRI does the raw
     * input show, with its control characters revealed.
     */
    public String summary() {
        if (result != null && result.getAiObject() != null) {
            if (result.getAiObject().hasDigitalLink()) return input;

            String hri = result.getAiObject().toHriString();
            if (hri != null && !hri.isEmpty()) return hri;
        }
        return ASCIIRevealerUtils.printable(input);
    }

    /** The number of AIs the parse recognized. */
    public int elementCount() {
        return (result == null || result.getAiObject() == null) ? 0 : result.getAiObject().getAis().size();
    }

    @Override
    public String toString() {
        return timestamp() + "  " + summary();
    }
}
