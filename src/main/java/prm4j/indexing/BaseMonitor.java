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

import prm4j.indexing.treebased.impl.DefaultParametricMonitor;

/**
 * An {@link BaseMonitor} is a concrete monitor instance, representing the internal state of an
 * {@link ParametricMonitor} for one single concrete variable binding.
 * <p>
 * Usage: This interface may be implemented by custom monitors to enable interplay with custom parametric monitors. To
 * implement custom monitors to work with the provided {@link DefaultParametricMonitor}, users should subclass the
 * {@link AbstractBaseMonitor} instead.
 *
 * @param <M>
 *            the type of the base monitor
 */
public interface BaseMonitor<M extends BaseMonitor<M>> {

    /**
     * Updates the base monitors internal state by consuming an event. After processing the event, the monitor is either
     * alive or dead. A dead monitor will no longer process events.
     *
     * @return <code>true</code> if a the monitor is still alive
     */
    public abstract boolean processEvent(Event event);

    /**
     * The monitor decides if a final state is reachable based on its current internal state. This allows efficient
     * parametric monitors to remove the monitor instance in case it should be impossible to reach any final state.
     *
     * @return <code>true</code> if a final state is still reachable
     */
    public abstract boolean isFinalStateReachable();

    /**
     * Creates a deep copy of this base monitor.
     */
    public abstract M copy();

}
