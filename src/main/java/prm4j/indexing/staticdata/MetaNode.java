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

import prm4j.indexing.realtime.MonitorSet;
import prm4j.indexing.realtime.Node;
import prm4j.indexing.realtime.NodeMap;

/**
 * Every {@link Node} is equipped with a MetaNode, containing factory methods and providing statically computed
 * algorithm logic.
 *
 */
public interface MetaNode {

    public ChainData[] getChainData();

    public MonitorSet createMonitorSet();

    public Node createNode();

    public Node createNode(int parameterId);

    public NodeMap createNodeMap();

}
