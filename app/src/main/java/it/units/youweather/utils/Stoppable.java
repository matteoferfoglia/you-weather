package it.units.youweather.utils;

/**
 * Interface for an object that can be stopped
 * (e.g., for cyclic operations).
 */
public interface Stoppable {
    /**
     * Stops the object.
     *
     * @return true on success, false on failure.
     */
    boolean stop();
}
