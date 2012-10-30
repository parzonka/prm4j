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

import prm4j.indexing.AbstractBaseMonitor;
import prm4j.indexing.BaseEvent;
import prm4j.logic.FiniteSpec;
import prm4j.logic.MonitorState;
import prm4j.logic.StatefulMonitor;

public class FSMSpec implements FiniteSpec {

    private final Set<BaseEvent> baseEvents;
    private final Set<MonitorState> states;
    private final MonitorState initialState;

    public FSMSpec(FSM fsm) {
	baseEvents = covariantUnmodifiableSet(fsm.getAlphabet().getSymbols());
	states = covariantUnmodifiableSet(fsm.getStates());
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
    public AbstractBaseMonitor getInitialMonitor() {
	return new StatefulMonitor(getInitialState());
    }

}
