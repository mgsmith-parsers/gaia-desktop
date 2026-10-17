package tools.pantheum.gaia.desktop.datamatrix;

/**
 * Draws an encoded symbol as inline SVG, for the report's {@code WebView} and for the HTML it
 * saves.
 *
 * <p>Inline and self-contained on purpose: the alternative — an {@code <img>} pointing at a barcode
 * service, which is what the tool this feature came from does — would send the element string, and
 * so real GTINs, batch numbers and serials, to a third party every time a report rendered. Nothing
 * else in this application makes a network call and this is not the place to start. A saved report
 * also keeps working with no network and no expiring URL.
 *
 * <p><b>Always dark on light.</b> The symbol does not follow the dark theme. A Data Matrix with
 * reflectance reversed is a different thing from the one GS1 specifies — some scanners read it,
 * conformance verification does not — so the symbol keeps a white tile of its own whatever the
 * page around it is doing.
 */
public final class DataMatrixSvg {

    /**
     * The quiet zone, in modules, on every side.
     *
     * <p>ISO/IEC 16022 asks for one; two is used here because the tile is also what a user
     * screenshots or prints, and a single module of white against a dark page reads as none.
     */
    private static final int QUIET_ZONE = 2;

    private DataMatrixSvg() {}

    /**
     * @param modules    the symbol, row-major, {@code true} where dark; no quiet zone
     * @param modulePx   the side of one module in CSS pixels; 4 keeps a 22×22 symbol legible on a
     *                   normal display without dominating the report
     * @param describedBy text for assistive technology — the element string is not it, since a
     *                   screen reader cannot usefully say a GS1 string; name what the symbol is
     * @return one {@code <svg>} element, no XML declaration, safe to drop into a page
     */
    public static String render(boolean[][] modules, int modulePx, String describedBy) {
        int rows = modules.length;
        int columns = modules[0].length;
        int side = QUIET_ZONE * 2;
        int width = (columns + side) * modulePx;
        int height = (rows + side) * modulePx;

        StringBuilder svg = new StringBuilder(1024 + rows * columns * 8);

        svg.append("<svg xmlns=\"http://www.w3.org/2000/svg\" class=\"datamatrix\"")
           .append(" width=\"").append(width).append("\" height=\"").append(height).append('"')
           .append(" viewBox=\"0 0 ").append(columns + side).append(' ').append(rows + side).append('"')
           .append(" role=\"img\" aria-label=\"").append(escapeAttribute(describedBy)).append("\">\n");

        // The white tile is part of the symbol, not decoration: it is the quiet zone.
        svg.append("  <rect width=\"").append(columns + side).append("\" height=\"").append(rows + side)
           .append("\" fill=\"#ffffff\"/>\n");

        // shape-rendering keeps adjacent modules from showing hairlines where they meet, which is
        // what makes a scanned screenshot fail.
        svg.append("  <g fill=\"#000000\" shape-rendering=\"crispEdges\">\n");

        for (int row = 0; row < rows; row++) {
            int col = 0;
            while (col < columns) {
                if (!modules[row][col]) {
                    col++;
                    continue;
                }
                // Runs of dark modules become one rect, which keeps a large symbol's SVG a few KB
                // rather than a few thousand elements.
                int run = 1;
                while (col + run < columns && modules[row][col + run]) run++;

                svg.append("    <rect x=\"").append(col + QUIET_ZONE)
                   .append("\" y=\"").append(row + QUIET_ZONE)
                   .append("\" width=\"").append(run).append("\" height=\"1\"/>\n");
                col += run;
            }
        }

        return svg.append("  </g>\n</svg>").toString();
    }

    private static String escapeAttribute(String text) {
        return text == null ? "" : text.replace("&", "&amp;").replace("<", "&lt;")
                                      .replace(">", "&gt;").replace("\"", "&quot;");
    }
}
