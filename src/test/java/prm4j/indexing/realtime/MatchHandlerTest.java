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

import java.util.Collection;
import java.util.Iterator;

import prm4j.api.MatchHandler2;
import prm4j.api.Parameter;

public class MatchHandlerTest {

    @SuppressWarnings("rawtypes")
    class CustomMatchHandler extends MatchHandler2<Collection, Iterator> {

	public CustomMatchHandler(Parameter<Collection> param1, Parameter<Iterator> param2) {
	    super(param1, param2);
	}

	@Override
	public void handleMatch(Collection obj1, Iterator obj2, Object auxiliaryData) {
	    System.out.println("Iterator " + obj2 + " was used, after collection " + obj1 + "was modified!");
	}

    }

}
