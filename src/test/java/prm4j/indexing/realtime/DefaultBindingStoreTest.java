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
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import prm4j.AbstractTest;
import prm4j.api.fsm.FSM;
import prm4j.api.fsm.FSMSpec;
import prm4j.spec.FiniteSpec;

public class DefaultBindingStoreTest extends AbstractTest {

    DefaultBindingStore bs;

    @Test
    public void getBinding_bindingPersists() throws Exception {
	FSM_obj_obj fsm = new FSM_obj_obj();
	setup(fsm.fsm);
	Object object = new Object();

	// exercise
	LowLevelBinding binding = bs.getOrCreateBinding(fsm.p1, object);

	// verify
	assertEquals(binding, bs.getBinding(fsm.p1, object));
    }
    private void setup(FSM fsm) {
	FiniteSpec finiteSpec = new FSMSpec(fsm);
	bs = new DefaultBindingStore(finiteSpec.getFullParameterSet(), 1);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void getBindings_unsafeMapIterator() throws Exception {
	FSM_unsafeMapIterator u = new FSM_unsafeMapIterator();
	FSM fsm = u.fsm;
	FiniteSpec finiteSpec = new FSMSpec(fsm);
	u.m.setIndex(0);
	u.c.setIndex(1);
	u.i.setIndex(2);

	DefaultBindingStore bs = new DefaultBindingStore(finiteSpec.getFullParameterSet(), 1);

	// create bindings
	Object[] boundObjects = new Object[finiteSpec.getFullParameterSet().size()];
	Map map = new HashMap();
	map.put(1, "a");
	Collection coll = map.entrySet();
	boundObjects[0] = map;
	boundObjects[1] = coll;
	LowLevelBinding[] bindings = bs.getBindings(boundObjects);
	LowLevelBinding mBinding = bindings[0];
	LowLevelBinding cBinding = bindings[1];

	// verify 'compression'
	assertEquals(2, bindings.length);

	// verify bound objects
	assertTrue(map == mBinding.get());
	assertTrue(coll == cBinding.get());
	// verify parameter index
	assertEquals(0, mBinding.getParameterIndex());
	assertEquals(1, cBinding.getParameterIndex());
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void removeExpiredBindingsNow_unsafeMapIterator() throws Exception {
	FSM_unsafeMapIterator u = new FSM_unsafeMapIterator();
	FSM fsm = u.fsm;
	FiniteSpec finiteSpec = new FSMSpec(fsm);
	u.m.setIndex(0);
	u.c.setIndex(1);
	u.i.setIndex(2);

	DefaultBindingStore bs = new DefaultBindingStore(finiteSpec.getFullParameterSet(), 1);

	// create bindings
	Object[] boundObjects = new Object[finiteSpec.getFullParameterSet().size()];
	Map map = new HashMap();
	map.put(1, "a");
	Collection coll = map.entrySet();
	boundObjects[0] = map;
	boundObjects[1] = coll;
	LowLevelBinding[] bindings = bs.getBindings(boundObjects);
	LowLevelBinding mBinding = bindings[0];
	LowLevelBinding cBinding = bindings[1];

	// register node mocks
	Node mNode = mock(Node.class);
	Node cNode = mock(Node.class);
	mBinding.registerNode(new WeakReference<Node>(mNode));
	cBinding.registerNode(new WeakReference<Node>(cNode));

	// nullify references
	boundObjects[0] = null;
	boundObjects[1] = null;
	map = null;
	coll = null;

	runGarbageCollectorAFewTimes();

	// exercise
	bs.removeExpiredBindingsNow();

	// verify
	verify(mNode).remove(mBinding);
	verify(cNode).remove(cBinding);

    }

}
