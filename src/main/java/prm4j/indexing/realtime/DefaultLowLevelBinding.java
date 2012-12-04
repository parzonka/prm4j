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
import java.util.ArrayList;
import java.util.List;

import prm4j.api.Parameter;
import prm4j.indexing.map.MinimalMapEntry;

public class DefaultLowLevelBinding extends WeakReference<Object> implements LowLevelBinding,
	MinimalMapEntry<Object, DefaultLowLevelBinding> {

    private long timestamp;
    private boolean disabled;
    private final int hashCode;
    @Deprecated
    private final Parameter<?> parameter;
    private DefaultLowLevelBinding next;
    private List<WeakReference<Node>> nodeRefs;

    @Deprecated
    public DefaultLowLevelBinding(Object boundObject, Parameter<?> parameter, int hashCode, ReferenceQueue<Object> q) {
	super(boundObject, q);
	this.hashCode = hashCode;
	this.parameter = parameter;
	timestamp = Long.MAX_VALUE; // indicates the binding was just created
	nodeRefs = new ArrayList<WeakReference<Node>>();
    }

    public DefaultLowLevelBinding(Object boundObject, int hashCode, ReferenceQueue<Object> q) {
	super(boundObject, q);
	this.hashCode = hashCode;
	parameter = null;
	timestamp = Long.MAX_VALUE; // indicates the binding was just created
	nodeRefs = new ArrayList<WeakReference<Node>>();
    }

    @Deprecated
    @Override
    public Parameter<?> getParameter() {
	return parameter;
    }

    @Deprecated
    @Override
    public int getParameterIndex() {
	return parameter.getIndex();
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
    public DefaultLowLevelBinding next() {
	return next;
    }

    @Override
    public void setNext(DefaultLowLevelBinding next) {
	this.next = next;
    }

    @Override
    public void registerNode(WeakReference<Node> nodeReference) {
	nodeRefs.add(nodeReference);
    }

    @Override
    public void release() {
	for (WeakReference<Node> ref : nodeRefs) {
	    final Node node = ref.get();
	    if (node != null) {
		node.remove(this);
	    }
	}
	nodeRefs = null;
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
	return parameter + "=" + get();
    }

    @Override
    @Deprecated
    public int compareTo(LowLevelBinding o) {
	return this.getParameterIndex() - o.getParameterIndex();
    }

}
