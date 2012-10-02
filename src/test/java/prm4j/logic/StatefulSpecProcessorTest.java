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
package prm4j.logic;

import org.junit.Test;
import static junit.framework.Assert.*;

import prm4j.AbstractTest;
import prm4j.api.fsm.FSM;
import prm4j.api.fsm.FSMSpec;

public class StatefulSpecProcessorTest extends AbstractTest {

    @Test
    public void accessors_unsafeMapIterator() throws Exception {
	FSM<Void> fsm = new FSM_unsafeMapIterator().fsm;
	StatefulSpecProcessor ssp = new StatefulSpecProcessor(new FSMSpec<Void>(fsm));

	assertEquals(ssp.getInitialState(), fsm.getInitialState());
	assertEquals(ssp.getSymbols(), fsm.getAlphabet().getSymbols());
    }

}
