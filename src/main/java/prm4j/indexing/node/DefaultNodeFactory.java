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
package prm4j.indexing.node;

import prm4j.indexing.binding.Binding;
import prm4j.indexing.model.ParameterNode;

public class DefaultNodeFactory extends NodeFactory {

    private final NodeManager nodeManager;

    public DefaultNodeFactory(NodeManager nodeManager) {
	this.nodeManager = nodeManager;
    }

    @Override
    public Node createNode(ParameterNode parameterNode, int parameterIndex, Binding binding) {
	Node node = new DefaultNode(parameterNode, parameterIndex, binding, nodeManager.getReferenceQueue());
	nodeManager.createdNode(node);
	return node;
    }

}
