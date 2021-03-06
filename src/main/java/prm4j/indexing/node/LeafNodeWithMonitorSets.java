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

import prm4j.indexing.binding.Binding;
import prm4j.indexing.model.ParameterNode;
import prm4j.indexing.monitor.MonitorSet;

public class LeafNodeWithMonitorSets extends LeafNode {

    private final MonitorSet[] monitorSets;

    public LeafNodeWithMonitorSets(ParameterNode parameterNode, Binding key, ReferenceQueue<Node> refQueue) {
	super(parameterNode, key, refQueue);
	monitorSets = new MonitorSet[parameterNode.getMonitorSetCount()];
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

}
