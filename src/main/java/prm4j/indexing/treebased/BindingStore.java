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

    public LowLevelBinding<A> getBinding(Object object);

    /**
     * Tests if a IBinding exists for the given object. As a side-effect, an IBinding may be pre-fetched because an
     * getBinding-operation is to be expected.
     *
     * @param object
     * @param boundObject
     * @return
     */
    public boolean contains(int parameterObject, Object boundObject);

    /**
     * DESIGN Implementors must set base nodes.
     *
     * @param parameterId
     * @param boundObject
     * @return
     */
    public LowLevelBinding<A> getBinding(int parameterId, Object boundObject);

}
