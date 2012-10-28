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

import prm4j.indexing.BaseEvent;

public interface ParametricProperty {

    public Set<BaseEvent> getDisablingEvents();

    public Set<BaseEvent> getCreationEvents();

    /**
     * @return mapping of base events to a list of subinstances in its enabling set
     */
    public Map<BaseEvent, List<ParameterSet>> getEnablingInstances();

    /**
     * @return mapping of base events to tuples representing a set of compatible joinable instances
     */
    public Map<BaseEvent, List<ParameterSetTuple>> getJoinableInstances();

    /**
     * @return mapping from instances to their subinstances which have associated base events
     */
    public Map<ParameterSet, Set<ParameterSetTuple>> getChainableSubinstances();

    /**
     * @return mapping from instances to sets of instances representing sets of monitors
     */
    public Map<ParameterSet, Set<ParameterSet>> getMonitorSets();

}
