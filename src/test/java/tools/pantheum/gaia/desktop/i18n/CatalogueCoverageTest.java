package tools.pantheum.gaia.desktop.i18n;

import tools.pantheum.gaia.GaiaConstants;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checks that the catalogue and the code agree about which keys exist.
 *
 * <p>{@link UiStrings} is deliberately forgiving at runtime — an unknown key renders as the
 * key itself rather than throwing in the middle of a layout pass, because a mis-typed label
 * should not take the window down. That forgiveness is why this test exists: without it a
 * missing key only ever surfaces as a log line and a menu item reading {@code act.hri}.
 *
 * <p>It reads the module's own sources rather than reflecting over the classes, because the
 * keys are string literals at the call sites and never survive into the bytecode as anything
 * a test could enumerate.
 */
@DisplayName("Catalogue coverage")
class CatalogueCoverageTest {

    /** Matches the literal key in {@code strings.get("x")}, {@code s.format("x", …)}, {@code item("x", …)}. */
    private static final Pattern KEY_AT_CALL_SITE = Pattern.compile(
            "(?:strings|s)\\.(?:get|format)\\(\\s*\"([^\"]+)\"|\\bitem\\(\\s*\"([^\"]+)\"");

    @Test
    @DisplayName("every key the code asks for is defined in the English catalogue")
    void noMissingKeys() throws IOException {
        Set<String> used     = keysUsedInSources();
        Set<String> defined  = englishKeys();

        assertTrue(used.size() > 50, "the scan found suspiciously few keys: " + used.size());

        Set<String> missing = new TreeSet<>(used);
        missing.removeAll(defined);
        assertEquals(Set.of(), missing,
                "these keys are referenced in the UI but missing from ui_EN.properties — "
                        + "they would render as the key itself");
    }

    @Test
    @DisplayName("a key defined in English is defined in all 35 catalogues")
    void everyCatalogueCoversEnglish() {
        Set<String> english = englishKeys();
        for (GaiaConstants.Language lang : GaiaConstants.Language.values()) {
            Set<String> keys = keys(lang);
            Set<String> missing = new TreeSet<>(english);
            missing.removeAll(keys);
            assertEquals(Set.of(), missing, "keys missing from the " + lang + " catalogue");
        }
    }

    // ------------------------------------------------------------------ helpers

    private static Set<String> keysUsedInSources() throws IOException {
        Path sources = sourceRoot();
        try (Stream<Path> files = Files.walk(sources)) {
            return files.filter(p -> p.toString().endsWith(".java"))
                    .flatMap(CatalogueCoverageTest::keysIn)
                    .collect(Collectors.toCollection(TreeSet::new));
        }
    }

    private static Stream<String> keysIn(Path file) {
        String src;
        try {
            src = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        Matcher m = KEY_AT_CALL_SITE.matcher(src);
        List<String> keys = new java.util.ArrayList<>();
        while (m.find()) {
            keys.add(m.group(1) != null ? m.group(1) : m.group(2));
        }
        return keys.stream();
    }

    /** The module's main sources, whether tests run from the module dir or the repo root. */
    private static Path sourceRoot() {
        for (String candidate : new String[] {"src/main/java", "gaia-desktop/src/main/java"}) {
            Path p = Paths.get(candidate);
            if (Files.isDirectory(p)) return p;
        }
        throw new AssertionError("could not find the module sources from " + Paths.get("").toAbsolutePath());
    }

    private static Set<String> englishKeys() {
        return keys(GaiaConstants.Language.ENGLISH);
    }

    private static Set<String> keys(GaiaConstants.Language lang) {
        Properties p = new Properties();
        String path = "/i18n/ui_" + tools.pantheum.gaia.desktop.parse.Languages.code(lang) + ".properties";
        try (InputStream in = CatalogueCoverageTest.class.getResourceAsStream(path)) {
            assertNotNull(in, "missing catalogue: " + path);
            try (Reader r = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                p.load(r);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return new TreeSet<>(p.stringPropertyNames());
    }
}
