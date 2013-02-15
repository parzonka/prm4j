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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import prm4j.Util;
import prm4j.api.BaseEvent;
import prm4j.api.Event;
import prm4j.indexing.Monitor;
import prm4j.indexing.BaseMonitorState;

/**
 * {@link Monitor} which is aware of all bindings of the trace it processed, and of all monitors which got updated
 * and created.
 */
public class AwareBaseMonitor extends StatefulMonitor {

    private final List<BaseEvent> baseEventTrace;
    private final Deque<AwareBaseMonitor> updatedMonitors;
    private final Deque<AwareBaseMonitor> createdMonitors;

    public AwareBaseMonitor(BaseMonitorState state) {
	super(state);
	baseEventTrace = new ArrayList<BaseEvent>();
	updatedMonitors = new ArrayDeque<AwareBaseMonitor>();
	createdMonitors = new ArrayDeque<AwareBaseMonitor>();
    }

    public AwareBaseMonitor(BaseMonitorState state, List<BaseEvent> baseEventTrace, Deque<AwareBaseMonitor> updatedMonitors,
	    Deque<AwareBaseMonitor> createdMonitors) {
	super(state);
	this.baseEventTrace = baseEventTrace;
	this.updatedMonitors = updatedMonitors;
	this.createdMonitors = createdMonitors;
	this.createdMonitors.add(this);
    }

    @Override
    public boolean processEvent(Event event) {
	updatedMonitors.add(this);
	baseEventTrace.add(event.getEvaluatedBaseEvent(this));
	return super.processEvent(event);
    }

    @Override
    public Monitor copy() {
	AwareBaseMonitor copy = new AwareBaseMonitor(state, new ArrayList<BaseEvent>(baseEventTrace), updatedMonitors,
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
    public Deque<AwareBaseMonitor> getCreatedMonitors() {
	return createdMonitors;
    }

    @Override
    public String toString() {
	return Util.bindingsToString(getBindings());
    }

}
