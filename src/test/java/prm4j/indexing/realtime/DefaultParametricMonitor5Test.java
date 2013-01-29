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
package prm4j.indexing.realtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import prm4j.api.BaseEvent;
import prm4j.api.fsm.FSMSpec;
import prm4j.indexing.BaseMonitor;
import prm4j.indexing.staticdata.StaticDataConverter;
import prm4j.spec.FiniteParametricProperty;
import prm4j.spec.FiniteSpec;

public class DefaultParametricMonitor5Test extends AbstractDefaultParametricMonitorTest {

    public final static BoundObject _ = null;

    FSM_unsafeMapIterator fsm;

    BoundObject m1;
    BoundObject c1;
    BoundObject c2;
    BoundObject c3;
    BoundObject i1;
    BoundObject i2;
    BoundObject i3;

    @Before
    public void init() {
	fsm = new FSM_unsafeMapIterator();
	createDefaultParametricMonitorWithAwareComponents(new FSMSpec(fsm.fsm));
	m1 = new BoundObject("m1");
	c1 = new BoundObject("c1");
	c2 = new BoundObject("c2");
	c3 = new BoundObject("c3");
	i1 = new BoundObject("i1");
	i2 = new BoundObject("i2");
	i3 = new BoundObject("i3");
    }

    @Override
    public void createDefaultParametricMonitorWithAwareComponents(FiniteSpec finiteSpec) {
	converter = new StaticDataConverter(new FiniteParametricProperty(finiteSpec));
	bindingStore = new AwareDefaultBindingStore(new DefaultBindingFactory(), finiteSpec.getFullParameterSet(), 1);
	prototypeMonitor = new AwareBaseMonitor(finiteSpec.getInitialState());
	nodeManager = new NodeManager();
	nodeStore = new AwareDefaultNodeStore(converter.getMetaTree(), nodeManager);
	pm = new DefaultParametricMonitor(bindingStore, nodeStore, prototypeMonitor, converter.getEventContext(),
		nodeManager, true);
    }

    @Test
    public void m1c1i1_createCorrectNodes() throws Exception {

	final ParametricInstance instance1 = instance(m1, c1, i1);

	pm.processEvent(instance1.createEvent(fsm.createColl));

	assertEquals(4L, nodeManager.getCreatedCount()); // root + m1, c1, m1c1
	assertCreatedNodes(array(m1, _, _), array(_, c1, _), array(m1, c1, _));
    }

    @Test
    public void m1c1i1_createCorrectNodesWithCorrectUpdateCount() throws Exception {

	final ParametricInstance instance1 = instance(m1, c1, i1);

	pm.processEvent(instance1.createEvent(fsm.createColl));
	pm.processEvent(instance1.createEvent(fsm.createIter));

	assertEquals(7L, nodeManager.getCreatedCount()); // root + ...
	assertCreatedNodes(array(m1, _, _), array(_, c1, _), array(m1, c1, _), array(_, c1, i1), array(m1, c1, i1),
		array(_, _, i1));
	assertEquals(3, BaseMonitor.getCreatedMonitorsCount()); // m1, m1c1, m1c1i1
	assertEquals(2, BaseMonitor.getUpdateddMonitorsCount()); // m1c1, m1c1i1
    }

    @Test
    public void m1c1i1_correctUpdateCount3() throws Exception {

	final ParametricInstance instance1 = instance(m1, c1, i1);

	pm.processEvent(instance1.createEvent(fsm.createColl)); // 1
	pm.processEvent(instance1.createEvent(fsm.createIter)); // 2
	pm.processEvent(instance1.createEvent(fsm.updateMap)); // 3

	// m1 is not updated at all, it never gets a monitor.
	// This is correct, because at (1) only the monitor for mi is created.
	// at (3) the monitor is created, but not updated. Only mci is updated, because the transition m->mc is not in
	// the chainSet, because it is not stagechanging!
	assertEquals(3, BaseMonitor.getUpdateddMonitorsCount()); // m1c1, m1c1i1, m1c1i1
    }

    @Test
    public void m1c1i1_correctUpdateCount4() throws Exception {

	final ParametricInstance instance1 = instance(m1, c1, i1);

	pm.processEvent(instance1.createEvent(fsm.createColl)); // 1
	pm.processEvent(instance1.createEvent(fsm.createIter)); // 2
	pm.processEvent(instance1.createEvent(fsm.updateMap)); // 3
	pm.processEvent(instance1.createEvent(fsm.updateMap)); // 4

	// at (3) the monitor is created, but not updated.Only mci is updated, because the transition m->mc is not in
	// the chainSet, because it is not stagechanging!
	// at (4) m1 is not updated, because it will never have a monitor! m1c1 not (not stagechanging), and m1c1i1 is
	// the only valid target.
	assertEquals(4, BaseMonitor.getUpdateddMonitorsCount()); // m1c1, m1c1i1, m1c1i1. (4:) m1c1i1
    }

    @Test
    public void m1c1i1_m1c2ci_createCorrectNodes() throws Exception {

	final ParametricInstance instance1 = instance(m1, c1, i1);
	final ParametricInstance instance2 = instance(m1, c2, i2);

	pm.processEvent(instance1.createEvent(fsm.createColl));
	pm.processEvent(instance2.createEvent(fsm.createColl));

	assertCreatedNodes(array(m1, _, _), array(_, c1, _), array(_, c2, _), array(m1, c1, _), array(m1, c2, _));

    }

    @Test
    public void m1c1i1_m1c2ci_update_correctTraces() throws Exception {

	final ParametricInstance instance1 = instance(m1, c1, i1);
	final ParametricInstance instance2 = instance(m1, c2, i2);

	pm.processEvent(instance1.createEvent(fsm.createColl));
	pm.processEvent(instance2.createEvent(fsm.createColl));
	pm.processEvent(instance1.createEvent(fsm.updateMap));

	assertTrace(array(m1, c1, _), fsm.createColl);
	assertTrace(array(m1, c2, _), fsm.createColl);
    }

    @Test
    public void m1c1i1_m1c2ci_update2_correctTraces() throws Exception {

	final ParametricInstance instance1 = instance(m1, c1, i1);
	final ParametricInstance instance2 = instance(m1, c2, i2);

	pm.processEvent(instance1.createEvent(fsm.createColl));
	pm.processEvent(instance1.createEvent(fsm.createIter));
	pm.processEvent(instance2.createEvent(fsm.createColl));
	pm.processEvent(instance2.createEvent(fsm.createIter));
	pm.processEvent(instance1.createEvent(fsm.updateMap));

	assertTrace(array(m1, c1, i1), fsm.createColl, fsm.createIter, fsm.updateMap);
	assertTrace(array(m1, c2, i2), fsm.createColl, fsm.createIter, fsm.updateMap);
	assertTrue(fsm.matchHandler.getHandledMatches().isEmpty());
    }

    @Test
    public void m1c1i1_m1c2ci_update3_correctTracesAndMatch() throws Exception {

	final ParametricInstance instance1 = instance(m1, c1, i1);
	final ParametricInstance instance2 = instance(m1, c2, i2);

	pm.processEvent(instance1.createEvent(fsm.createColl));
	pm.processEvent(instance1.createEvent(fsm.createIter));
	pm.processEvent(instance2.createEvent(fsm.createColl));
	pm.processEvent(instance2.createEvent(fsm.createIter));
	pm.processEvent(instance1.createEvent(fsm.updateMap));
	pm.processEvent(instance1.createEvent(fsm.useIter));
	pm.processEvent(instance2.createEvent(fsm.useIter));

	assertTrace(array(m1, c1, i1), fsm.createColl, fsm.createIter, fsm.updateMap, fsm.useIter);
	assertTrace(array(m1, c2, i2), fsm.createColl, fsm.createIter, fsm.updateMap, fsm.useIter);
	assertEquals(list(0, 1), fsm.matchHandler.getHandledMatches());
    }

    @Test
    public void m1c1i1_m1c2ci_update4_correctTracesAndMatch() throws Exception {

	// different kind of specification using _ for null, forcing new object creation
	final ParametricInstance instance1 = instance(m1, _, _);
	final ParametricInstance instance2 = instance(m1, _, _);

	pm.processEvent(instance1.createEvent(fsm.createColl));
	pm.processEvent(instance1.createEvent(fsm.createIter));
	pm.processEvent(instance2.createEvent(fsm.createColl));
	pm.processEvent(instance2.createEvent(fsm.createIter));
	pm.processEvent(instance1.createEvent(fsm.updateMap));
	pm.processEvent(instance1.createEvent(fsm.useIter));
	pm.processEvent(instance2.createEvent(fsm.useIter));

	assertTrace(instance1.getBoundObjects(), fsm.createColl, fsm.createIter, fsm.updateMap, fsm.useIter);
	assertTrace(instance2.getBoundObjects(), fsm.createColl, fsm.createIter, fsm.updateMap, fsm.useIter);
	assertEquals(list(0, 1), fsm.matchHandler.getHandledMatches());
    }

    @Test
    public void m1c1i1_m1c2ci_update5_correctTracesAndMatch() throws Exception {

	final ParametricInstance instance1 = instance(m1, _, _);
	final ParametricInstance instance2 = instance(m1, _, _);

	pm.processEvent(instance1.createEvent(fsm.createColl));
	pm.processEvent(instance1.createEvent(fsm.createIter));
	pm.processEvent(instance1.createEvent(fsm.updateMap));
	pm.processEvent(instance1.createEvent(fsm.useIter));

	pm.processEvent(instance2.createEvent(fsm.createColl));
	pm.processEvent(instance2.createEvent(fsm.createIter));
	pm.processEvent(instance2.createEvent(fsm.updateMap));
	pm.processEvent(instance2.createEvent(fsm.useIter));

	// this monitor gets nullified during the second createColl event because it is terminated
	assertNull(getNode((Object[]) instance1.getBoundObjects()).getMonitor());
	assertTrace(instance2.getBoundObjects(), fsm.createColl, fsm.createIter, fsm.updateMap, fsm.useIter);
	assertEquals(list(0, 1), fsm.matchHandler.getHandledMatches());
    }

    @Test
    public void FSM_unsafeMapIterator_ensureCorrectFinalStates() throws Exception {
	assertFalse(fsm.initial.isFinal());
	assertFalse(fsm.s1.isFinal());
	assertFalse(fsm.s2.isFinal());
	assertTrue(fsm.error.isFinal());
    }

    @Test
    public void m1c1i1_m1c2ci_update5_monitorsAreDeadAfterReachingErrorState() throws Exception {

	final ParametricInstance instance1 = instance(m1, _, _);

	pm.processEvent(instance1.createEvent(fsm.createColl));
	pm.processEvent(instance1.createEvent(fsm.createIter));
	pm.processEvent(instance1.createEvent(fsm.updateMap));
	pm.processEvent(instance1.createEvent(fsm.useIter));

	assertEquals(list(0), fsm.matchHandler.getHandledMatches());
    }

    @Test
    public void multipleInstances_correctNumberOfMatches() throws Exception {

	for (int i = 0; i < 10; i++) {
	    final ParametricInstance instance = instance(m1, _, _);
	    pm.processEvent(instance.createEvent(fsm.createColl));
	    pm.processEvent(instance.createEvent(fsm.createIter));
	    pm.processEvent(instance.createEvent(fsm.updateMap));
	    pm.processEvent(instance.createEvent(fsm.useIter));
	    assertTrace(instance.getBoundObjects(), fsm.createColl, fsm.createIter, fsm.updateMap, fsm.useIter);
	}
	assertEquals(10, fsm.matchHandler.getHandledMatches().size());
    }

    @Test
    public void multipleAcceptingInstances_monitorSetSizeStays1() throws Exception {

	for (int i = 0; i < 10; i++) {
	    final ParametricInstance instance = instance(m1, _, _);
	    pm.processEvent(instance.createEvent(fsm.createColl));
	    pm.processEvent(instance.createEvent(fsm.createIter));
	    pm.processEvent(instance.createEvent(fsm.updateMap));
	    pm.processEvent(instance.createEvent(fsm.useIter));
	    assertEquals(1, getNode(m1, _, _).getMonitorSet(0).getSize());
	}
    }

    @Test
    public void multipleOpenInstances_monitorSetSizeGrows() throws Exception {

	for (int i = 0; i < 100; i++) {
	    final ParametricInstance instance = instance(m1, _, _);
	    pm.processEvent(instance.createEvent(fsm.createColl));
	    pm.processEvent(instance.createEvent(fsm.createIter));
	    pm.processEvent(instance.createEvent(fsm.updateMap));
	    assertEquals(i + 1, getNode(m1, _, _).getMonitorSet(0).getSize());
	}
    }

    protected void processEvents(ParametricInstance eventGenerator, BaseEvent... baseEvents) {
	for (BaseEvent baseEvent : baseEvents) {
	    pm.processEvent(eventGenerator.createEvent(baseEvent));
	}
    }

}
