/*
 * Copyright (c) 2012 Mateusz Parzonka, Eric Bodden
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Eric Bodden - initial API
 * Mateusz Parzonka - adapted API
 */
package prm4j.indexing;


/**
 * An indexing strategy has the single purpose of dispatching a parametric event, represented by instances of the type
 * {@link Event}, to all monitors corresponding to compatible variable bindings.
 *
 * For example, consider an event with a binding {a=a1,b=b1} where a1 and b1 are concrete objects. Also assume that
 * there are monitor instances for the bindings {}, {a=a1}, and {a=a1,b=b2}. The indexing algorithm will dispatch the
 * event to both {}, {a=a1} but not to {a=a1,b=b2} because the latter binding conflicts on variable b: the event binds b
 * to b1 while the monitor binds it to b2. When the event is dispatched to {} and {a=a1}, this will progress these
 * monitors' states and the new states will be stored for the binding {a=a1,b=b1}.
 *
 * Indexing is tricky to get both correct and efficient. Therefore there can be multiple different indexing strategies.
 * However, they all should be equivalent with respect to their externally visible behavior.
 *
 * @param <E>
 *            the type of base event processed by monitors
 */
public interface IndexingStrategy<E> {

    /**
     * Update the internal state of all related monitors by sending the parameter event to the respective monitors.
     *
     * @param symbol
     *            The symbol of the event.
     * @param binding
     *            The variable binding at this event.
     */
    void processEvent(Event<E> event);

}
