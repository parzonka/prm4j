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

import java.util.HashSet;
import java.util.Set;

import prm4j.Util;
import prm4j.api.BaseEvent;
import prm4j.indexing.monitor.MonitorState;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

/**
 * Encapsulates traversal of a FSM starting from a state. The visitor traverses all paths. Note: This is *not* an
 * implementation of the GoF visitor pattern.
 */
public class FSMVisitor {

    final Set<BaseEvent> baseEvents;
    private final Multimap<MonitorState, Set<BaseEvent>> state2inducedEventSets;

    public FSMVisitor(Set<BaseEvent> baseEvents) {
	this.baseEvents = baseEvents;
	state2inducedEventSets = HashMultimap.create();
    }

    public boolean visited(MonitorState state, Set<BaseEvent> eventSet) {
	final boolean result = state2inducedEventSets.get(state).contains(eventSet);
	if (!result) {
	    state2inducedEventSets.get(state).add(eventSet);
	}
	return result;
    }

    /**
     * Start traversal from this state.
     * 
     * @param state
     */
    public void visit(MonitorState state) {
	visit(state, new HashSet<BaseEvent>());
    }

    protected void visit(MonitorState state, Set<BaseEvent> baseEventSet) {
	for (BaseEvent baseEvent : baseEvents) {
	    MonitorState nextState = state.getSuccessor(baseEvent);
	    processTransition(state, baseEvent, nextState, Sets.union(baseEventSet, Util.set(baseEvent)));
	    if (nextState != null) {
		if (!visited(nextState, Sets.union(baseEventSet, Util.set(baseEvent)))) {
		    visit(nextState, Sets.union(baseEventSet, Util.set(baseEvent)));
		}
	    }
	}
    }

    /**
     * Override this method to implement processing code for a transition from thisState to nextState labeled with
     * baseEvent.
     * 
     * @param thisState
     * @param baseEvent
     * @param nextState
     * @param inducedBaseEventSet
     */
    public void processTransition(MonitorState thisState, BaseEvent baseEvent, MonitorState nextState,
	    Set<BaseEvent> inducedBaseEventSet) {
    }

    public Multimap<MonitorState, Set<BaseEvent>> getState2InducedEventSets() {
	return state2inducedEventSets;
    }

}
