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

import prm4j.indexing.Event;

public class Symbol1<P1> extends Symbol {

    private final Parameter<P1> param1;

    Symbol1(Alphabet alphabet, int uniqueId, String uniqueName, Parameter<P1> param1) {
	super(alphabet, uniqueId, uniqueName, 1);
	this.param1 = param1;
	parameters.add(param1);
    }

    public Event<Symbol> createEvent(P1 obj1) {
	Object[] boundObjects = createObjectArray();
	bindObject(this.param1, obj1, boundObjects);
	return new Event<Symbol>(this, boundObjects);
    }

}
