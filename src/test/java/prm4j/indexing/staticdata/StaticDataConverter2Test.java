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

import java.util.Arrays;
import java.util.HashSet;
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

	assertChainData(mt, asSet(fsm.p1));
	assertChainData(mt, asSet(fsm.p2));
	assertChainData(mt, asSet(fsm.p3));
	assertChainData(mt, asSet(fsm.p1, fsm.p2), chainData(array(1), 0));
	assertChainData(mt, asSet(fsm.p2, fsm.p3));
	assertChainData(mt, asSet(fsm.p1, fsm.p2, fsm.p3), chainData(array(0, 1), 0), chainData(array(1, 2), 0),
		chainData(array(2), 0));
    }

    @Test
    public void getJoinData_FSM_ab_bc_c() {

	FSM_ab_bc_c fsm = new FSM_ab_bc_c();
	FiniteParametricProperty fpp = new FiniteParametricProperty(new FSMSpec(fsm.fsm));
	StaticDataConverter sdc = new StaticDataConverter(fpp);
	EventContext ec = sdc.getEventContext();

	assertJoinData(ec, fsm.e2, joinData(array(1), 0, array(-1, 1, 2), array(0, 0), array(2)));
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

    protected static JoinData joinData(int[] nodeMask, int monitorSetId, int[] extensionPattern, int[] copyPattern, int[] diffMask) {
	return new JoinData(nodeMask, monitorSetId, extensionPattern, copyPattern, diffMask);
    }

}
