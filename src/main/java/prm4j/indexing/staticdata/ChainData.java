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
package prm4j.indexing.staticdata;

/**
 * Represents all less informative instances of the event instance which match its enable sets and are associated with
 * symbols, PLUS the compatible partial instances which map to parameter sets to fully instantiate joinable instances.
 */
public class ChainData {

    private final int[] nodeMask;
    private final int monitorSetId;

    public ChainData(int[] nodeMask, int monitorSetId) {
	super();
	this.nodeMask = nodeMask;
	this.monitorSetId = monitorSetId;
    }

    public int[] getNodeMask() {
	return nodeMask;
    }

    public int getMonitorSetId() {
	return monitorSetId;
    }

}
