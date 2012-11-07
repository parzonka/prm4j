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

    private final static MaxData[] EMPTY_MAX_DATA = new MaxData[0];
    private final static JoinData[] EMPTY_JOIN_DATA = new JoinData[0];

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
	// TODO createMetaTree();
    }

    /**
     * Creates arrays of maxData, joinData, chainData.
     */
    private void convertToLowLevelStaticData() {
	for (Set<Parameter<?>> parameterSet : pp.getMonitorSetData().keys()) {
	    ArrayList<Tuple<Set<Parameter<?>>, Boolean>> tupleList = new ArrayList<Tuple<Set<Parameter<?>>, Boolean>>(
		    pp.getMonitorSetData().get(parameterSet));
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
		final int[] nodeMask = toParameterMask(parameterSet);
		final int[] diffMask = toParameterMask(Util.difference(baseEvent.getParameters(), parameterSet));
		maxData.put(baseEvent, new MaxData(nodeMask, diffMask));
	    }
	    for (Tuple<Set<Parameter<?>>, Set<Parameter<?>>> tuple : pp.getJoinData().get(baseEvent)) {
		final int[] nodeMask = toParameterMask(tuple.getLeft());
		final int monitorSetId = monitorSetIds.get(tuple.getLeft(), tuple.getRight());
		final boolean[] extensionPattern = getExtensionPattern(baseEvent.getParameters(), tuple.getRight());
		final int[] copyPattern = getCopyPattern(baseEvent.getParameters(), tuple.getRight());
		final int[] diffMask = toParameterMask(Util.difference(baseEvent.getParameters(), tuple.getLeft()));
		joinData.put(baseEvent, new JoinData(nodeMask, monitorSetId, extensionPattern, copyPattern, diffMask));
	    }
	}
	for (Set<Parameter<?>> parameterSet : pp.getChainData().keys()) {
	    for (Tuple<Set<Parameter<?>>, Set<Parameter<?>>> tuple : pp.getChainData().get(parameterSet)) {
		final int[] nodeMask = toParameterMask(parameterSet);
		final int monitorSetId = monitorSetIds.get(tuple.getLeft(), tuple.getRight());
		chainData.put(parameterSet, new ChainData(nodeMask, monitorSetId));
	    }
	}
    }

    protected static boolean[] getExtensionPattern(Set<Parameter<?>> ps1, Set<Parameter<?>> ps2) {
	List<Boolean> result = new ArrayList<Boolean>();
	int i = 0;
	int j = 0;
	while (i < ps1.size() || j < ps2.size()) {
	    if (i < ps1.size() && j < ps2.size() && toParameterMask(ps1)[i] == toParameterMask(ps2)[j]) {
		result.add(true);
		i++;
		j++;
	    } else if (i < ps1.size() && (j >= ps2.size() || toParameterMask(ps1)[i] < toParameterMask(ps2)[j])) {
		result.add(true);
		i++;
	    } else {
		result.add(false);
		j++;
	    }
	}
	return toPrimitiveBooleanArray(result);
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
    protected static int[] toParameterMask(Set<Parameter<?>> parameterSet) {
	int[] result = new int[parameterSet.size()];
	int i = 0;
	for (Parameter<?> parameter : parameterSet) {
	    result[i++] = parameter.getIndex();
	}
	Arrays.sort(result);
	return result;
    }

    public EventContext getEventContext() {
	return new EventContext(getJoinData(), getMaxData(), getCreationEvents(), getDisablingEvents());
    }

    /**
     * @return the rootnode of the tree of meta nodes
     */
    public MetaNode getMetaTree() {
	return rootNode;
    }

    protected SetMultimap<Set<Parameter<?>>, ChainData> getChainData() {
	return chainData;
    }

    protected MaxData[][] getMaxData() {
	MaxData[][] maxDataArray = new MaxData[pp.getBaseEvents().size()][];
	for (BaseEvent baseEvent : pp.getBaseEvents()) {
	    maxDataArray[baseEvent.getIndex()] = maxData.get(baseEvent) != null ? maxData.get(baseEvent).toArray(
		    EMPTY_MAX_DATA) : EMPTY_MAX_DATA;
	}
	return maxDataArray;
    }

    protected JoinData[][] getJoinData() {
	JoinData[][] joinDataArray = new JoinData[pp.getBaseEvents().size()][];
	for (BaseEvent baseEvent : pp.getBaseEvents()) {
	    joinDataArray[baseEvent.getIndex()] = joinData.get(baseEvent) != null ? joinData.get(baseEvent).toArray(
		    EMPTY_JOIN_DATA) : EMPTY_JOIN_DATA;
	}
	return joinDataArray;
    }

    protected boolean[] getCreationEvents() {
	boolean[] creationEvents = new boolean[pp.getBaseEvents().size()];
	for (BaseEvent baseEvent : pp.getBaseEvents()) {
	    creationEvents[baseEvent.getIndex()] = pp.getCreationEvents().contains(baseEvent);
	}
	return creationEvents;
    }

    protected boolean[] getDisablingEvents() {
	boolean[] disablingEvents = new boolean[pp.getBaseEvents().size()];
	for (BaseEvent baseEvent : pp.getBaseEvents()) {
	    disablingEvents[baseEvent.getIndex()] = pp.getDisablingEvents().contains(baseEvent);
	}
	return disablingEvents;
    }
}
