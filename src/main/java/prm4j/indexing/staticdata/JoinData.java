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
 * Represents all instances which are compatible with the event instance.
 * <p>
 * Used by {@link EventContext}
 */
public class JoinData {

    // identifies the node, which represents the compatible part of the instance, we want to join with
    private final int[] nodeMask;
    // identifies the monitor set, which contains the monitors carrying the bindings we will join with (they are strictly more
    // informative than the node, selected by the nodeMask)
    private final int monitorSetId;
    // prepares the event bindings for the join
    private final boolean[] extensionPattern;
    // identifies the bindings which will be used for the join, picking out only "new" parameters
    private final int[] copyPattern;
    // identifies the bindings which are in given binding without joining binding; used for disable-calculation
    private final int[] diffMask;

    public JoinData(int[] nodeMask, int monitorSetId, boolean[] extensionPattern, int[] copyPattern,
	    int[] diffMask) {
	super();
	this.nodeMask = nodeMask;
	this.monitorSetId = monitorSetId;
	this.extensionPattern = extensionPattern;
	this.copyPattern = copyPattern;
	this.diffMask = diffMask;
    }

    public int[] getNodeMask() {
	return nodeMask;
    }

    public int getMonitorSetId() {
	return monitorSetId;
    }

    public boolean[] getExtensionPattern() {
	return extensionPattern;
    }

    /**
     *
     * { joiningBinding[i1], joinableBinding[j1], joiningBinding[i2], joinableBinding[j2], ... }
     *
     * @return
     */
    public int[] getCopyPattern() {
	return copyPattern;
    }

    public int[] getDiffMask() {
	return diffMask;
    }

}
