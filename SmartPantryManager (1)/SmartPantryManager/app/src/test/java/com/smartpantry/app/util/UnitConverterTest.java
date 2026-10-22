package com.smartpantry.app.util;

/*
 * Smart Pantry Manager - Mobile App Development 700
 * Author: Micaiah Nhamburo
 */

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class UnitConverterTest {

    @Test
    public void unitAliasesAreNormalised() {
        assertEquals("g", UnitConverter.normalizeUnit("Grams"));
        assertEquals("kg", UnitConverter.normalizeUnit("kilos"));
        assertEquals("tbsp", UnitConverter.normalizeUnit("TBS"));
        assertEquals("piece", UnitConverter.normalizeUnit("slices"));
        assertEquals("piece", UnitConverter.normalizeUnit("whatever"));
    }

    @Test
    public void quantitiesConvertToBaseUnits() {
        assertEquals(1500.0, UnitConverter.toBase(1.5, "kg"), 1e-9);
        assertEquals(15.0, UnitConverter.toBase(1, "tbsp"), 1e-9);
        assertEquals(480.0, UnitConverter.toBase(2, "cup"), 1e-9);
        assertEquals(250.0, UnitConverter.toBase(0.25, "l"), 1e-9);
    }

    @Test
    public void dimensionsAreCorrect() {
        assertEquals(UnitConverter.Dimension.MASS, UnitConverter.dimensionOf("kg"));
        assertEquals(UnitConverter.Dimension.VOLUME, UnitConverter.dimensionOf("tsp"));
        assertEquals(UnitConverter.Dimension.COUNT, UnitConverter.dimensionOf("piece"));
    }
}
