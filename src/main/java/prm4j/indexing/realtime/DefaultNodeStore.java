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

    private final MetaNode metaTree;

    private Node rootNode;

    public DefaultNodeStore(MetaNode metaTree) {
	this.metaTree = metaTree;
	rootNode = metaTree.createRootNode();
    }

    @Override
    public Node getOrCreateNode(LowLevelBinding[] bindings) {
	Node node = rootNode;
	// we iterate over the rest { node1 , ..., nodeN }, traversing the tree
	for (int i = 0; i < bindings.length; i++) {
	    // traverse the node tree until the parameter instance is fully realized
	    node = node.getOrCreateNode(i, bindings[i]);
	}
	return node;
    }

    @Override
    public Node getOrCreateNode(LowLevelBinding[] bindings, int[] parameterMask) {
	Node node = rootNode;
	// we iterate over the rest { node1 , ..., nodeN }, traversing the tree
	for (int i = 0; i < parameterMask.length; i++) {
	    // traverse the node tree until the parameter instance is fully realized
	    node = node.getOrCreateNode(parameterMask[i], bindings[parameterMask[i]]);
	}
	return node;
    }

    @Override
    public Node getNode(LowLevelBinding[] bindings) {
	Node node = rootNode;
	// we iterate over the rest { node1 , ..., nodeN }, traversing the tree
	for (int i = 0; i < bindings.length; i++) {
	    // traverse the node tree until the parameter instance is fully realized
	    node = node.getNode(i, bindings[i]);
	    if (node == null) {
		return NullNode.instance;
	    }
	}
	return node;
    }

    @Override
    public Node getNode(LowLevelBinding[] bindings, int[] parameterMask) {
	Node node = rootNode;
	// we iterate over the rest { node1 , ..., nodeN }, traversing the tree
	for (int i = 0; i < parameterMask.length; i++) {
	    node = node.getNode(parameterMask[i], bindings[parameterMask[i]]);
	    if (node == null) {
		return NullNode.instance;
	    }
	}
	return node;
    }

    public Node getRootNode() {
	return rootNode;
    }

    @Override
    public void reset() {
	rootNode = metaTree.createRootNode();
    }

}
