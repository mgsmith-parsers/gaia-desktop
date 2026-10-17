package tools.pantheum.gaia.desktop.parse;

import tools.pantheum.gaia.GaiaConstants;

import java.time.chrono.IsoChronology;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.FormatStyle;
import java.util.Locale;

/**
 * Derives the first-launch parser options from the machine's own regional settings, so a
 * fresh install already renders dates and messages the way the rest of the desktop does.
 *
 * <p>Everything is read from {@link Locale#getDefault()}, which the JVM initialises from the
 * OS region/language settings. The date options come from the locale's <em>short</em> date
 * pattern — {@code "d/MM/yy"}, {@code "M/d/yy"}, {@code "dd.MM.yyyy"}, {@code "yyyy-MM-dd"}
 * and so on — which is the same pattern the OS uses for compact dates elsewhere:
 *
 * <ul>
 *   <li>the order of {@code d}, {@code M} and {@code y} gives the {@link GaiaConstants.DateEndian};</li>
 *   <li>the first {@code /}, {@code -} or {@code .} between components gives the
 *       {@link GaiaConstants.DateSeparator};</li>
 *   <li>{@code MMM} (rather than {@code M}/{@code MM}) gives a three-letter month;</li>
 *   <li>{@code yyyy} (rather than {@code yy}) gives a four-digit year.</li>
 * </ul>
 *
 * <p>Only ever consulted when there is no saved options file — after that the user's own
 * choices win, including choices that happen to match these.
 */
public final class SystemDefaults {

    private SystemDefaults() {}

    /** Applies the machine's regional settings to {@code options}. */
    public static void applyTo(ParseOptions options) {
        applyTo(options, Locale.getDefault());
    }

    /** Applies {@code locale}'s regional settings to {@code options}. Package-visible for testing. */
    static void applyTo(ParseOptions options, Locale locale) {
        options.languageProperty().set(language(locale));

        String pattern = shortDatePattern(locale);
        options.dateEndianProperty().set(endian(pattern));
        options.dateSeparatorProperty().set(separator(pattern));
        options.monthFormatProperty().set(monthFormat(pattern));
        options.yearFormatProperty().set(yearFormat(pattern));
    }

    /** The locale's short date pattern, e.g. {@code "dd/MM/yyyy"}; {@code "dd/MM/yyyy"} if unavailable. */
    static String shortDatePattern(Locale locale) {
        try {
            return DateTimeFormatterBuilder.getLocalizedDateTimePattern(
                    FormatStyle.SHORT, null, IsoChronology.INSTANCE, locale);
        } catch (RuntimeException e) {
            // A locale with no ISO short-date pattern: fall back to the GAIA default shape.
            return "dd/MM/yyyy";
        }
    }

    /** The error-message language for {@code locale}, defaulting to English when unsupported. */
    static GaiaConstants.Language language(Locale locale) {
        GaiaConstants.Language lang = Languages.fromLocale(locale);
        return lang != null ? lang : GaiaConstants.Language.ENGLISH;
    }

    /** LITTLE (d·M·y), MIDDLE (M·d·y), or BIG (y first), read from the component order. */
    static GaiaConstants.DateEndian endian(String pattern) {
        int day   = indexOfAny(pattern, "d");
        int month = indexOfAny(pattern, "ML");
        int year  = indexOfAny(pattern, "yu");

        if (day < 0 || month < 0 || year < 0) return GaiaConstants.DateEndian.LITTLE;
        if (year < month && year < day)       return GaiaConstants.DateEndian.BIG;
        if (month < day)                      return GaiaConstants.DateEndian.MIDDLE;
        return GaiaConstants.DateEndian.LITTLE;
    }

    /** The first {@code /}, {@code -} or {@code .} in the pattern; SLASH when it uses something else. */
    static GaiaConstants.DateSeparator separator(String pattern) {
        for (int i = 0; i < pattern.length(); i++) {
            switch (pattern.charAt(i)) {
                case '/': return GaiaConstants.DateSeparator.SLASH;
                case '-': return GaiaConstants.DateSeparator.HYPHEN;
                case '.': return GaiaConstants.DateSeparator.PERIOD;
                default:  break;
            }
        }
        // Locales that separate with a word or a space (e.g. some CJK forms) keep the default.
        return GaiaConstants.DateSeparator.SLASH;
    }

    /** THREE_LETTER when the pattern asks for an abbreviated month ({@code MMM}). */
    static GaiaConstants.MonthFormat monthFormat(String pattern) {
        return runLength(pattern, 'M') >= 3 || runLength(pattern, 'L') >= 3
                ? GaiaConstants.MonthFormat.THREE_LETTER
                : GaiaConstants.MonthFormat.TWO_DIGIT;
    }

    /** TWO_DIGIT when the pattern asks for a short year ({@code yy}). */
    static GaiaConstants.YearFormat yearFormat(String pattern) {
        int run = Math.max(runLength(pattern, 'y'), runLength(pattern, 'u'));
        return run > 0 && run < 4
                ? GaiaConstants.YearFormat.TWO_DIGIT
                : GaiaConstants.YearFormat.FOUR_DIGIT;
    }

    /** The index of the first occurrence of any character in {@code chars}, or -1. */
    private static int indexOfAny(String pattern, String chars) {
        for (int i = 0; i < pattern.length(); i++) {
            if (chars.indexOf(pattern.charAt(i)) >= 0) return i;
        }
        return -1;
    }

    /** The length of the first run of {@code c} in {@code pattern}, or 0 when absent. */
    private static int runLength(String pattern, char c) {
        int start = pattern.indexOf(c);
        if (start < 0) return 0;
        int end = start;
        while (end < pattern.length() && pattern.charAt(end) == c) end++;
        return end - start;
    }
}
