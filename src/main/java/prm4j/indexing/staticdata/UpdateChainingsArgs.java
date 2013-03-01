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
package prm4j.indexing.staticdata;

import java.util.Arrays;
import java.util.List;

import prm4j.Util;

/**
 * Represents all less informative instances of the event instance which match its enable sets and are associated with
 * symbols, PLUS the compatible partial instances which map to parameter sets to fully instantiate joinable instances.
 */
public class UpdateChainingsArgs {

    public final int[] nodeMask;
    public final int monitorSetId;

    public UpdateChainingsArgs(int[] nodeMask, int monitorSetId) {
	super();
	this.nodeMask = nodeMask;
	this.monitorSetId = monitorSetId;
    }

    public UpdateChainingsArgs(List<Integer> nodeMask, int monitorSetId) {
	super();
	this.nodeMask = Util.toPrimitiveIntegerArray(nodeMask);
	this.monitorSetId = monitorSetId;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + monitorSetId;
	result = prime * result + Arrays.hashCode(nodeMask);
	return result;
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj) {
	    return true;
	}
	if (obj == null) {
	    return false;
	}
	if (getClass() != obj.getClass()) {
	    return false;
	}
	UpdateChainingsArgs other = (UpdateChainingsArgs) obj;
	if (monitorSetId != other.monitorSetId) {
	    return false;
	}
	if (!Arrays.equals(nodeMask, other.nodeMask)) {
	    return false;
	}
	return true;
    }

    @Override
    public String toString() {
	return "UpdateChainingsArgs [nodeMask=" + Arrays.toString(nodeMask) + ", monitorSetId=" + monitorSetId + "]";
    }

}
