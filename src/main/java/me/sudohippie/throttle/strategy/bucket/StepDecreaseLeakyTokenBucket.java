package me.sudohippie.throttle.strategy.bucket;

import java.util.concurrent.TimeUnit;

/**
 * StepDecreaseLeakyTokenBucket is concrete representation of {@link LeakyTokenBucket}.
 *
 * This strategy is synonymous to a leaking bucket filled with some substance (in this case tokens). Here,
 * at the beginning of every refill interval, the bucket is filled to capacity with tokens. Over time,
 * the bucket leaks tokens at a constant rate as defined by input parameters stepTokens and stepInterval until
 * the start of the next interval.
 *
 * Raghav Sidhanti
 * 9/25/13
 */
public class StepDecreaseLeakyTokenBucket extends LeakyTokenBucket {

    /**
     * Constructor to build a StepDecreaseLeakyTokenBucket.
     *
     * @param maxTokens The maximum tokens this bucket can hold.
     * @param refillInterval The interval at which the bucket must be refilled to capacity with tokens.
     * @param refillIntervalTimeUnit {@link TimeUnit} class representing unit of time of refill interval
     * @param stepTokens The number of tokens this bucket leaks, at every step interval.
     * @param stepInterval The interval at which the token leaks tokens.
     * @param stepIntervalTimeUnit {@link TimeUnit} class representing unit of time of step interval
     */
    protected StepDecreaseLeakyTokenBucket(long maxTokens, long refillInterval, TimeUnit refillIntervalTimeUnit, long stepTokens, long stepInterval, TimeUnit stepIntervalTimeUnit) {
        super(maxTokens, refillInterval, refillIntervalTimeUnit, stepTokens, stepInterval, stepIntervalTimeUnit);
    }

    @Override
    protected void updateTokens() {
        long currentTime = System.currentTimeMillis();

        // if current time exceeds next refill time
        if(currentTime >= nextRefillTime){
            // set tokens to max
            tokens = maxTokens;
            // calculate next refill time
            nextRefillTime = currentTime + refillInterval;

            return;
        }
        // calculate max tokens possible till end
        long timeToNextRefill = nextRefillTime - currentTime;
        long stepsToNextRefill = timeToNextRefill / stepInterval;
        long maxPossibleTokens = stepsToNextRefill * stepTokens;
        // edge case, if current time not at edge of step
        if((timeToNextRefill % stepInterval) > 0) maxPossibleTokens += stepTokens;
        // tokens must be lesser of current and max possible tokens
        if(maxPossibleTokens < tokens) tokens = maxPossibleTokens;
    }
}
