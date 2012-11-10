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
package prm4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Util {

    private Util() {
	// not instantiable
    }

    public static Comparator<Set<?>> TOPOLOGICAL_SET_COMPARATOR = new TopologicalSetComparator();
    public static Comparator<Set<?>> REVERSE_TOPOLOGICAL_SET_COMPARATOR = new ReverseTopologicalSetComparator();

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
	return set2.containsAll(set1) && !set1.equals(set2);
    }

    public static <T> boolean isSubsetEq(Set<T> set1, Set<T> set2) {
	return set2.containsAll(set1);
    }

    public static <T> boolean isSuperset(Set<T> set1, Set<T> set2) {
	return set1.containsAll(set2);
    }

    static class TopologicalSetComparator implements Comparator<Set<?>> {
	@Override
	public int compare(Set<?> set1, Set<?> set2) {
	    return set1.size() - set2.size();
	}
    }

    static class ReverseTopologicalSetComparator implements Comparator<Set<?>> {
	@Override
	public int compare(Set<?> set1, Set<?> set2) {
	    return set2.size() - set1.size();
	}
    }

    public static boolean[] toPrimitiveBooleanArray(Collection<Boolean> collection) {
	boolean[] result = new boolean[collection.size()];
	int i = 0;
	for (Boolean b : collection) {
	    result[i++] = b;
	}
	return result;
    }

    public static int[] toPrimitiveIntegerArray(Collection<Integer> collection) {
	int[] result = new int[collection.size()];
	int i = 0;
	for (Integer n : collection) {
	    result[i++] = n;
	}
	return result;
    }

    public static <T extends Comparable<? super T>> List<T> asSortedList(Collection<T> c) {
	List<T> list = new ArrayList<T>(c);
	java.util.Collections.sort(list);
	return list;
    }

    public static <S, T extends S> Set<S> covariantUnmodifiableSet(Set<T> set) {
	return Collections.unmodifiableSet(new HashSet<S>(set));
    }

    public static <T1, T2> Tuple<T1, T2> tuple(T1 left, T2 right) {
	return new Tuple<T1, T2>(left, right);
    }

    public static class Tuple<T1, T2> {

	private final T1 left;
	private final T2 right;

	public Tuple(T1 left, T2 right) {
	    super();
	    this.left = left;
	    this.right = right;
	}

	public T1 getLeft() {
	    return left;
	}

	public T2 getRight() {
	    return right;
	}

	@Override
	public int hashCode() {
	    final int prime = 31;
	    int result = 1;
	    result = prime * result + ((left == null) ? 0 : left.hashCode());
	    result = prime * result + ((right == null) ? 0 : right.hashCode());
	    return result;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean equals(Object obj) {
	    if (this == obj)
		return true;
	    if (obj == null)
		return false;
	    if (getClass() != obj.getClass())
		return false;
	    Tuple other = (Tuple) obj;
	    if (left == null) {
		if (other.left != null)
		    return false;
	    } else if (!left.equals(other.left))
		return false;
	    if (right == null) {
		if (other.right != null)
		    return false;
	    } else if (!right.equals(other.right))
		return false;
	    return true;
	}

	@Override
	public String toString() {
	    return "(" + left + ", " + right + ")";
	}

    }

}
