package tools.pantheum.gaia.desktop.combine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import tools.pantheum.gaia.GaiaConstants;
import tools.pantheum.gaia.GaiaParser;
import tools.pantheum.gaia.config.ParseConfig;
import tools.pantheum.gaia.datacarrier.registry.DataCarrierType;
import tools.pantheum.gaia.error.GaiaError;
import tools.pantheum.gaia.gs1.constants.GS1Constants;
import tools.pantheum.gaia.gs1.model.GS1AIObjectElement;
import tools.pantheum.gaia.result.ParseResult;

/**
 * Combines up to {@value #MAX_SCANS} GS1-128 scans — the separate barcodes of a legacy label —
 * into the one element string a GS1 Data Matrix would carry.
 *
 * <p>Each scan is parsed by the core, which strips its correlation ID, AIM Code ID and ECI before
 * it reads the AIs, so what is combined is the AIs alone. A scan is refused when it is not
 * GS1-128, when its AIs cannot be read (a {@code SYNTAX_ERROR}), when it carries no AIs, when it
 * repeats an earlier scan's element string, or when {@value #MAX_SCANS} are already held.
 * Everything else about it — a check digit, a format, and in particular an AI that requires one
 * carried by another barcode of the same label — is judged on the combination, not on the scan.
 * That is the whole point: the barcodes of a legacy label are individually incomplete.
 *
 * <p>The combination keeps the AIs in scan order. An AI met again with the same value is kept once
 * and reported as a duplicate; met again with a different value it is a conflict, and there is no
 * combined element string at all until the conflict is gone. Otherwise the AIs are joined as GS1
 * requires — an FNC1 after each variable-length AI that is not the last — and the result is parsed
 * as a GS1 Data Matrix ({@code ]d2}), which validates the combination as a whole.
 *
 * <p>Nothing here is user-visible text. A refusal names a catalogue key and its arguments rather
 * than an English sentence, because every string the window shows is resolved through
 * {@code UiStrings} and has to exist in all 35 catalogues — see {@link Refusal}.
 *
 * <p>The value types below are hand-written classes, from when this module targeted Java 11;
 * on Java 21 they could be records.
 *
 * <p>JavaFX-free and unit-tested without the toolkit, like {@code html/GaiaDesktopReport}. Not
 * thread-safe: one combiner belongs to one window and is used on the JavaFX thread.
 */
public final class ScanCombiner {

    /** How many barcodes a label is combined from at most. */
    public static final int MAX_SCANS = 3;

    /** The AIM Code ID the combined element string is validated under: GS1 Data Matrix. */
    static final String GS1_DATA_MATRIX = "]d2";

    private final GaiaParser parser;
    private ParseConfig config;
    private final List<Scan> scans = new ArrayList<>();

    /**
     * @param parser the parser every scan and the combination go through
     * @param config how they are parsed; it should carry no input modifiers, since the scans are
     *               combined exactly as the barcodes read
     */
    public ScanCombiner(GaiaParser parser, ParseConfig config) {
        this.parser = parser;
        this.config = config;
    }

    /** One accepted scan: the barcode as it arrived, prefixes and all, and the core's parse of it. */
    public static final class Scan {

        private final String input;
        private final ParseResult result;

        Scan(String input, ParseResult result) {
            this.input = input;
            this.result = result;
        }

        /** The barcode as it arrived. */
        public String input() {
            return input;
        }

        /** The core's parse of it. */
        public ParseResult result() {
            return result;
        }

        /** The element string itself: the input without its correlation ID, AIM Code ID and ECI. */
        public String elementString() {
            return result.getPayloadContent();
        }

        /** The AIs this scan carries, in order. */
        public List<GS1AIObjectElement> ais() {
            return result.getAiObject() != null ? result.getAiObject().getAis() : Collections.emptyList();
        }
    }

    /**
     * Why a barcode was refused, as a catalogue key and the arguments to fill it with.
     *
     * <p>Deliberately not a formatted sentence: the combiner is localization-free, and the caller
     * resolves {@code key()} through {@code UiStrings} in whichever language the window is showing.
     */
    public static final class Refusal {

        private final String key;
        private final List<String> args;

        Refusal(String key, List<String> args) {
            this.key = key;
            this.args = args;
        }

        /** A {@code combine.refused.*} key, present in every catalogue. */
        public String key() {
            return key;
        }

        /** The substitutions, already strings, in the order the template names them. */
        public List<String> args() {
            return args;
        }

        @Override
        public String toString() {
            return key + args;
        }
    }

    /** What {@link #add} did with a barcode: took it, or refused it and why. */
    public static final class Added {

        private final boolean accepted;
        private final Refusal refusal;

        private Added(boolean accepted, Refusal refusal) {
            this.accepted = accepted;
            this.refusal = refusal;
        }

        static Added taken() {
            return new Added(true, null);
        }

        static Added refused(String key, String... args) {
            return new Added(false, new Refusal(key, Collections.unmodifiableList(Arrays.asList(args))));
        }

        /** Whether the barcode was taken. */
        public boolean accepted() {
            return accepted;
        }

        /** Why it was refused; null when it was taken. */
        public Refusal refusal() {
            return refusal;
        }
    }

    /** One AI of the combination, and the scan (1-based) it was first read from. */
    public static final class Element {

        private final String ai;
        private final String value;
        private final boolean fixedLength;
        private final int scan;

        public Element(String ai, String value, boolean fixedLength, int scan) {
            this.ai = ai;
            this.value = value;
            this.fixedLength = fixedLength;
            this.scan = scan;
        }

        public String ai() {
            return ai;
        }

        public String value() {
            return value;
        }

        /** Whether GS1 gives this AI a fixed length — it decides whether an FNC1 follows it. */
        public boolean fixedLength() {
            return fixedLength;
        }

        /** The 1-based scan it was first read from. */
        public int scan() {
            return scan;
        }
    }

    /** An AI read more than once with the same value: kept once. Scans are 1-based. */
    public static final class Duplicate {

        private final String ai;
        private final String value;
        private final List<Integer> scans;

        Duplicate(String ai, String value, List<Integer> scans) {
            this.ai = ai;
            this.value = value;
            this.scans = scans;
        }

        public String ai() {
            return ai;
        }

        public String value() {
            return value;
        }

        /** Every scan it was read from, in order. */
        public List<Integer> scans() {
            return scans;
        }
    }

    /** An AI read with different values: each value by the scan (1-based) it came from. */
    public static final class Conflict {

        private final String ai;
        private final Map<Integer, String> values;

        Conflict(String ai, Map<Integer, String> values) {
            this.ai = ai;
            this.values = values;
        }

        public String ai() {
            return ai;
        }

        /** The disagreeing values, by the scan each came from. */
        public Map<Integer, String> values() {
            return values;
        }
    }

    /** The scans combined. */
    public static final class Combination {

        private final List<Element> elements;
        private final List<Duplicate> duplicates;
        private final List<Conflict> conflicts;
        private final String elementString;
        private final ParseResult validation;

        Combination(List<Element> elements, List<Duplicate> duplicates, List<Conflict> conflicts,
                    String elementString, ParseResult validation) {
            this.elements = elements;
            this.duplicates = duplicates;
            this.conflicts = conflicts;
            this.elementString = elementString;
            this.validation = validation;
        }

        /** The AIs, in scan order, each once. */
        public List<Element> elements() {
            return elements;
        }

        /** AIs read again with the same value. */
        public List<Duplicate> duplicates() {
            return duplicates;
        }

        /** AIs read again with a different value. */
        public List<Conflict> conflicts() {
            return conflicts;
        }

        /**
         * The combined element string, FNC1 as {@code 0x1D}; null while there is a conflict or
         * nothing to combine.
         */
        public String elementString() {
            return elementString;
        }

        /** The core's parse of that element string as a GS1 Data Matrix; null with it. */
        public ParseResult validation() {
            return validation;
        }

        /** Whether the combination may be encoded: no conflict, and the core finds the whole valid. */
        public boolean encodable() {
            return conflicts.isEmpty() && validation != null && validation.isValid();
        }
    }

    /** The scans held, in the order they were taken. */
    public List<Scan> scans() {
        return Collections.unmodifiableList(scans);
    }

    /** Whether another barcode would be refused for want of room. */
    public boolean isFull() {
        return scans.size() >= MAX_SCANS;
    }

    /**
     * Parses a barcode and, unless it is refused, adds it to the scans.
     *
     * @param input the barcode, prefixes and all, FNC1 as {@code 0x1D}
     */
    public Added add(String input) {
        if (input == null || input.isEmpty()) {
            return Added.refused("combine.refused.empty");
        }

        if (isFull()) {
            return Added.refused("combine.refused.full", String.valueOf(MAX_SCANS));
        }

        ParseResult result = parser.parse(input, config);

        // No AIM Code ID at all — a barcode typed or pasted without one — is taken as GS1-128,
        // since that is what the entry field is for. Only a carrier that says it is something
        // else is refused.
        if (result.hasDataCarrier()
                && DataCarrierType.forAimCodeId(result.getDataCarrier().getAimCodeId()) != DataCarrierType.GS1_128) {
            return Added.refused("combine.refused.notGs1128",
                    result.getDataCarrier().getAimCodeId(), result.getDataCarrier().getName());
        }

        for (GaiaError error : result.getErrors()) {
            if (error.getLevel() == GaiaConstants.ErrorLevel.SYNTAX_ERROR) {
                return Added.refused("combine.refused.unreadable", error.getId(), error.getMessage());
            }
        }

        Scan scan = new Scan(input, result);

        if (scan.ais().isEmpty()) {
            return Added.refused("combine.refused.noAis");
        }

        for (int i = 0; i < scans.size(); i++) {
            if (scans.get(i).elementString().equals(scan.elementString())) {
                return Added.refused("combine.refused.alreadyScanned", String.valueOf(i + 1));
            }
        }

        scans.add(scan);
        return Added.taken();
    }

    /**
     * Parses every scan held again under {@code config}, and uses it from now on — so a change
     * of language or date format reaches the scans already taken, not just the next one.
     *
     * <p>The scans are kept as they are, in order: no option changes whether a barcode is a
     * readable GS1-128 with AIs in it, so a scan accepted once is not re-judged for refusal.
     */
    public void reparse(ParseConfig config) {
        this.config = config;
        scans.replaceAll(scan -> new Scan(scan.input(), parser.parse(scan.input(), config)));
    }

    /** Drops the last scan taken, if any. */
    public void removeLast() {
        if (!scans.isEmpty()) {
            scans.remove(scans.size() - 1);
        }
    }

    /** Drops every scan. */
    public void clear() {
        scans.clear();
    }

    /** The scans held, combined; see the class comment. */
    public Combination combine() {
        List<Element> elements = new ArrayList<>();
        Map<String, Element> byAi = new LinkedHashMap<>();
        Map<String, Duplicate> duplicates = new LinkedHashMap<>();
        Map<String, Conflict> conflicts = new LinkedHashMap<>();

        for (int s = 0; s < scans.size(); s++) {
            int scan = s + 1;

            for (GS1AIObjectElement ai : scans.get(s).ais()) {
                Element first = byAi.get(ai.getAi());

                if (first == null) {
                    Element element = new Element(ai.getAi(), ai.getValue(), ai.isFixedLength(), scan);
                    byAi.put(ai.getAi(), element);
                    elements.add(element);
                } else if (first.value().equals(ai.getValue())) {
                    Duplicate duplicate = duplicates.get(ai.getAi());
                    if (duplicate == null) {
                        List<Integer> seenIn = new ArrayList<>();
                        seenIn.add(first.scan());
                        duplicate = new Duplicate(ai.getAi(), first.value(), seenIn);
                        duplicates.put(ai.getAi(), duplicate);
                    }
                    if (!duplicate.scans().contains(scan)) {
                        duplicate.scans().add(scan);
                    }
                } else {
                    Conflict conflict = conflicts.get(ai.getAi());
                    if (conflict == null) {
                        Map<Integer, String> values = new LinkedHashMap<>();
                        values.put(first.scan(), first.value());
                        conflict = new Conflict(ai.getAi(), values);
                        conflicts.put(ai.getAi(), conflict);
                    }
                    conflict.values().putIfAbsent(scan, ai.getValue());
                }
            }
        }

        String elementString = null;
        ParseResult validation = null;

        if (conflicts.isEmpty() && !elements.isEmpty()) {
            elementString = join(elements);
            validation = parser.parse(GS1_DATA_MATRIX + elementString, config);
        }

        return new Combination(elements, new ArrayList<>(duplicates.values()),
                new ArrayList<>(conflicts.values()), elementString, validation);
    }

    /** The AIs as one element string: an FNC1 after each variable-length AI but the last. */
    static String join(List<Element> elements) {
        StringBuilder joined = new StringBuilder();

        for (int i = 0; i < elements.size(); i++) {
            Element element = elements.get(i);
            joined.append(element.ai()).append(element.value());

            if (i < elements.size() - 1 && !element.fixedLength()) {
                joined.append(GS1Constants.FNC1_GS);
            }
        }

        return joined.toString();
    }
}
