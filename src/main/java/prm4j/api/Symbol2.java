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

public class Symbol2<P1, P2> extends Symbol {

    private final Parameter<P1> param1;
    private final Parameter<P2> param2;

    Symbol2(Alphabet alphabet, int uniqueId, String uniqueName, Parameter<P1> param1, Parameter<P2> param2) {
	super(alphabet, uniqueId, uniqueName);
	this.param1 = param1;
	this.param2 = param2;
	setParameters(param1, param2);
    }

    public Event createEvent(P1 obj1, P2 obj2) {
	Object[] boundObjects = createObjectArray();
	bindObject(this.param1, obj1, boundObjects);
	bindObject(this.param2, obj2, boundObjects);
	return new Event(this, boundObjects);
    }

    public Event createEvent(P1 obj1, P2 obj2, Object auxiliaryData) {
	Object[] boundObjects = createObjectArray();
	bindObject(this.param1, obj1, boundObjects);
	bindObject(this.param2, obj2, boundObjects);
	return new Event(this, boundObjects, null, auxiliaryData);
    }

    public Event createConditionalEvent(P1 obj1, P2 obj2, Condition condition) {
	Object[] boundObjects = createObjectArray();
	bindObject(this.param1, obj1, boundObjects);
	bindObject(this.param2, obj2, boundObjects);
	return new Event(this, boundObjects, condition, null);
    }

    public Event createConditionalEvent(P1 obj1, P2 obj2, Condition condition, Object auxiliaryData) {
	Object[] boundObjects = createObjectArray();
	bindObject(this.param1, obj1, boundObjects);
	bindObject(this.param2, obj2, boundObjects);
	return new Event(this, boundObjects, condition, auxiliaryData);
    }

}
