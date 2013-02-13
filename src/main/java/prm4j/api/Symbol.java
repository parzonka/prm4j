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
package prm4j.api;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Immutable.<br>
 * TODO ensure a symbol can be used only be one strategy
 */
public abstract class Symbol implements BaseEvent {

    private final Alphabet alphabet;
    private final int index;
    private final String uniqueName;
    private Set<Parameter<?>> parameterSet;
    private int[] parameterMask;

    protected Symbol(Alphabet alphabet, int index, String uniqueName) {
	super();
	this.alphabet = alphabet;
	this.index = index;
	this.uniqueName = uniqueName;
	parameterSet = new HashSet<Parameter<?>>();
    }

    @Override
    public int getIndex() {
	return index;
    }

    /**
     * Sets the set representation and the parameter mask representation of the parameters associated with this symbol.
     * 
     * @param parameters
     */
    protected void setParameters(Parameter<?>... parameters) {
	final Set<Parameter<?>> parameterSet = new HashSet<Parameter<?>>();
	for (int i = 0; i < parameters.length; i++) {
	    if (parameters[i] != null) {
		parameterSet.add(parameters[i]);
	    }
	}
	final int[] parameterMask = new int[parameterSet.size()];
	int j = 0;
	for (int i = 0; i < parameters.length; i++) {
	    if (parameters[i] != null) {
		parameterMask[j++] = parameters[i].getIndex();
	    }
	}
	assert parameterMask.length == parameterSet.size();

	Arrays.sort(parameterMask);
	this.parameterMask = parameterMask;
	this.parameterSet = parameterSet;
    }

    /**
     * Assigns the object to its position in boundObject identified by the index of the given parameter.
     * 
     * @param parameter
     * @param object
     * @param boundObjects
     */
    protected static <P> void bindObject(Parameter<P> parameter, P object, Object[] boundObjects) {
	assert boundObjects[parameter.getIndex()] == null : "Each parameter can be bound only once.";
	boundObjects[parameter.getIndex()] = object;
    }

    @Override
    public Set<Parameter<?>> getParameters() {
	return Collections.unmodifiableSet(parameterSet);
    }

    @Override
    public int getParameterCount() {
	return parameterSet.size();
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

    @Override
    public int[] getParameterMask() {
	return parameterMask;
    }

}
