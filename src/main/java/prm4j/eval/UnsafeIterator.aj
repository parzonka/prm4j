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
package prm4j.eval;

import java.util.Collection;
import java.util.Iterator;

import org.aspectj.lang.annotation.SuppressAjWarnings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import prm4j.api.ParametricMonitor;
import prm4j.api.ParametricMonitorFactory;
import prm4j.api.fsm.FSMSpec;

@SuppressWarnings("rawtypes")
@SuppressAjWarnings({"adviceDidNotMatch"})
public aspect UnsafeIterator {

    protected final Logger logger = LoggerFactory.getLogger(UnsafeIterator.class);

    final FSM_unsafeIterator fsm;
    final ParametricMonitor pm;

    public UnsafeIterator() {
	System.out.println("Starting aspect!");
	fsm = new FSM_unsafeIterator();
	pm = ParametricMonitorFactory.createParametricMonitor(new FSMSpec(fsm.fsm));
	System.out.println("Parametric monitor for UnsafeIterator created!");
    }

    pointcut UnsafeIterator_create(Collection c) : (call(Iterator Collection+.iterator()) && target(c)) && !within(prm4j..*) && !within(org.dacapo..*);

    after(Collection c) returning (Iterator i) : UnsafeIterator_create(c) {
	pm.processEvent(fsm.create.createEvent(c, i));
    }

    pointcut UnsafeIterator_updatesource(Collection c) : ((call(* Collection+.remove*(..)) || call(* Collection+.add*(..))) && target(c)) && !within(prm4j..*) && !within(org.dacapo..*);

    after(Collection c) : UnsafeIterator_updatesource(c) {
	pm.processEvent(fsm.updateSource.createEvent(c));
    }

    pointcut UnsafeIterator_next(Iterator i) : (call(* Iterator.next()) && target(i)) && !within(prm4j..*) && !within(org.dacapo..*);

    before(Iterator i) : UnsafeIterator_next(i) {
	pm.processEvent(fsm.next.createEvent(i));
    }

}
