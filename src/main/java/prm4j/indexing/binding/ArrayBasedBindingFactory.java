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
package prm4j.indexing.binding;

import java.lang.ref.ReferenceQueue;

public class ArrayBasedBindingFactory implements BindingFactory {

    @Override
    public Binding[] createTable(int size) {
	return new ArrayBasedBinding[size];
    }

    @Override
    public Binding createBinding(Object boundObject, int hashCode, ReferenceQueue<Object> referenceQueue,
	    int fullParameterCount) {
	return new ArrayBasedBinding(boundObject, hashCode, referenceQueue, fullParameterCount);
    }

}
