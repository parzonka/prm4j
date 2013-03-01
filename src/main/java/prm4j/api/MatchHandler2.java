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

public abstract class MatchHandler2<P1, P2> extends MatchHandler {

    private final Parameter<P1> param1;
    private final Parameter<P2> param2;

    public MatchHandler2(Parameter<P1> param1, Parameter<P2> param2) {
	super();
	this.param1 = param1;
	this.param1.setStrong(true);
	this.param2 = param2;
	this.param2.setStrong(true);
    }

    @Override
    public void handleMatch(Binding[] bindings, Object auxiliaryData) {
	handleMatch(getBoundObject(this.param1, bindings), getBoundObject(this.param2, bindings), auxiliaryData);
    }

    public abstract void handleMatch(P1 obj1, P2 obj2, Object auxiliaryData);

}
