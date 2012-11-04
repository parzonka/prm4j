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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import prm4j.Util;
import prm4j.Util.Tuple;
import prm4j.api.Parameter;
import prm4j.indexing.BaseEvent;
import prm4j.logic.ParametricProperty;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Table;

public class StaticDataConverter {

    private final ParametricProperty pp;
    private final ListMultimap<BaseEvent, MaxData> maxData;
    private final ListMultimap<BaseEvent, JoinData> joinData;
    private final SetMultimap<Set<Parameter<?>>, ChainData> chainData;
    private final Table<Set<Parameter<?>>, Set<Parameter<?>>, Integer> monitorSetIds;

    public StaticDataConverter(ParametricProperty pp) {
	this.pp = pp;
	maxData = ArrayListMultimap.create();
	joinData = ArrayListMultimap.create();
	chainData = HashMultimap.create();
	monitorSetIds = HashBasedTable.create();
	convert(pp);
    }

    private void convert(ParametricProperty pp) {
	for (Entry<Set<Parameter<?>>, Set<Parameter<?>>> entry : pp.getMonitorSetData().entries()) {
	    @SuppressWarnings("unchecked")
	    Set<Parameter<?>>[] sortedSets = entry.getValue().toArray(new Set[0]);
	    Arrays.sort(sortedSets, Util.TOPOLOGICAL_SET_COMPARATOR);
	    int i = 0;
	    for (Set<Parameter<?>> set : sortedSets) {
		monitorSetIds.put(entry.getKey(), set, i++);
	    }
	}
	for (BaseEvent baseEvent : pp.getBaseEvents()) {
	    for (Set<Parameter<?>> parameterSet : pp.getMaxData().get(baseEvent)) {
		final int[] nodeMask = parameterMask(parameterSet);
		final int[] diffMask = parameterMask(Util.difference(baseEvent.getParameters(), parameterSet));
		maxData.put(baseEvent, new MaxData(nodeMask, diffMask));
	    }
	    for (Tuple<Set<Parameter<?>>, Set<Parameter<?>>> tuple : pp.getJoinData().get(baseEvent)) {
		final int[] nodeMask = parameterMask(tuple.getLeft());
		final int monitorSetId = monitorSetIds.get(tuple.getLeft(), tuple.getRight());
		final boolean[] extensionPattern = getExtensionPattern(baseEvent.getParameters(), tuple.getRight());
		final int[] copyPattern = getCopyPattern(baseEvent.getParameters(), tuple.getRight());
		final int[] diffMask = parameterMask(Util.difference(baseEvent.getParameters(), tuple.getLeft()));
		joinData.put(baseEvent, new JoinData(nodeMask, monitorSetId, extensionPattern, copyPattern, diffMask));
	    }
	}
	for (Set<Parameter<?>> parameterSet : pp.getChainData().keys()) {
	    for (Tuple<Set<Parameter<?>>, Set<Parameter<?>>> tuple : pp.getChainData().get(parameterSet)) {
		final int[] nodeMask = parameterMask(parameterSet);
		final int monitorSetId = monitorSetIds.get(tuple.getLeft(), tuple.getRight());
		chainData.put(parameterSet, new ChainData(nodeMask, monitorSetId));
	    }
	}
    }

    protected static boolean[] getExtensionPattern(Set<Parameter<?>> ps1, Set<Parameter<?>> ps2) {
	List<Boolean> result = new ArrayList<Boolean>();
	int i = 0;
	int j = 0;
	while (i < ps1.size()) {
	    if (j >= ps2.size() || parameterMask(ps1)[i] <= parameterMask(ps2)[j]) {
		result.add(true);
		i++;
	    } else {
		result.add(false);
		j++;
	    }
	}
	return toPrimitiveBooleanArray(result);
    }

    protected static int[] getCopyPattern(Set<Parameter<?>> ps1, Set<Parameter<?>> ps2) {
	List<Integer> result = new ArrayList<Integer>();
	int i = 0;
	int j = 0;
	while (i < ps1.size()) {
	    if (j >= ps2.size() || parameterMask(ps1)[i] <= parameterMask(ps2)[j]) {
		i++;
	    } else {
		result.add(j); // source
		result.add(i); // target
		j++;
	    }
	}
	return toPrimitiveIntegerArray(result);
    }

    private static boolean[] toPrimitiveBooleanArray(Collection<Boolean> collection) {
	boolean[] result = new boolean[collection.size()];
	int i = 0;
	for (Boolean b : collection) {
	    result[i++] = b;
	}
	return result;
    }

    private static int[] toPrimitiveIntegerArray(Collection<Integer> collection) {
	int[] result = new int[collection.size()];
	int i = 0;
	for (Integer n : collection) {
	    result[i++] = n;
	}
	return result;
    }

    /**
     * @param parameterSet
     * @return an array representation of the parameter ids of the given parameter set (sorted)
     */
    private static int[] parameterMask(Set<Parameter<?>> parameterSet) {
	int[] result = new int[parameterSet.size()];
	int i = 0;
	for (Parameter<?> parameter : parameterSet) {
	    result[i++] = parameter.getParameterId();
	}
	Arrays.sort(result);
	return result;
    }

    public List<MaxData> getMaxData(BaseEvent baseEvent) {
	return maxData.get(baseEvent);
    }

    public EventContext getEventContext() {
	return new EventContext(pp.getBaseEvents(), joinData, maxData,
		pp.getCreationEvents(), pp.getDisablingEvents());
    }

    public Set<ChainData> getChainData(Set<Parameter<?>> parameterSet) {
	return chainData.get(parameterSet);
    }

}
