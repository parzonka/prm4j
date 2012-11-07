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

import java.util.HashSet;
import java.util.Set;

import prm4j.api.Parameter;
import prm4j.indexing.realtime.DefaultNode;
import prm4j.indexing.realtime.Node;
import prm4j.indexing.realtime.NodeMap;

/**
 * Every {@link Node} is equipped with a MetaNode, containing factory methods and providing statically computed
 * algorithm logic.
 *
 */
public class MetaNode {

    private MetaNode[] successors;
    private ChainData[] chainData;
    private Set<Parameter<?>> parameterSet;
    private boolean isConfigured = false;
    private int monitorSetCount;

    public MetaNode(Set<Parameter<?>> parameterSet) {
	super();
	this.parameterSet = parameterSet;
    }

    public ChainData[] getChainData() {
	return chainData;
    }

    public Node createNode() {
	return new DefaultNode(this);
    }

    public Node createNode(int parameterId) {
	return successors[parameterId].createNode();
    }

    public NodeMap createNodeMap() {
	// TODO Auto-generated method stub
	return null;
    }

    void setChainingData(ChainData[] chainData) {
	this.chainData = chainData;
    }

    public MetaNode getMetaNode(Parameter<?> parameter) {
	MetaNode node = successors[parameter.getIndex()];
	if (node == null) {
	    Set<Parameter<?>> parameterSet = new HashSet<Parameter<?>>();
	    parameterSet.addAll(this.parameterSet);
	    assert !parameterSet.contains(parameter) : "Parameter set could not have had contained new parameter.";
	    parameterSet.add(parameter);
	    node = new MetaNode(parameterSet);
	    successors[parameter.getIndex()] = node;
	}
	return node;
    }

    public Set<Parameter<?>> getParameterSet() {
        return parameterSet;
    }

    boolean isConfigured() {
	return isConfigured;
    }

    void setConfigured(boolean isConfigured) {
	this.isConfigured = isConfigured;
    }

    public int getMonitorSetCount() {
	return monitorSetCount;
    }

    public void setMonitorSetCount(int monitorSetCount) {
	this.monitorSetCount = monitorSetCount;
    }

}
