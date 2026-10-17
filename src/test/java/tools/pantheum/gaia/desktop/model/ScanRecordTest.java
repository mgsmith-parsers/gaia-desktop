package tools.pantheum.gaia.desktop.model;

import tools.pantheum.gaia.GaiaParser;
import tools.pantheum.gaia.result.ParseResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Tests for {@link ScanRecord}, in particular how a capture's timestamp is shown. */
@DisplayName("ScanRecord")
class ScanRecordTest {

    private static final GaiaParser PARSER = new GaiaParser();
    private static final String INPUT = "0109506000134352";

    private static ScanRecord at(LocalDateTime when) {
        ParseResult result = PARSER.parse(INPUT);
        return ScanRecord.restored(INPUT, ScanSource.SCANNER, "Data Matrix", when, result);
    }

    @Test
    @DisplayName("a capture made today shows the time alone")
    void todayDropsTheDate() {
        LocalDate today = LocalDate.of(2026, 8, 9);
        ScanRecord r = at(today.atTime(14, 22, 5));

        assertEquals("14:22:05", r.timestamp(today));
    }

    @Test
    @DisplayName("a capture from another day keeps its date")
    void otherDaysKeepTheDate() {
        LocalDate today = LocalDate.of(2026, 8, 9);
        ScanRecord yesterday = at(today.minusDays(1).atTime(23, 59, 59));
        ScanRecord lastYear  = at(LocalDateTime.of(2025, 12, 31, 8, 0, 0));

        assertEquals("2026-08-08 23:59:59", yesterday.timestamp(today));
        assertEquals("2025-12-31 08:00:00", lastYear.timestamp(today));
    }

    @Test
    @DisplayName("the same rule reaches the report's provenance line")
    void provenanceFollowsTheSameRule() {
        LocalDate today = LocalDate.of(2026, 8, 9);
        ScanRecord r = at(today.minusDays(3).atTime(9, 15, 0));

        // provenance() resolves "today" itself, so assert on the parts that are fixed.
        assertTrue(r.provenance().startsWith("scanner · Data Matrix · "), r.provenance());
        assertTrue(r.provenance().contains("2026-08-06 09:15:00"),
                "an older capture must carry its date into the report: " + r.provenance());
    }

    @Test
    @DisplayName("time() stays the bare clock time, whatever day the capture is from")
    void timeIsAlwaysJustTheClock() {
        assertEquals("09:15:00", at(LocalDateTime.of(2020, 1, 1, 9, 15, 0)).time());
    }

    @Test
    @DisplayName("a Digital Link capture shows its URL, not its HRI")
    void digitalLinkSummaryIsTheUrl() {
        String url = "https://id.gs1.org/01/09506000134352/10/LOT-ABC";
        ParseResult resp = PARSER.parse(url);
        assertTrue(resp.getAiObject().hasDigitalLink(), "precondition: parses as a Digital Link");

        ScanRecord r = ScanRecord.restored(url, ScanSource.BUILT, null,
                LocalDateTime.now(), resp);

        assertEquals(url, r.summary(),
                "the URL is what was built or pasted, and what the user is looking for");
    }

    @Test
    @DisplayName("an element-string capture still shows its HRI, which is the readable form")
    void elementStringSummaryIsTheHri() {
        String elementString = "01095060001343521726123110LOT-001";
        ParseResult resp = PARSER.parse(elementString);
        assertFalse(resp.getAiObject().hasDigitalLink(), "precondition: not a Digital Link");

        ScanRecord r = ScanRecord.restored(elementString, ScanSource.MANUAL, null,
                LocalDateTime.now(), resp);

        assertEquals(resp.getAiObject().toHriString(), r.summary());
        assertTrue(r.summary().contains("(01)"), "got: " + r.summary());
    }

    @Test
    @DisplayName("a new capture is stamped with the current date, not just a time")
    void newCapturesCarryADate() {
        ScanRecord r = new ScanRecord(INPUT, ScanSource.MANUAL, null, PARSER.parse(INPUT));
        assertEquals(LocalDate.now(), r.captured().toLocalDate());
    }
}
