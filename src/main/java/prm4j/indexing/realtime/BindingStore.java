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

public class BindingStore {

    ReferenceQueue<Object> referenceQueue;
    MinimalMap<Object, DefaultLowLevelBinding>[] stores;

    @SuppressWarnings("unchecked")
    public BindingStore(Set<Parameter<?>> fullParameterSet) {

	referenceQueue = new ReferenceQueue<Object>();
	final int parameterCount = fullParameterSet.size();
	stores = new MinimalMap[parameterCount];
	for (int i = 0; i < parameterCount; i++) {
	    // TODO
	}
    }

    /**
     * Returns an instance for the given bound objects.
     *
     * @param boundObjects
     * @return
     */
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
		result[j++] = stores[i].get(boundObject);
	    }
	}
	return result;
    }

    class SingleBindingStore extends MinimalMap<Object, DefaultLowLevelBinding> {

	final Parameter<?> parameter;

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

}
