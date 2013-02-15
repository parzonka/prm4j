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

import java.util.Collections;
import java.util.List;
import java.util.Set;

import prm4j.AbstractTest;
import prm4j.Util;
import prm4j.api.BaseEvent;
import prm4j.api.Parameter;
import prm4j.api.fsm.FSMSpec;
import prm4j.spec.FiniteParametricProperty;

public class AbstractStaticDataConverterTest extends AbstractTest {

    protected FiniteParametricProperty fpp;
    protected StaticDataConverter sdc;

    protected void init(FSM_HasNext fsm) {
	fpp = new FiniteParametricProperty(new FSMSpec(fsm.fsm));
	sdc = new StaticDataConverter(fpp);
    }

    protected final static MaxData[] EMPTY_MAXDATA_ARRAY = new MaxData[0];
    protected final static Set<ChainData> EMPTY_CHAINDATA_SET = Collections.emptySet();
    protected final static List<JoinData> EMPTY_JOINDATA_LIST = Collections.emptyList();

    protected void assertChainData(Set<Parameter<?>> parameterSet, Set<ChainData> chainDataSet) {
	assertEquals(chainDataSet, sdc.getMetaTree().getMetaNode(Util.asSortedList(parameterSet)).getChainDataSet());
    }

    protected void assertJoinData(BaseEvent baseEvent, List<JoinData> joinDataList) {
	assertArrayEquals(joinDataList.toArray(), sdc.getEventContext().getJoinData(baseEvent));
    }

    protected void assertMaxData(BaseEvent baseEvent, List<MaxData> maxDataList) {
	assertArrayEquals(maxDataList.toArray(), sdc.getEventContext().getMaxData(baseEvent));
    }

    protected static ChainData chainData(int[] nodeMask, int monitorSetId) {
	return new ChainData(nodeMask, monitorSetId);
    }

    protected static JoinData joinData(int[] nodeMask, int monitorSetId, int[] extensionPattern, int[] copyPattern,
	    int[][] disableMasks) {
	return new JoinData(nodeMask, monitorSetId, extensionPattern, copyPattern, disableMasks);
    }

}
