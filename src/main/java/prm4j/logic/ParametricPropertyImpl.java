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

import static prm4j.logic.SetUtil.intersection;
import static prm4j.logic.SetUtil.isSubset;
import static prm4j.logic.SetUtil.tuple;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import prm4j.api.Parameter;
import prm4j.indexing.BaseEvent;
import prm4j.logic.SetUtil.Tuple;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.SetMultimap;

/**
 * Immutable self-calculating data object.
 */
public class ParametricPropertyImpl implements ParametricProperty {

    private final FiniteSpec finiteSpec;
    private final Set<BaseEvent> creationEvents;
    private final Map<BaseEvent, Set<Set<BaseEvent>>> propertyEnableSets;
    private final Map<BaseEvent, Set<Set<Parameter<?>>>> parameterEnableSets;
    private final Map<MonitorState, Set<Set<BaseEvent>>> statePropertyCoEnableSets;
    private final Map<MonitorState, Set<Set<Parameter<?>>>> stateParameterCoEnableSets;
    private Set<BaseEvent> disablingEvents;
    private ListMultimap<BaseEvent, Set<Parameter<?>>> enablingInstances;
    private ListMultimap<BaseEvent, Tuple<Set<Parameter<?>>, Set<Parameter<?>>>> joinableInstances;
    private SetMultimap<Set<Parameter<?>>, Tuple<Set<Parameter<?>>, Set<Parameter<?>>>> chainableInstances;
    private SetMultimap<Set<Parameter<?>>, Set<Parameter<?>>> monitorSets;

    public ParametricPropertyImpl(FiniteSpec finiteSpec) {
	this.finiteSpec = finiteSpec;
	creationEvents = calculateCreationEvents();
	propertyEnableSets = Collections.unmodifiableMap(new PropertyEnableSetCalculator().calculateEnableSets());
	parameterEnableSets = Collections.unmodifiableMap(toMap2SetOfSetOfParameters(propertyEnableSets));
	calculateRelations();
	// TODO statePropertyCoEnableSets
	statePropertyCoEnableSets = Collections.unmodifiableMap(new HashMap<MonitorState, Set<Set<BaseEvent>>>());
	stateParameterCoEnableSets = Collections.unmodifiableMap(toMap2SetOfSetOfParameters(statePropertyCoEnableSets));
    }

    /**
     * Creation events are events for which the successor of the initial state is:
     * <ul>
     * <li>not a dead state</li>
     * <li>not the initial state itself (self-loop)</li>
     * </ul>
     *
     * @return the creation events
     */
    private Set<BaseEvent> calculateCreationEvents() {
	Set<BaseEvent> creationSymbols = new HashSet<BaseEvent>();
	MonitorState initialState = finiteSpec.getInitialState();
	for (BaseEvent symbol : finiteSpec.getBaseEvents()) {
	    MonitorState successor = initialState.getSuccessor(symbol);
	    if (successor == null || !successor.equals(initialState)) {
		creationSymbols.add(symbol);
	    }
	}
	return creationSymbols;
    }

    private class PropertyEnableSetCalculator {

	private final Map<BaseEvent, Set<Set<BaseEvent>>> enableSets;
	private final Map<MonitorState, Set<Set<BaseEvent>>> stateToSeenBaseEvents;

	public PropertyEnableSetCalculator() {
	    enableSets = new HashMap<BaseEvent, Set<Set<BaseEvent>>>();
	    stateToSeenBaseEvents = new HashMap<MonitorState, Set<Set<BaseEvent>>>();
	    for (BaseEvent baseEvent : finiteSpec.getBaseEvents()) {
		enableSets.put(baseEvent, new HashSet<Set<BaseEvent>>());
	    }
	    for (MonitorState state : finiteSpec.getStates()) {
		stateToSeenBaseEvents.put(state, new HashSet<Set<BaseEvent>>());
	    }
	}

	public Map<BaseEvent, Set<Set<BaseEvent>>> calculateEnableSets() {
	    computeEnableSets(finiteSpec.getInitialState(), new HashSet<BaseEvent>());
	    return enableSets;
	}

	private void computeEnableSets(MonitorState state, Set<BaseEvent> seenBaseEvents) {
	    if (state == null)
		throw new NullPointerException("state may not be null!");
	    for (BaseEvent baseEvent : finiteSpec.getBaseEvents()) {
		if (state.getSuccessor(baseEvent) != null) {
		    final Set<BaseEvent> seenBaseEventsWithoutSelfloop = new HashSet<BaseEvent>(seenBaseEvents);
		    seenBaseEventsWithoutSelfloop.remove(baseEvent);
		    enableSets.get(baseEvent).add(seenBaseEventsWithoutSelfloop);
		    final Set<BaseEvent> nextSeenBaseEvents = new HashSet<BaseEvent>(seenBaseEvents);
		    nextSeenBaseEvents.add(baseEvent);
		    if (!stateToSeenBaseEvents.get(state).contains(nextSeenBaseEvents)) {
			stateToSeenBaseEvents.get(state).add(nextSeenBaseEvents);
			computeEnableSets(state.getSuccessor(baseEvent), nextSeenBaseEvents);
		    }
		}
	    }
	}
    }

    private static <T> Map<T, Set<Set<Parameter<?>>>> toMap2SetOfSetOfParameters(
	    Map<T, Set<Set<BaseEvent>>> baseEvent2setOfSetOfBaseEvent) {
	Map<T, Set<Set<Parameter<?>>>> result = new HashMap<T, Set<Set<Parameter<?>>>>();
	for (Entry<T, Set<Set<BaseEvent>>> entry : baseEvent2setOfSetOfBaseEvent.entrySet()) {
	    result.put(entry.getKey(), toParameterSets(entry.getValue()));
	}
	return result;
    }

    private static Set<Set<Parameter<?>>> toParameterSets(Set<Set<BaseEvent>> propertyEnableSet) {
	Set<Set<Parameter<?>>> result = new HashSet<Set<Parameter<?>>>();
	for (Set<BaseEvent> set : propertyEnableSet) {
	    Set<Parameter<?>> parameterSet = new HashSet<Parameter<?>>();
	    for (BaseEvent baseEvent : set) {
		parameterSet.addAll(baseEvent.getParameters());
	    }
	    result.add(parameterSet);
	}
	return result;
    }

    private void calculateRelations() { // 1
	enablingInstances = ArrayListMultimap.create();
	joinableInstances = ArrayListMultimap.create();
	chainableInstances = HashMultimap.create();
	monitorSets = HashMultimap.create();
	Set<Tuple<Set<Parameter<?>>, Set<Parameter<?>>>> temp = new HashSet<Tuple<Set<Parameter<?>>, Set<Parameter<?>>>>(); // 2
	for (BaseEvent baseEvent : finiteSpec.getBaseEvents()) { // 3
	    Set<Parameter<?>> parameterSet = baseEvent.getParameters(); // 4
	    for (BaseEvent baseEvent2 : finiteSpec.getBaseEvents()) { // 5
		Set<Parameter<?>> parameterSet2 = baseEvent2.getParameters(); // 5
		if (SetUtil.isSubset(parameterSet2, parameterSet)) { // 6
		    temp.add(SetUtil.tuple(parameterSet2, parameterSet)); // 7
		} // 8
	    } // 9
	    List<Set<Parameter<?>>> enableSetInReverseTopolicalOrdering = new ArrayList<Set<Parameter<?>>>(
		    parameterEnableSets.get(baseEvent)); // 10
	    Collections.sort(enableSetInReverseTopolicalOrdering, SetUtil.REVERSE_TOPOLOGICAL_SET_COMPARATOR); // 10
	    for (Set<Parameter<?>> enablingParameterSet : enableSetInReverseTopolicalOrdering) { // 10
		if (isSubset(enablingParameterSet, parameterSet)) { // 11
		    enablingInstances.get(baseEvent).add(enablingParameterSet); // 12
		} else { // 13
		    Set<Parameter<?>> compatibleSubset = intersection(parameterSet, enablingParameterSet); // 14
		    joinableInstances.put(baseEvent, tuple(compatibleSubset, enablingParameterSet)); // 15
		    chainableInstances.put(enablingParameterSet, tuple(compatibleSubset, enablingParameterSet)); // 16
		    monitorSets.put(compatibleSubset, enablingParameterSet); // 17
		} // 18
	    } // 19
	} // 20
	final Set<Parameter<?>> emptySet = new HashSet<Parameter<?>>();
	for (Tuple<Set<Parameter<?>>, Set<Parameter<?>>> tuple : temp) { // 21
	    if (!monitorSets.get(tuple.getLeft()).contains(tuple.getRight())) { // 22
		chainableInstances.put(tuple.getRight(), tuple(tuple.getLeft(), emptySet)); // 23
		monitorSets.put(tuple.getLeft(), emptySet); // 24
	    } // 25
	} // 26
    } // 27

    @Override
    public Set<BaseEvent> getCreationEvents() {
	return creationEvents;
    }

    @Override
    public Set<BaseEvent> getDisablingEvents() {
	return disablingEvents;
    }

    @Override
    public ListMultimap<BaseEvent, Set<Parameter<?>>> getEnablingInstances() {
	return enablingInstances;
    }

    @Override
    public ListMultimap<BaseEvent, Tuple<Set<Parameter<?>>, Set<Parameter<?>>>> getJoinableInstances() {
	return joinableInstances;
    }

    @Override
    public SetMultimap<Set<Parameter<?>>, Tuple<Set<Parameter<?>>, Set<Parameter<?>>>> getChainableSubinstances() {
	return chainableInstances;
    }

    @Override
    public SetMultimap<Set<Parameter<?>>, Set<Parameter<?>>> getMonitorSets() {
	return monitorSets;
    }

    Map<BaseEvent, Set<Set<BaseEvent>>> getPropertyEnableSets() {
	return propertyEnableSets;
    }

    Map<BaseEvent, Set<Set<Parameter<?>>>> getParameterEnableSets() {
	return parameterEnableSets;
    }

    Map<MonitorState, Set<Set<BaseEvent>>> getStatePropertyCoEnableSets() {
	return statePropertyCoEnableSets;
    }

    Map<MonitorState, Set<Set<Parameter<?>>>> getStateParameterCoEnableSets() {
	return stateParameterCoEnableSets;
    }

    MonitorState getInitialState() {
	return finiteSpec.getInitialState();
    }

    @Override
    public Set<BaseEvent> getBaseEvents() {
	return finiteSpec.getBaseEvents();
    }

}
