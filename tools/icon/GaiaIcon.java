/*
 * Draws the GAIA Desktop icon: the palette's accent in a macOS-grid rounded square (824 px on a
 * 1024 canvas), the GAIA wordmark, and a GS1-128-like run of bars. Not part of the build — the
 * outputs are checked in. To regenerate them on a Mac, from gaia-desktop/:
 *
 *   java tools/icon/GaiaIcon.java /tmp/gaia-1024.png
 *   mkdir /tmp/gaia.iconset
 *   for s in 16 32 128 256 512; do
 *     sips -z $s $s /tmp/gaia-1024.png --out /tmp/gaia.iconset/icon_${s}x${s}.png
 *     sips -z $((s*2)) $((s*2)) /tmp/gaia-1024.png --out /tmp/gaia.iconset/icon_${s}x${s}@2x.png
 *   done
 *   iconutil -c icns /tmp/gaia.iconset -o src/main/macos/gaia-desktop.icns
 *   for s in 16 32 64 128 256; do
 *     sips -z $s $s /tmp/gaia-1024.png --out src/main/resources/icons/gaia-desktop-$s.png
 *   done
 *
 * The .icns is the macOS app bundle's icon; the PNGs are the window icon on every platform.
 */
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class GaiaIcon {
  public static void main(String[] a) throws Exception {
    int S = 1024;
    BufferedImage img = new BufferedImage(S, S, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = img.createGraphics();
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

    // macOS icon grid: an 824-px rounded square centred on the 1024 canvas.
    double x = 100, y = 100, w = 824, r = 185;
    RoundRectangle2D tile = new RoundRectangle2D.Double(x, y, w, w, r * 2, r * 2);

    // Soft shadow under the tile.
    for (int i = 18; i > 0; i--) {
      g.setColor(new Color(0, 0, 0, 3));
      g.fill(new RoundRectangle2D.Double(x - i * 0.6, y + 10 - i * 0.2, w + i * 1.2, w + i * 1.2, (r + i) * 2, (r + i) * 2));
    }

    // The palette's accent, light to deep.
    g.setPaint(new GradientPaint(0, (float) y, new Color(0x16, 0xA3, 0xAC), 0, (float) (y + w), new Color(0x07, 0x52, 0x58)));
    g.fill(tile);
    // A faint highlight along the top edge.
    g.setClip(tile);
    g.setPaint(new GradientPaint(0, (float) y, new Color(255, 255, 255, 46), 0, (float) (y + 260), new Color(255, 255, 255, 0)));
    g.fill(new Rectangle2D.Double(x, y, w, 260));
    g.setClip(null);

    // Wordmark.
    Font font = new Font("Helvetica Neue", Font.BOLD, 236);
    g.setFont(font);
    FontMetrics fm = g.getFontMetrics();
    String word = "GAIA";
    int tw = fm.stringWidth(word);
    g.setColor(Color.WHITE);
    g.drawString(word, (S - tw) / 2, 470);

    // Barcode: bars of GS1-128-like varied widths, in a white band.
    int[] bars = {2,1,1,2,3,1,1,3,2,1,2,2,1,1,3,1,2,1,1,2,2,3,1,1,2,1,3,2,1,1,2,1,1,3,2,2,1,2,1,1,2,3};
    int units = 0;
    for (int b : bars) units += b;
    int unit = 600 / units, total = unit * units;
    int bx = (S - total) / 2, by = 540, bh = 210;
    boolean dark = true;
    for (int b : bars) {
      if (dark) { g.setColor(Color.WHITE); g.fillRect(bx, by, b * unit, bh); }
      bx += b * unit; dark = !dark;
    }
    // A thin rule under the bars, like the human-readable line's baseline.
    g.setColor(new Color(255, 255, 255, 170));
    g.fill(new RoundRectangle2D.Double((S - total) / 2.0, by + bh + 34, total, 14, 14, 14));
    g.dispose();
    ImageIO.write(img, "png", new File(a[0]));
  }
}
