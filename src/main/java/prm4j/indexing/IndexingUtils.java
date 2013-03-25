/*
 * Copyright (c) 2012, 2013 Mateusz Parzonka
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Mateusz Parzonka - initial API and implementation
 */
package prm4j.indexing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import prm4j.Util;
import prm4j.api.BaseEvent;
import prm4j.api.Parameter;
import prm4j.indexing.binding.Binding;

public class IndexingUtils {

    private IndexingUtils() {
	// not to be instantiated
    }

    /**
     * Returns a parameter mask p with p(i) -> j where j is a parameter index.
     * 
     * @param parameterSet
     * @return a parameter mask mapping to the original parameter indices (i.e., uncompressed binding representation)
     */
    public static int[] toParameterMask(Set<Parameter<?>> parameterSet) {
	int[] result = new int[parameterSet.size()];
	int i = 0;
	for (Parameter<?> parameter : parameterSet) {
	    result[i++] = parameter.getIndex();
	}
	Arrays.sort(result);
	return result;
    }

    /**
     * Returns a parameter mask p with p(i) -> j where j is the index in a compressed binding representation.
     * 
     * @param parameterSet
     * @param availableParameters
     *            specifies the parameters available in the compressed binding representation
     * @return a parameter mask mapping to indices in a compressed binding representation
     */
    protected static int[] toMappedParameterMask(Set<Parameter<?>> parameterSet, Set<Parameter<?>> availableParameters) {
	int[] result = new int[parameterSet.size()];
	int i = 0;
	int j = 0;
	// iterate through the available parameters and select by position in the parameter list
	for (Parameter<?> parameter : Util.asSortedList(availableParameters)) {
	    if (parameterSet.contains(parameter)) {
		result[i++] = j;
	    }
	    j++;
	}
	return result;
    }

    /**
     * Returns the result of the map operation over a set of parameters sets with function 'toParameterMask'.
     * 
     * @param collOfParameterSets
     * @return an array of parameter masks mapping to the original parameter indices (i.e., uncompressed binding
     *         representation)
     */
    public static int[][] toParameterMasks(Collection<Set<Parameter<?>>> collOfParameterSets) {
	final int[][] result = new int[collOfParameterSets.size()][];
	int i = 0;
	for (Set<Parameter<?>> parameterSet : collOfParameterSets) {
	    result[i++] = toParameterMask(parameterSet);
	}
	return result;
    }

    /**
     * Returns the result of the map operation over a list of parameters sets with function 'toParameterMask'.
     * 
     * @param collOfParameterSets
     * @return an array of parameter masks mapping to the original parameter indices (i.e., uncompressed binding
     *         representation)
     */
    public static int[][] toParameterMasks(final Collection<Set<Parameter<?>>> collOfParameterSets,
	    final Set<Parameter<?>> availableParameters) {
	final int[][] result = new int[collOfParameterSets.size()][];
	int i = 0;
	for (Set<Parameter<?>> parameterSet : collOfParameterSets) {
	    result[i++] = toMappedParameterMask(parameterSet, availableParameters);
	}
	return result;
    }

    /**
     * Returns a compressed binding representation for the given uncompressed bindings and the parameter mask.
     * 
     * @param uncompressedBindings
     * @param parameterMask
     * @return compressed binding representation associated to the given parameter mask
     */
    public static Binding[] toCompressedBindings(Binding[] uncompressedBindings, int[] parameterMask) {
	Binding[] result = new Binding[parameterMask.length];
	int j = 0;
	for (int i = 0; i < parameterMask.length; i++) {
	    result[j++] = uncompressedBindings[parameterMask[i]];
	}
	return result;
    }

    public static int[] toPrimitiveIntegerArray(List<Integer> listOfInt) {
	int[] result = new int[listOfInt.size()];
	int i = 0;
	for (Integer n : listOfInt) {
	    result[i++] = n;
	}
	return result;
    }

    /**
     * Map over a set of base events with 'getParameters'.
     * 
     * @param setOfBaseEvents
     * @return a set of parameter sets per baseEvent
     */
    public static Set<Set<Parameter<?>>> toParameterSets(final Set<BaseEvent> setOfBaseEvents) {
	final Set<Set<Parameter<?>>> result = new HashSet<Set<Parameter<?>>>();
	for (BaseEvent baseEvent : setOfBaseEvents) {
	    result.add(baseEvent.getParameters());
	}
	return result;
    }

    /**
     * Returns the set of i where i is the index of a parameter in the given parameter set.
     * 
     * @param parameterSet
     * @return the set of parameter indices
     */
    public static Set<Integer> toParameterIndexSet(final Set<Parameter<?>> parameterSet) {
	final Set<Integer> result = new HashSet<Integer>();
	for (Parameter<?> parameter : parameterSet) {
	    result.add(parameter.getIndex());
	}
	return result;
    }

    /**
     * @param collOfParameterSets
     * @return a list of parameter sets in topological ascending order, i.e. small sets first, largest last. If two sets
     *         have the same size, the parameter indices are added and the set with the smallest number is sorted first.
     */
    public static List<Set<Parameter<?>>> toListOfParameterSetsAscending(
	    Collection<Set<Parameter<?>>> collOfParameterSets) {
	List<Set<Parameter<?>>> result = new ArrayList<Set<Parameter<?>>>(collOfParameterSets);
	Collections.sort(result, new Comparator<Set<Parameter<?>>>() {

	    @Override
	    public int compare(Set<Parameter<?>> o1, Set<Parameter<?>> o2) {
		if (o1.size() == o2.size()) {
		    int indexSum1 = 0;
		    for (Parameter<?> parameterSet : o1) {
			indexSum1 += parameterSet.getIndex();
		    }
		    int indexSum2 = 0;
		    for (Parameter<?> parameterSet : o2) {
			indexSum2 += parameterSet.getIndex();
		    }
		    return indexSum1 - indexSum2;

		}
		return o1.size() - o2.size();
	    }

	});

	return result;
    }

}
