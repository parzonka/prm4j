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
import prm4j.logic.NodeContext;

public class DefaultNode<A> implements Node<A> {

    private final NodeContext<A> nodeContext;
    private final MonitorSet<A>[] monitorSets;

    private NodeMap<A> nodeMap;
    private AbstractBaseMonitor<A> monitor;

    @SuppressWarnings("unchecked")
    public DefaultNode(NodeContext<A> nodeContext, int monitorSetCount) {
	super();
	this.nodeContext = nodeContext;
	this.monitorSets = new MonitorSet[monitorSetCount];
    }

    @Override
    public NodeContext<A> getNodeContext() {
	return nodeContext;
    }

    @Override
    public AbstractBaseMonitor<A> getMonitor() {
	return monitor;
    }

    @Override
    public void setMonitor(AbstractBaseMonitor<A> monitor) {
	this.monitor = monitor;
    }

    @Override
    public NodeMap<A> getNodeMap() {
	// lazy creation
	if (nodeMap == null) {
	    nodeMap = nodeContext.createNodeMap();
	}
	return nodeMap;
    }

    @Override
    public MonitorSet<A> getMonitorSet(int parameterSetId) {
	// lazy creation
	MonitorSet<A> monitorSet = monitorSets[parameterSetId];
	if (monitorSet == null) {
	    monitorSet = nodeContext.createMonitorSet();
	    monitorSets[parameterSetId] = monitorSet;
	}
	return monitorSet;
    }

    @Override
    public Node<A> next() {
	// TODO Auto-generated method stub
	return null;
    }

}
