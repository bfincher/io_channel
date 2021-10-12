package com.fincher.iochannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Various utilities */
public class Utilities {

	private static Utilities instance = new Utilities();

	public static Utilities getInstance() {
		return instance;
	}

	/**
	 * Set a instance of Utilities for testing purposes
	 * 
	 * @param testInstance The test instance
	 */
	@Deprecated
	public static void setInstanceForTesting(Utilities testInstance) {
		instance = testInstance;
	}

	Utilities() {
	}

	/**
	 * Get a slf4j Logger for the given class
	 * 
	 * @param cls The class for which the logger should be retrieved
	 * @return a slf4j Logger
	 */
	public Logger getLogger(Class<?> cls) {
		return LoggerFactory.getLogger(cls);
	}

}
