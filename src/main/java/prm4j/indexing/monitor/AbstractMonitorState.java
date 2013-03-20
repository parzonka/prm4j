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
 * @see {@link Monitor}
 */
public abstract class AbstractMonitorState implements MonitorState {

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

    @Override
    public abstract MonitorState getSuccessor(BaseEvent baseEvent);

    @Override
    public abstract boolean isAccepting();

    @Override
    public abstract MatchHandler getMatchHandler();

    @Override
    public boolean isFiniteStateSpace() {
	return index >= 0;
    }

    @Override
    public abstract boolean isFinal();

    @Override
    public int getIndex() {
	return index;
    }

    @Override
    public int hashCode() {
	return index;
    }

    @Override
    public boolean equals(Object obj) {
	return index == ((MonitorState) obj).getIndex();
    }

}