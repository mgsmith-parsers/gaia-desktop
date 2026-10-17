package tools.pantheum.gaia.desktop.store;

import tools.pantheum.gaia.GaiaConstants;
import tools.pantheum.gaia.desktop.Theme;
import tools.pantheum.gaia.desktop.parse.ParseOptions;
import tools.pantheum.gaia.desktop.parse.SystemDefaults;
import tools.pantheum.gaia.desktop.scanner.ScannerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Properties;
import java.util.function.Function;

/**
 * Reads and writes {@code ~/.gaia-desktop/options.properties} — the parser options plus the
 * bits of window state worth remembering: the theme, whether the options panel is open, the
 * last scanner device, which mode (Parse, Build or Combine) the window was left in, and the
 * update check's settings.
 *
 * <p>On a first launch, when no file exists, the options are seeded from the machine's
 * regional settings by {@link SystemDefaults} rather than from GAIA's built-in defaults.
 *
 * <p>A missing or damaged file is never fatal: every value falls back to its default
 * individually, so hand-editing one line wrong costs that line and nothing else.
 */
public final class OptionsStore {

    private static final Logger log = LoggerFactory.getLogger(OptionsStore.class);

    private OptionsStore() {}

    /** Bumped when the key set changes in a way older files cannot satisfy. */
    private static final String VERSION = "1";

    private static final String K_VERSION       = "file.version";
    private static final String K_MODE          = "parse.mode";
    private static final String K_LANGUAGE      = "parse.language";
    private static final String K_ENDIAN        = "date.endian";
    private static final String K_SEPARATOR     = "date.separator";
    private static final String K_MONTH         = "date.month";
    private static final String K_YEAR          = "date.year";
    private static final String K_SKIP_REQUIRES = "check.skipRequires";
    private static final String K_SKIP_EXCLUDES = "check.skipExcludes";
    private static final String K_THEME         = "ui.theme";
    private static final String K_OPTIONS_PANE  = "ui.optionsPane";
    private static final String K_DEVICE        = "ui.scannerDevice";
    private static final String K_UI_MODE       = "ui.mode";
    private static final String K_UPDATE_CHECK  = "update.check";
    private static final String K_UPDATE_LAST   = "update.lastCheck";
    private static final String K_UPDATE_SKIP   = "update.skipVersion";

    /** The window state stored alongside the parser options. */
    public static final class UiState {
        private Theme   theme          = Theme.LIGHT;
        private boolean optionsPaneOpen;                 // closed on a first launch
        private String  scannerDevice  = ScannerService.DEFAULT_LOGICAL_NAME;
        private String  mode           = "PARSE";        // parse on a first launch
        private boolean updateCheck    = true;           // on unless switched off
        private Instant lastUpdateCheck;                 // null: never checked
        private String  skippedVersion;                  // null: none skipped

        public Theme   theme()           { return theme; }
        public boolean optionsPaneOpen() { return optionsPaneOpen; }
        public String  scannerDevice()   { return scannerDevice; }
        /** "PARSE", "BUILD" or "COMBINE" — which strip the window opens in. */
        public String  mode()            { return mode; }
        /** Whether the app checks GitHub for a newer release on its own. */
        public boolean updateCheck()     { return updateCheck; }
        /** When it last did, or null if it never has. */
        public Instant lastUpdateCheck() { return lastUpdateCheck; }
        /** A release the user chose not to be reminded of, or null. */
        public String  skippedVersion()  { return skippedVersion; }

        public void theme(Theme t)             { this.theme = t; }
        public void optionsPaneOpen(boolean b) { this.optionsPaneOpen = b; }
        /** Ignores null or blank — a cleared control is not a new setting. */
        public void scannerDevice(String s)    { if (s != null && !s.trim().isEmpty()) this.scannerDevice = s; }
        public void mode(String m)             { if (m != null && !m.trim().isEmpty()) this.mode = m; }
        public void updateCheck(boolean b)     { this.updateCheck = b; }
        public void lastUpdateCheck(Instant i) { this.lastUpdateCheck = i; }
        /** Null or blank clears it. */
        public void skippedVersion(String v)   { this.skippedVersion = v == null || v.trim().isEmpty() ? null : v.trim(); }
    }

    /**
     * Loads saved options into {@code options} and {@code ui}.
     *
     * @return {@code true} when a saved file was read, {@code false} on a first launch —
     *         in which case {@code options} has been seeded from the system settings
     */
    public static boolean load(ParseOptions options, UiState ui) {
        Path file = AppPaths.optionsFile();
        if (!Files.isReadable(file)) {
            log.info("No saved options at {} — seeding from the system regional settings", file);
            SystemDefaults.applyTo(options);
            return false;
        }

        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(file)) {
            p.load(in);
        } catch (IOException e) {
            log.warn("Could not read {} — falling back to the system regional settings: {}", file, e.getMessage());
            SystemDefaults.applyTo(options);
            return false;
        }

        // Start from the system defaults so that a key added in a later version, and therefore
        // absent from this file, still lands on something sensible rather than on GAIA's
        // built-in default.
        SystemDefaults.applyTo(options);

        options.parseModeProperty()    .set(readEnum(p, K_MODE,      GaiaConstants.ParseMode::valueOf,     options.parseModeProperty().get()));
        options.languageProperty()     .set(readEnum(p, K_LANGUAGE,  GaiaConstants.Language::valueOf,      options.languageProperty().get()));
        options.dateEndianProperty()   .set(readEnum(p, K_ENDIAN,    GaiaConstants.DateEndian::valueOf,    options.dateEndianProperty().get()));
        options.dateSeparatorProperty().set(readEnum(p, K_SEPARATOR, GaiaConstants.DateSeparator::valueOf, options.dateSeparatorProperty().get()));
        options.monthFormatProperty()  .set(readEnum(p, K_MONTH,     GaiaConstants.MonthFormat::valueOf,   options.monthFormatProperty().get()));
        options.yearFormatProperty()   .set(readEnum(p, K_YEAR,      GaiaConstants.YearFormat::valueOf,    options.yearFormatProperty().get()));
        options.skipRequiresCheckProperty().set(readBoolean(p, K_SKIP_REQUIRES, false));
        options.skipExcludesCheckProperty().set(readBoolean(p, K_SKIP_EXCLUDES, false));

        ui.theme(readEnum(p, K_THEME, Theme::valueOf, Theme.LIGHT));
        ui.optionsPaneOpen(readBoolean(p, K_OPTIONS_PANE, false));
        String device = p.getProperty(K_DEVICE);
        if (device != null && !device.trim().isEmpty()) ui.scannerDevice(device.trim());
        String uiMode = p.getProperty(K_UI_MODE);
        if ("BUILD".equals(uiMode) || "PARSE".equals(uiMode) || "COMBINE".equals(uiMode)) ui.mode(uiMode);
        ui.updateCheck(readBoolean(p, K_UPDATE_CHECK, true));
        ui.lastUpdateCheck(readInstant(p, K_UPDATE_LAST));
        ui.skippedVersion(p.getProperty(K_UPDATE_SKIP));

        log.info("Loaded options from {}", file);
        return true;
    }

    /** Writes the current options and window state, replacing any previous file. */
    public static void save(ParseOptions options, UiState ui) {
        save(options, ui, options.languageProperty().get());
    }

    /**
     * {@link #save(ParseOptions, UiState)} with the message language written explicitly.
     *
     * <p>The one caller that needs this is a {@code --language} run: the window is showing the
     * language from the command line, but what belongs in the file is the language the user
     * actually chose, so a launcher shortcut cannot quietly overwrite their setting.
     */
    public static void save(ParseOptions options, UiState ui, GaiaConstants.Language language) {
        Properties p = new Properties();
        p.setProperty(K_VERSION,       VERSION);
        p.setProperty(K_MODE,          options.parseModeProperty().get().name());
        p.setProperty(K_LANGUAGE,      (language != null ? language : options.languageProperty().get()).name());
        p.setProperty(K_ENDIAN,        options.dateEndianProperty().get().name());
        p.setProperty(K_SEPARATOR,     options.dateSeparatorProperty().get().name());
        p.setProperty(K_MONTH,         options.monthFormatProperty().get().name());
        p.setProperty(K_YEAR,          options.yearFormatProperty().get().name());
        p.setProperty(K_SKIP_REQUIRES, String.valueOf(options.skipRequiresCheckProperty().get()));
        p.setProperty(K_SKIP_EXCLUDES, String.valueOf(options.skipExcludesCheckProperty().get()));
        p.setProperty(K_THEME,         ui.theme().name());
        p.setProperty(K_OPTIONS_PANE,  String.valueOf(ui.optionsPaneOpen()));
        // Never hand setProperty a null: it throws, and losing a setting must not take down
        // the caller — persistOptions() runs from UI event handlers, where an exception
        // escapes onto the FX thread.
        put(p, K_DEVICE,  ui.scannerDevice(), ScannerService.DEFAULT_LOGICAL_NAME);
        put(p, K_UI_MODE, ui.mode(),          "PARSE");
        p.setProperty(K_UPDATE_CHECK,  String.valueOf(ui.updateCheck()));
        if (ui.lastUpdateCheck() != null) p.setProperty(K_UPDATE_LAST, ui.lastUpdateCheck().toString());
        if (ui.skippedVersion()  != null) p.setProperty(K_UPDATE_SKIP, ui.skippedVersion());

        Path file = AppPaths.optionsFile();
        try {
            AppPaths.ensureHome();
            try (OutputStream out = Files.newOutputStream(file)) {
                p.store(out, "GAIA Desktop options — edit while the app is closed, or it will overwrite them");
            }
        } catch (IOException e) {
            // Losing settings is an annoyance, not a reason to interrupt the user mid-scan.
            log.warn("Could not write {}: {}", file, e.getMessage());
        }
    }

    /** Stores {@code value}, or {@code fallback} when it is null or blank. */
    private static void put(Properties p, String key, String value, String fallback) {
        p.setProperty(key, value == null || value.trim().isEmpty() ? fallback : value);
    }

    private static <T> T readEnum(Properties p, String key, Function<String, T> parser, T fallback) {
        String raw = p.getProperty(key);
        if (raw == null || raw.trim().isEmpty()) return fallback;
        try {
            return parser.apply(raw.trim());
        } catch (IllegalArgumentException e) {
            log.warn("Ignoring unknown value '{}' for {}", raw, key);
            return fallback;
        }
    }

    /** An ISO-8601 instant, or null when absent or unreadable — then a check is simply due. */
    private static Instant readInstant(Properties p, String key) {
        String raw = p.getProperty(key);
        if (raw == null || raw.trim().isEmpty()) return null;
        try {
            return Instant.parse(raw.trim());
        } catch (DateTimeParseException e) {
            log.warn("Ignoring unreadable time '{}' for {}", raw, key);
            return null;
        }
    }

    private static boolean readBoolean(Properties p, String key, boolean fallback) {
        String raw = p.getProperty(key);
        if (raw == null) return fallback;
        String v = raw.trim();
        return v.equalsIgnoreCase("true") || v.equalsIgnoreCase("false")
                ? Boolean.parseBoolean(v)
                : fallback;
    }
}
