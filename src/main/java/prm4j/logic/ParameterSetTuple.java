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
package prm4j.logic;

public class ParameterSetTuple {

    private final ParameterSet nodeId;
    private final ParameterSet monitorSetId;

    public ParameterSetTuple(ParameterSet nodeId, ParameterSet monitorSetId) {
	super();
	this.nodeId = nodeId;
	this.monitorSetId = monitorSetId;
    }

    public ParameterSet getNodeMask() {
	return nodeId;
    }

    public ParameterSet getMonitorSetId() {
	return monitorSetId;
    }

}
