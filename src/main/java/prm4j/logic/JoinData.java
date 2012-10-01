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

/**
 * Represents all instances which are compatible with the event instance.
 * <p>
 * Used by {@link EventContext}
 */
public class JoinData {

    // identifies the node, which represents the compatible part of the instance, we want to join with
    private final int[] parameterMask;
    // identifies the parameterSets, which select the set of instances we will join with (the are strictly more
    // informative than the node, selected by the traversal mask
    private final int[] parameterSetIds;
    // prepares the event bindings for the join
    private final int[] joinTargetPattern;
    // identifies the bindings which will be used for the join, picking out only "new" parameters
    private final int[] joinSourcePattern;
    // identifies the bindings which will be checked for disable-values
    private final int[] disableMask;

    public JoinData(int[] traversalMask, int[] parameterSetIds, int[] joinTargetPattern, int[] joinSourcePattern,
	    int[] disableMask) {
	super();
	parameterMask = traversalMask;
	this.parameterSetIds = parameterSetIds;
	this.joinTargetPattern = joinTargetPattern;
	this.joinSourcePattern = joinSourcePattern;
	this.disableMask = disableMask;
    }

    public int[] getTraversalMask() {
	return parameterMask;
    }

    public int[] getParameterSetIds() {
	return parameterSetIds;
    }

    public int[] getJoinTargetPattern() {
	return joinTargetPattern;
    }

    /**
     *
     * { joiningBinding[i1], joinableBinding[j1], joiningBinding[i2], joinableBinding[j2] }
     *
     * @return
     */
    public int[] getJoinSourcePattern() {
	return joinSourcePattern;
    }

    public int[] getDisableMask() {
	return disableMask;
    }

}
