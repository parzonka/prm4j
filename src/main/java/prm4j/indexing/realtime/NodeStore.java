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

public interface NodeStore {

    public Node getOrCreateNode(LowLevelBinding[] bindings);

    public Node getOrCreateNode(LowLevelBinding[] bindings, int[] parameterMask);

    public Node getNode(LowLevelBinding[] bindings);

    public Node getNode(LowLevelBinding[] bindings, int[] parameterMask);

    public void reset();

}
