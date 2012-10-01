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

import prm4j.indexing.Binding;

public interface Instance<A> {

    public Binding[] getBindings();

    /**
     * Return the associated node for this instance.
     *
     * @return the associated node
     */
    public Node<A> getNode();

    /**
     * Return the associated node for the sub-instance selected by the given parameterMask.
     *
     * @param parameterMask
     *            each int selects a binding from the instance bindings
     * @return the associated node
     */
    public Node<A> getNode(int[] parameterMask);

}
