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

import prm4j.indexing.staticdata.MetaNode;

public class DefaultNodeStore implements NodeStore {

    private final Node rootNode;

    public DefaultNodeStore(MetaNode metaTree) {
	rootNode = metaTree.createRootNode();
    }

    @Override
    public Node getNode(LowLevelBinding[] bindings) {
	Node node = getRootNode();
	// we iterate over the rest { node1 , ..., nodeN }, traversing the tree
	for (int i = 0; i < bindings.length; i++) {
	    // traverse the node tree until the parameter instance is fully realized
	    node = node.getNode(bindings[i]);
	}
	return node;
    }

    @Override
    public Node getNode(LowLevelBinding[] bindings, int[] parameterMask) {
	Node node = getRootNode();
	// we iterate over the rest { node1 , ..., nodeN }, traversing the tree
	for (int i = 0; i < parameterMask.length; i++) {
	    // traverse the node tree until the parameter instance is fully realized
	    node = node.getNode(bindings[parameterMask[i]]);
	}
	return node;
    }

    @Override
    public Node getNodeNonCreative(LowLevelBinding[] bindings) {
	Node node = getRootNode();
	// we iterate over the rest { node1 , ..., nodeN }, traversing the tree
	for (int i = 0; i < bindings.length; i++) {
	    // traverse the node tree until the parameter instance is fully realized
	    node = node.getNode(bindings[i]);
	    if (node == null) {
		return null;
	    }
	}
	return node;
    }

    @Override
    public Node getNodeNonCreative(LowLevelBinding[] bindings, int[] parameterMask) {
	Node node = getRootNode();
	// we iterate over the rest { node1 , ..., nodeN }, traversing the tree
	for (int i = 0; i < parameterMask.length; i++) {
	    node = node.getNodeNonCreative(bindings[parameterMask[i]]);
	    if (node == null) {
		return null;
	    }
	}
	return node;
    }

    public Node getRootNode() {
	return rootNode;
    }

}
