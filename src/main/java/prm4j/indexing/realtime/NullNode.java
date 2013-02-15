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

import prm4j.indexing.Monitor;
import prm4j.indexing.staticdata.MetaNode;

public class NullNode implements Node {

    public final static Node instance = new NullNode();
    private final static MonitorSet EMPTY_MONITOR_SET = new MonitorSet();
    private final static MonitorSet[] EMPTY_MONITOR_SETS = new MonitorSet[0];

    private NodeRef nodeRef;

    public NullNode() {
	nodeRef = new NodeRef(this, null);
    }

    @Override
    public LowLevelBinding getKey() {
	return null;
    }

    public Node instance() {
	return instance;
    }

    @Override
    public Node next() {
	throw new UnsupportedOperationException();
    }

    @Override
    public void setNext(Node nextEntry) {
	throw new UnsupportedOperationException();
    }

    @Override
    public MetaNode getMetaNode() {
	return null;
    }

    @Override
    public Monitor getMonitor() {
	return null;
    }

    @Override
    public void setMonitor(Monitor monitor) {
	throw new UnsupportedOperationException();
    }

    @Override
    public void remove(LowLevelBinding binding) {
	throw new UnsupportedOperationException();
    }

    @Override
    public MonitorSet getMonitorSet(int monitorSetId) {
	return EMPTY_MONITOR_SET;
    }

    @Override
    public MonitorSet[] getMonitorSets() {
	return EMPTY_MONITOR_SETS;
    }

    @Override
    public int size() {
	return 0;
    }

    @Override
    public NodeRef getNodeRef() {
	return nodeRef;
    }

    @Override
    public Node getOrCreateNode(int parameterIndex, LowLevelBinding binding) {
	return null;
    }

    @Override
    public Node getNode(int parameterIndex, LowLevelBinding binding) {
	return null;
    }

    @Override
    public int parameterIndex() {
	return 0;
    }

    @Override
    public void setTimestamp(long timestamp) {
	// do nothing
    }

    @Override
    public long getTimestamp() {
	return Long.MIN_VALUE; // the null node did always exist
    }

    @Override
    public String toString() {
	return "NullNode";
    }

}
