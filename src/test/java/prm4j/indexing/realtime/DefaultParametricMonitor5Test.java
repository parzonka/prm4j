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

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import prm4j.api.BaseEvent;
import prm4j.api.Event;
import prm4j.api.Parameter;
import prm4j.api.fsm.FSMSpec;
import prm4j.indexing.staticdata.StaticDataConverter;
import prm4j.spec.FiniteParametricProperty;
import prm4j.spec.FiniteSpec;

public class DefaultParametricMonitor5Test extends AbstractDefaultParametricMonitorTest {

    public final static BoundObject _ = null;
    private static int boundObjectCounter = 0;
    private static int instanceCounter = 0;

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
	boundObjectCounter = 0;
	instanceCounter = 0;
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
	bindingStore = new AwareDefaultBindingStore(finiteSpec.getFullParameterSet(), 1);
	nodeStore = new AwareDefaultNodeStore(converter.getMetaTree());
	prototypeMonitor = new AwareBaseMonitor(finiteSpec.getInitialState());
	pm = new DefaultParametricMonitor(bindingStore, nodeStore, prototypeMonitor, converter.getEventContext());
    }

    @Test
    public void m1c1i1_m1c2ci_createCorrectNodes() throws Exception {

	final ParametricInstance instance1 = instance(m1, c1, i1);
	final ParametricInstance instance2 = instance(m1, c2, i2);

	pm.processEvent(instance1.createEvent(fsm.createColl));
	pm.processEvent(instance2.createEvent(fsm.createColl));

	assertCreatedNodes(array(m1, _, _), array(_, c1, _), array(_, c2), array(m1, c1, _), array(m1, c2, _));

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

	assertTrace(instance1.boundObjects, fsm.createColl, fsm.createIter, fsm.updateMap, fsm.useIter);
	assertTrace(instance2.boundObjects, fsm.createColl, fsm.createIter, fsm.updateMap, fsm.useIter);
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

	assertTrace(instance1.boundObjects, fsm.createColl, fsm.createIter, fsm.updateMap, fsm.useIter);
	assertTrace(instance2.boundObjects, fsm.createColl, fsm.createIter, fsm.updateMap, fsm.useIter);
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
	    assertTrace(instance.boundObjects, fsm.createColl, fsm.createIter, fsm.updateMap, fsm.useIter);
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
	    assertEquals(1, getNode(m1, _,_).getMonitorSet(0).getSize());
	}
    }

    @Test
    public void multipleOpenInstances_monitorSetSizeGrows() throws Exception {

	for (int i = 0; i < 100; i++) {
	    final ParametricInstance instance = instance(m1, _, _);
	    pm.processEvent(instance.createEvent(fsm.createColl));
	    pm.processEvent(instance.createEvent(fsm.createIter));
	    pm.processEvent(instance.createEvent(fsm.updateMap));
	    assertEquals(i+1, getNode(m1, _,_).getMonitorSet(0).getSize());
	}
    }

    protected void processEvents(ParametricInstance eventGenerator, BaseEvent... baseEvents) {
	for (BaseEvent baseEvent : baseEvents) {
	    pm.processEvent(eventGenerator.createEvent(baseEvent));
	}
    }

    protected ParametricInstance instance(BoundObject... boundObjects) {
	return new ParametricInstance(boundObjects);
    }

    public class ParametricInstance {

	final int instanceId;
	final BoundObject[] boundObjects;
	final String[] boundObjectIds;

	public ParametricInstance(BoundObject[] boundObjects) {
	    this.boundObjects = new BoundObject[boundObjects.length];
	    boundObjectIds = new String[boundObjects.length];
	    for (int i = 0; i < boundObjects.length; i++) {
		BoundObject boundObject = boundObjects[i];
		if (boundObject == null) {
		    boundObject = new BoundObject();
		}
		this.boundObjects[i] = boundObject;
		boundObjectIds[i] = boundObject.id;
	    }
	    instanceId = instanceCounter++;
	}

	public Event createEvent(BaseEvent baseEvent) {
	    final Object[] obj = new Object[boundObjects.length];
	    for (Parameter<?> parameter : baseEvent.getParameters()) {
		obj[parameter.getIndex()] = boundObjects[parameter.getIndex()];
	    }
	    return new Event(baseEvent, obj, instanceId);
	}

	public List<Event> createEvents(BaseEvent... baseEvents) {
	    final List<Event> result = new ArrayList<Event>();
	    for (BaseEvent baseEvent : baseEvents) {
		final Object[] objects = new Object[boundObjects.length];
		for (Parameter<?> parameter : baseEvent.getParameters()) {
		    objects[parameter.getIndex()] = boundObjects[parameter.getIndex()];
		}
		result.add(new Event(baseEvent, objects, instanceId));
	    }
	    return result;
	}
    }

    public class BoundObject {

	public final String id;

	public BoundObject(String id) {
	    this.id = id;
	}

	public BoundObject() {
	    id = "" + boundObjectCounter++;
	}

	@Override
	public String toString() {
	    return id;
	}

    }

}
