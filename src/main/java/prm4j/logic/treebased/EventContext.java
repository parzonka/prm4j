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
package prm4j.logic.treebased;

import java.util.Set;

import com.google.common.collect.ListMultimap;

import prm4j.api.BaseEvent;

public class EventContext {

    private final JoinData[][] joinDataArray;
    private final MaxData[][] maxDataArray;
    private final boolean[] creationEvents;
    private final boolean[] disablingEvents;

    public EventContext(Set<BaseEvent> baseEvents, ListMultimap<BaseEvent, JoinData> joinData, ListMultimap<BaseEvent, MaxData> maxData,
	    Set<BaseEvent> creationEvents, Set<BaseEvent> disablingEvents) {
	joinDataArray = new JoinData[baseEvents.size()][];
	maxDataArray = new MaxData[baseEvents.size()][];
	this.creationEvents = new boolean[baseEvents.size()];
	this.disablingEvents = new boolean[baseEvents.size()];
	for (BaseEvent baseEvent : baseEvents) {
	    maxDataArray[baseEvent.getIndex()] = maxData.get(baseEvent) != null ? maxData.get(baseEvent).toArray(new MaxData[0]) : null;
	    joinDataArray[baseEvent.getIndex()] = joinData.get(baseEvent) != null ? joinData.get(baseEvent).toArray(new JoinData[0]) : null;
	    this.creationEvents[baseEvent.getIndex()] = creationEvents.contains(baseEvent);
	    this.disablingEvents[baseEvent.getIndex()] = disablingEvents.contains(baseEvent);
	}
    }

    public MaxData[] getMaxData(BaseEvent baseEvent) {
	return maxDataArray[baseEvent.getIndex()];
    }

    public JoinData[] getJoinData(BaseEvent baseEvent) {
   	return joinDataArray[baseEvent.getIndex()];
       }

    public boolean isCreationEvent(BaseEvent baseEvent) {
	return creationEvents[baseEvent.getIndex()];
    }

    public boolean isDisablingEvent(BaseEvent baseEvent) {
	return disablingEvents[baseEvent.getIndex()];
    }

}
