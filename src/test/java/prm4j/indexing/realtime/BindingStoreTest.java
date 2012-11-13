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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import prm4j.AbstractTest;
import prm4j.api.fsm.FSM;
import prm4j.api.fsm.FSMSpec;
import prm4j.spec.FiniteSpec;

public class BindingStoreTest extends AbstractTest {

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void getBindings_unsafeMapIterator_1() throws Exception {
	FSM_unsafeMapIterator u = new FSM_unsafeMapIterator();
	FSM fsm = u.fsm;
	FiniteSpec finiteSpec = new FSMSpec(fsm);
	u.m.setIndex(0);
	u.c.setIndex(1);
	u.i.setIndex(2);

	BindingStore bs = new BindingStore(finiteSpec.getFullParameterSet(), 1);

	Object[] boundObjects = new Object[finiteSpec.getFullParameterSet().size()];
	Map map = new HashMap();
	map.put(1, "a");
	Collection coll = map.entrySet();
	boundObjects[0] = map;
	boundObjects[1] = coll;

	LowLevelBinding[] bindings = bs.getBindings(boundObjects);
	LowLevelBinding mBinding = bindings[0];
	LowLevelBinding cBinding = bindings[1];

	// check 'compression'
	assertEquals(2, bindings.length);

	// check bound objects
	assertTrue(map == mBinding.get());
	assertTrue(coll == cBinding.get());
	// check parameter index
	assertEquals(0, mBinding.getParameterId());
	assertEquals(1, cBinding.getParameterId());
    }

}
