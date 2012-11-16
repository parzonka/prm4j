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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static prm4j.Util.tuple;

import org.junit.Before;
import org.junit.Test;

import prm4j.api.fsm.FSMSpec;
import prm4j.indexing.BaseMonitor;
import prm4j.spec.FiniteSpec;

public class DefaultParametricMonitor3Test extends AbstractDefaultParametricMonitorTest {

    FSM_ab_bc_c fsm;
    final String a = "a";
    final String b = "b";
    final String c = "c";

    @Before
    public void init() {
	fsm = new FSM_ab_bc_c();
	FiniteSpec finiteSpec = new FSMSpec(fsm.fsm);
	createDefaultParametricMonitorWithAwareComponents(finiteSpec);
    }

    // firstEvent_ab //////////////////////////////////////////////////////////////////

    @Test
    public void firstEvent_ab_createsOnlyOneMonitor() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a, b));

	// verify
	popNextCreatedMonitor();
	assertNoMoreCreatedMonitors();
    }

    @Test
    public void firstEvent_ab_updatesOnlyOneMonitor() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a, b));

	// verify
	popNextUpdatedMonitor();
	assertNoMoreUpdatedMonitors();
    }

    @Test
    public void firstEvent_ab_createsMonitorWithCreationTime0() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a, b));

	// verify
	assertEquals(0L, popNextUpdatedMonitor().getCreationTime());
    }

    @Test
    public void firstEvent_ab_createsCorrectTrace() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a, b));

	// verify
	assertTrace(popNextUpdatedMonitor(), fsm.e1);
    }

    @Test
    public void firstEvent_ab_monitorBindsAllItsParameters() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a, b));

	// verify
	assertBoundObjects(popNextUpdatedMonitor(), a, b);
    }

    @Test
    public void firstEvent_ab_noMatchDetected() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a, b));

	// verify
	assertTrue(fsm.matchHandler.getHandledMatches().isEmpty());
    }

    @Test
    public void firstEvent_ab_chainingIsPerformedCorrectly() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a, b));

	// verify
	assertEquals(0, getNode(a, null, null).getMonitorSets().length);
	assertEquals(1, getNode(null, b, null).getMonitorSets().length);
	assertEquals(1, getNode(null, b, null).getMonitorSet(0).getSize());
	getNode(null, b, null).getMonitorSet(0).contains(getNode(a, b, null).getMonitor());
    }

    // twoEvents ab and bc //////////////////////////////////////////////////////////////////

    /*
     * We test if joining works
     */

    @Test
    public void joining_ab_bc_createsCorrectMonitors() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a, b));
	pm.processEvent(fsm.e2.createEvent(b, c));

	// verify
	assertBoundObjects(popNextCreatedMonitor(), a, b);
	assertBoundObjects(popNextCreatedMonitor(), a, b, c);
	assertNoMoreCreatedMonitors();
    }

    @Test
    public void joining_ab_bc_createsCorrectNodes() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a, b));
	pm.processEvent(fsm.e2.createEvent(b, c));

	// verify
	assertCreatedNodes(array(a, null, null), array(null, b, null), array(null, null, c), array(a, b, null),
		array(null, b, c), array(a, b, c));
    }

    @Test
    public void joining_ab_bc_chainingFromABtoABCexists() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a, b));
	pm.processEvent(fsm.e2.createEvent(b, c));

	// verify
	assertChaining(array(a, b, null), array(a, b, c));
    }

    @Test
    public void joining_ab_bc_chainingFromCtoBCexists() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a, b));
	pm.processEvent(fsm.e2.createEvent(b, c));

	// verify
	assertChaining(array(null, null, c), array(null, b, c));
    }

}
