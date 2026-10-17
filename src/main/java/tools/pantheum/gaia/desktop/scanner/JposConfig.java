package tools.pantheum.gaia.desktop.scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Points JavaPOS at the {@code jpos.xml} bundled in the jar, before the first control opens.
 *
 * <p>With the Maven JavaPOS jars ({@code javapos-config-loader} 4.x) and no
 * {@code jpos/res/jpos.properties} on the class path, three things go wrong, each on its own
 * enough to make every {@code open()} fail with <i>"Service does not exist in loaded JCL
 * registry"</i>:
 * <ol>
 *   <li><b>The loader.</b> Without {@code jpos.config.regPopulatorClass} the service manager falls
 *       back to {@code SimpleRegPopulator}, which reads a serialized {@code jpos.cfg} and never
 *       looks at {@code jpos.xml}.</li>
 *   <li><b>The file.</b> The XML loader opens {@code jpos.config.populatorFile} relative to the
 *       working directory only, not from the class path, so the copy inside the jar is not found
 *       unless the app happens to start in a directory that holds one.</li>
 *   <li><b>The parser.</b> The loader restricts DTD access through a JAXP property that Xerces
 *       2.12.2 — in {@code lib/} for the Zebra SDK's older loader — rejects
 *       ({@code SAXNotRecognizedException: accessExternalDTD}), and with Xerces on the class path
 *       JAXP picks it by default.</li>
 * </ol>
 * So this sets the loader, the bundled file's URL ({@code jar:file:…!/jpos.xml}) and the JDK's
 * own SAX parser as system properties, which the loader reads as well as its properties file.
 *
 * <p>Only then, though. Each property already set — on the command line, say, to use another
 * {@code jpos.xml} — is left as it is. And nothing is set when a {@code jpos.properties} is on the
 * class path or the 4.x loader is not, on the reading that someone else then owns the
 * configuration.
 *
 * <p>The Zebra Scanner SDK's own JavaPOS jar, {@code javaPOS114.jar}, is the case that rule was
 * written for, and it does not work out: the jar carries a {@code jpos.properties}, so this steps
 * aside, but the manifest {@code Class-Path} still puts the 4.x loader ahead of it, and the
 * jar's {@code jcl.xsd} accepts JavaPOS versions only up to 1.10. The bundled {@code jpos.xml}
 * declares 1.12–1.14, so every entry is rejected and every open fails with error 109 (and with no
 * file named at all, the loader looks for a {@code jpos.xml} in the working directory, where there
 * usually is none). {@code run.bat} therefore never puts that jar on
 * the class path: the JavaPOS API comes from {@code lib\}, and the SDK supplies only its service
 * jars and natives.
 */
final class JposConfig {

    private static final Logger log = LoggerFactory.getLogger(JposConfig.class);

    static final String JPOS_PROPERTIES     = "jpos/res/jpos.properties";
    static final String BUNDLED_JPOS_XML    = "jpos.xml";

    static final String POPULATOR_CLASS     = "jpos.config.regPopulatorClass";
    static final String POPULATOR_FILE      = "jpos.config.populatorFile";
    static final String POPULATOR_FILE_URL  = "jpos.config.populatorFileURL";
    static final String SAX_PARSER_FACTORY  = "javax.xml.parsers.SAXParserFactory";

    /** javapos-config-loader 4.x's JAXP-based XML loader; absent from the SDK's javaPOS114.jar. */
    static final String JAVAX_POPULATOR     = "jpos.config.simple.xml.JavaxRegPopulator";
    /** The JDK's built-in SAX parser, which accepts the JAXP access restrictions. */
    static final String JDK_SAX_PARSER      = "com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl";

    private static boolean applied;

    private JposConfig() {}

    /**
     * Applies {@link #settingsFor} to the system properties, once per JVM. Must run before
     * anything touches {@code jpos.loader.JposServiceLoader}, which reads its configuration a
     * single time, when its class initializes.
     */
    static synchronized void apply() {
        if (applied) return;
        applied = true;

        ClassLoader loader = JposConfig.class.getClassLoader();
        Map<String, String> settings = settingsFor(
                loader.getResource(JPOS_PROPERTIES) != null,
                isPresent(JAVAX_POPULATOR, loader),
                loader.getResource(BUNDLED_JPOS_XML),
                System.getProperties());

        if (settings.isEmpty()) {
            log.info("JavaPOS configuration left to {}", loader.getResource(JPOS_PROPERTIES) != null
                    ? JPOS_PROPERTIES + " on the class path" : "the command line");
            return;
        }
        settings.forEach(System::setProperty);
        log.info("JavaPOS configured: {}", settings);
    }

    /**
     * The system properties to set, given what is on the class path and what is already set.
     * Empty when the configuration belongs to someone else.
     *
     * @param hasJposProperties whether a {@code jpos/res/jpos.properties} is on the class path
     * @param hasJavaxPopulator whether javapos-config-loader 4.x's XML loader is on the class path
     * @param bundledJposXml    the bundled {@code jpos.xml}, or {@code null} if there is none
     * @param current           the system properties as they stand
     */
    static Map<String, String> settingsFor(boolean hasJposProperties, boolean hasJavaxPopulator,
                                           URL bundledJposXml, Properties current) {
        Map<String, String> settings = new LinkedHashMap<>();
        if (hasJposProperties || !hasJavaxPopulator) return settings;

        if (!current.containsKey(POPULATOR_CLASS)) {
            settings.put(POPULATOR_CLASS, JAVAX_POPULATOR);
        }
        // A file or URL named on the command line wins over the bundled copy.
        if (bundledJposXml != null
                && !current.containsKey(POPULATOR_FILE)
                && !current.containsKey(POPULATOR_FILE_URL)) {
            settings.put(POPULATOR_FILE_URL, bundledJposXml.toExternalForm());
        }
        if (!current.containsKey(SAX_PARSER_FACTORY)) {
            settings.put(SAX_PARSER_FACTORY, JDK_SAX_PARSER);
        }
        return settings;
    }

    private static boolean isPresent(String className, ClassLoader loader) {
        try {
            Class.forName(className, false, loader);
            return true;
        } catch (ClassNotFoundException | LinkageError e) {
            return false;
        }
    }
}
