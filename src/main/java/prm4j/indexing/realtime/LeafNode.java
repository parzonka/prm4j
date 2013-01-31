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

import java.lang.ref.ReferenceQueue;

import prm4j.Util;
import prm4j.api.Parameter;
import prm4j.indexing.BaseMonitor;
import prm4j.indexing.staticdata.MetaNode;

public class LeafNode implements Node {

    private final static MonitorSet[] EMPTY_MONITOR_SET = new MonitorSet[0];
    private final MetaNode metaNode;
    private final NodeRef nodeRef;

    private final LowLevelBinding key;
    private Node nextNode;

    /**
     * @param metaNode
     * @param key
     *            may be null, if node is root node
     * @param hashCode
     *            hash code of the key
     */
    public LeafNode(MetaNode metaNode, LowLevelBinding key, ReferenceQueue<Node> refQueue) {
	this.metaNode = metaNode;
	this.key = key;
	nodeRef = new NodeRef(this, refQueue);
    }

    @Override
    public MetaNode getMetaNode() {
	return metaNode;
    }

    @Override
    public BaseMonitor getMonitor() {
	return nodeRef.monitor;
    }

    @Override
    public Node getOrCreateNode(int parameterIndex, LowLevelBinding binding) {
	throw new UnsupportedOperationException("This node should not need to use this operation!");
    }

    @Override
    public Node getNode(int parameterIndex, LowLevelBinding binding) {
	throw new UnsupportedOperationException("This node should not need to use this operation!");
    }

    @Override
    public void setMonitor(BaseMonitor monitor) {
	nodeRef.monitor = monitor;
	monitor.setMetaNode(metaNode);
    }

    @Override
    public MonitorSet getMonitorSet(int monitorSetId) {
	throw new UnsupportedOperationException("This node should not need to use this operation!");
    }

    @Override
    public MonitorSet[] getMonitorSets() {
	return EMPTY_MONITOR_SET;
    }

    @Override
    public String toString() {
	if (nodeRef.monitor != null) {
	    // output e.g.: (p2=b, p3=c, p5=e)
	    return Util.bindingsToString(nodeRef.monitor.getLowLevelBindings());
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
    public LowLevelBinding getKey() {
	return key;
    }

    @Override
    public Node next() {
	return nextNode;
    }

    @Override
    public void setNext(Node nextEntry) {
	nextNode = nextEntry;

    }

    @Override
    public void remove(LowLevelBinding binding) {
	throw new UnsupportedOperationException("This node should not need to use this operation!");

    }

    @Override
    public int size() {
	throw new UnsupportedOperationException("This node should not need to use this operation!");
    }

}
