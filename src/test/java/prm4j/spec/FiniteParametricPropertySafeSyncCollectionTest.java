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
package prm4j.spec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static prm4j.Util.tuple;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import prm4j.AbstractTest;
import prm4j.Util.Tuple;
import prm4j.api.BaseEvent;
import prm4j.api.Parameter;
import prm4j.api.fsm.FSMSpec;
import prm4j.indexing.BaseMonitorState;
import prm4j.indexing.staticdata.MetaNode;
import prm4j.indexing.staticdata.StaticDataConverter;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.SetMultimap;

public class FiniteParametricPropertySafeSyncCollectionTest extends AbstractTest {

    FSM_SafeSyncCollection fsm;
    FiniteParametricProperty fpp;

    @Before
    public void init() {
	fsm = new FSM_SafeSyncCollection();
	fpp = new FiniteParametricProperty(new FSMSpec(fsm.fsm));
    }

    @Test
    public void getCreationEvents() throws Exception {
	Set<BaseEvent> actual = fpp.getCreationEvents();
	// verify
	Set<BaseEvent> expected = new HashSet<BaseEvent>();
	expected.add(fsm.sync);
	assertEquals(expected, actual);
    }

    @Test
    public void getMonitorSetData() throws Exception {
	SetMultimap<Set<Parameter<?>>, Tuple<Set<Parameter<?>>, Boolean>> actual = fpp.getMonitorSetData();
	// verify
	SetMultimap<Set<Parameter<?>>, Tuple<Set<Parameter<?>>, Boolean>> expected = HashMultimap.create();
	expected.put(asSet(fsm.c), tuple(EMPTY_PARAMETER_SET, true));
	expected.put(asSet(fsm.i), tuple(EMPTY_PARAMETER_SET, true));
	assertEquals(expected, actual);

    }

    @Test
    public void getChainData() throws Exception {
	SetMultimap<Set<Parameter<?>>, Tuple<Set<Parameter<?>>, Set<Parameter<?>>>> actual = fpp.getChainData();
	// verify
	SetMultimap<Set<Parameter<?>>, Tuple<Set<Parameter<?>>, Set<Parameter<?>>>> expected = HashMultimap.create();
	expected.put(asSet(fsm.c, fsm.i), tuple(asSet(fsm.c), EMPTY_PARAMETER_SET));
	expected.put(asSet(fsm.c, fsm.i), tuple(asSet(fsm.i), EMPTY_PARAMETER_SET));
	assertEquals(expected, actual);
    }

    @Test
    public void getJoinData() throws Exception {
	ListMultimap<BaseEvent, Tuple<Set<Parameter<?>>, Set<Parameter<?>>>> actual = fpp.getJoinData();
	// verify
	ListMultimap<BaseEvent, Tuple<Set<Parameter<?>>, Set<Parameter<?>>>> expected = ArrayListMultimap.create();
	assertEquals(expected, actual);
    }

    @Test
    public void getAliveParameterSets() throws Exception {
	SetMultimap<BaseMonitorState, Set<Parameter<?>>> actual = fpp.getAliveParameterSets();
	// verify
	SetMultimap<BaseMonitorState, Set<Parameter<?>>> expected = HashMultimap.create();

	expected.put(fsm.initial, asSet(fsm.c, fsm.i));
	expected.put(fsm.s1, asSet(fsm.c, fsm.i));
	expected.put(fsm.s2, asSet(fsm.i));
	// fsm.error is not defined, it is a dead state

	assertEquals(expected, actual);
    }

    @Test
    public void getAliveParameterMasks() throws Exception {
	StaticDataConverter sdc = new StaticDataConverter(fpp);
	boolean[][][] state2ParameterMasks = sdc.getMetaTree().getAliveParameterMasks();

	assertEquals(4, state2ParameterMasks.length);

	// verify that we have the correct number of parameterMasks
	assertEquals(1, state2ParameterMasks[fsm.initial.getIndex()].length);
	assertEquals(1, state2ParameterMasks[fsm.s1.getIndex()].length);
	assertEquals(1, state2ParameterMasks[fsm.s2.getIndex()].length);
	assertNull(state2ParameterMasks[fsm.error.getIndex()]);
	MetaNode c = sdc.getMetaTree().getMetaNode(fsm.c);
	MetaNode i = sdc.getMetaTree().getMetaNode(fsm.i);
	MetaNode ci = sdc.getMetaTree().getMetaNode(fsm.c, fsm.i);

	// verify parameterMasks
	assertBooleanArrayEquals(array(true), c.getAliveParameterMasks()[fsm.initial.getIndex()][0]);
	assertBooleanArrayEquals(array(true), c.getAliveParameterMasks()[fsm.s1.getIndex()][0]);
	assertBooleanArrayEquals(array(false), c.getAliveParameterMasks()[fsm.s2.getIndex()][0]);

	// verify parameterMasks
	assertBooleanArrayEquals(array(true), i.getAliveParameterMasks()[fsm.initial.getIndex()][0]);
	assertBooleanArrayEquals(array(true), i.getAliveParameterMasks()[fsm.s1.getIndex()][0]);
	assertBooleanArrayEquals(array(true), i.getAliveParameterMasks()[fsm.s2.getIndex()][0]);

	assertBooleanArrayEquals(array(true, true), ci.getAliveParameterMasks()[fsm.initial.getIndex()][0]);
	assertBooleanArrayEquals(array(true, true), ci.getAliveParameterMasks()[fsm.s1.getIndex()][0]);
	assertBooleanArrayEquals(array(false, true), ci.getAliveParameterMasks()[fsm.s2.getIndex()][0]);
    }


}
