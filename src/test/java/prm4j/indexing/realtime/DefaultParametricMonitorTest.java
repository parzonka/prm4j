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
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import prm4j.AbstractTest;
import prm4j.api.Symbol;
import prm4j.api.fsm.FSMSpec;
import prm4j.indexing.staticdata.StaticDataConverter;
import prm4j.spec.FiniteParametricProperty;
import prm4j.spec.FiniteSpec;

public class DefaultParametricMonitorTest extends AbstractTest {

    StaticDataConverter converter;
    AwareDefaultBindingStore bindingStore;
    AwareDefaultNodeStore nodeStore;
    AwareBaseMonitor prototypeMonitor;
    DefaultParametricMonitor pm;

    AwareBaseMonitor monitor; // working variable

    FSM_threeSameStrings fsm;
    final String a = "a";
    final String b = "b";
    final String c = "c";

    public void createDefaultParametricMonitorWithAwareComponents(FiniteSpec finiteSpec) {
	converter = new StaticDataConverter(new FiniteParametricProperty(finiteSpec));
	bindingStore = new AwareDefaultBindingStore(finiteSpec.getFullParameterSet(), 1);
	nodeStore = new AwareDefaultNodeStore(converter.getMetaTree());
	prototypeMonitor = new AwareBaseMonitor();
	pm = new DefaultParametricMonitor(bindingStore, nodeStore, prototypeMonitor, converter.getEventContext());
    }

    @Before
    public void init() {
	fsm = new FSM_threeSameStrings();
	FiniteSpec finiteSpec = new FSMSpec(fsm.fsm);
	createDefaultParametricMonitorWithAwareComponents(finiteSpec);
    }

    @Test
    public void verifyPostConditions() throws Exception {
	assertNoMoreCreatedMonitors();
	assertNoMoreUpdatedMonitors();
    }

    // firstEvent //////////////////////////////////////////////////////////////////

    @Test
    public void firstEventCreatesOnlyOneMonitor() throws Exception {
	// exercise
	pm.processEvent(fsm.createString.createEvent(a));

	// verify
	monitor = popNextCreatedMonitor();
	assertNoMoreCreatedMonitors();
    }

    @Test
    public void firstEventUpdatesOnlyOneMonitor() throws Exception {
	// exercise
	pm.processEvent(fsm.createString.createEvent(a));

	// verify
	monitor = popNextUpdatedMonitor();
	assertNoMoreUpdatedMonitors();
    }

    @Test
    public void firstEventCreatesMonitorWithCreationTime0() throws Exception {
	// exercise
	pm.processEvent(fsm.createString.createEvent(a));

	// verify
	monitor = popNextUpdatedMonitor();
	assertEquals(0L, monitor.getCreationTime());
    }

    @Test
    public void firstEventCreatesCorrectTrace() throws Exception {
	// exercise
	pm.processEvent(fsm.createString.createEvent(a));

	// verify
	monitor = popNextUpdatedMonitor();
	assertTrace(monitor, fsm.createString);
    }

    @Test
    public void firstEventsMonitorBindsAllItsParameters() throws Exception {
	// exercise
	pm.processEvent(fsm.createString.createEvent(a));

	// verify
	monitor = popNextUpdatedMonitor();
	assertBoundObjects(monitor, a);
    }

    // recurringEvent = same event as first event again ////////////////////////////////

    @Test
    public void recurringEvent_doesNotCreateNewMonitor() throws Exception {
	// exercise
	pm.processEvent(fsm.createString.createEvent(a));
	popNextCreatedMonitor();
	pm.processEvent(fsm.createString.createEvent(a));

	// verify
	assertNoMoreCreatedMonitors();
    }

    @Test
    public void recurringEvent_updatesSameMonitor() throws Exception {
	// exercise
	pm.processEvent(fsm.createString.createEvent(a));
	pm.processEvent(fsm.createString.createEvent(a));

	// verify
	monitor = popNextUpdatedMonitor();
	assertEquals(monitor, popNextUpdatedMonitor());
	assertNoMoreUpdatedMonitors();
    }

    @Test
    public void recurringEvent_createsCorrectTrace() throws Exception {
	// exercise
	pm.processEvent(fsm.createString.createEvent(a));
	pm.processEvent(fsm.createString.createEvent(a));

	// verify
	monitor = popNextUpdatedMonitor();
	assertTrace(monitor, fsm.createString, fsm.createString);
    }

    @Test
    public void recurringEvent_monitorHasSameTimestamp0() throws Exception {
	// exercise
	pm.processEvent(fsm.createString.createEvent(a));
	pm.processEvent(fsm.createString.createEvent(a));

	// verify
	monitor = popNextUpdatedMonitor();
	assertEquals(0L, monitor.getCreationTime());
	monitor = popNextUpdatedMonitor();
	assertEquals(0L, monitor.getCreationTime());
    }

    @Test
    public void recurringEvent_monitorStillBindsSameObjects() throws Exception {
	// exercise
	pm.processEvent(fsm.createString.createEvent(a));
	pm.processEvent(fsm.createString.createEvent(a));

	// verify
	monitor = popNextUpdatedMonitor();
	assertBoundObjects(monitor, a);
    }

 // twoEvents = two different events ////////////////////////////////

    @Test
    public void twoEvents_secondEventDoesCreateASingleNewMonitor() throws Exception {
	// exercise
	pm.processEvent(fsm.createString.createEvent(a));
	popNextCreatedMonitor();
	pm.processEvent(fsm.createString.createEvent(b));

	// verify
	popNextCreatedMonitor();
	assertNoMoreCreatedMonitors();
    }

    @Test
    public void twoEvents_createdMonitorsAreDifferent() throws Exception {
	// exercise
	pm.processEvent(fsm.createString.createEvent(a));
	pm.processEvent(fsm.createString.createEvent(b));

	// verify
	assertNotSame(popNextCreatedMonitor(), popNextCreatedMonitor());
    }

    @Test
    public void twoEvents_updatedMonitorsAreDifferent() throws Exception {
	// exercise
	pm.processEvent(fsm.createString.createEvent(a));
	pm.processEvent(fsm.createString.createEvent(b));

	// verify
	assertNotSame(popNextUpdatedMonitor(), popNextUpdatedMonitor());
	assertNoMoreUpdatedMonitors();
    }

    @Test
    public void twoEvents_bothTracesAreCorrect() throws Exception {
	// exercise
	pm.processEvent(fsm.createString.createEvent(a));
	pm.processEvent(fsm.createString.createEvent(b));

	// verify
	assertTrace(popNextUpdatedMonitor(), fsm.createString);
	assertTrace(popNextUpdatedMonitor(), fsm.createString);
    }

    @Test
    public void twoEvents_timestampsAreCorrect() throws Exception {
	// exercise
	pm.processEvent(fsm.createString.createEvent(a));
	pm.processEvent(fsm.createString.createEvent(b));

	// verify
	assertEquals(0L, popNextCreatedMonitor().getCreationTime());
	assertEquals(1L, popNextCreatedMonitor().getCreationTime());
    }

    @Test
    public void twoEvents_boundObjectsAreCorrect() throws Exception {
	// exercise
	pm.processEvent(fsm.createString.createEvent(a));
	pm.processEvent(fsm.createString.createEvent(b));

	// verify
	assertBoundObjects(popNextCreatedMonitor(), a);
	assertBoundObjects(popNextCreatedMonitor(), b);
    }

    // helper //////////////////////////////////////////////////////////////////////////////

    protected AwareBaseMonitor popNextUpdatedMonitor() {
	if (prototypeMonitor.getUpdatedMonitors().isEmpty())
	    fail("There were no more updated monitors!");
	return prototypeMonitor.getUpdatedMonitors().pop();
    }

    protected AwareBaseMonitor popNextCreatedMonitor() {
	if (prototypeMonitor.getCreatedMonitors().isEmpty())
	    fail("There were no more created monitors!");
	return prototypeMonitor.getCreatedMonitors().pop();
    }

    protected void assertBoundObjects(AwareBaseMonitor monitor, Object... boundObjects) {
	LowLevelBinding[] bindings = monitor.getLowLevelBindings();
	Object[] monitorBoundObjects = new Object[bindings.length];
	for (int i = 0; i < bindings.length; i++) {
	    monitorBoundObjects[i] = bindings[i].get();
	}
	assertArrayEquals(boundObjects, monitorBoundObjects);
    }

    protected void assertNoMoreUpdatedMonitors() {
	assertTrue("There were more updated monitors: " + prototypeMonitor.getUpdatedMonitors(), prototypeMonitor
		.getUpdatedMonitors().isEmpty());
    }

    protected void assertNoMoreCreatedMonitors() {
	assertTrue("There were more created monitors: " + prototypeMonitor.getCreatedMonitors(), prototypeMonitor
		.getCreatedMonitors().isEmpty());
    }

    protected void assertTrace(AwareBaseMonitor monitor, Symbol... symbols) {
	assertEquals(Arrays.asList(symbols), monitor.getBaseEventTrace());
    }
}
