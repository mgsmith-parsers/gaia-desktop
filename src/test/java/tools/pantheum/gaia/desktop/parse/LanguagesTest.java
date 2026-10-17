package tools.pantheum.gaia.desktop.parse;

import tools.pantheum.gaia.GaiaConstants;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link Languages#fromArgument}, which backs {@code --language=…}.
 *
 * <p>The argument is typed by a person into a shortcut or a script, so the parsing is generous
 * on purpose: three spellings are all reasonable, and none of them should have to be looked up.
 * These tests pin that generosity, and pin the refusal to guess.
 */
@DisplayName("Languages.fromArgument")
class LanguagesTest {

    @ParameterizedTest(name = "\"{0}\" → FRENCH")
    @ValueSource(strings = {"FR", "fr", "Fr", " fr ", "FRENCH", "french", "fr-FR", "fr-CA"})
    @DisplayName("the catalogue code, the enum name and a BCP 47 tag all resolve")
    void theThreeSpellings(String value) {
        assertEquals(GaiaConstants.Language.FRENCH, Languages.fromArgument(value));
    }

    @Test
    @DisplayName("the codes that are not plain ISO language codes work too")
    void extendedCodes() {
        assertEquals(GaiaConstants.Language.NIGERIAN_PIDGIN, Languages.fromArgument("PCM"));
        assertEquals(GaiaConstants.Language.EGYPTIAN_ARABIC, Languages.fromArgument("ARZ"));
        assertEquals(GaiaConstants.Language.CANTONESE,       Languages.fromArgument("YUE"));
        assertEquals(GaiaConstants.Language.WU_CHINESE,      Languages.fromArgument("WUU"));
        // The enum name carries an underscore; a hyphen is the friendlier thing to type.
        assertEquals(GaiaConstants.Language.WU_CHINESE, Languages.fromArgument("WU-CHINESE"));
        assertEquals(GaiaConstants.Language.WU_CHINESE, Languages.fromArgument("WU_CHINESE"));
    }

    @Test
    @DisplayName("a region distinguishes the two cases a bare code cannot")
    void regionsThatMatter() {
        // Both are real GS1 catalogues, and neither is reachable from its bare language code.
        assertEquals(GaiaConstants.Language.CANTONESE,       Languages.fromArgument("zh-HK"));
        assertEquals(GaiaConstants.Language.EGYPTIAN_ARABIC, Languages.fromArgument("ar-EG"));
        assertEquals(GaiaConstants.Language.CHINESE,         Languages.fromArgument("zh-CN"));
        assertEquals(GaiaConstants.Language.ARABIC,          Languages.fromArgument("ar-SA"));
    }

    @ParameterizedTest(name = "\"{0}\" is refused")
    @ValueSource(strings = {"", "   ", "xx", "klingon", "français", "FR;rm -rf", "12"})
    @DisplayName("anything else is refused rather than guessed at")
    void refused(String value) {
        assertNull(Languages.fromArgument(value),
                "a value that names no catalogue must be ignored, so a typo in a shortcut "
                        + "leaves the app in the saved language rather than a surprising one");
    }

    @Test
    @DisplayName("null is refused rather than throwing")
    void nullIsRefused() {
        assertNull(Languages.fromArgument(null));
    }

    @Test
    @DisplayName("every language this build carries is reachable by its own code")
    void everyLanguageIsReachable() {
        for (GaiaConstants.Language lang : GaiaConstants.Language.values()) {
            assertEquals(lang, Languages.fromArgument(Languages.code(lang)),
                    "unreachable by code: " + lang);
            assertEquals(lang, Languages.fromArgument(lang.name()),
                    "unreachable by enum name: " + lang);
        }
    }

    @Test
    @DisplayName("the accepted-codes list names them all, for the warning on a bad value")
    void acceptedCodesListsEverything() {
        String codes = Languages.acceptedCodes();
        assertNotNull(codes);
        for (GaiaConstants.Language lang : GaiaConstants.Language.values()) {
            assertTrue(codes.contains(Languages.code(lang)), "missing from the list: " + lang);
        }
    }
}
