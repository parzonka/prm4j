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
package prm4j.logic.treebased;

/**
 * Represents all less informative instances of the event instance which match its enable sets and are associated with
 * symbols, PLUS the compatible partial instances which map to parameter sets to fully instantiate joinable instances.
 */
public class ChainingData {

    private final int[] parameterMask;
    private final int[] parameterSetIds;

    public ChainingData(int[] parameterMask, int[] parameterSetIds) {
	super();
	this.parameterMask = parameterMask;
	this.parameterSetIds = parameterSetIds;
    }

    public int[] getParameterMask() {
	return parameterMask;
    }

    public int[] getParameterSetIds() {
	return parameterSetIds;
    }

}