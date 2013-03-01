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

/**
 * A parameter can be bound to an object (aka parameter value).
 * 
 * @param <T>
 *            the type of the object which can be bound to this parameter
 */
public class Parameter<T> implements Comparable<Parameter<?>> {

    private int parameterIndex = -1;
    private final String uniqueName;
    private boolean isPersistent = false;

    public Parameter(String uniqueName) {
	this.uniqueName = uniqueName;
    }

    /**
     * Returns a unique index for this parameter. It is mandatory that all indexes are in the interval [0, ..., n-1]
     * with n = number of parameters.
     * 
     * @return
     */
    public final int getIndex() {
	if (parameterIndex < 0) {
	    throw new IllegalStateException("Parameter index was not set yet.");
	}
	return this.parameterIndex;
    }

    public void setIndex(int parameterIndex) {
	if (parameterIndex < 0) {
	    throw new IllegalStateException("Parameter index has to be non-negative.");
	}
	this.parameterIndex = parameterIndex;
    }

    /**
     * @return <code>true</code>, if associated parameter values should be kept in memory as long as the matches are
     *         possible involving this parameter.
     */
    public boolean isPersistent() {
	return this.isPersistent;
    }

    public void setPersistent(boolean isPersistent) {
	this.isPersistent = isPersistent;
    }

    @Override
    public String toString() {
	return this.uniqueName;
    }

    @Override
    public int compareTo(Parameter<?> o) {
	if (parameterIndex < 0) {
	    throw new IllegalStateException("Parameter index was not set yet.");
	}
	if (parameterIndex == o.getIndex()) {
	    throw new IllegalStateException("Indices must be different.");
	}
	return parameterIndex - o.getIndex();
    }

}
