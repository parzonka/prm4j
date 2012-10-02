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
package prm4j.logic;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import prm4j.api.Parameter;
import prm4j.api.Symbol;
import prm4j.api.fsm.FSM;

public class StatefulSpecConverter {

    private final Map<Symbol, Set<Set<Symbol>>> propertyEnableSets;
    private final Map<Symbol, Set<Set<Parameter<?>>>> parameterEnableSets;
    private final Map<MonitorState<?>, Set<Set<Symbol>>> statePropertyCoEnableSets;
    private final Map<MonitorState<?>, Set<Set<Parameter<?>>>> stateParameterCoEnableSets;
    private final MonitorState<?> initialState;
    private final Set<Symbol> symbols;

    public StatefulSpecConverter(StatefulSpec spec) {
	propertyEnableSets = spec.getPropertyEnableSet();
	parameterEnableSets = toMap2SetOfSetOfParameters(propertyEnableSets);
	statePropertyCoEnableSets = spec.getStateCoEnableSet();
	stateParameterCoEnableSets = toMap2SetOfSetOfParameters(statePropertyCoEnableSets);
	initialState = spec.getInitialState();
	symbols = spec.getSymbols();
    }

    private static <T> Map<T, Set<Set<Parameter<?>>>> toMap2SetOfSetOfParameters(
	    Map<T, Set<Set<Symbol>>> symbol2setOfSetOfSymbol) {
	Map<T, Set<Set<Parameter<?>>>> result = new HashMap<T, Set<Set<Parameter<?>>>>();
	for (Entry<T, Set<Set<Symbol>>> entry : symbol2setOfSetOfSymbol.entrySet()) {
	    result.put(entry.getKey(), toParameterSets(entry.getValue()));
	}
	return result;
    }

    private static Set<Set<Parameter<?>>> toParameterSets(Set<Set<Symbol>> propertyEnableSet) {
	Set<Set<Parameter<?>>> result = new HashSet<Set<Parameter<?>>>();
	for (Set<Symbol> set : propertyEnableSet) {
	    Set<Parameter<?>> parameterSet = new HashSet<Parameter<?>>();
	    for (Symbol symbol : set) {
		parameterSet.addAll(symbol.getParameters());
	    }
	    result.add(parameterSet);
	}
	return result;
    }

    /**
     * Calculate the largest set of parameters which is needed to trigger a match.
     *
     * @return
     */
    public Set<Parameter<?>> getLongestMatchingInstance() {
	int maxSize = -1;
	Set<Parameter<?>> maxSet = null;
	for (Set<Parameter<?>> set : stateParameterCoEnableSets.get(initialState)) {
	    if (maxSize < set.size()) {
		maxSize = set.size();
		maxSet = set;
	    }
	}
	return Collections.unmodifiableSet(maxSet);
    }

    public Map<Symbol, Set<Set<Symbol>>> getPropertyEnableSets() {
        return propertyEnableSets;
    }

    public Map<Symbol, Set<Set<Parameter<?>>>> getParameterEnableSets() {
        return parameterEnableSets;
    }

    public Map<MonitorState<?>, Set<Set<Symbol>>> getStatePropertyCoEnableSets() {
        return statePropertyCoEnableSets;
    }

    public Map<MonitorState<?>, Set<Set<Parameter<?>>>> getStateParameterCoEnableSets() {
        return stateParameterCoEnableSets;
    }

    public MonitorState<?> getInitialState() {
        return initialState;
    }

    public Set<Symbol> getSymbols() {
        return symbols;
    }



}
