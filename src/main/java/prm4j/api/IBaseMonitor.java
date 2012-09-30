/*
 * Copyright (c) 2012 Mateusz Parzonka, Eric Bodden
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Eric Bodden - initial API
 * Mateusz Parzonka - adapted API
 */
package prm4j.api;

import prm4j.old.v2.indexing.Event;

/**
 * A base monitor keeps track of a trace of events for a fixed set of bindings.
 *
 * @param <A>
 *            the type of the auxiliary data usable by base monitors
 * @param <M>
 *            the type of the base monitor
 */
public interface IBaseMonitor<A, M extends IBaseMonitor<A, M>> {

    /**
     * Updates the base monitors internal state by consuming an parametric event. After processing the event, the
     * monitor is either alive or dead. A dead monitor will no longer process events.
     *
     * @return <code>true</code> if a the monitor is still alive
     */
    public abstract boolean processEvent(Event<A> event);

    /**
     * The monitor decides if a final state is reachable based on its current internal state. This allows efficient
     * indexing strategies to remove the monitor instance in case it should be impossible to reach any final state.
     *
     * @param bindings
     * @return <code>true</code> if a final state is still reachable
     */
    public abstract boolean isFinalStateReachable();

    /**
     * Creates a deep copy of this base monitor.
     */
    public abstract M copy();

}
