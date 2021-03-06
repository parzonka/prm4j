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

import prm4j.indexing.binding.Binding;

public abstract class MatchHandler3<P1, P2, P3> extends MatchHandler {

    private final Parameter<P1> param1;
    private final Parameter<P2> param2;
    private final Parameter<P3> param3;

    public MatchHandler3(Parameter<P1> param1, Parameter<P2> param2, Parameter<P3> param3) {
	super();
	this.param1 = param1;
	this.param1.setPersistent(true);
	this.param2 = param2;
	this.param2.setPersistent(true);
	this.param3 = param3;
	this.param3.setPersistent(true);
    }

    @Override
    public void handleMatch(Binding[] bindings, Object auxiliaryData) {
	handleMatch(getBoundObject(this.param1, bindings), getBoundObject(this.param2, bindings),
		getBoundObject(this.param3, bindings), auxiliaryData);
    }

    public abstract void handleMatch(P1 obj1, P2 obj2, P3 obj3, Object auxiliaryData);

}
