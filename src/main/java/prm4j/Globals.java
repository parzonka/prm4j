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
package prm4j;

import prm4j.indexing.realtime.LowLevelBinding;

/**
 * Encapsulates configuration via Java System Properties.
 */
public class Globals {

    /**
     * Controls configuration printing on application start.
     */
    public static final boolean PRINT_CONFIGURATION = true;

    public final static boolean LOGGING = isSystemProperty("prm4j.logging", "true");

    public static final int MONITOR_CLEANING_INTERVAL = Integer.parseInt(getSystemProperty(
	    "prm4j.monitorCleaningInterval", "10000"));

    /**
     * Specifies the number of retrieve-operations after which the store will try to clean expired bindings. It will
     * poll the a reference queue and remove the bindings from the store, triggering their resource removal.
     */
    public final static int BINDING_CLEANING_INTERVAL = Integer.parseInt(getSystemProperty(
	    "prm4j.bindingCleaningInterval", "10000"));

    public final static boolean CHECK_MONITOR_VALIDITY_ON_EACH_UPDATE = getBooleanSystemProperty(
	    "prm4j.checkMonitorValidityOnEachUpdate", true);

    /**
     * Specifies the method how backlinks in {@link LowLevelBinding}s are stored. <code>true</code> (default) for
     * linked-link based storage, <code>false</code> for array based.
     */
    public final static boolean LINKEDLIST_STORED_BACKLINKS = getBooleanSystemProperty(
	    "prm4j.linkedListStoredBacklinks", false);

    /**
     * Compare the value of a system property.
     * 
     * @param key
     * @param expectedValue
     * @return <code>true</code> if property value is expected value
     */
    static boolean isSystemProperty(String key, String expectedValue) {
	final String value = System.getProperty(key);
	final boolean result = value != null ? expectedValue.equals(value) : false;
	if (PRINT_CONFIGURATION) {
	    System.out.println("[prm4j.config] " + key + "=" + result);
	}
	return result;
    }

    /**
     * Return the value of a system property or a default value if undefined.
     * 
     * @param key
     * @param defaultValue
     * @return defined system property or default
     */
    static String getSystemProperty(String key, String defaultValue) {
	final String value = System.getProperty(key);
	final String result = value != null ? value : defaultValue;
	if (PRINT_CONFIGURATION) {
	    System.out.println("[prm4j.config] " + key + "=" + result);
	}
	return result;
    }

    /**
     * Return the value of a system property or a default value if undefined.
     * 
     * @param key
     * @param defaultValue
     * @return defined system property or default
     */
    static boolean getBooleanSystemProperty(String key, boolean expectedValue) {
	final String value = System.getProperty(key);
	final boolean result = value != null ? Boolean.parseBoolean(value) : expectedValue;
	if (PRINT_CONFIGURATION) {
	    System.out.println("[prm4j.config] " + key + "=" + result);
	}
	return result;
    }

}
