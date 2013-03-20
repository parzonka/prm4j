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
package prm4j.spec.finite;

import prm4j.api.BaseEvent;
import prm4j.indexing.monitor.MonitorState;

public class PartEnableInitialState extends MonitorStateDecorator {

    public PartEnableInitialState(MonitorState monitorState) {
	super(monitorState);
    }

    @Override
    public MonitorState getSuccessor(BaseEvent baseEvent) {
	if (monitorState.getSuccessor(baseEvent) != null && monitorState.getSuccessor(baseEvent).equals(monitorState)) {
	    return null;
	}
	return monitorState.getSuccessor(baseEvent);
    }

}
