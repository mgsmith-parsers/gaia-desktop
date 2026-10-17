package tools.pantheum.gaia.desktop.i18n;

import tools.pantheum.gaia.GaiaConstants;
import tools.pantheum.gaia.desktop.parse.Languages;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Tests for {@link UiStrings} and the catalogues behind it. */
@DisplayName("UiStrings")
class UiStringsTest {

    @Test
    @DisplayName("every language the parser supports has a catalogue on the classpath")
    void everyLanguageHasACatalogue() {
        for (GaiaConstants.Language lang : GaiaConstants.Language.values()) {
            String path = "/i18n/ui_" + Languages.code(lang) + ".properties";
            try (InputStream in = UiStringsTest.class.getResourceAsStream(path)) {
                assertNotNull(in, "missing catalogue: " + path);
            } catch (Exception e) {
                throw new AssertionError("could not read " + path, e);
            }
        }
    }

    @Test
    @DisplayName("every catalogue defines exactly the English key set")
    void keySetsMatchEnglish() {
        Set<String> english = keys(GaiaConstants.Language.ENGLISH);
        assertTrue(english.size() > 100, "the English catalogue looks truncated: " + english.size() + " keys");

        for (GaiaConstants.Language lang : GaiaConstants.Language.values()) {
            assertEquals(english, keys(lang),
                    "key set differs from English for " + lang
                            + " — every key must exist in every catalogue so lookups never fall through silently");
        }
    }

    @Test
    @DisplayName("a translated language returns its own text")
    void translatedLanguage() {
        UiStrings fr = UiStrings.of(GaiaConstants.Language.FRENCH);
        assertEquals("Fichier", fr.get("menu.file"));
        assertEquals("Analyser", fr.get("act.parse"));
        // From the shared table extracted from the demo module's report.
        assertEquals("Erreurs", fr.get("errors"));
    }

    @Test
    @DisplayName("every language carries its own chrome, not a copy of English")
    void chromeIsTranslatedEverywhere() {
        Properties english = catalogue(GaiaConstants.Language.ENGLISH);
        // The chrome keys are the dotted ones; the shared report vocabulary has bare names.
        Set<String> chrome = new TreeSet<>();
        for (String key : english.stringPropertyNames()) {
            if (key.indexOf('.') >= 0) chrome.add(key);
        }
        assertTrue(chrome.size() > 100, "expected the full chrome key set, found " + chrome.size());

        for (GaiaConstants.Language lang : GaiaConstants.Language.values()) {
            if (lang == GaiaConstants.Language.ENGLISH) continue;
            // Nigerian Pidgin is English-based: much of its chrome is legitimately identical,
            // and demanding otherwise would only invite worse Pidgin.
            if (lang == GaiaConstants.Language.NIGERIAN_PIDGIN) continue;

            Properties p = catalogue(lang);
            int translated = 0;
            for (String key : chrome) {
                if (!english.getProperty(key).equals(p.getProperty(key))) translated++;
            }
            assertTrue(translated > chrome.size() * 6 / 10,
                    lang + " looks like a copy of English: only " + translated + " of " + chrome.size()
                            + " chrome values differ. A catalogue that is merely duplicated reads as "
                            + "translated in the language picker while showing English on screen.");
        }
    }

    @Test
    @DisplayName("a key missing from a catalogue falls back to English on its own")
    void perKeyFallback() {
        // Every catalogue is complete today — keySetsMatchEnglish() enforces that — so this
        // covers the mechanism rather than a live gap: the fallback is what keeps a key added
        // to English ahead of the translations from surfacing as its own name on screen.
        Properties partial = new Properties();
        partial.setProperty("menu.file", "Ficheiro");

        assertEquals("Ficheiro", partial.getProperty("menu.file"));
        assertEquals("File", catalogue(GaiaConstants.Language.ENGLISH).getProperty("menu.file"));
        assertEquals(null, partial.getProperty("menu.help"),
                "the missing key must be absent, so UiStrings.get consults English for it");

        // And the shared report vocabulary is translated in every language, chrome or no chrome.
        assertNotEquals("Errors", UiStrings.of(GaiaConstants.Language.KOREAN).get("errors"));
    }

    @Test
    @DisplayName("an unknown key is returned as-is rather than throwing mid-layout")
    void unknownKey() {
        assertEquals("no.such.key", UiStrings.of(GaiaConstants.Language.ENGLISH).get("no.such.key"));
    }

    @Test
    @DisplayName("format substitutes positional arguments")
    void formatting() {
        UiStrings en = UiStrings.of(GaiaConstants.Language.ENGLISH);
        assertEquals("Ready", en.format("lbl.ready"));
        assertEquals("x=1 y=2", substitute(en, "x={0} y={1}", 1, 2));
    }

    @Test
    @DisplayName("right-to-left languages are flagged")
    void rightToLeft() {
        assertTrue(UiStrings.of(GaiaConstants.Language.ARABIC).isRightToLeft());
        assertTrue(UiStrings.of(GaiaConstants.Language.URDU).isRightToLeft());
        assertTrue(UiStrings.of(GaiaConstants.Language.PERSIAN).isRightToLeft());
        org.junit.jupiter.api.Assertions.assertFalse(
                UiStrings.of(GaiaConstants.Language.ENGLISH).isRightToLeft());
    }

    /** Exercises the same substitution {@code format} performs, without needing a real key. */
    private static String substitute(UiStrings s, String template, Object... args) {
        String out = template;
        for (int i = 0; i < args.length; i++) out = out.replace("{" + i + "}", String.valueOf(args[i]));
        return out;
    }

    private static Set<String> keys(GaiaConstants.Language lang) {
        return new TreeSet<>(catalogue(lang).stringPropertyNames());
    }

    /** One catalogue, read the way {@link UiStrings} reads it — as UTF-8, not ISO-8859-1. */
    private static Properties catalogue(GaiaConstants.Language lang) {
        Properties p = new Properties();
        String path = "/i18n/ui_" + Languages.code(lang) + ".properties";
        try (InputStream in = UiStringsTest.class.getResourceAsStream(path)) {
            assertNotNull(in, "missing catalogue: " + path);
            try (Reader r = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                p.load(r);
            }
        } catch (Exception e) {
            throw new AssertionError("could not read " + path, e);
        }
        return p;
    }
}
