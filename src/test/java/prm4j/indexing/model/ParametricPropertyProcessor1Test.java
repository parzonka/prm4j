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
import static org.junit.Assert.assertEquals;
import static prm4j.Util.toNodeMask;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import prm4j.AbstractTest;
import prm4j.api.Parameter;
import prm4j.api.fsm.FSM;
import prm4j.api.fsm.FSMSpec;
import prm4j.indexing.model.FindMaxArgs;
import prm4j.indexing.model.JoinArgs;
import prm4j.indexing.model.ParameterNode;
import prm4j.indexing.model.ParametricPropertyProcessor;
import prm4j.indexing.model.UpdateChainingsArgs;
import prm4j.spec.FiniteParametricProperty;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

@SuppressWarnings("rawtypes")
public class ParametricPropertyProcessor1Test extends AbstractTest {

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

	int[] actual = ParametricPropertyProcessor.getExtensionPattern(ps1, ps2);

	int[] expected = { 0, 2, -1 };

	assertArrayEquals(expected, actual);
    }

    @Test
    public void getExtensionPatternNew_p2p4_p0p2() {

	Set<Parameter<?>> ps1 = asSet(p2, p4);
	Set<Parameter<?>> ps2 = asSet(p0, p2);

	int[] actual = ParametricPropertyProcessor.getExtensionPattern(ps1, ps2);

	int[] expected = { -1, 2, 4 };

	assertArrayEquals(expected, actual);
    }

    @Test
    public void getExtensionPatternNew_p0_p4() {

	Set<Parameter<?>> ps1 = asSet(p0);
	Set<Parameter<?>> ps2 = asSet(p4);

	int[] actual = ParametricPropertyProcessor.getExtensionPattern(ps1, ps2);

	int[] expected = { 0, -1 };

	assertArrayEquals(expected, actual);
    }

    @Test
    public void getExtensionPatternNew_p4_p0() {

	Set<Parameter<?>> ps1 = asSet(p4);
	Set<Parameter<?>> ps2 = asSet(p0);

	int[] actual = ParametricPropertyProcessor.getExtensionPattern(ps1, ps2);

	int[] expected = { -1, 4 };

	assertArrayEquals(expected, actual);
    }

    @Test
    public void getExtensionPatternNew_p2p4_p0p4() {

	Set<Parameter<?>> ps1 = asSet(p2, p4);
	Set<Parameter<?>> ps2 = asSet(p0, p4);

	int[] actual = ParametricPropertyProcessor.getExtensionPattern(ps1, ps2);

	int[] expected = { -1, 2, 4 };

	assertArrayEquals(expected, actual);
    }

    @Test
    public void getExtensionPatternNew_p2p3p4_p0p1p4() {

	Set<Parameter<?>> ps1 = asSet(p2, p3, p4);
	Set<Parameter<?>> ps2 = asSet(p0, p1, p4);

	int[] actual = ParametricPropertyProcessor.getExtensionPattern(ps1, ps2);

	int[] expected = { -1, -1, 2, 3, 4 };

	assertArrayEquals(expected, actual);
    }

    @Test
    public void getExtensionPatternNew_p1p3_p0p2p4() {

	Set<Parameter<?>> ps1 = asSet(p1, p3);
	Set<Parameter<?>> ps2 = asSet(p0, p2, p4);

	int[] actual = ParametricPropertyProcessor.getExtensionPattern(ps1, ps2);

	int[] expected = { -1, 1, -1, 3, -1 };

	assertArrayEquals(expected, actual);
    }

    @Test
    public void getExtensionPatternNew_p0p1p2p3_p1p2p3p4() {

	Set<Parameter<?>> ps1 = asSet(p0, p1, p2, p3);
	Set<Parameter<?>> ps2 = asSet(p1, p2, p3, p4);

	int[] actual = ParametricPropertyProcessor.getExtensionPattern(ps1, ps2);

	int[] expected = { 0, 1, 2, 3, -1 };

	assertArrayEquals(expected, actual);
    }

    @Test
    public void getExtensionPatternNew_p2_p0p1p2() {

	Set<Parameter<?>> ps1 = asSet(p2);
	Set<Parameter<?>> ps2 = asSet(p0, p1, p2);

	int[] actual = ParametricPropertyProcessor.getExtensionPattern(ps1, ps2);

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

	int[] actual = ParametricPropertyProcessor.getExtensionPattern(ps1, ps2);

	int[] expected = { 0, 1, -1 };

	assertArrayEquals(expected, actual);
    }

    // /////////////// getCopyPattern ///////////////////////////

    @Test
    public void getCopyPattern_p0_p4() {

	Set<Parameter<?>> ps1 = asSet(p0);
	Set<Parameter<?>> ps2 = asSet(p4);

	int[] actual = ParametricPropertyProcessor.getCopyPattern(ps1, ps2);

	int[] expected = { 0, 1 };

	assertArrayEquals(expected, actual);
    }

    @Test
    public void getCopyPattern_p1p2_p0p1() {

	Set<Parameter<?>> ps1 = asSet(p1, p2);
	Set<Parameter<?>> ps2 = asSet(p0, p1);

	int[] actual = ParametricPropertyProcessor.getCopyPattern(ps1, ps2);

	int[] expected = { 0, 0 };

	assertArrayEquals(expected, actual);
    }

    @Test
    public void getCopyPattern_p4_p0() {

	Set<Parameter<?>> ps1 = asSet(p4);
	Set<Parameter<?>> ps2 = asSet(p0);

	int[] actual = ParametricPropertyProcessor.getCopyPattern(ps1, ps2);

	int[] expected = { 0, 0 };

	assertArrayEquals(expected, actual);
    }

    @Test
    public void getCopyPattern_p0p2_p2p4() {

	Set<Parameter<?>> ps1 = asSet(p0, p2);
	Set<Parameter<?>> ps2 = asSet(p2, p4);

	int[] actual = ParametricPropertyProcessor.getCopyPattern(ps1, ps2);

	int[] expected = { 1, 2 };

	assertArrayEquals(expected, actual);
    }

    @Test
    public void getCopyPattern_p0p1p2p3_p1p2p3p4() {

	Set<Parameter<?>> ps1 = asSet(p0, p1, p2, p3);
	Set<Parameter<?>> ps2 = asSet(p1, p2, p3, p4);

	int[] actual = ParametricPropertyProcessor.getCopyPattern(ps1, ps2);

	int[] expected = { 3, 4 };

	assertArrayEquals(expected, actual);
    }

    @Test
    public void getCopyPattern_p2_p0p1p2() {

	Set<Parameter<?>> ps1 = asSet(p2);
	Set<Parameter<?>> ps2 = asSet(p0, p1, p2);

	int[] actual = ParametricPropertyProcessor.getCopyPattern(ps1, ps2);

	int[] expected = { 0, 0, 1, 1 };

	assertArrayEquals(expected, actual);
    }

    @Test
    public void getCopyPattern_p2_p0p1p2p3p4() {

	Set<Parameter<?>> ps1 = asSet(p2);
	Set<Parameter<?>> ps2 = asSet(p0, p1, p2, p3, p4);

	int[] actual = ParametricPropertyProcessor.getCopyPattern(ps1, ps2);

	int[] expected = { 0, 0, 1, 1, 3, 3, 4, 4 };

	assertArrayEquals(expected, actual);
    }

    @Test
    public void getCopyPattern_p0p1p4_p2p3p4() {

	Set<Parameter<?>> ps1 = asSet(p0, p1, p4);
	Set<Parameter<?>> ps2 = asSet(p2, p3, p4);

	int[] actual = ParametricPropertyProcessor.getCopyPattern(ps1, ps2);

	int[] expected = { 0, 2, 1, 3 };

	assertArrayEquals(expected, actual);
    }

    @Test
    public void getCopyPattern_p2p4_p0p1p3p4() {

	Set<Parameter<?>> ps1 = asSet(p2, p4);
	Set<Parameter<?>> ps2 = asSet(p0, p1, p3, p4);

	int[] actual = ParametricPropertyProcessor.getCopyPattern(ps1, ps2);

	int[] expected = { 0, 0, 1, 1, 2, 3 };

	assertArrayEquals(expected, actual);
    }

    // /////////////// toParameterMask ///////////////////////////

    @Test
    public void toParameterMask_arbitrary() {

	int[] actual = ParametricPropertyProcessor.toParameterMask(asSet(p1, p2, p4));

	int[] expected = { 1, 2, 4 };

	assertArrayEquals(expected, actual);
    }

    @Test
    public void toParameterMask_emptyset() {

	int[] actual = ParametricPropertyProcessor.toParameterMask(EMPTY_PARAMETER_SET);

	int[] expected = {};

	assertArrayEquals(expected, actual);
    }

    // /////////////// getMaxData ///////////////////////////

    @Test
    public void getMaxData_unsafeMapIterator() {

	FSM_SafeMapIterator u = new FSM_SafeMapIterator();
	FSM fsm = u.fsm;
	u.m.setIndex(0);
	u.c.setIndex(1);
	u.i.setIndex(2);
	FiniteParametricProperty fpp = new FiniteParametricProperty(new FSMSpec(fsm));
	ParametricPropertyProcessor sdc = new ParametricPropertyProcessor(fpp);

	FindMaxArgs[][] actual = sdc.getMaxData();

	FindMaxArgs[][] expected = new FindMaxArgs[fpp.getBaseEvents().size()][];
	expected[u.createColl.getIndex()] = new FindMaxArgs[0];
	expected[u.updateMap.getIndex()] = new FindMaxArgs[0];
	expected[u.createIter.getIndex()] = new FindMaxArgs[0];
	expected[u.useIter.getIndex()] = new FindMaxArgs[0];

	assertArrayEquals(expected, actual);
    }

    @Test
    public void getJoinData_unsafeMapIterator() {

	FSM_SafeMapIterator u = new FSM_SafeMapIterator();
	FSM fsm = u.fsm;
	u.m.setIndex(0);
	u.c.setIndex(1);
	u.i.setIndex(2);
	FiniteParametricProperty fpp = new FiniteParametricProperty(new FSMSpec(fsm));
	ParametricPropertyProcessor sdc = new ParametricPropertyProcessor(fpp);

	JoinArgs[][] actual = sdc.getJoinData();

	JoinArgs[][] expected = new JoinArgs[fpp.getBaseEvents().size()][];
	expected[u.createColl.getIndex()] = new JoinArgs[0];

	expected[u.updateMap.getIndex()] = new JoinArgs[0];

	JoinArgs[] jd = new JoinArgs[1];
	int[][] disableMasks = { { 2 }, { 1, 2 } };
	jd[0] = new JoinArgs(array(1), 0, array(-1, 1, 2), array(0, 0), disableMasks);
	expected[u.createIter.getIndex()] = jd;

	expected[u.useIter.getIndex()] = new JoinArgs[0];

	assert2DimArrayEquals(expected, actual);
    }

    @Test
    public void getChainData_unsafeMapIterator() {

	FSM_SafeMapIterator u = new FSM_SafeMapIterator();
	FSM fsm = u.fsm;
	u.m.setIndex(0);
	u.c.setIndex(1);
	u.i.setIndex(2);
	FiniteParametricProperty fpp = new FiniteParametricProperty(new FSMSpec(fsm));
	ParametricPropertyProcessor sdc = new ParametricPropertyProcessor(fpp);

	SetMultimap<Set<Parameter<?>>, UpdateChainingsArgs> actual = sdc.getChainData();

	SetMultimap<Set<Parameter<?>>, UpdateChainingsArgs> expected = HashMultimap.create();
	expected.put(asSet(u.m, u.c, u.i), new UpdateChainingsArgs(list(0), 0));
	expected.put(asSet(u.m, u.c, u.i), new UpdateChainingsArgs(list(2), 0));
	expected.put(asSet(u.m, u.c, u.i), new UpdateChainingsArgs(list(0, 1), 0));
	expected.put(asSet(u.m, u.c, u.i), new UpdateChainingsArgs(list(1, 2), 0));
	// optimized away by recognizing non-state changing transitions:
	// expected.put(asSet(u.m, u.c), new UpdateChainingsArgs(list(0), 0));
	expected.put(asSet(u.m, u.c), new UpdateChainingsArgs(list(1), 0));

	assertEquals(expected, actual);
    }

}
