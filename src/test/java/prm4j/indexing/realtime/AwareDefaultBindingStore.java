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

import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Set;

import prm4j.api.Parameter;

/**
 * {@link DefaultBindingStore} which is aware of all bindings it produces and retrieves.
 */
public class AwareDefaultBindingStore extends DefaultBindingStore {

    private final Deque<WeakReference<Binding[]>> retrievedBindings;

    public AwareDefaultBindingStore(Set<Parameter<?>> fullParameterSet, int cleaningInterval) {
	super(new ArrayBasedBindingFactory(), fullParameterSet, cleaningInterval);
	retrievedBindings = new ArrayDeque<WeakReference<Binding[]>>();
    }

    @Override
    public Binding[] getBindings(Object[] boundObjects) {
	Binding[] bindings = super.getBindings(boundObjects);
	getListOfBindings().add(new WeakReference<Binding[]>(bindings));
	return bindings;
    }

    public Deque<WeakReference<Binding[]>> getListOfBindings() {
	return retrievedBindings;
    }

}
