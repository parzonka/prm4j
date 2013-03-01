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

import prm4j.indexing.map.MinimalMapEntry;

public interface Binding extends MinimalMapEntry<Object, Binding> {

    public Object get();

    /**
     * Releases all resources used in the indexing data structure and/or notifies monitors about unreachability of the
     * parameter object. Amount of released resources can vary strongly with the implementation.
     */
    void release();

    /**
     * Register a map which uses this binding as key.
     * 
     * @param nodeRef
     */
    void registerHolder(Holder<Binding> bindingHolder);

}
