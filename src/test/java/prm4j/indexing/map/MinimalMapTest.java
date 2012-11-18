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
	map.getOrCreate("a");
	assertEquals(1, map.size());
	map.getOrCreate("b");
	assertEquals(2, map.size());
    }

    @Test
    public void getOrCreate_createEntryAndRetrieveEntry() throws Exception {
	MinimalMap<Object, MockEntry> map = new MockMap();
	String a = "a";
	MockEntry x = map.getOrCreate(a);
	MockEntry y = map.getOrCreate(a);
	assertEquals(1, map.size());
	assertEquals(x, y);
    }

    @Test
    public void getOrCreate_createNewEntryForSameKeyWhenRemoved() throws Exception {
	MinimalMap<Object, MockEntry> map = new MockMap();
	String a = "a";
	MockEntry x = map.getOrCreate(a);
	map.remove(a);
	MockEntry y = map.getOrCreate(a);
	assertNotSame(x, y);

    }

    @Test
    public void remove_sizeDecrementsUponRemoval() throws Exception {
	MinimalMap<Object, MockEntry> map = new MockMap();
	String a = "a";
	map.getOrCreate(a);
	map.remove(a);
	assertEquals(0, map.size());
    }

    @Test
    public void remove_getOrCreate_collision1() throws Exception {
	MinimalMap<Object, MockEntry> map = new MockMap();
	String a = "a";
	String b = "b";
	MockEntry aEntry = map.getOrCreate(a, 0);
	map.getOrCreate(b, 0);
	assertEquals(2, map.size());
	map.remove(b, 0);
	assertEquals(1, map.size());
	MockEntry aEntry2 = map.getOrCreate(a, 0);
	assertTrue(aEntry == aEntry2);
    }

    @Test
    public void remove_getOrCreate_collision2() throws Exception {
	MinimalMap<Object, MockEntry> map = new MockMap();
	String a = "a";
	String b = "b";
	map.getOrCreate(b, 0);
	MockEntry aEntry = map.getOrCreate(a, 0);
	assertEquals(2, map.size());
	map.remove(b, 0);
	assertEquals(1, map.size());
	MockEntry aEntry2 = map.getOrCreate(a, 0);
	assertTrue(aEntry == aEntry2);
    }

    @Test
    public void removeEntry_getOrCreate_createNewEntryForSameKeyWhenRemoved() throws Exception {
	MinimalMap<Object, MockEntry> map = new MockMap();
	String a = "a";
	MockEntry x = map.getOrCreate(a, 0);
	map.removeEntry(x);
	MockEntry y = map.getOrCreate(a);
	assertNotSame(x, y);
    }

    @Test
    public void removeEntry_sizeDecrementsUponRemoval() throws Exception {
	MinimalMap<Object, MockEntry> map = new MockMap();
	String a = "a";
	MockEntry x = map.getOrCreate(a);
	map.removeEntry(x);
	assertEquals(0, map.size());
    }

    @Test
    public void removeEntry_getOrCreate_collision1() throws Exception {
	MinimalMap<Object, MockEntry> map = new MockMap();
	String a = "a";
	MockEntry bEntry = map.getOrCreate("b", 0);
	MockEntry aEntry = map.getOrCreate(a, 0);
	assertEquals(2, map.size());
	map.removeEntry(bEntry);
	assertEquals(1, map.size());
	MockEntry aEntry2 = map.getOrCreate(a, 0);
	assertTrue(aEntry == aEntry2);
    }

    @Test
    public void removeEntry_getOrCreate_collision2() throws Exception {
	MinimalMap<Object, MockEntry> map = new MockMap();
	String a = "a";
	MockEntry aEntry = map.getOrCreate(a, 0);
	MockEntry bEntry = map.getOrCreate("b", 0);
	assertEquals(2, map.size());
	map.removeEntry(bEntry);
	assertEquals(1, map.size());
	MockEntry aEntry2 = map.getOrCreate(a, 0);
	assertTrue(aEntry == aEntry2);
    }

    @Test
    public void ensureCapacity_hittingThresholdForcesResizing() throws Exception {
	MinimalMap<Object, MockEntry> map = new MockMap();
	assertEquals(8, map.table.length);
	map.getOrCreate(1);
	map.getOrCreate(2);
	map.getOrCreate(3);
	map.getOrCreate(4);
	map.getOrCreate(5);
	assertEquals(8, map.table.length);
	map.getOrCreate(6);
	assertEquals(16, map.table.length);
	map.getOrCreate(7);
	map.getOrCreate(8);
    }

    @Test
    public void ensureCapacity_resizeKeepsEntries() throws Exception {
	MinimalMap<Object, MockEntry> map = new MockMap();
	assertEquals(8, map.table.length);
	MockEntry e1 = map.getOrCreate(1);
	MockEntry e2 = map.getOrCreate(2);
	MockEntry e3 = map.getOrCreate(3);
	MockEntry e4 = map.getOrCreate(4);
	MockEntry e5 = map.getOrCreate(5);
	MockEntry e6 = map.getOrCreate(6);
	assertEquals(16, map.table.length);
	MockEntry e7 = map.getOrCreate(7);

	assertEquals(e1, map.getOrCreate(1));
	assertEquals(e2, map.getOrCreate(2));
	assertEquals(e3, map.getOrCreate(3));
	assertEquals(e4, map.getOrCreate(4));
	assertEquals(e5, map.getOrCreate(5));
	assertEquals(e6, map.getOrCreate(6));
	assertEquals(e7, map.getOrCreate(7));
    }

    @Test
    public void getNonCreative_createEntryAndRetrieveEntry() throws Exception {
	MinimalMap<Object, MockEntry> map = new MockMap();
	String a = "a";
	MockEntry x = map.getOrCreate(a, 1);
	MockEntry y = map.getNonCreative(a, 1);
	assertEquals(x, y);
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
	public int hashCode() {
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
