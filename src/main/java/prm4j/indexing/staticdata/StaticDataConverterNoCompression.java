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
public class StaticDataConverterNoCompression {

    private final static MaxData[] EMPTY_MAX_DATA = new MaxData[0];
    private final static JoinData[] EMPTY_JOIN_DATA = new JoinData[0];

    private final ParametricProperty pp;
    private final ListMultimap<BaseEvent, MaxData> maxData;
    private final ListMultimap<BaseEvent, JoinData> joinData;
    private final SetMultimap<Set<Parameter<?>>, ChainData> chainData;
    private final Table<Set<Parameter<?>>, Set<Parameter<?>>, Integer> monitorSetIds;
    private final MetaNode metaTree;

    public StaticDataConverterNoCompression(ParametricProperty pp) {
	this.pp = pp;
	maxData = ArrayListMultimap.create();
	joinData = ArrayListMultimap.create();
	chainData = HashMultimap.create();
	monitorSetIds = HashBasedTable.create();
	convertToLowLevelStaticData();
	metaTree = new MetaNode(new HashSet<Parameter<?>>(), pp.getParameters());
	createMetaTree();
    }

    /**
     * Creates arrays of maxData, joinData, chainData.
     */
    private void convertToLowLevelStaticData() { // 1
	for (Set<Parameter<?>> parameterSet : pp.getMonitorSetData().keys()) { // 2
	    int i = 0; // 3
	    for (Tuple<Set<Parameter<?>>, Boolean> tuple : getMonitorSetDataInTopologicalOrdering(parameterSet)) { // 4
		monitorSetIds.put(parameterSet, tuple.getLeft(), i++); // 5, 6
	    } // 7
	} // 8
	for (BaseEvent baseEvent : pp.getBaseEvents()) { // 9
	    for (Set<Parameter<?>> parameterSet : pp.getMaxData().get(baseEvent)) { // 10
		final int[] nodeMask = toParameterMask(parameterSet); // 12
		final int[] diffMask = toParameterMask(Util.difference(baseEvent.getParameters(), parameterSet)); // 13
		maxData.put(baseEvent, new MaxData(nodeMask, diffMask)); // 11, 14
	    } // 15
	    for (Tuple<Set<Parameter<?>>, Set<Parameter<?>>> tuple : pp.getJoinData().get(baseEvent)) { // 16
		// we have to select the compatible parameters from the parameters in the base event
		final int[] nodeMask = toParameterSubsetMask(tuple.getLeft(), baseEvent.getParameters()); // 18
		final int monitorSetId = monitorSetIds.get(tuple.getLeft(), tuple.getRight()); // 19
		final boolean[] extensionPattern = getExtensionPattern(baseEvent.getParameters(), tuple.getRight()); // 20
		final int[] copyPattern = getCopyPattern(baseEvent.getParameters(), tuple.getRight()); // 21
		final int[] diffMask = toParameterSubsetMask(
			Util.difference(baseEvent.getParameters(), tuple.getLeft()), baseEvent.getParameters()); // 22
		joinData.put(baseEvent, new JoinData(nodeMask, monitorSetId, extensionPattern, copyPattern, diffMask)); // 23
	    } // 24
	} // 25
	for (Set<Parameter<?>> parameterSet : pp.getChainData().keys()) { // 26
	    for (Tuple<Set<Parameter<?>>, Set<Parameter<?>>> tuple : pp.getChainData().get(parameterSet)) { // 27
		final int[] nodeMask = toParameterMask(tuple.getLeft()); // 29
		final int monitorSetId = monitorSetIds.get(tuple.getLeft(), tuple.getRight()); // 30
		chainData.put(parameterSet, new ChainData(nodeMask, monitorSetId)); // 28, 31
	    } // 32
	} // 33
    } // 34

    /**
     * Return a ordered list of tuples. The ordering is not neccessary for correctness. It is just useful for display
     * purposes so that the empty set (as left component) will get associated with the id 0.
     *
     * @param parameterSet
     * @return a list of tuples (Set, Boolean) ordered by size of the set.
     */
    private List<Tuple<Set<Parameter<?>>, Boolean>> getMonitorSetDataInTopologicalOrdering(
	    Set<Parameter<?>> parameterSet) {
	ArrayList<Tuple<Set<Parameter<?>>, Boolean>> tupleList = new ArrayList<Tuple<Set<Parameter<?>>, Boolean>>(pp
		.getMonitorSetData().get(parameterSet));
	Collections.sort(tupleList, new Comparator<Tuple<Set<Parameter<?>>, Boolean>>() {
	    @Override
	    public int compare(Tuple<Set<Parameter<?>>, Boolean> t1, Tuple<Set<Parameter<?>>, Boolean> t2) {
		return t1.getLeft().size() - t2.getLeft().size();
	    }
	});
	return tupleList;
    }

    /**
     *
     * @param baseSet
     *            all parameters of this set will be kept
     * @param joiningSet
     *            new parameters from this set will join
     * @return
     */
    protected static boolean[] getExtensionPattern(Set<Parameter<?>> baseSet, Set<Parameter<?>> joiningSet) {
	List<Boolean> result = new ArrayList<Boolean>();
	int i = 0;
	int j = 0;
	while (i < baseSet.size() || j < joiningSet.size()) {
	    if (i < baseSet.size() && j < joiningSet.size()
		    && toParameterMask(baseSet)[i] == toParameterMask(joiningSet)[j]) {
		result.add(true);
		i++;
		j++;
	    } else if (i < baseSet.size()
		    && (j >= joiningSet.size() || toParameterMask(baseSet)[i] < toParameterMask(joiningSet)[j])) {
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
	Set<Set<Parameter<?>>> allParameterSets = new HashSet<Set<Parameter<?>>>();
	allParameterSets.addAll(pp.getMonitorSetData().keys());
	allParameterSets.addAll(pp.getPossibleParameterSets());
	for (Set<Parameter<?>> parameterSet : allParameterSets) {
	    MetaNode node = metaTree;
	    for (Parameter<?> parameter : Util.asSortedList(parameterSet)) {
		node = node.getMetaNode(parameter);
		node.setChainData(chainData.get(node.getNodeParameterSet()));
		node.setMonitorSetCount(monitorSetIds.row(node.getNodeParameterSet()).size());
	    }
	}
    }

    /**
     * Returns a parameter mask which selects all parameters in the given set from a full parameter set
     *
     * @param parameterSet
     * @return
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

    /**
     * Returns a parameterMask which selects all parameters in the subset from the parameter set.
     *
     * @param subset
     * @param parameterSet
     * @return
     */
    protected static int[] toParameterSubsetMask(Set<Parameter<?>> subset, Set<Parameter<?>> parameterSet) {
	int[] result = new int[subset.size()];
	int i = 0;
	int j = 0;
	for (Parameter<?> parameter : Util.asSortedList(parameterSet)) {
	    if (subset.contains(parameter)) {
		result[i++] = j;
	    }
	    j++;
	}
	return result;
    }

    public EventContext getEventContext() {
	return new EventContext(getJoinData(), getMaxData(), getCreationEvents(), getDisablingEvents());
    }

    /**
     * @return the rootnode of the tree of meta nodes
     */
    public MetaNode getMetaTree() {
	return metaTree;
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

    protected Table<Set<Parameter<?>>, Set<Parameter<?>>, Integer> getMonitorSetIds() {
	return monitorSetIds;
    }

}
