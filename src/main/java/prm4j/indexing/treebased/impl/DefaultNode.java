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
import prm4j.logic.MetaNode;
import prm4j.logic.NodePrototype;

public class DefaultNode<A> implements Node<A>, NodePrototype<A> {

    private final MetaNode<A> metaNode;
    private final MonitorSet<A>[] monitorSets;

    private NodeMap<A> nodeMap;
    private AbstractBaseMonitor<A> monitor;

    @SuppressWarnings("unchecked")
    public DefaultNode(MetaNode<A> nodeContext, int monitorSetCount) {
	super();
	this.metaNode = nodeContext;
	this.monitorSets = new MonitorSet[monitorSetCount];
    }

    @Override
    public MetaNode<A> getNodeContext() {
	return metaNode;
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
	    nodeMap = metaNode.createNodeMap();
	}
	return nodeMap;
    }

    @Override
    public MonitorSet<A> getMonitorSet(int parameterSetId) {
	// lazy creation
	MonitorSet<A> monitorSet = monitorSets[parameterSetId];
	if (monitorSet == null) {
	    monitorSet = metaNode.createMonitorSet();
	    monitorSets[parameterSetId] = monitorSet;
	}
	return monitorSet;
    }

    @Override
    public Node<A> next() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Node<A> clonePrototype() {
	try {
	    return (Node<A>) this.clone();
	} catch (CloneNotSupportedException e) {
	    throw new IllegalStateException(e);
	}
    }

}
