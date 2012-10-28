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

import java.util.HashSet;

import prm4j.api.Parameter;

public class ParameterSet extends HashSet<Parameter<?>> implements Comparable<ParameterSet>{

    private static final long serialVersionUID = -4934535886509155024L;

    @Override
    public int compareTo(ParameterSet other) {
	return this.size() - other.size();
    }

}
