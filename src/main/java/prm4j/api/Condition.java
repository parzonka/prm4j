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
package prm4j.api;

import prm4j.indexing.monitor.Monitor;

public abstract class Condition {

    private transient Monitor monitor;

    /**
     * Evaluate this condition based on the state of the base monitor and its attached parametric instance with
     * bindings.
     * 
     * @param monitor
     * @return
     */
    protected boolean eval(Monitor monitor) {
	this.monitor = monitor;
	boolean result = false;
	// protects from condition specification which try to access non-existing parameter values or similar
	try {
	    result = eval();
	} finally {
	    // do nothing
	}
	this.monitor = null;
	return result;
    }

    protected <T> T getParameterValue(Parameter<T> parameter) {
	return monitor.getParameterNode().getParameterValue(parameter, monitor.getCompressedBindings());
    }

    public abstract boolean eval();

}
