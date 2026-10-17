package tools.pantheum.gaia.desktop.combine;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import tools.pantheum.gaia.GaiaConstants;
import tools.pantheum.gaia.GaiaParser;
import tools.pantheum.gaia.config.ParseConfig;
import tools.pantheum.gaia.desktop.combine.ScanCombiner.Added;
import tools.pantheum.gaia.desktop.combine.ScanCombiner.Combination;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The combining behind the Combine strip: the barcodes of a legacy GS1-128 label — here an SSCC,
 * the contents' GTIN and count, and a best-before date and batch — taken one at a time, stripped
 * of their prefixes, and combined into the element string of one GS1 Data Matrix.
 *
 * <p>No JavaFX: {@link ScanCombiner} is deliberately toolkit-free, so these run in a plain JVM.
 */
class ScanCombinerTest {

    private static final String GS = "\u001D";

    private static final String SSCC     = "]C1" + "00" + "093123450000000012";
    private static final String CONTENTS = "]C1" + "02" + "09312345000012" + "37" + "24";
    private static final String DATES    = "]C1" + "15" + "261231" + "10" + "LOT1";

    private static final String COMBINED =
            "00093123450000000012" + "0209312345000012" + "3724" + GS + "15261231" + "10LOT1";

    private static ScanCombiner combiner() {
        return new ScanCombiner(new GaiaParser(), ParseConfig.builder()
                .requestedParseMode(GaiaConstants.ParseMode.INTERPRETATION)
                .language(GaiaConstants.Language.ENGLISH)
                .build());
    }

    private static void take(ScanCombiner combiner, String... barcodes) {
        for (String barcode : barcodes) {
            Added added = combiner.add(barcode);
            assertTrue(added.accepted(), () -> barcode + " was refused: " + added.refusal());
        }
    }

    @Test
    void theBarcodesOfALabelCombineIntoOneValidElementString() {
        ScanCombiner combiner = combiner();
        take(combiner, SSCC, CONTENTS, DATES);

        Combination combination = combiner.combine();

        assertEquals(COMBINED, combination.elementString());
        assertTrue(combination.encodable(), () -> combination.validation().getIssues().toString());
        assertEquals("(00)093123450000000012 (02)09312345000012 (37)24 (15)261231 (10)LOT1",
                combination.validation().getAiObject().toHriString());
    }

    @Test
    void aScanMissingAnAiItsLabelCarriesElsewhereIsTakenAndJudgedOnTheCombination() {
        ScanCombiner combiner = combiner();

        // (02) and (37) require (00), which is on another barcode of the same label.
        take(combiner, CONTENTS);
        assertFalse(combiner.combine().encodable(), "the contents barcode alone is not valid");

        take(combiner, SSCC);
        assertTrue(combiner.combine().encodable());
    }

    @Test
    void theCorrelationIdAndAimCodeIdAreStrippedBeforeCombining() {
        ScanCombiner combiner = combiner();
        take(combiner, "]C1" + "10" + "LOT1" + GS, SSCC, "]C1" + "02" + "09312345000012" + "37" + "24");

        assertEquals("10LOT1" + GS + "00093123450000000012" + "0209312345000012" + "3724",
                combiner.combine().elementString());
    }

    @Test
    void anAiReadAgainWithTheSameValueIsKeptOnceAndReported() {
        ScanCombiner combiner = combiner();
        take(combiner, SSCC, CONTENTS + GS + "10LOT1", DATES);

        Combination combination = combiner.combine();

        assertEquals(1, combination.duplicates().size());
        assertEquals("10", combination.duplicates().get(0).ai());
        assertEquals(List.of(2, 3), combination.duplicates().get(0).scans());
        assertEquals(1, combination.elementString().split("10LOT1", -1).length - 1,
                "the repeated AI is carried once, not twice");
        assertTrue(combination.encodable());
    }

    @Test
    void anAiReadAgainWithADifferentValueIsAConflictAndNothingIsCombined() {
        ScanCombiner combiner = combiner();
        take(combiner, SSCC, CONTENTS + GS + "10LOT1", "]C1" + "15" + "261231" + "10" + "LOT2");

        Combination combination = combiner.combine();

        assertEquals(1, combination.conflicts().size());
        assertEquals(Map.of(2, "LOT1", 3, "LOT2"), combination.conflicts().get(0).values());
        assertNull(combination.elementString());
        assertFalse(combination.encodable());
    }

    @Test
    void theSameBarcodeScannedAgainIsRefusedWhateverItsCorrelationId() {
        ScanCombiner combiner = combiner();
        take(combiner, "11111111~" + SSCC);

        Added again = combiner.add("22222222~" + SSCC);

        assertFalse(again.accepted());
        assertEquals("combine.refused.alreadyScanned", again.refusal().key());
        assertEquals(List.of("1"), again.refusal().args());
        assertEquals(1, combiner.scans().size());
    }

    @Test
    void aFourthBarcodeIsRefused() {
        ScanCombiner combiner = combiner();
        take(combiner, SSCC, CONTENTS, DATES);

        assertTrue(combiner.isFull());
        Added fourth = combiner.add("]C1" + "00" + "193123450000000019");

        assertFalse(fourth.accepted());
        assertEquals("combine.refused.full", fourth.refusal().key());
        assertEquals(List.of("3"), fourth.refusal().args());
        assertEquals(3, combiner.scans().size());
    }

    @Test
    void aBarcodeThatIsNotGs1128IsRefusedByItsAimCodeId() {
        ScanCombiner combiner = combiner();

        Added added = combiner.add("]d2" + "00" + "093123450000000012");

        assertFalse(added.accepted());
        assertEquals("combine.refused.notGs1128", added.refusal().key());
        assertEquals("]d2", added.refusal().args().get(0));
        assertTrue(combiner.scans().isEmpty());
    }

    @Test
    void aBarcodeWithoutAnAimCodeIdIsTakenAsGs1128() {
        ScanCombiner combiner = combiner();

        // The entry field is the way in on a machine with no scanner, and nothing typed there
        // carries an AIM Code ID.
        take(combiner, "00093123450000000012");

        assertEquals(1, combiner.scans().size());
    }

    @Test
    void aBarcodeWhoseAisCannotBeReadIsRefused() {
        ScanCombiner combiner = combiner();

        // (01) wants 14 digits and this carries 13, so the element string runs out mid-value:
        // GE-S003, a SYNTAX_ERROR. Nothing can be combined from AIs that could not be read.
        Added added = combiner.add("]C1" + "01" + "0950600013435");

        assertFalse(added.accepted());
        assertEquals("combine.refused.unreadable", added.refusal().key());
        assertEquals("GE-S003", added.refusal().args().get(0));
        assertTrue(combiner.scans().isEmpty());
    }

    @Test
    void aBarcodeWhoseAisReadButFailValidationIsTakenAndJudgedOnTheCombination() {
        ScanCombiner combiner = combiner();

        // A bad check digit is a FORMAT_ERROR, not a SYNTAX_ERROR: the AIs were read, so the scan
        // is taken and the fault shows up against the combination instead of hiding the barcode.
        take(combiner, "]C1" + "00" + "09312345000000001X");

        assertEquals(1, combiner.scans().size());
        assertFalse(combiner.combine().encodable(), "the combination carries the format error");
    }

    @Test
    void anEmptyBarcodeIsRefused() {
        ScanCombiner combiner = combiner();

        assertEquals("combine.refused.empty", combiner.add("").refusal().key());
        assertEquals("combine.refused.empty", combiner.add(null).refusal().key());
    }

    @Test
    void removeLastAndClearDropScans() {
        ScanCombiner combiner = combiner();
        take(combiner, SSCC, CONTENTS, DATES);

        combiner.removeLast();
        assertEquals(2, combiner.scans().size());
        assertFalse(combiner.isFull());

        combiner.clear();
        assertTrue(combiner.scans().isEmpty());
        assertNull(combiner.combine().elementString(), "nothing held, nothing combined");
        assertFalse(combiner.combine().encodable());
    }

    @Test
    void reparseKeepsTheScansAndSpeaksTheNewLanguage() {
        ScanCombiner combiner = combiner();
        // The contents barcode alone carries a GE-S005: (02) and (37) require (00).
        take(combiner, CONTENTS, DATES);
        String english = combiner.scans().get(0).result().getIssues().get(0).getMessage();

        combiner.reparse(ParseConfig.builder()
                .requestedParseMode(GaiaConstants.ParseMode.INTERPRETATION)
                .language(GaiaConstants.Language.GERMAN)
                .build());

        assertEquals(2, combiner.scans().size(), "every scan is kept");
        assertEquals(CONTENTS, combiner.scans().get(0).input(), "in the order taken");
        String german = combiner.scans().get(0).result().getIssues().get(0).getMessage();
        assertEquals("GE-S005", combiner.scans().get(0).result().getIssues().get(0).getId());
        assertFalse(english.equals(german), "the scan's own issue is now in German: " + german);

        // And so is what comes after: the next scan and the combination.
        take(combiner, SSCC);
        assertTrue(combiner.combine().encodable());
    }

    @Test
    void removeLastOnAnEmptyCombinerDoesNothing() {
        ScanCombiner combiner = combiner();

        combiner.removeLast();

        assertTrue(combiner.scans().isEmpty());
    }

    @Test
    void aFixedLengthAiIsJoinedWithoutASeparatorAndAVariableOneWithIt() {
        // (00) is fixed length, so no FNC1 follows it; (10) is variable, so one does — except at
        // the end, where GS1 does not want a trailing separator.
        String joined = ScanCombiner.join(List.of(
                new ScanCombiner.Element("00", "093123450000000012", true, 1),
                new ScanCombiner.Element("10", "LOT1", false, 1),
                new ScanCombiner.Element("15", "261231", true, 2)));

        assertEquals("00093123450000000012" + "10LOT1" + GS + "15261231", joined);
    }
}
