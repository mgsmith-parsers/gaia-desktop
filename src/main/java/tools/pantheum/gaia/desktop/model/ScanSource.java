package tools.pantheum.gaia.desktop.model;

/** Where the input of a {@link ScanRecord} came from. */
public enum ScanSource {

    /** Typed or pasted into the input bar. */
    MANUAL("manual"),
    /** Received from the JavaPOS scanner. */
    SCANNER("scanner"),
    /** Read from a text file (one input per line). */
    FILE("file"),
    /** Assembled from AI/value pairs by the builder, then parsed like any other input. */
    BUILT("built");

    private final String label;

    ScanSource(String label) {
        this.label = label;
    }

    public String label() { return label; }

    @Override
    public String toString() { return label; }
}
