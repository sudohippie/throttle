package me.sudohippie.throttle;

import me.sudohippie.throttle.strategy.ThrottleStrategy;

import java.util.concurrent.TimeUnit;

/**
 * Bridge to enable throttling.
 *
 * User must specify his/her desired throttling strategy.
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

	/**
	 * If the request can be serviced, the wait time will be 0 else a
	 * positive value in the chosen {@code TimeUnit}.
	 * @param timeUnit TimeUnit indicating the wait time.
	 * @return 0 or more value in the time unit chosen
	 */
	public long waitTime(TimeUnit timeUnit){
		return strategy.timeToRelease(1,timeUnit);
	}

}
