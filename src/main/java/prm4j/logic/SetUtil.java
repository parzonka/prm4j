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
package prm4j.logic;

import java.util.Set;
import java.util.HashSet;

public class SetUtil {

    private SetUtil() {
	// not instantiable
    }

    public static <T> Set<T> union(Set<T> set1, Set<T> set2) {
	Set<T> result = new HashSet<T>(set1);
	result.addAll(set2);
	return result;
    }

    public static <T> Set<T> intersection(Set<T> set1, Set<T> set2) {
	Set<T> result = new HashSet<T>();
	for (T e : set1)
	    if (set2.contains(e))
		result.add(e);
	return result;
    }

    public static <T> Set<T> difference(Set<T> set1, Set<T> set2) {
	Set<T> result = new HashSet<T>(set1);
	result.removeAll(set2);
	return result;
    }

    public static <T> boolean isSubset(Set<T> set1, Set<T> set2) {
	return set2.containsAll(set1);
    }

    public static <T> boolean isSuperset(Set<T> set1, Set<T> set2) {
	return set1.containsAll(set2);
    }

}
