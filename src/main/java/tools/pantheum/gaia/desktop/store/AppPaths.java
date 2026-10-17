package tools.pantheum.gaia.desktop.store;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Where the desktop app keeps its state between runs: {@code ~/.gaia-desktop/}, holding one
 * file per concern —
 *
 * <pre>
 *   ~/.gaia-desktop/options.properties   parser options and window state
 *   ~/.gaia-desktop/captures.tsv         the capture history
 * </pre>
 *
 * <p>Two files rather than one because they change on different clocks and for different
 * reasons: options are a handful of lines rewritten when the user changes a setting, while
 * captures grow with every scan. Keeping them apart means a corrupt or hand-edited capture
 * log can be deleted without losing the settings, and vice versa.
 *
 * <p>The directory can be moved with the {@code gaia.desktop.home} system property, which is
 * what the tests use to stay out of the real home directory.
 */
public final class AppPaths {

    private AppPaths() {}

    /** System property that overrides the state directory. */
    public static final String HOME_PROPERTY = "gaia.desktop.home";

    private static final String DIR_NAME       = ".gaia-desktop";
    private static final String OPTIONS_FILE   = "options.properties";
    private static final String CAPTURES_FILE  = "captures.tsv";

    /** The state directory. Not created here — the stores create it when they first write. */
    public static Path home() {
        String override = System.getProperty(HOME_PROPERTY);
        return override != null && !override.isEmpty()
                ? Paths.get(override)
                : Paths.get(System.getProperty("user.home"), DIR_NAME);
    }

    /** {@code ~/.gaia-desktop/options.properties}. */
    public static Path optionsFile() {
        return home().resolve(OPTIONS_FILE);
    }

    /** {@code ~/.gaia-desktop/captures.tsv}. */
    public static Path capturesFile() {
        return home().resolve(CAPTURES_FILE);
    }

    /** Creates the state directory if it does not exist yet. */
    static void ensureHome() throws IOException {
        Files.createDirectories(home());
    }
}
