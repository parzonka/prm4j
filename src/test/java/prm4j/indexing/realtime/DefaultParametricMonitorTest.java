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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import prm4j.AbstractTest;
import prm4j.api.fsm.FSM;
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

    public void createDefaultParametricMonitorWithAwareComponents(FiniteSpec finiteSpec) {
	converter = new StaticDataConverter(new FiniteParametricProperty(finiteSpec));
	bindingStore = new AwareDefaultBindingStore(finiteSpec.getFullParameterSet(), 1);
	nodeStore = new AwareDefaultNodeStore(converter.getMetaTree());
	prototypeMonitor = new AwareBaseMonitor();
	pm = new DefaultParametricMonitor(bindingStore, nodeStore, prototypeMonitor, converter.getEventContext());
    }

    @Test
    public void newEvents_monitorUpdatesTimestampBoundObjects() throws Exception {
	FSM_threeSameStrings fsm = new FSM_threeSameStrings();
	FiniteSpec finiteSpec = new FSMSpec(fsm.fsm);

	createDefaultParametricMonitorWithAwareComponents(finiteSpec);

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
	return prototypeMonitor.getUpdatedMonitors().pop();
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
}
