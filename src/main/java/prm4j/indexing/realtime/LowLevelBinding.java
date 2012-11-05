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

/**
 * A binding used by optimized indexing strategies.
 */
public interface LowLevelBinding extends prm4j.api.Binding {

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
//    void registerMap(MapReference mapReference); // TODO resource registration

    /**
     * Return the node representing the instance this binding can form.
     *
     * @return
     */
    Node getBaseNode();

    boolean isDisabled();

    void setDisabled(boolean disable);

    long getTimestamp();

    void setTimestamp(long timestamp);

}
