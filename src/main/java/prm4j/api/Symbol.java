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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Immutable.<br>
 * TODO ensure a symbol can be used only be one strategy
 */
public abstract class Symbol {

    private final Alphabet alphabet;
    private final int index;
    private final String uniqueName;
    private final int parameterCount;
    protected final Set<Parameter<?>> parameters;

    protected Symbol(Alphabet alphabet, int index, String uniqueName, int parameterCount) {
	super();
	this.alphabet = alphabet;
	this.index = index;
	this.uniqueName = uniqueName;
	parameters = new HashSet<Parameter<?>>();
	this.parameterCount = parameterCount;
    }

    /**
     * Returns the unique index for this symbol.
     *
     * @return unique index number
     */
    public int getIndex() {
	return index;
    }

    /**
     * Assigns the object to its position in boundObject identified by the given parameter id.
     *
     * @param parameter
     * @param object
     * @param boundObjects
     */
    protected static <P> void bindObject(Parameter<P> parameter, P object, Object[] boundObjects) {
	assert boundObjects[parameter.getParameterId()] == null : "Each parameter can be bound only once.";
	boundObjects[parameter.getParameterId()] = object;
    }

    /**
     * Returns a immutable representation of the associated parameters for this symbol.
     *
     * @return immutable set of symbols
     */
    public Set<Parameter<?>> getParameters() {
	return Collections.unmodifiableSet(parameters);
    }

    /**
     * Returns the number of parameters which this symbol is able to bind.
     *
     * @return the parameter count
     */
    public int getParameterCount() {
	return parameterCount;
    }

    /**
     * Returns an array of objects, where the position in the array equals the index of the bound parameter.
     *
     * @return the array of bound objects
     */
    protected Object[] createObjectArray() {
	return new Object[alphabet.getParameterCount()];
    }

    @Override
    public String toString() {
	return uniqueName;
    }

}
