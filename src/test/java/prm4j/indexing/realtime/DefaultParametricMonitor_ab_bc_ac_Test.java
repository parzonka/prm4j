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
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

import prm4j.api.fsm.FSMSpec;
import prm4j.spec.finite.FiniteSpec;

public class DefaultParametricMonitor_ab_bc_ac_Test extends AbstractDefaultParametricMonitorTest {

    FSM_ab_bc_ac fsm;
    final String a1 = "a1";
    final String b1 = "b1";
    final String c1 = "c1";
    final String a2 = "a2";
    final String b2 = "b2";
    final String c2 = "c2";

    @Before
    public void init() {
	fsm = new FSM_ab_bc_ac();
	FiniteSpec finiteSpec = new FSMSpec(fsm.fsm);
	createDefaultParametricMonitorWithAwareComponents(finiteSpec);
    }

    // firstEvent //////////////////////////////////////////////////////////////////

    @Test
    public void simpleMatch() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a1, b1));
	pm.processEvent(fsm.e2.createEvent(b1, c1));
	pm.processEvent(fsm.e3.createEvent(a1, c1));

	// verify
	assertEquals(1, fsm.matchHandler.getHandledMatches().size());
    }

    @Test
    public void disableEventPreventsMatch1() throws Exception {
	// exercise
	pm.processEvent(fsm.e2.createEvent(b1, c1));
	pm.processEvent(fsm.e1.createEvent(a1, b1));
	pm.processEvent(fsm.e2.createEvent(b1, c1));
	pm.processEvent(fsm.e3.createEvent(a1, c1));

	// verify
	assertEquals(0, fsm.matchHandler.getHandledMatches().size());
    }

    @Test
    public void a1b1_a1c1_nodeA1C1isCreated() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a1, b1));
	pm.processEvent(fsm.e3.createEvent(a1, c1));

	// verify
	assertNodeExists(getNode(a1, b1, _));
	assertNodeExists(getNode(a1, _, c1));
    }

    @Test
    public void a1b1_a1c1_nodeA1B1C1isNotCreated() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a1, b1));
	pm.processEvent(fsm.e3.createEvent(a1, c1));

	// verify
	assertNullNode(getNode(a1, b1, c1));
    }

    @Test
    public void a1b1_a1c1_correctMonitorsAreCreated() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a1, b1));
	pm.processEvent(fsm.e3.createEvent(a1, c1));

	// verify
	assertMonitorExistsAndIsNotTheDeadMonitor(getMonitor(a1, b1, _));
	assertDeadMonitor(a1, _, c1);
    }

    @Test
    public void a1b1_a1c1_correctTimestamps() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a1, b1));
	pm.processEvent(fsm.e3.createEvent(a1, c1));

	// verify
	assertEquals(0L, getNode(a1, b1, _).getTimestamp());
	assertEquals(0L, getNode(a1, b1, _).getMonitor().getTimestamp());
	assertEquals(1L, getNode(a1, _, c1).getTimestamp());
	assertEquals(1L, getNode(a1, _, c1).getMonitor().getTimestamp());
    }

    @Test
    public void monitorForABC() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a1, b1));
	pm.processEvent(fsm.e3.createEvent(a1, c1)); // disable a1c1 via timestamp
	pm.processEvent(fsm.e2.createEvent(b1, c1)); // monitor for a1b1c1 is not created since a1c1 already defined

	// verify
	assertNull(getNode(a1, b1, c1).getMonitor());
    }

    @Test
    public void disableEventPreventsMatch2() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a1, b1));
	pm.processEvent(fsm.e3.createEvent(a1, c1)); // disable a1c1 via timestamp
	pm.processEvent(fsm.e2.createEvent(b1, c1)); // monitor for a1b1c1 is not created since a1c1 already defined
	pm.processEvent(fsm.e3.createEvent(a1, c1));

	// verify
	assertEquals(0, fsm.matchHandler.getHandledMatches().size());
    }

    @Test
    public void disableEventFromOtherTraceDoesNotDisableMatch() throws Exception {
	// exercise
	pm.processEvent(fsm.e2.createEvent(b1, c2)); // uses c2, should not disable trace slice for a1b1c1
	pm.processEvent(fsm.e1.createEvent(a1, b1));
	pm.processEvent(fsm.e2.createEvent(b1, c1));
	pm.processEvent(fsm.e3.createEvent(a1, c1));

	assertEquals(1, fsm.matchHandler.getHandledMatches().size());
    }

}
