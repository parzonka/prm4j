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
import prm4j.indexing.map.MinimalMap;
import prm4j.indexing.staticdata.MetaNode;

public class DefaultNode extends MinimalMap<LowLevelBinding, NodeReference> implements Node {

    private final MetaNode metaNode;
    private final MonitorSet[] monitorSets;
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
    public Node getNode(LowLevelBinding binding) {
	return get(binding).get();
    }

    @Override
    public void setMonitor(BaseMonitor monitor) {
	this.monitor = monitor;
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
    public MonitorSet[] getMonitorSets() {
	return monitorSets;
    }

    // Map overrides

    @Override
    protected NodeReference[] createTable(int size) {
	return new NodeReference[size];
    }

    /**
     * {@inheritDoc}
     *
     * Creates a {@link NodeReference} with a weak reference to a newly created {@link Node}. <br>
     * The {@link NodeReference} is also an entry with a binding as key using the same hashcode.
     */
    @Override
    protected NodeReference createEntry(LowLevelBinding binding, int bindingHashCode) {
	return new NodeReference(metaNode.createNode(binding.getParameterId()), binding, bindingHashCode);
    }

}
