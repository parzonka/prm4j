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
package prm4j.indexing.realtime;

import java.lang.ref.ReferenceQueue;

public interface BindingFactory {

    public LowLevelBinding[] createTable(int size);

    public LowLevelBinding createBinding(Object boundObject, int hashCode, ReferenceQueue<Object> referenceQueue,
	    int fullParameterCount);

}
