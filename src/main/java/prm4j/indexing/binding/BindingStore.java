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

/**
 * Stores bindings for all monitored objects.
 */
public interface BindingStore {

    /**
     * Returns an uncompressed array of {@link Binding}s modeling an instance for the given bound objects.
     *
     * @param boundObjects
     *            (uncompressed)
     * @return bindings (uncompressed)
     */
    public Binding[] getBindings(Object[] boundObjects);

    /**
     * Retrieves the binding for a given bound object, or returns <code>null</code>, if the binding does not exist.
     *
     * @param boundObject
     * @return the binding
     */
    public Binding getBinding(Object boundObject);

    /**
     * Creates the binding for a given bound object, or retrieves it if it already exists.
     *
     * @param parameter
     * @return the binding
     */
    public Binding getOrCreateBinding(Object boundObject);

    /**
     * Removes a binding from the binding store.
     *
     * @param binding
     * @return <code>true</code> if the binding could found and removed successfully
     */
    public boolean removeBinding(Binding binding);

    public int size();

    public void reset();

    /**
     * DIAGNOSTIC
     *
     * @return the number of created bindings
     */
    public long getCreatedBindingsCount();

    /**
     * DIAGNOSTIC
     *
     * @return the number garbage collected bindings
     */
    public long getCollectedBindingsCount();

}