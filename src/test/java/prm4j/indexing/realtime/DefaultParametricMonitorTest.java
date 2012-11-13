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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.junit.Test;

import prm4j.AbstractTest;
import prm4j.api.ParametricMonitor;
import prm4j.api.ParametricMonitorFactory;
import prm4j.api.fsm.FSM;
import prm4j.api.fsm.FSMSpec;
import prm4j.spec.FiniteSpec;

public class DefaultParametricMonitorTest extends AbstractTest {

    @Test
    public void process() throws Exception {
	FSM_unsafeMapIterator u = new FSM_unsafeMapIterator();
	FSM fsm = u.fsm;
	FiniteSpec finiteSpec = new FSMSpec(fsm);
	u.m.setIndex(0);
	u.c.setIndex(1);
	u.i.setIndex(2);

	ParametricMonitor pm = ParametricMonitorFactory.createParametricMonitor(finiteSpec);

	Map<Integer, String> m1 = new HashMap<Integer, String>();
	Collection<Integer> c1 = m1.keySet();
	pm.processEvent(u.createColl.createEvent(m1, c1)); // we created collection c1 from m1
	Iterator<Integer> i1 = c1.iterator();
	pm.processEvent(u.createIter.createEvent(c1, i1)); // we created iterator i1 from c1
	m1.put(1, "a");
	pm.processEvent(u.updateMap.createEvent(m1));
//	i1.next(); // triggers concurrent modification exception (which is desirable)
	pm.processEvent(u.useIter.createEvent(i1));

    }

}
