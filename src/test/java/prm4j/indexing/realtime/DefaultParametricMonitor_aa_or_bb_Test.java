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
import org.junit.Ignore;
import org.junit.Test;

import prm4j.api.Alphabet;
import prm4j.api.Parameter;
import prm4j.api.Symbol1;
import prm4j.api.fsm.FSM;
import prm4j.api.fsm.FSMSpec;
import prm4j.api.fsm.FSMState;
import prm4j.indexing.realtime.AwareMatchHandler.AwareMatchHandler1;
import prm4j.spec.finite.FiniteSpec;

/**
 * TODO This pattern can not be handled properly for the moment. Needs different calculation of enable sets with added
 * consideration of events with are not in the trace slice but disable other trace slices. This is out of conceptual
 * scope in the original theory due to trace slice semantics.
 */
public class DefaultParametricMonitor_aa_or_bb_Test extends AbstractDefaultParametricMonitorTest {

    public static class FSM_aa_or_bb {

	public final Alphabet alphabet = new Alphabet();

	public final Parameter<String> p1 = alphabet.createParameter("p1", String.class);
	public final Parameter<String> p2 = alphabet.createParameter("p2", String.class);

	public final Symbol1<String> e1 = alphabet.createSymbol1("e1", p1);
	public final Symbol1<String> e2 = alphabet.createSymbol1("e2", p2);

	public final AwareMatchHandler1<String> matchHandler = AwareMatchHandler.create(p1);

	public final FSM fsm = new FSM(alphabet);

	public final FSMState initial = fsm.createInitialState();
	public final FSMState s1 = fsm.createState();
	public final FSMState s2 = fsm.createState();
	public final FSMState error = fsm.createAcceptingState(matchHandler);

	public FSM_aa_or_bb() {
	    initial.addTransition(e1, s1);
	    initial.addTransition(e2, s2);
	    s1.addTransition(e1, error);
	    s2.addTransition(e2, error);
	}

    }

    FSM_aa_or_bb fsm;
    final String a1 = "a1";
    final String b1 = "b1";
    final String a2 = "a2";
    final String b2 = "b2";

    @Before
    public void init() {
	fsm = new FSM_aa_or_bb();
	FiniteSpec finiteSpec = new FSMSpec(fsm.fsm);
	createDefaultParametricMonitorWithAwareComponents(finiteSpec);
    }

    @Test
    public void a1a1_match() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a1));
	pm.processEvent(fsm.e1.createEvent(a1));

	// verify
	assertEquals(1, fsm.matchHandler.getHandledMatches().size());
    }

    @Test
    public void a1a2_no_match() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a1));
	pm.processEvent(fsm.e1.createEvent(a2));

	// verify
	assertEquals(0, fsm.matchHandler.getHandledMatches().size());
    }

    @Ignore("Feature not implemented")
    @Test
    public void a1b1_createsCombination() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a1));
	pm.processEvent(fsm.e2.createEvent(b1));

	// verify
	// a combination is not created because parameter nodes for a1 and b1 are both leafnodes.
	// this is a normal result of the enable set analysis.
	assertNodeExists(getNode(a1, b1));
    }

}
