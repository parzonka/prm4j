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

import prm4j.api.ParametricMonitor;
import prm4j.indexing.realtime.DefaultParametricMonitor;
import prm4j.logic.FiniteParametricProperty;
import prm4j.logic.FiniteSpec;
import prm4j.logic.treebased.StaticDataConverter;

public class ParametricMonitorFactory {

    public ParametricMonitor createParametricMonitor(FiniteSpec finiteSpec) {
	StaticDataConverter pp = new StaticDataConverter(new FiniteParametricProperty(finiteSpec));
	return new DefaultParametricMonitor(pp.getEventContext(), finiteSpec.getInitialMonitor());
    }

}
