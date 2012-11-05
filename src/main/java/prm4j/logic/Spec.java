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

import prm4j.api.BaseEvent;
import prm4j.indexing.BaseMonitor;

/**
 * Represents a specification of a property. May have a infinite number of states.
 */
public interface Spec {

    public Set<BaseEvent> getBaseEvents();

    public MonitorState getInitialState();

    public BaseMonitor getInitialMonitor();

}
