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

import java.util.Arrays;
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

    private final MetaNode[] successors;
    private ChainData[] chainData;
    private Set<Parameter<?>> fullParameterSet;
    private boolean isConfigured = false;
    private int monitorSetCount;

    public MetaNode(Set<Parameter<?>> fullParameterSet) {
	super();
	this.fullParameterSet = fullParameterSet;
	successors = new MetaNode[fullParameterSet.size()];
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

    void setChainData(ChainData[] chainData) {
	this.chainData = chainData;
    }

    public MetaNode getMetaNode(Parameter<?> parameter) {
	MetaNode node = successors[parameter.getIndex()];
	if (node == null) {
	    Set<Parameter<?>> parameterSet = new HashSet<Parameter<?>>();
	    parameterSet.addAll(fullParameterSet);
	    assert !parameterSet.contains(parameter) : "Parameter set could not have had contained new parameter.";
	    parameterSet.add(parameter);
	    node = new MetaNode(parameterSet);
	    successors[parameter.getIndex()] = node;
	}
	return node;
    }

    public Set<Parameter<?>> getParameterSet() {
	return fullParameterSet;
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

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + Arrays.hashCode(chainData);
	result = prime * result + (isConfigured ? 1231 : 1237);
	result = prime * result + monitorSetCount;
	result = prime * result + ((fullParameterSet == null) ? 0 : fullParameterSet.hashCode());
	result = prime * result + Arrays.hashCode(successors);
	return result;
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	MetaNode other = (MetaNode) obj;
	if (!Arrays.equals(chainData, other.chainData))
	    return false;
	if (isConfigured != other.isConfigured)
	    return false;
	if (monitorSetCount != other.monitorSetCount)
	    return false;
	if (fullParameterSet == null) {
	    if (other.fullParameterSet != null)
		return false;
	} else if (!fullParameterSet.equals(other.fullParameterSet))
	    return false;
	if (!Arrays.equals(successors, other.successors))
	    return false;
	return true;
    }

    @Override
    public String toString() {
	return "MetaNode [successors=" + Arrays.toString(successors) + ", chainData=" + Arrays.toString(chainData)
		+ ", parameterSet=" + fullParameterSet + ", isConfigured=" + isConfigured + ", monitorSetCount="
		+ monitorSetCount + "]";
    }

}
