package tools.pantheum.gaia.desktop.store;

import tools.pantheum.gaia.desktop.model.ScanRecord;
import tools.pantheum.gaia.desktop.model.ScanSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads and writes {@code ~/.gaia-desktop/captures.tsv} — the capture history, newest first.
 *
 * <p>Only what cannot be recomputed is stored: the input, where it came from, and when. The
 * {@link tools.pantheum.gaia.result.ParseResult} is deliberately <em>not</em> serialised —
 * it is re-derived by parsing the input on load, which also means a restored history reflects
 * the parser options in force now rather than the ones in force when it was captured.
 *
 * <p>The format is one tab-separated record per line:
 *
 * <pre>
 *   captured &lt;TAB&gt; source &lt;TAB&gt; symbology &lt;TAB&gt; input
 *   2026-08-09T14:22:05.123    scanner   Data Matrix   0109506000134352\x1d10LOT-001
 * </pre>
 *
 * <p>The timestamp is an ISO local date-time. It carries the date because a history outlives
 * the session that made it — a row restored tomorrow has to say which day it came from, even
 * though the list drops the date again for anything captured today.
 *
 * <p>Inputs routinely contain the FNC1 separator (0x1D) and may contain any other control
 * character a scanner emits, none of which survive a line-based file untouched — so the input
 * field is escaped ({@code \\}, {@code \t}, {@code \n}, {@code \r}, and {@code \xNN} for
 * everything else below 0x20). Decoding reverses it exactly, byte for byte.
 */
public final class CaptureStore {

    private static final Logger log = LoggerFactory.getLogger(CaptureStore.class);

    private CaptureStore() {}

    /**
     * The most captures kept on disk. A scanning session can run for hours; the cap keeps the
     * file small and start-up fast (every restored capture is re-parsed) without the user ever
     * having to prune it.
     */
    public static final int MAX_ENTRIES = 500;

    /** A restored capture: the fields that were stored, before re-parsing. */
    public static final class StoredCapture {
        private final String     input;
        private final ScanSource source;
        private final String     symbology;
        private final LocalDateTime captured;

        StoredCapture(String input, ScanSource source, String symbology, LocalDateTime captured) {
            this.input     = input;
            this.source    = source;
            this.symbology = symbology;
            this.captured  = captured;
        }

        public String     input()     { return input; }
        public ScanSource source()    { return source; }
        public String     symbology() { return symbology; }
        public LocalDateTime captured() { return captured; }
    }

    /**
     * Reads the saved captures, newest first. Returns an empty list when there is no file —
     * and skips, with a warning, any single line that cannot be read rather than discarding
     * the whole history.
     */
    public static List<StoredCapture> load() {
        Path file = AppPaths.capturesFile();
        List<StoredCapture> captures = new ArrayList<>();
        if (!Files.isReadable(file)) return captures;

        List<String> lines;
        try {
            // ISO-8859-1 for the same reason the scanner reads it: every byte maps 1:1, so an
            // escaped input round-trips regardless of what the scanner sent.
            lines = Files.readAllLines(file, StandardCharsets.ISO_8859_1);
        } catch (IOException e) {
            log.warn("Could not read {}: {}", file, e.getMessage());
            return captures;
        }

        for (String line : lines) {
            if (line.trim().isEmpty() || line.startsWith("#")) continue;
            StoredCapture capture = parseLine(line);
            if (capture != null) captures.add(capture);
            if (captures.size() >= MAX_ENTRIES) break;
        }
        log.info("Restored {} capture(s) from {}", captures.size(), file);
        return captures;
    }

    /** Writes {@code records} — newest first — replacing any previous file. */
    public static void save(List<ScanRecord> records) {
        StringBuilder out = new StringBuilder(4096);
        // ASCII only: the file is written as ISO-8859-1 so that escaped inputs round-trip,
        // and a non-ASCII character in the header would not survive that encoding.
        out.append("# GAIA Desktop capture history: captured(ISO)<TAB>source<TAB>symbology<TAB>input\n");

        int written = 0;
        for (ScanRecord r : records) {
            if (written++ >= MAX_ENTRIES) break;
            out.append(r.captured()).append('\t')
               .append(r.source().name()).append('\t')
               .append(r.symbology() == null ? "" : r.symbology()).append('\t')
               .append(escape(r.input())).append('\n');
        }

        Path file = AppPaths.capturesFile();
        try {
            AppPaths.ensureHome();
            Files.write(file, out.toString().getBytes(StandardCharsets.ISO_8859_1));
        } catch (IOException e) {
            log.warn("Could not write {}: {}", file, e.getMessage());
        }
    }

    /** Deletes the capture file, if there is one. */
    public static void clear() {
        try {
            Files.deleteIfExists(AppPaths.capturesFile());
        } catch (IOException e) {
            log.warn("Could not delete {}: {}", AppPaths.capturesFile(), e.getMessage());
        }
    }

    // ------------------------------------------------------------------ format

    private static StoredCapture parseLine(String line) {
        // -1: keep a trailing empty field, and never let a tab inside the (escaped) input
        // split it — escaping guarantees the input field contains no literal tab.
        String[] parts = line.split("\t", -1);
        if (parts.length < 4) {
            log.warn("Skipping malformed capture line: {}", abbreviate(line));
            return null;
        }

        LocalDateTime captured = parseCaptured(parts[0]);
        if (captured == null) {
            log.warn("Skipping capture line with an unreadable timestamp '{}'", parts[0]);
            return null;
        }

        ScanSource source;
        try {
            source = ScanSource.valueOf(parts[1]);
        } catch (IllegalArgumentException e) {
            source = ScanSource.FILE;  // unknown provenance: not manual, not a scan
        }

        String symbology = parts[2].isEmpty() ? null : parts[2];
        // Field 4 is the input even if a later version appends columns after it: escaping
        // guarantees the input itself holds no tab, so the split cannot run past its end.
        String input = unescape(parts[3]);
        return new StoredCapture(input, source, symbology, captured);
    }

    /**
     * Reads the timestamp field, which is an ISO local date-time
     * ({@code 2026-08-09T14:22:05.123}).
     *
     * <p>Files written before the date was recorded hold a bare time ({@code 14:22:05.123}).
     * Those are still read, dated to today: the true date is gone, and today is the only
     * answer that keeps the row in the history rather than discarding it.
     *
     * @return the parsed timestamp, or {@code null} when the field is neither form
     */
    private static LocalDateTime parseCaptured(String field) {
        try {
            return LocalDateTime.parse(field);
        } catch (DateTimeParseException notADateTime) {
            try {
                return LocalTime.parse(field).atDate(LocalDate.now());
            } catch (DateTimeParseException notATimeEither) {
                return null;
            }
        }
    }

    /** Escapes a raw input so it survives as one field on one line. */
    static String escape(String s) {
        StringBuilder b = new StringBuilder(s.length() + 8);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\\': b.append("\\\\"); break;
                case '\t': b.append("\\t");  break;
                case '\n': b.append("\\n");  break;
                case '\r': b.append("\\r");  break;
                default:
                    if (c < 0x20 || c == 0x7F) {
                        b.append(String.format("\\x%02X", (int) c));
                    } else {
                        b.append(c);
                    }
            }
        }
        return b.toString();
    }

    /** The inverse of {@link #escape}. Unknown escapes are kept verbatim rather than dropped. */
    static String unescape(String s) {
        StringBuilder b = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c != '\\' || i == s.length() - 1) {
                b.append(c);
                continue;
            }
            char next = s.charAt(++i);
            switch (next) {
                case '\\': b.append('\\'); break;
                case 't':  b.append('\t'); break;
                case 'n':  b.append('\n'); break;
                case 'r':  b.append('\r'); break;
                case 'x':
                    if (i + 2 < s.length()) {
                        try {
                            b.append((char) Integer.parseInt(s.substring(i + 1, i + 3), 16));
                            i += 2;
                        } catch (NumberFormatException e) {
                            b.append("\\x");
                        }
                    } else {
                        b.append("\\x");
                    }
                    break;
                default:
                    b.append('\\').append(next);
            }
        }
        return b.toString();
    }

    private static String abbreviate(String s) {
        return s.length() <= 60 ? s : s.substring(0, 57) + "...";
    }
}
