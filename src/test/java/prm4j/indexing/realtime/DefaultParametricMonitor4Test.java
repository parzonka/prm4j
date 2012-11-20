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

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

import prm4j.api.fsm.FSMSpec;
import prm4j.spec.FiniteSpec;

public class DefaultParametricMonitor4Test extends AbstractDefaultParametricMonitorTest {

    FSM_ab_b_with_initial_b_loop fsm;
    final String a = "a";
    final String b = "b";
    final String c = "c";

    @Before
    public void init() {
	fsm = new FSM_ab_b_with_initial_b_loop();
	FiniteSpec finiteSpec = new FSMSpec(fsm.fsm);
	createDefaultParametricMonitorWithAwareComponents(finiteSpec);
    }

    // firstEvent_b //////////////////////////////////////////////////////////////////

    @Test
    public void firstEvent_b_createsNoMonitors() throws Exception {
	// exercise
	pm.processEvent(fsm.e2.createEvent(b));

	// verify
	assertNoMoreCreatedMonitors();
    }

    @Test
    public void firstEvent_b_createsNoNodes() throws Exception {
	// exercise
	pm.processEvent(fsm.e2.createEvent(b));

	// verify
	assertCreatedNodes();
    }

    @Test
    public void firstEvent_b_bNodeHasCorrectMonitorSets() throws Exception {
	// exercise
	pm.processEvent(fsm.e2.createEvent(b));

	// verify
	assertEquals(0, getNode().getMonitorSets().length);
    }

    @Test
    public void firstEvent_b_a_createsNoNodes() throws Exception {
	// exercise
	pm.processEvent(fsm.e2.createEvent(b));
	pm.processEvent(fsm.e1.createEvent(a, b));

	// verify
	assertCreatedNodes(array(a, null), array(null, b), array(a, b));
    }

}
