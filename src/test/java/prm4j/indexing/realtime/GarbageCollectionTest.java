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

import org.junit.Test;

import prm4j.AbstractTest;
import prm4j.api.fsm.FSM;
import prm4j.api.fsm.FSMSpec;
import prm4j.indexing.BaseMonitor;
import prm4j.indexing.StatefulMonitor;
import prm4j.indexing.staticdata.StaticDataConverter;
import prm4j.spec.FiniteParametricProperty;
import prm4j.spec.FiniteSpec;

public class GarbageCollectionTest extends AbstractTest {

    protected StaticDataConverter converter;
    protected DefaultBindingStore bindingStore;
    protected DefaultNodeStore nodeStore;
    protected BaseMonitor prototypeMonitor;
    protected DefaultParametricMonitor pm;

    public void createDefaultParametricMonitorWithAwareComponents(FSM fsm, int cleaningInterval) {
	FiniteSpec finiteSpec = new FSMSpec(fsm);
	converter = new StaticDataConverter(new FiniteParametricProperty(finiteSpec));
	bindingStore = new DefaultBindingStore(finiteSpec.getFullParameterSet(), 1);
	nodeStore = new DefaultNodeStore(converter.getMetaTree());
	prototypeMonitor = new StatefulMonitor(finiteSpec.getInitialState());
	pm = new DefaultParametricMonitor(bindingStore, nodeStore, prototypeMonitor, converter.getEventContext());
    }

    @Test
    public void aliveBinding_nodeIsPersistent() throws Exception {

	FSM_obj_obj fsm = new FSM_obj_obj();
	createDefaultParametricMonitorWithAwareComponents(fsm.fsm, 1);

	// exercise
	Object object = new Object();
	LowLevelBinding[] bindings = bindingStore.getBindings(array(object));
	Node node = nodeStore.getOrCreateNode(bindings);

	// verify
	assertEquals(node, nodeStore.getNode(bindings));
    }

    @Test
    public void aliveBinding_bindingIsPersistent() throws Exception {

	FSM_obj_obj fsm = new FSM_obj_obj();
	createDefaultParametricMonitorWithAwareComponents(fsm.fsm, 1);

	// exercise
	Object object = new Object();
	LowLevelBinding[] bindings = bindingStore.getBindings(array(object));
	nodeStore.getOrCreateNode(bindings);

	// verify
	assertEquals(1, bindingStore.size());
    }

}
