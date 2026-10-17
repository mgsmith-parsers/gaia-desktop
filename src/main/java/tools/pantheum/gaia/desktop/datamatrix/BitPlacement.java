package tools.pantheum.gaia.desktop.datamatrix;

import java.util.ArrayList;
import java.util.List;

/**
 * Turns a symbol's modules into codewords: strips the finder patterns and clock tracks, then
 * reverses the ECC 200 bit placement of ISO/IEC 16022 Annex F.
 *
 * <p>Data Matrix does not lay its codewords out in reading order. Each codeword's eight bits are
 * scattered in an L-shaped "utah" pattern that walks diagonally across the symbol and wraps around
 * its edges, with four special corner arrangements for where the diagonal runs out of room. The
 * point is damage tolerance: a blot that destroys a contiguous patch of the symbol damages one or
 * two bits of many codewords rather than all eight bits of a few, and Reed-Solomon corrects
 * codewords, not bits.
 *
 * <p>The standard defines placement as an <em>encoding</em> procedure. This class runs that same
 * walk to build the layout — which codeword owns which eight modules — and then reads through it.
 *
 * <h2>The wrap is not a plain modulo</h2>
 * When the diagonal runs off an edge, Annex F wraps it to the far side <em>and shifts the other
 * coordinate</em> by an amount that depends on the symbol's dimensions. Wrapping with a plain
 * modulo instead produces a layout that is right in the middle of the symbol and wrong at every
 * edge — which decodes small symbols correctly and larger ones as noise.
 *
 * <p><b>Twin.</b> Copied from {@code gaia-demo}'s own Data Matrix decoder, which must not be
 * depended on from here (see {@code CLAUDE.md}); the same arrangement as
 * {@code scanner/Symbologies}. Two deliberate differences, both so that
 * {@link DataMatrixEncoder} can walk the layout in the other direction: {@link #placementOrder}
 * is package-private rather than private, and {@link #insertDataRegions} is new — the inverse
 * of {@link #extractDataRegions}. Everything else is verbatim; fix a bug in both.
 */
final class BitPlacement {

    private BitPlacement() {}

    /**
     * Extracts the mapping matrix — the symbol with every finder pattern and clock track removed.
     *
     * <p>A symbol is a grid of data regions, each wrapped in a solid L on two sides and an
     * alternating clock track on the other two. Those are alignment aids, carry no data, and must
     * be dropped before the placement walk means anything.
     */
    static boolean[][] extractDataRegions(boolean[][] modules, SymbolVersion version) {
        int regionRows    = version.getRegionRows();
        int regionColumns = version.getRegionColumns();

        boolean[][] mapping = new boolean[version.getMappingRows()][version.getMappingColumns()];

        for (int regionDown = 0; regionDown < version.getRegionsDown(); regionDown++) {
            for (int regionAcross = 0; regionAcross < version.getRegionsAcross(); regionAcross++) {
                // Each region sits past its predecessors plus the two modules of its own border.
                int sourceRow = regionDown   * (regionRows    + 2) + 1;
                int sourceCol = regionAcross * (regionColumns + 2) + 1;
                int targetRow = regionDown   * regionRows;
                int targetCol = regionAcross * regionColumns;

                for (int row = 0; row < regionRows; row++) {
                    for (int col = 0; col < regionColumns; col++) {
                        mapping[targetRow + row][targetCol + col] =
                                modules[sourceRow + row][sourceCol + col];
                    }
                }
            }
        }
        return mapping;
    }

    /**
     * The inverse of {@link #extractDataRegions}: lays a mapping matrix into a full symbol and
     * draws the alignment patterns around each data region.
     *
     * <p>Every region carries a solid L down its left side and along its bottom, and an
     * alternating clock track along its top and right, starting dark in the corner. Those are what
     * a scanner locks onto; they carry no data, which is why the mapping matrix knows nothing about
     * them. The quiet zone is the renderer's business, not the symbol's.
     *
     * @param mapping the data modules, {@link SymbolVersion#getMappingRows()} by
     *                {@link SymbolVersion#getMappingColumns()}
     * @return the symbol, {@link SymbolVersion#getRows()} by {@link SymbolVersion#getColumns()}
     */
    static boolean[][] insertDataRegions(boolean[][] mapping, SymbolVersion version) {
        int regionRows    = version.getRegionRows();
        int regionColumns = version.getRegionColumns();

        boolean[][] modules = new boolean[version.getRows()][version.getColumns()];

        for (int regionDown = 0; regionDown < version.getRegionsDown(); regionDown++) {
            for (int regionAcross = 0; regionAcross < version.getRegionsAcross(); regionAcross++) {
                int targetRow = regionDown   * (regionRows    + 2);
                int targetCol = regionAcross * (regionColumns + 2);
                int sourceRow = regionDown   * regionRows;
                int sourceCol = regionAcross * regionColumns;

                // The solid L: left edge and bottom edge of this region's border.
                for (int row = 0; row < regionRows + 2; row++) {
                    modules[targetRow + row][targetCol] = true;
                }
                for (int col = 0; col < regionColumns + 2; col++) {
                    modules[targetRow + regionRows + 1][targetCol + col] = true;
                }

                // The clock track: top and right edges, alternating, dark where the index is even
                // so that it meets the solid L dark in both corners.
                for (int col = 0; col < regionColumns + 2; col++) {
                    modules[targetRow][targetCol + col] = col % 2 == 0;
                }
                for (int row = 0; row < regionRows + 2; row++) {
                    modules[targetRow + row][targetCol + regionColumns + 1] =
                            (regionRows + 1 - row) % 2 == 0;
                }

                for (int row = 0; row < regionRows; row++) {
                    for (int col = 0; col < regionColumns; col++) {
                        modules[targetRow + row + 1][targetCol + col + 1] =
                                mapping[sourceRow + row][sourceCol + col];
                    }
                }
            }
        }
        return modules;
    }

    /**
     * Reads the codewords out of a mapping matrix, in the order the encoder wrote them.
     *
     * @return one value 0-255 per codeword in the symbol
     */
    static int[] readCodewords(boolean[][] mapping, SymbolVersion version) {
        int rows    = version.getMappingRows();
        int columns = version.getMappingColumns();

        List<int[]> layout = placementOrder(rows, columns);
        int[] codewords = new int[version.getTotalCodewords()];

        for (int i = 0; i < codewords.length && i < layout.size(); i++) {
            int[] bits = layout.get(i);
            int value = 0;
            for (int bit = 0; bit < 8; bit++) {
                value <<= 1;
                if (mapping[bits[bit * 2]][bits[bit * 2 + 1]]) value |= 1;
            }
            codewords[i] = value;
        }
        return codewords;
    }

    /**
     * Runs the Annex F walk, returning for each codeword the (row, column) of its eight bits, most
     * significant bit first.
     */
    static List<int[]> placementOrder(int rows, int columns) {
        List<int[]> layout = new ArrayList<>();
        boolean[][] placed = new boolean[rows][columns];

        int row = 4;
        int col = 0;

        do {
            // Corner cases: codewords whose bits will not fit the usual diagonal pattern.
            if (row == rows && col == 0) {
                add(layout, placed, cornerA(rows, columns));
            }
            if (row == rows - 2 && col == 0 && columns % 4 != 0) {
                add(layout, placed, cornerB(rows, columns));
            }
            if (row == rows - 2 && col == 0 && columns % 8 == 4) {
                add(layout, placed, cornerC(rows, columns));
            }
            if (row == rows + 4 && col == 2 && columns % 8 == 0) {
                add(layout, placed, cornerD(rows, columns));
            }

            // Sweep diagonally up and to the right.
            do {
                if (row < rows && col >= 0 && !placed[row][col]) {
                    add(layout, placed, utah(row, col, rows, columns));
                }
                row -= 2;
                col += 2;
            } while (row >= 0 && col < columns);
            row += 1;
            col += 3;

            // Then diagonally down and to the left.
            do {
                if (row >= 0 && col < columns && !placed[row][col]) {
                    add(layout, placed, utah(row, col, rows, columns));
                }
                row += 2;
                col -= 2;
            } while (row < rows && col >= 0);
            row += 3;
            col += 1;

        } while (row < rows || col < columns);

        return layout;
    }

    /** Records one codeword's eight positions and marks those modules as spoken for. */
    private static void add(List<int[]> layout, boolean[][] placed, int[] positions) {
        for (int i = 0; i < 8; i++) {
            placed[positions[i * 2]][positions[i * 2 + 1]] = true;
        }
        layout.add(positions);
    }

    /** The standard pattern — one codeword's eight bits in their L arrangement. */
    private static int[] utah(int row, int col, int rows, int columns) {
        return resolve(new int[][]{
                {row - 2, col - 2}, {row - 2, col - 1},
                {row - 1, col - 2}, {row - 1, col - 1}, {row - 1, col},
                {row,     col - 2}, {row,     col - 1}, {row,     col},
        }, rows, columns);
    }

    // -------------------------------------------------------------------------
    // The four corner patterns of Annex F
    // -------------------------------------------------------------------------

    private static int[] cornerA(int rows, int columns) {
        return resolve(new int[][]{
                {rows - 1, 0}, {rows - 1, 1}, {rows - 1, 2}, {0, columns - 2},
                {0, columns - 1}, {1, columns - 1}, {2, columns - 1}, {3, columns - 1},
        }, rows, columns);
    }

    private static int[] cornerB(int rows, int columns) {
        return resolve(new int[][]{
                {rows - 3, 0}, {rows - 2, 0}, {rows - 1, 0}, {0, columns - 4},
                {0, columns - 3}, {0, columns - 2}, {0, columns - 1}, {1, columns - 1},
        }, rows, columns);
    }

    private static int[] cornerC(int rows, int columns) {
        return resolve(new int[][]{
                {rows - 3, 0}, {rows - 2, 0}, {rows - 1, 0}, {0, columns - 2},
                {0, columns - 1}, {1, columns - 1}, {2, columns - 1}, {3, columns - 1},
        }, rows, columns);
    }

    private static int[] cornerD(int rows, int columns) {
        return resolve(new int[][]{
                {rows - 1, 0}, {rows - 1, columns - 1}, {0, columns - 3}, {0, columns - 2},
                {0, columns - 1}, {1, columns - 3}, {1, columns - 2}, {1, columns - 1},
        }, rows, columns);
    }

    // -------------------------------------------------------------------------

    /** Resolves eight (row, column) pairs through the wrap rule and flattens them. */
    private static int[] resolve(int[][] offsets, int rows, int columns) {
        int[] flat = new int[16];
        for (int i = 0; i < 8; i++) {
            int row = offsets[i][0];
            int col = offsets[i][1];

            // Annex F's wrap: coming off an edge moves the other coordinate as well.
            if (row < 0) {
                row += rows;
                col += 4 - ((rows + 4) % 8);
            }
            if (col < 0) {
                col += columns;
                row += 4 - ((columns + 4) % 8);
            }
            flat[i * 2]     = row;
            flat[i * 2 + 1] = col;
        }
        return flat;
    }
}
