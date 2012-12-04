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

import prm4j.api.Parameter;

public interface BindingStore {

    /**
     * Returns an array of {@link LowLevelBinding}s modeling an instance for the given bound objects.
     *
     * @param boundObjects
     * @return the instance
     */
    public LowLevelBinding[] getBindingsNoCompression(Object[] boundObjects);

    public LowLevelBinding getBinding(Parameter<?> parameter, Object boundObject);

    public LowLevelBinding getOrCreateBinding(Parameter<?> parameter, Object boundObject);

    boolean removeBinding(LowLevelBinding binding);

    public int size();


}