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

import prm4j.Globals;
import prm4j.api.BaseEvent;
import prm4j.api.Event;
import prm4j.api.MatchHandler;
import prm4j.api.ParametricMonitor;
import prm4j.indexing.Monitor;
import prm4j.indexing.staticdata.ChainData;
import prm4j.indexing.staticdata.EventContext;
import prm4j.indexing.staticdata.JoinData;
import prm4j.indexing.staticdata.MaxData;
import prm4j.indexing.staticdata.MetaNode;
import prm4j.spec.Spec;

public class DefaultParametricMonitor implements ParametricMonitor {

    protected final Monitor monitorPrototype;
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
	bindingStore = new DefaultBindingStore(new LinkedListBindingFactory(), spec.getFullParameterSet());
	monitorPrototype = spec.getInitialMonitor();
	nodeManager = new NodeManager();
	nodeStore = new DefaultNodeStore(metaTree, nodeManager);
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
    public DefaultParametricMonitor(BindingStore bindingStore, NodeStore nodeStore, Monitor monitorPrototype,
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
	Monitor instanceMonitor = instanceNode.getMonitor();

	if (instanceMonitor == null) { // 7
	    // direct update phase
	    for (MonitorSet monitorSet : instanceNode.getMonitorSets()) { // (30 - 32) new
		if (monitorSet != null) {
		    monitorSet.processEvent(event);
		}
	    }
	    findMaxPhase: for (MaxData maxData : eventContext.getMaxData(baseEvent)) { // 8
		Monitor maxMonitor = nodeStore.getNode(bindings, maxData.nodeMask).getMonitor(); // 9
		if (maxMonitor != null) { // 10
		    final long maxMonitorTimestamp = maxMonitor.getTimestamp();
		    for (int[] disableMask : maxData.disableMasks) {
			final Node subInstanceNode = nodeStore.getNode(bindings, disableMask);
			if (subInstanceNode != NullNode.instance) {
			    if (subInstanceNode.getTimestamp() > maxMonitorTimestamp
				    || (subInstanceNode.getMonitor() != null && subInstanceNode.getMonitor()
					    .getTimestamp() < maxMonitorTimestamp)) {
				continue findMaxPhase;
			    }
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
			nodeStore.getOrCreateNode(bindings, chainData.nodeMask).getMonitorSet(chainData.monitorSetId)
				.add(instanceNode.getNodeRef()); // 111
		    } // 107
		    break findMaxPhase;
		}
	    }

	    monitorCreation: if (instanceMonitor == null && eventContext.isCreationEvent(baseEvent)) {

		for (int[] existingMonitorMask : eventContext.getExistingMonitorMasks(baseEvent)) {
		    Node someNode = nodeStore.getNode(bindings, existingMonitorMask);
		    if (someNode.getMonitor() != null) {
			break monitorCreation;
		    }
		}

		if (eventContext.isDisablingEvent(baseEvent)) { // 2
		    instanceMonitor = new DeadMonitor(timestamp);
		    nodeStore.getOrCreateNode(bindings, parameterMask).setMonitor(instanceMonitor);
		} else {
		    // inlined DefineNew from 93
		    instanceMonitor = monitorPrototype.copy(toCompressedBindings(bindings, parameterMask), timestamp);
		    if (instanceNode == NullNode.instance) {
			instanceNode = nodeStore.getOrCreateNode(bindings, parameterMask); // get real instance node
		    }
		    instanceNode.setMonitor(instanceMonitor); // 98
		    // since we need some information in the meta node, we cannot process the event first before node
		    // creation
		    instanceMonitor.process(event); // 95

		    // inlined chain-method
		    for (ChainData chainData : instanceNode.getMetaNode().getChainDataArray()) { // 110
			final Node node = nodeStore.getOrCreateNode(bindings, chainData.nodeMask);
			node.getMonitorSet(chainData.monitorSetId).add(instanceNode.getNodeRef()); // 111
		    } // 99
		}
	    }

	    // inlined Join from 42
	    joinPhase: for (JoinData joinData : eventContext.getJoinData(baseEvent)) { // 43

		// if node does not exist there can't be any joinable monitors
		final Node compatibleNode = nodeStore.getNode(bindings, joinData.nodeMask);
		if (compatibleNode == NullNode.instance) {
		    continue joinPhase;
		}

		long maxInstanceTimestamp = Long.MIN_VALUE;
		long minMonitorTimestamp = Long.MAX_VALUE;

		final int[][] disableMasks = joinData.disableMasks;
		for (int i = 0; i < disableMasks.length; i++) {
		    final Node subInstanceNode = nodeStore.getNode(bindings, disableMasks[i]);
		    final long instanceTimestamp = subInstanceNode.getTimestamp();
		    if (maxInstanceTimestamp < instanceTimestamp) {
			maxInstanceTimestamp = instanceTimestamp;
		    }
		    if (subInstanceNode.getMonitor() != null) {
			final long monitorTimestamp = subInstanceNode.getMonitor().getTimestamp();
			if (minMonitorTimestamp > monitorTimestamp) {
			    minMonitorTimestamp = monitorTimestamp;
			}
		    }
		}

		// calculate once the bindings to be joined with the whole monitor set
		final LowLevelBinding[] joinableBindings = createJoinableBindings(bindings, joinData.extensionPattern); // 56
															// -
															// 61

		// join is performed in monitor set
		compatibleNode.getMonitorSet(joinData.monitorSetId).join(nodeStore, event, joinableBindings,
			minMonitorTimestamp, maxInstanceTimestamp, joinData.copyPattern);

	    }
	    nodeStore.getNode(bindings, parameterMask).setTimestamp(timestamp);
	} else {
	    // update phase
	    instanceMonitor.process(event); // 30
	    for (MonitorSet monitorSet : instanceNode.getMonitorSets()) { // 30 - 32
		if (monitorSet != null) {
		    monitorSet.processEvent(event);
		}
	    }
	}

	// we increment the timestamp at the end (deviating from the algorithm) because we want to count the number of
	// events (and reuse the logger in the nodeManager, but this may be changed in the future)
	nodeManager.tryToClean(timestamp);
	if (logger != null) {
	    logger.log(timestamp);
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
