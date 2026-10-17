package tools.pantheum.gaia.desktop.parse;

import tools.pantheum.gaia.GaiaParser;
import tools.pantheum.gaia.gs1.util.ASCIIRevealerUtils;
import tools.pantheum.gaia.result.ParseResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Tests for {@link GsToken} — the {@code {GS}} stand-in a text field can actually hold. */
@DisplayName("GsToken")
class GsTokenTest {

    private static final GaiaParser PARSER = new GaiaParser();

    @Test
    @DisplayName("the token matches the notation the report already renders")
    void tokenMatchesPrintable() {
        // The input bar must accept exactly what a report displays, so a value can be copied
        // out of one and pasted into the other.
        assertEquals(GsToken.TOKEN, ASCIIRevealerUtils.printable(GsToken.GS));
    }

    @Test
    @DisplayName("display form converts to a real separator for the parser")
    void toParserInput() {
        assertEquals("0109506000134352" + GsToken.GS + "10LOT-001",
                GsToken.toParserInput("0109506000134352{GS}10LOT-001"));
        assertEquals("10A" + GsToken.GS + "21B" + GsToken.GS + "17271231",
                GsToken.toParserInput("10A{GS}21B{GS}17271231"));
    }

    @Test
    @DisplayName("a real separator converts to the display form")
    void toDisplay() {
        assertEquals("0109506000134352{GS}10LOT-001",
                GsToken.toDisplay("0109506000134352" + GsToken.GS + "10LOT-001"));
    }

    @Test
    @DisplayName("the two directions round-trip")
    void roundTrip() {
        String raw = "10LOT-A" + GsToken.GS + "21SER" + GsToken.GS + "17271231";
        assertEquals(raw, GsToken.toParserInput(GsToken.toDisplay(raw)));

        String displayed = "10LOT-A{GS}21SER";
        assertEquals(displayed, GsToken.toDisplay(GsToken.toParserInput(displayed)));
    }

    @Test
    @DisplayName("text without a separator is returned untouched")
    void noSeparator() {
        assertEquals("0109506000134352", GsToken.toParserInput("0109506000134352"));
        assertEquals("0109506000134352", GsToken.toDisplay("0109506000134352"));
        assertFalse(GsToken.containsSeparator("0109506000134352"));
        assertTrue(GsToken.containsSeparator("01" + GsToken.GS + "10"));
        assertFalse(GsToken.containsSeparator(null));
        assertNull(GsToken.toParserInput(null));
        assertNull(GsToken.toDisplay(null));
    }

    @Test
    @DisplayName("what the user types as {GS} parses as a real separator")
    void typedTokenParses() {
        // The whole point: an element string typed with the token must reach the parser as
        // the bytes it means, and a variable-length AI must stop at the separator.
        String typed = "10LOT-001{GS}0109506000134352";
        ParseResult resp = PARSER.parse(GsToken.toParserInput(typed));

        assertTrue(resp.isValid(), "the token must reach the parser as a real FNC1");
        assertEquals(2, resp.getAiObject().getAis().size());
        assertEquals("LOT-001", resp.getAiObject().getAis().get(0).getValue(),
                "AI 10 must stop at the separator rather than swallowing what follows");
    }

    @Test
    @DisplayName("without the separator the same characters parse as something else entirely")
    void withoutTheSeparator() {
        // Why the text field silently eating 0x1D matters: the input stays "valid-looking"
        // but means something different.
        ParseResult swallowed = PARSER.parse("10LOT-0010109506000134352");

        assertEquals(1, swallowed.getAiObject().getAis().size(),
                "with no separator AI 10 takes the rest of the string");
        assertEquals("LOT-0010109506000134352", swallowed.getAiObject().getAis().get(0).getValue());
    }
}
