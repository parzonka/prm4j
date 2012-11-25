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
package prm4j.eval;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import prm4j.api.Alphabet;
import prm4j.api.MatchHandler;
import prm4j.api.Parameter;
import prm4j.api.Symbol1;
import prm4j.api.Symbol2;
import prm4j.api.fsm.FSM;
import prm4j.api.fsm.FSMState;

@SuppressWarnings("rawtypes")
public class FSM_unsafeMapIterator {

	public final Alphabet alphabet = new Alphabet();

	public final Parameter<Map> m = alphabet.createParameter("m", Map.class);
	public final Parameter<Collection> c = alphabet.createParameter("c", Collection.class);
	public final Parameter<Iterator> i = alphabet.createParameter("i", Iterator.class);

	public final Symbol2<Map, Collection> createColl = alphabet.createSymbol2("createColl", m, c);
	public final Symbol2<Collection, Iterator> createIter = alphabet.createSymbol2("createIter", c, i);
	public final Symbol1<Map> updateMap = alphabet.createSymbol1("updateMap", m);
	public final Symbol1<Iterator> useIter = alphabet.createSymbol1("useIter", i);

	public final FSM fsm = new FSM(alphabet);

	public final FSMState initial = fsm.createInitialState();
	public final FSMState s1 = fsm.createState();
	public final FSMState s2 = fsm.createState();
	public final FSMState s3 = fsm.createState();
	public final FSMState error = fsm.createAcceptingState(MatchHandler.NO_OP);

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
