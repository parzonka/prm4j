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
import static org.junit.Assert.assertNotSame;

import org.junit.Before;
import org.junit.Test;

import prm4j.AbstractTest;
import prm4j.api.fsm.FSMSpec;
import prm4j.indexing.binding.ArrayBasedBinding;
import prm4j.indexing.binding.Binding;
import prm4j.indexing.model.ParametricPropertyProcessor;
import prm4j.indexing.node.DefaultNodeStore;
import prm4j.indexing.node.Node;
import prm4j.indexing.node.NodeManager;
import prm4j.indexing.node.NullNode;
import prm4j.spec.finite.FiniteParametricProperty;
import prm4j.spec.finite.FiniteSpec;

public class DefaultNodeStoreTest extends AbstractTest {

    FSM_a_a_a fsm;
    FiniteSpec finiteSpec;
    ParametricPropertyProcessor processor;
    DefaultNodeStore nodeStore;
    Binding[] bindings;

    @Before
    public void init() throws Exception {
	fsm = new FSM_a_a_a();
	finiteSpec = new FSMSpec(fsm.fsm);
	processor = new ParametricPropertyProcessor(new FiniteParametricProperty(finiteSpec));
	nodeStore = new DefaultNodeStore(processor.getParameterTree(), new NodeManager());
	bindings = new Binding[1];
	bindings[0] = new ArrayBasedBinding("a", 42, null, 2);
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
    public void getOrCreateNode_get_returnsStoredNode() throws Exception {
	Node node1 = nodeStore.getOrCreateNode(bindings);
	Node node2 = nodeStore.getNode(bindings);
	assertEquals(node1, node2);
    }

}
