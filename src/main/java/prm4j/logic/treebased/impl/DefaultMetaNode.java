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
package prm4j.logic.treebased.impl;

import prm4j.indexing.treebased.MonitorSet;
import prm4j.indexing.treebased.Node;
import prm4j.indexing.treebased.NodeMap;
import prm4j.logic.treebased.ChainingData;
import prm4j.logic.treebased.MetaNode;
import prm4j.logic.treebased.NodePrototype;

/**
 * @param <E>
 *            the type of base event processed by monitors
 */
public class DefaultMetaNode<E> implements MetaNode<E> {

    private MetaNode<E>[] successors;
    private NodePrototype<E> nodePrototype;
    private ChainingData[] chainingData;

    public DefaultMetaNode(MetaNode<E>[] successors, NodePrototype<E> nodePrototype) {
	super();
	this.successors = successors;
	this.nodePrototype = nodePrototype;
    }

    @Override
    public ChainingData[] getChainingData() {
	return chainingData;
    }

    @Override
    public MonitorSet<E> createMonitorSet() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public Node<E> createNode() {
	return nodePrototype.clonePrototype();
    }

    @Override
    public Node<E> createNode(int parameterId) {
	return successors[parameterId].createNode();
    }

    @Override
    public NodeMap<E> createNodeMap() {
	// TODO Auto-generated method stub
	return null;
    }

    public void setSuccessor(int parameterId, MetaNode<E> nodeContext) {
	this.successors[parameterId] = nodeContext;
    }

    public void setNodePrototype(NodePrototype<E> nodePrototype) {
	this.nodePrototype = nodePrototype;
    }

    public void setChainingData(ChainingData[] chainingData) {
	this.chainingData = chainingData;
    }

}
