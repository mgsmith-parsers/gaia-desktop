package tools.pantheum.gaia.desktop;

import tools.pantheum.gaia.GaiaVersion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link GaiaDesktopVersion}.
 *
 * <p>These run under Maven, after {@code process-resources} has filtered the version stub —
 * which is the point: they fail if the filtering configuration in the pom is ever removed or
 * broadened to the wrong files, rather than letting a jar ship reporting "unknown".
 */
@DisplayName("GaiaDesktopVersion")
class GaiaDesktopVersionTest {

    @Test
    @DisplayName("the application version is filtered in from the pom at build time")
    void versionIsFiltered() {
        assertNotEquals("unknown", GaiaDesktopVersion.VERSION,
                "gaia-desktop-version.properties was not filtered — check the <resources> block in the pom");
        assertTrue(GaiaDesktopVersion.VERSION.matches("\\d+\\.\\d+\\.\\d+(-\\w+)?"),
                "not a version: " + GaiaDesktopVersion.VERSION);
    }

    @Test
    @DisplayName("the build timestamp is filtered in too")
    void timestampIsFiltered() {
        assertNotEquals("unknown", GaiaDesktopVersion.TIMESTAMP);
        assertTrue(GaiaDesktopVersion.TIMESTAMP.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z"),
                "not an ISO-8601 timestamp: " + GaiaDesktopVersion.TIMESTAMP);
    }

    @Test
    @DisplayName("the application version comes from this module, not from the core")
    void separateFromTheCore() {
        // The two are versioned independently, and they may legitimately agree — as they did
        // when both read 1.0.0. So this asserts the *source*, not a difference in text:
        // each version is filtered from its own module's pom into its own resource, and it is
        // that separation the class exists to keep.
        assertNotEquals("unknown", GaiaDesktopVersion.VERSION);
        assertNotEquals("unknown", GaiaVersion.VERSION);

        assertNotNull(GaiaDesktopVersion.class.getResource("/gaia-desktop-version.properties"),
                "the app version must come from gaia-desktop's own filtered resource");
        assertNotNull(GaiaVersion.class.getResource("/gaia-version.properties"),
                "the core version must come from the core's own resource");

        // Only gaia-desktop's stub carries a build timestamp, which is what tells the two
        // apart when their version strings match.
        assertTrue(GaiaDesktopVersion.TIMESTAMP.endsWith("Z"), GaiaDesktopVersion.TIMESTAMP);
    }
}
