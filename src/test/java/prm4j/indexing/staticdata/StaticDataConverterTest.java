/*
 * Copyright (c) 2012 Mateusz Parzonka, Eric Bodden
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Mateusz Parzonka - initial API and implementation
 */
package prm4j.indexing.staticdata;

import static org.junit.Assert.assertArrayEquals;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import prm4j.AbstractTest;
import prm4j.api.Parameter;

@SuppressWarnings("rawtypes")
public class StaticDataConverterTest extends AbstractTest {

    private final static Parameter p1 = new Parameter("p1");
    private final static Parameter p2 = new Parameter("p2");
    private final static Parameter p3 = new Parameter("p3");
    private final static Parameter p4 = new Parameter("p4");
    private final static Parameter p5 = new Parameter("p5");

    @Before
    public void initialize() {
	p1.setIndex(1);
	p2.setIndex(2);
	p3.setIndex(3);
	p4.setIndex(4);
	p5.setIndex(5);
    }

    @Test
    public void getExtensionPattern_p1p3_p3p3() {

	Set<Parameter<?>> ps1 = asSet(p1, p3);
	Set<Parameter<?>> ps2 = asSet(p3, p5);

	boolean[] actual = StaticDataConverter.getExtensionPattern(ps1, ps2);

	boolean[] expected = { true, true, false };

	assertBooleanArrayEquals(expected, actual);
    }

    @Test
    public void getExtensionPattern_p1_p5() {

	Set<Parameter<?>> ps1 = asSet(p1);
	Set<Parameter<?>> ps2 = asSet(p5);

	boolean[] actual = StaticDataConverter.getExtensionPattern(ps1, ps2);

	boolean[] expected = { true, false };

	assertBooleanArrayEquals(expected, actual);
    }

    @Test
    public void getExtensionPattern_p5_p1() {

	Set<Parameter<?>> ps1 = asSet(p5);
	Set<Parameter<?>> ps2 = asSet(p1);

	boolean[] actual = StaticDataConverter.getExtensionPattern(ps1, ps2);

	boolean[] expected = { false, true };

	assertBooleanArrayEquals(expected, actual);
    }

    @Test
    public void getExtensionPattern_p3p5_p1p5() {

	Set<Parameter<?>> ps1 = asSet(p3, p5);
	Set<Parameter<?>> ps2 = asSet(p1, p5);

	boolean[] actual = StaticDataConverter.getExtensionPattern(ps1, ps2);

	boolean[] expected = { false, true, true };

	assertBooleanArrayEquals(expected, actual);
    }

    @Test
    public void getExtensionPattern_p3p4p5_p1p2p5() {

	Set<Parameter<?>> ps1 = asSet(p3, p4, p5);
	Set<Parameter<?>> ps2 = asSet(p1, p2, p5);

	boolean[] actual = StaticDataConverter.getExtensionPattern(ps1, ps2);

	boolean[] expected = { false, false, true, true, true };

	assertBooleanArrayEquals(expected, actual);
    }

    @Test
    public void getExtensionPattern_p2p4_p1p3p5() {

	Set<Parameter<?>> ps1 = asSet(p2, p4);
	Set<Parameter<?>> ps2 = asSet(p1, p3, p5);

	boolean[] actual = StaticDataConverter.getExtensionPattern(ps1, ps2);

	boolean[] expected = { false, true, false, true, false };

	assertBooleanArrayEquals(expected, actual);
    }

    @Test
    public void getExtensionPattern_p1p2p3p4_p2p3p4p5() {

	Set<Parameter<?>> ps1 = asSet(p1, p2, p3, p4);
	Set<Parameter<?>> ps2 = asSet(p2, p3, p4, p5);

	boolean[] actual = StaticDataConverter.getExtensionPattern(ps1, ps2);

	boolean[] expected = { true, true, true, true, false };

	assertBooleanArrayEquals(expected, actual);
    }

    @Test
    public void getCopyPattern() {

    }

    @Test
    public void toParameterMask_arbitrary() {

	int[] actual = StaticDataConverter.toParameterMask(asSet(p2, p3, p5));

	int[] expected = { 2, 3, 5 };

	assertArrayEquals(expected, actual);
    }

}
