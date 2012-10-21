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
import prm4j.logic.treebased.ChainingData;
import prm4j.logic.treebased.EventContext;
import prm4j.logic.treebased.JoinData;

/**
 * @param <E>
 *            the type of base event processed by monitors
 */
public class FastIndexingStrategy<E> implements IndexingStrategy<E> {

    private BindingStore<E> bindingStore;
    private NodeStore<E> nodeStore;
    private EventContext eventContext;

    @Override
    public void processEvent(Event<E> event) {

	final LowLevelBinding<E>[] bindings = bindingStore.getBindings(event.getBoundObjects());
	final Node<E> instanceNode = nodeStore.getNode(bindings);
	final AbstractBaseMonitor<E> instanceMonitor = instanceNode.getMonitor();

	if (instanceMonitor == null) {
	    // TODO join and chain with implicit update
	    for (JoinData joinData : eventContext.getJoinData(event)) {
		joinWithAllCompatibleInstances(joinData, bindings, event);
	    }

	} else {
	    // TODO just update
	}

    }

    private void joinWithAllCompatibleInstances(final JoinData joinData, final LowLevelBinding<E>[] bindings,
	    final Event<E> event) {

	final Node<E> compatibleNode = nodeStore.getNode(bindings, joinData.getNodeMask());
	// calculate once the bindings to be joined with the whole monitor set
	final LowLevelBinding<E>[] joinableBindings = getJoinableBindings(bindings, joinData.getExtensionPattern());
	for (int parameterSetId : joinData.getMonitorSetIds()) {
	    // iterate over all compatible nodes
	    final MonitorSetIterator<E> iter = compatibleNode.getMonitorSet(parameterSetId).getIterator();
	    AbstractBaseMonitor<E> compatibleMonitor = null;
	    boolean isCompatibleMonitorAlive = false;
	    while (iter.hasNext(compatibleMonitor, isCompatibleMonitorAlive)) {
		compatibleMonitor = iter.next();
		isCompatibleMonitorAlive = expand(compatibleMonitor, joinableBindings, joinData.getCopyPattern(),
			joinData.getDiffMask(), event);
	    }
	}
    }

    /**
     * Returns an array of bindings containing "gaps" enabling efficient joins by filling these gaps.
     *
     * @param binding
     * @param expansionPattern
     *            allows transformation of the binding to a joinable binding
     * @return a joinable binding
     */
    static <E> LowLevelBinding<E>[] getJoinableBindings(LowLevelBinding<E>[] binding, int[] expansionPattern) {
	@SuppressWarnings("unchecked")
	final LowLevelBinding<E>[] result = new LowLevelBinding[expansionPattern.length];
	for (int i = 0; i < expansionPattern.length; i++) {
	    final int destination = expansionPattern[i];
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
     * @param copyPattern
     *            integer pattern to perform an efficient join operation
     * @param event
     *            the current event
     * @return <code>true</code> if oldMonitor is still alive, or <code>false</code> if it should be removed from the
     *         monitorSet instead. A monitor is dead, if it can't reach a final state anymore (it is caught in a dead
     *         state or similar).<br>
     *         In case of <code>false</code>, no new monitor will be stored, because it would be dead from the start.
     */
    protected boolean expand(AbstractBaseMonitor<E> oldMonitor, LowLevelBinding<E>[] joinableBindings,
	    int[] copyPattern, int[] diffMask, Event<E> event) {

	// OPTIONAL: Uses co-enable set: check if old monitor is alive
	if (!oldMonitor.isFinalStateReachable()) {
	    // in this case this monitor and all derived monitors are dead
	    return false;
	    // this could be merged with the join reducing time complexity, but would create another array, so its
	    // probably not better
	}

	// OPTIONAL: Uses enable set
	for (int paramId : diffMask) {
	    if (joinableBindings[paramId].getDisable() > oldMonitor.getTau()
		    || (joinableBindings[paramId].getTau() > 0 && joinableBindings[paramId].getTau() < oldMonitor
			    .getTau())) {
		// don't create a new monitor but the old one is alright
		return true;
	    }
	}

	// create a duplicate of the joinableBindings which will be set in the new monitor
	final LowLevelBinding<E>[] newBindings = createJoin(joinableBindings, oldMonitor.getLowLevelBindings(),
		copyPattern);

	// traverse to the last node, it will be created on the fly if not existent
	final Node<E> lastNode = nodeStore.getNode(newBindings);

	if (lastNode.getMonitor() != null) {
	    return true;
	}

	final AbstractBaseMonitor<E> newMonitor = oldMonitor.copy(newBindings);

	// process the event immediately, instead of doing it in the batch update phase after expansion
	if (!newMonitor.processEvent(event.getBaseEvent())) {
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
    private LowLevelBinding<E>[] createJoin(LowLevelBinding<E>[] joinableBindings,
	    LowLevelBinding<E>[] joiningBindings, int[] copyPattern) {
	final LowLevelBinding<E>[] newBindings = new LowLevelBinding[joinableBindings.length];
	System.arraycopy(joinableBindings, 0, newBindings, 0, joinableBindings.length);
	// fill in the missing bindings into the duplicate from the old monitor
	for (int j = 0; j < copyPattern.length; j += 2) {
	    // copy from j to j+1
	    newBindings[copyPattern[j + 1]] = joiningBindings[copyPattern[j]];
	}
	return newBindings;
    }

    private void updateChainings(Node<E> node) {

	final AbstractBaseMonitor<E> monitor = node.getMonitor();
	final LowLevelBinding<E>[] bindings = monitor.getLowLevelBindings();

	for (ChainingData chainingData : node.getNodeContext().getChainingData()) {
	    Node<E> lessInformativeNode = nodeStore.getNode(bindings, chainingData.getParameterMask());
	    // parameterSetId == 0 selects the set of strictly more informative instance monitors
	    for (int parameterSetId : chainingData.getParameterSetIds()) {
		lessInformativeNode.getMonitorSet(parameterSetId).add(monitor);
	    }
	}
    }

}
