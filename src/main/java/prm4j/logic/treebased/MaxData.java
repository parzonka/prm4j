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

public class MaxData {

    private final int[] nodeMask;
    private final int[] diffMask;

    public MaxData(int[] nodeMask, int[] diffMask) {
	super();
	this.nodeMask = nodeMask;
	this.diffMask = diffMask;
    }

    public int[] getNodeMask() {
	return nodeMask;
    }

    public int[] getDiffMask() {
	return diffMask;
    }

}
