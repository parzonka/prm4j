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
import prm4j.logic.ChainingData;
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
		joinWithAllCompatibleInstances(joinData, bindings, event);
	    }

	} else {
	    // TODO just update
	}

    }

    private void joinWithAllCompatibleInstances(final JoinData joinData, final LowLevelBinding<A>[] bindings,
	    final Event<A> event) {

	final Node<A> compatibleNode = nodeStore.getNode(bindings, joinData.getTraversalMask());
	// calculate once the bindings to be joined with the whole monitor set
	final LowLevelBinding<A>[] joinableBindings = getJoinableBindings(bindings, joinData.getJoinTargetPattern());
	for (int parameterSetId : joinData.getParameterSetIds()) {
	    // iterate over all compatible nodes
	    final MonitorSetIterator<A> iter = compatibleNode.getMonitorSet(parameterSetId).getIterator();
	    AbstractBaseMonitor<A> compatibleMonitor = null;
	    boolean isCompatibleMonitorAlive = false;
	    while (iter.hasNext(compatibleMonitor, isCompatibleMonitorAlive)) {
		compatibleMonitor = iter.next();
		isCompatibleMonitorAlive = expand(compatibleMonitor, joinableBindings, joinData.getJoinSourcePattern(),
			joinData.getDisableMask(), event);
	    }
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

    /**
     * Operation which<br>
     * <ol>
     * <li>joins the given bindings with the bindings of the given oldMonitor to create a new monitor associated with
     * these bindings
     * <li>processes the given current event with the new monitor
     * <li>stores the new monitor in its associated node
     * <li>evaluates if the oldMonitor should be removed from the containing monitor set
     * </ol>
     *
     * @param oldMonitor
     *            the old monitor which is the source for the join
     * @param joinableBindings
     *            bindings which reserved space for the join
     * @param rootNode
     *            the node associated with the parameterless instance
     * @param joinSourcePattern
     *            integer pattern to perform an efficient join operation
     * @param event
     *            the current event
     * @return <code>true</code> if oldMonitor is still alive, or <code>false</code> if it should be removed from the
     *         monitorSet instead. A monitor is dead, if it can't reach a final state anymore (it is caught in a dead
     *         state or similar).<br>
     *         In case of <code>false</code>, no new monitor will be stored, because it would be dead from the start.
     */
    protected boolean expand(AbstractBaseMonitor<A> oldMonitor, LowLevelBinding<A>[] joinableBindings,
	    int[] joinSourcePattern, int[] disableMask, Event<A> event) {

	// OPTIONAL: Uses co-enable set: check if old monitor is alive
	if (!oldMonitor.isFinalStateReachable()) {
	    // in this case this monitor and all derived monitors are dead
	    return false;
	    // this could be merged with the join reducing time complexity, but would create another array, so its
	    // probably not better
	}

	// OPTIONAL: Uses enable set
	for (int paramId : disableMask) {
	    if (joinableBindings[paramId].getDisable() > oldMonitor.getTau()
		    || (joinableBindings[paramId].getTau() > 0 && joinableBindings[paramId].getTau() < oldMonitor
			    .getTau())) {
		// don't create a new monitor but the old one is alright
		return true;
	    }
	}

	// create a duplicate of the joinableBindings which will be set in the new monitor
	final LowLevelBinding<A>[] newBindings = createJoin(joinableBindings, oldMonitor.getLowLevelBindings(),
		joinSourcePattern);

	// traverse to the last node, it will be created on the fly if not existent
	final Node<A> lastNode = nodeStore.getNode(newBindings);

	if (lastNode.getMonitor() != null) {
	    return true;
	}

	final AbstractBaseMonitor<A> newMonitor = oldMonitor.copy(newBindings);

	// process the event immediately, instead of doing it in the batch update phase after expansion
	if (!newMonitor.processEvent(event)) {
	    return false;
	}

	// OPTIONAL own optimization, proof needed
	if (!newMonitor.isFinalStateReachable()) {
	    // DISCUSS prevent adding the monitor to the node if its final state is not reachable anyway
	    // but we end up with a node without monitor... we have to proof this is sound - the new bindings are now
	    // disabled
	    return true;
	}

	lastNode.setMonitor(newMonitor);

	// update chaining table with crosslinks
	updateChainings(lastNode);
	return true;
    }

    @SuppressWarnings("unchecked")
    private LowLevelBinding<A>[] createJoin(LowLevelBinding<A>[] joinableBindings,
	    LowLevelBinding<A>[] joiningBindings, int[] joinSourcePattern) {
	final LowLevelBinding<A>[] newBindings = new LowLevelBinding[joinableBindings.length];
	System.arraycopy(joinableBindings, 0, newBindings, 0, joinableBindings.length);
	// fill in the missing bindings into the duplicate from the old monitor
	for (int j = 0; j < joinSourcePattern.length; j += 2) {
	    // copy from j to j+1
	    newBindings[joinSourcePattern[j + 1]] = joiningBindings[joinSourcePattern[j]];
	}
	return newBindings;
    }

    private void updateChainings(Node<A> node) {

	final AbstractBaseMonitor<A> monitor = node.getMonitor();
	final LowLevelBinding<A>[] bindings = monitor.getLowLevelBindings();

	for (ChainingData chainingData : node.getNodeContext().getChainingData()) {
	    Node<A> lessInformativeNode = nodeStore.getNode(bindings, chainingData.getParameterMask());
	    // parameterSetId == 0 selects the set of strictly more informative instance monitors
	    for (int parameterSetId : chainingData.getParameterSetIds()) {
		lessInformativeNode.getMonitorSet(parameterSetId).add(monitor);
	    }
	}
    }

}
