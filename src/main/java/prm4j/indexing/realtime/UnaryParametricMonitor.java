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
import prm4j.indexing.staticdata.EventContext;
import prm4j.indexing.staticdata.MetaNode;
import prm4j.spec.Spec;

public class UnaryParametricMonitor implements ParametricMonitor {

    protected final BaseMonitor monitorPrototype;
    protected final BindingStore bindingStore;
    protected final MonitorMap monitorMap;
    protected long timestamp = 0L;

    /**
     * Creates a DefaultParametricMonitor using default {@link BindingStore} and {@link NodeStore} implementations (and
     * configurations).
     *
     * @param metaTree
     * @param eventContext
     * @param spec
     */
    public UnaryParametricMonitor(MetaNode metaTree, EventContext eventContext, Spec spec) {
	bindingStore = new DefaultBindingStore(spec.getFullParameterSet());
	monitorPrototype = spec.getInitialMonitor();
	monitorMap = new MonitorMap();
    }

    /**
     * Creates a DefaultParametricMonitor which externally configurable BindingStore and NodeStore.
     *
     * @param bindingStore
     * @param nodeStore
     * @param monitorPrototype
     * @param eventContext
     */
    public UnaryParametricMonitor(BindingStore bindingStore, NodeStore nodeStore, BaseMonitor monitorPrototype,
	    EventContext eventContext, NodeManager nodeManager) {
	this.bindingStore = bindingStore;
	this.monitorPrototype = monitorPrototype;
	monitorMap = new MonitorMap();
    }

    @Override
    public synchronized void processEvent(Event event) {

	final LowLevelBinding[] bindings = bindingStore.getBindings(event.getBoundObjects());
	final LowLevelBinding binding = bindings[0];

	MonitorMapEntry entry = monitorMap.get(binding);

	if (entry == null) { // 7
	    entry = monitorMap.getOrCreate(binding);
	    entry.monitor = monitorPrototype.copy(bindings);
	}
	if (!entry.monitor.processEvent(event)) {
	    // nullify dead monitors
	    entry.monitor = null;
	}
	timestamp++;
    }

    @Override
    public void reset() {
	timestamp = 0L;
	bindingStore.reset();
	monitorMap.reset();
    }

    private class MonitorMap extends MinimalMap<LowLevelBinding, MonitorMapEntry> {

	@Override
	protected MonitorMapEntry[] createTable(int size) {
	    return new MonitorMapEntry[size];
	}

	@Override
	protected MonitorMapEntry createEntry(LowLevelBinding key, int hashCode) {
	    return new MonitorMapEntry(key);
	}

    }

    private class MonitorMapEntry implements MinimalMapEntry<LowLevelBinding, MonitorMapEntry> {

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

    }

}
