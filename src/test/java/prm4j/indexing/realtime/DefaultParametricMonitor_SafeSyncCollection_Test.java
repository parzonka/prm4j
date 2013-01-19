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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

import prm4j.api.Condition;
import prm4j.api.fsm.FSMSpec;

@SuppressWarnings("rawtypes")
public class DefaultParametricMonitor_SafeSyncCollection_Test extends AbstractDefaultParametricMonitorTest {

    private FSM_SafeSyncCollection fsm;

    private Collection c1;
    private Iterator i1;

    /**
     * A condition can be attached to an event and must evaluate to "true" if the event has to be processed by the base
     * monitor.
     */
    final Condition threadHoldsLockOnCollection = new Condition() {
	@Override
	public boolean eval() {
	    final Collection coll = getParameterValue(fsm.c);
	    assert coll != null : "Collection must not be null";
	    return Thread.holdsLock(coll);
	}
    };
    /**
     * A second condition.
     */
    final Condition threadDoesNotHoldLockOnCollection = new Condition() {
	@Override
	public boolean eval() {
	    final Collection coll = getParameterValue(fsm.c);
	    assert coll != null : "Collection must not be null";
	    return !Thread.holdsLock(getParameterValue(fsm.c));
	}
    };

    @Before
    public void init() {
	fsm = new FSM_SafeSyncCollection();
	createDefaultParametricMonitorWithAwareComponents(new FSMSpec(fsm.fsm));

	c1 = new ArrayList<Object>();

	i1 = c1.iterator();
    }

    void assertMatch() {
	assertTrue(!fsm.matchHandler.getHandledMatches().isEmpty());
    }

    void assertNoMatch() {
	assertTrue(fsm.matchHandler.getHandledMatches().isEmpty());
    }

    /**
     * This is the condition which actually specifies the behavior of the SafeSyncCollection pattern.
     */
    @Test
    public void asyncAccessIterWithoutHoldLockCondition_match() throws Exception {
	// exercise
	pm.processEvent(fsm.sync.createEvent(c1));
	assertNoMatch();
	pm.processEvent(fsm.syncCreateIter.createEvent(c1, i1));
	assertNoMatch();
	pm.processEvent(fsm.accessIter.createConditionalEvent(i1, threadDoesNotHoldLockOnCollection));

	// verify
	assertMatch();
    }

    /**
     * Not modeled by the SafeSyncCollection pattern, just for testing the {@link Condition}-mechanism.
     */
    @Test
    public void asyncAccessIterWithHoldLockCondition_noMatch() throws Exception {
	// exercise
	pm.processEvent(fsm.sync.createEvent(c1));
	assertNoMatch();
	pm.processEvent(fsm.syncCreateIter.createEvent(c1, i1));
	assertNoMatch();
	pm.processEvent(fsm.accessIter.createConditionalEvent(i1, threadHoldsLockOnCollection));

	// verify
	assertNoMatch();
    }

    /**
     * Not modeled by the SafeSyncCollection pattern, just for testing the {@link Condition}-mechanism.
     */
    @Test
    public void syncAccessIterWithHoldLockCondition_match() throws Exception {
	// exercise
	pm.processEvent(fsm.sync.createEvent(c1));
	assertNoMatch();
	pm.processEvent(fsm.syncCreateIter.createEvent(c1, i1));
	assertNoMatch();
	synchronized (c1) {
	    pm.processEvent(fsm.accessIter.createConditionalEvent(i1, threadHoldsLockOnCollection));
	}
	// verify
	assertMatch();
    }

    /**
     * Not modeled by the SafeSyncCollection pattern, just for testing the {@link Condition}-mechanism.
     */
    @Test
    public void syncAccessIterWithoutHoldLockCondition_noMatch() throws Exception {
	// exercise
	pm.processEvent(fsm.sync.createEvent(c1));
	assertNoMatch();
	pm.processEvent(fsm.syncCreateIter.createEvent(c1, i1));
	assertNoMatch();
	synchronized (c1) {
	    pm.processEvent(fsm.accessIter.createConditionalEvent(i1, threadDoesNotHoldLockOnCollection));
	}

	// verify
	assertNoMatch();
    }

    /**
     * The other matching case
     */
    @Test
    public void syncCreateIter_match() throws Exception {
	// exercise
	pm.processEvent(fsm.sync.createEvent(c1));
	assertNoMatch();
	pm.processEvent(fsm.asyncCreateIter.createEvent(c1, i1));

	// verify
	assertMatch();
    }

}
