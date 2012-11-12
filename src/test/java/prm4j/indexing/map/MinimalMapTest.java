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

import static org.junit.Assert.*;

import org.junit.Test;

public class MinimalMapTest {

    @Test
    public void size_sizeGrowsWithAddedEntries() throws Exception {
	MinimalMap<MockEntry> map = new MockMap();
	assertEquals(0, map.size());
	map.get("a");
	assertEquals(1, map.size());
	map.get("b");
	assertEquals(2, map.size());
    }

    @Test
    public void get_createEntryAndRetrieveEntry() throws Exception {
	MinimalMap<MockEntry> map = new MockMap();
	String a = "a";
	MockEntry x = map.get(a);
	MockEntry y = map.get(a);
	assertEquals(1, map.size());
	assertEquals(x, y);
    }

    @Test
    public void get_createNewEntryForSameKeyWhenRemoved() throws Exception {
	MinimalMap<MockEntry> map = new MockMap();
	String a = "a";
	MockEntry x = map.get(a);
	map.remove(a);
	MockEntry y = map.get(a);
	assertNotSame(x, y);

    }

    @Test
    public void remove_sizeDecrementsUponRemoval() throws Exception {
	MinimalMap<MockEntry> map = new MockMap();
	String a = "a";
	map.get(a);
	map.remove(a);
	assertEquals(0, map.size());
    }

    static class MockMap extends MinimalMap<MockEntry> {

	@Override
	protected MockEntry[] createTable(int size) {
	    return new MockEntry[size];
	}

	@Override
	protected MockEntry createEntry(Object key, int hashCode) {
	    return new MockEntry(key, hashCode);
	}

    }

    static class MockEntry implements MinimalMapEntry<MockEntry> {

	private final Object key;
	private final int hashCode;
	private MockEntry next;

	public MockEntry(Object key, int hashCode) {
	    this.key = key;
	    this.hashCode = hashCode;
	}

	@Override
	public int getHashCode() {
	    return hashCode;
	}

	@Override
	public Object getKey() {
	    return key;
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
