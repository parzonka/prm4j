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

import prm4j.api.ParametricMonitor;
import prm4j.indexing.staticdata.NodeFactory;

/**
 * Coordinates the garbage collection of monitors which can never reach an accepting state. Provides some diagnostics
 * regarding node creation.
 */
public class NodeManager {

    /**
     * Interval for polling the reference queue for {@link NodeRef}s after garbage-collection of {@link Node}s. The
     * interval is measured in number of events processed by the {@link ParametricMonitor} (aka 'timestamp'). Larger
     * numbers (e.g. 100000) have proven sufficient, since {@link NodeRef}s containing the orphaned monitors usually get
     * garbage collected themselves quite quickly.
     */
    private final static int CLEANING_INTERVAL = 100000;

    /**
     * The number of created nodes by each {@link NodeFactory}.
     */
    private long createdNodeCount;

    /**
     * The number of monitors for which their associated {@link Node} has been garbage collected.
     */
    private long orphanedMonitors;

    /**
     * The number of orphaned monitors that could never reach an accepting state and got garbage collected.
     */
    private long collectedMonitors;

    /**
     * Contains {@link NodeRef}s.
     */
    private final ReferenceQueue<Node> referenceQueue;

    public NodeManager() {
	referenceQueue = new ReferenceQueue<Node>();
    }

    /**
     * Calls {@link #reallyClean()} each time the cleaning interval is reached.
     *
     * @param timestamp
     */
    public void tryToClean(long timestamp) {
	if (timestamp % CLEANING_INTERVAL == 0)
	    reallyClean();
    }

    /**
     * Polls all expired {@link NodeRef}s and nullifies all monitors which can never reach an accepting state.
     */
    public void reallyClean() {
	NodeRef nodeRef = (NodeRef) referenceQueue.poll();
	while (nodeRef != null) {
	    orphanedMonitors++;
	    if (nodeRef.monitor != null && !nodeRef.monitor.isAcceptingStateReachable()) {
		nodeRef.monitor = null;
		collectedMonitors++;
	    }
	    nodeRef = (NodeRef) referenceQueue.poll();
	}
    }

    /**
     * DIAGNOSTIC: Called by each {@link NodeFactory} each time a node has been created.
     *
     * @param node
     */
    public void createdNode(Node node) {
	createdNodeCount++;
    }

    /**
     * DIAGNOSTIC: Returns the number of created nodes by each {@link NodeFactory}.
     *
     * @return the number of created nodes
     */
    public long getCreatedCount() {
	return createdNodeCount;
    }

    public ReferenceQueue<Node> getReferenceQueue() {
	return referenceQueue;
    }

    /**
     * DIAGNOSTIC: Returns the number of monitors for which their associated {@link Node} has been garbage collected.
     *
     * @return the number of orphaned monitors
     */
    public long getOrphanedMonitorsCount() {
	return orphanedMonitors;
    }

    /**
     * DIAGNOSTIC: Returns the number of orphaned monitors that could never reach an accepting state and got garbage
     * collected.
     *
     * @return the number of garbage collected monitors
     */
    public long getCollectedMonitorsCount() {
	return collectedMonitors;
    }

}
