package tools.pantheum.gaia.desktop.scanner;

/**
 * Callbacks from {@link ScannerService}.
 *
 * <p>Events are reported as an {@link Event} constant plus the values that vary — never as a
 * ready-made sentence. The scanner layer has no catalogue and no language: composing the text
 * here would put English in a window that is otherwise translated into 35 languages. The
 * listener owns the wording.
 *
 * <p><b>Threading:</b> every method is invoked on the JavaPOS event thread, or — for the
 * connected and disconnected statuses — on the service's {@code gaia-scanner} device thread;
 * never on the JavaFX application thread. Implementations that touch the scene graph must hop
 * across with {@code Platform.runLater}.
 */
public interface ScannerListener {

    /**
     * What happened. The {@code details} that accompany each constant are the values its
     * message needs, in order.
     */
    enum Event {
        /** The device is open, claimed and enabled. Detail: the logical device name. */
        CONNECTED,
        /** The device has been released and closed. No details. */
        DISCONNECTED,
        /** The device reported a status update. Detail: the JavaPOS status value. */
        DEVICE_STATUS,
        /** The device or the JavaPOS layer reported an error. Details: code, extended code. */
        DEVICE_ERROR,
        /** A scan arrived but its data could not be read. Detail: the JavaPOS message. */
        READ_FAILED,
        /** The listener itself threw while handling a scan. Detail: the exception. */
        HANDLER_FAILED
    }

    /**
     * A barcode was scanned.
     *
     * @param barcode   the transmitted data, decoded as ISO-8859-1 so the AIM Code ID prefix
     *                  and every GS (0x1D) separator survive byte-for-byte
     * @param symbology a human-readable symbology name, e.g. {@code "Data Matrix"}
     */
    void onScan(String barcode, String symbology);

    /**
     * The connection state changed, or the device reported a status update.
     *
     * @param event     which of those it was
     * @param connected whether a device is connected now
     * @param details   the values {@code event} carries, in the order its javadoc lists them
     */
    void onStatus(Event event, boolean connected, String... details);

    /**
     * The device, the JavaPOS layer, or a scan handler failed.
     *
     * @param event   which failure it was
     * @param details the values {@code event} carries, in the order its javadoc lists them
     */
    void onError(Event event, String... details);
}
