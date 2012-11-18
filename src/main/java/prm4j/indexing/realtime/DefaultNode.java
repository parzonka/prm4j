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

import prm4j.Util;
import prm4j.indexing.BaseMonitor;
import prm4j.indexing.staticdata.MetaNode;

public class DefaultNode extends AbstractNode {

    private final MetaNode metaNode;
    private final MonitorSet[] monitorSets;
    private BaseMonitor monitor;

    /**
     * @param metaNode
     * @param key
     *            may be null, if node is root node
     * @param hashCode
     *            hash code of the key
     */
    public DefaultNode(MetaNode metaNode, LowLevelBinding key, int hashCode) {
	super(key, hashCode);
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
    public Node getNode(LowLevelBinding binding) {
	return get(binding);
    }

    @Override
    public Node getNodeNonCreative(LowLevelBinding binding) {
	return getNonCreative(binding, binding.hashCode());
    }

    @Override
    public void setMonitor(BaseMonitor monitor) {
	this.monitor = monitor;
    }

    @Override
    public MonitorSet getMonitorSet(int monitorSetId) {
	// lazy creation
	MonitorSet monitorSet = monitorSets[monitorSetId];
	if (monitorSet == null) {
	    monitorSet = new MonitorSet();
	    monitorSets[monitorSetId] = monitorSet;
	}
	return monitorSet;
    }

    @Override
    public MonitorSet[] getMonitorSets() {
	return monitorSets;
    }

    @Override
    public String toString() {
	return monitor == null ? "(..., " + getKey() + ")" : Util.bindingsToString(monitor.getLowLevelBindings());
    }

}
