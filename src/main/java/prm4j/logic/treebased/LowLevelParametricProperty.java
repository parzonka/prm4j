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

import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import prm4j.api.Parameter;
import prm4j.indexing.BaseEvent;
import prm4j.logic.ParametricProperty;
import prm4j.logic.SetUtil;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Table;

public class LowLevelParametricProperty {

    private ListMultimap<BaseEvent, EnableData> enableData;
    private ListMultimap<BaseEvent, JoinData> joinData;
    private SetMultimap<Set<Parameter<?>>, ChainingData> chainingData;
    private Table<Set<Parameter<?>>, Set<Parameter<?>>, Integer> monitorSetIds;

    public LowLevelParametricProperty(ParametricProperty pp) {
	convert(pp);
    }

    private void convert(ParametricProperty pp) {
	for (Entry<Set<Parameter<?>>, Set<Parameter<?>>> entry : pp.getMonitorSets().entries()) {
	    @SuppressWarnings("unchecked")
	    Set<Parameter<?>>[] sortedSets = entry.getValue().toArray(new Set[0]);
	    Arrays.sort(sortedSets, SetUtil.TOPOLOGICAL_SET_COMPARATOR);
	    int i = 0;
	    for (Set<Parameter<?>> set : sortedSets) {
		monitorSetIds.put(entry.getKey(), set, i++);
	    }
	}

    }

    public List<EnableData> getEnableData(BaseEvent baseEvent) {
	return enableData.get(baseEvent);
    }

    public List<JoinData> getJoinData(BaseEvent baseEvent) {
	return joinData.get(baseEvent);
    }

    public Set<ChainingData> getChainingData(Set<Parameter<?>> parameterSet) {
	return chainingData.get(parameterSet);
    }

}
