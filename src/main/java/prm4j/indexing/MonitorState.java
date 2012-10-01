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

import prm4j.api.MatchHandler;
import prm4j.api.Symbol;

/**
 * @param <A>
 *            the type of the auxiliary data usable by base monitors
 */
public interface MonitorState<A> {

    public abstract MonitorState<A> getSuccessor(Symbol symbol);

    public abstract boolean isFinal();

    public abstract MatchHandler getMatchHandler();

}