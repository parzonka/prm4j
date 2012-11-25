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

import prm4j.api.Alphabet;
import prm4j.api.MatchHandler;
import prm4j.api.Parameter;
import prm4j.api.Symbol1;
import prm4j.api.Symbol2;
import prm4j.api.fsm.FSM;
import prm4j.api.fsm.FSMState;

@SuppressWarnings("rawtypes")
public class FSM_unsafeIterator {

	public final Alphabet alphabet = new Alphabet();

	public final Parameter<Collection> c = alphabet.createParameter("c", Collection.class);
	public final Parameter<Iterator> i = alphabet.createParameter("i", Iterator.class);

	public final Symbol2<Collection, Iterator> create = alphabet.createSymbol2("create", c, i);
	public final Symbol1<Collection> updateSource = alphabet.createSymbol1("updateSource", c);
	public final Symbol1<Iterator> next = alphabet.createSymbol1("next", i);

	public final FSM fsm = new FSM(alphabet);

	public final FSMState initial = fsm.createInitialState();
	public final FSMState s1 = fsm.createState();
	public final FSMState s2 = fsm.createState();
	public final FSMState s3 = fsm.createState();
	public final FSMState error = fsm.createAcceptingState(MatchHandler.SYS_OUT);

	public FSM_unsafeIterator() {
	    initial.addTransition(create, s1);
	    s1.addTransition(next, s1);
	    s1.addTransition(updateSource, s2);
	    s2.addTransition(updateSource, s2);
	    s2.addTransition(next, error);
	}
}
