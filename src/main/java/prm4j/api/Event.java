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

import java.util.Arrays;

/**
 * An event contains a generic base event and a number of {@link Binding}s.
 */
public class Event {

    private final BaseEvent baseEvent;
    private final Object[] boundObjects;
    private final Object auxiliaryData;

    public Event(BaseEvent baseEvent, Object[] parameterValues) {
	this(baseEvent, parameterValues, null);
    }

    public Event(BaseEvent baseEvent, Object[] parameterValues, Object auxiliaryData) {
	this.baseEvent = baseEvent;
	boundObjects = parameterValues;
	this.auxiliaryData = auxiliaryData;
    }

    public BaseEvent getBaseEvent() {
	return baseEvent;
    }

    public Object getBoundObject(int parameterId) {
	return boundObjects[parameterId];
    }

    public Object[] getBoundObjects() {
	return boundObjects;
    }

    public Object getAuxiliaryData() {
	return auxiliaryData;
    }

    @Override
    public String toString() {
        return baseEvent.toString() + Arrays.toString(boundObjects);
    }

}
