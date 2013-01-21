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
package prm4j.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import prm4j.api.Alphabet;
import prm4j.api.MatchHandler;
import prm4j.api.Parameter;
import prm4j.api.Symbol0;
import prm4j.api.Symbol1;
import prm4j.api.Symbol2;
import prm4j.api.fsm.FSM;
import prm4j.api.fsm.FSMState;
import prm4j.indexing.realtime.AwareMatchHandler;
import prm4j.indexing.realtime.AwareMatchHandler.AwareMatchHandler0;
import prm4j.indexing.realtime.AwareMatchHandler.AwareMatchHandler1;
import prm4j.indexing.realtime.AwareMatchHandler.AwareMatchHandler2;

public abstract class FSMDefinitions {

    @SuppressWarnings("rawtypes")
    public static class FSM_unsafeMapIterator {

	public final Alphabet alphabet = new Alphabet();

	public final Parameter<Map> m = alphabet.createParameter("m", Map.class);
	public final Parameter<Collection> c = alphabet.createParameter("c", Collection.class);
	public final Parameter<Iterator> i = alphabet.createParameter("i", Iterator.class);

	public final Symbol2<Map, Collection> createColl = alphabet.createSymbol2("createColl", m, c);
	public final Symbol2<Collection, Iterator> createIter = alphabet.createSymbol2("createIter", c, i);
	public final Symbol1<Map> updateMap = alphabet.createSymbol1("updateMap", m);
	public final Symbol1<Iterator> useIter = alphabet.createSymbol1("useIter", i);

	public final FSM fsm = new FSM(alphabet);

	public final AwareMatchHandler0 matchHandler = AwareMatchHandler.create();

	public final FSMState initial = fsm.createInitialState();
	public final FSMState s1 = fsm.createState();
	public final FSMState s2 = fsm.createState();
	public final FSMState s3 = fsm.createState();
	public final FSMState error = fsm.createAcceptingState(matchHandler);

	public FSM_unsafeMapIterator() {
	    initial.addTransition(createColl, s1);
	    initial.addTransition(updateMap, initial);
	    s1.addTransition(updateMap, s1);
	    s1.addTransition(createIter, s2);
	    s2.addTransition(useIter, s2);
	    s2.addTransition(updateMap, s3);
	    s3.addTransition(updateMap, s3);
	    s3.addTransition(useIter, error);
	}

    }

    /**
     * Do not call the hasNext method before the next method of an iterator.
     */
    @SuppressWarnings("rawtypes")
    public class FSM_HasNext {

	public final Alphabet alphabet = new Alphabet();

	public final Parameter<Iterator> i = alphabet.createParameter("i", Iterator.class);

	public final Symbol1<Iterator> hasNext = alphabet.createSymbol1("hasNext", i);
	public final Symbol1<Iterator> next = alphabet.createSymbol1("next", i);

	public final FSM fsm = new FSM(alphabet);

	public final AwareMatchHandler0 matchHandler = AwareMatchHandler.create();

	public final FSMState initial = fsm.createInitialState();
	public final FSMState safe = fsm.createState("safe");
	public final FSMState error = fsm.createAcceptingState(matchHandler, "error");

	public FSM_HasNext() {
	    initial.addTransition(hasNext, safe);
	    initial.addTransition(next, error);
	    safe.addTransition(hasNext, safe);
	    safe.addTransition(next, initial);
	}
    }

    @SuppressWarnings("rawtypes")
    public static class FSM_SafeSyncCollection {

	public final Alphabet alphabet = new Alphabet();

	public final Parameter<Collection> c = alphabet.createParameter("c", Collection.class);
	public final Parameter<Iterator> i = alphabet.createParameter("i", Iterator.class);

	public final Symbol1<Collection> sync = alphabet.createSymbol1("sync", c);
	public final Symbol2<Collection, Iterator> asyncCreateIter = alphabet.createSymbol2("asyncCreateIter", c, i);
	public final Symbol2<Collection, Iterator> syncCreateIter = alphabet.createSymbol2("syncCreateIter", c, i);
	public final Symbol1<Iterator> accessIter = alphabet.createSymbol1("accessIter", i);

	public final FSM fsm = new FSM(alphabet);

	public final AwareMatchHandler0 matchHandler = AwareMatchHandler.create();

	public final FSMState initial = fsm.createInitialState();
	public final FSMState s1 = fsm.createState();
	public final FSMState s2 = fsm.createState();
	public final FSMState error = fsm.createAcceptingState(matchHandler);

	public FSM_SafeSyncCollection() {
	    initial.addTransition(sync, s1);
	    s1.addTransition(asyncCreateIter, error);
	    s1.addTransition(syncCreateIter, s2);
	    s2.addTransition(accessIter, error);
	}

    }

    public static class FSM_SafeSyncCollection_NotRaw {

   	public final Alphabet alphabet = new Alphabet();


   	public final Parameter<Collection<Object>> c = alphabet.addParameter(new Parameter<Collection<Object>>("c"));
   	public final Parameter<Iterator<Object>> i = alphabet.addParameter(new Parameter<Iterator<Object>>("i"));

   	public final Symbol1<Collection<Object>> sync = alphabet.createSymbol1("sync", c);
   	public final Symbol2<Collection<Object>, Iterator<Object>> asyncCreateIter = alphabet.createSymbol2("asyncCreateIter", c, i);
   	public final Symbol2<Collection<Object>, Iterator<Object>> syncCreateIter = alphabet.createSymbol2("syncCreateIter", c, i);
   	public final Symbol1<Iterator<Object>> accessIter = alphabet.createSymbol1("accessIter", i);

   	public final FSM fsm = new FSM(alphabet);

   	public final AwareMatchHandler0 matchHandler = AwareMatchHandler.create();

   	public final FSMState initial = fsm.createInitialState();
   	public final FSMState s1 = fsm.createState();
   	public final FSMState s2 = fsm.createState();
   	public final FSMState error = fsm.createAcceptingState(matchHandler);

   	public FSM_SafeSyncCollection_NotRaw() {
   	    initial.addTransition(sync, s1);
   	    s1.addTransition(asyncCreateIter, error);
   	    s1.addTransition(syncCreateIter, s2);
   	    s2.addTransition(accessIter, error);
   	}

       }

    /**
     * Twos <b>A</b>s trigger the error state, a <b>B</b> will end in a dead state.
     */
    public static abstract class AbstractFSM_2symbols3states {

	public final Alphabet alphabet = new Alphabet();

	public final Symbol0 a = alphabet.createSymbol0("a");
	public final Symbol0 b = alphabet.createSymbol0("b");

	public final FSM fsm = new FSM(alphabet);

	public final FSMState initial = fsm.createInitialState();
	public final FSMState s1 = fsm.createState();
	public final FSMState error = fsm.createAcceptingState(MatchHandler.NO_OP);

	public AbstractFSM_2symbols3states() {
	    setupTransitions();
	}

	public abstract void setupTransitions();
    }

    /**
     * Watches for <code>e1e3</code> traces. Taken from
     * "Efficient Formalism-Independent Monitoring of Parametric Properties".
     */
    public static abstract class FSM_e1e3 {

	public final Alphabet alphabet = new Alphabet();

	public final Parameter<String> p1 = alphabet.createParameter("p1", String.class);
	public final Parameter<String> p2 = alphabet.createParameter("p2", String.class);

	public final Symbol1<String> e1 = alphabet.createSymbol1("e1", p1);
	public final Symbol1<String> e2 = alphabet.createSymbol1("e2", p2);
	public final Symbol2<String, String> e3 = alphabet.createSymbol2("e3", p1, p2);

	public final FSM fsm = new FSM(alphabet);

	public final FSMState initial = fsm.createInitialState();
	public final FSMState s1 = fsm.createState();
	public final FSMState error = fsm.createAcceptingState(MatchHandler.NO_OP);

	public FSM_e1e3() {
	    initial.addTransition(e1, s1);
	    initial.addTransition(e2, initial); // self-loop
	    s1.addTransition(e3, error);
	}

    }

    public static class FSM_a_a_a {

	public final Alphabet alphabet = new Alphabet();

	public final Parameter<String> p1 = alphabet.createParameter("p1", String.class);

	public final Symbol1<String> e1 = alphabet.createSymbol1("e1", p1);

	public final AwareMatchHandler1<String> matchHandler = AwareMatchHandler.create(p1);

	public final FSM fsm = new FSM(alphabet);

	public final FSMState initial = fsm.createInitialState();
	public final FSMState s1 = fsm.createState();
	public final FSMState s2 = fsm.createState();
	public final FSMState error = fsm.createAcceptingState(matchHandler);

	public FSM_a_a_a() {
	    initial.addTransition(e1, s1);
	    s1.addTransition(e1, s2);
	    s2.addTransition(e1, error);
	}

    }

    public static class FSM_obj_obj {

	public final Alphabet alphabet = new Alphabet();

	public final Parameter<Object> p1 = alphabet.createParameter("p1", Object.class);

	public final Symbol1<Object> e1 = alphabet.createSymbol1("e1", p1);

	public final AwareMatchHandler1<Object> matchHandler = AwareMatchHandler.create(p1);

	public final FSM fsm = new FSM(alphabet);

	public final FSMState initial = fsm.createInitialState();
	public final FSMState s1 = fsm.createState();
	public final FSMState error = fsm.createAcceptingState(matchHandler);

	public FSM_obj_obj() {
	    initial.addTransition(e1, s1);
	    s1.addTransition(e1, error);
	}

    }

    public static class FSM_a_ab_a_b {

	public final Alphabet alphabet = new Alphabet();

	public final Parameter<String> p1 = alphabet.createParameter("p1", String.class);
	public final Parameter<String> p2 = alphabet.createParameter("p2", String.class);

	public final Symbol1<String> e1 = alphabet.createSymbol1("e1", p1);
	public final Symbol2<String, String> e2 = alphabet.createSymbol2("e2", p1, p2);
	public final Symbol1<String> e3 = alphabet.createSymbol1("e3", p2);

	public final AwareMatchHandler2<String, String> matchHandler = AwareMatchHandler.create(p1, p2);

	public final FSM fsm = new FSM(alphabet);

	public final FSMState initial = fsm.createInitialState();
	public final FSMState s1 = fsm.createState();
	public final FSMState s2 = fsm.createState();
	public final FSMState s3 = fsm.createState();
	public final FSMState error = fsm.createAcceptingState(matchHandler);

	public FSM_a_ab_a_b() {
	    initial.addTransition(e1, s1);
	    s1.addTransition(e2, s2);
	    s2.addTransition(e1, s3);
	    s3.addTransition(e3, error);
	}
    }

    public static class FSM_ab_bc_c {

	public final Alphabet alphabet = new Alphabet();

	public final Parameter<String> p1 = alphabet.createParameter("p1", String.class);
	public final Parameter<String> p2 = alphabet.createParameter("p2", String.class);
	public final Parameter<String> p3 = alphabet.createParameter("p3", String.class);

	public final Symbol2<String, String> e1 = alphabet.createSymbol2("e1", p1, p2);
	public final Symbol2<String, String> e2 = alphabet.createSymbol2("e2", p2, p3);
	public final Symbol1<String> e3 = alphabet.createSymbol1("e3", p3);

	public final AwareMatchHandler2<String, String> matchHandler = AwareMatchHandler.create(p1, p3);

	public final FSM fsm = new FSM(alphabet);

	public final FSMState initial = fsm.createInitialState();
	public final FSMState s1 = fsm.createState();
	public final FSMState s2 = fsm.createState();
	public final FSMState error = fsm.createAcceptingState(matchHandler);

	public FSM_ab_bc_c() {
	    initial.addTransition(e1, s1);
	    s1.addTransition(e2, s2);
	    s2.addTransition(e3, error);
	}
    }

    /**
     * A sequence of as, with b destroying the trace.
     */
    public static class FSM_ab_b_with_initial_b_loop {

	public final Alphabet alphabet = new Alphabet();

	public final Parameter<String> p1 = alphabet.createParameter("p1", String.class);
	public final Parameter<String> p2 = alphabet.createParameter("p2", String.class);

	public final Symbol2<String, String> e1 = alphabet.createSymbol2("e1", p1, p2);
	public final Symbol1<String> e2 = alphabet.createSymbol1("e2", p2);

	public final AwareMatchHandler0 matchHandler = AwareMatchHandler.create();

	public final FSM fsm = new FSM(alphabet);

	public final FSMState initial = fsm.createInitialState();
	public final FSMState s1 = fsm.createState();
	public final FSMState error = fsm.createAcceptingState(matchHandler);

	public FSM_ab_b_with_initial_b_loop() {
	    initial.addTransition(e1, s1);
	    initial.addTransition(e2, initial);
	    s1.addTransition(e2, error);
	}
    }

    /**
     * A sequence of as, with b destroying the trace. Specifications of this kind will not be handled in the moment.
     */
    public static class FSM_a_a_no_b {

	public final Alphabet alphabet = new Alphabet();

	public final Parameter<String> p1 = alphabet.createParameter("p1", String.class);
	public final Parameter<String> p2 = alphabet.createParameter("p2", String.class);

	public final Symbol1<String> e1 = alphabet.createSymbol1("e1", p1);
	public final Symbol1<String> e2 = alphabet.createSymbol1("e2", p2);

	public final AwareMatchHandler0 matchHandler = AwareMatchHandler.create();

	public final FSM fsm = new FSM(alphabet);

	public final FSMState initial = fsm.createInitialState();
	public final FSMState s1 = fsm.createState();
	public final FSMState error = fsm.createAcceptingState(matchHandler);

	public FSM_a_a_no_b() {
	    initial.addTransition(e1, s1);
	    initial.addTransition(e2, initial);
	    s1.addTransition(e1, error);
	}
    }

}
