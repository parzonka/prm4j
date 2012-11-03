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
package prm4j.logic;

import java.util.Set;

import prm4j.Util.Tuple;
import prm4j.api.Parameter;
import prm4j.indexing.BaseEvent;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.SetMultimap;

public interface ParametricProperty {

    public Set<BaseEvent> getBaseEvents();

    public Set<BaseEvent> getDisablingEvents();

    /**
     * Creation events are events for which the successor of the initial state is:
     * <ul>
     * <li>not a dead state</li>
     * <li>not the initial state itself (self-loop)</li>
     * </ul>
     *
     * @return the creation events
     */
    public Set<BaseEvent> getCreationEvents();

    /**
     * @return mapping of base events to a list of subinstances in its enabling set
     */
    public ListMultimap<BaseEvent, Set<Parameter<?>>> getMaxData();

    /**
     * @return mapping of base events to tuples representing a set of compatible joinable instances
     */
    public ListMultimap<BaseEvent, Tuple<Set<Parameter<?>>, Set<Parameter<?>>>> getJoinData();

    /**
     * @return mapping from instances to their subinstances which have associated base events
     */
    public SetMultimap<Set<Parameter<?>>, Tuple<Set<Parameter<?>>, Set<Parameter<?>>>> getChainData();

    /**
     * @return mapping from instances to sets of instances representing sets of monitors
     */
    public SetMultimap<Set<Parameter<?>>, Set<Parameter<?>>> getMonitorSetData();

}
