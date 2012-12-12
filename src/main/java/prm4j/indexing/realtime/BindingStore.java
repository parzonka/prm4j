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
 * Stores bindings for all monitored objects.
 */
public interface BindingStore {

    /**
     * Returns an uncompressed array of {@link LowLevelBinding}s modeling an instance for the given bound objects.
     *
     * @param boundObjects
     *            (uncompressed)
     * @return bindings (uncompressed)
     */
    public LowLevelBinding[] getBindings(Object[] boundObjects);

    /**
     * Retrieves the binding for a given bound object, or returns <code>null</code>, if the binding does not exist.
     *
     * @param boundObject
     * @return the binding
     */
    public LowLevelBinding getBinding(Object boundObject);

    /**
     * Creates the binding for a given bound object, or retrieves it if it already exists.
     *
     * @param parameter
     * @return the binding
     */
    public LowLevelBinding getOrCreateBinding(Object boundObject);

    /**
     * Removes a binding from the binding store.
     *
     * @param binding
     * @return <code>true</code> if the binding could found and removed successfully
     */
    public boolean removeBinding(LowLevelBinding binding);

    public int size();

    public void reset();

}