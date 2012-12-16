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
package prm4j.indexing.staticdata;

import prm4j.indexing.realtime.LowLevelBinding;
import prm4j.indexing.realtime.Node;

public abstract class NodeFactory {

    public abstract Node createNode(MetaNode metaNode, int parameterIndex, LowLevelBinding binding);

    @Override
    public String toString() {
	return this.getClass().getCanonicalName();
    }

}
