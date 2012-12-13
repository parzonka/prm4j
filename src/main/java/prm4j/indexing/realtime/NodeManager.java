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

    long counter;
    private final ReferenceQueue<Node> referenceQueue;

    public NodeManager() {
	referenceQueue = new ReferenceQueue<Node>();
    }

    public ReferenceQueue<Node> getReferenceQueue() {

	return null;
    }

    public void tryToClean() {
	reallyClean();
    }

    private void reallyClean() {
	System.out.println("Really clean");
	NodeRef nodeRef = (NodeRef) referenceQueue.poll();
	while (nodeRef != null) {
	    nodeRef.monitor.getLowLevelBindings();
	    System.out.println(nodeRef);
	    nodeRef = (NodeRef) referenceQueue.poll();
	}
    }

}
