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

import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

import prm4j.api.fsm.FSMSpec;

@SuppressWarnings("rawtypes")
public class DefaultParametricMonitor_HasNext_Test extends AbstractDefaultParametricMonitorTest {

    FSM_HasNext fsm;

    Iterator i1;
    Iterator i2;

    @Before
    public void init() {
	fsm = new FSM_HasNext();
	createDefaultParametricMonitorWithAwareComponents(new FSMSpec(fsm.fsm));

	i1 = new Iterator<Object>() {
	    @Override
	    public boolean hasNext() {
		return false;
	    }

	    @Override
	    public Object next() {
		return null;
	    }

	    @Override
	    public void remove() {
	    }
	};

	i2 = new Iterator<String>() {
	    @Override
	    public boolean hasNext() {
		return false;
	    }

	    @Override
	    public String next() {
		return null;
	    }

	    @Override
	    public void remove() {
	    }
	};
    }

    @Test
    public void firstEvent_hasNext_createsOnlyOneMonitor() throws Exception {
	// exercise
	pm.processEvent(fsm.hasNext.createEvent(i1));

	// verify
	popNextCreatedMonitor();
	assertNoMoreCreatedMonitors();
    }

    @Test
    public void firstEvent_next_createsOnlyOneMonitor() throws Exception {
	// exercise
	pm.processEvent(fsm.hasNext.createEvent(i1));

	// verify
	popNextCreatedMonitor();
	assertNoMoreCreatedMonitors();
    }

    @Test
    public void handleMatch_trace1() throws Exception {
	// exercise
	pm.processEvent(fsm.hasNext.createEvent(i1));
	assertTrue(fsm.matchHandler.getHandledMatches().isEmpty());
	pm.processEvent(fsm.next.createEvent(i1));
	assertTrue(fsm.matchHandler.getHandledMatches().isEmpty());
	pm.processEvent(fsm.next.createEvent(i1));
	assertTrue(!fsm.matchHandler.getHandledMatches().isEmpty());
    }

    @Test
    public void handleMatch_firstEventLeadsToErrorState() throws Exception {
	// exercise
	pm.processEvent(fsm.next.createEvent(i1));
	assertTrue(!fsm.matchHandler.getHandledMatches().isEmpty());
    }

    @Test
    public void handleMatch_successiveMatchesOnErrorState() throws Exception {
	// exercise
	pm.processEvent(fsm.next.createEvent(i1));
	assertTrue(!fsm.matchHandler.getHandledMatches().isEmpty());
	pm.processEvent(fsm.next.createEvent(i1));
	assertTrue(!fsm.matchHandler.getHandledMatches().isEmpty());
    }

}
