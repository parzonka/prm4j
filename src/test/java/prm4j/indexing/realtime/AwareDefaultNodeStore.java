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

import java.util.ArrayDeque;
import java.util.Deque;

import prm4j.indexing.staticdata.MetaNode;

/**
 * {@link DefaultNodeStore} which is aware of all nodes it produces and retrieves.
 */
public class AwareDefaultNodeStore extends DefaultNodeStore {

    private final Deque<Node> retrievedNodes;

    public AwareDefaultNodeStore(MetaNode metaTree) {
	super(metaTree);
	retrievedNodes = new ArrayDeque<Node>();
    }

    @Override
    public Node getNode(LowLevelBinding[] bindings) {
	Node node = super.getNode(bindings);
	getListOfNodes().add(node);
	return node;
    }

    @Override
    public Node getNode(LowLevelBinding[] bindings, int[] parameterMask) {
	Node node = super.getNode(bindings, parameterMask);
	getListOfNodes().add(node);
	return node;
    }

    public Deque<Node> getListOfNodes() {
	return retrievedNodes;
    }

}
