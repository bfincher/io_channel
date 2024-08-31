package com.fincher.iochannel;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Various utilities */
public class Utilities {

    private static Utilities instance = new Utilities();

    private static final Clock clock = Clock.systemDefaultZone();

    /**
     * Get the singleton utilities instance
     * 
     * @return The singleton instance
     */
    public static Utilities getInstance() {
        return instance;
    }

    /**
     * Set a instance of Utilities for testing purposes
     * 
     * @param testInstance The test instance
     */
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

    /**
     * Sleep for the given duration
     * 
     * @param synchronizer The object to synchronize on for the sleep (wait)
     * @param duration The duration to sleep
     * @throws InterruptedException If interrupted while sleeping
     */
    public static void sleep(final Object synchronizer, Duration duration) throws InterruptedException {
        Instant sleepUntil = clock.instant().plus(duration);
        synchronized (synchronizer) { // NOSONAR
            Duration sleepTime = Duration.between(Instant.now(), sleepUntil);
            while (sleepTime.isNegative() || sleepTime.isZero()) {
                synchronizer.wait(sleepTime.toSeconds(), sleepTime.getNano());
                sleepTime = Duration.between(Instant.now(), sleepUntil);
            }
        }
    }

}
