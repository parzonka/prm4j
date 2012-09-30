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
package prm4j.indexing;

import prm4j.api.BaseMonitor;
import prm4j.api.ParametricMonitor;

/**
 * A concrete monitor instance, representing the internal state of a {@link ParametricMonitor} for one single concrete
 * variable binding.
 *
 * @param <A>
 *            the type of the auxiliary data usable by base monitors
 */
public abstract class AbstractBaseMonitor<A> implements BaseMonitor<A, AbstractBaseMonitor<A>> {

    // low level access
    private LowLevelBinding<A>[] bindings;
    // low level access
    private long tau;

    /**
     * Creates a low level deep copy of this monitor.
     *
     * @param bindings
     * @return
     */
    final AbstractBaseMonitor<A> copy(LowLevelBinding<A>[] bindings) {
	AbstractBaseMonitor<A> copy = copy();
	copy.setBindings(bindings);
	copy.setTau(tau);
	return copy;
    }

    private final void setBindings(LowLevelBinding<A>[] bindings) {
	this.bindings = bindings;
    }

    final LowLevelBinding<A>[] getLowLevelBindings() {
	return bindings;
    }

    protected final prm4j.api.Binding[] getBindings() {
	// upcast
	return bindings;
    }

    final long getTau() {
	return tau;
    }

    final void setTau(long tau) {
	this.tau = tau;
    }

}
