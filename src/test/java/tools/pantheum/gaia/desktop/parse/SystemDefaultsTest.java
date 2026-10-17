package tools.pantheum.gaia.desktop.parse;

import tools.pantheum.gaia.GaiaConstants;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/** Tests for {@link SystemDefaults} — the first-launch options taken from the machine's locale. */
@DisplayName("SystemDefaults")
class SystemDefaultsTest {

    @Test
    @DisplayName("component order is read from the locale's short date pattern")
    void endianFromPattern() {
        assertEquals(GaiaConstants.DateEndian.LITTLE, SystemDefaults.endian("dd/MM/yyyy"));
        assertEquals(GaiaConstants.DateEndian.MIDDLE, SystemDefaults.endian("M/d/yy"));
        assertEquals(GaiaConstants.DateEndian.BIG,    SystemDefaults.endian("yyyy-MM-dd"));
        // Nothing recognisable: keep GAIA's own default rather than guessing.
        assertEquals(GaiaConstants.DateEndian.LITTLE, SystemDefaults.endian(""));
    }

    @Test
    @DisplayName("the separator is the first one the pattern actually uses")
    void separatorFromPattern() {
        assertEquals(GaiaConstants.DateSeparator.SLASH,  SystemDefaults.separator("dd/MM/yyyy"));
        assertEquals(GaiaConstants.DateSeparator.HYPHEN, SystemDefaults.separator("yyyy-MM-dd"));
        assertEquals(GaiaConstants.DateSeparator.PERIOD, SystemDefaults.separator("dd.MM.yyyy"));
        // Locales that separate with a word or a space keep the default.
        assertEquals(GaiaConstants.DateSeparator.SLASH,  SystemDefaults.separator("y'年'M'月'd'日'"));
    }

    @Test
    @DisplayName("month and year widths follow the pattern")
    void widthsFromPattern() {
        assertEquals(GaiaConstants.MonthFormat.TWO_DIGIT,    SystemDefaults.monthFormat("dd/MM/yyyy"));
        assertEquals(GaiaConstants.MonthFormat.THREE_LETTER, SystemDefaults.monthFormat("d MMM y"));
        assertEquals(GaiaConstants.YearFormat.FOUR_DIGIT,    SystemDefaults.yearFormat("dd/MM/yyyy"));
        assertEquals(GaiaConstants.YearFormat.TWO_DIGIT,     SystemDefaults.yearFormat("dd/MM/yy"));
    }

    @Test
    @DisplayName("the message language comes from the locale, falling back to English")
    void languageFromLocale() {
        assertEquals(GaiaConstants.Language.GERMAN,  SystemDefaults.language(Locale.GERMANY));
        assertEquals(GaiaConstants.Language.JAPANESE, SystemDefaults.language(Locale.JAPAN));
        assertEquals(GaiaConstants.Language.ENGLISH, SystemDefaults.language(Locale.of("is", "IS")));
    }

    @Test
    @DisplayName("regional variants the language code alone cannot carry")
    void regionalVariants() {
        assertEquals(GaiaConstants.Language.CANTONESE,
                Languages.fromLocale(Locale.forLanguageTag("zh-HK")));
        assertEquals(GaiaConstants.Language.CHINESE,
                Languages.fromLocale(Locale.forLanguageTag("zh-CN")));
        assertEquals(GaiaConstants.Language.EGYPTIAN_ARABIC,
                Languages.fromLocale(Locale.forLanguageTag("ar-EG")));
        assertEquals(GaiaConstants.Language.ARABIC,
                Languages.fromLocale(Locale.forLanguageTag("ar-SA")));
        assertNull(Languages.fromLocale(Locale.forLanguageTag("is-IS")));
    }

    @Test
    @DisplayName("a locale's settings are applied to the options as a whole")
    void appliedToOptions() {
        ParseOptions options = new ParseOptions();
        SystemDefaults.applyTo(options, Locale.GERMANY);

        assertEquals(GaiaConstants.Language.GERMAN, options.languageProperty().get());
        assertEquals(GaiaConstants.DateEndian.LITTLE, options.dateEndianProperty().get());
        assertEquals(GaiaConstants.DateSeparator.PERIOD, options.dateSeparatorProperty().get(),
                "German short dates are dd.MM.yy");
    }

    @Test
    @DisplayName("every JVM locale yields a usable pattern")
    void everyLocaleHasAPattern() {
        for (Locale locale : Locale.getAvailableLocales()) {
            String pattern = SystemDefaults.shortDatePattern(locale);
            org.junit.jupiter.api.Assertions.assertNotNull(pattern, "null pattern for " + locale);
            // Must not throw, and must land on a real enum constant for any locale on the JVM.
            SystemDefaults.endian(pattern);
            SystemDefaults.separator(pattern);
            SystemDefaults.monthFormat(pattern);
            SystemDefaults.yearFormat(pattern);
        }
    }
}
