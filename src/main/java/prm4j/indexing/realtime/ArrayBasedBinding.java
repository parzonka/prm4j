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
import java.util.Arrays;

public class ArrayBasedBinding extends AbstractBinding {

    private Holder<Binding>[] bindingHolders;
    private int bindingHoldersSize;

    public ArrayBasedBinding(Object boundObject, int hashCode, ReferenceQueue<Object> q, int initialHoldersSize) {
	super(boundObject, hashCode, q);
	// hijack the field to store the initial capacity needed for lazy creation of the nodeRefs array
	bindingHoldersSize = initialHoldersSize;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void registerHolder(Holder<Binding> bindingHolder) {
	// create bindingHoldersSize lazily
	if (bindingHolders == null) {
	    // we had hijacked the bindingHoldersSize field to store the initial bindingHolders capacity
	    bindingHolders = new Holder[bindingHoldersSize];
	    bindingHoldersSize = 0; // reset the actual size
	} else {
	    // ensure capacity
	    if (bindingHoldersSize >= bindingHolders.length) {
		final int capacity = (bindingHolders.length * 3) / 2 + 1;
		bindingHolders = Arrays.copyOf(bindingHolders, capacity);
	    }
	}
	bindingHolders[bindingHoldersSize++] = bindingHolder;
    }

    @Override
    public void release() {
	if (bindingHolders != null) {
	    for (int i = 0; i < bindingHoldersSize; i++) {
		bindingHolders[i].release(this);
	    }
	    bindingHolders = null;
	}
    }

}
