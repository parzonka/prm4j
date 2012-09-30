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

public class Symbol2<P1, P2> extends Symbol {

    private final Parameter<P1> param1;
    private final Parameter<P2> param2;

    Symbol2(Alphabet alphabet, int uniqueId, String uniqueName, Parameter<P1> param1, Parameter<P2> param2) {
	super(alphabet, uniqueId, uniqueName, 2);
	this.param1 = param1;
	this.param2 = param2;
	parameters.add(param1);
	parameters.add(this.param2);
    }

    public Event<Void> createEvent(P1 obj1, P2 obj2) {
	Object[] boundObjects = createObjectArray();
	bindObject(this.param1, obj1, boundObjects);
	bindObject(this.param2, obj2, boundObjects);
	return new Event<Void>(this, boundObjects);
    }

    public <L> Event<L> createLabeledEvent(L label, P1 obj1, P2 obj2) {
	Object[] boundObjects = createObjectArray();
	bindObject(this.param1, obj1, boundObjects);
	bindObject(this.param2, obj2, boundObjects);
	return new Event<L>(this, label, boundObjects);
    }

}
