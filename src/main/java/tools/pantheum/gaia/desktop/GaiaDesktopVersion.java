package tools.pantheum.gaia.desktop;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * The build version and timestamp of <em>this application</em>, sourced from
 * {@code gaia-desktop-version.properties}, which Maven resource filtering populates from
 * {@code gaia-desktop/pom.xml} at build time.
 *
 * <p>Deliberately separate from {@link tools.pantheum.gaia.GaiaVersion}, which reports the
 * version of the core parser this app is built against. The two move independently, and the
 * fact that they currently agree is a coincidence of release timing, not a guarantee — so a
 * report or a bug report that names only one of them is ambiguous. Everywhere a version is
 * shown, both are shown, along with this module's build timestamp.
 *
 * <p>Mirrors the core's {@code GaiaVersion} exactly, including the {@code "unknown"} fallback
 * for running unpackaged from an IDE before {@code mvn process-resources} has filtered the
 * resource.
 */
public final class GaiaDesktopVersion {

    /** Application version, e.g. {@code "1.0.0"}. */
    public static final String VERSION   = load("version");

    /** ISO-8601 build timestamp, e.g. {@code "2026-08-09T23:20:00Z"}. */
    public static final String TIMESTAMP = load("build.timestamp");

    private GaiaDesktopVersion() {}

    // -------------------------------------------------------------------------

    private static String load(String key) {
        try (InputStream is = GaiaDesktopVersion.class.getResourceAsStream("/gaia-desktop-version.properties")) {
            if (is == null) return "unknown";
            Properties props = new Properties();
            props.load(is);
            String value = props.getProperty(key, "unknown").trim();
            // An unfiltered resource still holds the literal "${project.version}".
            return value.isEmpty() || value.startsWith("${") ? "unknown" : value;
        } catch (IOException e) {
            return "unknown";
        }
    }
}
