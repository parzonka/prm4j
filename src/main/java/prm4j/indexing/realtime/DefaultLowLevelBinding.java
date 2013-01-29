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
import java.util.Arrays;

public class DefaultLowLevelBinding extends WeakReference<Object> implements LowLevelBinding {

    private long timestamp;
    private boolean disabled;
    private final int hashCode;
    private LowLevelBinding next;
    private Object[] nodeRefs;
    private int nodeRefsSize;
    private Node node;

    public DefaultLowLevelBinding(Object boundObject, int hashCode, ReferenceQueue<Object> q, int initialNodeRefsSize) {
	super(boundObject, q);
	this.hashCode = hashCode;
	timestamp = Long.MAX_VALUE; // indicates the binding was just created
	// hijack the field to store the initial capacity needed for lazy creation of the nodeRefs array
	nodeRefsSize = initialNodeRefsSize;
    }

    /**
     * {@inheritDoc}
     * 
     * The key used in the {@link BindingStore} is the object.
     */
    @Override
    public Object getKey() {
	return get();
    }

    @Override
    public int hashCode() {
	return hashCode;
    }

    @Override
    public LowLevelBinding next() {
	return next;
    }

    @Override
    public void setNext(LowLevelBinding next) {
	this.next = next;
    }

    @Override
    public void registerNode(Object nodeReference) {
	// create nodeRefs lazily
	if (nodeRefs == null) {
	    // we had hijacked the nodeRefsSize field to store the initial nodeRefs capacity
	    nodeRefs = new Object[nodeRefsSize];
	    nodeRefsSize = 0; // reset the actual size
	} else {
	    // ensure capacity
	    if (nodeRefsSize >= nodeRefs.length) {
		final int capacity = (nodeRefs.length * 3) / 2 + 1;
		nodeRefs = Arrays.copyOf(nodeRefs, capacity);
	    }
	}
	nodeRefs[nodeRefsSize++] = nodeReference;
    }

    @Override
    public void release() {
	if (nodeRefs != null) {
	    for (int i = 0; i < nodeRefsSize; i++) {
		@SuppressWarnings("unchecked")
		final Node node = ((WeakReference<Node>) nodeRefs[i]).get();
		if (node != null) {
		    node.remove(this);
		}
	    }
	    nodeRefs = null;
	}
    }

    @Override
    public boolean isDisabled() {
	return disabled;
    }

    @Override
    public void setDisabled(boolean disabled) {
	this.disabled = disabled;

    }

    @Override
    public long getTimestamp() {
	return timestamp;
    }

    @Override
    public void setTimestamp(long timestamp) {
	this.timestamp = timestamp;
    }

    @Override
    public String toString() {
	return "Binding(" + hashCode + ")=" + get();
    }

    @Override
    public Node getNode() {
	return node;
    }

    @Override
    public void setNode(Node node) {
	this.node = node;
    }

}
