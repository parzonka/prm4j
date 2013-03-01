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

public abstract class MatchHandler1<P1> extends MatchHandler {

    private final Parameter<P1> param1;

    public MatchHandler1(Parameter<P1> param1) {
	super();
	this.param1 = param1;
	this.param1.setPersistent(true);
    }

    @Override
    public void handleMatch(Binding[] bindings, Object auxiliaryData) {
	handleMatch(getBoundObject(this.param1, bindings), auxiliaryData);
    }

    public abstract void handleMatch(P1 obj1, Object auxiliaryData);

}
