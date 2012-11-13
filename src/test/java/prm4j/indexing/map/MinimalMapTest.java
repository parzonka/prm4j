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
	MinimalMap<Object, MockEntry> map = new MockMap();
	assertEquals(0, map.size());
	map.get("a");
	assertEquals(1, map.size());
	map.get("b");
	assertEquals(2, map.size());
    }

    @Test
    public void get_createEntryAndRetrieveEntry() throws Exception {
	MinimalMap<Object, MockEntry> map = new MockMap();
	String a = "a";
	MockEntry x = map.get(a);
	MockEntry y = map.get(a);
	assertEquals(1, map.size());
	assertEquals(x, y);
    }

    @Test
    public void get_createNewEntryForSameKeyWhenRemoved() throws Exception {
	MinimalMap<Object, MockEntry> map = new MockMap();
	String a = "a";
	MockEntry x = map.get(a);
	map.remove(a);
	MockEntry y = map.get(a);
	assertNotSame(x, y);

    }

    @Test
    public void remove_sizeDecrementsUponRemoval() throws Exception {
	MinimalMap<Object, MockEntry> map = new MockMap();
	String a = "a";
	map.get(a);
	map.remove(a);
	assertEquals(0, map.size());
    }

    @Test
    public void remove_get_collision1() throws Exception {
	MinimalMap<Object, MockEntry> map = new MockMap();
	String a = "a";
	String b = "b";
	MockEntry aEntry = map.get(a, 0);
	map.get(b, 0);
	assertEquals(2, map.size());
	map.remove(b, 0);
	assertEquals(1, map.size());
	MockEntry aEntry2 = map.get(a, 0);
	assertTrue(aEntry == aEntry2);
    }

    @Test
    public void remove_get_collision2() throws Exception {
	MinimalMap<Object, MockEntry> map = new MockMap();
	String a = "a";
	String b = "b";
	map.get(b, 0);
	MockEntry aEntry = map.get(a, 0);
	assertEquals(2, map.size());
	map.remove(b, 0);
	assertEquals(1, map.size());
	MockEntry aEntry2 = map.get(a, 0);
	assertTrue(aEntry == aEntry2);
    }

    @Test
    public void removeEntry_get_createNewEntryForSameKeyWhenRemoved() throws Exception {
	MinimalMap<Object, MockEntry> map = new MockMap();
	String a = "a";
	MockEntry x = map.get(a, 0);
	map.removeEntry(x);
	MockEntry y = map.get(a);
	assertNotSame(x, y);
    }

    @Test
    public void removeEntry_sizeDecrementsUponRemoval() throws Exception {
	MinimalMap<Object, MockEntry> map = new MockMap();
	String a = "a";
	MockEntry x = map.get(a);
	map.removeEntry(x);
	assertEquals(0, map.size());
    }

    @Test
    public void removeEntry_get_collision1() throws Exception {
	MinimalMap<Object, MockEntry> map = new MockMap();
	String a = "a";
	MockEntry bEntry = map.get("b", 0);
	MockEntry aEntry = map.get(a, 0);
	assertEquals(2, map.size());
	map.removeEntry(bEntry);
	assertEquals(1, map.size());
	MockEntry aEntry2 = map.get(a, 0);
	assertTrue(aEntry == aEntry2);
    }

    @Test
    public void removeEntry_get_collision2() throws Exception {
	MinimalMap<Object, MockEntry> map = new MockMap();
	String a = "a";
	MockEntry aEntry = map.get(a, 0);
	MockEntry bEntry = map.get("b", 0);
	assertEquals(2, map.size());
	map.removeEntry(bEntry);
	assertEquals(1, map.size());
	MockEntry aEntry2 = map.get(a, 0);
	assertTrue(aEntry == aEntry2);
    }

    @Test
    public void ensureCapacity_hittingThresholdForcesResizing() throws Exception {
	MinimalMap<Object, MockEntry> map = new MockMap();
	assertEquals(8, map.table.length);
	map.get(1);
	map.get(2);
	map.get(3);
	map.get(4);
	map.get(5);
	assertEquals(8, map.table.length);
	map.get(6);
	assertEquals(16, map.table.length);
	map.get(7);
	map.get(8);
    }

    @Test
    public void ensureCapacity_resizeKeepsEntries() throws Exception {
	MinimalMap<Object, MockEntry> map = new MockMap();
	assertEquals(8, map.table.length);
	MockEntry e1 = map.get(1);
	MockEntry e2 = map.get(2);
	MockEntry e3 = map.get(3);
	MockEntry e4 = map.get(4);
	MockEntry e5 = map.get(5);
	MockEntry e6 = map.get(6);
	assertEquals(16, map.table.length);
	MockEntry e7 = map.get(7);

	assertEquals(e1, map.get(1));
	assertEquals(e2, map.get(2));
	assertEquals(e3, map.get(3));
	assertEquals(e4, map.get(4));
	assertEquals(e5, map.get(5));
	assertEquals(e6, map.get(6));
	assertEquals(e7, map.get(7));
    }

    static class MockMap extends MinimalMap<Object, MockEntry> {

	@Override
	protected MockEntry[] createTable(int size) {
	    return new MockEntry[size];
	}

	@Override
	protected MockEntry createEntry(Object key, int hashCode) {
	    return new MockEntry(key, hashCode);
	}

    }

    static class MockEntry implements MinimalMapEntry<Object, MockEntry> {

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
