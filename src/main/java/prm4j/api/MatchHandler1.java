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

public abstract class MatchHandler1<P1> extends MatchHandler {

    private final Parameter<P1> param1;

    public MatchHandler1(Parameter<P1> param1) {
	super();
	this.param1 = param1;
	this.param1.setStrong(true);
    }

    @Override
    public void handleMatch(IBinding[] bindings) {
	handleMatch(getBoundObject(this.param1, bindings));
    }

    public abstract void handleMatch(P1 obj1);

}
