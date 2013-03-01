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

import java.lang.ref.ReferenceQueue;
import java.util.Set;

import prm4j.Globals;
import prm4j.api.Parameter;
import prm4j.indexing.map.MinimalMap;

public class DefaultBindingStore implements BindingStore {

    private final ReferenceQueue<Object> referenceQueue;
    private final Cleaner cleaner = new Cleaner();
    private final int cleaningInterval;
    private final int fullParameterCount;
    private final Binding[] bindings;
    private final BindingFactory bindingFactory;

    private long createdBindingsCount;
    private long collectedBindingsCount;

    private MinimalMap<Object, Binding> store;

    public DefaultBindingStore(BindingFactory bindingFactory, Set<Parameter<?>> fullParameterSet) {
	this(bindingFactory, fullParameterSet, Globals.BINDING_CLEANING_INTERVAL);
    }

    public DefaultBindingStore(BindingFactory bindingFactory, Set<Parameter<?>> fullParameterSet, int cleaningInterval) {
	this.bindingFactory = bindingFactory;
	fullParameterCount = fullParameterSet.size();
	this.cleaningInterval = cleaningInterval;
	referenceQueue = new ReferenceQueue<Object>();
	store = new DefaultStore();
	bindings = createInitialBindings();
    }

    private Binding[] createInitialBindings() {
	Binding[] result = new Binding[fullParameterCount];
	for (int i = 0; i < result.length; i++) {
	    // fill the bindings-array with pseudo-bindings
	    result[i] = new ArrayBasedBinding(new Object(), 0, null, 0);
	}
	return result;
    }

    @Override
    public Binding[] getBindings(Object[] boundObjects) {
	assert boundObjects.length == fullParameterCount;
	for (int i = 0; i < boundObjects.length; i++) {
	    final Object boundObject = boundObjects[i];
	    // the bindings-array serves as a very basic cache
	    if (boundObject != null && boundObject != bindings[i].get()) {
		bindings[i] = store.getOrCreate(boundObject);
	    }
	}
	cleaner.clean();
	return bindings;
    }

    @Override
    public Binding getBinding(Object boundObject) {
	return store.get(boundObject);
    }

    @Override
    public Binding getOrCreateBinding(Object boundObject) {
	return store.getOrCreate(boundObject);
    }

    @Override
    public boolean removeBinding(Binding binding) {
	return store.removeEntry(binding);
    }

    @Override
    public int size() {
	return store.size();
    }

    protected ReferenceQueue<Object> getReferenceQueue() {
	return referenceQueue;
    }

    public void removeExpiredBindingsNow() {
	cleaner.removeExpiredBindings();
    }

    /**
     * Stores bindings associated to a single parameter
     */
    class DefaultStore extends MinimalMap<Object, Binding> {

	@Override
	protected Binding[] createTable(int size) {
	    return bindingFactory.createTable(size);
	}

	@Override
	protected Binding createEntry(Object key, int hashCode) {
	    createdBindingsCount++;
	    return bindingFactory.createBinding(key, hashCode, referenceQueue, fullParameterCount);
	}
    }

    class Cleaner {

	private int attempts = 0;

	public void clean() {
	    if (attempts++ >= cleaningInterval) {
		removeExpiredBindings();
		attempts = 0;
	    }
	}

	private void removeExpiredBindings() {
	    Binding binding = (Binding) referenceQueue.poll();
	    while (binding != null) {
		removeBinding(binding);
		binding.release();
		collectedBindingsCount++;
		binding = (Binding) referenceQueue.poll();
	    }
	}
    }

    @Override
    public void reset() {
	store = new DefaultStore();
	System.gc();
	removeExpiredBindingsNow();
	System.gc();
	createdBindingsCount = 0L;
	collectedBindingsCount = 0L;
    }

    @Override
    public long getCreatedBindingsCount() {
	return createdBindingsCount;
    }

    @Override
    public long getCollectedBindingsCount() {
	return collectedBindingsCount;
    }

}
