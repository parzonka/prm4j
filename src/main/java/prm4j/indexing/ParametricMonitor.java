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
 */
public interface ParametricMonitor {

    void processEvent(Event event);

    void reset();

}
