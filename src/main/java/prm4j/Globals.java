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

public class Globals {

    public final static boolean DEBUG = isSystemProperty("prm4j.debug", "true");

    /**
     * Compare the value of a system property.
     *
     * @param key
     * @param expectedValue
     * @return <code>true</code> if property value is expected value
     */
    static boolean isSystemProperty(String key, String expectedValue) {
	final String value = System.getProperty(key);
	if (value == null) {
	    return false;
	}
	return value.equals(expectedValue);
    }
}
