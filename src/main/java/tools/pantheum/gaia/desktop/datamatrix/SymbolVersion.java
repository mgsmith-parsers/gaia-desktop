package tools.pantheum.gaia.desktop.datamatrix;

/**
 * One ECC 200 Data Matrix symbol size, from the table in ISO/IEC 16022 Annex C (plus the
 * rectangular extensions of ISO/IEC 21471, "DMRE").
 *
 * <p>A symbol's size determines everything downstream: how many data regions it is divided into,
 * how many codewords it holds, and how those codewords are split into Reed-Solomon blocks. The
 * table is fixed by the standard and is the first thing a decoder must get right — every later
 * stage is indexed off it.
 *
 * <h2>Rows and columns</h2>
 * {@link #getRows()} / {@link #getColumns()} are the <em>whole symbol</em>, finder pattern and
 * clock track included — what a detector hands over. Each data region is
 * {@link #getRegionRows()} x {@link #getRegionColumns()} of pure data, with its two-module L
 * finder and its clock track excluded, so a symbol holds
 * {@code (rows / (regionRows + 2)) * (columns / (regionColumns + 2))} regions.
 *
 * <p><b>Twin.</b> Copied verbatim from {@code gaia-demo}'s own Data Matrix decoder, package
 * line apart. The modules must not depend on one another (see {@code CLAUDE.md}), and this is
 * the same arrangement {@code scanner/Symbologies} uses. Fix a bug in both.
 */
public final class SymbolVersion {

    /**
     * The full ECC 200 table: {rows, columns, regionRows, regionColumns, eccCodewordsPerBlock,
     * then pairs of (blockCount, dataCodewordsPerBlock)}.
     *
     * <p>Transcribed from ISO/IEC 16022 and checked entry-by-entry against ZXing's own table by
     * {@code SymbolVersionTest} — a wrong row here would misdecode every symbol of that size, and
     * the failure would look like data corruption rather than a table error.
     */
    private static final int[][] TABLE = {
            // rows cols  rRows rCols  ecc   blocks (count, dataCodewords)...
            {  10,  10,     8,    8,     5,   1,    3 },
            {  12,  12,    10,   10,     7,   1,    5 },
            {  14,  14,    12,   12,    10,   1,    8 },
            {  16,  16,    14,   14,    12,   1,   12 },
            {  18,  18,    16,   16,    14,   1,   18 },
            {  20,  20,    18,   18,    18,   1,   22 },
            {  22,  22,    20,   20,    20,   1,   30 },
            {  24,  24,    22,   22,    24,   1,   36 },
            {  26,  26,    24,   24,    28,   1,   44 },
            {  32,  32,    14,   14,    36,   1,   62 },
            {  36,  36,    16,   16,    42,   1,   86 },
            {  40,  40,    18,   18,    48,   1,  114 },
            {  44,  44,    20,   20,    56,   1,  144 },
            {  48,  48,    22,   22,    68,   1,  174 },
            {  52,  52,    24,   24,    42,   2,  102 },
            {  64,  64,    14,   14,    56,   2,  140 },
            {  72,  72,    16,   16,    36,   4,   92 },
            {  80,  80,    18,   18,    48,   4,  114 },
            {  88,  88,    20,   20,    56,   4,  144 },
            {  96,  96,    22,   22,    68,   4,  174 },
            { 104, 104,    24,   24,    56,   6,  136 },
            { 120, 120,    18,   18,    68,   6,  175 },
            { 132, 132,    20,   20,    62,   8,  163 },
            { 144, 144,    22,   22,    62,   8,  156,   2, 155 },   // the only mixed-block size
            // Rectangular — ISO/IEC 16022
            {   8,  18,     6,   16,     7,   1,    5 },
            {   8,  32,     6,   14,    11,   1,   10 },
            {  12,  26,    10,   24,    14,   1,   16 },
            {  12,  36,    10,   16,    18,   1,   22 },
            {  16,  36,    14,   16,    24,   1,   32 },
            {  16,  48,    14,   22,    28,   1,   49 },
            // Rectangular extensions — ISO/IEC 21471 (DMRE)
            {   8,  48,     6,   22,    15,   1,   18 },
            {   8,  64,     6,   14,    18,   1,   24 },
            {   8,  80,     6,   18,    22,   1,   32 },
            {   8,  96,     6,   22,    28,   1,   38 },
            {   8, 120,     6,   18,    32,   1,   49 },
            {   8, 144,     6,   22,    36,   1,   63 },
            {  12,  64,    10,   14,    27,   1,   43 },
            {  12,  88,    10,   20,    36,   1,   64 },
            {  16,  64,    14,   14,    36,   1,   62 },
            {  20,  36,    18,   16,    28,   1,   44 },
            {  20,  44,    18,   20,    34,   1,   56 },
            {  20,  64,    18,   14,    42,   1,   84 },
            {  22,  48,    20,   22,    38,   1,   72 },
            {  24,  48,    22,   22,    41,   1,   80 },
            {  24,  64,    22,   14,    46,   1,  108 },
            {  26,  40,    24,   18,    38,   1,   70 },
            {  26,  48,    24,   22,    42,   1,   90 },
            {  26,  64,    24,   14,    50,   1,  118 },
    };

    private final int   rows;
    private final int   columns;
    private final int   regionRows;
    private final int   regionColumns;
    private final int   eccCodewordsPerBlock;
    private final int[] blockCounts;
    private final int[] blockDataCodewords;

    private SymbolVersion(int[] row) {
        this.rows                 = row[0];
        this.columns              = row[1];
        this.regionRows           = row[2];
        this.regionColumns        = row[3];
        this.eccCodewordsPerBlock = row[4];

        int groups = (row.length - 5) / 2;
        this.blockCounts        = new int[groups];
        this.blockDataCodewords = new int[groups];
        for (int i = 0; i < groups; i++) {
            blockCounts[i]        = row[5 + 2 * i];
            blockDataCodewords[i] = row[6 + 2 * i];
        }
    }

    /**
     * Returns the version for a symbol of the given size.
     *
     * @throws IllegalArgumentException when no ECC 200 symbol has those dimensions — which for a
     *         decoder means the detector handed over something that is not a Data Matrix, or
     *         sampled it at the wrong grid size
     */
    public static SymbolVersion forSize(int rows, int columns) {
        for (int[] row : TABLE) {
            if (row[0] == rows && row[1] == columns) return new SymbolVersion(row);
        }
        throw new IllegalArgumentException(
                "no ECC 200 symbol is " + rows + "x" + columns + " modules");
    }

    /** Returns every version in the table, smallest first — for exhaustive tests. */
    public static SymbolVersion[] all() {
        SymbolVersion[] versions = new SymbolVersion[TABLE.length];
        for (int i = 0; i < TABLE.length; i++) versions[i] = new SymbolVersion(TABLE[i]);
        return versions;
    }

    public int getRows()                 { return rows; }
    public int getColumns()              { return columns; }
    public int getRegionRows()           { return regionRows; }
    public int getRegionColumns()        { return regionColumns; }
    public int getEccCodewordsPerBlock() { return eccCodewordsPerBlock; }

    /** Data regions across the symbol. */
    public int getRegionsAcross() { return columns / (regionColumns + 2); }

    /** Data regions down the symbol. */
    public int getRegionsDown() { return rows / (regionRows + 2); }

    /** The mapping matrix width — the symbol with its finder patterns and clock tracks removed. */
    public int getMappingColumns() { return getRegionsAcross() * regionColumns; }

    /** The mapping matrix height — the symbol with its finder patterns and clock tracks removed. */
    public int getMappingRows() { return getRegionsDown() * regionRows; }

    /** Total codewords in the symbol, data and error correction together. */
    public int getTotalCodewords() {
        int total = 0;
        for (int i = 0; i < blockCounts.length; i++) {
            total += blockCounts[i] * (blockDataCodewords[i] + eccCodewordsPerBlock);
        }
        return total;
    }

    /** Data codewords across every block. */
    public int getDataCodewords() {
        int data = 0;
        for (int i = 0; i < blockCounts.length; i++) {
            data += blockCounts[i] * blockDataCodewords[i];
        }
        return data;
    }

    /** The number of Reed-Solomon blocks the codewords are interleaved across. */
    public int getBlockCount() {
        int blocks = 0;
        for (int count : blockCounts) blocks += count;
        return blocks;
    }

    /**
     * Returns the data-codeword count of each Reed-Solomon block, in block order.
     *
     * <p>All sizes but one use blocks of equal length; 144x144 alone mixes 8 blocks of 156 with
     * 2 of 155, and a decoder that assumes uniform blocks de-interleaves that size wrongly.
     */
    public int[] getBlockDataSizes() {
        int[] sizes = new int[getBlockCount()];
        int at = 0;
        for (int group = 0; group < blockCounts.length; group++) {
            for (int i = 0; i < blockCounts[group]; i++) {
                sizes[at++] = blockDataCodewords[group];
            }
        }
        return sizes;
    }

    @Override
    public String toString() {
        return rows + "x" + columns;
    }
}
