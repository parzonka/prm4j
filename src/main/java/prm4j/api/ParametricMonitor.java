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

/**
 * A parametric monitor can be thought as a set of base monitors running in parallel, one for each parameter instance.
 */
public interface ParametricMonitor {

    void processEvent(Event event);

    void reset();

}
