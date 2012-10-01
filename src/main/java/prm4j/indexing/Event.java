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
 * @param <A>
 *            the type of the auxiliary data usable by base monitors
 */
public class Event<A> {

    private final Symbol symbol;
    private final A auxiliaryData;
    private final Object[] boundObjects;

    public Event(Symbol symbol, Object[] parameterObjects) {
	this.symbol = symbol;
	this.auxiliaryData = null;
	this.boundObjects = parameterObjects;
    }

    public Event(Symbol symbol, A auxiliaryData, Object[] parameterObjects) {
	this.symbol = symbol;
	this.auxiliaryData = auxiliaryData;
	this.boundObjects = parameterObjects;
    }

    public Symbol getSymbol() {
	return this.symbol;
    }

    public A getAuxiliaryData() {
	return this.auxiliaryData;
    }

    public Object getBoundObject(int parameterId) {
	return this.boundObjects[parameterId];
    }

    public Object[] getBoundObjects() {
	return this.boundObjects;
    }

}
