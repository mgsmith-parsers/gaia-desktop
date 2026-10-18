package tools.pantheum.gaia.desktop.parse;

import tools.pantheum.gaia.GaiaConstants;

import java.util.Locale;

/**
 * Display names for the parser's error-message languages, for the language picker.
 */
public final class Languages {

    private Languages() {}

    /**
     * The {@link GaiaConstants.Language} matching {@code locale}, or {@code null} when the
     * parser has no catalogue for it.
     *
     * <p>Matched on the ISO language code, with two refinements the plain code cannot carry:
     * {@code zh-HK}/{@code zh-MO} mean Cantonese rather than Simplified Chinese, and the
     * extended codes {@code yue}/{@code wuu}/{@code arz}/{@code pcm} are recognised when the
     * platform reports them.
     */
    public static GaiaConstants.Language fromLocale(Locale locale) {
        if (locale == null) return null;

        String code   = locale.getLanguage().toLowerCase(Locale.ROOT);
        String region = locale.getCountry().toUpperCase(Locale.ROOT);

        // Locale.getLanguage() still returns the withdrawn ISO codes for these three.
        if (code.equals("iw")) code = "he";
        if (code.equals("in")) code = "id";
        if (code.equals("ji")) code = "yi";

        if (code.equals("zh") && (region.equals("HK") || region.equals("MO"))) {
            return GaiaConstants.Language.CANTONESE;
        }

        switch (code) {
            case "en":  return GaiaConstants.Language.ENGLISH;
            case "fr":  return GaiaConstants.Language.FRENCH;
            case "es":  return GaiaConstants.Language.SPANISH;
            case "de":  return GaiaConstants.Language.GERMAN;
            case "it":  return GaiaConstants.Language.ITALIAN;
            case "pt":  return GaiaConstants.Language.PORTUGUESE;
            case "nl":  return GaiaConstants.Language.DUTCH;
            case "pl":  return GaiaConstants.Language.POLISH;
            case "ru":  return GaiaConstants.Language.RUSSIAN;
            case "uk":  return GaiaConstants.Language.UKRAINIAN;
            case "cs":  return GaiaConstants.Language.CZECH;
            case "sv":  return GaiaConstants.Language.SWEDISH;
            case "zh":  return GaiaConstants.Language.CHINESE;
            case "ja":  return GaiaConstants.Language.JAPANESE;
            case "ko":  return GaiaConstants.Language.KOREAN;
            case "ar":  return region.equals("EG")
                    ? GaiaConstants.Language.EGYPTIAN_ARABIC : GaiaConstants.Language.ARABIC;
            case "arz": return GaiaConstants.Language.EGYPTIAN_ARABIC;
            case "id":  return GaiaConstants.Language.INDONESIAN;
            case "hi":  return GaiaConstants.Language.HINDI;
            case "tr":  return GaiaConstants.Language.TURKISH;
            case "bn":  return GaiaConstants.Language.BENGALI;
            case "ur":  return GaiaConstants.Language.URDU;
            case "vi":  return GaiaConstants.Language.VIETNAMESE;
            case "pcm": return GaiaConstants.Language.NIGERIAN_PIDGIN;
            case "mr":  return GaiaConstants.Language.MARATHI;
            case "te":  return GaiaConstants.Language.TELUGU;
            case "ta":  return GaiaConstants.Language.TAMIL;
            case "yue": return GaiaConstants.Language.CANTONESE;
            case "wuu": return GaiaConstants.Language.WU_CHINESE;
            case "tl":  case "fil": return GaiaConstants.Language.TAGALOG;
            case "fa":  return GaiaConstants.Language.PERSIAN;
            case "ha":  return GaiaConstants.Language.HAUSA;
            case "pa":  return GaiaConstants.Language.PUNJABI;
            case "jv":  return GaiaConstants.Language.JAVANESE;
            case "sw":  return GaiaConstants.Language.SWAHILI;
            default:    return null;
        }
    }

    /**
     * The language named on the command line by {@code --language=…}, or {@code null} when the
     * value names nothing this build carries.
     *
     * <p>Deliberately generous about the form, because the three obvious things to type are all
     * reasonable: the catalogue code ({@code FR}, {@code PCM}, {@code ARZ}), the enum constant
     * ({@code FRENCH}, {@code NIGERIAN_PIDGIN}, hyphens accepted for underscores), or a BCP 47
     * tag ({@code fr}, {@code zh-HK}). Matching is case-insensitive throughout.
     *
     * <p>Returns {@code null} rather than throwing: a typo on a launcher shortcut should leave
     * the app running in the saved language, not refuse to start.
     */
    public static GaiaConstants.Language fromArgument(String value) {
        if (value == null) return null;
        String v = value.trim();
        if (v.isEmpty()) return null;

        for (GaiaConstants.Language lang : GaiaConstants.Language.values()) {
            if (code(lang).equalsIgnoreCase(v)) return lang;
        }
        String constant = v.replace('-', '_').toUpperCase(Locale.ROOT);
        for (GaiaConstants.Language lang : GaiaConstants.Language.values()) {
            if (lang.name().equals(constant)) return lang;
        }
        for (GaiaConstants.Language lang : GaiaConstants.Language.values()) {
            if (tag(lang).equalsIgnoreCase(v)) return lang;
        }
        // Last: a full locale tag, so pt-BR and zh-HK land where fromLocale would put them.
        return fromLocale(Locale.forLanguageTag(v));
    }

    /** Every catalogue code {@link #fromArgument} accepts, for a "did you mean" message. */
    public static String acceptedCodes() {
        StringBuilder b = new StringBuilder();
        for (GaiaConstants.Language lang : GaiaConstants.Language.values()) {
            if (b.length() > 0) b.append(", ");
            b.append(code(lang));
        }
        return b.toString();
    }

    /**
     * Whether the language is written right to left, so the UI and the report need
     * right-to-left orientation.
     */
    public static boolean isRightToLeft(GaiaConstants.Language lang) {
        switch (lang) {
            case ARABIC:
            case EGYPTIAN_ARABIC:
            case URDU:
            case PERSIAN:
                return true;
            default:
                return false;
        }
    }

    /** The BCP 47 tag for the report's {@code <html lang>} attribute, e.g. {@code "pt"}. */
    public static String tag(GaiaConstants.Language lang) {
        switch (lang) {
            case ENGLISH:         return "en";
            case FRENCH:          return "fr";
            case SPANISH:         return "es";
            case GERMAN:          return "de";
            case ITALIAN:         return "it";
            case PORTUGUESE:      return "pt";
            case DUTCH:           return "nl";
            case POLISH:          return "pl";
            case RUSSIAN:         return "ru";
            case UKRAINIAN:       return "uk";
            case CZECH:           return "cs";
            case SWEDISH:         return "sv";
            case CHINESE:         return "zh-Hans";
            case JAPANESE:        return "ja";
            case KOREAN:          return "ko";
            case ARABIC:          return "ar";
            case INDONESIAN:      return "id";
            case HINDI:           return "hi";
            case TURKISH:         return "tr";
            case BENGALI:         return "bn";
            case URDU:            return "ur";
            case VIETNAMESE:      return "vi";
            case NIGERIAN_PIDGIN: return "pcm";
            case EGYPTIAN_ARABIC: return "arz";
            case MARATHI:         return "mr";
            case TELUGU:          return "te";
            case TAMIL:           return "ta";
            case CANTONESE:       return "yue";
            case WU_CHINESE:      return "wuu";
            case TAGALOG:         return "tl";
            case PERSIAN:         return "fa";
            case HAUSA:           return "ha";
            case PUNJABI:         return "pa";
            case JAVANESE:        return "jv";
            case SWAHILI:         return "sw";
            default:              return "en";
        }
    }

    /** The catalogue code used for resource files, matching the core's {@code localization/} dirs. */
    public static String code(GaiaConstants.Language lang) {
        switch (lang) {
            case ENGLISH:         return "EN";
            case FRENCH:          return "FR";
            case SPANISH:         return "ES";
            case GERMAN:          return "DE";
            case ITALIAN:         return "IT";
            case PORTUGUESE:      return "PT";
            case DUTCH:           return "NL";
            case POLISH:          return "PL";
            case RUSSIAN:         return "RU";
            case UKRAINIAN:       return "UK";
            case CZECH:           return "CS";
            case SWEDISH:         return "SV";
            case CHINESE:         return "ZH";
            case JAPANESE:        return "JA";
            case KOREAN:          return "KO";
            case ARABIC:          return "AR";
            case INDONESIAN:      return "ID";
            case HINDI:           return "HI";
            case TURKISH:         return "TR";
            case BENGALI:         return "BN";
            case URDU:            return "UR";
            case VIETNAMESE:      return "VI";
            case NIGERIAN_PIDGIN: return "PCM";
            case EGYPTIAN_ARABIC: return "ARZ";
            case MARATHI:         return "MR";
            case TELUGU:          return "TE";
            case TAMIL:           return "TA";
            case CANTONESE:       return "YUE";
            case WU_CHINESE:      return "WUU";
            case TAGALOG:         return "TL";
            case PERSIAN:         return "FA";
            case HAUSA:           return "HA";
            case PUNJABI:         return "PA";
            case JAVANESE:        return "JV";
            case SWAHILI:         return "SW";
            default:              return "EN";
        }
    }

    /** The picker label, e.g. {@code "German — Deutsch"}. */
    public static String label(GaiaConstants.Language lang) {
        String english = english(lang);
        String native_ = nativeName(lang);
        return english.equals(native_) ? english : english + " — " + native_;
    }

    /** The language's English name, e.g. {@code "German"}. */
    public static String english(GaiaConstants.Language lang) {
        switch (lang) {
            case NIGERIAN_PIDGIN: return "Nigerian Pidgin";
            case EGYPTIAN_ARABIC: return "Egyptian Arabic";
            case WU_CHINESE:      return "Wu Chinese";
            case CHINESE:         return "Chinese (Simplified)";
            case TAGALOG:         return "Tagalog (Filipino)";
            case PERSIAN:         return "Persian (Farsi)";
            case CANTONESE:       return "Cantonese";
            default:
                // ENGLISH -> "English", PORTUGUESE -> "Portuguese", ...
                String n = lang.name().toLowerCase(Locale.ROOT);
                return Character.toUpperCase(n.charAt(0)) + n.substring(1);
        }
    }

    /** The language's native name, e.g. {@code "Deutsch"}. */
    public static String nativeName(GaiaConstants.Language lang) {
        switch (lang) {
            case FRENCH:          return "Français";
            case SPANISH:         return "Español";
            case GERMAN:          return "Deutsch";
            case ITALIAN:         return "Italiano";
            case PORTUGUESE:      return "Português";
            case DUTCH:           return "Nederlands";
            case POLISH:          return "Polski";
            case RUSSIAN:         return "Русский";
            case UKRAINIAN:       return "Українська";
            case CZECH:           return "Čeština";
            case SWEDISH:         return "Svenska";
            case CHINESE:         return "简体中文";
            case JAPANESE:        return "日本語";
            case KOREAN:          return "한국어";
            case ARABIC:          return "العربية";
            case INDONESIAN:      return "Bahasa Indonesia";
            case HINDI:           return "हिन्दी";
            case TURKISH:         return "Türkçe";
            case BENGALI:         return "বাংলা";
            case URDU:            return "اردو";
            case VIETNAMESE:      return "Tiếng Việt";
            case NIGERIAN_PIDGIN: return "Naijá";
            case EGYPTIAN_ARABIC: return "العربية المصرية";
            case MARATHI:         return "मराठी";
            case TELUGU:          return "తెలుగు";
            case TAMIL:           return "தமிழ்";
            case CANTONESE:       return "粵語";
            case WU_CHINESE:      return "吳語";
            case TAGALOG:         return "Tagalog";
            case PERSIAN:         return "فارسی";
            case HAUSA:           return "Hausa";
            case PUNJABI:         return "ਪੰਜਾਬੀ";
            case JAVANESE:        return "Basa Jawa";
            case SWAHILI:         return "Kiswahili";
            case ENGLISH:
            default:              return "English";
        }
    }
}
