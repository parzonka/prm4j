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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
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
import prm4j.indexing.model.ModelVerifier;
import prm4j.indexing.model.ModelVerifier.ListMultimapVerifier;
import prm4j.indexing.model.ModelVerifier.SetMultimapVerifier;
import prm4j.indexing.model.ParameterNode;
import prm4j.indexing.model.ParametricPropertyModel;
import prm4j.indexing.model.ParametricPropertyProcessor;
import prm4j.spec.finite.FiniteParametricProperty;

public class ParametricPropertyModel_SafeSyncCollectionTest extends AbstractTest {

    FSM_SafeSyncCollection fsm;
    ParametricProperty fpp;
    ParametricPropertyModel ppm;
    ParametricPropertyProcessor proc;

    @Before
    public void init() {
	fsm = new FSM_SafeSyncCollection();
	fpp = new FiniteParametricProperty(new FSMSpec(fsm.fsm));
	ppm = new ParametricPropertyModel(fpp);
	proc = new ParametricPropertyProcessor(fpp);
    }

    // parametric property

    @Test
    public void getCreationEvents() throws Exception {
	Set<BaseEvent> actual = fpp.getCreationEvents();
	// verify
	Set<BaseEvent> expected = new HashSet<BaseEvent>();
	expected.add(fsm.sync);
	assertEquals(expected, actual);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void getMonitorStateSpec() throws Exception {
	SetMultimapVerifier<Set<Parameter<?>>, Tuple<Set<Parameter<?>>, Boolean>> expected = new ModelVerifier(ppm)
		.getMonitorStateSpec();
	expected.put(asSet(fsm.c), tuple(EMPTY_PARAMETER_SET, true));
	expected.put(asSet(fsm.i), tuple(EMPTY_PARAMETER_SET, true));
	expected.verify();

    }

    @SuppressWarnings("unchecked")
    @Test
    public void getUpdateChainingTuples() throws Exception {

	SetMultimapVerifier<Set<Parameter<?>>, Tuple<Set<Parameter<?>>, Set<Parameter<?>>>> expected = new ModelVerifier(
		ppm).getUpdateChainingTuples();
	expected.put(asSet(fsm.c, fsm.i), tuple(asSet(fsm.c), EMPTY_PARAMETER_SET));
	expected.put(asSet(fsm.c, fsm.i), tuple(asSet(fsm.i), EMPTY_PARAMETER_SET));
	expected.verify();
    }

    @Test
    public void getJoinTuples() throws Exception {
	ListMultimapVerifier<BaseEvent, Tuple<Set<Parameter<?>>, Set<Parameter<?>>>> expected = new ModelVerifier(
		ppm).getJoinTuples();
	expected.verify();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void getAliveParameterSets() throws Exception {
	SetMultimapVerifier<Set<Parameter<?>>, Set<Parameter<?>>> expected = new ModelVerifier(ppm)
		.getAliveParameterSets();
	expected.put(asSet(fsm.c), asSet(fsm.c, fsm.i));
	expected.put(asSet(fsm.c, fsm.i), asSet(fsm.i));
	expected.verify();

    }

    @Test
    public void getAliveParameterMasks() throws Exception {
	ParametricPropertyProcessor sdc = new ParametricPropertyProcessor(fpp);

	ParameterNode c = sdc.getParameterTree().getParameterNode(fsm.c);
	ParameterNode i = sdc.getParameterTree().getParameterNode(fsm.i);
	ParameterNode ci = sdc.getParameterTree().getParameterNode(fsm.c, fsm.i);

	// verify
	assertEquals(1, c.getAliveParameterMasks().length);
	assertArrayEquals(array(0, 0), c.getAliveParameterMasks()[0]);

	assertEquals(0, i.getAliveParameterMasks().length);

	assertEquals(1, ci.getAliveParameterMasks().length);
	assertArrayEquals(array(1), ci.getAliveParameterMasks()[0]);
    }
}
