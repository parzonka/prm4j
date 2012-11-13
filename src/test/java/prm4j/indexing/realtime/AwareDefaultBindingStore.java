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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import prm4j.api.Parameter;

/**
 * {@link DefaultBindingStore} which is aware of all bindings it produces and retrieves.
 */
public class AwareDefaultBindingStore extends DefaultBindingStore {

    private final List<WeakReference<LowLevelBinding[]>> listOfBindings;

    public AwareDefaultBindingStore(Set<Parameter<?>> fullParameterSet, int cleaningInterval) {
	super(fullParameterSet, cleaningInterval);
	listOfBindings = new ArrayList<WeakReference<LowLevelBinding[]>>();
    }

    @Override
    public LowLevelBinding[] getBindings(Object[] boundObjects) {
	LowLevelBinding[] bindings = super.getBindings(boundObjects);
	getListOfBindings().add(new WeakReference<LowLevelBinding[]>(bindings));
	return bindings;
    }

    public List<WeakReference<LowLevelBinding[]>> getListOfBindings() {
	return listOfBindings;
    }

}
