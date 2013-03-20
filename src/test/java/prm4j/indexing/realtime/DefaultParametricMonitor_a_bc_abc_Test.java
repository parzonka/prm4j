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
import prm4j.spec.finite.FiniteSpec;

public class DefaultParametricMonitor_a_bc_abc_Test extends AbstractDefaultParametricMonitorTest {

    public static class FSM_a_bc_abc {

	public final Alphabet alphabet = new Alphabet();

	public final Parameter<String> p1 = alphabet.createParameter("p1", String.class);
	public final Parameter<String> p2 = alphabet.createParameter("p2", String.class);
	public final Parameter<String> p3 = alphabet.createParameter("p2", String.class);

	public final Symbol1<String> a = alphabet.createSymbol1("a", p1);
	public final Symbol2<String, String> ab = alphabet.createSymbol2("ab", p1, p2);
	public final Symbol2<String, String> bc = alphabet.createSymbol2("bc", p2, p3);

	public final AwareMatchHandler1<String> matchHandler = AwareMatchHandler.create(p1);

	public final FSM fsm = new FSM(alphabet);

	public final FSMState initial = fsm.createInitialState();
	public final FSMState s1 = fsm.createState();
	public final FSMState error = fsm.createAcceptingState(matchHandler);

	public FSM_a_bc_abc() {
	    initial.addTransition(a, s1);
	    s1.addTransition(bc, error);
	}

    }

    FSM_a_bc_abc fsm;
    final String a1 = "a1";
    final String b1 = "b1";
    final String c1 = "c1";
    final String a2 = "a2";
    final String b2 = "b2";
    final String c2 = "c2";

    @Before
    public void init() {
	fsm = new FSM_a_bc_abc();
	FiniteSpec finiteSpec = new FSMSpec(fsm.fsm);
	createDefaultParametricMonitorWithAwareComponents(finiteSpec);
    }

    @Test
    public void a1_b1c1_match() throws Exception {
	// exercise
	pm.processEvent(fsm.a.createEvent(a1));
	pm.processEvent(fsm.bc.createEvent(b1, c1));

	// verify
	assertEquals(1, fsm.matchHandler.getHandledMatches().size());
    }

    @Test
    public void a1_a1b1_expectDeadMonitor() throws Exception {
	// exercise
	pm.processEvent(fsm.a.createEvent(a1));
	pm.processEvent(fsm.ab.createEvent(a1, b1));

	// verify
	assertDeadMonitor(a1, b1, _);
    }

    @Test
    public void a1_a1b1_b1c1_nomatch() throws Exception {
	// exercise
	pm.processEvent(fsm.a.createEvent(a1));
	pm.processEvent(fsm.ab.createEvent(a1, b1));
	pm.processEvent(fsm.bc.createEvent(b1, c1));

	// verify
	assertEquals(0, fsm.matchHandler.getHandledMatches().size());
    }

}
