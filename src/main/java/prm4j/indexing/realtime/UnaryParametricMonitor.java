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
package prm4j.indexing.realtime;

import prm4j.api.Event;
import prm4j.api.ParametricMonitor;
import prm4j.indexing.BaseMonitor;
import prm4j.indexing.map.MinimalMap;
import prm4j.indexing.map.MinimalMapEntry;
import prm4j.spec.Spec;

/**
 * Optimized {@link ParametricMonitor} for parametric properties with exactly one parameter.
 */
public class UnaryParametricMonitor implements ParametricMonitor {

    /**
     * Reusable parameter-to-object bindings serving as keys in the monitor map.
     */
    protected final BindingStore bindingStore;

    /**
     * Will be cloned when a new monitor gets created.
     */
    protected final BaseMonitor monitorPrototype;

    /**
     * Associates bindings with monitors.
     */
    protected final MonitorMap monitorMap;

    /**
     * Has no real purpose here, but may be used for diagnostic purposes in sub-classes.
     */
    protected long timestamp = 0L;

    /**
     * Creates a {@link UnaryParametricMonitor} for a given specification using default {@link BindingStore}
     * implementation and configuration.
     *
     * @param spec
     */
    public UnaryParametricMonitor(Spec spec) {
	bindingStore = new DefaultBindingStore(spec.getFullParameterSet());
	monitorPrototype = spec.getInitialMonitor();
	monitorMap = new MonitorMap();
    }

    /**
     * Creates a {@link UnaryParametricMonitor} with externally configurable {@link BindingStore}.
     *
     * @param bindingStore
     * @param monitorPrototype
     */
    public UnaryParametricMonitor(BindingStore bindingStore, BaseMonitor monitorPrototype) {
	this.bindingStore = bindingStore;
	this.monitorPrototype = monitorPrototype;
	monitorMap = new MonitorMap();
    }

    @Override
    public synchronized void processEvent(Event event) {

	/*
	 * The parametric property can be specified using a single parameter, so each binding is associated only with a
	 * single monitor and the monitoring structure consist of a single associative array.
	 */

	final LowLevelBinding[] bindings = bindingStore.getBindings(event.getBoundObjects());
	final LowLevelBinding binding = bindings[0];

	MonitorMapEntry entry = getMonitorMap().get(binding);

	if (entry == null) {
	    entry = getMonitorMap().getOrCreate(binding);
	    // a simple clone is enough, since the compressed representation equals the uncompressed representation
	    entry.monitor = monitorPrototype.copy(bindings.clone());
	}
	if (entry.getMonitor() != null && !entry.getMonitor().processEvent(event)) {
	    // nullify dead monitors
	    entry.monitor = null;
	}
	// has no real purpose here, but may be used for diagnostic purposes in sub-classes
	timestamp++;
    }

    @Override
    public void reset() {
	timestamp = 0L;
	bindingStore.reset();
	getMonitorMap().reset();
    }

    /**
     * DIAGNOSTIC: Returns the mapping of bindings to monitors.
     *
     * @return the monitor map
     */
    protected MonitorMap getMonitorMap() {
	return monitorMap;
    }

    class MonitorMap extends MinimalMap<LowLevelBinding, MonitorMapEntry> {

	@Override
	protected MonitorMapEntry[] createTable(int size) {
	    return new MonitorMapEntry[size];
	}

	@Override
	protected MonitorMapEntry createEntry(LowLevelBinding key, int hashCode) {
	    return new MonitorMapEntry(key);
	}

    }

    class MonitorMapEntry implements MinimalMapEntry<LowLevelBinding, MonitorMapEntry> {

	private final LowLevelBinding binding;
	private MonitorMapEntry next;

	private BaseMonitor monitor;

	public MonitorMapEntry(LowLevelBinding binding) {
	    this.binding = binding;
	}

	@Override
	public LowLevelBinding getKey() {
	    return binding;
	}

	@Override
	public MonitorMapEntry next() {
	    return next;
	}

	@Override
	public void setNext(MonitorMapEntry nextEntry) {
	    next = nextEntry;
	}

	public BaseMonitor getMonitor() {
	    return monitor;
	}

    }

}
