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

    /**
     * This {@link MatchHandler} does nothing.
     */
    public final static MatchHandler NO_OP = new MatchHandler0() {
	@Override
	public void handleMatch() {
	}
    };

    /**
     * Retrieve bound objects with getBoundObject(...) TODO doc method.
     *
     * @param bindings
     */
    public abstract void handleMatch(Binding[] bindings);

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
    protected <P> P getBoundObject(Parameter<P> param, Binding[] bindings) {
	// matches are rare, so we simply search the bindings linearly
	for (Binding binding : bindings) {
	    if (binding != null && binding.getParameterId() == param.getIndex()) {
		@SuppressWarnings("unchecked")
		P boundObject = (P) binding.get();
		return boundObject;
	    }
	}
	throw new IllegalArgumentException("Bound object for parameter ["
		+ "] coult not be retrieved from the bindings!");
    }

}
