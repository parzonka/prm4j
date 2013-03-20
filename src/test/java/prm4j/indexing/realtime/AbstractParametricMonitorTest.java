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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;

import org.junit.After;

import prm4j.AbstractTest;
import prm4j.api.ParametricMonitor;
import prm4j.api.Symbol;
import prm4j.indexing.DefaultParametricMonitor;
import prm4j.indexing.binding.ArrayBasedBindingFactory;
import prm4j.indexing.binding.DefaultBindingStore;
import prm4j.indexing.model.ParametricPropertyProcessor;
import prm4j.indexing.monitor.AbstractMonitor;
import prm4j.indexing.monitor.DeadMonitor;
import prm4j.indexing.monitor.Monitor;
import prm4j.indexing.monitor.MonitorSet;
import prm4j.indexing.monitor.StatefulMonitor;
import prm4j.indexing.node.DefaultNodeStore;
import prm4j.indexing.node.Node;
import prm4j.indexing.node.NodeManager;
import prm4j.indexing.node.NodeStore;
import prm4j.spec.finite.FiniteParametricProperty;
import prm4j.spec.finite.FiniteSpec;

public class AbstractParametricMonitorTest extends AbstractTest {

    public final static BoundObject _ = null;
    protected FiniteParametricProperty fpp;
    protected ParametricPropertyProcessor processor;
    protected DefaultBindingStore bindingStore;
    protected NodeStore nodeStore;
    protected Monitor prototypeMonitor;
    protected NodeManager nodeManager;
    protected ParametricMonitor pm;

    public void createDefaultParametricMonitor(FiniteSpec finiteSpec) {
	fpp = new FiniteParametricProperty(finiteSpec);
	processor = new ParametricPropertyProcessor(fpp);
	bindingStore = new DefaultBindingStore(new ArrayBasedBindingFactory(), finiteSpec.getFullParameterSet(), 1);
	nodeManager = new NodeManager();
	nodeStore = new DefaultNodeStore(processor.getParameterTree(), nodeManager);
	prototypeMonitor = new StatefulMonitor(fpp.getSpec().getInitialState());
	pm = new DefaultParametricMonitor(bindingStore, nodeStore, prototypeMonitor, processor.getEventContext(),
		nodeManager, true);
    }

    protected Node getNode(Object... boundObjects) {
	int[] parameterMask = toParameterMask(boundObjects);
	return nodeStore.getNode(bindingStore.getBindings(boundObjects), parameterMask);
    }

    protected Node getOrCreateNode(Object... boundObjects) {
	int[] parameterMask = toParameterMask(boundObjects);
	return nodeStore.getOrCreateNode(bindingStore.getBindings(boundObjects), parameterMask);
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
	processor = null;
	bindingStore = null;
	nodeStore = null;
	prototypeMonitor = null;
	pm = null;
	AbstractMonitor.reset();
	DeadMonitor.reset();
    }

}
