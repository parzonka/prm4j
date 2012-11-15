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

import prm4j.api.Event;
import prm4j.indexing.BaseMonitor;
import prm4j.indexing.staticdata.ChainData;

public class MonitorSet {

    private int size = 0;
    private final BaseMonitor[] monitorSet;

    public MonitorSet() {
	monitorSet = new BaseMonitor[8];
    }

    public void add(BaseMonitor monitor) {
	monitorSet[size++] = monitor;
	ensureCapacity();
    }

    private void ensureCapacity() {
	// TODO ensureCapacity
    }

    public void processEvent(Event event) {
	for (int i = 0; i < size; i++) { // 63
	    final BaseMonitor compatibleMonitor = monitorSet[i];
	    if (compatibleMonitor == null) {
		break;
	    }
	    compatibleMonitor.processEvent(event);
	}

    }

    public void join(NodeStore nodeStore, LowLevelBinding[] bindings, Event event,
	    final LowLevelBinding[] joinableBindings, boolean someBindingsAreKnown, long tmax, int[] copyPattern) {
	// iterate over all compatible nodes
	LowLevelBinding[] joinable = joinableBindings.clone(); // 62

	for (int i = 0; i < size; i++) { // 63

	    // TODO check for liveliness and cleanup

	    final BaseMonitor compatibleMonitor = monitorSet[i];
	    if (compatibleMonitor == null) {
		break;
	    }
	    if (someBindingsAreKnown && compatibleMonitor.getCreationTime() < tmax) { // 64
		continue; // 65
	    }
	    createJoin(joinable, compatibleMonitor.getLowLevelBindings(), copyPattern); // 67 - 71
	    final Node lastNode = nodeStore.getNode(joinable);
	    if (lastNode.getMonitor() == null) { // 72
		// inlined DefineTo // 73
		final BaseMonitor monitor = compatibleMonitor.copy(joinable); // 102-105
		monitor.processEvent(event); // 103
		lastNode.setMonitor(monitor); // 106
		// chain phase
		for (ChainData chainData : nodeStore.getNode(bindings).getMetaNode().getChainDataArray()) {
		    nodeStore.getNode(bindings, chainData.getNodeMask()).getMonitorSet(chainData.getMonitorSetId())
			    .add(monitor);
		} // 99
		joinable = joinableBindings.clone(); // 74
	    }
	}
    }

    private static void createJoin(LowLevelBinding[] joinableBindings, LowLevelBinding[] joiningBindings,
	    int[] copyPattern) {
	// fill in the missing bindings into the duplicate from the old monitor
	for (int j = 0; j < copyPattern.length; j += 2) {
	    // copy from j to j+1
	    joinableBindings[copyPattern[j + 1]] = joiningBindings[copyPattern[j]];
	}
    }

    /**
     * Searches the set linearly if monitor is contained, testing for object identity. Should be used only for
     * diagnostic purposes.
     *
     * @param monitor
     * @return true if monitor is contained
     */
    public boolean contains(BaseMonitor monitor) {
	for (int i = 0; i < size; i++) {
	    if (monitorSet[i] == monitor) {
		return true;
	    }
	}
	return false;
    }

}
