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
import prm4j.api.MatchHandler;
import prm4j.logic.MonitorStateProvider;

/**
 * A finite state automaton.
 */
public class FSM implements MonitorStateProvider {

    private final Alphabet alphabet;
    private final Set<FSMState> states;
    private final Set<String> usedNames;
    private int stateCount = 0;
    private FSMState initialState;

    public FSM(Alphabet alphabet) {
	this.alphabet = alphabet;
	states = new HashSet<FSMState>();
	usedNames = new HashSet<String>();
    }

    /**
     * Create a new state which is labeled with the name <code>"initial"</code>.
     *
     * @return the created state
     */
    public FSMState createInitialState() {
	if (initialState != null)
	    throw new IllegalStateException("Initial state already created!");
	initialState = createState("initial");
	return initialState;
    }

    /**
     * Create a new state which is labeled with a generated name of the form <code>"state_NUMBER"</code>.
     *
     * @return the created state
     */
    public FSMState createState() {
	return createState(generateStateName());
    }

    /**
     * Create a new state labeled with the given optional name.
     *
     * @return the created state
     */
    public FSMState createState(String optionalName) {
	return createState(false, null, optionalName);
    }

    /**
     * Create a new final state which is labeled with a generated name of the form <code>"state_NUMBER"</code>.
     *
     * @return the created final state
     */
    public FSMState createFinalState(MatchHandler matchHandler) {
	return createFinalState(matchHandler, generateFinalStateName());
    }

    /**
     * Create a new final state labeled with the given optional name.
     *
     * @return the created state
     */
    public FSMState createFinalState(MatchHandler matchHandler, String optionalName) {
	if (matchHandler == null) {
	    throw new NullPointerException("MatchHandler may not be null!");
	}
	return createState(true, matchHandler, optionalName);
    }

    private String generateStateName() {
	return "state " + stateCount;
    }

    private String generateFinalStateName() {
	return "state " + stateCount + " (final)";
    }

    private FSMState createState(boolean isFinal, MatchHandler eventHandler, String name) {
	if (usedNames.contains(name))
	    throw new IllegalArgumentException("The name [" + name + "] has already been used!");
	usedNames.add(name);
	FSMState state = new FSMState(alphabet, false, eventHandler, name);
	states.add(state);
	stateCount++;
	return state;
    }

    /**
     * Returns the underlying alphabet for this FSM.
     *
     * @return the alphabet
     */
    public Alphabet getAlphabet() {
	return alphabet;
    }

    /**
     * Returns all created states.
     *
     * @return an unmodifiable set of created states
     */
    public Set<FSMState> getStates() {
	return states;
    }

    /**
     * Returns the number of created states.
     *
     * @return the number of created states
     */
    public int getStateCount() {
	return stateCount;
    }

    /**
     * Returns the names which where used for the states.
     *
     * @return an unmodifiable set of the used names
     */
    public Set<String> getUsedNames() {
	return Collections.unmodifiableSet(usedNames);
    }

    /**
     * Returns the initial state.
     *
     * @return the initial state
     */
    @Override
    public FSMState getInitialState() {
	if (initialState == null)
	    throw new IllegalStateException("No initial state created!");
	return initialState;
    }

}
