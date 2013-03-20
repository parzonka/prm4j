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
package prm4j.indexing.realtime;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Before;
import org.junit.Test;

import prm4j.api.fsm.FSMSpec;
import prm4j.indexing.model.EventContext;
import prm4j.indexing.model.FindMaxArgs;
import prm4j.indexing.model.JoinArgs;
import prm4j.indexing.model.UpdateChainingsArgs;
import prm4j.spec.finite.FiniteSpec;

public class DefaultParametricMonitor_UnsafeMapIteratorTest extends AbstractDefaultParametricMonitorTest {

    FSM_SafeMapIterator fsm;

    @Before
    public void init() {
	fsm = new FSM_SafeMapIterator();
	FiniteSpec finiteSpec = new FSMSpec(fsm.fsm);
	createDefaultParametricMonitorWithAwareComponents(finiteSpec);
    }

    @Test
    public void eventContext_getFindMaxArgs() {
	EventContext eventContext = processor.getEventContext();
	assertArrayEquals(eventContext.getFindMaxArgs(fsm.createColl), new FindMaxArgs[0]);
	assertArrayEquals(eventContext.getFindMaxArgs(fsm.updateMap), new FindMaxArgs[0]);
	assertArrayEquals(eventContext.getFindMaxArgs(fsm.createIter), new FindMaxArgs[0]);
	assertArrayEquals(eventContext.getFindMaxArgs(fsm.useIter), new FindMaxArgs[0]);
    }

    @Test
    public void eventContext_getJoinArgs() {
	EventContext eventContext = processor.getEventContext();
	JoinArgs[] jd = new JoinArgs[1];
	int[][] disableMasks = { { 2 }, { 1, 2 } };
	jd[0] = new JoinArgs(array(1), 0, array(-1, 1, 2), array(0, 0), disableMasks);
	// verify
	assertArrayEquals(eventContext.getJoinArgs(fsm.createColl), new JoinArgs[0]);
	assertArrayEquals(eventContext.getJoinArgs(fsm.createIter), jd);
	assertArrayEquals(eventContext.getJoinArgs(fsm.updateMap), new JoinArgs[0]);
	assertArrayEquals(eventContext.getJoinArgs(fsm.useIter), new JoinArgs[0]);
    }

    @Test
    public void getChainData_unsafeMapIterator() {
	assertChainData(processor.getParameterTree(), asSet(fsm.m, fsm.c, fsm.i), new UpdateChainingsArgs(list(0), 0),
		new UpdateChainingsArgs(list(2), 0), new UpdateChainingsArgs(list(0, 1), 0), new UpdateChainingsArgs(
			list(1, 2), 0));
	assertChainData(processor.getParameterTree(), asSet(fsm.m, fsm.c), new UpdateChainingsArgs(list(1), 0));
    }

}
