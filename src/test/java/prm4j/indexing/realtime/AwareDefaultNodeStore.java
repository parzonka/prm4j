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

import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.Deque;

import prm4j.indexing.staticdata.MetaNode;

/**
 * {@link DefaultNodeStore} which is aware of all nodes it produces and retrieves.
 */
public class AwareDefaultNodeStore extends DefaultNodeStore {

    private final Deque<WeakReference<Node>> retrievedNodes;

    public AwareDefaultNodeStore(MetaNode metaTree) {
	super(metaTree);
	retrievedNodes = new ArrayDeque<WeakReference<Node>>();
    }

    @Override
    public Node getNode(LowLevelBinding[] bindings) {
	Node node = super.getNode(bindings);
	getListOfNodes().add(new WeakReference<Node>(node) {
	    @Override
	    public String toString() {
		Node node = get();
		return node == null ? "[collected reference]" : node.toString();
	    }
	});
	return node;
    }

    @Override
    public Node getNode(LowLevelBinding[] bindings, int[] parameterMask) {
	Node node = super.getNode(bindings, parameterMask);
	getListOfNodes().add(new WeakReference<Node>(node));
	return node;
    }

    public Deque<WeakReference<Node>> getListOfNodes() {
	return retrievedNodes;
    }

}
