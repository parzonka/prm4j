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
package prm4j;

import java.util.Arrays;

import javagraphviz.GraphvizEngine;
import javagraphviz.GvGraph;
import javagraphviz.GvNode;
import prm4j.indexing.staticdata.ChainData;
import prm4j.indexing.staticdata.MetaNode;

public class Visualizer {

    /**
     * Generates a diagram from meta tree
     *
     * @param metaTree
     * @param subPath
     *            the name of the test-class and testing method
     * @param name
     *            the name of the graph (expected or actual)
     */
    public static void visualizeMetaTree(MetaNode metaTree, String subPath, String name) {

	// define a graph with the Digraph Type.
	GvGraph graph = GvGraph.createDigraph(name);
	graph.setAttribute("rankdir", "TD");

	visualizeMetaNode(metaTree, graph);

	GraphvizEngine ge = new GraphvizEngine();
	ge.setExecutionPath("target/graphviz/" + subPath);
	ge.process(graph);

    }

    private static void visualizeMetaNode(MetaNode metaNode, GvGraph graph) {
	if (metaNode.getChainDataArray() != null) {
	    for (ChainData chainData : metaNode.getChainDataArray()) {
		GvNode x = graph.getNode(Arrays.toString(chainData.getNodeMask()));
		GvNode y = graph.getNode(Arrays.toString(metaNode.getNodeMask()));
		graph.addEdge(x, y).setStyle("dashed");
	    }
	}
	for (MetaNode successor : metaNode.getSuccessors()) {
	    GvNode dotNode = graph.getNode(Arrays.toString(metaNode.getNodeMask()));
	    if (successor != null) {
		GvNode succNode = graph.getNode(Arrays.toString(successor.getNodeMask()));
		graph.addEdge(dotNode, succNode);
		visualizeMetaNode(successor, graph);
	    }
	}
    }

}
