package tools.pantheum.gaia.desktop.datamatrix;

import java.util.List;

import tools.pantheum.gaia.gs1.constants.GS1Constants;

/**
 * Encodes a GS1 element string as the module grid of a GS1 Data Matrix (ECC 200).
 *
 * <p>Written because no library in this project can do it. ZXing's {@code DataMatrixWriter} — the
 * obvious candidate, and already a {@code gaia-demo} dependency — writes the {@code 0x1D}
 * separator as a <em>literal character</em> rather than the FNC1 codeword, so its symbols decode
 * as plain Data Matrix ({@code ]d1}) and never as GS1 ({@code ]d2}): its high-level encoder has no
 * mention of codeword 232 at all. A symbol like that looks right, scans, and is wrong, which is
 * the one failure this application must not ship.
 *
 * <p>So the pipeline here is the standard one, in ASCII encodation only — which is all GS1 element
 * strings need, being digits and invariant-set characters:
 *
 * <ol>
 *   <li>FNC1 (codeword 232) in first position. That is what makes the symbol GS1, and what makes a
 *       scanner report {@code ]d2}.</li>
 *   <li>Digit pairs as one codeword each ({@code 130 + value}), every other character as
 *       {@code value + 1}, each FNC1 separator as 232 again.</li>
 *   <li>The smallest square symbol whose data capacity fits, padded with 129 and then the
 *       randomised 253 sequence.</li>
 *   <li>Reed-Solomon check codewords per block over GF(256) with the Data Matrix polynomial, then
 *       interleaved as ECC 200 wants them.</li>
 *   <li>The codewords walked into the mapping matrix by {@link BitPlacement}, and the alignment
 *       patterns drawn around each data region.</li>
 * </ol>
 *
 * <p>Verified by decoding the output again — see {@code DataMatrixEncoderTest}, which reads every
 * symbol back with ZXing (test scope only) and checks both that the data survives and that the
 * symbology identifier really is {@code ]d2}.
 *
 * <p>Rectangular symbols are not offered: a GS1 Data Matrix may be rectangular, but nothing here
 * needs one and every rectangular row in the table would widen the test surface for no gain.
 */
public final class DataMatrixEncoder {

    /** The FNC1 codeword: first position makes the symbol GS1, later positions separate AIs. */
    private static final int FNC1 = 232;

    /** The first pad codeword; the ones after it are randomised. */
    private static final int PAD = 129;

    private DataMatrixEncoder() {}

    /** Thrown when an element string will not fit the largest symbol in the table. */
    public static final class TooLongException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        TooLongException(int codewords, int capacity) {
            super("the element string needs " + codewords + " codewords; the largest square symbol holds "
                    + capacity);
        }
    }

    /**
     * Encodes an element string as a square GS1 Data Matrix.
     *
     * @param elementString the AIs, FNC1 as {@code 0x1D}, with no AIM Code ID prefix — the symbol
     *                      declares itself GS1 through its leading FNC1 codeword, not through text
     * @return the modules, row-major, {@code true} where a module is dark; no quiet zone
     * @throws TooLongException if it will not fit
     */
    public static boolean[][] encode(String elementString) {
        int[] data = toCodewords(elementString);
        SymbolVersion version = smallestSquareFor(data.length);

        int[] codewords = padTo(data, version.getDataCodewords());
        int[] withEcc = interleaveWithEcc(codewords, version);

        return BitPlacement.insertDataRegions(toMapping(withEcc, version), version);
    }

    /** The symbol chosen for an element string, for a report that wants to name its size. */
    public static SymbolVersion versionFor(String elementString) {
        return smallestSquareFor(toCodewords(elementString).length);
    }

    // ------------------------------------------------------------------ encodation

    /**
     * ASCII encodation, with a leading FNC1 so the symbol is a GS1 Data Matrix.
     *
     * <p>A digit pair is worth one codeword, which is why GS1 data packs so well. Anything outside
     * ASCII would need an extended-character escape; element strings never carry one, so a
     * character above 127 is refused rather than silently mangled.
     */
    static int[] toCodewords(String elementString) {
        int[] out = new int[elementString.length() + 2];
        int n = 0;
        out[n++] = FNC1;

        int i = 0;
        while (i < elementString.length()) {
            char c = elementString.charAt(i);

            if (c == GS1Constants.FNC1_GS) {
                out[n++] = FNC1;
                i++;
            } else if (isDigit(c) && i + 1 < elementString.length() && isDigit(elementString.charAt(i + 1))) {
                out[n++] = 130 + (c - '0') * 10 + (elementString.charAt(i + 1) - '0');
                i += 2;
            } else if (c < 128) {
                out[n++] = c + 1;
                i++;
            } else {
                throw new IllegalArgumentException(
                        "ASCII encodation cannot carry U+" + Integer.toHexString(c) + " at " + i);
            }
        }

        int[] trimmed = new int[n];
        System.arraycopy(out, 0, trimmed, 0, n);
        return trimmed;
    }

    private static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private static SymbolVersion smallestSquareFor(int codewords) {
        SymbolVersion best = null;

        for (SymbolVersion version : SymbolVersion.all()) {
            if (version.getRows() != version.getColumns()) continue;
            if (version.getDataCodewords() < codewords) continue;
            if (best == null || version.getDataCodewords() < best.getDataCodewords()) best = version;
        }

        if (best == null) {
            int largest = 0;
            for (SymbolVersion version : SymbolVersion.all()) {
                if (version.getRows() == version.getColumns()) {
                    largest = Math.max(largest, version.getDataCodewords());
                }
            }
            throw new TooLongException(codewords, largest);
        }
        return best;
    }

    /**
     * Pads to the symbol's data capacity: 129 once, then the randomised 253 sequence.
     *
     * <p>The randomisation exists so that a run of padding does not draw a visible block in the
     * symbol, which would give the binarizer a large featureless area to mis-threshold. The
     * position fed to it is 1-based over the data codewords.
     */
    static int[] padTo(int[] data, int capacity) {
        int[] out = new int[capacity];
        System.arraycopy(data, 0, out, 0, data.length);

        for (int i = data.length; i < capacity; i++) {
            if (i == data.length) {
                out[i] = PAD;
            } else {
                int pseudoRandom = ((149 * (i + 1)) % 253) + 1;
                int value = PAD + pseudoRandom;
                out[i] = value <= 254 ? value : value - 254;
            }
        }
        return out;
    }

    // ------------------------------------------------------------------ error correction

    /**
     * Splits the data into blocks, computes each block's check codewords, and interleaves the lot
     * the way ECC 200 reads them back: every block's first data codeword, then every block's
     * second, and so on, then the same for the check codewords.
     *
     * <p>Only the larger symbols use more than one block, but getting this wrong on those is
     * invisible until someone scans a long element string, so it is done properly rather than
     * assumed to be one block.
     */
    static int[] interleaveWithEcc(int[] data, SymbolVersion version) {
        int blocks = version.getBlockCount();
        int[] blockSizes = version.getBlockDataSizes();
        int eccPerBlock = version.getEccCodewordsPerBlock();

        int[][] blockData = new int[blocks][];
        for (int b = 0; b < blocks; b++) blockData[b] = new int[blockSizes[b]];

        // Round-robin, which is how the decoder de-interleaves it.
        int[] filled = new int[blocks];
        for (int i = 0, b = 0; i < data.length; i++) {
            while (filled[b] == blockSizes[b]) b = (b + 1) % blocks;
            blockData[b][filled[b]++] = data[i];
            b = (b + 1) % blocks;
        }

        int[][] blockEcc = new int[blocks][];
        for (int b = 0; b < blocks; b++) blockEcc[b] = checkCodewords(blockData[b], eccPerBlock);

        int[] out = new int[version.getTotalCodewords()];
        int n = 0;

        int longest = 0;
        for (int size : blockSizes) longest = Math.max(longest, size);
        for (int i = 0; i < longest; i++) {
            for (int b = 0; b < blocks; b++) {
                if (i < blockSizes[b]) out[n++] = blockData[b][i];
            }
        }
        for (int i = 0; i < eccPerBlock; i++) {
            for (int b = 0; b < blocks; b++) out[n++] = blockEcc[b][i];
        }
        return out;
    }

    /**
     * The Reed-Solomon check codewords for one block: the remainder of the message shifted up by
     * the check count, divided by the generator polynomial.
     *
     * <p>The generator's roots start at α¹, not α⁰ — {@link GaloisField256#GENERATOR_BASE} — which
     * is the Data Matrix convention and differs from QR Code's.
     */
    static int[] checkCodewords(int[] block, int eccCount) {
        Poly generator = Poly.ONE;
        for (int i = 0; i < eccCount; i++) {
            generator = generator.multiply(
                    new Poly(new int[] {1, GaloisField256.exp(i + GaloisField256.GENERATOR_BASE)}));
        }

        Poly remainder = new Poly(block.clone()).multiplyByMonomial(eccCount, 1).divide(generator)[1];

        // A remainder of lower degree than the check count means leading zero coefficients, which
        // the Poly invariant drops; they are real check codewords and have to be written back.
        int[] out = new int[eccCount];
        int[] coefficients = remainder.coefficients();
        System.arraycopy(coefficients, 0, out, eccCount - coefficients.length, coefficients.length);
        return out;
    }

    // ------------------------------------------------------------------ placement

    /** Walks the codewords into the mapping matrix, most significant bit first, per Annex F. */
    static boolean[][] toMapping(int[] codewords, SymbolVersion version) {
        int rows = version.getMappingRows();
        int columns = version.getMappingColumns();

        boolean[][] mapping = new boolean[rows][columns];
        List<int[]> order = BitPlacement.placementOrder(rows, columns);

        for (int i = 0; i < order.size() && i < codewords.length; i++) {
            int[] positions = order.get(i);
            int codeword = codewords[i];

            // Eight (row, column) pairs per codeword, bit 7 first.
            for (int bit = 0; bit < 8; bit++) {
                mapping[positions[bit * 2]][positions[bit * 2 + 1]] =
                        (codeword & (1 << (7 - bit))) != 0;
            }
        }
        return mapping;
    }
}
