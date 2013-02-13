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
 * A parameter which can be bound to an object.
 *
 * @param <T>
 *            the type of the object which can be bound to this parameter
 */
public class Parameter<T> implements Comparable<Parameter<?>> {

    private int parameterIndex = -1;
    private final String uniqueName;
    private boolean isStrong = false;

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

    public boolean isStrong() {
	return this.isStrong;
    }

    public void setStrong(boolean isStrong) {
	this.isStrong = isStrong;
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
