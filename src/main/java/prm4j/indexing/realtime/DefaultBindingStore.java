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

import prm4j.api.Parameter;
import prm4j.indexing.map.MinimalMap;

public class DefaultBindingStore implements BindingStore {

    /**
     * Specifies the number of retrieve-operations after which the store will try to clean expired bindings. It will
     * poll the a reference queue and remove the bindings from the store, triggering their resource removal.
     */
    public static final int DEFAULT_CLEANING_INTERVAL = 1024;

    private final ReferenceQueue<Object> referenceQueue;
    private final Cleaner cleaner = new Cleaner();
    private final int cleaningInterval;
    private final int fullParameterCount;
    private final LowLevelBinding[] bindings;
    private final BindingFactory bindingFactory;

    private long createdBindingsCount;
    private long collectedBindingsCount;

    private MinimalMap<Object, LowLevelBinding> store;

    public DefaultBindingStore(BindingFactory bindingFactory, Set<Parameter<?>> fullParameterSet) {
	this(bindingFactory, fullParameterSet, DEFAULT_CLEANING_INTERVAL);
    }

    public DefaultBindingStore(BindingFactory bindingFactory, Set<Parameter<?>> fullParameterSet, int cleaningInterval) {
	this.bindingFactory = bindingFactory;
	fullParameterCount = fullParameterSet.size();
	this.cleaningInterval = cleaningInterval;
	referenceQueue = new ReferenceQueue<Object>();
	store = new DefaultStore();
	bindings = createInitialBindings();
    }

    private LowLevelBinding[] createInitialBindings() {
	LowLevelBinding[] result = new LowLevelBinding[fullParameterCount];
	for (int i = 0; i < result.length; i++) {
	    // fill the bindings-array with pseudo-bindings
	    result[i] = new DefaultLowLevelBinding(new Object(), 0, null, 0);
	}
	return result;
    }

    @Override
    public LowLevelBinding[] getBindings(Object[] boundObjects) {
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
    public LowLevelBinding getBinding(Object boundObject) {
	return store.get(boundObject);
    }

    @Override
    public LowLevelBinding getOrCreateBinding(Object boundObject) {
	return store.getOrCreate(boundObject);
    }

    @Override
    public boolean removeBinding(LowLevelBinding binding) {
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
    class DefaultStore extends MinimalMap<Object, LowLevelBinding> {

	@Override
	protected LowLevelBinding[] createTable(int size) {
	    return bindingFactory.createTable(size);
	}

	@Override
	protected LowLevelBinding createEntry(Object key, int hashCode) {
	    createdBindingsCount++;
	    return bindingFactory.createBinding(key, hashCode, referenceQueue, fullParameterCount);
	}
    }

    /**
     * Stores bindings associated to a single parameter
     */
    class UnaryStore extends MinimalMap<Object, LowLevelBinding> {

	@Override
	protected LowLevelBinding[] createTable(int size) {
	    return new UnaryLowLevelBinding[size];
	}

	@Override
	protected LowLevelBinding createEntry(Object key, int hashCode) {
	    createdBindingsCount++;
	    return new UnaryLowLevelBinding(key, hashCode, referenceQueue);
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
	    LowLevelBinding binding = (LowLevelBinding) referenceQueue.poll();
	    while (binding != null) {
		removeBinding(binding);
		binding.release();
		collectedBindingsCount++;
		binding = (LowLevelBinding) referenceQueue.poll();
	    }
	}
    }

    @Override
    public void reset() {
	if (fullParameterCount == 1) {
	    store = new UnaryStore();
	} else {
	    store = new DefaultStore();
	}
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
