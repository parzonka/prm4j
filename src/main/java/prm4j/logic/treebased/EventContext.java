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

import com.google.common.collect.ListMultimap;

import prm4j.indexing.BaseEvent;

public class EventContext {

    private final JoinData[][] joinDataArray;
    private final boolean[] disablingEvents;

    public EventContext(ListMultimap<BaseEvent,JoinData> joinData, Object object) {
	joinDataArray = new JoinData[joinData.keys().size()][];
	for (BaseEvent baseEvent : joinData.keys()) {
	    joinDataArray[baseEvent.getIndex()] = joinData.get(baseEvent).toArray(new JoinData[0]);
	}
	disablingEvents = null; // TODO
    }

    public JoinData[] getJoinData(BaseEvent baseEvent) {
	return joinDataArray[baseEvent.getIndex()];
    }

    public boolean isDisablingEvent(BaseEvent baseEvent) {
	return disablingEvents[baseEvent.getIndex()];
    }

}
