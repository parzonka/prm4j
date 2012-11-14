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
import prm4j.indexing.BaseMonitor;
import prm4j.indexing.staticdata.ChainData;
import prm4j.indexing.staticdata.EventContext;
import prm4j.indexing.staticdata.JoinData;
import prm4j.indexing.staticdata.MaxData;
import prm4j.indexing.staticdata.MetaNode;
import prm4j.spec.Spec;

public class DefaultParametricMonitor implements ParametricMonitor {

    private final BaseMonitor monitorPrototype;
    private final BindingStore bindingStore;
    private final NodeStore nodeStore;
    private final EventContext eventContext;
    private long timestamp = 0L;

    public DefaultParametricMonitor(MetaNode metaTree, EventContext eventContext, Spec spec) {
	this.eventContext = eventContext;
	bindingStore = new DefaultBindingStore(spec.getFullParameterSet(), 1);
	nodeStore = new DefaultNodeStore(metaTree);
	monitorPrototype = spec.getInitialMonitor();
    }

    public DefaultParametricMonitor(BindingStore bindingStore, NodeStore nodeStore, BaseMonitor monitorPrototype, EventContext eventContext) {
	this.bindingStore = bindingStore;
	this.nodeStore = nodeStore;
	this.monitorPrototype = monitorPrototype;
	this.eventContext = eventContext;
    }


    @Override
    public void processEvent(Event event) {

	final LowLevelBinding[] bindings = bindingStore.getBindings(event.getBoundObjects());
	final BaseEvent baseEvent = event.getBaseEvent();
	final Node instanceNode = nodeStore.getNode(bindings);
	final BaseMonitor instanceMonitor = instanceNode.getMonitor();

	if (eventContext.isDisablingEvent(event.getBaseEvent())) { // 2
	    for (LowLevelBinding binding : bindings) { // 3
		binding.setDisabled(true); // 4
	    } // 5
	} // 6

	if (instanceMonitor == null) { // 7
	    findMaxPhase: for (MaxData maxData : eventContext.getMaxData(baseEvent)) { // 8
		BaseMonitor m = nodeStore.getNode(bindings, maxData.getNodeMask()).getMonitor(); // 9
		if (m != null) { // 10
		    for (int i : maxData.getDiffMask()) { // 11
			LowLevelBinding b = bindings[i];
			if (b.getTimestamp() < timestamp && (b.getTimestamp() > m.getCreationTime() || b.isDisabled())) { // 12
			    continue findMaxPhase; // 13
			}
		    }
		    // inlined DefineTo from 73
		    BaseMonitor monitor = m.copy(); // 102-105
		    monitor.processEvent(event); // 103
		    instanceNode.setMonitor(monitor); // 106
		    chain(bindings, monitor); // 107
		    break findMaxPhase;
		}
	    }
	    monitorCreation: if (instanceNode.getMonitor() == null) {
		if (eventContext.isCreationEvent(baseEvent)) { // 20
		    for (LowLevelBinding b : bindings) { // 21
			if (b.isDisabled()) { // 22
			    break monitorCreation; // 23
			}
		    }
		    // inlined DefineNew from 93
		    BaseMonitor monitor = monitorPrototype.copy(bindings, timestamp); // 94 - 97
		    monitor.processEvent(event); // 95
		    instanceNode.setMonitor(monitor); // 98
		    chain(bindings, monitor); // 99
		}
	    }
	    // inlined Join from 42
	    joinPhase: for (JoinData joinData : eventContext.getJoinData(baseEvent)) { // 43
		long tmax = 0L; // 44
		final int[] copyPattern = joinData.getCopyPattern(); // use the copy pattern as diff mask
		for (int i = 0; i < copyPattern.length; i = i + 2) { // 45
		    final LowLevelBinding b = bindings[copyPattern[i]];
		    final long bTimestamp = b.getTimestamp();
		    if (bTimestamp < timestamp) { // 46
			if (b.isDisabled()) { // 47
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

		// join is performed in monitor set
		compatibleNode.getMonitorSet(joinData.getMonitorSetId()).join(nodeStore, bindings, event,
			    joinableBindings, someBindingsAreKnown, tmax, joinData.getCopyPattern());
	    }
	} else {
	    // update phase
	    instanceMonitor.processEvent(event); // 30
	    for (MonitorSet monitorSet : instanceNode.getMonitorSets()) { // 30 - 32
		monitorSet.processEvent(event);
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

    private void chain(LowLevelBinding[] bindings, BaseMonitor monitor) {
	for (ChainData chainData : nodeStore.getNode(bindings).getMetaNode().getChainDataArray()) {
	    nodeStore.getNode(bindings, chainData.getNodeMask()).getMonitorSet(chainData.getMonitorSetId())
		    .add(monitor);
	}
    }

    @Override
    public void reset() {
	// TODO Auto-generated method stub
    }

}
