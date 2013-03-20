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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import prm4j.AbstractTest;
import prm4j.api.fsm.FSMSpec;
import prm4j.spec.finite.FiniteParametricProperty;

public class ParametricPropertyProcessorTest extends AbstractTest {

    @Test
    public void getChainData_FSM_a_ab_a_b() {

	FSM_a_ab_a_b fsm = new FSM_a_ab_a_b();
	FiniteParametricProperty fpp = new FiniteParametricProperty(new FSMSpec(fsm.fsm));
	ParametricPropertyProcessor sdc = new ParametricPropertyProcessor(fpp);
	ParameterNode mt = sdc.getParameterTree();

	assertChainData(mt, asSet(fsm.p1));
	assertChainData(mt, asSet(fsm.p2));
	assertChainData(mt, asSet(fsm.p1, fsm.p2), updateChainingsArgs(array(0), 0), updateChainingsArgs(array(1), 0));
    }

    @Test
    public void getChainData_FSM_ab_bc_c() {

	FSM_ab_bc_c fsm = new FSM_ab_bc_c();
	FiniteParametricProperty fpp = new FiniteParametricProperty(new FSMSpec(fsm.fsm));
	ParametricPropertyProcessor sdc = new ParametricPropertyProcessor(fpp);
	ParameterNode mt = sdc.getParameterTree();

	assertChainData(mt, asSet(fsm.a));
	assertChainData(mt, asSet(fsm.b));
	assertChainData(mt, asSet(fsm.c));
	assertChainData(mt, asSet(fsm.a, fsm.b), updateChainingsArgs(array(1), 0));
	assertChainData(mt, asSet(fsm.b, fsm.c));
	assertChainData(mt, asSet(fsm.a, fsm.b, fsm.c), updateChainingsArgs(array(0, 1), 0),
		updateChainingsArgs(array(1, 2), 0), updateChainingsArgs(array(2), 0));
    }

    // @Test deactivated, the assertion holds, but nested arrays are compared on identity. To little time to implement
    // austom 2-dim comparison.
    public void getJoinData_FSM_ab_bc_c() {

	FSM_ab_bc_c fsm = new FSM_ab_bc_c();
	FiniteParametricProperty fpp = new FiniteParametricProperty(new FSMSpec(fsm.fsm));
	ParametricPropertyProcessor sdc = new ParametricPropertyProcessor(fpp);
	EventContext ec = sdc.getEventContext();

	int[][] disableMasks = new int[2][];
	disableMasks[0] = array(2);
	disableMasks[1] = array(1, 2);
	assertJoinData(ec, fsm.e2, joinArgs(array(1), 0, array(-1, 1, 2), array(0, 0), disableMasks));
    }

    @Test
    public void getChainData_FSM_ab_b_with_initial_b_loop() {

	FSM_ab_b_with_initial_b_loop fsm = new FSM_ab_b_with_initial_b_loop();
	FiniteParametricProperty fpp = new FiniteParametricProperty(new FSMSpec(fsm.fsm));
	ParametricPropertyProcessor sdc = new ParametricPropertyProcessor(fpp);
	ParameterNode mt = sdc.getParameterTree();

	assertChainData(mt, asSet(fsm.p1));
	assertChainData(mt, asSet(fsm.p2));
	assertChainData(mt, asSet(fsm.p1, fsm.p2), updateChainingsArgs(array(1), 0));
    }

    @Test
    public void getParameterTreeMonitorSetCount_FSM_ab_b_with_initial_b_loop() {

	FSM_ab_b_with_initial_b_loop fsm = new FSM_ab_b_with_initial_b_loop();
	FiniteParametricProperty fpp = new FiniteParametricProperty(new FSMSpec(fsm.fsm));
	ParametricPropertyProcessor sdc = new ParametricPropertyProcessor(fpp);
	ParameterNode parameterTreeRoot = sdc.getParameterTree();

	assertEquals(0, parameterTreeRoot.getMonitorSetCount());
	assertEquals(0, parameterTreeRoot.createAndGetParameterNode(fsm.p1).getMonitorSetCount());
	assertEquals(1, parameterTreeRoot.createAndGetParameterNode(fsm.p2).getMonitorSetCount());
    }

}
