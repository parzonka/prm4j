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
package prm4j.logic;

import prm4j.indexing.treebased.MonitorSet;
import prm4j.indexing.treebased.Node;
import prm4j.indexing.treebased.NodeMap;

public class DefaultNodeContext<A> implements NodeContext<A> {

    private NodeContext<A>[] successors;
    private NodePrototype<A> nodePrototype;

    public DefaultNodeContext(NodeContext<A>[] successors, NodePrototype<A> nodePrototype) {
	super();
	this.successors = successors;
	this.nodePrototype = nodePrototype;
    }

    @Override
    public ChainingData[] getChainingData() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public MonitorSet<A> createMonitorSet() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public Node<A> createNode() {
	return nodePrototype.clonePrototype();
    }

    @Override
    public Node<A> createNode(int parameterId) {
	return successors[parameterId].createNode();
    }

    @Override
    public NodeMap<A> createNodeMap() {
	// TODO Auto-generated method stub
	return null;
    }

    public void setSuccessor(int parameterId, NodeContext<A> nodeContext) {
	this.successors[parameterId] = nodeContext;
    }

    public void setNodePrototype(NodePrototype<A> nodePrototype) {
	this.nodePrototype = nodePrototype;
    }

}
