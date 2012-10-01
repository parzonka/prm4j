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

public interface MonitorStateProvider<A> {

    /**
     * Returns an initial monitor state. Clients assume, that changes to this state are not possible or have no side
     * effects to other initial states returned by this method.
     *
     * @return an initial state
     */
    public MonitorState<A> getInitialState();

}
