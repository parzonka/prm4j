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
package prm4j.indexing.realtime;

import java.lang.ref.ReferenceQueue;

/**
 * Implementation of LowLevelBinding using a linked list as a back reference to its associated hash maps.
 */
public class LinkedListLowLevelBinding extends AbstractLowLevelBinding {

    private Link link;

    public LinkedListLowLevelBinding(Object boundObject, int hashCode, ReferenceQueue<Object> q) {
	super(boundObject, hashCode, q);
    }

    @Override
    public void registerHolder(Holder<LowLevelBinding> bindingHolder) {
	// prepend to the linked list
	link = new Link(bindingHolder, link);
    }

    @Override
    public void release() {
	while (link != null) {
	    link.bindingHolder.release(this);
	    link = link.next;
	}
    }

    class Link {

	final Link next;
	final Holder<LowLevelBinding> bindingHolder;

	Link(Holder<LowLevelBinding> bindingHolder, Link next) {
	    this.bindingHolder = bindingHolder;
	    this.next = next;
	}
    }
}
