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
package prm4j.indexing.realtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import prm4j.api.fsm.FSMSpec;
import prm4j.spec.FiniteSpec;

public class DefaultParametricMonitor2Test extends AbstractDefaultParametricMonitorTest {

    FSM_a_ab_a_b fsm;
    final String a = "a";
    final String b = "b";
    final String c = "c";

    @Before
    public void init() {
	fsm = new FSM_a_ab_a_b();
	FiniteSpec finiteSpec = new FSMSpec(fsm.fsm);
	createDefaultParametricMonitorWithAwareComponents(finiteSpec);
    }

    // firstEvent_a //////////////////////////////////////////////////////////////////

    @Test
    public void firstEvent_a_createsOnlyOneMonitor() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a));

	// verify
	popNextCreatedMonitor();
	assertNoMoreCreatedMonitors();
    }

    @Test
    public void firstEvent_a_updatesOnlyOneMonitor() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a));

	// verify
	popNextUpdatedMonitor();
	assertNoMoreUpdatedMonitors();
    }

    @Test
    public void firstEvent_a_createsMonitorWithCreationTime0() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a));

	// verify
	assertEquals(0L, popNextUpdatedMonitor().getCreationTime());
    }

    @Test
    public void firstEvent_a_createsCorrectTrace() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a));

	// verify
	assertTrace(popNextUpdatedMonitor(), fsm.e1);
    }

    @Test
    public void firstEvent_a_monitorBindsAllItsParameters() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a));

	// verify
	assertBoundObjects(popNextUpdatedMonitor(), a);
    }

    @Test
    public void firstEvent_a_noMatchDetected() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a));

	// verify
	assertTrue(fsm.matchHandler.getHandledMatches().isEmpty());
    }

    @Test
    public void firstEvent_a_onlyOneNodeIsRetrieved() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a));

	// verify
	assertNotNull(popNextRetrievedNode());
	assertNoMoreRetrievedNodes();
    }

    @Test
    public void firstEvent_a_nodeHasOneMonitorSet() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a));

	// verify
	assertArrayEquals(new MonitorSet[1], popNextRetrievedNode().getMonitorSets());
    }

    @Test
    public void firstEvent_a_metaNodeHasCorrectParameterSet() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a));

	// verify
	assertEquals(asSet(fsm.p1), popNextRetrievedNode().getMetaNode().getNodeParameterSet());
    }

}
