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
package prm4j.spec.finite;

import static prm4j.Util.tuple;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import prm4j.Util;
import prm4j.Util.Tuple;
import prm4j.api.BaseEvent;
import prm4j.api.Parameter;
import prm4j.indexing.monitor.MonitorState;
import prm4j.spec.ParametricProperty;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;

public class FiniteParametricProperty implements ParametricProperty {

    private final FiniteSpec finiteSpec;
    private final Set<MonitorState> partEnableStates;
    private final MonitorState partEnableInitialState;

    public FiniteParametricProperty(FiniteSpec finiteSpec) {
	this.finiteSpec = finiteSpec;
	partEnableInitialState = new PartEnableInitialState(finiteSpec.getInitialState());
	partEnableStates = getPartEnableStates(finiteSpec);
    }

    /**
     * Decorate the initial state with a monitor state which has no self-loops.
     * 
     * @param finiteSpec
     * @return
     */
    private Set<MonitorState> getPartEnableStates(FiniteSpec finiteSpec) {
	Set<MonitorState> result = new HashSet<MonitorState>();
	for (MonitorState ms : finiteSpec.getStates()) {
	    if (ms.isInitial()) {
		result.add(new PartEnableInitialState(ms));
	    } else {
		result.add(ms);
	    }
	}
	return result;
    }

    @Override
    public FiniteSpec getSpec() {
	return finiteSpec;
    }

    @Override
    public Set<BaseEvent> getCreationEvents() {
	Set<BaseEvent> creationEvents = new HashSet<BaseEvent>();
	MonitorState initialState = finiteSpec.getInitialState();
	for (BaseEvent symbol : finiteSpec.getBaseEvents()) {
	    MonitorState successor = initialState.getSuccessor(symbol);
	    if (successor != initialState) {
		creationEvents.add(symbol);
	    }
	}
	return creationEvents;
    }

    @Override
    public Set<BaseEvent> getDisableEvents() {
	Set<BaseEvent> disableEvents = new HashSet<BaseEvent>();
	MonitorState initialState = finiteSpec.getInitialState();
	for (BaseEvent symbol : finiteSpec.getBaseEvents()) {
	    MonitorState successor = initialState.getSuccessor(symbol);
	    if (successor == null) {
		disableEvents.add(symbol);
	    }
	}
	return disableEvents;
    }

    @Override
    public SetMultimap<BaseEvent, Set<Parameter<?>>> getEnableParameterSets() {
	final SetMultimap<BaseEvent, Set<BaseEvent>> event2eventSets = HashMultimap.create();
	new FSMVisitor(finiteSpec.getBaseEvents()) {
	    @Override
	    public void processTransition(MonitorState thisState, BaseEvent baseEvent, MonitorState nextState,
		    Set<BaseEvent> inducedEventSet) {
		if (nextState != null) {
		    event2eventSets.put(baseEvent, Sets.difference(inducedEventSet, Util.set(baseEvent)));
		}
	    }
	}.visit(partEnableInitialState);
	return toParameterSetMultimap(event2eventSets);
    }

    /**
     * Returns a mapping of state to a set of parameter sets {...,X',...} such that a monitor for instance i is alive,
     * when there exists an X' such that all bindings i(x) for all x in X' are alive.
     * 
     * @return mapping of state to a set of alive parameter sets
     */
    public SetMultimap<MonitorState, Set<Parameter<?>>> getState2AliveParameterSets() {
	SetMultimap<MonitorState, Set<Parameter<?>>> set2aliveParams = HashMultimap.create();
	for (MonitorState state : partEnableStates) {
	    FSMVisitor visitor = new FSMVisitor(finiteSpec.getBaseEvents());
	    visitor.visit(state);
	    for (MonitorState visitedState : visitor.getState2InducedEventSets().keySet()) {
		Set<Set<Parameter<?>>> parameterSets = new HashSet<Set<Parameter<?>>>();
		if (visitedState.isAccepting()) {
		    parameterSets.addAll(toParameterSets(visitor.getState2InducedEventSets().get(visitedState)));
		}
		set2aliveParams.get(state).addAll(toSubsetMinimalParameterSets(parameterSets));
	    }
	}
	return set2aliveParams;
    }

    /**
     * Returns the subset-minimal set of the given parameter set, i.e., for all X in parameter sets remove all supersets
     * of X.
     * 
     * @param parameterSets
     * @return the subset-minimal set of the given parameter set
     */
    private static Set<Set<Parameter<?>>> toSubsetMinimalParameterSets(Set<Set<Parameter<?>>> parameterSets) {
	final Set<Set<Parameter<?>>> minimizedParameterSets = new HashSet<Set<Parameter<?>>>(parameterSets);
	for (Set<Parameter<?>> parameterSet : parameterSets) {
	    final Iterator<Set<Parameter<?>>> iter = minimizedParameterSets.iterator();
	    while (iter.hasNext()) {
		if (Util.isSuperset(iter.next(), parameterSet)) {
		    iter.remove();
		}
	    }
	}
	return minimizedParameterSets;
    }

    /**
     * Returns a mapping of parameter set X to a set of parameter sets {...,X',...} such that an monitor for instance i
     * with Dom(i) = X is alive, when there exists an X' such that all bindings i(x) for all x in X' are alive.
     */
    @Override
    public SetMultimap<Set<Parameter<?>>, Set<Parameter<?>>> getAliveParameterSets() {
	final SetMultimap<MonitorState, Set<Parameter<?>>> state2aliveParameterSets = getState2AliveParameterSets();
	final FSMVisitor visitor = new FSMVisitor(finiteSpec.getBaseEvents());
	visitor.visit(partEnableInitialState);
	Multimap<Set<Parameter<?>>, MonitorState> paramSet2monitorStates = reverseSetMultimap(toParameterSetMultimap(visitor
		.getState2InducedEventSets()));
	final SetMultimap<Set<Parameter<?>>, Set<Parameter<?>>> result = HashMultimap.create();
	for (Set<Parameter<?>> parameterSet : paramSet2monitorStates.keySet()) {
	    final Set<Set<Parameter<?>>> parameterSets = new HashSet<Set<Parameter<?>>>();
	    for (MonitorState monitorState : paramSet2monitorStates.get(parameterSet)) {
		state2aliveParameterSets.keySet().contains(monitorState);
		parameterSets.addAll(state2aliveParameterSets.get(monitorState));
	    }
	    result.get(parameterSet).addAll(toSubsetMinimalParameterSets(parameterSets));
	}
	return result;
    }

    /**
     * Returns the update relation X -> X' where an instance i with Dom(i) = X propagates events to an instance i' with
     * Dom(i') = X'.
     */
    @Override
    public Set<Tuple<Set<Parameter<?>>, Set<Parameter<?>>>> getUpdates() {
	final Set<Tuple<Set<Parameter<?>>, Set<Parameter<?>>>> updates = new HashSet<Tuple<Set<Parameter<?>>, Set<Parameter<?>>>>();
	new FSMVisitor(finiteSpec.getBaseEvents()) {
	    @Override
	    public void processTransition(MonitorState thisState, BaseEvent baseEvent, MonitorState nextState,
		    Set<BaseEvent> inducedEventSet) {
		// 1. "thisState != nextState" filters non-state changing updates
		// 2. "!baseEvent.getParameters().equals(toParameterSet(inducedEventSet))" filters self-updates
		if (thisState != nextState && !baseEvent.getParameters().equals(toParameterSet(inducedEventSet))) {
		    updates.add(tuple(baseEvent.getParameters(), toParameterSet(inducedEventSet)));
		}
	    }
	}.visit(finiteSpec.getInitialState());
	return updates;
    }

    private static <A, B> Multimap<B, A> reverseSetMultimap(Multimap<A, B> multiMap) {
	SetMultimap<B, A> reversedSetMultimap = HashMultimap.create();
	for (A a : multiMap.keySet()) {
	    for (B b : multiMap.get(a)) {
		reversedSetMultimap.put(b, a);
	    }
	}
	return reversedSetMultimap;
    }

    /**
     * Return a multimap {something -> parameterset} for a multimap {something -> baseEvent}.
     * 
     * @param baseEventMultimap
     * @return
     */
    private static <T> SetMultimap<T, Set<Parameter<?>>> toParameterSetMultimap(
	    Multimap<T, Set<BaseEvent>> baseEventMultimap) {
	final SetMultimap<T, Set<Parameter<?>>> result = HashMultimap.create();
	final Map<T, Collection<Set<BaseEvent>>> mapBaseEvent2SetOfSetOfBaseEvents = baseEventMultimap.asMap();
	for (Entry<T, Collection<Set<BaseEvent>>> entry : mapBaseEvent2SetOfSetOfBaseEvents.entrySet()) {
	    for (Set<Parameter<?>> parameterSet : toParameterSets(entry.getValue())) {
		result.put(entry.getKey(), parameterSet);
	    }
	}
	return result;
    }

    /**
     * Return a set of parameter sets for a collection of base event sets.
     * 
     * @param collectionOfEventSets
     * @return set of parameter sets
     */
    private static Set<Set<Parameter<?>>> toParameterSets(Collection<Set<BaseEvent>> collectionOfEventSets) {
	final Set<Set<Parameter<?>>> result = new HashSet<Set<Parameter<?>>>();
	for (Set<BaseEvent> eventSet : collectionOfEventSets) {
	    result.add(toParameterSet(eventSet));
	}
	return result;
    }

    /**
     * Return a parameter set for a collection of base events.
     * 
     * @param eventCollection
     * @return
     */
    private static Set<Parameter<?>> toParameterSet(Collection<BaseEvent> eventCollection) {
	Set<Parameter<?>> parameterSet = new HashSet<Parameter<?>>();
	for (BaseEvent baseEvent : eventCollection) {
	    parameterSet.addAll(baseEvent.getParameters());
	}
	return parameterSet;
    }
}
