package tools.pantheum.gaia.desktop.i18n;

import tools.pantheum.gaia.GaiaConstants;
import tools.pantheum.gaia.desktop.parse.Languages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Properties;

/**
 * The label catalogue behind every piece of user-visible text in the app — menus, buttons,
 * the options panel, the status line, and the HTML report.
 *
 * <p>One properties file per language lives in {@code /i18n/ui_<CODE>.properties}, where
 * {@code CODE} is the same code the core uses for its error catalogues ({@code EN}, {@code FR},
 * {@code PCM}, …). Files are read as UTF-8, not the ISO-8859-1 that {@code Properties} assumes
 * by default, so the translations are stored readably rather than as {@code \\uXXXX} escapes.
 *
 * <p><b>Fallback is per key, not per file.</b> A key missing from a language falls back to
 * English on its own, which is what lets a partially translated language be useful the moment
 * its first key lands rather than only when it is complete. The English catalogue is the
 * contract: every key must exist there.
 *
 * <p>Catalogues are cached per language after first use — a language switch re-reads nothing.
 */
public final class UiStrings {

    private static final Logger log = LoggerFactory.getLogger(UiStrings.class);

    private static final String PATH = "/i18n/ui_%s.properties";

    private static final Map<GaiaConstants.Language, Properties> CACHE =
            Collections.synchronizedMap(new EnumMap<>(GaiaConstants.Language.class));

    private final GaiaConstants.Language language;
    private final Properties catalogue;
    private final Properties english;

    private UiStrings(GaiaConstants.Language language) {
        this.language  = language;
        this.catalogue = load(language);
        this.english   = load(GaiaConstants.Language.ENGLISH);
    }

    /** The catalogue for {@code language}. */
    public static UiStrings of(GaiaConstants.Language language) {
        return new UiStrings(language != null ? language : GaiaConstants.Language.ENGLISH);
    }

    /** The language this catalogue was built for. */
    public GaiaConstants.Language language() {
        return language;
    }

    /** Whether this language reads right to left, so the UI and report need RTL orientation. */
    public boolean isRightToLeft() {
        return Languages.isRightToLeft(language);
    }

    /**
     * The label for {@code key}, falling back to English and then — so a typo shows up on
     * screen instead of throwing in a layout pass — to the key itself.
     */
    public String get(String key) {
        String value = catalogue.getProperty(key);
        if (value == null) value = english.getProperty(key);
        if (value == null) {
            log.warn("No label for key '{}'", key);
            return key;
        }
        return value;
    }

    /** {@link #get(String)} with {@code {0}}, {@code {1}}, … replaced by {@code args}. */
    public String format(String key, Object... args) {
        String value = get(key);
        for (int i = 0; i < args.length; i++) {
            value = value.replace("{" + i + "}", String.valueOf(args[i]));
        }
        return value;
    }

    private static Properties load(GaiaConstants.Language language) {
        Properties cached = CACHE.get(language);
        if (cached != null) return cached;

        Properties p = new Properties();
        String path = String.format(PATH, Languages.code(language));
        try (InputStream in = UiStrings.class.getResourceAsStream(path)) {
            if (in == null) {
                // Not fatal: every lookup falls through to English.
                log.warn("No UI catalogue at {} — falling back to English", path);
            } else {
                try (Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                    p.load(reader);
                }
            }
        } catch (IOException e) {
            log.warn("Could not read {}: {}", path, e.getMessage());
        }

        CACHE.put(language, p);
        return p;
    }
}
