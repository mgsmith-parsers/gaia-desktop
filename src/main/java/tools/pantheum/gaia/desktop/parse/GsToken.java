package tools.pantheum.gaia.desktop.parse;

/**
 * Translates between the FNC1 separator as the parser needs it — a raw {@code 0x1D} byte —
 * and the {@code {GS}} token the input field can actually hold.
 *
 * <h2>Why a token is needed at all</h2>
 *
 * <p>A JavaFX {@code TextField} silently discards every character below {@code 0x20}
 * ({@code TextInputControl.filterInput}). Not "renders it oddly" — removes it. Typing,
 * pasting, {@code setText} and {@code insertText} are all affected, so
 * {@code setText("01" + GS + "10LOT")} yields {@code "0110LOT"}: an element string whose
 * meaning has quietly changed, because the variable-length AI now swallows what followed it.
 *
 * <p>So the separator is carried through the UI as the literal text {@code {GS}} and turned
 * back into {@code 0x1D} on the way to the parser. The token is not invented here — it is
 * exactly what {@code ASCIIRevealerUtils.printable} renders {@code 0x1D} as, so the input bar
 * accepts what the report displays, and a value copied out of a report can be pasted straight
 * back in.
 *
 * <p>The scanner path never comes through here: scans go straight to the parser as bytes.
 * This is only for what a human types, pastes, or drags into the input field.
 */
public final class GsToken {

    private GsToken() {}

    /** ASCII 0x1D — FNC1, the separator between variable-length AIs. */
    public static final char GS_CHAR = 0x1D;

    /** {@link #GS_CHAR} as a string. */
    public static final String GS = String.valueOf(GS_CHAR);

    /** The printable stand-in shown in the input field, matching {@code printable()}'s notation. */
    public static final String TOKEN = "{GS}";

    /** Replaces every {@link #TOKEN} with a real separator, ready for the parser. */
    public static String toParserInput(String displayed) {
        return displayed == null ? null : displayed.replace(TOKEN, GS);
    }

    /** Replaces every real separator with {@link #TOKEN}, ready for the input field. */
    public static String toDisplay(String raw) {
        return raw == null ? null : raw.replace(GS, TOKEN);
    }

    /** Whether {@code text} carries a real separator that a text field would silently eat. */
    public static boolean containsSeparator(String text) {
        return text != null && text.indexOf(GS_CHAR) >= 0;
    }
}
