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

import java.util.Map;

public class JDKMapEntry<K, V> {
	final K key;
	V value;
	JDKMapEntry<K, V> next;
	final int hash;

	/**
	 * Creates new entry.
	 */
	JDKMapEntry(int h, K k, V v, JDKMapEntry<K, V> n) {
	    value = v;
	    next = n;
	    key = k;
	    hash = h;
	}

	public final K getKey() {
	    return key;
	}

	public final V getValue() {
	    return value;
	}

	public final V setValue(V newValue) {
	    V oldValue = value;
	    value = newValue;
	    return oldValue;
	}

	@Override
	public final boolean equals(Object o) {
	    if (!(o instanceof Map.Entry))
		return false;
	    Map.Entry e = (Map.Entry) o;
	    Object k1 = getKey();
	    Object k2 = e.getKey();
	    if (k1 == k2 || (k1 != null && k1.equals(k2))) {
		Object v1 = getValue();
		Object v2 = e.getValue();
		if (v1 == v2 || (v1 != null && v1.equals(v2)))
		    return true;
	    }
	    return false;
	}

	@Override
	public final int hashCode() {
	    return (key == null ? 0 : key.hashCode()) ^ (value == null ? 0 : value.hashCode());
	}

	@Override
	public final String toString() {
	    return getKey() + "=" + getValue();
	}

	/**
	 * This method is invoked whenever the value in an entry is overwritten by an invocation of put(k,v) for a key k
	 * that's already in the MiniMap.
	 */
	void recordAccess(JDKHashMap<K, V> m) {
	}

	/**
	 * This method is invoked whenever the entry is removed from the table.
	 */
	void recordRemoval(JDKHashMap<K, V> m) {
	}
  }
