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
import static org.junit.Assert.assertNotSame;

import org.junit.Before;
import org.junit.Test;

import prm4j.AbstractTest;
import prm4j.api.fsm.FSMSpec;
import prm4j.indexing.staticdata.StaticDataConverter;
import prm4j.spec.FiniteParametricProperty;
import prm4j.spec.FiniteSpec;

public class DefaultNodeStoreTest extends AbstractTest {

    FSM_a_a_a fsm;
    FiniteSpec finiteSpec;
    StaticDataConverter converter;
    DefaultNodeStore nodeStore;
    LowLevelBinding[] bindings;

    @Before
    public void init() throws Exception {
	fsm = new FSM_a_a_a();
	finiteSpec = new FSMSpec(fsm.fsm);
	converter = new StaticDataConverter(new FiniteParametricProperty(finiteSpec));
	nodeStore = new DefaultNodeStore(converter.getMetaTree());
	bindings = new LowLevelBinding[1];
	bindings[0] = new DefaultLowLevelBinding("a", fsm.p1, 42, null);
    }

    @Test
    public void getNode_nonExistingNodeIsNullNode() throws Exception {
	assertEquals(NullNode.instance, nodeStore.getNode(bindings));
    }

    @Test
    public void getOrCreateNode_createNewNode() throws Exception {
	assertNotSame(NullNode.instance, nodeStore.getOrCreateNode(bindings));
    }

    @Test
    public void getOrCreateNode_getNonCreativeReturnsStoredNode() throws Exception {
	Node node1 = nodeStore.getOrCreateNode(bindings);
	Node node2 = nodeStore.getNode(bindings);
	assertEquals(node1, node2);
    }

}
