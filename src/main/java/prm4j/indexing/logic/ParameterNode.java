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
package prm4j.indexing.logic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import prm4j.Util;
import prm4j.api.Parameter;
import prm4j.indexing.binding.Binding;
import prm4j.indexing.node.DefaultNodeFactory;
import prm4j.indexing.node.LeafNodeFactory;
import prm4j.indexing.node.LeafNodeWithMonitorSetsFactory;
import prm4j.indexing.node.Node;
import prm4j.indexing.node.NodeFactory;
import prm4j.indexing.node.NodeManager;

/**
 * Every {@link Node} is equipped with a ParameterNode, containing factory methods and providing statically computed
 * algorithm logic.
 * 
 */
public class ParameterNode {

    private final Set<Parameter<?>> fullParameterSet;
    private final Set<Parameter<?>> nodeParameterSet;
    private final List<Parameter<?>> nodeParameterList;
    private final int[] compressedIndex;
    private final Parameter<?> lastParameter;
    private final int lastParameterIndex;
    private final ParameterNode[] successors;

    private NodeManager nodeManager;
    private NodeFactory nodeFactory;

    private Set<UpdateChainingsArgs> chainDataSet;
    private UpdateChainingsArgs[] chainDataArray;
    private int monitorSetCount;

    /**
     * stateIndex * parameterMasksCount * parameterMask
     */
    private boolean[][] aliveParameterMasks;

    public ParameterNode(Set<Parameter<?>> nodeParameterSet, Set<Parameter<?>> fullParameterSet) {
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
	successors = new ParameterNode[fullParameterSet.size()];
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
    public UpdateChainingsArgs[] getUpdateChainingsArgs() {
	return chainDataArray;
    }

    /**
     * @return the set representation of the chain data.
     */
    public Set<UpdateChainingsArgs> getChainDataSet() {
	return chainDataSet;
    }

    /**
     * Creates a node for the given binding.
     * 
     * @param binding
     * @return the node
     */
    public Node createNode(Binding binding) {
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
    public Node createNode(int parameterIndex, Binding binding) {
	return successors[parameterIndex].createNode(binding);
    }

    void setChainData(Set<UpdateChainingsArgs> chainDataSet) {
	chainDataArray = chainDataSet.toArray(new UpdateChainingsArgs[0]);
	this.chainDataSet = chainDataSet;
    }

    /**
     * Retrieves the ParameterNode of the successor for the given parameter.
     * 
     * @param parameter
     * @return the successor parameter node or <code>null</code>, if it does not exist
     */
    public ParameterNode getParameterNode(Parameter<?> parameter) {
	return successors[parameter.getIndex()];
    }

    /**
     * Retrieves the ParameterNode of the successor for the given parameter index.
     * 
     * @param parameterIndex
     * @return the successor parameter node or <code>null</code>, if it does not exist
     */
    public ParameterNode getParameterNode(int parameterIndex) {
	return successors[parameterIndex];
    }

    /**
     * Retrieves the ParameterNode of the successor for the given parameter, creating one, if it does not exist.
     * 
     * @param parameter
     *            the parameter which will augment the parameter set associated with the parameter node
     * @return the successor parameter node
     */
    public ParameterNode createAndGetParameterNode(Parameter<?> parameter) {
	ParameterNode parameterNode = successors[parameter.getIndex()];
	if (parameterNode == null) {
	    Set<Parameter<?>> nextNodeParameterSet = new HashSet<Parameter<?>>(nodeParameterSet);
	    assert !nextNodeParameterSet.contains(parameter) : "Parameter set could not have had contained new parameter.";
	    nextNodeParameterSet.add(parameter);
	    parameterNode = new ParameterNode(nextNodeParameterSet, fullParameterSet);
	    successors[parameter.getIndex()] = parameterNode;
	}
	return parameterNode;
    }

    /**
     * Retrieves the ParameterNode traversing the successor sub-meta-tree.
     * 
     * @param parameters
     *            a sequence of parameters selecting the parameter node
     * @return a parameter node
     */
    public ParameterNode getParameterNode(Parameter<?>... parameters) {
	ParameterNode node = this;
	for (Parameter<?> parameter : parameters) {
	    node = node.createAndGetParameterNode(parameter);
	}
	return node;
    }

    /**
     * Retrieves the ParameterNode traversing the successor sub-meta-tree.
     * 
     * @param parameters
     *            a sequence of parameters selecting the parameter node
     * @return a parameter node
     */
    public ParameterNode getParameterNode(List<Parameter<?>> parameterList) {
	ParameterNode node = this;
	for (Parameter<?> parameter : parameterList) {
	    node = node.createAndGetParameterNode(parameter);
	}
	return node;
    }

    /**
     * Returns the parameter set which uniquely identifies this parameter node.
     * 
     * @return the node parameter set
     */
    public Set<Parameter<?>> getNodeParameterSet() {
	return nodeParameterSet;
    }

    /**
     * Returns the parameters, which uniquely identify this parameter node, sorted by parameter id.
     * 
     * @return parameters sorted by parameter id
     */
    public List<Parameter<?>> getNodeParameterList() {
	return nodeParameterList;
    }

    /**
     * Returns the parameter ids, which uniquely identify this parameter node, sorted by parameter id.
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
	if (this == obj) {
	    return true;
	}
	if (obj == null) {
	    return false;
	}
	if (getClass() != obj.getClass()) {
	    return false;
	}
	ParameterNode other = (ParameterNode) obj;
	if (chainDataSet == null) {
	    if (other.chainDataSet != null) {
		return false;
	    }
	} else if (!chainDataSet.equals(other.chainDataSet)) {
	    return false;
	}
	if (fullParameterSet == null) {
	    if (other.fullParameterSet != null) {
		return false;
	    }
	} else if (!fullParameterSet.equals(other.fullParameterSet)) {
	    return false;
	}
	if (monitorSetCount != other.monitorSetCount) {
	    return false;
	}
	if (nodeParameterSet == null) {
	    if (other.nodeParameterSet != null) {
		return false;
	    }
	} else if (!nodeParameterSet.equals(other.nodeParameterSet)) {
	    return false;
	}
	if (!Arrays.equals(successors, other.successors)) {
	    return false;
	}
	return true;
    }

    @Override
    public String toString() {
	return "ParameterNode [successors=" + Arrays.toString(successors) + ", chainDataSet=" + chainDataSet
		+ ", nodeParameterSet=" + nodeParameterSet + ", monitorSetCount=" + monitorSetCount + "]";
    }

    /**
     * @return a set representation of the successors of this {@link ParameterNode}.
     */
    public Set<ParameterNode> getSuccessors() {
	final Set<ParameterNode> result = new HashSet<ParameterNode>();
	for (int i = 0; i < successors.length; i++) {
	    final ParameterNode successor = successors[i];
	    if (successor != null) {
		result.add(successor);
	    }
	}
	return result;
    }

    /**
     * @return a list of all parameter nodes in this tree including this node.
     */
    public List<ParameterNode> getAllNodesInSubtree() {
	final List<ParameterNode> result = new ArrayList<ParameterNode>();
	result.add(this);
	for (int i = 0; i < result.size(); i++) {
	    Set<ParameterNode> nodes = result.get(i).getSuccessors();
	    for (ParameterNode parameterNode : nodes) {
		result.add(parameterNode);
	    }
	}
	return result;
    }

    /**
     * Sets the nodeManager to all parameter nodes in this tree including this node.
     * 
     * @param nodeManager
     *            the node manager
     */
    public void setNodeManagerToTree(NodeManager nodeManager) {
	for (ParameterNode parameterNode : getAllNodesInSubtree()) {
	    parameterNode.setNodeManager(nodeManager);
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
	for (ParameterNode succ : successors) {
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
    public boolean isAcceptingStateReachable(Binding[] compressedBindings) {
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
    public <T> T getParameterValue(Parameter<T> parameter, Binding[] compressedBindings) {
	return (T) compressedBindings[compressedIndex[parameter.getIndex()]].get();
    }

    @Override
    public int hashCode() {
	return nodeParameterSet.hashCode();
    }

}
