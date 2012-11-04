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

import prm4j.api.BaseEvent;
import prm4j.api.Event;
import prm4j.api.ParametricMonitor;
import prm4j.indexing.AbstractBaseMonitor;
import prm4j.indexing.staticdata.ChainData;
import prm4j.indexing.staticdata.EventContext;
import prm4j.indexing.staticdata.JoinData;
import prm4j.indexing.staticdata.MaxData;

public class DefaultParametricMonitor implements ParametricMonitor {

    private final AbstractBaseMonitor monitorPrototype;
    private BindingStore bindingStore;
    private NodeStore nodeStore;
    private final EventContext eventContext;
    private long timestamp = 0L;

    public DefaultParametricMonitor(EventContext eventContext, AbstractBaseMonitor monitorPrototype) {
	this.eventContext = eventContext;
	this.monitorPrototype = monitorPrototype;
    }

    @Override
    public void processEvent(Event event) {

	final LowLevelBinding[] bindings = bindingStore.getBindings(event.getBoundObjects());
	final BaseEvent baseEvent = event.getBaseEvent();
	final Node instanceNode = nodeStore.getNode(bindings);
	final AbstractBaseMonitor instanceMonitor = instanceNode.getMonitor();

	if (eventContext.isDisablingEvent(event.getBaseEvent())) { // 2
	    for (LowLevelBinding binding : bindings) { // 3
		binding.setDisable(true); // 4
	    } // 5
	} // 6

	if (instanceMonitor == null) { // 7
	    findMaxPhase: for (MaxData maxData : eventContext.getMaxData(baseEvent)) { // 8
		AbstractBaseMonitor m = nodeStore.getNode(bindings, maxData.getNodeMask()).getMonitor(); // 9
		if (m != null) { // 10
		    for (int i : maxData.getDiffMask()) { // 11
			LowLevelBinding b = bindings[i];
			if (b.getTimestamp() < timestamp && (b.getTimestamp() > m.getCreationTime() || b.getDisable())) { // 12
			    continue findMaxPhase; // 13
			}
		    }
		    // inlined DefineTo from 73
		    AbstractBaseMonitor monitor = m.copy(); // 102-105
		    monitor.processEvent(event); // 103
		    instanceNode.setMonitor(monitor); // 106
		    chain(bindings, monitor); // 107
		    break findMaxPhase;
		}
	    }
	    monitorCreation: if (instanceNode.getMonitor() == null) {
		if (eventContext.isCreationEvent(baseEvent)) { // 20
		    for (LowLevelBinding b : bindings) { // 21
			if (b.getDisable()) { // 22
			    break monitorCreation; // 23
			}
		    }
		    // inlined DefineNew from 93
		    AbstractBaseMonitor monitor = monitorPrototype.copy(bindings, timestamp); // 94 - 97
		    monitor.processEvent(event); // 95
		    instanceNode.setMonitor(monitor); // 98
		    chain(bindings, monitor); // 99
		}
	    }
	    // inlined Join from 42
	    joinPhase: for (JoinData joinData : eventContext.getJoinData(baseEvent)) { // 43
		long tmax = 0L; // 44
		for (int i : joinData.getDiffMask()) { // 45
		    final LowLevelBinding b = bindings[i];
		    final long bTimestamp = b.getTimestamp();
		    if (bTimestamp < timestamp) { // 46
			if (b.getDisable()) { // 47
			    continue joinPhase; // 48
			} else if (tmax < bTimestamp) { // 49
			    tmax = bTimestamp; // 50
			} // 51
		    } // 52
		} // 53
		final boolean someBindingsAreKnown = tmax < timestamp;
		final Node compatibleNode = nodeStore.getNode(bindings, joinData.getNodeMask());
		// calculate once the bindings to be joined with the whole monitor set
		final LowLevelBinding[] joinableBindings = createJoinableBindings(bindings,
			joinData.getExtensionPattern()); // 56 - 61
		// iterate over all compatible nodes
		final MonitorSetIterator iter = compatibleNode.getMonitorSet(joinData.getMonitorSetId()).getIterator();
		AbstractBaseMonitor compatibleMonitor = null;
		boolean isCompatibleMonitorAlive = false;
		// iterate over all compatible nodes
		LowLevelBinding[] joinable = joinableBindings.clone(); // 62
		monitorSetIteration: while (iter.hasNext(compatibleMonitor, isCompatibleMonitorAlive)) { // 63
		    compatibleMonitor = iter.next();
		    isCompatibleMonitorAlive = true;
		    if (someBindingsAreKnown && compatibleMonitor.getCreationTime() < tmax) { // 64
			continue monitorSetIteration; // 65
		    }
		    createJoin(joinable, compatibleMonitor.getLowLevelBindings(), joinData.getCopyPattern()); // 67 - 71
		    final Node lastNode = nodeStore.getNode(joinable);
		    if (lastNode.getMonitor() == null) { // 72
			// inlined DefineTo // 73
			AbstractBaseMonitor monitor = compatibleMonitor.copy(joinable); // 102-105
			monitor.processEvent(event); // 103
			lastNode.setMonitor(monitor); // 106
			chain(joinable, monitor); // 99
			joinable = joinableBindings.clone(); // 74
		    }
		}
	    }
	} else {
	    // update phase
	    for (MonitorSet monitorSet : instanceNode.getMonitorSets()) { // 30 - 32
		MonitorSetIterator iter = monitorSet.getIterator();
		AbstractBaseMonitor monitor = null;
		boolean isMonitorAlive = false;
		while (iter.hasNext(monitor, isMonitorAlive)) {
		    isMonitorAlive = iter.next().processEvent(event); // 33
		}
	    }
	}
	for (LowLevelBinding b : bindings) { // 37
	    b.setTimestamp(timestamp); // 38
	} // 39
	timestamp++; // 40
    }

    /**
     * Returns an array of bindings containing "gaps" enabling efficient joins by filling these gaps.
     *
     * @param bindings
     * @param extensionPattern
     *            allows transformation of the bindings to joinable bindings
     * @return joinable bindings
     */
    static LowLevelBinding[] createJoinableBindings(LowLevelBinding[] bindings, boolean[] extensionPattern) {
	final LowLevelBinding[] joinableBindings = new LowLevelBinding[extensionPattern.length];
	int sourceIndex = 0;
	for (int i = 0; i < extensionPattern.length; i++) {
	    if (extensionPattern[i]) {
		joinableBindings[i] = bindings[sourceIndex++];
	    }
	}
	assert sourceIndex == bindings.length : "All bindings have to be taken into account.";
	return joinableBindings;
    }

    private static void createJoin(LowLevelBinding[] joinableBindings, LowLevelBinding[] joiningBindings,
	    int[] copyPattern) {
	// fill in the missing bindings into the duplicate from the old monitor
	for (int j = 0; j < copyPattern.length; j += 2) {
	    // copy from j to j+1
	    joinableBindings[copyPattern[j + 1]] = joiningBindings[copyPattern[j]];
	}
    }

    private void chain(LowLevelBinding[] bindings, AbstractBaseMonitor monitor) {
	for (ChainData chainData : nodeStore.getNode(bindings).getNodeContext().getChainData()) {
	    nodeStore.getNode(bindings, chainData.getNodeMask()).getMonitorSet(chainData.getMonitorSetId())
		    .add(monitor);
	}
    }

    @Override
    public void reset() {
	// TODO Auto-generated method stub
    }

}
