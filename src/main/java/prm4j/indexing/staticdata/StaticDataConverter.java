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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import prm4j.Util;
import prm4j.Util.Tuple;
import prm4j.api.BaseEvent;
import prm4j.api.Parameter;
import prm4j.spec.ParametricProperty;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Table;

/**
 * Converts a {@link ParametricProperty} to an {@link EventContext} and a tree of {@link MetaNode}s.
 */
public class StaticDataConverter {

    private final ParametricProperty pp;
    private final ListMultimap<BaseEvent, MaxData> maxData;
    private final ListMultimap<BaseEvent, JoinData> joinData;
    private final SetMultimap<Set<Parameter<?>>, ChainData> chainData;
    private final Table<Set<Parameter<?>>, Set<Parameter<?>>, Integer> monitorSetIds;
    private final MetaNode rootNode;

    public StaticDataConverter(ParametricProperty pp) {
	this.pp = pp;
	maxData = ArrayListMultimap.create();
	joinData = ArrayListMultimap.create();
	chainData = HashMultimap.create();
	monitorSetIds = HashBasedTable.create();
	convertToLowLevelStaticData();
	rootNode = new MetaNode(new HashSet<Parameter<?>>());
	createMetaTree();
    }

    /**
     * Creates arrays of maxData, joinData, chainData.
     */
    private void convertToLowLevelStaticData() {
	for (Set<Parameter<?>> parameterSet : pp.getMonitorSetData().keys()) {
	    ArrayList<Tuple<Set<Parameter<?>>, Boolean>> tupleList = new ArrayList<Tuple<Set<Parameter<?>>, Boolean>>(pp
		    .getMonitorSetData().get(parameterSet));
	    Collections.sort(tupleList, new Comparator<Tuple<Set<Parameter<?>>, Boolean>>() {
		@Override
		public int compare(Tuple<Set<Parameter<?>>, Boolean> t1, Tuple<Set<Parameter<?>>, Boolean> t2) {
		    return t1.getLeft().size() - t2.getLeft().size();
		}
	    });
	    int i = 0;
	    for (Tuple<Set<Parameter<?>>, Boolean> tuple : tupleList) {
		monitorSetIds.put(parameterSet, tuple.getLeft(), i++);
	    }
	}
	for (BaseEvent baseEvent : pp.getBaseEvents()) {
	    for (Set<Parameter<?>> parameterSet : pp.getMaxData().get(baseEvent)) {
		final int[] nodeMask = parameterMask(parameterSet);
		final int[] diffMask = parameterMask(Util.difference(baseEvent.getParameters(), parameterSet));
		getMaxData().put(baseEvent, new MaxData(nodeMask, diffMask));
	    }
	    for (Tuple<Set<Parameter<?>>, Set<Parameter<?>>> tuple : pp.getJoinData().get(baseEvent)) {
		final int[] nodeMask = parameterMask(tuple.getLeft());
		final int monitorSetId = monitorSetIds.get(tuple.getLeft(), tuple.getRight());
		final boolean[] extensionPattern = getExtensionPattern(baseEvent.getParameters(), tuple.getRight());
		final int[] copyPattern = getCopyPattern(baseEvent.getParameters(), tuple.getRight());
		final int[] diffMask = parameterMask(Util.difference(baseEvent.getParameters(), tuple.getLeft()));
		getJoinData().put(baseEvent,
			new JoinData(nodeMask, monitorSetId, extensionPattern, copyPattern, diffMask));
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
     * Creates a tree of meta nodes
     */
    private void createMetaTree() {
	for (Set<Parameter<?>> parameterSet : pp.getPossibleParameterSets()) {
	    List<Parameter<?>> parameterList = new ArrayList<Parameter<?>>(parameterSet);
	    Collections.sort(parameterList);
	    MetaNode node = rootNode;
	    for (Parameter<?> parameter : parameterList) {
		node = node.getMetaNode(parameter);
		if (!node.isConfigured()) {
		    node.setChainingData(chainData.get(node.getParameterSet()).toArray(new ChainData[0]));
		    node.setMonitorSetCount(monitorSetIds.row(node.getParameterSet()).size());
		    node.setConfigured(true);
		}
	    }
	}
    }

    /**
     * @param parameterSet
     * @return an array representation of the parameter ids of the given parameter set (sorted)
     */
    private static int[] parameterMask(Set<Parameter<?>> parameterSet) {
	int[] result = new int[parameterSet.size()];
	int i = 0;
	for (Parameter<?> parameter : parameterSet) {
	    result[i++] = parameter.getIndex();
	}
	Arrays.sort(result);
	return result;
    }

    public EventContext getEventContext() {
	return new EventContext(pp.getBaseEvents(), getJoinData(), getMaxData(), pp.getCreationEvents(),
		pp.getDisablingEvents());
    }

    /**
     * @return the rootnode of the tree of meta nodes
     */
    public MetaNode getMetaTree() {
	return rootNode;
    }

    protected Set<ChainData> getChainData(Set<Parameter<?>> parameterSet) {
	return chainData.get(parameterSet);
    }

    protected ListMultimap<BaseEvent, MaxData> getMaxData() {
	return maxData;
    }

    protected ListMultimap<BaseEvent, JoinData> getJoinData() {
	return joinData;
    }

}
