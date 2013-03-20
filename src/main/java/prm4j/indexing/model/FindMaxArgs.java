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
package prm4j.indexing.model;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;

import prm4j.api.BaseEvent;
import prm4j.api.Parameter;
import static prm4j.indexing.IndexingUtils.*;

public class FindMaxArgs {

    private final static FindMaxArgs[] EMPTY_FIND_MAX_ARGS_ARRAY = new FindMaxArgs[0];

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

    public static FindMaxArgs[] createArgsArray(ParametricPropertyModel ppa, BaseEvent baseEvent) {
	List<Set<Parameter<?>>> enableParameterSets = ppa.getFindMaxInstanceTypes().get(baseEvent);
	if (enableParameterSets == null) {
	    return EMPTY_FIND_MAX_ARGS_ARRAY;
	}
	FindMaxArgs[] result = new FindMaxArgs[enableParameterSets.size()];
	int i = 0;
	for (Set<Parameter<?>> enableParameterSet : enableParameterSets) {
	    final int[] nodeMask = toParameterMask(enableParameterSet); // 12
	    final int[][] disableMasks = toParameterMasks(getDisableSets(ppa, baseEvent, enableParameterSet));
	    result[i++] = new FindMaxArgs(nodeMask, disableMasks);
	}
	return result;
    }

    public static List<Set<Parameter<?>>> getDisableSets(ParametricPropertyModel ppa, BaseEvent baseEvent,
	    final Set<Parameter<?>> enableSet) {
	final Set<Parameter<?>> combination = Sets.union(baseEvent.getParameters(), enableSet);
	return toListOfParameterSetsAscending(Sets.filter(toParameterSets(ppa.getParametricProperty().getSpec()
		.getBaseEvents()), new Predicate<Set<Parameter<?>>>() {
	    @Override
	    public boolean apply(Set<Parameter<?>> baseEventParameterSet) {
		return combination.containsAll(baseEventParameterSet) && !enableSet.containsAll(baseEventParameterSet);
	    }
	}));
    }
}
