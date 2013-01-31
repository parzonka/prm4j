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

public class Symbol3<P1, P2, P3> extends Symbol {

    private final Parameter<P1> param1;
    private final Parameter<P2> param2;
    private final Parameter<P3> param3;

    public Symbol3(Alphabet alphabet, int uniqueId, String uniqueName, Parameter<P1> param1, Parameter<P2> param2,
	    Parameter<P3> param3) {
	super(alphabet, uniqueId, uniqueName);
	this.param1 = param1;
	this.param2 = param2;
	this.param3 = param3;
	setParameters(param1, param2, param3);
    }

    public Event createEvent(P1 obj1, P2 obj2, P3 obj3) {
	Object[] boundObjects = createObjectArray();
	bindObject(this.param1, obj1, boundObjects);
	bindObject(this.param2, obj2, boundObjects);
	bindObject(this.param3, obj3, boundObjects);
	return new Event(this, boundObjects);
    }

    public Event createConditionalEvent(P1 obj1, P2 obj2, P3 obj3, Condition condition) {
	Object[] boundObjects = createObjectArray();
	bindObject(this.param1, obj1, boundObjects);
	bindObject(this.param2, obj2, boundObjects);
	bindObject(this.param3, obj3, boundObjects);
	return new Event(this, boundObjects, condition, null);
    }

    public Event createConditionalEvent(P1 obj1, P2 obj2, P3 obj3, Object auxiliaryData) {
	Object[] boundObjects = createObjectArray();
	bindObject(this.param1, obj1, boundObjects);
	bindObject(this.param2, obj2, boundObjects);
	bindObject(this.param3, obj3, boundObjects);
	return new Event(this, boundObjects, null, auxiliaryData);
    }

    public Event createConditionalEvent(P1 obj1, P2 obj2, P3 obj3, Condition condition, Object auxiliaryData) {
	Object[] boundObjects = createObjectArray();
	bindObject(this.param1, obj1, boundObjects);
	bindObject(this.param2, obj2, boundObjects);
	bindObject(this.param3, obj3, boundObjects);
	return new Event(this, boundObjects, condition, auxiliaryData);
    }

}
