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

import org.junit.Before;
import org.junit.Test;

import prm4j.indexing.model.ParameterNode;
import prm4j.indexing.model.ParametricPropertyProcessor;

public class ParametricPropertyProcessor_HasNext_Test extends AbstractParametricPropertyProcessorTest {

    FSM_HasNext fsm;

    @Before
    public void init() {
	fsm = new FSM_HasNext();
	init(fsm);
    }

    @Test
    public void getChainData_nothingGetsChained() throws Exception {
	assertChainData(asSet(fsm.i), EMPTY_CHAINDATA_SET);
    }

    @Test
    public void getJoinData_nothingGetsJoined() throws Exception {
	assertEquals(2, sdc.getJoinData().length);
	assertJoinData(fsm.next, EMPTY_JOINDATA_LIST);
	assertJoinData(fsm.hasNext, EMPTY_JOINDATA_LIST);
    }

    @Test
    public void getMaxData_noMaxima() throws Exception {
	assertEquals(2, sdc.getMaxData().length);
	assertArrayEquals(EMPTY_MAXDATA_ARRAY, sdc.getMaxData()[fsm.next.getIndex()]);
	assertArrayEquals(EMPTY_MAXDATA_ARRAY, sdc.getMaxData()[fsm.hasNext.getIndex()]);
    }

    @Test
    public void getAliveParameterMasks() throws Exception {
	ParametricPropertyProcessor sdc = new ParametricPropertyProcessor(fpp);
	ParameterNode parameterNode = sdc.getParameterTree().getParameterNode(fsm.i);

	// verify
	assertEquals(1, parameterNode.getAliveParameterMasks().length);
	assertBooleanArrayEquals(array(true), parameterNode.getAliveParameterMasks()[0]);
    }

}
