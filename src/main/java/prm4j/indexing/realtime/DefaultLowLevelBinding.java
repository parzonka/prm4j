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
    private final int parameterIndex;
    private DefaultLowLevelBinding next;
    private List<WeakReference<Node>> nodeRefs;

    public DefaultLowLevelBinding(Object boundObject, Parameter<?> parameter, int hashCode, ReferenceQueue<Object> q) {
	super(boundObject, q);
	this.hashCode = hashCode;
	parameterIndex = parameter.getIndex();
	nodeRefs = new ArrayList<WeakReference<Node>>();
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
