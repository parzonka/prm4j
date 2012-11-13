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
package prm4j.api;

import prm4j.indexing.realtime.DefaultParametricMonitor;
import prm4j.indexing.staticdata.StaticDataConverter;
import prm4j.spec.FiniteParametricProperty;
import prm4j.spec.FiniteSpec;

public class ParametricMonitorFactory {

    public static ParametricMonitor createParametricMonitor(FiniteSpec finiteSpec) {
	StaticDataConverter converter = new StaticDataConverter(new FiniteParametricProperty(finiteSpec));
	return new DefaultParametricMonitor(converter.getMetaTree(), converter.getEventContext(), finiteSpec);
    }

}
