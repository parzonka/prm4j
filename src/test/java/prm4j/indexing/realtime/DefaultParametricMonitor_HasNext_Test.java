/*
 * Copyright (c) 2012, 2013 Mateusz Parzonka
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Mateusz Parzonka - initial API and implementation
 */
package prm4j.indexing.realtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

import prm4j.api.fsm.FSMSpec;
import prm4j.indexing.model.ModelVerifier;
import prm4j.indexing.node.LeafNode;

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
		// do nothing
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
		// do nothing
	    }
	};
    }

    @Test
    @SuppressWarnings("unchecked")
    public void model() throws Exception {
	final ModelVerifier mv = new ModelVerifier(ppm);
	mv.getJoinTuples().verify();
	mv.getFindMaxInstanceTypes().verify();
	mv.getAliveParameterSets().put(asSet(fsm.i), asSet(fsm.i)).verify();
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
    public void firstEvent_leafNodeIsCreated() throws Exception {
	// exercise
	pm.processEvent(fsm.next.createEvent(i1));
	// verify
	assertEquals(LeafNode.class, getNode(i1).getClass());
    }

    @Test
    public void handleMatch_trace1() throws Exception {
	// exercise
	pm.processEvent(fsm.hasNext.createEvent(i1));
	assertMatchesCount(0);
	pm.processEvent(fsm.next.createEvent(i1));
	assertMatchesCount(0);
	assertTrue(fsm.matchHandler.getHandledMatches().isEmpty());
	pm.processEvent(fsm.next.createEvent(i1));
	assertTrue(!fsm.matchHandler.getHandledMatches().isEmpty());
    }

    @Test
    public void handleMatch_firstEventLeadsToErrorState() throws Exception {
	// exercise
	pm.processEvent(fsm.next.createEvent(i1));
	// verify
	assertMatchesCount(1);
    }

    @Test
    public void handleMatch_successiveMatchesOnErrorState() throws Exception {
	pm.processEvent(fsm.next.createEvent(i1));
	assertMatchesCount(1);
	pm.processEvent(fsm.next.createEvent(i1));
	assertMatchesCount(2);
	pm.processEvent(fsm.next.createEvent(i2));
	assertMatchesCount(3);
    }

    @Test
    public void detectMatches_hasNext_next_next_next() throws Exception {
	pm.processEvent(fsm.hasNext.createEvent(i1));
	assertMatchesCount(0);
	pm.processEvent(fsm.next.createEvent(i1));
	assertMatchesCount(0);
	pm.processEvent(fsm.next.createEvent(i1));
	assertMatchesCount(1);
	pm.processEvent(fsm.next.createEvent(i2));
	assertMatchesCount(2);
    }

    private void assertMatchesCount(int count) {
	assertEquals(count, fsm.matchHandler.getHandledMatches().size());
    }

}
