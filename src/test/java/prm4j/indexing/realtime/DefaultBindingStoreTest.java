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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static prm4j.Util.map;
import static prm4j.Util.tuple;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.junit.Test;

import prm4j.AbstractTest;
import prm4j.api.fsm.FSM;
import prm4j.api.fsm.FSMSpec;
import prm4j.spec.FiniteSpec;

public class DefaultBindingStoreTest extends AbstractTest {

    // cleaning interval = 1 => check for expired bindings on each 'getBindings'
    DefaultBindingStore bs;

    @Test
    public void createBinding_sizeIncreases() throws Exception {
	FSM_obj_obj fsm = new FSM_obj_obj();
	createBindingStore(fsm.fsm, 1);
	Object object = new Object();
	assertEquals(0, bs.size());

	// exercise
	bs.getBindings(array(object));

	// verify
	assertEquals(1, bs.size());
    }

    @Test
    public void createBinding_bindingPersists() throws Exception {
	FSM_obj_obj fsm = new FSM_obj_obj();
	createBindingStore(fsm.fsm, 1);
	Object object = new Object();

	// exercise
	LowLevelBinding[] bindings = bs.getBindings(array(object));

	// verify
	assertArrayEquals(bindings, array(bs.getBinding(object)));
    }

    @Test
    public void removeBinding_sizeHasDiminished() throws Exception {
	FSM_obj_obj fsm = new FSM_obj_obj();
	createBindingStore(fsm.fsm, 1);
	Object object = new Object();

	// exercise
	LowLevelBinding[] bindings = bs.getBindings(array(object));
	bs.removeBinding(bindings[0]);

	// verify
	assertEquals(0, bs.size());
    }

    @Test
    public void unreachableBoundObject_boundObjectIsNullInBinding() throws Exception {
	FSM_obj_obj fsm = new FSM_obj_obj();
	createBindingStore(fsm.fsm, 1);

	// exercise
	Object object = new Object();
	LowLevelBinding[] bindings = bs.getBindings(array(object));
	object = null;

	runGarbageCollectorAFewTimes();

	// verify
	assertNull(bindings[0].get());
    }

    @Test
    public void unreachableBoundObject_bindingIsEnqueuedInReferenceQueue() throws Exception {
	FSM_obj_obj fsm = new FSM_obj_obj();
	createBindingStore(fsm.fsm, 1);

	// exercise
	Object object = new Object();
	LowLevelBinding[] bindings = bs.getBindings(array(object));
	object = null;

	runGarbageCollectorAFewTimes();

	// verify
	assertEquals(bindings[0], bs.getReferenceQueue().poll());
    }

    @Test
    public void getBinding_removeExpiredBindingsNow_bindingGetsCleaned() throws Exception {
	FSM_obj_obj fsm = new FSM_obj_obj();
	createBindingStore(fsm.fsm, 1);

	// exercise
	Object object = new Object();
	bs.getBindings(array(object));

	object = null;
	runGarbageCollectorAFewTimes();

	bs.removeExpiredBindingsNow(); // manually triggers cleanup

	// verify
	assertEquals(0, bs.size());
    }

    @Test
    public void getBinding_getAnotherBinding_twoBindingsPersist() throws Exception {
	FSM_obj_obj fsm = new FSM_obj_obj();
	createBindingStore(fsm.fsm, 1);

	// exercise
	Object object = new Object();
	bs.getBindings(array(object));

	runGarbageCollectorAFewTimes(); // this should do nothing

	Object object2 = new Object();
	bs.getBindings(array(object2)); // get another binding

	// verify
	assertEquals(2, bs.size());
    }

    @Test
    public void getBinding_expirationAndGetAnotherBinding_bindingGetsCleaned() throws Exception {
	FSM_obj_obj fsm = new FSM_obj_obj();
	createBindingStore(fsm.fsm, 1);

	// exercise
	Object object = new Object();
	bs.getBindings(array(object));

	object = null;

	runGarbageCollectorAFewTimes();

	Object object2 = new Object();
	bs.getBindings(array(object2)); // get another binding

	// verify
	assertEquals(1, bs.size()); // one got removed, one still persists
    }

    @Test
    public void getBinding_getBinding_expiration_bindingGetsCleaned() throws Exception {
	FSM_obj_obj fsm = new FSM_obj_obj();
	createBindingStore(fsm.fsm, 1);

	// exercise
	Object object = new Object();
	bs.getBindings(array(object));

	Object object2 = new Object();
	bs.getBindings(array(object2)); // get another binding

	Object object3 = new Object();
	bs.getBindings(array(object3)); // get another binding

	object = null;
	object2 = null;
	runGarbageCollectorAFewTimes();

	bs.getBindings(array(object3)); // get third binding

	// verify
	assertEquals(1, bs.size()); // object and object2 got removed, object3 still persists
    }

    @Test
    public void createFiveBindings_cleanSuccessfulyWithInterval5() throws Exception {
	FSM_obj_obj fsm = new FSM_obj_obj();
	createBindingStore(fsm.fsm, 5); // cleaning interval 5

	// exercise
	bs.getBindings(array(new Object()));
	assertEquals(1, bs.size());
	runGarbageCollectorAFewTimes();

	bs.getBindings(array(new Object()));
	assertEquals(2, bs.size());
	runGarbageCollectorAFewTimes();

	bs.getBindings(array(new Object()));
	assertEquals(3, bs.size());
	runGarbageCollectorAFewTimes();

	bs.getBindings(array(new Object()));
	assertEquals(4, bs.size());
	runGarbageCollectorAFewTimes();

	bs.getBindings(array(new Object()));
	assertEquals(5, bs.size());
	runGarbageCollectorAFewTimes();

	bs.getBindings(array(new Object()));

	// verify
	assertEquals(1, bs.size()); // first 5 objects got removed, last one still persists
    }

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void getBindings_unsafeMapIterator() throws Exception {
	createBindingStore(new FSM_SafeMapIterator().fsm, 1);

	// create bindings
	Map map = map(tuple(1, "a"));
	Collection coll = map.entrySet();
	LowLevelBinding[] bindings = bs.getBindings(array(map, coll, null));

	LowLevelBinding mBinding = bindings[0];
	LowLevelBinding cBinding = bindings[1];

	// verify binding store does not compress bindings
	assertEquals(3, bindings.length);

	// verify bound objects
	assertTrue(map == mBinding.get());
	assertTrue(coll == cBinding.get());
    }

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void removeExpiredBindingsNow_unsafeMapIterator() throws Exception {
	createBindingStore(new FSM_SafeMapIterator().fsm, 1);

	// create bindings
	Map map = map(tuple(1, "a"));
	Collection coll = map.entrySet();
	LowLevelBinding[] bindings = bs.getBindings(array(map, coll, null));

	LowLevelBinding mBinding = bindings[0];
	LowLevelBinding cBinding = bindings[1];

	// register node mocks
	Node mNode = mock(Node.class);
	Node cNode = mock(Node.class);
	mBinding.registerHolder(new NodeRef(mNode, null));
	cBinding.registerHolder(new NodeRef(cNode, null));

	// nullify references
	map = null;
	coll = null;

	runGarbageCollectorAFewTimes();

	// exercise
	bs.removeExpiredBindingsNow();

	// verify
	verify(mNode).remove(mBinding);
	verify(cNode).remove(cBinding);
    }

    private void createBindingStore(FSM fsm, int cleaningInterval) {
	FiniteSpec finiteSpec = new FSMSpec(fsm);
	bs = new DefaultBindingStore(new ArrayBasedBindingFactory(), finiteSpec.getFullParameterSet(), cleaningInterval);
    }

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void getBindingsNoCompression_unsafeMapIterator() throws Exception {
	createBindingStore(new FSM_SafeMapIterator().fsm, 1);

	// create bindings
	Map map = map(tuple(1, "a"));
	Collection coll = map.entrySet();
	LowLevelBinding[] bindings = bs.getBindings(array(map, coll, null));

	LowLevelBinding mBinding = bindings[0];
	LowLevelBinding cBinding = bindings[1];

	// verify no 'compression'
	assertEquals(3, bindings.length);

	// verify bound objects
	assertTrue(map == mBinding.get());
	assertTrue(coll == cBinding.get());
    }

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void getBindingsNoCompression_unsafeMapIterator2() throws Exception {
	createBindingStore(new FSM_SafeMapIterator().fsm, 1);

	// create bindings
	Map map = map(tuple(1, "a"));
	Collection coll = map.entrySet();
	Iterator iter = coll.iterator();
	LowLevelBinding[] bindings = bs.getBindings(array(map, null, iter));

	LowLevelBinding mBinding = bindings[0];
	LowLevelBinding iBinding = bindings[2];

	// verify no 'compression'
	assertEquals(3, bindings.length);

	// verify bound objects
	assertTrue(map == mBinding.get());
	assertTrue(iter == iBinding.get());
    }

}
