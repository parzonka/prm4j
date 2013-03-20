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
package prm4j.spec.finite;

import prm4j.api.BaseEvent;
import prm4j.api.MatchHandler;
import prm4j.indexing.monitor.MonitorState;

public class MonitorStateDecorator implements MonitorState {

    protected final MonitorState monitorState;

    public MonitorStateDecorator(MonitorState monitorState) {
	this.monitorState = monitorState;
    }

    @Override
    public MonitorState getSuccessor(BaseEvent baseEvent) {
	return monitorState.getSuccessor(baseEvent);
    }

    @Override
    public boolean isAccepting() {
	return monitorState.isAccepting();
    }

    @Override
    public MatchHandler getMatchHandler() {
	return monitorState.getMatchHandler();
    }

    @Override
    public boolean isFiniteStateSpace() {
	return monitorState.isFiniteStateSpace();
    }

    @Override
    public boolean isFinal() {
	return monitorState.isFinal();
    }

    @Override
    public int getIndex() {
	return monitorState.getIndex();
    }

    @Override
    public boolean equals(Object obj) {
	return monitorState.equals(obj);
    }

    @Override
    public int hashCode() {
	return monitorState.hashCode();
    }

    @Override
    public boolean isInitial() {
	return monitorState.isInitial();
    }

    @Override
    public String toString() {
	return monitorState.toString();
    }

}
