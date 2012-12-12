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
import prm4j.api.Binding;
import prm4j.api.Parameter;
import prm4j.indexing.realtime.DefaultNodeFactory;
import prm4j.indexing.realtime.LeafNodeWithMonitorSetsFactory;
import prm4j.indexing.realtime.LeafNodeFactory;
import prm4j.indexing.realtime.LowLevelBinding;
import prm4j.indexing.realtime.Node;

/**
 * Every {@link Node} is equipped with a MetaNode, containing factory methods and providing statically computed
 * algorithm logic.
 *
 */
public class MetaNode {

    private final MetaNode[] successors;

    private Set<Parameter<?>> fullParameterSet;
    private Set<Parameter<?>> nodeParameterSet;
    private List<Parameter<?>> nodeParameterList;

    private Set<ChainData> chainDataSet;
    private ChainData[] chainDataArray;

    private int monitorSetCount;
    private final Parameter<?> lastParameter;
    private final int lastParameterIndex;

    private NodeFactory nodeFactory;

    public MetaNode(Set<Parameter<?>> nodeParameterSet, Set<Parameter<?>> fullParameterSet) {
	super();
	assert parameterIndexIsValid(fullParameterSet) : "Full parameter set must be valid.";
	assert Util.isSubsetEq(nodeParameterSet, fullParameterSet) : "Node parameters must be a subset of the full parameter set.";
	nodeParameterList = Util.asSortedList(nodeParameterSet);
	if (!nodeParameterSet.isEmpty()) {
	    lastParameter = nodeParameterList.get(nodeParameterList.size() - 1);
	    lastParameterIndex = lastParameter.getIndex();
	} else {
	    lastParameter = null;
	    lastParameterIndex = -1;
	}
	this.nodeParameterSet = nodeParameterSet;
	this.fullParameterSet = fullParameterSet;
	successors = new MetaNode[fullParameterSet.size()];
    }

    /**
     * Tests if the parameterIndex of all parameters is a sequence in [0, ..., parameterCount] without repetitions.
     *
     * @param parameterSet
     * @return <code>true</code> if the parameter set is valid
     */
    private static boolean parameterIndexIsValid(Set<Parameter<?>> parameterSet) {
	Set<Integer> usedIndices = new HashSet<Integer>();
	for (Parameter<?> parameter : parameterSet) {
	    usedIndices.add(parameter.getIndex());
	    if (parameter.getIndex() >= parameterSet.size() || parameter.getIndex() < 0) {
		return false;
	    }
	}
	return usedIndices.size() == parameterSet.size();
    }

    public ChainData[] getChainDataArray() {
	return chainDataArray;
    }

    public Set<ChainData> getChainDataSet() {
	return chainDataSet;
    }

    public Node createNode(LowLevelBinding key) {
	return nodeFactory.createNode(this, lastParameterIndex, key);
    }

    public Node createRootNode() {
	return createNode(null);
    }

    /**
     * Creates a node by a successor.
     *
     * @param parameterIndex
     *            selects the successor
     * @param binding
     *            the binding which will augment the binding set associated with the node
     * @return the node
     */
    public Node createNode(int parameterIndex, LowLevelBinding binding) {
	return getSuccessors()[parameterIndex].createNode(binding);
    }

    void setChainData(Set<ChainData> chainDataSet) {
	chainDataArray = chainDataSet.toArray(new ChainData[0]);
	this.chainDataSet = chainDataSet;
    }

    /**
     * Retrieves the MetaNode of the successor for the given parameter.
     *
     * @param parameter
     * @return the successor meta node or <code>null</code>, if it does not exist
     */
    public MetaNode getMetaNode(Parameter<?> parameter) {
	return getSuccessors()[parameter.getIndex()];
    }

    /**
     * Retrieves the MetaNode of the successor for the given parameter, creating one, if it does not exist.
     *
     * @param parameter
     *            the parameter which will augment the parameter set associated with the meta node
     * @return the successor meta node
     */
    public MetaNode createAndGetMetaNode(Parameter<?> parameter) {
	MetaNode metaNode = getSuccessors()[parameter.getIndex()];
	if (metaNode == null) {
	    Set<Parameter<?>> nextNodeParameterSet = new HashSet<Parameter<?>>(nodeParameterSet);
	    assert !nextNodeParameterSet.contains(parameter) : "Parameter set could not have had contained new parameter.";
	    nextNodeParameterSet.add(parameter);
	    metaNode = new MetaNode(nextNodeParameterSet, fullParameterSet);
	    getSuccessors()[parameter.getIndex()] = metaNode;
	}
	return metaNode;
    }

    /**
     * Retrieves the MetaNode traversing the successor sub-meta-tree.
     *
     * @param parameters
     *            a sequence of parameters selecting the meta node
     * @return a meta node
     */
    public MetaNode getMetaNode(Parameter<?>... parameters) {
	MetaNode node = this;
	for (Parameter<?> parameter : parameters) {
	    node = node.createAndGetMetaNode(parameter);
	}
	return node;
    }

    /**
     * Retrieves the MetaNode traversing the successor sub-meta-tree.
     *
     * @param parameters
     *            a sequence of parameters selecting the meta node
     * @return a meta node
     */
    public MetaNode getMetaNode(List<Parameter<?>> parameterList) {
	MetaNode node = this;
	for (Parameter<?> parameter : parameterList) {
	    node = node.createAndGetMetaNode(parameter);
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
	return nodeParameterList;
    }

    /**
     * Returns the parameter ids, which uniquely identify this meta node, sorted by parameter id.
     *
     * @return parameters sorted by parameter id
     */
    public int[] getNodeMask() {
	return Util.toNodeMask(nodeParameterSet);
    }

    /**
     * Returns the number of monitor sets for this kind of node.
     *
     * @return
     */
    public int getMonitorSetCount() {
	return monitorSetCount;
    }

    /**
     * Sets the number of monitor sets for this kind of node.
     *
     * @param monitorSetCount
     */
    public void setMonitorSetCount(int monitorSetCount) {
	this.monitorSetCount = monitorSetCount;
    }

    /**
     * Transforms bindings from the compressed representation to the uncompressed representation.
     *
     * @param compressedBindings
     * @return uncompressed bindings
     */
    public Binding[] uncompressBindings(Binding[] compressedBindings) {
	assert compressedBindings.length == nodeParameterList.size();
	Binding[] result = new Binding[fullParameterSet.size()];
	for (int i = 0; i < compressedBindings.length; i++) {
	    result[nodeParameterList.get(i).getIndex()] = compressedBindings[i];
	}
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

    public int getLastParameterIndex() {
	return lastParameterIndex;
    }

    /**
     * Select and initialize node factory
     */
    public void initializeNodeFactory() {
	boolean hasSuccessors = false;
	for (MetaNode succ : successors) {
	    if (succ != null) {
		hasSuccessors = true;
		break;
	    }
	}
	if (hasSuccessors) {
	    nodeFactory = new DefaultNodeFactory();
	} else {
	    if (monitorSetCount > 0) {
		nodeFactory = new LeafNodeWithMonitorSetsFactory();
	    } else {
		nodeFactory = new LeafNodeFactory();
	    }
	}
    }
}
