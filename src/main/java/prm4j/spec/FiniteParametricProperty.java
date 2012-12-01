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
import static prm4j.Util.isSubsetEq;
import static prm4j.Util.isSuperset;
import static prm4j.Util.set;
import static prm4j.Util.tuple;
import static prm4j.Util.unmodifiableDifference;
import static prm4j.Util.unmodifiableUnion;

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
    private final SetMultimap<Set<Parameter<?>>, Tuple<Set<Parameter<?>>, Boolean>> monitorSetData;
    private final static Set<Parameter<?>> EMPTY_PARAMETER_SET = new HashSet<Parameter<?>>();
    private final Set<Tuple<Set<Parameter<?>>, Set<Parameter<?>>>> updates;

    public FiniteParametricProperty(FiniteSpec finiteSpec) {

	this.finiteSpec = finiteSpec;
	creationEvents = calculateCreationEvents();
	disablingEvents = calculateDisablingEvents();

	RelationCalculator p = new RelationCalculator();
	enablingEventSets = Collections.unmodifiableMap(p.getEnablingEventSets());
	possibleParameterSets = Collections.unmodifiableSet(p.getPossibleParameterSets());
	enablingParameterSets = Collections.unmodifiableMap(toMap2SetOfSetOfParameters(enablingEventSets));
	updates = Collections.unmodifiableSet(p.getUpdates());

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

    private class RelationCalculator {

	private final Map<BaseEvent, Set<Set<BaseEvent>>> enablingEventSets;
	private final Set<Set<Parameter<?>>> possibleParameterSets;
	private final Map<BaseMonitorState, Set<Set<BaseEvent>>> stateToSeenBaseEvents;
	private final Set<Tuple<Set<Parameter<?>>, Set<Parameter<?>>>> updates;

	public RelationCalculator() {
	    enablingEventSets = new HashMap<BaseEvent, Set<Set<BaseEvent>>>();
	    possibleParameterSets = new HashSet<Set<Parameter<?>>>();
	    stateToSeenBaseEvents = new HashMap<BaseMonitorState, Set<Set<BaseEvent>>>();
	    updates = new HashSet<Tuple<Set<Parameter<?>>, Set<Parameter<?>>>>();
	    for (BaseEvent baseEvent : finiteSpec.getBaseEvents()) {
		enablingEventSets.put(baseEvent, new HashSet<Set<BaseEvent>>());
	    }
	    for (BaseMonitorState state : finiteSpec.getStates()) {
		stateToSeenBaseEvents.put(state, new HashSet<Set<BaseEvent>>());
	    }
	    computeRelations(finiteSpec.getInitialState(), new HashSet<BaseEvent>(), new HashSet<Parameter<?>>()); // 2
	}

	public Map<BaseEvent, Set<Set<BaseEvent>>> getEnablingEventSets() {
	    return enablingEventSets;
	}

	public Set<Set<Parameter<?>>> getPossibleParameterSets() {
	    return possibleParameterSets;
	}

	public Set<? extends Tuple<Set<Parameter<?>>, Set<Parameter<?>>>> getUpdates() {
	    return updates;
	}

	private void computeRelations(BaseMonitorState state, Set<BaseEvent> seenBaseEvents,
		Set<Parameter<?>> parameterSet) { // 5
	    stateToSeenBaseEvents.get(state).add(seenBaseEvents); // 6
	    possibleParameterSets.add(parameterSet); // 7
	    for (BaseEvent baseEvent : finiteSpec.getBaseEvents()) { // 8
		final BaseMonitorState nextState = state.getSuccessor(baseEvent);
		final Set<BaseEvent> nextSeenBaseEvents = unmodifiableUnion(seenBaseEvents, set(baseEvent)); // 11
		final Set<Parameter<?>> nextParameterSet = unmodifiableUnion(parameterSet, baseEvent.getParameters()); // 12
		// add to updates only if the base event does change state and it is no self-update:
		if (state != nextState && !baseEvent.getParameters().equals(nextParameterSet)) {
		    updates.add(tuple(baseEvent.getParameters(), nextParameterSet));
		}
		if (nextState != null) { // 9 TODO path to accepting state
		    // we remove the current base event because an event does not need to enable itself
		    final Set<BaseEvent> seenBaseEventsWithoutCurrentBaseEvent = unmodifiableDifference(seenBaseEvents,
			    set(baseEvent)); // 10
		    enablingEventSets.get(baseEvent).add(seenBaseEventsWithoutCurrentBaseEvent); // 10

		    // compute from next different state if state was not visited before carrying the same
		    // nextSeenBaseEvents
		    if (nextState != state && !stateToSeenBaseEvents.get(state).contains(nextSeenBaseEvents)) { // 11
			computeRelations(state.getSuccessor(baseEvent), nextSeenBaseEvents, nextParameterSet); // 12
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

    /**
     * Calculates maxData, joinData, chainData and monitorSetData. Numbers represent line numbers in the algorithm
     * presented in thesis.
     */
    private void calculateStaticData() { // 1
	for (BaseEvent baseEvent : finiteSpec.getBaseEvents()) { // 3
	    final Set<Parameter<?>> parameterSet = baseEvent.getParameters(); // 4
	    for (Set<Parameter<?>> enablingParameterSet : getEnableSetsInReverseTopologicalOrdering(baseEvent)) { // 10
		if (!enablingParameterSet.equals(EMPTY_PARAMETER_SET)
			&& !isSubsetEq(parameterSet, enablingParameterSet)) { // 11
		    if (isSuperset(parameterSet, enablingParameterSet)) { // 12
			maxData.get(baseEvent).add(enablingParameterSet); // 13
		    } else { // 14
			final Set<Parameter<?>> compatibleSubset = intersection(parameterSet, enablingParameterSet); // 15
			final Tuple<Set<Parameter<?>>, Set<Parameter<?>>> tuple = tuple(compatibleSubset,
				enablingParameterSet);
			joinData.put(baseEvent, tuple); // 16
			chainData.put(enablingParameterSet, tuple); // 17
			if (updates.contains(tuple)) { // 18
			    monitorSetData.put(compatibleSubset, tuple(enablingParameterSet, true)); // 19
			} else { // 20
			    monitorSetData.put(compatibleSubset, tuple(enablingParameterSet, false)); // 21
			} // 22
		    } // 23
		} // 24
	    } // 25
	} // 26
	for (Tuple<Set<Parameter<?>>, Set<Parameter<?>>> tuple : updates) { // 27
	    if (!monitorSetData.get(tuple.getLeft()).contains(tuple(tuple.getRight(), true))) { // 28
		chainData.put(tuple.getRight(), tuple(tuple.getLeft(), EMPTY_PARAMETER_SET)); // 29
		monitorSetData.put(tuple.getLeft(), tuple(EMPTY_PARAMETER_SET, true)); // 30
	    } // 30
	} // 31
    }// 32

    private List<Set<Parameter<?>>> getEnableSetsInReverseTopologicalOrdering(BaseEvent baseEvent) {
	List<Set<Parameter<?>>> enableSetInReverseTopolicalOrdering = new ArrayList<Set<Parameter<?>>>(
		enablingParameterSets.get(baseEvent));
	Collections.sort(enableSetInReverseTopolicalOrdering, Util.REVERSE_TOPOLOGICAL_SET_COMPARATOR);
	return enableSetInReverseTopolicalOrdering;
    }

    private void calculateAcceptingParameters() {
	// TODO implement calculateAcceptingParameters
	boolean[] acceptingParameters = new boolean[finiteSpec.getFullParameterSet().size()];
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
    public SetMultimap<Set<Parameter<?>>, Tuple<Set<Parameter<?>>, Boolean>> getMonitorSetData() {
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

    @Override
    public Set<Parameter<?>> getParameters() {
	return finiteSpec.getFullParameterSet();
    }

    public Set<Tuple<Set<Parameter<?>>, Set<Parameter<?>>>> getUpdates() {
	return updates;
    }

}
