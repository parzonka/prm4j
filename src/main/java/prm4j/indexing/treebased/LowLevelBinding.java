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
package prm4j.indexing.treebased;

/**
 * A binding used by optimized indexing strategies.
 *
 * @param <E>
 *            the type of base event processed by monitors
 */
public interface LowLevelBinding<E> extends prm4j.indexing.Binding {

    /**
     * Releases all resources used in the indexing data structure and/or notifies monitors about unreachability of the
     * parameter object. Amount of released resources can vary strongly with the implementation.
     */
    void release();

    /**
     * Register a map where this binding is used.
     *
     * @param mapReference
     */
//    void registerMap(MapReference<E> mapReference); // TODO resource registration

    /**
     * Return the node representing the instance this binding can form.
     *
     * @return
     */
    Node<E> getBaseNode();

    long getDisable();

    void setDisable(long disable);

    long getTau();

    void setTau(long tau);

}
