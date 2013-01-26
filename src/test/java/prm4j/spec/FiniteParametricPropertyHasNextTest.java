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

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import prm4j.AbstractTest;
import prm4j.api.Parameter;
import prm4j.api.fsm.FSMSpec;
import prm4j.indexing.realtime.NodeManager;
import prm4j.indexing.staticdata.StaticDataConverter;

public class FiniteParametricPropertyHasNextTest extends AbstractTest {

    FSM_HasNext fsm;
    FiniteParametricProperty fpp;

    @Before
    public void init() {
	fsm = new FSM_HasNext();
	fpp = new FiniteParametricProperty(new FSMSpec(fsm.fsm));
    }

    @Test
    public void getCreationEvents() throws Exception {
	StaticDataConverter sdc = new StaticDataConverter(fpp);
	sdc.getMetaTree().setNodeManagerToTree(new NodeManager());
    }

    @Test
    public void getAliveParameterSets() throws Exception {
	Set<Set<Parameter<?>>> actual = fpp.getAliveParameterSets();
	// verify
	Set<Set<Parameter<?>>> expected = new HashSet<Set<Parameter<?>>>();

	expected.add(asSet(fsm.i));

	assertEquals(expected, actual);
    }

}
