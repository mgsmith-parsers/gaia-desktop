package tools.pantheum.gaia.desktop.scanner;

import jpos.JposConst;
import jpos.JposException;
import jpos.Scanner;
import jpos.events.DataEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The two parts of {@link ScannerService} that must hold up when the device misbehaves — the
 * teardown, and the re-arm after a scan — driven against a scripted {@link Scanner} rather than
 * a real one, since Zebra's services exist only on Windows with a scanner attached.
 */
@DisplayName("ScannerService lifecycle")
class ScannerLifecycleTest {

    /** A {@link Scanner} with no service behind it: records each call, and fails the ones it is told to. */
    private static final class ScriptedScanner extends Scanner {

        final List<String> calls = new ArrayList<>();
        int     state    = JposConst.JPOS_S_IDLE;
        boolean claimed  = true;
        boolean enabled  = true;
        Set<String> failing = Set.of();
        byte[]  scanData = new byte[0];

        private void step(String name) throws JposException {
            calls.add(name);
            if (failing.contains(name)) throw new JposException(JposConst.JPOS_E_FAILURE, name + " failed");
        }

        @Override public int getState() { return state; }
        @Override public boolean getClaimed() throws JposException { step("getClaimed"); return claimed; }
        @Override public boolean getDeviceEnabled() throws JposException { step("getDeviceEnabled"); return enabled; }
        @Override public void setDeviceEnabled(boolean on) throws JposException { step("setDeviceEnabled(" + on + ")"); }
        @Override public void release() throws JposException { step("release"); }
        @Override public synchronized void close() throws JposException { step("close"); }
        @Override public byte[] getScanData() throws JposException { step("getScanData"); return scanData; }
        @Override public int getScanDataType() throws JposException { step("getScanDataType"); return 0; }
        @Override public void setDataEventEnabled(boolean on) throws JposException { step("setDataEventEnabled(" + on + ")"); }
    }

    /** Records what the service tells the window. */
    private static final class RecordingListener implements ScannerListener {
        final List<String> events = new ArrayList<>();
        boolean throwOnScan;

        @Override public void onScan(String barcode, String symbology) {
            events.add("scan:" + barcode);
            if (throwOnScan) throw new IllegalStateException("window bug");
        }
        @Override public void onStatus(Event event, boolean connected, String... details) { events.add("status:" + event); }
        @Override public void onError(Event event, String... details) { events.add("error:" + event); }
    }

    // ---------------------------------------------------------------- teardown

    @Test
    @DisplayName("a claimed, enabled device is disabled, released and closed, in that order")
    void fullTeardown() {
        ScriptedScanner s = new ScriptedScanner();

        ScannerService.closeQuietly(s);

        assertEquals(List.of("getClaimed", "getDeviceEnabled", "setDeviceEnabled(false)", "release", "close"), s.calls);
    }

    @Test
    @DisplayName("after a failed claim it is still closed, and release is not attempted")
    void unclaimedIsStillClosed() {
        ScriptedScanner s = new ScriptedScanner();
        s.claimed = false;   // open succeeded, claim did not

        ScannerService.closeQuietly(s);

        assertEquals(List.of("getClaimed", "close"), s.calls,
                "the bug was release() throwing \"not claimed\" and skipping close(), leaving the device open");
    }

    @Test
    @DisplayName("a step that fails does not stop the ones after it")
    void failingStepsDoNotSkipClose() {
        ScriptedScanner s = new ScriptedScanner();
        s.failing = Set.of("setDeviceEnabled(false)", "release");

        ScannerService.closeQuietly(s);

        assertEquals(List.of("getClaimed", "getDeviceEnabled", "setDeviceEnabled(false)", "release", "close"), s.calls);
    }

    @Test
    @DisplayName("when the claim cannot even be read, close is still attempted")
    void unreadableClaimStillCloses() {
        ScriptedScanner s = new ScriptedScanner();
        s.failing = Set.of("getClaimed");

        ScannerService.closeQuietly(s);

        assertEquals(List.of("getClaimed", "close"), s.calls);
    }

    @Test
    @DisplayName("an already closed control is left alone")
    void closedIsLeftAlone() {
        ScriptedScanner s = new ScriptedScanner();
        s.state = JposConst.JPOS_S_CLOSED;

        ScannerService.closeQuietly(s);

        assertTrue(s.calls.isEmpty(), s.calls.toString());
    }

    // ---------------------------------------------------------------- re-arm after a scan

    private static void scan(ScannerService service, ScriptedScanner s) {
        service.new ScanDataListener(s).dataOccurred(new DataEvent(s, 0));
    }

    @Test
    @DisplayName("a good read is delivered byte for byte, and the next one is armed")
    void goodReadRearms() {
        RecordingListener window = new RecordingListener();
        ScannerService service = new ScannerService(window);
        ScriptedScanner s = new ScriptedScanner();
        s.scanData = ("]C1" + "10" + "LOT1" + "\u001D" + "21" + "X").getBytes(StandardCharsets.ISO_8859_1);

        scan(service, s);

        assertEquals(List.of("scan:]C110LOT1\u001D21X"), window.events);
        assertEquals("setDataEventEnabled(true)", s.calls.get(s.calls.size() - 1));
    }

    @Test
    @DisplayName("a read that fails still arms the next one, so the scanner does not go silent")
    void failedReadStillRearms() {
        RecordingListener window = new RecordingListener();
        ScannerService service = new ScannerService(window);
        ScriptedScanner s = new ScriptedScanner();
        s.failing = Set.of("getScanData");

        scan(service, s);

        assertEquals(List.of("error:READ_FAILED"), window.events);
        assertEquals(List.of("getScanData", "setDataEventEnabled(true)"), s.calls);
    }

    @Test
    @DisplayName("a failure in the window's handler still arms the next read")
    void handlerFailureStillRearms() {
        RecordingListener window = new RecordingListener();
        window.throwOnScan = true;
        ScannerService service = new ScannerService(window);
        ScriptedScanner s = new ScriptedScanner();

        scan(service, s);

        assertEquals(List.of("scan:", "error:HANDLER_FAILED"), window.events);
        assertEquals("setDataEventEnabled(true)", s.calls.get(s.calls.size() - 1));
    }

    @Test
    @DisplayName("a re-arm that fails — the device closed under the event — is not an error")
    void rearmOnClosedDeviceIsQuiet() {
        RecordingListener window = new RecordingListener();
        ScannerService service = new ScannerService(window);
        ScriptedScanner s = new ScriptedScanner();
        s.failing = Set.of("setDataEventEnabled(true)");

        scan(service, s);

        assertEquals(List.of("scan:"), window.events, "the scan is delivered and nothing is reported");
    }
}
