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
package prm4j.indexing.realtime;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static prm4j.Util.tuple;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import prm4j.Util.Tuple;
import prm4j.api.BaseEvent;
import prm4j.api.Parameter;
import prm4j.api.fsm.FSMSpec;
import prm4j.indexing.binding.Binding;
import prm4j.indexing.model.ModelVerifier;
import prm4j.indexing.model.ModelVerifier.ListMultimapVerifier;
import prm4j.spec.finite.FiniteSpec;

public class DefaultParametricMonitor_ab_bc_cd_ad_Test extends AbstractDefaultParametricMonitorTest {

    FSM_ab_bc_cd_ad fsm;

    Parameter<?> a;
    Parameter<?> b;
    Parameter<?> c;
    Parameter<?> d;

    final String a1 = "a1";
    final String b1 = "b1";
    final String c1 = "c1";
    final String d1 = "d1";
    final String a2 = "a2";
    final String b2 = "b2";
    final String c2 = "c2";
    final String d2 = "d2";

    @Before
    public void init() {
	fsm = new FSM_ab_bc_cd_ad();
	a = fsm.a;
	b = fsm.b;
	c = fsm.c;
	d = fsm.d;
	FiniteSpec finiteSpec = new FSMSpec(fsm.fsm);
	createDefaultParametricMonitorWithAwareComponents(finiteSpec);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void model() throws Exception {
	ModelVerifier m = new ModelVerifier(ppm);

	m.getFindMaxInstanceTypes().verify();

	ListMultimapVerifier<BaseEvent, Tuple<Set<Parameter<?>>, Set<Parameter<?>>>> expectedJoinTuples = m
		.getJoinTuples();
	expectedJoinTuples.put(fsm.event_cd, list(tuple(asSet(fsm.c), asSet(fsm.a, fsm.b, fsm.c))));
	expectedJoinTuples.put(fsm.event_bc, list(tuple(asSet(fsm.b), asSet(fsm.a, fsm.b))));
	expectedJoinTuples.verify();

	m.expectDisableInstanceTypes().verify();
    }

    @Test
    public void ab_bc_cd_createNodes1() throws Exception {
	// exercise
	pm.processEvent(fsm.event_ab.createEvent(a1, b1));
	pm.processEvent(fsm.event_bc.createEvent(b1, c1));

	assertNodeExists(getNode(a1, b1, c1, _));
    }

    @Test
    public void ab_correctBindingsInMonitor() throws Exception {
	// exercise
	pm.processEvent(fsm.event_ab.createEvent(a1, b1));

	Binding b_a1 = bindingStore.getBinding(a1);
	Binding b_b1 = bindingStore.getBinding(b1);

	assertArrayEquals(array(b_a1, b_b1), getNode(a1, b1, _, _).getMonitor().getCompressedBindings());
    }

    @Test
    public void ab_bc_cd_correctUncompressedBindings() throws Exception {
	// exercise
	pm.processEvent(fsm.event_ab.createEvent(a1, b1));
	pm.processEvent(fsm.event_bc.createEvent(b1, c1));

	Binding b_a1 = bindingStore.getBinding(a1);
	Binding b_b1 = bindingStore.getBinding(b1);
	Binding b_c1 = bindingStore.getBinding(c1);

	assertArrayEquals(array(b_a1, b_b1, null, null), getNode(a1, b1, _, _).getMonitor().getUncompressedBindings());
	assertArrayEquals(array(b_a1, b_b1, b_c1, null), getNode(a1, b1, c1, _).getMonitor().getUncompressedBindings());
    }

    @Test
    public void ab_bc_correctBindingsInMonitor() throws Exception {
	// exercise
	pm.processEvent(fsm.event_ab.createEvent(a1, b1));
	pm.processEvent(fsm.event_bc.createEvent(b1, c1));
	Binding b_a1 = bindingStore.getBinding(a1);
	Binding b_b1 = bindingStore.getBinding(b1);
	Binding b_c1 = bindingStore.getBinding(c1);

	assertArrayEquals(array(b_a1, b_b1, b_c1), getNode(a1, b1, c1, _).getMonitor().getCompressedBindings());
    }

    @Test
    public void ab_bc_cd_ad_createNodes2() throws Exception {
	// exercise
	pm.processEvent(fsm.event_ab.createEvent(a1, b1));
	pm.processEvent(fsm.event_bc.createEvent(b1, c1));
	pm.processEvent(fsm.event_cd.createEvent(c1, d1));

	assertNodeExists(getNode(a1, b1, c1, d1));
    }

    @Test
    public void ab_bc_cd_ad_simpleMatch() throws Exception {
	// exercise
	pm.processEvent(fsm.event_ab.createEvent(a1, b1));
	pm.processEvent(fsm.event_bc.createEvent(b1, c1));
	pm.processEvent(fsm.event_cd.createEvent(c1, d1));
	pm.processEvent(fsm.event_ad.createEvent(a1, d1));

	assertEquals(1, fsm.matchHandler.getHandledMatches().size());
    }

    @Test
    public void ab_bc_cd_ad_disableMatch1() throws Exception {
	// exercise
	pm.processEvent(fsm.event_cd.createEvent(c1, d1));
	pm.processEvent(fsm.event_ab.createEvent(a1, b1));
	pm.processEvent(fsm.event_bc.createEvent(b1, c1));
	pm.processEvent(fsm.event_cd.createEvent(c1, d1));
	pm.processEvent(fsm.event_ad.createEvent(a1, d1));

	assertEquals(0, fsm.matchHandler.getHandledMatches().size());
    }

    @Test
    public void ab_bc_cd_ad_disableMatch2() throws Exception {
	// exercise
	pm.processEvent(fsm.event_ab.createEvent(a1, b1));
	pm.processEvent(fsm.event_cd.createEvent(c1, d1));
	pm.processEvent(fsm.event_bc.createEvent(b1, c1));
	pm.processEvent(fsm.event_cd.createEvent(c1, d1));
	pm.processEvent(fsm.event_ad.createEvent(a1, d1));

	assertEquals(0, fsm.matchHandler.getHandledMatches().size());
    }

    @Test
    public void ab_bc_cd_ad_interveningTraceSlice() throws Exception {
	// exercise
	pm.processEvent(fsm.event_ab.createEvent(c1, d2)); // an event from another traceslice
	pm.processEvent(fsm.event_ab.createEvent(a1, b1));
	pm.processEvent(fsm.event_bc.createEvent(b1, c1));
	pm.processEvent(fsm.event_cd.createEvent(c1, d1)); // c1d2 prevents creation of node c1d1 because the timestamp
							   // of c1
	// an improved version would need to store timestamps with nodes, not bindings
	pm.processEvent(fsm.event_ad.createEvent(a1, d1));

	// algorithm seems to fixed here
	assertEquals(1, fsm.matchHandler.getHandledMatches().size());
    }

}
