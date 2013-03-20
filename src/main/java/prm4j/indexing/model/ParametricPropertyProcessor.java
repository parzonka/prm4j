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

import java.util.HashSet;
import java.util.Set;

import prm4j.Util;
import prm4j.Util.Tuple;
import prm4j.api.Parameter;
import prm4j.indexing.IndexingUtils;
import prm4j.spec.ParametricProperty;

import com.google.common.collect.Table;

public class ParametricPropertyProcessor {

    private final EventContext eventContext;
    private final ParameterNode parameterTree;

    public ParametricPropertyProcessor(ParametricProperty finiteParametricProperty) {
	ParametricPropertyModel ppm = new ParametricPropertyModel(finiteParametricProperty);
	eventContext = new EventContext(ppm);
	parameterTree = createParameterTree(ppm);
    }

    private ParameterNode createParameterTree(ParametricPropertyModel ppm) {
	final ParameterNode parameterTreeRoot = new ParameterNode(new HashSet<Parameter<?>>(), ppm
		.getParametricProperty().getSpec().getFullParameterSet());
	final Table<Set<Parameter<?>>, Set<Parameter<?>>, Integer> monitorSetIds = ppm.getMonitorSetIds();

	for (Set<Parameter<?>> parameterSet : ppm.getRelevantInstanceTypes()) {
	    if (parameterSet.isEmpty()) {
		parameterTreeRoot.setChainData(createUpdateChainingArgs(ppm, parameterSet));
		parameterTreeRoot.setMonitorSetCount(monitorSetIds.row(parameterSet).size());
		parameterTreeRoot.setAliveParameterMasks(calculateAliveParameterMasksBoolean(ppm, parameterSet));
	    }
	    ParameterNode node = parameterTreeRoot;
	    for (Parameter<?> parameter : Util.asSortedList(parameterSet)) {
		if (node.getParameterNode(parameter) != null) {
		    node = node.getParameterNode(parameter);
		} else {
		    node = node.createAndGetParameterNode(parameter);
		    node.setChainData(createUpdateChainingArgs(ppm, node.getNodeParameterSet()));
		    node.setMonitorSetCount(monitorSetIds.row(node.getNodeParameterSet()).size());
		    node.setAliveParameterMasks(calculateAliveParameterMasksBoolean(ppm, node.getNodeParameterSet()));
		}
	    }
	}
	return parameterTreeRoot;
    }

    private int[][] calculateAliveParameterMasksBoolean(ParametricPropertyModel ppm, Set<Parameter<?>> parameterSet) {
	Set<Set<Parameter<?>>> parameterSets = ppm.getParametricProperty().getAliveParameterSets().get(parameterSet);
	IndexingUtils.toParameterMasks(parameterSets, parameterSet);
	return IndexingUtils.toParameterMasks(parameterSets, parameterSet);
    }

    private Set<UpdateChainingsArgs> createUpdateChainingArgs(ParametricPropertyModel ppm,
	    Set<Parameter<?>> parameterSet) {
	final Set<UpdateChainingsArgs> result = new HashSet<UpdateChainingsArgs>();
	for (Tuple<Set<Parameter<?>>, Set<Parameter<?>>> tuple : ppm.getUpdateChainingTuples().get(parameterSet)) {
	    result.add(new UpdateChainingsArgs(toParameterMask(tuple._1()), ppm.getMonitorSetIds().get(tuple._1(),
		    tuple._2())));
	}
	return result;
    }

    /**
     * @return the eventContext
     */
    public EventContext getEventContext() {
	return eventContext;
    }

    /**
     * @return the parameterTree
     */
    public ParameterNode getParameterTree() {
	return parameterTree;
    }

}
