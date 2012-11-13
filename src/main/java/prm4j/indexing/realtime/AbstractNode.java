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

import prm4j.indexing.map.MinimalMap;

public abstract class AbstractNode extends MinimalMap<LowLevelBinding, Node> implements Node {

    private final LowLevelBinding key;
    private final int hashCode;
    private Node nextNode;

    public AbstractNode(LowLevelBinding key, int hashCode) {
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
    public Node next() {
	return nextNode;
    }

    @Override
    public void setNext(Node nextNode) {
	this.nextNode = nextNode;
    }

    @Override
    protected Node[] createTable(int size) {
	return new Node[size];
    }

    @Override
    protected Node createEntry(LowLevelBinding key, int hashCode) {
	return getMetaNode().createNode(key.getParameterId(), key, hashCode);
    }

}
