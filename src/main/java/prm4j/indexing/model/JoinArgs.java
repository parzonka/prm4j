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

import static prm4j.indexing.IndexingUtils.toParameterMask;
import static prm4j.indexing.IndexingUtils.toParameterMasks;
import static prm4j.indexing.IndexingUtils.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import prm4j.Util.Tuple;
import prm4j.api.BaseEvent;
import prm4j.api.Parameter;
import prm4j.indexing.monitor.DeadMonitor;

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;

/**
 * Represents all instances which are compatible with the event instance.
 * <p>
 * Used by {@link EventContext}
 */
public class JoinArgs {

    // identifies the node, which represents the compatible part of the instance, we want to join with
    public final int[] nodeMask;
    // identifies the monitor set, which contains the monitors carrying the bindings we will join with (they are
    // strictly more informative than the node, selected by the nodeMask)
    public final int monitorSetId;
    // prepares the event bindings for the join
    public final int[] extensionPattern;
    // identifies the bindings which will be used for the join, picking out only "new" parameters
    // { joiningBinding[i1], joinableBinding[j1], joiningBinding[i2], joinableBinding[j2], ... }
    public final int[] copyPattern;

    /**
     * ParameterMasks to be used with uncompressed bindings. Each parameterMask selects a instance to check if it has a
     * monitor. If it has a monitor, do not create a monitor (and node, if possible) in this join. The checked instances
     * are usually configured with the {@link DeadMonitor}, to define that they are disabled.
     * 
     * @return array of parameter masks
     */
    public int[][] disableMasks;

    public JoinArgs(int[] nodeMask, int monitorSetId, int[] extensionPattern, int[] copyPattern, int[][] disableMasks) {
	super();
	this.nodeMask = nodeMask;
	this.monitorSetId = monitorSetId;
	this.extensionPattern = extensionPattern;
	this.copyPattern = copyPattern;
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
	return "JoinArgs [nodeMask=" + Arrays.toString(nodeMask) + ", monitorSetId=" + monitorSetId
		+ ", extensionPattern=" + Arrays.toString(extensionPattern) + ", copyPattern="
		+ Arrays.toString(copyPattern) + ", disableMasks=" + disableMasksString.toString() + "]";
    }

    public static JoinArgs[] createArgsArray(ParametricPropertyModel ppm, BaseEvent baseEvent) {
	final JoinArgs[] result = new JoinArgs[ppm.getJoinTuples().get(baseEvent).size()];
	final Table<Set<Parameter<?>>, Set<Parameter<?>>, Integer> monitorSetIds = ppm.getMonitorSetIds();
	int i = 0;
	for (Tuple<Set<Parameter<?>>, Set<Parameter<?>>> tuple : ppm.getJoinTuples().get(baseEvent)) {
	    final Set<Parameter<?>> compatibleSubset = tuple._1();
	    final Set<Parameter<?>> enableSet = tuple._2();
	    final int[] nodeMask = toParameterMask(compatibleSubset);
	    final int monitorSetId = monitorSetIds.get(compatibleSubset, enableSet);
	    final int[] extensionPattern = getExtensionPattern(baseEvent.getParameters(), enableSet);
	    final int[] copyPattern = getCopyPattern(baseEvent.getParameters(), enableSet);
	    final int[][] disableMasks = toParameterMasks(getDisableSets(ppm, baseEvent, enableSet),
		    Sets.union(baseEvent.getParameters(), enableSet));
	    result[i++] = new JoinArgs(nodeMask, monitorSetId, extensionPattern, copyPattern, disableMasks);
	}
	return result;
    }

    protected static List<Set<Parameter<?>>> getDisableSets(ParametricPropertyModel ppm, BaseEvent baseEvent,
	    final Set<Parameter<?>> enableSet) {
	final Set<Parameter<?>> combination = Sets.union(baseEvent.getParameters(), enableSet);
	return toListOfParameterSetsAscending(Sets.filter(toParameterSets(ppm.getParametricProperty().getSpec()
		.getBaseEvents()), new Predicate<Set<Parameter<?>>>() {
	    @Override
	    public boolean apply(Set<Parameter<?>> baseEventParameterSet) {
		return combination.containsAll(baseEventParameterSet) && !enableSet.containsAll(baseEventParameterSet);
	    }
	}));
    }

    /**
     * Creates a int pattern needed for the join operation.
     * 
     * @param baseSet
     *            all parameters of this set will be kept
     * @param joiningSet
     *            new parameters from this set will join
     * @return
     */
    protected static int[] getExtensionPattern(Set<Parameter<?>> baseSet, Set<Parameter<?>> joiningSet) {
	final List<Integer> result = new ArrayList<Integer>();
	final Set<Integer> baseParameterIndexSet = toParameterIndexSet(baseSet);
	final int[] joinedArray = toParameterMask(Sets.union(baseSet, joiningSet));
	for (int parameterIndex : joinedArray) {
	    if (baseParameterIndexSet.contains(parameterIndex)) {
		result.add(parameterIndex);
	    } else {
		result.add(-1);
	    }
	}
	return toPrimitiveIntegerArray(result);
    }

    /**
     * Returns a pattern { s1, t1, ..., sN, tN } which represents a instruction to copy a binding from sourceBinding[s1]
     * to targetBinding[t1] to perform a join.
     * 
     * @param ps1
     *            parameter set which masks the target binding
     * @param ps2
     *            parameter set which masks the source binding
     * @return the pattern
     */
    protected static int[] getCopyPattern(Set<Parameter<?>> ps1, Set<Parameter<?>> ps2) {
	List<Integer> result = new ArrayList<Integer>();
	int i = 0;
	int j = 0;
	int k = 0;
	while (i < ps1.size() || j < ps2.size()) {
	    if (i < ps1.size() && j < ps2.size() && toParameterMask(ps1)[i] == toParameterMask(ps2)[j]) {
		i++;
		j++;
	    } else if (i < ps1.size() && (j >= ps2.size() || toParameterMask(ps1)[i] < toParameterMask(ps2)[j])) {
		i++;
	    } else {
		result.add(j++);
		result.add(i + k++);
	    }
	}
	return toPrimitiveIntegerArray(result);
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + Arrays.hashCode(copyPattern);
	result = prime * result + Arrays.hashCode(disableMasks);
	result = prime * result + Arrays.hashCode(extensionPattern);
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
	JoinArgs other = (JoinArgs) obj;
	if (!Arrays.equals(copyPattern, other.copyPattern)) {
	    return false;
	}
	if (!Arrays.deepEquals(disableMasks, other.disableMasks)) {
	    return false;
	}
	if (!Arrays.equals(extensionPattern, other.extensionPattern)) {
	    return false;
	}
	if (monitorSetId != other.monitorSetId) {
	    return false;
	}
	if (!Arrays.equals(nodeMask, other.nodeMask)) {
	    return false;
	}
	return true;
    }

}
