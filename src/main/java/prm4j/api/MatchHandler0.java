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
package prm4j.api;

import prm4j.indexing.realtime.Binding;

public abstract class MatchHandler0 extends MatchHandler {

    public MatchHandler0() {
	super();
    }

    @Override
    public void handleMatch(Binding[] bindings, Object auxiliaryData) {
	handleMatch(auxiliaryData);
    }

    public abstract void handleMatch(Object auxiliaryData);

}
