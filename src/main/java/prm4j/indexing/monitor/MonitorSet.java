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
package prm4j.indexing.monitor;

import java.util.Arrays;

import prm4j.api.Event;
import prm4j.indexing.binding.Binding;
import prm4j.indexing.model.JoinArgs;
import prm4j.indexing.model.UpdateChainingsArgs;
import prm4j.indexing.node.Node;
import prm4j.indexing.node.NodeRef;
import prm4j.indexing.node.NodeStore;

/**
 * Holds a set of {@link Monitor}s.
 */
public class MonitorSet {

    /**
     * Initial capacity of the set.
     */
    protected static final int DEFAULT_CAPACITY = 16;

    /**
     * Number of contained monitors
     */
    private int size = 0;
    /**
     * Stores the monitors
     */
    private NodeRef[] monitorSet;

    public MonitorSet() {
	monitorSet = new NodeRef[DEFAULT_CAPACITY];
    }

    /**
     * Adds a monitor to the monitor set.
     * 
     * @param monitor
     */
    public void add(NodeRef monitor) {
	monitorSet[size++] = monitor;
	ensureCapacity();
    }

    /**
     * Enlarge the capacity if we run out of space.
     */
    private void ensureCapacity() {
	if (size >= monitorSet.length) {
	    int capacity = (monitorSet.length * 3) / 2 + 1;
	    monitorSet = Arrays.copyOf(monitorSet, capacity);
	}
    }

    /**
     * Updates all alive monitors in this monitor set by processing the given event. All monitors in the set are tested,
     * if they have reached the end of their lifetime. In this case, they are removed from the monitor set. A monitor
     * has reached the end of its lifetime if is already terminated, or if some bindings, necessary to reach an
     * accepting state, have already expired.
     * 
     * @param event
     *            the current event
     */
    public void processUpdate(Event event) {
	int deadPartitionStart = 0;
	for (int i = 0; i < size; i++) { // 63
	    final NodeRef nodeRef = monitorSet[i];
	    final Monitor monitor = nodeRef.monitor;
	    // check if the monitor was already gc'ed by the NodeManager
	    if (nodeRef.monitor == null) {
		continue;
	    }
	    // check if the monitor was terminated by some other operation
	    if (nodeRef.monitor.isTerminated()) {
		nodeRef.monitor = null;
		continue;
	    }
	    // if the monitor is still alive after processing, its reference is copied into the alive partition
	    if (monitor.process(event)) {
		monitorSet[deadPartitionStart++] = nodeRef;
	    }
	}
	// nullify the dead partition
	for (int i = deadPartitionStart; i < size; i++) {
	    monitorSet[i] = null;
	}
	size = deadPartitionStart;
    }

    /**
     * Creates new nodes and bindings by trying to 'join' the current event bindings with all bindings in this monitor
     * set. All monitors in the set are tested, if they have reached the end of their lifetime. In this case, they are
     * removed from the monitor set. A monitor has reached the end of its lifetime if is already terminated, or if some
     * bindings, necessary to reach an accepting state, have already expired.
     * 
     * @param nodeStore
     *            access to all nodes
     * @param event
     *            the current event
     * @param joinableBindings
     *            a special copy of the event bindings as an 'expanded' array containing gaps to prepare the join (aka
     *            'merge') with the bindings in the compatible monitor
     * @param joinArgs
     *            stores expansionPattern and copyPattern
     */
    public void join(NodeStore nodeStore, Event event, final Binding[] joinableBindings, final JoinArgs joinArgs) {

	// create initial copy of the joinable; will be cloned again if this one is used in a monitor
	Binding[] joinable = joinableBindings.clone(); // 62
	final int[] copyPattern = joinArgs.copyPattern;
	final int[][] disableMasks = joinArgs.disableMasks;

	// post-loop invariant: all monitors up to monitorSet[deadPartitionStart] are not dead
	int deadPartitionStart = 0;

	compatibleNodesLoop: for (int i = 0; i < size; i++) {

	    final NodeRef compatibleNodeRef = monitorSet[i];
	    final Monitor compatibleMonitor = compatibleNodeRef.monitor;
	    final long compatibleMonitorTimestamp = compatibleMonitor.getTimestamp();

	    final Binding[] compatibleBindings = compatibleMonitor.getCompressedBindings();
	    // test if lifetime of monitor is already over
	    if (compatibleBindings == null) {
		continue; // don't increment the deadPartitionStart => this monitor will be removed from the set
	    }
	    createJoin(joinable, compatibleBindings, copyPattern);

	    // time check
	    for (int j = 0; j < disableMasks.length; j++) {
		final Node subInstanceNode = nodeStore.getNode(joinable, disableMasks[j]);
		if (subInstanceNode.getTimestamp() > compatibleMonitorTimestamp
			|| (subInstanceNode.getMonitor() != null && compatibleMonitorTimestamp > subInstanceNode
				.getMonitor().getTimestamp())) {
		    deadPartitionStart++; // this monitor may be still alive, we just avoid joining with it
		    continue compatibleNodesLoop;
		}
	    }

	    // retrieve the node associated with the joined binding
	    final Node joinedInstanceNode = nodeStore.getOrCreateNode(joinable);
	    // due to multiple joining phases, it can happen that the node already has a monitor
	    if (joinedInstanceNode.getMonitor() == null) {
		// inlined 'DefineTo'
		final Monitor monitor = compatibleMonitor.copy(joinable);
		joinedInstanceNode.setMonitor(monitor);
		// process and test if monitor is still alive
		if (monitor.process(event)) {
		    // this monitor is alive, so copy its reference to the alive partition
		    monitorSet[deadPartitionStart++] = compatibleNodeRef;
		}

		// update-chainings phase
		for (UpdateChainingsArgs updateChainingsArgs : joinedInstanceNode.getParameterNode()
			.getUpdateChainingsArgs()) {
		    nodeStore.getOrCreateNode(joinable, updateChainingsArgs.nodeMask)
			    .getMonitorSet(updateChainingsArgs.monitorSetId).add(joinedInstanceNode.getNodeRef());
		}
		// copy got used => clone again
		joinable = joinableBindings.clone(); // 74
	    }
	}
	// remove all dead monitors from the monitor set by nullifying all entries in the dead partition
	for (int i = deadPartitionStart; i < size; i++) {
	    monitorSet[i] = null;
	}
	size = deadPartitionStart;
    }

    /**
     * Merges the joiningBindings into the joinableBindings.
     * 
     * @param joinableBindings
     *            special expanded array representation of the current event bindings
     * @param joiningBindings
     *            compatible bindings from which some bindings will get copied
     * @param copyPattern
     *            encodes which binding from joiningBindings gets copied to which location in joinableBindings
     */
    private static void createJoin(Binding[] joinableBindings, Binding[] joiningBindings, int[] copyPattern) {
	for (int j = 0; j < copyPattern.length; j += 2) {
	    // copy from j to j+1
	    joinableBindings[copyPattern[j + 1]] = joiningBindings[copyPattern[j]];
	}
    }

    /**
     * DIAGNOSTIC: Searches the set linearly if monitor is contained, testing for object identity.
     * 
     * @param monitor
     * @return <code>true</code> if monitor is contained
     */
    public boolean contains(Monitor monitor) {
	for (int i = 0; i < size; i++) {
	    if (monitorSet[i].monitor == monitor) {
		return true;
	    }
	}
	return false;
    }

    /**
     * Returns the number of monitors contained in the monitor set. It is unknown if the monitors are alive or dead.
     * 
     * @return number of contained monitors
     */
    public int getSize() {
	return size;
    }

    /**
     * Returns the array representation of this monitor set up to the full also containing any null values.
     */
    @Override
    public String toString() {
	return Arrays.toString(monitorSet);
    }

}
