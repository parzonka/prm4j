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
package prm4j.spec;

import java.util.Set;

import prm4j.Util.Tuple;
import prm4j.api.BaseEvent;
import prm4j.api.Parameter;
import prm4j.indexing.monitor.BaseMonitorState;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.SetMultimap;

/**
 * Instances of this interface fully specify a parametric property to be monitored.
 */
public interface ParametricProperty {

    public boolean isFinite();

    public int getStateCount();

    public Set<BaseEvent> getBaseEvents();

    public Set<Parameter<?>> getParameters();

    public BaseMonitorState getInitialState();

    public Set<BaseEvent> getCreationEvents();

    public Set<BaseEvent> getDisablingEvents();

    public Set<Set<Parameter<?>>> getPossibleParameterSets();

    public SetMultimap<BaseEvent, Set<BaseEvent>> getEnablingEventSets();

    public SetMultimap<BaseEvent, Set<Parameter<?>>> getEnablingParameterSets();

    /**
     * @return mapping of base events to a list of subinstances in its enabling set
     */
    public ListMultimap<BaseEvent, Set<Parameter<?>>> getMaxData();

    /**
     * @return multiMap[baseEvent -> tuple(compatibleSubset, enablingParameterSet)]
     */
    public ListMultimap<BaseEvent, Tuple<Set<Parameter<?>>, Set<Parameter<?>>>> getJoinData();

    /**
     * @return mapping from instances to their subinstances which have associated base events
     */
    public SetMultimap<Set<Parameter<?>>, Tuple<Set<Parameter<?>>, Set<Parameter<?>>>> getChainData();

    /**
     * @return mapping from instances to sets of instances representing sets of monitors
     */
    public SetMultimap<Set<Parameter<?>>, Tuple<Set<Parameter<?>>, Boolean>> getMonitorSetData();

    public Set<Set<Parameter<?>>> getAliveParameterSets();

}
