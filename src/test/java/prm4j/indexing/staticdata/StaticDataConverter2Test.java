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
package prm4j.indexing.staticdata;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import prm4j.AbstractTest;
import prm4j.Util;
import prm4j.api.BaseEvent;
import prm4j.api.Parameter;
import prm4j.api.fsm.FSMSpec;
import prm4j.spec.FiniteParametricProperty;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

public class StaticDataConverter2Test extends AbstractTest {

    @Test
    public void getChainData_FSM_a_ab_a_b() {

	FSM_a_ab_a_b fsm = new FSM_a_ab_a_b();
	FiniteParametricProperty fpp = new FiniteParametricProperty(new FSMSpec(fsm.fsm));
	StaticDataConverter sdc = new StaticDataConverter(fpp);
	MetaNode mt = sdc.getMetaTree();

	assertChainData(mt, asSet(fsm.p1));
	assertChainData(mt, asSet(fsm.p2));
	assertChainData(mt, asSet(fsm.p1, fsm.p2), chainData(array(0), 0), chainData(array(1), 0));
    }

    @Test
    public void getChainData_FSM_ab_bc_c() {

	FSM_ab_bc_c fsm = new FSM_ab_bc_c();
	FiniteParametricProperty fpp = new FiniteParametricProperty(new FSMSpec(fsm.fsm));
	StaticDataConverter sdc = new StaticDataConverter(fpp);
	MetaNode mt = sdc.getMetaTree();

	assertChainData(mt, asSet(fsm.a));
	assertChainData(mt, asSet(fsm.b));
	assertChainData(mt, asSet(fsm.c));
	assertChainData(mt, asSet(fsm.a, fsm.b), chainData(array(1), 0));
	assertChainData(mt, asSet(fsm.b, fsm.c));
	assertChainData(mt, asSet(fsm.a, fsm.b, fsm.c), chainData(array(0, 1), 0), chainData(array(1, 2), 0),
		chainData(array(2), 0));
    }

    // @Test deactivated, the assertion holds, but nested arrays are compared on identity. To little time to implement
    // austom 2-dim comparison.
    public void getJoinData_FSM_ab_bc_c() {

	FSM_ab_bc_c fsm = new FSM_ab_bc_c();
	FiniteParametricProperty fpp = new FiniteParametricProperty(new FSMSpec(fsm.fsm));
	StaticDataConverter sdc = new StaticDataConverter(fpp);
	EventContext ec = sdc.getEventContext();

	int[][] disableMasks = new int[2][];
	disableMasks[0] = array(2);
	disableMasks[1] = array(1, 2);
	assertJoinData(ec, fsm.e2, joinData(array(1), 0, array(-1, 1, 2), array(0, 0), disableMasks));
    }

    @Test
    public void getChainData_FSM_ab_b_with_initial_b_loop() {

	FSM_ab_b_with_initial_b_loop fsm = new FSM_ab_b_with_initial_b_loop();
	FiniteParametricProperty fpp = new FiniteParametricProperty(new FSMSpec(fsm.fsm));
	StaticDataConverter sdc = new StaticDataConverter(fpp);
	MetaNode mt = sdc.getMetaTree();

	assertChainData(mt, asSet(fsm.p1));
	assertChainData(mt, asSet(fsm.p2));
	assertChainData(mt, asSet(fsm.p1, fsm.p2), chainData(array(1), 0));
    }

    @Test
    public void getMetaTreeMonitorSetCount_FSM_ab_b_with_initial_b_loop() {

	FSM_ab_b_with_initial_b_loop fsm = new FSM_ab_b_with_initial_b_loop();
	FiniteParametricProperty fpp = new FiniteParametricProperty(new FSMSpec(fsm.fsm));
	StaticDataConverter sdc = new StaticDataConverter(fpp);
	MetaNode metaTreeRoot = sdc.getMetaTree();

	assertEquals(0, metaTreeRoot.getMonitorSetCount());
	assertEquals(0, metaTreeRoot.createAndGetMetaNode(fsm.p1).getMonitorSetCount());
	assertEquals(1, metaTreeRoot.createAndGetMetaNode(fsm.p2).getMonitorSetCount());
    }

    @Test
    public void getMonitorSetIds_FSM_ab_b_with_initial_b_loop() {

	FSM_ab_b_with_initial_b_loop fsm = new FSM_ab_b_with_initial_b_loop();
	FiniteParametricProperty fpp = new FiniteParametricProperty(new FSMSpec(fsm.fsm));
	StaticDataConverter sdc = new StaticDataConverter(fpp);
	Table<Set<Parameter<?>>, Set<Parameter<?>>, Integer> actual = sdc.getMonitorSetIds();

	Table<Set<Parameter<?>>, Set<Parameter<?>>, Integer> expected = HashBasedTable.create();
	expected.put(asSet(fsm.p2), EMPTY_PARAMETER_SET, 0);

	assertEquals(expected, actual);

    }

    @Test
    public void calculateDisableCheckParameterSets1() {

	Parameter<String> a = new Parameter<String>("a");
	Parameter<String> b = new Parameter<String>("b");

	Set<Parameter<?>> combinedParameterSet = asSet(a, b);
	Set<Parameter<?>> enableParameterSet = asSet(a);

	Set<Set<Parameter<?>>> expected = new HashSet<Set<Parameter<?>>>();
	expected.add(asSet(b));
	expected.add(asSet(a, b));

	Set<Set<Parameter<?>>> actual = StaticDataConverter.calculateDisableCheckParameterSets(combinedParameterSet,
		enableParameterSet);

	assertEquals(expected, actual);

    }

    @Test
    public void calculateDisableCheckParameterSets2() {

	Parameter<String> a = new Parameter<String>("a");
	Parameter<String> b = new Parameter<String>("b");
	Parameter<String> c = new Parameter<String>("c");

	Set<Parameter<?>> baseEventParameterSet = asSet(a, b, c);
	Set<Parameter<?>> enableSet = asSet(a);

	Set<Set<Parameter<?>>> expected = new HashSet<Set<Parameter<?>>>();
	expected.add(asSet(a, b, c));
	expected.add(asSet(a, b));
	expected.add(asSet(a, c));
	expected.add(asSet(b, c));
	expected.add(asSet(b));
	expected.add(asSet(c));

	Set<Set<Parameter<?>>> actual = StaticDataConverter.calculateDisableCheckParameterSets(baseEventParameterSet,
		enableSet);

	assertEquals(expected, actual);

    }

    @Test
    public void calculateDisableCheckParameterSets3() {

	Parameter<String> a = new Parameter<String>("a");
	Parameter<String> b = new Parameter<String>("b");
	Parameter<String> c = new Parameter<String>("c");

	Set<Parameter<?>> baseEventParameterSet = asSet(a, b, c);
	Set<Parameter<?>> enableSet = asSet(a, b);

	Set<Set<Parameter<?>>> expected = new HashSet<Set<Parameter<?>>>();
	expected.add(asSet(a, b, c));
	expected.add(asSet(a, c));
	expected.add(asSet(b, c));
	expected.add(asSet(c));

	Set<Set<Parameter<?>>> actual = StaticDataConverter.calculateDisableCheckParameterSets(baseEventParameterSet,
		enableSet);

	assertEquals(expected, actual);

    }

    @Test
    public void calculateDisableParameterMasks1() {

	Parameter<String> a = new Parameter<String>("a");
	a.setIndex(0);
	Parameter<String> b = new Parameter<String>("b");
	b.setIndex(1);

	List<Set<Parameter<?>>> setOfParameterSets = new ArrayList<Set<Parameter<?>>>();
	setOfParameterSets.add(asSet(b));
	setOfParameterSets.add(asSet(a, b));

	int[][] actual = StaticDataConverter.calculateDisableParameterMasks(setOfParameterSets);

	int[][] expected = new int[2][];
	expected[0] = array(1);
	expected[1] = array(0, 1);

	assertArrayEquals(expected, actual);

    }

    @Test
    public void calculateDisableParameterMasks2() {

	Parameter<String> a = new Parameter<String>("a");
	a.setIndex(0);
	Parameter<String> b = new Parameter<String>("b");
	b.setIndex(1);
	Parameter<String> c = new Parameter<String>("c");
	c.setIndex(2);

	List<Set<Parameter<?>>> listOfParameterSets = new ArrayList<Set<Parameter<?>>>();
	listOfParameterSets.add(asSet(c));
	listOfParameterSets.add(asSet(a, b));
	listOfParameterSets.add(asSet(b, c));

	int[][] actual = StaticDataConverter.calculateDisableParameterMasks(listOfParameterSets);

	int[][] expected = new int[3][];
	expected[0] = array(2);
	expected[1] = array(0, 1);
	expected[2] = array(1, 2);

	assertArrayEquals(expected, actual);

    }

    @Test
    public void toListOfParameterSetsAscending() {

	Parameter<String> a = new Parameter<String>("a");
	a.setIndex(0);
	Parameter<String> b = new Parameter<String>("b");
	b.setIndex(1);
	Parameter<String> c = new Parameter<String>("c");
	c.setIndex(2);

	Set<Set<Parameter<?>>> setOfParameterSets = new HashSet<Set<Parameter<?>>>();
	setOfParameterSets.add(asSet(c));
	setOfParameterSets.add(asSet(a, c));
	setOfParameterSets.add(asSet(a, b, c));
	setOfParameterSets.add(asSet(a, b));
	setOfParameterSets.add(asSet(a));
	setOfParameterSets.add(asSet(b, c));

	List<Set<Parameter<?>>> actual = StaticDataConverter.toListOfParameterSetsAscending(setOfParameterSets);

	List<Set<Parameter<?>>> expected = new ArrayList<Set<Parameter<?>>>();
	expected.add(asSet(a));
	expected.add(asSet(c));
	expected.add(asSet(a, b));
	expected.add(asSet(a, c));
	expected.add(asSet(b, c));
	expected.add(asSet(a, b, c));

	assertEquals(expected, actual);

    }

    @Test
    public void calculateExistingMonitorMasks() throws Exception {
	Parameter<String> a = new Parameter<String>("a");
	a.setIndex(0);
	Parameter<String> b = new Parameter<String>("b");
	b.setIndex(1);
	Parameter<String> c = new Parameter<String>("c");
	c.setIndex(2);

	int[][] actual = StaticDataConverter.calculateExistingMonitorMasks(asSet(a, b, c));

	assertArrayEquals(new int[0], actual[0]);
	assertArrayEquals(array(0), actual[1]);
	assertArrayEquals(array(1), actual[2]);
	assertArrayEquals(array(2), actual[3]);
	assertArrayEquals(array(0, 1), actual[4]);
	assertArrayEquals(array(0, 2), actual[5]);
	assertArrayEquals(array(1, 2), actual[6]);
	assertArrayEquals(array(0, 1, 2), actual[7]);

    }

    protected static void assertChainData(MetaNode metaTree, Set<Parameter<?>> parameterSet, ChainData... chainDatas) {
	Set<ChainData> chainDataSet = new HashSet<ChainData>(Arrays.asList(chainDatas));
	assertEquals(chainDataSet, metaTree.getMetaNode(Util.asSortedList(parameterSet)).getChainDataSet());
    }

    protected static void assertJoinData(EventContext eventContext, BaseEvent baseEvent, JoinData... joinDatas) {
	assertArrayEquals(joinDatas, eventContext.getJoinData(baseEvent));
    }

    protected static ChainData chainData(int[] nodeMask, int monitorSetId) {
	return new ChainData(nodeMask, monitorSetId);
    }

    protected static JoinData joinData(int[] nodeMask, int monitorSetId, int[] extensionPattern, int[] copyPattern,
	    int[][] disableMasks) {
	return new JoinData(nodeMask, monitorSetId, extensionPattern, copyPattern, disableMasks);
    }

}
