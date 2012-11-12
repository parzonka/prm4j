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
 * This hash map stores its values directly (as entries themselves), omitting the creation of wrapping map entries. This
 * is useful, when the values store the keys of the map anyway and may be aware of the {@link MinimalMap}. Values
 * implement the {@link MinimalMapEntry}-interface to provide access to the key.
 *
 * @param <E>
 *            the type of the value which is also used as map entry
 */
public class MinimalMap<E extends MinimalMapEntry<E>> {

    protected E[] entries;

    /**
     * Retrieves the value associated with this key using hash code variant implemented in this class.
     *
     * @param key
     * @return
     */
    public E get(final Object key) {
	return get(key, hashCode(key));
    }

    /**
     * Retrieves the value associated with this key provided hash code.
     *
     * @param key
     * @param hashcode
     *            should be consistently calculated with the provided method of this class (or a subtype)
     * @return
     */
    public E get(final Object key, final int hashCode) {

	final int hashIndex = hashIndex(hashCode);
	E entry = entries[hashIndex];

	while (entry != null) {
	    if (key == entry.getKey()) {
		return entry;
	    }
	    entry = entry.next();
	}
	return null;
    }

    /**
     * Calculates the hashcode for the given key, which defaults to a variant of its object identity. Subclasses may
     * implement their own hash code.
     *
     * @param key
     * @return the hash code
     */
    public int hashCode(Object key) {
	int h = System.identityHashCode(key);
	// This function ensures that hashCodes that differ only by
	// constant multiples at each bit position have a bounded
	// number of collisions (approximately 8 at default load factor).
	h ^= (h >>> 20) ^ (h >>> 12);
	return h ^ (h >>> 7) ^ (h >>> 4);
    }

    protected int hashIndex(int hashCode) {
	return hashCode & (entries.length - 1);
    }

}
