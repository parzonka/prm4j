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
import java.util.Set;

import prm4j.Util;
import prm4j.api.Parameter;
import prm4j.indexing.map.MinimalMap;

public class DefaultBindingStore implements BindingStore {

    private final ReferenceQueue<Object> referenceQueue;
    private final MinimalMap<Object, DefaultLowLevelBinding>[] stores;
    private final Cleaner cleaner = new Cleaner();
    private final int cleaningInterval;

    @SuppressWarnings("unchecked")
    public DefaultBindingStore(Set<Parameter<?>> fullParameterSet, int cleaningInterval) {

	referenceQueue = new ReferenceQueue<Object>();
	this.cleaningInterval = cleaningInterval;

	stores = new MinimalMap[fullParameterSet.size()];
	for (Parameter<?> parameter : Util.asSortedList(fullParameterSet)) {
	    stores[parameter.getIndex()] = new SingleBindingStore(parameter);
	}
    }

    @Override
    public LowLevelBinding[] getBindings(Object[] boundObjects) {
	int objectsCount = 0;
	for (int i = 0; i < boundObjects.length; i++) {
	    if (boundObjects[i] != null) {
		objectsCount++;
	    }
	}
	LowLevelBinding[] result = new LowLevelBinding[objectsCount];
	int j = 0;
	for (int i = 0; i < boundObjects.length; i++) {
	    final Object boundObject = boundObjects[i];
	    if (boundObject != null) {
		result[j++] = stores[i].getOrCreate(boundObject);
	    }
	}
	cleaner.clean();
	return result;
    }

    @Override
    public LowLevelBinding getBinding(Parameter<?> parameter, Object boundObject) {
	return stores[parameter.getIndex()].get(boundObject);
    }

    protected ReferenceQueue<Object> getReferenceQueue() {
	return referenceQueue;
    }

    protected void removeExpiredBindingsNow() {
	cleaner.removeExpiredBindings();
    }

    /**
     * Stores the bindings associated to a single parameter
     */
    class SingleBindingStore extends MinimalMap<Object, DefaultLowLevelBinding> {

	private final Parameter<?> parameter;

	public SingleBindingStore(Parameter<?> parameter) {
	    this.parameter = parameter;
	}

	@Override
	protected DefaultLowLevelBinding[] createTable(int size) {
	    return new DefaultLowLevelBinding[size];
	}

	@Override
	protected DefaultLowLevelBinding createEntry(Object key, int hashCode) {
	    return new DefaultLowLevelBinding(key, parameter, hashCode, referenceQueue);
	}
    }

    class Cleaner {

	private int attempts = 0;

	public void clean() {
	    if (attempts++ > cleaningInterval) {
		removeExpiredBindings();
		attempts = 0;
	    }
	}

	private void removeExpiredBindings() {
	    DefaultLowLevelBinding binding = (DefaultLowLevelBinding) referenceQueue.poll();
	    while (binding != null) {
		stores[binding.getParameterIndex()].removeEntry(binding);
		binding.release();
		binding = (DefaultLowLevelBinding) referenceQueue.poll();
	    }
	}
    }

}
