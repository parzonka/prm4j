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
package prm4j.indexing.treebased.impl;

import prm4j.indexing.AbstractBaseMonitor;
import prm4j.indexing.Event;
import prm4j.indexing.IndexingStrategy;
import prm4j.indexing.treebased.BindingStore;
import prm4j.indexing.treebased.LowLevelBinding;
import prm4j.indexing.treebased.MonitorSetIterator;
import prm4j.indexing.treebased.Node;
import prm4j.indexing.treebased.NodeStore;
import prm4j.logic.EventContext;
import prm4j.logic.JoinData;

public class FastIndexingStrategy<A> implements IndexingStrategy<A> {

    private BindingStore<A> bindingStore;
    private NodeStore<A> nodeStore;
    private EventContext eventContext;

    @Override
    public void processEvent(Event<A> event) {

	final LowLevelBinding<A>[] bindings = bindingStore.getBindings(event.getBoundObjects());
	final Node<A> instanceNode = nodeStore.getNode(bindings);
	final AbstractBaseMonitor<A> instanceMonitor = instanceNode.getMonitor();

	if (instanceMonitor == null) {
	    // TODO join and chain with implicit update
	    for (JoinData joinData : eventContext.getJoinData(event)) {
		final Node<A> compatibleNode = nodeStore.getNode(bindings, joinData.getTraversalMask());
		// calculate once the bindings to be joined with the whole monitor set
		final LowLevelBinding<A>[] joinableBindings = getJoinableBindings(bindings,
			joinData.getJoinTargetPattern());

		for (int parameterSetId : joinData.getParameterSetIds()) {
		    // iterate over all compatible nodes
		    MonitorSetIterator<A> iter = compatibleNode.getMonitorSet(parameterSetId).getIterator();
		    AbstractBaseMonitor<A> compatibleMonitor = null;
		    boolean isCompatibleMonitorAlive = false;
		    while (iter.hasNext(compatibleMonitor, isCompatibleMonitorAlive)) {
			compatibleMonitor = iter.next();
			isCompatibleMonitorAlive = expand(compatibleMonitor, joinableBindings,
				joinData.getJoinSourcePattern(), joinData.getDisableMask(), event);
		    }
		}
	    }

	} else {
	    // TODO just update
	}

    }

    /**
     * Returns an array of bindings containing "gaps" enabling efficient joins by filling these gaps.
     *
     * @param binding
     * @param joinTargetPattern
     *            allows transformation of the binding to a joinable binding
     * @return a joinable binding
     */
    static <A> LowLevelBinding<A>[] getJoinableBindings(LowLevelBinding<A>[] binding, int[] joinTargetPattern) {
	@SuppressWarnings("unchecked")
	final LowLevelBinding<A>[] result = new LowLevelBinding[joinTargetPattern.length];
	for (int i = 0; i < joinTargetPattern.length; i++) {
	    final int destination = joinTargetPattern[i];
	    if (destination >= 0) // pointers to -1 encode null in the result
		result[i] = binding[destination];
	}
	return result;
    }

    protected boolean expand(AbstractBaseMonitor<A> compatibleMonitor, LowLevelBinding<A>[] joinableBindings,
	    int[] joinSourcePattern, int[] disableMask, Event<A> event) {
	// TODO Auto-generated method stub
	return false;
    }

}
