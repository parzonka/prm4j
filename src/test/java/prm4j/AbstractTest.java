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
package prm4j;

import java.util.HashSet;
import java.util.Set;

import prm4j.api.Parameter;
import prm4j.api.Symbol;
import prm4j.util.FSMDefinitions;

/**
 * Provides utilities for testing.
 */
public abstract class AbstractTest extends FSMDefinitions /* we mix in other definitions layer by layer */{

    public final static Set<Parameter<?>> EMPTY_PARAMETER_SET = new HashSet<Parameter<?>>();

    /**
     * Downcast SymbolN to Symbol.
     *
     * @param values
     * @return
     */
    public static Set<Symbol> asSet(Symbol... values) {
	Set<Symbol> set = new HashSet<Symbol>();
	for (Symbol s : values) {
	    set.add(s);
	}
	return set;
    }

    public static Set<Parameter<?>> asSet(Parameter<?>... values) {
	Set<Parameter<?>> set = new HashSet<Parameter<?>>();
	for (Parameter<?> s : values) {
	    set.add(s);
	}
	return set;
    }

}
