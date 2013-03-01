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

import prm4j.indexing.monitor.BaseMonitorState;

/**
 * Represents a specification of a property with finite number of states.
 */
public interface FiniteSpec extends Spec{

    public Set<BaseMonitorState> getStates();

}
