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
package prm4j.api;

import java.util.Set;

public interface BaseEvent {

    /**
     * Returns the unique index for this base event.
     *
     * @return unique index number
     */
    public int getIndex();

    /**
     * Returns a immutable representation of the associated parameters for this base event.
     *
     * @return immutable set of symbols
     */
    public Set<Parameter<?>> getParameters();

    /**
     * Returns the number of parameters which this base event is able to bind.
     *
     * @return the parameter count
     */
    public int getParameterCount();

    /**
     * Returns an compressed (without null-values) array representation of all parameter indices of the associated
     * parameters to this base event.
     *
     * @return uncompressed parameter indices
     */
    public int[] getParameterMask();

}
