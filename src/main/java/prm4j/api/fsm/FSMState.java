/*
 * Copyright (c) 2012 Mateusz Parzonka, Eric Bodden
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Eric Bodden - initial API and implementation
 * Mateusz Parzonka - adapted API and implementation
 */
package prm4j.api.fsm;

import prm4j.api.Alphabet;
import prm4j.api.BaseEvent;
import prm4j.api.MatchHandler;
import prm4j.api.Symbol;
import prm4j.indexing.BaseMonitorState;

public class FSMState extends BaseMonitorState {

    private FSMState[] successorTable;
    private final boolean isAccepting;
    private boolean isFinal;
    private final String label;// for display purposes only
    private final Alphabet alphabet;
    private final MatchHandler matchHandler;

    public FSMState(int index, Alphabet alphabet, boolean isAccepting, MatchHandler matchHandler, String label) {
	super(index);
	this.isAccepting = isAccepting;
	isFinal = true; // a state is final if it has no successor
	this.label = label;
	this.alphabet = alphabet;
	this.matchHandler = matchHandler;
	successorTable = new FSMState[alphabet.size()];
    }

    public void addTransition(Symbol symbol, FSMState successor) {
	assert successorTable[symbol.getIndex()] == null : "successor already set";
	if (!alphabet.getSymbols().contains(symbol)) {
	    throw new IllegalArgumentException("Symbol for transition is not contained in alphabet!");
	}
	successorTable[symbol.getIndex()] = successor;
	isFinal = false;
    }

    @Override
    public BaseMonitorState getSuccessor(BaseEvent baseEvent) {
	return successorTable[baseEvent.getIndex()];
    }

    @Override
    public String toString() {
	return label;
    }

    @Override
    public boolean isAccepting() {
	return isAccepting;
    }

    @Override
    public MatchHandler getMatchHandler() {
	return matchHandler;
    }

    @Override
    public boolean isFinal() {
	return isFinal;
    }

}
