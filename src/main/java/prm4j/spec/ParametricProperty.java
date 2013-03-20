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
package prm4j.spec;

import java.util.Set;

import prm4j.Util.Tuple;
import prm4j.api.BaseEvent;
import prm4j.api.Parameter;

import com.google.common.collect.SetMultimap;

public interface ParametricProperty {

    public Spec getSpec();

    public Set<BaseEvent> getCreationEvents();

    public Set<BaseEvent> getDisableEvents();

    public SetMultimap<BaseEvent, Set<Parameter<?>>> getEnableParameterSets();

    public SetMultimap<Set<Parameter<?>>, Set<Parameter<?>>> getAliveParameterSets();

    public Set<Tuple<Set<Parameter<?>>, Set<Parameter<?>>>> getUpdates();

}
