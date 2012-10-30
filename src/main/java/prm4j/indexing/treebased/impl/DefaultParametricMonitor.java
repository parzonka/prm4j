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
import prm4j.indexing.BaseMonitor;
import prm4j.indexing.Event;
import prm4j.indexing.ParametricMonitor;
import prm4j.indexing.treebased.BindingStore;
import prm4j.indexing.treebased.LowLevelBinding;
import prm4j.indexing.treebased.MonitorSetIterator;
import prm4j.indexing.treebased.Node;
import prm4j.indexing.treebased.NodeStore;
import prm4j.logic.treebased.ChainingData;
import prm4j.logic.treebased.EventContext;
import prm4j.logic.treebased.JoinData;

public class DefaultParametricMonitor<M extends BaseMonitor<M>> implements ParametricMonitor<M> {

    private final M monitorPrototype;
    private BindingStore bindingStore;
    private NodeStore nodeStore;
    private final EventContext eventContext;

    public DefaultParametricMonitor(EventContext eventContext, M monitorPrototype) {
	this.eventContext = eventContext;
	this.monitorPrototype = monitorPrototype;
    }

    @Override
    public void processEvent(Event event) {

	final LowLevelBinding[] bindings = bindingStore.getBindings(event.getBoundObjects());
	final Node instanceNode = nodeStore.getNode(bindings);
	final AbstractBaseMonitor instanceMonitor = instanceNode.getMonitor();

	if (instanceMonitor == null) {
	    // TODO join and chain with implicit update
	    for (JoinData joinData : eventContext.getJoinData(event.getBaseEvent())) {
		joinWithAllCompatibleInstances(joinData, bindings, event);
	    }

	} else {
	    // TODO just update
	}

    }

    private void joinWithAllCompatibleInstances(final JoinData joinData, final LowLevelBinding[] bindings,
	    final Event event) {

	final Node compatibleNode = nodeStore.getNode(bindings, joinData.getNodeMask());
	// calculate once the bindings to be joined with the whole monitor set
	final LowLevelBinding[] joinableBindings = getJoinableBindings(bindings, joinData.getExtensionPattern());
	// iterate over all compatible nodes
	final MonitorSetIterator iter = compatibleNode.getMonitorSet(joinData.getMonitorSetId()).getIterator();
	AbstractBaseMonitor compatibleMonitor = null;
	boolean isCompatibleMonitorAlive = false;
	// iterate over all compatible nodes
	while (iter.hasNext(compatibleMonitor, isCompatibleMonitorAlive)) {
	    compatibleMonitor = iter.next();
	    isCompatibleMonitorAlive = expand(compatibleMonitor, joinableBindings, joinData.getCopyPattern(),
		    joinData.getDiffMask(), event);
	}
    }

    /**
     * Returns an array of bindings containing "gaps" enabling efficient joins by filling these gaps.
     *
     * @param bindings
     * @param extensionPattern
     *            allows transformation of the bindings to joinable bindings
     * @return joinable bindings
     */
    static LowLevelBinding[] getJoinableBindings(LowLevelBinding[] bindings, boolean[] extensionPattern) {
	final LowLevelBinding[] joinableBindings = new LowLevelBinding[extensionPattern.length];
	int sourceIndex = 0;
	for (int i = 0; i < extensionPattern.length; i++) {
	    if (extensionPattern[i]) {
		joinableBindings[i] = bindings[sourceIndex++];
	    }
	}
	assert sourceIndex == bindings.length : "All bindings have to be taken into account.";
	return joinableBindings;
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
    protected boolean expand(AbstractBaseMonitor oldMonitor, LowLevelBinding[] joinableBindings, int[] copyPattern,
	    int[] diffMask, Event event) {

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
	final LowLevelBinding[] newBindings = createJoin(joinableBindings, oldMonitor.getLowLevelBindings(),
		copyPattern);

	// traverse to the last node, it will be created on the fly if not existent
	final Node lastNode = nodeStore.getNode(newBindings);

	if (lastNode.getMonitor() != null) {
	    return true;
	}

	final AbstractBaseMonitor newMonitor = oldMonitor.copy(newBindings);

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

    private LowLevelBinding[] createJoin(LowLevelBinding[] joinableBindings, LowLevelBinding[] joiningBindings,
	    int[] copyPattern) {
	final LowLevelBinding[] newBindings = new LowLevelBinding[joinableBindings.length];
	System.arraycopy(joinableBindings, 0, newBindings, 0, joinableBindings.length);
	// fill in the missing bindings into the duplicate from the old monitor
	for (int j = 0; j < copyPattern.length; j += 2) {
	    // copy from j to j+1
	    newBindings[copyPattern[j + 1]] = joiningBindings[copyPattern[j]];
	}
	return newBindings;
    }

    private void updateChainings(Node node) {

	final AbstractBaseMonitor monitor = node.getMonitor();
	final LowLevelBinding[] bindings = monitor.getLowLevelBindings();

	for (ChainingData chainingData : node.getNodeContext().getChainingData()) {
	    Node lessInformativeNode = nodeStore.getNode(bindings, chainingData.getNodeMask());
	    // monitorSetId == 0 selects the set of strictly more informative instance monitors
	    lessInformativeNode.getMonitorSet(chainingData.getMonitorSetId()).add(monitor);
	}
    }

    @Override
    public M createBaseMonitor() {
	return monitorPrototype.copy();
    }

    @Override
    public void reset() {
	// TODO Auto-generated method stub
    }

}
