/*
 * Copyright (c) 2012, 2013 Mateusz Parzonka
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Mateusz Parzonka - initial API and implementation
 */
package prm4j.indexing.model;

import static org.junit.Assert.assertArrayEquals;
import static prm4j.indexing.model.JoinArgs.getCopyPattern;
import static prm4j.indexing.model.JoinArgs.getExtensionPattern;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import prm4j.AbstractTest;
import prm4j.api.Parameter;

@SuppressWarnings("rawtypes")
public class JoinArgsTest extends AbstractTest {

    private final static Parameter p0 = new Parameter("p0");
    private final static Parameter p1 = new Parameter("p1");
    private final static Parameter p2 = new Parameter("p2");
    private final static Parameter p3 = new Parameter("p3");
    private final static Parameter p4 = new Parameter("p4");

    @Before
    public void initialize() {
	p0.setIndex(0);
	p1.setIndex(1);
	p2.setIndex(2);
	p3.setIndex(3);
	p4.setIndex(4);
    }

    // /////////////// getExtensionPatternNew ///////////////////////////

    @Test
    public void getExtensionPatternNew_p0p2_p2p4() {

	Set<Parameter<?>> ps1 = asSet(p0, p2);
	Set<Parameter<?>> ps2 = asSet(p2, p4);

	int[] actual = getExtensionPattern(ps1, ps2);

	int[] expected = { 0, 2, -1 };

	assertArrayEquals(expected, actual);
    }

    @Test
    public void getExtensionPatternNew_p2p4_p0p2() {

	Set<Parameter<?>> ps1 = asSet(p2, p4);
	Set<Parameter<?>> ps2 = asSet(p0, p2);

	int[] actual = getExtensionPattern(ps1, ps2);

	int[] expected = { -1, 2, 4 };

	assertArrayEquals(expected, actual);
    }

    @Test
    public void getExtensionPatternNew_p0_p4() {

	Set<Parameter<?>> ps1 = asSet(p0);
	Set<Parameter<?>> ps2 = asSet(p4);

	int[] actual = getExtensionPattern(ps1, ps2);

	int[] expected = { 0, -1 };

	assertArrayEquals(expected, actual);
    }

    @Test
    public void getExtensionPatternNew_p4_p0() {

	Set<Parameter<?>> ps1 = asSet(p4);
	Set<Parameter<?>> ps2 = asSet(p0);

	int[] actual = getExtensionPattern(ps1, ps2);

	int[] expected = { -1, 4 };

	assertArrayEquals(expected, actual);
    }

    @Test
    public void getExtensionPatternNew_p2p4_p0p4() {

	Set<Parameter<?>> ps1 = asSet(p2, p4);
	Set<Parameter<?>> ps2 = asSet(p0, p4);

	int[] actual = getExtensionPattern(ps1, ps2);

	int[] expected = { -1, 2, 4 };

	assertArrayEquals(expected, actual);
    }

    @Test
    public void getExtensionPatternNew_p2p3p4_p0p1p4() {

	Set<Parameter<?>> ps1 = asSet(p2, p3, p4);
	Set<Parameter<?>> ps2 = asSet(p0, p1, p4);

	int[] actual = getExtensionPattern(ps1, ps2);

	int[] expected = { -1, -1, 2, 3, 4 };

	assertArrayEquals(expected, actual);
    }

    @Test
    public void getExtensionPatternNew_p1p3_p0p2p4() {

	Set<Parameter<?>> ps1 = asSet(p1, p3);
	Set<Parameter<?>> ps2 = asSet(p0, p2, p4);

	int[] actual = getExtensionPattern(ps1, ps2);

	int[] expected = { -1, 1, -1, 3, -1 };

	assertArrayEquals(expected, actual);
    }

    @Test
    public void getExtensionPatternNew_p0p1p2p3_p1p2p3p4() {

	Set<Parameter<?>> ps1 = asSet(p0, p1, p2, p3);
	Set<Parameter<?>> ps2 = asSet(p1, p2, p3, p4);

	int[] actual = getExtensionPattern(ps1, ps2);

	int[] expected = { 0, 1, 2, 3, -1 };

	assertArrayEquals(expected, actual);
    }

    @Test
    public void getExtensionPatternNew_p2_p0p1p2() {

	Set<Parameter<?>> ps1 = asSet(p2);
	Set<Parameter<?>> ps2 = asSet(p0, p1, p2);

	int[] actual = getExtensionPattern(ps1, ps2);

	int[] expected = { -1, -1, 2 };

	assertArrayEquals(expected, actual);
    }

    @Test
    public void getExtensionPatternNew_unsafeMapIterator() {

	FSM_SafeMapIterator fsm = new FSM_SafeMapIterator();
	fsm.m.setIndex(0);
	fsm.c.setIndex(1);
	fsm.i.setIndex(2);

	Set<Parameter<?>> ps1 = asSet(fsm.m, fsm.c);
	Set<Parameter<?>> ps2 = asSet(fsm.c, fsm.i);

	int[] actual = getExtensionPattern(ps1, ps2);

	int[] expected = { 0, 1, -1 };

	assertArrayEquals(expected, actual);
    }

    // /////////////// getCopyPattern ///////////////////////////

    @Test
    public void getCopyPattern_p0_p4() {

	Set<Parameter<?>> ps1 = asSet(p0);
	Set<Parameter<?>> ps2 = asSet(p4);

	int[] actual = getCopyPattern(ps1, ps2);

	int[] expected = { 0, 1 };

	assertArrayEquals(expected, actual);
    }

    @Test
    public void getCopyPattern_p1p2_p0p1() {

	Set<Parameter<?>> ps1 = asSet(p1, p2);
	Set<Parameter<?>> ps2 = asSet(p0, p1);

	int[] actual = getCopyPattern(ps1, ps2);

	int[] expected = { 0, 0 };

	assertArrayEquals(expected, actual);
    }

    @Test
    public void getCopyPattern_p4_p0() {

	Set<Parameter<?>> ps1 = asSet(p4);
	Set<Parameter<?>> ps2 = asSet(p0);

	int[] actual = getCopyPattern(ps1, ps2);

	int[] expected = { 0, 0 };

	assertArrayEquals(expected, actual);
    }

    @Test
    public void getCopyPattern_p0p2_p2p4() {

	Set<Parameter<?>> ps1 = asSet(p0, p2);
	Set<Parameter<?>> ps2 = asSet(p2, p4);

	int[] actual = getCopyPattern(ps1, ps2);

	int[] expected = { 1, 2 };

	assertArrayEquals(expected, actual);
    }

    @Test
    public void getCopyPattern_p0p1p2p3_p1p2p3p4() {

	Set<Parameter<?>> ps1 = asSet(p0, p1, p2, p3);
	Set<Parameter<?>> ps2 = asSet(p1, p2, p3, p4);

	int[] actual = getCopyPattern(ps1, ps2);

	int[] expected = { 3, 4 };

	assertArrayEquals(expected, actual);
    }

    @Test
    public void getCopyPattern_p2_p0p1p2() {

	Set<Parameter<?>> ps1 = asSet(p2);
	Set<Parameter<?>> ps2 = asSet(p0, p1, p2);

	int[] actual = getCopyPattern(ps1, ps2);

	int[] expected = { 0, 0, 1, 1 };

	assertArrayEquals(expected, actual);
    }

    @Test
    public void getCopyPattern_p2_p0p1p2p3p4() {

	Set<Parameter<?>> ps1 = asSet(p2);
	Set<Parameter<?>> ps2 = asSet(p0, p1, p2, p3, p4);

	int[] actual = getCopyPattern(ps1, ps2);

	int[] expected = { 0, 0, 1, 1, 3, 3, 4, 4 };

	assertArrayEquals(expected, actual);
    }

    @Test
    public void getCopyPattern_p0p1p4_p2p3p4() {

	Set<Parameter<?>> ps1 = asSet(p0, p1, p4);
	Set<Parameter<?>> ps2 = asSet(p2, p3, p4);

	int[] actual = getCopyPattern(ps1, ps2);

	int[] expected = { 0, 2, 1, 3 };

	assertArrayEquals(expected, actual);
    }

    @Test
    public void getCopyPattern_p2p4_p0p1p3p4() {

	Set<Parameter<?>> ps1 = asSet(p2, p4);
	Set<Parameter<?>> ps2 = asSet(p0, p1, p3, p4);

	int[] actual = getCopyPattern(ps1, ps2);

	int[] expected = { 0, 0, 1, 1, 2, 3 };

	assertArrayEquals(expected, actual);
    }

}
