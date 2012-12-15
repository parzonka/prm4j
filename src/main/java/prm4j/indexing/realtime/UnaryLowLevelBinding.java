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

/**
 * Memory-efficient {@link LowLevelBinding} implementation, only usable in 1-parameter patterns (e.g. HasNext).
 */
public class UnaryLowLevelBinding extends WeakReference<Object> implements LowLevelBinding {

    private final int hashCode;
    private LowLevelBinding next;
    private WeakReference<Node> nodeRef;

    public UnaryLowLevelBinding(Object boundObject, int hashCode, ReferenceQueue<Object> referenceQueue) {
	super(boundObject, referenceQueue);
	this.hashCode = hashCode;
    }

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
    public void registerNode(WeakReference<Node> nodeReference) {
	nodeRef = nodeReference;
    }

    @Override
    public void release() {
	if (nodeRef != null) {
	    Node node = nodeRef.get();
	    if (node != null) {
		node.remove(this);
	    }
	    nodeRef = null;
	}
    }

    @Override
    public boolean isDisabled() {
	throw new UnsupportedOperationException("This binding should not need to use this operation!");
    }

    @Override
    public void setDisabled(boolean disabled) {
	throw new UnsupportedOperationException("This binding should not need to use this operation!");
    }

    @Override
    public long getTimestamp() {
	throw new UnsupportedOperationException("This binding should not need to use this operation!");
    }

    @Override
    public void setTimestamp(long timestamp) {
	throw new UnsupportedOperationException("This binding should not need to use this operation!");
    }

    @Override
    public String toString() {
	return "Binding(" + hashCode + ")=" + get();
    }

}
