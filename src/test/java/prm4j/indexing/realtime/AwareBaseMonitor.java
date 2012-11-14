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
package prm4j.indexing.realtime;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import prm4j.Util;
import prm4j.api.BaseEvent;
import prm4j.api.Event;
import prm4j.indexing.BaseMonitor;

/**
 * {@link BaseMonitor} which is aware of all bindings of the trace it processed, and of all monitors which got updated
 * and created.
 */
public class AwareBaseMonitor extends BaseMonitor {

    private final List<BaseEvent> baseEventTrace;
    private final Deque<AwareBaseMonitor> updatedMonitors;
    private final List<AwareBaseMonitor> createdMonitors;

    public AwareBaseMonitor() {
	super();
	baseEventTrace = new ArrayList<BaseEvent>();
	updatedMonitors = new ArrayDeque<AwareBaseMonitor>();
	createdMonitors = new ArrayList<AwareBaseMonitor>();
	createdMonitors.add(this);
    }

    public AwareBaseMonitor(List<BaseEvent> baseEventTrace, Deque<AwareBaseMonitor> updatedMonitors,
	    List<AwareBaseMonitor> createdMonitors) {
	super();
	this.baseEventTrace = baseEventTrace;
	this.updatedMonitors = updatedMonitors;
	this.createdMonitors = createdMonitors;
	this.createdMonitors.add(this);
    }

    @Override
    public boolean processEvent(Event event) {
	updatedMonitors.add(this);
	baseEventTrace.add(event.getBaseEvent());
	return true;
    }

    @Override
    public boolean isAcceptingStateReachable() {
	return true;
    }

    @Override
    public BaseMonitor copy() {
	AwareBaseMonitor copy = new AwareBaseMonitor(new ArrayList<BaseEvent>(baseEventTrace), updatedMonitors,
		createdMonitors);
	return copy;
    }

    /**
     * Returns the trace of base events for this monitor.
     *
     * @return trace
     */
    public List<BaseEvent> getBaseEventTrace() {
	return baseEventTrace;
    }

    /**
     * Returns the list of monitors, for which 'processEvent' was called.
     *
     * @return updated monitors
     */
    public Deque<AwareBaseMonitor> getUpdatedMonitors() {
	return updatedMonitors;
    }

    /**
     * Returns the list of monitors, which where created to monitor a specific instance.
     *
     * @return created monitors
     */
    public List<AwareBaseMonitor> getCreatedMonitors() {
	return createdMonitors;
    }

    @Override
    public String toString() {
	return Util.bindingsToString(getBindings());
    }

}
