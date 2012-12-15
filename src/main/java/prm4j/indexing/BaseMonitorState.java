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
import prm4j.api.MatchHandler;

public abstract class BaseMonitorState {

    private final int index;

    public BaseMonitorState(int index) {
	this.index = index;
    }

    public abstract BaseMonitorState getSuccessor(BaseEvent baseEvent);

    /**
     * Tests, if the current state is an accepting state.
     *
     * @return <code>true</code> if the current state is accepting
     */
    public abstract boolean isAccepting();

    public abstract MatchHandler getMatchHandler();

    /**
     * Tests, if all successor states are dead states. (A dead state is a non-accepting state, where all successors are
     * dead states. Because final state <i>may</i> be an accepting state, it is not always a dead state.)
     *
     * @return <code>true</code> if all successor states are dead states
     */
    public abstract boolean isFinal();

    public int getIndex() {
	return index;
    }

}