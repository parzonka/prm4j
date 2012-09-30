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
package rm4j.api;

import java.util.HashSet;
import java.util.Set;

/**
 * TODO implement auto-naming.
 */
public class Alphabet {

    private int symbolCount = 0;
    private int parameterCount = 0;
    private final Set<Parameter<?>> parameters;
    private final Set<Symbol> symbols;

    public Alphabet() {
	super();
	parameters = new HashSet<Parameter<?>>();
	symbols = new HashSet<Symbol>();
    }

    /**
     * Creates a parameter of type {@link Object}.
     *
     * @return the parameter
     */
    public Parameter<Object> createParameter() {
	Parameter<Object> parameter = new Parameter<Object>("TODO");
	parameters.add(parameter);
	parameterCount++;
	return parameter;
    }

    /**
     * Creates a parameter of given object type.
     *
     * @param parameterObjectType
     * @return the parameter
     */
    public <P> Parameter<P> createParameter(Class<P> parameterObjectType) {
	Parameter<P> parameter = new Parameter<P>("TODO");
	parameters.add(parameter);
	parameterCount++;
	return parameter;
    }

    public <P> Parameter<P> createParameter(String optionalName, Class<P> parameterObjectType) {
	Parameter<P> parameter = new Parameter<P>(optionalName);
	parameters.add(parameter);
	parameterCount++;
	return parameter;
    }

    /**
     * Creates a symbol associated with one parameter.
     *
     * @param param1
     * @return the symbol
     */
    public <P1> Symbol1<P1> createSymbol1(Parameter<P1> param1) {
	Symbol1<P1> symbol = new Symbol1<P1>(this, symbolCount, "Symbol_" + symbolCount, param1);
	symbols.add(symbol);
	symbolCount++;
	return symbol;
    }

    /**
     * Creates a symbol associated with one parameter providing an optional name.
     *
     * @param optionalName
     * @param param1
     * @return the symbol
     */
    public <P1> Symbol1<P1> createSymbol1(String optionalName, Parameter<P1> param1) {
	Symbol1<P1> symbol = new Symbol1<P1>(this, symbolCount, optionalName, param1);
	symbols.add(symbol);
	symbolCount++;
	return symbol;
    }

    /**
     * Creates a symbol associated with two parameters.
     *
     * @param param1
     * @param param2
     * @return the symbol
     */
    public <P1, P2> Symbol2<P1, P2> createSymbol2(Parameter<P1> param1, Parameter<P2> param2) {
	Symbol2<P1, P2> symbol = new Symbol2<P1, P2>(this, symbolCount, "Symbol_" + symbolCount, param1, param2);
	symbols.add(symbol);
	symbolCount++;
	return symbol;
    }

    /**
     * Creates a symbol associated with two parameter providing an optional name.
     *
     * @param optionalName
     * @param param1
     * @param param2
     * @return the symbol
     */
    public <P1, P2> Symbol2<P1, P2> createSymbol2(String optionalName, Parameter<P1> param1, Parameter<P2> param2) {
	Symbol2<P1, P2> symbol = new Symbol2<P1, P2>(this, symbolCount, optionalName, param1, param2);
	symbols.add(symbol);
	symbolCount++;
	return symbol;
    }

    public Set<Symbol> getSymbols() {
	return symbols;
    }

    public int size() {
	return symbolCount;
    }

    public int getParameterCount() {
	return parameterCount;
    }

}
