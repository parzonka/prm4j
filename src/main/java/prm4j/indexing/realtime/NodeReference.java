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

import java.lang.ref.WeakReference;

import prm4j.indexing.map.MinimalMapEntry;

public class NodeReference extends WeakReference<Node> implements MinimalMapEntry<LowLevelBinding, NodeReference> {

    private final int hashCode;
    private final LowLevelBinding key;
    private NodeReference next;

    public NodeReference(Node referent, LowLevelBinding key, int hashCode) {
	super(referent);
	this.key = key;
	this.hashCode = hashCode;
    }

    @Override
    public int getHashCode() {
	return hashCode;
    }

    @Override
    public LowLevelBinding getKey() {
	return key;
    }

    @Override
    public NodeReference next() {
	return next;
    }

    @Override
    public void setNext(NodeReference next) {
	this.next = next;
    }

}
