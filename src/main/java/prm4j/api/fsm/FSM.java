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
package prm4j.api.fsm;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import prm4j.api.Alphabet;
import prm4j.api.MonitorState;
import prm4j.api.MatchHandler;

/**
 * A finite state automaton.
 *
 * @param <A>
 *            the type of the auxiliary data usable by base monitors
 */
public class FSM<A> {

    private final Alphabet alphabet;
    private final Set<FSMState<A>> states;
    private final Set<String> usedNames;
    private int stateCount = 0;
    private MonitorState<A> initialState;

    public FSM(Alphabet alphabet) {
	this.alphabet = alphabet;
	this.states = new HashSet<FSMState<A>>();
	this.usedNames = new HashSet<String>();
    }

    /**
     * Create a new state which is labeled with the name <code>"initial"</code>.
     *
     * @return the created state
     */
    public MonitorState<A> createInitialState() {
	if (this.initialState != null)
	    throw new IllegalStateException("Initial state already created!");
	this.initialState = createState("initial");
	return this.initialState;
    }

    /**
     * Create a new state which is labeled with a generated name of the form <code>"state_NUMBER"</code>.
     *
     * @return the created state
     */
    public MonitorState<A> createState() {
	return createState(generateStateName());
    }

    /**
     * Create a new state labeled with the given optional name.
     *
     * @return the created state
     */
    public MonitorState<A> createState(String optionalName) {
	return createState(false, null, optionalName);
    }

    /**
     * Create a new final state which is labeled with a generated name of the form <code>"state_NUMBER"</code>.
     *
     * @return the created final state
     */
    public MonitorState<A> createFinalState(MatchHandler matchHandler) {
	return createFinalState(matchHandler, generateFinalStateName());
    }

    /**
     * Create a new final state labeled with the given optional name.
     *
     * @return the created state
     */
    public MonitorState<A> createFinalState(MatchHandler matchHandler, String optionalName) {
	if (matchHandler == null) {
	    throw new NullPointerException("MatchHandler may not be null!");
	}
	return createState(true, matchHandler, optionalName);
    }

    private String generateStateName() {
	return "state " + this.stateCount;
    }

    private String generateFinalStateName() {
	return "state " + this.stateCount + " (final)";
    }

    private MonitorState<A> createState(boolean isFinal, MatchHandler eventHandler, String name) {
	if (this.usedNames.contains(name))
	    throw new IllegalArgumentException("The name [" + name + "] has already been used!");
	this.usedNames.add(name);
	FSMState<A> state = new FSMState<A>(this.alphabet, false, eventHandler, name);
	this.states.add(state);
	this.stateCount++;
	return state;
    }

    /**
     * Returns the underlying alphabet for this FSM.
     *
     * @return the alphabet
     */
    public Alphabet getAlphabet() {
	return this.alphabet;
    }

    /**
     * Returns all created states.
     *
     * @return an unmodifiable set of created states
     */
    public Set<FSMState<A>> getStates() {
	return this.states;
    }

    /**
     * Returns the number of created states.
     *
     * @return the number of created states
     */
    public int getStateCount() {
	return this.stateCount;
    }

    /**
     * Returns the names which where used for the states.
     *
     * @return an unmodifiable set of the used names
     */
    public Set<String> getUsedNames() {
	return Collections.unmodifiableSet(this.usedNames);
    }

    /**
     * Returns the initial state.
     *
     * @return the initial state
     */
    public MonitorState<A> getInitialState() {
	if (this.initialState == null)
	    throw new IllegalStateException("No initial state created!");
	return this.initialState;
    }

}
