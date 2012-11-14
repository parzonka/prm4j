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
import prm4j.indexing.BaseMonitor;
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
    final String b = "a";
    final String c = "a";

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

    @Test
    // TODO refactor
    public void newEvents_monitorUpdatesTimestampBoundObjects() throws Exception {

	String a = "a";
	String b = "b";

	// exercise
	pm.processEvent(fsm.createString.createEvent(a));
	pm.processEvent(fsm.createString.createEvent(b));

	// verify
	monitor = popNextUpdatedMonitor();
	assertCreationTime(0L, monitor);
	assertBoundObjects(monitor, a);
	monitor = popNextUpdatedMonitor();
	assertCreationTime(1L, monitor);
	assertBoundObjects(monitor, b);
	assertNoMoreUpdatedMonitors();
    }

    private void assertCreationTime(long timeStamp, BaseMonitor monitor) {
	assertEquals(timeStamp, monitor.getCreationTime());
    }

    public AwareBaseMonitor popNextUpdatedMonitor() {
	if (prototypeMonitor.getUpdatedMonitors().isEmpty())
	    fail("There were no more updated monitors!");
	return prototypeMonitor.getUpdatedMonitors().pop();
    }

    public AwareBaseMonitor popNextCreatedMonitor() {
	if (prototypeMonitor.getCreatedMonitors().isEmpty())
	    fail("There were no more created monitors!");
	return prototypeMonitor.getCreatedMonitors().pop();
    }

    private void assertBoundObjects(AwareBaseMonitor monitor, Object... boundObjects) {
	LowLevelBinding[] bindings = monitor.getLowLevelBindings();
	Object[] monitorBoundObjects = new Object[bindings.length];
	for (int i = 0; i < bindings.length; i++) {
	    monitorBoundObjects[i] = bindings[i].get();
	}
	assertArrayEquals(boundObjects, monitorBoundObjects);
    }

    public void assertNoMoreUpdatedMonitors() {
	assertTrue("There were more updated monitors: " + prototypeMonitor.getUpdatedMonitors(), prototypeMonitor
		.getUpdatedMonitors().isEmpty());
    }

    public void assertNoMoreCreatedMonitors() {
	assertTrue("There were more created monitors: " + prototypeMonitor.getCreatedMonitors(), prototypeMonitor
		.getCreatedMonitors().isEmpty());
    }

    public void assertTrace(AwareBaseMonitor monitor, Symbol... symbols) {
	assertEquals(Arrays.asList(symbols), monitor.getBaseEventTrace());
    }
}
