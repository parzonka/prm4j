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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

import prm4j.api.Condition;
import prm4j.api.fsm.FSMSpec;

@SuppressWarnings("rawtypes")
public class DefaultParametricMonitor_SafeSyncCollection_Test extends AbstractDefaultParametricMonitorTest {

    FSM_SafeSyncCollection fsm;

    Collection c1;
    Iterator i1;

    /**
     * A condition can be attached to an event and must evaluate to "true" if the event has to be processed by the base
     * monitor.
     */
    final Condition threadHoldsLockOnCollection = new Condition() {
	@Override
	public boolean eval() {
	    return Thread.holdsLock(getParameterValue(fsm.c));
	}
    };
    /**
     * A second condition.
     */
    final Condition threadDoesNotHoldLockOnCollection = new Condition() {
	@Override
	public boolean eval() {
	    return !Thread.holdsLock(getParameterValue(fsm.c));
	}
    };

    @Before
    public void init() {
	fsm = new FSM_SafeSyncCollection();
	createDefaultParametricMonitorWithAwareComponents(new FSMSpec(fsm.fsm));

	c1 = new Collection<Object>() {

	    @Override
	    public boolean add(Object arg0) {
		return false;
	    }

	    @Override
	    public boolean addAll(Collection<? extends Object> arg0) {
		return false;
	    }

	    @Override
	    public void clear() {

	    }

	    @Override
	    public boolean contains(Object arg0) {
		return false;
	    }

	    @Override
	    public boolean containsAll(Collection<?> arg0) {
		return false;
	    }

	    @Override
	    public boolean isEmpty() {
		return false;
	    }

	    @Override
	    public Iterator<Object> iterator() {
		return null;
	    }

	    @Override
	    public boolean remove(Object arg0) {
		return false;
	    }

	    @Override
	    public boolean removeAll(Collection<?> arg0) {
		return false;
	    }

	    @Override
	    public boolean retainAll(Collection<?> arg0) {
		return false;
	    }

	    @Override
	    public int size() {
		return 0;
	    }

	    @Override
	    public Object[] toArray() {
		return null;
	    }

	    @Override
	    public <T> T[] toArray(T[] arg0) {
		return null;
	    }
	};

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
    }

    @Test
    public void firstEvent_hasNext_createsOnlyOneMonitor() throws Exception {
	// exercise
	pm.processEvent(fsm.sync.createEvent(c1));

	// verify
	popNextCreatedMonitor();
	assertNoMoreCreatedMonitors();
    }

    @Test
    public void accessIterWithHoldLock_noMatch() throws Exception {
	// exercise
	pm.processEvent(fsm.sync.createEvent(c1));
	assertTrue(fsm.matchHandler.getHandledMatches().isEmpty());
	pm.processEvent(fsm.syncCreateIter.createEvent(c1, i1));
	assertTrue(fsm.matchHandler.getHandledMatches().isEmpty());
	pm.processEvent(fsm.accessIter.createConditionalEvent(i1, threadHoldsLockOnCollection));

	// verify
	assertTrue(fsm.matchHandler.getHandledMatches().isEmpty());
    }

    @Test
    public void accessIterWithoutHoldLock_match() throws Exception {
	// exercise
	pm.processEvent(fsm.sync.createEvent(c1));
	assertTrue(fsm.matchHandler.getHandledMatches().isEmpty());
	pm.processEvent(fsm.syncCreateIter.createEvent(c1, i1));
	assertTrue(fsm.matchHandler.getHandledMatches().isEmpty());
	pm.processEvent(fsm.accessIter.createConditionalEvent(i1, threadDoesNotHoldLockOnCollection));

	// verify
	assertFalse(fsm.matchHandler.getHandledMatches().isEmpty());
    }

}
