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
package prm4j.indexing.realtime;

import java.util.ArrayList;
import java.util.List;

import prm4j.Util;
import prm4j.Util.Tuple;
import prm4j.api.MatchHandler1;
import prm4j.api.MatchHandler2;
import prm4j.api.Parameter;

public class AwareMatchHandler<P1> {

    public static <P1> AwareMatchHandler1<P1> create(Parameter<P1> param1) {
	return new AwareMatchHandler1<P1>(param1);
    }

    public static <P1, P2> AwareMatchHandler2<P1, P2> create(Parameter<P1> param1, Parameter<P2> param2) {
	return new AwareMatchHandler2<P1, P2>(param1, param2);
    }

    public static class AwareMatchHandler1<P1> extends MatchHandler1<P1> {

	private final List<P1> handledMatches;

	public AwareMatchHandler1(Parameter<P1> param1) {
	    super(param1);
	    handledMatches = new ArrayList<P1>();
	}

	@Override
	public void handleMatch(P1 obj1) {
	    handledMatches.add(obj1);
	}

	/**
	 * Returns a list of object which participated in a detected match.
	 *
	 * @return
	 */
	public List<P1> getHandledMatches() {
	    return handledMatches;
	}

    }

    public static class AwareMatchHandler2<P1, P2> extends MatchHandler2<P1, P2> {

	private final List<Tuple<P1, P2>> handledMatches;

	public AwareMatchHandler2(Parameter<P1> param1, Parameter<P2> param2) {
	    super(param1, param2);
	    handledMatches = new ArrayList<Tuple<P1, P2>>();
	}

	@Override
	public void handleMatch(P1 obj1, P2 obj2) {
	    handledMatches.add(Util.tuple(obj1, obj2));
	}

	/**
	 * Returns a list of object tuples which participated in a detected match.
	 *
	 * @return
	 */
	public List<Tuple<P1, P2>> getHandledMatches() {
	    return handledMatches;
	}

    }
}