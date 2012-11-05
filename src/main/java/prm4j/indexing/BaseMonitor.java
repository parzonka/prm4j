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

import prm4j.api.Event;
import prm4j.api.ParametricMonitor;
import prm4j.indexing.realtime.LowLevelBinding;

/**
 * Abstract base class for a concrete monitor instance, representing the internal state of a {@link ParametricMonitor}
 * for one single concrete variable binding.
 */
public abstract class BaseMonitor {

    // low level access
    private LowLevelBinding[] bindings;
    // low level access
    private long creationTime;

    /**
     * Creates a low level deep copy of this monitor.
     *
     * @param bindings
     * @return
     */
    public final BaseMonitor copy(LowLevelBinding[] bindings) {
	BaseMonitor copy = copy();
	copy.setBindings(bindings);
	copy.setCreationTime(creationTime);
	return copy;
    }

    public final BaseMonitor copy(LowLevelBinding[] bindings, long timestamp) {
	BaseMonitor copy = copy();
	copy.setBindings(bindings);
	copy.setCreationTime(timestamp);
	return copy;
    }

    private final void setBindings(LowLevelBinding[] bindings) {
	this.bindings = bindings;
    }

    public final LowLevelBinding[] getLowLevelBindings() {
	return bindings;
    }

    protected final prm4j.api.Binding[] getBindings() {
	// upcast
	return bindings;
    }

    public final long getCreationTime() {
	return creationTime;
    }

    final void setCreationTime(long creationTime) {
	this.creationTime = creationTime;
    }

    /**
     * Updates the base monitors internal state by consuming an event. After processing the event, the monitor is either
     * alive or dead. A dead monitor will no longer process events.
     *
     * @return <code>true</code> if a the monitor is still alive
     */
    public abstract boolean processEvent(Event event);

    /**
     * The monitor decides if am accepting state is reachable based on its current internal state. This allows efficient
     * parametric monitors to remove the monitor instance in case it should be impossible to reach any accepting state.
     *
     * @return <code>true</code> if an accepting state is still reachable
     */
    public abstract boolean isAcceptingStateReachable();

    /**
     * Creates a deep copy of this base monitor.
     */
    public abstract BaseMonitor copy();

}
