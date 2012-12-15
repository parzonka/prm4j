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

/**
 * This hash map uses, stores and returns its values as entries, omitting the creation of wrapping map entries. This is
 * useful, when the values have to store their keys anyway and being aware of the {@link MinimalMap} is no problem.
 * Values have to implement {@link MinimalMapEntry}-interface to be used as entries.
 * <p>
 * The map transparently creates entries which are not found in the map, omitting a 'put' method.
 *
 * @param <E>
 *            the type of the value which is also used as map entry
 */
public abstract class MinimalMap<K, E extends MinimalMapEntry<K, E>> {

    /**
     * The default initial capacity - MUST be a power of two.
     */
    static final int DEFAULT_INITIAL_CAPACITY = 8;

    /**
     * The maximum capacity, used if a higher value is implicitly specified by either of the constructors with
     * arguments. MUST be a power of two <= 1<<30.
     */
    static final int MAXIMUM_CAPACITY = 1 << 30;

    /**
     * The load factor.
     */
    static final float DEFAULT_LOAD_FACTOR = 0.75f;

    /**
     * The table, resized as necessary. Length MUST Always be a power of two.
     */
    protected E[] table;

    /**
     * The number of key-value mappings contained in this map.
     */
    protected int size = 0;

    /**
     * The next size value at which to resize (capacity * load factor).
     *
     * @serial
     */
    int threshold;

    public MinimalMap() {
	table = createTable(DEFAULT_INITIAL_CAPACITY);
	threshold = (int) (table.length * DEFAULT_LOAD_FACTOR);
    }

    protected abstract E[] createTable(int size);

    /**
     * Retrieves the entry associated with this key using hash code variant implemented in this class.
     *
     * @param key
     * @return the entry
     */
    public E getOrCreate(final K key) {
	return getOrCreate(key, hashCode(key));
    }

    /**
     * Retrieves the entry associated with this key and the provided hash code or creates a and stores new entry based
     * on the key and hash code.
     *
     * @param key
     * @param hashcode
     *            should be consistently calculated with the provided method of this class (or a subtype)
     * @return the entry
     */
    public E getOrCreate(final K key, final int hashCode) {

	final int index = hashIndex(hashCode, table.length);
	E entry = table[index];

	E lastEntry = null;
	while (entry != null) {
	    if (key == entry.getKey()) {
		return entry;
	    }
	    lastEntry = entry;
	    entry = entry.next();
	}
	entry = createEntry(key, hashCode);
	if (lastEntry == null) {
	    table[index] = entry;
	} else {
	    lastEntry.setNext(entry);
	}
	size++;
	ensureCapacity();
	return entry;
    }

    /**
     * Retrieves the entry associated with this key or returns null.
     *
     * @param key
     * @return the entry or null, if entry is not stored in the map
     */
    public E get(final K key) {
	return get(key, hashCode(key));
    }

    /**
     * Retrieves the entry associated with this key and the provided hash code or returns null.
     *
     * @param key
     * @param hashcode
     *            should be consistently calculated with the provided method of this class (or a subtype)
     * @return the entry or null, if entry is not stored in the map
     */
    public E get(final K key, final int hashCode) {

	final int index = hashIndex(hashCode, table.length);
	E entry = table[index];

	while (entry != null) {
	    if (key == entry.getKey()) {
		return entry;
	    }
	    entry = entry.next();
	}
	return null;
    }

    private void ensureCapacity() {

	if (table.length == MAXIMUM_CAPACITY) {
	    return;
	}

	if (size >= threshold) {
	    final int newCapacity = table.length * 2;
	    final E[] oldTable = table;
	    // transfer to new table
	    final E[] newTable = createTable(newCapacity);
	    for (int i = 0; i < oldTable.length; i++) {
		E entry = oldTable[i];
		if (entry != null) {
		    oldTable[i] = null; // help gc
		    do {
			E nextEntry = entry.next();
			int newIndex = hashIndex(entry.hashCode(), newCapacity);
			entry.setNext(newTable[newIndex]);
			newTable[newIndex] = entry;
			entry = nextEntry;
		    } while (entry != null);
		}
	    }
	    table = newTable;
	    threshold = (int) (newCapacity * DEFAULT_LOAD_FACTOR);
	}

    }

    /**
     * Creates a new entry based on the key and hash code.
     *
     * @param key
     * @param hashCode
     * @return the entry
     */
    protected abstract E createEntry(K key, int hashCode);

    public void remove(final K key) {
	remove(key, hashCode(key));
    }

    public void remove(final K key, final int hashCode) {

	final int hashIndex = hashIndex(hashCode, table.length);
	E entry = table[hashIndex];

	E lastEntry = null;
	while (entry != null) {
	    E nextEntry = entry.next();
	    if (key == entry.getKey()) {
		if (lastEntry == null) {
		    table[hashIndex] = nextEntry;
		} else {
		    lastEntry.setNext(nextEntry);
		}
		size--;
	    }
	    lastEntry = entry;
	    entry = nextEntry;
	}
    }

    public boolean removeEntry(final E entryToRemove) {

	final int hashCode = entryToRemove.hashCode();
	final int hashIndex = hashIndex(hashCode, table.length);
	E entry = table[hashIndex];

	E lastEntry = null;
	while (entry != null) {
	    E nextEntry = entry.next();
	    if (entryToRemove == entry) {
		if (lastEntry == null) {
		    table[hashIndex] = nextEntry;
		} else {
		    lastEntry.setNext(nextEntry);
		}
		size--;
		return true;
	    }
	    lastEntry = entry;
	    entry = nextEntry;
	}
	return false;
    }

    /**
     * Calculates the hashcode for the given key, which defaults to a variant of its object identity. Subclasses may
     * implement their own hash code.
     *
     * @param key
     * @return the hash code
     */
    public int hashCode(K key) {
	int h = System.identityHashCode(key);
	// This function ensures that hashCodes that differ only by
	// constant multiples at each bit position have a bounded
	// number of collisions (approximately 8 at default load factor).
	h ^= (h >>> 20) ^ (h >>> 12);
	return h ^ (h >>> 7) ^ (h >>> 4);
    }

    protected int hashIndex(int hashCode, int tableLength) {
	return hashCode & (tableLength - 1);
    }

    public int size() {
	return size;
    }

    public void reset() {
	table = createTable(DEFAULT_INITIAL_CAPACITY);
	threshold = (int) (table.length * DEFAULT_LOAD_FACTOR);
	size = 0;
    }

}
