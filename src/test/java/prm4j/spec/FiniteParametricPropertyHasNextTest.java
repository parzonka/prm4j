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
package prm4j.spec;

import static org.junit.Assert.*;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static prm4j.Util.set;
import static prm4j.Util.tuple;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import prm4j.AbstractTest;
import prm4j.Util;
import prm4j.Util.Tuple;
import prm4j.api.BaseEvent;
import prm4j.api.Parameter;
import prm4j.api.fsm.FSMSpec;
import prm4j.spec.FiniteParametricProperty;

public class FiniteParametricPropertyHasNextTest extends AbstractTest {

    FSM_HasNext fsm;
    FiniteParametricProperty fpp;
    @Before
    public void init() {
	fsm = new FSM_HasNext();
	fpp = new FiniteParametricProperty(new FSMSpec(fsm.fsm));
    }

    @Test
    public void getMonitorSetData() throws Exception {
	SetMultimap<Set<Parameter<?>>, Tuple<Set<Parameter<?>>, Boolean>> actual = fpp.getMonitorSetData();
	SetMultimap<Set<Parameter<?>>, Tuple<Set<Parameter<?>>, Boolean>> expected = HashMultimap.create();

	System.out.println(fpp.getMonitorSetData());
	System.out.println(fpp.getChainData());
	System.out.println(fpp.getJoinData());
	System.out.println(fpp.getCreationEvents());

    }


}
