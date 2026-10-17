package tools.pantheum.gaia.desktop.datamatrix;

import java.util.EnumMap;
import java.util.Map;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.ResultMetadataType;
import com.google.zxing.common.GlobalHistogramBinarizer;

import org.junit.jupiter.api.Test;

import tools.pantheum.gaia.GaiaConstants;
import tools.pantheum.gaia.GaiaParser;
import tools.pantheum.gaia.config.ParseConfig;
import tools.pantheum.gaia.result.ParseResult;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Proves the encoder by reading its symbols back with a decoder it shares no code with.
 *
 * <p>Two things are checked of every symbol, and the second is the one that matters: that the data
 * survives, and that the symbology identifier is <b>{@code ]d2}</b> — a GS1 Data Matrix. A symbol
 * that carries the right characters but announces itself as plain Data Matrix ({@code ]d1}) is the
 * exact defect that rules ZXing's own writer out of this job, and it is invisible to the eye.
 *
 * <p>Then the decoded string goes through the core parser, which is the real question a user has:
 * does the symbol this report draws parse back to the AIs the report validated?
 */
class DataMatrixEncoderTest {

    private static final String GS = "\u001D";

    /** The worked example from the combiner's tests: an SSCC, contents, and dates. */
    private static final String LABEL =
            "00093123450000000012" + "0209312345000012" + "3724" + GS + "15261231" + "10LOT1";

    // ---------------------------------------------------------------- round trips

    @Test
    void aCombinedLabelDecodesBackToItselfAsAGs1DataMatrix() {
        assertEquals(LABEL, payloadOf(DataMatrixEncoder.encode(LABEL)));
    }

    @Test
    void theDecodedSymbolParsesBackToTheAisItWasBuiltFrom() {
        String decoded = payloadOf(DataMatrixEncoder.encode(LABEL));

        ParseResult result = new GaiaParser().parse("]d2" + decoded, ParseConfig.builder()
                .requestedParseMode(GaiaConstants.ParseMode.INTERPRETATION)
                .language(GaiaConstants.Language.ENGLISH)
                .build());

        assertTrue(result.isValid(), () -> result.getIssues().toString());
        assertEquals("(00)093123450000000012 (02)09312345000012 (37)24 (15)261231 (10)LOT1",
                result.getAiObject().toHriString());
    }

    @Test
    void everySeparatorSurvivesAndNotAsALiteralCharacter() {
        // Three variable-length AIs in a row: three FNC1 codewords, none of them at the end.
        String many = "0109312345000012" + "10LOT1" + GS + "21SERIAL9" + GS + "240VARIANT" + GS + "15261231";

        String decoded = payloadOf(DataMatrixEncoder.encode(many));

        assertEquals(many, decoded);
        assertEquals(3, decoded.split(GS, -1).length - 1, "all three separators present");
    }

    @Test
    void anOddNumberOfDigitsAndAnAlphanumericValueBothSurvive() {
        // Digit pairs are one codeword each; an odd tail and letters fall back to single codewords,
        // which is where an off-by-one in the encodation would show.
        String odd = "0109312345000012" + "10ABC12345" + GS + "3100000123";

        assertEquals(odd, payloadOf(DataMatrixEncoder.encode(odd)));
    }

    @Test
    void aLongElementStringUsesABiggerSymbolAndStillDecodes() {
        StringBuilder bigger = new StringBuilder("00093123450000000012");
        for (int i = 0; i < 6; i++) {
            bigger.append("10LOT").append(i).append("ABCDEFGHIJ").append(GS);
        }
        bigger.append("15261231");
        String data = bigger.toString();

        boolean[][] modules = DataMatrixEncoder.encode(data);

        assertTrue(modules.length > 22, "this should not fit the 22x22 symbol: " + modules.length);
        assertEquals(data, payloadOf(modules));
    }

    // ---------------------------------------------------------------- symbol choice and shape

    @Test
    void theSmallestSquareSymbolThatFitsIsChosen() {
        SymbolVersion version = DataMatrixEncoder.versionFor(LABEL);

        assertEquals(version.getRows(), version.getColumns(), "square only");
        boolean[][] modules = DataMatrixEncoder.encode(LABEL);
        assertEquals(version.getRows(), modules.length);
        assertEquals(version.getColumns(), modules[0].length);

        // One codeword smaller and it would not fit: that is what "smallest that fits" means.
        int needed = DataMatrixEncoder.toCodewords(LABEL).length;
        assertTrue(version.getDataCodewords() >= needed);
        for (SymbolVersion other : SymbolVersion.all()) {
            if (other.getRows() == other.getColumns() && other.getDataCodewords() >= needed) {
                assertTrue(version.getDataCodewords() <= other.getDataCodewords());
            }
        }
    }

    @Test
    void theAlignmentPatternsAreDrawn() {
        boolean[][] m = DataMatrixEncoder.encode(LABEL);
        int rows = m.length;
        int columns = m[0].length;

        for (int row = 0; row < rows; row++) {
            assertTrue(m[row][0], "the solid L runs down the left edge, row " + row);
        }
        for (int col = 0; col < columns; col++) {
            assertTrue(m[rows - 1][col], "the solid L runs along the bottom edge, col " + col);
        }
        for (int col = 0; col < columns; col++) {
            assertEquals(col % 2 == 0, m[0][col], "the clock track alternates along the top, col " + col);
        }
    }

    @Test
    void anElementStringTooLongForTheLargestSymbolIsRefusedClearly() {
        StringBuilder huge = new StringBuilder("0109312345000012");
        for (int i = 0; i < 400; i++) huge.append("10LOTABCDEFGHIJKLMNOP").append(GS);

        assertThrows(DataMatrixEncoder.TooLongException.class,
                () -> DataMatrixEncoder.encode(huge.toString()));
    }

    @Test
    void aCharacterAsciiEncodationCannotCarryIsRefusedRatherThanMangled() {
        assertThrows(IllegalArgumentException.class, () -> DataMatrixEncoder.encode("10CAFÉ"));
    }

    // ---------------------------------------------------------------- the pieces

    @Test
    void theLeadingCodewordIsFnc1AndDigitPairsPackIntoOne() {
        int[] codewords = DataMatrixEncoder.toCodewords("00" + "12" + GS + "9");

        // 232 (the GS1 flag), "00" -> 130, "12" -> 142, the separator -> 232, "9" -> '9' + 1.
        assertArrayEquals(new int[] {232, 130, 142, 232, '9' + 1}, codewords);
    }

    @Test
    void paddingIsOneUnrandomisedCodewordThenTheRandomisedSequence() {
        int[] padded = DataMatrixEncoder.padTo(new int[] {232, 130}, 6);

        assertEquals(129, padded[2], "the first pad is plain 129");
        for (int i = 3; i < padded.length; i++) {
            assertTrue(padded[i] >= 1 && padded[i] <= 254, "pad in range at " + i);
            assertTrue(padded[i] != 129 || i == 2, "later pads are randomised, not a run of 129");
        }
    }

    @Test
    void theCheckCodewordCountAlwaysMatchesTheSymbol() {
        for (SymbolVersion version : SymbolVersion.all()) {
            if (version.getRows() != version.getColumns()) continue;

            int ecc = version.getEccCodewordsPerBlock();
            int[] check = DataMatrixEncoder.checkCodewords(new int[] {232, 130, 142}, ecc);

            assertEquals(ecc, check.length, version.getRows() + "x" + version.getColumns());
        }
    }

    @Test
    void theRenderedSvgIsSelfContainedAndFetchesNothing() {
        String svg = DataMatrixSvg.render(DataMatrixEncoder.encode(LABEL), 4, "GS1 Data Matrix");

        assertTrue(svg.startsWith("<svg "), svg.substring(0, Math.min(40, svg.length())));
        assertTrue(svg.endsWith("</svg>"));
        assertTrue(svg.contains("<rect"), "the modules are drawn");

        // The SVG namespace is a URI but not a fetch; everything that would actually go out is.
        String withoutNamespace = svg.replace("http://www.w3.org/2000/svg", "");
        for (String outbound : new String[] {"http", "<image", "href", "url(", "<use"}) {
            assertTrue(withoutNamespace.indexOf(outbound) < 0,
                    "the SVG must not reference " + outbound + ", which would leave the machine");
        }
    }

    // ---------------------------------------------------------------- harness

    /**
     * The element string a symbol carries, as decoded.
     *
     * <p>Asserts the symbology identifier is {@code ]d2} on the way past, and strips one leading
     * {@code 0x1D}: ZXing renders <em>every</em> FNC1 codeword as char 29, the leading GS1 flag
     * included, so its text is one character longer than the element string encoded. The flag is
     * what {@code ]d2} is reporting, so it is not part of the payload.
     */
    private static String payloadOf(boolean[][] modules) {
        Result decoded = decode(modules);

        assertEquals("]d2", symbologyOf(decoded), "the symbol must announce itself as GS1, not plain");
        assertTrue(decoded.getText().startsWith(GS), "the leading FNC1 flag should decode as 0x1D");

        return decoded.getText().substring(1);
    }

    private static Result decode(boolean[][] modules) {
        try {
            Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
            hints.put(DecodeHintType.PURE_BARCODE, Boolean.TRUE);
            hints.put(DecodeHintType.POSSIBLE_FORMATS, java.util.List.of(BarcodeFormat.DATA_MATRIX));

            return new MultiFormatReader().decode(
                    new BinaryBitmap(new GlobalHistogramBinarizer(new ModuleSource(modules))), hints);
        } catch (Exception e) {
            throw new AssertionError("the symbol would not decode: " + e, e);
        }
    }

    private static String symbologyOf(Result result) {
        Object id = result.getResultMetadata() == null
                ? null
                : result.getResultMetadata().get(ResultMetadataType.SYMBOLOGY_IDENTIFIER);
        return id == null ? null : id.toString();
    }

    /** The module grid as a greyscale image, one module to one pixel, plus the quiet zone. */
    private static final class ModuleSource extends LuminanceSource {

        private static final int QUIET = 2;

        private final boolean[][] modules;

        ModuleSource(boolean[][] modules) {
            super(modules[0].length + QUIET * 2, modules.length + QUIET * 2);
            this.modules = modules;
        }

        private byte luminance(int x, int y) {
            int row = y - QUIET;
            int col = x - QUIET;
            boolean dark = row >= 0 && row < modules.length && col >= 0 && col < modules[0].length
                    && modules[row][col];
            return (byte) (dark ? 0 : 0xFF);
        }

        @Override
        public byte[] getRow(int y, byte[] row) {
            if (row == null || row.length < getWidth()) row = new byte[getWidth()];
            for (int x = 0; x < getWidth(); x++) row[x] = luminance(x, y);
            return row;
        }

        @Override
        public byte[] getMatrix() {
            byte[] matrix = new byte[getWidth() * getHeight()];
            for (int y = 0; y < getHeight(); y++) {
                for (int x = 0; x < getWidth(); x++) matrix[y * getWidth() + x] = luminance(x, y);
            }
            return matrix;
        }
    }
}
