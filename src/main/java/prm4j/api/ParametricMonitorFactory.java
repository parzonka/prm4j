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

import prm4j.Globals;
import prm4j.indexing.DefaultParametricMonitor;
import prm4j.indexing.binding.ArrayBasedBindingFactory;
import prm4j.indexing.binding.BindingFactory;
import prm4j.indexing.binding.BindingStore;
import prm4j.indexing.binding.DefaultBindingStore;
import prm4j.indexing.binding.LinkedListBindingFactory;
import prm4j.indexing.logic.ParametricPropertyProcessor;
import prm4j.indexing.monitor.BaseMonitor;
import prm4j.indexing.monitor.Monitor;
import prm4j.indexing.node.DefaultNodeStore;
import prm4j.indexing.node.NodeManager;
import prm4j.indexing.node.NodeStore;
import prm4j.spec.FiniteParametricProperty;
import prm4j.spec.FiniteSpec;
import prm4j.spec.ParametricProperty;

public class ParametricMonitorFactory {

    public static ParametricMonitor createParametricMonitor(FiniteSpec finiteSpec) {

	BaseMonitor.reset(); // Diagnostic

	final ParametricProperty parametricProperty = new FiniteParametricProperty(finiteSpec);
	final ParametricPropertyProcessor processor = new ParametricPropertyProcessor(parametricProperty);

	// build object graph
	final BindingFactory bindingFactory = Globals.LINKEDLIST_STORED_BACKLINKS ? new LinkedListBindingFactory()
		: new ArrayBasedBindingFactory();
	final BindingStore bindingStore = new DefaultBindingStore(bindingFactory, finiteSpec.getFullParameterSet());
	final NodeManager nodeManager = new NodeManager();
	final NodeStore nodeStore = new DefaultNodeStore(processor.getParameterTree(), nodeManager);
	final Monitor prototypeMonitor = finiteSpec.getInitialMonitor();

	final ParametricMonitor parametricMonitor = new DefaultParametricMonitor(bindingStore, nodeStore,
		prototypeMonitor, processor.getEventContext(), nodeManager, false);

	return parametricMonitor;
    }
}
