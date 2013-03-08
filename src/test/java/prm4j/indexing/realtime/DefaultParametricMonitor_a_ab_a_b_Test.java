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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import prm4j.api.fsm.FSMSpec;
import prm4j.indexing.monitor.AbstractMonitor;
import prm4j.indexing.monitor.MonitorSet;
import prm4j.spec.FiniteSpec;

public class DefaultParametricMonitor_a_ab_a_b_Test extends AbstractDefaultParametricMonitorTest {

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

    @Test
    public void setup() throws Exception {
	// exercise
	assertEquals(asSet(fsm.e1, fsm.e2, fsm.e3), fpp.getCreationEvents());
	assertEquals(asSet(fsm.e2, fsm.e3), fpp.getDisablingEvents());
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
	assertEquals(0L, popNextUpdatedMonitor().getTimestamp());
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
    public void firstEvent_a_parameterNodeHasCorrectParameterSet() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a));

	// verify
	assertEquals(asSet(fsm.p1), popNextRetrievedNode().getParameterNode().getNodeParameterSet());
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
    public void firstEvent_ab_doesCreateNodeForDeadMonitor() throws Exception {
	// exercise
	pm.processEvent(fsm.e2.createEvent(a, b));

	// verify
	assertCreatedNodes(array(a, b)); // nodes has to be created disable creations of nodes of more informative
					 // instances
    }

    @Test
    public void firstEvent_ab_nodeWithDeadMonitorIsCreated() throws Exception {
	// exercise
	pm.processEvent(fsm.e2.createEvent(a, b));

	// verify
	assertDeadMonitor(a, b);
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
    public void twoEvents_a_b_updateCountIsCorrect() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a));
	pm.processEvent(fsm.e1.createEvent(b));
	pm.processEvent(fsm.e1.createEvent(a));

	// verify
	assertEquals(3, AbstractMonitor.getUpdateddMonitorsCount()); // a, b, a
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
	assertEquals(0L, popNextCreatedMonitor().getTimestamp());
	assertEquals(1L, popNextCreatedMonitor().getTimestamp());
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
    public void twoEventsWithUpdate_a_ab_updateCountIsCorrect() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a));
	pm.processEvent(fsm.e2.createEvent(a, b));
	pm.processEvent(fsm.e1.createEvent(a));

	// verify
	assertEquals(4, AbstractMonitor.getUpdateddMonitorsCount()); // a, ab, a, ab
    }

    @Test
    public void a_ab_creates3Nodes() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a));
	pm.processEvent(fsm.e2.createEvent(a, b));
	// verify
	assertNumberOfCreatedNodes(3);
	assertNodeExists(getNode(a, _));
	assertNodeExists(getNode(a, b));
	assertNodeExists(getNode(_, b));
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
	assertEquals(0L, popNextCreatedMonitor().getTimestamp());
	assertEquals(0L, popNextCreatedMonitor().getTimestamp());
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

    @Test
    public void twoEvents_a_ab_tracesAreCorrect() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a));
	pm.processEvent(fsm.e2.createEvent(a, b));

	// verify
	assertTrace(popNextCreatedMonitor(), fsm.e1);
	assertTrace(popNextCreatedMonitor(), fsm.e1, fsm.e2);
    }

    @Test
    public void twoEvents_a_ab_monitorForBisNotCreated() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a));
	pm.processEvent(fsm.e2.createEvent(a, b));

	// verify
	assertNodeExists(getNode(null, b));
	assertNull(getMonitor(null, b));
    }

    // twoEvents_a_a associated to different base events ////////////////////////////////

    @Test
    public void twoEvents_e1a_e3a_differentParamsBoundToTheSameObjectAreEqual() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a));
	pm.processEvent(fsm.e3.createEvent(a));

	// verify
	assertEquals(popNextRetrievedBinding()[0], popNextRetrievedBinding()[0]);
    }

    // twoEvent_ab_a = ab followed by a (and some with b) ////////////////////////////////

    /*
     * ab is in the disabling set and disables any traces with a, so we will not see any monitors created associated to
     * the instance a
     */

    @Test
    public void twoEvents_ab_a_monitorForAIsCreated() throws Exception {
	// exercise
	pm.processEvent(fsm.e2.createEvent(a, b));
	pm.processEvent(fsm.e1.createEvent(a));

	// verify
	assertDeadMonitor(a, b);
	assertNumberOfCreatedMonitors(1);
	assertMonitorExistsAndIsNotTheDeadMonitor(getMonitor(a, _));
	// monitor for a is created, since it can be part of many trace slices: (a, b1), (a, b2) ...
    }

    @Test
    public void twoEvents_ab_a_noUpdates() throws Exception {
	// exercise
	pm.processEvent(fsm.e2.createEvent(a, b));
	pm.processEvent(fsm.e1.createEvent(a));

	// verify
	assertTrace(getMonitor(a, _), fsm.e1);
	assertDeadMonitor(a, b);
    }

    @Test
    public void firstEvent_ab_bothBindingsNotDisabledSeparately() throws Exception {
	// exercise
	pm.processEvent(fsm.e2.createEvent(a, b));

	// verify
	assertEquals(0L, getNode(a, b).getTimestamp());
	assertEquals(0L, getNode(a, b).getMonitor().getTimestamp());

    }

    @Test
    public void firstEvent_ab_a_instanceABisTimestamped() throws Exception {
	// exercise
	pm.processEvent(fsm.e2.createEvent(a, b));
	pm.processEvent(fsm.e1.createEvent(a));

	// verify
	assertEquals(1L, getNode(a, _).getTimestamp());
	assertEquals(1L, getNode(a, _).getMonitor().getTimestamp());
    }

    @Test
    public void firstEvent_ab_b_monitorABisDead() throws Exception {
	// exercise
	pm.processEvent(fsm.e2.createEvent(a, b)); // instance ab is disabled via timestamp
	pm.processEvent(fsm.e3.createEvent(b)); // binding b is disabled

	// verify
	assertDeadMonitor(a, b);

    }

    @Test
    public void firstEvent_ab_b_deadMonitorIsCreatedForab() throws Exception {
	// exercise
	pm.processEvent(fsm.e2.createEvent(a, b));
	pm.processEvent(fsm.e3.createEvent(b));

	// verify
	assertDeadMonitor(a, b);
    }

    @Test
    public void twoEvents_ab_b_disableSetPreventsCreationOfBothMonitors() throws Exception {
	// exercise
	pm.processEvent(fsm.e2.createEvent(a, b));
	pm.processEvent(fsm.e3.createEvent(b));

	// verify
	assertNoMoreCreatedMonitors();
    }

    // twoEvent_ab_a = ab followed by a (and some with b) ////////////////////////////////

    /*
     * ab is in the disabling set and disables any traces with a, so we will not see any monitors created associated to
     * the instance a
     */

    @Test
    public void moreEvents_e1a_e2ab_e1a_tracesAreCorrect() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a));
	pm.processEvent(fsm.e2.createEvent(a, b));
	pm.processEvent(fsm.e1.createEvent(a));

	// verify
	assertTrace(getMonitor(a, _), fsm.e1, fsm.e1);
	assertTrace(getMonitor(a, b), fsm.e1, fsm.e2, fsm.e1);
    }

    @Test
    public void moreEvents_e1a_e2ab_e3b_numberOfMonitorSetsIsCorrect() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a));
	pm.processEvent(fsm.e2.createEvent(a, b));
	pm.processEvent(fsm.e3.createEvent(b));

	// verify
	assertEquals(1, getNode(array(a, _)).getMonitorSets().length);
	assertEquals(1, getNode(array(_, b)).getMonitorSets().length);
	assertEquals(0, getNode(array(a, _), array(_, b)).getMonitorSets().length);
    }

    @Test
    public void moreEvents_e1a_e2ab_e3b_tracesAreCorrect() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a));
	pm.processEvent(fsm.e2.createEvent(a, b));
	pm.processEvent(fsm.e3.createEvent(b));

	// verify
	assertTrace(getMonitor(a, _), fsm.e1);
	assertTrace(getMonitor(a, b), fsm.e1, fsm.e2, fsm.e3);
    }

    @Test
    public void moreEvents_e1a_e2ab_e3b_monitorForBisDead() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a));
	pm.processEvent(fsm.e2.createEvent(a, b));
	pm.processEvent(fsm.e3.createEvent(b));

	// verify
	assertNodeExists(getNode(null, b));
	assertDeadMonitor(null, b);
    }

    @Test
    public void moreEvents_e1a_e2ab_e1a_e3b_tracesAreCorrect() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a));
	pm.processEvent(fsm.e2.createEvent(a, b));
	pm.processEvent(fsm.e1.createEvent(a));
	pm.processEvent(fsm.e3.createEvent(b));

	// verify
	assertTrace(popNextCreatedMonitor(), fsm.e1, fsm.e1);
	assertTrace(popNextCreatedMonitor(), fsm.e1, fsm.e2, fsm.e1, fsm.e3);
    }

    @Test
    public void moreEvents_e1a_e2ab_e3b_e1a_matchIsDetected() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a));
	assertTrue(fsm.matchHandler.getHandledMatches().isEmpty());
	pm.processEvent(fsm.e2.createEvent(a, b));
	assertTrue(fsm.matchHandler.getHandledMatches().isEmpty());
	pm.processEvent(fsm.e1.createEvent(a));
	assertTrue(fsm.matchHandler.getHandledMatches().isEmpty());
	pm.processEvent(fsm.e3.createEvent(b));

	// verify
	assertTrue(!fsm.matchHandler.getHandledMatches().isEmpty());
    }

}
