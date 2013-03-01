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

import prm4j.indexing.map.NodeMap;

public abstract class AbstractNode extends NodeMap<Node> implements Node {

    private final Binding key;
    private Node nextNode;

    /**
     * @param metaNode
     * @param key
     *            may be null, if node is root node
     * @param hashCode
     *            hash code of the key
     */
    public AbstractNode(Binding key) {
	this.key = key;
    }

    @Override
    public int hashCode() {
	return key.hashCode();
    }

    @Override
    public Binding getKey() {
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
    protected Node createEntry(int parameterIndex, Binding key) {
	return getMetaNode().createNode(parameterIndex, key);
    }

}
