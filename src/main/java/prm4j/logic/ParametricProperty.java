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

import java.util.List;
import java.util.Map;
import java.util.Set;

import prm4j.api.Parameter;
import prm4j.indexing.BaseEvent;
import prm4j.logic.SetUtil.Tuple;

public interface ParametricProperty {

    public Set<BaseEvent> getDisablingEvents();

    /**
     * Creation events are events for which the successor of the initial state is:
     * <ul>
     * <li>not a dead state</li>
     * <li>not the initial state itself (self-loop)</li>
     * </ul>
     * @return the creation events
     */
    public Set<BaseEvent> getCreationEvents();

    /**
     * @return mapping of base events to a list of subinstances in its enabling set
     */
    public Map<BaseEvent, List<Set<Parameter<?>>>> getEnablingInstances();

    /**
     * @return mapping of base events to tuples representing a set of compatible joinable instances
     */
    public Map<BaseEvent, List<Tuple<Set<Parameter<?>>, Set<Parameter<?>>>>> getJoinableInstances();

    /**
     * @return mapping from instances to their subinstances which have associated base events
     */
    public Map<Set<Parameter<?>>, Set<Tuple<Set<Parameter<?>>, Set<Parameter<?>>>>> getChainableSubinstances();

    /**
     * @return mapping from instances to sets of instances representing sets of monitors
     */
    public Map<Set<Parameter<?>>, Set<Set<Parameter<?>>>> getMonitorSets();

}
