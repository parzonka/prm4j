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
package prm4j.indexing.model;

import prm4j.api.BaseEvent;

/**
 * Encapsulates arguments used in the real-time algorithm related to {@link BaseEvent}s.
 */
public class EventContext {

    private final JoinArgs[][] joinArgsArray;
    private final FindMaxArgs[][] findMaxArgsArray;
    private final boolean[] creationEvents;
    private final boolean[] disableEvents;
    // baseEvent * numberOfExistingMonitorMasks * parameterMaskLength
    private final int[][][] existingMonitorMasks;

    public EventContext(JoinArgs[][] joinArgsArray, FindMaxArgs[][] findMaxArgsArray, boolean[] creationEvents,
	    boolean[] disableEvents, int[][][] existingMonitorMasks) {
	this.joinArgsArray = joinArgsArray;
	this.findMaxArgsArray = findMaxArgsArray;
	this.creationEvents = creationEvents;
	this.disableEvents = disableEvents;
	this.existingMonitorMasks = existingMonitorMasks;
    }

    public FindMaxArgs[] getFindMaxArgs(BaseEvent baseEvent) {
	return findMaxArgsArray[baseEvent.getIndex()];
    }

    public JoinArgs[] getJoinArgs(BaseEvent baseEvent) {
	return joinArgsArray[baseEvent.getIndex()];
    }

    public boolean isCreationEvent(BaseEvent baseEvent) {
	return creationEvents[baseEvent.getIndex()];
    }

    public boolean isDisableEvent(BaseEvent baseEvent) {
	return disableEvents[baseEvent.getIndex()];
    }

    /**
     * @param baseEvent
     * @return numberOfExistingMonitorMasks * parameterMaskLength
     */
    public int[][] getExistingMonitorMasks(BaseEvent baseEvent) {
	return existingMonitorMasks[baseEvent.getIndex()];
    }

}
