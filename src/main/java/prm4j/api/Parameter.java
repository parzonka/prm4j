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

/**
 * A parameter which can be bound to an object.
 *
 * @param <T>
 *            the type of the object which can be bound to this parameter
 */
public class Parameter<T> {

    private int parameterId = -1;
    private final String uniqueName;
    private boolean isStrong = false;

    public Parameter(String uniqueName) {
	this.uniqueName = uniqueName;
    }

    protected final int getParameterId() {
	return this.parameterId;
    }

    protected void setParameterId(int parameterId) {
	this.parameterId = parameterId;
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

}
