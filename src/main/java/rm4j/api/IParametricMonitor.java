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
package rm4j.api;

import rm4j.old.v2.indexing.Event;

/**
 * A parametric monitor can be thought as a set of base monitors running in parallel, one for each parameter instance.
 *
 * @param <A>
 *            the type of the auxiliary data usable by base monitors
 * @param <M>
 *            the type of the base monitor
 */
public interface IParametricMonitor<A, M extends IBaseMonitor<A, M>> {

    M createBaseMonitor();

    void processEvent(Event<A> event);

    void reset();

}
