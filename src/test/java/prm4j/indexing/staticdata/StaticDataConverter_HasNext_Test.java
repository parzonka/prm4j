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
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class StaticDataConverter_HasNext_Test extends AbstractStaticDataConverterTest {

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
	StaticDataConverter sdc = new StaticDataConverter(fpp);
	MetaNode metaNode = sdc.getMetaTree().getMetaNode(fsm.i);

	// verify
	assertEquals(1, metaNode.getAliveParameterMasks().length);
	assertBooleanArrayEquals(array(true), metaNode.getAliveParameterMasks()[0]);
    }

}
