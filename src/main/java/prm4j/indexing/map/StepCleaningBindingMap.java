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

import java.lang.ref.ReferenceQueue;

import prm4j.indexing.realtime.BindingStore;
import prm4j.indexing.realtime.LowLevelBinding;

/**
 * BindingMap to be used with a {@link BindingStore} which does not depend on garbage collection by
 * {@link ReferenceQueue}. Performs small cleaning steps on each operation performed on the map. Performs a complete
 * cleaning on each resize of the inner table.
 */
public abstract class StepCleaningBindingMap extends MinimalMap<Object, LowLevelBinding> {

    private int cleaningIndex = 0;

    /**
     * Performs a cleaning step, i.e. finds an existing linked list in the map, checks all bindings if they are alive
     * and
     */
    protected void clean() {
	LowLevelBinding entry = null;
	while (entry == null) {
	    cleaningIndex = cleaningIndex >= table.length ? 0 : cleaningIndex + 1;
	    entry = table[cleaningIndex];
	}
	LowLevelBinding lastEntry = null;
	while (entry != null) {
	    final LowLevelBinding nextEntry = entry.next();
	    if (entry.get() == null) {
		if (lastEntry == null) {
		    table[cleaningIndex] = nextEntry;
		} else {
		    lastEntry.setNext(nextEntry);
		}
		entry.release();
		size--;
	    }
	    lastEntry = entry;
	    entry = nextEntry;
	}
    }

    @Override
    protected void ensureCapacity() {
	clean();
	if (table.length == MAXIMUM_CAPACITY) {
	    return;
	}

	if (size >= threshold) {
	    final int newCapacity = table.length * 2;
	    final LowLevelBinding[] oldTable = table;
	    // transfer to new table
	    final LowLevelBinding[] newTable = createTable(newCapacity);
	    for (int i = 0; i < oldTable.length; i++) {
		LowLevelBinding entry = oldTable[i];
		if (entry != null) {
		    oldTable[i] = null; // help gc
		    do {
			final LowLevelBinding nextEntry = entry.next();
			// check for expired bindings
			if (entry.get() != null) {
			    final int newIndex = hashIndex(entry.hashCode(), newCapacity);
			    entry.setNext(newTable[newIndex]);
			    newTable[newIndex] = entry;
			} else {
			    entry.release();
			}
			entry = nextEntry;
		    } while (entry != null);
		}
	    }
	    table = newTable;
	    threshold = (int) (newCapacity * DEFAULT_LOAD_FACTOR);
	}
    }

    /**
     * Currently unused.
     */
    protected void cleanIterate() {
	// remove expired bindings
	if (size >= threshold) {
	    for (int i = 0; i < table.length; i++) {
		LowLevelBinding entry = table[i];
		LowLevelBinding lastEntry = null;
		while (entry != null) {
		    final LowLevelBinding nextEntry = entry.next();
		    if (entry.get() == null) {
			if (lastEntry == null) {
			    table[i] = nextEntry;
			} else {
			    lastEntry.setNext(nextEntry);
			}
			entry.release();
			size--;
		    }
		    lastEntry = entry;
		    entry = nextEntry;
		}
	    }
	}
    }

    @Override
    public LowLevelBinding getOrCreate(Object key) {
	clean();
	return super.getOrCreate(key);
    }

    @Override
    public LowLevelBinding getOrCreate(Object key, int hashCode) {
	clean();
	return super.getOrCreate(key, hashCode);
    }

    @Override
    public LowLevelBinding get(Object key) {
	clean();
	return super.get(key);
    }

    @Override
    public LowLevelBinding get(Object key, int hashCode) {
	clean();
	return super.get(key, hashCode);
    }

    @Override
    public void remove(Object key) {
	clean();
	super.remove(key);
    }

    @Override
    public void remove(Object key, int hashCode) {
	clean();
	super.remove(key, hashCode);
    }

    @Override
    public boolean removeEntry(LowLevelBinding entryToRemove) {
	clean();
	return super.removeEntry(entryToRemove);
    }
}
