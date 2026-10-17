package tools.pantheum.gaia.desktop.update;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import tools.pantheum.gaia.desktop.GaiaDesktopVersion;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/**
 * Asks GitHub whether a newer GAIA Desktop has been released.
 *
 * <p>One request, to GitHub's <i>latest release</i> endpoint, which returns the newest
 * published release and never a draft or a pre-release. Nothing is downloaded and nothing is
 * installed: the caller only learns the version and the page to send the user to. The app is
 * unsigned, and a download the user starts from the release page is one they can see.
 *
 * <p>No API token is needed — the repository is public — so the request carries nothing about
 * the user but what any HTTPS request does (their address, and a {@code User-Agent} naming the
 * app version, which GitHub requires). It is subject to GitHub's limit for anonymous requests,
 * 60 an hour per address, which is why the window checks at most once a day.
 *
 * <p>JavaFX-free and blocking: call {@link #fetchLatest()} off the FX thread. Every failure —
 * offline, a proxy refusing it, the rate limit — comes back as an exception for the caller to
 * log, never as a release. A repository with no release yet is not a failure: GitHub answers
 * 404, and that reads as "nothing newer".
 */
public final class UpdateChecker {

    /** The repository's releases, as people see them. */
    public static final String RELEASES_PAGE = "https://github.com/mgsmith-parsers/gaia-desktop/releases";

    /** GitHub's REST endpoint for the newest published, non-pre-release release. */
    static final String LATEST_RELEASE_API = "https://api.github.com/repos/mgsmith-parsers/gaia-desktop/releases/latest";

    /** How often the window may check on its own. */
    public static final Duration CHECK_INTERVAL = Duration.ofHours(24);

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);

    private static final ObjectMapper JSON = new ObjectMapper();

    /** A published release: its version (the tag without a leading {@code v}) and its page. */
    public record Release(String version, String pageUrl) {}

    private final HttpClient http;

    public UpdateChecker() {
        // NORMAL redirects: GitHub answers a renamed repository with a redirect.
        this(HttpClient.newBuilder()
                .connectTimeout(CONNECT_TIMEOUT)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build());
    }

    UpdateChecker(HttpClient http) {
        this.http = http;
    }

    /**
     * The newest published release, or empty when there is none — GitHub's 404 while the
     * repository has no release yet — or when GitHub reports one that cannot be used: a draft
     * or pre-release (the endpoint should not return either), or one without a version.
     *
     * @throws IOException when GitHub cannot be reached or answers anything but 200 or 404
     */
    public Optional<Release> fetchLatest() throws IOException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(LATEST_RELEASE_API))
                .timeout(REQUEST_TIMEOUT)
                .header("Accept", "application/vnd.github+json")
                .header("X-GitHub-Api-Version", "2022-11-28")
                .header("User-Agent", "GAIA-Desktop/" + GaiaDesktopVersion.VERSION)
                .GET()
                .build();
        HttpResponse<String> response;
        try {
            response = http.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("interrupted while checking for updates", e);
        }
        if (response.statusCode() == 404) {
            return Optional.empty();   // no release published yet
        }
        if (response.statusCode() != 200) {
            throw new IOException("GitHub answered HTTP " + response.statusCode());
        }
        return parse(response.body());
    }

    /** Reads GitHub's release document; see {@link #fetchLatest()}. */
    static Optional<Release> parse(String json) throws IOException {
        JsonNode release = JSON.readTree(json);
        if (release == null || release.path("draft").asBoolean(false) || release.path("prerelease").asBoolean(false)) {
            return Optional.empty();
        }
        String tag = release.path("tag_name").asText("").trim();
        String version = stripV(tag);
        if (version.isEmpty() || parts(version) == null) {
            return Optional.empty();
        }
        String page = release.path("html_url").asText("").trim();
        return Optional.of(new Release(version, page.startsWith("https://") ? page : RELEASES_PAGE));
    }

    /**
     * Whether {@code candidate} is a newer version than {@code current}. False whenever either
     * cannot be read — in particular for a development build, whose version is {@code unknown}:
     * a build from source is not out of date, and must not be told it is.
     */
    public static boolean isNewer(String candidate, String current) {
        int[] a = parts(stripV(candidate));
        int[] b = parts(stripV(current));
        if (a == null || b == null) return false;
        for (int i = 0; i < Math.max(a.length, b.length); i++) {
            int x = i < a.length ? a[i] : 0;
            int y = i < b.length ? b[i] : 0;
            if (x != y) return x > y;
        }
        return false;
    }

    /**
     * Whether {@code version} is a release number at all. A development build's is
     * {@code unknown}, and nothing can be newer than that — so it never asks GitHub.
     */
    public static boolean isRelease(String version) {
        return parts(stripV(version)) != null;
    }

    /** Whether an automatic check is due, {@link #CHECK_INTERVAL} after the last one. */
    public static boolean isDue(Instant lastCheck, Instant now) {
        return lastCheck == null || !now.isBefore(lastCheck.plus(CHECK_INTERVAL));
    }

    /** {@code v1.2.3} → {@code 1.2.3}; tags are written either way. */
    static String stripV(String version) {
        if (version == null) return "";
        String v = version.trim();
        return v.startsWith("v") || v.startsWith("V") ? v.substring(1) : v;
    }

    /**
     * The numeric parts of a release version — {@code 1.0.10} → {@code [1, 0, 10]} — or null
     * when it is not one. Only plain numbers count: {@code 1.1.0-SNAPSHOT} or {@code 2.0-rc1}
     * are not releases anyone should be sent to, and {@code unknown} is not a version at all.
     */
    static int[] parts(String version) {
        if (version == null || !version.matches("\\d+(\\.\\d+)*")) return null;
        String[] raw = version.split("\\.");
        int[] numbers = new int[raw.length];
        try {
            for (int i = 0; i < raw.length; i++) numbers[i] = Integer.parseInt(raw[i]);
        } catch (NumberFormatException e) {
            return null;   // a part too long for an int is not a real version either
        }
        return numbers;
    }
}
