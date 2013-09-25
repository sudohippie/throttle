package me.sudohippie.throttle.strategy;

/**
 * Represents a throttling strategy.
 *
 * Raghav Sidhanti
 * 9/25/13
 */
public abstract class ThrottleStrategy {

    /**
     * This method will return <i>true</i> if the request
     * has been throttled (rate-limited) or <i>false</i> if
     * the request is not throttled (not rate-limited).
     *
     * @return
     */
    public abstract boolean isThrottled();
}
