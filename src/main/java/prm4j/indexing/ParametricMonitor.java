/*
 * Copyright (c) 2012 Mateusz Parzonka, Eric Bodden
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Eric Bodden - initial API and implementation
 * Mateusz Parzonka - adapted API and implementation
 */
package prm4j.indexing;

/**
 * A parametric monitor can be thought as a set of base monitors running in parallel, one for each parameter instance.
 *
 * @param <E>
 *            the type of base event processed by monitors
 * @param <M>
 *            the type of the base monitor
 */
public interface ParametricMonitor<E, M extends BaseMonitor<E, M>> {

    M createBaseMonitor();

    void processEvent(Event<E> event);

    void reset();

}
