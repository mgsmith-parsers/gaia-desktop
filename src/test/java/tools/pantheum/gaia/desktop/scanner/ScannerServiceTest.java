package tools.pantheum.gaia.desktop.scanner;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import jpos.JposException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link ScannerService}'s platform rule — the one part of the service that can be
 * exercised without a device attached.
 *
 * <p>The rule decides whether the window builds any scanner control at all, so getting it wrong
 * is visible in two opposite ways: a dead button on macOS and Linux, or no way to scan on
 * Windows. It is tested against the {@code os.name} string rather than the live platform so
 * both answers are checked on whichever machine runs the build.
 */
@DisplayName("ScannerService")
class ScannerServiceTest {

    @ParameterizedTest(name = "supported on \"{0}\"")
    @ValueSource(strings = {"Windows 10", "Windows 11", "Windows Server 2019", "windows 7"})
    @DisplayName("Windows carries Zebra's JavaPOS services")
    void windowsIsSupported(String osName) {
        assertTrue(ScannerService.supportsJavaPos(osName), osName);
    }

    @ParameterizedTest(name = "not supported on \"{0}\"")
    @ValueSource(strings = {"Mac OS X", "macOS", "Linux", "FreeBSD", "SunOS", "AIX", ""})
    @DisplayName("everywhere else has no service to open")
    void everythingElseIsNot(String osName) {
        assertFalse(ScannerService.supportsJavaPos(osName), osName);
    }

    @Test
    @DisplayName("an absent os.name is treated as unsupported, not as a crash")
    void nullIsUnsupported() {
        assertFalse(ScannerService.supportsJavaPos(null));
    }

    @Test
    @DisplayName("the live check agrees with the running platform")
    void liveCheckMatchesThisMachine() {
        assertEquals(ScannerService.supportsJavaPos(System.getProperty("os.name")),
                ScannerService.isSupportedPlatform());
    }

    @Test
    @DisplayName("the picker lists the default first, then the rest alphabetically")
    void pickerOrderPutsTheDefaultFirst() {
        assertEquals(List.of("ZebraAllScanners", "ZebraAllScanners114", "ZebraScannerUSB", "ZebraVDI"),
                ScannerService.pickerOrder(List.of("ZebraVDI", "ZebraScannerUSB", "ZebraAllScanners", "ZebraAllScanners114")));
    }

    @Test
    @DisplayName("a configuration without the default is not given one it cannot open")
    void pickerOrderDoesNotInventTheDefault() {
        assertEquals(List.of("MyCounterScanner", "MyHandheld"),
                ScannerService.pickerOrder(List.of("MyHandheld", "MyCounterScanner")));
    }

    @Test
    @DisplayName("with nothing configured the picker still offers the default")
    void pickerOrderIsNeverEmpty() {
        assertEquals(List.of(ScannerService.DEFAULT_LOGICAL_NAME), ScannerService.pickerOrder(List.of()));
    }

    @Test
    @DisplayName("a failed connect comes back as the JposException itself, and leaves it disconnected")
    void failedConnectSurfacesTheJposException() {
        // No device of this name exists anywhere — and on macOS or Linux no service at all — so
        // open() fails on the device thread. The caller must see JavaPOS's own exception, not an
        // ExecutionException from the hand-off, because that is what the window reports.
        List<String> statuses = new ArrayList<>();
        ScannerService service = new ScannerService(new ScannerListener() {
            @Override public void onScan(String barcode, String symbology) { }
            @Override public void onStatus(Event event, boolean connected, String... details) { statuses.add(event.name()); }
            @Override public void onError(Event event, String... details) { }
        });

        assertThrows(JposException.class, () -> service.connect("GaiaNoSuchScanner"));
        assertFalse(service.isConnected());
        assertNull(service.logicalName());

        service.disconnect();   // nothing open: a no-op, and no DISCONNECTED for a connect that never happened
        assertTrue(statuses.isEmpty(), statuses.toString());
    }
}
