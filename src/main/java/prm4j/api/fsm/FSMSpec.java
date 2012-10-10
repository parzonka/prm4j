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
import prm4j.indexing.BaseEvent;
import prm4j.logic.MonitorState;
import prm4j.logic.StatefulSpec;

/**
 * @param <E>
 *            the type of base event processed by monitors
 */
public class FSMSpec implements StatefulSpec {

    private FSM fsm;

    public FSMSpec(FSM fsm) {
	this.fsm = fsm;
    }

    @Override
    public Set<? extends BaseEvent> getBaseEvents() {
	return fsm.getAlphabet().getSymbols();
    }

    public Set<Symbol> getSymbols() {
	return fsm.getAlphabet().getSymbols();
    }

    @Override
    public Set<MonitorState> getStates() {
	Set<MonitorState> states = new HashSet<MonitorState>();
	states.addAll(fsm.getStates());
	return states;
    }

    @Override
    public Set<? extends BaseEvent> getCreationBaseEvents() {
	Set<Symbol> creationSymbols = new HashSet<Symbol>();
	MonitorState initialState = fsm.getInitialState();
	for (Symbol symbol : fsm.getAlphabet().getSymbols()) {
	    MonitorState successor = initialState.getSuccessor(symbol);
	    if (successor == null || !successor.equals(initialState)) {
		creationSymbols.add(symbol);
	    }
	}
	return creationSymbols;
    }

    @Override
    public Map<Symbol, Set<Set<Symbol>>> getPropertyEnableSets() {
	return new PropertyEnableSetCalculator(fsm).getEnableSets();
    }

    @Override
    public Map<MonitorState, Set<Set<Symbol>>> getStatePropertyCoEnableSets() {
	Map<MonitorState, Set<Set<Symbol>>> result = new HashMap<MonitorState, Set<Set<Symbol>>>();
	// TODO getStateCoEnableSet
	return result;
    }

    @Override
    public MonitorState getInitialState() {
	return fsm.getInitialState();
    }

    private static class PropertyEnableSetCalculator {

	private final MonitorState initialState;
	private final Set<Symbol> symbols;
	private final Set<FSMState> states;
	private final Map<Symbol, Set<Set<Symbol>>> enableSets;
	private final Map<FSMState, Set<Set<Symbol>>> stateToSeenSymbols;

	public PropertyEnableSetCalculator(FSM fsm) {
	    initialState = fsm.getInitialState();
	    symbols = fsm.getAlphabet().getSymbols();
	    states = fsm.getStates();
	    enableSets = new HashMap<Symbol, Set<Set<Symbol>>>();
	    stateToSeenSymbols = new HashMap<FSMState, Set<Set<Symbol>>>();
	    for (Symbol symbol : symbols) {
		enableSets.put(symbol, new HashSet<Set<Symbol>>());
	    }
	    for (FSMState state : states) {
		stateToSeenSymbols.put(state, new HashSet<Set<Symbol>>());
	    }
	}

	public Map<Symbol, Set<Set<Symbol>>> getEnableSets() {
	    computeEnableSets(initialState, new HashSet<Symbol>());
	    return enableSets;
	}

	private void computeEnableSets(MonitorState state, Set<Symbol> seenSymbols) {
	    if (state == null)
		throw new NullPointerException("state may not be null!");
	    for (Symbol symbol : symbols) {
		if (state.getSuccessor(symbol) != null) {
		    final Set<Symbol> seenSymbolsWithoutSelfloop = new HashSet<Symbol>(seenSymbols);
		    seenSymbolsWithoutSelfloop.remove(symbol);
		    enableSets.get(symbol).add(seenSymbolsWithoutSelfloop);
		    final Set<Symbol> nextSeenSymbols = new HashSet<Symbol>(seenSymbols);
		    nextSeenSymbols.add(symbol);
		    if (!stateToSeenSymbols.get(state).contains(nextSeenSymbols)) {
			stateToSeenSymbols.get(state).add(nextSeenSymbols);
			computeEnableSets(state.getSuccessor(symbol), nextSeenSymbols);
		    }
		}
	    }
	}
    }




}
