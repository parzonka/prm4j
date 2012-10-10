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
package prm4j.indexing;

import prm4j.api.Symbol;

/**
 * A event contains a {@link Symbol} and a number of {@link Binding}s.
 *
 * @param <E>
 *            the type of the base event processed by monitors
 */
public class Event<E> {

    private final E baseEvent;
    private final Object[] boundObjects;

    public Event(E baseEvent, Object[] parameterValues) {
	this.baseEvent = baseEvent;
	this.boundObjects = parameterValues;
    }

    public E getSymbol() {
	return baseEvent;
    }

    public Object getBoundObject(int parameterId) {
	return this.boundObjects[parameterId];
    }

    public Object[] getBoundObjects() {
	return this.boundObjects;
    }

}
