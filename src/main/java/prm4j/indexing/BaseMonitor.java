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
package prm4j.indexing;

/**
 * An {@link BaseMonitor} is a concrete monitor instance, representing the internal state of an
 * {@link ParametricMonitor} for one single concrete variable binding.
 * <p>
 * Usage: This interface may be implemented by custom monitors to enable interplay with custom indexing strategies. To
 * implement custom monitors to work with the provided indexing strategies, users should subclass the abstract base
 * monitors associated with those strategies instead.
 *
 * @param <E>
 *            the type of base event processed by monitors
 * @param <M>
 *            the type of the base monitor
 */
public interface BaseMonitor<E, M extends BaseMonitor<E, M>> {

    /**
     * Updates the base monitors internal state by consuming an base event. After processing the event, the monitor is
     * either alive or dead. A dead monitor will no longer process events.
     *
     * @return <code>true</code> if a the monitor is still alive
     */
    public abstract boolean processEvent(E baseEvent);

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
