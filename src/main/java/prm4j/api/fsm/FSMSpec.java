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

import java.util.Collections;
import java.util.Set;

import prm4j.indexing.BaseEvent;
import prm4j.logic.FiniteSpec;
import prm4j.logic.MonitorState;

public class FSMSpec implements FiniteSpec {

    private final Set<? extends BaseEvent> baseEvents;
    private final Set<? extends MonitorState> states;
    private final MonitorState initialState;

    public FSMSpec(FSM fsm) {
	baseEvents = Collections.unmodifiableSet(fsm.getAlphabet().getSymbols());
	states = Collections.unmodifiableSet(fsm.getStates());
	initialState = fsm.getInitialState();
    }

    @Override
    public Set<? extends BaseEvent> getBaseEvents() {
	return baseEvents;
    }

    @Override
    public Set<? extends MonitorState> getStates() {
	return states;
    }

    @Override
    public MonitorState getInitialState() {
	return initialState;
    }

}
