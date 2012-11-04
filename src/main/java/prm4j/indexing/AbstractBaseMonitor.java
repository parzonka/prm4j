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

import prm4j.api.ParametricMonitor;
import prm4j.indexing.treebased.LowLevelBinding;

/**
 * A concrete monitor instance, representing the internal state of a {@link ParametricMonitor} for one single concrete
 * variable binding.
 */
public abstract class AbstractBaseMonitor implements BaseMonitor<AbstractBaseMonitor> {

    // low level access
    private LowLevelBinding[] bindings;
    // low level access
    private long creationTime;

    /**
     * Creates a low level deep copy of this monitor.
     *
     * @param bindings
     * @return
     */
    public final AbstractBaseMonitor copy(LowLevelBinding[] bindings) {
	AbstractBaseMonitor copy = copy();
	copy.setBindings(bindings);
	copy.setCreationTime(creationTime);
	return copy;
    }

    public final AbstractBaseMonitor copy(LowLevelBinding[] bindings, long timestamp) {
	AbstractBaseMonitor copy = copy();
	copy.setBindings(bindings);
	copy.setCreationTime(timestamp);
	return copy;
    }

    private final void setBindings(LowLevelBinding[] bindings) {
	this.bindings = bindings;
    }

    public final LowLevelBinding[] getLowLevelBindings() {
	return bindings;
    }

    protected final prm4j.api.Binding[] getBindings() {
	// upcast
	return bindings;
    }

    public final long getCreationTime() {
	return creationTime;
    }

    final void setCreationTime(long creationTime) {
	this.creationTime = creationTime;
    }

}
