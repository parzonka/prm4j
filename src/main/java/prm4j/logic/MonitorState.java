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
package prm4j.logic;

import prm4j.api.BaseEvent;
import prm4j.api.MatchHandler;

public interface MonitorState {

    public abstract MonitorState getSuccessor(BaseEvent baseEvent);

    public abstract boolean isFinal();

    public abstract MatchHandler getMatchHandler();

}