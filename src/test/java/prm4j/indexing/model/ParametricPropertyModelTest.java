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
package prm4j.indexing.model;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import prm4j.AbstractTest;
import prm4j.api.Parameter;
import prm4j.api.fsm.FSMSpec;
import prm4j.indexing.IndexingUtils;
import prm4j.spec.finite.FiniteParametricProperty;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

public class ParametricPropertyModelTest extends AbstractTest {

    @Test
    public void getMonitorSetIds_FSM_ab_b_with_initial_b_loop() {

	FSM_ab_b_with_initial_b_loop fsm = new FSM_ab_b_with_initial_b_loop();
	FiniteParametricProperty fpp = new FiniteParametricProperty(new FSMSpec(fsm.fsm));
	ParametricPropertyModel ppm = new ParametricPropertyModel(fpp);
	Table<Set<Parameter<?>>, Set<Parameter<?>>, Integer> actual = ppm.getMonitorSetIds();

	Table<Set<Parameter<?>>, Set<Parameter<?>>, Integer> expected = HashBasedTable.create();
	expected.put(asSet(fsm.p2), EMPTY_PARAMETER_SET, 0);

	assertEquals(expected, actual);

    }

    @Test
    public void calculateDisableParameterMasks1() {

	Parameter<String> a = new Parameter<String>("a");
	a.setIndex(0);
	Parameter<String> b = new Parameter<String>("b");
	b.setIndex(1);

	List<Set<Parameter<?>>> setOfParameterSets = new ArrayList<Set<Parameter<?>>>();
	setOfParameterSets.add(asSet(b));
	setOfParameterSets.add(asSet(a, b));

	int[][] actual = IndexingUtils.toParameterMasks(setOfParameterSets);

	int[][] expected = new int[2][];
	expected[0] = array(1);
	expected[1] = array(0, 1);

	assertArrayEquals(expected, actual);

    }

    @Test
    public void calculateDisableParameterMasks2() {

	Parameter<String> a = new Parameter<String>("a");
	a.setIndex(0);
	Parameter<String> b = new Parameter<String>("b");
	b.setIndex(1);
	Parameter<String> c = new Parameter<String>("c");
	c.setIndex(2);

	List<Set<Parameter<?>>> listOfParameterSets = new ArrayList<Set<Parameter<?>>>();
	listOfParameterSets.add(asSet(c));
	listOfParameterSets.add(asSet(a, b));
	listOfParameterSets.add(asSet(b, c));

	int[][] actual = IndexingUtils.toParameterMasks(listOfParameterSets);

	int[][] expected = new int[3][];
	expected[0] = array(2);
	expected[1] = array(0, 1);
	expected[2] = array(1, 2);

	assertArrayEquals(expected, actual);

    }

    @Test
    public void toListOfParameterSetsAscending() {

	Parameter<String> a = new Parameter<String>("a");
	a.setIndex(0);
	Parameter<String> b = new Parameter<String>("b");
	b.setIndex(1);
	Parameter<String> c = new Parameter<String>("c");
	c.setIndex(2);

	Set<Set<Parameter<?>>> setOfParameterSets = new HashSet<Set<Parameter<?>>>();
	setOfParameterSets.add(asSet(c));
	setOfParameterSets.add(asSet(a, c));
	setOfParameterSets.add(asSet(a, b, c));
	setOfParameterSets.add(asSet(a, b));
	setOfParameterSets.add(asSet(a));
	setOfParameterSets.add(asSet(b, c));

	List<Set<Parameter<?>>> actual = IndexingUtils.toListOfParameterSetsAscending(setOfParameterSets);

	List<Set<Parameter<?>>> expected = new ArrayList<Set<Parameter<?>>>();
	expected.add(asSet(a));
	expected.add(asSet(c));
	expected.add(asSet(a, b));
	expected.add(asSet(a, c));
	expected.add(asSet(b, c));
	expected.add(asSet(a, b, c));

	assertEquals(expected, actual);

    }

}
