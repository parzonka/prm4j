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

public class NullNode implements Node {

    public final static Node instance = new NullNode();
    private final static MonitorSet EMPTY_MONITOR_SET = new MonitorSet();
    private final static MonitorSet[] EMPTY_MONITOR_SETS = new MonitorSet[0];

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
    public BaseMonitor getMonitor() {
	return null;
    }

    @Override
    public void setMonitor(BaseMonitor monitor) {
	throw new UnsupportedOperationException();
    }

    @Override
    public Node getNode(LowLevelBinding binding) {
	return null;
    }

    @Override
    public Node getNodeNonCreative(LowLevelBinding binding) {
	return null;
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

}
