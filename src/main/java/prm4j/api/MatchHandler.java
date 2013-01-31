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
package prm4j.api;

public abstract class MatchHandler {

    private static long matchCounter = 0L;

    /**
     * This {@link MatchHandler} does nothing.
     */
    public final static MatchHandler NO_OP = new MatchHandler0() {
	@Override
	public void handleMatch(Object auxiliaryData) {
	    // do nothing
	}
    };

    /**
     * This {@link MatchHandler} prints on match to the standard output stream.
     */
    public final static MatchHandler SYS_OUT = new MatchHandler0() {
	@Override
	public void handleMatch(Object auxiliaryData) {
	    System.out.println("Match detected!");
	}
    };

    /**
     * Retrieve bound objects with getBoundObject(...) TODO doc method.
     * 
     * @param bindings
     */
    public void handleAndCountMatch(Binding[] bindings, Object auxiliaryData) {
	matchCounter++;
	handleMatch(bindings, auxiliaryData);
    }

    public abstract void handleMatch(Binding[] bindings, Object auxiliaryData);

    /**
     * Retrieves the object which was stored in the monitor bindings. The object may be <code>null</code> if selected
     * object was already garbage collected in the monitored application.
     * 
     * @param param
     *            selects the object from the bindings
     * @param bindings
     *            current bindings when the match was completed
     * @return the object which was bound to the given parameter. <code>null</code>, if the object was already garbage
     *         collected.
     */
    @SuppressWarnings("unchecked")
    protected <P> P getBoundObject(Parameter<P> param, Binding[] bindings) {
	return (P) bindings[param.getIndex()];
    }

    public static long getMatchCount() {
	return matchCounter;
    }

    public static void reset() {
	MatchHandler.matchCounter = 0L;
    }
}
