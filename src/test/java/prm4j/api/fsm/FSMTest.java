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
package prm4j.api.fsm;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import prm4j.AbstractTest;

public class FSMTest extends AbstractTest {

    @Test
    public void isAccepting() throws Exception {
	FSM_SafeMapIterator fsm = new FSM_SafeMapIterator();
	assertTrue(fsm.error.isAccepting());
    }

}
