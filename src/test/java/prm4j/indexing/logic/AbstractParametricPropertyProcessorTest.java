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
package prm4j.indexing.logic;

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
import prm4j.indexing.logic.FindMaxArgs;
import prm4j.indexing.logic.JoinArgs;
import prm4j.indexing.logic.ParametricPropertyProcessor;
import prm4j.indexing.logic.UpdateChainingsArgs;
import prm4j.spec.FiniteParametricProperty;

public class AbstractParametricPropertyProcessorTest extends AbstractTest {

    protected FiniteParametricProperty fpp;
    protected ParametricPropertyProcessor sdc;

    protected void init(FSM_HasNext fsm) {
	fpp = new FiniteParametricProperty(new FSMSpec(fsm.fsm));
	sdc = new ParametricPropertyProcessor(fpp);
    }

    protected final static FindMaxArgs[] EMPTY_MAXDATA_ARRAY = new FindMaxArgs[0];
    protected final static Set<UpdateChainingsArgs> EMPTY_CHAINDATA_SET = Collections.emptySet();
    protected final static List<JoinArgs> EMPTY_JOINDATA_LIST = Collections.emptyList();

    protected void assertChainData(Set<Parameter<?>> parameterSet, Set<UpdateChainingsArgs> chainDataSet) {
	assertEquals(chainDataSet, sdc.getParameterTree().getParameterNode(Util.asSortedList(parameterSet)).getChainDataSet());
    }

    protected void assertJoinData(BaseEvent baseEvent, List<JoinArgs> joinDataList) {
	assertArrayEquals(joinDataList.toArray(), sdc.getEventContext().getJoinArgs(baseEvent));
    }

    protected void assertMaxData(BaseEvent baseEvent, List<FindMaxArgs> maxDataList) {
	assertArrayEquals(maxDataList.toArray(), sdc.getEventContext().getFindMaxArgs(baseEvent));
    }

    protected static UpdateChainingsArgs updateChainingsArgs(int[] nodeMask, int monitorSetId) {
	return new UpdateChainingsArgs(nodeMask, monitorSetId);
    }

    protected static JoinArgs joinArgs(int[] nodeMask, int monitorSetId, int[] extensionPattern, int[] copyPattern,
	    int[][] disableMasks) {
	return new JoinArgs(nodeMask, monitorSetId, extensionPattern, copyPattern, disableMasks);
    }

}
