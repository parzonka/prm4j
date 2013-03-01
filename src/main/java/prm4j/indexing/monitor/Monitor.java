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

import prm4j.api.Event;
import prm4j.api.ParametricMonitor;
import prm4j.indexing.binding.Binding;
import prm4j.indexing.logic.ParameterNode;

/**
 * base class for a concrete monitor instance, representing the internal state of a {@link ParametricMonitor} for one
 * single concrete variable binding.
 */
public interface Monitor {

    /**
     * Compressed bindings do not contain null values. No information about the associated parameter is available and
     * has to be maintained externally.
     * 
     * @return a compressed representation of bindings
     */
    public Binding[] getCompressedBindings();

    /**
     * Uncompressed bindings my contain null values. The position in the array represents the parameter id of the
     * associated parameter for each binding.
     * 
     * @return a uncompressed representation of bindings
     */
    public Binding[] getUncompressedBindings();

    /**
     * Ends the life-span of this monitor. A terminated monitor can be removed from all data structures it is referenced
     * by.
     */
    public void terminate();

    /**
     * Returns <code>true</code>, if the life of this monitor is over.
     * 
     * @return <code>true</code> if terminated
     */
    public boolean isTerminated();

    /**
     * @return the creation timestamp of this monitor. This refers to the creation time of this monitor, if it was
     *         created as a new monitor, or to the timestamp of the monitor it was derived from.
     */
    public long getTimestamp();

    /**
     * Updates the base monitors internal state by consuming an event. After processing the event, the monitor is either
     * alive or dead. A dead monitor will no longer process events.
     * 
     * @return <code>true</code> if a the monitor is still alive
     */
    public boolean process(Event event);

    /**
     * Updates the base monitors internal state by consuming an event. After processing the event, the monitor is either
     * alive or dead. A dead monitor will no longer process events.
     * 
     * @return <code>true</code> if a the monitor is still alive
     */
    public boolean processEvent(Event event);

    /**
     * The monitor decides if am accepting state is reachable based on its current internal state. This allows efficient
     * parametric monitors to remove the monitor instance in case it should be impossible to reach any accepting state.
     * 
     * @return <code>true</code> if an accepting state is still reachable
     */
    public boolean isAcceptingStateReachable();

    /**
     * Creates a deep copy of this base monitor.
     */
    public Monitor copy();

    public Monitor copy(Binding[] bindings);

    /**
     * Creates a copy of this monitor by setting new stored compressed bindings and timestamp.
     * 
     * @param compressedBindings
     * @param timestamp
     * @return a new monitor with given bindings and timestamp
     */
    public Monitor copy(Binding[] compressedBindings, long timestamp);

    public ParameterNode getParameterNode();

    public void setParameterNode(ParameterNode parameterNode);

    public void setCompressedBindings(Binding[] bindings);

    public void setTimestamp(long timestamp);

    /**
     * This monitor was not created a accepting state. It prevent other monitors from being created.
     * 
     * @return <code>true</code> if monitor is dead
     */
    public boolean isDead();

}
