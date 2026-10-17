package tools.pantheum.gaia.desktop.build;

import tools.pantheum.gaia.GaiaBuilder;
import tools.pantheum.gaia.GaiaConstants;
import tools.pantheum.gaia.config.BuilderDigitalLinkConfig;
import tools.pantheum.gaia.error.GaiaError;
import tools.pantheum.gaia.gs1.localization.AiDescriptionRegistry;
import tools.pantheum.gaia.gs1.registry.AiDefinition;
import tools.pantheum.gaia.gs1.registry.AiDefinitionRegistry;
import tools.pantheum.gaia.result.BuildResult;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * The build half of the app, kept free of JavaFX so it can be tested without a toolkit:
 * turning a list of AI/value pairs into an element string or a Digital Link URI, and
 * answering the questions the editor asks about an AI as it is typed.
 *
 * <p>Everything here delegates to {@link GaiaBuilder} and the core's
 * {@link AiDefinitionRegistry}. Nothing about GS1 is re-implemented, and in particular the
 * AI list is not copied — {@link #catalogue()} reads the core's own registry, so the picker
 * cannot drift from what the builder will accept.
 *
 * <p>{@code GaiaBuilder} validates by parsing its own candidate output, so a successful
 * build is guaranteed to parse cleanly: the desktop app feeds the result straight back in as
 * an ordinary input rather than rendering it a second, separate way.
 */
public final class ElementBuilder {

    private ElementBuilder() {}

    private static final AiDefinitionRegistry AIS = AiDefinitionRegistry.getInstance();

    // ------------------------------------------------------------------ AI lookups

    /** Whether the core recognises {@code ai} — the same check {@code GaiaBuilder.ai} makes. */
    public static boolean isKnownAi(String ai) {
        return ai != null && !ai.isEmpty() && AIS.isKnown(ai);
    }

    /** The AI's data title, e.g. {@code "GTIN"}; empty when the AI is not recognised. */
    public static String titleOf(String ai) {
        return definition(ai).map(AiDefinition::getTitle).orElse("");
    }

    /**
     * The AI's description in {@code language}, e.g. {@code "Global Trade Item Number (GTIN)"};
     * empty when the AI is not recognised.
     *
     * <p>Resolved exactly the way the core's own parser resolves it: the localized catalogue
     * first, falling back to the AI definition's English text — {@code descriptionFor}
     * returns {@code null} both for English and for an AI a catalogue does not carry, and its
     * contract is that {@code null} means "use the definition's description".
     */
    public static String descriptionOf(String ai, GaiaConstants.Language language) {
        if (!isKnownAi(ai)) return "";
        String localized = AiDescriptionRegistry.INSTANCE.descriptionFor(ai, language);
        return localized != null ? localized : definition(ai).map(AiDefinition::getDescription).orElse("");
    }

    /** The AI's format string, e.g. {@code "N2+N14"}; empty when the AI is not recognised. */
    public static String formatOf(String ai) {
        return definition(ai).map(AiDefinition::getFormatString).orElse("");
    }

    private static Optional<AiDefinition> definition(String ai) {
        return ai == null || ai.isEmpty() ? Optional.empty() : AIS.find(ai);
    }

    /** Every AI the core knows, as {@code (01) GTIN} entries ordered by AI code, for the picker. */
    public static List<String> catalogue() {
        List<String> labels = new ArrayList<>(AIS.size());
        for (AiDefinition d : AIS.all()) {
            labels.add("(" + d.getApplicationIdentifier() + ") " + d.getTitle());
        }
        labels.sort(Comparator.naturalOrder());
        return labels;
    }

    /** The AI code out of a {@link #catalogue()} entry, e.g. {@code "(01) GTIN"} → {@code "01"}. */
    public static String aiFromLabel(String label) {
        if (label == null) return "";
        int close = label.indexOf(')');
        return label.startsWith("(") && close > 1 ? label.substring(1, close) : label.trim();
    }

    // ------------------------------------------------------------------ building

    /**
     * Assembles {@code pairs} into a GS1 element string.
     *
     * @param pairs    AI/value pairs in the order they should appear
     * @param language the language for any error messages
     * @return the build outcome — never throws for bad input; a rejected build comes back as
     *         {@link BuildResult#isSuccess()} {@code false} with the errors attached
     */
    public static BuildResult buildElementString(List<String[]> pairs, GaiaConstants.Language language) {
        BuildResult rejected = rejectUnknownAis(pairs);
        return rejected != null ? rejected : builderFor(pairs, language).tryBuildElementString();
    }

    /**
     * Assembles {@code pairs} into a GS1 Digital Link URI.
     *
     * @param domain     the resolver domain, e.g. {@code "example.com"}; blank uses the
     *                   canonical {@code id.gs1.org}
     * @param pathPrefix an optional path segment before the AI path, e.g. {@code "/products"}
     */
    public static BuildResult buildDigitalLink(List<String[]> pairs, GaiaConstants.Language language,
                                               String domain, String pathPrefix) {
        BuildResult rejected = rejectUnknownAis(pairs);
        if (rejected != null) return rejected;

        GaiaBuilder builder = builderFor(pairs, language);
        boolean customDomain = domain != null && !domain.trim().isEmpty();
        boolean customPrefix = pathPrefix != null && !pathPrefix.trim().isEmpty();
        if (!customDomain && !customPrefix) {
            return builder.tryBuildDigitalLinkUri();
        }

        BuilderDigitalLinkConfig.Builder config = BuilderDigitalLinkConfig.builder();
        if (customDomain) config.domain(domain.trim());
        if (customPrefix) config.pathPrefix(pathPrefix.trim());
        return builder.tryBuildDigitalLinkUri(config.build());
    }

    /**
     * A failed build as the editor should show it: one line per error, each carrying the
     * error id, the AI it belongs to, and — the part that matters — the parser's own
     * localized explanation.
     *
     * <p>The {@link BuildResult}'s own message is only a count ("1 validation error(s)"),
     * which is why showing that alone told the user nothing they could act on. It is used
     * only when there are no errors to list, as with an unrecognised AI.
     */
    public static String describeFailure(BuildResult result) {
        List<String> lines = new ArrayList<>();
        for (GaiaError error : result.getErrors()) {
            StringBuilder line = new StringBuilder(error.getId());
            if (error.getAi() != null && !error.getAi().isEmpty()) {
                line.append("  (").append(error.getAi()).append(')');
            }
            if (error.getMessage() != null && !error.getMessage().isEmpty()) {
                line.append("  ").append(error.getMessage());
            }
            String text = line.toString();
            if (!lines.contains(text)) lines.add(text);   // the same error can arrive twice
        }
        if (lines.isEmpty() && result.getMessage() != null) lines.add(result.getMessage());
        return String.join("\n", lines);
    }

    /**
     * {@code GaiaBuilder.ai} throws on an unrecognised AI, which would surface as a stack
     * trace rather than a build failure — so unknown AIs are turned into an ordinary failed
     * {@link BuildResult} first, the same shape every other rejection has.
     *
     * <p>This message is assembled without a catalogue and so is English-only. That is
     * deliberate: it is a backstop for callers outside the UI. The editor checks for unknown
     * AIs itself and reports them from the catalogue, so the untranslated text here is not
     * what a user sees.
     */
    private static BuildResult rejectUnknownAis(List<String[]> pairs) {
        List<String> unknown = new ArrayList<>();
        for (String[] pair : pairs) {
            if (!isKnownAi(pair[0])) unknown.add(pair[0]);
        }
        if (unknown.isEmpty()) return null;
        return BuildResult.failure("Unrecognised Application Identifier: " + String.join(", ", unknown),
                List.of());
    }

    private static GaiaBuilder builderFor(List<String[]> pairs, GaiaConstants.Language language) {
        GaiaBuilder builder = GaiaBuilder.create().language(language);
        for (String[] pair : pairs) {
            builder.ai(pair[0], pair[1]);
        }
        return builder;
    }
}
