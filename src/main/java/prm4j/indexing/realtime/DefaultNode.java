/*
 * Copyright (c) 2012, 2013 Mateusz Parzonka
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Mateusz Parzonka - initial API and implementation
 */
package prm4j.indexing.realtime;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

import prm4j.Util;
import prm4j.api.Parameter;
import prm4j.indexing.Monitor;
import prm4j.indexing.staticdata.MetaNode;

public class DefaultNode extends AbstractNode {

    private final MetaNode metaNode;
    private final MonitorSet[] monitorSets;
    private final NodeRef nodeRef;
    private long timestamp = Long.MIN_VALUE; // this instance was not seen yet

    private WeakReference<Node> cachedNodeRef = null;
    private LowLevelBinding cachedBinding = null;
    private int cachedParameterIndex = -1;

    /**
     * @param metaNode
     * @param parameterIndex
     * @param key
     * @param refQueue
     */
    public DefaultNode(MetaNode metaNode, int parameterIndex, LowLevelBinding key, ReferenceQueue<Node> refQueue) {
	super(key);
	this.metaNode = metaNode;
	monitorSets = new MonitorSet[metaNode.getMonitorSetCount()];
	nodeRef = new NodeRef(this, refQueue);
    }

    /**
     * Creates a node without a reference queue. Call this to create a root node that will never garbage collected.
     * 
     * @param metaNode
     * @param parameterIndex
     * @param key
     */
    public DefaultNode(MetaNode metaNode, int parameterIndex, LowLevelBinding key) {
	super(key);
	this.metaNode = metaNode;
	monitorSets = new MonitorSet[metaNode.getMonitorSetCount()];
	nodeRef = new NodeRef(this, null);
    }

    @Override
    public MetaNode getMetaNode() {
	return metaNode;
    }

    @Override
    public Monitor getMonitor() {
	return nodeRef.monitor;
    }

    @Override
    public Node getOrCreateNode(int parameterIndex, LowLevelBinding binding) {
	binding.registerHolder(nodeRef);
	if (cachedParameterIndex != parameterIndex || cachedBinding != binding) {
	    cachedParameterIndex = parameterIndex;
	    cachedBinding = binding;
	    cachedNodeRef = getOrCreate(parameterIndex, binding).getNodeRef();
	}
	return cachedNodeRef.get();
    }

    @Override
    public Node getNode(int parameterIndex, LowLevelBinding binding) {
	if (cachedParameterIndex != parameterIndex || cachedBinding != binding) {
	    final Node node = get(parameterIndex, binding);
	    if (node != null) {
		cachedParameterIndex = parameterIndex;
		cachedBinding = binding;
		cachedNodeRef = node.getNodeRef();
		return node;
	    }
	    return null;
	}
	return cachedNodeRef.get();
    }

    @Override
    public void setMonitor(Monitor monitor) {
	nodeRef.monitor = monitor;
	monitor.setMetaNode(metaNode);
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
	if (nodeRef.monitor != null) {
	    // output e.g.: (p2=b, p3=c, p5=e)
	    return Util.bindingsToString(nodeRef.monitor.getCompressedBindings());
	}
	// output e.g.: (p2=?, p3=?, p5=e) because we only now the key and the node parameter set
	final StringBuilder sb = new StringBuilder();
	sb.append("(");
	int i = 0;
	final int max = metaNode.getNodeParameterList().size();
	for (Parameter<?> parameter : metaNode.getNodeParameterList()) {
	    if (++i < max) {
		sb.append(parameter).append("=?, ");
	    } else {
		break;
	    }
	}
	final String key = getKey() == null ? "" : getKey().toString();
	sb.append(key).append(")");
	return sb.toString();
    }

    @Override
    public NodeRef getNodeRef() {
	return nodeRef;
    }

    @Override
    public int parameterIndex() {
	return metaNode.getLastParameterIndex();
    }

    @Override
    public void setTimestamp(long timestamp) {
	this.timestamp = timestamp;

    }

    @Override
    public long getTimestamp() {
	return timestamp;
    }

}
