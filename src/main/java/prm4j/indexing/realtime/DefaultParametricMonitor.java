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

import prm4j.Globals;
import prm4j.api.BaseEvent;
import prm4j.api.Event;
import prm4j.api.MatchHandler;
import prm4j.api.ParametricMonitor;
import prm4j.indexing.BaseMonitor;
import prm4j.indexing.staticdata.ChainData;
import prm4j.indexing.staticdata.EventContext;
import prm4j.indexing.staticdata.JoinData;
import prm4j.indexing.staticdata.MaxData;
import prm4j.indexing.staticdata.MetaNode;
import prm4j.spec.Spec;

public class DefaultParametricMonitor implements ParametricMonitor {

    protected final BaseMonitor monitorPrototype;
    protected BindingStore bindingStore;
    protected NodeStore nodeStore;
    protected final EventContext eventContext;
    protected long timestamp = 0L;
    protected final NodeManager nodeManager;

    protected boolean monitorActivated = false;

    protected final ParametricMonitorLogger logger;

    /**
     * Creates a DefaultParametricMonitor using default {@link BindingStore} and {@link NodeStore} implementations (and
     * configurations).
     * 
     * @param metaTree
     * @param eventContext
     * @param spec
     */
    public DefaultParametricMonitor(MetaNode metaTree, EventContext eventContext, Spec spec) {
	this.eventContext = eventContext;
	bindingStore = new DefaultBindingStore(new DefaultBindingFactory(), spec.getFullParameterSet());
	monitorPrototype = spec.getInitialMonitor();
	nodeManager = new NodeManager();
	nodeStore = new DirectNodeStore(metaTree, nodeManager);
	logger = Globals.DEBUG ? new ParametricMonitorLogger(bindingStore, nodeManager) : null;
    }

    /**
     * Creates a DefaultParametricMonitor which externally configurable BindingStore and NodeStore.
     * 
     * @param bindingStore
     * @param nodeStore
     * @param monitorPrototype
     * @param eventContext
     * @param nodeManager
     * @param activated
     */
    public DefaultParametricMonitor(BindingStore bindingStore, NodeStore nodeStore, BaseMonitor monitorPrototype,
	    EventContext eventContext, NodeManager nodeManager, boolean activated) {
	this.bindingStore = bindingStore;
	this.nodeStore = nodeStore;
	this.monitorPrototype = monitorPrototype;
	this.eventContext = eventContext;
	this.nodeManager = nodeManager;
	monitorActivated = activated;
	logger = Globals.DEBUG ? new ParametricMonitorLogger(bindingStore, nodeManager) : null;
    }

    @Override
    public synchronized void processEvent(Event event) {

	final BaseEvent baseEvent = event.getBaseEvent();

	// wait for a creation event to activate monitoring
	if (!monitorActivated) {
	    if (eventContext.isCreationEvent(baseEvent)) {
		monitorActivated = true;
	    } else {
		return;
	    }
	}

	// selects all bindings from 'bindings' which are not null
	final int[] parameterMask = baseEvent.getParameterMask();
	// uncompressed representation of bindings
	final LowLevelBinding[] bindings = bindingStore.getBindings(event.getBoundObjects());
	// node associated to the current bindings. May be NullNode if binding is encountered the first time
	Node instanceNode = nodeStore.getNode(bindings, parameterMask);
	// monitor associated with the instance node. May be null if the instance node is a NullNode
	BaseMonitor instanceMonitor = instanceNode.getMonitor();

	// disable all bindings which are
	if (eventContext.isDisablingEvent(baseEvent)) { // 2
	    for (int i = 0; i < parameterMask.length; i++) { // 3
		bindings[parameterMask[i]].setDisabled(true); // 4
	    } // 5
	} // 6

	if (instanceMonitor == null) { // 7
	    // direct update phase
	    for (MonitorSet monitorSet : instanceNode.getMonitorSets()) { // (30 - 32) new
		if (monitorSet != null) {
		    monitorSet.processEvent(event);
		}
	    }
	    findMaxPhase: for (MaxData maxData : eventContext.getMaxData(baseEvent)) { // 8
		BaseMonitor maxMonitor = nodeStore.getNode(bindings, maxData.getNodeMask()).getMonitor(); // 9
		if (maxMonitor != null) { // 10
		    for (int i : maxData.getDiffMask()) { // 11
			LowLevelBinding b = bindings[i];
			if (b.getTimestamp() < timestamp
				&& (b.getTimestamp() > maxMonitor.getCreationTime() || b.isDisabled())) { // 12
			    continue findMaxPhase; // 13
			}
		    }
		    if (instanceNode == NullNode.instance) {
			instanceNode = nodeStore.getOrCreateNode(bindings, parameterMask); // get real
											   // instance node
		    }
		    // inlined DefineTo from 73
		    instanceMonitor = maxMonitor.copy(toCompressedBindings(bindings, parameterMask)); // 102-105
		    instanceNode.setMonitor(instanceMonitor); // 106
		    instanceMonitor.process(event); // 103

		    // inlined chain-method
		    for (ChainData chainData : instanceNode.getMetaNode().getChainDataArray()) { // 110
			nodeStore.getOrCreateNode(bindings, chainData.getNodeMask())
				.getMonitorSet(chainData.getMonitorSetId()).add(instanceNode.getNodeRef()); // 111
		    } // 107
		    break findMaxPhase;
		}
	    }

	    Node node = null;
	    monitorCreation: if (instanceMonitor == null) {
		if (eventContext.isCreationEvent(baseEvent)) { // 20
		    for (int i = 0; i < parameterMask.length; i++) { // 21
			if (bindings[i].isDisabled()) {// 22
			    break monitorCreation; // 23
			}
		    }

		    // inlined DefineNew from 93
		    instanceMonitor = monitorPrototype.copy(toCompressedBindings(bindings, parameterMask), timestamp); // 94
														       // -
														       // 97
		    if (instanceNode == NullNode.instance) {
			instanceNode = nodeStore.getOrCreateNode(bindings, parameterMask); // get real
											   // instance node
		    }
		    instanceNode.setMonitor(instanceMonitor); // 98
		    // since we need some information in the meta node, we cannot process the event first before node
		    // creation
		    instanceMonitor.process(event); // 95

		    // inlined chain-method
		    for (ChainData chainData : instanceNode.getMetaNode().getChainDataArray()) { // 110
			node = nodeStore.getOrCreateNode(bindings, chainData.getNodeMask());
			node.getMonitorSet(chainData.getMonitorSetId()).add(instanceNode.getNodeRef()); // 111
		    } // 99
		}
	    }
	    // inlined Join from 42
	    joinPhase: for (JoinData joinData : eventContext.getJoinData(baseEvent)) { // 43

		// if node does not exist there can't be any joinable monitors
		final Node compatibleNode = nodeStore.getNode(bindings, joinData.getNodeMask());
		if (compatibleNode == NullNode.instance) {
		    continue joinPhase;
		}

		// if bindings are disabled, the binding will not add to a valid trace
		long tmax = 0L; // 44
		final int[] diffMask = joinData.getDiffMask();
		for (int i = 0; i < diffMask.length; i++) { // 45
		    final LowLevelBinding b = bindings[diffMask[i]];
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

		// calculate once the bindings to be joined with the whole monitor set
		final LowLevelBinding[] joinableBindings = createJoinableBindings(bindings,
			joinData.getExtensionPattern()); // 56 - 61

		// join is performed in monitor set
		compatibleNode.getMonitorSet(joinData.getMonitorSetId()).join(nodeStore, event, joinableBindings,
			someBindingsAreKnown, tmax, joinData.getCopyPattern());
	    }

	} else {
	    // update phase
	    instanceMonitor.process(event); // 30
	    for (MonitorSet monitorSet : instanceNode.getMonitorSets()) { // 30 - 32
		if (monitorSet != null) {
		    monitorSet.processEvent(event);
		}
	    }
	}
	for (int i = 0; i < parameterMask.length; i++) { // 37
	    bindings[parameterMask[i]].setTimestamp(timestamp);
	}
	nodeManager.tryToClean(timestamp);
	if (logger != null) {
	    logger.log(timestamp, event);
	}
	timestamp++; // 40
    }

    private static LowLevelBinding[] toCompressedBindings(LowLevelBinding[] uncompressedBindings, int[] parameterMask) {
	LowLevelBinding[] result = new LowLevelBinding[parameterMask.length];
	int j = 0;
	for (int i = 0; i < parameterMask.length; i++) {
	    result[j++] = uncompressedBindings[parameterMask[i]];
	}
	return result;
    }

    /**
     * Returns an array of bindings containing "gaps" enabling efficient joins by filling these gaps.
     * 
     * @param bindings
     * @param extensionPattern
     *            allows transformation of the bindings to joinable bindings
     * @return joinable bindings
     */
    static LowLevelBinding[] createJoinableBindings(LowLevelBinding[] bindings, int[] extensionPattern) {
	final LowLevelBinding[] joinableBindings = new LowLevelBinding[extensionPattern.length];
	for (int i = 0; i < extensionPattern.length; i++) {
	    final int e = extensionPattern[i];
	    if (e >= 0) {
		joinableBindings[i] = bindings[e];
	    }
	}
	return joinableBindings;
    }

    @Override
    public void reset() {
	monitorActivated = false;
	timestamp = 0L;
	if (logger != null) {
	    logger.reset();
	}
	bindingStore.reset();
	nodeStore.reset();
	nodeManager.reset();
	BaseMonitor.reset();
	MatchHandler.reset();
    }

}
