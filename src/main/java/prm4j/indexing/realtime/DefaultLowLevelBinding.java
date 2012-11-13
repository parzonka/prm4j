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

import prm4j.api.Parameter;
import prm4j.indexing.map.MinimalMapEntry;

public class DefaultLowLevelBinding extends WeakReference<Object> implements LowLevelBinding,
	MinimalMapEntry<Object, DefaultLowLevelBinding> {

    private long timestamp;
    private boolean disabled;
    private final int hashCode;
    private final int parameterIndex;
    private DefaultLowLevelBinding next;

    public DefaultLowLevelBinding(Object boundObject, Parameter<?> parameter, int hashCode, ReferenceQueue<Object> q) {
	super(boundObject, q);
	this.hashCode = hashCode;
	parameterIndex = parameter.getIndex();
    }

    @Override
    public int getParameterId() {
	return parameterIndex;
    }

    @Override
    public int getHashCode() {
	return hashCode;
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
    public DefaultLowLevelBinding next() {
	return next;
    }

    @Override
    public void setNext(DefaultLowLevelBinding next) {
	this.next = next;
    }

    @Override
    public void release() {
	// TODO Garbage collection from bindings
    }

    @Override
    public Node getBaseNode() {
	return null;
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

}
