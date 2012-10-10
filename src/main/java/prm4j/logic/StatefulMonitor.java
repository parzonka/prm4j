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

import prm4j.api.MatchHandler;
import prm4j.api.Symbol;
import prm4j.indexing.AbstractBaseMonitor;
import prm4j.indexing.Event;

/**
 * A base monitor holding a {@link MonitorState} which is updated when processing {@link Event}s.
 *
 */
public class StatefulMonitor extends AbstractBaseMonitor<Symbol> {

    private MonitorState<Symbol> state;

    public StatefulMonitor(MonitorState<Symbol> state) {
	this.state = state;
    }

    @Override
    public boolean processEvent(Symbol baseEvent) {
	state = state.getSuccessor(baseEvent);
	MatchHandler matchHandler = state.getMatchHandler();
	if (matchHandler != null) {
	    matchHandler.handleMatch(getBindings());
	    // when a state is a final state it is still possible we will reach another final state (or loop on the a
	    // final state), so we don't return false here
	}
	return true;
    }

    @Override
    public StatefulMonitor copy() {
	return new StatefulMonitor(state);
    }

    @Override
    public boolean isFinalStateReachable() {
	// TODO co-enable set calculation or similar
	return true;
    }

}
