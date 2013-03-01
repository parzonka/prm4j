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

/**
 * A binding encapsulates the bound object (aka parameter value). The Binding is agnostic regarding the parameter it is
 * bound to, it is only known that there exists at least one parameter which is bound to it. A Binding may be bound to
 * multiple parameters.
 */
public interface Binding extends MinimalMapEntry<Object, Binding> {

    /**
     * @return the bound object (parameter value9
     */
    public Object get();

    /**
     * Releases all resources used in the indexing data structure and/or notifies monitors about unreachability of the
     * parameter object. Amount of released resources can vary with the implementation.
     */
    void release();

    /**
     * Register a holder which uses this binding as a resource. This is normally a node in an indexing tree.
     * 
     * @param bindingHolder
     */
    void registerHolder(Holder<Binding> bindingHolder);

}
