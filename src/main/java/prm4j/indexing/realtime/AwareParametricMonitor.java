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
package prm4j.indexing.realtime;

import prm4j.api.Event;
import prm4j.indexing.staticdata.EventContext;
import prm4j.indexing.staticdata.MetaNode;
import prm4j.spec.Spec;

public class AwareParametricMonitor extends DefaultParametricMonitor {

    public AwareParametricMonitor(MetaNode metaTree, EventContext eventContext, Spec spec) {
	super(metaTree, eventContext, spec);
    }

    @Override
    public synchronized void processEvent(Event event) {
	super.processEvent(event);
	if (timestamp % 1000 == 0) {
	    // TODO
	}
    }

}
