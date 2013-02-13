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
package prm4j.indexing.realtime;

import java.util.ArrayList;
import java.util.List;

import prm4j.Util;
import prm4j.api.MatchHandler0;
import prm4j.api.MatchHandler1;
import prm4j.api.MatchHandler2;
import prm4j.api.Parameter;

public class AwareMatchHandler<P1> {

    public static <P1> AwareMatchHandler0 create() {
	return new AwareMatchHandler0();
    }

    public static <P1> AwareMatchHandler1<P1> create(Parameter<P1> param1) {
	return new AwareMatchHandler1<P1>(param1);
    }

    public static <P1, P2> AwareMatchHandler2<P1, P2> create(Parameter<P1> param1, Parameter<P2> param2) {
	return new AwareMatchHandler2<P1, P2>(param1, param2);
    }

    public static class AwareMatchHandler0 extends MatchHandler0 {

	private final List<Object> handledMatches;

	public AwareMatchHandler0() {
	    super();
	    handledMatches = new ArrayList<Object>();
	}

	@Override
	public void handleMatch(Object auxiliaryData) {
	    handledMatches.add(auxiliaryData);
	}

	/**
	 * Returns the list of auxiliary data (can be null) if a match was handled. The length of the list represents
	 * the number of matches.
	 *
	 * @return a list of auxiliary data
	 */
	public List<Object> getHandledMatches() {
	    return handledMatches;
	}

    }

    public static class AwareMatchHandler1<P1> extends MatchHandler1<P1> {

	private final List<Object> handledMatches;

	public AwareMatchHandler1(Parameter<P1> param1) {
	    super(param1);
	    handledMatches = new ArrayList<Object>();
	}

	@Override
	public void handleMatch(P1 obj1, Object auxiliaryData) {
	    handledMatches.add(auxiliaryData);
	}

	/**
	 * Returns the list of auxiliary data (can be null) if a match was handled. The length of the list represents
	 * the number of matches.
	 *
	 * @return a list of auxiliary data
	 */
	public List<Object> getHandledMatches() {
	    return handledMatches;
	}

    }

    public static class AwareMatchHandler2<P1, P2> extends MatchHandler2<P1, P2> {

	private final List<Object> handledMatches;

	public AwareMatchHandler2(Parameter<P1> param1, Parameter<P2> param2) {
	    super(param1, param2);
	    handledMatches = new ArrayList<Object>();
	}

	@Override
	public void handleMatch(P1 obj1, P2 obj2, Object auxiliaryData) {
	    handledMatches.add(Util.tuple(obj1, obj2));
	}

	/**
	 * Returns the list of auxiliary data (can be null) if a match was handled. The length of the list represents
	 * the number of matches.
	 *
	 * @return a list of auxiliary data
	 */
	public List<Object> getHandledMatches() {
	    return handledMatches;
	}

    }
}