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
package prm4j.api.fsm;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import prm4j.api.Symbol;
import prm4j.logic.MonitorState;
import prm4j.logic.StatefulSpec;

public class FSMSpec<A> implements StatefulSpec {

    private FSM<A> fsm;

    public FSMSpec(FSM<A> fsm) {
	this.fsm = fsm;
    }

    @Override
    public Set<Symbol> getSymbols() {
	return fsm.getAlphabet().getSymbols();
    }

    @Override
    public Set<MonitorState<?>> getStates() {
	Set<MonitorState<?>> states = new HashSet<MonitorState<?>>();
	states.addAll(fsm.getStates());
	return states;
    }

    @Override
    public Set<Symbol> getCreationSymbols() {
	Set<Symbol> creationSymbols = new HashSet<Symbol>();
	MonitorState<?> initialState = fsm.getInitialState();
	for (Symbol symbol : fsm.getAlphabet().getSymbols()) {
	    MonitorState<?> successor = initialState.getSuccessor(symbol);
	    if (successor == null || !successor.equals(initialState)) {
		creationSymbols.add(symbol);
	    }
	}
	return creationSymbols;
    }

    @Override
    public Map<Symbol, Set<Set<Symbol>>> getPropertyEnableSets() {
	return new PropertyEnableSetCalculator<A>(fsm).getEnableSets();
    }

    @Override
    public Map<MonitorState<?>, Set<Set<Symbol>>> getStatePropertyCoEnableSets() {
	Map<MonitorState<?>, Set<Set<Symbol>>> result = new HashMap<MonitorState<?>, Set<Set<Symbol>>>();
	// TODO getStateCoEnableSet
	return result;
    }

    @Override
    public MonitorState<?> getInitialState() {
	return fsm.getInitialState();
    }

    private static class PropertyEnableSetCalculator<A> {

	private final MonitorState<A> initialState;
	private final Set<Symbol> symbols;
	private final Set<FSMState<A>> states;
	private final Map<Symbol, Set<Set<Symbol>>> enableSets;
	private final Map<FSMState<A>, Set<Set<Symbol>>> stateToSeenSymbols;

	public PropertyEnableSetCalculator(FSM<A> fsm) {
	    this.initialState = fsm.getInitialState();
	    this.symbols = fsm.getAlphabet().getSymbols();
	    this.states = fsm.getStates();
	    this.enableSets = new HashMap<Symbol, Set<Set<Symbol>>>();
	    this.stateToSeenSymbols = new HashMap<FSMState<A>, Set<Set<Symbol>>>();
	    for (Symbol symbol : this.symbols) {
		this.enableSets.put(symbol, new HashSet<Set<Symbol>>());
	    }
	    for (FSMState<A> state : this.states) {
		this.stateToSeenSymbols.put(state, new HashSet<Set<Symbol>>());
	    }
	}

	public Map<Symbol, Set<Set<Symbol>>> getEnableSets() {
	    computeEnableSets(this.initialState, new HashSet<Symbol>());
	    return this.enableSets;
	}

	private void computeEnableSets(MonitorState<A> state, Set<Symbol> seenSymbols) {
	    if (state == null)
		throw new NullPointerException("state may not be null!");
	    for (Symbol symbol : this.symbols) {
		if (state.getSuccessor(symbol) != null) {
		    final Set<Symbol> seenSymbolsWithoutSelfloop = new HashSet<Symbol>(seenSymbols);
		    seenSymbolsWithoutSelfloop.remove(symbol);
		    this.enableSets.get(symbol).add(seenSymbolsWithoutSelfloop);
		    final Set<Symbol> nextSeenSymbols = new HashSet<Symbol>(seenSymbols);
		    nextSeenSymbols.add(symbol);
		    if (!this.stateToSeenSymbols.get(state).contains(nextSeenSymbols)) {
			this.stateToSeenSymbols.get(state).add(nextSeenSymbols);
			computeEnableSets(state.getSuccessor(symbol), nextSeenSymbols);
		    }
		}
	    }
	}
    }

}
