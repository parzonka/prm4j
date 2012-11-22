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
package prm4j.indexing;

import prm4j.api.BaseEvent;
import prm4j.api.MatchHandler;
import prm4j.indexing.realtime.LowLevelBinding;

public abstract class BaseMonitorState {

    private boolean[] acceptingParameters;

    /**
     * Tests, if a accepting state can be reached by some event in the future. Checks interally, if all bindings are
     * still alive, needed to reach a accepting state.
     *
     * @param bindings bindings to be checked
     * @return true, if accepting state can be reached
     */
    public boolean isAcceptingStateReachable(LowLevelBinding[] bindings) {
	for (LowLevelBinding b : bindings) {
	    if (acceptingParameters[b.getParameterIndex()]) {
		if (b.get() == null)
		    return false;
	    }
	}
	return true;
    }

    public abstract BaseMonitorState getSuccessor(BaseEvent baseEvent);

    public abstract boolean isAccepting();

    public abstract MatchHandler getMatchHandler();

    public void setAcceptingParameters(boolean[] acceptingParameters) {
	this.acceptingParameters = acceptingParameters;
    }

    public abstract boolean isFinal();

}