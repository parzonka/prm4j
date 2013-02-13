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

public abstract class AbstractLowLevelBinding extends WeakReference<Object> implements LowLevelBinding {

    private long timestamp;
    private boolean disabled;
    private final int hashCode;
    private LowLevelBinding next;

    public AbstractLowLevelBinding(Object boundObject, int hashCode, ReferenceQueue<Object> q) {
	super(boundObject, q);
	this.hashCode = hashCode;
	timestamp = Long.MAX_VALUE; // indicates the binding was just created
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
	// TODO remove if DirectNodeStore proves to be more effective
	throw new UnsupportedOperationException("This class should not need to use this operation!");
    }

    @Override
    public void setNode(Node node) {
	// TODO remove if DirectNodeStore proves to be more effective
	throw new UnsupportedOperationException("This class should not need to use this operation!");
    }

}
