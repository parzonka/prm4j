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
package prm4j.indexing.map;

import prm4j.indexing.binding.Binding;

public interface NodeMapEntry<E extends NodeMapEntry<E>>{

    public Binding getKey();

    public int parameterIndex();

    public E next();

    public void setNext(E nextEntry);

}
