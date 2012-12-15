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

public class NodeManager {

    private long createdCount;
    private long cleanedCount;

    private final ReferenceQueue<Node> referenceQueue;

    public NodeManager() {
	referenceQueue = new ReferenceQueue<Node>();
    }

    public ReferenceQueue<Node> getReferenceQueue() {
	return referenceQueue;
    }

    public void tryToClean(long timestamp) {
	if (timestamp % 100000 == 0)
	    reallyClean();
    }

    public void reallyClean() {
	NodeRef nodeRef = (NodeRef) referenceQueue.poll();
	while (nodeRef != null) {
	    cleanedCount++;
	    // TODO clean monitor
	    nodeRef = (NodeRef) referenceQueue.poll();
	}
    }

    public Object getCleanedCount() {
	return cleanedCount;
    }

    public void createdNode(Node node) {
	createdCount++;
    }

    public long getCreatedCount() {
	return createdCount;
    }

}
