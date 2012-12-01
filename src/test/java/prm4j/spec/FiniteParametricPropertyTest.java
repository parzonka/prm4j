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
import static prm4j.Util.tuple;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import prm4j.AbstractTest;
import prm4j.Util.Tuple;
import prm4j.api.BaseEvent;
import prm4j.api.Parameter;
import prm4j.api.Symbol;
import prm4j.api.fsm.FSM;
import prm4j.api.fsm.FSMSpec;
import prm4j.indexing.BaseMonitorState;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.SetMultimap;

/**
 * Unit tests for {@link FiniteParametricProperty}.
 */
public class FiniteParametricPropertyTest extends AbstractTest {

    @Test
    public void accessors_unsafeMapIterator() throws Exception {
	FSM fsm = new FSM_unsafeMapIterator().fsm;
	FiniteParametricProperty fs = new FiniteParametricProperty(new FSMSpec(fsm));

	assertEquals(fs.getInitialState(), fsm.getInitialState());
	assertEquals(fs.getBaseEvents(), fsm.getAlphabet().getSymbols());
    }

    @Test
    public void getCreationEvents_unsafeMapIterator() throws Exception {
	FSM_unsafeMapIterator u = new FSM_unsafeMapIterator();
	FSM fsm = u.fsm;
	FiniteParametricProperty pp = new FiniteParametricProperty(new FSMSpec(fsm));

	Set<BaseEvent> actual = pp.getCreationEvents();

	Set<BaseEvent> expected = new HashSet<BaseEvent>();
	expected.add(u.createColl);

	assertEquals(expected, actual);
    }

    @Test
    public void getDisablingEvents_unsafeMapIterator() throws Exception {
	FSM_unsafeMapIterator u = new FSM_unsafeMapIterator();
	FSM fsm = u.fsm;
	FiniteParametricProperty pp = new FiniteParametricProperty(new FSMSpec(fsm));

	Set<BaseEvent> actual = pp.getDisablingEvents();

	Set<BaseEvent> expected = new HashSet<BaseEvent>();
	expected.add(u.createIter);
	expected.add(u.useIter);

	assertEquals(expected, actual);
    }

    @Test
    public void getEnablingEventSets_unsafeMapIterator() throws Exception {
	FSM_unsafeMapIterator u = new FSM_unsafeMapIterator();
	FSM fsm = u.fsm;
	FiniteParametricProperty fs = new FiniteParametricProperty(new FSMSpec(fsm));

	SetMultimap<BaseEvent, Set<BaseEvent>> actual = fs.getEnablingEventSets();

	SetMultimap<Symbol, Set<Symbol>> expected = HashMultimap.create();

	expected.put(u.createColl, Collections.<Symbol> emptySet());
	expected.put(u.createIter, asSet(u.createColl));
	// expected.put(u.createIter, asSet(u.createColl, u.updateMap)); // omitted by filtering loops on states
	expected.put(u.useIter, asSet(u.createColl, u.createIter));
	expected.put(u.useIter, asSet(u.createColl, u.createIter, u.updateMap));
	expected.put(u.updateMap, Collections.<Symbol> emptySet());
	expected.put(u.updateMap, asSet(u.createColl));
	expected.put(u.updateMap, asSet(u.createColl, u.createIter));
	// expected.put(u.updateMap, asSet(u.createColl, u.createIter, u.useIter)); // omitted by filtering loops on
	// states

	assertEquals(expected, actual);
    }

    @Test
    public void getEnablingParameterSets_unsafeMapIterator() throws Exception {
	FSM_unsafeMapIterator u = new FSM_unsafeMapIterator();
	FSM fsm = u.fsm;
	FiniteParametricProperty fs = new FiniteParametricProperty(new FSMSpec(fsm));

	SetMultimap<BaseEvent, Set<Parameter<?>>> actual = fs.getEnablingParameterSets();

	SetMultimap<BaseEvent, Set<Parameter<?>>> expected = HashMultimap.create();

	expected.put(u.createColl, Collections.<Parameter<?>> emptySet());
	expected.put(u.createIter, asSet(u.m, u.c));
	expected.put(u.useIter, asSet(u.m, u.c, u.i));
	expected.put(u.updateMap, EMPTY_PARAMETER_SET);
	expected.put(u.updateMap, asSet(u.m, u.c));
	expected.put(u.updateMap, asSet(u.m, u.c, u.i));

	assertEquals(expected, actual);
    }

    @Test
    public void getMaxData_unsafeMapIterator() throws Exception {
	FSM_unsafeMapIterator u = new FSM_unsafeMapIterator();
	FSM fsm = u.fsm;
	FiniteParametricProperty fs = new FiniteParametricProperty(new FSMSpec(fsm));

	ListMultimap<BaseEvent, Set<Parameter<?>>> actual = fs.getMaxData();

	ListMultimap<BaseEvent, Set<Parameter<?>>> expected = ArrayListMultimap.create();

	assertEquals(expected, actual);
    }

    @Test
    public void getJoinData_unsafeMapIterator() throws Exception {
	FSM_unsafeMapIterator u = new FSM_unsafeMapIterator();
	FSM fsm = u.fsm;
	FiniteParametricProperty fs = new FiniteParametricProperty(new FSMSpec(fsm));

	ListMultimap<BaseEvent, Tuple<Set<Parameter<?>>, Set<Parameter<?>>>> actual = fs.getJoinData();

	ListMultimap<BaseEvent, Tuple<Set<Parameter<?>>, Set<Parameter<?>>>> expected = ArrayListMultimap.create();
	expected.put(u.createIter, tuple(asSet(u.c), asSet(u.m, u.c)));

	assertEquals(expected, actual);
    }

    @Test
    public void getChainData_unsafeMapIterator() throws Exception {
	FSM_unsafeMapIterator u = new FSM_unsafeMapIterator();
	FSM fsm = u.fsm;
	FiniteParametricProperty fs = new FiniteParametricProperty(new FSMSpec(fsm));

	SetMultimap<Set<Parameter<?>>, Tuple<Set<Parameter<?>>, Set<Parameter<?>>>> actual = fs.getChainData();

	SetMultimap<Set<Parameter<?>>, Tuple<Set<Parameter<?>>, Set<Parameter<?>>>> expected = HashMultimap.create();
	// optimized away by recognizing non-state changing transitions:
	// expected.put(asSet(u.m, u.c), tuple(asSet(u.m), EMPTY_PARAMETER_SET)); // m -> mc (update)
	// necessary for joins:
	expected.put(asSet(u.m, u.c), tuple(asSet(u.c), asSet(u.m, u.c))); // c -> mc (join)
	// could be optimized away from a self-loop spec
	expected.put(asSet(u.m, u.c, u.i), tuple(asSet(u.c, u.i), EMPTY_PARAMETER_SET)); // ci -> mci (update)
	// could be optimized away from a self-loop spec, because mc is never in maxData(..., ci) or joinData(..., ci)
	expected.put(asSet(u.m, u.c, u.i), tuple(asSet(u.c, u.m), EMPTY_PARAMETER_SET)); // mc -> mci (update)
	// necessary for updates
	expected.put(asSet(u.m, u.c, u.i), tuple(asSet(u.m), EMPTY_PARAMETER_SET)); // m -> mci (update)
	// necessary for updates
	expected.put(asSet(u.m, u.c, u.i), tuple(asSet(u.i), EMPTY_PARAMETER_SET)); // i -> mci (update)

	assertEquals(expected, actual);
    }

    @Test
    public void getMonitorSetData_unsafeMapIterator() throws Exception {
	FSM_unsafeMapIterator u = new FSM_unsafeMapIterator();
	FSM fsm = u.fsm;
	FiniteParametricProperty fs = new FiniteParametricProperty(new FSMSpec(fsm));

	SetMultimap<Set<Parameter<?>>, Tuple<Set<Parameter<?>>, Boolean>> actual = fs.getMonitorSetData();

	SetMultimap<Set<Parameter<?>>, Tuple<Set<Parameter<?>>, Boolean>> expected = HashMultimap.create();
	expected.put(asSet(u.c, u.i), tuple(EMPTY_PARAMETER_SET, true)); // ci has single update set
	expected.put(asSet(u.c), tuple(asSet(u.c, u.m), false)); // c has single join set
	expected.put(asSet(u.c, u.m), tuple(EMPTY_PARAMETER_SET, true)); // cm has single update set
	expected.put(asSet(u.m), tuple(EMPTY_PARAMETER_SET, true)); // m has single update set
	expected.put(asSet(u.i), tuple(EMPTY_PARAMETER_SET, true)); // i has single update set

	assertEquals(expected, actual);
    }

    @Test
    public void getPossibleParameterSets_unsafeMapIterator() throws Exception {
	FSM_unsafeMapIterator u = new FSM_unsafeMapIterator();
	FSM fsm = u.fsm;
	FiniteParametricProperty fs = new FiniteParametricProperty(new FSMSpec(fsm));

	Set<Set<Parameter<?>>> actual = fs.getPossibleParameterSets();

	Set<Set<Parameter<?>>> expected = new HashSet<Set<Parameter<?>>>();
	expected.add(EMPTY_PARAMETER_SET);
	expected.add(asSet(u.m, u.c));
	expected.add(asSet(u.m, u.c, u.i));

	assertEquals(expected, actual);
    }

    @Test
    public void getParameters_unsafeMapIterator() throws Exception {
	FSM_unsafeMapIterator u = new FSM_unsafeMapIterator();
	FSM fsm = u.fsm;
	FiniteParametricProperty fs = new FiniteParametricProperty(new FSMSpec(fsm));

	Set<Parameter<?>> actual = fs.getParameters();

	Set<Parameter<?>> expected = new HashSet<Parameter<?>>();

	expected.add(u.c);
	expected.add(u.m);
	expected.add(u.i);

	assertEquals(expected, actual);
    }

    // TODO @Test
    public void getStatePropertyCoEnableSets_unsafeMapIterator() throws Exception {
	FSM_unsafeMapIterator u = new FSM_unsafeMapIterator();
	FSM fsm = u.fsm;
	FiniteParametricProperty fs = new FiniteParametricProperty(new FSMSpec(fsm));

	SetMultimap<BaseMonitorState, Set<BaseEvent>> actual = fs.getStatePropertyCoEnableSets();

	Map<BaseMonitorState, Set<Set<Symbol>>> expected = new HashMap<BaseMonitorState, Set<Set<Symbol>>>();
	for (BaseMonitorState state : u.fsm.getStates()) {
	    expected.put(state, new HashSet<Set<Symbol>>());
	}
	expected.get(u.initial).add(asSet(u.createColl, u.createIter, u.updateMap, u.useIter));
	expected.get(u.s1).add(asSet(u.createIter, u.useIter, u.updateMap));
	expected.get(u.s2).add(asSet(u.useIter, u.updateMap));
	expected.get(u.s3).add(asSet(u.useIter));
	expected.get(u.error).add(Collections.<Symbol> emptySet());

	// TODO failing test: implement functionality
	assertEquals(expected, actual);
    }

    @Test
    public void getChainData_FSM_a_a_a() throws Exception {
	FSM_a_a_a fsm = new FSM_a_a_a();
	FiniteParametricProperty fs = new FiniteParametricProperty(new FSMSpec(fsm.fsm));

	SetMultimap<Set<Parameter<?>>, Tuple<Set<Parameter<?>>, Set<Parameter<?>>>> actual = fs.getChainData();
	SetMultimap<Set<Parameter<?>>, Tuple<Set<Parameter<?>>, Set<Parameter<?>>>> expected = HashMultimap.create();

	assertEquals(expected, actual);
    }

    @Test
    public void getEnablingEventSets_FSM_ab_b_with_initial_b_loop() throws Exception {
	FSM_ab_b_with_initial_b_loop fsm = new FSM_ab_b_with_initial_b_loop();
	FiniteParametricProperty fs = new FiniteParametricProperty(new FSMSpec(fsm.fsm));

	SetMultimap<BaseEvent, Set<BaseEvent>> actual = fs.getEnablingEventSets();
	SetMultimap<BaseEvent, Set<BaseEvent>> expected = HashMultimap.create();

	expected.put(fsm.e1, Collections.<BaseEvent> emptySet());
	expected.put(fsm.e2, Collections.<BaseEvent> emptySet());
	expected.put(fsm.e2, asSet((BaseEvent) fsm.e1));

	assertEquals(expected, actual);
    }

    @Test
    public void getMonitorSetData_FSM_ab_b_with_initial_b_loop() throws Exception {
	FSM_ab_b_with_initial_b_loop fsm = new FSM_ab_b_with_initial_b_loop();
	FiniteParametricProperty fs = new FiniteParametricProperty(new FSMSpec(fsm.fsm));

	SetMultimap<Set<Parameter<?>>, Tuple<Set<Parameter<?>>, Boolean>> actual = fs.getMonitorSetData();
	SetMultimap<Set<Parameter<?>>, Tuple<Set<Parameter<?>>, Boolean>> expected = HashMultimap.create();

	// compatible parameter set, (selected parameter set, sends updates)
	expected.put(asSet(fsm.p2), tuple(EMPTY_PARAMETER_SET, true));

	assertEquals(expected, actual);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void getPossibleParameterSets_FSM_ab_b_with_initial_b_loop() throws Exception {
	FSM_ab_b_with_initial_b_loop fsm = new FSM_ab_b_with_initial_b_loop();
	FiniteParametricProperty fs = new FiniteParametricProperty(new FSMSpec(fsm.fsm));

	Set<Set<Parameter<?>>> actual = fs.getPossibleParameterSets();

	Set<Set<Parameter<?>>> expected = asSet(EMPTY_PARAMETER_SET, asSet(fsm.p1, fsm.p2));

	assertEquals(expected, actual);
    }
}
