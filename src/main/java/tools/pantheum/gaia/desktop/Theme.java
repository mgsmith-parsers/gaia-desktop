package tools.pantheum.gaia.desktop;

/**
 * The application's colour scheme. Applied in two places that have to agree:
 * the JavaFX chrome (a style class on the scene root, see {@code css/gaia-desktop.css})
 * and the HTML report inside the {@code WebView} (a {@code data-theme} attribute,
 * see {@link tools.pantheum.gaia.desktop.html.GaiaDesktopReport}).
 */
public enum Theme {

    LIGHT("Light",  "light", "theme-light"),
    DARK ("Dark",   "dark",  "theme-dark");

    private final String label;
    private final String htmlAttribute;
    private final String styleClass;

    Theme(String label, String htmlAttribute, String styleClass) {
        this.label         = label;
        this.htmlAttribute = htmlAttribute;
        this.styleClass    = styleClass;
    }

    /** The name shown in the UI. */
    public String label() { return label; }

    /** The value written to {@code <html data-theme="...">} in the report. */
    public String htmlAttribute() { return htmlAttribute; }

    /** The JavaFX style class placed on the scene root. */
    public String styleClass() { return styleClass; }

    @Override
    public String toString() { return label; }
}
