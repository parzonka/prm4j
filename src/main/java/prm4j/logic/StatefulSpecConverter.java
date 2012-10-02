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
    private final Map<MonitorState<?>, Set<Set<Symbol>>> stateCoEnableSets;
    private final MonitorState<?> initialState;
    private final List<Symbol> symbols;

    public StatefulSpecConverter(StatefulSpec spec) {
	propertyEnableSets = spec.getPropertyEnableSet();
	parameterEnableSets = getParameterEnableSets(propertyEnableSets);
	stateCoEnableSets = spec.getStateCoEnableSet();
	initialState = spec.getInitialState();
	symbols = spec.getSymbols();
    }

    private static <A> Map<Symbol, Set<Set<Parameter<?>>>> getParameterEnableSets(
	    Map<Symbol, Set<Set<Symbol>>> symbol2setOfSetOfSymbol) {
	Map<Symbol, Set<Set<Parameter<?>>>> result = new HashMap<Symbol, Set<Set<Parameter<?>>>>();
	for (Entry<Symbol, Set<Set<Symbol>>> entry : symbol2setOfSetOfSymbol.entrySet()) {
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

}
