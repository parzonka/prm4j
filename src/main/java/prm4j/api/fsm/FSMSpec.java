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
package prm4j.api.fsm;

import static java.util.Collections.unmodifiableSet;

import java.util.HashSet;
import java.util.Set;

import prm4j.api.BaseEvent;
import prm4j.api.Parameter;
import prm4j.indexing.monitor.MonitorState;
import prm4j.indexing.monitor.Monitor;
import prm4j.indexing.monitor.StatefulMonitor;
import prm4j.spec.FiniteSpec;

public class FSMSpec implements FiniteSpec {

    private final Set<BaseEvent> baseEvents;
    private final Set<Parameter<?>> parameters;
    private final Set<MonitorState> states;
    private final MonitorState initialState;

    public FSMSpec(FSM fsm) {
	baseEvents = unmodifiableSet(new HashSet<BaseEvent>(fsm.getAlphabet().getSymbols()));
	parameters = unmodifiableSet(fsm.getAlphabet().getParameters());
	states = unmodifiableSet(new HashSet<MonitorState>(fsm.getStates()));
	initialState = fsm.getInitialState();
    }

    @Override
    public Set<BaseEvent> getBaseEvents() {
	return baseEvents;
    }

    @Override
    public Set<MonitorState> getStates() {
	return states;
    }

    @Override
    public MonitorState getInitialState() {
	return initialState;
    }

    @Override
    public Monitor getMonitorPrototype() {
	return new StatefulMonitor(getInitialState());
    }

    @Override
    public Set<Parameter<?>> getFullParameterSet() {
	return parameters;
    }

}
