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

import prm4j.api.Symbol;

/**
 * A specification which can be implemented by classes which provide parameters, symbols and have a finite number of
 * states.
 */
public interface StatefulSpec {

    /**
     * Get all symbols.
     *
     * @return all symbols
     */
    public List<Symbol> getSymbols();

    /**
     * Returns the property enable set. It maps each symbol S into a set of symbol sets, where each symbol have to be
     * seen, before S can be observerd.
     *
     * @return the property enable set
     */
    public Map<Symbol, Set<Set<Symbol>>> getPropertyEnableSet();

    /**
     * Return the state co-enable set. It maps each state to set of symbol sets, where each symbol has to be seen at
     * least once to trigger a match. When some symbols may not be seen, no match can be triggered.
     *
     * @return the state co-enable set
     */
    public Map<MonitorState<?>, Set<Set<Symbol>>> getStateCoEnableSet();

    /**
     * Returns the initial state.
     *
     * @return the initial state
     */
    public MonitorState<?> getInitialState();

}
