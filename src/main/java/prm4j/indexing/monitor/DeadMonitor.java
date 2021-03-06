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
package prm4j.indexing.monitor;

import prm4j.api.Event;
import prm4j.indexing.binding.Binding;
import prm4j.indexing.model.ParameterNode;

public class DeadMonitor implements Monitor {

    private static long createdMonitorsCount = 0L;

    private final long creationTime;

    public DeadMonitor(long creationTime) {
	createdMonitorsCount++;
	this.creationTime = creationTime;
    }

    @Override
    public boolean isDead() {
	return true;
    }

    @Override
    public boolean processEvent(Event event) {
	throw new IllegalStateException("Dead monitor should not receive updates!");
    }

    @Override
    public boolean isAlive() {
	return false;
    }

    @Override
    public Monitor copy() {
	return this;
    }

    @Override
    public Binding[] getCompressedBindings() {
	return null;
    }

    @Override
    public Binding[] getUncompressedBindings() {
	return null;
    }

    @Override
    public void terminate() {

    }

    @Override
    public boolean isTerminated() {
	return false;
    }

    @Override
    public long getTimestamp() {
	return creationTime;
    }

    @Override
    public boolean process(Event event) {
	return false;
    }

    @Override
    public ParameterNode getParameterNode() {
	return null;
    }

    @Override
    public void setParameterNode(ParameterNode parameterNode) {

    }

    @Override
    public void setCompressedBindings(Binding[] bindings) {
    }

    @Override
    public void setTimestamp(long timestamp) {

    }

    @Override
    public Monitor copy(Binding[] bindings) {
	return null;
    }

    @Override
    public Monitor copy(Binding[] bindings, long timestamp) {
	return null;
    }

    /**
     * DIAGNOSTIC
     * 
     * @return the number of created dead monitors
     */
    public static long getCreatedMonitorsCount() {
	return createdMonitorsCount;
    }

    /**
     * DIAGNOSTIC
     */
    public static long reset() {
	return createdMonitorsCount = 0;
    }

}
