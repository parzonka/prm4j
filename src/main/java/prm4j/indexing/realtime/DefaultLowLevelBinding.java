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
    private Holder<LowLevelBinding>[] bindingHolders;
    private int bindingHoldersSize;

    public DefaultLowLevelBinding(Object boundObject, int hashCode, ReferenceQueue<Object> q, int initialHoldersSize) {
	super(boundObject, q);
	this.hashCode = hashCode;
	timestamp = Long.MAX_VALUE; // indicates the binding was just created
	// hijack the field to store the initial capacity needed for lazy creation of the nodeRefs array
	bindingHoldersSize = initialHoldersSize;
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

    @SuppressWarnings("unchecked")
    @Override
    public void registerHolder(Holder<LowLevelBinding> bindingHolder) {
	// create bindingHoldersSize lazily
	if (bindingHolders == null) {
	    // we had hijacked the bindingHoldersSize field to store the initial bindingHolders capacity
	    bindingHolders = new Holder[bindingHoldersSize];
	    bindingHoldersSize = 0; // reset the actual size
	} else {
	    // ensure capacity
	    if (bindingHoldersSize >= bindingHolders.length) {
		final int capacity = (bindingHolders.length * 3) / 2 + 1;
		bindingHolders = Arrays.copyOf(bindingHolders, capacity);
	    }
	}
	bindingHolders[bindingHoldersSize++] = bindingHolder;
    }

    @Override
    public void release() {
	if (bindingHolders != null) {
	    for (int i = 0; i < bindingHoldersSize; i++) {
		bindingHolders[i].release(this);
	    }
	    bindingHolders = null;
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
	// TODO remove if DirectNodeStore proves to be more effective
	throw new UnsupportedOperationException("This class should not need to use this operation!");
    }

    @Override
    public void setNode(Node node) {
	// TODO remove if DirectNodeStore proves to be more effective
	throw new UnsupportedOperationException("This class should not need to use this operation!");
    }
}
