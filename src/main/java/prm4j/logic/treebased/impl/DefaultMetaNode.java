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

import prm4j.indexing.realtime.MonitorSet;
import prm4j.indexing.realtime.Node;
import prm4j.indexing.realtime.NodeMap;
import prm4j.indexing.staticdata.ChainData;
import prm4j.indexing.staticdata.MetaNode;
import prm4j.logic.treebased.NodePrototype;

public class DefaultMetaNode implements MetaNode {

    private MetaNode[] successors;
    private NodePrototype nodePrototype;
    private ChainData[] chainingData;

    public DefaultMetaNode(MetaNode[] successors, NodePrototype nodePrototype) {
	super();
	this.successors = successors;
	this.nodePrototype = nodePrototype;
    }

    @Override
    public ChainData[] getChainData() {
	return chainingData;
    }

    @Override
    public MonitorSet createMonitorSet() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public Node createNode() {
	return nodePrototype.clonePrototype();
    }

    @Override
    public Node createNode(int parameterId) {
	return successors[parameterId].createNode();
    }

    @Override
    public NodeMap createNodeMap() {
	// TODO Auto-generated method stub
	return null;
    }

    public void setSuccessor(int parameterId, MetaNode nodeContext) {
	successors[parameterId] = nodeContext;
    }

    public void setNodePrototype(NodePrototype nodePrototype) {
	this.nodePrototype = nodePrototype;
    }

    public void setChainingData(ChainData[] chainingData) {
	this.chainingData = chainingData;
    }

}
