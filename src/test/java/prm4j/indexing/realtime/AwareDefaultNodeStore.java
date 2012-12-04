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
import java.util.Collections;
import java.util.Deque;
import java.util.Set;
import java.util.WeakHashMap;

import prm4j.indexing.staticdata.MetaNode;

/**
 * {@link DefaultNodeStore} which is aware of all nodes it produces and retrieves.
 */
public class AwareDefaultNodeStore extends DefaultNodeStore {

    private final Deque<WeakReference<Node>> retrievedNodes;
    private final Set<Node> createdNodes; // weak set

    public AwareDefaultNodeStore(MetaNode metaTree) {
	super(metaTree);
	retrievedNodes = new ArrayDeque<WeakReference<Node>>();
	createdNodes = Collections.newSetFromMap(new WeakHashMap<Node, Boolean>());
    }

    @Override
    public Node getOrCreateNode(LowLevelBinding[] bindings) {
	Node node = getRootNode();
	for (int i = 0; i < bindings.length; i++) {
	    node = node.getOrCreateNode(i, bindings[i]);
	    createdNodes.add(node);
	}
	addToRetrievedNodes(node);
	return node;
    }

    @Override
    public Node getOrCreateNode(LowLevelBinding[] bindings, int[] parameterMask) {
	Node node = getRootNode();
	for (int i = 0; i < parameterMask.length; i++) {
	    node = node.getOrCreateNode(parameterMask[i], bindings[parameterMask[i]]);
	    createdNodes.add(node);
	}
	addToRetrievedNodes(node);
	return node;
    }

    public Deque<WeakReference<Node>> getRetrievedNodes() {
	return retrievedNodes;
    }

    private void addToRetrievedNodes(Node node) {
	retrievedNodes.add(new WeakReference<Node>(node) {
	    @Override
	    public String toString() {
		Node node = get();
		return node == null ? "[collected reference]" : node.toString();
	    }
	});
    }

    public Node getNode(Set<LowLevelBinding> setOfBindings) {
	return super.getNode(prm4j.Util.asSortedList(setOfBindings).toArray(new LowLevelBinding[0]));
    }

    public Set<Node> getCreatedNodes() {
	return createdNodes;
    }

}
