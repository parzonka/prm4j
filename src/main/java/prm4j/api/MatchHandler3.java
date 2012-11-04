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
package prm4j.api;


public abstract class MatchHandler3<P1, P2, P3> extends MatchHandler {

    private final Parameter<P1> param1;
    private final Parameter<P2> param2;
    private final Parameter<P3> param3;

    public MatchHandler3(Parameter<P1> param1, Parameter<P2> param2, Parameter<P3> param3) {
	super();
	this.param1 = param1;
	this.param1.setStrong(true);
	this.param2 = param2;
	this.param2.setStrong(true);
	this.param3 = param3;
	this.param3.setStrong(true);
    }

    @Override
    public void handleMatch(Binding[] bindings) {
	handleMatch(getBoundObject(this.param1, bindings), getBoundObject(this.param2, bindings),
		getBoundObject(this.param3, bindings));
    }

    public abstract void handleMatch(P1 obj1, P2 obj2, P3 obj3);

}
