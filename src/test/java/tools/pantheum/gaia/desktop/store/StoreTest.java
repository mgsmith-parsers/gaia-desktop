package tools.pantheum.gaia.desktop.store;

import tools.pantheum.gaia.GaiaConstants;
import tools.pantheum.gaia.GaiaParser;
import tools.pantheum.gaia.desktop.Theme;
import tools.pantheum.gaia.desktop.model.ScanRecord;
import tools.pantheum.gaia.desktop.model.ScanSource;
import tools.pantheum.gaia.desktop.parse.Languages;
import tools.pantheum.gaia.desktop.parse.ParseOptions;
import tools.pantheum.gaia.desktop.scanner.ScannerService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link OptionsStore} and {@link CaptureStore}.
 *
 * <p>Every test redirects the state directory with {@code gaia.desktop.home}, so nothing here
 * touches the real {@code ~/.gaia-desktop}.
 */
@DisplayName("Persistence")
class StoreTest {

    private static final GaiaParser PARSER = new GaiaParser();
    private static final String GS = String.valueOf((char) 0x1D);

    @TempDir Path home;

    @BeforeEach
    void redirectHome() {
        System.setProperty(AppPaths.HOME_PROPERTY, home.toString());
    }

    @AfterEach
    void restoreHome() {
        System.clearProperty(AppPaths.HOME_PROPERTY);
    }

    // ------------------------------------------------------------------ options

    @Test
    @DisplayName("options and captures live in separate files")
    void separateFiles() {
        assertNotEquals(AppPaths.optionsFile(), AppPaths.capturesFile());
        assertEquals(home.resolve("options.properties"), AppPaths.optionsFile());
        assertEquals(home.resolve("captures.tsv"), AppPaths.capturesFile());
    }

    @Test
    @DisplayName("a first launch reports no saved file and seeds from the system settings")
    void firstLaunch() {
        ParseOptions options = new ParseOptions();
        OptionsStore.UiState ui = new OptionsStore.UiState();

        assertFalse(OptionsStore.load(options, ui), "no file yet — this is a first launch");
        assertFalse(ui.optionsPaneOpen(), "the options panel starts closed");

        GaiaConstants.Language fromSystem = Languages.fromLocale(Locale.getDefault());
        assertEquals(fromSystem != null ? fromSystem : GaiaConstants.Language.ENGLISH,
                options.languageProperty().get(),
                "the language must come from the machine's locale, not from GAIA's default");
    }

    @Test
    @DisplayName("options survive a save/load round trip")
    void optionsRoundTrip() {
        ParseOptions saved = new ParseOptions();
        saved.parseModeProperty().set(GaiaConstants.ParseMode.CONTENT);
        saved.languageProperty().set(GaiaConstants.Language.JAPANESE);
        saved.dateEndianProperty().set(GaiaConstants.DateEndian.BIG);
        saved.dateSeparatorProperty().set(GaiaConstants.DateSeparator.HYPHEN);
        saved.monthFormatProperty().set(GaiaConstants.MonthFormat.THREE_LETTER);
        saved.yearFormatProperty().set(GaiaConstants.YearFormat.TWO_DIGIT);
        saved.skipRequiresCheckProperty().set(true);

        OptionsStore.UiState savedUi = new OptionsStore.UiState();
        savedUi.theme(Theme.DARK);
        savedUi.optionsPaneOpen(true);
        savedUi.scannerDevice("ZebraScannerSNAPI");

        OptionsStore.save(saved, savedUi);
        assertTrue(Files.exists(AppPaths.optionsFile()));

        ParseOptions loaded = new ParseOptions();
        OptionsStore.UiState loadedUi = new OptionsStore.UiState();
        assertTrue(OptionsStore.load(loaded, loadedUi), "a saved file must be reported as found");

        assertEquals(GaiaConstants.ParseMode.CONTENT,        loaded.parseModeProperty().get());
        assertEquals(GaiaConstants.Language.JAPANESE,        loaded.languageProperty().get());
        assertEquals(GaiaConstants.DateEndian.BIG,           loaded.dateEndianProperty().get());
        assertEquals(GaiaConstants.DateSeparator.HYPHEN,     loaded.dateSeparatorProperty().get());
        assertEquals(GaiaConstants.MonthFormat.THREE_LETTER, loaded.monthFormatProperty().get());
        assertEquals(GaiaConstants.YearFormat.TWO_DIGIT,     loaded.yearFormatProperty().get());
        assertTrue(loaded.skipRequiresCheckProperty().get());
        assertFalse(loaded.skipExcludesCheckProperty().get());

        assertEquals(Theme.DARK, loadedUi.theme());
        assertTrue(loadedUi.optionsPaneOpen());
        assertEquals("ZebraScannerSNAPI", loadedUi.scannerDevice());
    }

    @Test
    @DisplayName("the update check's settings survive a round trip, and default to checking")
    void updateSettingsRoundTrip() {
        OptionsStore.UiState fresh = new OptionsStore.UiState();
        assertTrue(fresh.updateCheck(), "on unless switched off");
        assertNull(fresh.lastUpdateCheck(), "never checked");
        assertNull(fresh.skippedVersion());

        OptionsStore.UiState savedUi = new OptionsStore.UiState();
        savedUi.updateCheck(false);
        savedUi.lastUpdateCheck(Instant.parse("2026-10-17T09:30:00Z"));
        savedUi.skippedVersion("1.1.0");
        OptionsStore.save(new ParseOptions(), savedUi);

        OptionsStore.UiState loaded = new OptionsStore.UiState();
        OptionsStore.load(new ParseOptions(), loaded);
        assertFalse(loaded.updateCheck());
        assertEquals(Instant.parse("2026-10-17T09:30:00Z"), loaded.lastUpdateCheck());
        assertEquals("1.1.0", loaded.skippedVersion());
    }

    @Test
    @DisplayName("an unreadable last-check time just makes a check due")
    void damagedUpdateTime() throws IOException {
        Files.write(AppPaths.optionsFile(),
                "update.lastCheck=yesterday-ish\nupdate.check=maybe\n".getBytes(StandardCharsets.UTF_8));

        OptionsStore.UiState ui = new OptionsStore.UiState();
        OptionsStore.load(new ParseOptions(), ui);
        assertNull(ui.lastUpdateCheck());
        assertTrue(ui.updateCheck(), "an unreadable switch keeps the default");
    }

    @Test
    @DisplayName("one bad line costs that line only")
    void damagedOptionsFile() throws IOException {
        Files.write(AppPaths.optionsFile(),
                ("parse.mode=NOT_A_MODE\n"
                        + "parse.language=JAPANESE\n"
                        + "ui.theme=chartreuse\n").getBytes(StandardCharsets.UTF_8));

        ParseOptions options = new ParseOptions();
        OptionsStore.UiState ui = new OptionsStore.UiState();
        OptionsStore.load(options, ui);

        assertEquals(GaiaConstants.Language.JAPANESE, options.languageProperty().get(),
                "the readable keys must still be applied");
        assertEquals(GaiaConstants.ParseMode.INTERPRETATION, options.parseModeProperty().get(),
                "an unknown enum value falls back rather than throwing");
        assertEquals(Theme.LIGHT, ui.theme());
    }

    @Test
    @DisplayName("a null device never crashes the save")
    void nullDeviceDoesNotCrashSave() {
        // Regression: a language change repopulates the device combo, which clears its
        // selection and fires onAction with a null value. That reached setProperty(key, null),
        // which throws — from a UI event handler, so it escaped onto the FX thread.
        OptionsStore.UiState ui = new OptionsStore.UiState();
        ui.scannerDevice(null);
        ui.mode(null);

        OptionsStore.save(new ParseOptions(), ui);   // must not throw

        ParseOptions loadedOptions = new ParseOptions();
        OptionsStore.UiState loaded = new OptionsStore.UiState();
        assertTrue(OptionsStore.load(loadedOptions, loaded), "the file must still be written and readable");
        assertEquals(ScannerService.DEFAULT_LOGICAL_NAME, loaded.scannerDevice());
        assertEquals("PARSE", loaded.mode());
    }

    @Test
    @DisplayName("a cleared control does not overwrite a good setting")
    void nullIsNotASetting() {
        OptionsStore.UiState ui = new OptionsStore.UiState();
        ui.scannerDevice("ZebraScannerSNAPI");
        ui.mode("BUILD");

        ui.scannerDevice(null);
        ui.scannerDevice("   ");
        ui.mode(null);

        assertEquals("ZebraScannerSNAPI", ui.scannerDevice(), "null must be ignored, not stored");
        assertEquals("BUILD", ui.mode());
    }

    // ------------------------------------------------------------------ captures

    @Test
    @DisplayName("an input keeps its FNC1 separators through a save/load round trip")
    void capturesRoundTrip() {
        String withGs = "0109506000134352" + GS + "10LOT-001";
        List<ScanRecord> records = List.of(
                new ScanRecord(withGs, ScanSource.SCANNER, "Data Matrix", PARSER.parse(withGs)),
                new ScanRecord("0109506000134352", ScanSource.MANUAL, null, PARSER.parse("0109506000134352")));

        CaptureStore.save(records);
        List<CaptureStore.StoredCapture> loaded = CaptureStore.load();

        assertEquals(2, loaded.size());
        assertEquals(withGs, loaded.get(0).input(), "the 0x1D separator must survive the file");
        assertTrue(loaded.get(0).input().contains(GS));
        assertEquals(ScanSource.SCANNER, loaded.get(0).source());
        assertEquals("Data Matrix", loaded.get(0).symbology());
        assertEquals(records.get(0).captured().withNano(0), loaded.get(0).captured().withNano(0),
                "the full date and time must survive, not just the clock time");
        assertEquals(java.time.LocalDate.now(), loaded.get(0).captured().toLocalDate());

        assertEquals(ScanSource.MANUAL, loaded.get(1).source());
        org.junit.jupiter.api.Assertions.assertNull(loaded.get(1).symbology(),
                "a manual capture has no symbology");
    }

    @Test
    @DisplayName("escaping survives every control character a scanner can emit")
    void escapingRoundTrip() {
        StringBuilder nasty = new StringBuilder("start");
        for (char c = 0; c < 0x20; c++) nasty.append(c);
        nasty.append((char) 0x7F).append("\\x1D\\\\").append("end");

        String escaped = CaptureStore.escape(nasty.toString());
        assertFalse(escaped.contains("\t"), "a tab would split the record into extra fields");
        assertFalse(escaped.contains("\n"), "a newline would split the record across lines");
        assertEquals(nasty.toString(), CaptureStore.unescape(escaped));
    }

    @Test
    @DisplayName("a malformed line is skipped, not fatal")
    void damagedCaptureFile() throws IOException {
        Files.write(AppPaths.capturesFile(),
                ("# comment\n"
                        + "not-a-time\tMANUAL\t\t0109506000134352\n"
                        + "2026-08-09T12:00:00\tMANUAL\t\t0109506000134352\n"
                        + "\n"
                        + "2026-08-09T12:00:01\tNOT_A_SOURCE\t\t0109506000134353\n").getBytes(StandardCharsets.ISO_8859_1));

        List<CaptureStore.StoredCapture> loaded = CaptureStore.load();
        assertEquals(2, loaded.size(), "the unreadable time is dropped; the unknown source is not");
        assertEquals("0109506000134352", loaded.get(0).input());
        assertEquals(ScanSource.FILE, loaded.get(1).source(), "an unknown source degrades to FILE");
    }

    @Test
    @DisplayName("the history is capped so the file cannot grow without bound")
    void historyIsCapped() {
        List<ScanRecord> many = new java.util.ArrayList<>();
        for (int i = 0; i < CaptureStore.MAX_ENTRIES + 50; i++) {
            many.add(new ScanRecord("0109506000134352", ScanSource.MANUAL, null,
                    PARSER.parse("0109506000134352")));
        }
        CaptureStore.save(many);
        assertEquals(CaptureStore.MAX_ENTRIES, CaptureStore.load().size());
    }

    @Test
    @DisplayName("clear removes the file")
    void clearRemovesFile() {
        CaptureStore.save(List.of(new ScanRecord("0109506000134352", ScanSource.MANUAL, null,
                PARSER.parse("0109506000134352"))));
        assertTrue(Files.exists(AppPaths.capturesFile()));

        CaptureStore.clear();
        assertFalse(Files.exists(AppPaths.capturesFile()));
        assertTrue(CaptureStore.load().isEmpty());
    }

    @Test
    @DisplayName("a capture file written before dates were recorded is still readable")
    void legacyTimeOnlyFile() throws IOException {
        // Files from the first release stored a bare clock time. They must still load —
        // dated to today, since the real date was never written down.
        Files.write(AppPaths.capturesFile(),
                "14:22:05.123\tSCANNER\tData Matrix\t0109506000134352\n".getBytes(StandardCharsets.ISO_8859_1));

        List<CaptureStore.StoredCapture> loaded = CaptureStore.load();
        assertEquals(1, loaded.size(), "a legacy line must not be discarded");
        assertEquals(java.time.LocalDate.now(), loaded.get(0).captured().toLocalDate());
        assertEquals(java.time.LocalTime.of(14, 22, 5, 123_000_000), loaded.get(0).captured().toLocalTime());
        assertEquals("0109506000134352", loaded.get(0).input());
    }

    @Test
    @DisplayName("a capture keeps its own date across a save/load cycle, not today's")
    void datesAreNotRewritten() {
        java.time.LocalDateTime lastWeek = java.time.LocalDateTime.now().minusDays(7).withNano(0);
        ScanRecord old = ScanRecord.restored("0109506000134352", ScanSource.FILE, null, lastWeek,
                PARSER.parse("0109506000134352"));

        CaptureStore.save(List.of(old));
        assertEquals(lastWeek, CaptureStore.load().get(0).captured());
    }
}
