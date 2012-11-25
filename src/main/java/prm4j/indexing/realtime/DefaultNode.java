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
package prm4j.indexing.realtime;

import java.lang.ref.WeakReference;

import prm4j.Util;
import prm4j.api.Parameter;
import prm4j.indexing.BaseMonitor;
import prm4j.indexing.staticdata.MetaNode;

public class DefaultNode extends AbstractNode {

    private final MetaNode metaNode;
    private final MonitorSet[] monitorSets;
    private BaseMonitor monitor;
    private final WeakReference<Node> nodeRef;

    private WeakReference<Node> cachedNodeRef = null;
    private LowLevelBinding cachedBinding = null;

    /**
     * @param metaNode
     * @param key
     *            may be null, if node is root node
     * @param hashCode
     *            hash code of the key
     */
    public DefaultNode(MetaNode metaNode, LowLevelBinding key, int hashCode) {
	super(key, hashCode);
	this.metaNode = metaNode;
	monitorSets = new MonitorSet[metaNode.getMonitorSetCount()];
	nodeRef = new WeakReference<Node>(this);
    }

    @Override
    public MetaNode getMetaNode() {
	return metaNode;
    }

    @Override
    public BaseMonitor getMonitor() {
	return monitor;
    }

    @Override
    public Node getOrCreateNode(LowLevelBinding binding) {
	binding.registerNode(nodeRef);
	if (cachedBinding != binding) {
	    cachedBinding = binding;
	    cachedNodeRef = getOrCreate(binding, binding.hashCode()).getNodeRef();
	}
	return cachedNodeRef.get();
    }

    @Override
    public Node getNode(LowLevelBinding binding) {
	if (cachedBinding != binding) {
	    final Node node = get(binding, binding.hashCode());
	    if (node != null) {
		cachedBinding = binding;
		cachedNodeRef = node.getNodeRef();
		return node;
	    } else {
		return null;
	    }
	}
	return cachedNodeRef.get();
    }

    @Override
    public void setMonitor(BaseMonitor monitor) {
	this.monitor = monitor;
    }

    @Override
    public void remove(LowLevelBinding binding) {
	super.remove(binding, binding.hashCode());
    }

    @Override
    public MonitorSet getMonitorSet(int monitorSetId) {
	// lazy creation
	MonitorSet monitorSet = monitorSets[monitorSetId];
	if (monitorSet == null) {
	    monitorSet = new MonitorSet();
	    monitorSets[monitorSetId] = monitorSet;
	}
	return monitorSet;
    }

    @Override
    public MonitorSet[] getMonitorSets() {
	return monitorSets;
    }

    @Override
    public String toString() {
	if (monitor != null) {
	    // output e.g.: (p2=b, p3=c, p5=e)
	    return Util.bindingsToString(monitor.getLowLevelBindings());
	} else {
	    // output e.g.: (p2=?, p3=?, p5=e) because we only now the key and the node parameter set
	    final StringBuilder sb = new StringBuilder();
	    sb.append("(");
	    int i = 0;
	    final int max = metaNode.getNodeParameterList().size();
	    for (Parameter<?> parameter : metaNode.getNodeParameterList()) {
		if (++i < max) {
		    sb.append(parameter).append("=?, ");
		} else {
		    break;
		}
	    }
	    final String key = getKey() == null ? "" : getKey().toString();
	    sb.append(key).append(")");
	    return sb.toString();
	}
    }

    @Override
    public WeakReference<Node> getNodeRef() {
	return nodeRef;
    }

}
