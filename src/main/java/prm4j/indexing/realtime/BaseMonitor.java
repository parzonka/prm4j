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
package prm4j.indexing.realtime;

import prm4j.api.Event;
import prm4j.indexing.Monitor;
import prm4j.indexing.staticdata.MetaNode;

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

/**
 * Abstract base class for a concrete monitor instance, representing the internal state of a {@link ParametricMonitor}
 * for one single concrete variable binding.
 */
public abstract class BaseMonitor implements Monitor {

    private final static LowLevelBinding[] EMPTY_BINDINGS = new LowLevelBinding[0];

    /**
     * Sum of derived and underived monitors.
     */
    private static long createdMonitorsCount = 0L;

    /**
     * Sum of all update events which got processed by all monitors.
     */
    private static long updatedMonitorsCount = 0L;

    /**
     * Created by deriving from the max monitor or during a join
     */
    private static long derivedMonitorsCount = 0L;

    private MetaNode metaNode;
    // low level access
    private LowLevelBinding[] compressedBindings;
    // low level access
    private long timestamp;

    public BaseMonitor() {
	createdMonitorsCount++;
	compressedBindings = EMPTY_BINDINGS;
	timestamp = 0L;
    }

    /**
     * Creates a low level deep copy of this monitor.
     * 
     * @param compressedBindings
     * @return
     */
    @Override
    public final Monitor copy(LowLevelBinding[] compressedBindings) {
	Monitor copy = copy();
	copy.setCompressedBindings(compressedBindings);
	copy.setTimestamp(timestamp);
	return copy;
    }

    @Override
    public final Monitor copy(LowLevelBinding[] compressedBindings, long timestamp) {
	Monitor copy = copy();
	copy.setCompressedBindings(compressedBindings);
	copy.setTimestamp(timestamp);
	return copy;
    }

    @Override
    public final LowLevelBinding[] getCompressedBindings() {
	return compressedBindings;
    }

    @Override
    public final prm4j.api.Binding[] getUncompressedBindings() {
	if (metaNode == null) {
	    // upcast
	    return compressedBindings;
	}
	// upcast
	return metaNode.uncompressBindings(compressedBindings);
    }

    /**
     * Ends the life-span of this monitor. A terminated monitor can be removed from all data structures it is referenced
     * by.
     */
    @Override
    public final void terminate() {
	compressedBindings = null;
    }

    /**
     * Returns <code>true</code>, if the life of this monitor is over.
     * 
     * @return <code>true</code> if terminated
     */
    @Override
    public final boolean isTerminated() {
	return compressedBindings == null;
    }

    @Override
    public final long getTimestamp() {
	return timestamp;
    }

    /**
     * Updates the base monitors internal state by consuming an event. After processing the event, the monitor is either
     * alive or dead. A dead monitor will no longer process events.
     * 
     * @return <code>true</code> if a the monitor is still alive
     */
    @Override
    public final boolean process(Event event) {
	updatedMonitorsCount++;
	return processEvent(event);
    }

    /**
     * Updates the base monitors internal state by consuming an event. After processing the event, the monitor is either
     * alive or dead. A dead monitor will no longer process events.
     * 
     * @return <code>true</code> if a the monitor is still alive
     */
    @Override
    public abstract boolean processEvent(Event event);

    /**
     * The monitor decides if am accepting state is reachable based on its current internal state. This allows efficient
     * parametric monitors to remove the monitor instance in case it should be impossible to reach any accepting state.
     * 
     * @return <code>true</code> if an accepting state is still reachable
     */
    @Override
    public abstract boolean isAcceptingStateReachable();

    /**
     * Creates a deep copy of this base monitor.
     */
    @Override
    public abstract Monitor copy();

    @Override
    public MetaNode getMetaNode() {
	return metaNode;
    }

    @Override
    public void setMetaNode(MetaNode metaNode) {
	this.metaNode = metaNode;
    }

    @Override
    public void setCompressedBindings(LowLevelBinding[] compressedBindings) {
	this.compressedBindings = compressedBindings;

    }

    @Override
    public void setTimestamp(long timestamp) {
	this.timestamp = timestamp;
    }

    /**
     * DIAGNOSTIC
     * 
     * @return the number of created monitors
     */
    public static long getCreatedMonitorsCount() {
	return createdMonitorsCount;
    }

    /**
     * DIAGNOSTIC
     * 
     * @return the number of updated monitors
     */
    public static long getUpdateddMonitorsCount() {
	return updatedMonitorsCount;
    }

    /**
     * DIAGNOSTIC
     * 
     * @return the number of derived monitors
     */
    public static long getDerivedMonitorsCount() {
	return derivedMonitorsCount;
    }

    /**
     * DIAGNOSTIC Resets internal created monitors and updated monitors counter.
     */
    public static void reset() {
	createdMonitorsCount = 0L;
	updatedMonitorsCount = 0L;
	derivedMonitorsCount = 0L;
    }

}
