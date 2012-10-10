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

/**
 * @param <E>
 *            the type of base event processed by monitors
 */
public class DefaultNode<E> implements Node<E>, NodePrototype<E> {

    private final MetaNode<E> metaNode;
    private final MonitorSet<E>[] monitorSets;

    private NodeMap<E> nodeMap;
    private AbstractBaseMonitor<E> monitor;

    @SuppressWarnings("unchecked")
    public DefaultNode(MetaNode<E> nodeContext, int monitorSetCount) {
	super();
	this.metaNode = nodeContext;
	this.monitorSets = new MonitorSet[monitorSetCount];
    }

    @Override
    public MetaNode<E> getNodeContext() {
	return metaNode;
    }

    @Override
    public AbstractBaseMonitor<E> getMonitor() {
	return monitor;
    }

    @Override
    public void setMonitor(AbstractBaseMonitor<E> monitor) {
	this.monitor = monitor;
    }

    @Override
    public NodeMap<E> getNodeMap() {
	// lazy creation
	if (nodeMap == null) {
	    nodeMap = metaNode.createNodeMap();
	}
	return nodeMap;
    }

    @Override
    public MonitorSet<E> getMonitorSet(int parameterSetId) {
	// lazy creation
	MonitorSet<E> monitorSet = monitorSets[parameterSetId];
	if (monitorSet == null) {
	    monitorSet = metaNode.createMonitorSet();
	    monitorSets[parameterSetId] = monitorSet;
	}
	return monitorSet;
    }

    @Override
    public Node<E> next() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Node<E> clonePrototype() {
	try {
	    return (Node<E>) this.clone();
	} catch (CloneNotSupportedException e) {
	    throw new IllegalStateException(e);
	}
    }

}
