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
package prm4j.indexing.realtime;

import prm4j.api.Binding;
import prm4j.api.Event;
import prm4j.indexing.Monitor;
import prm4j.indexing.staticdata.MetaNode;

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
    public boolean isAcceptingStateReachable() {
	return false;
    }

    @Override
    public Monitor copy() {
	return this;
    }

    @Override
    public LowLevelBinding[] getCompressedBindings() {
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
    public MetaNode getMetaNode() {
	return null;
    }

    @Override
    public void setMetaNode(MetaNode metaNode) {

    }

    @Override
    public void setBindings(LowLevelBinding[] bindings) {
    }

    @Override
    public void setTimestamp(long timestamp) {

    }

    @Override
    public Monitor copy(LowLevelBinding[] bindings) {
	return null;
    }

    @Override
    public Monitor copy(LowLevelBinding[] bindings, long timestamp) {
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
