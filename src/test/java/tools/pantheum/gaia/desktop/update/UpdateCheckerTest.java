package tools.pantheum.gaia.desktop.update;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link UpdateChecker} without the network: reading GitHub's release document, comparing
 * versions, and deciding when a check is due.
 */
@DisplayName("UpdateChecker")
class UpdateCheckerTest {

    // ---------------------------------------------------------------- comparing versions

    @Test
    @DisplayName("compares versions number by number, not as text")
    void numericComparison() {
        assertTrue(UpdateChecker.isNewer("1.0.10", "1.0.9"), "10 > 9, though \"10\" < \"9\" as text");
        assertTrue(UpdateChecker.isNewer("1.1.0", "1.0.99"));
        assertTrue(UpdateChecker.isNewer("2.0.0", "1.9.9"));
        assertFalse(UpdateChecker.isNewer("1.0.9", "1.0.10"));
    }

    @Test
    @DisplayName("the same version, however written, is not newer")
    void sameVersion() {
        assertFalse(UpdateChecker.isNewer("1.0.0", "1.0.0"));
        assertFalse(UpdateChecker.isNewer("v1.0.0", "1.0.0"), "a v prefix is not a difference");
        assertFalse(UpdateChecker.isNewer("1.0", "1.0.0"), "a missing part counts as zero");
        assertTrue(UpdateChecker.isNewer("1.0.1", "1.0"));
    }

    @Test
    @DisplayName("a development build is never told it is out of date")
    void unknownCurrentVersion() {
        assertFalse(UpdateChecker.isNewer("9.9.9", "unknown"));
        assertFalse(UpdateChecker.isNewer("9.9.9", null));
        assertFalse(UpdateChecker.isNewer("9.9.9", "1.1.0-SNAPSHOT"));
    }

    @Test
    @DisplayName("a development build is not a release, so it never asks")
    void developmentBuild() {
        assertTrue(UpdateChecker.isRelease("1.0.0"));
        assertTrue(UpdateChecker.isRelease("v1.0.0"));
        assertFalse(UpdateChecker.isRelease("unknown"));
        assertFalse(UpdateChecker.isRelease("1.1.0-SNAPSHOT"));
    }

    @Test
    @DisplayName("only plain release numbers are versions")
    void versionParts() {
        assertArrayEquals(new int[] {1, 0, 10}, UpdateChecker.parts("1.0.10"));
        assertNull(UpdateChecker.parts("2.0-rc1"));
        assertNull(UpdateChecker.parts("unknown"));
        assertNull(UpdateChecker.parts(""));
        assertNull(UpdateChecker.parts("1..2"));
        assertNull(UpdateChecker.parts("99999999999.0"), "too large for an int");
    }

    // ---------------------------------------------------------------- reading GitHub's answer

    @Test
    @DisplayName("reads the tag and the release page from GitHub's document")
    void parsesARelease() throws IOException {
        Optional<UpdateChecker.Release> release = UpdateChecker.parse("""
                {"tag_name": "v1.1.0", "html_url": "https://github.com/mgsmith-parsers/gaia-desktop/releases/tag/v1.1.0",
                 "draft": false, "prerelease": false, "name": "GAIA Desktop 1.1.0"}
                """);

        assertTrue(release.isPresent());
        assertEquals("1.1.0", release.get().version(), "the tag without its v");
        assertEquals("https://github.com/mgsmith-parsers/gaia-desktop/releases/tag/v1.1.0", release.get().pageUrl());
    }

    @Test
    @DisplayName("drafts, pre-releases and tags that are not versions are not offered")
    void ignoresWhatIsNotARelease() throws IOException {
        assertTrue(UpdateChecker.parse("{\"tag_name\":\"v1.1.0\",\"draft\":true}").isEmpty());
        assertTrue(UpdateChecker.parse("{\"tag_name\":\"v1.1.0\",\"prerelease\":true}").isEmpty());
        assertTrue(UpdateChecker.parse("{\"tag_name\":\"nightly\"}").isEmpty());
        assertTrue(UpdateChecker.parse("{}").isEmpty());
    }

    @Test
    @DisplayName("a missing or odd page link falls back to the releases page")
    void pageFallback() throws IOException {
        assertEquals(UpdateChecker.RELEASES_PAGE,
                UpdateChecker.parse("{\"tag_name\":\"1.2.0\"}").orElseThrow().pageUrl());
        assertEquals(UpdateChecker.RELEASES_PAGE,
                UpdateChecker.parse("{\"tag_name\":\"1.2.0\",\"html_url\":\"javascript:alert(1)\"}").orElseThrow().pageUrl(),
                "only an https page is ever handed to the browser");
    }

    @Test
    @DisplayName("an answer that is not JSON is an error, not a release")
    void notJson() {
        assertThrows(IOException.class, () -> UpdateChecker.parse("<html>rate limited</html>"));
    }

    // ---------------------------------------------------------------- when to check

    @Test
    @DisplayName("an automatic check is due once a day, and on the first run")
    void dueOncePerInterval() {
        Instant now = Instant.parse("2026-10-17T09:00:00Z");
        assertTrue(UpdateChecker.isDue(null, now), "never checked");
        assertFalse(UpdateChecker.isDue(now.minus(Duration.ofHours(23)), now));
        assertTrue(UpdateChecker.isDue(now.minus(Duration.ofHours(24)), now));
        assertTrue(UpdateChecker.isDue(now.minus(Duration.ofDays(30)), now));
    }
}
