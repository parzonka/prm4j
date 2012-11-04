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

}
