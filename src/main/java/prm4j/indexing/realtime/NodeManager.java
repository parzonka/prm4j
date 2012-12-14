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
import java.util.ArrayList;
import java.util.List;

public class NodeManager {

    private long createdCount;
    private long cleanedCount;

    /*
     * The ReferenceQueue for Nodes had too little entries. The reason is probably that the NodeRefs (weak references)
     * had been garbage collected before the gc could notice that their referent was unreachable. This is now fixed
     * temporarily by storing all NodeRefs in a list in the manager. This list cannot be resetted reliably without
     * missing entries in the reference queue; Another solution has yet to be found, if we don't want to keep the list
     * of NodeRefs forever.
     */
    private final List<NodeRef> nodeRefs;

    private final ReferenceQueue<Node> referenceQueue;

    public NodeManager() {
	referenceQueue = new ReferenceQueue<Node>();
	nodeRefs = new ArrayList<NodeRef>();
    }

    public ReferenceQueue<Node> getReferenceQueue() {
	return referenceQueue;
    }

    public void tryToClean(long timestamp) {
	if (timestamp % 1000 == 0)
	    reallyClean();
    }

    public void reallyClean() {
	NodeRef nodeRef = (NodeRef) referenceQueue.poll();
	while (nodeRef != null) {
	    // System.out.println("[prm4j.NodeManager] I would like to cleanup:" + nodeRef);
	    cleanedCount++;
	    nodeRef = (NodeRef) referenceQueue.poll();
	}
    }

    public Object getCleanedCount() {
	return cleanedCount;
    }

    public void createdNode(Node node) {
	nodeRefs.add(node.getNodeRef());
	createdCount++;
	// System.out.println("[prm4j.NodeManager] Created node " + createdCount++);
    }

    public long getCreatedCount() {
	return createdCount;
    }

}
