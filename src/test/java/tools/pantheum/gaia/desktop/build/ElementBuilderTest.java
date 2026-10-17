package tools.pantheum.gaia.desktop.build;

import tools.pantheum.gaia.GaiaConstants;
import tools.pantheum.gaia.GaiaParser;
import tools.pantheum.gaia.result.BuildResult;
import tools.pantheum.gaia.result.ParseResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Tests for {@link ElementBuilder} — the build half, free of JavaFX. */
@DisplayName("ElementBuilder")
class ElementBuilderTest {

    private static final GaiaParser PARSER = new GaiaParser();
    private static final GaiaConstants.Language EN = GaiaConstants.Language.ENGLISH;
    private static final String GS = String.valueOf((char) 0x1D);

    private static List<String[]> pairs(String... aiThenValue) {
        List<String[]> out = new java.util.ArrayList<>();
        for (int i = 0; i < aiThenValue.length; i += 2) {
            out.add(new String[] {aiThenValue[i], aiThenValue[i + 1]});
        }
        return out;
    }

    @Test
    @DisplayName("builds an element string that parses back to the same AIs")
    void buildsElementString() {
        BuildResult result = ElementBuilder.buildElementString(
                pairs("01", "09506000134352", "17", "261231", "10", "LOT-001"), EN);

        assertTrue(result.isSuccess(), result.getMessage());
        assertEquals("01095060001343521726123110LOT-001", result.getValue());

        // The builder validates by parsing its own output, so this must hold by construction.
        ParseResult back = PARSER.parse(result.getValue());
        assertTrue(back.isValid(), "a built string must parse cleanly");
        assertEquals(3, back.getAiObject().getAis().size());
    }

    @Test
    @DisplayName("a variable-length AI that is not last gets its FNC1 separator")
    void insertsSeparators() {
        BuildResult result = ElementBuilder.buildElementString(
                pairs("10", "LOT-001", "01", "09506000134352"), EN);

        assertTrue(result.isSuccess(), result.getMessage());
        assertTrue(result.getValue().contains(GS),
                "AI 10 is variable-length and not last, so a separator must follow it");
        assertTrue(PARSER.parse(result.getValue()).isValid());
    }

    @Test
    @DisplayName("builds a Digital Link on the canonical domain by default")
    void buildsCanonicalDigitalLink() {
        BuildResult result = ElementBuilder.buildDigitalLink(
                pairs("01", "09506000134352", "10", "LOT-ABC"), EN, "", "");

        assertTrue(result.isSuccess(), result.getMessage());
        assertTrue(result.getValue().startsWith("https://id.gs1.org/01/09506000134352"),
                "got: " + result.getValue());
        assertTrue(PARSER.parse(result.getValue()).getAiObject().hasDigitalLink());
    }

    @Test
    @DisplayName("a custom domain and path prefix are honoured")
    void buildsCustomDigitalLink() {
        BuildResult result = ElementBuilder.buildDigitalLink(
                pairs("01", "09506000134352"), EN, "example.com", "/products");

        assertTrue(result.isSuccess(), result.getMessage());
        assertTrue(result.getValue().contains("example.com"), "got: " + result.getValue());
        assertTrue(result.getValue().contains("/products/01/09506000134352"), "got: " + result.getValue());
    }

    @Test
    @DisplayName("an invalid value comes back as a failure, not an exception")
    void invalidValueFails() {
        // A GTIN with a bad check digit: well-formed input, rejected content.
        BuildResult result = ElementBuilder.buildElementString(pairs("01", "09506000134350"), EN);

        assertFalse(result.isSuccess());
        assertFalse(result.getErrors().isEmpty(), "the rejection must carry its errors");
    }

    @Test
    @DisplayName("an unrecognised AI is a failure, not an IllegalArgumentException")
    void unknownAiFails() {
        // GaiaBuilder.ai() throws for an unknown AI; the editor must see an ordinary
        // BuildResult instead of a stack trace.
        BuildResult result = ElementBuilder.buildElementString(pairs("9999", "x"), EN);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("9999"), "got: " + result.getMessage());
    }

    @Test
    @DisplayName("error messages follow the chosen language")
    void errorsAreLocalized() {
        BuildResult en = ElementBuilder.buildElementString(pairs("01", "09506000134350"), EN);
        BuildResult fr = ElementBuilder.buildElementString(pairs("01", "09506000134350"),
                GaiaConstants.Language.FRENCH);

        assertFalse(en.isSuccess());
        assertFalse(fr.isSuccess());
        assertFalse(en.getErrors().isEmpty());
        assertNotEqualsIgnoringNull(en.getErrors().get(0).getMessage(),
                fr.getErrors().get(0).getMessage());
    }

    @Test
    @DisplayName("a failure is described with the parser's message, not just the error id")
    void failureDescription() {
        BuildResult result = ElementBuilder.buildElementString(pairs("01", "09506000134350"), EN);
        String described = ElementBuilder.describeFailure(result);

        assertTrue(described.contains("GE-C003"), "the id is still useful: " + described);
        assertTrue(described.contains("(01)"), "the AI it belongs to must be named: " + described);
        assertTrue(described.contains("Check digit"),
                "the explanation is the part the user can act on: " + described);
        assertFalse(described.contains("validation error(s)"),
                "the BuildResult's own message is only a count, and is redundant once the "
                        + "errors are listed: " + described);
    }

    @Test
    @DisplayName("the failure description is localized with everything else")
    void failureDescriptionIsLocalized() {
        BuildResult fr = ElementBuilder.buildElementString(pairs("01", "09506000134350"),
                GaiaConstants.Language.FRENCH);
        String described = ElementBuilder.describeFailure(fr);

        assertTrue(described.contains("GE-C003"));
        assertTrue(described.contains("chiffre de contrôle"), "got: " + described);
    }

    @Test
    @DisplayName("one line per error, with duplicates collapsed")
    void failureDescriptionListsEachError() {
        // Two AIs that each fail their own requires-check.
        BuildResult result = ElementBuilder.buildElementString(pairs("10", "LOT-A", "21", "SER-B"), EN);
        assertFalse(result.isSuccess());

        String described = ElementBuilder.describeFailure(result);
        long lines = described.lines().count();
        assertEquals(result.getErrors().stream().map(e -> e.getId() + e.getAi() + e.getMessage())
                        .distinct().count(), lines,
                "expected one line per distinct error, got:\n" + described);
    }

    @Test
    @DisplayName("a failure with no errors still says something")
    void failureWithoutErrors() {
        // An unrecognised AI never reaches the parser, so it carries a message and no errors.
        String described = ElementBuilder.describeFailure(
                ElementBuilder.buildElementString(pairs("9999", "x"), EN));
        assertTrue(described.contains("9999"), "got: " + described);
    }

    private static void assertNotEqualsIgnoringNull(String a, String b) {
        assertTrue(a != null && b != null && !a.equals(b),
                "expected different messages, got '" + a + "' and '" + b + "'");
    }

    // ------------------------------------------------------------------ AI lookups

    @Test
    @DisplayName("AI lookups come from the core registry, not a copied list")
    void aiLookups() {
        assertTrue(ElementBuilder.isKnownAi("01"));
        assertTrue(ElementBuilder.isKnownAi("3103"));
        assertFalse(ElementBuilder.isKnownAi("9999"));
        assertFalse(ElementBuilder.isKnownAi(""));
        assertFalse(ElementBuilder.isKnownAi(null));

        assertEquals("GTIN", ElementBuilder.titleOf("01"));
        assertEquals("", ElementBuilder.titleOf("9999"));
        assertFalse(ElementBuilder.formatOf("01").isEmpty());
    }

    @Test
    @DisplayName("the AI description is localized, falling back to the definition's English")
    void descriptions() {
        String en = ElementBuilder.descriptionOf("01", EN);
        assertFalse(en.isEmpty(), "English must come from the AI definition");
        assertTrue(en.contains("Global Trade Item Number"), "got: " + en);

        // descriptionFor() returns null for English and for an AI a catalogue omits; the
        // fallback to the definition is what keeps the column populated either way.
        String fr = ElementBuilder.descriptionOf("01", GaiaConstants.Language.FRENCH);
        assertFalse(fr.isEmpty());
        assertNotEqualsIgnoringNull(en, fr);

        assertEquals("", ElementBuilder.descriptionOf("9999", EN), "an unknown AI has no description");
        assertEquals("", ElementBuilder.descriptionOf("", EN));
        assertEquals("", ElementBuilder.descriptionOf(null, EN));
    }

    @Test
    @DisplayName("every AI in the picker has a description to show")
    void everyAiHasADescription() {
        int missing = 0;
        for (String label : ElementBuilder.catalogue()) {
            if (ElementBuilder.descriptionOf(ElementBuilder.aiFromLabel(label), EN).isEmpty()) missing++;
        }
        assertEquals(0, missing, missing + " AI(s) would show an empty description column");
    }

    @Test
    @DisplayName("the picker catalogue covers every AI the builder accepts")
    void catalogueMatchesTheRegistry() {
        List<String> catalogue = ElementBuilder.catalogue();

        assertTrue(catalogue.size() > 500, "expected the full AI table, got " + catalogue.size());
        for (String label : catalogue) {
            String ai = ElementBuilder.aiFromLabel(label);
            assertTrue(ElementBuilder.isKnownAi(ai),
                    "the picker offers '" + label + "' but the builder would reject AI '" + ai + "'");
        }
        assertTrue(catalogue.contains("(01) GTIN"), "got e.g. " + catalogue.get(0));
    }

    @Test
    @DisplayName("blank rows never reach the builder")
    void aiPairEmptiness() {
        assertTrue(new AiPair().isEmpty());
        assertTrue(new AiPair("  ", "").isEmpty());
        assertFalse(new AiPair("01", "").isEmpty());
        assertFalse(new AiPair("", "value").isEmpty());
    }

    @Test
    @DisplayName("a row describes itself from its AI code as it is typed")
    void aiPairDerivesItsLabels() {
        AiPair row = new AiPair();
        assertFalse(row.isKnown());
        assertEquals("", row.titleProperty().get());

        row.aiProperty().set("01");
        assertTrue(row.isKnown());
        assertEquals("GTIN", row.titleProperty().get());
        assertFalse(row.formatProperty().get().isEmpty());

        row.aiProperty().set("9999");
        assertFalse(row.isKnown(), "the derived state must follow the code back down again");
        assertEquals("", row.titleProperty().get());
    }
}
