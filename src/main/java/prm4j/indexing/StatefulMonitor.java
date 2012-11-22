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
package prm4j.indexing;

import prm4j.api.BaseEvent;
import prm4j.api.Event;
import prm4j.api.MatchHandler;

/**
 * A base monitor holding a {@link BaseMonitorState} which is updated when processing {@link BaseEvent}s.
 *
 */
public class StatefulMonitor extends BaseMonitor {

    protected BaseMonitorState state;

    public StatefulMonitor(BaseMonitorState state) {
	this.state = state;
    }

    @Override
    public boolean processEvent(Event event) {
	if (state == null) {
	    terminate();
	    return false;
	}
	state = state.getSuccessor(event.getBaseEvent());
	if (state == null) {
	    terminate();
	    return false;
	}
	MatchHandler matchHandler = state.getMatchHandler();
	if (matchHandler != null) {
	    matchHandler.handleMatch(getBindings(), event.getAuxiliaryData());
	    // when a state is a final state, it is still possible we will reach another final state (or loop on a
	    // final state), so we don't return false here
	    if (state.isFinal()) {
		terminate();
		return false;
	    }
	}
	return true;
    }

    @Override
    public BaseMonitor copy() {
	return new StatefulMonitor(state);
    }

    @Override
    public boolean isAcceptingStateReachable() {
	// TODO co-enable set calculation or similar
	return state != null && !state.isFinal();
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((state == null) ? 0 : state.hashCode());
	return result;
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	StatefulMonitor other = (StatefulMonitor) obj;
	if (state == null) {
	    if (other.state != null)
		return false;
	} else if (!state.equals(other.state))
	    return false;
	return true;
    }

}