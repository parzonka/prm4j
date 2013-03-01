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
package prm4j.indexing.logic;

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

import com.google.common.base.Predicate;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;

/**
 * Converts a {@link ParametricProperty} to an {@link EventContext} and a tree of {@link ParameterNode}s.
 */
public class ParametricPropertyProcessor {

    private final static FindMaxArgs[] EMPTY_MAX_DATA = new FindMaxArgs[0];
    private final static JoinArgs[] EMPTY_JOIN_DATA = new JoinArgs[0];

    private final ParametricProperty pp;
    private final ListMultimap<BaseEvent, FindMaxArgs> findMaxArgs;
    private final ListMultimap<BaseEvent, JoinArgs> joinArgs;
    private final Table<BaseEvent, Set<Parameter<?>>, List<Set<Parameter<?>>>> disableParameterSets;
    private final SetMultimap<Set<Parameter<?>>, UpdateChainingsArgs> updateChainingsArgs;
    private final Table<Set<Parameter<?>>, Set<Parameter<?>>, Integer> monitorSetIds;
    private final int[][][] existingMonitorMasks;
    private final ParameterNode parameterTree;

    public ParametricPropertyProcessor(ParametricProperty pp) {
	this.pp = pp;
	findMaxArgs = ArrayListMultimap.create();
	joinArgs = ArrayListMultimap.create();
	disableParameterSets = HashBasedTable.create();
	updateChainingsArgs = HashMultimap.create();
	monitorSetIds = HashBasedTable.create();
	existingMonitorMasks = new int[pp.getBaseEvents().size()][][];
	convertToLowLevelStaticData();
	parameterTree = new ParameterNode(new HashSet<Parameter<?>>(), pp.getParameters());
	createParameterTree();
    }

    /**
     * Creates arrays of findMaxArgs, joinArgs, updateChainingsArgs.
     */
    private void convertToLowLevelStaticData() { // 1
	for (Set<Parameter<?>> parameterSet : pp.getMonitorSetData().keys()) { // 2
	    int i = 0; // 3
	    for (Tuple<Set<Parameter<?>>, Boolean> tuple : getMonitorSetDataInTopologicalOrdering(parameterSet)) { // 4
		monitorSetIds.put(parameterSet, tuple._1(), i++); // 5, 6
	    } // 7
	} // 8
	for (final BaseEvent baseEvent : pp.getBaseEvents()) { // 9
	    for (final Set<Parameter<?>> enableParameterSet : pp.getMaxData().get(baseEvent)) { // 10
		final int[] nodeMask = toParameterMask(enableParameterSet); // 12
		final int[][] disableMasks = toParameterMasks(getDisableSets(baseEvent, enableParameterSet));
		findMaxArgs.put(baseEvent, new FindMaxArgs(nodeMask, disableMasks)); // 11, 14
	    } // 15
	    existingMonitorMasks[baseEvent.getIndex()] = calculateExistingMonitorMasks(baseEvent.getParameters());
	    /*
	     * Iterate through a ordered list of tuples for each base event. Each tuple is of type (ParameterSet,
	     * ParameterSet) and has the following semantics: (compatibleParameterSet, enablingParameterSet) in relation
	     * to the base event or its parameter set. In a join operation, we want to create a combination with the
	     * enabling parameter set. We have to retrieve it via the compatible parameter set, because it has a
	     * reference to it in its monitor set.
	     */
	    // tuple(compatibleSubset, enablingParameterSet)
	    for (Tuple<Set<Parameter<?>>, Set<Parameter<?>>> tuple : pp.getJoinData().get(baseEvent)) { // 16
		final Set<Parameter<?>> compatibleSubset = tuple._1();
		final Set<Parameter<?>> enableSet = tuple._2();
		// the nodeMask selects the compatible node which has references to the enabling instance we want to
		// combine with
		final int[] nodeMask = toParameterMask(compatibleSubset); // 18
		final int monitorSetId = monitorSetIds.get(compatibleSubset, enableSet); // 19
		final int[] extensionPattern = getExtensionPattern(baseEvent.getParameters(), enableSet); // 20
		final int[] copyPattern = getCopyPattern(baseEvent.getParameters(), enableSet); // 21
		// TODO the list of disable parameter sets would be even smaller if we would just calculate the set of
		// disable events for the property. Disable events are events which point to a dead state from any
		// state but an accepting state.
		final List<Set<Parameter<?>>> listOfDisableParameterSets = getDisableSets(baseEvent, enableSet);
		disableParameterSets.put(baseEvent, enableSet, listOfDisableParameterSets);
		final int[][] disableMasks = toParameterMasks(listOfDisableParameterSets);
		joinArgs.put(baseEvent, new JoinArgs(nodeMask, monitorSetId, extensionPattern, copyPattern,
			disableMasks)); // 23
	    } // 24
	} // 25
	for (Set<Parameter<?>> parameterSet : pp.getChainData().keys()) { // 26
	    for (Tuple<Set<Parameter<?>>, Set<Parameter<?>>> tuple : pp.getChainData().get(parameterSet)) { // 27
		final int[] nodeMask = toParameterMask(tuple._1()); // 29
		final int monitorSetId = monitorSetIds.get(tuple._1(), tuple._2()); // 30
		updateChainingsArgs.put(parameterSet, new UpdateChainingsArgs(nodeMask, monitorSetId)); // 28, 31
	    } // 32
	} // 33
    } // 34

    /**
     * Return the list of all baseEvent parameter sets which are subsets of the union of the given baseEvent and
     * enableSet but not subsets of the given enableSet.
     * 
     * @param baseEvent
     * @param enableSet
     * @return a list of parameter sets
     */
    public List<Set<Parameter<?>>> getDisableSets(BaseEvent baseEvent, final Set<Parameter<?>> enableSet) {
	final Set<Parameter<?>> combination = Sets.union(baseEvent.getParameters(), enableSet);
	return toListOfParameterSetsAscending(Sets.filter(toParameterSets(pp.getBaseEvents()),
		new Predicate<Set<Parameter<?>>>() {
		    @Override
		    public boolean apply(Set<Parameter<?>> baseEventParameterSet) {
			return combination.containsAll(baseEventParameterSet)
				&& !enableSet.containsAll(baseEventParameterSet);
		    }
		}));
    }

    public static int[][] calculateExistingMonitorMasks(Set<Parameter<?>> baseEventParameterSet) {
	final Set<Set<Parameter<?>>> powerset = new HashSet<Set<Parameter<?>>>();
	for (Set<Parameter<?>> parameterSet : Sets.powerSet(baseEventParameterSet)) {
	    powerset.add(parameterSet);
	}
	final int[][] result = new int[powerset.size()][];
	int i = 0;
	for (Set<Parameter<?>> parameterSet : toListOfParameterSetsAscending(powerset)) {
	    result[i++] = toParameterMask(parameterSet);
	}
	return result;
    }

    /**
     * Returns all sets in setOfSets, which are no subsets of filterSet.
     * 
     * @param setOfSets
     * @param filterSet
     * @return a set without subsets of filterSet
     */
    public static <T> Set<Set<T>> toSetWithoutSubsets(Set<Set<T>> setOfSets, Set<T> filterSet) {
	final Set<Set<T>> result = new HashSet<Set<T>>();
	for (Set<T> parameterSet : setOfSets) {
	    if (!filterSet.containsAll(parameterSet)) {
		result.add(parameterSet);
	    }
	}
	return result;
    }

    /**
     * Basically a map over a set of base events with 'getParameters'.
     * 
     * @param setOfBaseEvents
     * @return a set of parameter sets per baseEvent
     */
    private static Set<Set<Parameter<?>>> toParameterSets(Set<BaseEvent> setOfBaseEvents) {
	Set<Set<Parameter<?>>> result = new HashSet<Set<Parameter<?>>>();
	for (BaseEvent baseEvent : setOfBaseEvents) {
	    result.add(baseEvent.getParameters());
	}
	return result;
    }

    /**
     * @param setOfParameterSets
     * @return a list of parameter sets in topological ascending order, i.e. small sets first, largest last. If two sets
     *         have the same size, the parameter indices are added and the set with the smallest number is sorted first.
     */
    public static List<Set<Parameter<?>>> toListOfParameterSetsAscending(Set<Set<Parameter<?>>> setOfParameterSets) {
	List<Set<Parameter<?>>> result = new ArrayList<Set<Parameter<?>>>(setOfParameterSets);
	Collections.sort(result, new Comparator<Set<Parameter<?>>>() {

	    @Override
	    public int compare(Set<Parameter<?>> o1, Set<Parameter<?>> o2) {
		if (o1.size() == o2.size()) {
		    int indexSum1 = 0;
		    for (Parameter<?> parameterSet : o1) {
			indexSum1 += parameterSet.getIndex();
		    }
		    int indexSum2 = 0;
		    for (Parameter<?> parameterSet : o2) {
			indexSum2 += parameterSet.getIndex();
		    }
		    return indexSum1 - indexSum2;

		}
		return o1.size() - o2.size();
	    }

	});

	return result;
    }

    /**
     * Basically a map over a list of parameters sets with 'toParameterMask'.
     * 
     * @param listOfParameterSets
     * @return an array of parameter masks (uncompressed)
     */
    public static int[][] toParameterMasks(List<Set<Parameter<?>>> listOfParameterSets) {
	final int[][] result = new int[listOfParameterSets.size()][];
	int i = 0;
	for (Set<Parameter<?>> parameterSet : listOfParameterSets) {
	    result[i++] = toParameterMask(parameterSet);
	}
	return result;
    }

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
		return t1._1().size() - t2._1().size();
	    }
	});
	return tupleList;
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

    protected static Set<Integer> toParameterIndexSet(Set<Parameter<?>> parameterSet) {
	Set<Integer> result = new HashSet<Integer>();
	for (Parameter<?> parameter : parameterSet) {
	    result.add(parameter.getIndex());
	}
	return result;
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

    private static int[] toPrimitiveIntegerArray(Collection<Integer> collection) {
	int[] result = new int[collection.size()];
	int i = 0;
	for (Integer n : collection) {
	    result[i++] = n;
	}
	return result;
    }

    /**
     * parameterMasksCount * parameterMask
     */
    private boolean[][] calculateAliveParameterMasksBoolean(Set<Parameter<?>> nodeParameterSet) {
	boolean[][] result = new boolean[pp.getAliveParameterSets().size()][];
	int i = 0;
	for (Set<Parameter<?>> parameterSet : pp.getAliveParameterSets()) {
	    // the alive parameter mask is a subset of the node parameter set
	    result[i++] = toParameterSubsetMaskBoolean(parameterSet, nodeParameterSet);
	}
	return result;
    }

    /**
     * Creates a tree of parameter nodes
     */
    private void createParameterTree() {
	final Set<ParameterNode> parameterNodes = new HashSet<ParameterNode>();
	parameterNodes.add(parameterTree);
	parameterTree.setAliveParameterMasks(calculateAliveParameterMasksBoolean(new HashSet<Parameter<?>>()));
	final Set<Set<Parameter<?>>> allParameterSets = new HashSet<Set<Parameter<?>>>();
	allParameterSets.addAll(pp.getMonitorSetData().keys());
	allParameterSets.addAll(pp.getPossibleParameterSets());
	for (Set<Parameter<?>> parameterSet : allParameterSets) {
	    ParameterNode node = parameterTree;
	    for (Parameter<?> parameter : Util.asSortedList(parameterSet)) {
		if (node.getParameterNode(parameter) != null) {
		    node = node.getParameterNode(parameter);
		} else {
		    node = node.createAndGetParameterNode(parameter);
		    node.setChainData(updateChainingsArgs.get(node.getNodeParameterSet()));
		    node.setMonitorSetCount(monitorSetIds.row(node.getNodeParameterSet()).size());
		    node.setAliveParameterMasks(calculateAliveParameterMasksBoolean(node.getNodeParameterSet()));
		}
		parameterNodes.add(node);
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

    /**
     * Returns a parameterMask which selects all parameters in the subset from the parameter set. Works with compressed
     * and uncompressed array representations.
     * 
     * @param subset
     * @param parameterSet
     *            if this is the full parameter set, the result is a mask for the uncompressed array representation. Use
     *            for compressed representation in any other cases.
     * @return boolean array, with the length of the parameter set. All parameter positions which are in the subset are
     *         <code>true</code>.
     */
    protected static boolean[] toParameterSubsetMaskBoolean(Set<Parameter<?>> subset, Set<Parameter<?>> parameterSet) {
	final boolean[] result = new boolean[parameterSet.size()];
	int i = 0;
	for (Parameter<?> parameter : Util.asSortedList(parameterSet)) {
	    if (subset.contains(parameter)) {
		result[i] = true;
	    }
	    i++;
	}
	return result;
    }

    public EventContext getEventContext() {
	return new EventContext(getJoinData(), getMaxData(), getCreationEvents(), getDisablingEvents(),
		existingMonitorMasks);
    }

    /**
     * @return the rootnode of the tree of parameter nodes
     */
    public ParameterNode getParameterTree() {
	return parameterTree;
    }

    protected SetMultimap<Set<Parameter<?>>, UpdateChainingsArgs> getChainData() {
	return updateChainingsArgs;
    }

    protected FindMaxArgs[][] getMaxData() {
	FindMaxArgs[][] maxDataArray = new FindMaxArgs[pp.getBaseEvents().size()][];
	for (BaseEvent baseEvent : pp.getBaseEvents()) {
	    maxDataArray[baseEvent.getIndex()] = findMaxArgs.get(baseEvent) != null ? findMaxArgs.get(baseEvent)
		    .toArray(EMPTY_MAX_DATA) : EMPTY_MAX_DATA;
	}
	return maxDataArray;
    }

    protected JoinArgs[][] getJoinData() {
	JoinArgs[][] joinDataArray = new JoinArgs[pp.getBaseEvents().size()][];
	for (BaseEvent baseEvent : pp.getBaseEvents()) {
	    joinDataArray[baseEvent.getIndex()] = joinArgs.get(baseEvent) != null ? joinArgs.get(baseEvent).toArray(
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

    public ParametricProperty getParametricProperty() {
	return pp;
    }

    public Table<BaseEvent, Set<Parameter<?>>, List<Set<Parameter<?>>>> getDisableParameterSets() {
	return disableParameterSets;
    }

}
