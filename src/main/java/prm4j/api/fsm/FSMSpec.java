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
package prm4j.api.fsm;

import static prm4j.Util.covariantUnmodifiableSet;

import java.util.Set;

import prm4j.api.BaseEvent;
import prm4j.api.Parameter;
import prm4j.indexing.BaseMonitor;
import prm4j.indexing.BaseMonitorState;
import prm4j.indexing.StatefulMonitor;
import prm4j.spec.FiniteSpec;

public class FSMSpec implements FiniteSpec {

    private final Set<BaseEvent> baseEvents;
    private final Set<Parameter<?>> parameters;
    private final Set<BaseMonitorState> states;
    private final BaseMonitorState initialState;

    public FSMSpec(FSM fsm) {
	baseEvents = covariantUnmodifiableSet(fsm.getAlphabet().getSymbols());
	parameters = covariantUnmodifiableSet(fsm.getAlphabet().getParameters());
	states = covariantUnmodifiableSet(fsm.getStates());
	initialState = fsm.getInitialState();
    }

    @Override
    public Set<BaseEvent> getBaseEvents() {
	return baseEvents;
    }

    @Override
    public Set<BaseMonitorState> getStates() {
	return states;
    }

    @Override
    public BaseMonitorState getInitialState() {
	return initialState;
    }

    @Override
    public BaseMonitor getInitialMonitor() {
	return new StatefulMonitor(getInitialState());
    }

    @Override
    public Set<Parameter<?>> getFullParameterSet() {
	return parameters;
    }

}
