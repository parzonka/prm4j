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
import java.util.HashSet;
import java.util.Set;

import org.junit.After;

import prm4j.AbstractTest;
import prm4j.api.ParametricMonitor;
import prm4j.api.Symbol;
import prm4j.indexing.BaseMonitor;
import prm4j.indexing.staticdata.StaticDataConverter;
import prm4j.spec.FiniteParametricProperty;
import prm4j.spec.FiniteSpec;

public class AbstractDefaultParametricMonitorTest extends AbstractTest {

    public final static Object _ = null;
    protected FiniteParametricProperty fpp;
    protected StaticDataConverter converter;
    protected AwareDefaultBindingStore bindingStore;
    protected AwareDefaultNodeStore nodeStore;
    protected AwareBaseMonitor prototypeMonitor;
    protected NodeManager nodeManager;
    protected ParametricMonitor pm;

    protected AwareBaseMonitor monitor; // working variable

    public void createDefaultParametricMonitorWithAwareComponents(FiniteSpec finiteSpec) {
	fpp = new FiniteParametricProperty(finiteSpec);
	converter = new StaticDataConverter(fpp);
	bindingStore = new AwareDefaultBindingStore(finiteSpec.getFullParameterSet(), 1);
	nodeManager = new NodeManager();
	nodeStore = new AwareDefaultNodeStore(converter.getMetaTree(), nodeManager);
	prototypeMonitor = new AwareBaseMonitor(finiteSpec.getInitialState());
	pm = new DefaultParametricMonitor(bindingStore, nodeStore, prototypeMonitor, converter.getEventContext(),
		nodeManager, true);
    }

    protected AwareBaseMonitor popNextUpdatedMonitor() {
	if (prototypeMonitor.getUpdatedMonitors().isEmpty()) {
	    fail("There were no more updated monitors!");
	}
	return prototypeMonitor.getUpdatedMonitors().pop();
    }

    protected AwareBaseMonitor popNextCreatedMonitor() {
	if (prototypeMonitor.getCreatedMonitors().isEmpty()) {
	    fail("There were no more created monitors!");
	}
	return prototypeMonitor.getCreatedMonitors().pop();
    }

    protected Node popNextRetrievedNode() {
	if (nodeStore.getRetrievedNodes().isEmpty()) {
	    fail("There were no more retrieved nodes!");
	}
	return nodeStore.getRetrievedNodes().pop().get();
    }

    protected LowLevelBinding[] popNextRetrievedBinding() {
	if (bindingStore.getListOfBindings().isEmpty()) {
	    fail("There were no more retrieved bindings!");
	}
	return bindingStore.getListOfBindings().pop().get();
    }

    protected Node getNode(Object... boundObjects) {
	int[] parameterMask = toParameterMask(boundObjects);
	return nodeStore.getNode(bindingStore.getBindings(boundObjects), parameterMask);
    }

    protected Node getOrCreateNode(Object... boundObjects) {
	int[] parameterMask = toParameterMask(boundObjects);
	return nodeStore.getOrCreateNode(bindingStore.getBindings(boundObjects), parameterMask);
    }

    protected void assertBoundObjects(AwareBaseMonitor monitor, Object... boundObjects) {
	LowLevelBinding[] bindings = monitor.getLowLevelBindings();
	Object[] monitorBoundObjects = new Object[bindings.length];
	for (int i = 0; i < bindings.length; i++) {
	    monitorBoundObjects[i] = bindings[i].get();
	}
	assertArrayEquals(boundObjects, monitorBoundObjects);
    }

    protected void assertCreatedNodes(Object[]... instances) {
	Set<Node> createdNodes = new HashSet<Node>(nodeStore.getCreatedNodes());
	nodeStore.getCreatedNodes().clear();
	for (Object[] instance : instances) {
	    getOrCreateNode(instance);
	}
	assertEquals(nodeStore.getCreatedNodes(), createdNodes);
    }

    protected void assertChaining(Object[] from, Object[] to) {
	Node fromNode = getNode(from);
	Node toNode = getNode(to);
	boolean contained = false;
	for (MonitorSet monitorSet : fromNode.getMonitorSets()) {
	    if (monitorSet != null && monitorSet.contains(toNode.getMonitor())) {
		if (contained) {
		    fail("to-monitor is contained in multiple monitor sets!");
		}
		contained = true;
	    }
	}
	if (!contained) {
	    fail(Arrays.toString(to) + " was not contained in any monitor set of " + Arrays.toString(from) + "!");
	}
    }

    protected void assertNoMoreUpdatedMonitors() {
	assertTrue("There were more updated monitors: " + prototypeMonitor.getUpdatedMonitors(), prototypeMonitor
		.getUpdatedMonitors().isEmpty());
    }

    protected void assertNoMoreCreatedMonitors() {
	assertTrue("There were more created monitors: " + prototypeMonitor.getCreatedMonitors(), prototypeMonitor
		.getCreatedMonitors().isEmpty());
    }

    protected void assertNoMoreRetrievedNodes() {
	assertTrue("There were more retrieved nodes: " + nodeStore.getRetrievedNodes(), nodeStore.getRetrievedNodes()
		.isEmpty());
    }

    protected void assertTrace(AwareBaseMonitor monitor, Symbol... symbols) {
	assertEquals(Arrays.asList(symbols), monitor.getBaseEventTrace());
    }

    protected void assertTrace(Object[] boundObjects, Symbol... symbols) {
	if (getNode(boundObjects) == null) {
	    throw new NullPointerException("Node could not be found for " + Arrays.toString(boundObjects));
	}
	if (getNode(boundObjects).getMonitor() == null) {
	    throw new NullPointerException("No monitor stored for node " + Arrays.toString(boundObjects));
	}
	assertEquals(Arrays.asList(symbols),
		((AwareBaseMonitor) getNode(boundObjects).getMonitor()).getBaseEventTrace());
    }

    private static int[] toParameterMask(Object[] boundObjects) {
	int objectsCount = 0;
	for (int i = 0; i < boundObjects.length; i++) {
	    if (boundObjects[i] != null) {
		objectsCount++;
	    }
	}
	int[] result = new int[objectsCount];
	int j = 0;
	for (int i = 0; i < boundObjects.length; i++) {
	    if (boundObjects[i] != null) {
		result[j++] = i;
	    }
	}
	return result;
    }

    @After
    public void cleanup() {
	converter = null;
	bindingStore = null;
	nodeStore = null;
	prototypeMonitor = null;
	pm = null;
	monitor = null;
	BaseMonitor.reset();
    }

}
