package tools.pantheum.gaia.desktop.scanner;

import jpos.loader.JposServiceLoader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link JposConfig}: when it configures JavaPOS and when it leaves it alone — and, end to end,
 * that the configuration it sets really loads the bundled {@code jpos.xml}.
 */
@DisplayName("JposConfig")
class JposConfigTest {

    private static final URL BUNDLED = JposConfigTest.class.getClassLoader().getResource("jpos.xml");

    @Test
    @DisplayName("with the Maven loader and nothing set, it sets the loader, the bundled file and the JDK parser")
    void configuresTheMavenLoader() {
        Map<String, String> settings = JposConfig.settingsFor(false, true, BUNDLED, new Properties());

        assertEquals(Map.of(
                JposConfig.POPULATOR_CLASS,    JposConfig.JAVAX_POPULATOR,
                JposConfig.POPULATOR_FILE_URL, BUNDLED.toExternalForm(),
                JposConfig.SAX_PARSER_FACTORY, JposConfig.JDK_SAX_PARSER), settings);
    }

    @Test
    @DisplayName("a jpos.properties on the class path — the Zebra SDK's javaPOS114.jar — is left in charge")
    void leavesAJposPropertiesAlone() {
        assertTrue(JposConfig.settingsFor(true, true, BUNDLED, new Properties()).isEmpty());
    }

    @Test
    @DisplayName("without the 4.x loader its class names would not resolve, so nothing is set")
    void leavesAnOlderLoaderAlone() {
        assertTrue(JposConfig.settingsFor(false, false, BUNDLED, new Properties()).isEmpty());
    }

    @Test
    @DisplayName("anything set on the command line wins, including another jpos.xml")
    void respectsWhatIsAlreadySet() {
        Properties current = new Properties();
        current.setProperty(JposConfig.POPULATOR_FILE_URL, "file:/opt/pos/jpos.xml");
        current.setProperty(JposConfig.SAX_PARSER_FACTORY, "some.other.Factory");

        Map<String, String> settings = JposConfig.settingsFor(false, true, BUNDLED, current);

        assertEquals(Map.of(JposConfig.POPULATOR_CLASS, JposConfig.JAVAX_POPULATOR), settings);
    }

    @Test
    @DisplayName("a populator file named on the command line also keeps the bundled URL out")
    void respectsAPopulatorFile() {
        Properties current = new Properties();
        current.setProperty(JposConfig.POPULATOR_FILE, "my-jpos.xml");

        Map<String, String> settings = JposConfig.settingsFor(false, true, BUNDLED, current);

        assertTrue(!settings.containsKey(JposConfig.POPULATOR_FILE_URL), settings.toString());
    }

    @Test
    @DisplayName("end to end: the picker lists exactly the bundled jpos.xml's scanners, scales left out")
    void pickerListsTheBundledScanners() throws Exception {
        // The class path here is the Maven one — javapos-config-loader 4.x and Xerces 2.12.2,
        // no jpos.properties — which is exactly the combination that loaded nothing before
        // JposConfig. logicalNames() applies it, then reads what JavaPOS loaded.
        List<String> picker = new ScannerService(new ScannerListener() {
            @Override public void onScan(String barcode, String symbology) { }
            @Override public void onStatus(Event event, boolean connected, String... details) { }
            @Override public void onError(Event event, String... details) { }
        }).logicalNames();

        List<String> inFile = new ArrayList<>();
        List<String> scales = new ArrayList<>();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        try (InputStream in = BUNDLED.openStream()) {
            NodeList entries = factory.newDocumentBuilder().parse(in).getElementsByTagName("JposEntry");
            for (int i = 0; i < entries.getLength(); i++) {
                Element entry = (Element) entries.item(i);
                Element jpos = (Element) entry.getElementsByTagName("jpos").item(0);
                (ScannerService.SCANNER_CATEGORY.equals(jpos.getAttribute("category")) ? inFile : scales)
                        .add(entry.getAttribute("logicalName"));
            }
        }

        assertTrue(inFile.size() >= 2 && !scales.isEmpty(), "the file has scanners and scales: " + inFile + " " + scales);
        assertEquals(ScannerService.pickerOrder(inFile), picker,
                "the picker is the file's Scanner entries, default first; JavaPOS last load error: "
                        + JposServiceLoader.getManager().getEntryRegistry().getRegPopulator().getLastLoadException());
        assertEquals(ScannerService.DEFAULT_LOGICAL_NAME, picker.get(0));
        for (String scale : scales) assertFalse(picker.contains(scale), scale + " is a scale, not a scanner");
    }
}
