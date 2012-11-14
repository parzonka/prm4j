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

    // firstEvent_ab //////////////////////////////////////////////////////////////////

    // monitors are not created because of ab is no creation event

    @Test
    public void firstEvent_ab_doesNotCreateMonitor() throws Exception {
	// exercise
	pm.processEvent(fsm.e2.createEvent(a, b));

	// verify
	assertNoMoreCreatedMonitors();
    }

    @Test
    public void firstEvent_ab_doesNotUpdateMonitor() throws Exception {
	// exercise
	pm.processEvent(fsm.e2.createEvent(a, b));

	// verify
	assertNoMoreUpdatedMonitors();
    }

    @Test
    public void firstEvent_ab_noMatchDetected() throws Exception {
	// exercise
	pm.processEvent(fsm.e2.createEvent(a, b));

	// verify
	assertTrue(fsm.matchHandler.getHandledMatches().isEmpty());
    }

    // twoEvent_a_b = a followed by b ////////////////////////////////

    @Test
    public void twoEvents_a_b_secondEventDoesCreateASingleNewMonitor() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a));
	popNextCreatedMonitor();
	pm.processEvent(fsm.e1.createEvent(b));

	// verify
	popNextCreatedMonitor();
	assertNoMoreCreatedMonitors();
    }

    @Test
    public void twoEvents_a_b_createdMonitorsAreDifferent() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a));
	pm.processEvent(fsm.e1.createEvent(b));

	// verify
	assertNotSame(popNextCreatedMonitor(), popNextCreatedMonitor());
    }

    @Test
    public void twoEvents_a_b_updatedMonitorsAreDifferent() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a));
	pm.processEvent(fsm.e1.createEvent(b));

	// verify
	assertNotSame(popNextUpdatedMonitor(), popNextUpdatedMonitor());
	assertNoMoreUpdatedMonitors();
    }

    @Test
    public void twoEvents_a_b_bothTracesAreCorrect() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a));
	pm.processEvent(fsm.e1.createEvent(b));

	// verify
	assertTrace(popNextUpdatedMonitor(), fsm.e1);
	assertTrace(popNextUpdatedMonitor(), fsm.e1);
    }

    @Test
    public void twoEvents_a_b_timestampsAreCorrect() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a));
	pm.processEvent(fsm.e1.createEvent(b));

	// verify
	assertEquals(0L, popNextCreatedMonitor().getCreationTime());
	assertEquals(1L, popNextCreatedMonitor().getCreationTime());
    }

    @Test
    public void twoEvents_a_b_boundObjectsAreCorrect() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a));
	pm.processEvent(fsm.e1.createEvent(b));

	// verify
	assertBoundObjects(popNextCreatedMonitor(), a);
	assertBoundObjects(popNextCreatedMonitor(), b);
    }

    // twoEvent_a_ab = a followed by ab ////////////////////////////////

    @Test
    public void twoEvents_a_ab_secondEventDoesCreateASingleNewMonitor() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a));
	popNextCreatedMonitor();
	pm.processEvent(fsm.e2.createEvent(a, b));

	// verify
	popNextCreatedMonitor();
	assertNoMoreCreatedMonitors();
    }

    @Test
    public void twoEvents_a_ab_createdMonitorsAreDifferent() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a));
	pm.processEvent(fsm.e2.createEvent(a, b));

	// verify
	assertNotSame(popNextCreatedMonitor(), popNextCreatedMonitor());
    }

    @Test
    public void twoEvents_a_ab_updatedMonitorsAreDifferent() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a));
	pm.processEvent(fsm.e2.createEvent(a, b));

	// verify
	assertNotSame(popNextUpdatedMonitor(), popNextUpdatedMonitor());
	assertNoMoreUpdatedMonitors();
    }

    @Test
    public void twoEvents_a_ab_bothTracesAreCorrect() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a));
	pm.processEvent(fsm.e2.createEvent(a, b));

	// verify
	assertTrace(popNextUpdatedMonitor(), fsm.e1);
	assertTrace(popNextUpdatedMonitor(), fsm.e1, fsm.e2);
    }

    @Test
    public void twoEvents_a_ab_timestampGetsPropagatedToNewMonitor() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a));
	pm.processEvent(fsm.e2.createEvent(a, b));

	// verify
	assertEquals(0L, popNextCreatedMonitor().getCreationTime());
	assertEquals(0L, popNextCreatedMonitor().getCreationTime());
    }

    @Test
    public void twoEvents_a_ab_boundObjectsAreCorrect() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a));
	pm.processEvent(fsm.e2.createEvent(a, b));

	// verify
	assertBoundObjects(popNextCreatedMonitor(), a);
	assertBoundObjects(popNextCreatedMonitor(), a, b);
    }

    // twoEvent_ab_a = ab followed by a (and some with b) ////////////////////////////////

    /*
     * ab is in the disabling set and disables any traces with a, so we will not see any monitors created associated to
     * the instance a
     */

    @Test
    public void twoEvents_ab_a_disableSetPreventsCreationOfBothMonitors() throws Exception {
	// exercise
	pm.processEvent(fsm.e2.createEvent(a, b));
	pm.processEvent(fsm.e1.createEvent(a));

	// verify
	assertNoMoreCreatedMonitors();
    }

    @Test
    public void twoEvents_ab_a_noUpdates() throws Exception {
	// exercise
	pm.processEvent(fsm.e2.createEvent(a, b));
	pm.processEvent(fsm.e1.createEvent(a));

	// verify
	assertNoMoreUpdatedMonitors();
    }

    @Test
    public void twoEvents_ab_b_disableSetPreventsCreationOfBothMonitors() throws Exception {
	// exercise
	pm.processEvent(fsm.e2.createEvent(a, b));
	pm.processEvent(fsm.e1.createEvent(b));

	// verify
	assertNoMoreCreatedMonitors();
    }

}
