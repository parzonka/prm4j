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
package prm4j.indexing.node;

import java.lang.ref.ReferenceQueue;

import prm4j.Util;
import prm4j.api.Parameter;
import prm4j.indexing.binding.Binding;
import prm4j.indexing.model.ParameterNode;
import prm4j.indexing.monitor.Monitor;
import prm4j.indexing.monitor.MonitorSet;

public class LeafNode implements Node {

    private final static MonitorSet[] EMPTY_MONITOR_SET = new MonitorSet[0];
    private final ParameterNode parameterNode;
    private final NodeRef nodeRef;
    private long timestamp = Long.MAX_VALUE; // this instance was not seen yet

    private final Binding key;
    private Node nextNode;

    /**
     * @param parameterNode
     * @param key
     *            may be null, if node is root node
     * @param hashCode
     *            hash code of the key
     */
    public LeafNode(ParameterNode parameterNode, Binding key, ReferenceQueue<Node> refQueue) {
	this.parameterNode = parameterNode;
	this.key = key;
	nodeRef = new NodeRef(this, refQueue);
    }

    @Override
    public ParameterNode getParameterNode() {
	return parameterNode;
    }

    @Override
    public Monitor getMonitor() {
	return nodeRef.monitor;
    }

    @Override
    public Node getOrCreateNode(int parameterIndex, Binding binding) {
	throw new UnsupportedOperationException("This node should not need to use this operation!");
    }

    @Override
    public Node getNode(int parameterIndex, Binding binding) {
	throw new UnsupportedOperationException("This node should not need to use this operation!");
    }

    @Override
    public void setMonitor(Monitor monitor) {
	nodeRef.monitor = monitor;
	monitor.setParameterNode(parameterNode);
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
	    return Util.bindingsToString(nodeRef.monitor.getCompressedBindings());
	}
	// output e.g.: (p2=?, p3=?, p5=e) because we only now the key and the node parameter set
	final StringBuilder sb = new StringBuilder();
	sb.append("(");
	int i = 0;
	final int max = parameterNode.getNodeParameterList().size();
	for (Parameter<?> parameter : parameterNode.getNodeParameterList()) {
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
	return parameterNode.getLastParameterIndex();
    }

    @Override
    public Binding getKey() {
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
    public void remove(Binding binding) {
	throw new UnsupportedOperationException("This node should not need to use this operation!");

    }

    @Override
    public int size() {
	throw new UnsupportedOperationException("This node should not need to use this operation!");
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
