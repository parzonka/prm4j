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

import static prm4j.Util.isSubset;
import static prm4j.Util.isSubsetEq;
import static prm4j.Util.isSuperset;
import static prm4j.Util.toReverseTopologicalOrdering;
import static prm4j.Util.tuple;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import prm4j.Util.Tuple;
import prm4j.api.BaseEvent;
import prm4j.api.Parameter;
import prm4j.spec.ParametricProperty;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;

public class ParametricPropertyModel {

    private final static Set<Parameter<?>> EMPTY_PARAMETER_SET = new HashSet<Parameter<?>>();

    private final ParametricProperty pp;
    private final ListMultimap<BaseEvent, Set<Parameter<?>>> maxArgs;
    private final ListMultimap<BaseEvent, Tuple<Set<Parameter<?>>, Set<Parameter<?>>>> joinArgs;
    private final SetMultimap<Set<Parameter<?>>, Tuple<Set<Parameter<?>>, Set<Parameter<?>>>> updateChainingArgs;
    private final SetMultimap<Set<Parameter<?>>, Tuple<Set<Parameter<?>>, Boolean>> monitorSetSpecs;

    public ParametricPropertyModel(ParametricProperty pp) {
	super();
	this.pp = pp;
	maxArgs = ArrayListMultimap.create();
	joinArgs = ArrayListMultimap.create();
	updateChainingArgs = HashMultimap.create();
	monitorSetSpecs = HashMultimap.create();
	initialize();
    }

    private void initialize() {
	final Set<Tuple<Set<Parameter<?>>, Set<Parameter<?>>>> updates = getParametricProperty().getUpdates();
	for (BaseEvent baseEvent : getParametricProperty().getSpec().getBaseEvents()) {
	    final Set<Parameter<?>> parameterSet = baseEvent.getParameters();
	    for (Set<Parameter<?>> enablingParameterSet : toReverseTopologicalOrdering(getParametricProperty()
		    .getEnableParameterSets().get(baseEvent))) {
		/*
		 * the empty parameter set {} can be filtered. No parameter set can contain less elements, so there can
		 * be no maxArgs = (X -> {}). And a joindata = (e -> ( {} -> {} )) makes no sense either. The same with
		 * chaining from {} to {} and updates.
		 */
		if (!enablingParameterSet.equals(EMPTY_PARAMETER_SET)
			&& !isSubsetEq(parameterSet, enablingParameterSet)) {
		    if (isSuperset(parameterSet, enablingParameterSet)) {
			maxArgs.put(baseEvent, enablingParameterSet);
		    } else {
			final Set<Parameter<?>> compatibleSubset = Sets
				.intersection(parameterSet, enablingParameterSet);
			final Tuple<Set<Parameter<?>>, Set<Parameter<?>>> tuple = tuple(compatibleSubset,
				enablingParameterSet);
			joinArgs.put(baseEvent, tuple);
			updateChainingArgs.put(enablingParameterSet, tuple);
			if (updates.contains(tuple)) {
			    monitorSetSpecs.put(compatibleSubset, tuple(enablingParameterSet, true));
			} else {
			    monitorSetSpecs.put(compatibleSubset, tuple(enablingParameterSet, false));
			}
		    }
		}
	    }
	}
	for (Tuple<Set<Parameter<?>>, Set<Parameter<?>>> tuple : updates) {
	    if (!monitorSetSpecs.containsEntry(tuple._1(), tuple(tuple._2(), true))) {
		updateChainingArgs.put(tuple._2(), tuple(tuple._1(), EMPTY_PARAMETER_SET));
		monitorSetSpecs.put(tuple._1(), tuple(EMPTY_PARAMETER_SET, true));
	    }
	}
    }

    /**
     * Returns a mapping of baseEvents to parameter sets X where instances i with Dom(i) = X are expected to have
     * monitors with dead states created from disable events.
     */
    public SetMultimap<BaseEvent, Set<Parameter<?>>> getDisableInstanceTypes() {
	final SetMultimap<BaseEvent, Set<Parameter<?>>> result = HashMultimap.create();
	for (BaseEvent baseEvent : pp.getSpec().getBaseEvents()) {
	    for (BaseEvent disableEvent : pp.getDisableEvents()) {
		if (isSubset(disableEvent.getParameters(), baseEvent.getParameters())) {
		    result.put(baseEvent, disableEvent.getParameters());
		}
	    }
	}
	return result;
    }

    /**
     * Returns a mapping (X, X') -> monitorSetId where X,X' are parameter sets. X identifies the node and X' identifies
     * the parameter set of the receiving instance.
     * 
     * @return mapping (X, X') -> monitorSetId
     */
    public Table<Set<Parameter<?>>, Set<Parameter<?>>, Integer> getMonitorSetIds() {
	final Table<Set<Parameter<?>>, Set<Parameter<?>>, Integer> monitorSetIds = HashBasedTable.create();
	for (Set<Parameter<?>> parameterSet : getMonitorSetSpecs().keys()) {
	    int i = 0;
	    for (Tuple<Set<Parameter<?>>, Boolean> tuple : toOrderedTupleListByLeftSize(getMonitorSetSpecs().get(
		    parameterSet))) {
		monitorSetIds.put(parameterSet, tuple._1(), i++);
	    }
	}
	return monitorSetIds;
    }

    /**
     * Return a ordered list of tuples. The ordering is not neccessary for correctness. It is just useful for display
     * purposes so that the empty set (as left component) will get associated with the id 0.
     * 
     * @param setOfTuples
     * @return a list of tuples ordered by size of the left component
     */
    private static <T, S> List<Tuple<Set<T>, S>> toOrderedTupleListByLeftSize(Set<Tuple<Set<T>, S>> setOfTuples) {
	final ArrayList<Tuple<Set<T>, S>> tupleList = new ArrayList<Tuple<Set<T>, S>>(setOfTuples);
	Collections.sort(tupleList, new Comparator<Tuple<Set<T>, S>>() {
	    @Override
	    public int compare(Tuple<Set<T>, S> t1, Tuple<Set<T>, S> t2) {
		return t1._1().size() - t2._1().size();
	    }
	});
	return tupleList;
    }

    /**
     * Returns the set of parameter sets X, where only instances i with Dom(i) = X can have monitors.
     * 
     * @return all instance types which can carry monitors
     */
    public Set<Set<Parameter<?>>> getMonitorInstanceTypes() {
	final Set<Set<Parameter<?>>> result = new HashSet<Set<Parameter<?>>>();
	for (BaseEvent baseEvent : getParametricProperty().getSpec().getBaseEvents()) {
	    for (Set<Parameter<?>> enableParameterSet : getParametricProperty().getEnableParameterSets().get(baseEvent)) {
		result.add(Sets.union(baseEvent.getParameters(), enableParameterSet));
	    }
	}
	for (BaseEvent creationEvent : getParametricProperty().getCreationEvents()) {
	    result.add(creationEvent.getParameters());
	}
	return result;
    }

    /**
     * Returns the set of X where X is (at least one)
     * <ul>
     * <li>a base event definition
     * <li>the parametric instance type of a node storing a monitor set of compatible nodes
     * <li>the parametric instance type of a node carrying a monitor
     * 
     * @return the instance types relevant in the parameter tree
     */
    public Set<Set<Parameter<?>>> getRelevantInstanceTypes() {
	final Set<Set<Parameter<?>>> result = new HashSet<Set<Parameter<?>>>();
	for (BaseEvent baseEvent : getParametricProperty().getSpec().getBaseEvents()) {
	    // a base event definition
	    result.add(baseEvent.getParameters());
	    // parametric instance type of a node storing a monitor set of compatible nodes
	    for (Tuple<Set<Parameter<?>>, Set<Parameter<?>>> tuple : getJoinTuples().get(baseEvent)) {
		result.add(tuple._1());
	    }
	    // parametric instance type of a node carrying a monitor
	    result.addAll(getMonitorInstanceTypes());
	}
	return result;
    }

    public ParametricProperty getParametricProperty() {
	return pp;
    }

    /**
     * Returns a mapping of baseEvents to lists of parameter sets X where X identifies a instance which may store a
     * monitor state relevant for the instance carried with the base event.
     * 
     * @return baseEvent to list of instance types relevant for the find-max phase
     */
    public ListMultimap<BaseEvent, Set<Parameter<?>>> getFindMaxInstanceTypes() {
	return maxArgs;
    }

    /**
     * Returns a mapping of baseEvents to lists of tuples (X,X') where X identifies a parametric instance which stores
     * the common part of the parametric instance carried in the event and X' identifies the domain of the combined
     * parametric instance.
     * 
     * @return baseEvent to list of instance types relevant for the join phase
     */
    public ListMultimap<BaseEvent, Tuple<Set<Parameter<?>>, Set<Parameter<?>>>> getJoinTuples() {
	return joinArgs;
    }

    /**
     * @return the updateChainingArgs
     */
    public SetMultimap<Set<Parameter<?>>, Tuple<Set<Parameter<?>>, Set<Parameter<?>>>> getUpdateChainingTuples() {
	return updateChainingArgs;
    }

    /**
     * Returns a mapping of X to set of tuples (X',b) where X identifies a parametric instance which stores a monitor
     * set and X' represents the domain of all instances stored in the set (when X' = {}, then the domain is not
     * relevant, e.g. it is the domain which is the difference from the domains of other monitor sets).
     * 
     * @return the monitor set specifications for each node
     */
    public SetMultimap<Set<Parameter<?>>, Tuple<Set<Parameter<?>>, Boolean>> getMonitorSetSpecs() {
	return monitorSetSpecs;
    }

    @Override
    public String toString() {
	return "getDisableInstanceTypes=" + getDisableInstanceTypes() + "\n" + "getFindMaxInstanceTypes="
		+ getFindMaxInstanceTypes() + "\n" + "getJoinTuples=" + getJoinTuples() + "\n"
		+ "getUpdateChainingTuples=" + getUpdateChainingTuples() + "\n" + "getMonitorSetSpecs="
		+ getMonitorSetSpecs() + "\n" + "getMonitorSetIds=" + getMonitorSetIds() + "\n"
		+ "getMonitorInstanceTypes=" + getMonitorInstanceTypes() + "\n" + "getRelevantInstanceTypes="
		+ getRelevantInstanceTypes();
    }
}
