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
package prm4j.api.fsm;

import java.util.Map;
import java.util.Set;

import prm4j.api.Symbol;
import prm4j.logic.MonitorState;
import prm4j.logic.StatefulSpec;

public class FSMSpec implements StatefulSpec {

    @Override
    public Set<Symbol> getSymbols() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public Map<Symbol, Set<Set<Symbol>>> getPropertyEnableSet() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public Map<MonitorState<?>, Set<Set<Symbol>>> getStateCoEnableSet() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public MonitorState<?> getInitialState() {
	// TODO Auto-generated method stub
	return null;
    }

}
