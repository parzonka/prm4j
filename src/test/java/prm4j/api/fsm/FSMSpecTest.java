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

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import prm4j.api.BaseEvent;
import prm4j.api.Parameter;
import prm4j.indexing.monitor.Monitor;
import prm4j.indexing.monitor.MonitorState;
import prm4j.indexing.monitor.StatefulMonitor;
import prm4j.util.FSMDefinitions.FSM_SafeMapIterator;

public class FSMSpecTest {

    @Test
    public void getBaseEvents_unsafeMapIterator() {
	FSM_SafeMapIterator u = new FSM_SafeMapIterator();
	FSM fsm = u.fsm;
	FSMSpec fs = new FSMSpec(fsm);

	Set<BaseEvent> actual = fs.getBaseEvents();

	Set<BaseEvent> expected = new HashSet<BaseEvent>();
	expected.add(u.createColl);
	expected.add(u.createIter);
	expected.add(u.updateMap);
	expected.add(u.useIter);

	assertEquals(expected, actual);
    }

    @Test
    public void getParameters_unsafeMapIterator() {
	FSM_SafeMapIterator u = new FSM_SafeMapIterator();
	FSM fsm = u.fsm;
	FSMSpec fs = new FSMSpec(fsm);

	Set<Parameter<?>> actual = fs.getFullParameterSet();

	Set<Parameter<?>> expected = new HashSet<Parameter<?>>();
	expected.add(u.c);
	expected.add(u.m);
	expected.add(u.i);

	assertEquals(expected, actual);
    }

    @Test
    public void getStates_unsafeMapIterator() {
	FSM_SafeMapIterator u = new FSM_SafeMapIterator();
	FSM fsm = u.fsm;
	FSMSpec fs = new FSMSpec(fsm);

	Set<MonitorState> actual = fs.getStates();

	Set<MonitorState> expected = new HashSet<MonitorState>();
	expected.add(u.initial);
	expected.add(u.s1);
	expected.add(u.s2);
	expected.add(u.s3);
	expected.add(u.error);

	assertEquals(expected, actual);
    }

    @Test
    public void getInitialState_unsafeMapIterator() {
	FSM_SafeMapIterator u = new FSM_SafeMapIterator();
	FSM fsm = u.fsm;
	FSMSpec fs = new FSMSpec(fsm);

	MonitorState actual = fs.getInitialState();

	MonitorState expected = u.initial;

	assertEquals(expected, actual);
    }

    @Test
    public void getInitialMonitor_unsafeMapIterator() {
	FSM_SafeMapIterator u = new FSM_SafeMapIterator();
	FSM fsm = u.fsm;
	FSMSpec fs = new FSMSpec(fsm);

	Monitor actual = fs.getInitialMonitor();

	Monitor expected = new StatefulMonitor(u.initial);

	assertEquals(expected, actual);
    }

}
