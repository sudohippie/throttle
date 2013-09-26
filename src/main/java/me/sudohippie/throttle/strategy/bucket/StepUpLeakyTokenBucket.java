package me.sudohippie.throttle.strategy.bucket;

import java.util.concurrent.TimeUnit;

/**
 * StepUpLeakyTokenBucket is concrete representation of {@link LeakyTokenBucket}.
 *
 * This strategy is synonymous to an empty bucket being filled with some substance (here tokens) over time.
 * Here, at the beginning of every refill interval the bucket is emptied. The bucket is then gradually filled
 * with tokens and rate is defined by the input parameters stepTokens and stepInterval.
 *
 * Raghav Sidhanti
 * 9/25/13
 */
public class StepUpLeakyTokenBucket extends LeakyTokenBucket {

    private long lastActivityTime;

    /**
     * Constructor to build a StepUpLeakyTokenBucket.
     *
     * @param maxTokens The maximum tokens this bucket can hold.
     * @param refillInterval The interval at which the bucket must be emptied.
     * @param refillIntervalTimeUnit {@link TimeUnit} class representing unit of time of refill interval
     * @param stepTokens The number of tokens added to the bucket at every step interval.
     * @param stepInterval The interval at which tokens are added.
     * @param stepIntervalTimeUnit {@link TimeUnit} class representing unit of time of step interval
     */
    public StepUpLeakyTokenBucket(int maxTokens, long refillInterval, TimeUnit refillIntervalTimeUnit, int stepTokens, long stepInterval, TimeUnit stepIntervalTimeUnit) {
        super(maxTokens, refillInterval, refillIntervalTimeUnit, stepTokens, stepInterval, stepIntervalTimeUnit);
    }

    @Override
    protected void updateTokens() {
        long currentTime = System.currentTimeMillis();

        // if current time has exceeded next refill interval,
        if(currentTime >= nextRefillTime){
            tokens = stepTokens;
            lastActivityTime = currentTime;
            nextRefillTime = currentTime + refillInterval;

            return;
        }

        // calculate tokens at current step
        long elapsedTimeSinceLastActivity = currentTime - lastActivityTime;
        long elapsedStepsSinceLastActivity = elapsedTimeSinceLastActivity / stepInterval;
        tokens += (elapsedStepsSinceLastActivity * stepTokens);
        // edge case, if at the beginning of a new step then add tokens
        if((elapsedTimeSinceLastActivity % stepInterval) == 0) tokens += stepTokens;
        // check for bucket overflow
        if(tokens > maxTokens) tokens = maxTokens;

        // update last activity time
        lastActivityTime = currentTime;
    }
}
