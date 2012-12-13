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
import java.lang.ref.WeakReference;

import prm4j.indexing.BaseMonitor;

public class NodeRef extends WeakReference<Node> {

    public BaseMonitor monitor;

    public NodeRef(Node node, ReferenceQueue<Node> refQueue) {
	super(node, refQueue);
    }

}
