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

import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Set;

import prm4j.api.Parameter;

/**
 * {@link DefaultBindingStore} which is aware of all bindings it produces and retrieves.
 */
public class AwareDefaultBindingStore extends DefaultBindingStore {

    private final Deque<WeakReference<LowLevelBinding[]>> retrievedBindings;

    public AwareDefaultBindingStore(Set<Parameter<?>> fullParameterSet, int cleaningInterval) {
	super(fullParameterSet, cleaningInterval);
	retrievedBindings = new ArrayDeque<WeakReference<LowLevelBinding[]>>();
    }

    @Override
    public LowLevelBinding[] getBindingsNoCompression(Object[] boundObjects) {
	LowLevelBinding[] bindings = super.getBindingsNoCompression(boundObjects);
	getListOfBindings().add(new WeakReference<LowLevelBinding[]>(bindings));
	return bindings;
    }

    public Deque<WeakReference<LowLevelBinding[]>> getListOfBindings() {
	return retrievedBindings;
    }

    private static LowLevelBinding[] toCompressedBindings(LowLevelBinding[] uncompressedBindings) {
	int objectsCount = 0;
	for (int i = 0; i < uncompressedBindings.length; i++) {
	    if (uncompressedBindings[i] != null) {
		objectsCount++;
	    }
	}
	LowLevelBinding[] result = new LowLevelBinding[objectsCount];
	int j = 0;
	for (int i = 0; i < uncompressedBindings.length; i++) {
	    final LowLevelBinding boundObject = uncompressedBindings[i];
	    if (boundObject != null) {
		result[j++] = boundObject;
	    }
	}
	return result;
    }

}
