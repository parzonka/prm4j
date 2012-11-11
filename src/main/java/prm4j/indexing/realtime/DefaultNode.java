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

import prm4j.indexing.BaseMonitor;
import prm4j.indexing.staticdata.MetaNode;

public class DefaultNode implements Node {

    private final MetaNode metaNode;
    private final MonitorSet[] monitorSets;

    private NodeMap nodeMap;
    private BaseMonitor monitor;

    public DefaultNode(MetaNode metaNode, int monitorSetCount) {
	super();
	this.metaNode = metaNode;
	monitorSets = new MonitorSet[monitorSetCount];
    }

    public DefaultNode(MetaNode metaNode) {
	this.metaNode = metaNode;
	monitorSets = new MonitorSet[metaNode.getMonitorSetCount()];
    }

    @Override
    public MetaNode getMetaNode() {
	return metaNode;
    }

    @Override
    public BaseMonitor getMonitor() {
	return monitor;
    }

    @Override
    public void setMonitor(BaseMonitor monitor) {
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
	    monitorSet = new MonitorSet();
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
    public MonitorSet[] getMonitorSets() {
	return monitorSets;
    }

}
