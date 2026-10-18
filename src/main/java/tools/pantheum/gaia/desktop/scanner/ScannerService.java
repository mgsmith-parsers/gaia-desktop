package tools.pantheum.gaia.desktop.scanner;

import jpos.JposConst;
import jpos.JposException;
import jpos.Scanner;
import jpos.config.JposEntry;
import jpos.config.JposEntryRegistry;
import jpos.events.DataEvent;
import jpos.events.DataListener;
import jpos.events.ErrorEvent;
import jpos.events.ErrorListener;
import jpos.events.StatusUpdateEvent;
import jpos.events.StatusUpdateListener;
import jpos.loader.JposServiceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Owns the JavaPOS {@link Scanner} control and turns its events into {@link ScannerListener}
 * callbacks. The device wiring is the standard JavaPOS sequence — open, claim, enable, then
 * re-arm the data event after every scan — but nothing here parses, prints, or writes a
 * report: the desktop app decides what to do with a scan.
 *
 * <p>The default logical device name {@code ZebraAllScanners} is resolved via the
 * {@code jpos.xml} bundled in the jar, which is also where the USB product ID for a specific
 * scanner model is configured. {@link JposConfig} points JavaPOS at it before the first open —
 * left alone, the Maven JavaPOS loader reads neither that file nor any other.
 *
 * <p><b>Windows only.</b> The services behind those logical names are Zebra's, and Zebra
 * ships them for Windows — see {@link #isSupportedPlatform()}, which the window asks before
 * it puts any scanner control on screen.
 *
 * <p>Nothing here composes text for the user: events go out as {@link ScannerListener.Event}
 * constants and the values that vary, and the UI supplies the wording from its catalogue. Log
 * lines are the exception — they are for whoever reads the log, and stay English.
 *
 * <p><b>Threading.</b> Every call into the device — open, claim, enable, and their reverse —
 * is made on this service's own thread, {@code gaia-scanner}, which lives as long as the
 * service: never on the JavaFX application thread, and never on a thread that ends after the
 * call. Zebra's service reaches the scanner through a JNI layer over the CoreScanner COM API, and
 * COM ties what it sets up to the apartment of the thread that called it: a connection opened on
 * a thread that then exits loses that apartment (the scanner claims, then stays silent), and a
 * close arriving on another thread uninitializes the wrong one. On the FX thread that second
 * mistake is worse still — JavaFX calls {@code OleInitialize} there at startup and registers
 * every window's drop target against it, so a {@code CoUninitialize} on it revokes the app's
 * drag and drop. {@link #connect} and {@link #disconnect} hand their work to that thread and wait for
 * it, so call them off the FX thread when the wait matters; {@code connect} can take as long as
 * the claim. Listener callbacks arrive on the JavaPOS event thread, or on {@code gaia-scanner}
 * for connect and disconnect; see {@link ScannerListener}.
 */
public final class ScannerService {

    private static final Logger log = LoggerFactory.getLogger(ScannerService.class);

    /** The logical device name used when the user does not pick another. */
    public static final String DEFAULT_LOGICAL_NAME = "ZebraAllScanners";

    /** The {@code jpos.xml} device category of the entries the picker offers; scales and the rest are left out. */
    static final String SCANNER_CATEGORY = "Scanner";

    private static final int CLAIM_TIMEOUT_MS = 5_000;

    /** How long a caller waits for a device call to come back before giving up on it. */
    private static final long CALL_TIMEOUT_SECONDS = 30;

    /**
     * Whether this machine can drive a scanner at all.
     *
     * <p>Zebra ships its JavaPOS services for <b>Windows only</b>. Nothing fails early
     * elsewhere — the jars resolve, the {@link Scanner} control constructs, and it is
     * {@code open()} that finally fails on the missing native layer, several seconds and one
     * dialog later. So the window asks this first and leaves the scanner controls out
     * altogether rather than offering a button that cannot work.
     *
     * <p>This is about the <em>services</em>, not about JavaPOS: a vendor shipping a
     * cross-platform service would work here, and widening the rule is then a change to
     * {@link #supportsJavaPos(String)} alone.
     */
    public static boolean isSupportedPlatform() {
        return supportsJavaPos(System.getProperty("os.name"));
    }

    /** {@link #isSupportedPlatform()} against a caller-supplied {@code os.name}, so it can be tested. */
    static boolean supportsJavaPos(String osName) {
        return osName != null && osName.toLowerCase(Locale.ROOT).startsWith("windows");
    }

    private final ScannerListener listener;

    /**
     * The one thread every call into the device is made on; see the class comment. A daemon,
     * so a thread of Zebra's that never ends cannot hold the process open, and created with the
     * service rather than per connection, so a disconnect and the next connect share it too.
     */
    private final ExecutorService deviceThread = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "gaia-scanner");
        thread.setDaemon(true);
        return thread;
    });

    private Scanner scanner;      // touched on deviceThread only; non-null only while connected
    // Read from the FX and JavaPOS event threads, which must not wait behind a device call.
    private volatile String  logicalName;
    private volatile boolean connected;

    public ScannerService(ScannerListener listener) {
        this.listener = listener;
    }

    /** Whether a device is currently open, claimed, and enabled. */
    public boolean isConnected() {
        return connected;
    }

    /** The logical device name of the current connection, or {@code null} when disconnected. */
    public String logicalName() {
        return logicalName;
    }

    /**
     * Opens, claims, and enables the named device, then subscribes to its events. Runs on the
     * device thread and waits for it — up to the claim timeout, and longer if the service is
     * slow to open — so call it off the FX thread.
     *
     * @throws JposException when the device cannot be opened, claimed, or enabled — the
     *         service is left disconnected and any half-open control is closed — or when the
     *         device thread does not answer in time
     */
    public void connect(String name) throws JposException {
        onDeviceThread("connect", () -> {
            open(name);
            return null;
        });
    }

    /**
     * Disables, releases, and closes the device. A no-op when already disconnected. Runs on the
     * device thread — the one that opened it — and waits for it.
     */
    public void disconnect() {
        try {
            onDeviceThread("disconnect", () -> {
                close();
                return null;
            });
        } catch (JposException e) {
            log.warn("Scanner did not disconnect cleanly: {}", e.getMessage());
        }
    }

    /**
     * The logical names of the scanners JavaPOS loaded from its configuration — the bundled
     * {@code jpos.xml}, or whichever file {@code jpos.config.populatorFileURL} names — for the
     * device picker, in {@link #pickerOrder}. Never empty: if nothing loads, the default alone, so
     * the picker always offers something and a connect can say what is wrong.
     *
     * <p>Reads the registry on the device thread, like every other JavaPOS call, and waits for
     * it — call it off the FX thread, which a connect in progress would otherwise stall.
     */
    public List<String> logicalNames() {
        try {
            return onDeviceThread("list devices", () -> {
                JposConfig.apply();
                JposEntryRegistry registry = JposServiceLoader.getManager().getEntryRegistry();
                return pickerOrder(scannerNames(registry.getEntries()));
            });
        } catch (JposException | RuntimeException | LinkageError e) {
            log.warn("Could not list the configured scanners, offering {} only: {}", DEFAULT_LOGICAL_NAME, e.toString());
            return List.of(DEFAULT_LOGICAL_NAME);
        }
    }

    /** The logical names of the entries whose device category is {@link #SCANNER_CATEGORY}. */
    static List<String> scannerNames(Enumeration<?> entries) {
        List<String> names = new ArrayList<>();
        while (entries.hasMoreElements()) {
            Object next = entries.nextElement();
            if (!(next instanceof JposEntry)) continue;
            JposEntry entry = (JposEntry) next;
            if (SCANNER_CATEGORY.equals(entry.getPropertyValue(JposEntry.DEVICE_CATEGORY_PROP_NAME))
                    && entry.getLogicalName() != null) {
                names.add(entry.getLogicalName());
            }
        }
        return names;
    }

    /**
     * The order the picker shows: {@link #DEFAULT_LOGICAL_NAME} first when the configuration
     * defines it, then the rest alphabetically — the registry does not keep the file's order. The
     * default is not added to a configuration that lacks it, where it would name a device that
     * cannot be opened; with no names at all it is the one entry, so the picker is never empty.
     */
    static List<String> pickerOrder(Collection<String> names) {
        if (names.isEmpty()) return List.of(DEFAULT_LOGICAL_NAME);
        TreeSet<String> rest = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        rest.addAll(names);
        List<String> ordered = new ArrayList<>(rest.size());
        if (rest.remove(DEFAULT_LOGICAL_NAME)) ordered.add(DEFAULT_LOGICAL_NAME);
        ordered.addAll(rest);
        return List.copyOf(ordered);
    }

    /**
     * Runs {@code call} on {@link #deviceThread} and waits for it, handing back a
     * {@link JposException} it threw as itself.
     */
    private <T> T onDeviceThread(String what, Callable<T> call) throws JposException {
        try {
            return deviceThread.submit(call).get(CALL_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof JposException) throw (JposException) cause;
            if (cause instanceof RuntimeException) throw (RuntimeException) cause;
            if (cause instanceof Error) throw (Error) cause;
            throw new JposException(JposConst.JPOS_E_FAILURE, "Scanner " + what + " failed", (Exception) cause);
        } catch (TimeoutException e) {
            throw new JposException(JposConst.JPOS_E_TIMEOUT,
                    "Scanner " + what + " did not finish within " + CALL_TIMEOUT_SECONDS + " s");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new JposException(JposConst.JPOS_E_FAILURE, "Interrupted waiting for scanner " + what);
        }
    }

    /** The open – claim – enable sequence. On {@link #deviceThread} only. */
    private void open(String name) throws JposException {
        if (scanner != null) return;

        String resolved = (name == null || name.trim().isEmpty()) ? DEFAULT_LOGICAL_NAME : name.trim();
        log.info("Connecting scanner (logicalName='{}')", resolved);

        // Before the first open: JavaPOS reads its configuration once, when it first loads.
        JposConfig.apply();

        Scanner s = new Scanner();
        try {
            s.open(resolved);
            s.claim(CLAIM_TIMEOUT_MS);
            s.setDeviceEnabled(true);
            s.setDecodeData(true);
            s.setDataEventEnabled(true);

            // Publish the connected state *before* subscribing. A device that fires a status
            // update the moment it is listened to would otherwise be reported with
            // isConnected() still false, and the UI would relabel its button back to
            // "connect" mid-connect until the line below corrected it.
            this.scanner     = s;
            this.logicalName = resolved;
            this.connected   = true;

            s.addDataListener(new ScanDataListener(s));
            s.addErrorListener(new ScanErrorListener());
            s.addStatusUpdateListener(new ScanStatusListener());
        } catch (JposException e) {
            // Never leave a partially-opened control behind — the next connect would fail
            // with "already open" rather than the real reason.
            this.scanner     = null;
            this.logicalName = null;
            this.connected   = false;
            closeQuietly(s);
            log.error("Scanner error [{}]: {}", e.getErrorCode(), e.getMessage(), e);
            throw e;
        }

        listener.onStatus(ScannerListener.Event.CONNECTED, true, resolved);
    }

    /** The disable – release – close sequence. On {@link #deviceThread} only. */
    private void close() {
        if (scanner == null) return;
        log.info("Disconnecting scanner (logicalName='{}')", logicalName);
        connected = false;
        closeQuietly(scanner);
        scanner     = null;
        logicalName = null;
        listener.onStatus(ScannerListener.Event.DISCONNECTED, false);
    }

    /**
     * Disable – release – close, each step on its own, so one that fails cannot skip the rest.
     * The steps that need a claim are taken only when there is one: after a failed claim,
     * {@code release()} throws "not claimed", and with all three in one {@code try} that used to
     * skip {@code close()} and leave the device open in Zebra's service, where the next connect
     * to it could fail. {@code close()} is always attempted on a control that is not closed —
     * per UPOS it also releases and disables whatever is left.
     */
    static void closeQuietly(Scanner s) {
        if (s.getState() == JposConst.JPOS_S_CLOSED) return;

        boolean claimed = quietly("check the claim of", s::getClaimed, false);
        if (claimed) {
            quietly("disable", () -> {
                if (s.getDeviceEnabled()) s.setDeviceEnabled(false);
                return null;
            }, null);
            quietly("release", () -> {
                s.release();
                return null;
            }, null);
        }
        quietly("close", () -> {
            s.close();
            return null;
        }, null);
    }

    /** A device call that may throw {@link JposException}. */
    @FunctionalInterface
    private interface JposCall<T> {
        T call() throws JposException;
    }

    /** Runs one step of a teardown, logging rather than throwing when it fails. */
    private static <T> T quietly(String what, JposCall<T> call, T fallback) {
        try {
            return call.call();
        } catch (JposException e) {
            log.warn("Could not {} scanner: {}", what, e.getMessage());
            return fallback;
        }
    }

    // -------------------------------------------------------------------------
    // Listeners
    // -------------------------------------------------------------------------

    final class ScanDataListener implements DataListener {

        private final Scanner scanner;

        ScanDataListener(Scanner scanner) {
            this.scanner = scanner;
        }

        @Override
        public void dataOccurred(DataEvent event) {
            try {
                // getScanData() returns the full transmitted barcode, preserving any AIM
                // Code ID prefix and the GS (0x1D) separators that variable-length GS1 AIs
                // depend on. ISO-8859-1 maps each byte 1:1 so 0x1D survives decoding.
                byte[] raw  = scanner.getScanData();
                int    type = scanner.getScanDataType();

                String barcode   = new String(raw, StandardCharsets.ISO_8859_1);
                String symbology = Symbologies.name(type);
                log.info("Scan: symbology={} (type={}, carrier={}) data={}",
                        symbology, type, Symbologies.carrierType(type), barcode);

                listener.onScan(barcode, symbology);

            } catch (JposException e) {
                log.error("Failed to read scan data", e);
                listener.onError(ScannerListener.Event.READ_FAILED, e.getMessage());
            } catch (RuntimeException e) {
                // Never let a listener bug stop the scanner.
                log.error("Scan handler failed", e);
                listener.onError(ScannerListener.Event.HANDLER_FAILED, e.toString());
            } finally {
                // JavaPOS disables data events after each one, so re-arm whatever happened above:
                // a read that failed must not leave the scanner silent until a reconnect.
                rearm();
            }
        }

        private void rearm() {
            try {
                scanner.setDataEventEnabled(true);
            } catch (JposException e) {
                // Expected when the event raced a disconnect and the control is already closed.
                log.debug("Could not re-arm the scanner's data event: {}", e.getMessage());
            }
        }
    }

    private final class ScanErrorListener implements ErrorListener {
        @Override
        public void errorOccurred(ErrorEvent event) {
            log.error("Scanner error: errorCode={} errorCodeExtended={}",
                    event.getErrorCode(), event.getErrorCodeExtended());
            listener.onError(ScannerListener.Event.DEVICE_ERROR,
                    String.valueOf(event.getErrorCode()), String.valueOf(event.getErrorCodeExtended()));
            event.setErrorResponse(JposConst.JPOS_ER_CONTINUEINPUT);
        }
    }

    private final class ScanStatusListener implements StatusUpdateListener {
        @Override
        public void statusUpdateOccurred(StatusUpdateEvent event) {
            log.info("Scanner status update: {}", event.getStatus());
            listener.onStatus(ScannerListener.Event.DEVICE_STATUS, isConnected(),
                    String.valueOf(event.getStatus()));
        }
    }
}
