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

import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import prm4j.api.Event;
import prm4j.spec.Spec;

public class AwareUnaryParametricMonitor extends UnaryParametricMonitor {

    private final Logger logger = getFileLogger("logs/Unary_memory.log");
    private final Logger stats = getFileLogger("logs/Unary_stats.log");

    public AwareUnaryParametricMonitor(Spec spec) {
	super(spec);
    }

    @Override
    public synchronized void processEvent(Event event) {
	super.processEvent(event);
	if (timestamp % 100 == 0) {
	    logger.log(Level.INFO, timestamp
		    + " : "
		    + (((double) (Runtime.getRuntime().totalMemory() / 1024) / 1024) - ((double) (Runtime.getRuntime()
			    .freeMemory() / 1024) / 1024)));
	}
    }

    /**
     * A simple file logger which outputs only the message.
     *
     * @param fileName
     *            path to the output file
     * @return the logger
     */
    private static Logger getFileLogger(String fileName) {
	final Logger logger = Logger.getLogger(fileName);
	try {
	    logger.setUseParentHandlers(false);
	    Handler handler = new FileHandler(fileName, true);
	    handler.setFormatter(new Formatter() {
		@Override
		public String format(LogRecord record) {
		    return record.getMessage() + "\n";
		}
	    });
	    logger.addHandler(handler);
	} catch (Exception e) {
	    throw new RuntimeException(e);
	}
	return logger;
    }

    @Override
    public void reset() {
	stats.log(Level.INFO, "Created bindings: " + bindingStore.getCreatedBindingsCount());
	stats.log(Level.INFO, "Collected bindings: " + bindingStore.getCollectedBindingsCount());
	stats.log(Level.INFO, "Stored bindings: " + bindingStore.size());
	super.reset();
    }

}
