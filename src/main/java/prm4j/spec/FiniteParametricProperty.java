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
package prm4j.spec;

import static prm4j.Util.intersection;
import static prm4j.Util.isSubset;
import static prm4j.Util.isSuperset;
import static prm4j.Util.isSubsetEq;
import static prm4j.Util.tuple;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import prm4j.Util;
import prm4j.Util.Tuple;
import prm4j.api.BaseEvent;
import prm4j.api.Parameter;
import prm4j.indexing.BaseMonitorState;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.SetMultimap;

/**
 * Immutable self-calculating data object.
 */
public class FiniteParametricProperty implements ParametricProperty {

    private final FiniteSpec finiteSpec;
    private final Set<BaseEvent> creationEvents;
    private final Set<BaseEvent> disablingEvents;
    private final Map<BaseEvent, Set<Set<BaseEvent>>> enablingEventSets;
    private final Map<BaseEvent, Set<Set<Parameter<?>>>> enablingParameterSets;
    private final Set<Set<Parameter<?>>> possibleParameterSets;
    private final Map<BaseMonitorState, Set<Set<BaseEvent>>> coenablingEventSets; // TODO
    private final Map<BaseMonitorState, Set<Set<Parameter<?>>>> coenablingParameterSets; // TODO
    private final ListMultimap<BaseEvent, Set<Parameter<?>>> maxData;
    private final ListMultimap<BaseEvent, Tuple<Set<Parameter<?>>, Set<Parameter<?>>>> joinData;
    private final SetMultimap<Set<Parameter<?>>, Tuple<Set<Parameter<?>>, Set<Parameter<?>>>> chainData;
    private final SetMultimap<Set<Parameter<?>>, Set<Parameter<?>>> monitorSetData;
    private final static Set<Parameter<?>> EMPTY_PARAMETER_SET = new HashSet<Parameter<?>>();

    public FiniteParametricProperty(FiniteSpec finiteSpec) {

	this.finiteSpec = finiteSpec;
	creationEvents = calculateCreationEvents();
	disablingEvents = calculateDisablingEvents();

	PossibleParameterAndEnablingEventSetCalculator p = new PossibleParameterAndEnablingEventSetCalculator();
	enablingEventSets = Collections.unmodifiableMap(p.getEnablingEventSets());
	possibleParameterSets = Collections.unmodifiableSet(p.getPossibleParameterSets());
	enablingParameterSets = Collections.unmodifiableMap(toMap2SetOfSetOfParameters(enablingEventSets));

	maxData = ArrayListMultimap.create();
	joinData = ArrayListMultimap.create();
	chainData = HashMultimap.create();
	monitorSetData = HashMultimap.create();
	calculateStaticData();

	// TODO statePropertyCoEnableSets
	coenablingEventSets = Collections.unmodifiableMap(new HashMap<BaseMonitorState, Set<Set<BaseEvent>>>());
	coenablingParameterSets = Collections.unmodifiableMap(toMap2SetOfSetOfParameters(coenablingEventSets));
	calculateAcceptingParameters();
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
	Set<BaseEvent> disablingEvents = new HashSet<BaseEvent>();
	BaseMonitorState initialState = finiteSpec.getInitialState();
	for (BaseEvent symbol : finiteSpec.getBaseEvents()) {
	    BaseMonitorState successor = initialState.getSuccessor(symbol);
	    if (successor != null && !successor.equals(initialState)) {
		disablingEvents.add(symbol);
	    }
	}
	return disablingEvents;
    }

    /**
     * Disabling events are events for which the successor of the initial state is a dead state.
     *
     * @return the disabling events
     */
    private Set<BaseEvent> calculateDisablingEvents() {
	Set<BaseEvent> disablingEvents = new HashSet<BaseEvent>();
	BaseMonitorState initialState = finiteSpec.getInitialState();
	for (BaseEvent symbol : finiteSpec.getBaseEvents()) {
	    BaseMonitorState successor = initialState.getSuccessor(symbol);
	    if (successor == null) {
		disablingEvents.add(symbol);
	    }
	}
	return disablingEvents;
    }

    private class PossibleParameterAndEnablingEventSetCalculator {

	private final Map<BaseEvent, Set<Set<BaseEvent>>> enablingEventSets;
	private final Set<Set<Parameter<?>>> possibleParameterSets;
	private final Map<BaseMonitorState, Set<Set<BaseEvent>>> stateToSeenBaseEvents;

	public PossibleParameterAndEnablingEventSetCalculator() {
	    enablingEventSets = new HashMap<BaseEvent, Set<Set<BaseEvent>>>();
	    possibleParameterSets = new HashSet<Set<Parameter<?>>>();
	    stateToSeenBaseEvents = new HashMap<BaseMonitorState, Set<Set<BaseEvent>>>();
	    for (BaseEvent baseEvent : finiteSpec.getBaseEvents()) {
		enablingEventSets.put(baseEvent, new HashSet<Set<BaseEvent>>());
	    }
	    for (BaseMonitorState state : finiteSpec.getStates()) {
		stateToSeenBaseEvents.put(state, new HashSet<Set<BaseEvent>>());
	    }
	    computeEnableSets(finiteSpec.getInitialState(), new HashSet<BaseEvent>(), new HashSet<Parameter<?>>()); // 2
	}

	public Map<BaseEvent, Set<Set<BaseEvent>>> getEnablingEventSets() {
	    return enablingEventSets;
	}

	public Set<Set<Parameter<?>>> getPossibleParameterSets() {
	    return possibleParameterSets;
	}

	private void computeEnableSets(BaseMonitorState state, Set<BaseEvent> seenBaseEvents,
		Set<Parameter<?>> parameterSet) { // 5
	    if (state == null)
		throw new NullPointerException("state may not be null!");
	    stateToSeenBaseEvents.get(state).add(seenBaseEvents); // 6
	    possibleParameterSets.add(parameterSet); // 7
	    for (BaseEvent baseEvent : finiteSpec.getBaseEvents()) { // 8
		if (state.getSuccessor(baseEvent) != null) { // 9 TODO path to accepting state
		    final Set<BaseEvent> seenBaseEventsWithoutSelfloop = new HashSet<BaseEvent>(seenBaseEvents); // 10
		    seenBaseEventsWithoutSelfloop.remove(baseEvent); // 10
		    enablingEventSets.get(baseEvent).add(seenBaseEventsWithoutSelfloop); // 10
		    final Set<BaseEvent> nextSeenBaseEvents = new HashSet<BaseEvent>(seenBaseEvents); // 11
		    nextSeenBaseEvents.add(baseEvent); // 11
		    if (!stateToSeenBaseEvents.get(state).contains(nextSeenBaseEvents)) { // 11
			Set<Parameter<?>> nextParameterSet = new HashSet<Parameter<?>>(parameterSet); // 12
			nextParameterSet.addAll(baseEvent.getParameters()); // 12
			computeEnableSets(state.getSuccessor(baseEvent), nextSeenBaseEvents, nextParameterSet); // 12
		    } // 13
		} // 14
	    } // 15
	} // 16
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

    private void calculateStaticData() { // 1
	Set<Tuple<Set<Parameter<?>>, Set<Parameter<?>>>> updates = new HashSet<Tuple<Set<Parameter<?>>, Set<Parameter<?>>>>(); // 2
	for (BaseEvent baseEvent : finiteSpec.getBaseEvents()) { // 3
	    for (Set<Parameter<?>> parameterSet : possibleParameterSets) { // 4
		if (isSubset(baseEvent.getParameters(), parameterSet)) { // 5
		    updates.add(tuple(baseEvent.getParameters(), parameterSet)); // 6
		}
	    }
	}
	for (BaseEvent baseEvent : finiteSpec.getBaseEvents()) { // 3
	    Set<Parameter<?>> parameterSet = baseEvent.getParameters(); // 4
	    List<Set<Parameter<?>>> enableSetInReverseTopolicalOrdering = new ArrayList<Set<Parameter<?>>>(
		    enablingParameterSets.get(baseEvent)); // 10
	    Collections.sort(enableSetInReverseTopolicalOrdering, Util.REVERSE_TOPOLOGICAL_SET_COMPARATOR); // 10
	    for (Set<Parameter<?>> enablingParameterSet : enableSetInReverseTopolicalOrdering) { // 10
		if (!enablingParameterSet.equals(EMPTY_PARAMETER_SET) && !isSubsetEq(parameterSet, enablingParameterSet)) { // 13
		    if (isSuperset(parameterSet, enablingParameterSet)) { // 14
			maxData.get(baseEvent).add(enablingParameterSet); // 15
		    } else { // 16
			Set<Parameter<?>> compatibleSubset = intersection(parameterSet, enablingParameterSet); // 17
			joinData.put(baseEvent, tuple(compatibleSubset, enablingParameterSet)); // 18
			monitorSetData.put(compatibleSubset, enablingParameterSet); // 19
		    } // 20
		} // 21
	    } // 22
	} // 23
	for (Tuple<Set<Parameter<?>>, Set<Parameter<?>>> tuple : updates) { // 24
	    if (monitorSetData.get(tuple.getLeft()).contains(tuple.getRight())) { // 25
		chainData.put(tuple.getRight(), tuple(tuple.getLeft(), tuple.getRight())); // 26
	    } else { // 27
		chainData.put(tuple.getRight(), tuple(tuple.getLeft(), EMPTY_PARAMETER_SET)); // 28
		monitorSetData.put(tuple.getLeft(), EMPTY_PARAMETER_SET); // 29
	    } // 25
	} // 26

    }// 27

    private void calculateAcceptingParameters() {
	// TODO implement calculateAcceptingParameters
	boolean[] acceptingParameters = new boolean[finiteSpec.getParameters().size()];
	for (BaseMonitorState state : finiteSpec.getStates()) {
	    state.setAcceptingParameters(acceptingParameters);
	}
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
    @Override
    public Set<BaseEvent> getCreationEvents() {
	return creationEvents;
    }

    /**
     * Disabling events are events for which the successor of the initial state is a dead state.
     *
     * @return the disabling events
     */
    @Override
    public Set<BaseEvent> getDisablingEvents() {
	return disablingEvents;
    }

    @Override
    public ListMultimap<BaseEvent, Set<Parameter<?>>> getMaxData() {
	return maxData;
    }

    @Override
    public ListMultimap<BaseEvent, Tuple<Set<Parameter<?>>, Set<Parameter<?>>>> getJoinData() {
	return joinData;
    }

    @Override
    public SetMultimap<Set<Parameter<?>>, Tuple<Set<Parameter<?>>, Set<Parameter<?>>>> getChainData() {
	return chainData;
    }

    @Override
    public SetMultimap<Set<Parameter<?>>, Set<Parameter<?>>> getMonitorSetData() {
	return monitorSetData;
    }

    @Override
    public Map<BaseEvent, Set<Set<BaseEvent>>> getEnablingEventSets() {
	return enablingEventSets;
    }

    @Override
    public Set<Set<Parameter<?>>> getPossibleParameterSets() {
	return possibleParameterSets;
    }

    @Override
    public Map<BaseEvent, Set<Set<Parameter<?>>>> getEnablingParameterSets() {
	return enablingParameterSets;
    }

    Map<BaseMonitorState, Set<Set<BaseEvent>>> getStatePropertyCoEnableSets() {
	return coenablingEventSets;
    }

    Map<BaseMonitorState, Set<Set<Parameter<?>>>> getStateParameterCoEnableSets() {
	return coenablingParameterSets;
    }

    @Override
    public BaseMonitorState getInitialState() {
	return finiteSpec.getInitialState();
    }

    @Override
    public Set<BaseEvent> getBaseEvents() {
	return finiteSpec.getBaseEvents();
    }

}
