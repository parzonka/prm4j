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
package prm4j.indexing.monitor;

import prm4j.Globals;
import prm4j.api.BaseEvent;
import prm4j.api.Event;
import prm4j.api.MatchHandler;

/**
 * A base monitor holding a {@link AbstractMonitorState} which is updated when processing {@link BaseEvent}s.
 */
public class StatefulMonitor extends AbstractMonitor {

    protected MonitorState state;

    public StatefulMonitor(MonitorState state) {
	this.state = state;
    }

    @Override
    public boolean processEvent(Event event) {
	if (state == null) {
	    terminate();
	    return false;
	}
	final BaseEvent baseEvent = event.getEvaluatedBaseEvent(this);
	if (baseEvent == null) {
	    // the condition evaluated to false, no transition is taken, monitor was alive => stays alive
	    return true;
	}
	state = state.getSuccessor(baseEvent);
	if (state == null) {
	    terminate();
	    return false;
	}
	final MatchHandler matchHandler = state.getMatchHandler();
	if (matchHandler != null) {
	    matchHandler.handleAndCountMatch(getUncompressedBindings(), event.getAuxiliaryData());
	    // when a state is a accepting state, it is still possible we will reach another accepting state (or loop on
	    // an accepting state)
	}
	if (state.isFinal()
		|| (Globals.CHECK_MONITOR_VALIDITY_ON_EACH_UPDATE && !getParameterNode().isAcceptingStateReachable(
			state, getCompressedBindings()))) {
	    terminate();
	    return false;
	}
	return true;
    }

    @Override
    public Monitor copy() {
	return new StatefulMonitor(state);
    }

    /**
     * {@inheritDoc}
     * <p>
     * The {@link StatefulMonitor} returns <code>true</code> if each of the following is true:
     * <ol>
     * <li>The monitor is not terminated.</li>
     * <li>Its state is not dead or a final state (a state where only dead states may be reached).</li>
     * <li>A subset of its bindings is alive that is necessary to reach an accepting state.</li>
     * </ol>
     */
    @Override
    public boolean isAlive() {
	return !isTerminated() && state != null && !state.isFinal()
		&& getParameterNode().isAcceptingStateReachable(state, getCompressedBindings());
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
	if (this == obj) {
	    return true;
	}
	if (obj == null) {
	    return false;
	}
	if (getClass() != obj.getClass()) {
	    return false;
	}
	StatefulMonitor other = (StatefulMonitor) obj;
	if (state == null) {
	    if (other.state != null) {
		return false;
	    }
	} else if (!state.equals(other.state)) {
	    return false;
	}
	return true;
    }

    @Override
    public boolean isDead() {
	return false;
    }

}