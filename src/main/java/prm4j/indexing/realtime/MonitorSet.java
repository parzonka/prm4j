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

import java.util.Arrays;

import prm4j.api.Event;
import prm4j.indexing.BaseMonitor;
import prm4j.indexing.staticdata.ChainData;

/**
 * Holds a set of {@link BaseMonitor}s.
 */
public class MonitorSet {

    private static int ID = 0;

    public final int monitorSetId;

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
	monitorSetId = ID++;
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
    public void processEvent(Event event) {
	int aliveMonitors = 0;
	for (int i = 0; i < size; i++) { // 63
	    final NodeRef nodeRef = monitorSet[i];
	    final BaseMonitor monitor = nodeRef.monitor;
	    if (monitor.isTerminated()) {
		continue;
	    } else {
		monitorSet[aliveMonitors++] = nodeRef;
	    }
	    monitor.processEvent(event);
	}
	for (int i = aliveMonitors; i < size; i++) {
	    monitorSet[i] = null;
	}
	size = aliveMonitors;
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
     * @param someBindingsAreKnown
     *            <code>true</code>, if some bindings have been seen already
     * @param tmax
     *            the latest 'seeing' time of all bindings in joinableBindings
     * @param copyPattern
     *            used to copy a number of bindings from the compatible monitor
     */
    public void join(NodeStore nodeStore, Event event, final LowLevelBinding[] joinableBindings,
	    boolean someBindingsAreKnown, long tmax, int[] copyPattern) {

	// create initial copy of the joinable; will gets cloned again if this one is used in a monitor
	LowLevelBinding[] joinable = joinableBindings.clone(); // 62

	// post-loop invariant: all monitors up to monitorSet[aliveMonitors] are not dead
	int aliveMonitors = 0;
	// iterate over all compatible nodes
	for (int i = 0; i < size; i++) { // 63

	    // this monitor holds some bindings we would like to copy to our joined bindings
	    final NodeRef compatibleNodeRef = monitorSet[i];
	    final BaseMonitor compatibleMonitor = compatibleNodeRef.monitor;

	    // test if some of the bindings had been used already after the compatible monitor was created.
	    if (someBindingsAreKnown && compatibleMonitor.getCreationTime() < tmax) { // 64
		// => the binding was not yet enabled => current event is not part of an accepting trace continued from
		// this monitor => the joined monitor would never reach accepting state
		aliveMonitors++; // this monitor may be still alive, we just avoid joining with it
		continue; // 65
	    }
	    final LowLevelBinding[] compatibleBindings = compatibleMonitor.getLowLevelBindings();
	    // test if lifetime of monitor is already over
	    if (compatibleBindings == null) {
		continue; // don't increment 'aliveMonitors' counter => this monitor will be removed from the set
	    }
	    // copy some compatible bindings to our joinable
	    createJoin(joinable, compatibleBindings, copyPattern); // 67 - 71
	    // retrieve the node associated with the joined binding
	    final Node lastNode = nodeStore.getOrCreateNode(joinable);
	    // due to multiple joining phases, it can happen that the node already has a monitor
	    if (lastNode.getMonitor() == null) { // 72
		// inlined 'DefineTo' // 73
		final BaseMonitor monitor = compatibleMonitor.copy(joinable); // 102-105
		// process and test if monitor is still alive
		if (monitor.processEvent(event)) { // 103
		    // this monitor is alive, so copy it to the alive partition
		    monitorSet[aliveMonitors++] = compatibleNodeRef;
		}
		lastNode.setMonitor(monitor); // 106
		// normal chain phase: connect necessary less informative instances so the joined binding will gets some
		// updates (or be used in join phase itself as compatible monitor)
		for (ChainData chainData : lastNode.getMetaNode().getChainDataArray()) {
		    nodeStore.getOrCreateNode(joinable, chainData.getNodeMask())
			    .getMonitorSet(chainData.getMonitorSetId()).add(lastNode.getNodeRef());
		} // 99
		  // copy got used => clone again
		joinable = joinableBindings.clone(); // 74
	    }
	}
	// remove all dead monitors from the monitor set by nullifying the 'dead partition'
	for (int i = aliveMonitors; i < size; i++) {
	    monitorSet[i] = null;
	}
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
    private static void createJoin(LowLevelBinding[] joinableBindings, LowLevelBinding[] joiningBindings,
	    int[] copyPattern) {
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
    public boolean contains(BaseMonitor monitor) {
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

    @Override
    public int hashCode() {
        return monitorSetId;
    }

}
