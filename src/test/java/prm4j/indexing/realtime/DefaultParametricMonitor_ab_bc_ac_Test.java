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

import org.junit.Before;
import org.junit.Test;

import prm4j.api.fsm.FSMSpec;
import prm4j.spec.FiniteSpec;

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
    public void disableEventPreventsMatch2() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a1, b1));
	pm.processEvent(fsm.e3.createEvent(a1, c1)); // disable c1
	pm.processEvent(fsm.e2.createEvent(b1, c1));
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

	// verify
	if (ALGORITHM_D_FIXED) {
	    assertEquals(1, fsm.matchHandler.getHandledMatches().size());
	} else {
	    assertEquals(0, fsm.matchHandler.getHandledMatches().size());
	}
    }

}
