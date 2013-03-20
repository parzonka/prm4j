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

import static org.junit.Assert.assertArrayEquals;
import static prm4j.indexing.IndexingUtils.toParameterMask;

import org.junit.Before;
import org.junit.Test;

import prm4j.AbstractTest;
import prm4j.api.Parameter;

@SuppressWarnings("rawtypes")
public class IndexingUtilsTest extends AbstractTest {

    private final static Parameter p0 = new Parameter("p0");
    private final static Parameter p1 = new Parameter("p1");
    private final static Parameter p2 = new Parameter("p2");
    private final static Parameter p3 = new Parameter("p3");
    private final static Parameter p4 = new Parameter("p4");

    @Before
    public void initialize() {
	p0.setIndex(0);
	p1.setIndex(1);
	p2.setIndex(2);
	p3.setIndex(3);
	p4.setIndex(4);
    }

    @Test
    public void toParameterMask_arbitrary() {

	int[] actual = toParameterMask(asSet(p1, p2, p4));

	int[] expected = { 1, 2, 4 };

	assertArrayEquals(expected, actual);
    }

    @Test
    public void toParameterMask_emptyset() {

	int[] actual = toParameterMask(EMPTY_PARAMETER_SET);

	int[] expected = {};

	assertArrayEquals(expected, actual);
    }

}
