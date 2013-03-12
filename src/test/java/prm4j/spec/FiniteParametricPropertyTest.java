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
package prm4j.spec;

import static org.junit.Assert.assertEquals;
import static prm4j.Util.tuple;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import prm4j.AbstractTest;
import prm4j.Util.Tuple;
import prm4j.api.BaseEvent;
import prm4j.api.Parameter;
import prm4j.api.Symbol;
import prm4j.api.fsm.FSM;
import prm4j.api.fsm.FSMSpec;

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
	FSM fsm = new FSM_SafeMapIterator().fsm;
	FiniteParametricProperty fs = new FiniteParametricProperty(new FSMSpec(fsm));

	assertEquals(fs.getInitialState(), fsm.getInitialState());
	assertEquals(fs.getBaseEvents(), fsm.getAlphabet().getSymbols());
    }

    @Test
    public void getCreationEvents_unsafeMapIterator() throws Exception {
	FSM_SafeMapIterator fsm = new FSM_SafeMapIterator();
	FiniteParametricProperty pp = new FiniteParametricProperty(new FSMSpec(fsm.fsm));

	Set<BaseEvent> actual = pp.getCreationEvents();

	Set<BaseEvent> expected = new HashSet<BaseEvent>();
	expected.add(fsm.createColl);

	assertEquals(expected, actual);
    }

    @Test
    public void getDisableEvents_unsafeMapIterator() throws Exception {
	FSM_SafeMapIterator fsm = new FSM_SafeMapIterator();
	FiniteParametricProperty pp = new FiniteParametricProperty(new FSMSpec(fsm.fsm));

	Set<BaseEvent> actual = pp.getDisableEvents();

	Set<BaseEvent> expected = new HashSet<BaseEvent>();

	assertEquals(expected, actual);
    }

    @Test
    public void getEnablingEventSets_unsafeMapIterator() throws Exception {
	FSM_SafeMapIterator fsm = new FSM_SafeMapIterator();
	FiniteParametricProperty fs = new FiniteParametricProperty(new FSMSpec(fsm.fsm));

	SetMultimap<BaseEvent, Set<BaseEvent>> actual = fs.getEnablingEventSets();

	SetMultimap<Symbol, Set<Symbol>> expected = HashMultimap.create();

	expected.put(fsm.createColl, Collections.<Symbol> emptySet());

	// expected.put(u.createIter, Collections.<Symbol> emptySet());
	expected.put(fsm.createIter, asSet(fsm.createColl));
	expected.put(fsm.createIter, asSet(fsm.createColl, fsm.updateMap)); // omitted by filtering loops on states
	// expected.put(u.useIter, Collections.<Symbol> emptySet());
	expected.put(fsm.useIter, asSet(fsm.createColl, fsm.createIter));
	expected.put(fsm.useIter, asSet(fsm.createColl, fsm.createIter, fsm.updateMap));

	// expected.put(u.updateMap, Collections.<Symbol> emptySet());
	expected.put(fsm.updateMap, asSet(fsm.createColl));
	expected.put(fsm.updateMap, asSet(fsm.createColl, fsm.createIter));
	expected.put(fsm.updateMap, asSet(fsm.createColl, fsm.createIter, fsm.useIter)); // omitted by filtering loops
											 // on
	// states

	assertEquals(expected, actual);
    }

    @Test
    public void getEnablingParameterSets_unsafeMapIterator() throws Exception {
	FSM_SafeMapIterator fsm = new FSM_SafeMapIterator();
	FiniteParametricProperty fs = new FiniteParametricProperty(new FSMSpec(fsm.fsm));

	SetMultimap<BaseEvent, Set<Parameter<?>>> actual = fs.getEnablingParameterSets();

	SetMultimap<BaseEvent, Set<Parameter<?>>> expected = HashMultimap.create();

	expected.put(fsm.createColl, Collections.<Parameter<?>> emptySet());
	expected.put(fsm.createIter, asSet(fsm.m, fsm.c));
	expected.put(fsm.useIter, asSet(fsm.m, fsm.c, fsm.i));
	expected.put(fsm.updateMap, asSet(fsm.m, fsm.c));
	expected.put(fsm.updateMap, asSet(fsm.m, fsm.c, fsm.i));

	assertEquals(expected, actual);
    }

    @Test
    public void getMaxData_unsafeMapIterator() throws Exception {
	FSM_SafeMapIterator fsm = new FSM_SafeMapIterator();
	FiniteParametricProperty fs = new FiniteParametricProperty(new FSMSpec(fsm.fsm));

	ListMultimap<BaseEvent, Set<Parameter<?>>> actual = fs.getMaxData();

	ListMultimap<BaseEvent, Set<Parameter<?>>> expected = ArrayListMultimap.create();

	assertEquals(expected, actual);
    }

    @Test
    public void getJoinData_unsafeMapIterator() throws Exception {
	FSM_SafeMapIterator fsm = new FSM_SafeMapIterator();
	FiniteParametricProperty fs = new FiniteParametricProperty(new FSMSpec(fsm.fsm));

	ListMultimap<BaseEvent, Tuple<Set<Parameter<?>>, Set<Parameter<?>>>> actual = fs.getJoinData();

	ListMultimap<BaseEvent, Tuple<Set<Parameter<?>>, Set<Parameter<?>>>> expected = ArrayListMultimap.create();
	expected.put(fsm.createIter, tuple(asSet(fsm.c), asSet(fsm.m, fsm.c)));

	assertEquals(expected, actual);
    }

    @Test
    public void getChainData_unsafeMapIterator() throws Exception {
	FSM_SafeMapIterator fsm = new FSM_SafeMapIterator();
	FiniteParametricProperty fs = new FiniteParametricProperty(new FSMSpec(fsm.fsm));

	SetMultimap<Set<Parameter<?>>, Tuple<Set<Parameter<?>>, Set<Parameter<?>>>> actual = fs.getChainData();

	SetMultimap<Set<Parameter<?>>, Tuple<Set<Parameter<?>>, Set<Parameter<?>>>> expected = HashMultimap.create();
	// optimized away by recognizing non-state changing transitions:
	// expected.put(asSet(u.m, u.c), tuple(asSet(u.m), EMPTY_PARAMETER_SET)); // m -> mc (update)
	// necessary for joins:
	expected.put(asSet(fsm.m, fsm.c), tuple(asSet(fsm.c), asSet(fsm.m, fsm.c))); // c -> mc (join)
	// could be optimized away from a self-loop spec
	expected.put(asSet(fsm.m, fsm.c, fsm.i), tuple(asSet(fsm.c, fsm.i), EMPTY_PARAMETER_SET)); // ci -> mci (update)
	// could be optimized away from a self-loop spec, because mc is never in maxData(..., ci) or joinData(..., ci)
	expected.put(asSet(fsm.m, fsm.c, fsm.i), tuple(asSet(fsm.c, fsm.m), EMPTY_PARAMETER_SET)); // mc -> mci (update)
	// necessary for updates
	expected.put(asSet(fsm.m, fsm.c, fsm.i), tuple(asSet(fsm.m), EMPTY_PARAMETER_SET)); // m -> mci (update)
	// necessary for updates
	expected.put(asSet(fsm.m, fsm.c, fsm.i), tuple(asSet(fsm.i), EMPTY_PARAMETER_SET)); // i -> mci (update)

	assertEquals(expected, actual);
    }

    @Test
    public void getMonitorSetData_unsafeMapIterator() throws Exception {
	FSM_SafeMapIterator fsm = new FSM_SafeMapIterator();
	FiniteParametricProperty fs = new FiniteParametricProperty(new FSMSpec(fsm.fsm));

	SetMultimap<Set<Parameter<?>>, Tuple<Set<Parameter<?>>, Boolean>> actual = fs.getMonitorSetData();

	SetMultimap<Set<Parameter<?>>, Tuple<Set<Parameter<?>>, Boolean>> expected = HashMultimap.create();
	expected.put(asSet(fsm.c, fsm.i), tuple(EMPTY_PARAMETER_SET, true)); // ci has single update set
	expected.put(asSet(fsm.c), tuple(asSet(fsm.c, fsm.m), false)); // c has single join set
	expected.put(asSet(fsm.c, fsm.m), tuple(EMPTY_PARAMETER_SET, true)); // cm has single update set
	expected.put(asSet(fsm.m), tuple(EMPTY_PARAMETER_SET, true)); // m has single update set
	expected.put(asSet(fsm.i), tuple(EMPTY_PARAMETER_SET, true)); // i has single update set

	assertEquals(expected, actual);
    }

    @Test
    public void getPossibleParameterSets_unsafeMapIterator() throws Exception {
	FSM_SafeMapIterator fsm = new FSM_SafeMapIterator();
	FiniteParametricProperty fs = new FiniteParametricProperty(new FSMSpec(fsm.fsm));

	Set<Set<Parameter<?>>> actual = fs.getPossibleParameterSets();

	Set<Set<Parameter<?>>> expected = new HashSet<Set<Parameter<?>>>();
	expected.add(EMPTY_PARAMETER_SET);
	expected.add(asSet(fsm.m, fsm.c));
	expected.add(asSet(fsm.m, fsm.c, fsm.i));

	assertEquals(expected, actual);
    }

    @Test
    public void getParameters_unsafeMapIterator() throws Exception {
	FSM_SafeMapIterator fsm = new FSM_SafeMapIterator();
	FiniteParametricProperty fs = new FiniteParametricProperty(new FSMSpec(fsm.fsm));

	Set<Parameter<?>> actual = fs.getParameters();

	Set<Parameter<?>> expected = new HashSet<Parameter<?>>();

	expected.add(fsm.c);
	expected.add(fsm.m);
	expected.add(fsm.i);

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
