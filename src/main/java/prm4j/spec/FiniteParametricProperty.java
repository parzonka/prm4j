/*
 * Copyright (c) 2012, 2013 Mateusz Parzonka
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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
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
import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;

/**
 * Immutable self-calculating data object.
 */
public class FiniteParametricProperty implements ParametricProperty {

    private final static Set<Parameter<?>> EMPTY_PARAMETER_SET = new HashSet<Parameter<?>>();

    private final FiniteSpec finiteSpec;

    private final ParameterSets parameterSets;
    private final RealtimeAlgorithmArguments realtimeAlgorithmArguments;
    private final CoenableSets coenableSets;

    public FiniteParametricProperty(FiniteSpec finiteSpec) {

	this.finiteSpec = finiteSpec;
	parameterSets = new ParameterSets();
	realtimeAlgorithmArguments = new RealtimeAlgorithmArguments();
	coenableSets = new CoenableSets();
    }

    private class ParameterSets {

	private final Set<BaseEvent> creationEvents;
	private final Set<BaseEvent> disablingEvents;
	private final SetMultimap<BaseEvent, Set<BaseEvent>> enablingEventSets;
	private final Set<Tuple<Set<Parameter<?>>, Set<Parameter<?>>>> updates;
	private final Set<Set<Parameter<?>>> possibleParameterSets;
	private final SetMultimap<BaseEvent, Set<Parameter<?>>> enablingParameterSets;
	private final SetMultimap<BaseMonitorState, Set<BaseEvent>> stateToSeenBaseEvents;

	public ParameterSets() {
	    creationEvents = calculateCreationEvents();
	    disablingEvents = calculateDisablingEvents();
	    enablingEventSets = HashMultimap.create();
	    updates = new HashSet<Tuple<Set<Parameter<?>>, Set<Parameter<?>>>>();
	    possibleParameterSets = new HashSet<Set<Parameter<?>>>();
	    stateToSeenBaseEvents = HashMultimap.create();
	    calculateCreationEvents();
	    calculateDisablingEvents();
	    computeRelations(finiteSpec.getInitialState(), new HashSet<BaseEvent>(), new HashSet<Parameter<?>>()); // 2
	    enablingParameterSets = toMap2SetOfSetOfParameters(enablingEventSets);
	}

	/**
	 * Creation events are events for which the successor of the initial state is a non-initial state (including the
	 * a dead state).
	 * 
	 * @return the creation events
	 */
	private Set<BaseEvent> calculateCreationEvents() {
	    Set<BaseEvent> creationEvents = new HashSet<BaseEvent>();
	    BaseMonitorState initialState = finiteSpec.getInitialState();
	    for (BaseEvent symbol : finiteSpec.getBaseEvents()) {
		BaseMonitorState successor = initialState.getSuccessor(symbol);
		if (successor != initialState) {
		    creationEvents.add(symbol);
		}
	    }
	    return creationEvents;
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

	private void computeRelations(BaseMonitorState state, Set<BaseEvent> seenBaseEvents,
		Set<Parameter<?>> parameterSet) { // 5
	    stateToSeenBaseEvents.put(state, seenBaseEvents); // 6
	    possibleParameterSets.add(parameterSet); // 7
	    for (BaseEvent baseEvent : finiteSpec.getBaseEvents()) { // 8
		final BaseMonitorState nextState = state.getSuccessor(baseEvent);

		final Set<Parameter<?>> nextParameterSet = unmodifiableUnion(parameterSet, baseEvent.getParameters()); // 12
		// add to updates only if the base event does change state and it is no self-update:
		if (state != nextState && !baseEvent.getParameters().equals(nextParameterSet)) {
		    updates.add(tuple(baseEvent.getParameters(), nextParameterSet));
		}
		/*
		 * (TODO) instead of just checking if nextState is null, we should also check if next state lies on a
		 * path to an accepting state. If the property is defined properly this should be always the case
		 * though.
		 */
		// We do not follow self-loops on the initial state. this is important to maintain total matching
		// semantics without user-annotation of creation events.
		if (nextState != null && !(state == getInitialState() && nextState == getInitialState())) {
		    // we remove the current base event because an event does not need to enable itself
		    final Set<BaseEvent> seenBaseEventsWithoutCurrentBaseEvent = unmodifiableDifference(seenBaseEvents,
			    set(baseEvent)); // 10

		    enablingEventSets.put(baseEvent, seenBaseEventsWithoutCurrentBaseEvent); // 10

		    // compute from next different state if state was not visited before carrying the same
		    // nextSeenBaseEvents
		    final Set<BaseEvent> nextSeenBaseEvents = unmodifiableUnion(seenBaseEvents, set(baseEvent)); // 11
		    if (!stateToSeenBaseEvents.containsEntry(state, nextSeenBaseEvents)) { // 11
			computeRelations(state.getSuccessor(baseEvent), nextSeenBaseEvents, nextParameterSet); // 12
		    } // 13
		} // 14
	    } // 15
	} // 16

	private <T> SetMultimap<T, Set<Parameter<?>>> toMap2SetOfSetOfParameters(
		Multimap<T, Set<BaseEvent>> multimapBaseEvent2SetOfBaseEvents) {
	    SetMultimap<T, Set<Parameter<?>>> result = HashMultimap.create();
	    Map<T, Collection<Set<BaseEvent>>> mapBaseEvent2SetOfSetOfBaseEvents = multimapBaseEvent2SetOfBaseEvents
		    .asMap();
	    for (Entry<T, Collection<Set<BaseEvent>>> entry : mapBaseEvent2SetOfSetOfBaseEvents.entrySet()) {
		for (Set<Parameter<?>> parameterSet : toParameterSets(entry.getValue())) {
		    result.put(entry.getKey(), parameterSet);
		}
	    }
	    return result;
	}
    }

    /**
     * Calculates maxData, joinData, chainData and monitorSetData. Numbers represent line numbers in the algorithm
     * presented in thesis.
     */
    private class RealtimeAlgorithmArguments {

	private final ListMultimap<BaseEvent, Set<Parameter<?>>> maxData;
	private final ListMultimap<BaseEvent, Tuple<Set<Parameter<?>>, Set<Parameter<?>>>> joinData;
	private final SetMultimap<Set<Parameter<?>>, Tuple<Set<Parameter<?>>, Set<Parameter<?>>>> chainData;
	private final SetMultimap<Set<Parameter<?>>, Tuple<Set<Parameter<?>>, Boolean>> monitorSetData;

	public RealtimeAlgorithmArguments() {
	    maxData = ArrayListMultimap.create();
	    joinData = ArrayListMultimap.create();
	    chainData = HashMultimap.create();
	    monitorSetData = HashMultimap.create();
	    main();
	}

	private void main() { // 1
	    for (BaseEvent baseEvent : finiteSpec.getBaseEvents()) { // 3
		final Set<Parameter<?>> parameterSet = baseEvent.getParameters(); // 4
		for (Set<Parameter<?>> enablingParameterSet : getEnablingParameterSetsInReverseTopologicalOrdering(baseEvent)) { // 5
		    /*
		     * the empty parameter set {} can be filtered. No parameter set can contain less elements, so there
		     * can be no maxData = (X -> {}). And a joindata = (e -> ( {} -> {} )) makes no sense either. The
		     * same with chaining from {} to {} and updates.
		     */
		    if (!enablingParameterSet.equals(EMPTY_PARAMETER_SET)
			    && !isSubsetEq(parameterSet, enablingParameterSet)) { // 6
			if (isSuperset(parameterSet, enablingParameterSet)) { // 7
			    maxData.put(baseEvent, enablingParameterSet); // 8
			} else { // 9
			    final Set<Parameter<?>> compatibleSubset = intersection(parameterSet, enablingParameterSet); // 10
			    final Tuple<Set<Parameter<?>>, Set<Parameter<?>>> tuple = tuple(compatibleSubset,
				    enablingParameterSet);
			    joinData.put(baseEvent, tuple); // 11
			    chainData.put(enablingParameterSet, tuple); // 12
			    if (parameterSets.updates.contains(tuple)) { // 13
				monitorSetData.put(compatibleSubset, tuple(enablingParameterSet, true)); // 14
			    } else { // 15
				monitorSetData.put(compatibleSubset, tuple(enablingParameterSet, false)); // 16
			    } // 17
			} // 18
		    } // 19
		} // 20
	    } // 21
	    for (Tuple<Set<Parameter<?>>, Set<Parameter<?>>> tuple : parameterSets.updates) { // 22
		if (!monitorSetData.containsEntry(tuple._1(), tuple(tuple._2(), true))) { // 23
		    chainData.put(tuple._2(), tuple(tuple._1(), EMPTY_PARAMETER_SET)); // 24
		    monitorSetData.put(tuple._1(), tuple(EMPTY_PARAMETER_SET, true)); // 25
		} // 26
	    } // 27
	}// 28

	private List<Set<Parameter<?>>> getEnablingParameterSetsInReverseTopologicalOrdering(BaseEvent baseEvent) {
	    List<Set<Parameter<?>>> enableSetInReverseTopolicalOrdering = new ArrayList<Set<Parameter<?>>>(
		    parameterSets.enablingParameterSets.get(baseEvent));
	    Collections.sort(enableSetInReverseTopolicalOrdering, Util.REVERSE_TOPOLOGICAL_SET_COMPARATOR);
	    return enableSetInReverseTopolicalOrdering;
	}

    }

    private class CoenableSets {

	private final SetMultimap<BaseMonitorState, BaseMonitorState> reversedFSM;
	private final Set<BaseMonitorState> acceptingStates;
	private final Set<Set<Parameter<?>>> aliveParameterSets;

	public CoenableSets() {
	    reversedFSM = HashMultimap.create();
	    acceptingStates = new HashSet<BaseMonitorState>();
	    aliveParameterSets = new HashSet<Set<Parameter<?>>>();
	    reverseIter(finiteSpec.getInitialState(), new HashSet<BaseMonitorState>());
	    for (BaseMonitorState acceptingState : acceptingStates) {
		alivenessIter(acceptingState, new HashSet<Parameter<?>>(), new HashSet<BaseMonitorState>());
	    }
	    minimizeAliveParameterSets();
	}

	public void reverseIter(BaseMonitorState state, Set<BaseMonitorState> visited) {
	    if (state.isAccepting()) {
		acceptingStates.add(state);
	    }
	    for (BaseEvent baseEvent : finiteSpec.getBaseEvents()) {
		BaseMonitorState successor = state.getSuccessor(baseEvent);
		if (successor != null && !visited.contains(successor)) {
		    reversedFSM.put(successor, state);
		    reverseIter(successor, unmodifiableUnion(visited, set(successor)));
		}
	    }
	}

	/**
	 * 
	 * @param state
	 *            the current state for this recursion
	 * @param parameterSet
	 *            contains all parameters which had alive bindings when the accepting state was reached
	 * @param visited
	 *            aggregate visited states so that the recursion will terminate
	 */
	public void alivenessIter(BaseMonitorState state, Set<Parameter<?>> parameterSet, Set<BaseMonitorState> visited) {
	    // 'predessor' means a preceding state in the *unreversed* FSM!
	    for (BaseMonitorState predessor : reversedFSM.get(state)) {
		if (!visited.contains(predessor)) {
		    // iterate through all edges which lead from the predessor to the current state
		    for (BaseEvent baseEvent : getBaseEvent(predessor, state)) {
			aliveParameterSets.add(unmodifiableUnion(parameterSet, baseEvent.getParameters()));
			alivenessIter(predessor, unmodifiableUnion(parameterSet, baseEvent.getParameters()),
				unmodifiableUnion(visited, set(predessor)));
		    }
		}
	    }
	}

	public Set<BaseEvent> getBaseEvent(BaseMonitorState from, BaseMonitorState to) {
	    Set<BaseEvent> result = new HashSet<BaseEvent>();
	    for (BaseEvent baseEvent : finiteSpec.getBaseEvents()) {
		if (from.getSuccessor(baseEvent) == to) {
		    result.add(baseEvent);
		}
	    }
	    return result;
	}

	/**
	 * Remove all supersets of contained sets.
	 */
	private void minimizeAliveParameterSets() {
	    for (Set<Parameter<?>> parameterSet : new HashSet<Set<Parameter<?>>>(aliveParameterSets)) {
		final Iterator<Set<Parameter<?>>> iter = aliveParameterSets.iterator();
		while (iter.hasNext()) {
		    if (Util.isSuperset(iter.next(), parameterSet)) {
			iter.remove();
		    }
		}
	    }
	}

    }

    private static Set<Set<Parameter<?>>> toParameterSets(Collection<Set<BaseEvent>> propertyEnableSet) {
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

    @Override
    public boolean isFinite() {
	return true;
    }

    @Override
    public int getStateCount() {
	return finiteSpec.getStates().size();
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
	return parameterSets.creationEvents;
    }

    /**
     * Disabling events are events for which the successor of the initial state is a dead state.
     * 
     * @return the disabling events
     */
    @Override
    public Set<BaseEvent> getDisablingEvents() {
	return parameterSets.disablingEvents;
    }

    @Override
    public ListMultimap<BaseEvent, Set<Parameter<?>>> getMaxData() {
	return realtimeAlgorithmArguments.maxData;
    }

    @Override
    public ListMultimap<BaseEvent, Tuple<Set<Parameter<?>>, Set<Parameter<?>>>> getJoinData() {
	return realtimeAlgorithmArguments.joinData;
    }

    @Override
    public SetMultimap<Set<Parameter<?>>, Tuple<Set<Parameter<?>>, Set<Parameter<?>>>> getChainData() {
	return realtimeAlgorithmArguments.chainData;
    }

    @Override
    public SetMultimap<Set<Parameter<?>>, Tuple<Set<Parameter<?>>, Boolean>> getMonitorSetData() {
	return realtimeAlgorithmArguments.monitorSetData;
    }

    @Override
    public SetMultimap<BaseEvent, Set<BaseEvent>> getEnablingEventSets() {
	return parameterSets.enablingEventSets;
    }

    @Override
    public Set<Set<Parameter<?>>> getPossibleParameterSets() {
	return Collections.unmodifiableSet(parameterSets.possibleParameterSets);
    }

    @Override
    public SetMultimap<BaseEvent, Set<Parameter<?>>> getEnablingParameterSets() {
	return parameterSets.enablingParameterSets;
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
	return parameterSets.updates;
    }

    @Override
    public Set<Set<Parameter<?>>> getAliveParameterSets() {
	return coenableSets.aliveParameterSets;
    }

}
