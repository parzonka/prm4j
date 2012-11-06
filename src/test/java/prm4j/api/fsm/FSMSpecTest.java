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

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import prm4j.api.BaseEvent;
import prm4j.api.Parameter;
import prm4j.indexing.BaseMonitor;
import prm4j.indexing.BaseMonitorState;
import prm4j.indexing.StatefulMonitor;
import prm4j.util.FSMDefinitions.FSM_unsafeMapIterator;

public class FSMSpecTest {

    @Test
    public void getBaseEvents_unsafeMapIterator() {
	FSM_unsafeMapIterator u = new FSM_unsafeMapIterator();
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
	FSM_unsafeMapIterator u = new FSM_unsafeMapIterator();
	FSM fsm = u.fsm;
	FSMSpec fs = new FSMSpec(fsm);

	Set<Parameter<?>> actual = fs.getParameters();

	Set<Parameter<?>> expected = new HashSet<Parameter<?>>();
	expected.add(u.c);
	expected.add(u.m);
	expected.add(u.i);

	assertEquals(expected, actual);
    }

    @Test
    public void getStates_unsafeMapIterator() {
	FSM_unsafeMapIterator u = new FSM_unsafeMapIterator();
	FSM fsm = u.fsm;
	FSMSpec fs = new FSMSpec(fsm);

	Set<BaseMonitorState> actual = fs.getStates();

	Set<BaseMonitorState> expected = new HashSet<BaseMonitorState>();
	expected.add(u.initial);
	expected.add(u.s1);
	expected.add(u.s2);
	expected.add(u.s3);
	expected.add(u.error);

	assertEquals(expected, actual);
    }

    @Test
    public void getInitialState_unsafeMapIterator() {
	FSM_unsafeMapIterator u = new FSM_unsafeMapIterator();
	FSM fsm = u.fsm;
	FSMSpec fs = new FSMSpec(fsm);

	BaseMonitorState actual = fs.getInitialState();

	BaseMonitorState expected = u.initial;

	assertEquals(expected, actual);
    }

    @Test
    public void getInitialMonitor_unsafeMapIterator() {
	FSM_unsafeMapIterator u = new FSM_unsafeMapIterator();
	FSM fsm = u.fsm;
	FSMSpec fs = new FSMSpec(fsm);

	BaseMonitor actual = fs.getInitialMonitor();

	BaseMonitor expected = new StatefulMonitor(u.initial);

	assertEquals(expected, actual);
    }

}
