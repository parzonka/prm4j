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

public interface BindingStore<A> {

    /**
     * Returns an instance for the given bound objects.
     *
     * @param boundObjects
     * @return
     */
    public Instance<A> getInstance(Object[] boundObjects);

    /**
     * Tests if a {@link LowLevelBinding} exists for the given object associated to the parameter with given id. As a
     * side-effect, an {@link LowLevelBinding} may be pre-fetched because an getBinding-operation is to be expected.
     *
     * @param parameterId
     * @param boundObject
     * @return <code>true</code> if a binding exists for the given bound object
     */
    public boolean contains(int parameterId, Object boundObject);

    /**
     * DESIGN Implementors must set base nodes.
     *
     * @param parameterId
     * @param boundObject
     * @return
     */
    public LowLevelBinding<A> getBinding(int parameterId, Object boundObject);

}
