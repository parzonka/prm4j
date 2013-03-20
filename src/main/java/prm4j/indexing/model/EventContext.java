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

import static prm4j.indexing.IndexingUtils.toParameterMasks;
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
    private final int[][][] disableMonitors;

    public EventContext(JoinArgs[][] joinArgsArray, FindMaxArgs[][] findMaxArgsArray, boolean[] creationEvents,
	    boolean[] disableEvents, int[][][] existingMonitorMasks) {
	this.joinArgsArray = joinArgsArray;
	this.findMaxArgsArray = findMaxArgsArray;
	this.creationEvents = creationEvents;
	this.disableEvents = disableEvents;
	disableMonitors = existingMonitorMasks;
    }

    public EventContext(ParametricPropertyModel ppm) {
	joinArgsArray = getJoinArgsArray(ppm);
	findMaxArgsArray = getFindMaxArgsArray(ppm);
	creationEvents = getCreationEvents(ppm);
	disableEvents = getDisableEvents(ppm);
	disableMonitors = getDisableMonitors(ppm);
    }

    protected static int[][][] getDisableMonitors(ParametricPropertyModel ppm) {
	final int[][][] result = new int[ppm.getParametricProperty().getSpec().getBaseEvents().size()][][];
	for (BaseEvent baseEvent : ppm.getParametricProperty().getSpec().getBaseEvents()) {
	    result[baseEvent.getIndex()] = toParameterMasks(ppm.getDisableInstanceTypes().get(baseEvent));
	}
	return result;
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
	return disableMonitors[baseEvent.getIndex()];
    }

    private JoinArgs[][] getJoinArgsArray(ParametricPropertyModel ppm) {
	final JoinArgs[][] result = new JoinArgs[ppm.getParametricProperty().getSpec().getBaseEvents().size()][];
	for (BaseEvent baseEvent : ppm.getParametricProperty().getSpec().getBaseEvents()) {
	    result[baseEvent.getIndex()] = JoinArgs.createArgsArray(ppm, baseEvent);
	}
	return result;
    }

    private FindMaxArgs[][] getFindMaxArgsArray(ParametricPropertyModel ppm) {
	final FindMaxArgs[][] result = new FindMaxArgs[ppm.getParametricProperty().getSpec().getBaseEvents().size()][];
	for (BaseEvent baseEvent : ppm.getParametricProperty().getSpec().getBaseEvents()) {
	    result[baseEvent.getIndex()] = FindMaxArgs.createArgsArray(ppm, baseEvent);
	}
	return result;
    }

    private boolean[] getCreationEvents(ParametricPropertyModel ppm) {
	final boolean[] result = new boolean[ppm.getParametricProperty().getSpec().getBaseEvents().size()];
	for (BaseEvent baseEvent : ppm.getParametricProperty().getSpec().getBaseEvents()) {
	    result[baseEvent.getIndex()] = ppm.getParametricProperty().getCreationEvents().contains(baseEvent);
	}
	return result;
    }

    private boolean[] getDisableEvents(ParametricPropertyModel ppm) {
	final boolean[] result = new boolean[ppm.getParametricProperty().getSpec().getBaseEvents().size()];
	for (BaseEvent baseEvent : ppm.getParametricProperty().getSpec().getBaseEvents()) {
	    result[baseEvent.getIndex()] = ppm.getParametricProperty().getDisableEvents().contains(baseEvent);
	}
	return result;
    }

}
