package tools.pantheum.gaia.desktop.scanner;

import jpos.ScannerConst;
import tools.pantheum.gaia.datacarrier.registry.DataCarrierType;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Maps the JavaPOS {@code SCAN_SDT_*} scan-data types to display names, and to the core's
 * {@link DataCarrierType} where the two vocabularies line up.
 *
 * <p>Two different things name a symbol here, and they do not have the same granularity:
 *
 * <ul>
 *   <li><b>JavaPOS</b> reports what the <em>scanner</em> decoded, and is the finer of the two:
 *       it separates UPC-A from EAN-13, and plain Data Matrix from GS1 DataMatrix.</li>
 *   <li><b>AIM Code IDs</b>, which the core registry is keyed by, group families under one
 *       code character — {@code ]E} covers EAN-13, UPC-A, UPC-E and EAN-8 alike.</li>
 * </ul>
 *
 * <p>So {@link #name(int)} keeps the precise JavaPOS name for display, and
 * {@link #carrierType(int)} is the coarser bridge into the core. Neither affects parsing: the
 * parser reads the transmitted data, including any {@code ]C1}-style prefix the scanner sends,
 * and never sees these values.
 *
 * <p>A type outside the table below still gets a usable name — the JavaPOS constant name, found
 * by reflection — rather than a bare number. That matters because vendor services do report
 * values outside the UnifiedPOS list, and because JavaPOS gains constants over time.
 *
 * <p><b>This class is copied to {@code tools.pantheum.gaia.demo.scanner.Symbologies}</b>,
 * identical but for the package line — the modules do not depend on each other, so the two are
 * kept in step by hand. Change one, change the other; both carry the same test.
 */
public final class Symbologies {

    private Symbologies() {}

    /**
     * DWCode's scan data type, which JavaPOS added in 1.15. This module builds against the 1.14
     * API, whose {@code ScannerConst} does not have it, so the value is written out here.
     *
     * <p>Kept rather than dropped: the code a barcode arrives with is chosen by the scanner's
     * <em>service</em> at run time, not by the API this was compiled against, so a service that
     * reports 146 does so whatever is on the class path. The value is fixed by UnifiedPOS.
     */
    static final int SCAN_SDT_DWCODE = 146;

    /** Display name per JavaPOS scan-data type. */
    private static final Map<Integer, String> NAMES;

    /** The core's carrier type per JavaPOS scan-data type, where a sound mapping exists. */
    private static final Map<Integer, DataCarrierType> CARRIERS;

    /** {@code SCAN_SDT_*} constant names by value, for the fallback. Built by reflection. */
    private static final Map<Integer, String> CONSTANT_NAMES = constantNames();

    static {
        Map<Integer, String>          names    = new HashMap<>();
        Map<Integer, DataCarrierType> carriers = new HashMap<>();

        // --- UPC / EAN ------------------------------------------------------------------
        // The whole family is one AIM code character, ]E — the core cannot tell these apart,
        // which is exactly why the JavaPOS name is the one worth displaying.
        put(names, carriers, ScannerConst.SCAN_SDT_UPCA,    "UPC-A",                      DataCarrierType.EAN_UPC);
        put(names, carriers, ScannerConst.SCAN_SDT_UPCE,    "UPC-E",                      DataCarrierType.EAN_UPC);
        put(names, carriers, ScannerConst.SCAN_SDT_EAN8,    "EAN-8 (JAN-8)",              DataCarrierType.EAN_UPC);
        put(names, carriers, ScannerConst.SCAN_SDT_EAN13,   "EAN-13 (JAN-13)",            DataCarrierType.EAN_UPC);
        put(names, carriers, ScannerConst.SCAN_SDT_UPCA_S,  "UPC-A with supplemental",    DataCarrierType.EAN_UPC);
        put(names, carriers, ScannerConst.SCAN_SDT_UPCE_S,  "UPC-E with supplemental",    DataCarrierType.EAN_UPC);
        put(names, carriers, ScannerConst.SCAN_SDT_EAN8_S,  "EAN-8 with supplemental",    DataCarrierType.EAN_UPC);
        put(names, carriers, ScannerConst.SCAN_SDT_EAN13_S, "EAN-13 with supplemental",   DataCarrierType.EAN_UPC);
        put(names, carriers, ScannerConst.SCAN_SDT_UPCD1,   "UPC-D1",                     DataCarrierType.EAN_UPC);
        put(names, carriers, ScannerConst.SCAN_SDT_UPCD2,   "UPC-D2",                     DataCarrierType.EAN_UPC);
        put(names, carriers, ScannerConst.SCAN_SDT_UPCD3,   "UPC-D3",                     DataCarrierType.EAN_UPC);
        put(names, carriers, ScannerConst.SCAN_SDT_UPCD4,   "UPC-D4",                     DataCarrierType.EAN_UPC);
        put(names, carriers, ScannerConst.SCAN_SDT_UPCD5,   "UPC-D5",                     DataCarrierType.EAN_UPC);

        // --- GS1 linear -----------------------------------------------------------------
        // GS1-128 is 120. JavaPOS 1.15 added SCAN_SDT_GS1128 as the current name for it, but
        // this module builds against 1.14, where it is still SCAN_SDT_EAN128 — the same value, so
        // the same barcodes. ISBT 128 shares ]C1 with GS1-128, and the core groups them under
        // one type for that reason.
        put(names, carriers, ScannerConst.SCAN_SDT_EAN128,  "GS1-128 (EAN-128)",           DataCarrierType.GS1_128);
        put(names, carriers, ScannerConst.SCAN_SDT_ISBT128,         "ISBT 128",                    DataCarrierType.GS1_128);
        put(names, carriers, ScannerConst.SCAN_SDT_GS1DATABAR,      "GS1 DataBar (RSS-14)",        DataCarrierType.GS1_DATABAR);
        put(names, carriers, ScannerConst.SCAN_SDT_GS1DATABAR_E,    "GS1 DataBar Expanded",        DataCarrierType.GS1_DATABAR);
        put(names, carriers, ScannerConst.SCAN_SDT_GS1DATABAR_TYPE2,"GS1 DataBar Type 2",          DataCarrierType.GS1_DATABAR);

        // Composite components. The core folds these into GS1_DATABAR, whose display name is
        // "GS1 DataBar / GS1 Composite" — every ]e modifier belongs to that family.
        put(names, carriers, ScannerConst.SCAN_SDT_CCA,   "GS1 Composite CC-A", DataCarrierType.GS1_DATABAR);
        put(names, carriers, ScannerConst.SCAN_SDT_CCB,   "GS1 Composite CC-B", DataCarrierType.GS1_DATABAR);
        put(names, carriers, ScannerConst.SCAN_SDT_CCC,   "GS1 Composite CC-C", DataCarrierType.GS1_DATABAR);
        // TLC-39 pairs a Code 39 linear with a MicroPDF417 — neither half alone names it.
        put(names, carriers, ScannerConst.SCAN_SDT_TLC39, "TLC-39",             DataCarrierType.UNKNOWN);

        // --- Code 39 and its derivatives ------------------------------------------------
        put(names, carriers, ScannerConst.SCAN_SDT_Code39,     "Code 39",                     DataCarrierType.CODE_39);
        put(names, carriers, ScannerConst.SCAN_SDT_Code39_CK,  "Code 39 with check digit",    DataCarrierType.CODE_39);
        put(names, carriers, ScannerConst.SCAN_SDT_Code32,     "Code 32 (Italian Pharmacode)",DataCarrierType.CODE_39);
        put(names, carriers, ScannerConst.SCAN_SDT_CodeCIP,    "Code CIP (French pharma)",    DataCarrierType.CODE_39);
        put(names, carriers, ScannerConst.SCAN_SDT_TRIOPTIC39, "Trioptic Code 39",            DataCarrierType.CODE_39);

        // --- Other linear ---------------------------------------------------------------
        put(names, carriers, ScannerConst.SCAN_SDT_Code93,  "Code 93",  DataCarrierType.CODE_93);
        put(names, carriers, ScannerConst.SCAN_SDT_Code128, "Code 128", DataCarrierType.CODE_128);
        put(names, carriers, ScannerConst.SCAN_SDT_Codabar, "Codabar",  DataCarrierType.CODABAR);
        put(names, carriers, ScannerConst.SCAN_SDT_Code11,  "Code 11",  DataCarrierType.CODE_11);
        put(names, carriers, ScannerConst.SCAN_SDT_MSI,     "MSI",      DataCarrierType.MSI_PLESSEY);
        put(names, carriers, ScannerConst.SCAN_SDT_PLESSEY, "Plessey",  DataCarrierType.PLESSEY_CODE);
        put(names, carriers, ScannerConst.SCAN_SDT_TELEPEN, "Telepen",  DataCarrierType.TELEPEN);
        put(names, carriers, SCAN_SDT_DWCODE,  "DWCode",   DataCarrierType.UNKNOWN);

        // --- 2 of 5 variants -------------------------------------------------------------
        // Only the interleaved ones map: AIM separates straight 2 of 5 by start/stop bar count
        // (]R vs ]S), which JavaPOS does not report, so guessing between them would be wrong.
        put(names, carriers, ScannerConst.SCAN_SDT_ITF,    "Interleaved 2 of 5",                  DataCarrierType.ITF);
        put(names, carriers, ScannerConst.SCAN_SDT_ITF_CK, "Interleaved 2 of 5 with check digit", DataCarrierType.ITF);
        put(names, carriers, ScannerConst.SCAN_SDT_TF,     "Standard (discrete) 2 of 5",          DataCarrierType.UNKNOWN);
        put(names, carriers, ScannerConst.SCAN_SDT_TFMAT,  "Matrix 2 of 5",                       DataCarrierType.UNKNOWN);
        put(names, carriers, ScannerConst.SCAN_SDT_AMES,   "Ames Code",                           DataCarrierType.UNKNOWN);

        // --- OCR --------------------------------------------------------------------------
        put(names, carriers, ScannerConst.SCAN_SDT_OCRA, "OCR-A", DataCarrierType.OCR);
        put(names, carriers, ScannerConst.SCAN_SDT_OCRB, "OCR-B", DataCarrierType.OCR);

        // --- Stacked and 2D ---------------------------------------------------------------
        put(names, carriers, ScannerConst.SCAN_SDT_PDF417,      "PDF417",         DataCarrierType.PDF417);
        put(names, carriers, ScannerConst.SCAN_SDT_UPDF417,     "MicroPDF417",    DataCarrierType.PDF417);
        put(names, carriers, ScannerConst.SCAN_SDT_MAXICODE,    "MaxiCode",       DataCarrierType.MAXICODE);
        put(names, carriers, ScannerConst.SCAN_SDT_DATAMATRIX,  "Data Matrix",    DataCarrierType.DATA_MATRIX);
        put(names, carriers, ScannerConst.SCAN_SDT_GS1DATAMATRIX,"GS1 DataMatrix",DataCarrierType.GS1_DATA_MATRIX);
        put(names, carriers, ScannerConst.SCAN_SDT_QRCODE,      "QR Code",        DataCarrierType.QR_CODE);
        put(names, carriers, ScannerConst.SCAN_SDT_UQRCODE,     "Micro QR Code",  DataCarrierType.QR_CODE);
        put(names, carriers, ScannerConst.SCAN_SDT_GS1QRCODE,   "GS1 QR Code",    DataCarrierType.GS1_QR_CODE);
        put(names, carriers, ScannerConst.SCAN_SDT_AZTEC,       "Aztec Code",     DataCarrierType.AZTEC_CODE);
        put(names, carriers, ScannerConst.SCAN_SDT_Code49,      "Code 49",        DataCarrierType.CODE_49);
        put(names, carriers, ScannerConst.SCAN_SDT_Code16k,     "Code 16K",       DataCarrierType.CODE_16K);
        put(names, carriers, ScannerConst.SCAN_SDT_CodablockA,  "Codablock A",    DataCarrierType.CODABLOCK);
        put(names, carriers, ScannerConst.SCAN_SDT_CodablockF,  "Codablock F",    DataCarrierType.CODABLOCK);
        put(names, carriers, ScannerConst.SCAN_SDT_Codablock256,"Codablock 256",  DataCarrierType.CODABLOCK);
        put(names, carriers, ScannerConst.SCAN_SDT_HANXIN,      "Han Xin Code",   DataCarrierType.HAN_XIN_CODE);

        // --- Postal -----------------------------------------------------------------------
        // The core's only postal type is MODULATED_HEIGHT_POSTAL (]m), so only the two that
        // really are modulated-height map to it; the four-state codes stay unmapped rather
        // than being filed under a type they are not.
        put(names, carriers, ScannerConst.SCAN_SDT_PostNet,        "USPS POSTNET",           DataCarrierType.MODULATED_HEIGHT_POSTAL);
        put(names, carriers, ScannerConst.SCAN_SDT_UsPlanet,       "USPS PLANET",            DataCarrierType.MODULATED_HEIGHT_POSTAL);
        put(names, carriers, ScannerConst.SCAN_SDT_UsIntelligent,  "USPS Intelligent Mail",  DataCarrierType.UNKNOWN);
        put(names, carriers, ScannerConst.SCAN_SDT_AusPost,        "Australia Post",         DataCarrierType.UNKNOWN);
        put(names, carriers, ScannerConst.SCAN_SDT_CanPost,        "Canada Post",            DataCarrierType.UNKNOWN);
        put(names, carriers, ScannerConst.SCAN_SDT_ChinaPost,      "China Post",             DataCarrierType.UNKNOWN);
        put(names, carriers, ScannerConst.SCAN_SDT_DutchKix,       "Dutch KIX",              DataCarrierType.UNKNOWN);
        put(names, carriers, ScannerConst.SCAN_SDT_InfoMail,       "InfoMail",               DataCarrierType.UNKNOWN);
        put(names, carriers, ScannerConst.SCAN_SDT_JapanPost,      "Japan Post",             DataCarrierType.UNKNOWN);
        put(names, carriers, ScannerConst.SCAN_SDT_KoreanPost,     "Korea Post",             DataCarrierType.UNKNOWN);
        put(names, carriers, ScannerConst.SCAN_SDT_SwedenPost,     "Sweden Post",            DataCarrierType.UNKNOWN);
        put(names, carriers, ScannerConst.SCAN_SDT_UkPost,         "Royal Mail (UK)",        DataCarrierType.UNKNOWN);

        // --- The two JavaPOS placeholders --------------------------------------------------
        put(names, carriers, ScannerConst.SCAN_SDT_UNKNOWN, "Unspecified",   DataCarrierType.UNKNOWN);
        put(names, carriers, ScannerConst.SCAN_SDT_OTHER,   "Other barcode", DataCarrierType.OTHER_BARCODE);

        NAMES    = Collections.unmodifiableMap(names);
        CARRIERS = Collections.unmodifiableMap(carriers);
    }

    /**
     * A human-readable name for a JavaPOS scan data type.
     *
     * <p>Never returns {@code null}. A type not in the table falls back to its JavaPOS constant
     * name — {@code "SCAN_SDT_FOO (999)"} — and, failing even that, to {@code "Unknown (999)"}.
     */
    public static String name(int type) {
        String name = NAMES.get(type);
        if (name != null) {
            return name;
        }
        String constant = CONSTANT_NAMES.get(type);
        return constant != null ? constant + " (" + type + ")" : "Unknown (" + type + ")";
    }

    /**
     * The core data carrier type this symbology belongs to, or {@link DataCarrierType#UNKNOWN}
     * when there is no sound mapping — either because the core has no type for it, or because
     * AIM draws the line somewhere JavaPOS does not report.
     *
     * <p>Never returns {@code null}. This is a coarser answer than {@link #name(int)}: a whole
     * family shares one AIM code character.
     */
    public static DataCarrierType carrierType(int type) {
        DataCarrierType carrier = CARRIERS.get(type);
        return carrier != null ? carrier : DataCarrierType.UNKNOWN;
    }

    /** True when this type is one the table names, rather than one the fallback describes. */
    public static boolean isKnown(int type) {
        return NAMES.containsKey(type);
    }

    // -------------------------------------------------------------------------------------

    /**
     * Adds one entry to both tables. Rejects a duplicate scan-data type loudly: several JavaPOS
     * constants share a value (EAN8/JAN8, EAN128/GS1128, GS1DATABAR/RSS14), and listing two of
     * them here would otherwise silently keep whichever came last.
     */
    private static void put(Map<Integer, String> names, Map<Integer, DataCarrierType> carriers,
                            int type, String name, DataCarrierType carrier) {
        if (names.put(type, name) != null) {
            throw new IllegalStateException("Duplicate scan data type " + type + " (" + name + ")");
        }
        carriers.put(type, carrier);
    }

    /**
     * Every {@code SCAN_SDT_*} constant on {@link ScannerConst}, by value, so an unmapped type
     * can still be named. Aliases are joined ("EAN128 / GS1128") and sorted, so the result does
     * not depend on field order.
     */
    private static Map<Integer, String> constantNames() {
        Map<Integer, String> byValue = new TreeMap<>();
        try {
            for (Field f : ScannerConst.class.getFields()) {
                if (!Modifier.isStatic(f.getModifiers()) || f.getType() != int.class
                        || !f.getName().startsWith("SCAN_SDT_")) {
                    continue;
                }
                int    value = f.getInt(null);
                String name  = f.getName();
                String seen  = byValue.get(value);
                byValue.put(value, seen == null ? name
                        : (seen.compareTo(name) <= 0 ? seen + " / " + name : name + " / " + seen));
            }
        } catch (IllegalAccessException | RuntimeException e) {
            // Reflection is a convenience here, not a requirement: name() still works without it.
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(byValue);
    }
}
