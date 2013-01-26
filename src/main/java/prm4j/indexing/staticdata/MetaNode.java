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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import prm4j.Util;
import prm4j.api.Binding;
import prm4j.api.Parameter;
import prm4j.indexing.realtime.DefaultNodeFactory;
import prm4j.indexing.realtime.LeafNodeFactory;
import prm4j.indexing.realtime.LeafNodeWithMonitorSetsFactory;
import prm4j.indexing.realtime.LowLevelBinding;
import prm4j.indexing.realtime.Node;
import prm4j.indexing.realtime.NodeManager;

/**
 * Every {@link Node} is equipped with a MetaNode, containing factory methods and providing statically computed
 * algorithm logic.
 *
 */
public class MetaNode {

    private final Set<Parameter<?>> fullParameterSet;
    private final Set<Parameter<?>> nodeParameterSet;
    private final List<Parameter<?>> nodeParameterList;
    private final int[] compressedIndex;
    private final Parameter<?> lastParameter;
    private final int lastParameterIndex;
    private final MetaNode[] successors;

    private NodeManager nodeManager;
    private NodeFactory nodeFactory;

    private Set<ChainData> chainDataSet;
    private ChainData[] chainDataArray;
    private int monitorSetCount;

    /**
     * stateIndex * parameterMasksCount * parameterMask
     */
    private boolean[][] aliveParameterMasks;

    public MetaNode(Set<Parameter<?>> nodeParameterSet, Set<Parameter<?>> fullParameterSet) {
	super();
	assert parameterIndexIsValid(fullParameterSet) : "Full parameter set must be valid.";
	assert Util.isSubsetEq(nodeParameterSet, fullParameterSet) : "Node parameters must be a subset of the full parameter set.";
	this.nodeParameterSet = nodeParameterSet;
	this.fullParameterSet = fullParameterSet;
	nodeParameterList = Util.asSortedList(nodeParameterSet);
	compressedIndex = getCompressedIndex();
	if (!nodeParameterSet.isEmpty()) {
	    lastParameter = nodeParameterList.get(nodeParameterList.size() - 1);
	    lastParameterIndex = lastParameter.getIndex();
	} else {
	    lastParameter = null;
	    lastParameterIndex = -1;
	}
	successors = new MetaNode[fullParameterSet.size()];
    }

    /**
     * Calculate a lookup table for mapping parameter indices to compressed binding indices.
     *
     * @return
     */
    private int[] getCompressedIndex() {
	int[] result = new int[fullParameterSet.size()];
	int i = 0;
	for (Parameter<?> parameter : nodeParameterList) {
	    result[parameter.getIndex()] = i++;
	}
	return result;
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

    /**
     * @return the array representation of the chain data.
     */
    public ChainData[] getChainDataArray() {
	return chainDataArray;
    }

    /**
     * @return the set representation of the chain data.
     */
    public Set<ChainData> getChainDataSet() {
	return chainDataSet;
    }

    /**
     * Creates a node for the given binding.
     *
     * @param binding
     * @return the node
     */
    public Node createNode(LowLevelBinding binding) {
	return nodeFactory.createNode(this, lastParameterIndex, binding);
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
	return successors[parameterIndex].createNode(binding);
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
	return successors[parameter.getIndex()];
    }

    /**
     * Retrieves the MetaNode of the successor for the given parameter, creating one, if it does not exist.
     *
     * @param parameter
     *            the parameter which will augment the parameter set associated with the meta node
     * @return the successor meta node
     */
    public MetaNode createAndGetMetaNode(Parameter<?> parameter) {
	MetaNode metaNode = successors[parameter.getIndex()];
	if (metaNode == null) {
	    Set<Parameter<?>> nextNodeParameterSet = new HashSet<Parameter<?>>(nodeParameterSet);
	    assert !nextNodeParameterSet.contains(parameter) : "Parameter set could not have had contained new parameter.";
	    nextNodeParameterSet.add(parameter);
	    metaNode = new MetaNode(nextNodeParameterSet, fullParameterSet);
	    successors[parameter.getIndex()] = metaNode;
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

    /**
     * @return a set representation of the successors of this {@link MetaNode}.
     */
    public Set<MetaNode> getSuccessors() {
	final Set<MetaNode> result = new HashSet<MetaNode>();
	for (int i = 0; i < successors.length; i++) {
	    final MetaNode successor = successors[i];
	    if (successor != null) {
		result.add(successor);
	    }
	}
	return result;
    }

    /**
     * @return a list of all meta nodes in this tree including this node.
     */
    public List<MetaNode> getAllNodesInSubtree() {
	final List<MetaNode> result = new ArrayList<MetaNode>();
	result.add(this);
	for (int i = 0; i < result.size(); i++) {
	    Set<MetaNode> nodes = result.get(i).getSuccessors();
	    for (MetaNode metaNode : nodes) {
		result.add(metaNode);
	    }
	}
	return result;
    }

    /**
     * Sets the nodeManager to all meta nodes in this tree including this node.
     *
     * @param nodeManager
     *            the node manager
     */
    public void setNodeManagerToTree(NodeManager nodeManager) {
	for (MetaNode metaNode : getAllNodesInSubtree()) {
	    metaNode.setNodeManager(nodeManager);
	}
    }

    /**
     * Returns the index of the parameter, which finally instantiated this instance, i.e. when this instance matches
     * <code>p1,...,pn</code>, it is <code>pn</code>.
     *
     * @return
     */
    public int getLastParameterIndex() {
	return lastParameterIndex;
    }

    public NodeManager getNodeManager() {
	return nodeManager;
    }

    public void setNodeManager(NodeManager nodeManager) {
	this.nodeManager = nodeManager;
	initializeNodeFactory();
    }

    /**
     * Select and initialize node factory
     */
    private void initializeNodeFactory() {
	boolean hasSuccessors = false;
	for (MetaNode succ : successors) {
	    if (succ != null) {
		hasSuccessors = true;
		break;
	    }
	}
	if (hasSuccessors) {
	    nodeFactory = new DefaultNodeFactory(nodeManager);
	} else {
	    if (monitorSetCount > 0) {
		nodeFactory = new LeafNodeWithMonitorSetsFactory(nodeManager);
	    } else {
		nodeFactory = new LeafNodeFactory(nodeManager);
	    }
	}
    }

    /**
     * Sets the information necessary to calculate if an accepting state can be reached from the given state and the
     * given bindings
     *
     * @param aliveParameterMasks
     */
    public void setAliveParameterMasks(boolean[][] aliveParameterMasks) {
	this.aliveParameterMasks = aliveParameterMasks;
    }

    public boolean[][] getAliveParameterMasks() {
	return aliveParameterMasks;
    }

    public boolean[] toCompressedParameterMask(int[] uncompressedParameterMask) {
	final boolean[] result = new boolean[nodeParameterList.size()];
	int i = 0;
	for (Parameter<?> param : nodeParameterList) {
	    for (int m : uncompressedParameterMask) {
		if (param.getIndex() == m) {
		    result[i] = true;
		}
	    }

	}
	return result;
    }

    /**
     * Tests, if an accepting state can be reached from the given bindings.
     *
     * @param compressedBindings
     *            A number of these bindings is checked for aliveness.
     * @return <code>true</code> if an accepting state is reachable
     */
    public boolean isAcceptingStateReachable(LowLevelBinding[] compressedBindings) {
	boolean[] parameterMask;
	outer: for (int i = 0; i < aliveParameterMasks.length; i++) {
	    parameterMask = aliveParameterMasks[i];
	    for (int j = 0; j < parameterMask.length; j++) {
		if (parameterMask[j] && compressedBindings[j].get() == null) {
		    continue outer;
		}
	    }
	    return true;
	}
	return false;
    }

    /**
     * Retrieves the parameter value for the given parameter and given compressed bindings.
     *
     * @param parameter
     * @param compressedBindings
     * @return the parameter value
     */
    @SuppressWarnings("unchecked")
    public <T> T getParameterValue(Parameter<T> parameter, LowLevelBinding[] compressedBindings) {
	return (T) compressedBindings[compressedIndex[parameter.getIndex()]].get();
    }

}
