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

public class MaxData {

    private final int[] nodeMask;
    private final int[] diffMask;
    private final int[][] disableMasks;

    public MaxData(int[] nodeMask, int[] diffMask, int[][] disableMasks) {
	super();
	this.nodeMask = nodeMask;
	this.diffMask = diffMask;
	this.disableMasks = disableMasks;
    }

    public int[] getNodeMask() {
	return nodeMask;
    }

    public int[] getDiffMask() {
	return diffMask;
    }

    public int[][] getDisableMasks() {
	return disableMasks;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
	StringBuilder disableMasksString = new StringBuilder("[");
	for (int[] disableMask : disableMasks) {
	    disableMasksString.append(Arrays.toString(disableMask));
	    disableMasksString.append(" ");
	}
	disableMasksString.append("]");
	return "MaxData [nodeMask=" + Arrays.toString(nodeMask) + ", diffMask=" + Arrays.toString(diffMask)
		+ ", disableMasks=" + disableMasksString.toString() + "]";
    }

}
