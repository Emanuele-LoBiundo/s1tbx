/*
 * Copyright (C) 2020 by SkyWatch Space Applications Inc. http://www.skywatch.com
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */
package org.esa.s1tbx.io.capella;

import org.esa.s1tbx.commons.test.MetadataValidator;
import org.esa.s1tbx.commons.test.ReaderTest;
import org.esa.s1tbx.commons.test.S1TBXTests;
import org.esa.snap.core.datamodel.Product;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assume.assumeTrue;

/**
 * Test Product Reader.
 *
 * @author lveci
 */
public class TestCapellaProductReader extends ReaderTest {

    private final static String sep = S1TBXTests.sep;

    private final static File inputGEOMeta = new File("E:\\data\\Capella\\Airborne\\GEO\\ARL_SM_GEO_HH_20190823162315_20190823162606_extended.json");
    private final static File inputGEOFolder = new File("E:\\data\\Capella\\Airborne\\GEO");

    private final static File inputSLCMeta = new File("E:\\data\\Capella\\Airborne\\SLC\\ARL_SM_SLC_HH_20190823162315_20190823162606_extended.json");
    private final static File inputSLCFolder = new File("E:\\data\\Capella\\Airborne\\SLC\\");

    final static MetadataValidator.ValidationOptions options = new MetadataValidator.ValidationOptions();

    public TestCapellaProductReader() {
        super(new CapellaProductReaderPlugIn());
    }

    @Before
    public void setUp() {
        // If any of the file does not exist: the test will be ignored
        assumeTrue(inputGEOMeta + " not found", inputGEOMeta.exists());
        assumeTrue(inputGEOFolder + " not found", inputGEOFolder.exists());
        assumeTrue(inputSLCMeta + " not found", inputSLCMeta.exists());
        assumeTrue(inputSLCFolder + " not found", inputSLCFolder.exists());

        options.validateOrbitStateVectors = false;
    }

    @Test
    public void testOpeningGEOFolder() throws Exception {
        Product prod = testReader(inputGEOFolder.toPath());
        validateProduct(prod);
        validateMetadata(prod, options);
        validateBands(prod, new String[] {"Amplitude_HH","Intensity_HH"});
    }

    @Test
    public void testOpeningGEOMetadata() throws Exception {
        Product prod = testReader(inputGEOMeta.toPath());
        validateProduct(prod);
        validateMetadata(prod, options);
        validateBands(prod, new String[] {"Amplitude_HH","Intensity_HH"});
    }

    @Test
    public void testOpeningSLCFolder() throws Exception {
        Product prod = testReader(inputSLCFolder.toPath());
        validateProduct(prod);
        validateMetadata(prod, options);
        validateBands(prod, new String[] {"i_HH","q_HH","Intensity_HH"});
    }

    @Test
    public void testOpeningSLCMetadata() throws Exception {
        Product prod = testReader(inputSLCMeta.toPath());
        validateProduct(prod);
        validateMetadata(prod, options);
        validateBands(prod, new String[] {"i_HH","q_HH","Intensity_HH"});
    }
}
