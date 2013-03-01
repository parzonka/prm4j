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
package prm4j.indexing.logic;

import java.util.Arrays;

public class FindMaxArgs {

    /**
     * Identifies the node which is checked for a defined monitor to derive its state from.
     */
    public final int[] nodeMask;
    /**
     * The set of theta'' in the first line of the defineTo method of algorithm D. The parameter sets identify instances
     * which will be checked if they have (dead) monitors.
     */
    public final int[][] disableMasks;

    public FindMaxArgs(int[] nodeMask, int[][] disableMasks) {
	super();
	this.nodeMask = nodeMask;
	this.disableMasks = disableMasks;
    }

    @Override
    public String toString() {
	StringBuilder disableMasksString = new StringBuilder("[");
	for (int[] disableMask : disableMasks) {
	    disableMasksString.append(Arrays.toString(disableMask));
	    disableMasksString.append(" ");
	}
	disableMasksString.append("]");
	return "FindMaxArgs [nodeMask=" + Arrays.toString(nodeMask) + ", disableMasks=" + disableMasksString.toString()
		+ "]";
    }

}
