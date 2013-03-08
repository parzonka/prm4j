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

import prm4j.api.BaseEvent;
import prm4j.api.MatchHandler;

/**
 * Models the internal state of a {@link StatefulMonitor} where the state space may be finite or infinite.
 *
 * @see  {@link Monitor}
 */
public abstract class AbstractMonitorState {

    /**
     * Unique index for finite state spaces.
     */
    private final int index;

    /**
     * Creates a monitor state assuming a finite state space. The monitor will have an unique index.
     *
     * @param index
     */
    public AbstractMonitorState(int index) {
	this.index = index;
    }

    /**
     * Creates a monitor state assuming an infinite state space. This monitor will not have an unique index.
     */
    public AbstractMonitorState() {
	index = -1;
    }

    public abstract AbstractMonitorState getSuccessor(BaseEvent baseEvent);

    /**
     * Tests, if the current state is an accepting state.
     *
     * @return <code>true</code> if the current state is accepting
     */
    public abstract boolean isAccepting();

    public abstract MatchHandler getMatchHandler();

    /**
     * @return <code>true</code> if the state space is finite, of <code>false</code> if there may exist an infinite
     *         number of possible states.
     */
    public boolean isFiniteStateSpace() {
	return index >= 0;
    }

    /**
     * Tests, if all successor states are dead states. (A dead state is a non-accepting state, where all successors are
     * dead states. Because final state <i>may</i> be an accepting state, it is not always a dead state.)
     *
     * @return <code>true</code> if all successor states are dead states
     */
    public abstract boolean isFinal();

    /**
     * Returns the unique index of this state if the state space is finite. A valid index is a non-negative integer.
     *
     * @return the unique index of this state or -1 if the state space is infinite.
     */
    public int getIndex() {
	return index;
    }

}