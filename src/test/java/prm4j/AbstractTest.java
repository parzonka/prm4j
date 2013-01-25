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
package prm4j;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;

import prm4j.api.BaseEvent;
import prm4j.api.Event;
import prm4j.api.Parameter;
import prm4j.api.Symbol;
import prm4j.util.FSMDefinitions;

/**
 * Provides utilities for testing.
 */
public abstract class AbstractTest extends FSMDefinitions /* we mix in other definitions layer by layer */{

    public final static Set<Parameter<?>> EMPTY_PARAMETER_SET = new HashSet<Parameter<?>>();

    private static int boundObjectCounter = 0;
    private static int instanceCounter = 0;

    @Before
    public void resetCounters() {
	boundObjectCounter = 0;
	instanceCounter = 0;
    }

    /**
     * Downcast SymbolN to Symbol.
     *
     * @param values
     * @return
     */
    public static Set<Symbol> asSet(Symbol... values) {
	Set<Symbol> set = new HashSet<Symbol>();
	for (Symbol s : values) {
	    set.add(s);
	}
	return set;
    }

    public static Set<Parameter<?>> asSet(Parameter<?>... values) {
	Set<Parameter<?>> set = new HashSet<Parameter<?>>();
	for (Parameter<?> s : values) {
	    set.add(s);
	}
	return set;
    }

    @SuppressWarnings("unchecked")
    public static <T> Set<T> asSet(T... values) {
	Set<Object> set = new HashSet<Object>();
	for (Object s : values) {
	    set.add(s);
	}
	return (Set<T>) set;
    }

    public static <T> List<T> list(T... objects) {
	List<T> result = new ArrayList<T>();
	for (T t : objects)
	    result.add(t);
	return result;
    }

    public static <T> int[] array(int... objects) {
	return objects;
    }

    public static <T> boolean[] array(boolean... objects) {
	return objects;
    }

    public static <T> T[] array(T... objects) {
	return objects;
    }

    /**
     * assertArrayEquals(boolean[] a1, boolean[] a2) not implemented in jUnit for some reason, see
     * https://github.com/KentBeck/junit/issues/86
     *
     * @param expected
     * @param actual
     */
    public static void assertBooleanArrayEquals(boolean[] expected, boolean[] actual) {
	if (expected == null && actual == null) {
	    return;
	}
	if (expected == null) {
	    fail("Actual array was not null!");
	}
	if (actual == null) {
	    fail("Actual array was null!");
	}
	if (expected.length != actual.length)
	    fail("Expected:<" + Arrays.toString(expected) + "> but was: <" + Arrays.toString(actual) + ">");
	for (int i = 0; i < expected.length; i++) {
	    if (expected[i] != actual[i])
		fail();
	}
    }

    public static void assert2DimArrayEquals(Object[][] expected, Object[][] actual) {
	if (expected.length != actual.length)
	    fail("Expected:<" + Arrays.toString(expected) + "> but was: <" + Arrays.toString(actual) + ">");
	for (int i = 0; i < expected.length; i++) {
	    if (expected[i].length != actual[i].length)
		fail("2-dim array differs in row " + i + ". Expected:<" + Arrays.toString(expected[i]) + "> but was: <"
			+ Arrays.toString(actual[i]) + ">");
	    for (int j = 0; j < expected[i].length; j++) {
		if (!expected[i][j].equals(actual[i][j])) {
		    fail("2-dim array differs in at [" + i + "][" + j + "]. Expected:<" + expected[i][j].toString()
			    + "> but was: <" + actual[i][j].toString() + ">");
		}
	    }
	}
    }

    protected static void runGarbageCollectorAFewTimes() {
	System.gc();
	System.gc();
	System.gc();
	System.gc();
	System.gc();
	System.gc();
	System.gc();
	System.gc();
    }

    public class ParametricInstance {

	final int instanceId;
	private final BoundObject[] boundObjects;
	final String[] boundObjectIds;

	public ParametricInstance(BoundObject[] boundObjects) {
	    this.boundObjects = new BoundObject[boundObjects.length];
	    boundObjectIds = new String[boundObjects.length];
	    for (int i = 0; i < boundObjects.length; i++) {
		BoundObject boundObject = boundObjects[i];
		if (boundObject == null) {
		    boundObject = new BoundObject();
		}
		this.getBoundObjects()[i] = boundObject;
		boundObjectIds[i] = boundObject.id;
	    }
	    instanceId = instanceCounter++;
	}

	public Event createEvent(BaseEvent baseEvent) {
	    final Object[] obj = new Object[getBoundObjects().length];
	    for (Parameter<?> parameter : baseEvent.getParameters()) {
		obj[parameter.getIndex()] = getBoundObjects()[parameter.getIndex()];
	    }
	    return new Event(baseEvent, obj, null, instanceId);
	}

	public List<Event> createEvents(BaseEvent... baseEvents) {
	    final List<Event> result = new ArrayList<Event>();
	    for (BaseEvent baseEvent : baseEvents) {
		final Object[] objects = new Object[getBoundObjects().length];
		for (Parameter<?> parameter : baseEvent.getParameters()) {
		    objects[parameter.getIndex()] = getBoundObjects()[parameter.getIndex()];
		}
		result.add(new Event(baseEvent, objects, null, instanceId));
	    }
	    return result;
	}

	public BoundObject[] getBoundObjects() {
	    return boundObjects;
	}
    }

    public ParametricInstance instance(BoundObject... boundObjects) {
	return new ParametricInstance(boundObjects);
    }

    public class BoundObject {

	public final String id;

	public BoundObject(String id) {
	    this.id = id;
	}

	public BoundObject() {
	    id = "" + boundObjectCounter++;
	}

	@Override
	public String toString() {
	    return id;
	}

    }

}
