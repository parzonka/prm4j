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

import org.junit.Before;
import org.junit.Test;

import prm4j.api.Alphabet;
import prm4j.api.Parameter;
import prm4j.api.Symbol1;
import prm4j.api.Symbol2;
import prm4j.api.fsm.FSM;
import prm4j.api.fsm.FSMSpec;
import prm4j.api.fsm.FSMState;
import prm4j.indexing.realtime.AwareMatchHandler.AwareMatchHandler1;
import prm4j.spec.FiniteSpec;

public class DefaultParametricMonitor_a_ab_Test extends AbstractDefaultParametricMonitorTest {

    public static class FSM_a_ab {

	public final Alphabet alphabet = new Alphabet();

	public final Parameter<String> p1 = alphabet.createParameter("p1", String.class);
	public final Parameter<String> p2 = alphabet.createParameter("p2", String.class);

	public final Symbol1<String> e1 = alphabet.createSymbol1("e1", p1);
	public final Symbol2<String, String> e2 = alphabet.createSymbol2("e1", p1, p2);

	public final AwareMatchHandler1<String> matchHandler = AwareMatchHandler.create(p1);

	public final FSM fsm = new FSM(alphabet);

	public final FSMState initial = fsm.createInitialState();
	public final FSMState s1 = fsm.createState();
	public final FSMState error = fsm.createAcceptingState(matchHandler);

	public FSM_a_ab() {
	    initial.addTransition(e1, s1);
	    s1.addTransition(e2, error);
	}

    }

    FSM_a_ab fsm;
    final String a1 = "a1";
    final String b1 = "b1";
    final String a2 = "a2";
    final String b2 = "b2";

    @Before
    public void init() {
	fsm = new FSM_a_ab();
	FiniteSpec finiteSpec = new FSMSpec(fsm.fsm);
	createDefaultParametricMonitorWithAwareComponents(finiteSpec);
    }

    @Test
    public void a1_a1b1_match() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a1));
	pm.processEvent(fsm.e2.createEvent(a1, b1));

	// verify
	assertEquals(1, fsm.matchHandler.getHandledMatches().size());
    }

    @Test
    public void a1b1_a1_a1b1_nomatch() throws Exception {
	// exercise
	pm.processEvent(fsm.e2.createEvent(a1, b1));
	pm.processEvent(fsm.e1.createEvent(a1));
	pm.processEvent(fsm.e2.createEvent(a1, b1));

	// verify
	assertEquals(0, fsm.matchHandler.getHandledMatches().size());
    }

    @Test
    public void a1_a1_a1b1_nomatch() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a1));
	pm.processEvent(fsm.e1.createEvent(a1));
	pm.processEvent(fsm.e2.createEvent(a1, b1));

	// verify
	assertEquals(0, fsm.matchHandler.getHandledMatches().size());
    }

    @Test
    public void a1_a2_a1b1_match() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a1));
	pm.processEvent(fsm.e1.createEvent(a2));
	pm.processEvent(fsm.e2.createEvent(a1, b1));

	// verify
	assertEquals(1, fsm.matchHandler.getHandledMatches().size());
    }

    @Test
    public void a2b1_a1_a1b1_match() throws Exception {
	// exercise
	pm.processEvent(fsm.e2.createEvent(a2, b1));
	pm.processEvent(fsm.e1.createEvent(a1));
	pm.processEvent(fsm.e2.createEvent(a1, b1));

	// verify
	if (ALGORITHM_D_FIXED) {
	    assertEquals(1, fsm.matchHandler.getHandledMatches().size());
	} else {
	    assertEquals(0, fsm.matchHandler.getHandledMatches().size());
	}
    }

    @Test
    public void a1b2_a1_a1b1_match() throws Exception {
	// exercise
	pm.processEvent(fsm.e2.createEvent(a1, b2));
	pm.processEvent(fsm.e1.createEvent(a1));
	pm.processEvent(fsm.e2.createEvent(a1, b1));

	// verify
	if (ALGORITHM_D_FIXED) {
	    assertEquals(1, fsm.matchHandler.getHandledMatches().size());
	} else {
	    assertEquals(0, fsm.matchHandler.getHandledMatches().size());
	}
    }

}
