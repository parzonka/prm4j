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
import java.util.List;
import java.util.Set;

import prm4j.Util;
import prm4j.api.Parameter;
import prm4j.indexing.realtime.DefaultNode;
import prm4j.indexing.realtime.LowLevelBinding;
import prm4j.indexing.realtime.Node;

/**
 * Every {@link Node} is equipped with a MetaNode, containing factory methods and providing statically computed
 * algorithm logic.
 *
 */
public class MetaNode {

    private final MetaNode[] successors;
    private Set<ChainData> chainDataSet;
    private Set<Parameter<?>> nodeParameterSet;
    private Set<Parameter<?>> fullParameterSet;
    private int monitorSetCount;
    private ChainData[] chainDataArray;

    public MetaNode(Set<Parameter<?>> nodeParameterSet, Set<Parameter<?>> fullParameterSet) {
	super();
	this.nodeParameterSet = nodeParameterSet;
	this.fullParameterSet = fullParameterSet;
	assert parameterIndexIsValid(fullParameterSet) : "Full parameter set must be valid.";
	successors = new MetaNode[fullParameterSet.size()];
    }

    private static boolean parameterIndexIsValid(Set<Parameter<?>> fullParameterSet) {
	for (Parameter<?> parameter : fullParameterSet) {
	    if (parameter.getIndex() >= fullParameterSet.size() || parameter.getIndex() < 0) {
		return false;
	    }
	}
	return true;
    }

    public ChainData[] getChainDataArray() {
	return chainDataArray;
    }

    public Set<ChainData> getChainDataSet() {
	return chainDataSet;
    }

    public Node createNode(LowLevelBinding key, int hashCode) {
	return new DefaultNode(this, key, hashCode);
    }

    public Node createRootNode() {
	return createNode(null, 0);
    }

    /**
     * Creates a Node by a successor.
     *
     * @param parameterId
     * @param key
     * @param hashCode
     * @return
     */
    public Node createNode(int parameterId, LowLevelBinding key, int hashCode) {
	return getSuccessors()[parameterId].createNode(key, hashCode);
    }

    void setChainData(Set<ChainData> chainDataSet) {
	chainDataArray = chainDataSet.toArray(new ChainData[0]);
	this.chainDataSet = chainDataSet;
    }

    public MetaNode getMetaNode(Parameter<?> parameter) {
	MetaNode node = getSuccessors()[parameter.getIndex()];
	if (node == null) {
	    Set<Parameter<?>> nodeParameterSet = new HashSet<Parameter<?>>();
	    nodeParameterSet.addAll(this.nodeParameterSet);
	    assert !nodeParameterSet.contains(parameter) : "Parameter set could not have had contained new parameter.";
	    nodeParameterSet.add(parameter);
	    node = new MetaNode(nodeParameterSet, fullParameterSet);
	    getSuccessors()[parameter.getIndex()] = node;
	}
	return node;
    }

    public MetaNode getMetaNode(Parameter<?>... parameters) {
	MetaNode node = this;
	for (Parameter<?> parameter : parameters) {
	    node = node.getMetaNode(parameter);
	}
	return node;
    }

    public MetaNode getMetaNode(List<Parameter<?>> parameterList) {
	MetaNode node = this;
	for (Parameter<?> parameter : parameterList) {
	    node = node.getMetaNode(parameter);
	}
	return node;
    }

    /**
     * Returns the parameter set which uniquely identifies this meta node.
     *
     * @return the node parameter set
     */
    public Set<Parameter<?>> getNodeParameterSet() {
	return nodeParameterSet;
    }

    /**
     * Returns the parameters, which uniquely identify this meta node, sorted by parameter id.
     *
     * @return parameters sorted by parameter id
     */
    public List<Parameter<?>> getNodeParameterList() {
	return Util.asSortedParameterList(nodeParameterSet);
    }

    /**
     * Returns the parameter ids, which uniquely identify this meta node, sorted by parameter id.
     *
     * @return parameters sorted by parameter id
     */
    public int[] getNodeMask() {
	return Util.toNodeMask(nodeParameterSet);
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
	result = prime * result + ((chainDataSet == null) ? 0 : chainDataSet.hashCode());
	result = prime * result + ((fullParameterSet == null) ? 0 : fullParameterSet.hashCode());
	result = prime * result + monitorSetCount;
	result = prime * result + ((nodeParameterSet == null) ? 0 : nodeParameterSet.hashCode());
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
	if (chainDataSet == null) {
	    if (other.chainDataSet != null)
		return false;
	} else if (!chainDataSet.equals(other.chainDataSet))
	    return false;
	if (fullParameterSet == null) {
	    if (other.fullParameterSet != null)
		return false;
	} else if (!fullParameterSet.equals(other.fullParameterSet))
	    return false;
	if (monitorSetCount != other.monitorSetCount)
	    return false;
	if (nodeParameterSet == null) {
	    if (other.nodeParameterSet != null)
		return false;
	} else if (!nodeParameterSet.equals(other.nodeParameterSet))
	    return false;
	if (!Arrays.equals(successors, other.successors))
	    return false;
	return true;
    }

    @Override
    public String toString() {
	return "MetaNode [successors=" + Arrays.toString(successors) + ", chainDataSet=" + chainDataSet
		+ ", nodeParameterSet=" + nodeParameterSet + ", monitorSetCount=" + monitorSetCount + "]";
    }

    public MetaNode[] getSuccessors() {
	return successors;
    }

}
