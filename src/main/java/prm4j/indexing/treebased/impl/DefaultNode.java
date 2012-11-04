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
package prm4j.indexing.treebased.impl;

import prm4j.indexing.AbstractBaseMonitor;
import prm4j.indexing.treebased.MonitorSet;
import prm4j.indexing.treebased.Node;
import prm4j.indexing.treebased.NodeMap;
import prm4j.logic.treebased.MetaNode;
import prm4j.logic.treebased.NodePrototype;

public class DefaultNode implements Node, NodePrototype {

    private final MetaNode metaNode;
    private final MonitorSet[] monitorSets;

    private NodeMap nodeMap;
    private AbstractBaseMonitor monitor;

    public DefaultNode(MetaNode nodeContext, int monitorSetCount) {
	super();
	metaNode = nodeContext;
	monitorSets = new MonitorSet[monitorSetCount];
    }

    @Override
    public MetaNode getNodeContext() {
	return metaNode;
    }

    @Override
    public AbstractBaseMonitor getMonitor() {
	return monitor;
    }

    @Override
    public void setMonitor(AbstractBaseMonitor monitor) {
	this.monitor = monitor;
    }

    @Override
    public NodeMap getNodeMap() {
	// lazy creation
	if (nodeMap == null) {
	    nodeMap = metaNode.createNodeMap();
	}
	return nodeMap;
    }

    @Override
    public MonitorSet getMonitorSet(int parameterSetId) {
	// lazy creation
	MonitorSet monitorSet = monitorSets[parameterSetId];
	if (monitorSet == null) {
	    monitorSet = metaNode.createMonitorSet();
	    monitorSets[parameterSetId] = monitorSet;
	}
	return monitorSet;
    }

    @Override
    public Node next() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public Node clonePrototype() {
	try {
	    return (Node) this.clone();
	} catch (CloneNotSupportedException e) {
	    throw new IllegalStateException(e);
	}
    }

    @Override
    public MonitorSet[] getMonitorSets() {
	return monitorSets;
    }

}
