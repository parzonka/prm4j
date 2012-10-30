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
package prm4j.indexing;

import prm4j.indexing.treebased.impl.DefaultParametricMonitor;
import prm4j.logic.FiniteParametricProperty;
import prm4j.logic.FiniteSpec;
import prm4j.logic.StatefulMonitor;
import prm4j.logic.treebased.LowLevelParametricProperty;

public class ParametricMonitorFactory {

    public ParametricMonitor<AbstractBaseMonitor> createParametricMonitor(FiniteSpec finiteSpec) {
	LowLevelParametricProperty pp = new LowLevelParametricProperty(new FiniteParametricProperty(finiteSpec));
	return new DefaultParametricMonitor<AbstractBaseMonitor>(pp, new StatefulMonitor(finiteSpec.getInitialState()));
    }

}
