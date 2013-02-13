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
import static org.mockito.Mockito.mock;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import prm4j.AbstractTest;
import prm4j.api.fsm.FSM;
import prm4j.api.fsm.FSMSpec;
import prm4j.indexing.BaseMonitor;
import prm4j.indexing.StatefulMonitor;
import prm4j.indexing.staticdata.StaticDataConverter;
import prm4j.spec.FiniteParametricProperty;
import prm4j.spec.FiniteSpec;

public class DefaultParametricMonitor_obj_obj_GarbaceCollectionTest extends AbstractTest {

    protected StaticDataConverter converter;
    protected DefaultBindingStore bindingStore;
    protected AwareDefaultNodeStore nodeStore;
    protected BaseMonitor prototypeMonitor;
    protected NodeManager nodeManager;
    protected DefaultParametricMonitor pm;

    public void createDefaultParametricMonitorWithAwareComponents(FSM fsm, int cleaningInterval) {
	FiniteSpec finiteSpec = new FSMSpec(fsm);
	converter = new StaticDataConverter(new FiniteParametricProperty(finiteSpec));
	bindingStore = new DefaultBindingStore(new ArrayBasedBindingFactory(), finiteSpec.getFullParameterSet(),
		cleaningInterval);
	prototypeMonitor = new StatefulMonitor(finiteSpec.getInitialState());
	nodeManager = new NodeManager();
	nodeStore = new AwareDefaultNodeStore(converter.getMetaTree(), nodeManager);
	converter.getMetaTree().setNodeManagerToTree(nodeManager);
	pm = new DefaultParametricMonitor(bindingStore, nodeStore, prototypeMonitor, converter.getEventContext(),
		nodeManager, true);
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
    public void aliveBinding_oneNodeIsStoredInRootnode() throws Exception {

	FSM_obj_obj fsm = new FSM_obj_obj();
	createDefaultParametricMonitorWithAwareComponents(fsm.fsm, 1);

	// exercise
	Object object = new Object();
	LowLevelBinding[] bindings = bindingStore.getBindings(array(object));
	nodeStore.getOrCreateNode(bindings);

	// verify
	assertEquals(1, nodeStore.getRootNode().size());
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

    @Test
    public void expiredBinding_bindingIsRemovedFromBindingStore() throws Exception {

	FSM_obj_obj fsm = new FSM_obj_obj();
	createDefaultParametricMonitorWithAwareComponents(fsm.fsm, 1);

	// exercise
	Object object = new Object();
	LowLevelBinding[] bindings = bindingStore.getBindings(array(object));
	nodeStore.getOrCreateNode(bindings);
	object = null;
	runGarbageCollectorAFewTimes();
	bindingStore.getBindings(array(new Object())); // object is collected, second object is added

	// verify
	assertEquals(1, bindingStore.size()); // second object persists
    }

    @Test
    public void expiredBinding_nodeIsRemovedFromNodeStore() throws Exception {

	FSM_obj_obj fsm = new FSM_obj_obj();
	createDefaultParametricMonitorWithAwareComponents(fsm.fsm, 1);

	// exercise
	Object object = new Object();
	LowLevelBinding[] bindings = bindingStore.getBindings(array(object));
	nodeStore.getOrCreateNode(bindings);
	assertEquals(1, nodeStore.getRootNode().size()); // node is stored

	object = null;
	runGarbageCollectorAFewTimes();
	bindingStore.getBindings(array(new Object())); // object is collected, second object is added

	// verify
	assertEquals(0, nodeStore.getRootNode().size()); // node was removed
    }

    @Test
    public void expiredBinding_NodeManagerGCsMonitorsWhichAreInADeadState() throws Exception {

	FSM_obj_obj fsm = new FSM_obj_obj();
	createDefaultParametricMonitorWithAwareComponents(fsm.fsm, 1);

	// precondition
	assertEquals(0L, nodeManager.getOrphanedMonitorsCount()); // no orphans seen yet

	// exercise
	Object object = new Object();
	LowLevelBinding[] bindings = bindingStore.getBindings(array(object));
	// set the node to a node which will never reach an accepting state.
	nodeStore.getOrCreateNode(bindings).getNodeRef().monitor = new StatefulMonitor(null);
	assertEquals(1, nodeStore.getRootNode().size()); // node is stored

	object = null;
	runGarbageCollectorAFewTimes();
	bindingStore.getBindings(array(new Object())); // object is collected, second object is added

	// verify
	assertEquals(0, nodeStore.getRootNode().size()); // node was removed
	runGarbageCollectorAFewTimes();
	nodeManager.reallyClean(); // trigger cleaning
	assertEquals(1L, nodeManager.getOrphanedMonitorsCount()); // monitor was consider an orphan
	assertEquals(1L, nodeManager.getCollectedMonitorsCount()); // monitor was gc'ed
    }

    @Test
    public void expiredBinding_NodeManagerWillNoGCmonitorsThatMayReachAnAcceptingState() throws Exception {

	FSM_obj_obj fsm = new FSM_obj_obj();
	createDefaultParametricMonitorWithAwareComponents(fsm.fsm, 1);

	// precondition
	assertEquals(0L, nodeManager.getOrphanedMonitorsCount()); // no orphans seen yet
	Object object = new Object();
	LowLevelBinding[] bindings = bindingStore.getBindings(array(object));

	// we will hold the nodeRef to simulate a reference from a monitor set
	final NodeRef nodeRef = nodeStore.getOrCreateNode(bindings).getNodeRef();
	assertEquals(1, nodeStore.getRootNode().size()); // verify: node is stored

	object = null; // nullification of node will allow node removal creating an orphaned monitor
	runGarbageCollectorAFewTimes();
	bindingStore.getBindings(array(new Object())); // object is collected, second object is added
	assertEquals(0, nodeStore.getRootNode().size()); // verify: node was removed

	// we have to fake the NodeManager, that we still have a stored binding, although the original object was
	// collected already. We therefor create fakeBindings with a fakeObject.
	final Object fakeObject = new Object();
	final LowLevelBinding[] fakeBindings = new LowLevelBinding[1];
	fakeBindings[0] = new ArrayBasedBinding(fakeObject, 42, null, 1);
	nodeRef.monitor = prototypeMonitor.copy(fakeBindings);
	// we also need a correct metanode, so that the accepting-state-test is performed correctly
	nodeRef.monitor.setMetaNode(nodeStore.getRootNode().getMetaNode().getMetaNode(fsm.p1));

	// verify
	runGarbageCollectorAFewTimes();
	nodeManager.reallyClean(); // trigger cleaning
	assertEquals(1L, nodeManager.getOrphanedMonitorsCount()); // monitor was considered an orphan
	assertEquals(0L, nodeManager.getCollectedMonitorsCount()); // monitor was *not* gc'ed, since the monitor thinks
								   // it still can reach an accepting state (due to the
								   // fakeBindings).
    }

    @Test
    public void fiveExpiredBindings_nodesAreRemovedFromNodeStore() throws Exception {

	FSM_obj_obj fsm = new FSM_obj_obj();
	createDefaultParametricMonitorWithAwareComponents(fsm.fsm, 5);

	// exercise
	nodeStore.getOrCreateNode(bindingStore.getBindings(array(new Object())));
	assertEquals(1, nodeStore.getRootNode().size()); // node is stored

	nodeStore.getOrCreateNode(bindingStore.getBindings(array(new Object())));
	assertEquals(2, nodeStore.getRootNode().size()); // node is stored

	nodeStore.getOrCreateNode(bindingStore.getBindings(array(new Object())));
	assertEquals(3, nodeStore.getRootNode().size()); // node is stored
	runGarbageCollectorAFewTimes(); // should do nothing

	nodeStore.getOrCreateNode(bindingStore.getBindings(array(new Object())));
	assertEquals(4, nodeStore.getRootNode().size()); // node is stored

	nodeStore.getOrCreateNode(bindingStore.getBindings(array(new Object())));
	assertEquals(5, nodeStore.getRootNode().size()); // node is stored
	runGarbageCollectorAFewTimes();

	nodeStore.getOrCreateNode(bindingStore.getBindings(array(new Object()))); // triggers cleanup in nodeStore

	// verify
	assertEquals(1, nodeStore.getRootNode().size()); // first 5 nodes are removed, last node persists
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void twoEvents_expiredBindings_unsafeMapIterator_allCleaned() throws Exception {

	FSM_SafeMapIterator fsm = new FSM_SafeMapIterator();
	createDefaultParametricMonitorWithAwareComponents(fsm.fsm, 1);

	Map map = mock(Map.class);
	Collection collection = mock(Collection.class);
	Iterator iterator = mock(Iterator.class);

	// exercise
	pm.processEvent(fsm.createColl.createEvent(map, collection));
	pm.processEvent(fsm.createIter.createEvent(collection, iterator));

	assertEquals(6, nodeStore.getCreatedNodes().size()); // six nodes are created

	map = null;
	collection = null;
	iterator = null;

	runGarbageCollectorAFewTimes(); // bindings reference null
	bindingStore.removeExpiredBindingsNow(); // bindings remove themselves from the nodes
	runGarbageCollectorAFewTimes(); // nodes are collected by the system gc
	runGarbageCollectorAFewTimes(); // gc needs more time here in 10% of all runs

	// verify: fails sporadically because of GC nondeterminism, just rerun the test!
	assertEquals(Collections.EMPTY_SET, nodeStore.getCreatedNodes()); // all nodes are deleted

    }

    protected void assertCreatedNodes(Object[]... instances) {
	Set<Node> createdNodes = new HashSet<Node>(nodeStore.getCreatedNodes());
	nodeStore.getCreatedNodes().clear();
	for (Object[] instance : instances) {
	    nodeStore.getOrCreateNode(bindingStore.getBindings(instance));
	}
	assertEquals(nodeStore.getCreatedNodes(), createdNodes);
    }
}
