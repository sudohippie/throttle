package me.sudohippie.throttle;

import me.sudohippie.throttle.strategy.ThrottleStrategy;

/**
 * Bridge to enable throttling.
 *
 * User must specify his desired throttling strategy.
 *
 * Raghav Sidhanti
 * 9/25/13
 */
public class Throttle {

    private final ThrottleStrategy strategy;

    public Throttle(ThrottleStrategy strategy) {
        this.strategy = strategy;
    }

    /**
     * Based on the throttling strategy chosen, this method
     * will return <i>true</i> if the request can be serviced.
     * <i>false</i> is returned when the return can not be serviced.
     *
     * @return
     */
    public boolean canProceed(){
        return !strategy.isThrottled();
    }
}
