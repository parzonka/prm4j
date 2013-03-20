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
package prm4j.indexing.model;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import prm4j.Util.Tuple;
import prm4j.api.BaseEvent;
import prm4j.api.Parameter;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.SetMultimap;

public class ModelVerifier {

    public ParametricPropertyModel processor;

    public ModelVerifier(ParametricPropertyModel processor) {
	this.processor = processor;
    }

    public SetMultimapVerifier<Set<Parameter<?>>, Tuple<Set<Parameter<?>>, Boolean>> getMonitorStateSpec() {
	return new SetMultimapVerifier<Set<Parameter<?>>, Tuple<Set<Parameter<?>>, Boolean>>(
		processor.getMonitorSetSpecs());
    }

    public SetMultimapVerifier<Set<Parameter<?>>, Tuple<Set<Parameter<?>>, Set<Parameter<?>>>> getUpdateChainingTuples() {
	return new SetMultimapVerifier<Set<Parameter<?>>, Tuple<Set<Parameter<?>>, Set<Parameter<?>>>>(
		processor.getUpdateChainingTuples());
    }

    public SetMultimapVerifier<BaseEvent, Set<Parameter<?>>> expectDisableInstanceTypes() {
	return new SetMultimapVerifier<BaseEvent, Set<Parameter<?>>>(processor.getDisableInstanceTypes());
    }

    public ListMultimapVerifier<BaseEvent, Tuple<Set<Parameter<?>>, Set<Parameter<?>>>> getJoinTuples() {
	return new ListMultimapVerifier<BaseEvent, Tuple<Set<Parameter<?>>, Set<Parameter<?>>>>(
		processor.getJoinTuples());
    }

    public SetMultimapVerifier<Set<Parameter<?>>, Set<Parameter<?>>> getAliveParameterSets() {
	return new SetMultimapVerifier<Set<Parameter<?>>, Set<Parameter<?>>>(processor.getParametricProperty()
		.getAliveParameterSets());
    }

    public ListMultimapVerifier<BaseEvent, Set<Parameter<?>>> getFindMaxInstanceTypes() {
	return new ListMultimapVerifier<BaseEvent, Set<Parameter<?>>>(processor.getFindMaxInstanceTypes());
    }

    public class SetMultimapVerifier<K, T> {
	private final SetMultimap<K, T> expected;
	private final SetMultimap<K, T> actual;

	SetMultimapVerifier(SetMultimap<K, T> actual) {
	    expected = HashMultimap.create();
	    this.actual = actual;
	}

	public SetMultimapVerifier<K, T> put(K key, T... entries) {
	    expected.putAll(key, Arrays.asList(entries));
	    return this;
	}

	public SetMultimapVerifier<K, T> put(K key, List<T> entries) {
	    expected.putAll(key, entries);
	    return this;
	}

	public void verify() {
	    assertEquals(expected, actual);
	}
    }

    public class ListMultimapVerifier<K, T> {
	private final ListMultimap<K, T> expected;
	private final ListMultimap<K, T> actual;

	ListMultimapVerifier(ListMultimap<K, T> actual) {
	    expected = ArrayListMultimap.create();
	    this.actual = actual;
	}

	public ListMultimapVerifier<K, T> put(K key, T... entries) {
	    expected.putAll(key, Arrays.asList(entries));
	    return this;
	}

	public ListMultimapVerifier<K, T> put(K key, List<T> entries) {
	    expected.putAll(key, entries);
	    return this;
	}

	public void verify() {
	    assertEquals(expected, actual);
	}
    }

}
