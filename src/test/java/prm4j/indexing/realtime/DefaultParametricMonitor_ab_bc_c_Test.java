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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static prm4j.Util.tuple;

import org.junit.Before;
import org.junit.Test;

import prm4j.api.fsm.FSMSpec;
import prm4j.indexing.model.ModelVerifier;
import prm4j.indexing.node.NullNode;
import prm4j.spec.FiniteSpec;

public class DefaultParametricMonitor_ab_bc_c_Test extends AbstractDefaultParametricMonitorTest {

    FSM_ab_bc_c fsm;
    final String a1 = "a1";
    final String b1 = "b1";
    final String c1 = "c1";

    @Before
    public void init() {
	fsm = new FSM_ab_bc_c();
	FiniteSpec finiteSpec = new FSMSpec(fsm.fsm);
	createDefaultParametricMonitorWithAwareComponents(finiteSpec);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void model() throws Exception {
	ModelVerifier modelVerifier = new ModelVerifier(processor);
	modelVerifier.joinOverParameterSets(fsm.e2, list(asSet(fsm.a, fsm.b)));
    }

    // firstEvent_ab //////////////////////////////////////////////////////////////////

    @Test
    public void firstEvent_ab_createsOnlyOneMonitor() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a1, b1));

	// verify
	popNextCreatedMonitor();
	assertNoMoreCreatedMonitors();
    }

    @Test
    public void firstEvent_ab_updatesOnlyOneMonitor() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a1, b1));

	// verify
	popNextUpdatedMonitor();
	assertNoMoreUpdatedMonitors();
    }

    @Test
    public void firstEvent_ab_retrievesTwoNodes() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a1, b1));

	// verify
	popNextRetrievedNode();
	popNextRetrievedNode();
	assertNoMoreRetrievedNodes();
    }

    @Test
    public void firstEvent_ab_nodesAreNotNullNodes() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a1, b1));

	// verify
	assertNotSame(NullNode.instance, popNextRetrievedNode());
	assertNotSame(NullNode.instance, popNextRetrievedNode());
    }

    @Test
    public void firstEvent_ab_createsMonitorWithCreationTime0() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a1, b1));

	// verify
	assertEquals(0L, popNextUpdatedMonitor().getTimestamp());
    }

    @Test
    public void firstEvent_ab_createsCorrectTrace() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a1, b1));

	// verify
	assertTrace(popNextUpdatedMonitor(), fsm.e1);
    }

    @Test
    public void firstEvent_ab_monitorBindsAllItsParameters() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a1, b1));

	// verify
	assertBoundObjects(popNextUpdatedMonitor(), a1, b1);
    }

    @Test
    public void firstEvent_ab_noMatchDetected() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a1, b1));

	// verify
	assertTrue(fsm.matchHandler.getHandledMatches().isEmpty());
    }

    @Test
    public void firstEvent_ab_nodesHaveDifferentParameterNodes() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a1, b1));

	// verify
	assertNotSame(getNode(a1, null, null).getParameterNode(), getNode(null, b1, null).getParameterNode());
	assertNotSame(getNode(a1, b1, null).getParameterNode(), getNode(null, b1, null).getParameterNode());
	assertNotSame(getNode(a1, b1, null).getParameterNode(), getNode(a1, null, null).getParameterNode());
    }

    @Test
    public void firstEvent_ab_chainingIsPerformedCorrectly() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a1, b1));

	// verify
	assertEquals(0, getNode(a1, null, null).getMonitorSets().length);
	assertEquals(1, getNode(null, b1, null).getMonitorSets().length);
	assertEquals(1, getNode(null, b1, null).getMonitorSet(0).getSize());
    }

    // twoEvents ab and bc //////////////////////////////////////////////////////////////////

    /*
     * We test if joining works
     */

    @Test
    public void joining_ab_bc_createsCorrectMonitors() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a1, b1));
	pm.processEvent(fsm.e2.createEvent(b1, c1));

	// verify
	assertBoundObjects(popNextCreatedMonitor(), a1, b1);
	assertBoundObjects(popNextCreatedMonitor(), a1, b1, c1);
	assertNoMoreCreatedMonitors();
    }

    @Test
    public void joining_ab_bc_createsCorrectNodes() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a1, b1));
	pm.processEvent(fsm.e2.createEvent(b1, c1));

	// verify
	assertCreatedNodes(array(a1, null, null), array(null, b1, null), array(null, null, c1), array(a1, b1, null),
		array(null, b1, c1), array(a1, b1, c1));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void finiteParametricProperty_ensureCorrectUpdates() throws Exception {

	// verify
	assertEquals(
		asSet(tuple(asSet(fsm.a, fsm.b), asSet(fsm.a, fsm.b, fsm.c)),
			tuple(asSet(fsm.b, fsm.c), asSet(fsm.a, fsm.b, fsm.c)),
			tuple(asSet(fsm.c), asSet(fsm.a, fsm.b, fsm.c))), fpp.getUpdates());
    }

    @Test
    public void joining_ab_bc_chainingFromABtoABCexists() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a1, b1));
	pm.processEvent(fsm.e2.createEvent(b1, c1));

	// verify
	assertChaining(array(a1, b1, null), array(a1, b1, c1));
    }

    @Test
    public void joining_ab_bc_chainingFromCtoABCexists() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a1, b1));
	pm.processEvent(fsm.e2.createEvent(b1, c1));

	// verify
	assertChaining(array(null, null, c1), array(a1, b1, c1));
    }

    @Test
    public void joining_ab_bc_chainingFromBCtoABCexists() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a1, b1));
	pm.processEvent(fsm.e2.createEvent(b1, c1));

	// verify
	assertChaining(array(null, b1, c1), array(a1, b1, c1));
    }

    @Test
    public void joining_ab_bc_correctTimestamps() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a1, b1));
	pm.processEvent(fsm.e2.createEvent(b1, c1));

	// verify
	assertEquals(0L, getNode(a1, b1, _).getTimestamp());
	assertEquals(0L, getNode(a1, b1, _).getMonitor().getTimestamp());
	assertEquals(1L, getNode(_, b1, c1).getTimestamp());
	assertEquals(1L, getNode(_, b1, c1).getMonitor().getTimestamp());
    }

    @Test
    public void joining_ab_bc_joinedMonitorExists1() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a1, b1));
	pm.processEvent(fsm.e2.createEvent(b1, c1));

	// verify
	assertMonitorExistsAndIsNotTheDeadMonitor(getMonitor(a1, b1, c1));
    }

    @Test
    public void joining_ab_bc_joinedMonitorExists2() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent("x", "y"));
	pm.processEvent(fsm.e1.createEvent(a1, b1));
	pm.processEvent(fsm.e2.createEvent(b1, c1));

	// verify
	assertMonitorExistsAndIsNotTheDeadMonitor(getMonitor(a1, b1, c1));
    }

    @Test
    public void joining_ab_bc_joinedMonitorHasCorrectTimestamp() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent("x", "y")); // time = 0
	pm.processEvent(fsm.e1.createEvent(a1, b1)); // time = 1
	pm.processEvent(fsm.e2.createEvent(b1, c1)); // derives 1 from (a, b)

	// verify
	assertEquals(1L, getNode(a1, b1, c1).getMonitor().getTimestamp());
    }

    @Test
    public void joining_ab_bc_assertCorrectTraces() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a1, b1));
	pm.processEvent(fsm.e2.createEvent(b1, c1));

	// verify
	assertTrace(popNextCreatedMonitor(), fsm.e1);
	assertTrace(popNextCreatedMonitor(), fsm.e1, fsm.e2);
    }

    // moreEvents //////////////////////////////////////////////////////////////////

    @Test
    public void moreEvents_ab_bc_c_matchesTrace() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a1, b1));
	assertTrue(fsm.matchHandler.getHandledMatches().isEmpty());
	pm.processEvent(fsm.e2.createEvent(b1, c1));
	assertTrue(fsm.matchHandler.getHandledMatches().isEmpty());
	pm.processEvent(fsm.e3.createEvent(c1));

	// verify
	assertTrue(!fsm.matchHandler.getHandledMatches().isEmpty());
    }

    @Test
    public void moreEvents_ab_bc_c_bc_ab_c_correctTrace() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a1, b1));
	pm.processEvent(fsm.e2.createEvent(b1, c1));
	pm.processEvent(fsm.e3.createEvent(c1));
	pm.processEvent(fsm.e2.createEvent(b1, c1));
	pm.processEvent(fsm.e1.createEvent(a1, b1));
	pm.processEvent(fsm.e3.createEvent(c1));

	// verify
	assertTrace(popNextCreatedMonitor(), fsm.e1, fsm.e1);
	assertTrace(popNextCreatedMonitor(), fsm.e1, fsm.e2, fsm.e3);
	// no more matches since the accepting state is a final state, and is cleaned from the monitor set
    }

    @Test
    public void moreEvents_ab_bc_c_c_matchesOnlyOneTrace() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a1, b1));
	pm.processEvent(fsm.e2.createEvent(b1, c1));
	pm.processEvent(fsm.e3.createEvent(c1));
	pm.processEvent(fsm.e3.createEvent(c1));

	// verify
	assertEquals(1, fsm.matchHandler.getHandledMatches().size());
    }

    @Test
    public void moreEvents_ab_bc_c_ab_bc_c_matchesOnlyOneTrace() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a1, b1));
	pm.processEvent(fsm.e2.createEvent(b1, c1));
	pm.processEvent(fsm.e3.createEvent(c1));
	pm.processEvent(fsm.e1.createEvent(a1, b1));
	pm.processEvent(fsm.e2.createEvent(b1, c1));
	pm.processEvent(fsm.e3.createEvent(c1));

	// verify
	assertEquals(1, fsm.matchHandler.getHandledMatches().size());
    }

    // disabling //////////////////////////////////////////////////////////////////

    @Test
    public void disabling_bc_singleNodeIsCreated() throws Exception {
	// exercise
	pm.processEvent(fsm.e2.createEvent(b1, c1));

	// verify
	assertCreatedNodes(array(_, b1, c1));
    }

    @Test
    public void disabling_bc_ab_noMonitorsAreCreated() throws Exception {

	// exercise
	pm.processEvent(fsm.e2.createEvent(b1, c1));
	pm.processEvent(fsm.e1.createEvent(a1, b1));

	// verify
	assertDeadMonitor(_, b1, c1);
	assertNumberOfCreatedMonitors(1);
	assertMonitorExistsAndIsNotTheDeadMonitor(getMonitor(a1, b1, _));
    }

    @Test
    public void disabling_bc_ab_noNodesAreCreated() throws Exception {
	// exercise
	pm.processEvent(fsm.e2.createEvent(b1, c1));
	pm.processEvent(fsm.e1.createEvent(a1, b1));

	// verify
	assertNodeExists(getNode(_, b1, c1));
	assertNodeExists(getNode(a1, b1, _));
	assertNumberOfCreatedNodes(4);
    }

}
