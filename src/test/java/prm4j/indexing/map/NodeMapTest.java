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
package prm4j.indexing.map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import prm4j.indexing.realtime.DefaultLowLevelBinding;
import prm4j.indexing.realtime.LowLevelBinding;

public class NodeMapTest {

    private final static LowLevelBinding b0 = new DefaultLowLevelBinding(null, 0, null);
    private final static LowLevelBinding b1 = new DefaultLowLevelBinding(null, 1, null);
    private final static LowLevelBinding b2 = new DefaultLowLevelBinding(null, 2, null);
    private final static LowLevelBinding b3 = new DefaultLowLevelBinding(null, 3, null);
    private final static LowLevelBinding b4 = new DefaultLowLevelBinding(null, 4, null);

    @Test
    public void size_sizeGrowsWithAddedEntries() throws Exception {
	MockMap map = new MockMap();
	assertEquals(0, map.size());
	map.getOrCreate(0, b0);
	assertEquals(1, map.size());
	map.getOrCreate(1, b1);
	assertEquals(2, map.size());
    }

    @Test
    public void getOrCreate_createEntryAndRetrieveEntry() throws Exception {
	MockMap map = new MockMap();
	MockEntry x = map.getOrCreate(0, b0);
	MockEntry y = map.getOrCreate(0, b0);
	// verify
	assertEquals(1, map.size());
	assertEquals(x, y);
    }

    @Test
    public void getOrCreate_createEntryAndRetrieveEntry2() throws Exception {
	MockMap map = new MockMap();
	MockEntry x = map.getOrCreate(12, b3);
	MockEntry y = map.get(12, b3);
	// verify
	assertEquals(1, map.size());
	assertEquals(x, y);
    }

    @Test
    public void getOrCreate_createEntryForSameBindingToDifferentParameters() throws Exception {
	MockMap map = new MockMap();
	MockEntry x = map.getOrCreate(0, b0);
	MockEntry y = map.getOrCreate(1, b0);
	// verify
	assertEquals(2, map.size());
	assertNotSame(x, y);
    }

    @Test
    public void get_entryForSameBindingWithDifferentParameterIsNotRetrievedAccidently() throws Exception {
	MockMap map = new MockMap();
	map.getOrCreate(0, b0);
	MockEntry y = map.get(1, b0);
	// verify
	assertNull(y);
    }

    @Test
    public void get_entriesForSameBindingAreRetrieved() throws Exception {
	MockMap map = new MockMap();
	MockEntry x = map.getOrCreate(0, b0);
	MockEntry y = map.getOrCreate(1, b0);
	assertEquals(x, map.get(0, b0));
	assertEquals(y, map.get(1, b0));
    }

    @Test
    public void getOrCreate_createNewEntryForSameKeyWhenRemoved() throws Exception {
	MockMap map = new MockMap();
	MockEntry x = map.getOrCreate(0, b0);
	map.remove(b0);
	MockEntry y = map.getOrCreate(0, b0);
	// verify
	assertNotSame(x, y);
    }

    @Test
    public void remove_nodeWasRemoved() throws Exception {
	MockMap map = new MockMap();
	int parameterIndex = 42;
	map.getOrCreate(parameterIndex, b0);
	map.remove(b0);
	// verify
	assertNull(map.get(parameterIndex, b0));
    }

    @Test
    public void remove_multipleNodesWithTheSameBindingAreRemoved() throws Exception {
	MockMap map = new MockMap();
	int parameterIndex = 42;
	int parameterIndex2 = 42;
	map.getOrCreate(parameterIndex, b0);
	map.getOrCreate(parameterIndex2, b0);
	map.remove(b0);
	// verify
	assertNull(map.get(parameterIndex, b0));
	assertNull(map.get(parameterIndex2, b0));
    }

    @Test
    public void remove_sizeDecrementsUponRemoval() throws Exception {
	MockMap map = new MockMap();
	map.getOrCreate(0, b0);
	map.remove(b0);
	// verify
	assertEquals(0, map.size());
    }

    @Test
    public void remove_getOrCreate_collision() throws Exception {
	MockMap map = new MockMap();
	map.getOrCreate(0, b0);
	DefaultLowLevelBinding b0x = new DefaultLowLevelBinding(null, 0, null);
	MockEntry aEntry = map.getOrCreate(1, b0x);
	assertEquals(2, map.size());
	map.remove(b0);
	assertEquals(1, map.size());
	MockEntry aEntry2 = map.getOrCreate(1, b0x);
	assertTrue(aEntry == aEntry2);
    }

    @Test
    public void ensureCapacity_hittingThresholdForcesResizing() throws Exception {
	MockMap map = new MockMap();
	assertEquals(8, map.table.length);
	map.getOrCreate(0, b0);
	map.getOrCreate(1, b1);
	map.getOrCreate(2, b2);
	map.getOrCreate(3, b3);
	map.getOrCreate(4, b4);
	assertEquals(8, map.table.length);
	map.getOrCreate(1, b0);
	assertEquals(16, map.table.length);
	map.getOrCreate(2, b0);
	map.getOrCreate(3, b1);
    }

    @Test
    public void ensureCapacity_resizeKeepsEntries() throws Exception {
	MockMap map = new MockMap();
	assertEquals(8, map.table.length);
	MockEntry e1 = map.getOrCreate(0, b0);
	MockEntry e2 = map.getOrCreate(1, b1);
	MockEntry e3 = map.getOrCreate(2, b2);
	MockEntry e4 = map.getOrCreate(3, b3);
	MockEntry e5 = map.getOrCreate(4, b4);
	MockEntry e6 = map.getOrCreate(1, b0);
	assertEquals(16, map.table.length);
	MockEntry e7 = map.getOrCreate(3, b2);

	assertEquals(e1, map.getOrCreate(0, b0));
	assertEquals(e2, map.getOrCreate(1, b1));
	assertEquals(e3, map.getOrCreate(2, b2));
	assertEquals(e4, map.getOrCreate(3, b3));
	assertEquals(e5, map.getOrCreate(4, b4));
	assertEquals(e6, map.getOrCreate(1, b0));
	assertEquals(e7, map.getOrCreate(3, b2));
    }

    static class MockMap extends NodeMap<MockEntry> {

	@Override
	protected MockEntry[] createTable(int size) {
	    return new MockEntry[size];
	}

	@Override
	protected MockEntry createEntry(int parameterIndex, LowLevelBinding binding) {
	    return new MockEntry(parameterIndex, binding);
	}

    }

    static class MockEntry implements NodeMapEntry<MockEntry> {

	private final LowLevelBinding binding;
	private final int parameterIndex;
	private MockEntry next;

	public MockEntry(int parameterIndex, LowLevelBinding binding) {
	    this.parameterIndex = parameterIndex;
	    this.binding = binding;
	}

	@Override
	public LowLevelBinding getKey() {
	    return binding;
	}

	@Override
	public int parameterIndex() {
	    return parameterIndex;
	}

	@Override
	public MockEntry next() {
	    return next;
	}

	@Override
	public void setNext(MockEntry nextEntry) {
	    next = nextEntry;
	}

    }

}
