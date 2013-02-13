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

public class DefaultParametricMonitor_ab_bc_cd_ad_Test extends AbstractDefaultParametricMonitorTest {

    FSM_ab_bc_cd_ad fsm;
    final String a1 = "a1";
    final String b1 = "b1";
    final String c1 = "c1";
    final String d1 = "d1";
    final String a2 = "a2";
    final String b2 = "b2";
    final String c2 = "c2";
    final String d2 = "d2";

    @Before
    public void init() {
	fsm = new FSM_ab_bc_cd_ad();
	FiniteSpec finiteSpec = new FSMSpec(fsm.fsm);
	createDefaultParametricMonitorWithAwareComponents(finiteSpec);
    }

    @Test
    public void ab_bc_cd_ad_simpleMatch() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a1, b1));
	pm.processEvent(fsm.e2.createEvent(b1, c1));
	pm.processEvent(fsm.e3.createEvent(c1, d1));
	pm.processEvent(fsm.e4.createEvent(a1, d1));

	assertEquals(1, fsm.matchHandler.getHandledMatches().size());
    }

    @Test
    public void ab_bc_cd_ad_disableMatch1() throws Exception {
	// exercise
	pm.processEvent(fsm.e3.createEvent(c1, d1));
	pm.processEvent(fsm.e1.createEvent(a1, b1));
	pm.processEvent(fsm.e2.createEvent(b1, c1));
	pm.processEvent(fsm.e3.createEvent(c1, d1));
	pm.processEvent(fsm.e4.createEvent(a1, d1));

	assertEquals(0, fsm.matchHandler.getHandledMatches().size());
    }

    @Test
    public void ab_bc_cd_ad_disableMatch2() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a1, b1));
	pm.processEvent(fsm.e3.createEvent(c1, d1));
	pm.processEvent(fsm.e2.createEvent(b1, c1));
	pm.processEvent(fsm.e3.createEvent(c1, d1));
	pm.processEvent(fsm.e4.createEvent(a1, d1));

	assertEquals(0, fsm.matchHandler.getHandledMatches().size());
    }

    @Test
    public void ab_bc_cd_ad_interveningTraceSlice() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(c1, d2)); // an event from another traceslice
	pm.processEvent(fsm.e1.createEvent(a1, b1));
	pm.processEvent(fsm.e2.createEvent(b1, c1));
	pm.processEvent(fsm.e3.createEvent(c1, d1)); // c1d2 prevents creation of node c1d1 because the timestamp of c1
	// an improved version would need to store timestamps with nodes, not bindings
	pm.processEvent(fsm.e4.createEvent(a1, d1));

	if (ALGORITHM_D_FIXED) {
	    assertEquals(1, fsm.matchHandler.getHandledMatches().size());
	} else {
	    assertEquals(0, fsm.matchHandler.getHandledMatches().size());
	}
    }

}
