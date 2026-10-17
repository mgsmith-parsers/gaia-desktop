package tools.pantheum.gaia.desktop.scanner;

import jpos.ScannerConst;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.pantheum.gaia.datacarrier.registry.DataCarrierType;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link Symbologies}.
 *
 * <p>The first test is the one that matters: it walks {@link ScannerConst} by reflection and
 * insists every scan-data type JavaPOS defines has a name here. The class originally covered 11
 * of 78, so a Zebra reporting GS1-128 or GS1 DataBar — the two most likely symbologies for a GS1
 * tool — logged "Unknown (120)". Reflection rather than a hardcoded list is deliberate: it also
 * fails when a JavaPOS upgrade adds a constant, which a fixed list never would.
 */
@DisplayName("Symbologies")
class SymbologiesTest {

    /** Every {@code SCAN_SDT_*} constant JavaPOS declares, by value. */
    private static TreeMap<Integer, String> declaredTypes() throws IllegalAccessException {
        TreeMap<Integer, String> types = new TreeMap<>();
        for (Field f : ScannerConst.class.getFields()) {
            if (Modifier.isStatic(f.getModifiers()) && f.getType() == int.class
                    && f.getName().startsWith("SCAN_SDT_")) {
                types.putIfAbsent(f.getInt(null), f.getName());
            }
        }
        return types;
    }

    @Test
    @DisplayName("every JavaPOS scan data type has a display name")
    void everyDeclaredTypeIsNamed() throws IllegalAccessException {
        TreeMap<Integer, String> declared = declaredTypes();
        // A floor, so a broken reflection walk cannot pass by finding nothing. JavaPOS 1.14 — the
        // API this module builds against — declares 70 distinct codes under 74 names (a few are
        // aliases); 1.15 adds two more.
        assertTrue(declared.size() >= 70, "expected the full JavaPOS table, found " + declared.size());

        List<String> unnamed = new ArrayList<>();
        for (java.util.Map.Entry<Integer, String> e : declared.entrySet()) {
            if (!Symbologies.isKnown(e.getKey())) {
                unnamed.add(e.getValue() + " = " + e.getKey());
            }
        }
        assertTrue(unnamed.isEmpty(), "scan data types with no name in Symbologies: " + unnamed);
    }

    @Test
    void typesAddedAfterTheCompiledApiAreStillNamed() {
        // DWCode (146) arrived in JavaPOS 1.15, so the reflection walk over the 1.14 ScannerConst
        // cannot see it — but a scanner's service can still report it. GS1-128 is the other 1.15
        // addition only by name: it is 120, which 1.14 already calls SCAN_SDT_EAN128.
        assertTrue(Symbologies.isKnown(Symbologies.SCAN_SDT_DWCODE));
        assertEquals("DWCode", Symbologies.name(146));
        assertEquals("GS1-128 (EAN-128)", Symbologies.name(120));
    }

    @Test
    @DisplayName("the GS1 symbologies are named and carry the right core type")
    void gs1SymbologiesMapToTheCore() {
        assertEquals("GS1-128 (EAN-128)", Symbologies.name(ScannerConst.SCAN_SDT_EAN128));
        assertEquals(DataCarrierType.GS1_128, Symbologies.carrierType(ScannerConst.SCAN_SDT_EAN128));

        assertEquals("GS1 DataBar (RSS-14)", Symbologies.name(ScannerConst.SCAN_SDT_GS1DATABAR));
        assertEquals(DataCarrierType.GS1_DATABAR, Symbologies.carrierType(ScannerConst.SCAN_SDT_GS1DATABAR));

        assertEquals("GS1 DataMatrix", Symbologies.name(ScannerConst.SCAN_SDT_GS1DATAMATRIX));
        assertEquals(DataCarrierType.GS1_DATA_MATRIX, Symbologies.carrierType(ScannerConst.SCAN_SDT_GS1DATAMATRIX));

        assertEquals("GS1 QR Code", Symbologies.name(ScannerConst.SCAN_SDT_GS1QRCODE));
        assertEquals(DataCarrierType.GS1_QR_CODE, Symbologies.carrierType(ScannerConst.SCAN_SDT_GS1QRCODE));

        // Every type the core calls a GS1 carrier must be one GS1 itself defines as carrying
        // AI data — the four reserved AIM variants plus DataBar.
        assertTrue(Symbologies.carrierType(ScannerConst.SCAN_SDT_CCA).isGs1DataCarrier());
    }

    @Test
    @DisplayName("plain and GS1 variants of the same symbology stay distinct")
    void plainAndGs1VariantsDiffer() {
        assertEquals(DataCarrierType.DATA_MATRIX, Symbologies.carrierType(ScannerConst.SCAN_SDT_DATAMATRIX));
        assertEquals(DataCarrierType.GS1_DATA_MATRIX, Symbologies.carrierType(ScannerConst.SCAN_SDT_GS1DATAMATRIX));
        assertFalse(Symbologies.carrierType(ScannerConst.SCAN_SDT_DATAMATRIX).isGs1DataCarrier());
    }

    @Test
    @DisplayName("an unrecognised type falls back to the JavaPOS constant name, then to the number")
    void unrecognisedTypesStillGetAName() {
        // 9999 is outside the JavaPOS table entirely — a vendor-specific value.
        String name = Symbologies.name(9999);
        assertNotNull(name);
        assertTrue(name.contains("9999"), name);
        assertFalse(Symbologies.isKnown(9999));
        assertEquals(DataCarrierType.UNKNOWN, Symbologies.carrierType(9999));
    }

    @Test
    @DisplayName("carrierType never returns null, for any declared type")
    void carrierTypeIsTotal() throws IllegalAccessException {
        for (Integer type : declaredTypes().keySet()) {
            assertNotNull(Symbologies.carrierType(type), "null carrier for type " + type);
            assertNotNull(Symbologies.name(type), "null name for type " + type);
        }
    }
}
